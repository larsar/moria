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
 */

package no.feide.moria.servlet;

import java.io.IOException;
// import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Arrays;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

import no.feide.moria.controller.AuthorizationException;
import no.feide.moria.controller.IllegalInputException;
import no.feide.moria.controller.InoperableStateException;
import no.feide.moria.controller.MoriaController;
import no.feide.moria.controller.UnknownTicketException;
import no.feide.moria.log.MessageLogger;

//import no.feide.moria.controller.MoriaController;

//import no.feide.moria.authorization.UnknownServicePrincipalException;

//import no.feide.moria.servlet.RequestUtil;

//import no.feide.moria.controller.MoriaController;
//import no.feide.moria.servlet.RequestUtil;

import java.util.Vector;


/**
 * This servlet is responsible for retrieving information about a user, and send
 * it to information.jsp for display.
 * 

 * Required properties:
 * TODO
 * 
 * @author Eva Indal
 * @version %I%
 */
public class InformationServlet extends HttpServlet {
    
    /** A hash map containing all possible attributes for a user.
     Each item in the hashmap maps from a attribute name to a
     AttribsData class instance */
    private HashMap feideattribs;
    
    /** Principal name of the sercive.
     *  Current value is "info"
     */ 
    private String PRINCIPAL = "info";
    
    /** Map used for storing userattributes, so language can be changed */
    private Map storedmap = null;
    
    /** Stored ticket for languages */
    private String storedticket = null;
    
    /** Used for logging. */
    private final MessageLogger log = new MessageLogger(InformationServlet.class);

    /**
     * Constructor.
     * Get info about user attributes 
     */
    public InformationServlet() {
        // TODO: xml file should really be reread when file is changed
        feideattribs = getAttribs();
    }

    
    /**
     * Implements a simple xml parser that parses the feideattribs.xml file
     * into a HashMap with AttribsData instances.
     * 
     * @return the content of the xml file
     * @see AttribsHandler
     *      AttribsData
     */
    public HashMap getAttribs() {
        AttribsHandler handler = new AttribsHandler();
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse( new File(RequestUtil.PATH_FEIDEATTRIBS), handler);
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return handler.getAttribs();
    }
        
    /**
     * Generate table for a user. The vector consists of rows of data. Each row
     * has four columns. The first column is the URL link for user attribute. The
     * second column is the attribute description as presented to the user. The 
     * third row is the actual data stored for the attribute, and the fourth
     * columns is either fd_mandatory or fd_optional, as a key for a mandatory
     * or optional attribute.  
     * 
     * @param userData the user data
     * @param bundle resourcebundle for language
     * @return Vector with table data
     */
    private Vector printTableToVector(Map userData, ResourceBundle bundle) {
        
        /* Stores the data in a temporary vector */
        Vector temp = new Vector();
        for (Iterator iterator = feideattribs.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            AttribsData attribsData = (AttribsData) feideattribs.get(key);
            temp.add(attribsData);
        }
        /* Sorts the description fields according to the xml file */
        Object[] attribsArray = temp.toArray();
        Arrays.sort(attribsArray, new AttribsData(0));

        Vector out = new Vector();

        /* Fetch the data to print */
        int n = temp.size();
        for (int i = 0; i < n; i++) {
            AttribsData adata = (AttribsData) attribsArray[i];
            String key2 = (String) adata.getData("key");
            boolean hasuserdata = userData.containsKey(key2);
            // Vector userdata = (Vector) userData.get(key2);
            String[] userdata = (String[]) userData.get(key2);

            String description = adata.getData("description");
            String bundleid = adata.getData("resourcebundle");
            String bundlename = (String) bundle.getString(bundleid);
            /* see if resourcebundle has a name */
            if (bundlename != null)
                description = bundlename;
            String link = adata.getData("link");
            String relevance = adata.getData("relevance");

            String userstring = "";
            String relevance_string = null;

            if (userdata != null) {
                for (int j = 0; j < userdata.length; j++) {
                    // log.logInfo("found data for '" + key2 + "' = " + userdata[j]);
                    userstring += userdata[j];
                    userstring += "<BR>";
                }
            }
            /*
             * Checks if the data is mandatory or optional for printing in the
             * right language
             */
            if (relevance.equals("Mandatory")) {
                // set to bundle name
                relevance_string = "fd_mandatory";
            } else {
                // set to bundle name
                relevance_string = "fd_optional";
            }
            out.add(link);
            out.add(description);
            out.add(userstring);
            out.add(relevance_string);
        }
        return out;
    }

    
    /**
     * Get the config from the context. The configuration is expected to be set
     * by the controller before requests are sent to this servlet.
     * 
     * @return the configuration
     */
    private Properties getConfig() {
        final Properties config;
        
        /* Validate config */
        try {
            config = (Properties) getServletContext().getAttribute(RequestUtil.PROP_CONFIG);
        } catch (ClassCastException e) {
            throw new IllegalStateException("Config is not correctly set in context.");
        }
        if (config == null) {
            throw new IllegalStateException("Config is not set in context.");
        }
        return config;
    }
    
    /**
     * Implements the HttpServlet.doGet method.
     * 
     * @param request   the HTTP requeest
     * @param response  the HTTP response
     * @throws IOException      required by interface
     * @throws ServletException required by interface
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        if (request.getParameter("logout") != null) {
            log.logInfo("Logout received");
            storedticket = null;
            storedmap = null;
            final RequestDispatcher rd = getServletContext().getNamedDispatcher("Logout");
            rd.forward(request, response);
            return;
        }
        /*
         * Makes a Properties object named config, which gets the config
         * from the getConfig() method
         */
        Properties config;

        try {
            config = getConfig();
        } catch (IllegalStateException e) {
            config = null;
        }

        /* Resource bundle. */
        String langFromCookie = null;
        if (config != null && request.getCookies() != null) {
            langFromCookie = RequestUtil.getCookieValue((String) config
                    .get(RequestUtil.PROP_COOKIE_LANG), request
                    .getCookies());
        }
        
        //FIXME "en"
        final ResourceBundle bundle = RequestUtil.getBundle(
                RequestUtil.BUNDLE_INFORMATIONSERVLET, 
                request.getParameter(RequestUtil.PARAM_LANG), langFromCookie, null,
                request.getHeader("Accept-Language"), "en");

        HttpSession session = request.getSession(true);
        
        request.setAttribute("bundle", bundle);        
        request.setAttribute("urlPrefix", config.get(RequestUtil.PROP_INFORMATION_URL_PREFIX));

        String loginTicketId = request.getParameter("moriaID");
        if (loginTicketId != null) {
 
            Map userData = null;
            
            // FIXME: is it ok/safe to store userData in servlet
            if (storedticket != null && storedticket.equals(loginTicketId)) {
                userData = storedmap;
            }
            else {
              try {
                userData = MoriaController.getUserAttributes(loginTicketId, PRINCIPAL);
              } 
              catch (IllegalInputException e) {
                  log.logCritical("IllegalInputException in doGet()");
              }
              catch (UnknownTicketException e) {
                  log.logCritical("UnknownTicketException in doGet()");               
              }
              catch (InoperableStateException e) {
                  log.logCritical("InoperableStateException in doGet()");
              }
              
              // FIXME: see comment above. Is this ok?
              if (userData != null) {
                  storedmap = userData;
                  storedticket = loginTicketId;
                  
                  // print some log info about userAttributes found
                  log.logInfo("UserAttributes found ok...");
                  for (Iterator it = userData.keySet().iterator(); it.hasNext();) {
                     String key = (String) it.next();
                     log.logInfo("Attribute: " + key);
                  }
              }
            }
            if (userData == null) {
                // TODO: print error message instead of empty table?
                userData = new HashMap();
            }
            
            // need userorg as an attribute in the JSP to be able to print
            // instructions on where to update the optional or mandatory info
            String [] userorgarray = (String[]) userData.get(RequestUtil.EDU_ORG_LEGAL_NAME);
            String userorg = "unknown userorg";
            if (userorgarray != null && userorgarray.length > 0) {
                userorg = userorgarray[0];
            }
            request.setAttribute("userorg", userorg);
                      
            Vector tabledata = printTableToVector(userData, bundle);
            request.setAttribute("tabledata", tabledata);

            /* Configured values */
            request.setAttribute(RequestUtil.ATTR_ORGANIZATIONS, RequestUtil.parseConfig(config, RequestUtil.PROP_ORG, bundle.getLocale().getLanguage()));
            request.setAttribute(RequestUtil.ATTR_LANGUAGES, RequestUtil.parseConfig(config, RequestUtil.PROP_LANGUAGE, RequestUtil.PROP_COMMON));
            request.setAttribute(RequestUtil.ATTR_BASE_URL, config.getProperty(RequestUtil.PROP_INFORMATION_URL_PREFIX) + "?" + config.getProperty(RequestUtil.PROP_LOGIN_TICKET_PARAM) + "=" + loginTicketId);
            request.setAttribute(RequestUtil.ATTR_SELECTED_LANG, bundle.getLocale());
            
            // update cookie if language has changed
            if (request.getParameter(RequestUtil.PARAM_LANG) != null)
                response.addCookie(RequestUtil.createCookie((String) config.get(RequestUtil.PROP_COOKIE_LANG), request.getParameter(RequestUtil.PARAM_LANG), new Integer((String) config.get(RequestUtil.PROP_COOKIE_LANG_TTL)).intValue()));

            // print the table in the JSP
            final RequestDispatcher rd = getServletContext().getNamedDispatcher("Information.JSP");
            rd.forward(request, response);
        }
        else {
            // call doPost() to get the user attributes from the Controller
            doPost(request, response);
        }
    }
    
    /**
     * Implements the HttpServlet.doPost method.
     * 
     * @param request   the HTTP requeest
     * @param response  the HTTP response
     * @throws IOException      required by interface
     * @throws ServletException required by interface
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    throws ServletException, IOException {
        
        String jspLocation = getServletContext().getInitParameter("jsp.location");
        log.logInfo("jsp.location is '" + jspLocation + "'");
        String moriaID = null;
        boolean error = false;

        String attributes = getAllAttributes();
        String urlPrefix = (String) request.getAttribute("urlPrefix") + "?moriaID=";
        String urlPostfix = "";
        String principal = PRINCIPAL;
        
        try {
            MoriaController.initController(getServletContext());
            log.logInfo("Requested attributes: " + attributes);
            log.logInfo("URL prefix: " + urlPrefix);
            log.logInfo("URL postfix: " + urlPostfix);
            log.logInfo("Principal: " + principal);
            moriaID = MoriaController.initiateAuthentication(attributes.split(","), 
                        urlPrefix, urlPostfix, false, principal);
            log.logInfo("Moria ID is now " + moriaID);

        } catch (IllegalInputException e) {
            error = true;
            log.logCritical("IllegalInputException");
            request.setAttribute("error", e);
        } catch (AuthorizationException e) {
            error = true;
            log.logCritical("AuthorizationException");
            request.setAttribute("error", e);
        } catch (InoperableStateException e) {
            error = true;
            log.logCritical("InoperableStateException");
            request.setAttribute("error", e);
        }

        if (!error) {
            Properties config = (Properties) getServletContext().getAttribute(RequestUtil.PROP_CONFIG);
            log.logCritical("Configuration: " + config.toString());
            String redirectURL = config.getProperty(RequestUtil.PROP_LOGIN_URL_PREFIX) + "?" + config.getProperty(RequestUtil.PROP_LOGIN_TICKET_PARAM) + "=" + moriaID;
            log.logCritical("Redirect URL: " + redirectURL);
            response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);
            response.setHeader("Location", redirectURL);
        } else {
            log.logCritical("error!");
            RequestDispatcher rd = getServletContext().getRequestDispatcher(jspLocation + "/information.jsp");
            rd.include(request, response);
        }
    }
    
    /**
     * Builds a list of all possible user attributes.
     * 
     * @return Comma-separated list of attributes
     */
    String getAllAttributes() {
        String acc = "";
        for (Iterator iterator = feideattribs.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            acc += key;
            if (iterator.hasNext()) acc += ",";
        }
        return acc;
    }
}

