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

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.io.PrintWriter;
import java.io.IOException;
import java.util.Iterator;

/**
 * A simple servlet to be used to demonstrate the use of the
 * AuthenticationFilter.
 */
public class DemoServlet extends HttpServlet {
    
    /**
     * A get request only results in the generation of a simple HTML
     * page. If the AuthenticationFilter has been used to filter all
     * requests to this servlet, the user has been authenticated by
     * the FEIDE login service (Moria).
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
    {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");



        /* Fetch userdata from the HttpSession. */
        HttpSession httpSession = request.getSession(true);
        HashMap userData = (HashMap) httpSession.getAttribute("userData");


        /* Logout */
        if (request.getParameter("logout") != null) {
            httpSession.removeAttribute("userData");
            ((HttpServletResponse)response).setStatus(HttpServletResponse.SC_MOVED_TEMPORARILY);  
            ((HttpServletResponse)response).setHeader("Location", request.getRequestURL().toString());
        }

        
        /* HTML header */
        out.println("<HTML>");
        out.println("<HEAD>");
        out.println("<TITLE>Mellon demo</TITLE>");
        out.println("</HEAD>");
        out.println("<BODY>");

        /* If userData == null the user has not been authenticated.
         * This should not happen. */
        if (userData == null) {
            out.println("<H1>NOT authenticated!</H1>");
            out.println("The user has not been authenticated. The AuthenticationFilter is probably disabled. This should not happen if the filter is configured correct.");
            return;
        }

        /* The user has been authenticated. */
        else {
            out.println("<H1>Access granted</H1>");
        }

        /* Logout */
        out.println("[<a href=\""+request.getRequestURL().toString()+"?logout"+"\">Logout</a>]<BR>");


        /* The user has been authenticated, but the userData hash is
         * empty. */
        if (userData.size() == 0) {
            out.println("The user has been authenticated, but no more user data is available.");
        }

        /* Create a table with the contents of the userData hash. */
        else {
            /* Logout link */

            out.println("<B>You have been authenticated and the following attributes are now stored in the HttpSession of the web service:</B></BR></BR>");
            
            out.println("<TABLE border=1>");
            out.println("<TR><TH>Key</TH><TH>Value</TH></TR>");

            for (Iterator iterator = userData.keySet().iterator(); iterator.hasNext();) {
                String key = (String) iterator.next();
                out.println("<TR><TD>"+key+"</TD><TD>"+userData.get(key)+"</TD></TR>");
            }

            out.println("</TABLE>");
        }

        out.println("</BODY>");
        out.println("</HTML>");
        
    }
}
