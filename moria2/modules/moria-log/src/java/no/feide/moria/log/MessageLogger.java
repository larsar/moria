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

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Serializable;

import org.apache.log4j.Logger;
import org.apache.log4j.Level;

/**
 * Logs generic messages from the system, may include ticket id
 * and/or exception.
 *
 * Supports four loglevels: Debug, Info, Warn, Critical.
 *
 * @author Bjørn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public final class MessageLogger implements Serializable {

    /**
     * Log to this logger.
     * Transient so the class can be serialized.
     */
    private transient Logger logger = null;

    /** Class using this logger instance. */
    private Class callingClass;

    /** The current log level. Intitalized to 'ALL' */
    private int logLevel = Level.ALL_INT;

    /**
     * Default constructor.
     *
     * @param callingClass
     *            the class that will use this logger instance
     */
    public MessageLogger(final Class callingClass) {
        this.callingClass = callingClass;
        logger = Logger.getLogger(callingClass);

        /* Cache the log level for the logger. */
        Level level = logger.getEffectiveLevel();

        if (level != null)
            logLevel = level.toInt();
    }

    /**
     * Log message with level critical.
     *
     * @param message
     *            the message to log
     */
    public void logCritical(final String message) {
        if (Level.FATAL_INT >= logLevel)
            getLogger().fatal(generateLogMessage(message, null, null));
    }

    /**
     * Log message with level critical.
     *
     * @param message
     *            the message to log
     * @param exception
     *            an exception associated with this log entry. Message and
     *            stacktrace will be logged
     */
    public void logCritical(final String message, final Exception exception) {
        if (Level.FATAL_INT >= logLevel)
            getLogger().fatal(generateLogMessage(message, null, exception));
    }

    /**
     * Log message with level critical including ticket id.
     *
     * @param message
     *            the message to log
     * @param ticketId
     *            the ticketid assosiated with this log message
     */
    public void logCritical(final String message, final String ticketId) {
        if (Level.FATAL_INT >= logLevel)
            getLogger().fatal(generateLogMessage(message, ticketId, null));
    }

    /**
     * Log message with level critical including ticket id.
     *
     * @param message
     *            the message to log
     * @param ticketId
     *            the ticketid assosiated with this log message
     * @param exception
     *            an exception associated with this log entry. Message and
     *            stacktrace will be logged
     */
    public void logCritical(final String message, final String ticketId, final Exception exception) {
        if (Level.FATAL_INT >= logLevel)
            getLogger().fatal(generateLogMessage(message, ticketId, exception));
    }

    /**
     * Log message with level warn.
     *
     * @param message
     *            the message to log
     */
    public void logWarn(final String message) {
        if (Level.WARN_INT >= logLevel)
            getLogger().warn(generateLogMessage(message, null, null));
    }

    /**
     * Log message with level warn.
     *
     * @param message
     *            the message to log
     * @param exception
     *            an exception associated with this log entry. Message and
     *            stacktrace will be logged
     */
    public void logWarn(final String message, final Exception exception) {
        if (Level.WARN_INT >= logLevel)
            getLogger().warn(generateLogMessage(message, null, exception));
    }

    /**
     * Log message with level warn including ticket id.
     *
     * @param message
     *            the message to log
     * @param ticketId
     *            the ticketid assosiated with this log message
     */
    public void logWarn(final String message, final String ticketId) {
        if (Level.WARN_INT >= logLevel)
            getLogger().warn(generateLogMessage(message, ticketId, null));
    }

    /**
     * Log message with level warn including ticket id.
     *
     * @param message
     *            the message to log
     * @param ticketId
     *            the ticketid assosiated with this log message
     * @param exception
     *            an exception associated with this log entry. Message and
     *            stacktrace will be logged
     */
    public void logWarn(final String message, final String ticketId, final Exception exception) {
        if (Level.WARN_INT >= logLevel)
            getLogger().warn(generateLogMessage(message, ticketId, exception));
    }

    /**
     * Log message with level info.
     *
     * @param message
     *            the message to log
     */
    public void logInfo(final String message) {
        if (Level.INFO_INT >= logLevel)
            getLogger().info(generateLogMessage(message, null, null));
    }

    /**
     * Log message with level info.
     *
     * @param message
     *            the message to log
     * @param exception
     *            an exception associated with this log entry. Message and
     *            stacktrace will be logged
     */
    public void logInfo(final String message, final Exception exception) {
        if (Level.INFO_INT >= logLevel)
            getLogger().info(generateLogMessage(message, null, exception));
    }

    /**
     * Log message with level info including ticket id.
     *
     * @param message
     *            the message to log
     * @param ticketId
     *            the ticketid assosiated with this log message
     */
    public void logInfo(final String message, final String ticketId) {
        if (Level.INFO_INT >= logLevel)
            getLogger().info(generateLogMessage(message, ticketId, null));
    }

    /**
     * Log message with level info including ticket id.
     *
     * @param message
     *            the message to log
     * @param ticketId
     *            the ticketid assosiated with this log message
     * @param exception
     *            an exception associated with this log entry. Message and
     *            stacktrace will be logged
     */
    public void logInfo(final String message, final String ticketId, final Exception exception) {
        if (Level.INFO_INT >= logLevel)
            getLogger().info(generateLogMessage(message, ticketId, exception));
    }

    /**
     * Log message with level debug.
     *
     * @param message
     *            the message to log
     */
    public void logDebug(final String message) {
        if (Level.DEBUG_INT >= logLevel)
            getLogger().debug(generateLogMessage(message, null, null));
    }

    /**
     * Log message with level debug.
     *
     * @param message
     *            the message to log
     * @param exception
     *            an exception associated with this log entry. Message and
     *            stacktrace will be logged
     */
    public void logDebug(final String message, final Exception exception) {
        if (Level.DEBUG_INT >= logLevel)
            getLogger().debug(generateLogMessage(message, null, exception));
    }

    /**
     * Log message with level debug including ticket id.
     *
     * @param message
     *            the message to log
     * @param ticketId
     *            the ticketid assosiated with this log message
     */
    public void logDebug(final String message, final String ticketId) {
        if (Level.DEBUG_INT >= logLevel)
            getLogger().debug(generateLogMessage(message, ticketId, null));
    }

    /**
     * Log message with level debug including ticket id.
     *
     * @param message
     *            the message to log
     * @param ticketId
     *            the ticketid assosiated with this log message
     * @param exception
     *            an exception associated with this log entry. Message and
     *            stacktrace will be logged
     */
    public void logDebug(final String message, final String ticketId, final Exception exception) {
        if (Level.DEBUG_INT >= logLevel)
            getLogger().debug(generateLogMessage(message, ticketId, exception));
    }

    /**
     * Generate the final log entry to give to the underlying log api.
     *
     * @param message
     *            the message to log
     * @param ticketId
     *            a ticket id. May be null.
     * @param exception
     *            exception to get stacktrace from. May be null.
     * @return the final log string
     */
    private String generateLogMessage(final String message, final String ticketId, final Exception exception) {

        StringBuffer buffer = new StringBuffer();

        /* Add default value '-' if variabel is null */
        buffer.append(ticketId != null ? "[" + ticketId + "] " : "[-] ");
        buffer.append(message != null ? "\"" + message + "\"" : "\"-\"");

        if (exception != null) {
            /* Capture stacktrace */
            OutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);

            exception.printStackTrace(printStream);
            printStream.flush();

            buffer.append(System.getProperty("line.separator") + outputStream.toString());
        }
        return buffer.toString();
    }

    /**
     * Returns the logger, instanciates it if not already so.
     * Private so that nobody overrides the formatting that is done by
     * generateLogMessage.
     *
     * @return the logger instance of this class
     */
    private Logger getLogger() {
        if (logger == null) {
            logger = Logger.getLogger(callingClass);

            /* Cache the log level for the logger. */
            Level level = logger.getEffectiveLevel();

            if (level != null) {
                logLevel = level.toInt();
            } else {
                logLevel = Level.ALL_INT;
            }
        }
        return logger;
    }
}