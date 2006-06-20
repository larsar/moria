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
import java.util.TreeMap;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.net.ssl.HttpsURLConnection;
import java.net.URL;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.MalformedURLException;

import javax.servlet.ServletException;
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
 * @version $Revision$
 */
// TODO: All status messages should be configurable externally (not in bundles).
public class StatusServlet
extends MoriaServlet {

    /** Used for logging. */
    private final MessageLogger log = new MessageLogger(StatusServlet.class);

    /**
     * List of parameters required by <code>StatusServlet</code>. <br>
     * <br>
     * Current required parameters are:
     * <ul>
     * <li><code>RequestUtil.PROP_BACKENDSTATUS_STATUS_XML</code>
     * </ul>
     * @see RequestUtil#PROP_BACKENDSTATUS_STATUS_XML
     */
    private static final String[] REQUIRED_PARAMETERS = {RequestUtil.PROP_BACKENDSTATUS_STATUS_XML, RequestUtil.PROP_COOKIE_LANG};

    /**
     * The error code used to signal a problem with the backend "ping" users.<br>
     * <br>
     * Current value is <code>"20"</code>.
     */
    private static final String ERRORCODE_BACKEND = "20";

    /**
     * The error code used to signal a problem with the SOAP interface.<br>
     * <br>
     * Current value is <code>"30"</code>.
     */
    private static final String ERRORCODE_SOAP = "30";

    /**
     * The error code used to signal a problem with the key servlets/services.<br>
     * <br>
     * Current value is <code>"60"</code>.
     */
    private static final String ERRORCODE_SERVICE = "60";

    /**
     * The error code used to signal a problem with the internal modules.<br>
     * <br>
     * Current value is <code>"70"</code>.
     */
    private static final String ERRORCODE_MODULE = "70";

    /**
     * The error level used to signal a problem with the backend "ping" users.<br>
     * <br>
     * Current value is <code>"warn"</code>.
     */
    private static final String ERRORLEVEL_BACKEND = "warn";

    /**
     * The error level used to signal a problem with the SOAP interface.<br>
     * <br>
     * Current value is <code>"crit"</code>.
     */
    private static final String ERRORLEVEL_SOAP = "crit";

    /**
     * The error level used to signal a problem with the key servlets/services.<br>
     * <br>
     * Current value is <code>"crit"</code>.
     */
    private static final String ERRORLEVEL_SERVICE = "crit";

    /**
     * The error level used to signal a problem with the internal modules.<br>
     * <br>
     * Current value is <code>"crit"</code>.
     */
    private static final String ERRORLEVEL_MODULE = "crit";

    /**
     * Complete status message used to signal that everything is working.<br>
     * <br>
     * Current value is <code>"00 ok"</code>.
     */
    private static final String READY_MESSAGE = "00 ok";

    /**
     * List of names, as reported by the
     * <code>MoriaController.getStatus()</code>.
     * @see MoriaController#getStatus()
     */
    private static final String[] INTERNAL_MODULE_NAMES = {"moria", "init", "am", "dm", "sm"};


    /**
     * @return the required parameters for this servlet.
     */
    public static String[] getRequiredParameters() {

        return REQUIRED_PARAMETERS;
    }

    /**
     * A hash map containing the attributes for a test-user. Each item in the
     * hashmap maps from a user name to an backendStatusUser class instance
     */
    private HashMap backendDataUsers = null;

    /**
     * Monitor for the status xml file.
     */
    private FileMonitor statusFileMonitor = null;

    /**
     * The organization the test user comes from.
     */
    private static final String STATUS_ATTRIBUTE = "eduPersonAffiliation";

    /**
     * The name of the service.
     */
    private static final String STATUS_PRINCIPAL = "status";


    /**
     * Implements a simple xml parser that parses the status.xml file into a
     * HashMap with BackendStatusUser instances.
     * @see BackendStatusHandler
     * @see BackendStatusUser
     */
    // TODO: JavaDoc the @return.
    public final synchronized HashMap getBackendStatusData() {

        if (statusFileMonitor == null || statusFileMonitor.hasChanged()) {
            Properties config = getConfig();
            if (config != null) {
                BackendStatusHandler handler = new BackendStatusHandler();
                SAXParserFactory factory = SAXParserFactory.newInstance();
                try {
                    String filename = (String) config.get(RequestUtil.PROP_BACKENDSTATUS_STATUS_XML);
                    SAXParser saxParser = factory.newSAXParser();
                    saxParser.parse(new File(filename), handler);
                    statusFileMonitor = new FileMonitor(filename);
                } catch (Throwable t) {
                    log.logCritical("Error parsing status configuration file '" + RequestUtil.PROP_BACKENDSTATUS_STATUS_XML + "'", t);
                } finally {
                    backendDataUsers = handler.getAttribs();
                }
            }
        }
        return backendDataUsers;
    }


    /**
     * Checks the configuration status of the Information, Login and Statistics
     * services.
     * @return An empty <code>Vector</code> everything is OK, otherwise one or
     *         more error messages on the form<br>
     *         <code><i>code level</i> moria-web <i>description</i></code><br>
     *         where <code><i>code</i></code> is given by
     *         <code>ERRORCODE_SERVICE</code> and <code><i>level</i></code>
     *         is given by <code>ERRORLEVEL_SERVICE</code>.
     * @see #ERRORCODE_SERVICE
     * @see #ERRORCODE_SERVICE
     */
    private Vector checkServices() {

        // Gettin' ready.
        Vector messages = new Vector();

        // Check the InformationServlet.
        try {
            this.getServletConfig(InformationServlet.getRequiredParameters(), log);
        } catch (IllegalStateException e) {
            messages.add(ERRORCODE_SERVICE + " " + ERRORLEVEL_SERVICE + " moria-web Configuration of InformationServlet failed");
        }

        // Check the LoginServlet.
        try {
            this.getServletConfig(LoginServlet.getRequiredParameters(), log);
        } catch (IllegalStateException e) {
            messages.add(ERRORCODE_SERVICE + " " + ERRORLEVEL_SERVICE + " moria-web Configuration of LoginServlet failed");
        }

        // Check the StatisticsServlet.
        try {
            this.getServletConfig(StatisticsServlet.getRequiredParameters(), log);
        } catch (IllegalStateException e) {
            messages.add(ERRORCODE_SERVICE + " " + ERRORLEVEL_SERVICE + " moria-web Configuration of StatisticsServlet failed");
        }

        // Done.
        return messages;
    }


    /**
     * Checks the modules through <code>MoriaController.getStatus()</code> and
     * creates an appropriate list of status messages.
     * @return An empty <code>Vector</code> for no errors, otherwise a list of
     *         entries on the form<br>
     *         <code><i>code level module description</i></code><br>
     *         where <code><i>code</i></code> is given by
     *         <code>MODULE_ERRORCODE</code>, <code><i>level</i></code> is
     *         given by <code>MODULE_ERRORLEVEL</code> and </code><code><i>module</i></code>
     *         is one of
     *         <ul>
     *         <li><code>moria</code>
     *         <li><code>moria-ctrl</code>
     *         <li><code>moria-am</code>
     *         <li><code>moria-dm</code>
     *         <li><code>moria-sm</code>
     *         </ul>
     *         Note that if a module fails, the <code>moria</code> error code
     *         will also be included, as the entire system will malfunction.
     *         Also note, that the web module is not checked in this method, but
     *         in <code>checkServices()</code> and <code>checkSOAP()</code>.
     * @see #ERRORCODE_MODULE
     * @see #ERRORLEVEL_MODULE
     * @see #checkServices()
     * @see #checkSOAP()
     */
    private Vector checkModules() {

        Map statusMap = MoriaController.getStatus();
        Vector messages = new Vector();

        if (statusMap != null) {

            // Map module states from MoriaController to short and long names
            // expected by surveillance solution.

            Map descriptions = new HashMap();
            descriptions.put("moria", new String[] {"moria", "System not ready"});
            descriptions.put("init", new String[] {"moria-ctrl", "Controller not ready"});
            descriptions.put("am", new String[] {"moria-am", "Authorization Manager not ready"});
            descriptions.put("dm", new String[] {"moria-dm", "Directory Manager not ready"});
            descriptions.put("sm", new String[] {"moria-sm", "Store Manager not ready"});

            // Create status messages for all modules.
            for (int i = 0; i < INTERNAL_MODULE_NAMES.length; i++) {

                // Get module status.
                Object stateObject = statusMap.get(INTERNAL_MODULE_NAMES[i]);
                boolean ready = false;
                if (stateObject instanceof Boolean)
                    ready = ((Boolean) stateObject).booleanValue();
                if (!ready) {

                    // Add status error message for this module.
                    final String[] description = (String[]) descriptions.get(INTERNAL_MODULE_NAMES[i]);
                    final String message = ERRORCODE_MODULE + " " + ERRORLEVEL_MODULE + " " + description[0] + " " + description[1];
                    messages.add(message);

                }
            }
        }

        // Done.
        return messages;
    }


    /**
     * Checks the SOAP page, by doing a HTTP GET on it using HTTP Basic
     * authentication, as a normal service would do. Note that this test does
     * not actually perform a SOAP operation.
     * @return An empty <code>Vector</code> if no errors, otherwise one or
     *         more messages on the form<br>
     *         <code><i>code level</i> moria-web <i>description</i></code><br>
     *         where <code><i>code</i></code> is given by
     *         <code>ERRORCODE_SOAP</code> and <code><i>level</i></code> is
     *         given by <code>ERRORLEVEL_SOAP</code>.
     * @see #ERRORCODE_SOAP
     * @see #ERRORLEVEL_SOAP
     */
    private Vector checkSOAP() {

        // Try to retrieve the Authentication service's WSDL.
        Vector messages = new Vector();
        try {

            // TODO: Why is this hard-coded?
            URL url = new URL("https://login.feide.no/moria2/v2_1/Authentication?wsdl");
            java.net.Authenticator.setDefault(

            new Authenticator() {

                protected java.net.PasswordAuthentication getPasswordAuthentication() {

                    // TODO: Why is this hard-coded?
                    char[] passwd = {'d', 'e', 'm', 'o', '_', 's', 'e', 'r', 'v', 'i', 'c', 'e'};
                    return new PasswordAuthentication(new String("demo_service"), passwd);
                }
            }

            );

            HttpsURLConnection connection = (HttpsURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.connect();
            int code = connection.getResponseCode();
            if (code != HttpsURLConnection.HTTP_OK) {
                messages.add(ERRORCODE_SOAP + " " + ERRORLEVEL_SOAP + " moria-web WSDL GET caused HTTP error code " + String.valueOf(code));
            }

        } catch (MalformedURLException e) {
            // As the URL is hard-coded(!) above, this will be caused by
            // re-deployment on another URL.
            messages.add(ERRORCODE_SOAP + " " + ERRORLEVEL_SOAP + " moria-web Illegal WSDL GET URL");
        } catch (IOException e) {
            // Possible general problem with SOAP interface.
            messages.add(ERRORCODE_SOAP + " " + ERRORLEVEL_SOAP + " moria-web Unable to connect to WSDL URL");
        }

        // Done.
        return messages;
    }


    /**
     * Check each of the backend "ping" users, by performing a normal (that is,
     * directly through the <code>MoriaController</code> rather than through
     * the SOAP interface) authentication with each.
     * @return An empty <code>Vector</code> if everything worked, otherwise a
     *         list of error messages on the form<br>
     *         <code><i>code level</i> moria-dm <i>description</i></code><br>
     *         where <code><i>code</i></code> is given by
     *         <code>ERRORCODE_BACKEND</code> and <code><i>level</i></code>
     *         is given by <code>ERRORLEVEL_BACKEND</code>.
     * @see #ERRORCODE_BACKEND
     * @see #ERRORLEVEL_BACKEND
     */
    public Vector checkBackend() {

        // Gettin' started.
        Vector messages = new Vector();

        // Check each configured backend "ping" user.
        String key, org;
        for (Iterator iterator = backendDataUsers.keySet().iterator(); iterator.hasNext();) {
            key = (String) iterator.next();
            BackendStatusUser userData = (BackendStatusUser) backendDataUsers.get(key);
            org = userData.getOrganization();
            try {

                // Ignoring all returned attributes.
                MoriaController.directNonInteractiveAuthentication(new String[] {STATUS_ATTRIBUTE}, userData.getName(), userData.getPassword(), STATUS_PRINCIPAL);

            } catch (Exception e) {
                messages.add(ERRORCODE_BACKEND + " " + ERRORLEVEL_BACKEND + " moria-dm No connectivity to " + org);
            }
        }
        return messages;
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
    public final void doGet(final HttpServletRequest request,
                            final HttpServletResponse response)
    throws IOException, ServletException {

        getBackendStatusData();

        Properties config = getConfig();
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String docType = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n";

        /* Resource bundle. */
        String language = null;
        String langFromCookie = null;
        if (config != null && request.getCookies() != null) {
            langFromCookie = RequestUtil.getCookieValue((String) config.get(RequestUtil.PROP_COOKIE_LANG), request.getCookies());
        }
        // Update cookie if language has changed
        if (request.getParameter(RequestUtil.PARAM_LANG) != null) {
            language = request.getParameter(RequestUtil.PARAM_LANG);
            response.addCookie(RequestUtil.createCookie((String) config.get(RequestUtil.PROP_COOKIE_LANG), language, new Integer((String) config.get(RequestUtil.PROP_COOKIE_LANG_TTL)).intValue()));
        }

        /* Get bundle, using either cookie or language parameter */
        final ResourceBundle bundle = RequestUtil.getBundle(RequestUtil.BUNDLE_STATUSSERVLET, language, langFromCookie, null, request.getHeader("Accept-Language"), (String) config.get(RequestUtil.PROP_LOGIN_DEFAULT_LANGUAGE));

        // Header
        out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
        out.println(docType + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=" + bundle.getLocale() + ">");
        out.println("<head><link rel=\"icon\" href=\"/favicon.ico\" type=\"image/png\">");
        out.println("<style type=\"text/css\">\n@import url(\"../resource/stil.css\");\n</style>");
        out.println("<link rel=\"author\" href=\"mailto:" + config.get(RequestUtil.RESOURCE_MAIL) + "\">");
        out.println("<title>" + bundle.getString("header_title") + "</title></head><body>");

        // Layout table
        out.println("<table summary=\"Layout-tabell\" class=\"invers\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
        out.println("<tbody><tr valign=\"middle\">");
        out.println("<td class=\"logo\" width=\"76\"><a href=" + config.get(RequestUtil.RESOURCE_LINK) + ">");
        out.println("<img src=\"../resource/logo.gif\" alt=" + config.get(RequestUtil.PROP_FAQ_OWNER) + " border=\"0\" height=\"41\" width=\"76\"></a></td>");
        out.println("<td width=\"0%\"><a class=\"noline\" href=" + config.get(RequestUtil.RESOURCE_LINK) + ">");
        out.println(bundle.getString("header_feide") + "</a></td>");
        out.println("<td width=\"35%\">&nbsp;</td>");

        // Language selection
        TreeMap languages = RequestUtil.parseConfig(config, RequestUtil.PROP_LANGUAGE, RequestUtil.PROP_COMMON);
        Iterator it = languages.keySet().iterator();
        while (it.hasNext()) {
            String longName = (String) it.next();
            String shortName = (String) languages.get(longName);
            if (RequestUtil.ATTR_SELECTED_LANG.equals(shortName)) {
                out.println("[" + longName + "]");
            } else
                out.println("<td align=\"centre\"><small><a class=\"invers\" href =" + config.get(RequestUtil.PROP_FAQ_STATUS) + "?" + RequestUtil.PARAM_LANG + "=" + shortName + ">" + longName + "</a></small></td>");
        }

        // More Layout
        out.println("<td class=\"dekor1\" width=\"100%\">&nbsp;</td></tr></tbody></table>");
        out.println("<div class=\"midt\">");
        out.println("<table cellspacing=\"0\">");
        out.println("<tbody><tr valign=\"top\">");
        out.println("<td class=\"kropp\">");

        // Check status
        Vector allerrors = new Vector();
        allerrors.addAll(checkModules());
        allerrors.addAll(checkServices());
        allerrors.addAll(checkSOAP());
        allerrors.addAll(checkBackend());
        if (allerrors.size() > 0) {
            for (int i = 0; i < allerrors.size(); i++) {
                out.println("<font color=#FFFFFF>" + (String) (allerrors.get(i)) + "</font>" + "<br>");
            }
        } else if (allerrors.size() == 0) {
            out.println("<font color=#FFFFFF>" + READY_MESSAGE + "</font>");
        }

        // Prepare to check test users.
        // TODO: Unneccessary duplication of checkBackend() test above.
        out.println("<p><table border=1><tr><th>" + bundle.getString("table_organization") + "</th><th>" + bundle.getString("table_status") + "</th></tr>");

        // Start checking a new user.
        for (Iterator iterator = backendDataUsers.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            BackendStatusUser userData = (BackendStatusUser) backendDataUsers.get(key);
            out.println("<tr><td>" + userData.getOrganization() + "</td>");
            try {

                // Ignoring the returned attribute values.
                MoriaController.directNonInteractiveAuthentication(new String[] {STATUS_ATTRIBUTE}, userData.getName(), userData.getPassword(), STATUS_PRINCIPAL);

                // This test user worked.
                out.println("<td>OK</td>");

            } catch (AuthenticationException e) {
                log.logWarn("Authentication failed for: " + userData.getName() + ", contact: " + userData.getContact());
                String message = "<a href=mailto:" + userData.getContact() + "?subject=" + userData.getOrganization() + ":%20" + bundle.getString("subject_authentication") + userData.getName() + ">" + bundle.getString("error_help");
                out.println("<td>" + bundle.getString("error_authentication") + message + "</a></td></tr>");
            } catch (DirectoryUnavailableException e) {
                log.logWarn("The directory is unavailable for: " + userData.getName() + ", contact: " + userData.getContact());
                String message = "<a href=mailto:" + userData.getContact() + "?subject=" + userData.getOrganization() + ":%20" + bundle.getString("subject_directory") + userData.getName() + ">" + bundle.getString("error_help");
                out.println("<td>" + bundle.getString("error_directory") + message + "</a></td></tr>");
            } catch (AuthorizationException e) {
                log.logWarn("Authorization failed for: " + userData.getName() + ", contact: " + userData.getContact());
                String message = "<a href=mailto:" + userData.getContact() + "?subject=" + userData.getOrganization() + ":%20" + bundle.getString("subject_authorization") + userData.getName() + ">" + bundle.getString("error_help");
                out.println("<td>" + bundle.getString("error_authorization") + message + "</a></td></tr>");
            } catch (IllegalInputException e) {
                log.logWarn("Illegal input for: " + userData.getName() + ", contact: " + userData.getContact());
                String message = "<a href=mailto:" + userData.getContact() + "?subject=" + userData.getOrganization() + ":%20" + bundle.getString("subject_illegal") + userData.getName() + ">" + bundle.getString("error_help");
                out.println("<td>" + bundle.getString("error_illegal") + message + "</a></td></tr>");
            } catch (InoperableStateException e) {
                log.logWarn("Inoperable state for: " + userData.getName() + ", contact: " + userData.getContact());
                // Only print moria-support adress if moria is inoperable
                if (userData.getOrganization().equals("Uninett")) {
                    String message = "<a href=mailto:" + userData.getContact() + "?subject=" + userData.getOrganization() + ":%20" + bundle.getString("subject_inoperable") + userData.getName() + ">" + bundle.getString("error_help");
                    out.println("<td>" + bundle.getString("error_inoperable") + message + "</a></td></tr>");
                } else {
                    out.println("<td>" + bundle.getString("error_inoperable"));
                }
            } finally {

                // Finish the table row.
                out.println("</tr>");

            }
        }

        // Done with all test users.
        out.println("</table></p>");

        // Layout
        out.println("</tr>");
        out.println("</table>");
        out.println("</tbody>");
        out.println("</div>");

        out.println("<p>");
        out.println("<table summary=\"Layout-tabell\" class=\"invers\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
        out.println("<tbody><tr class=\"bunn\" valign=\"middle\">");
        out.println("<td class=\"invers\" align=\"left\"><small><a class=\"invers\" href=\"mailto:" + config.get(RequestUtil.RESOURCE_MAIL) + "\">" + config.get(RequestUtil.RESOURCE_MAIL) + "</a></small></td>");
        out.println("<td class=\"invers\" align=\"right\"><small>" + config.get(RequestUtil.RESOURCE_DATE) + "</small></td>");
        out.println("</tr></tbody></table></p>");

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

        try {
            return getServletConfig(getRequiredParameters(), log);
        } catch (IllegalStateException e) {
            return null;
        }
    }

}
