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

import java.util.logging.Logger;
import java.util.Properties;
import java.util.HashMap;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Enumeration;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.IOException;
import java.io.FileNotFoundException;

import no.feide.moria.Configuration;
import no.feide.moria.ConfigurationException;
import no.feide.moria.stats.StatsStore;
import no.feide.moria.stats.WebServiceStats;
import no.feide.moria.authorization.AuthorizationData;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Cookie;

import org.apache.velocity.Template;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.apache.velocity.servlet.VelocityServlet;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

/**
 * Presents the actual login page.
 * @author Lars Preben S. Arnesen l.p.arnesen@usit.uio.no
 * @version $Id$
 */
public class StatsServlet extends VelocityServlet {
    
    /** Used for logging. */
    private static Logger log = Logger.getLogger(LoginServlet.class.toString());
    /** Statistics */
    private StatsStore stats = StatsStore.getInstance();
   


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



    /**
     * Handles all http requests from the client. A login page is
     * returned for GET-requests and POST requests are considered as
     * login attempts.
     * @param request  The http request
     * @param response The http response
     * @param context  The Velocity contex
     * @return Template to be used for request
     */   
    public Template handleRequest( HttpServletRequest request,
	HttpServletResponse response, Context context ) throws ServletException {        
        
        log.finer("handleRequest(HttpServletRequest, HttpServletResponse, Context)");


        /* A GET should only return the login page. POST is used for
         * login attempts.*/ 
        try {
            
            /* Uptime */
            HashMap upTime = stats.upTime();
            for (Iterator it = upTime.keySet().iterator(); it.hasNext(); ) {
                String label = (String) it.next();
                context.put(label, upTime.get(label));
            }
            
            /* Authorization data */
            context.put("numOfWebServices", ""+AuthorizationData.getInstance().numOfWebServices());

            /* Configuration */
            context.put("properties", Configuration.getProperties());

            /* Web Services */
            HashMap wsStats = stats.getStats();
            context.put("wsStats", wsStats);
            Object[] sortedWsNames =  wsStats.keySet().toArray();
            Arrays.sort(sortedWsNames);

            context.put("sortedWsNames", sortedWsNames);
            context.put("deniedSessionsAuthentication", new Integer(stats.getDeniedSessionsAuthentication()));

            return getTemplate("stats.vtl");
        }

        catch( ParseErrorException e ) {
            log.severe("Parse error. " + e);
            throw new ServletException(e);
        }

        catch( ResourceNotFoundException e ) {
            log.severe("Template file not found. " + e);
            throw new ServletException(e);
        }

        catch (ConfigurationException e) {
            log.severe("Configuration exception. " +e);
            throw new ServletException(e);
        }

        catch( Exception e ) {
            StringWriter stackTrace = new StringWriter();
            e.printStackTrace(new PrintWriter(stackTrace));
            log.severe("Unspecified error during template parsing: \n" + stackTrace.toString());
            throw new ServletException(e);
        }
    }
}
