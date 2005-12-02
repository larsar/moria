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

import no.feide.moria.log.MessageLogger;

import org.apache.commons.codec.binary.Base64;

import java.io.IOException;

/**
 * This Servlet is only used to show pictures.
 * @author Eva Indal
 * @version $Revision$
 */
public class PictureServlet
extends HttpServlet {

    /**
     * Used for logging.
     */
    MessageLogger log = new MessageLogger(PictureServlet.class);
    
    /**
     * Name of the request attribute containing the actual picture(s).
     */
    public static final String PICTURE_ATTRIBUTE = "picture";


    /**
     * Implements the HttpServlet.doGet method.
     * @param request
     *            The HTTP request.
     * @param response
     *            The HTTP response.
     * @throws IOException
     * @throws ServletException
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException {
                
        try {

            int idx = 0;
            String index = request.getParameter("index");
            if (index != null) {
                idx = Integer.parseInt(index);
            }

            // login.feide.no/Picture can be entered as an URL without any further arguments.
	    // That will lead to a null pointer exception, caught by the catch-clause below, but 
            // we really should avoid creating such errors. For one, they end up in the log file looking
	    // horribly serious, when there is little reason to worry.


            response.setContentType("image/jpeg");
            String[] picture = (String[]) request.getSession().getAttribute(PICTURE_ATTRIBUTE);
	    
	    if (picture == null) {
		log.logWarn("PictureServlet called without picture context. Nothing to display");
	    }
	    else {
		    byte[] decoded = Base64.decodeBase64(picture[idx].getBytes("ISO-8859-1"));

		    ServletOutputStream writer = response.getOutputStream();
		    writer.write(decoded);
		    writer.close();
	    }

        } catch (Throwable t) {
            log.logWarn("Picture display failed", t);
        }

    }
}
