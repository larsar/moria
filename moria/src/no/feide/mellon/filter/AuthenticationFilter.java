package no.feide.mellon.filter;

import java.io.IOException;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.ServletRequest;

import no.feide.mellon.Moria;
import no.feide.mellon.MoriaException;
import java.util.HashMap;


/**
 * @author Lars Preben S. Arnesen, l.p.arnesen@usit.uio.no
 * @version $Id$
 */


public class AuthenticationFilter implements Filter {
  
    private FilterConfig config = null;

    /** Initialize config. */
    public void init(FilterConfig config) throws ServletException {
        this.config = config;

	// Read Mellon-side properties.
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
        Moria moria;

        try {
            moria = Moria.getInstance();
        }
        
        catch (MoriaException e) {
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
                     redirectURL = moria.requestSession(System.getProperty("no.feide.mellon.requestedAttributes").split(","), backToMellonURL, "");
                }
                
                catch (MoriaException e) {
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

            HashMap attributes;

            /* Fetch user attributes from Moria. */
            try {
                attributes = moria.getAttributes(moriaID);
            }

            /* The user has NOT been authenticated. This should never
             * be the case since only authenticated users are
             * redirected from the login service. */
            catch (MoriaException e) {
                ((HttpServletResponse)response).setStatus(HttpServletResponse.SC_FORBIDDEN);  
                return;
            }


            /* We got user data. Store it in the HttpSession for later
               authentication. The user is authenticated, let him pass. */
            httpSession.setAttribute("userData", attributes);
            chain.doFilter(request, response);

        }
        
    }
}
