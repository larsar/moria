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
public class AccessLoggerTest extends TestCase {

    private AccessLogger accessLogger;

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
        return new TestSuite(AccessLoggerTest.class);
    }

    /**
     * @see junit.framework.TestCase#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();

        /* Initiate logger */
        accessLogger = new AccessLogger();

        /* Initiate the random generator */
        if (random == null) random = new Random();
    }

    /**
     * Method to test: void logUser(AccessStatusType, String, String, String, String, String)
     */
    public final void testLogUser() {
        String servicePrincipal = null;
        String userId = null;
        String userIpAddr = null;
        String incomingTicketId = null;
        String outgoingTicketId = null;

        accessLogger.logUser(null, servicePrincipal, userId, userIpAddr, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, servicePrincipal, userId, userIpAddr, incomingTicketId, outgoingTicketId);

        /* ServicePrincipal */
        servicePrincipal = "no.feide.test";

        accessLogger.logUser(null, servicePrincipal, userId, userIpAddr, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, servicePrincipal, userId, userIpAddr, incomingTicketId, outgoingTicketId);

        /* userId */
        userId = "demo@feide.no";

        accessLogger.logUser(null, null, userId, userIpAddr, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(null, servicePrincipal, userId, userIpAddr, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, null, userId, userIpAddr, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, servicePrincipal, userId, userIpAddr, incomingTicketId, outgoingTicketId);

        /* userIpAddr */
        userIpAddr = "10.3.12.75";

        accessLogger.logUser(null, null, null, userIpAddr, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(null, null, userId, userIpAddr, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(null, servicePrincipal, null, userIpAddr, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(null, servicePrincipal, userId, userIpAddr, incomingTicketId, outgoingTicketId);

        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, null, null, userIpAddr, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, null, userId, userIpAddr, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, servicePrincipal, null, userIpAddr, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, servicePrincipal, userId, userIpAddr, incomingTicketId, outgoingTicketId);

        /* incomingTicketId */
        incomingTicketId = new Integer(random.nextInt(maxRandomNumber)).toString();

        accessLogger.logUser(null, null, null, null, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(null, null, null, userIpAddr, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(null, null, userId, null, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(null, null, userId, userIpAddr, incomingTicketId, outgoingTicketId);

        accessLogger.logUser(null, servicePrincipal, null, null, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(null, servicePrincipal, null, userIpAddr, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(null, servicePrincipal, userId, null, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(null, servicePrincipal, userId, userIpAddr, incomingTicketId, outgoingTicketId);

        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, null, null, null, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, null, null, userIpAddr, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, null, userId, null, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, null, userId, userIpAddr, incomingTicketId, outgoingTicketId);

        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, servicePrincipal, null, null, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, servicePrincipal, null, userIpAddr, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, servicePrincipal, userId, null, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, servicePrincipal, userId, userIpAddr, incomingTicketId, outgoingTicketId);

        /* outgoingTicketId */
        outgoingTicketId = new Integer(random.nextInt(maxRandomNumber)).toString();

        accessLogger.logUser(null, null, null, null, null, outgoingTicketId);
        accessLogger.logUser(null, null, null, null, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(null, null, null, userIpAddr, null, outgoingTicketId);
        accessLogger.logUser(null, null, null, userIpAddr, incomingTicketId, outgoingTicketId);

        accessLogger.logUser(null, null, userId, null, null, outgoingTicketId);
        accessLogger.logUser(null, null, userId, null, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(null, null, userId, userIpAddr, null, outgoingTicketId);
        accessLogger.logUser(null, null, userId, userIpAddr, incomingTicketId, outgoingTicketId);

        accessLogger.logUser(null, servicePrincipal, null, null, null, outgoingTicketId);
        accessLogger.logUser(null, servicePrincipal, null, null, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(null, servicePrincipal, null, userIpAddr, null, outgoingTicketId);
        accessLogger.logUser(null, servicePrincipal, null, userIpAddr, incomingTicketId, outgoingTicketId);

        accessLogger.logUser(null, servicePrincipal, userId, null, null, outgoingTicketId);
        accessLogger.logUser(null, servicePrincipal, userId, null, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(null, servicePrincipal, userId, userIpAddr, null, outgoingTicketId);
        accessLogger.logUser(null, servicePrincipal, userId, userIpAddr, incomingTicketId, outgoingTicketId);

        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, null, null, null, null, outgoingTicketId);
        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, null, null, null, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, null, null, userIpAddr, null, outgoingTicketId);
        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, null, null, userIpAddr, incomingTicketId, outgoingTicketId);

        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, null, userId, null, null, outgoingTicketId);
        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, null, userId, null, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, null, userId, userIpAddr, null, outgoingTicketId);
        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, null, userId, userIpAddr, incomingTicketId, outgoingTicketId);

        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, servicePrincipal, null, null, null, outgoingTicketId);
        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, servicePrincipal, null, null, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, servicePrincipal, null, userIpAddr, null, outgoingTicketId);
        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, servicePrincipal, null, userIpAddr, incomingTicketId, outgoingTicketId);

        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, servicePrincipal, userId, null, null, outgoingTicketId);
        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, servicePrincipal, userId, null, incomingTicketId, outgoingTicketId);
        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, servicePrincipal, userId, userIpAddr, null, outgoingTicketId);
        accessLogger.logUser(AccessStatusType.BAD_USER_CREDENTIALS, servicePrincipal, userId, userIpAddr, incomingTicketId, outgoingTicketId);
    }

    /**
     * Method to test: void logService(AccessStatusType, String, String, String) 
     */
    public final void testLogService() {
        String servicePrincipal = null;
        String incomingTicketId = null;
        String outgoingTicketId = null;

        accessLogger.logService(null, servicePrincipal, incomingTicketId, outgoingTicketId);
        accessLogger.logService(AccessStatusType.BAD_SERVICE_CREDENTIALS, servicePrincipal, incomingTicketId, outgoingTicketId);

        /* servicePrincipal */
        servicePrincipal="no.feide.test";

        accessLogger.logService(null, servicePrincipal, incomingTicketId, outgoingTicketId);
        accessLogger.logService(AccessStatusType.BAD_SERVICE_CREDENTIALS, servicePrincipal, incomingTicketId, outgoingTicketId);

        /* incomingTicketId */
        incomingTicketId = new Integer(random.nextInt(maxRandomNumber)).toString();

        accessLogger.logService(null, null, incomingTicketId, outgoingTicketId);
        accessLogger.logService(null, servicePrincipal, incomingTicketId, outgoingTicketId);
        accessLogger.logService(AccessStatusType.BAD_SERVICE_CREDENTIALS, null, incomingTicketId, outgoingTicketId);
        accessLogger.logService(AccessStatusType.BAD_SERVICE_CREDENTIALS, servicePrincipal, incomingTicketId, outgoingTicketId);

        /* outgoingTicketId */
        outgoingTicketId = new Integer(random.nextInt(maxRandomNumber)).toString();

        accessLogger.logService(null, null, null, outgoingTicketId);
        accessLogger.logService(null, null, incomingTicketId, outgoingTicketId);
        accessLogger.logService(null, servicePrincipal, null, outgoingTicketId);
        accessLogger.logService(null, servicePrincipal, incomingTicketId, outgoingTicketId);

        accessLogger.logService(AccessStatusType.BAD_SERVICE_CREDENTIALS, null, null, outgoingTicketId);
        accessLogger.logService(AccessStatusType.BAD_SERVICE_CREDENTIALS, null, incomingTicketId, outgoingTicketId);
        accessLogger.logService(AccessStatusType.BAD_SERVICE_CREDENTIALS, servicePrincipal, null, outgoingTicketId);
        accessLogger.logService(AccessStatusType.BAD_SERVICE_CREDENTIALS, servicePrincipal, incomingTicketId, outgoingTicketId);
    }
}