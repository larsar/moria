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

package no.feide.moria.webservices.v2_1;

import java.rmi.Remote;

import no.feide.moria.servlet.soap.SOAPException;


/**
 * @author Bj&oslash;rn Ola Smievoll &lt;b.o.smievoll@conduct.no&gt;
 * @version $Revision$
 */
public interface Authentication extends Remote {

    /**
     * Initiates authentication.
     *
     * The initial call done by a service to start a login attempt.
     *
     * @param attributes
     *          The attributes the service wants returned on login
     * @param returnURLPrefix
     *          The prefix of the url the user is to be returned to
     * @param returnURLPostfix
     *          The optional postfix of the return url
     * @param forceInteractiveAuthentication
     *          Whether or not cookie based authentication (SSO Light)
     *          should be allowed.
     * @return The Moria url the client is to be redirected to.
     * @throws SOAPException
     *          If anything fails during the call.
     */
    String initiateAuthentication(String[] attributes, String returnURLPrefix, String returnURLPostfix,
                                  boolean forceInteractiveAuthentication)
    throws SOAPException;

    /**
     * Performs direct non-interactive authentication.
     *
     * A redirect- and html-less login method.  Only to be used in
     * special cases where the client for some reason does not
     * support the standard login procedure.  Inherently insecure as
     * the service will have knowledge of the plaintext password.
     *
     * @param attributes
     *          The attributes the service wants returned on login.
     * @param username
     *          The user name of the user to be authenticated.
     * @param password
     *          The password of the user to be authenticated.
     * @return Array of attributes as requested.
     * @throws SOAPException
     *          If anything fails during the call.
     */
    Attribute[] directNonInteractiveAuthentication(String[] attributes, String username, String password)
    throws SOAPException;

    /**
     * Performs proxy authentication.
     *
     * Called by a subsystem to authenticate a user.
     *
     * @param attributes
     *          The attributes the service wants returned on login.
     * @param proxyTicket
     *          The proxy ticket given to the calling system by its initiator.
     * @return Array of attributes as requested.
     * @throws SOAPException
     *          If anything fails during the call.
     */
    Attribute[] proxyAuthentication(String[] attributes, String proxyTicket)
    throws SOAPException;

    /**
     * Gets a proxy ticket.
     *
     * A service may as part of the initial attribute request ask for
     * a ticket granting ticket that later may be used in this call.
     *
     * The returned proxy ticket is to be handed over to the specified
     * underlying system and may be used by that system only
     * to authenticate the request.
     *
     * @param ticketGrantingTicket
     *          A TGT that has been issued previously.
     * @param proxyServicePrincipal
     *          The service which the proxy ticket should be issued for.
     * @return A proxy ticket.
     * @throws SOAPException
     *          If anything fails during the call.
     */
    String getProxyTicket(String ticketGrantingTicket, String proxyServicePrincipal)
    throws SOAPException;

    /**
     * Gets user attributes.
     *
     * Called by the service when the user returns after a successful
     * login.
     *
     * @param serviceTicket
     *          The ticket included in the return request issued by the client.
     * @return Array of attributes as requested in initiateAuthentication.
     * @throws SOAPException
     *          If anything fails during the call.
     */
    Attribute[] getUserAttributes(String serviceTicket)
    throws SOAPException;

    /**
     * Verifies the existence of a given user in the underlying directories.
     *
     * @param username
     *          The username to be validated.
     * @return true if the user is found.
     * @throws SOAPException
     *          If anything fails during the call.
     */
    boolean verifyUserExistence(String username)
    throws SOAPException;
}
