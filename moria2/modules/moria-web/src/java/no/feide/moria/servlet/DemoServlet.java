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

package no.feide.moria.servlet;

import no.feide.moria.log.MessageLogger;
import no.feide.moria.webservices.v2_0.Attribute;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.rpc.ParameterMode;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.axis.client.Call;
import org.apache.axis.encoding.ser.VectorDeserializerFactory;
import org.apache.axis.encoding.ser.VectorSerializerFactory;

/**
 * This is a simple demonstration servlet, primarily intended as a code example
 * on how to access the Moria SOAP interface. <br>
 * <br>
 * This implementation uses Axis for its remote operations; for details on how
 * this is done refer to the private methods
 * <ul>
 * <li><code>initiateAuthentication</code>
 * <li><code>getUserAttributes</code>
 * </ul>
 * These methods mirror the server-side SOAP methods published by the Moria
 * instance, but are not generated by Axis' SOAP tools (WSDL2Java). For details
 * on how to implement client-side methods using other SOAP implementations than
 * Axis (such as Sun's JAX-RPC) or other programming languages, please refer to
 * the documentation for the SOAP implementation/library in question. <br>
 * <br>
 * The servlet works as follows: <br>
 * <ol>
 * <li>If the HTTP request does not contain a service ticket (found by checking
 * URL parameter <code>PARAM_TICKET</code>) the user is redirected to a Moria
 * instance (given by <code>SERVICE_ENDPOINT</code>) for authentication. <br>
 * This is done using the <code>initiateAuthentication(...)</code> method to
 * receive a correct redirect URL to the Moria instance.
 * <li>Once the user has gone through a successful authentication, he or she is
 * redirected back to this servlet, now with the previously missing service
 * ticket.
 * <li>The servlet then attempts to read the attributes requested by the
 * earlier use of <code>initiateAuthentication(...)</code>, by using
 * <code>getUserAttributes(...)</code> with the given service ticket.
 * <li>If successful, the attributes and their values are then displayed.
 * </ol>
 * A few other points to note:
 * <ul>
 * <li>The attributes requested are given by <code>ATTRIBUTE_REQUEST</code>.
 * <li>This servlet, as a Moria client service, authenticates itself to Moria
 * using the username/password combination given by <code>CLIENT_USERNAME</code>
 * and <code>CLIENT_PASSWORD</code>.
 * <li>The Moria instance used is given by <code>SERVICE_ENDPOINT</code>.
 * </ul>
 * @see #initiateAuthentication(String[], String, String, boolean)
 * @see #getUserAttributes(String)
 * @see #PARAM_TICKET
 * @see #SERVICE_ENDPOINT
 * @see #MAIN_ATTRIBUTE_REQUEST
 * @see #CLIENT_USERNAME
 * @see #CLIENT_PASSWORD
 * @see #SERVICE_ENDPOINT
 */
public class DemoServlet
extends HttpServlet {

    /** Used for logging. */
    private final MessageLogger log = new MessageLogger(DemoServlet.class);

    /** Copy of configuration properties. */
    private Properties config = null;

    private String[] REQUIRED_PARAMETERS = {"MASTER_ATTRIBUTE_REQUEST", "SLAVE_ATTRIBUTE_REQUEST", "SERVICE_ENDPOINT", "MASTER_CLIENT_USERNAME", "MASTER_CLIENT_PASSWORD", "SLAVE_CLIENT_USERNAME", "SLAVE_CLIENT_PASSWORD", "ATTRIBUTE_QNAME"};

    /**
     * Name of the URL parameter used to retrieve the Moria service ticket. <br>
     * <br>
     * Current value is <code>"ticket"</code>.
     */
    private final String PARAM_TICKET = "ticket";


    /**
     * Handles the GET requests.
     * @param request
     *            The HTTP request object. If it contains a request parameter
     *            <i>moriaID </i> then the request's attribute <i>attributes
     *            </i> will be filled with the attributes contained in the
     *            session given by <i>moriaID </i>.
     * @param response
     *            The HTTP response object.
     * @throws java.io.IOException
     *             If an input or output error is detected when the servlet
     *             handles the GET request.
     * @throws javax.servlet.ServletException
     *             If the request for the GET could not be handled.
     * @see javax.servlet.http.HttpServlet.doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    public final void doGet(final HttpServletRequest request, final HttpServletResponse response)
    throws IOException, ServletException {

        // Be sure to dump all exceptions.
        try {

            // Handle logout request.
            if (request.getParameter("logout") != null) {
                
                // Redirect to the configured logout URL.
                response.sendRedirect(config.getProperty(RequestUtil.PROP_DEMO_LOGOUT_URL));
            }

            // Get configuration.
            final Properties config = getConfig();

            // Do we have a ticket?
            final String ticket = request.getParameter(PARAM_TICKET);
            if (ticket == null) {

                // No ticket; redirect for authentication.
                String redirectURL = initiateAuthentication(convert(config.getProperty(RequestUtil.PROP_DEMO_MASTER_ATTRIBUTE_REQUEST)), request.getRequestURL().toString() + "?" + PARAM_TICKET + "=", "", true);
                response.sendRedirect(redirectURL);

            } else {

                // We have a ticket.
                PrintWriter out = response.getWriter();
                out.println("<html><head><title>Moria Demo Service</title></head><body>");
                out.println("<h1 align=\"center\">Authentication successful</h1>");
                out.println("<p align=\"center\"><a href=\"" + config.getProperty(RequestUtil.PROP_DEMO_LOGOUT_URL) + "\">Logout</a></p>");
                out.println("<i>System '" + config.getProperty(RequestUtil.PROP_DEMO_MASTER_USERNAME) + "':</i>");
                String ticketGrantingTicket = null; // For later use.

                // Get and display attributes.
                Attribute[] attributes = getUserAttributes(ticket);
                out.println("<table align=\"center\"><tr><td><b>Attribute Name</b></td><td><b>Attribute Value(s)</b></td></tr>");
                for (int i = 0; i < attributes.length; i++) {

                    // Is this the ticket granting ticket?
                    String name = attributes[i].getName();
                    if (name.equals("tgt")) {

                        // Just remember the ticket for later use.
                        ticketGrantingTicket = attributes[i].getValues()[0];

                    }

                    else {

                        // Show the actual attribute and its values.
                        out.println("<tr><td>" + name + "</td>");
                        String[] values = attributes[i].getValues();
                        for (int j = 0; j < values.length; j++) {
                            if (j > 0)
                                out.println("<tr><td></td>");
                            out.println("<td>" + values[j] + "</td></tr>");
                        }

                    }

                }
                out.println("</table>");
                out.println("<p><b>Ticket granting ticket:</b> <tt>" + ticketGrantingTicket + "</tt></p>");

                // Should we try to fake a subsystem using SSO?
                out.println("<hr><i>Subsystem '" + config.getProperty(RequestUtil.PROP_DEMO_SLAVE_USERNAME) + "':</i>");
                if (ticketGrantingTicket == null) {

                    out.println("<p align=\"center\">No ticket granting ticket was retrieved.<br>SSO denied.</p>");

                } else {

                    // Now get a proxy ticket for your subsystem (we're actually
                    // our own subsystem, to keep the code simple).
                    final String proxyTicket = getProxyTicket(ticketGrantingTicket, config.getProperty(RequestUtil.PROP_DEMO_SLAVE_USERNAME));

                    // We now have a proxy ticket; now let's fake our own
                    // subsystem. Retrieve and display some attributes.
                    attributes = proxyAuthentication(convert(config.getProperty(RequestUtil.PROP_DEMO_SLAVE_ATTRIBUTE_REQUEST)), proxyTicket);
                    out.println("<table align=\"center\"><tr><td><b>Attribute Name</b></td><td><b>Attribute Value(s)</b></td></tr>");
                    for (int i = 0; i < attributes.length; i++) {

                        // Show the actual attribute and its values.
                        String name = attributes[i].getName();
                        out.println("<tr><td>" + name + "</td>");
                        String[] values = attributes[i].getValues();
                        for (int j = 0; j < values.length; j++) {
                            if (j > 0)
                                out.println("<tr><td></td>");
                            out.println("<td>" + values[j] + "</td></tr>");
                        }

                    }
                    out.println("</table>");
                    out.println("<p><b>Proxy ticket:</b> <tt>" + proxyTicket + "</tt></p>");

                }

                // We're done!
                out.println("</html></body>");

            }

        } catch (RemoteException e) {
            log.logCritical("RemoteException caught", e);
            throw new ServletException(e);
        }

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
     * @see #SERVICE_ENDPOINT
     */
    private String initiateAuthentication(final String[] attributeRequest, final String urlPrefix, final String urlPostfix, final boolean denySSO)
    throws MalformedURLException, RemoteException {

        // Get configuration.
        final Properties config = getConfig();

        // Preparing call.
        Call call = new Call(new URL(config.getProperty(RequestUtil.PROP_DEMO_SERVICE_ENDPOINT) + "?WSDL"));
        call.setUsername(config.getProperty(RequestUtil.PROP_DEMO_MASTER_USERNAME));
        call.setPassword(config.getProperty(RequestUtil.PROP_DEMO_MASTER_PASSWORD));
        final Object[] parameters = {convert(config.getProperty(RequestUtil.PROP_DEMO_MASTER_ATTRIBUTE_REQUEST)), urlPrefix, urlPostfix, new Boolean(false)};

        // Performing call.
        return (String) call.invoke(new QName("initiateAuthentication"), parameters);

    }


    /**
     * Get the previously requested (through
     * <code>initiateAuthentication(...)</code> attributes following a
     * successful user authentication.
     * @param serviceTicket
     *            A legal service ticket returned to the client service
     *            following a successful authentication on the user's part.
     * @return An array of <code>Attribute</code> objects, containing the
     *         resulting values of the original attribute request. Note that if
     *         the requested attributes does not exist, or if the client service
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
    private Attribute[] getUserAttributes(String serviceTicket)
    throws MalformedURLException, RemoteException {

        // Get configuration
        final Properties config = getConfig();

        // Prepare call.
        Call call = new Call(new URL(config.getProperty(RequestUtil.PROP_DEMO_SERVICE_ENDPOINT) + "?WSDL"));
        call.setUsername(config.getProperty(RequestUtil.PROP_DEMO_MASTER_USERNAME));
        call.setPassword(config.getProperty(RequestUtil.PROP_DEMO_MASTER_PASSWORD));
        final QName attributeQName = new QName(config.getProperty(RequestUtil.PROP_DEMO_ATTRIBUTE_NAMESPACE_URI), "Attribute");
        call.setReturnType(attributeQName);
        VectorSerializerFactory serializer = new VectorSerializerFactory(Attribute.class, attributeQName);
        VectorDeserializerFactory deserializer = new VectorDeserializerFactory(Attribute.class, attributeQName);
        call.registerTypeMapping(Attribute.class, attributeQName, serializer, deserializer);
        call.addParameter("serviceTicket", new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, ParameterMode.IN);
        final Object[] parameters = {serviceTicket};

        // Perform the call.
        final Object returnedAttributes = call.invoke(new QName(config.getProperty(RequestUtil.PROP_DEMO_SERVICE_ENDPOINT), "getUserAttributes"), parameters);

        // Convert and return.
        return (Attribute[]) returnedAttributes;

    }


    /**
     * Get a proxy ticket, by supplying a previously retrieved ticket granting
     * ticket for the given subsystem. This ticket may then be used by the
     * subsystem to assume that authentication has taken place. <br>
     * <br>
     * @param ticketGrantingTicket
     *            A legal ticket granting ticket previously retrieved by
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
    private String getProxyTicket(final String ticketGrantingTicket, final String proxyServicePrincipal)
    throws MalformedURLException, RemoteException {

        // Get configuration.
        final Properties config = getConfig();

        // Preparing call.
        Call call = new Call(new URL(config.getProperty(RequestUtil.PROP_DEMO_SERVICE_ENDPOINT) + "?WSDL"));
        call.setUsername(config.getProperty(RequestUtil.PROP_DEMO_MASTER_USERNAME));
        call.setPassword(config.getProperty(RequestUtil.PROP_DEMO_MASTER_PASSWORD));
        final Object[] parameters = {ticketGrantingTicket, proxyServicePrincipal};

        // Performing call.
        return (String) call.invoke(new QName("getProxyTicket"), parameters);

    }


    /**
     * Use a proxy ticket retrieved by <code>getProxyTicket(...)</code> to
     * verify that the user has been authenticated, and retrieve a number of
     * attributes.
     * @param attributes
     *            The attributes requested as part of this proxy authentication.
     * @param proxyTicket
     *            The proxy ticket.
     * @return An array of <code>Attribute</code> objects, containing the
     *         resulting values of the original attribute request. Note that if
     *         the requested attributes does not exist, or if the client service
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
    private Attribute[] proxyAuthentication(final String[] attributes, final String proxyTicket)
    throws MalformedURLException, RemoteException {

        // Get configuration.
        final Properties config = getConfig();

        // Prepare call.
        Call call = new Call(new URL(config.getProperty(RequestUtil.PROP_DEMO_SERVICE_ENDPOINT) + "?WSDL"));
        call.setUsername(config.getProperty(RequestUtil.PROP_DEMO_SLAVE_USERNAME));
        call.setPassword(config.getProperty(RequestUtil.PROP_DEMO_SLAVE_PASSWORD));
        final QName attributeQName = new QName(config.getProperty(RequestUtil.PROP_DEMO_ATTRIBUTE_NAMESPACE_URI), "Attribute");
        call.setReturnType(attributeQName);
        VectorSerializerFactory serializer = new VectorSerializerFactory(Attribute.class, attributeQName);
        VectorDeserializerFactory deserializer = new VectorDeserializerFactory(Attribute.class, attributeQName);
        call.registerTypeMapping(Attribute.class, attributeQName, serializer, deserializer);
        call.addParameter("attributes", new QName("http://www.w3.org/2001/XMLSchema", "string[]"), String[].class, ParameterMode.IN);
        call.addParameter("proxyTicket", new QName("http://www.w3.org/2001/XMLSchema", "string"), String.class, ParameterMode.IN);
        final Object[] parameters = {convert(config.getProperty(RequestUtil.PROP_DEMO_SLAVE_ATTRIBUTE_REQUEST)), proxyTicket};

        // Perform the call.
        final Object returnedAttributes = call.invoke(new QName(config.getProperty(RequestUtil.PROP_DEMO_SERVICE_ENDPOINT), "proxyAuthentication"), parameters);

        // Convert and return.
        return (Attribute[]) returnedAttributes;

    }


    /**
     * Get this servlet's configuration from the web module, given by
     * <code>RequestUtil.PROP_CONFIG</code>.
     * @return The last valid configuration.
     * @throws IllegalStateException
     *             If unable to read the current configuration from the servlet
     *             context, and there is no previous configuration. Also thrown
     *             if any of the required parameters (given by
     *             <code>REQUIRED_PARAMETERS</code>) are not set.
     * @see #REQUIRED_PARAMETERS
     * @see RequestUtil#PROP_CONFIG
     */
    private Properties getConfig() throws IllegalStateException {

        // Validate configuration, and check whether we have a fallback.
        try {
            config = (Properties) getServletContext().getAttribute(RequestUtil.PROP_CONFIG);
        } catch (ClassCastException e) {
            log.logInfo("Unable to get configuration from context");
        }
        if (config == null)
            throw new IllegalStateException("Configuration is not set");

        // Are we missing some required properties?
        for (int i = 0; i < REQUIRED_PARAMETERS.length; i++) {
            String requiredParameter = REQUIRED_PARAMETERS[i];
            if ((requiredParameter == null) || (requiredParameter.equals("")))
                throw new IllegalStateException("Required parameter '" + requiredParameter + "' is not set");
        }
        return config;

    }


    /**
     * Convert a comma-separated list into an array.
     * @param commaSeparatedList
     *            A comma-separated list of elements. May be <code>null</code>
     *            or an empty string.
     * @return <code>commaSeparatedList</code> as a string array. Will always
     *         return an empty array if <code>commaSeparatedList</code> is
     *         <code>null</code> or an empty string.
     */
    private String[] convert(String commaSeparatedList) {

        // Sanity checks.
        if ((commaSeparatedList == null) || (commaSeparatedList.length() == 0))
            return new String[] {};

        // Convert and return.
        ArrayList buffer = new ArrayList();
        StringTokenizer tokenizer = new StringTokenizer(commaSeparatedList, ",");
        while (tokenizer.hasMoreTokens())
            buffer.add(tokenizer.nextToken());
        return (String[]) buffer.toArray(new String[] {});

    }

}