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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import javax.servlet.RequestDispatcher;
import java.io.IOException;

/**
 * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
 * @version $Revision$
 */
public class LoginServlet extends HttpServlet {

    /**
     * Handles the GET requests.
     *
     * @param request           the HTTP request object
     * @param response          the HTTP response object
     * @throws IOException
     * @throws ServletException
     */
    public final void doGet(final HttpServletRequest request, final HttpServletResponse response)
            throws IOException, ServletException {
        request.setAttribute("foo", "bar");

        RequestDispatcher rd = getServletContext().getRequestDispatcher("/login.jsp");
        rd.include(request, response);
    }
}
