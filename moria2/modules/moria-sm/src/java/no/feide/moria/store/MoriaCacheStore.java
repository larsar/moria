/*
 * Copyright (c) 2004 UNINETT FAS
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * $Id$
 */

package no.feide.moria.store;

import java.util.HashMap;

import org.jboss.cache.PropertyConfigurator;
import org.jboss.cache.TreeCache;
import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.jboss.cache.lock.LockingException;
import org.jboss.cache.lock.TimeoutException;

/**
 * @author Bjørn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public class MoriaCacheStore implements MoriaStore {

    private TreeCache tree;

    /**
     * The time to live for different tickets
     */
    private final long loginTicketTTL, serviceTicketTTL, ssoTicketTTL, tgTicketTTL, proxyTicketTTL;

    private final static String DATA_ATTRIBUTE = "moriaData";

    /**
     * Constructs a new instance
     */
    public MoriaCacheStore() {
        try {
            tree = new TreeCache();
            PropertyConfigurator config = new PropertyConfigurator();
            config.configure(tree, "jboss-cache.xml");
            tree.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
            // TODO: Handle exception properly
        }

        // TODO: Get values from config
        loginTicketTTL = 300; /* 5 min */
        serviceTicketTTL = 300; /* 5 min */
        ssoTicketTTL = 28800; /* 8 hours */
        tgTicketTTL = 7200; /* 2 hours */
        proxyTicketTTL = 300; /* 5 min */
    }

    /**
     * @see no.feide.moria.store.MoriaStore#createAuthnAttempt(java.lang.String[],
            *      java.lang.String, java.lang.String, java.lang.String, boolean)
     */
    public String createAuthnAttempt(String[] requestedAttributes, String responseURLPrefix, String responseURLPostfix,
                                     String servicePrincipal, boolean forceInteractiveAuthentication) throws IllegalArgumentException {

        MoriaTicket ticket = null;
        MoriaAuthnAttempt authnAttempt;

        if (requestedAttributes == null) throw new IllegalArgumentException("requestedAttributes cannot be null");

        if (responseURLPrefix == null) throw new IllegalArgumentException("responseURLPrefix cannot be null");

        if (responseURLPostfix == null) throw new IllegalArgumentException("responseURLPostfix cannot be null");

        if (servicePrincipal == null || servicePrincipal.equals(""))
            throw new IllegalArgumentException("servicePrincipal cannot be null or empty string");

        ticket = new MoriaTicket(MoriaTicket.LOGIN_TICKET, servicePrincipal, loginTicketTTL);

        authnAttempt = new MoriaAuthnAttempt(requestedAttributes, responseURLPrefix, responseURLPostfix,
                forceInteractiveAuthentication, servicePrincipal);
        insertIntoStore(ticket, authnAttempt, null);

        return ticket.getTicketId();
    }

    /**
     * Insert a authentication attempt or cached user data into the cache. Either authnAttempt or cachedUserData
     * must be null.
     *
     * @param ticket         the ticket to connect to the inserted object
     * @param authnAttempt   the authentication attempt to store
     * @param cachedUserData the user data to store
     * @throws IllegalArgumentException if ticket is null, if and only if not one of the data objects are set
     */
    private void insertIntoStore(MoriaTicket ticket, MoriaAuthnAttempt authnAttempt, CachedUserData cachedUserData) {
        /* Validate parameters */
        if (ticket == null) {
            throw new IllegalArgumentException("ticket cannot be null");
        }
        if (authnAttempt == null && cachedUserData == null) {
            throw new IllegalArgumentException("authnAttempt and cachedUserData cannot both be null.");
        }
        if (authnAttempt != null && cachedUserData != null) {
            throw new IllegalArgumentException("Either authnAttempt or cachedUserData must be null.");
        }

        Fqn fqn = new Fqn(ticket.getTicketId());
        HashMap data = new HashMap();
        data.put("ticketType", new Integer(ticket.getTicketType()));
        data.put("ttl", new Long(ticket.getExpiryTime()));
        data.put("principal", ticket.getServicePrincipal());

        if (authnAttempt != null) {
            data.put(DATA_ATTRIBUTE, authnAttempt);
        } else {
            data.put(DATA_ATTRIBUTE, cachedUserData);
        }

        try {
            tree.put(fqn, data);
        } catch (Exception e) {
            // TODO: Handle this exception properly
            throw new RuntimeException(e);
        }
    }

    /**
     * Removes a ticket, and possibly a connected userdata or authnAttempt from the cache.
     *
     * @param ticketId
     * @throws IllegalArgumentException if the ticketId is null
     */
    private void removeFromStore(String ticketId) {
        // TODO: Possibly make this public and part of the API?
        /* Validate parameters */
        if (ticketId == null) {
            throw new IllegalArgumentException("ticketId cannot be null");
        }

        try {
            tree.remove(ticketId);
        } catch (Exception e) {
            // TODO: Handle this exception properly
            throw new RuntimeException(e);
        }
    }


    /**
     * @see no.feide.moria.store.MoriaStore#getAuthnAttempt(java.lang.String, boolean)
     */
    public MoriaAuthnAttempt getAuthnAttempt(String ticketId, boolean keep) throws InvalidTicketException {

        if (ticketId == null || ticketId.equals("")) {
            throw new IllegalArgumentException("ticketId must be a non-empty string");
        }

        MoriaTicket ticket = getTicket(ticketId);
        if (ticket == null) {
            return null;
        }

        validateTicket(ticket, MoriaTicket.LOGIN_TICKET);
        MoriaAuthnAttempt authnAttempt = null;

        try {
            if (tree.exists(ticketId)) {
                authnAttempt = (MoriaAuthnAttempt) tree.get(ticketId).get(DATA_ATTRIBUTE);
                if (!keep) {
                    removeFromStore(ticketId);
                }
            }
        } catch (LockingException e) {
            // TODO: Handle this exception properly
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            // TODO: Handle this exception properly
            throw new RuntimeException(e);
        }

        return authnAttempt;
    }

    /**
     * @see no.feide.moria.store.MoriaStore#cacheUserData(java.util.HashMap)
     */
    public String cacheUserData(HashMap attributes) {

        if (attributes == null) {
            throw new IllegalArgumentException("attributes cannot be null");
        }

        CachedUserData userData = new CachedUserData(attributes);
        MoriaTicket ssoTicket = new MoriaTicket(MoriaTicket.SSO_TICKET, null, ssoTicketTTL);
        insertIntoStore(ssoTicket, null, userData);

        return ssoTicket.getTicketId();
    }

    /**
     * @throws IllegalArgumentException if ticketId is null or an empty string
     * @see no.feide.moria.store.MoriaStore#getUserData(java.lang.String)
     * @return cachedUserdata, null if the ticket does not exist
     */
    public CachedUserData getUserData(String ticketId) throws InvalidTicketException {
        /* Validate argument */
        if (ticketId == null || ticketId.equals("")) {
            throw new IllegalArgumentException("ticketId must be a non-empty string");
        }
        MoriaTicket ticket = getTicket(ticketId);
        if (ticket == null) {
            return null;
        }

        validateTicket(ticket, new int[] { MoriaTicket.SSO_TICKET, MoriaTicket.TICKET_GRANTING_TICKET, MoriaTicket.PROXY_TICKET});

        /*
         * If the returned value isn't a CachedUserData object, we can't do
         * much else than throw an exception, so we just go for the default
         * ClassCastException
         *
         */
        try {
            if (tree.exists(ticketId)) {
                return (CachedUserData) tree.get(ticketId).get(DATA_ATTRIBUTE);
            } else {
                return null;
            }
        } catch (LockingException e) {
            throw new RuntimeException(e);
            // TODO: Handle this exception properly
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
            // TODO: Handle this exception properly
        }
    }

    /**
     * @see no.feide.moria.store.MoriaStore#createServiceTicket(java.lang.String, java.lang.String)
     */
    public String createServiceTicket(String loginTicketId, String servicePrincipal) throws InvalidTicketException {

        /* Validate arguments */
        if (loginTicketId == null || loginTicketId.equals("")) {
            throw new IllegalArgumentException("loginTicketId must be a non-empty string");
        }
        if (servicePrincipal == null || servicePrincipal.equals("")) {
            throw new IllegalArgumentException("servicePrincipal must be a non-empty string");
        }

        MoriaTicket loginTicket = getTicket(loginTicketId);
        validateTicket(loginTicket, MoriaTicket.LOGIN_TICKET);

        /*
         * Create new service ticket and assosiate it with the same
         * authentication attempt as the login ticket
         */
            MoriaTicket serviceTicket = new MoriaTicket(MoriaTicket.SERVICE_TICKET, loginTicket.getServicePrincipal(),
                    serviceTicketTTL);
            insertIntoStore(serviceTicket, getAuthnAttempt(loginTicketId, true), null);
            return serviceTicket.getTicketId();
    }

    /**
     * @see no.feide.moria.store.MoriaStore#createTicketGrantingTicket(java.lang.String, java.lang.String)
     */
    public String createTicketGrantingTicket(String ticketId, String servicePrincipal) throws InvalidTicketException {
        /* Validate arguments */
        if (ticketId == null || ticketId.equals("")) {
            throw new IllegalArgumentException("ticketId must be a non-empty string");
        }
        if (servicePrincipal == null || servicePrincipal.equals("")) {
            throw new IllegalArgumentException("servicePrincipal must be a non-empty string");
        }

        /* Create new ticket granting ticket and assosiate it with the same user data as the sso ticket. */
        MoriaTicket tgTicket;
        CachedUserData userData = getUserData(ticketId);
        if (userData == null) {
            throw new InvalidTicketException("SSO ticket has no data assosiated with it");
        }

        tgTicket = new MoriaTicket(MoriaTicket.TICKET_GRANTING_TICKET, servicePrincipal, tgTicketTTL);
        insertIntoStore(tgTicket, null, userData);

        return tgTicket.getTicketId();
    }

    /**
     * @see no.feide.moria.store.MoriaStore#createProxyTicket(java.lang.String, java.lang.String)
     */
    public String createProxyTicket(String tgTicketId, String servicePrincipal) throws InvalidTicketException {
        /* Validate arguments */
        if (tgTicketId == null || tgTicketId.equals("")) {
            throw new IllegalArgumentException("tgTicketId must be a non-empty string");
        }
        if (servicePrincipal == null || servicePrincipal.equals("")) {
            throw new IllegalArgumentException("servicePrincipal must be a non-empty string");
        }

        MoriaTicket tgTicket = getTicket(tgTicketId);

        if (tgTicket == null) {
            return null;
        }

        validateTicket(tgTicket, MoriaTicket.TICKET_GRANTING_TICKET);
        CachedUserData userData = getUserData(tgTicketId);
        if (userData == null) {
            return null;
        }
        MoriaTicket proxyTicket = new MoriaTicket(MoriaTicket.PROXY_TICKET, servicePrincipal, proxyTicketTTL);
        insertIntoStore(proxyTicket, null, userData);

        return proxyTicket.getTicketId();
    }

    /**
     * @see no.feide.moria.store.MoriaStore#setTransientAttributes(java.lang.String, java.util.HashMap)
     *      java.lang.String[])
     */
    public void setTransientAttributes(String  loginTicketId, HashMap transientAttributes) throws InvalidTicketException {
        /* Validate arguments */
        if (loginTicketId == null || loginTicketId.equals("")) {
            throw new IllegalArgumentException("loginTicketId must be a non-empty string");
        }
        if (transientAttributes == null) {
            throw new IllegalArgumentException("transientAttributes cannot be null");
        }

        MoriaTicket loginTicket = getTicket(loginTicketId);
        if (loginTicket == null) {
            throw new IllegalStateException("ticket does not exist");
            // TODO Log
        }

        validateTicket(loginTicket, MoriaTicket.LOGIN_TICKET);
        MoriaAuthnAttempt authnAttempt = getAuthnAttempt(loginTicketId, true);
        if (authnAttempt == null) {
            throw new IllegalStateException("AuthenticationAttempt does not exist");
            // TODO Log
        }

        authnAttempt.setTransientAttributes(transientAttributes);

        /* Insert into cache again to trigger distributed update */
        // TODO: Verify that this is the only way to do it
        insertIntoStore(loginTicket, authnAttempt, null);
    }

    /**
     * Check validity of ticket against type and expiry time
     *
     * @param ticket ticket to be checked
     * @param type   the expected type of the ticket
     * @throws InvalidTicketException thrown if ticket is found invalid
     * @throws IllegalArgumentException if ticket is null
     */
    private void validateTicket(MoriaTicket ticket, int type) throws InvalidTicketException {
        validateTicket(ticket, new int[]{type});
    }

    /**
     * Check validity of ticket against a set of types and expiry time
     *
     * @param ticket ticket to be checked
     * @param types  array of valid types for the ticket
     * @throws InvalidTicketException thrown if the ticket is found invalid
     * @throws IllegalArgumentException if ticket is null
     */
    private void validateTicket(MoriaTicket ticket, int[] types) throws InvalidTicketException {
        boolean valid = false;

        /* Validate arguments */
         if (ticket == null) {
             throw new IllegalArgumentException("ticket cannot be null");
         }

         /* Loop through ticket types and count invalid ones */
        for (int i = 0; i < types.length; i++) {
            if (ticket.getTicketType() == types[i]) {
                valid = true;
                break;
            }
        }

        /* Throw exception if all types were invalid */
        if (!valid) throw new InvalidTicketException("Ticket has wrong type: " + ticket.getTicketType());

        /*
         * Then check if it still is valid. We let the dedicated
         * vacuuming-service take care of removing it at later time, so we just
         * throw an exception
         */
        if (ticket.hasExpired()) throw new InvalidTicketException("Ticket has expired");

    }

    MoriaTicket getTicket(String ticketId) {
        /* Validate parameter */
        if (ticketId == null || ticketId.equals("")) {
            throw new IllegalArgumentException("ticketId must be a non-empty string.");
        }
        Node node;
        try {
            node = tree.get(ticketId);
        } catch (LockingException e) {
            // TODO: Handle exception properly
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            // TODO: Handle exception properly
            throw new RuntimeException(e);
        }
        if (node == null) {
            return null;
        } else {
            return new MoriaTicket(ticketId, ((Integer) node.get("ticketType")).intValue(),
                    (String) node.get("principal"), ((Long) node.get("ttl")).longValue());
        }

    }
}
