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
 * The store manager's main functionality is to handle tickets. The
 * interface makes it possible to create tickets and store them and
 * their associated data. The interface also has support for removal
 * of expired tickets.
 *
 * @author Bj&oslash;rn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public interface MoriaStore {

    /**
     * Sets the configuration of the store.
     *
     * @param properties
     *          Object containing the necessary attributes for store configuration.
     * @throws MoriaStoreConfigurationException
     *          If the store cannot be started with the given configuration.
     * @throws IllegalArgumentException
     *          If properties is null.
     */
    void setConfig(Properties properties)
            throws MoriaStoreConfigurationException;

    /**
     * Stops this instance of the store.
     */
    void stop();

    /**
     * Creates an authentication attempt based on a service request.
     *
     * @param requestAttributes
     *          The user attributes the requesting service asks for.
     * @param responseURLPrefix
     *          The forward part of the url the client is to be redirected to.
     * @param responseURLPostfix
     *          The end part of the url the client is to be redirected to.
     * @param forceInteractiveAuthentication
     *          If the user should be forced to login interactively. I.e. disable
     *          support for single sign-on.
     * @param servicePrincipal
     *          The id of the service doing the request.
     * @return A login ticket identifying the authentication attempt.
     * @throws MoriaStoreException
     *          If the operation fails.
     * @throws IllegalArgumentException
     *          If any of the arguments are null, and if responseURLPrefix or servicePrincipal are zero length.
     */
    String createAuthnAttempt(final String[] requestAttributes, final String responseURLPrefix, final String responseURLPostfix,
                              final boolean forceInteractiveAuthentication, final String servicePrincipal)
            throws MoriaStoreException;

    /**
     * Gets the authentication attempt associated with the ticket given as argument.
     *
     * @param ticketId
     *          the ticket from the incoming client request (must be LOGIN or SERVICE)
     * @param keep
     *          if true the authnAttempt and ticket will be kept in the store after this operation
     * @param servicePrincipal
     *          the principal of the service requesting the operation (null if login ticket is supplied)
     * @return the MoriaAuthnAttempt associated with the ticket
     * @throws InvalidTicketException
     *          if the incoming ticket is found to be invalid
     * @throws NonExistentTicketException
     *          If ticket does not exist
     * @throws MoriaStoreException
     *          If the operation fails
     * @throws IllegalArgumentException
     *          If ticketId is null or zero length and if no servicePrincipal is
     *          supplied with a service ticket
     */
    MoriaAuthnAttempt getAuthnAttempt(final String ticketId, final boolean keep, final String servicePrincipal)
            throws InvalidTicketException, NonExistentTicketException, MoriaStoreException;

    /**
     * Creates a new CachedUserData object in the underlying store and associates it with an SSO
     * ticket which is returned.
     *
     * @param attributes
     *          The attribute map to be cached.
     * @param userorg
     *          The userorg that is to be associated with the ticket.
     * @return The SSO ticket that identifies the cached user data.
     * @throws MoriaStoreException
     *          If the operation fails.
     * @throws IllegalArgumentException
     *             If attributes is null, or
     *             userorg is null or an empty  string.
     */
    String cacheUserData(final HashMap attributes, final String userorg)
            throws MoriaStoreException;

    /**
     * Returns the userdata associated with the incoming ticket, which must be either a
     * proxy ticket, an SSO ticket or ticket granting ticket.
     *
     * @param proxyTicketId
     *          A ticket to identify a userdata object (SSO, TGT or PROXY).
     * @param servicePrincipal
     *          The name of the service requesting the data,
     * @return A clone of the object containing the userdata.
     * @throws InvalidTicketException
     *          If the incoming ticket is not of the correct type or
     *          has an invalid principal.
     * @throws NonExistentTicketException
     *          If ticket does not exist.
     * @throws MoriaStoreException
     *          If the operation fails.
     * @throws IllegalArgumentException
     *          If ticketId is null or zero length, or SSO ticket principal 
     *          is null or zero length.
     */
    CachedUserData getUserData(final String proxyTicketId, final String servicePrincipal)
            throws InvalidTicketException, NonExistentTicketException, MoriaStoreException;

    /**
     * Creates a service ticket that the service will use when requesting user attributes after a
     * successful authentication.
     *
     * @param loginTicketId
     *          A login ticket associated with an authentication attempt.
     * @return A service ticket associated with the authentication attempt object.
     * @throws InvalidTicketException
     *          If the supplied ticket is not a login ticket.
     * @throws NonExistentTicketException
     *          If ticket does not exist.
     * @throws MoriaStoreException
     *          If the operation fails.
     * @throws IllegalArgumentException
     *          If loginTicketId is null or zero length.
     */
    String createServiceTicket(final String loginTicketId)
            throws InvalidTicketException, NonExistentTicketException, MoriaStoreException;

    /**
     * Creates a new ticket granting ticket, using an sso ticket.
     *
     * @param ssoTicketId
     *          An sso ticket that is already associated with a cached userdata object.
     * @param targetServicePrincipal
     *          The id of the service that will use the TGT.
     * @return A ticket-granting ticket that the requesting service may use for later proxy
     *          authentication.
     * @throws InvalidTicketException
     *          If the argument ticket is not an SSO ticket or has an invalid principal.
     * @throws NonExistentTicketException
     *          If ticket does not exist.
     * @throws MoriaStoreException
     *          If the operation fails.
     * @throws IllegalArgumentException
     *          If any of the arguments are null or zero length.
     */
    String createTicketGrantingTicket(final String ssoTicketId, final String targetServicePrincipal)
            throws InvalidTicketException, NonExistentTicketException, MoriaStoreException;

    /**
     * Creates a new proxy ticket from a TGT and associates the new ticket with the same user data as
     * the TGT.
     *
     * @param tgTicketId
     *          A TGT issued earlier to a service.
     * @param servicePrincipal
     *          The id of the service making the request.
     * @param targetServicePrincipal
     *          The id of the service that will use the proxy ticket.
     * @return Proxy ticket that may be used by the requesting service.
     * @throws InvalidTicketException
     *          If the incoming ticket is not a TGT or has an invalid principal.
     * @throws NonExistentTicketException
     *          If ticket does not exist.
     * @throws MoriaStoreException
     *          If the operation fails.
     * @throws IllegalArgumentException
     *          If any of the arguments are null or zero length.
     */
    String createProxyTicket(final String tgTicketId, final String servicePrincipal, final String targetServicePrincipal)
            throws InvalidTicketException, NonExistentTicketException, MoriaStoreException;

    /**
     * Sets transient attributes stored with authentication attempt.
     *
     * @param loginTicketId
     *          Ticket that identifies the AuthnAttempt that the attributes will be
     *          associated with.
     * @param transientAttributes
     *          Attributes to store with the AuthnAttempt.
     * @throws InvalidTicketException
     *          If ticket is found invalid.
     * @throws NonExistentTicketException
     *          If ticket does not exist.
     * @throws MoriaStoreException
     *          If the operation fails.
     * @throws IllegalArgumentException
     *          If loginTicketId is null or zero length, or transientAttributes is null.
     */
    void setTransientAttributes(final String loginTicketId, final HashMap transientAttributes)
            throws InvalidTicketException, NonExistentTicketException, MoriaStoreException;

    /**
     * Sets transient attributes stored with authentication attempt,
     * copied from a cached user data object.
     *
     * @param loginTicketId
     *          Ticket that identifies the AuthnAttempt that the attributes will be
     *          associated with.
     * @param ssoTicketId
     *          Ticket associated with a set of cached user data.
     * @throws InvalidTicketException
     *          If either ticket is found invalid.
     * @throws NonExistentTicketException
     *          If either ticket does not exist.
     * @throws MoriaStoreException
     *          If the operation fails.
     * @throws IllegalArgumentException
     *          If either ticket id is null or zero length.
     */
    void setTransientAttributes(final String loginTicketId, final String ssoTicketId)
            throws InvalidTicketException, NonExistentTicketException, MoriaStoreException;

    /**
     * Removes an SSO ticket from the store.
     *
     * @param ssoTicketId
     *          the ticketId of the ticket to remove
     * @throws NonExistentTicketException
     *          If ticket does not exist
     * @throws MoriaStoreException
     *          If the operation fails
     * @throws IllegalArgumentException
     *          If ssoTicketId is null or zero length
     */
    void removeSSOTicket(final String ssoTicketId)
            throws NonExistentTicketException, MoriaStoreException;
    
    /**
     * Returns the service principal for the ticket
     * 
     * @param ticketId The ticket id.
     * @param ticketType The ticket type.
     * @return Service principal.
     * @throws InvalidTicketException
     *          If the ticket is invalid.
     * @throws NonExistentTicketException
     *          If ticket does not exist.
     * @throws MoriaStoreException
     *          If the operation fails.
     * @throws IllegalArgumentException
     *          If ticketId is null or zero length.
     */
    String getTicketServicePrincipal(final String ticketId, MoriaTicketType ticketType)
            throws InvalidTicketException, NonExistentTicketException, MoriaStoreException;

    /**
     * Sets the userorg of a ticket.
     *
     * @param ticketId The ticket id.
     * @param ticketType The ticket type.
     * @param userorg The userorg of the user creating the ticket.
     * @throws InvalidTicketException
     *          if the ticket is invalid.
     * @throws NonExistentTicketException
     *          If ticket does not exist.
     * @throws MoriaStoreException
     *          If the operation fails.
     * @throws IllegalArgumentException
     *          If ticketId is null or zero length.
     */
    void setTicketUserorg(final String ticketId, MoriaTicketType ticketType, String userorg)
           throws InvalidTicketException, NonExistentTicketException, MoriaStoreException;

    /**
     * Gets the userorg of a ticket.
     *
     * @param ticketId the ticket id.
     * @param ticketType the ticket type.
     * @return the organization of the user creating the ticket, or null if not set.
     * @throws InvalidTicketException
     *          If the ticket is invalid.
     * @throws NonExistentTicketException
     *          If ticket does not exist.
     * @throws MoriaStoreException
     *          If the operation fails.
     * @throws IllegalArgumentException
     *          If ticketId is null or zero length.
     */
    String getTicketUserorg(final String ticketId, MoriaTicketType ticketType)
           throws InvalidTicketException, NonExistentTicketException, MoriaStoreException;

}
