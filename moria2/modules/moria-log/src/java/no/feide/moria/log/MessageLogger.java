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
import org.apache.log4j.Priority;

/**
 * Logs generic messages from the system, may include ticket id and/or
 * throwable. Supports four loglevels: Debug, Info, Warn, Critical.
 * @author Bj&oslash;rn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public final class MessageLogger
implements Serializable {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 7948139732249969470L;

    /**
     * Log to this logger. Transient so the class can be serialized.
     */
    private transient Logger logger = null;

    /** Class using this logger instance. */
    private Class callingClass;

    /** The current log level. Intitalized to 'ALL' */
    private int logLevel = Level.ALL_INT;


    /**
     * Default constructor.
     * @param callingClass
     *            The class that will use this logger instance.
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
     * Logs a message with level critical.
     * @param message
     *            The message to log.
     */
    public void logCritical(final String message) {

        if (Level.FATAL_INT >= logLevel)
            getLogger().fatal(generateLogMessage(message, null, null));
    }


    /**
     * Logs a message with level critical.
     * @param message
     *            The message to log.
     * @param throwable
     *            An throwable associated with this log entry. Message and
     *            stacktrace will be logged.
     */
    public void logCritical(final String message, final Throwable throwable) {

        if (Level.FATAL_INT >= logLevel)
            getLogger().fatal(generateLogMessage(message, null, throwable));
    }


    /**
     * Logs a message with level critical including ticket id.
     * @param message
     *            The message to log.
     * @param ticketId
     *            The ticket id associated with this log message.
     */
    public void logCritical(final String message, final String ticketId) {

        if (Level.FATAL_INT >= logLevel)
            getLogger().fatal(generateLogMessage(message, ticketId, null));
    }


    /**
     * Logs a message with level critical including ticket id.
     * @param message
     *            The message to log.
     * @param ticketId
     *            The ticket id associated with this log message.
     * @param throwable
     *            An throwable associated with this log entry. Message and
     *            stacktrace will be logged.
     */
    public void logCritical(final String message,
                            final String ticketId,
                            final Throwable throwable) {

        if (Level.FATAL_INT >= logLevel)
            getLogger().fatal(generateLogMessage(message, ticketId, throwable));
    }


    /**
     * Logs a message with level warn.
     * @param message
     *            The message to log.
     */
    public void logWarn(final String message) {

        if (Level.WARN_INT >= logLevel)
            getLogger().warn(generateLogMessage(message, null, null));
    }


    /**
     * Logs a message with level warn.
     * @param message
     *            The message to log.
     * @param throwable
     *            An throwable associated with this log entry. Message and
     *            stacktrace will be logged.
     */
    public void logWarn(final String message, final Throwable throwable) {

        if (Level.WARN_INT >= logLevel)
            getLogger().warn(generateLogMessage(message, null, throwable));
    }


    /**
     * Logs a message with level warn including ticket id.
     * @param message
     *            The message to log.
     * @param ticketId
     *            The ticket id associated with this log message.
     */
    public void logWarn(final String message, final String ticketId) {

        if (Level.WARN_INT >= logLevel)
            getLogger().warn(generateLogMessage(message, ticketId, null));
    }


    /**
     * Logs a message with level warn including ticket id.
     * @param message
     *            The message to log.
     * @param ticketId
     *            The ticket id associated with this log message.
     * @param throwable
     *            An throwable associated with this log entry. Message and
     *            stacktrace will be logged.
     */
    public void logWarn(final String message,
                        final String ticketId,
                        final Throwable throwable) {

        if (Level.WARN_INT >= logLevel)
            getLogger().warn(generateLogMessage(message, ticketId, throwable));
    }


    /**
     * Logs a message with level info.
     * @param message
     *            The message to log.
     */
    public void logInfo(final String message) {

        if (Level.INFO_INT >= logLevel)
            getLogger().info(generateLogMessage(message, null, null));
    }


    /**
     * Logs a message with level info.
     * @param message
     *            The message to log.
     * @param throwable
     *            An throwable associated with this log entry. Message and
     *            stacktrace will be logged.
     */
    public void logInfo(final String message, final Throwable throwable) {

        if (Level.INFO_INT >= logLevel)
            getLogger().info(generateLogMessage(message, null, throwable));
    }


    /**
     * Logs a message with level info including ticket id.
     * @param message
     *            The message to log.
     * @param ticketId
     *            The ticket id associated with this log message.
     */
    public void logInfo(final String message, final String ticketId) {

        if (Level.INFO_INT >= logLevel)
            getLogger().info(generateLogMessage(message, ticketId, null));
    }


    /**
     * Logs a message with level info including ticket id.
     * @param message
     *            The message to log.
     * @param ticketId
     *            The ticket id associated with this log message.
     * @param throwable
     *            An throwable associated with this log entry. Message and
     *            stacktrace will be logged.
     */
    public void logInfo(final String message,
                        final String ticketId,
                        final Throwable throwable) {

        if (Level.INFO_INT >= logLevel)
            getLogger().info(generateLogMessage(message, ticketId, throwable));
    }


    /**
     * Logs a message with level debug.
     * @param message
     *            The message to log.
     */
    public void logDebug(final String message) {

        if (Level.DEBUG_INT >= logLevel)
            getLogger().debug(generateLogMessage(message, null, null));
    }


    /**
     * Logs a message with level debug.
     * @param message
     *            The message to log.
     * @param throwable
     *            An throwable associated with this log entry. Message and
     *            stacktrace will be logged.
     */
    public void logDebug(final String message, final Throwable throwable) {

        if (Level.DEBUG_INT >= logLevel)
            getLogger().debug(generateLogMessage(message, null, throwable));
    }


    /**
     * Logs a message with level debug including ticket id.
     * @param message
     *            The message to log.
     * @param ticketId
     *            The ticket id associated with this log message.
     */
    public void logDebug(final String message, final String ticketId) {

        if (Level.DEBUG_INT >= logLevel)
            getLogger().debug(generateLogMessage(message, ticketId, null));
    }


    /**
     * Logs a message with level debug including ticket id.
     * @param message
     *            The message to log.
     * @param ticketId
     *            The ticket id associated with this log message.
     * @param throwable
     *            An throwable associated with this log entry. Message and
     *            stacktrace will be logged.
     */
    public void logDebug(final String message,
                         final String ticketId,
                         final Throwable throwable) {

        if (Level.DEBUG_INT >= logLevel)
            getLogger().debug(generateLogMessage(message, ticketId, throwable));
    }


    /**
     * Generates the final log entry to give to the underlying log api.
     * @param message
     *            The message to log.
     * @param ticketId
     *            A ticket id. May be null.
     * @param throwable
     *            Throwable to get stacktrace from. May be null.
     * @return The final log string.
     */
    private String generateLogMessage(final String message,
                                      final String ticketId,
                                      final Throwable throwable) {

        StringBuffer buffer = new StringBuffer();

        /* Add default value '-' if variabel is null */
        buffer.append(ticketId != null ? "[" + ticketId + "] " : "[-] ");
        buffer.append(message != null ? "\"" + message + "\"" : "\"-\"");

        if (throwable != null) {
            /* Capture stacktrace */
            OutputStream outputStream = new ByteArrayOutputStream();
            PrintStream printStream = new PrintStream(outputStream);

            throwable.printStackTrace(printStream);
            printStream.flush();

            buffer.append(System.getProperty("line.separator") + outputStream.toString());
        }
        return buffer.toString();
    }


    /**
     * Returns the logger, instantiates it if not already done. Private, so that
     * nothing is able to override the formatting that is done by
     * generateLogMessage.
     * @return The logger instance of this class.
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


    /**
     * Checks whether the actual log level would generate log output for a given
     * log level.
     * @param level
     *            The desired level to check against.
     * @return <code>true</code> if log output for the given level would
     *         result, otherwise <code>false</code>.
     */
    public boolean isEnabledFor(final Priority level) {
        
        if (logger == null)
            return false;
        return logger.isEnabledFor(level);

    }

}
