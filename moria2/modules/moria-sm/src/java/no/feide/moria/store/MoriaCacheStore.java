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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Properties;

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

    /** The configured state of the store. */
    private boolean isConfigured = false;

    /** The logger used by this class. */
    private MessageLogger messageLogger = new MessageLogger(MoriaCacheStore.class);

    /** The time to live for different tickets. */
    private Long loginTicketTTL, serviceTicketTTL, ssoTicketTTL, tgTicketTTL, proxyTicketTTL;

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
     * @throws MoriaStoreException
     *          thrown if creation of JBoss TreeCache fails.
     */
    public MoriaCacheStore() throws MoriaStoreException {

        try {
            store = new TreeCache();
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new MoriaStoreException("Unable to create TreeCache instance.", e);
        }
    }

    /**
     * Configures the store.
     *
     * This method expects the property "no.feide.moria.store.cacheconf" to be
     * set and point to a JBossCache spesific configuration file.
     *
     * The method will return without actually executing and thus maintaining the current
     * state if called more than once per object instance.
     *
     * @param properties
     *          the properties used to configure the store
     * @throws IllegalArgumentException
     *          if properties is null
     */
    public void setConfig(final Properties properties) {

        /* Return if this cache instance has already been configured once. */
        if (isConfigured) {
            messageLogger.logWarn("setConfig() called on already configured instance.");
            return;
        }

        /* Throw exception if argument is null. */
        if (properties == null) {
            throw new IllegalArgumentException("properties cannot be null.");
        }

        /* Read the the file name from the properties. */
        String cacheConfigPropertyName = "no.feide.moria.store.cachestoreconf";
        String cacheConfigProperty = properties.getProperty(cacheConfigPropertyName);

        if (cacheConfigProperty == null) {
            throw new MoriaStoreConfigurationException(cacheConfigPropertyName + " must be set.");
        }

        /* Open the file. */
        FileInputStream cacheConfigFile;

        try {
            cacheConfigFile = new FileInputStream(cacheConfigProperty);
        } catch (FileNotFoundException fnnf) {
            String message = "The configuration file for the store was not found.";
            messageLogger.logCritical(message, fnnf);
            throw new MoriaStoreConfigurationException(message, fnnf);
        }

        /* Configure the cache. */
        PropertyConfigurator configurator = new PropertyConfigurator();

        try {
            configurator.configure(store, cacheConfigFile);
        } catch (Exception e) {
            String message = "Unable to configure the cache.";
            messageLogger.logCritical(message, e);
            throw new MoriaStoreConfigurationException(message, e);
        }

        // TODO: Get values from config
        loginTicketTTL = new Long(300); /* 5 min */
        serviceTicketTTL = new Long(300); /* 5 min */
        ssoTicketTTL = new Long(28800); /* 8 hours */
        tgTicketTTL = new Long(7200); /* 2 hours */
        proxyTicketTTL = new Long(300); /* 5 min */

        /* Start the configured cache. */
        try {
            store.start();
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new MoriaStoreConfigurationException("Unable to start the cache", e);
        }

        /* Set the configuration state of this instance to true. */
        isConfigured = true;
    }

    /**
     * @see no.feide.moria.store.MoriaStore#createAuthnAttempt(java.lang.String[], java.lang.String,
     *      java.lang.String, boolean, java.lang.String)
     */
    public String createAuthnAttempt(final String[] requestedAttributes, final String responseURLPrefix,
            final String responseURLPostfix, final boolean forceInteractiveAuthentication, final String servicePrincipal)
            throws MoriaStoreException {

        MoriaTicket ticket = null;
        MoriaAuthnAttempt authnAttempt;

        if (requestedAttributes == null) {
            throw new IllegalArgumentException("requestedAttributes cannot be null.");
        }

        if (responseURLPrefix == null || responseURLPrefix.equals("")) {
            throw new IllegalArgumentException("responseURLPrefix cannot be null or empty string.");
        }

        if (responseURLPostfix == null) {
            throw new IllegalArgumentException("responseURLPostfix cannot be null.");
        }

        if (servicePrincipal == null || servicePrincipal.equals("")) {
            throw new IllegalArgumentException("servicePrincipal cannot be null or empty string.");
        }

        authnAttempt = new MoriaAuthnAttempt(requestedAttributes, responseURLPrefix, responseURLPostfix,
                forceInteractiveAuthentication, servicePrincipal);

        ticket = new MoriaTicket(MoriaTicketType.LOGIN_TICKET, servicePrincipal, loginTicketTTL, authnAttempt);

        insertIntoStore(ticket);

        return ticket.getTicketId();
    }

    /**
     * @see no.feide.moria.store.MoriaStore#getAuthnAttempt(java.lang.String, boolean, java.lang.String)
     */
    public MoriaAuthnAttempt getAuthnAttempt(final String ticketId, final boolean keep, final String servicePrincipal)
            throws InvalidTicketException, NonExistentTicketException, MoriaStoreException {

        /* Validate ticketId. */
        if (ticketId == null || ticketId.equals("")) {
            throw new IllegalArgumentException("loginTicketId must be a non-empty string.");
        }

        MoriaTicketType[] potentialTicketTypes = new MoriaTicketType[] {MoriaTicketType.LOGIN_TICKET,
                MoriaTicketType.SERVICE_TICKET};

        MoriaTicket ticket = getFromStore(potentialTicketTypes, ticketId);

        if (ticket == null) {
            throw new NonExistentTicketException();
        }

        if (ticket.getTicketType().equals(MoriaTicketType.LOGIN_TICKET)) {
            validateTicket(ticket, MoriaTicketType.LOGIN_TICKET, null);
        } else {
            /* Validate servicePrincipal. Can not be null for service tickets. */
            if (servicePrincipal == null || servicePrincipal.equals("")) {
                throw new IllegalArgumentException("servicePrincipal must be a non-empty string for service tickets.");
            }
            validateTicket(ticket, MoriaTicketType.SERVICE_TICKET, servicePrincipal);
        }

        MoriaAuthnAttempt authnAttempt = null;

        MoriaStoreData data = ticket.getData();

        if (data != null && data instanceof MoriaAuthnAttempt) {
            authnAttempt = (MoriaAuthnAttempt) data;
        } else {
            throw new InvalidTicketException("No authentication attempt associated with ticket.");
        }

        /* Delete the ticket if so indicated. */
        if (!keep) {
            removeFromStore(ticket);
        }

        return authnAttempt;
    }

    /**
     * @see no.feide.moria.store.MoriaStore#cacheUserData(java.util.HashMap)
     */
    public String cacheUserData(final HashMap attributes) throws MoriaStoreException {

        /* Validate argument. */
        if (attributes == null) {
            throw new IllegalArgumentException("attributes cannot be null");
        }

        CachedUserData userData = new CachedUserData(attributes);
        /* Create new SSO ticket with null-value servicePrincipal. */
        MoriaTicket ssoTicket = new MoriaTicket(MoriaTicketType.SSO_TICKET, null, ssoTicketTTL, userData);
        insertIntoStore(ssoTicket);

        return ssoTicket.getTicketId();
    }

    /**
     * @see no.feide.moria.store.MoriaStore#getUserData(java.lang.String, java.lang.String)
     */
    public CachedUserData getUserData(final String ticketId, final String servicePrincipal) throws NonExistentTicketException,
            InvalidTicketException, MoriaStoreException {

        /* Validate argument. */
        if (ticketId == null || ticketId.equals("")) {
            throw new IllegalArgumentException("loginTicketId must be a non-empty string.");
        }

        MoriaTicketType[] potentialTicketTypes = new MoriaTicketType[] {MoriaTicketType.SSO_TICKET,
                MoriaTicketType.TICKET_GRANTING_TICKET, MoriaTicketType.PROXY_TICKET};

        MoriaTicket ticket = getFromStore(potentialTicketTypes, ticketId);

        if (ticket == null) {
            throw new NonExistentTicketException();
        }

        if (!ticket.getTicketType().equals(MoriaTicketType.SSO_TICKET)) {
            if (servicePrincipal == null || servicePrincipal.equals("")) {
                throw new IllegalArgumentException("servicePrincipal must be a non-empty string for this ticket type.");
            }
        }

        validateTicket(ticket, potentialTicketTypes, servicePrincipal);

        CachedUserData cachedUserData = null;

        MoriaStoreData data = ticket.getData();

        if (data != null && data instanceof CachedUserData) {
            cachedUserData = (CachedUserData) data;
        } else {
            throw new InvalidTicketException("No user data associated with ticket.");
        }

        return cachedUserData;
    }

    /**
     * @see no.feide.moria.store.MoriaStore#createServiceTicket(java.lang.String)
     */
    public String createServiceTicket(final String loginTicketId) throws InvalidTicketException, NonExistentTicketException,
            MoriaStoreException {

        /* Validate argument. */
        if (loginTicketId == null || loginTicketId.equals("")) {
            throw new IllegalArgumentException("loginTicketId must be a non-empty string");
        }

        MoriaTicket loginTicket = getFromStore(MoriaTicketType.LOGIN_TICKET, loginTicketId);

        if (loginTicket == null) {
            throw new NonExistentTicketException();
        }

        /* Primarily to check timestamp. */
        validateTicket(loginTicket, MoriaTicketType.LOGIN_TICKET, null);

        /*
         * Create new service ticket and associate it with the same authentication
         * attempt as the login ticket.
         */
        MoriaAuthnAttempt authnAttempt = null;

        MoriaStoreData data = loginTicket.getData();

        if (data != null && data instanceof MoriaAuthnAttempt) {
            authnAttempt = (MoriaAuthnAttempt) data;
        } else {
            throw new InvalidTicketException("No authentication attempt associated with login ticket.");
        }

        MoriaTicket serviceTicket = new MoriaTicket(MoriaTicketType.SERVICE_TICKET, loginTicket.getServicePrincipal(),
                serviceTicketTTL, authnAttempt);
        insertIntoStore(serviceTicket);
        /*  Delete the now used login ticket. */
        removeFromStore(loginTicket);

        return serviceTicket.getTicketId();
    }

    /**
     * @see no.feide.moria.store.MoriaStore#createTicketGrantingTicket(java.lang.String,
     *      java.lang.String)
     */
    public String createTicketGrantingTicket(final String ssoTicketId, final String targetServicePrincipal)
            throws InvalidTicketException, NonExistentTicketException, MoriaStoreException {

        /* Validate arguments. */
        if (ssoTicketId == null || ssoTicketId.equals("")) {
            throw new IllegalArgumentException("ticketId must be a non-empty string");
        }

        if (targetServicePrincipal == null || targetServicePrincipal.equals("")) {
            throw new IllegalArgumentException("servicePrincipal must be a non-empty string");
        }

        MoriaTicket ssoTicket = getFromStore(MoriaTicketType.SSO_TICKET, ssoTicketId);

        if (ssoTicket == null) {
            throw new NonExistentTicketException();
        }

        /* Primarily to check timestamp. */
        validateTicket(ssoTicket, MoriaTicketType.SSO_TICKET, null);

        /*
         * Create new ticket granting ticket and associate it with the same
         * user data as the SSO ticket.
         */
        CachedUserData cachedUserData = null;

        MoriaStoreData data = ssoTicket.getData();

        if (data != null && data instanceof CachedUserData) {
            cachedUserData = (CachedUserData) data;
        } else {
            throw new InvalidTicketException("No user data associated with SSO ticket.");
        }

        MoriaTicket tgTicket = new MoriaTicket(MoriaTicketType.TICKET_GRANTING_TICKET, targetServicePrincipal, tgTicketTTL,
                cachedUserData);
        insertIntoStore(tgTicket);

        return tgTicket.getTicketId();
    }

    /**
     * @see no.feide.moria.store.MoriaStore#createProxyTicket(java.lang.String, java.lang.String, java.lang.String)
     */
    public String createProxyTicket(final String tgTicketId, final String servicePrincipal, final String targetServicePrincipal)
            throws InvalidTicketException, NonExistentTicketException, MoriaStoreException {

        /* Validate arguments. */
        if (tgTicketId == null || tgTicketId.equals("")) {
            throw new IllegalArgumentException("tgTicketId must be a non-empty string.");
        }

        if (servicePrincipal == null || servicePrincipal.equals("")) {
            throw new IllegalArgumentException("servicePrincipal must be a non-empty string.");
        }

        if (targetServicePrincipal == null || targetServicePrincipal.equals("")) {
            throw new IllegalArgumentException("targetServicePrincipal must be a non-empty string.");
        }

        MoriaTicket tgTicket = getFromStore(MoriaTicketType.TICKET_GRANTING_TICKET, tgTicketId);

        if (tgTicket == null) {
            throw new NonExistentTicketException();
        }

        /* Primarily to check timestamp. */
        validateTicket(tgTicket, MoriaTicketType.TICKET_GRANTING_TICKET, servicePrincipal);

        /*
         * Create new ticket granting ticket and associate it with the same
         * user data as the TG ticket.
         */
        CachedUserData cachedUserData = null;

        MoriaStoreData data = tgTicket.getData();

        if (data != null && data instanceof CachedUserData) {
            cachedUserData = (CachedUserData) data;
        } else {
            throw new InvalidTicketException("No user data associated with ticket granting ticket.");
        }

        MoriaTicket proxyTicket = new MoriaTicket(MoriaTicketType.PROXY_TICKET, targetServicePrincipal, proxyTicketTTL,
                cachedUserData);
        insertIntoStore(proxyTicket);

        return proxyTicket.getTicketId();
    }

    /**
     * @see no.feide.moria.store.MoriaStore#setTransientAttributes(java.lang.String, java.util.HashMap)
     */
    public void setTransientAttributes(final String loginTicketId, final HashMap transientAttributes)
            throws InvalidTicketException, NonExistentTicketException, MoriaStoreException {

        /* Validate arguments. */
        if (loginTicketId == null || loginTicketId.equals("")) {
            throw new IllegalArgumentException("loginTicketId must be a non-empty string.");
        }

        if (transientAttributes == null) {
            throw new IllegalArgumentException("transientAttributes cannot be null.");
        }

        MoriaTicket loginTicket = getFromStore(MoriaTicketType.LOGIN_TICKET, loginTicketId);

        if (loginTicket == null) {
            throw new NonExistentTicketException();
        }

        /* Primarily to check timestamp. */
        validateTicket(loginTicket, MoriaTicketType.LOGIN_TICKET, null);

        MoriaAuthnAttempt authnAttempt = null;

        MoriaStoreData data = loginTicket.getData();

        if (data != null && data instanceof MoriaAuthnAttempt) {
            authnAttempt = (MoriaAuthnAttempt) data;
        } else {
            throw new InvalidTicketException("No authentication attempt associated with login ticket.");
        }

        authnAttempt.setTransientAttributes(transientAttributes);

        /* Insert into cache again to trigger distributed update. */
        insertIntoStore(loginTicket);
    }

    /**
     * @see no.feide.moria.store.MoriaStore#setTransientAttributes(java.lang.String, java.lang.String)
     */
    public void setTransientAttributes(final String loginTicketId, final String ssoTicketId) throws InvalidTicketException,
            NonExistentTicketException, MoriaStoreException {

        /* Validate arguments. */
        if (loginTicketId == null || loginTicketId.equals("")) {
            throw new IllegalArgumentException("loginTicketId must be a non-empty string.");
        }

        if (ssoTicketId == null || ssoTicketId.equals("")) {
            throw new IllegalArgumentException("ssoTicketId must be a non-empty string.");
        }

        MoriaTicket loginTicket = getFromStore(MoriaTicketType.LOGIN_TICKET, loginTicketId);

        if (loginTicket == null) {
            throw new NonExistentTicketException();
        }

        MoriaTicket ssoTicket = getFromStore(MoriaTicketType.SSO_TICKET, ssoTicketId);

        if (ssoTicket == null) {
            throw new NonExistentTicketException();
        }

        /* Primarily to check timestamp. */
        validateTicket(loginTicket, MoriaTicketType.LOGIN_TICKET, null);
        validateTicket(ssoTicket, MoriaTicketType.SSO_TICKET, null);

        CachedUserData cachedUserData = null;
        MoriaAuthnAttempt authnAttempt = null;

        MoriaStoreData ssoData = ssoTicket.getData();

        if (ssoData != null && ssoData instanceof CachedUserData) {
            cachedUserData = (CachedUserData) ssoData;
        } else {
            throw new InvalidTicketException("No cached user data associated with sso ticket.");
        }

        MoriaStoreData loginData = loginTicket.getData();

        if (loginData != null && loginData instanceof MoriaAuthnAttempt) {
            authnAttempt = (MoriaAuthnAttempt) loginData;
        } else {
            throw new InvalidTicketException("No authentication attempt associated with login ticket.");
        }

        /* Transfer cached userdata to login attempt. */
        authnAttempt.setTransientAttributes(cachedUserData.getAttributes());

        /* Insert into cache again to trigger distributed update. */
        insertIntoStore(loginTicket);
    }

    /**
     * @see no.feide.moria.store.MoriaStore#removeSSOTicket(java.lang.String)
     */
    public void removeSSOTicket(final String ssoTicketId) throws InvalidTicketException, NonExistentTicketException,
            MoriaStoreException {

        /* Validate parameter. */
        if (ssoTicketId == null || ssoTicketId.equals("")) {
            throw new IllegalArgumentException("ticketType cannot be null.");
        }

        MoriaTicket ssoTicket = getFromStore(MoriaTicketType.SSO_TICKET, ssoTicketId);

        if (ssoTicket != null) {
            removeFromStore(ssoTicket);
        } else {
            throw new NonExistentTicketException();
        }
    }

    /**
     * Check validity of ticket against type and expiry time.
     *
     * @param ticket
     *          ticket to be checked
     * @param ticketType
     *          the expected type of the ticket
     * @param servicePrincipal
     *          the service expected to be associated with this ticket
     * @throws IllegalArgumentException
     *          if ticket is null, or ticketType is null or zero length
     * @throws InvalidTicketException
     *          thrown if ticket is found invalid
     */
    private void validateTicket(final MoriaTicket ticket, final MoriaTicketType ticketType, final String servicePrincipal)
            throws InvalidTicketException {
        validateTicket(ticket, new MoriaTicketType[] {ticketType}, servicePrincipal);
    }

    /**
     * Check validity of ticket against a set of types and expiry time.
     *
     * @param ticket
     *          ticket to be checked
     * @param ticketTypes
     *          array of valid types for the ticket
     * @param servicePrincipal
     *          the service that is using the ticket. May be null if no
     *          service is available.
     * @throws IllegalArgumentException
     *          if ticket is null, or ticketType is null or zero length
     * @throws InvalidTicketException
     *          thrown if the ticket is found to be invalid
     */
    private void validateTicket(final MoriaTicket ticket, final MoriaTicketType[] ticketTypes, final String servicePrincipal)
            throws InvalidTicketException {

        /* Validate arguments. */
        if (ticket == null) {
            throw new IllegalArgumentException("ticket cannot be null.");
        }

        if (ticketTypes == null || ticketTypes.length < 1) {
            throw new IllegalArgumentException("ticketTypes cannot be null or zero length.");
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

        for (int i = 0; i < ticketTypes.length; i++) {
            if (ticket.getTicketType().equals(ticketTypes[i])) {
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
     * Retrives a ticket instance which may be one of a number of types.
     *
     * @param ticketTypes
     *          array of potential ticket types for the ticket id
     * @param ticketId
     *          id of the ticket to be retrived
     * @return a ticket or null of none found
     * @throws IllegalArgumentException
     *          if the any of arguments are null value or zero length
     * @throws MoriaStoreException
     *          if access to the store failed in some way.
     */
    MoriaTicket getFromStore(final MoriaTicketType[] ticketTypes, final String ticketId) throws MoriaStoreException {

        /* Validate parameters. */
        if (ticketTypes == null || ticketTypes.length < 1) {
            throw new IllegalArgumentException("ticketTypes cannot be null or zero length.");
        }

        if (ticketId == null || ticketId.equals("")) {
            throw new IllegalArgumentException("ticketId must be a non-empty string.");
        }

        MoriaTicket ticket = null;

        /* Itterate of type array. Break if ticket is returned. */
        for (int i = 0; i < ticketTypes.length; i++) {
            ticket = getFromStore(ticketTypes[i], ticketId);
            if (ticket != null)
                break;
        }

        return ticket;
    }

    /**
     * Retrives a ticket instance from the store.
     *
     * @param ticketType
     *          the type of the ticket
     * @param ticketId
     *          the id of the ticket
     * @return a ticket instance
     * @throws IllegalArgumentException
     *          if ticketType is null, or ticketId is null or an empty string
     * @throws MoriaStoreException
     *          thrown if operations on the TreeCache fails
     */
    MoriaTicket getFromStore(final MoriaTicketType ticketType, final String ticketId) throws MoriaStoreException {

        /* Validate parameters. */
        if (ticketType == null) {
            throw new IllegalArgumentException("ticketType cannot be null.");
        }

        if (ticketId == null || ticketId.equals("")) {
            throw new IllegalArgumentException("ticketId must be a non-empty string.");
        }

        /* The name of the node to be retrived. */
        Fqn fqn = new Fqn(new Object[] {ticketType, ticketId});

        /* Return null if the node does not exist. */
        if (store.exists(fqn)) {
            /* The object to hold the node that will be retrived from the store. */
            Node node;

            try {
                node = store.get(fqn);
            } catch (LockingException e) {
                throw new MoriaStoreException("Locking of store failed", e);
            } catch (TimeoutException e) {
                throw new MoriaStoreException("Access to store timed out", e);
            }

            if (node == null) {
                return null;
            } else {
                return new MoriaTicket(ticketId, (MoriaTicketType) node.get(TICKET_TYPE_ATTRIBUTE), (String) node
                        .get(PRINCIPAL_ATTRIBUTE), (Long) node.get(TTL_ATTRIBUTE), (MoriaStoreData) node.get(DATA_ATTRIBUTE));
            }
        }

        /* Return null if node isn't found. */
        return null;
    }

    /**
     * Insert a authentication attempt or cached user data into the cache. Either authnAttempt or
     * cachedUserData must be null.
     *
     * @param ticket
     *          the ticket to connect to the inserted object
     * @throws IllegalArgumentException
     *          if ticket is null
     * @throws MoriaStoreException
     *          thrown if operations on the TreeCache fails
     */
    private void insertIntoStore(final MoriaTicket ticket) throws MoriaStoreException {

        /* Validate parameters */
        if (ticket == null) {
            throw new IllegalArgumentException("ticket cannot be null.");
        }

        Fqn fqn = new Fqn(new Object[] {ticket.getTicketType(), ticket.getTicketId()});

        HashMap attributes = new HashMap();
        attributes.put(TICKET_TYPE_ATTRIBUTE, ticket.getTicketType());
        attributes.put(TTL_ATTRIBUTE, ticket.getExpiryTime());
        attributes.put(PRINCIPAL_ATTRIBUTE, ticket.getServicePrincipal());
        attributes.put(DATA_ATTRIBUTE, ticket.getData());

        try {
            store.put(fqn, attributes);
        } catch (RuntimeException re) {
            throw re;
        } catch (Exception e) {
            throw new MoriaStoreException("Insertion into store failed.", e);
        }
    }

    /**
     * Removes a ticket, and possibly a connected userdata or authnAttempt from the cache.
     *
     * @param ticket
     *          the ticket to be removed
     * @throws IllegalArgumentException
     *          if ticket is null
     * @throws NonExistentTicketException
     *          if the ticket does not exist
     * @throws MoriaStoreException
     *          if an exception is thrown when operating on the store
     */
    private void removeFromStore(final MoriaTicket ticket) throws NonExistentTicketException, MoriaStoreException {

        /* Validate parameters. */
        if (ticket == null) {
            throw new IllegalArgumentException("ticketId cannot be null.");
        }

        Fqn fqn = new Fqn(new Object[] {ticket.getTicketType(), ticket.getTicketId()});

        if (store.exists(fqn)) {
            try {
                store.remove(fqn);
            } catch (RuntimeException re) {
                throw re;
            } catch (Exception e) {
                throw new MoriaStoreException("Removal from store failed.", e);
            }
        } else {
            throw new NonExistentTicketException();
        }
    }
}
