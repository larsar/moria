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

import no.feide.moria.servlet.MoriaServlet;
import no.feide.moria.SessionStore;
import no.feide.moria.Session;
import no.feide.moria.NoSuchSessionException;
import no.feide.moria.SessionException;

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

import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.logging.Logger;
import java.io.PrintWriter;
import java.io.StringWriter;

public class LogoutServlet extends MoriaServlet {

    /** Used for logging. */
    private static Logger log = Logger.getLogger(LoginServlet.class.toString());


    /**
     * Handles all http requests from the client. If an existing
     * session is found it's invalidated. If a redirect URL is sent as
     * part of the request, the user is redirected, else the user is
     * presented with a information page.
     * @param request  The http request
     * @param response The http response
     * @param context  The Velocity contex
     * @return Template to be used for request
     */   
    public Template handleRequest( HttpServletRequest request,
                                   HttpServletResponse response, Context context ) throws ServletException {        
        
        log.finer("handleRequest(HttpServletRequest, HttpServletResponse, Context)");
        

        /* Find users http session */
        HttpSession httpSession = 
            ((HttpServletRequest)request).getSession(true);

        /* URL for redirecting the user back to */
        String redirectUrl = (String) request.getParameter("redirect");

        String existingSessionID = (String) httpSession.getAttribute("moriaID");

        /* Find and invalidate session */
        try {
            SessionStore sessionStore = SessionStore.getInstance();
            sessionStore.deleteSession(sessionStore.getSession(existingSessionID));
            log.info("SSO Logout, SID="+existingSessionID);
        }

        catch (NoSuchSessionException e) {
            log.info("Did not find SSO session: "+existingSessionID);
        }
  
        catch (SessionException e) {
            log.severe("Unable to access SessionStore: "+existingSessionID);
        }

        /* Redirect if a URL is given as parameter to the request */
        if (redirectUrl != null && !redirectUrl.equals("")) {
            response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);  
            response.setHeader("Location", redirectUrl);
            log.info("Logout-redirec to: "+redirectUrl);
            return null;
        }

        HashMap bundleData = getBundle("logout", request, response, "nb");
        ResourceBundle bundle = (ResourceBundle) bundleData.get("bundle");

        loadBundleIntoContext(bundle, context, null, null);

        /* Else return a logout page */
        try {
            return getTemplate("logout.vtl");
        }
        
        catch (Exception e) {
            System.out.println(e);
            return null;
        }
    }
}
