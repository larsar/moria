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
     */
    String createAuthnAttempt(final String[] requestAttributes, final String responseURLPrefix,
            final String responseURLPostfix, final boolean forceInteractiveAuthentication, final String servicePrincipal)
            throws MoriaStoreException;

    /**
     * Gets the authentication attempt assosiated with the ticket given as argument. Should return
     * null if no Authentication attempt is found.
     *
     * @param loginTicketId
     *          the ticket from the incoming client request
     * @param keep
     *          if true the authnAttempt and ticket will be kept in the store after this operation
     * @return the MoriaAuthnAttempt assosiated with the ticket
     * @throws InvalidTicketException
     *          if the incoming ticket is not a login ticket
     * @throws MoriaStoreException
     *          thrown if the operation fails
     */
    MoriaAuthnAttempt getAuthnAttempt(final String loginTicketId, final boolean keep) throws InvalidTicketException,
            MoriaStoreException;

    /**
     * Creates a new CachedUserData object in the underlying store and assosiates it with a SSO
     * ticket which is returned.
     *
     * @param attributes
     *          the attribute map to be cached
     * @return the SSO ticket that identifies the cached user data
     * @throws MoriaStoreException
     *          thrown if the operation fails
     */
    String cacheUserData(final HashMap attributes) throws MoriaStoreException;

    /**
     * Return the userdata assosiated with the incoming ticket, which must be either a
     * sso, ticket granting or proxy ticket.
     *
     * @param ticketId
     *          a ticket to identify a userdata object (SSO, TG or PROXY)
     * @return a clone of the object containing the userdata
     * @throws InvalidTicketException
     *          thrown if the incoming ticket is not of the correct type or
     *          has an invalid principal
     * @throws MoriaStoreException
     *          thrown if the operation fails
     */
    CachedUserData getUserData(final String ticketId) throws InvalidTicketException, MoriaStoreException;

    /**
     * Creates a service ticket that the service will use when requesting user attributes after a
     * successfull authentication.
     *
     * @param loginTicketId
     *          a login ticket assosiated with an authentication attempt
     * @return a service ticket assosiated with the authentication attempt object
     * @throws InvalidTicketException
     *          thrown if the argument ticket is not a login-ticket
     * @throws MoriaStoreException
     *          thrown if the operation fails
     */
    String createServiceTicket(final String loginTicketId)
            throws InvalidTicketException, MoriaStoreException;

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
     * @throws MoriaStoreException
     *          thrown if the operation fails
     */
    String createTicketGrantingTicket(final String ssoTicketId, final String targetServicePrincipal)
            throws InvalidTicketException, MoriaStoreException;

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
     * @throws MoriaStoreException
     *          thrown if the operation fails
     */
    String createProxyTicket(final String tgTicketId, final String servicePrincipal, final String targetServicePrincipal)
            throws InvalidTicketException, MoriaStoreException;

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
     * @throws MoriaStoreException
     *          thrown if the operation fails
     */
    void setTransientAttributes(final String loginTicketId, final HashMap transientAttributes)
            throws InvalidTicketException, MoriaStoreException;

    /**
     * Set the configuration of the store.
     *
     * @param properties
     *          object containing the necessary attributes for store configuration
     */
    void setConfig(Properties properties);

    /**
     * Removes a ssoTicket from the store.
     *
     * @param ssoTicketId the ticketId of the ticket to remove
     * @throws InvalidTicketException if the ticket does not exist
     * @throws MoriaStoreException if the operation fails
     */
    void removeSSOTicket(final String ssoTicketId) throws InvalidTicketException, MoriaStoreException;
}
