/*
 * Copyright (c) 2004 FEIDE
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
 * 
 */

package no.feide.moria.log;

import java.util.Properties;

/**
 * This is the Logger used for all logging in Moria. It's goal is to make
 * logging a worry free task for the other components.
 * 
 * @author Bjørn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public final class Logger {

    /*
     * Status types used by this system
     */
    public static final int STATUS_BAD_USER_CREDENTIALS = 0x01;
    public static final int STATUS_BAD_SERVICE_CREDENTIALS = 0x02;

    /*
     * Log levels used by this system
     */
    public static final int LEVEL_INFO = 0x01;
    public static final int LEVEL_WARN = 0x02;
    public static final int LEVEL_DEBUG = 0x03;

    /**
     * @param properties
     */
    public static void setConfig(Properties properties) {
    }

    /**
     * Used for logging user-inititated access (user interaction through the
     * web interface).
     * 
     * @param status
     *            indicates the type of event. Use STATUS_* constants defined
     *            in this class
     * @param serviceId
     *            the id of the service that is responsible for this operation
     * @param userId
     *            the id of the user, may be null if unknow at time of event
     * @param userIpAddr
     *            the address the request originated from
     * @param incomingTicket
     *            the ticket given with the request
     * @param outgoingTicket
     *            the potential returned ticket, may be null
     */
    public static void logUserAccess(int status, String serviceId,
            String userId, String userIpAddr, String incomingTicket,
            String outgoingTicket) {
    }

    /**
     * Used for logging service-inititated access
     * 
     * @param status
     *            indicates the type of event. Use STATUS_* constants defined
     *            in this class
     * @param serviceId
     *            the id of the service that is peforming the operation
     * @param incomingTicket
     *            the ticket given with the request
     * 
     * @param outgoingTicket
     *            the potential returned ticket, may be null
     */
    public static void logServiceAccess(int status, String serviceId,
            String incomingTicket, String outgoingTicket) {
    }

    /**
     * Standard issue error log. Whatever, whenever.
     * 
     * @param level
     *            the log level, indicates importance of error
     * @param incomingTicket
     *            the potential ticket assosiated with the operation that
     *            caused the log entry
     * @param cause
     *            Explanation of cause and effect
     */
    public static void logError(int level, String incomingTicket, String cause) {
    }
}
