/*
 * Copyright (c) 2004 UNINETT FAS This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA. $Id: SimpleAxisServlet.java,v 1.7 2006/01/09
 * 10:44:29 catoolsen Exp $
 */

package no.feide.moria.servlet.soap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.rmi.RemoteException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.feide.moria.log.MessageLogger;

import org.apache.axis.AxisEngine;
import org.apache.axis.AxisFault;
import org.apache.axis.Constants;
import org.apache.axis.Message;
import org.apache.axis.MessageContext;
import org.apache.axis.handlers.soap.SOAPService;
import org.apache.axis.transport.http.AxisHttpSession;
import org.apache.axis.transport.http.AxisServlet;
import org.apache.axis.transport.http.HTTPConstants;
import org.apache.axis.transport.http.ServletEndpointContextImpl;
import org.apache.xml.serialize.XMLSerializer;
import org.w3c.dom.Document;

/**
 * @author Bj&oslash;rn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public final class SimpleAxisServlet
extends AxisServlet {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 6202683940363529171L;

    /**
     * Logger for this class.
     */
    private MessageLogger messageLogger = new MessageLogger(SimpleAxisServlet.class);

    /**
     * The default content type, when it is necessary to override Axis' defaults
     * (not likely, but used as a fallback).<br>
     * <br>
     * Current value is <code>"text/xml; charset=utf-8"</code>.
     */
    // TODO: Verify default content type for SOAP messages.
    private final static String DEFAULT_CONTENT_TYPE = "text/xml; charset=utf-8";

    /**
     * Default constructor.
     */
    public SimpleAxisServlet() {

        super();
    }

    /**
     * Initializes the servlet. Called by the container.
     * @throws ServletException
     *             If unable to initialize the servlet.
     */
    public void init() throws ServletException {

        super.init();
    }

    /**
     * Handles HTTP GET requests. As SOAP uses POST, this basically returns a
     * empty page.
     * @param request
     *            The incoming HTTP request object.
     * @param response
     *            The outgoing HTTP reponse object.
     */
    public void doGet(final HttpServletRequest request,
                      final HttpServletResponse response) {

        /* Avoid null-pointer exceptions. */
        if (request == null) {
            messageLogger.logCritical("Response object cannot be null", new NullPointerException("request is null"));
            return;
        }

        if (response == null) {
            messageLogger.logCritical("Response object cannot be null", new NullPointerException("response is null"));
            return;
        }

        /* The access point to the web services */
        AxisEngine axisEngine = null;

        /* The object representing the requested web service */
        SOAPService service = null;

        /* Writer for response */
        PrintWriter printWriter = null;

        /* Get writer for the WSDL output (XML) */
        try {
            printWriter = response.getWriter();
        } catch (IOException ioe) {
            handleException("Unable to get response writer", ioe, request, response);
            return;
        }

        /* Retrieve axis engine */
        try {
            axisEngine = getEngine();
        } catch (AxisFault fault) {
            handleException("Unable to get AxisEngine", fault, request, response);
            return;
        }

        /* Context used by the axis engine to handle the request */
        MessageContext messageContext = createMessageContext(axisEngine, request, response);

        /* Set the username given in the request. */
        messageContext.setUsername(request.getRemoteUser());

        /* Identify service. */
        String serviceName = request.getServletPath();

        try {
            service = axisEngine.getService(serviceName);
        } catch (AxisFault fault) {
            handleException("Unable to get SOAPService", fault, request, response);
            return;
        }

        /* Make NullPointerException page and return if service is null */
        if (service == null) {
            handleException("No SOAPService object returned", new NullPointerException("service is null"), request, response);
            return;
        }

        /* Add service to message context */
        try {
            messageContext.setService(service);
        } catch (AxisFault af) {
            handleException("Unable to set SOAPService in messageContext", af, request, response);
            return;
        }

        /* Identify type of response (HTML or WSDL) */
        String queryString = request.getQueryString();

        /*
         * Return WSDL if correct query string has been given, otherwise return
         * simple HTML page
         */
        if (queryString != null && queryString.equalsIgnoreCase("wsdl")) {

            /* XML document object to hold the returned WSDL data */
            Document wsdl = null;

            /* Get the service to generate the WSDL */
            try {
                service.generateWSDL(messageContext);
            } catch (AxisFault af) {
                handleException("Unable to generate WSDL", af, request, response);
                return;
            }

            /* The generated XML is put into the context */
            Object object = messageContext.getProperty("WSDL");
            if (object instanceof Document)
                wsdl = (Document) object;

            /* Make exception page and return if the WSDL data was not generated */
            if (wsdl == null) {
                handleException("No WSDL data available", new NullPointerException("wsdl is null"), request, response);
                return;
            }

            /* Serialize DOM to XML string */
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            XMLSerializer xmlSerializer = new XMLSerializer();
            xmlSerializer.setOutputByteStream(outputStream);

            /* Log and return if serialization fails */
            try {
                xmlSerializer.serialize(wsdl);
            } catch (IOException ioe) {
                handleException("Unable to serialize WSDL", ioe, request, response);
                return;
            }

            /* Print result to client */
            response.setCharacterEncoding("UTF-8");
            response.setContentType(DEFAULT_CONTENT_TYPE);
            messageLogger.logDebug("Response content type on GET is '" + DEFAULT_CONTENT_TYPE + "' by default");
            printWriter.print(outputStream.toString());

        } else {
            /* Get JSP for handling HTML output */
            RequestDispatcher requestDispatcher = request.getSession().getServletContext().getNamedDispatcher("Axis.JSP");

            /* Set the service name as attribute for the JSP */
            request.setAttribute("serviceName", serviceName);

            /* Log and return if dispatch fails */
            try {
                requestDispatcher.forward(request, response);
            } catch (Exception e) {
                // handleException("Unable to dispatch request to " +
                // jspLocation + "/Axis.JSP", e, request, response);
                return;
            }
        }
    }

    /**
     * Handles HTTP POST requests. This method does the real work, handling the
     * SOAP requests.
     * @param request
     *            The incoming HTTP request object.
     * @param response
     *            The outgoing HTTP response object.
     */
    public void doPost(final HttpServletRequest request,
                       final HttpServletResponse response) {

        /* Avoid null-pointer exceptions. */
        if (request == null) {
            messageLogger.logCritical("Response object cannot be null", new NullPointerException("request is null"));
            return;
        }

        if (response == null) {
            messageLogger.logCritical("Response object cannot be null", new NullPointerException("response is null"));
            return;
        }

        /* Supposed to boost performace. */
        response.setBufferSize(8192);

        /* The access point to the AXIS subsystem. */
        AxisEngine axisEngine = null;

        /* The objecte representing the incoming SOAP message. */
        Message requestMessage = null;

        /* The object representing the requested web service. */
        SOAPService service = null;

        /* Retrieve instance of AXIS engine. */
        try {
            axisEngine = getEngine();
        } catch (AxisFault fault) {
            handleException("Unable to get AxisEngine", fault, request, response);
            return;
        }

        /*
         * Create message context used by the AXIS subsystem to handle the
         * request.
         */
        MessageContext messageContext = createMessageContext(axisEngine, request, response);

        /* Set the username given in the request. */
        messageContext.setUsername(request.getRemoteUser());

        /* Identify service. */
        String serviceName = request.getServletPath();

        /*
         * Retrieve SOAP service object. Make NullPointerException page and
         * return if SOAP service is null.
         */
        try {
            service = axisEngine.getService(serviceName);
        } catch (AxisFault fault) {
            handleException("Unable to get SOAPService", fault, request, response);
            return;
        }

        if (service == null) {
            handleException("No SOAPService object returned", new NullPointerException("service is null"), request, response);
            return;
        }

        /* Add the SOAP service to message context. */
        try {
            messageContext.setService(service);
        } catch (AxisFault af) {
            handleException("Unable to set SOAPService in messageContext", af, request, response);
            return;
        }

        /*
         * Create request message. Log and return if we can't get input stream
         * from request.
         */
        try {
            requestMessage = new Message(request.getInputStream(), false, request.getHeader("Content-Type"), request.getHeader("Content-Location"));
        } catch (IOException ioe) {
            handleException("Unable to get InputStream from request", ioe, request, response);
            return;
        }

        /* Add the request message to the message context. */
        messageContext.setRequestMessage(requestMessage);

        /* Get the SOAP action header from the HTTP request. */
        String soapAction = request.getHeader("SOAPAction");

        /*
         * If the SoapAction header is undefined we set the variable equal to
         * the request uri.
         */
        if (soapAction == null)
            soapAction = request.getRequestURI();

        /* Add the SOAP action to the message context. */
        messageContext.setUseSOAPAction(true);
        messageContext.setSOAPActionURI(soapAction);

        /* Create session wrapper for the HTTP session. */
        messageContext.setSession(new AxisHttpSession(request));

        /*
         * Invoke the engine and thereby process the request, log and return if
         * it fails.
         */
        try {
            axisEngine.invoke(messageContext);
        } catch (AxisFault af) {

            if ((af.getFaultString().startsWith("No such operation")) || (af.getFaultString().startsWith("org.xml.sax.SAXException"))) {

                // Handle exceptions caused by illegal input.
                IllegalInputException e = new IllegalInputException(af.getFaultString());
                handleException("Invocation of the Axis engine failed", e, request, response);

            } else

                // All other Axis exceptions.
                handleException("Invocation of the axis engine failed", af, request, response);

            return;
        }

        /* Read response message from engine. */
        Message responseMessage = messageContext.getResponseMessage();

        /* Log and return if no response. */
        if (responseMessage == null) {
            handleException("No response from engine", new NullPointerException("responseMessage is null"), request, response);
            return;
        }

        /*
         * If we're unable to retrieve the content type, set it to a default
         * value.
         */
        try {
            response.setContentType(responseMessage.getContentType(messageContext.getSOAPConstants()));
        } catch (AxisFault af) {
            handleException("Unable to retrieve content type of response", af, request, response);
            response.setContentType(DEFAULT_CONTENT_TYPE);
        }

        /* Write message to client */
        messageLogger.logDebug("Response content type on POST is '" + response.getContentType() + "' from Axis");
        try {
            responseMessage.writeTo(response.getOutputStream());
        } catch (Exception e) {
            handleException("Unable to write response to client", e, request, response);
            return;
        }

    }

    /**
     * Creates a new MessageContext, initialized with some standard values.
     * @param axisEngine
     *            The AxisEngine that will be used to handle SOAP operations.
     * @param request
     *            The incoming request.
     * @param response
     *            The outgoing response.
     * @return An initialized MessageContext.
     */
    private MessageContext createMessageContext(final AxisEngine axisEngine,
                                                final HttpServletRequest request,
                                                final HttpServletResponse response) {

        /* Create new message context */
        MessageContext messageContext = new MessageContext(axisEngine);

        /* Set the transport */
        messageContext.setTransportName("transport.name");

        /* Save some HTTP specific info in the bag in case someone needs it */
        messageContext.setProperty(Constants.MC_RELATIVE_PATH, request.getServletPath());
        messageContext.setProperty(Constants.MC_REMOTE_ADDR, request.getRemoteAddr());
        messageContext.setProperty(HTTPConstants.MC_HTTP_SERVLET, this);
        messageContext.setProperty(HTTPConstants.MC_HTTP_SERVLETREQUEST, request);
        messageContext.setProperty(HTTPConstants.MC_HTTP_SERVLETRESPONSE, response);
        messageContext.setProperty(HTTPConstants.MC_HTTP_SERVLETPATHINFO, request.getPathInfo());
        messageContext.setProperty(HTTPConstants.MC_HTTP_SERVLETLOCATION, getWebInfPath());
        messageContext.setProperty(HTTPConstants.HEADER_AUTHORIZATION, request.getHeader(HTTPConstants.HEADER_AUTHORIZATION));

        /* Set up a javax.xml.rpc.server.ServletEndpointContext. */
        ServletEndpointContextImpl endpointContext = new ServletEndpointContextImpl();
        messageContext.setProperty(Constants.MC_SERVLET_ENDPOINT_CONTEXT, endpointContext);

        /* Save the real path. */
        String realPath = getServletContext().getRealPath(request.getServletPath());

        if (realPath != null)
            messageContext.setProperty(Constants.MC_REALPATH, realPath);

        /* Set config path. */
        messageContext.setProperty(Constants.MC_CONFIGPATH, getWebInfPath());

        /* Axis voodoo. */
        messageContext.setProperty(MessageContext.TRANS_URL, request.getRequestURL().toString());

        return messageContext;
    }

    /**
     * Logs exception with message and prints user friendly error message to
     * client.
     * @param message
     *            Message to be logged with the exception.
     * @param exception
     *            The exception to be handled.
     * @param request
     *            Request object for this invocation.
     * @param response
     *            Response object for this invocation.
     */
    private void handleException(final String message,
                                 final Exception exception,
                                 final HttpServletRequest request,
                                 final HttpServletResponse response) {

        /* We're not able to recover from this */
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        /* Get the exception cause. */
        Throwable cause = null;
        if (exception instanceof AxisFault)
            cause = exception.getCause();
        else
            cause = exception;

        /* Used later to get the JSP for handling HTML output. */
        RequestDispatcher requestDispatcher;

        /* Set up the error response specially for SOAP requests. */
        if (request.getMethod().equals("POST") && request.getHeader("SOAPAction") != null) {

            /*
             * Set the faultCode and faultString elements, depending on
             * exception.
             */
            if (cause instanceof SOAPException) {

                /* A known type of exception. */
                request.setAttribute("faultCode", ((SOAPException) cause).getFaultcode());
                request.setAttribute("faultString", ((SOAPException) cause).getFaultstring());

            } else if (cause instanceof RemoteException) {

                // An older type of exception handling, replaced by the v2.1
                // SOAP interface onwards.
                request.setAttribute("faultCode", "Server");
                request.setAttribute("faultString", cause.getMessage());

            } else {

                /* An unknown type of exception. Should not happen. */
                InternalException internal = new InternalException("");
                request.setAttribute("faultCode", internal.getFaultcode());
                request.setAttribute("faultString", internal.getFaultstring());
                messageLogger.logCritical("Unknown internal exception", cause);

            }

            /* Log what we're doing. */
            messageLogger.logWarn("Replying with SOAP Fault - faultCode: '" + request.getAttribute("faultCode") + "' faultString: '" + request.getAttribute("faultString") + "'", cause);

            /* Get the request dispatcher. */
            requestDispatcher = request.getSession().getServletContext().getNamedDispatcher("Axis-SOAP-Error.JSP");

        } else {

            /* Not a SOAP request? */
            request.setAttribute("logMessage", message);
            requestDispatcher = request.getSession().getServletContext().getNamedDispatcher("Axis-Error.JSP");

        }

        /* Log and return if dispatch fails */
        try {
            requestDispatcher.forward(request, response);
        } catch (Exception e) {
            messageLogger.logCritical("Unable to dispatch to Axis error jsp", e);
            return;
        }
    }
}
