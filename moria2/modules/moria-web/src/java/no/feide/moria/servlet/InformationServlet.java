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
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.SAXParser;

//import no.feide.moria.controller.MoriaController;

//import no.feide.moria.servlet.RequestUtil;

//import no.feide.moria.servlet.RequestUtil;

//import no.feide.moria.authorization.UnknownServicePrincipalException;

//import no.feide.moria.servlet.RequestUtil;

//import no.feide.moria.controller.MoriaController;
//import no.feide.moria.servlet.RequestUtil;

import java.util.Vector;
import java.util.Arrays;
/**
 * This servlet prints all available information about a user.
 * 
 * @author Eva Indal
 */
public class InformationServlet extends HttpServlet {
    /* a hash map containing all possible attributes for a user.
     Each item in the hashmap maps from a attribute name to a
     AttribsData class instance */
    private HashMap feideattribs;     
    
    /**
     * Constructor.
     * Get info about user attributes 
     */
    public InformationServlet() {
        feideattribs = getAttribs();
    }
    
    /**
     * Temporary helper function used to create data for a test user
     * Used by getExampleUserData() 
     */
    private Vector getSingleVector(String s) {
        Vector v = new Vector();
        v.add(s);
        return v;
    }
    
    /**
     * Temporary function used for testing.
     * Fill in some dummy user data in a HashMap and return it. 
     * 
     */
    public HashMap getExampleUserData() {
        HashMap map = new HashMap();
        
        map.put(new String("norEduPersonNIN"), getSingleVector("12345612345"));
        map.put(new String("eduPersonPrincipalName"), getSingleVector("indal@uninett.no"));
        map.put(new String("norEduPersonBirthDate"), getSingleVector("03.05.78"));
        map.put(new String("eduOrgLegalName"), getSingleVector("UNINETT"));
        map.put(new String("preferredLanguage"), getSingleVector("Bokmål"));
        
        Vector vec = new Vector();
        vec.add(new String("Unit1"));
        vec.add(new String("Unit2"));
        vec.add(new String("Unit3"));
        map.put(new String("eduPersonOrgUnitDN"), vec);
        return map;
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
     * Print the HTML header
     * 
     * @param out the html PrintWriter
     * @param bundle resourcebundle for language
     */
    private void printHeader(PrintWriter out, ResourceBundle bundle) {
        out.println("<HTML>");
        out.println("<HEAD>");
        out.println("<TITLE>" + bundle.getString("header_title") + "</TITLE>");
        out.println("</HEAD>");
        out.println("<BODY>");        
    }
    
    /**
     * Print the HTML footer
     * 
     * @param out the html PrintWriter
     * @param bundle resourcebundle for language
     */
    private void printFooter(PrintWriter out, ResourceBundle bundle) {
        out.println("</BODY>");
        out.println("</HTML>");        
    }
    
    /**
     * Print table for a user
     * 
     * @param out the html PrintWriter
     * @param userData the user data
     * @param bundle resourcebundle for language
     */
    private void printTable(PrintWriter out, HashMap userData, ResourceBundle bundle) {
        out.println("<B>" + bundle.getString("user_info") + "</B></BR></BR>");
        
        out.println("<TABLE border=1>");
        out.println("<TR><TH>" + bundle.getString("tc_description") + "</TH><TH>" + bundle.getString("tc_value") + "</TH><TH>" + bundle.getString("tc_relevance") + "</TH></TR>");
        
        Vector temp = new Vector();
        for (Iterator iterator = feideattribs.keySet().iterator(); iterator.hasNext();) {
            String key = (String) iterator.next();
            AttribsData attribsData = (AttribsData) feideattribs.get(key);
            temp.add(attribsData);
        }
        /* Sorts the description fields according to the xml file */
        Object[] attribsArray = temp.toArray();
        Arrays.sort(attribsArray, new AttribsData(0));
        
        /* Fetch the data to print */
        int n = temp.size();
        for (int i = 0; i < n; i++) {
            AttribsData adata = (AttribsData) attribsArray[i];             
            String key2 = (String) adata.getData("key");              
            Vector userdata = (Vector) userData.get(key2);
            String description = adata.getData("description");
            String bundleid = adata.getData("resourcebundle");
            String bundlename = (String) bundle.getString(bundleid);
            /* see if resourcebundle has a name */
            if (bundlename != null) description = bundlename;
            String link = adata.getData("link");
            String relevance = adata.getData("relevance");
            
            String userstring = "";
            Vector userorgvec = (Vector) userData.get(RequestUtil.EDU_ORG_LEGAL_NAME);
            String userorg = (String) userorgvec.get(0);
            String relevance_string = null;
            
            if (userdata != null) {
                Vector vec = (Vector) userdata;
                for (int j = 0; j < vec.size(); j++) {
                    userstring += (String) vec.get(j);
                    userstring += "<BR>";
                }
            }
            /* Checks if the user has data in the mandatory fields*/
            else if (relevance.compareTo(new String("Mandatory")) == 0) {
                userstring = "<FONT COLOR=\"#ff0000\">" + bundle.getString("m_missing") + " " + userorg + "</FONT>";
            }
            else {
                userstring = bundle.getString("o_missing")+ " "  + userorg;
            }
            /*Checks if the data is mandatory or optional for printing in the right language*/
            if (relevance.equals("Mandatory")) {
                relevance_string = bundle.getString("fd_mandatory");
            }
            else {
                relevance_string = bundle.getString("fd_optional");
            }
            out.println("<TR><TD>"+"<A HREF=" + link + ">" + description + "</A>"+"</TD>"
                    +"<TD>"+userstring+"</TD>"
                    +"<TD>"+relevance_string+"</TD>"+ "</TR>");
        }
        out.println("</TABLE>");
    }
    
    /**
     * Get the config from the context. The configuration is expected to be set by the controller before requests are
     * sent to this servlet.
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
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException { 
        response.setContentType("text/html");
        
        /*makes a Properties object named config, which gets the config from the getConfig() method */
        Properties config;
        
        try {
            config = getConfig();
        } catch (IllegalStateException e) {
            config = null;
        }
        /* Resource bundle. */
        String langFromCookie = null;
        if (config != null && request.getCookies() != null) {
            langFromCookie = 
                RequestUtil.getCookieValue((String) config.get(RequestUtil.PROP_COOKIE_LANG), 
                        request.getCookies());
        }
        final ResourceBundle bundle = 
            RequestUtil.getBundle(RequestUtil.BUNDLE_INFORMATIONSERVLET, request.getParameter("lang"), langFromCookie,
                    null,
                    request.getHeader("Accept-Language"),
            "en");
        
        PrintWriter out = response.getWriter();
        printHeader(out, bundle);
        
        /* Fetch userdata from the HttpSession */
        HttpSession session = request.getSession(true);
        // HashMap userData = (HashMap) session.getAttribute("userData");
        
        /* just fill in some dummy data for now */
        HashMap userData = getExampleUserData();
        
        
        /* Logout */
        if (request.getParameter("logout") != null) {
            session.removeAttribute("userData");
            ((HttpServletResponse)response).setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);  
            ((HttpServletResponse)response).setHeader("Location", request.getRequestURL().toString());
        }
        
        /* If userData == null the user has not been authenticated.
         This should not happen. */ 
        if (userData == null) {
            out.println("<H1>" + bundle.getString("user_na") + "</H1>");
            out.println(bundle.getString("user_error"));
            printFooter(out, bundle);
            out.close();
            return;
        }
        
        /* The user has been authenticated. */ 
        else {
            out.println("<H1>" + bundle.getString("user_msg") + "</H1>");
        }
        
        /* Logout */
        out.println("[<a href=\""+request.getRequestURL().toString()+"?logout"+"\">" + bundle.getString("user_logout") + "</a>]<BR>");
        
        
        /* The user has been authenticated, but the userData hash is
         empty. */
        if (userData.size() == 0) {
            out.println(bundle.getString("user_nodata"));
        }
        
        /* Create a table with the contents of the userData hash. */
        else { 
            printTable(out, userData, bundle);
        }
        
        printFooter(out, bundle);
        
        out.close();
        
    }
    
}
