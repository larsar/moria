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


import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import org.apache.axis.client.Call;
import org.apache.axis.encoding.ser.VectorDeserializerFactory;
import org.apache.axis.encoding.ser.VectorSerializerFactory;

/**
 * This is an example implementation of a Mellon2 API, aptly named
 * <code>Moria</code>, since it is a client-side representation of the Moria
 * service.
 * <br><br>
 * In this implementation, all method stubs are hard-coded; that is, not
 * generated from the Moria service WSDL. The original target interface was the
 * v2.1 SOAP interface of Moria, which is something to consider when choosing
 * service endpoint and attribute namespace URI.
 * <br><br>
 * The stub methods mirror the server-side SOAP methods published by the Moria
 * instance, but are not generated by Axis' SOAP tools (WSDL2Java). For details
 * on how to implement client-side methods using other SOAP implementations than
 * Axis (such as Sun's JAX-RPC) or other programming languages, please refer to
 * the documentation for the SOAP implementation/library in question.
 * <br><br>
 * Note that this code is distributed 'as-is', for example purposes only. Use
 * at your own discretion.
 */
public class Moria {
    
    /**
     * Used to hold the parent service's username. 
     */
    private String myUsername;
    
    /**
     * Used to hold the parent service's password.
     */
    private String myPassword;
    
    /**
     * Used to hold the endpoint URL.
     */
    private String myEndpoint;
    
    /**
     * Used to hold the attribute namespace URI. This is important for mapping
     * remote attributes to the local class <code>MoriaAttribute</code>.
     */
    private String myAttributeNamespace;

    /**
     * Name of the URL parameter used to retrieve the Moria service ticket. <br>
     * <br>
     * Current value is <code>"ticket"</code>.
     */
    private static final String PARAM_TICKET = "ticket";
    
    
    /**
     * Constructor. Creates a new Moria instance against the given service
     * endpoint. No communication is established at this point. 
     * @param serviceEndpoint Moria's service endpoint.
     * @param serviceUsername The parent service's username, used when
     *                        authenticating the service itself against Moria.
     * @param servicePassword The parent service's password, used when
     *                        authenticating the service itself against Moria.
     * @param attributeNamespaceURI The attribute namespace URI, used for
     *                              mapping the remote attribute class
     *                              definition to the local
     *                              <code>MoriaAttribute</code> definition.
     */
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
     * @return An array of <code>MoriaAttribute</code> objects, containing the
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
    public MoriaAttribute[] getUserAttributes(final String serviceTicket)
    throws MalformedURLException, RemoteException {

        // Prepare call.
        Call call = new Call(new URL(myEndpoint + "?WSDL"));
        call.setUsername(myUsername);
        call.setPassword(myPassword);
        final QName attributeQName = new QName(myAttributeNamespace, "Attribute");
        call.setReturnType(attributeQName);
        VectorSerializerFactory serializer = new VectorSerializerFactory(MoriaAttribute.class, attributeQName);
        VectorDeserializerFactory deserializer = new VectorDeserializerFactory(MoriaAttribute.class, attributeQName);
        call.registerTypeMapping(MoriaAttribute.class, attributeQName, serializer, deserializer);
        call.addParameter("serviceTicket", new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, ParameterMode.IN);
        final Object[] parameters = {serviceTicket};

        // Perform the call.
        final Object returnedAttributes = call.invoke(new QName(myEndpoint, "getUserAttributes"), parameters);

        // Convert and return.
        return (MoriaAttribute[]) returnedAttributes;

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
    public MoriaAttribute[] proxyAuthentication(final String[] attributes, final String proxyTicket)
    throws MalformedURLException, RemoteException {

        // Prepare call.
        Call call = new Call(new URL(myEndpoint + "?WSDL"));
        call.setUsername(myUsername);
        call.setPassword(myPassword);
        final QName attributeQName = new QName(myAttributeNamespace, "Attribute");
        call.setReturnType(attributeQName);
        VectorSerializerFactory serializer = new VectorSerializerFactory(MoriaAttribute.class, attributeQName);
        VectorDeserializerFactory deserializer = new VectorDeserializerFactory(MoriaAttribute.class, attributeQName);
        call.registerTypeMapping(MoriaAttribute.class, attributeQName, serializer, deserializer);
        call.addParameter("attributes", new QName("http://www.w3.org/2001/XMLSchema", "string[]"), String[].class, ParameterMode.IN);
        call.addParameter("proxyTicket", new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, ParameterMode.IN);
        final Object[] parameters = {attributes, proxyTicket};

        // Perform the call.
        final Object returnedAttributes = call.invoke(new QName(myEndpoint,
                                                                "proxyAuthentication"), parameters);

        // Convert and return.
        return (MoriaAttribute[]) returnedAttributes;

    }

}
