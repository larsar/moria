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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Test class for MoriaTicket
 * 
 * @author Bjørn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public class MoriaTicketTest extends TestCase {

    String id1;
    String id2;
    String principal1;
    String principal2;

    public MoriaTicketTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(MoriaTicketTest.class);
    }

    public void setUp() {
        /* Property needed by the RandomId class */
        if (System.getProperty("no.feide.moria.store.randomid.nodeid") == null)
            System.setProperty("no.feide.moria.store.randomid.nodeid", "no1");

        id1 = MoriaTicket.newId();
        id2 = MoriaTicket.newId();
        principal1 = "no.feide.test1";
        principal2 = "no.feide.test2";
    }

    /**
     * Verify that non of the tickets are equal
     */
    public void testTicketTypes() {

        /* Check every combination of tickets */
        assertTrue(MoriaTicket.LOGIN_TICKET != MoriaTicket.SERVICE_TICKET);
        assertTrue(MoriaTicket.LOGIN_TICKET != MoriaTicket.SSO_TICKET);
        assertTrue(MoriaTicket.LOGIN_TICKET != MoriaTicket.TICKET_GRANTING_TICKET);
        assertTrue(MoriaTicket.LOGIN_TICKET != MoriaTicket.PROXY_TICKET);
        assertTrue(MoriaTicket.SERVICE_TICKET != MoriaTicket.SSO_TICKET);
        assertTrue(MoriaTicket.SERVICE_TICKET != MoriaTicket.TICKET_GRANTING_TICKET);
        assertTrue(MoriaTicket.SERVICE_TICKET != MoriaTicket.PROXY_TICKET);
        assertTrue(MoriaTicket.SSO_TICKET != MoriaTicket.TICKET_GRANTING_TICKET);
        assertTrue(MoriaTicket.SSO_TICKET != MoriaTicket.PROXY_TICKET);
        assertTrue(MoriaTicket.TICKET_GRANTING_TICKET != MoriaTicket.PROXY_TICKET);
    }

    /**
     * Test the constructor. Should throw exceptions for invalid arguments.
     */
    public void testConstructor()
        throws IllegalArgumentException, InvalidTicketException, InterruptedException {

        /* Test for illegal id */
        try {
            MoriaTicket ticket = new MoriaTicket(null, MoriaTicket.LOGIN_TICKET, principal1, 5);
            fail("An IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException success) {
        }

        /* Test for illegal ticket */
        try {
            MoriaTicket ticket = new MoriaTicket(id1, -1, principal1, 5);
            fail("An InvalidTicketException should have been thrown");
        } catch (InvalidTicketException success) {
        }

        /* Test for illegal principal */
        try {
            MoriaTicket ticket = new MoriaTicket(id1, MoriaTicket.LOGIN_TICKET, null, 5);
            fail("An IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException success) {
        }
        
        try {
            MoriaTicket ticket = new MoriaTicket(id1, MoriaTicket.SSO_TICKET, principal1, 5);
            fail("An IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException success){
        }

        /* Test for illegal time to live */
        try {
            MoriaTicket ticket = new MoriaTicket(id1, MoriaTicket.LOGIN_TICKET, principal1, -1);
            fail("An IllegalArgumentException should have been thrown");
        } catch (IllegalArgumentException e) {
        }

        /* Test for expiry */
        MoriaTicket ticket = new MoriaTicket(id1, MoriaTicket.LOGIN_TICKET, principal1, 1);
        Thread.sleep(1001);
        assertTrue("Ticket should have expired", ticket.hasExpired());
    }

    /**
     * Tests the overriden equal() method of MoriaTicket. Equality is based
     * soley on the id field of the object.
     * 
     * @throws InvalidTicketException thrown by MoriaTicket constructors
     */
    public void testEquality() throws InvalidTicketException {

        /*
         * Same type and principal, different id. Should result in non-equal
         * tickets.
         */
        assertFalse(
            "Tickets supposed to have different id and not be equal",
            new MoriaTicket(id1, MoriaTicket.LOGIN_TICKET, principal1, 5).equals(
                new MoriaTicket(id2, MoriaTicket.LOGIN_TICKET, principal1, 5)));

        /* Same id, different type and principal. Should result in equality. */
        assertEquals(
            "Tickets have same id and should be considered equal",
            new MoriaTicket(id2, MoriaTicket.TICKET_GRANTING_TICKET, principal1, 5),
            new MoriaTicket(id2, MoriaTicket.SSO_TICKET, null, 5));

    }

    /**
     * Test if the values given in the constructor are returned correctly.
     * 
     * @throws IllegalArgumentException
     * @throws InvalidTicketException
     */
    public void testGetters() throws IllegalArgumentException, InvalidTicketException {
        MoriaTicket ticket = new MoriaTicket(id2, MoriaTicket.LOGIN_TICKET, principal2, 5);

        String errorMsg = "The value given at object construction does not match the returned value";
        assertEquals(errorMsg, id2, ticket.getTicketId());
        assertEquals(errorMsg, MoriaTicket.LOGIN_TICKET, ticket.getTicketType());
        assertEquals(errorMsg, principal2, ticket.getServicePrincipal());
    }
}
