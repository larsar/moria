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

import no.feide.moria.controller.MoriaController;
import no.feide.moria.store.InvalidTicketException;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Properties;
import java.util.ResourceBundle;

//import no.feide.moria.controller.MoriaController;

/**
 * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
 * @version $Revision$
 */
public class LoginServlet extends HttpServlet {


    /**
     * Handles the GET requests.
     *
     * @param request  the HTTP request object
     * @param response the HTTP response object
     * @throws IOException
     * @throws ServletException
     */
    public final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException, ServletException {
        // TODO: Do not throw exceptions, set INTERNAL SERVER ERRROR status
        String jspLocation = getServletContext().getInitParameter("jsp.location");
        Properties config = getConfig();

        /* Ticket */
        String loginTicket = request.getParameter(config.getProperty("loginTicketID"));

        /* Service properties */
        HashMap serviceProperties;
        try {
            serviceProperties = MoriaController.getServiceProperties(loginTicket);
        } catch (InvalidTicketException e) {
            throw new ServletException(e);
            // TODO: Should catch InvalidTicketException/ControllerException and display error
        }

        /* Resource bundle */
        ResourceBundle bundle = RequestUtil.getBundle("login", request.getParameter("lang"), request.getCookies(),
                (String) serviceProperties.get("lang"), request.getHeader("Accept-Language"), "en");
        request.setAttribute("bundle", bundle);

        /* Configured values */
        request.setAttribute("organizationNames", RequestUtil.parseConfig(getConfig(), "org", bundle.getLocale().getLanguage()));
        request.setAttribute("languages", RequestUtil.parseConfig(getConfig(), "lang", "common"));

        /* Selected realm */
        String selectedRealm = RequestUtil.getCookieValue("realm", request.getCookies());
        if (selectedRealm == null || selectedRealm.equals("")) {
            selectedRealm = (String) serviceProperties.get("home");
        }
        request.setAttribute("selectedRealm", selectedRealm);

        /* Selected language */
        request.setAttribute("selectedLang", bundle.getLocale());

        /* Service attributes */
        request.setAttribute("clientName", (String) serviceProperties.get("displayName"));
        request.setAttribute("clientURL", (String) serviceProperties.get("url"));

        /* Seclevel */
        // TODO: Set based on requested attributes
        request.setAttribute("secLevel", "low");


        // TODO: Include instead of forward
        /* Process jsp */
        RequestDispatcher rd = getServletContext().getRequestDispatcher(jspLocation + "/login.jsp");
        rd.include(request, response);
    }


    private Properties getConfig() {
        Properties config;

        /* Validate config */
        try {
            ServletContext context = getServletContext();
            config = (Properties) getServletContext().getAttribute("config");
        } catch (ClassCastException e) {
            // TODO: Log
            throw new IllegalStateException("Config is not correctly set in context.");
        }
        if (config == null) {
            // TODO: Log
            throw new IllegalStateException("Config is not set in context.");
        }

        return config;
    }
}
