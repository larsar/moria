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
 */

package no.feide.moria.store;

import java.util.Date;
import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test class for MoriaTicket
 *
 * @author Bj&oslash;rn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public class MoriaTicketTest extends TestCase {
    
    /** Dummy organization identifier. */ 
    private final String dummyOrg = "dummyOrg";

    String id1;

    String id2;

    String principal1;

    String principal2;

    MoriaAuthnAttempt authnAttempt;

    CachedUserData cachedUserData;

    String urlPrefix;

    String urlPostfix;

    public MoriaTicketTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(MoriaTicketTest.class);
    }

    public void setUp() {
        /* Property needed by the RandomId class */
        String nodeIdPropertyName = "no.feide.moria.store.nodeid";

        if (System.getProperty(nodeIdPropertyName) == null)
            fail(nodeIdPropertyName + " must be set.");

        id1 = MoriaTicket.newId();
        id2 = MoriaTicket.newId();
        principal1 = "no.feide.test1";
        principal2 = "no.feide.test2";

        urlPrefix = "http://localhost:8080/mellon/";
        urlPostfix = "/Demo";

        String[] requestAttrs = new String[] {"a", "b", "c"};

        HashMap cachedUserDataMap = new HashMap();
        cachedUserDataMap.put("a", "d");
        cachedUserDataMap.put("b", "e");
        cachedUserDataMap.put("c", "f");

        authnAttempt = new MoriaAuthnAttempt(requestAttrs, urlPrefix, urlPostfix, false, principal1);
        cachedUserData = new CachedUserData(cachedUserDataMap);
    }

    /**
     * Verify that non of the tickets are equal
     */
    public void testTicketTypes() {

        /* Check every combination of tickets */
        assertTrue(MoriaTicketType.LOGIN_TICKET != MoriaTicketType.SERVICE_TICKET);
        assertTrue(MoriaTicketType.LOGIN_TICKET != MoriaTicketType.SSO_TICKET);
        assertTrue(MoriaTicketType.LOGIN_TICKET != MoriaTicketType.TICKET_GRANTING_TICKET);
        assertTrue(MoriaTicketType.LOGIN_TICKET != MoriaTicketType.PROXY_TICKET);
        assertTrue(MoriaTicketType.SERVICE_TICKET != MoriaTicketType.SSO_TICKET);
        assertTrue(MoriaTicketType.SERVICE_TICKET != MoriaTicketType.TICKET_GRANTING_TICKET);
        assertTrue(MoriaTicketType.SERVICE_TICKET != MoriaTicketType.PROXY_TICKET);
        assertTrue(MoriaTicketType.SSO_TICKET != MoriaTicketType.TICKET_GRANTING_TICKET);
        assertTrue(MoriaTicketType.SSO_TICKET != MoriaTicketType.PROXY_TICKET);
        assertTrue(MoriaTicketType.TICKET_GRANTING_TICKET != MoriaTicketType.PROXY_TICKET);
    }

    /**
     * Test the constructor. Should throw exceptions for invalid arguments.
     */
    public void testConstructor()
            throws IllegalArgumentException, InvalidTicketException, InterruptedException {

        final long now = new Date().getTime();
        final Long expiryTime = new Long(now + 10000);
        /* Test for illegal id. */
        try {
            MoriaTicket ticket = new MoriaTicket(null, MoriaTicketType.LOGIN_TICKET, principal1, expiryTime, authnAttempt, dummyOrg);
            fail("An IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException success) {
        }

        try {
            MoriaTicket ticket = new MoriaTicket("", MoriaTicketType.LOGIN_TICKET, principal1, expiryTime, authnAttempt, dummyOrg);
            fail("An IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException success) {
        }

        /* Test for illegal ticket. */
        try {
            MoriaTicket ticket = new MoriaTicket(id1, null, principal1, expiryTime, authnAttempt, dummyOrg);
            fail("An IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException success) {
        }

        /* Test for illegal principal. */
        try {
            MoriaTicket ticket = new MoriaTicket(id1, MoriaTicketType.LOGIN_TICKET, null, expiryTime, authnAttempt, dummyOrg);
            fail("An IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException success) {
        }

        try {
            MoriaTicket ticket = new MoriaTicket(id1, MoriaTicketType.LOGIN_TICKET, "", expiryTime, authnAttempt, dummyOrg);
            fail("An IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException success) {
        }

        /* SSO must have null principal. */
        try {
            MoriaTicket ticket = new MoriaTicket(id1, MoriaTicketType.SSO_TICKET, principal1, expiryTime, cachedUserData, dummyOrg);
            fail("An IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException success) {
        }

        /* Test for illegal time to live. */
        try {
            MoriaTicket ticket = new MoriaTicket(id1, MoriaTicketType.TICKET_GRANTING_TICKET, principal1, null, cachedUserData, dummyOrg);
            fail("An IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException success) {
        }

        try {
            MoriaTicket ticket = new MoriaTicket(id1, MoriaTicketType.PROXY_TICKET, principal1, new Long(-1), cachedUserData, dummyOrg);
            fail("An IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException success) {
        }

        /* Test for expiry. */
        {
            MoriaTicket ticket = new MoriaTicket(id1, MoriaTicketType.LOGIN_TICKET, principal1, new Long(new Date().getTime() + 1000), authnAttempt, dummyOrg);
            Thread.sleep(1500);
            assertTrue("Ticket should have expired", ticket.hasExpired());
        }

        /* Test data object type check. */

        {
            MoriaTicket ticket = new MoriaTicket(id1, MoriaTicketType.LOGIN_TICKET, principal1, expiryTime, null, dummyOrg);
        }

        try {
            MoriaTicket ticket = new MoriaTicket(id1, MoriaTicketType.LOGIN_TICKET, principal1, expiryTime, cachedUserData, dummyOrg);
            fail("An IllegalArgumentException should have been thrown. Login ticket can't have cachedUserData.");
        } catch (IllegalArgumentException success) {
        }
        {
            MoriaTicket ticket = new MoriaTicket(id1, MoriaTicketType.LOGIN_TICKET, principal1, expiryTime, authnAttempt, dummyOrg);
        }
        try {
            MoriaTicket ticket = new MoriaTicket(id1, MoriaTicketType.SERVICE_TICKET, principal1, expiryTime, cachedUserData, dummyOrg);
            fail("An IllegalArgumentException should have been thrown. Service ticket can't have cachedUserData.");
        } catch (IllegalArgumentException success) {
        }
        {
            MoriaTicket ticket = new MoriaTicket(id1, MoriaTicketType.SERVICE_TICKET, principal1, expiryTime, authnAttempt, dummyOrg);
        }

        try {
            MoriaTicket ticket = new MoriaTicket(id1, MoriaTicketType.SSO_TICKET, null, expiryTime, authnAttempt, dummyOrg);
            fail("An IllegalArgumentException should have been thrown. SSO ticket can't have authnAttempt");
        } catch (IllegalArgumentException success) {
        }
        {
            MoriaTicket ticket = new MoriaTicket(id1, MoriaTicketType.SSO_TICKET, null, expiryTime, cachedUserData, dummyOrg);
        }

        try {
            MoriaTicket ticket = new MoriaTicket(id1, MoriaTicketType.TICKET_GRANTING_TICKET, principal1, expiryTime, authnAttempt, dummyOrg);
            fail("An IllegalArgumentException should have been thrown. TG ticket can't have authnAttempt");
        } catch (IllegalArgumentException success) {
        }
        {
            MoriaTicket ticket = new MoriaTicket(id1, MoriaTicketType.TICKET_GRANTING_TICKET, principal1, expiryTime, cachedUserData, dummyOrg);
        }

        try {
            MoriaTicket ticket = new MoriaTicket(id1, MoriaTicketType.PROXY_TICKET, principal1, expiryTime, authnAttempt, dummyOrg);
            fail("An IllegalArgumentException should have been thrown. PROXY ticket can't have authnAttempt");
        } catch (IllegalArgumentException success) {
        }
        {
            MoriaTicket ticket = new MoriaTicket(id1, MoriaTicketType.PROXY_TICKET, principal1, expiryTime, cachedUserData, dummyOrg);
        }
    }

    /**
     * Tests the overriden equal() method of MoriaTicket. Equality is based
     * soley on the id field of the object.
     *
     * @throws InvalidTicketException thrown by MoriaTicket constructors
     */
    public void testEquality()
            throws InvalidTicketException {

        /*
         * Same type and principal, different id. Should result in non-equal
         * tickets.
         */

        final Long expiryTime = new Long(new Date().getTime() + 5000);
        assertFalse("Tickets supposed to have different id and not be equal",
                new MoriaTicket(id1, MoriaTicketType.LOGIN_TICKET, principal1, expiryTime, authnAttempt, dummyOrg).equals(
                        new MoriaTicket(id2, MoriaTicketType.LOGIN_TICKET, principal1, expiryTime, authnAttempt, dummyOrg)));

        /* Same id, different type and principal. Should result in equality. */
        assertEquals("Tickets have same id and should be considered equal", 
                new MoriaTicket(id2, MoriaTicketType.TICKET_GRANTING_TICKET, principal1, expiryTime, cachedUserData, dummyOrg),
                new MoriaTicket(id2, MoriaTicketType.SSO_TICKET, null, expiryTime, null, dummyOrg));

    }

    /**
     * Test if the values given in the constructor are returned correctly.
     *
     * @throws IllegalArgumentException
     * @throws InvalidTicketException
     */
    public void testGetters()
            throws IllegalArgumentException, InvalidTicketException {
        MoriaTicket ticket = new MoriaTicket(id2, MoriaTicketType.LOGIN_TICKET, principal2, new Long(new Date().getTime() + 5000),
                                             authnAttempt, dummyOrg);

        String errorMsg = "The value given at object construction does not match the returned value";
        assertEquals(errorMsg, id2, ticket.getTicketId());
        assertEquals(errorMsg, MoriaTicketType.LOGIN_TICKET, ticket.getTicketType());
        assertEquals(errorMsg, principal2, ticket.getServicePrincipal());
        assertEquals(errorMsg, authnAttempt, ticket.getData());
    }
}
