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
import no.feide.moria.configuration.ConfigurationManager;
import no.feide.moria.configuration.ConfigurationManagerException;
import no.feide.moria.store.InvalidTicketException;
import no.feide.moria.store.MoriaAuthnAttempt;
import no.feide.moria.store.MoriaStore;
import no.feide.moria.store.MoriaStoreFactory;

import javax.servlet.ServletContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
 * @version $Revision$
 */
public class MoriaController {

    /**
     * The single instance of the data store
     */
    private static MoriaStore store;

    /**
     * The single instance of the configuration manager
     */
    private static ConfigurationManager configManager;

    /**
     * The single instance of the configuration manager
     */
    private static AuthorizationManager authzManager;

    /**
     * Flag set to true if the controller has been initialized
     */
    private static Boolean ready = new Boolean(false);

    /**
     * The servlet context for the servlets using the controller
     */
    private static ServletContext servletContext;

    /**
     *
     *
     */
    synchronized static void init() {
        synchronized (ready) {
            // TODO: Implemented just to get the current code running
            if (ready.booleanValue()) {
                return;
            }
            ready = new Boolean(true);

            // TODO: Ensure single instance of store
            store = MoriaStoreFactory.createMoriaStore();

            // TODO: Should use value specified on the command line, in startup servlet or something
            // like that
            if (System.getProperty("no.feide.moria.configuration.cm") == null)
                System.setProperty("no.feide.moria.configuration.cm", "/cm-test-valid.properties");
            if (System.getProperty("no.feide.moria.store.randomid.nodeid") == null)
                System.setProperty("no.feide.moria.store.randomid.nodeid", "no1");

            /* Authorization manager */
            authzManager = new AuthorizationManager();

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
     * Shut down the controller.
     */
    synchronized static void stop() {
        synchronized (ready) {
            if (ready.booleanValue()) {
                configManager.stop();
                configManager = null;
                store = null;
                authzManager = null;
                ready = new Boolean(false);
            }
        }
    }

    /* For Login Servlet */

    /**
     * @param loginTicket
     * @param ssoTicket
     * @return
     * @throws UnknownTicketException
     */
    public static String attemptSingleSignOn(final String loginTicket, final String ssoTicket) throws UnknownTicketException {

        // If the login ticket is invalid throw exception
       // if (!validateLoginTicket(loginTicket))
         //   throw new UnknownTicketException("Single Sign-On failed for ticket: " + loginTicket);
        // TODO: Implement
        return null;
    }

    /**
     * @param loginTicket
     * @param ssoTicket
     * @param userId
     * @param password
     * @return
     * @throws UnknownTicketException
     */
    public static boolean attemptLogin(final String loginTicket, final String ssoTicket, final String userId,
                                       final String password) throws UnknownTicketException {
        // TODO: Implement
        return false;
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
                                                final String returnURLPostfix, final boolean forceInteractiveAuthentication, final String servicePrincipal)
            throws AuthorizationException, IllegalInputException {

        if (!ready.booleanValue()) {
            throw new IllegalStateException("Controller not initialized");
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
        if (!authzManager.allowAccessTo(servicePrincipal, attributes)) {
            // TODO: Access log
            throw new AuthorizationException("Access to the requested attributes is denied.");
        }
        if (!authzManager.allowOperations(servicePrincipal, new String[]{"InteractiveAuth"})) {
            throw new AuthorizationException("Access to the requested operations is denied.");
        }

        /* URL validation */
        if (returnURLPrefix == null || returnURLPrefix.equals("")) {
            throw new IllegalInputException("URLPrefix cannot be null or an empty string.");
        }
        if (returnURLPostfix == null) {
            throw new IllegalInputException("URLPostfix cannot be null.");
        }
        if (!(isLegalURL(returnURLPrefix + "FakeMoriaID" + "urlPostfix"))) {
            throw new IllegalInputException("URLPrefix and URLPostfix combined does not make a valid URL.");
        }

        /* Create authentication attempt */
        return store.createAuthnAttempt(attributes, returnURLPrefix, returnURLPostfix,
                forceInteractiveAuthentication, servicePrincipal);
    }

    /**
     * @param serviceTicket
     * @param servicePrincipal
     * @return Map containing user attributes in strings or string arrays
     * @throws AuthorizationException
     * @throws IllegalInputException
     */
    public static Map getUserAttributes(final String serviceTicket, final String servicePrincipal) throws AuthorizationException,
            IllegalInputException {
        // TODO: Implement
        if (!ready.booleanValue()) {
            throw new IllegalStateException("Controller not initialized");
        }
        return null;
    }

    /**
     * @param attributes
     * @param userId
     * @param password
     * @param servicePrincipal
     * @return Map containing user attributes in strings or string arrays
     * @throws AuthorizationException
     * @throws IllegalInputException
     */
    public static Map directNonInteractiveAuthentication(final String[] attributes, final String userId, final String password,
                                                         final String servicePrincipal) throws AuthorizationException, IllegalInputException {
        // TODO: Implement
        if (!ready.booleanValue()) {
            throw new IllegalStateException("Controller not initialized");
        }
        return null;
    }

    /**
     * @param attributes
     * @param proxyTicket
     * @param servicePrincipal
     * @return Map containing user attributes in strings or string arrays
     * @throws AuthorizationException
     * @throws IllegalInputException
     */
    public static Map proxyAuthentication(final String[] attributes, final String proxyTicket, final String servicePrincipal)
            throws AuthorizationException, IllegalInputException {
        // TODO: Implement
        if (!ready.booleanValue()) {
            throw new IllegalStateException("Controller not initialized");
        }
        return null;
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
                                        final String servicePrincipal) throws AuthorizationException, IllegalInputException {
        // TODO: Implement
        if (!ready.booleanValue()) {
            throw new IllegalStateException("Controller not initialized");
        }
        return null;
    }

    /**
     * @param username
     * @param servicePrincipal
     * @return
     * @throws AuthorizationException
     * @throws IllegalInputException
     */
    public static boolean verifyUserExistence(final String username, final String servicePrincipal) throws AuthorizationException,
            IllegalInputException {
        // TOOD: Implement
        if (!ready.booleanValue()) {
            throw new IllegalStateException("Controller not initialized");
        }
        return false;
    }

    /* For Configuration Manager */

    /**
     * @param properties
     * @param module
     */
    synchronized public static void setConfig(final String module, final Properties properties) {
        //init();
        if (!ready.booleanValue()) {
            throw new IllegalStateException("Controller not initialized");
        }
        if (module.equals(ConfigurationManager.MODULE_AM)) {
            if (authzManager != null) {
                authzManager.setConfig(properties);
            }
        } else if (module.equals(ConfigurationManager.MODULE_WEB)) {
            if (servletContext != null) {
                System.out.println("Setting context");
                servletContext.setAttribute("config", properties);
            } else {
                // TODO: Log event
                // MessageLogger.logCritical("Servlet context not set. Config cannot be updated.");
            }
        }
    }

    /**
     * Start the controller. The controller is supposed to be started from a servlet. The supplied
     * ServletContext can be used to transfer config from the configuration manager to the servlets.
     *
     * @param sc the servletContext from the caller
     */
    public static void initController(ServletContext sc) {
        servletContext = sc;
        init();
        // TODO: Finish implementation, the init() method will change
    }

    /**
     * Validate URL. Uses blacklist to indicate whether the URL should be accepted or not.
     *
     * @param url the URL to validate
     * @return true if the URL is valid, else false
     */
    static boolean isLegalURL(String url) {
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
    public static HashMap getServiceProperties(String loginTicketId) throws UnknownTicketException {
        if (!ready.booleanValue()) {
            throw new IllegalStateException("Controller not initialized");
        }
        /* Validate arguments */
        if (loginTicketId == null || loginTicketId.equals("")) {
            throw new IllegalArgumentException("loginTicketId must be a non-empty string, was: " + loginTicketId);
        }

        try {
            MoriaAuthnAttempt authnAttempt = store.getAuthnAttempt(loginTicketId, true);
            return authzManager.getServiceProperties(authnAttempt.getServicePrincipal());
        } catch (InvalidTicketException e) {
            // TODO: Log? (should have been logged by sublayer)
            throw new UnknownTicketException("Ticket does not exist");
        }

        // TODO: Finish implementation
        }

    /**
     * Get the seclevel for an authentication attempt.
     *
     * @param loginTicketId the ticket associated with
     * @return int describing the security level for the requested attributes in the authentication attempt
     * @throws UnknownTicketException if the ticket does is invalid
     */
    public static int getSecLevel(String loginTicketId) throws UnknownTicketException {
        if (!ready.booleanValue()) {
            throw new IllegalStateException("Controller not initialized");
        }
        /* Validate argument */
        if (loginTicketId == null || loginTicketId.equals("")) {
            throw new IllegalArgumentException("loginTicketId must be a non-empty string, was: " + loginTicketId);
        }

        MoriaAuthnAttempt authnAttempt;
        try {
            authnAttempt = store.getAuthnAttempt(loginTicketId, true);
        } catch (InvalidTicketException e) {
            // TODO: Log?, should have been logged extensively in the store
            throw new UnknownTicketException("Ticket does not exist.");
        }

        return authzManager.getSecLevel(authnAttempt.getServicePrincipal(), authnAttempt.getRequestedAttributes());
    }
}