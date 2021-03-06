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

package no.feide.mellon.servlet;

import java.io.IOException;
import java.rmi.RemoteException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.rpc.ServiceException;

import no.feide.mellon.MoriaConnector;
import no.feide.mellon.MoriaUserData;



/**
 * @author Lars Preben S. Arnesen, l.p.arnesen@usit.uio.no
 * @version $Id$
 */


public class MoriaAuthenticationFilter implements Filter {
	
	private FilterConfig config = null;
	
	/** Initialize config. */
	public void init(FilterConfig config) throws ServletException {
		this.config = config;		
		try {
			System.getProperties().load(getClass().getResourceAsStream("/mellon.properties"));
		} catch (IOException e) {
			throw new ServletException("IOException caught and re-thrown as ServletException");
		}        
	}
	
	/** Remove config. */
	public void destroy() {
		config = null;
	}
	
    /** Perform authentication. If a requests already belongs to a
     * session and the session contains user data, the user alrady has
     * been authenticated and are let through. If no user data is
     * present, the user is redirected to FEIDE for login. After the
     * user returns from FEIDE, user data will be fetched from Moria
     * (FEIDE) and stored in the HttpSession. Servlets or other filter
     * (for instance an authentication filter, may use these data for
     * user authorization. */
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {

    	/* The API for Moria (the FEIDE authentication service). */
    	MoriaConnector moria;
    	
    	String connectorClass = System.getProperty("no.feide.mellon.connector");
    	if (connectorClass == null) {
    		System.err.println("No connector specified.");
    		return;
    	}
    	
    	try {
			moria = (MoriaConnector)Class.forName(connectorClass).newInstance();
		} 
    	catch (InstantiationException e1) {
			System.err.println("Error instantiating connector.");
			e1.printStackTrace();
			return;
		} 
    	catch (IllegalAccessException e1) {
			System.err.println("Error instantiating connector.");
			e1.printStackTrace();
			return;
		} 
    	catch (ClassNotFoundException e1) {
			System.err.println("Error instantiating connector.");
			e1.printStackTrace();
			return;
		}
    	
    	try {
			moria.connect( System.getProperty("no.feide.mellon.serviceUsername"), System.getProperty("no.feide.mellon.servicePassword"));
    	}
    	
    	catch (ServiceException e) {
    		e.printStackTrace();
    		throw new ServletException(e);
    	}
            
            
        /* The HttpSession is used for user tracking so that the user
         * don't have to authenticate more than once per "surfing
         * session".*/
        HttpSession httpSession = 
            ((HttpServletRequest)request).getSession(true);

        /* The URL parameter used to identify the user after being
         * sent back from FEIDE. */
        String moriaID = request.getParameter("moriaID");
        
        /* If moriaID is null, the user is either not authenticated
         * has been authenticated on an earlier request. */
        if (moriaID == null) {

            /* userData is null when the user is NOT authenticated */ 
            if (httpSession.getAttribute("userData") == null) {
                
                HttpServletRequest httpRequest = (HttpServletRequest) request;
                String redirectURL; 

                /* Construct the URL that Moria should redirect the
                 * user back to after authentication. */
                String requestURL = httpRequest.getRequestURL().toString();
                String backToMellonURL  = requestURL;

                if (httpRequest.getQueryString() != null) 
                    backToMellonURL += "?"+httpRequest.getQueryString()+"&moriaID=";
                else
                    backToMellonURL += "?moriaID=";

                /* Establish contact with Moria and aquire a login
                 * session. The user should be redirected to this
                 * URL. */
                try {
                     redirectURL = moria.requestSession(System.getProperty("no.feide.mellon.requestedAttributes").split(","), backToMellonURL, "", false);
                }
                
                catch (RemoteException e) {
                    throw new ServletException(e);
                }
                
                /* Redirect to login page */
                ((HttpServletResponse)response).setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);  
                ((HttpServletResponse)response).setHeader("Location", redirectURL);

            }
            
            /* User data is present in the HttpSession. This indicates
             * that the user has been authenticated earlier and the
             * request should pass through this filter without any
             * other actions taken.*/
            else {
                chain.doFilter(request, response); 
            }
        }

        /* moriaID is present in the request. The user has been
           redirected from FEIDE (after authentication) } and we
           should retrieve data about the user and store it in the
           HttpSession for later authentication (whitout redirecting
           the user).*/
        else {

            MoriaUserData userData;

            /* Fetch user attributes from Moria. */
            try {
                userData = new MoriaUserData(moria.getAttributes(moriaID));
            }

            /* The user has NOT been authenticated. This should never
             * be the case since only authenticated users are
             * redirected from the login service. */
            catch (RemoteException e) {
                ((HttpServletResponse)response).setStatus(HttpServletResponse.SC_FORBIDDEN);  
                return;
            }


            /* We got user data. Store it in the HttpSession for later
               authentication. The user is authenticated, let him pass. */
            httpSession.setAttribute("userData", userData.getUserData());
            chain.doFilter(request, response);

        }
        
    }
}
