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

package no.feide.mellon;

import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;

import no.feide.mellon.demo.Attribute;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import org.apache.axis.client.Call;
import org.apache.axis.encoding.ser.VectorDeserializerFactory;
import org.apache.axis.encoding.ser.VectorSerializerFactory;

/**
 *
 */
public class Moria {
    
    private String myUsername;
    private String myPassword;
    private String myEndpoint;
    private String myAttributeNamespace;

    /**
     * Name of the URL parameter used to retrieve the Moria service ticket. <br>
     * <br>
     * Current value is <code>"ticket"</code>.
     */
    private static final String PARAM_TICKET = "ticket";
    
    
    public Moria(final String serviceEndpoint,
                 final String serviceUsername,
                 final String servicePassword,
                 final String attributeNamespaceURI) {
        
        myEndpoint = serviceEndpoint;
        myUsername = serviceUsername;
        myPassword = servicePassword;
        myAttributeNamespace = attributeNamespaceURI;
        
    }


    /**
     * Initiates an authentication session, hiding any Axis internals from the
     * developer.
     * @param attributeRequest
     *            The attribute request.
     * @param urlPrefix
     *            The prefix of the URL where the user will be redirected back
     *            to after successful authentication.
     * @param urlPostfix
     *            The postfix of the URL where the user will be redirected back
     *            to after successful authentication. The resulting return URL
     *            will be <code>"urlPrefix<i>moriaID</i>urlPostfix"</code>,
     *            where <i>moriaID </i> is the session ID given to this
     *            authentication attempt. This ID should be used after
     *            authentication to retrieve the attributes requested through
     *            <code>attributeRequest</code>.
     * @param denySSO
     *            <code>true</code> to disallow use of SSO, otherwise
     *            <code>false</code>.
     * @return An URL to the login page for Moria, to which the user should be
     *         redirected.
     * @throws MalformedURLException
     *             If the constant <code>SERVICE_ENDPOINT</code> contains an
     *             illegal URL.
     * @throws RemoteException
     *             If an exception was thrown by the remote service.
     * @see #PROP_DEMO_SERVICE_ENDPOINT
     */
    public String initiateAuthentication(final String[] attributeRequest, final String urlPrefix, final String urlPostfix, final boolean denySSO)
    throws MalformedURLException, RemoteException {

        // Preparing call.
        Call call = new Call(new URL(myEndpoint + "?WSDL"));
        call.setUsername(myUsername);
        call.setPassword(myPassword);
        final Object[] parameters = {
        	attributeRequest, urlPrefix, urlPostfix, new Boolean(false)
        };

        // Performing call.
        return (String) call.invoke(new QName("initiateAuthentication"), parameters);

    }


    /**
     * Gets user attributes following a successful user authentication.
     *
     * The attributes have previously been requested through
     * <code>initiateAuthentication(...)</code>.
     *
     * @param serviceTicket
     *            A legal service ticket returned to the client service
     *            following a successful authentication on the user's part.
     * @return An array of <code>Attribute</code> objects, containing the
     *         resulting values of the original attribute request. Note that if
     *         the requested attributes do not exist, or if the client service
     *         is not authorized to read the attributes, they will not be
     *         returned. May consequently return an empty array, but never
     *         <code>null</code>.
     * @throws MalformedURLException
     *             If the URL given by <code>SERVICE_ENDPOINT + "?WSDL</code>
     *             is illegal.
     * @throws RemoteException
     *             If the remote call failed for some reason.
     * @see #initiateAuthentication(String[], String, String, boolean)
     */
    public Attribute[] getUserAttributes(final String serviceTicket)
    throws MalformedURLException, RemoteException {

        // Prepare call.
        Call call = new Call(new URL(myEndpoint + "?WSDL"));
        call.setUsername(myUsername);
        call.setPassword(myPassword);
        final QName attributeQName = new QName(myAttributeNamespace, "Attribute");
        call.setReturnType(attributeQName);
        VectorSerializerFactory serializer = new VectorSerializerFactory(Attribute.class, attributeQName);
        VectorDeserializerFactory deserializer = new VectorDeserializerFactory(Attribute.class, attributeQName);
        call.registerTypeMapping(Attribute.class, attributeQName, serializer, deserializer);
        call.addParameter("serviceTicket", new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, ParameterMode.IN);
        final Object[] parameters = {serviceTicket};

        // Perform the call.
        final Object returnedAttributes = call.invoke(new QName(myEndpoint, "getUserAttributes"), parameters);

        // Convert and return.
        return (Attribute[]) returnedAttributes;

    }


    /**
     * Gets a proxy ticket, by supplying a previously retrieved ticket granting
     * ticket for the given subsystem. This ticket may then be used by the
     * subsystem as proof that authentication has taken place. <br>
     * <br>
     * @param ticketGrantingTicket
     *            A valid ticket granting ticket previously retrieved by
     *            requesting the special attribute <i>tgt </i> using
     *            <code>initiateAuthentication(...)</code> and
     *            <code>getUserAttributes(...)</code>.
     * @param proxyServicePrincipal
     *            The principal of the subsystem to create a proxy ticket for.
     * @return A new proxy ticket, for use by the subsystem identified as
     *         <code>proxyServicePrincipal</code>.
     * @throws MalformedURLException
     *             If the constant <code>SERVICE_ENDPOINT</code> contains an
     *             illegal URL.
     * @throws RemoteException
     *             If an exception was thrown by the remote service.
     * @see #initiateAuthentication(String[], String, String, boolean)
     * @see #getUserAttributes(String)
     */
    public String getProxyTicket(final String ticketGrantingTicket, final String proxyServicePrincipal)
    throws MalformedURLException, RemoteException {

        // Preparing call.
        Call call = new Call(new URL(myEndpoint + "?WSDL"));
        call.setUsername(myUsername);
        call.setPassword(myPassword);
        final Object[] parameters = {ticketGrantingTicket, proxyServicePrincipal};

        // Performing call.
        return (String) call.invoke(new QName("getProxyTicket"), parameters);

    }


    /**
     * Performs proxy authentication.
     *
     * Verifies, by using a proxy ticket retrieved by
     * <code>getProxyTicket</code>, that the user has been authenticated
     * and retrieves a number of attributes.
     *
     * @param attributes
     *            The attributes requested as part of this proxy authentication.
     * @param proxyTicket
     *            The proxy ticket.
     * @return An array of <code>Attribute</code> objects, containing the
     *         resulting values of the original attribute request. Note that if
     *         the requested attributes do not exist, or if the client service
     *         is not authorized to read the attributes, they will not be
     *         returned. May consequently return an empty array, but never
     *         <code>null</code>.
     * @throws MalformedURLException
     *             If the URL given by <code>SERVICE_ENDPOINT + "?WSDL</code>
     *             is illegal.
     * @throws RemoteException
     *             If the remote call failed for some reason.
     * @see #getProxyTicket(String, String)
     */
    public Attribute[] proxyAuthentication(final String[] attributes, final String proxyTicket)
    throws MalformedURLException, RemoteException {

        // Prepare call.
        Call call = new Call(new URL(myEndpoint + "?WSDL"));
        call.setUsername(myUsername);
        call.setPassword(myPassword);
        final QName attributeQName = new QName(myAttributeNamespace, "Attribute");
        call.setReturnType(attributeQName);
        VectorSerializerFactory serializer = new VectorSerializerFactory(Attribute.class, attributeQName);
        VectorDeserializerFactory deserializer = new VectorDeserializerFactory(Attribute.class, attributeQName);
        call.registerTypeMapping(Attribute.class, attributeQName, serializer, deserializer);
        call.addParameter("attributes", new QName("http://www.w3.org/2001/XMLSchema", "string[]"), String[].class, ParameterMode.IN);
        call.addParameter("proxyTicket", new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, ParameterMode.IN);
        final Object[] parameters = {attributes, proxyTicket};

        // Perform the call.
        final Object returnedAttributes = call.invoke(new QName(myEndpoint,
                                                                "proxyAuthentication"), parameters);

        // Convert and return.
        return (Attribute[]) returnedAttributes;

    }

}
