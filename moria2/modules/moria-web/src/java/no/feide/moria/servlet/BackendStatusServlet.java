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

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.Properties;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.feide.moria.controller.AuthenticationException;
import no.feide.moria.controller.AuthorizationException;
import no.feide.moria.controller.DirectoryUnavailableException;
import no.feide.moria.controller.IllegalInputException;
import no.feide.moria.controller.InoperableStateException;
import no.feide.moria.controller.MoriaController;
import no.feide.moria.log.MessageLogger;

/**
 * 
 */
public class BackendStatusServlet
extends HttpServlet {

    /** Used for logging. */
    private final MessageLogger log = new MessageLogger(BackendStatusServlet.class);

    /** Copy of configuration properties. */
    private Properties config = null;

    /** Required parameters. */
    private static final String[] REQUIRED_PARAMETERS = {
    };
    
    private static final String STATUS_ATTRIBUTE = "eduPersonAffiliation";
    
    private static final String STATUS_USERNAME = "test@uninett.no";
    
    private static final String STATUS_PASSWORD = "test";
    
    private static final String STATUS_PRINCIPAL = "status";

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
        
        PrintWriter out = response.getWriter();
        out.println("<html><head><title>Moria Status Service</title></head><body>");
        
        // Prepare to check test users.
        out.println("<table>");

        // Start checking a new user.
        out.println("<tr><td>" + STATUS_USERNAME);
        try {
            
        	final Map attributes = MoriaController.directNonInteractiveAuthentication(new String[] {STATUS_ATTRIBUTE},
        		STATUS_USERNAME, STATUS_PASSWORD, STATUS_PRINCIPAL);
        	
            // This test user worked.
            out.println("OK</td></tr>");
            
        } catch (AuthenticationException e) {
            out.println("Failed authentication; check configuration</td></tr>");            
        } catch (DirectoryUnavailableException e) {
            out.println("Directory unavailable</td></tr>");            
        } catch (AuthorizationException e) {
            out.println("Failed authorization; check configuration</td></tr>");            
        } catch (IllegalInputException e) {
            out.println("Illegal input; check configuration</td></tr>");            
        } catch (InoperableStateException e) {
            out.println("Inoperable state; check configuration</td></tr>");            
        } finally {
            
            // Finish the table row.
            out.println("</td></tr>");
            
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
