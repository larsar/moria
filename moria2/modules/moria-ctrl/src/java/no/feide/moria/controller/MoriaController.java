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

/**
 * @author Bjørn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public class MoriaController {

    /** The single instance of the data store */
    private static MoriaStore store;

    /**
     * 
     *
     */
    public static void init() {
        // TODO: Ensure single instance of store
        store = MoriaStoreFactory.createMoriaStore();
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
     * @param config
     * @param module
     */
    public static void setConfig(String module, Properties properties) {

    }
}
