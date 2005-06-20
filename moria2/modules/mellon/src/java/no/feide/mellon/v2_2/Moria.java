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
import no.feide.moria.servlet.soap.AuthorizationFailedException;
import no.feide.moria.servlet.soap.IllegalInputException;
import no.feide.moria.servlet.soap.InternalException;
import no.feide.moria.servlet.soap.SOAPException;
import no.feide.moria.servlet.soap.UnknownTicketException;
import no.feide.moria.webservices.v2_2.Attribute;
import no.feide.moria.webservices.v2_2.AuthenticationSoapBindingStub;

import org.apache.axis.AxisFault;

/**
 * 
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
     * @param serviceTicket
     * @return foobar
     * @throws RemoteException
     * @throws InternalException
     * @throws IllegalInputException
     * @throws UnknownTicketException
     * @throws AuthorizationFailedException
     */
    public Attribute[] getUserAttributes(final String serviceTicket)
    throws RemoteException, InternalException, IllegalInputException,
    UnknownTicketException, AuthorizationFailedException {

        return moria2.getUserAttributes(serviceTicket);

    }


    /**
     * @param attributes
     * @param returnURLPrefix
     * @param returnURLPostfix
     * @param forceInteractiveAuthentication
     * @return foobar
     * @throws RemoteException
     * @throws SOAPException
     */
    public String initiateAuthentication(final String[] attributes, final String returnURLPrefix, final String returnURLPostfix, final boolean forceInteractiveAuthentication)
    throws RemoteException, SOAPException {

        return moria2.initiateAuthentication(attributes, returnURLPrefix, returnURLPostfix, forceInteractiveAuthentication);

    }


    /**
     * @param attributes
     * @param username
     * @param password
     * @return foobar
     * @throws RemoteException
     * @throws SOAPException
     */
    public Attribute[] directNonInteractiveAuthentication(final String[] attributes, final String username, final String password)
    throws RemoteException, SOAPException {

        return moria2.directNonInteractiveAuthentication(attributes, username, password);

    }


    /**
     * @param attributes
     * @param proxyTicket
     * @return foobar
     * @throws RemoteException
     * @throws SOAPException
     */
    public Attribute[] proxyAuthentication(final String[] attributes, final String proxyTicket)
    throws RemoteException, SOAPException {

        return moria2.proxyAuthentication(attributes, proxyTicket);

    }


    /**
     * @param ticketGrantingTicket
     * @param proxyServicePrincipal
     * @return foobar
     * @throws RemoteException
     * @throws SOAPException
     */
    public String getProxyTicket(final String ticketGrantingTicket, final String proxyServicePrincipal)
    throws RemoteException, SOAPException {

        return moria2.getProxyTicket(ticketGrantingTicket, proxyServicePrincipal);

    }


    /**
     * @param username
     * @return foobar
     * @throws RemoteException
     * @throws SOAPException
     */
    public boolean verifyUserExistence(final String username)
    throws RemoteException, SOAPException {

        return moria2.verifyUserExistence(username);

    }

}
