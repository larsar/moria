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

/**
 * @author Bjørn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public interface MoriaStore {

    /* TODO: Identify acctual exceptions thrown */
    /**
     * Creates an authentication attempt based on a service request.
     * 
     * @param requestAttributes
     *            the user attributes the requesting service asks for
     * @param responseURLPrefix
     *            the forward part of the url the client is to be redirected to
     * @param responseURLPostfix
     *            the end part of the url the client is to be redirected to
     * @param servicePrincipal
     *            the name of the service doing the request
     * @param forceInteractiveAuthentication
     *            if the user should be forced to login interactivly. I.e.
     *            disable support for single sign-on
     * @return a login ticket identifying the authentication attempt
     * 
     * @throws IllegalArgumentException
     *             thrown if any of the arguments are null
     */
    public MoriaTicket createAuthnAttempt(String[] requestAttributes, String responseURLPrefix, String responseURLPostfix,
            String servicePrincipal, boolean forceInteractiveAuthentication) throws IllegalArgumentException;

    /**
     * Gets the authentication attempt assosiated with the ticket given as
     * argument. Should return null if no Authentication attempt is found.
     * 
     * @param loginTicket
     *            the ticket from the incoming client request
     * @param keep
     *            if true the authnAttempt and ticket will be kept in the store
     *            after this operation
     * @return the MoriaAuthnAttempt assosiated with the ticket
     * @throws InvalidTicketException
     *             if the incoming ticket is not a login ticket
     */
    public MoriaAuthnAttempt getAuthnAttempt(MoriaTicket loginTicket, boolean keep) throws InvalidTicketException;

    /**
     * Creates a new CachedUserData object in the underlying store and
     * assosiates it with a SSO ticket which is returned.
     * 
     * @param attributes
     *            the attribute map to be cached
     * @return the SSO ticket that identifies the cached user data
     */
    public MoriaTicket cacheUserData(HashMap attributes);

    /**
     * Return the userdata assosiated with the incoming ticket, which must be
     * either a proxy or sso ticket
     * 
     * @param ticket
     *            a ticket to identify a userdata object
     * @return a clone of the object containing the userdata
     * @throws InvalidTicketException
     *             thrown if the incoming ticket is not of the correct type or
     *             has an invalid principal
     */
    public CachedUserData getUserData(MoriaTicket ticket) throws InvalidTicketException;

    /**
     * Creates a service ticket that the service will use when requesting user
     * attributes after a successfull authentication.
     * 
     * @param loginTicket
     *            the login ticket assosiated with an authentication attempt
     * @return a service ticket assosiated with the authentication attempt
     *         object
     */
    public MoriaTicket createServiceTicket(MoriaTicket loginTicket) throws InvalidTicketException;

    /**
     * Create a new ticket granting ticket, using a sso ticket.
     * 
     * @param ssoTicket
     *            a sso ticket that's already assosiated with a cached userdata
     *            object
     * @param servicePrincipal
     *            the id of the service that will use the TGT
     * @return a ticket-granting ticket that the requesting service may use for
     *         later proxy authentication
     * @throws InvalidTicketException
     *             thrown if the argument ticket is not a SSO-ticket or has an
     *             invalid principal
     */
    public MoriaTicket createTicketGrantingTicket(MoriaTicket ssoTicket, String servicePrincipal) throws InvalidTicketException;

    /**
     * Create a new proxy ticket from a TGT and assosiate the new ticket with
     * the same user data as the TGT.
     * 
     * @param tgTicket
     *            a TGT issued earlier to a service
     * @param servicePrincipal
     *            the id of the service that will use the proxy ticket
     * @return proxy ticket that may be used by the requesting service
     * @throws InvalidTicketException
     *             thrown if the incoming ticket is not a TGT or has an invalid
     *             principal
     */
    public MoriaTicket createProxyTicket(MoriaTicket tgTicket, String servicePrincipal) throws InvalidTicketException;

    /**
     * Provide transient attributes to be added to and stored with
     * authentication attempt.
     * 
     * @param ticket
     *            the ticket that identifies the AuthnAttempt that the
     *            attributes will be assosiated with
     * @param transientAttributes
     *            the attributes to store with the AuthAttempt
     * @throws InvalidTicketException
     *             thrown if ticket is found invalid
     */
    public void setTransientAttributes(MoriaTicket ticket, String[] transientAttributes) throws InvalidTicketException;
}