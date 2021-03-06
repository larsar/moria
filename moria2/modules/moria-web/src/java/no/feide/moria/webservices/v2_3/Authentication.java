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
 */

package no.feide.moria.webservices.v2_3;

import java.rmi.Remote;
import no.feide.moria.servlet.soap.AuthenticationFailedException;
import no.feide.moria.servlet.soap.AuthenticationUnavailableException;
import no.feide.moria.servlet.soap.AuthorizationFailedException;
import no.feide.moria.servlet.soap.IllegalInputException;
import no.feide.moria.servlet.soap.InternalException;
import no.feide.moria.servlet.soap.UnknownTicketException;

/**
 * Defines v2.3 of the Moria2 SOAP interface.
 */
public interface Authentication
extends Remote {

    /**
     * Initiates authentication. This is the initial call done by a service to
     * start a login attempt.
     * @param attributes
     *            The attributes the service wants returned on login.
     * @param returnURLPrefix
     *            The prefix of the URL the user is to be returned to after
     *            successful authentication.
     * @param returnURLPostfix
     *            The optional postfix of the return URL.
     * @param forceInteractiveAuthentication
     *            If <code>true</code>, user is forced through authentication
     *            even if SSO is possible.
     * @return An URL to which the client is to be redirected to for
     *         authentication.
     * @throws AuthorizationFailedException
     *             If the service is now allowed to perform this operation, or
     *             if the service is not allowed to read one or more of the
     *             requested attributes.
     * @throws IllegalInputException
     *             If the method is called with illegal parameters, such as a
     *             <code>returnURLPrefix</code>/
     *             <code>returnURLPostfix</code> combination that does not
     *             yield a valid URL.
     * @throws InternalException
     *             If an internal problem prevents Moria2 from performing this
     *             operation.
     */
    String initiateAuthentication(String[] attributes, String returnURLPrefix, String returnURLPostfix, boolean forceInteractiveAuthentication)
    throws AuthorizationFailedException, IllegalInputException,
    InternalException;


    /**
     * Performs direct non-interactive authentication. A redirect- and HTML-less
     * login method. Only to be used in special cases where the client for some
     * reason does not support the standard login procedure. Inherently insecure
     * as the service will have knowledge of the plaintext password.
     * @param attributes
     *            The attributes the service wants returned following
     *            authentication.
     * @param username
     *            The user name of the user to be authenticated.
     * @param password
     *            The password of the user to be authenticated.
     * @return Array of attributes as requested.
     * @throws AuthorizationFailedException
     *             If the service is now allowed to perform this operation, or
     *             if the service is not allowed to read one or more of the
     *             requested attributes.
     * @throws AuthenticationFailedException
     *             If the user credentials (given by <code>username</code>/
     *             <code>password</code>) are not valid.
     * @throws AuthenticationUnavailableException
     *             If the third-party authentication server responsible for
     *             authenticating this user is not available.
     * @throws IllegalInputException
     *             If the method is called with illegal parameters.
     * @throws InternalException
     *             If an internal problem prevents Moria2 from performing this
     *             operation.
     */
    Attribute[] directNonInteractiveAuthentication(String[] attributes, String username, String password)
    throws AuthorizationFailedException, AuthenticationFailedException,
    AuthenticationUnavailableException, IllegalInputException,
    InternalException;


    /**
     * Performs proxy authentication. Called by a subsystem to authenticate a
     * user.
     * @param attributes
     *            The attributes the service wants returned following proxy
     *            authentication.
     * @param proxyTicket
     *            The proxy ticket given to the calling system by its initiator.
     * @return Array of attributes as requested.
     * @throws AuthorizationFailedException
     *             If the service is now allowed to perform this operation, or
     *             if the service is not allowed to read one or more of the
     *             requested attributes.
     * @throws IllegalInputException
     *             If the method is called with illegal parameters.
     * @throws InternalException
     *             If an internal problem prevents Moria2 from performing this
     *             operation.
     * @throws UnknownTicketException
     *             If the proxy ticket given by <code>proxyTicket</code> does
     *             not match an existing and valid session.
     */
    Attribute[] proxyAuthentication(String[] attributes, String proxyTicket)
    throws AuthorizationFailedException, IllegalInputException,
    InternalException, UnknownTicketException;


    /**
     * Gets a proxy ticket. A service may as part of the initial attribute
     * request ask for a ticket granting ticket that later may be used in this
     * call. The returned proxy ticket is to be handed over to the specified
     * underlying system and may be used by that system only to authenticate the
     * request.
     * @param ticketGrantingTicket
     *            A TGT that has been issued previously.
     * @param proxyServicePrincipal
     *            The service which the proxy ticket should be issued for.
     * @return A proxy ticket.
     * @throws AuthorizationFailedException
     *             If the service is now allowed to perform this operation.
     * @throws IllegalInputException
     *             If the method is called with illegal parameters.
     * @throws InternalException
     *             If an internal problem prevents Moria2 from performing this
     *             operation.
     * @throws UnknownTicketException
     *             If the ticket granting ticket given by
     *             <code>ticketGrantingTicket</code> does not match an
     *             existing and valid session.
     */
    String getProxyTicket(String ticketGrantingTicket, String proxyServicePrincipal)
    throws AuthorizationFailedException, IllegalInputException,
    InternalException, UnknownTicketException;


    /**
     * Gets user attributes. Called by the service when the user returns after a
     * successful login.
     * @param serviceTicket
     *            The ticket included in the return request issued by the
     *            client.
     * @return Array of attributes as requested in initiateAuthentication.
     * @throws AuthorizationFailedException
     *             If the service is now allowed to perform this operation.
     * @throws IllegalInputException
     *             If the method is called with an illegal parameter.
     * @throws InternalException
     *             If an internal problem prevents Moria2 from performing this
     *             operation.
     * @throws UnknownTicketException
     *             If the service ticket given by <code>serviceTicket</code>
     *             does not match an existing or valid session.
     */
    Attribute[] getUserAttributes(String serviceTicket)
    throws AuthorizationFailedException, IllegalInputException,
    InternalException, UnknownTicketException;


    /**
     * Verifies the existence of a given user in the underlying directories.
     * @param username
     *            The username to be validated.
     * @return <code>true</code> if the user is found, otherwise
     *         <code>false</code>.
     * @throws AuthorizationFailedException
     *             If the service is now allowed to perform this operation.
     * @throws AuthenticationUnavailableException
     *             If the authentication server where this user belongs is not
     *             available.
     * @throws IllegalInputException
     *             If the method is called with an illegal parameter.
     * @throws InternalException
     *             If an internal problem prevents Moria2 from performing this
     *             operation.
     */
    boolean verifyUserExistence(String username)
    throws AuthorizationFailedException, AuthenticationUnavailableException,
    IllegalInputException, InternalException;
}
