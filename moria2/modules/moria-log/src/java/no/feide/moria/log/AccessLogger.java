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
 *
 */

package no.feide.moria.log;

import java.io.Serializable;

import org.apache.log4j.Logger;

/**
 * Logs system events in a strict format that may later
 * be used for usage statistics generation.
 *
 * Logging is done at log4j level WARN.  If the loglevel of
 * log4j is set above this, no log-entries will be written.
 *
 * The format of the log-lines is the following:
 * <pre>
 * [2004-04-30 17:10:19,046] "BAD USER CREDENTIALS" "no.feide.test" "demo@feide.no" "235892791" "350215527"
 *
 * [Timestamp] "STATUS" "service principal" "userid" "incoming ticket" "outgoing ticket"
 * </pre>
 *
 * @author Bjørn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public final class AccessLogger implements Serializable {

    /**
     * The name of this logger.
     * Same for all classes, so we can be redirect output to a common file.
     */
    private static final Class ACCESS_LOGGER_CLASS = AccessLogger.class;

    /**
     * Log to this logger.
     * Transient, so the class can be serialized.
     */
    private transient Logger logger = null;

    /**
     * Default constructor.
     */
    public AccessLogger() {
        this.logger = Logger.getLogger(ACCESS_LOGGER_CLASS);
    }

    /**
     * Logs user-initiated access (user interaction through the
     * web interface).
     *
     * @param status
     *            Indicates the type of event.
     * @param servicePrincipal
     *            The id of the service that is responsible for this operation.
     * @param userId
     *            The id of the user, may be null if unknown at time of event.
     * @param incomingTicketId
     *            The id of the ticket given with the request.
     * @param outgoingTicketId
     *            The id of the potentially returned ticket, may be null.
     */
    public void logUser(final AccessStatusType status, final String servicePrincipal, final String userId,
            final String incomingTicketId, final String outgoingTicketId) {
        /* Generate the log message and log it. */
        getLogger().warn(generateLogMessage(status, servicePrincipal, userId, incomingTicketId, outgoingTicketId));
    }

    /**
     * Logs service-initiated access.
     *
     * @param status
     *            Indicates the type of event.
     * @param servicePrincipal
     *            The id of the service that is performing the operation.
     * @param incomingTicketId
     *            The id of the ticket given with the request.
     * @param outgoingTicketId
     *            The id of the potentially returned ticket, may be null.
     */
    public void logService(final AccessStatusType status, final String servicePrincipal, final String incomingTicketId,
            final String outgoingTicketId) {

        /* Generate the log message and log it. */
        getLogger().warn(generateLogMessage(status, servicePrincipal, null, incomingTicketId, outgoingTicketId));
    }

    /**
     * Generates log messages in the correct format.
     *
     * @param status
     *            Indicates the type of event.
     * @param servicePrincipal
     *            The id of the service that is performing the operation.
     * @param userId
     *            The id of the user.
     * @param incomingTicketId
     *            The id of the ticket given with the request.
     * @param outgoingTicketId
     *            The id of the potentially returned ticket, may be null.
     * @return The string to be logged.
     */
    private String generateLogMessage(final AccessStatusType status, final String servicePrincipal, final String userId,
            final String incomingTicketId, final String outgoingTicketId) {

        StringBuffer buffer = new StringBuffer();

        /* Add default value "-" if variabel is null */
        buffer.append(status != null ? "\"" + status + "\" " : "\"-\" ");
        buffer.append(servicePrincipal != null ? "\"" + servicePrincipal + "\" " : "\"-\" ");
        buffer.append(userId != null ? "\"" + userId + "\" " : "\"-\" ");
        buffer.append(incomingTicketId != null ? "\"" + incomingTicketId + "\" " : "\"-\" ");
        buffer.append(outgoingTicketId != null ? "\"" + outgoingTicketId + "\" " : "\"-\" ");

        return buffer.toString();
    }

    /**
     * Returns the logger, instantiates it if not already done.
     * Private, so that nothing is able to override the formatting that is 
     * done by generateLogMessage.
     *
     * @return The logger instance of this class.
     */
    private Logger getLogger() {
        if (logger == null)
            logger = Logger.getLogger(ACCESS_LOGGER_CLASS);

        return logger;
    }
}
