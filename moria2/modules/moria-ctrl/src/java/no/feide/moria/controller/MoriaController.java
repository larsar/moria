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
 *
 */

package no.feide.moria.controller;

import java.util.Properties;

import no.feide.moria.store.MoriaStore;
import no.feide.moria.store.MoriaStoreFactory;
import no.feide.moria.store.UnknownTicketException;
import no.feide.moria.store.MoriaTicket;
import no.feide.moria.store.InvalidTicketException;
import no.feide.moria.store.MoriaAuthnAttempt;
import no.feide.moria.configuration.ConfigurationManager;
import no.feide.moria.configuration.ConfigurationManagerException;
import no.feide.moria.authorization.AuthorizationManager;
import no.feide.moria.log.MessageLogger;

import javax.servlet.ServletContext;

/**
 * @author Bjørn Ola Smievoll &lt;b.o@smievoll.no&gt;
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

            // TODO: Should use value specified on the command line, in startup servlet or something like that
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
                //TODO: Handle exeption properly, should probably throw new MoriaControllerException
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
     * @param loginTicketId
     * @return
     */
    public static boolean validateLoginTicket(String loginTicketId) {

        if (!ready.booleanValue()) {
            throw new IllegalStateException("Controller is not initialized.");
        }

        /* Valdiate parameter */
        if (loginTicketId == null || loginTicketId.equals("")) {
            throw new IllegalArgumentException("loginTicketId cannot be null or an empty string.");
        }

        MoriaAuthnAttempt authnAttempt = null;
        try {
            authnAttempt = store.getAuthnAttempt(loginTicketId, true);
        } catch (InvalidTicketException e) {
            // TODO: Log
            return false;
        }

        if (authnAttempt != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param loginTicketId
     * @param ssoTicketId
     * @return
     * @throws UnknownTicketException
     */
    public static String attemptSingleSignOn(String loginTicketId,
                                             String ssoTicketId) throws UnknownTicketException {

        // If the login ticket is invalid throw exception
        if (!validateLoginTicket(loginTicketId))
            throw new UnknownTicketException("Single Sign-On failed for ticket: " + loginTicketId);
        // TODO: Implement
        return null;
    }

    /**
     * @param urlId
     * @param cookieId
     * @param userId
     * @param password
     * @param domain
     * @return
     */
    public static boolean attemptLogin(String urlId, String cookieId,
                                       String userId, String password, String domain) {
        // TODO: Implement
        return false;
    }

    /* For Web Service */

    /**
     * @param attributes
     * @param urlPrefix
     * @param urlPostfix
     * @param forceInteractiveAuthentication
     * @return
     */
    public static String initiateMoriaAuthentication(String principal, String[] attributes,
                                                     String urlPrefix, String urlPostfix,
                                                     boolean forceInteractiveAuthentication)
            throws AuthorizationException, MoriaControllerException {

        if (!ready.booleanValue()) {
            throw new IllegalStateException("Controller not initialized");
        }

        /* Validate parameters */
        if (principal == null || principal.equals("")) {
            throw new MoriaControllerException("Principal cannot be null or an empty string.");
        }
        if (attributes == null) {
            throw new MoriaControllerException("Attributes cannot be null.");
        }
        if (urlPrefix == null || urlPrefix.equals("")) {
            throw new MoriaControllerException("URLPrefix cannot be null or an empty string.");
        }
        if (urlPostfix == null || urlPostfix.equals("")) {
            throw new MoriaControllerException("URLPostfix cannot be null.");
        }

        /* Authorization */
        if (!authzManager.allowAccessTo(principal, attributes)) {
            // TODO: Access log
            throw new AuthorizationException("Access to the requested attributes is denied.");
        }
        if (!authzManager.allowOperations(principal, new String[]{"InteractiveAuth"})) {
            throw new AuthorizationException("Access to the requested operations is denied.");
        }

        /* URL validation */
        if (urlPrefix == null || urlPrefix.equals("")) {
            throw new MoriaControllerException("URLPrefix cannot be null or an empty string.");
        }
        if (urlPostfix == null) {
            throw new MoriaControllerException("URLPostfix cannot be null.");
        }
        if (!(isLegalURL(urlPrefix + "FakeMoriaID" + "urlPostfix"))) {
            throw new MoriaControllerException("URLPrefix and URLPostfix combined does not make a valid URL.");
        }

        /* Create authentication attempt */
        return store.createAuthnAttempt(attributes, urlPrefix, urlPostfix, principal, forceInteractiveAuthentication);
    }

    /**
     * @param ticketId
     * @return
     */
    public static String[] getUserAttributes(String ticketId) {
        // TODO: Implement
        return null;
    }

    /**
     * @param attributes
     * @param userId
     * @param password
     * @param domain
     * @return
     */
    public static String directNonInteractiveAuthentication(String[] attributes, String userId, String password, String domain) {
        // TODO: Implement
        return null;
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
     * Start the controller. The controller is supposed to be started from a servlet.
     * The supplied ServletContext can be used to transfer config from the configuration
     * manager to the servlets.
     *
     * @param sc the servletContext from the caller
     */
    public static void initController(ServletContext sc) {
        servletContext = sc;
        init();
        // TODO: Finish implementation, the init() method will change
    }

    /**
     * Validate URL. Uses blacklist to indicate whether the URL should
     * be accepted or not.
     *
     * @param url the URL to validate
     * @return true if the URL is valid, else false
     */
    static boolean isLegalURL(String url) {
        // TODO: Implement a more complete URL validator

        if (url == null || url.equals("")) {
            throw new IllegalArgumentException("url must be a non-empty string.");
        }

        String[] illegal = new String[]{
            "\n",
            "\r"
        };

        /* Protocol */
        if (url.indexOf("http://") != 0 && url.indexOf("https://") != 0)
            return false;

        /* Illegal characters */
        for (int i = 0; i < illegal.length; i++) {
            if (url.indexOf(illegal[i]) != -1) {
                System.out.println("Contains: " + illegal[i]);
                return false;
            }
        }

        return true;
    }
}
