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

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import no.feide.moria.controller.AuthenticationException;
import no.feide.moria.controller.AuthorizationException;
import no.feide.moria.controller.DirectoryUnavailableException;
import no.feide.moria.controller.IllegalInputException;
import no.feide.moria.controller.InoperableStateException;
import no.feide.moria.controller.MoriaController;
import no.feide.moria.log.MessageLogger;

/**
 * The StatusServlet shows the status of Moria. 
 */
public class StatusServlet
extends HttpServlet {

    /** Used for logging. */
    private final MessageLogger log = new MessageLogger(StatusServlet.class);

    /** Copy of configuration properties. */
    private Properties config = null;

    /**
     * List of parameters required by <code>StatusServlet</code>.
     * <br>
     * <br>
     * Current required parameters are:
     * <ul>
     * <li><code>RequestUtil.PROP_BACKENDSTATUS_STATUS_XML</code>
     * </ul>
     * @see RequestUtil#PROP_BACKENDSTATUS_STATUS_XML
     */
    private static final String[] REQUIRED_PARAMETERS = {
            RequestUtil.PROP_BACKENDSTATUS_STATUS_XML };
    
    /**
     * A hash map containing the attributes for a test-user.
     * Each item in the hashmap maps from a user name to an
     * backendStatusUser class instance
     */
    private HashMap backendDataUsers = null;
    
    private static final String STATUS_ATTRIBUTE = "eduPersonAffiliation";    
    private static final String STATUS_PRINCIPAL = "status";
    
    /**
     * Implements a simple xml parser that parses the status.xml file
     * into a HashMap with BackendStatusUser instances.
     *
     * @see BackendStatusHandler
     * @see BackendStatusUser
     */
    public final synchronized HashMap getBackendStatusData() {
         if (backendDataUsers == null) {
          Properties config = getConfig();
          if (config != null) {
            BackendStatusHandler handler = new BackendStatusHandler();
            SAXParserFactory factory = SAXParserFactory.newInstance();
            try {
               String filename = (String) config.get(RequestUtil.PROP_BACKENDSTATUS_STATUS_XML);
               SAXParser saxParser = factory.newSAXParser();
               saxParser.parse(new File(filename), handler);
            } catch (Throwable t) {
              log.logCritical("Error parsing status.xml");
            } finally {
              backendDataUsers = handler.getAttribs();
            }
          }
        }
        return backendDataUsers;
    }
    
    private void printTable() {
        HashMap backendStatusData = getBackendStatusData();
        
    }


    /**
     * Handles the GET requests.
     * @param request
     *            The HTTP request object.
     * @param response
     *            The HTTP response object.
     * @throws java.io.IOException
     *             If an input or output error is detected when the servlet
     *             handles the GET request.
     * @throws javax.servlet.ServletException
     *             If the request for the GET could not be handled.
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    public final void doGet(final HttpServletRequest request, final HttpServletResponse response)
    throws IOException, ServletException {
        getBackendStatusData();
        
        PrintWriter out = response.getWriter();
        out.println("<html><head><title>Moria Status Service</title></head><body>");
        
        //Check status
        Map statusMap = MoriaController.getStatus();
        String statusMsg = "";

        if (statusMap != null) {

            String[] states = {"moria", "init", "am", "dm", "sm", "web"};
            Map moduleNames = new HashMap();
            moduleNames.put("moria", "Moria");
            moduleNames.put("init", "Controller");
            moduleNames.put("am", "Authorization manager");
            moduleNames.put("dm", "Directory manager");
            moduleNames.put("sm", "Store manager");
            moduleNames.put("web", "Web application");

            for (int i = 0; i < states.length; i++) {

                Object stateObject = statusMap.get(states[i]);
                Boolean isReady = new Boolean(false);

                if (stateObject instanceof Boolean) {
                    isReady = (Boolean) stateObject;
                }

                if (states[i].equals("moria") && isReady.booleanValue()) {
                    statusMsg = "All ready" + System.getProperty("line.separator");
                    break;
                } else {
                    statusMsg += moduleNames.get(states[i]) + " ready: " + isReady.toString().toUpperCase()
                            + "<br />" + System.getProperty("line.separator");
                }
            }
        }
        //Print the status message
        out.println("<p><b>Status:</b><br/>" + statusMsg + "</p>");
        
        // Prepare to check test users.
        out.println("<table border=1><tr><th>Test users</th><th>Organization</th><th>Status</th></tr>");
  
        // Start checking a new user.
        
        for (Iterator iterator = backendDataUsers.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            BackendStatusUser userData = (BackendStatusUser) backendDataUsers.get(key);
            out.println("<tr><td>" +  userData.getName() + "</td>");
            try {
                out.println("<td>" + MoriaController.getUserOrg(userData.getName()) + "</td>");
                final Map attributes = MoriaController.directNonInteractiveAuthentication(new String[] {STATUS_ATTRIBUTE},
                        userData.getName(), userData.getPassword(), STATUS_PRINCIPAL);
        	
                // This test user worked.
                out.println("<td>OK</td>");
            
            } catch (AuthenticationException e) {
                out.println("<td>unknown</td><td> Failed authentication; check configuration</td></tr>");            
            } catch (DirectoryUnavailableException e) {
                out.println("<td> Directory unavailable</td></tr>");            
            } catch (AuthorizationException e) {
                out.println("<td> Failed authorization; check configuration</td></tr>");            
            } catch (IllegalInputException e) {
                out.println("<td> Illegal input; check configuration</td></tr>");            
            } catch (InoperableStateException e) {
                out.println("<td> Inoperable state; check configuration</td></tr>");            
            } finally {
            
                // Finish the table row.
                out.println("</tr>");
            
            }
        }
        
        // Done with all test users.
        out.println("</table>");
        
        // Finish up.
        out.println("</body></html>");

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
    private Properties getConfig() {

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

}
