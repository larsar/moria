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
import java.util.HashMap;
import java.util.TreeMap;

import no.feide.moria.store.MoriaStore;
import no.feide.moria.store.MoriaStoreFactory;
import no.feide.moria.store.UnknownTicketException;
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

    /** The single instance of the data store */
    private static MoriaStore store;

    /** The single instance of the configuration manager */
    private static ConfigurationManager configManager;

    /** The single instance of the configuration manager */
    private static AuthorizationManager authzManager;

    // TODO: Should probably be implemented otherwise
    /** Flag set to true if the controller has been initialized */
    private static boolean isInitialized = false;

    /** The servlet context for the servlets using the controller */
    private static ServletContext servletContext;

    // TODO: Only for debugging. Should be initialized in another way.
    //static {
    //    init();
    //}

    /**
     *
     *
     */
    synchronized public static void init() {
        // TODO: Implemented just to get the current code running
        if (isInitialized) {
            return;
        }
        isInitialized = true;

        // TODO: Ensure single instance of store
        //store = MoriaStoreFactory.createMoriaStore();

        // TODO: Should use value specified on the command line, in startup servlet or something like that
        System.setProperty("no.feide.moria.configuration.cm", "/cm-test-valid.properties");

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

    /* For Login Servlet */

    /**
     * @param loginTicketId
     * @return
     */
    public static boolean validateLoginTicket(String loginTicketId) {
        // TODO: Implement
        return false;
    }

    /**
     *
     * @param loginTicketId
     * @param ssoTicketId
     * @return
     * @throws UnknownTicketException
     */
    public static String attemptSingleSignOn(String loginTicketId,
            String ssoTicketId) throws UnknownTicketException {

        // If the login ticket is invalid throw exception
        if (!validateLoginTicket(loginTicketId))
                throw new UnknownTicketException(
                        "Single Sign-On failed for ticket: " + loginTicketId);
        // TODO: Implement
        return null;
    }

    /**
     *
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
     *
     * @param attributes
     * @param returnURLPrefix
     * @param returnURLPostfix
     * @param forceInteractiveAuthentication
     * @return
     */
    public static String initiateMoriaAuthentication(String[] attributes,
            String returnURLPrefix, String returnURLPostfix,
            boolean forceInteractiveAuthentication) {
        // TODO: Implement
        return null;
    }

    /**
     *
     * @param ticketId
     * @return
     */
    public static String[] getUserAttributes(String ticketId) {
        // TODO: Implement
        return null;
    }

    /**
     *
     * @param attributes
     * @param userId
     * @param password
     * @param domain
     * @return
     */
    public static String directNonInteractiveAuthentication(
            String[] attributes, String userId, String password, String domain) {
        // TODO: Implement
        return null;
    }

    /* For Configuration Manager */

    /**
     *
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

    public static void initController(ServletContext sc) {
        servletContext = sc;
        init();
    }

}
