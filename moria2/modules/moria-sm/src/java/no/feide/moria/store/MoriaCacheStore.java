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

package no.feide.moria.store;

import java.util.HashMap;

import no.feide.moria.log.MessageLogger;

import org.jboss.cache.Fqn;
import org.jboss.cache.Node;
import org.jboss.cache.PropertyConfigurator;
import org.jboss.cache.TreeCache;
import org.jboss.cache.lock.LockingException;
import org.jboss.cache.lock.TimeoutException;

/**
 * Distributed store implementation using JBoss Cache.
 *
 * @author Bjørn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public final class MoriaCacheStore implements MoriaStore {

    /** The cache instance. */
    private TreeCache store;

    /** The logger used by this class. */
    private MessageLogger messageLogger = new MessageLogger(MoriaCacheStore.class);

    /** The time to live for different tickets. */
    private final Long loginTicketTTL, serviceTicketTTL, ssoTicketTTL, tgTicketTTL, proxyTicketTTL;

    /** The common hashmap key for the ticket type. */
    private static final String TICKET_TYPE_ATTRIBUTE = "TicketType";

    /** The common hashmap key for the time to live. */
    private static final String TTL_ATTRIBUTE = "TimeToLive";

    /** The common hashmap key for the principal. */
    private static final String PRINCIPAL_ATTRIBUTE = "Principal";

    /**
     * The common hashmap key for the data attributes (MoriaAuthnAttempt &
     * CachedUserData).
     */
    private static final String DATA_ATTRIBUTE = "MoriaData";

    /**
     * Constructs a new instance.
     */
    public MoriaCacheStore() {
        try {
            store = new TreeCache();
            PropertyConfigurator configurator = new PropertyConfigurator();
            // TODO: Implement configuration handling properly
            configurator.configure(store, "jboss-cache.xml");
            store.start();
        } catch (Exception e) {
            throw new RuntimeException(e);
            // TODO: Handle exception properly
        }

        // TODO: Get values from config
        loginTicketTTL = new Long(300); /* 5 min */
        serviceTicketTTL = new Long(300); /* 5 min */
        ssoTicketTTL = new Long(28800); /* 8 hours */
        tgTicketTTL = new Long(7200); /* 2 hours */
        proxyTicketTTL = new Long(300); /* 5 min */
    }

    /**
     * @see no.feide.moria.store.MoriaStore#createAuthnAttempt(java.lang.String[], java.lang.String,
     *      java.lang.String, boolean, java.lang.String)
     */
    public String createAuthnAttempt(final String[] requestedAttributes, final String responseURLPrefix,
            final String responseURLPostfix, final boolean forceInteractiveAuthentication, final String servicePrincipal) {

        MoriaTicket ticket = null;
        MoriaAuthnAttempt authnAttempt;

        if (requestedAttributes == null) {
            throw new IllegalArgumentException("requestedAttributes cannot be null");
        }

        if (responseURLPrefix == null) {
            throw new IllegalArgumentException("responseURLPrefix cannot be null");
        }

        if (responseURLPostfix == null) {
            throw new IllegalArgumentException("responseURLPostfix cannot be null");
        }

        if (servicePrincipal == null || servicePrincipal.equals("")) {
            throw new IllegalArgumentException("servicePrincipal cannot be null or empty string");
        }

        ticket = new MoriaTicket(MoriaTicketType.LOGIN_TICKET, servicePrincipal, loginTicketTTL);

        authnAttempt = new MoriaAuthnAttempt(requestedAttributes, responseURLPrefix, responseURLPostfix,
                forceInteractiveAuthentication, servicePrincipal);
        insertIntoStore(ticket, authnAttempt, null);

        return ticket.getTicketId();
    }

    /**
     * Insert a authentication attempt or cached user data into the cache. Either authnAttempt or
     * cachedUserData must be null.
     *
     * @param ticket
     *          the ticket to connect to the inserted object
     * @param authnAttempt
     *          the authentication attempt to store
     * @param cachedUserData
     *          the user data to store
     * @throws IllegalArgumentException
     *          if ticket is null, if and only if not one of the data objects are set
     */
    private void insertIntoStore(final MoriaTicket ticket, final MoriaAuthnAttempt authnAttempt, final CachedUserData cachedUserData) {

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

        Fqn fqn = new Fqn(new Object[] {ticket.getTicketType(), ticket.getTicketId()});
        HashMap data = new HashMap();
        data.put(TICKET_TYPE_ATTRIBUTE, ticket.getTicketType());
        data.put(TTL_ATTRIBUTE, ticket.getExpiryTime());
        data.put(PRINCIPAL_ATTRIBUTE, ticket.getServicePrincipal());

        if (authnAttempt != null) {
            data.put(DATA_ATTRIBUTE, authnAttempt);
        } else {
            data.put(DATA_ATTRIBUTE, cachedUserData);
        }

        try {
            store.put(fqn, data);
        } catch (Exception e) {
            // TODO: Handle this exception properly
            throw new RuntimeException(e);
        }
    }

    /**
     * Removes a ticket, and possibly a connected userdata or authnAttempt from the cache.
     *
     * @param ticket
     *          the ticket to be removed
     * @throws IllegalArgumentException
     *          if the ticketId is null
     */
    private void removeFromStore(final MoriaTicket ticket) {

        /* Validate parameters. */
        if (ticket == null) {
            throw new IllegalArgumentException("ticketId cannot be null");
        }

        Fqn fqn = new Fqn(new Object[] {ticket.getTicketType(), ticket.getTicketId()});

        try {
            store.remove(fqn);
        } catch (Exception e) {
            // TODO: Handle this exception properly
            throw new RuntimeException(e);
        }
    }

    /**
     * @see no.feide.moria.store.MoriaStore#getAuthnAttempt(java.lang.String, boolean)
     */
    public MoriaAuthnAttempt getAuthnAttempt(final String loginTicketId, final boolean keep) throws InvalidTicketException {

        if (loginTicketId == null || loginTicketId.equals("")) {
            throw new IllegalArgumentException("loginTicketId must be a non-empty string");
        }

        MoriaTicket ticket = getTicketFromStore(MoriaTicketType.LOGIN_TICKET, loginTicketId);

        if (ticket == null) {
            return null;
        }

        /* Validate ticket. */
        validateTicket(ticket, MoriaTicketType.LOGIN_TICKET, null);

        MoriaAuthnAttempt authnAttempt = null;

        Fqn fqn = new Fqn(new Object[] {ticket.getTicketType(), ticket.getTicketId()});

        try {
            if (store.exists(fqn)) {
                Object dataObject = store.get(fqn).get(DATA_ATTRIBUTE);
                /* authnAttempt will remain null if the returned object is of incorrect type. */
                if (dataObject instanceof MoriaAuthnAttempt) {
                    authnAttempt = (MoriaAuthnAttempt) dataObject;
                }
                if (!keep) {
                    removeFromStore(ticket);
                }
            }
        } catch (LockingException e) {
            messageLogger.logCritical("LockingException thrown by store. Rethrowing as RuntimeException.", e);
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            messageLogger.logCritical("TimeoutException thrown by store. Rethrowing as RuntimeException.", e);
            throw new RuntimeException(e);
        }

        return authnAttempt;
    }

    /**
     * @see no.feide.moria.store.MoriaStore#cacheUserData(java.util.HashMap)
     */
    public String cacheUserData(final HashMap attributes) {

        if (attributes == null) {
            throw new IllegalArgumentException("attributes cannot be null");
        }

        CachedUserData userData = new CachedUserData(attributes);
        /* Create new SSO ticket with null-value servicePrincipal. */
        MoriaTicket ssoTicket = new MoriaTicket(MoriaTicketType.SSO_TICKET, null, ssoTicketTTL);
        insertIntoStore(ssoTicket, null, userData);

        return ssoTicket.getTicketId();
    }

    /**
     * @see no.feide.moria.store.MoriaStore#getUserData(java.lang.String)
     */
    public CachedUserData getUserData(final String ticketId) throws InvalidTicketException {

        /* Validate argument */
        if (ticketId == null || ticketId.equals("")) {
            throw new IllegalArgumentException("loginTicketId must be a non-empty string");
        }

        MoriaTicketType[] potentialTicketTypes = new MoriaTicketType[] {MoriaTicketType.SSO_TICKET,
                MoriaTicketType.TICKET_GRANTING_TICKET, MoriaTicketType.PROXY_TICKET};

        CachedUserData cachedUserData = null;

        /* Itterate over the potential ticket types looking for an ticket instance. */
        for (int i = 0; i < potentialTicketTypes.length; i++) {

            Fqn fqn = new Fqn(new Object[] {potentialTicketTypes[i], ticketId});

            try {
                if (store.exists(fqn)) {
                    Node node = store.get(fqn);

                    /* Check node/ticket validity (TTL). */
                    MoriaTicket ticket = new MoriaTicket(ticketId, potentialTicketTypes[i], (String) node.get(PRINCIPAL_ATTRIBUTE),
                            (Long) node.get(TTL_ATTRIBUTE));
                    validateTicket(ticket, potentialTicketTypes[i], null);

                    Object dataObject = node.get(DATA_ATTRIBUTE);
                    /* cachedUserData will remain null if the returned object is of incorrect type. */
                    if (dataObject instanceof CachedUserData) {
                        cachedUserData = (CachedUserData) dataObject;
                    }
                    /* Break the for-loop if a ticket was found. */
                    break;
                }
            } catch (LockingException e) {
                messageLogger.logCritical("LockingException thrown by store. Rethrowing as RuntimeException.", e);
                throw new RuntimeException(e);
            } catch (TimeoutException e) {
                messageLogger.logCritical("TimeoutException thrown by store. Rethrowing as RuntimeException.", e);
                throw new RuntimeException(e);
            }
        }
        return cachedUserData;
    }

    /**
     * @see no.feide.moria.store.MoriaStore#createServiceTicket(java.lang.String, java.lang.String)
     */
    public String createServiceTicket(final String loginTicketId, final String targetServicePrincipal)
            throws InvalidTicketException {

        /* Validate arguments */
        if (loginTicketId == null || loginTicketId.equals("")) {
            throw new IllegalArgumentException("loginTicketId must be a non-empty string");
        }
        if (targetServicePrincipal == null || targetServicePrincipal.equals("")) {
            throw new IllegalArgumentException("servicePrincipal must be a non-empty string");
        }

        MoriaTicket loginTicket = getTicketFromStore(MoriaTicketType.LOGIN_TICKET, loginTicketId);
        validateTicket(loginTicket, MoriaTicketType.LOGIN_TICKET, targetServicePrincipal);

        /*
         * Create new service ticket and assosiate it with the same authentication attempt as the
         * login ticket
         */
        MoriaTicket serviceTicket = new MoriaTicket(MoriaTicketType.SERVICE_TICKET, loginTicket.getServicePrincipal(),
                serviceTicketTTL);
        insertIntoStore(serviceTicket, getAuthnAttempt(loginTicketId, true), null);
        return serviceTicket.getTicketId();
    }

    /**
     * @see no.feide.moria.store.MoriaStore#createTicketGrantingTicket(java.lang.String,
     *      java.lang.String)
     */
    public String createTicketGrantingTicket(final String ticketId, final String targetServicePrincipal)
            throws InvalidTicketException {

        /* Validate arguments */
        if (ticketId == null || ticketId.equals("")) {
            throw new IllegalArgumentException("ticketId must be a non-empty string");
        }

        if (targetServicePrincipal == null || targetServicePrincipal.equals("")) {
            throw new IllegalArgumentException("servicePrincipal must be a non-empty string");
        }

        /*
         * Create new ticket granting ticket and assosiate it with the same user data as the sso
         * ticket.
         */
        MoriaTicket tgTicket;
        CachedUserData userData = getUserData(ticketId);

        if (userData == null) {
            throw new InvalidTicketException("SSO ticket has no data assosiated with it");
        }

        tgTicket = new MoriaTicket(MoriaTicketType.TICKET_GRANTING_TICKET, targetServicePrincipal, tgTicketTTL);
        insertIntoStore(tgTicket, null, userData);

        return tgTicket.getTicketId();
    }

    /**
     * @see no.feide.moria.store.MoriaStore#createProxyTicket(java.lang.String, java.lang.String, java.lang.String)
     */
    public String createProxyTicket(final String tgTicketId, final String servicePrincipal, final String targetServicePrincipal)
            throws InvalidTicketException {

        /* Validate arguments */
        if (tgTicketId == null || tgTicketId.equals("")) {
            throw new IllegalArgumentException("tgTicketId must be a non-empty string");
        }

        if (servicePrincipal == null || servicePrincipal.equals("")) {
            throw new IllegalArgumentException("servicePrincipal must be a non-empty string");
        }

        if (targetServicePrincipal == null || targetServicePrincipal.equals("")) {
            throw new IllegalArgumentException("targetServicePrincipal must be a non-empty string");
        }

        MoriaTicket tgTicket = getTicketFromStore(MoriaTicketType.TICKET_GRANTING_TICKET, tgTicketId);

        if (tgTicket == null) {
            return null;
        }

        validateTicket(tgTicket, MoriaTicketType.TICKET_GRANTING_TICKET, servicePrincipal);
        CachedUserData userData = getUserData(tgTicketId);

        if (userData == null) {
            return null;
        }

        MoriaTicket proxyTicket = new MoriaTicket(MoriaTicketType.PROXY_TICKET, targetServicePrincipal, proxyTicketTTL);
        insertIntoStore(proxyTicket, null, userData);

        return proxyTicket.getTicketId();
    }

    /**
     * @see no.feide.moria.store.MoriaStore#setTransientAttributes(java.lang.String, java.util.HashMap)
     */
    public void setTransientAttributes(final String loginTicketId, final HashMap transientAttributes) throws InvalidTicketException {
        /* Validate arguments */
        if (loginTicketId == null || loginTicketId.equals("")) {
            throw new IllegalArgumentException("loginTicketId must be a non-empty string.");
        }

        if (transientAttributes == null) {
            throw new IllegalArgumentException("transientAttributes cannot be null.");
        }

        MoriaTicket loginTicket = getTicketFromStore(MoriaTicketType.LOGIN_TICKET, loginTicketId);

        if (loginTicket == null) {
            String message = "Ticket does not exist.";
            messageLogger.logWarn(message, loginTicketId);
            throw new InvalidTicketException(message);
        }

        validateTicket(loginTicket, MoriaTicketType.LOGIN_TICKET, null);
        MoriaAuthnAttempt authnAttempt = getAuthnAttempt(loginTicketId, true);

        if (authnAttempt == null) {
            String message = "AuthenticationAttempt does not exist.";
            messageLogger.logWarn(message, loginTicketId);
            throw new IllegalStateException(message);
        }

        authnAttempt.setTransientAttributes(transientAttributes);

        /* Insert into cache again to trigger distributed update. */
        insertIntoStore(loginTicket, authnAttempt, null);
    }

    /**
     * Check validity of ticket against type and expiry time.
     *
     * @param ticket
     *          ticket to be checked
     * @param type
     *          the expected type of the ticket
     * @param servicePrincipal
     *          the service expected to be assosiated with this ticket
     * @throws InvalidTicketException
     *          thrown if ticket is found invalid
     * @throws IllegalArgumentException
     *          if ticket is null
     */
    private void validateTicket(final MoriaTicket ticket, final MoriaTicketType type, final String servicePrincipal)
            throws InvalidTicketException {
        validateTicket(ticket, new MoriaTicketType[] {type}, servicePrincipal);
    }

    /**
     * Check validity of ticket against a set of types and expiry time.
     *
     * @param ticket
     *          ticket to be checked
     * @param types
     *          array of valid types for the ticket
     * @param servicePrincipal
     *          the service that is using the ticket. May be null if no
     *          service is available.
     * @throws InvalidTicketException
     *          thrown if the ticket is found to be invalid
     * @throws IllegalArgumentException
     *          if ticket is null
     */
    private void validateTicket(final MoriaTicket ticket, final MoriaTicketType[] types, final String servicePrincipal)
            throws InvalidTicketException {

        /* Validate arguments. */
        if (ticket == null) {
            throw new IllegalArgumentException("ticket cannot be null.");
        }

        if (types == null) {
            throw new IllegalArgumentException("types cannot be null.");
        }

        /*
         * Check if it still is valid. We let the dedicated vacuming-service take care of
         * removing it at later time, so we just throw an exception.
         */
        if (ticket.hasExpired()) {
            String message = "Ticket has expired.";
            messageLogger.logInfo(message, ticket.getTicketId());
            throw new InvalidTicketException(message);
        }

        /* Authorize the caller. */
        if (servicePrincipal != null && !ticket.getServicePrincipal().equals(servicePrincipal)) {
            String message = "Illegal use of ticket by service: " + servicePrincipal;
            messageLogger.logWarn(message, ticket.getTicketId());
            throw new InvalidTicketException(message);
        }

        /* Loop through ticket types until valid type found. */
        boolean valid = false;

        for (int i = 0; i < types.length; i++) {
            if (ticket.getTicketType().equals(types[i])) {
                valid = true;
                break;
            }
        }

        /* Throw exception if all types were invalid. */
        if (!valid) {
            String message = "Ticket has wrong type: " + ticket.getTicketType();
            messageLogger.logWarn(message, ticket.getTicketId());
            throw new InvalidTicketException(message);
        }
    }

    /**
     * Retrives a ticket instance from the store.
     *
     * @param ticketType
     *          the type of the ticket
     * @param ticketId
     *          the id of the ticket
     * @return a ticket instance
     */
    MoriaTicket getTicketFromStore(final MoriaTicketType ticketType, final String ticketId) {

        /* Validate parameters. */
        if (ticketType == null) {
            throw new IllegalArgumentException("ticketType cannot be null.");
        }

        if (ticketId == null || ticketId.equals("")) {
            throw new IllegalArgumentException("ticketId must be a non-empty string.");
        }

        /* The name of the node to be retrived. */
        Fqn fqn = new Fqn(new Object[] {ticketType, ticketId});

        /* The object to hold the node that will be retrived from the store. */
        Node node;

        try {
            node = store.get(fqn);
        } catch (LockingException e) {
            messageLogger.logCritical("LockingException thrown by store. Rethrowing as RuntimeException.", e);
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            messageLogger.logCritical("TimeoutException thrown by store. Rethrowing as RuntimeException.", e);
            throw new RuntimeException(e);
        }

        if (node == null) {
            return null;
        } else {
            return new MoriaTicket(ticketId, (MoriaTicketType) node.get(TICKET_TYPE_ATTRIBUTE), (String) node
                    .get(PRINCIPAL_ATTRIBUTE), (Long) node.get(TTL_ATTRIBUTE));
        }
    }
}
