/*
* Copyright (c) 2004 UNINETT FAS
*
* This program is free software; you can redistribute it and/or modify it
* under the terms of the GNU General Public License as published by the Free
* Software Foundation; either version 2 of the License, or (at your option)
* any later version.
*
* This program is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
* FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
* more details.
*
* You should have received a copy of the GNU General Public License along with
* this program; if not, write to the Free Software Foundation, Inc., 59 Temple
* Place - Suite 330, Boston, MA 02111-1307, USA.
*
* $Id$
*/

package no.feide.moria.controller;

import no.feide.moria.authorization.AuthorizationManager;
import no.feide.moria.authorization.UnknownAttributeException;
import no.feide.moria.authorization.UnknownServicePrincipalException;
import no.feide.moria.configuration.ConfigurationManager;
import no.feide.moria.configuration.ConfigurationManagerException;
import no.feide.moria.directory.Credentials;
import no.feide.moria.directory.DirectoryManager;
import no.feide.moria.directory.backend.AuthenticationFailedException;
import no.feide.moria.directory.backend.BackendException;
import no.feide.moria.log.AccessLogger;
import no.feide.moria.log.AccessStatusType;
import no.feide.moria.log.MessageLogger;
import no.feide.moria.store.InvalidTicketException;
import no.feide.moria.store.MoriaAuthnAttempt;
import no.feide.moria.store.MoriaStore;
import no.feide.moria.store.MoriaStoreException;
import no.feide.moria.store.MoriaStoreFactory;
import no.feide.moria.store.NonExistentTicketException;

import javax.servlet.ServletContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.HashSet;
import java.util.Iterator;

/**
 * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
 * @version $Revision$
 */
public class MoriaController {

    /**
     * Ticket type constant, indicating a SSO ticket, for use when returning a HashMap of two tickets.
     *
     * @see MoriaController#attemptLogin(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     * @see MoriaController#attemptSingleSignOn(java.lang.String, java.lang.String)
     */
    public static String SSO_TICKET = "sso";

    /**
     * Ticket type constant, indicating a login ticket, for use when returning a HashMap with multiple tickets.
     *
     * @see MoriaController#attemptLogin(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     * @see MoriaController#attemptSingleSignOn(java.lang.String, java.lang.String)
     */
    public static String SERVICE_TICKET = "service";

    /**
     * Operation type for local authentication.
     */
    private static String DIRECT_AUTH_OPER = "DirectAuth";

    /**
     * Operation type for interactive authentication.
     */
    private static String INTERACTIVE_AUTH_OPER = "InteractiveAuth";

    /**
     * Operation type for interactive authentication.
     */
    private static String PROXY_AUTH_OPER = "ProxyAuth";

    /**
     * Operation type for verify user existence.
     */
    private static String VERIFY_USER_EXISTENCE_OPER = "VerifyUserExistence";

    /**
     * Identifier for the TGT used in attribute requests
     */
    static String TGT_IDENTIFIER = "tgt";

    /**
     * The single instance of the data store.
     */
    private static MoriaStore store;

    /**
     * The single instance of the configuration manager
     */
    private static ConfigurationManager configManager;

    /**
     * The single instance of the authorization manager.
     */
    private static AuthorizationManager authzManager;

    /**
     * The single instance of the directory manager.
     */
    private static DirectoryManager directoryManager;

    /**
     * Flag set to true if the controller has been initialized.
     */
    private static Boolean isInitialized = new Boolean(false);

    /**
     * Flag set to true if the controller and all modules are ready.
     */
    private static boolean ready = false;

    /**
     * Flag set to true if the authorization manager is ready.
     */
    private static boolean amReady = false;

    /**
     * Flag set to true if the directory manager is ready.
     */
    private static boolean dmReady = false;

    /**
     * Flag set to true if the store manager is ready.
     */
    private static boolean smReady = false;

    /**
     * The servlet context for the servlets using the controller.
     */
    private static ServletContext servletContext;

    /**
     * Used for access logging.
     */
    private static AccessLogger accessLogger;

    /**
     * Used for message/error logging.
     */
    private static MessageLogger messageLogger;

    synchronized static void init() {
        synchronized (isInitialized) {
            // TODO: Implemented just to get the current code running
            if (isInitialized.booleanValue()) {
                return;
            }
            isInitialized = new Boolean(true);


            try {
                store = MoriaStoreFactory.createMoriaStore();
            } catch (MoriaStoreException e) {
                // TODO: Log and throw exception
            }

            /* Logging */
            accessLogger = new AccessLogger();
            messageLogger = new MessageLogger(MoriaController.class);


            /* Authorization manager */
            authzManager = new AuthorizationManager();

            directoryManager = new DirectoryManager();

            /* Configuration manager */
            try {
                configManager = new ConfigurationManager();
            } catch (ConfigurationManagerException e) {
                //TODO: Handle exeption properly, should probably throw new
                // IllegalInputException
                System.out.println("ConfigurationManagerException caught.");
                e.printStackTrace();
            }

        }
    }

    /**
     * Shut down the controller. All ready status fields are set to false.
     */
    synchronized static void stop() {
        synchronized (isInitialized) {
            if (ready) {
                authzManager = null;
                amReady = false;
                configManager.stop();
                configManager = null;
                directoryManager = null;
                dmReady = false;
                store = null;
                smReady = false;
                servletContext = null;
                ready = false;
                isInitialized = new Boolean(false);
            }
        }
    }

    /* For Login Servlet */

    /**
     * @param loginTicketId
     * @param ssoTicketId
     * @return
     * @throws UnknownTicketException
     */
    public static String attemptSingleSignOn(final String loginTicketId, final String ssoTicketId)
            throws UnknownTicketException, InoperableStateException, IllegalInputException {

        /* Check controller status */
        if (!ready) {
            throw new InoperableStateException("Controller is not ready");
        }

        // If the login ticket is invalid throw exception
        // if (!validateLoginTicket(loginTicket))
        //   throw new UnknownTicketException("Single Sign-On failed for ticket: " + loginTicket);
        // TODO: Implement
        // TODO: Must return two tickets SSO + service

        /* Validate arguments */
        if (loginTicketId == null || loginTicketId.equals("")) {
            throw new IllegalInputException("loginTicketId must be a non-empty string.");
        }
        if (ssoTicketId == null || ssoTicketId.equals("")) {
            throw new IllegalInputException("ssoTicketId must be a non-empty string.");
        }

        try {
            /* Put transient attributes into authnattempt */
            store.setTransientAttributes(loginTicketId, ssoTicketId);

            /* Get service ticket */
            return store.createServiceTicket(loginTicketId);
        } catch (InvalidTicketException e) {
            // TODO: Message log
            throw new UnknownTicketException("The ticket does not exist.");
        } catch (NonExistentTicketException e) {
            // TODO: Access log
            throw new UnknownTicketException("The ticket does not exist.");
        } catch (MoriaStoreException e) {
            // TODO: Message log
            throw new InoperableStateException("Moria is unavailable, the store is down.");
        }
    }

    /**
     * @param loginTicketId
     * @param ssoTicketId
     * @param userId
     * @param password
     * @return a HashMap with two tickets: login and SSO
     * @throws UnknownTicketException
     * @throws InoperableStateException
     * @throws IllegalInputException
     */
    public static Map attemptLogin(final String loginTicketId, final String ssoTicketId, final String userId,
                                   final String password)
            throws UnknownTicketException, InoperableStateException, IllegalInputException, AuthenticationException,
                   DirectoryUnavailableException {

        /* Check controller status */
        if (!ready) {
            throw new InoperableStateException("Controller is not ready");
        }

        /* Validate arguments */
        if (loginTicketId == null || loginTicketId.equals("")) {
            throw new IllegalInputException("loginTicketId must be a non-empty string.");
        }
        if (userId == null || userId.equals("")) {
            throw new IllegalInputException("userId must be a non-empty string.");
        }
        if (password == null || password.equals("")) {
            throw new IllegalInputException("password must be a non-empty string.");
        }

        /* Find authentication attempt */
        MoriaAuthnAttempt authnAttempt;
        try {
            authnAttempt = store.getAuthnAttempt(loginTicketId, true, null);
        } catch (NonExistentTicketException e) {
            // TODO: Access log
            throw new UnknownTicketException("Ticket does not exist");
        } catch (InvalidTicketException e) {
            // TODO: Access log
            throw new UnknownTicketException("Ticket does not exist");
        } catch (MoriaStoreException e) {
            // TODO: Message log?
            throw new InoperableStateException("Store is out of order");
        }

        /* Parse requestedAttributes and extract special attributes */
        String[] requestedAttributes = authnAttempt.getRequestedAttributes();
        HashSet parsedRequestAttributes = new HashSet();
        boolean appendTGT = false;
        for (int i = 0; i < requestedAttributes.length; i++) {
            if (requestedAttributes[i].equals(TGT_IDENTIFIER)) {
                appendTGT = true;
            } else {
                parsedRequestAttributes.add(requestedAttributes[i]);
            }
        }

        /* Resolve list of cached + requested attributes */
        HashSet cachableAttributes = authzManager.getCachableAttributes();
        HashSet retrieveAttributes = new HashSet(cachableAttributes);
        retrieveAttributes.addAll(parsedRequestAttributes);

        /* Authenticate */
        HashMap fetchedAttributes;
        try {
            fetchedAttributes =
            directoryManager.authenticate(new Credentials(userId, password),
                                          (String[]) retrieveAttributes.toArray(new String[retrieveAttributes.size()]));
        } catch (AuthenticationFailedException e) {
            // TODO: Access log
            throw new AuthenticationException("Wrong username/password");
        } catch (BackendException e) {
            // TODO: Access log
            throw new DirectoryUnavailableException("Directory unavailable. Authentication failed.");
        }

        /* Remove existing SSO ticket */
        if (ssoTicketId != null && !ssoTicketId.equals("")) {
            try {
                store.removeSSOTicket(ssoTicketId);
            } catch (NonExistentTicketException e) {
                /* The ticket has probably already timed out. */
            } catch (InvalidTicketException e) {
                // TODO: Message log
                /* This should not happen unless the ticket is not a SSO ticket. Can't do much about it. */
            } catch (MoriaStoreException e) {
                // TODO: Message log
                throw new InoperableStateException("Store is unavailable");
            }
        }



        /* Cache attributes and get tickets */
        String serviceTicketId;
        String newSSOTicketId;
        HashMap cacheAttributes = new HashMap();
        HashMap authnAttemptAttrs = new HashMap();

        Iterator it = cachableAttributes.iterator();
        while (it.hasNext()) {
            String attrName = (String) it.next();
            cacheAttributes.put(attrName, fetchedAttributes.get(attrName));
        }

        for (int i = 0; i < requestedAttributes.length; i++) {
            authnAttemptAttrs.put(requestedAttributes[i], fetchedAttributes.get(requestedAttributes[i]));
        }


        try {
            newSSOTicketId = store.cacheUserData(cacheAttributes);
            if (appendTGT) {
                authnAttemptAttrs.put(TGT_IDENTIFIER,
                                      store.createTicketGrantingTicket(newSSOTicketId,
                                                                       authnAttempt.getServicePrincipal()));
            }
            store.setTransientAttributes(loginTicketId, authnAttemptAttrs);
            serviceTicketId = store.createServiceTicket(loginTicketId);
        } catch (NonExistentTicketException e) {
            // TODO: Message log, should not happen due to validation above
            throw new UnknownTicketException("Ticket does not exist");
        } catch (InvalidTicketException e) {
            // TODO: Message log, should not happen due to validation above
            throw new UnknownTicketException("Ticket does not exist");
        } catch (MoriaStoreException e) {
            throw new InoperableStateException("Store is unavailable");
        }

        /* Return tickets */
        HashMap tickets = new HashMap();
        tickets.put(SERVICE_TICKET, serviceTicketId);
        tickets.put(SSO_TICKET, newSSOTicketId);

        return tickets;
    }

    /* For Web Service */

    /**
     * @param attributes
     * @param returnURLPrefix
     * @param returnURLPostfix
     * @param forceInteractiveAuthentication
     * @return
     * @throws AuthorizationException
     * @throws IllegalInputException
     */
    public static String initiateAuthentication(final String[] attributes, final String returnURLPrefix,
                                                final String returnURLPostfix,
                                                final boolean forceInteractiveAuthentication,
                                                final String servicePrincipal)
            throws AuthorizationException, IllegalInputException, InoperableStateException {

        /* Check controller state */
        if (!ready) {
            throw new InoperableStateException("Controller is not ready");
        }

        /* Validate parameters */
        if (servicePrincipal == null || servicePrincipal.equals("")) {
            throw new IllegalInputException("servicePrincipal cannot be null or an empty string.");
        }
        if (attributes == null) {
            throw new IllegalInputException("Attributes cannot be null.");
        }
        if (returnURLPrefix == null || returnURLPrefix.equals("")) {
            throw new IllegalInputException("URLPrefix cannot be null or an empty string.");
        }
        if (returnURLPostfix == null) {
            throw new IllegalInputException("URLPostfix cannot be null.");
        }

        /* Authorization */
        authorizationCheck(servicePrincipal, attributes, INTERACTIVE_AUTH_OPER);
        /* URL validation */
        if (!(isLegalURL(returnURLPrefix + "FakeMoriaID" + "urlPostfix"))) {
            throw new IllegalInputException("URLPrefix and URLPostfix combined does not make a valid URL.");
        }

        /* Create authentication attempt */
        try {
            return store.createAuthnAttempt(attributes, returnURLPrefix, returnURLPostfix,
                                            forceInteractiveAuthentication, servicePrincipal);
        } catch (MoriaStoreException e) {
            throw new InoperableStateException("Moria is unavailable, the store is down");
        }
    }

    /**
     * Performs a authorization validation of a service request. If the request does not pass the authorization test, an
     * exception is thrown.
     *
     * @param servicePrincipal the principal for the service performing the request
     * @param attributes       the requested attributes
     * @param operation        the requested operation
     * @throws AuthorizationException if the authorization failed
     */
    private static void authorizationCheck(final String servicePrincipal, final String[] attributes,
                                           final String operation)
            throws AuthorizationException {
        AccessStatusType statusType;

        /* Set logging status type */
        if (operation == DIRECT_AUTH_OPER) {
            statusType = AccessStatusType.ACCESS_DENIED_DIRECT_AUTH;
        } else if (operation.equals(INTERACTIVE_AUTH_OPER)) {
            statusType = AccessStatusType.ACCESS_DENIED_INITIATE_AUTH;
        } else if (operation.equals(PROXY_AUTH_OPER)) {
            statusType = AccessStatusType.ACCESS_DENIED_PROXY_AUTH;
        } else if (operation.equals(VERIFY_USER_EXISTENCE_OPER)) {
            statusType = AccessStatusType.ACCESS_DENIED_VERIFY_USER_EXISTENCE;
        } else {
            throw new IllegalArgumentException("Wrong operation type: " + operation);
        }

        try {
            /* Operations */
            if (!authzManager.allowOperations(servicePrincipal, new String[]{operation})) {
                accessLogger.logService(statusType, servicePrincipal, null, null);
                messageLogger.logInfo("Service '" + servicePrincipal
                                      + "' tried to perform '" + operation + "', but can only do '"
                                      + authzManager.getOperations(servicePrincipal));
                throw new AuthorizationException("Access to the requested operation is denied.");
            }

            /* Attributes */
            if (!authzManager.allowAccessTo(servicePrincipal, attributes)) {
                accessLogger.logService(statusType, servicePrincipal, null, null);
                messageLogger.logInfo("Service '" + servicePrincipal
                                      + "' tried to access '" + attributes + "', but have only access to '"
                                      + authzManager.getAttributes(servicePrincipal));
                throw new AuthorizationException("Access to the requested attributes is denied.");
            }
        } catch (UnknownServicePrincipalException e) {
            // TODO: Log event (Message log)
            throw new AuthorizationException("Authorization failed for: " + servicePrincipal);
        }
    }

    /**
     * @param serviceTicketId
     * @param servicePrincipal
     * @return Map containing user attributes in strings or string arrays
     * @throws IllegalInputException
     */
    public static Map getUserAttributes(final String serviceTicketId, final String servicePrincipal)
            throws IllegalInputException, UnknownTicketException, InoperableStateException {
        /* Check controller state */
        if (!ready) {
            throw new InoperableStateException("Controller is not ready");
        }

        /* Validate arguments */
        if (serviceTicketId == null || serviceTicketId.equals("")) {
            throw new IllegalInputException("serviceTicketId must be a non-empty string.");
        }
        if (servicePrincipal == null || servicePrincipal.equals("")) {
            throw new IllegalInputException("servicePrincipal must be a non-empty string.");
        }

        /* Return the attributes */
        try {
            return store.getAuthnAttempt(serviceTicketId, false, servicePrincipal).getTransientAttributes();
        } catch (NonExistentTicketException e) {
            // TODO: Log access
            throw new UnknownTicketException("The ticket does not exist.");
        } catch (InvalidTicketException e) {
            // TODO: Log access?  Throw unknownticketexception os authorizationexception?
            throw new UnknownTicketException("The ticket does not exist.");
        } catch (MoriaStoreException e) {
            // TODO: Message log
            throw new InoperableStateException("Moria is unavailable, the store is down.");
        }
    }

    /**
     * @param requestedAttributes
     * @param userId
     * @param password
     * @param servicePrincipal
     * @return Map containing user attributes in strings or string arrays
     * @throws AuthorizationException
     * @throws IllegalInputException
     */
    public static Map directNonInteractiveAuthentication(final String[] requestedAttributes, final String userId,
                                                         final String password, final String servicePrincipal)
            throws AuthorizationException, IllegalInputException, InoperableStateException, AuthenticationException,
                   DirectoryUnavailableException {

        /* Check controller state */
        if (!ready) {
            throw new InoperableStateException("Controller is not ready");
        }

        /* Validate arguments */
        if (requestedAttributes == null) {
            throw new IllegalInputException("Attributes cannot be null");
        }
        if (userId == null || userId.equals("")) {
            throw new IllegalInputException("UserId must be a non-empty string");
        }
        if (password == null || password.equals("")) {
            throw new IllegalInputException("password must be a non-empty string");
        }
        if (servicePrincipal == null || servicePrincipal.equals("")) {
            throw new IllegalInputException("servicePrincipal must be a non-empty string");
        }

        /* Authorize service */
        authorizationCheck(servicePrincipal, requestedAttributes, DIRECT_AUTH_OPER);

        /* Authenticate */
        HashMap attributes;
        try {
            attributes = directoryManager.authenticate(new Credentials(userId, password), requestedAttributes);
        } catch (AuthenticationFailedException e) {
            // TODO: Access log
            throw new AuthenticationException("Wrong username/password");
        } catch (BackendException e) {
            // TODO: Access log
            throw new DirectoryUnavailableException("Directory unavailable. Authentication failed.");
        }

        /* Return requestedAttributes */

        return attributes;
    }

    /**
     * @param requestedAttributes
     * @param proxyTicketId
     * @param servicePrincipal
     * @return Map containing user attributes in strings or string arrays
     * @throws AuthorizationException
     * @throws IllegalInputException
     */
    public static Map proxyAuthentication(final String[] requestedAttributes, final String proxyTicketId,
                                          final String servicePrincipal)
            throws AuthorizationException, IllegalInputException, InoperableStateException, UnknownTicketException {
        // TODO: Implement
        /* Check controller state */
        if (!ready) {
            throw new InoperableStateException("Controller is not ready");
        }

        /* Validate arguments */
        if (requestedAttributes == null) {
            throw new IllegalInputException("requestedAttributes cannot be null");
        }
        if (proxyTicketId == null || proxyTicketId.equals("")) {
            throw new IllegalInputException("proxyTicket must be a non-empty string.");
        }
        if (servicePrincipal == null || servicePrincipal.equals("")) {
            throw new IllegalInputException("servicePrincipal must be a non-empty string.");
        }

        /* Authorize service */
        authorizationCheck(servicePrincipal, requestedAttributes, PROXY_AUTH_OPER);

        /* Check that attributes are cached */
        HashMap result = new HashMap();
        HashMap userData;
        HashSet cachedAttributes = authzManager.getCachableAttributes();
        try {
            userData = store.getUserData(proxyTicketId, servicePrincipal).getAttributes();
        } catch (InvalidTicketException e) {
            // TODO: Log
            throw new UnknownTicketException("Ticket does not exist.");
        } catch (NonExistentTicketException e) {
            // TODO: Log
            throw new UnknownTicketException("Ticket does not exist.");
        } catch (MoriaStoreException e) {
            throw new InoperableStateException("Moria is unavailable, the store is down");
        }

        /* Get requested, cached attributes */
        for (int i = 0; i < requestedAttributes.length; i++) {
            String attr = requestedAttributes[i];
            if (!cachedAttributes.contains(attr)) {
                throw new AuthorizationException("Requested attributes is not cached: '" + attr + "'");
            }
            result.put(attr, userData.get(attr));
        }

        /* Return attributes */
        return result;
    }

    /**
     * @param ticketGrantingTicket
     * @param proxyServicePrincipal
     * @param servicePrincipal
     * @return
     * @throws AuthorizationException
     * @throws IllegalInputException
     */
    public static String getProxyTicket(final String ticketGrantingTicket, final String proxyServicePrincipal,
                                        final String servicePrincipal)
            throws AuthorizationException, IllegalInputException, InoperableStateException, UnknownTicketException {

        /* Check controller state */
        if (!ready) {
            throw new InoperableStateException("Controller is not ready");
        }

        /* Validate arguments */
        if (ticketGrantingTicket == null || ticketGrantingTicket.equals("")) {
            throw new IllegalInputException("ticketGrantingTicket must be a non-empty string.");
        }
        if (proxyServicePrincipal == null || proxyServicePrincipal.equals("")) {
            throw new IllegalInputException("proxyServicePrincipal must be a non-empty string.");
        }
        if (servicePrincipal == null || servicePrincipal.equals("")) {
            throw new IllegalInputException("servicePrincipal must be a non-empty string.");
        }

        /* Authorize creation of proxy ticket */
        authorizationCheck(servicePrincipal, new String[]{}, PROXY_AUTH_OPER);

        try {
            if (!authzManager.getSubsystems(servicePrincipal).contains(proxyServicePrincipal)) {
                // TODO: Log
                throw new AuthorizationException("Request for proxy ticket denied.");
            }
        } catch (UnknownServicePrincipalException e) {
            //TODO: Log
            throw new AuthorizationException("Unknown service principal.");
        }


        /* Return proxyTicket */
        try {
            return store.createProxyTicket(ticketGrantingTicket, servicePrincipal, proxyServicePrincipal);
        } catch (InvalidTicketException e) {
            // TODO: Log
            throw new UnknownTicketException("Ticket does not exist.");
        } catch (NonExistentTicketException e) {
            // TODO: Log
            throw new UnknownTicketException("Ticket does not eist.");
        } catch (MoriaStoreException e) {
            // TODO: Log
            throw new InoperableStateException("Moria is unavailable, the store is down");
        }
    }

    /**
     * @param userId
     * @param servicePrincipal
     * @return
     * @throws AuthorizationException
     * @throws IllegalInputException
     */
    public static boolean verifyUserExistence(final String userId, final String servicePrincipal)
            throws AuthorizationException, IllegalInputException, InoperableStateException,
                   DirectoryUnavailableException {

        /* Check controller state */
        if (!ready) {
            throw new InoperableStateException("Controller is not ready");
        }

        // TODO: Must authorize operation, probably best done in authorizationCheck()

        /* Validate arguments */
        if (userId == null || userId.equals("")) {
            throw new IllegalInputException("userId must be non-empty string.");
        }
        if (servicePrincipal == null || servicePrincipal.equals("")) {
            throw new IllegalInputException("servicePrincipal must be non-empty string.");
        }

        /* Authorization */
        authorizationCheck(servicePrincipal, new String[]{}, VERIFY_USER_EXISTENCE_OPER);

        /* Verify user (call DM) */
        try {
            return directoryManager.userExists(userId);
        } catch (BackendException e) {
            // TODO: Log
            throw new DirectoryUnavailableException("Directory is unavailable.");
        }
    }

    /* For Configuration Manager */

    /**
     * @param properties
     * @param module
     */
    synchronized public static void setConfig(final String module, final Properties properties) {
        //init();
        if (module.equals(ConfigurationManager.MODULE_AM)) {
            if (authzManager != null) {
                authzManager.setConfig(properties);
                amReady = true;
            }
        } else if (module.equals(ConfigurationManager.MODULE_DM)) {
            if (directoryManager != null) {
                directoryManager.setConfig(properties);
                dmReady = true;
            }
        } else if (module.equals(ConfigurationManager.MODULE_SM)) {
            if (store != null) {
                store.setConfig(properties);
                smReady = true;
            }
        } else if (module.equals(ConfigurationManager.MODULE_WEB)) {
            if (servletContext != null) {
                servletContext.setAttribute("no.feide.moria.web.config", properties);
            } else {
                // TODO: Log event
                // MessageLogger.logCritical("Servlet context not set. Config cannot be updated.");
            }
        }

        /* If all modules are ready, the controller is ready */
        if (isInitialized.booleanValue() && amReady && dmReady && smReady) {
            ready = true;
        }

    }

    /**
     * Start the controller. The controller is supposed to be started from a servlet. The supplied ServletContext can be
     * used to transfer config from the configuration manager to the servlets.
     *
     * @param sc the servletContext from the caller
     */
    public static void initController(final ServletContext sc) {

        /* Abort if called multiple times */
        synchronized (isInitialized) {
            if (isInitialized.booleanValue()) {
                return;
            }
        }

        /* Store servlet context for web module configuration. */
        servletContext = sc;
        init();
    }

    /**
     * Validate URL. Uses blacklist to indicate whether the URL should be accepted or not.
     *
     * @param url the URL to validate
     * @return true if the URL is valid, else false
     */
    static boolean isLegalURL(final String url) {
        // TODO: Implement a more complete URL validator

        if (url == null || url.equals("")) {
            throw new IllegalArgumentException("url must be a non-empty string.");
        }

        String[] illegal = new String[]{"\n", "\r"};

        /* Protocol */
        if (url.indexOf("http://") != 0 && url.indexOf("https://") != 0) return false;

        /* Illegal characters */
        for (int i = 0; i < illegal.length; i++) {
            if (url.indexOf(illegal[i]) != -1) {
                System.out.println("Contains: " + illegal[i]);
                return false;
            }
        }

        return true;
    }

    /**
     * Returns the service properties for an authentication attempt.
     *
     * @param loginTicketId
     * @return a HashMap with service properties
     * @throws UnknownTicketException if the ticket does not point to a authentication attempt
     */
    public static HashMap getServiceProperties(final String loginTicketId)
            throws UnknownTicketException, InoperableStateException, IllegalInputException {

        /* Check controller state */
        if (!ready) {
            throw new InoperableStateException("Controller is not ready");
        }

        /* Validate arguments */
        if (loginTicketId == null || loginTicketId.equals("")) {
            throw new IllegalInputException("loginTicketId must be a non-empty string, was: " + loginTicketId);
        }

        MoriaAuthnAttempt authnAttempt;
        try {
            authnAttempt = store.getAuthnAttempt(loginTicketId, true, null);
        } catch (NonExistentTicketException e) {
            // TODO: Log
            throw new UnknownTicketException("Ticket does not exist");
        } catch (InvalidTicketException e) {
            // TODO: Log, security error?
            throw new UnknownTicketException("Ticket does not exist");
        } catch (MoriaStoreException e) {
            // TODO: Log
            throw new InoperableStateException("Moria is unavailable, store is down");
        }

        try {
            return authzManager.getServiceProperties(authnAttempt.getServicePrincipal());
        } catch (UnknownServicePrincipalException e) {
            // TODO: Log loginTicketId points to a non-existing service (unlikely to happen)
            throw new UnknownTicketException("Ticket is no longer connected to a service.");
        }
    }

    /**
     * Get the seclevel for an authentication attempt.
     *
     * @param loginTicketId the ticket associated with
     * @return int describing the security level for the requested attributes in the authentication attempt
     * @throws UnknownTicketException if the ticket does is invalid
     */
    public static int getSecLevel(final String loginTicketId)
            throws UnknownTicketException, InoperableStateException, AuthorizationException {

        /* Check controller state */
        if (!ready) {
            throw new InoperableStateException("Controller is not ready");
        }

        /* Validate argument */
        if (loginTicketId == null || loginTicketId.equals("")) {
            throw new IllegalArgumentException("loginTicketId must be a non-empty string, was: " + loginTicketId);
        }

        MoriaAuthnAttempt authnAttempt;
        try {
            authnAttempt = store.getAuthnAttempt(loginTicketId, true, null);
        } catch (NonExistentTicketException e) {
            // TODO: Log
            throw new UnknownTicketException("Ticket does not exist.");
        } catch (InvalidTicketException e) {
            // TODO: Log, security violation?
            throw new UnknownTicketException("Ticket does not exist.");
        } catch (MoriaStoreException e) {
            // TODO: Log
            throw new InoperableStateException("Moria is unavailable, store is down");
        }

        try {
            return authzManager.getSecLevel(authnAttempt.getServicePrincipal(), authnAttempt.getRequestedAttributes());
        } catch (UnknownServicePrincipalException e) {
            // TODO: Log loginticket points to a non-existing service (unlikely to happen)
            throw new UnknownTicketException("Ticket is no longer connected to a service.");
        } catch (UnknownAttributeException e) {
            // TODO: Log, should never happen since getSeclevel is used on login page, attrs allready authorized
            throw new AuthorizationException("Attribute does not exist");
        }
    }

    public static void invalidateSSOTicket(final String ssoTicketId) throws IllegalInputException,
                                                                            InoperableStateException {

        /* Check controller state */
        if (!ready) {
            throw new InoperableStateException("Controller is not ready");
        }

        /* Validate argument */
        if (ssoTicketId == null || ssoTicketId.equals("")) {
            throw new IllegalInputException("ssoTicketId must be a non-empty string.");
        }

        try {
            store.removeSSOTicket(ssoTicketId);
        } catch (MoriaStoreException e) {
            // TODO: Log
            throw new InoperableStateException("Moria is unavailable, the store is down.");
        } catch (InvalidTicketException e) {
            // TODO: Log
            /* We don't care, it's just removal of a ticket */
        } catch (NonExistentTicketException e) {
            // TODO: Log
            /* We don't care, it's just removal of a ticket */
        }
    }

    /**
     * Get the total status of the controller. The method returns a HashMap with Boolean values. The following elements
     * are in the map: init: <code>true</code> if the <code>initController</code> method has been called, else
     * <code>false</code> dm: <code>true</code> if the <code>DirectoryManager.setConfig</conde> method has been called,
     * else <code>false</code> sm: <code>true</code> if the <code>MoriaStore.setConfig</conde> method has been called,
     * else <code>false</code> am: <code>true</code> if the <code>AuthorizationManager.setConfig</conde> method has been
     * called, else <code>false</code> moria: <code>true</code> all the above are true (the controller is ready to use)
     *
     * @return
     * @see MoriaController#initController(javax.servlet.ServletContext)
     * @see DirectoryManager#setConfig(java.util.Properties)
     * @see MoriaStore#setConfig(java.util.Properties)
     * @see AuthorizationManager#setConfig(java.util.Properties)
     */
    public final static HashMap getStatus() {
        HashMap totalStatus = new HashMap();
        totalStatus.put("init", isInitialized);
        totalStatus.put("dm", new Boolean(dmReady));
        totalStatus.put("sm", new Boolean(smReady));
        totalStatus.put("am", new Boolean(amReady));
        totalStatus.put("moria", new Boolean(ready));

        return totalStatus;
    }

}