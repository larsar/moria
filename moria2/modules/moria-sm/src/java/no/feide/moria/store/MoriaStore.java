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
import java.util.Properties;

/**
 * @author Bjørn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public interface MoriaStore {

    /**
     * Set the configuration of the store.
     *
     * @param properties
     *          object containing the necessary attributes for store configuration
     * @throws MoriaStoreConfigurationException
     *          if the store cannot be started with the give configuration
     * @throws IllegalArgumentException
     *          if properties is null
     */
    void setConfig(Properties properties) throws MoriaStoreConfigurationException;

    /**
     * Creates an authentication attempt based on a service request.
     *
     * @param requestAttributes
     *          the user attributes the requesting service asks for
     * @param responseURLPrefix
     *          the forward part of the url the client is to be redirected to
     * @param responseURLPostfix
     *          the end part of the url the client is to be redirected to
     * @param forceInteractiveAuthentication
     *          if the user should be forced to login interactivly. I.e. disable
     *          support for single sign-on
     * @param servicePrincipal
     *          the id of the service doing the request
     * @return a login ticket identifying the authentication attempt
     * @throws MoriaStoreException
     *          thrown if the operation fails
     * @throws IllegalArgumentException
     *          if any of the arguments are null, and if responseURLPrefix and servicePrincipal are zero length
     */
    String createAuthnAttempt(final String[] requestAttributes, final String responseURLPrefix, final String responseURLPostfix,
            final boolean forceInteractiveAuthentication, final String servicePrincipal) throws MoriaStoreException;

    /**
     * Gets the authentication attempt assosiated with the ticket given as argument.
     *
     * @param ticketId
     *          the ticket from the incoming client request (must be LOGIN or SERVICE)
     * @param keep
     *          if true the authnAttempt and ticket will be kept in the store after this operation
     * @param servicePrincipal
     *          the principal of the service requesting the operation (null if login ticket is supplied)
     * @return the MoriaAuthnAttempt assosiated with the ticket
     * @throws InvalidTicketException
     *          if the incoming ticket is found to be invalid
     * @throws NonExistentTicketException
     *          thrown if ticket does not exist
     * @throws MoriaStoreException
     *          thrown if the operation fails
     * @throws IllegalArgumentException
     *          if ticketId is null or zero length and if no servicePrincipal is
     *          supplied with a service ticket
     */
    MoriaAuthnAttempt getAuthnAttempt(final String ticketId, final boolean keep, final String servicePrincipal)
            throws InvalidTicketException, NonExistentTicketException, MoriaStoreException;

    /**
     * Creates a new CachedUserData object in the underlying store and assosiates it with a SSO
     * ticket which is returned.
     *
     * @param attributes
     *          the attribute map to be cached
     * @return the SSO ticket that identifies the cached user data
     * @throws MoriaStoreException
     *          thrown if the operation fails
     * @throws IllegalArgumentException
     *          if attributes is null
     */
    String cacheUserData(final HashMap attributes) throws MoriaStoreException;

    /**
     * Return the userdata assosiated with the incoming ticket, which must be either a
     * proxy ticket.
     *
     * @param proxyTicketId
     *          a ticket to identify a userdata object (SSO, TG or PROXY)
     * @param servicePrincipal
     *          the name of the service requesting the data
     * @return a clone of the object containing the userdata
     * @throws InvalidTicketException
     *          thrown if the incoming ticket is not of the correct type or
     *          has an invalid principal
     * @throws NonExistentTicketException
     *          thrown if ticket does not exist
     * @throws MoriaStoreException
     *          thrown if the operation fails
     * @throws IllegalArgumentException
     *          if ticketId is null or zero length
     */
    CachedUserData getUserData(final String proxyTicketId, final String servicePrincipal) throws InvalidTicketException,
            NonExistentTicketException, MoriaStoreException;

    /**
     * Creates a service ticket that the service will use when requesting user attributes after a
     * successfull authentication.
     *
     * @param loginTicketId
     *          a login ticket assosiated with an authentication attempt
     * @return a service ticket assosiated with the authentication attempt object
     * @throws InvalidTicketException
     *          thrown if the argument ticket is not a login-ticket
     * @throws NonExistentTicketException
     *          thrown if ticket does not exist
     * @throws MoriaStoreException
     *          thrown if the operation fails
     * @throws IllegalArgumentException
     *          if loginTicketId is null or zero length
     */
    String createServiceTicket(final String loginTicketId) throws InvalidTicketException, NonExistentTicketException,
            MoriaStoreException;

    /**
     * Create a new ticket granting ticket, using a sso ticket.
     *
     * @param ssoTicketId
     *          a sso ticket that's already assosiated with a cached userdata object
     * @param targetServicePrincipal
     *          the id of the service that will use the TGT
     * @return a ticket-granting ticket that the requesting service may use for later proxy
     *          authentication
     * @throws InvalidTicketException
     *          thrown if the argument ticket is not a SSO-ticket or has an invalid principal
     * @throws NonExistentTicketException
     *          thrown if ticket does not exist
     * @throws MoriaStoreException
     *          thrown if the operation fails
     * @throws IllegalArgumentException
     *          if any of the arguments are null or zero length
     */
    String createTicketGrantingTicket(final String ssoTicketId, final String targetServicePrincipal) throws InvalidTicketException,
            NonExistentTicketException, MoriaStoreException;

    /**
     * Create a new proxy ticket from a TGT and assosiate the new ticket with the same user data as
     * the TGT.
     *
     * @param tgTicketId
     *          a TGT issued earlier to a service
     * @param servicePrincipal
     *          the id of the service doing the request
     * @param targetServicePrincipal
     *          the id of the service that will use the proxy ticket
     * @return proxy ticket that may be used by the requesting service
     * @throws InvalidTicketException
     *          thrown if the incoming ticket is not a TGT or has an invalid principal
     * @throws NonExistentTicketException
     *          thrown if ticket does not exist
     * @throws MoriaStoreException
     *          thrown if the operation fails
     * @throws IllegalArgumentException
     *          if any of the arguments are null or zero length
     */
    String createProxyTicket(final String tgTicketId, final String servicePrincipal, final String targetServicePrincipal)
            throws InvalidTicketException, NonExistentTicketException, MoriaStoreException;

    /**
     * Provide transient attributes to be added to and stored with authentication attempt.
     *
     * @param loginTicketId
     *          the ticket that identifies the AuthnAttempt that the attributes will be
     *          assosiated with
     * @param transientAttributes
     *          the attributes to store with the AuthAttempt
     * @throws InvalidTicketException
     *          thrown if ticket is found invalid
     * @throws NonExistentTicketException
     *          thrown if ticket does not exist
     * @throws MoriaStoreException
     *          thrown if the operation fails
     * @throws IllegalArgumentException
     *          if loginTicketId is null or zero length, or transientAttributes is null
     */
    void setTransientAttributes(final String loginTicketId, final HashMap transientAttributes) throws InvalidTicketException,
            NonExistentTicketException, MoriaStoreException;

    /**
     * Provide transient attributes to be copied form a cached user data object to and stored with authentication attempt.
     *
     * @param loginTicketId
     *          the ticket that identifies the AuthnAttempt that the attributes will be
     *          assosiated with
     * @param ssoTicketId
     *          the ticket associated with a set of cached user data
     * @throws InvalidTicketException
     *          thrown if ticket is found invalid
     * @throws NonExistentTicketException
     *          thrown if ticket does not exist
     * @throws MoriaStoreException
     *          thrown if the operation fails
     * @throws IllegalArgumentException
     *          if loginTicketId is null or zero length, or transientAttributes is null
     */
    void setTransientAttributes(final String loginTicketId, final String ssoTicketId) throws InvalidTicketException,
            NonExistentTicketException, MoriaStoreException;

    /**
     * Removes a ssoTicket from the store.
     *
     * @param ssoTicketId
     *          the ticketId of the ticket to remove
     * @throws InvalidTicketException
     *          if the ticket does not exist
     * @throws NonExistentTicketException
     *          thrown if ticket does not exist
     * @throws MoriaStoreException
     *          if the operation fails
     * @throws IllegalArgumentException
     *          if ssoTicketId is null or zero length
     */
    void removeSSOTicket(final String ssoTicketId) throws InvalidTicketException, NonExistentTicketException, MoriaStoreException;
}
