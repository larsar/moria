/*
 * Copyright (c) 2004 UNINETT FAS
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * $Id$
 */

package no.feide.moria.servlet;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
 * @author Bj�rn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public class SimpleAxisServlet extends AxisServlet {

    /**
     * Identifies the directory containing the jsp-files. Assigned a value in
     * init()
     */
    private String JSP_LOCATION;

    /**
     * Default constructor
     */
    public SimpleAxisServlet() {
        super();
    }

    public void init() {
        super.init();
        /* Read value from context-param set in web.xml */
        JSP_LOCATION = getServletContext().getInitParameter("jsp.location");
    }

    /**
     * Handle HTTP GET request. As SOAP uses POST this basically returns a
     * empty page.
     * 
     * @param request
     *            The incoming HTTP request object
     * @param response
     *            The outgoing HTTP reponse object
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) {

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

        /* Retrive axis engine */
        try {
            axisEngine = getEngine();
        } catch (AxisFault fault) {
            handleException("Unable to get AxisEngine", fault, request, response);
            return;
        }

        /* Context used by the axis engine to handle the request */
        MessageContext messageContext = createMessageContext(axisEngine, request, response);

        /* Identify service */
        String serviceName = request.getServletPath();

        try {
            service = axisEngine.getService(serviceName);
        } catch (AxisFault fault) {
            handleException("Unable to get SOAPService", fault, request, response);
            return;
        }

        /* Throw NullPointerException and return if service is null */
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
            if (object instanceof Document) wsdl = (Document) object;

            /* Throw exception and return if the WSDL data was not generated */
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
            response.setContentType("text/xml; charset=UTF-8");
            printWriter.print(outputStream.toString());

        } else {
            /* Get JSP for handling HTML output */
            RequestDispatcher requestDispatcher = request.getRequestDispatcher(JSP_LOCATION + "/axis.jsp");

            /* Set the service name as attribute for the JSP */
            request.setAttribute("serviceName", serviceName);

            /* Log and return if dispatch fails */
            try {
                requestDispatcher.forward(request, response);
            } catch (Exception e) {
                handleException("Unable to dispatch request to " + JSP_LOCATION + "/axis.jsp", e, request, response);
                return;
            }
        }
    }

    /**
     * Handle HTTP POST request. This method does the real work, handling the
     * SOAP requests.
     * 
     * @param request
     *            The incomming HTTP request object
     * @param response
     *            The outgoing HTTP response object
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response) {

        /* Supposed to boost performace */
        response.setBufferSize(8192);

        /* The access point to the AXIS subsystem */
        AxisEngine axisEngine = null;

        /* The objecte representing the incoming SOAP message */
        Message requestMessage = null;

        /* The object representing the requested web service */
        SOAPService service = null;

        /* Retrive instance of AXIS engine */
        try {
            axisEngine = getEngine();
        } catch (AxisFault fault) {
            handleException("Unable to get AxisEngine", fault, request, response);
            return;
        }

        /*
         * Create message context used by the AXIS subsystem to handle the
         * request
         */
        MessageContext messageContext = createMessageContext(axisEngine, request, response);

        /* Identify service */
        String serviceName = request.getServletPath();

        /*
         * Retrive SOAP service object. Throw NullPointerException and return
         * if SOAP service is null
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

        /* Add the SOAP service to message context */
        try {
            messageContext.setService(service);
        } catch (AxisFault af) {
            handleException("Unable to set SOAPService in messageContext", af, request, response);
            return;
        }

        /*
         * Create request message. Log and return if we can't get input stream
         * from request
         */
        try {
            requestMessage = new Message(request.getInputStream(), false, request.getHeader("Content-Type"), request
                    .getHeader("Content-Location"));
        } catch (IOException ioe) {
            handleException("Unable to get InputStream from request", ioe, request, response);
            return;
        }

        /* Add the request message to the message context */
        messageContext.setRequestMessage(requestMessage);

        /* Get the SOAP action header from the HTTP request */
        String soapAction = request.getHeader("SOAPAction");

        /*
         * If the SoapAction header is undefined we set the variable equal to
         * the request uri
         */
        if (soapAction == null) soapAction = request.getRequestURI();

        /* Add the SOAP action to the message context */
        messageContext.setUseSOAPAction(true);
        messageContext.setSOAPActionURI(soapAction);

        /* Create session wrapper for the HTTP session */
        messageContext.setSession(new AxisHttpSession(request));

        /*
         * Invoke the engine and thereby process the request, log and return if
         * it fails
         */
        try {
            axisEngine.invoke(messageContext);
        } catch (AxisFault af) {
            handleException("Invocation of the axis engine failed", af, request, response);
            return;
        }

        /* Read response message from engine */
        Message responseMessage = messageContext.getResponseMessage();

        /* Log and return if no response */
        if (responseMessage == null) {
            handleException("No response from engine", new NullPointerException("responseMessage is null"), request, response);
            return;
        }

        /*
         * If we're unable to retrive the content type, set it to a default
         * value
         */
        try {
            response.setContentType(responseMessage.getContentType(messageContext.getSOAPConstants()));
        } catch (AxisFault af) {
            handleException("Unable to retrive content type of response", af, request, response);
            // TODO: Verify default content type for SOAP messages
            response.setContentType("application/soap+xml");
        }

        /* Write message to client */
        try {
            responseMessage.writeTo(response.getOutputStream());
        } catch (Exception e) {
            handleException("Unable to write response to client", e, request, response);
            return;
        }
    }

    /**
     * Create a new MessageContext, initialized with some standard values.
     * 
     * @param axisEngine
     *            the AxisEngine that will be used to handle SOAP operations
     * @param request
     *            the incoming request
     * @param response
     *            the outgoing response
     * @return a initiated MessageContext
     */
    private MessageContext createMessageContext(AxisEngine axisEngine, HttpServletRequest request, HttpServletResponse response) {
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

        /* Set up a javax.xml.rpc.server.ServletEndpointContext */
        ServletEndpointContextImpl endpointContext = new ServletEndpointContextImpl();
        messageContext.setProperty(Constants.MC_SERVLET_ENDPOINT_CONTEXT, endpointContext);

        /* Save the real path */
        String realPath = getServletContext().getRealPath(request.getServletPath());
        if (realPath != null) messageContext.setProperty(Constants.MC_REALPATH, realPath);

        /* Set config path */
        messageContext.setProperty(Constants.MC_CONFIGPATH, getWebInfPath());

        /* */
        messageContext.setProperty(MessageContext.TRANS_URL, request.getRequestURL().toString());

        return messageContext;
    }

    /**
     * Log exception with message and print user friendly error message to
     * client
     * 
     * @param message
     *            message to be logged with the exception
     * @param exception
     *            the exception to be handled
     * @param response
     *            response object for this invocation
     */
    private void handleException(String message, Exception exception, HttpServletRequest request, HttpServletResponse response) {
        // TODO: Implement. Remove throwing of ServletException, print sane
        // message and log exception.

        /* We're not able to recover from this */
        response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);

        String logMessage = generateLogMessage(message, exception);

        // TODO: Replace with log
        System.out.print(logMessage);

        /* Get JSP for handling HTML output */
        RequestDispatcher requestDispatcher;

        /* Set up the error response specially for SOAP requests */
        if (request.getMethod().equals("POST") && request.getHeader("SOAPAction") != null) {
            requestDispatcher = request.getRequestDispatcher(JSP_LOCATION + "/axis-soap-error.jsp");
            response.setContentType("application/soap+xml; charset=utf-8");
            response.setCharacterEncoding("UTF-8");
        } else {
            requestDispatcher = request.getRequestDispatcher(JSP_LOCATION + "/axis-error.jsp");

            /* Set the attributs for the response and the JSP */
            response.setContentType("text/html; charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            request.setAttribute("logMessage", logMessage);
        }

        /* Log and return if dispatch fails */
        try {
            requestDispatcher.forward(request, response);
        } catch (Exception e) {
            // TODO: Log
            return;
        }
    }

    /**
     * Generate the final string to give to the underlying log api.
     * 
     * @param message
     *            the message to log
     * @param exception
     *            exception to get stacktrace from. May be null.
     * @return the final log string
     */
    private static String generateLogMessage(final String message, final Exception exception) {

        final StringBuffer buffer = new StringBuffer();
        final String lineSeparator = System.getProperty("line.separator");

        buffer.append(message != null ? "\"" + message + "\"" : "\"-\"");
        buffer.append(lineSeparator);

        /* Capture stacktrace from exception */
        if (exception != null) {
            final OutputStream outputStream = new ByteArrayOutputStream();
            final PrintStream printStream = new PrintStream(outputStream);

            exception.printStackTrace(printStream);
            printStream.flush();

            buffer.append(outputStream.toString() + lineSeparator);
        }

        return buffer.toString();
    }
}