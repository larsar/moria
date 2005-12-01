/*
 * Copyright (c) 2004 UNINETT FAS
 * 
 * This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * $Id$
 */

package no.feide.moria.servlet;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.TreeMap;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.feide.moria.authorization.UnknownServicePrincipalException;
import no.feide.moria.controller.AuthenticationException;
import no.feide.moria.controller.AuthorizationException;
import no.feide.moria.controller.DirectoryUnavailableException;
import no.feide.moria.controller.IllegalInputException;
import no.feide.moria.controller.InoperableStateException;
import no.feide.moria.controller.MoriaController;
import no.feide.moria.controller.MoriaControllerException;
import no.feide.moria.controller.UnknownTicketException;
import no.feide.moria.log.MessageLogger;

/**
 * Use this servlet to bootstrap the system. Set
 * &lt;load-on-startup&gt;1&lt;/load-on-startup&gt; in web.xml.
 * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
 * @version $Revision$
 */
public final class LoginServlet
extends HttpServlet {

    /** Used for logging. */
    private final MessageLogger log = new MessageLogger(LoginServlet.class);

    /**
     * List of parameters required by <code>LoginServlet</code>. <br>
     * <br>
     * Current required parameters are:
     * <ul>
     * <li><code>RequestUtil.PROP_COOKIE_SSO</code>
     * <li><code>RequestUtil.PROP_COOKIE_SSO_TTL</code>
     * <li><code>RequestUtil.PROP_COOKIE_DENYSSO</code>
     * <li><code>RequestUtil.PROP_COOKIE_DENYSSO_TTL</code>
     * <li><code>RequestUtil.PROP_COOKIE_LANG</code>
     * <li><code>RequestUtil.PROP_COOKIE_LANG_TTL</code>
     * <li><code>RequestUtil.PROP_COOKIE_ORG</code>
     * <li><code>RequestUtil.PROP_COOKIE_ORG_TTL</code>
     * <li><code>RequestUtil.PROP_LOGIN_TICKET_PARAM</code>
     * <li><code>RequestUtil.PROP_LOGIN_DEFAULT_LANGUAGE</code>
     * <li><code>RequestUtil.PROP_LOGIN_URL_PREFIX</code>
     * <li><code>RequestUtil.FAQ_LINK</code>
     * <li><code>RequestUtil.PROP_FAQ_STATUS</code>
     * <li><code>RequestUtil.PROP_FAQ_OWNER</code>
     * </ul>
     * @see RequestUtil#PROP_COOKIE_SSO
     * @see RequestUtil#PROP_COOKIE_SSO_TTL
     * @see RequestUtil#PROP_COOKIE_DENYSSO
     * @see RequestUtil#PROP_COOKIE_DENYSSO_TTL
     * @see RequestUtil#PROP_COOKIE_LANG
     * @see RequestUtil#PROP_COOKIE_LANG_TTL
     * @see RequestUtil#PROP_COOKIE_ORG
     * @see RequestUtil#PROP_COOKIE_ORG_TTL
     * @see RequestUtil#PROP_LOGIN_TICKET_PARAM
     * @see RequestUtil#PROP_LOGIN_DEFAULT_LANGUAGE
     * @see RequestUtil#PROP_LOGIN_URL_PREFIX
     * @see RequestUtil#PROP_FAQ_STATUS
     */
    private static final String[] REQUIRED_PARAMETERS = {
                                                         RequestUtil.PROP_COOKIE_DENYSSO,
                                                         RequestUtil.PROP_LOGIN_TICKET_PARAM,
                                                         RequestUtil.PROP_COOKIE_SSO,
                                                         RequestUtil.PROP_COOKIE_LANG,
                                                         RequestUtil.PROP_LOGIN_DEFAULT_LANGUAGE,
                                                         RequestUtil.PROP_COOKIE_DENYSSO_TTL,
                                                         RequestUtil.PROP_COOKIE_ORG,
                                                         RequestUtil.PROP_COOKIE_ORG_TTL,
                                                         RequestUtil.PROP_COOKIE_SSO_TTL,
                                                         RequestUtil.PROP_COOKIE_LANG_TTL,
                                                         RequestUtil.PROP_LOGIN_URL_PREFIX,
                                                         RequestUtil.FAQ_LINK,
                                                         RequestUtil.PROP_FAQ_STATUS};


    /**
     * Handles the GET request. The GET request should contain a login ticket as
     * parameter. An SSO ticket can also be presented by the user's web browser
     * (in form of a cookie). The method will try to perform an SSO
     * authentication if the conditions for this is met, else it will present
     * the login page to the user.
     * @param request
     *            The HTTP request object.
     * @param response
     *            The HTTP response object.
     * @throws IOException
     *             Required by interface.
     * @throws ServletException
     *             Required by interface.
     */

    // TODO: Elaborate the JavaDoc, with references to RequestUtil and required
    // parameters.
    public void doGet(final HttpServletRequest request,
                      final HttpServletResponse response)
    throws IOException, ServletException {

        // Get current configuration.
        final Properties config = getConfig();

        // Public computer - should we deny use of SSO?
        final String denySSOChoice = RequestUtil.getCookieValue(config.getProperty(RequestUtil.PROP_COOKIE_DENYSSO), request.getCookies());
        final boolean denySSO;
        if (denySSOChoice == null || denySSOChoice.equals("false") || denySSOChoice.equals("")) {

            // Allow SSO.
            request.setAttribute(RequestUtil.ATTR_SELECTED_DENYSSO, new Boolean(false));
            denySSO = false;

        } else {

            // Deny SSO.
            request.setAttribute(RequestUtil.ATTR_SELECTED_DENYSSO, new Boolean(true));
            denySSO = true;

        }

        // Attempt SSO.
        if (!denySSO) {
            final String serviceTicket;
            try {

                // Get SSO cookie name.
                final String ssoTicketId = RequestUtil.getCookieValue(config.getProperty(RequestUtil.PROP_COOKIE_SSO), request.getCookies());

                if (ssoTicketId != null) {
                    serviceTicket = MoriaController.attemptSingleSignOn(request.getParameter(config.getProperty(RequestUtil.PROP_LOGIN_TICKET_PARAM)), ssoTicketId);

                    // Redirect back to web service.
                    response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
                    response.setHeader("Location", MoriaController.getRedirectURL(serviceTicket));

                    // SSO succeeded, we're finished.
                    return;
                }
            } catch (UnknownTicketException e) {
                // SSO failed; continue with normal authentication.
            } catch (MoriaControllerException e) {
                // Do not handle this exception here. Will be handled by the
                // showLoginPage method.
            } catch (UnknownServicePrincipalException e) {
                // TODO: Resolve handling.
            }
        }

        // Display login page.
        showLoginPage(request, response, null);
    }


    /**
     * Handles the POST request. The POST request indicates that the user is
     * trying to authenticate. If the authentication is successful, the user is
     * redirected back to the originating web service, else the user is
     * presented with an error message.
     * @param request
     *            The HTTP request.
     * @param response
     *            The HTTP response.
     * @throws IOException
     *             Required by interface.
     * @throws ServletException
     *             Required by interface.
     */
    public void doPost(final HttpServletRequest request,
                       final HttpServletResponse response)
    throws IOException, ServletException {

        // Get current configuration.
        final Properties config = getConfig();

        // Get login and SSO ticket ID.
        final String loginTicketId = request.getParameter(config.getProperty(RequestUtil.PROP_LOGIN_TICKET_PARAM));
        final String ssoTicketId = request.getParameter(config.getProperty(RequestUtil.PROP_COOKIE_SSO));

        // Get user's credentials and organization (organization from dropdown).
        final String username = request.getParameter(RequestUtil.PARAM_USERNAME);
        final String password = request.getParameter(RequestUtil.PARAM_PASSWORD);
        String org = request.getParameter(RequestUtil.PARAM_ORG);

        // Did the user choose to deny SSO?
        String denySSOStr = request.getParameter(RequestUtil.PARAM_DENYSSO);
        final boolean denySSO = (denySSOStr != null && denySSOStr.equals("true"));
        if (denySSO)
            denySSOStr = "true";
        else
            denySSOStr = "false";

        // Set cookie to remember user's SSO choice.
        final Cookie denySSOCookie = RequestUtil.createCookie((String) config.get(RequestUtil.PROP_COOKIE_DENYSSO), denySSOStr, new Integer((String) config.get(RequestUtil.PROP_COOKIE_DENYSSO_TTL)).intValue());
        response.addCookie(denySSOCookie);
        request.setAttribute(RequestUtil.ATTR_SELECTED_DENYSSO, new Boolean(denySSO));

        // Parse organization, if set in username, and validate results.
        if (username.indexOf("@") != -1)
            org = username.substring(username.indexOf("@") + 1, username.length());
        if (org == null || org.equals("") || org.equals("null")) {
            showLoginPage(request, response, RequestUtil.ERROR_NO_ORG);
            return;
        } else if (!RequestUtil.parseConfig(getConfig(), RequestUtil.PROP_ORG, (String) config.get(RequestUtil.PROP_LOGIN_DEFAULT_LANGUAGE)).containsValue(org)) {
            showLoginPage(request, response, RequestUtil.ERROR_INVALID_ORG);
            return;
        }

        // Store user's organization selection in cookie.
        final Cookie orgCookie = RequestUtil.createCookie((String) config.get(RequestUtil.PROP_COOKIE_ORG), request.getParameter(RequestUtil.PARAM_ORG), new Integer((String) config.get(RequestUtil.PROP_COOKIE_ORG_TTL)).intValue());
        response.addCookie(orgCookie);

        /* Attempt login */
        final Map tickets;
        final String redirectURL;
        try {
            tickets = MoriaController.attemptLogin(loginTicketId, ssoTicketId, username + "@" + org, password, denySSO);
            redirectURL = MoriaController.getRedirectURL((String) tickets.get(MoriaController.SERVICE_TICKET));
        } catch (AuthenticationException e) {
            showLoginPage(request, response, RequestUtil.ERROR_AUTHENTICATION_FAILED);
            return;
        } catch (UnknownTicketException e) {
            showLoginPage(request, response, RequestUtil.ERROR_UNKNOWN_TICKET);
            return;
        } catch (DirectoryUnavailableException e) {
            showLoginPage(request, response, RequestUtil.ERROR_DIRECTORY_DOWN);
            return;
        } catch (InoperableStateException e) {
            showLoginPage(request, response, RequestUtil.ERROR_MORIA_DOWN);
            return;
        } catch (IllegalInputException e) {
            showLoginPage(request, response, RequestUtil.ERROR_NO_CREDENTIALS);
            return;
        } catch (AuthorizationException e) {
            showLoginPage(request, response, RequestUtil.ERROR_AUTHORIZATION_FAILED);
            return;
        }

        // Authentication is now complete.

        // If we didn't disallow SSO, then store the SSO ticket in a cookie.
        if (!denySSO) {
            final Cookie ssoTicketCookie = RequestUtil.createCookie((String) config.get(RequestUtil.PROP_COOKIE_SSO), (String) tickets.get(MoriaController.SSO_TICKET), new Integer((String) config.get(RequestUtil.PROP_COOKIE_SSO_TTL)).intValue());
            response.addCookie(ssoTicketCookie);
        }

        // Redirect back to the web service.
        response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
        response.setHeader("Location", redirectURL);
    }


    /**
     * Displays the login page. The method fills the request object with values
     * and then passes the request to the JSP.
     * @param request
     *            The HTTP request.
     * @param response
     *            The HTTP response.
     * @param errorType
     *            The type of the error set by the caller.
     * @throws IOException
     *             When the JSP throws this exception.
     * @throws ServletException
     *             When the JSP throws this exception.
     */
    // TODO: Elaborate JavaDoc for this method.
    private void showLoginPage(final HttpServletRequest request,
                               final HttpServletResponse response,
                               String errorType)
    throws IOException, ServletException {
        
        /*
         * Preparations requiring module configuration only:
         */ 

        // Get configuration.
        final Properties config = getConfig();
        
        // Get the login ticket.
        final String loginTicket = request.getParameter(config.getProperty(RequestUtil.PROP_LOGIN_TICKET_PARAM));
        
        // Set the base authentication page URL.
        request.setAttribute(RequestUtil.ATTR_BASE_URL, config.getProperty(RequestUtil.PROP_LOGIN_URL_PREFIX) + "?" + config.getProperty(RequestUtil.PROP_LOGIN_TICKET_PARAM) + "=" + loginTicket);
        
        // Set link to the FAQ.
        request.setAttribute("faqlink", config.get(RequestUtil.FAQ_LINK));
        
        // Set the user's language cookie, if language was changed.
        if (request.getParameter(RequestUtil.PARAM_LANG) != null)
            response.addCookie(RequestUtil.createCookie((String) config.get(RequestUtil.PROP_COOKIE_LANG), request.getParameter(RequestUtil.PARAM_LANG), new Integer((String) config.get(RequestUtil.PROP_COOKIE_LANG_TTL)).intValue()));

        /*
         * Preparations also requiring service properties. 
         */
        
        // Get service properties and set security level.
        HashMap serviceProperties = null;
        try {

            serviceProperties = MoriaController.getServiceProperties(loginTicket);
            request.setAttribute(RequestUtil.ATTR_SEC_LEVEL, "" + MoriaController.getSecLevel(loginTicket));

            // TODO: Move error handling into doGet instead, where
            // MoriaControllerException is ignored.
        } catch (UnknownTicketException e) {
            errorType = RequestUtil.ERROR_UNKNOWN_TICKET;
        } catch (IllegalInputException e) {
            errorType = RequestUtil.ERROR_UNKNOWN_TICKET;
        } catch (InoperableStateException e) {
            errorType = RequestUtil.ERROR_MORIA_DOWN;
        }

        // Set service display name and URL.
        if (serviceProperties != null) {
            request.setAttribute(RequestUtil.ATTR_CLIENT_NAME, serviceProperties.get(RequestUtil.CONFIG_DISPLAY_NAME));
            request.setAttribute(RequestUtil.ATTR_CLIENT_URL, serviceProperties.get(RequestUtil.CONFIG_URL));
        }
        
        /*
         * Preparations also requiring user cookies.
         */
        
        // Get user cookies.
        final Cookie[] userCookies = request.getCookies();

        // Can we get page language from user's cookie?
        String cookieLanguage = null;
        if (userCookies != null)
            cookieLanguage = RequestUtil.getCookieValue((String) config.get(RequestUtil.PROP_COOKIE_LANG), userCookies);

        // Can we get page language from service defaults?
        String serviceLanguage = null;
        if (serviceProperties != null)
            serviceLanguage = (String) serviceProperties.get(RequestUtil.CONFIG_LANG);

        // Set page language.
        final ResourceBundle bundle = RequestUtil.getBundle(RequestUtil.BUNDLE_LOGIN, request.getParameter(RequestUtil.PARAM_LANG), cookieLanguage, serviceLanguage, request.getHeader("Accept-Language"), (String) config.get(RequestUtil.PROP_LOGIN_DEFAULT_LANGUAGE));
        request.setAttribute(RequestUtil.ATTR_BUNDLE, bundle);
        request.setAttribute(RequestUtil.ATTR_LANGUAGES, RequestUtil.parseConfig(getConfig(), RequestUtil.PROP_LANGUAGE, RequestUtil.PROP_COMMON));
        request.setAttribute(RequestUtil.ATTR_SELECTED_LANG, bundle.getLocale());

        /*
         * Preparations also requiring service principal. 
         */
        
        // Can we get the service principal?
        String servicePrincipal = null;
        if (serviceProperties != null)
            servicePrincipal = (String) serviceProperties.get(RequestUtil.CONFIG_SERVICE_PRINCIPAL);

        // Resolve list of allowed organizations for this service principal.
        TreeMap allowedOrganizations = null;
        if (servicePrincipal != null) {
            
            // Remove illegal organizations for this (known) service.
            allowedOrganizations = RequestUtil.parseConfig(getConfig(), RequestUtil.PROP_ORG, bundle.getLocale().getLanguage());
            Iterator i = allowedOrganizations.values().iterator();
            while (i.hasNext()) {
                try {
                    if (!MoriaController.isOrganizationAllowedForService(servicePrincipal, (String) i.next()))
                        i.remove();
                } catch (UnknownServicePrincipalException e) {
                    // This is ignored; an unknown service principal should
                    // never even reach this stage.
                    log.logWarn("Unexpected service principal: " + servicePrincipal);
                }
            }
        }
        request.setAttribute(RequestUtil.ATTR_ORGANIZATIONS, allowedOrganizations);
        
        // Get default organization from service configuration, if possible.
        String defaultOrganization = null;
        if (serviceProperties != null)
            defaultOrganization = (String) serviceProperties.get(RequestUtil.CONFIG_HOME);
        
        // Override with default organization from URL parameter.
        String value = request.getParameter(RequestUtil.PARAM_ORG);
        if (value != null)
            defaultOrganization = value;
        
        // Override with default organization from user cookie.
        value = RequestUtil.getCookieValue((String) config.get(RequestUtil.PROP_COOKIE_ORG), userCookies); 
        if (value != null)
            defaultOrganization = value;
        
        // Set default organization.
        request.setAttribute(RequestUtil.ATTR_SELECTED_ORG, defaultOrganization);

        /*
         * Finish preparations and show page.
         */
        
        // Set error message, if any.
        request.setAttribute(RequestUtil.ATTR_ERROR_TYPE, errorType);

        // Process JSP.
        final RequestDispatcher rd = getServletContext().getNamedDispatcher("Login.JSP");
        rd.forward(request, response);
    }


    /**
     * Gets the current configuration from the context. The configuration is
     * expected to be set by the controller before requests are sent to this
     * servlet.
     * @return The current configuration, as read from the servlet context.
     * @throws IllegalStateException
     *             If the configuration has not been set, or if any required
     *             configuration parameters are missing.
     * @see #REQUIRED_PARAMETERS
     */
    private Properties getConfig() {

        /* Read configuration from context. */
        final Properties config;
        try {
            config = (Properties) getServletContext().getAttribute(RequestUtil.PROP_CONFIG);
        } catch (ClassCastException e) {
            log.logCritical("Config is not correctly set in context");
            throw new IllegalStateException();
        }

        // Has the configuration been set at all?
        if (config == null) {
            log.logCritical("Configuration is not set in context");
            throw new IllegalStateException();
        }

        // Are we missing some required properties?
        for (int i = 0; i < REQUIRED_PARAMETERS.length; i++) {
            String parvalue = config.getProperty(REQUIRED_PARAMETERS[i]);
            if ((parvalue == null) || (parvalue.equals(""))) {
                log.logCritical("Required parameter '" + REQUIRED_PARAMETERS[i] + "' is not set");
                throw new IllegalStateException();
            }
        }
        return config;
    }
}
