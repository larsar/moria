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
 */

package no.feide.moria.log;

import java.util.Random;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Bjørn Ola Smievoll &lt;b.o.smievoll@conduct.no&gt;
 * @version $Revision$
 */
public class MessageLoggerTest extends TestCase {

    private MessageLogger messageLogger;

    private Random random;

    private final int maxRandomNumber = 999999999;

    /**
     * 
     * @return
     */
    public static Test suite() {
        return new TestSuite(MessageLoggerTest.class);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        /* Intiate logger */
        messageLogger = new MessageLogger(MessageLoggerTest.class);

        /* Initiate the random generator */
        if (random == null) random = new Random();
    }

    /**
     * Class to test for void logCritical(String).
     */
    public final void testLogCriticalString() {
        messageLogger.logCritical(null);
        messageLogger.logCritical("Testing logCritical(message)");
    }

    /**
     * Class to test for void logCritical(String, Exception).
     */
    public final void testLogCriticalStringException() {
        String message = null;
        Exception exception = null;
        messageLogger.logCritical(message, exception);

        message = "Testing logCritical(message, exception)";
        messageLogger.logCritical(message, exception);

        exception = new Exception("Test exception");
        messageLogger.logCritical(null, exception);

        messageLogger.logCritical(message, exception);
    }

    /**
     * Class to test for void logCritical(String, String).
     */
    public final void testLogCriticalStringString() {
        String message = null;
        String ticketId = null;
        messageLogger.logCritical(message, ticketId);

        message = "Testing logCritical(message, ticketid)";
        messageLogger.logCritical(message, ticketId);

        ticketId = new Integer(random.nextInt(maxRandomNumber)).toString();
        messageLogger.logCritical(null, ticketId);

        messageLogger.logCritical(message, ticketId);
    }

    /**
     * Class to test for void logCritical(String, String, Exception).
     */
    public final void testLogCriticalStringStringException() {
        String message = null;
        String ticketId = null;
        Exception exception = null;
        messageLogger.logCritical(message, ticketId, exception);

        message = "Testing logCritical(message, ticketid, exception)";
        messageLogger.logCritical(message, ticketId, exception);

        ticketId = new Integer(random.nextInt(maxRandomNumber)).toString();
        messageLogger.logCritical(null, ticketId, exception);
        messageLogger.logCritical(message, ticketId, exception);

        exception = new Exception("Test exception");
        messageLogger.logCritical(null, null, exception);
        messageLogger.logCritical(null, ticketId, exception);
        messageLogger.logCritical(message, null, exception);
        messageLogger.logCritical(message, ticketId, exception);
    }

    /**
     * Class to test for void logWarn(String).
     */
    public final void testLogWarnString() {
        messageLogger.logWarn(null);
        messageLogger.logWarn("Testing logWarn(message)");
    }

    /**
     * Class to test for void logWarn(String, Exception).
     */
    public final void testLogWarnStringException() {
        String message = null;
        Exception exception = null;
        messageLogger.logWarn(message, exception);

        message = "Testing logWarn(message, exception)";
        messageLogger.logWarn(message, exception);

        exception = new Exception("Test exception");
        messageLogger.logWarn(null, exception);

        messageLogger.logWarn(message, exception);
    }

    /**
     * Class to test for void logWarn(String, String).
     */
    public final void testLogWarnStringString() {
        String message = null;
        String ticketId = null;
        messageLogger.logWarn(message, ticketId);

        message = "Testing logWarn(message, ticketid)";
        messageLogger.logWarn(message, ticketId);

        ticketId = new Integer(random.nextInt(maxRandomNumber)).toString();
        messageLogger.logWarn(null, ticketId);

        messageLogger.logWarn(message, ticketId);
    }

    /**
     * Class to test for void logWarn(String, String, Exception).
     */
    public final void testLogWarnStringStringException() {
        String message = null;
        String ticketId = null;
        Exception exception = null;
        messageLogger.logWarn(message, ticketId, exception);

        message = "Testing logWarn(message, ticketid, exception)";
        messageLogger.logWarn(message, ticketId, exception);

        ticketId = new Integer(random.nextInt(maxRandomNumber)).toString();
        messageLogger.logWarn(null, ticketId, exception);
        messageLogger.logWarn(message, ticketId, exception);

        exception = new Exception("Test exception");
        messageLogger.logWarn(null, null, exception);
        messageLogger.logWarn(null, ticketId, exception);
        messageLogger.logWarn(message, null, exception);
        messageLogger.logWarn(message, ticketId, exception);
    }

    /**
     * Class to test for void logInfo(String).
     */
    public final void testLogInfoString() {
        messageLogger.logInfo(null);
        messageLogger.logInfo("Testing logInfo(message)");
    }

    /**
     * Class to test for void logInfo(String, Exception).
     */
    public final void testLogInfoStringException() {
        String message = null;
        Exception exception = null;
        messageLogger.logInfo(message, exception);

        message = "Testing logInfo(message, exception)";
        messageLogger.logInfo(message, exception);

        exception = new Exception("Test exception");
        messageLogger.logInfo(null, exception);

        messageLogger.logInfo(message, exception);
    }

    /**
     * Class to test for void logInfo(String, String).
     */
    public final void testLogInfoStringString() {
        String message = null;
        String ticketId = null;
        messageLogger.logInfo(message, ticketId);

        message = "Testing logInfo(message, ticketid)";
        messageLogger.logInfo(message, ticketId);

        ticketId = new Integer(random.nextInt(maxRandomNumber)).toString();
        messageLogger.logInfo(null, ticketId);

        messageLogger.logInfo(message, ticketId);
    }

    /**
     * Class to test for void logInfo(String, String, Exception).
     */
    public final void testLogInfoStringStringException() {
        String message = null;
        String ticketId = null;
        Exception exception = null;
        messageLogger.logInfo(message, ticketId, exception);

        message = "Testing logInfo(message, ticketid, exception)";
        messageLogger.logInfo(message, ticketId, exception);

        ticketId = new Integer(random.nextInt(maxRandomNumber)).toString();
        messageLogger.logInfo(null, ticketId, exception);
        messageLogger.logInfo(message, ticketId, exception);

        exception = new Exception("Test exception");
        messageLogger.logInfo(null, null, exception);
        messageLogger.logInfo(null, ticketId, exception);
        messageLogger.logInfo(message, null, exception);
        messageLogger.logInfo(message, ticketId, exception);
    }

    /**
     * Class to test for void logDebug(String).
     */
    public final void testLogDebugString() {
        messageLogger.logDebug(null);
        messageLogger.logDebug("Testing logDebug(message)");
    }

    /**
     * Class to test for void logDebug(String, Exception).
     */
    public final void testLogDebugStringException() {
        String message = null;
        Exception exception = null;
        messageLogger.logDebug(message, exception);

        message = "Testing logDebug(message, exception)";
        messageLogger.logDebug(message, exception);

        exception = new Exception("Test exception");
        messageLogger.logDebug(null, exception);

        messageLogger.logDebug(message, exception);
    }

    /**
     * Class to test for void logDebug(String, String).
     */
    public final void testLogDebugStringString() {
        String message = null;
        String ticketId = null;
        messageLogger.logDebug(message, ticketId);

        message = "Testing logDebug(message, ticketid)";
        messageLogger.logDebug(message, ticketId);

        ticketId = new Integer(random.nextInt(maxRandomNumber)).toString();
        messageLogger.logDebug(null, ticketId);

        messageLogger.logDebug(message, ticketId);
    }

    /**
     * Class to test for void logDebug(String, String, Exception).
     */
    public final void testLogDebugStringStringException() {
        String message = null;
        String ticketId = null;
        Exception exception = null;
        messageLogger.logDebug(message, ticketId, exception);

        message = "Testing logDebug(message, ticketid, exception)";
        messageLogger.logDebug(message, ticketId, exception);

        ticketId = new Integer(random.nextInt(maxRandomNumber)).toString();
        messageLogger.logDebug(null, ticketId, exception);
        messageLogger.logDebug(message, ticketId, exception);

        exception = new Exception("Test exception");
        messageLogger.logDebug(null, null, exception);
        messageLogger.logDebug(null, ticketId, exception);
        messageLogger.logDebug(message, null, exception);
        messageLogger.logDebug(message, ticketId, exception);
    }
}