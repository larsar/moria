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

import java.util.Properties;
import java.util.ResourceBundle;

import javax.servlet.RequestDispatcher;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.feide.moria.controller.IllegalInputException;
import no.feide.moria.controller.InoperableStateException;
import no.feide.moria.controller.MoriaController;
import no.feide.moria.log.MessageLogger;

/**
 * This servlet handles logout request. It will invalidate the SSO ticket
 * in the underlying store and remove the cookie form the client.
 * <p>
 * It uses two properties from the config:
 * <dl>
 *  <dt>no.feide.moria.web.sso_cookie.name</dt>
 *  <dd>The the cookie name</dd>
 *  <dt>no.feide.moria.web.logout.url_param</dt>
 *  <dd>The name of the optional parameter in the request holding the
 *      redirect url.</dd>
 * </dl>
 *
 * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
 * @version $Revision$
 */
public final class LogoutServlet extends HttpServlet {

    /** The logger used in this class. */
    private MessageLogger messageLogger;

    /** Constant holding the property name of the SSO cookie. */
    private static final String SSO_COOKIE_PROPERTY_NAME = "no.feide.moria.web.sso_cookie.name";

    /** Constant holding the property name of the url parameter. */
    private static final String URL_PARAM_PROPERTY_NAME = "no.feide.moria.web.logout.url_param";

    /**
     * Intitiates the servlet.
     */
    public void init() {
        messageLogger = new MessageLogger(LogoutServlet.class);
    }

    /**
     * Handles the GET requests.
     *
     * @param request
     *          the HTTP request object
     * @param response
     *          the HTTP response object
     */
    public void doGet(final HttpServletRequest request, final HttpServletResponse response) {

        Properties config = RequestUtil.getConfig(getServletContext());

        String ssoCookieName = config.getProperty(SSO_COOKIE_PROPERTY_NAME);

        if (ssoCookieName == null || ssoCookieName.equals("")) {
            ssoCookieName = "MoriaSSOCookie";
            messageLogger.logWarn("Parameter: " + SSO_COOKIE_PROPERTY_NAME + " not set in config. Using default value: "
                    + ssoCookieName);
        }

        Cookie[] cookies = request.getCookies();
        String cookieValue = null;

        if (cookies != null) {
            cookieValue = RequestUtil.getCookieValue(ssoCookieName, cookies);
        } else {
            showPage(request, response);
        }

        if (cookieValue == null) {
            showPage(request, response);
        }

        /* Invalidate ticket. */
        boolean controllerFailed = false;

        try {
            MoriaController.invalidateSSOTicket(cookieValue);
        } catch (IllegalInputException iie) {
            messageLogger.logWarn("Bad input given.", cookieValue, iie);
            controllerFailed = true;
        } catch (InoperableStateException ise) {
            messageLogger.logWarn("Controller in inoperable state.", cookieValue, ise);
            controllerFailed = true;
        }

        if (controllerFailed) {
            RequestDispatcher requestDispatcher = getServletContext().getNamedDispatcher("JSP-Error.JSP");

            try {
                requestDispatcher.forward(request, response);
            } catch (Exception e) {
                messageLogger.logCritical("Dispatch to JSP-Error.JSP failed", cookieValue, e);
            }
            /* If everything fails there's not much to do but return. */
            return;
        }

        /* Remove cookie if set. */
        if (cookieValue != null) {
            Cookie ssoCookie = RequestUtil.createCookie(config.getProperty(SSO_COOKIE_PROPERTY_NAME), cookieValue, 0);
            response.addCookie(ssoCookie);
        }

        /* If redirect url is given in the request; redirect. Else
         * display default response page.
         */
        String urlParam = config.getProperty(URL_PARAM_PROPERTY_NAME);

        if (urlParam == null) {
            urlParam = "url";
            messageLogger.logWarn("Parameter: " + URL_PARAM_PROPERTY_NAME + " not set in config. Using default value: " + urlParam);
        }

        String url = request.getParameter(urlParam);

        if (url != null) {
            response.setStatus(HttpServletResponse.SC_MOVED_PERMANENTLY);
            response.addHeader("Location", url);
        } else {
            showPage(request, response);
        }
    }

    /**
     * Handles POST requests.  Just calls doGet().
     *
     * @param request
     *          the HTTP request object
     * @param response
     *          the HTTP response object
     */
    public void doPost(final HttpServletRequest request, final HttpServletResponse response) {
        doGet(request, response);
    }

    /**
     * Dispatches request to JSP.
     *
     * @param request
     *          the HTTP request object
     * @param response
     *          the HTTP response object
     */
    private void showPage(final HttpServletRequest request, final HttpServletResponse response) {
        /* Resource bundle. */
        ResourceBundle bundle = RequestUtil.getBundle("logout", request.getParameter("lang"), request.getCookies(), null,
                request.getHeader("Accept-Language"), "en");
        request.setAttribute("bundle", bundle);

        RequestDispatcher requestDispatcher = getServletContext().getNamedDispatcher("Logout.JSP");

        try {
            requestDispatcher.forward(request, response);
        } catch (Exception e) {
            messageLogger.logCritical("Dispatch to Logout.JSP failed", e);
        }
    }
}
