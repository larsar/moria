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

/**
 * @author Bjørn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public final class MessageLogger extends Logger {

    /**
     * Default private constructor
     *  
     */
    private MessageLogger() {
    }

    /**
     * Log message with level critical
     * 
     * @param message
     *            the message to log
     */
    public static void logCritical(final String message) {
        log4j.fatal(generateLogMessage(message, null, null));
    }

    /**
     * Log message with level critical
     * 
     * @param message
     *            the message to log
     * @param exception
     *            an exception associated with this log entry. Message and
     *            stacktrace will be logged
     */
    public static void logCritical(final String message, final Exception exception) {
        log4j.fatal(generateLogMessage(message, null, exception));
    }

    /**
     * Log message with level critical including ticket id
     * 
     * @param message
     *            the message to log
     * @param ticketId
     *            the ticketid assosiated with this log message
     */
    public static void logCritical(final String message, final String ticketId) {
        log4j.fatal(generateLogMessage(message, ticketId, null));
    }

    /**
     * Log message with level critical including ticket id
     * 
     * @param message
     *            the message to log
     * @param ticketId
     *            the ticketid assosiated with this log message
     * @param exception
     *            an exception associated with this log entry. Message and
     *            stacktrace will be logged
     */
    public static void logCritical(final String message, final String ticketId, final Exception exception) {
        log4j.fatal(generateLogMessage(message, ticketId, exception));
    }

    /**
     * Log message with level warn
     * 
     * @param message
     *            the message to log
     */
    public static void logWarn(final String message) {
        log4j.warn(generateLogMessage(message, null ,null));
    }

    /**
     * Log message with level warn
     * 
     * @param message
     *            the message to log
     * @param exception
     *            an exception associated with this log entry. Message and
     *            stacktrace will be logged
     */
    public static void logWarn(final String message, final Exception exception) {
        log4j.warn(generateLogMessage(message, null, exception));
    }

    /**
     * Log message with level warn including ticket id
     * 
     * @param message
     *            the message to log
     * @param ticketId
     *            the ticketid assosiated with this log message
     */
    public static void logWarn(final String message, final String ticketId) {
        log4j.warn(generateLogMessage(message, ticketId, null));
    }

    /**
     * Log message with level warn including ticket id
     * 
     * @param message
     *            the message to log
     * @param ticketId
     *            the ticketid assosiated with this log message
     * @param exception
     *            an exception associated with this log entry. Message and
     *            stacktrace will be logged
     */
    public static void logWarn(final String message, final String ticketId, final Exception exception) {
        log4j.warn(generateLogMessage(message, ticketId, exception));
    }

    /**
     * Log message with level info
     * 
     * @param message
     *            the message to log
     */
    public static void logInfo(final String message) {
        log4j.info(generateLogMessage(message, null, null));
    }

    /**
     * Log message with level info
     * 
     * @param message
     *            the message to log
     * @param exception
     *            an exception associated with this log entry. Message and
     *            stacktrace will be logged
     */
    public static void logInfo(final String message, final Exception exception) {
        log4j.info(generateLogMessage(message, null, exception));
    }

    /**
     * Log message with level info including ticket id
     * 
     * @param message
     *            the message to log
     * @param ticketId
     *            the ticketid assosiated with this log message
     */
    public static void logInfo(final String message, final String ticketId) {
        log4j.info(generateLogMessage(message, ticketId, null));
    }

    /**
     * Log message with level info including ticket id
     * 
     * @param message
     *            the message to log
     * @param ticketId
     *            the ticketid assosiated with this log message
     * @param exception
     *            an exception associated with this log entry. Message and
     *            stacktrace will be logged
     */
    public static void logInfo(final String message, final String ticketId, final Exception exception) {
        log4j.info(generateLogMessage(message, ticketId, exception));
    }

    /**
     * Log message with level debug
     * 
     * @param message
     *            the message to log
     */
    public static void logDebug(final String message) {
        log4j.debug(generateLogMessage(message, null, null));
    }

    /**
     * Log message with level debug
     * 
     * @param message
     *            the message to log
     * @param exception
     *            an exception associated with this log entry. Message and
     *            stacktrace will be logged
     */
    public static void logDebug(final String message, final Exception exception) {
        log4j.debug(generateLogMessage(message, null, exception));
    }

    /**
     * Log message with level debug including ticket id
     * 
     * @param message
     *            the message to log
     * @param ticketId
     *            the ticketid assosiated with this log message
     */
    public static void logDebug(final String message, final String ticketId) {
        log4j.debug(generateLogMessage(message, ticketId, null));
    }

    /**
     * Log message with level debug including ticket id
     * 
     * @param message
     *            the message to log
     * @param ticketId
     *            the ticketid assosiated with this log message
     * @param exception
     *            an exception associated with this log entry. Message and
     *            stacktrace will be logged
     */
    public static void logDebug(final String message, final String ticketId, final Exception exception) {
        log4j.debug(generateLogMessage(message, ticketId, exception));
    }

    /**
     * Generate the final string to give to the underlying log api.
     * 
     * @param message
     *            the message to log
     * @param ticketId
     *            a ticket id. May be null.
     * @param exception
     *            exception to get stacktrace from. May be null.
     * @return the final log string
     */
    private static String generateLogMessage(final String message, final String ticketId, final Exception exception)
            throws IllegalArgumentException {

        StringBuffer buffer = new StringBuffer();

        buffer.append(ticketId != null ? "[" + ticketId + "] " : "[-] ");
        buffer.append(message != null ? "\"message\"" : "\"-\"");

        if (exception != null) {
            /*
             * Append message int exception on own line.
             */
            buffer.append(System.getProperty("line.separator"));
            buffer.append(exception.getMessage());
            buffer.append(System.getProperty("line.separator"));

            /*
             * Append string representation of every element in the exception
             * stack.
             */
            StackTraceElement[] stackTrace = exception.getStackTrace();

            for (int i = 0; i < stackTrace.length; i++) {
                buffer.append(stackTrace[i].toString());
                buffer.append(System.getProperty("line.seperator"));
            }
        }
        return buffer.toString();
    }
}