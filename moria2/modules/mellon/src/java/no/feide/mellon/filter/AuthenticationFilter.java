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
import java.io.InputStream;
import java.util.HashMap;
import java.util.Properties;

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
 * The less-than-elegant use of system properties to store filter configuration
 * has been preserved to provide maximum backwards compatibility with the
 * Mellon1 version of this filter.
 */
public class AuthenticationFilter
implements Filter {

    /**
     * Used for logging.
     */
    private MessageLogger log = new MessageLogger(AuthenticationFilter.class);


    /**
     * Required configuration properties.<br>
     * <br>
     * Current values are
     * <ul>
     * <li><code>"no.feide.mellon.serviceUsername"</code></li>
     * <li><code>"no.feide.mellon.servicePassword"</code></li>
     * <li><code>"no.feide.mellon.endpoint"</code></li>
     * </ul>
     */
    public static final String[] REQUIRED_PROPERTIES = {"no.feide.mellon.serviceUsername", "no.feide.mellon.servicePassword", "no.feide.mellon.endpoint"};


    /**
     * Optional configuration properties.<br>
     * <br>
     * Current values are
     * <ul>
     * <li><code>"no.feide.mellon.requestedAttributes"</code></li>
     * <li><code>"no.feide.mellon.runtimeConfiguration"</code></li>
     * </ul>
     */
    public static final String[] OPTIONAL_PROPERTIES = {"no.feide.mellon.requestedAttributes", "no.feide.mellon.runtimeConfiguration"};

    /**
     * The filename of the filter configuration file.<br>
     * <br>
     * Current value is <code>"/mellon.properties"</code>.
     */
    public static final String PROPERTY_FILE = "/mellon.properties";


    /**
     * Initialize configuration for this filter.
     * @param config
     *            The filter configuration.
     * @throws ServletException
     *             If unable to set the filter's Mellon2 configuration.
     */
    public void init(FilterConfig config) throws ServletException {

        // Read filter configuration properties from file.
        Properties properties = new Properties();
        InputStream file = getClass().getResourceAsStream(PROPERTY_FILE);
        if (file != null)
            try {
                properties.load(file);
            } catch (IOException e) {
                log.logWarn("Unable to read an existing property file '" + PROPERTY_FILE + "'", e);
            }

        // Process required configuration properties.
        for (int i = 0; i < REQUIRED_PROPERTIES.length; i++)
            setProperty(REQUIRED_PROPERTIES[i], config, properties);

        // Process optional configuration properties.
        for (int i = 0; i < OPTIONAL_PROPERTIES.length; i++)
            try {
                setProperty(OPTIONAL_PROPERTIES[i], config, properties);
            } catch (ServletException e) {
                log.logInfo("Optional property '" + OPTIONAL_PROPERTIES[i] + "' not set");
            }

    }


    /**
     * Utility method used to set each configuration property. System properties
     * already set take precedence over properties set in the filter's
     * configuration, which again take precedence over properties set in the
     * configuration file <code>PROPERTY_FILE</code>.
     * @param property
     *            The property to set. string.
     * @param filterConfig
     *            The filter's configuration.
     * @param propertyFile
     *            Properties read from the file <code>PROPERTY_FILE</code>.
     * @throws ServletException
     *             If unable to set <code>property</code> based on these three
     *             sources.
     */
    private void setProperty(String property, FilterConfig filterConfig, Properties propertyFile)
    throws ServletException {

        // Make sure to log any success before returning.
        String value = null;
        try {

            // Check already set system properties.
            value = System.getProperty(property);
            if (value != null)
                return;

            // Check filter configuration.
            value = filterConfig.getInitParameter(property);
            if (value != null) {
                System.setProperty(property, value);
                return;
            }

            // Check property file.
            value = propertyFile.getProperty(property);
            if (value != null) {
                System.setProperty(property, value);
                return;
            }

        } finally {
            log.logDebug(property + "=" + value);
        }

        throw new ServletException("Unable to set property '" + property + "'");
    }


    /**
     * Does nothing.
     */
    public void destroy() {

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

    }}
