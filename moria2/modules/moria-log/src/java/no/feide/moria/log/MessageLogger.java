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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * @author Bjørn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public final class MessageLogger extends Logger {

    private final static String LOGGER_NAME = MessageLogger.class.getName();

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
        log4j.warn(generateLogMessage(message, null, null));
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
     * Generate the final log entry to give to the underlying log api
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
        buffer.append(message != null ? "\"" + message + "\"" : "\"-\"" + System.getProperty("line.separator"));

        if (exception != null) {

            /* Capture stacktrace */
            OutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);

            exception.printStackTrace(printStream);
            printStream.flush();

            buffer.append(outputStream.toString() + System.getProperty("line.separator"));
        }
        return buffer.toString();
    }
}