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

package no.feide.mellon.v2_2;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import no.feide.mellon.MoriaException;
import no.feide.moria.servlet.soap.AuthenticationFailedException;
import no.feide.moria.servlet.soap.AuthenticationUnavailableException;
import no.feide.moria.servlet.soap.AuthorizationFailedException;
import no.feide.moria.servlet.soap.IllegalInputException;
import no.feide.moria.servlet.soap.InternalException;
import no.feide.moria.servlet.soap.UnknownTicketException;
import no.feide.moria.webservices.v2_2.Attribute;
import no.feide.moria.webservices.v2_2.AuthenticationSoapBindingStub;

import org.apache.axis.AxisFault;

/**
 * A client-side Moria2 v2.2 API, hiding the internals of generated stub usage.
 */
public class Moria {

    /**
     * Internal representation of the Moria2 service.
     */
    private AuthenticationSoapBindingStub moria2;


    /**
     * @param endpoint
     *            The Moria2 service endpoint URL. Must be a non-empty string.
     * @param username
     *            The client service's username, which is used by Moria2 to
     *            check the service's right to read attributes and perform
     *            operations. Must be a non-empty string.
     * @param password
     *            The client service's password, which is used by Moria2 to
     *            check the service's right to read attributes and perform
     *            operations. Must be a non-empty string.
     * @throws IllegalArgumentException
     *             If <code>endpoint</code>,<code>username</code> or
     *             <code>password</code> is <code>null</code> or an empty
     *             string.
     * @throws MalformedURLException
     *             If the URL given by <code>endpoint</code> is malformed.
     * @throws MoriaException
     *             If unable to instantiate a client-side stub to the Moria2
     *             endpoint.
     */
    public Moria(final String endpoint, final String username, final String password)
    throws IllegalArgumentException, MalformedURLException, MoriaException {

        super();

        // Sanity checks
        if ((endpoint == null) || (endpoint.length() == 0))
            throw new IllegalArgumentException("Endpoint parameter must be a non-empty string");
        if ((username == null) || (username.length() == 0))
            throw new IllegalArgumentException("Username parameter must be a non-empty string");
        if ((password == null) || (password.length() == 0))
            throw new IllegalArgumentException("Password parameter must be a non-empty string");

        // Create connection to Moria2 and set service credentials.
        try {
            moria2 = new AuthenticationSoapBindingStub(new URL(endpoint), null);
        } catch (AxisFault e) {
            throw new MoriaException("Unable to instantiate client-side stub to endpoint " + endpoint, e);
        }
        moria2.setUsername(username);
        moria2.setPassword(password);

    }


    /**
     * Gets user attributes. Called by the service when the user returns after a
     * successful login.
     * @param serviceTicket
     *            The ticket included in the return request issued by the
     *            client.
     * @return Array of attributes as requested in initiateAuthentication.
     * @throws RemoteException
     *             If an exception occurs in the underlying SOAP layer.
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
    public Attribute[] getUserAttributes(final String serviceTicket)
    throws RemoteException, InternalException, IllegalInputException,
    UnknownTicketException, AuthorizationFailedException {

        return moria2.getUserAttributes(serviceTicket);

    }


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
     * @throws RemoteException
     *             If an exception occurs in the underlying SOAP layer.
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
    public String initiateAuthentication(final String[] attributes, final String returnURLPrefix, final String returnURLPostfix, final boolean forceInteractiveAuthentication)
    throws RemoteException, InternalException, IllegalInputException,
    AuthorizationFailedException {

        return moria2.initiateAuthentication(attributes, returnURLPrefix, returnURLPostfix, forceInteractiveAuthentication);

    }


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
     * @throws RemoteException
     *             If an exception occurs in the underlying SOAP layer.
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
    public Attribute[] directNonInteractiveAuthentication(final String[] attributes, final String username, final String password)
    throws RemoteException, InternalException, IllegalInputException,
    AuthenticationFailedException, AuthorizationFailedException,
    AuthenticationUnavailableException {

        return moria2.directNonInteractiveAuthentication(attributes, username, password);

    }


    /**
     * Performs proxy authentication. Called by a subsystem to authenticate a
     * user.
     * @param attributes
     *            The attributes the service wants returned following proxy
     *            authentication.
     * @param proxyTicket
     *            The proxy ticket given to the calling system by its initiator.
     * @return Array of attributes as requested.
     * @throws RemoteException
     *             If an exception occurs in the underlying SOAP layer.
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
    public Attribute[] proxyAuthentication(final String[] attributes, final String proxyTicket)
    throws RemoteException, InternalException, IllegalInputException,
    UnknownTicketException, AuthorizationFailedException {

        return moria2.proxyAuthentication(attributes, proxyTicket);

    }


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
     * @throws RemoteException
     *             If an exception occurs in the underlying SOAP layer.
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
    public String getProxyTicket(final String ticketGrantingTicket, final String proxyServicePrincipal)
    throws RemoteException, InternalException, IllegalInputException,
    UnknownTicketException, AuthorizationFailedException {

        return moria2.getProxyTicket(ticketGrantingTicket, proxyServicePrincipal);

    }


    /**
     * Verifies the existence of a given user in the underlying directories.
     * @param username
     *            The username to be validated.
     * @return <code>true</code> if the user is found, otherwise
     *         <code>false</code>.
     * @throws RemoteException
     *             If an exception occurs in the underlying SOAP layer.
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
    public boolean verifyUserExistence(final String username)
    throws RemoteException, InternalException, IllegalInputException,
    AuthorizationFailedException, AuthenticationUnavailableException {

        return moria2.verifyUserExistence(username);

    }

}
