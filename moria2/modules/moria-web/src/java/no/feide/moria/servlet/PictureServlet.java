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
import javax.servlet.ServletOutputStream;
import java.io.IOException;
/**
 * This Servlet is only used to show pictures.
 * 
 * @author Eva Indal
 * @version $Revision$
 */
public class PictureServlet extends HttpServlet {

    /**
     * Implements the HttpServlet.doGet method.
     *
     * @param request   The HTTP request.
     * @param response  The HTTP response.
     * @throws IOException
     * @throws ServletException
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws IOException, ServletException {
           try {
           int idx = 0;
           String index = request.getParameter("index");
           if (index != null) {
               idx = Integer.parseInt(index);
           }
           response.setContentType("image/jpeg");
           String [] picture = (String[]) request.getSession().getAttribute("picture");
           ServletOutputStream writer = response.getOutputStream();
           writer.write(picture[idx].getBytes("ISO-8859-1"));
           writer.close();
           } catch (Throwable t) {
               System.err.println("Picture failed");
           }

}
}