package no.feide.mellon.servlet;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Vector;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.http.*;
//import javax.xml.rpc.Stub;
import no.feide.mellon.Moria;
import no.feide.moria.service.SessionDescriptor;
import no.feide.moria.service.UserAttribute;


/**
 * Will establish a Moria session and redirect to the URL handling the
 * actual login.
 */
public class SessionServlet
extends HttpServlet {
    
    /** Used for logging. */
    private static Logger log = Logger.getLogger(SessionServlet.class.toString());

    /**
     * Handles GET requests.
     * @param request The GET request.
     * @param response The response, used to redirect.
     * @throws IOException
     * @throws ServletException If a <code>SessionException</code> is
     *                          caught.
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
    {
        log.finer("doPost(HttpServletRequest, HttpServletResponse)");
        
        try {       
	    // Verify request query.
	    if ( (request.getParameter("url") == null) && (request.getParameter("id") == null) ) {
	        log.severe("Neither URL or ID included in request query: "+request.getQueryString());
		throw new ServletException("Neither URL or ID included in request query: "+request.getQueryString());
	    }
            
            // Prepare Moria interface.
            Moria moria = Moria.getInstance();            

            // Is this the first or second round?
            if (request.getParameter("id") == null) { 
                
                // First round; create a new Moria session and get the session
                // descriptor. Also, request the user attributes.
                SessionDescriptor session = moria.requestSession(new String[] {"cn", "uid"}, request.getParameter("url"));

                // Redirect to the session URL and include the session ID.
                response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);  // Shouldn't this be SC_FOUND?
                response.setHeader("Location", session.getURL()+"?id="+session.getID());
                log.info("Redirect to Moria: "+session.getURL()+"?id="+session.getID());
                return;  // We're done.
                
	    } else {
                
                // Second round; verify the Moria session.
		String id = request.getParameter("id");
		String url = moria.verifySession(id);

                if (url == null) {
                    // Some user-friendly HTML should go here...
                    log.severe("Session does not exist: "+id);
                    throw new ServletException("Session does not exist: "+id);
                }
                
                // Receive and show attributes.
                UserAttribute[] attributes = moria.getAttributes(id);
                StringWriter buffer = new StringWriter();
                buffer.write("Received attributes:\n");
                for (int i=0; i<attributes.length; i++) {
                    buffer.write(attributes[i].getName()+':');
                    Vector v = attributes[i].getValues();
                    for (int j=0; j<v.size(); j++)
                        buffer.write(" \""+(String)v.get(j)+'\"');
                    buffer.write("\n");
                }
                log.info(buffer.toString());
                
		// Redirect to the original resource URL.
		response.setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);  // Shouldn't this be SC_FOUND?
		response.setHeader("Location", url);
		log.info("Redirect to resource: "+url);
	    }
        
        } catch (Exception e) {
            log.severe(e.getClass().getName()+" caught and re-thrown as ServletException");
            throw new ServletException(e);
        }
        
        // TODO.
        java.io.PrintWriter out = response.getWriter();
        out.println("<html><body>Authenticated!</body></html>");
    }

}
