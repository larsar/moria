/*
 * Copyright (c) 2004 UNINETT FAS
 * 
 * This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 * 
 * $Id$
 */

package no.feide.moria.servlet;

import javax.servlet.http.HttpServlet;

import no.feide.moria.log.MessageLogger;

import java.util.Properties;

/**
 * 
 * @author Eva Indal
 *
 */
public class MoriaServlet extends HttpServlet {

    /**
     * Get the config from the context. The configuration is expected to be set
     * by the controller before requests are sent to this servlet.
     *
     * @in 
     * @return The configuration.
     * @throws IllegalStateException
     *          If the config is not properly set.
     *
     */
    public Properties getServletConfig(String[] required_parameters, MessageLogger log) throws IllegalStateException {
        final Properties config;

        /* Validate config */
        try {
            config = (Properties) getServletContext().getAttribute(RequestUtil.PROP_CONFIG);
        } catch (ClassCastException e) {
            throw new IllegalStateException("Config is not correctly set in context.");
        }
        if (config == null)
            throw new IllegalStateException("Config is not set in context.");

        // Are we missing some required properties?
        for (int i = 0; i < required_parameters.length; i++) {
            String parvalue = config.getProperty(required_parameters[i]);
            if ((parvalue == null) || (parvalue.equals(""))) {
                    log.logCritical("Required parameter '" + required_parameters[i] + "' is not set");
                    throw new IllegalStateException();
            }
        }
        return config;    
    }
}
