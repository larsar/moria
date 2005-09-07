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

package no.feide.mellon.filter;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import no.feide.mellon.Moria;
import no.feide.mellon.MoriaException;
import no.feide.moria.log.MessageLogger;

/**
 * 
 */
public class AuthenticationFilter
implements Filter {

    /**
     * The filter configuration.
     */
    private FilterConfig config = null;
    
    /**
     * Used for logging.
     */
    MessageLogger log = new MessageLogger(AuthenticationFilter.class);


    /**
     * Initialize configuration for this filter.
     * @param config
     *            The filter configuration.
     * @throws ServletException
     *             If unable to read the filter configuration.
     */
    public void init(FilterConfig config) throws ServletException {

        this.config = config;

        // Read filter configuration properties.
        try {
            System.getProperties().load(getClass().getResourceAsStream("/mellon.properties"));
        } catch (IOException e) {
            log.logWarn("Unable to read property file /mellon.properties; assuming properties are set in context", e);
        }

    }


    /**
     * Remove configuration.
     */
    public void destroy() {

        config = null;

    }


    /**
     * Perform authentication. If a requests already belongs to a session and
     * the session contains user data, the user alrady has been authenticated
     * and are let through. If no user data is present, the user is redirected
     * to the Moria2 instance for login. After the user returns following a
     * successful authentication, user data will be fetched from Moria2 and
     * stored in the <code>HttpSession</code>. Servlets, or other filters,
     * may then use these data for user authorization.
     * @param request
     *            The request.
     * @param response
     *            The response.
     * @param chain
     *            The filter chain.
     * @throws IOException
     * @throws ServletException
     *             If a problem should occur using the client-side API.
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
    throws IOException, ServletException {

        // Get the service instance.
        Moria moria = null;
        try {
            moria = Moria.getInstance();
        } catch (MoriaException e) {
            throw new ServletException(e);
        }

        // The HttpSession is used for user tracking so that the user don't have
        // to authenticate more than once.
        final HttpSession httpSession = ((HttpServletRequest) request).getSession(true);

        // The URL parameter used to identify the user after being sent back
        // from Moria2.
        final String moriaID = request.getParameter("moriaID");

        // If moriaID is null, the user is either not authenticated has been
        // authenticated on an earlier request.
        if (moriaID == null) {

            // userData is null when the user is NOT authenticated.
            if (httpSession.getAttribute("userData") == null) {

                // Construct the URL that Moria2 should redirect the user back
                // to after authentication.
                final HttpServletRequest httpRequest = (HttpServletRequest) request;
                ;
                String backToMellonURL = httpRequest.getRequestURL().toString();
                if (httpRequest.getQueryString() != null)
                    backToMellonURL += "?" + httpRequest.getQueryString() + "&moriaID=";
                else
                    backToMellonURL += "?moriaID=";

                // Establish contact with Moria and aquire a login session. The
                // user should be redirected to this URL.
                String redirectURL = null;
                try {
                    redirectURL = moria.requestSession(System.getProperty("no.feide.mellon.requestedAttributes").split(","), backToMellonURL, "", false);
                } catch (MoriaException e) {

                    // Map MoriaException as ServletException.
                    throw new ServletException(e);

                }

                // Redirect to login page.
                ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                ((HttpServletResponse) response).setHeader("Location", redirectURL);

            }

            // User data is present in the HttpSession. This indicates that the
            // user has been authenticated earlier and the request should pass
            // through this filter without any other actions taken.
            else {
                chain.doFilter(request, response);
            }
        }

        // moriaID is present in the request. The user has been redirected from
        // Moria2 following authentication. We should retrieve data about the
        // user and store it in the HttpSession for later authentication
        // (without redirecting the user).
        else {

            HashMap attributes;

            // Fetch user attributes.
            try {
                attributes = moria.getAttributes(moriaID);
            }

            // The user has NOT been authenticated. This should never be the
            // case since only authenticated users are redirected from the login
            // service.
            catch (MoriaException e) {
                ((HttpServletResponse) response).setStatus(HttpServletResponse.SC_FORBIDDEN);
                return;
            }

            // We got user data. Store it in the HttpSession for later
            // authentication. The user is authenticated, let him pass.
            httpSession.setAttribute("userData", attributes);
            chain.doFilter(request, response);

        }

    }
}
