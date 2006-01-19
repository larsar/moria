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
import java.util.Calendar;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.util.MissingResourceException;

import no.feide.moria.log.MessageLogger;
import no.feide.moria.controller.MoriaController;

/**
 * The StatisticsServlet shows the usage of Moria.
 * @version $Revision$
 */
public class StatisticsServlet
extends MoriaServlet {

    /** Used for logging. */
    private final MessageLogger log = new MessageLogger(StatusServlet.class);

    /**
     * List of parameters required by <code>StatusServlet</code>.
     * <br>
     * <br>
     * Current required parameters are:
     * <ul>
     * <li><code>RequestUtil.PROP_BACKENDSTATUS_STATISTICS_XML</code>
     * <li><code>RequestUtil.PROP_BACKENDSTATUS_STATISTICS2_XML</code>
     * <li><code>RequestUtil.PROP_BACKENDSTATUS_IGNORE</code>
     * <li><code>RequestUtil.PROP_COOKIE_LANG_XML</code>
     * </ul>
     * @see RequestUtil#PROP_BACKENDSTATUS_STATISTICS_XML
     */
    private static final String[] REQUIRED_PARAMETERS = {
            RequestUtil.PROP_BACKENDSTATUS_STATISTICS_XML,
            RequestUtil.PROP_BACKENDSTATUS_STATISTICS2_XML,
            RequestUtil.PROP_BACKENDSTATUS_STATISTICS2_BASENAME_XML,
            RequestUtil.PROP_BACKENDSTATUS_IGNORE,
            RequestUtil.PROP_COOKIE_LANG };

    /**
     * 
     * @return the required parameters for this servlet.
     */
    public static String[] getRequiredParameters() {
        return REQUIRED_PARAMETERS;
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
                
        response.setContentType("text/html");
        PrintWriter out = response.getWriter();
        String docType = "<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.01 Transitional//EN\" \"http://www.w3.org/TR/html4/loose.dtd\">\n";
        
        String year = request.getParameter("year");
        if (year != null && year != "") {
            try {
                int number = Integer.parseInt(year);
                if (number < 2005) year = null;
            }
            catch (NumberFormatException e) {
                year = null;
            }
        }
        if (year == null || year.equals("")) {
            int yearnumber = Calendar.getInstance().get(Calendar.YEAR);
            Integer tmp = new Integer(yearnumber);
            year = tmp.toString();
        }       
        /* Resource bundle. */
        String language = null;
        String langFromCookie = null;
        Properties config = getConfig();
        if (config != null && request.getCookies() != null) {
           langFromCookie = RequestUtil.getCookieValue((String) config.get(RequestUtil.PROP_COOKIE_LANG), request.getCookies());
        }
        // Update cookie if language has changed
        if (request.getParameter(RequestUtil.PARAM_LANG) != null) {
            language = request.getParameter(RequestUtil.PARAM_LANG);
            response.addCookie(RequestUtil.createCookie(
                    (String) config.get(RequestUtil.PROP_COOKIE_LANG),
                    language,
                    new Integer((String) config.get(RequestUtil.PROP_COOKIE_LANG_TTL)).intValue()));
        }

        /* Get bundle, using either cookie or language parameter */
       final ResourceBundle bundle = RequestUtil.getBundle(
                RequestUtil.BUNDLE_STATISTICSSERVLET,
                language, langFromCookie, null,
                request.getHeader("Accept-Language"), 
                (String) config.get(RequestUtil.PROP_LOGIN_DEFAULT_LANGUAGE));
        
        // Header
        out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
        out.println(docType + "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=" + bundle.getLocale() + ">"); 
        out.println("<head><link rel=\"icon\" href=\"/favicon.ico\" type=\"image/png\">"); 
        out.println("<style type=\"text/css\">\n@import url(\"../resource/stil.css\");\n</style>");
        out.println("<link rel=\"author\" href=\"mailto:" + config.get(RequestUtil.RESOURCE_MAIL)+ "\">");
        out.println("<title>" + bundle.getString("header_title") + "</title></head><body>");
        
        //Layout table
        out.println("<table summary=\"Layout-tabell\" class=\"invers\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
        out.println("<tbody><tr valign=\"middle\">");
        out.println("<td class=\"logo\" width=\"76\"><a href=" + config.get(RequestUtil.RESOURCE_LINK) + ">");
        out.println("<img src=\"../resource/logo.gif\" alt=" + config.get(RequestUtil.PROP_FAQ_OWNER) + " border=\"0\" height=\"41\" width=\"76\"></a></td>");
        out.println("<td width=\"0%\"><a class=\"noline\" href=" + config.get(RequestUtil.RESOURCE_LINK) + ">");
        out.println(bundle.getString("header_feide") + "</a></td>");
        out.println("<td width=\"35%\">&nbsp;</td>");
        
        	// Language selection
            TreeMap languages = (TreeMap)RequestUtil.parseConfig(config, RequestUtil.PROP_LANGUAGE, RequestUtil.PROP_COMMON);
            Iterator it = languages.keySet().iterator();
            while(it.hasNext()) {
                String longName = (String) it.next();
                String shortName  = (String) languages.get(longName);
                if (RequestUtil.ATTR_SELECTED_LANG.equals(shortName)) {
                    out.println("[" + longName + "]");
                } else
                    out.println("<td align=\"centre\"><small><a class=\"invers\" href ="
                            + config.get(RequestUtil.PROP_STATISTICS_URL) + "?" + RequestUtil.PARAM_LANG + "=" + shortName + ">" + longName
                            + "</a></small></td>");
                }
         
        //More Layout
        out.println("<td class=\"dekor1\" width=\"100%\">&nbsp;</td></tr></tbody></table>");
        out.println("<div class=\"midt\">");
        out.println("<table cellspacing=\"0\">");
        out.println("<tbody><tr valign=\"top\">");
        out.println("<td class=\"kropp\">");
        
        //Print info
        if (year.equals("2005")) {
            
            // Link to new statistics
            out.println("<a href=" + (String)config.get(RequestUtil.PROP_STATISTICS_URL) + ">" + bundle.getString("stat_2006") + "</a>");
            this.printOldStatistics(out, bundle);
                
            //Statistics from moria2
            out.println("<br><h3>" + bundle.getString("stat_info2") + "</h3>");
            out.println(bundle.getString("stat_info3"));
            String filename2 = (String) config.get(RequestUtil.PROP_BACKENDSTATUS_STATISTICS2_XML);
            this.printStatistics(out, bundle, filename2);
        }
        else {
            // Link to statistics for 2005
            out.println("<a href=" + (String)config.get(RequestUtil.PROP_STATISTICS_URL) + "?year=2005" + ">" + bundle.getString("stat_2005") + "</a>" );
            out.println("<br><h3>" + bundle.getString("stat_info") + "</h3>");
            out.println(bundle.getString("stat_info3"));
            String filename2 = (String) config.get(RequestUtil.PROP_BACKENDSTATUS_STATISTICS2_BASENAME_XML);
            filename2 += "_" + year + ".xml";
            this.printStatistics(out, bundle, filename2);
        }
        //Layout
        out.println("</tr>");
        out.println("</table>");
        out.println("</tbody>");
        out.println("</div>");

        out.println("<p>");
        out.println("<table summary=\"Layout-tabell\" class=\"invers\" border=\"0\" cellpadding=\"0\" cellspacing=\"0\" width=\"100%\">");
        out.println("<tbody><tr class=\"bunn\" valign=\"middle\">");
        out.println("<td class=\"invers\" align=\"left\"><small><a class=\"invers\" href=\"mailto:"
                + config.get(RequestUtil.RESOURCE_MAIL) + "\">" + config.get(RequestUtil.RESOURCE_MAIL) + "</a></small></td>");
        out.println("<td class=\"invers\" align=\"right\"><small>" + config.get(RequestUtil.RESOURCE_DATE) + "</small></td>");
        out.println("</tr></tbody></table></p>");
                        
        // Finish up.
        out.println("</body></html>");
        

    }
    
    private void printOldStatistics(PrintWriter out, ResourceBundle bundle) {
        Properties config = getConfig();
        
        // Print statistics    
        out.println("<br><h3>" + bundle.getString("stat_info") + "</h3>");
        
        //Statistics from moria1
        String filename = (String) config.get(RequestUtil.PROP_BACKENDSTATUS_STATISTICS_XML);
        this.printStatistics(out, bundle, filename);
        
        out.println("<br><i>" + bundle.getString("vortex_number") + "</i>");
    }
    
    private void printStatistics(PrintWriter out, ResourceBundle bundle, String filename) {
        Properties config = getConfig();
        if (config != null) {
            StatisticsHandler handler = new StatisticsHandler();
            
            // Read ignore-list from config file
            String ignorestring = (String)config.get(RequestUtil.PROP_BACKENDSTATUS_IGNORE);
            String[] ignore = ignorestring.split(",");
            for (int i=0; i<ignore.length; i++) {
                handler.addIgnoreService(ignore[i]);
            }
            SAXParserFactory factory = SAXParserFactory.newInstance();
            try {
                SAXParser saxParser = factory.newSAXParser();
                saxParser.parse(new File(filename), handler);
                
                int numstats = handler.getNumStatisticsCollections();
                if (numstats == 1) numstats = 0;
                for (int s = -1; s < numstats; s++) {                 
                    StatisticsCollection stat = null;
                    if (s < 0) {
                        stat = handler.getAccumStatistics();
                        out.print("<p>");
                    }
                    else {
                        stat = handler.getStatisticsCollection(s);
                        String name = stat.getOrgName();
                        try {
                            out.println("<p><b>" + bundle.getString(name) + "</b>");
                        } catch (MissingResourceException m) {
                            out.println("<p><b>" + name + "</b>");
                        }
                    } 
                    final int nummonths = stat.getNumMonths();
                    if (nummonths > 0) {
                        // Make table
                        out.println("<table border=1><tr><th>" + bundle.getString("stat_services") + "</th>");
                        for (int i = 0; i < nummonths; i++) {
                            out.print("<th>");
                            out.print(bundle.getString(stat.getMonthName(i)));
                            out.print("</th>");
                        }
                        out.print("<th>" + bundle.getString("stat_sum") + "</th>");
                        for (int j = 0; j < stat.getNumStatisticsData(); j++) {
                            StatisticsData data = stat.getStatisticsData(j);
                            out.print("<tr>");
                            String name = data.getName();
                            // Print the translated name for a service if it exists
                            try {
                                out.print("<td>" + bundle.getString(name) + "</td>");
                            } catch (MissingResourceException m) {
                                out.print("<td>" + name + "</td>");
                            } 
                            // Print numbers and summarize
                            int sum = 0;
                            for (int i = 0; i < nummonths; i++) {
                                sum += data.getCount(stat.getMonthName (i));
                                out.print("<td align=right>");
                                out.print(Integer.toString(data.getCount(stat.getMonthName(i))));
                                out.print("</td>");
                            }
                            out.print("<td align=right>" + Integer.toString(sum) + "</td>");
                            out.print("</tr>");
                        }
                        
                        
                        out.println("</table>");
                    }
                }
            }
            catch (Throwable t) {
                out.println("Error while parsing xml");
                out.println(t.getMessage());
            }
        }
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
        return getServletConfig(getRequiredParameters(), log);
    }
    

}

