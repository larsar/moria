/*
 * Copyright (c) 2004 UNINETT FAS A/S
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

/**
 * @author Bjørn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public final class AccessLogger extends Logger {

    /**
     * Default private construtor
     */
    private AccessLogger() {

    }

    /*
     * Status types used by this logger
     */

    /** Used when user gives wrong username or password */
    public static final int STATUS_BAD_USER_CREDENTIALS = 0x01;

    /** Used when service gives wrong username or password */
    public static final int STATUS_BAD_SERVICE_CREDENTIALS = 0x02;

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
     * @param incomingTicketId
     *            the id of the ticket given with the request
     * @param outgoingTicketId
     *            the id of the potentially returned ticket, may be null
     */
    public static void logUser(final int status, final String serviceId,
            final String userId, final String userIpAddr,
            final String incomingTicketId, final String outgoingTicketId) {
    }

    /**
     * Used for logging service-inititated access
     * 
     * @param status
     *            indicates the type of event. Use STATUS_* constants defined
     *            in this class
     * @param serviceId
     *            the id of the service that is peforming the operation
     * @param incomingTicketId
     *            the id of the ticket given with the request
     * @param outgoingTicketId
     *            the id of the potentially returned ticket, may be null
     */
    public static void logService(final int status, final String serviceId,
            final String incomingTicketId, final String outgoingTicketId) {
    }
}
