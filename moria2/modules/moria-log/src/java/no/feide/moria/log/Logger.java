/*
 * Copyright (c) 2004 UNINETT FAS
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 * 
 * $Id$
 *  
 */

package no.feide.moria.log;

import java.util.Properties;

import org.apache.log4j.PropertyConfigurator;

/**
 * This is the Logger used for all logging in Moria. It's goal is to make
 * logging a worry free task for the other components.
 * 
 * @author Bjørn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public abstract class Logger {

    protected static org.apache.log4j.Logger log4j = org.apache.log4j.Logger.getLogger(Logger.class);

    /**
     * Default constructor
     *  
     */
    protected Logger() {
    }

    /**
     * Use this method to configure the logger
     * 
     * @param properties
     *            a set of properties
     */
    public static void setConfig(final Properties properties) {
        PropertyConfigurator.configure(properties);
    }
}