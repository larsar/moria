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

    /** The logger used by the tests */
    private MessageLogger messageLogger;

    /** PRNG to generate pseudo tickets */
    private Random random;

    /** Ceiling for PRNG */
    private final int maxRandomNumber = 999999999;

    /**
     * Create a test suite of all tests in class. 
     *
     * @return a suite of all tests in class
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
     * Method to test: void logCritical(String).
     */
    public final void testLogCriticalString() {
        messageLogger.logCritical(null);
        messageLogger.logCritical("Testing logCritical(message)");
    }

    /**
     * Method to test: void logCritical(String, Exception).
     */
    public final void testLogCriticalStringException() {
        String message = null;
        Exception exception = null;

        messageLogger.logCritical(message, exception);

        /* message */
        message = "Testing logCritical(message, exception)";

        messageLogger.logCritical(message, exception);

        /* exception */
        exception = new Exception("Test exception");

        messageLogger.logCritical(null, exception);
        messageLogger.logCritical(message, exception);
    }

    /**
     * Method to test: void logCritical(String, String).
     */
    public final void testLogCriticalStringString() {
        String message = null;
        String ticketId = null;

        messageLogger.logCritical(message, ticketId);

        /* message */
        message = "Testing logCritical(message, ticketid)";

        messageLogger.logCritical(message, ticketId);

        /* ticketId */
        ticketId = new Integer(random.nextInt(maxRandomNumber)).toString();

        messageLogger.logCritical(null, ticketId);
        messageLogger.logCritical(message, ticketId);
    }

    /**
     * Method to test: void logCritical(String, String, Exception).
     */
    public final void testLogCriticalStringStringException() {
        String message = null;
        String ticketId = null;
        Exception exception = null;

        messageLogger.logCritical(message, ticketId, exception);

        /* message */
        message = "Testing logCritical(message, ticketid, exception)";

        messageLogger.logCritical(message, ticketId, exception);

        /* ticketId */
        ticketId = new Integer(random.nextInt(maxRandomNumber)).toString();

        messageLogger.logCritical(null, ticketId, exception);
        messageLogger.logCritical(message, ticketId, exception);

        /* exception */
        exception = new Exception("Test exception");

        messageLogger.logCritical(null, null, exception);
        messageLogger.logCritical(null, ticketId, exception);
        messageLogger.logCritical(message, null, exception);
        messageLogger.logCritical(message, ticketId, exception);
    }

    /**
     * Method to test: void logWarn(String).
     */
    public final void testLogWarnString() {
        messageLogger.logWarn(null);
        messageLogger.logWarn("Testing logWarn(message)");
    }

    /**
     * Method to test: void logWarn(String, Exception).
     */
    public final void testLogWarnStringException() {
        String message = null;
        Exception exception = null;

        messageLogger.logWarn(message, exception);

        /* message */
        message = "Testing logWarn(message, exception)";

        messageLogger.logWarn(message, exception);

        /* exception */
        exception = new Exception("Test exception");

        messageLogger.logWarn(null, exception);
        messageLogger.logWarn(message, exception);
    }

    /**
     * Method to test: void logWarn(String, String).
     */
    public final void testLogWarnStringString() {
        String message = null;
        String ticketId = null;

        messageLogger.logWarn(message, ticketId);

        /* message */
        message = "Testing logWarn(message, ticketid)";

        messageLogger.logWarn(message, ticketId);

        /* ticketId */
        ticketId = new Integer(random.nextInt(maxRandomNumber)).toString();

        messageLogger.logWarn(null, ticketId);
        messageLogger.logWarn(message, ticketId);
    }

    /**
     * Method to test: void logWarn(String, String, Exception).
     */
    public final void testLogWarnStringStringException() {
        String message = null;
        String ticketId = null;
        Exception exception = null;

        messageLogger.logWarn(message, ticketId, exception);

        /* message */
        message = "Testing logWarn(message, ticketid, exception)";

        messageLogger.logWarn(message, ticketId, exception);

        /* ticketId */
        ticketId = new Integer(random.nextInt(maxRandomNumber)).toString();

        messageLogger.logWarn(null, ticketId, exception);
        messageLogger.logWarn(message, ticketId, exception);

        /* exception */
        exception = new Exception("Test exception");

        messageLogger.logWarn(null, null, exception);
        messageLogger.logWarn(null, ticketId, exception);
        messageLogger.logWarn(message, null, exception);
        messageLogger.logWarn(message, ticketId, exception);
    }

    /**
     * Method to test: void logInfo(String).
     */
    public final void testLogInfoString() {
        messageLogger.logInfo(null);
        messageLogger.logInfo("Testing logInfo(message)");
    }

    /**
     * Method to test: void logInfo(String, Exception).
     */
    public final void testLogInfoStringException() {
        String message = null;
        Exception exception = null;

        messageLogger.logInfo(message, exception);

        /* message */
        message = "Testing logInfo(message, exception)";

        messageLogger.logInfo(message, exception);

        /* exception */
        exception = new Exception("Test exception");

        messageLogger.logInfo(null, exception);
        messageLogger.logInfo(message, exception);
    }

    /**
     * Method to test: void logInfo(String, String).
     */
    public final void testLogInfoStringString() {
        String message = null;
        String ticketId = null;

        messageLogger.logInfo(message, ticketId);

        /* message */
        message = "Testing logInfo(message, ticketid)";

        messageLogger.logInfo(message, ticketId);

        /* ticketId */
        ticketId = new Integer(random.nextInt(maxRandomNumber)).toString();

        messageLogger.logInfo(null, ticketId);
        messageLogger.logInfo(message, ticketId);
    }

    /**
     * Method to test: void logInfo(String, String, Exception).
     */
    public final void testLogInfoStringStringException() {
        String message = null;
        String ticketId = null;
        Exception exception = null;

        messageLogger.logInfo(message, ticketId, exception);

        /* message */
        message = "Testing logInfo(message, ticketid, exception)";

        messageLogger.logInfo(message, ticketId, exception);

        /* ticketId */
        ticketId = new Integer(random.nextInt(maxRandomNumber)).toString();

        messageLogger.logInfo(null, ticketId, exception);
        messageLogger.logInfo(message, ticketId, exception);

        /* exception */
        exception = new Exception("Test exception");

        messageLogger.logInfo(null, null, exception);
        messageLogger.logInfo(null, ticketId, exception);
        messageLogger.logInfo(message, null, exception);
        messageLogger.logInfo(message, ticketId, exception);
    }

    /**
     * Method to test: void logDebug(String).
     */
    public final void testLogDebugString() {
        messageLogger.logDebug(null);
        messageLogger.logDebug("Testing logDebug(message)");
    }

    /**
     * Method to test: void logDebug(String, Exception).
     */
    public final void testLogDebugStringException() {
        String message = null;
        Exception exception = null;

        messageLogger.logDebug(message, exception);

        /* message */
        message = "Testing logDebug(message, exception)";

        messageLogger.logDebug(message, exception);

        /* exception */
        exception = new Exception("Test exception");

        messageLogger.logDebug(null, exception);
        messageLogger.logDebug(message, exception);
    }

    /**
     * Method to test: void logDebug(String, String).
     */
    public final void testLogDebugStringString() {
        String message = null;
        String ticketId = null;

        messageLogger.logDebug(message, ticketId);

        /* message */
        message = "Testing logDebug(message, ticketid)";

        messageLogger.logDebug(message, ticketId);

        /* ticketId */
        ticketId = new Integer(random.nextInt(maxRandomNumber)).toString();

        messageLogger.logDebug(null, ticketId);
        messageLogger.logDebug(message, ticketId);
    }

    /**
     * Method to test: void logDebug(String, String, Exception).
     */
    public final void testLogDebugStringStringException() {
        String message = null;
        String ticketId = null;
        Exception exception = null;
        messageLogger.logDebug(message, ticketId, exception);

        /* message */
        message = "Testing logDebug(message, ticketid, exception)";

        messageLogger.logDebug(message, ticketId, exception);

        /* ticketId */
        ticketId = new Integer(random.nextInt(maxRandomNumber)).toString();

        messageLogger.logDebug(null, ticketId, exception);
        messageLogger.logDebug(message, ticketId, exception);

        /* exception */
        exception = new Exception("Test exception");

        messageLogger.logDebug(null, null, exception);
        messageLogger.logDebug(null, ticketId, exception);
        messageLogger.logDebug(message, null, exception);
        messageLogger.logDebug(message, ticketId, exception);
    }
}