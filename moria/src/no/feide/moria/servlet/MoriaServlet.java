/**
 * Copyright (C) 2003 FEIDE
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package no.feide.moria.servlet;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.servlet.ServletConfig;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import no.feide.moria.Configuration;
import no.feide.moria.ConfigurationException;

import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.servlet.VelocityServlet;

public class MoriaServlet extends VelocityServlet {

    /** Used for logging. */
    private static Logger log = Logger.getLogger(LoginServlet.class.toString());

    /**
     * Called by the VelocityServlet init(). Reads the template path
     * from Properties.
     * @throws IOException If a <code>ConfigurationException</code> is
     *                     caught.
     */
    protected Properties loadConfiguration(ServletConfig config )
        throws IOException, FileNotFoundException {
        log.finer("loadConfiguration(ServletConfig)");

        try {
            

            Properties p = new Properties();
            String path = Configuration.getProperty("no.feide.moria.servlet.TemplateDir");
            

            p.setProperty("file.resource.loader.cache", "true");
            p.setProperty("file.resource.loader.modificationCheckInterval","0");

            /* If path is null, log it. */ 
            if (path == null) {
                log.severe("Path to Velocity templates not set.");
                throw new FileNotFoundException("Template path not found.");
            }

            p.setProperty(Velocity.FILE_RESOURCE_LOADER_PATH,  path);
            p.setProperty("runtime.log", Configuration.getProperty("no.feide.moria.VelocityLog"));
            
            return p;
            
        } catch (ConfigurationException e) {
            log.severe("ConfigurationException caught and re-thrown as IOException");
            throw new IOException("ConfigurationException caught");
        }
    }


    String selectLanguage(HttpServletRequest request, HttpServletResponse response, String defaultLang) {

        // else if (acceptLanguage == null || acceptLanguage.equals("")) 
        return defaultLang;
    }



    HashMap getBundle(String bundleName, HttpServletRequest request, HttpServletResponse response, String defaultLang) {
        Locale locale = null;
        ResourceBundle bundle = null;
        ResourceBundle fallback = null;
        String selectedLanguage = "";
        String acceptLanguage = request.getHeader("Accept-Language");
        
        HashMap result = new HashMap();

        if (acceptLanguage == null) 
            acceptLanguage = "";

        /* Select language. Prefer: URL parameter, Cookie, Browser setting */
        String overrideLang = request.getParameter("lang");
        if (overrideLang == null) 
            overrideLang = getCookieValue("lang", request);

        if (overrideLang != "") {
            setCookieValue("lang", overrideLang, response);
            acceptLanguage =  overrideLang;
        }

        StringTokenizer tokenizer = new StringTokenizer(acceptLanguage, ",");

        /* Find fallback resource bundle. */
        try {
            fallback = ResourceBundle.getBundle(bundleName, new Locale("bogus"));
        }
        catch (MissingResourceException e) {
            /* No fallback */
        }            

        /* Parse Accept-Language and find matching resource bundle */
        while (tokenizer.hasMoreTokens()) {
            String lang = tokenizer.nextToken();
            int index;

            /* Languages are devided by ";" */
            if ((index = lang.indexOf(";")) != -1) {
                lang = lang.substring(0, index);
            }

            lang = lang.trim();

            /* Language and country is devided by "-" (optional) */
            if ((index = lang.indexOf("-")) != -1) {
                lang = lang.substring(0, index);
            }
            
            selectedLanguage = lang;

            locale = new Locale(lang); 
            bundle = ResourceBundle.getBundle(bundleName, locale);

            /* Abort search if a bundle (not fallback) is found */
            if (bundle != fallback) 
                break;

            /* About search if the fallback bundle is actually requested */
            else if (bundle == fallback && locale.getLanguage().equals(Locale.getDefault().getLanguage()))
                break;

        }


        /* Should never happen, but just in case. */
        if (bundle == null) 
            bundle = ResourceBundle.getBundle(bundleName, new Locale(defaultLang));

        result.put("selectedLanguage", selectedLanguage);
        result.put("bundle", bundle);
        return result;
    }



    /**
     * Return a requested cookie value
     * @param cookieName Name of the cookie
     * @param request The Http request
     * @return Requested value, empty string if not found
     */
    private String getCookieValue(String cookieName, HttpServletRequest request) {
        String value = "";
        Cookie[] cookies = request.getCookies();

        if (cookies != null) {
            for (int i = 0; i < cookies.length; i++) {
                if (cookies[i].getName().equals(cookieName)) {
                    value = cookies[i].getValue();
                }
            }
        }

        return value;
    }

    
    
    /**
     * Add a cookie to the response.
     * @param cookieName Name of the cookie
     * @param cookieValue Value to be set
     * @param response The http response
     */
    private void setCookieValue(String cookieName, String cookieValue, HttpServletResponse response) {
        Cookie cookie = new Cookie(cookieName, cookieValue);
        int validDays;
        
        try {
            validDays = new Integer(Configuration.getProperty("no.feide.moria.servlet.cookieValidDays")).intValue();
        }

        catch (ConfigurationException e) {
            log.warning("Unable to read properties (cookieValidDays).");
            validDays = 1;
        }

        cookie.setMaxAge(validDays*24*60*60); // Days to seconds
        cookie.setVersion(0);
        response.addCookie(cookie);
    }


    void loadBundleIntoContext(ResourceBundle bundle, Context context, String wsName, String wsURL) {
            /* Set template-variables from properties */
        for (Enumeration e = bundle.getKeys(); e.hasMoreElements();) {
            String key = (String) e.nextElement();
            String value = bundle.getString(key);
            int index;
            
            /* This changes WS_NAME to hyperlink in all property
             * strings from the bundle.*/
            if ((wsName != null && wsURL != null) && (index = value.indexOf("WS_NAME")) != -1 && wsName != null) {
                value = value.substring(0, index)+"<A href=\""+wsURL+"\">"+wsName+"</A>"+value.substring(index+7, value.length());
            }

            context.put(key, value);
        }   
    }
    
}
