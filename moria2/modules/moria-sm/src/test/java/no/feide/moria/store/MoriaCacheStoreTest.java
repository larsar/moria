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
 */

package no.feide.moria.store;

import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import no.feide.moria.store.CachedUserData;
import no.feide.moria.store.InvalidTicketException;
import no.feide.moria.store.MoriaAuthnAttempt;
import no.feide.moria.store.MoriaTicket;

/**
 * Test class for classes implementing the MoriaStore interface.
 *
 * @author Bjørn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public class MoriaCacheStoreTest extends TestCase {

    MoriaCacheStore store;

    String[] attributes = {"eduPersonOrgDN", "eduPersonAffiliation"};

    String prefix = "http://demo.feide.no/mellon-demo/Demo?moriaId=";

    String postfix = "";

    String principal = "no.feide.demo";

    String targetPrincipal = "no.feide.demo-target";

    boolean forceAuthn = true;

    public static Test suite() {
        return new TestSuite(MoriaCacheStoreTest.class);
    }

    public void setUp() {
        /* Property needed by the RandomId class */
        if (System.getProperty("no.feide.moria.store.randomid.nodeid") == null)
            System.setProperty("no.feide.moria.store.randomid.nodeid", "no1");

        store = new MoriaCacheStore();
    }

    /**
     * Test creation + retrival and removal.
     *
     * @throws InvalidTicketException
     */
    public void testCreateAuthnAttempt() throws InvalidTicketException {

        String loginTicketId = store.createAuthnAttempt(attributes, prefix, postfix, forceAuthn, principal);
        MoriaTicket loginTicket = store.getTicketFromStore(MoriaTicketType.LOGIN_TICKET, loginTicketId);

        assertNotNull("Login ticket should not be null", loginTicket);
        assertEquals("", principal, loginTicket.getServicePrincipal());

        MoriaAuthnAttempt authnAttempt = store.getAuthnAttempt(loginTicketId, false);

        assertNotNull("Authentication attempt should not be null", authnAttempt);

        assertTrue("Attribute array does not match", compareStringArrays(attributes, authnAttempt.getRequestedAttributes()));
        assertEquals("URL prefix does not match", prefix, authnAttempt.getReturnURLPrefix());
        assertEquals("URL postfix does not match", postfix, authnAttempt.getReturnURLPostfix());
        assertEquals("Forced authenetication does not match", forceAuthn, authnAttempt.isForceInterativeAuthentication());

        try {
            loginTicketId = store.createAuthnAttempt(null, prefix, postfix, forceAuthn, principal);
            fail("Null value for attributes did not cause exception to be thrown");
        }
        catch (IllegalArgumentException success) {
        }

        /* Emtpy string array should work */
        loginTicketId = store.createAuthnAttempt(new String[] {null}, prefix, postfix, forceAuthn, principal);
        store.getAuthnAttempt(loginTicketId, false);

        try {
            loginTicketId = store.createAuthnAttempt(attributes, null, postfix, forceAuthn, principal);
            fail("Null value for prefix did not cause exception to be thrown");
        }
        catch (IllegalArgumentException success) {
        }

        try {
            loginTicketId = store.createAuthnAttempt(attributes, prefix, null, forceAuthn, principal);
            fail("Null value for postfix did not cause exception to be thrown");
        }
        catch (IllegalArgumentException success) {
        }

        try {
            loginTicketId = store.createAuthnAttempt(attributes, prefix, postfix, forceAuthn, null);
            fail("Null value for pricipal did not cause exception to be thrown");
        }
        catch (IllegalArgumentException success) {
        }

        try {
            loginTicketId = store.createAuthnAttempt(attributes, prefix, postfix, forceAuthn, "");
            fail("Empty principal did not cause exception to be thrown");
        }
        catch (IllegalArgumentException success) {
        }
    }

    // TODO: getTicket() test
    public void testGetTicket() {
        /* Illegal parameters */
        try {
            store.getTicketFromStore(null, null);
            fail("IllegalArgumentException should be raised, null value");
        }
        catch (IllegalArgumentException success) {
        }

        try {
            store.getTicketFromStore(null, "");
            fail("IllegalArgumentException should be raised, empty string");
        }
        catch (IllegalArgumentException success) {
        }

        try {
            store.getTicketFromStore(MoriaTicketType.LOGIN_TICKET, null);
            fail("IllegalArgumentException should be raised, empty string");
        }
        catch (IllegalArgumentException success) {
        }

        try {
            store.getTicketFromStore(MoriaTicketType.LOGIN_TICKET, "");
            fail("IllegalArgumentException should be raised, empty string");
        }
        catch (IllegalArgumentException success) {
        }

        /* Non-existing ticket */
        MoriaTicket ticket = store.getTicketFromStore(MoriaTicketType.LOGIN_TICKET, "doesNotExist");
        assertNull("Ticket should not be generated", ticket);

        /* Login ticket */
        String loginTicketId = store.createAuthnAttempt(attributes, prefix, postfix, forceAuthn, principal);
        MoriaTicket loginTicket = store.getTicketFromStore(MoriaTicketType.LOGIN_TICKET, loginTicketId);
        assertNotNull("Ticket should not be null", loginTicket);
        assertEquals("Ticket ID differs", loginTicketId, loginTicket.getTicketId());
        assertEquals("Ticket type differs", MoriaTicketType.LOGIN_TICKET, loginTicket.getTicketType());

        // TODO: test for all kinds of tickets
    }

    public void testGetAuthnAttempt() throws InvalidTicketException {
        /* Illegal arguments */
        try {
            store.getAuthnAttempt(null, false);
            fail("IllegalArgumentException should be raised, null value.");
        }
        catch (IllegalArgumentException success) {
        }
        try {
            store.getAuthnAttempt("", false);
            fail("IllegalArgumentException should be raised, empty string.");
        }
        catch (IllegalArgumentException success) {
        }

        /* Wrong ticket */
        assertNull("Invalid ticket, authnAttempt should be null", store.getAuthnAttempt("doesNotExist", false));

        String ticketId;

        /* Wrong ticket type */
        // TODO: Create other event+ticket, test should fail with InvalidTicketException
        /* Normal use */
        ticketId = store.createAuthnAttempt(attributes, prefix, postfix, forceAuthn, principal);
        MoriaAuthnAttempt authnAttempt = store.getAuthnAttempt(ticketId, true);
        assertNotNull("authnAttempt should not be null", authnAttempt);
        String[] actualAttributes = authnAttempt.getRequestedAttributes();

        assertTrue("attributes differs", compareStringArrays(attributes, actualAttributes));
        assertEquals("prefix should be equal", prefix, authnAttempt.getReturnURLPrefix());
        assertEquals("postfix should be equal", postfix, authnAttempt.getReturnURLPostfix());
        assertEquals("forceAuthn should be equal", forceAuthn, authnAttempt.isForceInterativeAuthentication());

        /* Keep, no keep */
        authnAttempt = store.getAuthnAttempt(ticketId, false);
        assertNotNull("authnAttempt should not be null (cached)", authnAttempt);
        authnAttempt = store.getAuthnAttempt(ticketId, true);
        assertNull("authnAttempt should be null (removed from cache)", authnAttempt);
    }

    /**
     * Test cacheUserData method.
     *
     * @throws InvalidTicketException
     * @see MoriaCacheStore#cacheUserData(java.util.HashMap)
     */
    public void testCacheUserData() throws InvalidTicketException {
        HashMap cachedAttrs = new HashMap();

        /* Invalid parameters */
        try {
            store.cacheUserData(null);
            fail("IllegalArgumentException should be raised, null value");
        }
        catch (IllegalArgumentException success) {
        }

        cachedAttrs.put("a", "b");
        cachedAttrs.put("c", "d");

        MoriaTicket ticket = store.getTicketFromStore(MoriaTicketType.SSO_TICKET, store.cacheUserData(cachedAttrs));
        assertNotNull("SSO ticket should not be null", ticket);
        assertEquals("SSO ticket has wrong type", MoriaTicketType.SSO_TICKET, ticket.getTicketType());
    }

    /**
     * Test Service ticket creation.
     *
     * @throws InvalidTicketException
     */
    public void testServiceTicketCreation() throws InvalidTicketException {

        /* Invalid arguments */
        try {
            store.createServiceTicket(null, principal);
            fail("IllegalArgumentException should be raised, ticket is null");
        }
        catch (IllegalArgumentException success) {
        }
        try {
            store.createServiceTicket("", principal);
            fail("IllegalArgumentException should be raised, ticket is empty string");
        }
        catch (IllegalArgumentException success) {
        }
        try {
            store.createServiceTicket("foo", null);
            fail("IllegalArgumentException should be raised, principal is null");
        }
        catch (IllegalArgumentException success) {
        }
        try {
            store.createServiceTicket("foo", "");
            fail("IllegalArgumentException should be raised, principal is empty string");
        }
        catch (IllegalArgumentException success) {
        }

        String loginTicketId = store.createAuthnAttempt(attributes, prefix, postfix, forceAuthn, principal);
        String serviceTicketId = store.createServiceTicket(loginTicketId, principal);

        assertNotNull("Service ticketId should not be null", serviceTicketId);
        assertNotNull("Service ticket should not be null", store
                .getTicketFromStore(MoriaTicketType.SERVICE_TICKET, serviceTicketId));
        assertEquals("Service ticket has wrong type", MoriaTicketType.SERVICE_TICKET, store.getTicketFromStore(
                MoriaTicketType.SERVICE_TICKET, serviceTicketId).getTicketType());
    }

    /**
     * Test user data retrival.
     *
     * @throws InvalidTicketException
     */
    public void testGetUserData() throws InvalidTicketException {

        /* Invalid arguments */
        try {
            store.getUserData(null);
            fail("IllegalArgumentException should be raised, ticketId is null");
        }
        catch (IllegalArgumentException success) {
        }

        try {
            store.getUserData("");
            fail("IllegalArgumentException should be raised, ticketId is an empty string");
        }
        catch (IllegalArgumentException success) {
        }

        HashMap cachedAttrs = new HashMap();
        cachedAttrs.put("a", "b");
        cachedAttrs.put("c", "d");

        String ssoTicketId = store.cacheUserData(cachedAttrs);
        CachedUserData userData = store.getUserData(ssoTicketId);

        assertNotNull("User data should not be null", userData);
        assertEquals("Returned user data doesn't match", "b", userData.getAttributes().get("a"));
        assertEquals("Returned user data doesn't match", "d", userData.getAttributes().get("c"));

        /* Test with invalid (non-existent) ticket */

        ssoTicketId = new MoriaTicket(MoriaTicketType.SSO_TICKET, null, new Long(30)).getTicketId();
        userData = store.getUserData(ssoTicketId);
        assertNull("No user data should be null for an invalid ticket", userData);
    }

    /**
     * Test TGT creation.
     *
     * @throws InvalidTicketException
     */
    public void testCreateTicketGrantingTicket() throws InvalidTicketException {
        /* Invalid arguments */
        try {
            store.createTicketGrantingTicket(null, principal);
            fail("IllegalArgumentException should be raised, ticketId is null");
        }
        catch (IllegalArgumentException success) {
        }
        try {
            store.createTicketGrantingTicket("", principal);
            fail("IllegalArgumentException should be raised, ticketId is an empty string");
        }
        catch (IllegalArgumentException success) {
        }
        try {
            store.createTicketGrantingTicket("foo", null);
            fail("IllegalArgumentException should be raised, principal is null");
        }
        catch (IllegalArgumentException success) {
        }
        try {
            store.createTicketGrantingTicket("foo", "");
            fail("IllegalArgumentException should be raised, principal is an empty string");
        }
        catch (IllegalArgumentException success) {
        }

        HashMap cachedAttrs = new HashMap();
        cachedAttrs.put("a", "b");
        cachedAttrs.put("c", "d");

        /* Normal use */
        String ssoTicketId = store.cacheUserData(cachedAttrs);
        String tgTicketId = store.createTicketGrantingTicket(ssoTicketId, principal);
        MoriaTicket tgTicket = store.getTicketFromStore(MoriaTicketType.TICKET_GRANTING_TICKET, tgTicketId);

        assertNotNull("TGT should not be null", tgTicketId);
        assertEquals("TGT has wrong type", MoriaTicketType.TICKET_GRANTING_TICKET, tgTicket.getTicketType());
        assertEquals("TGT has wrong principal", principal, tgTicket.getServicePrincipal());

        /* Non-existing ticket */
        tgTicketId = null;
        ssoTicketId = new MoriaTicket(MoriaTicketType.SSO_TICKET, null, new Long(30)).getTicketId();
        try {
            tgTicketId = store.createTicketGrantingTicket(ssoTicketId, principal);
            fail("InvalidTicketException should have been thrown");
        }
        catch (InvalidTicketException success) {
        }
        assertNull("TGT should be null for an invalid ticket", tgTicketId);
    }

    /**
     * Test Proxy ticket creation.
     *
     * @throws InvalidTicketException
     */
    public void testCreateProxyTicket() throws InvalidTicketException {
        /* Invalid arguments */
        try {
            store.createTicketGrantingTicket(null, principal);
            fail("IllegalArgumentException should be raised, ticketId is null");
        }
        catch (IllegalArgumentException success) {
        }
        try {
            store.createTicketGrantingTicket("", principal);
            fail("IllegalArgumentException should be raised, ticketId is an empty string");
        }
        catch (IllegalArgumentException success) {
        }
        try {
            store.createTicketGrantingTicket("foo", null);
            fail("IllegalArgumentException should be raised, principal is null");
        }
        catch (IllegalArgumentException success) {
        }
        try {
            store.createTicketGrantingTicket("foo", "");
            fail("IllegalArgumentException should be raised, principal is an empty string");
        }
        catch (IllegalArgumentException success) {
        }

        /* Invalid ticket */
        assertNull("Invalid ticket, should be null", store.createProxyTicket("doesNotExist", "foobar", "zotfoz"));

        /* Normal use */
        HashMap cachedAttrs = new HashMap();

        cachedAttrs.put("a", "b");
        cachedAttrs.put("c", "d");

        String ssoTicketId = store.cacheUserData(cachedAttrs);
        String tgTicketId = store.createTicketGrantingTicket(ssoTicketId, principal);
        String proxyTicketId = store.createProxyTicket(tgTicketId, principal, targetPrincipal);

        assertNotNull("Proxy ticket should not be null", store.getTicketFromStore(MoriaTicketType.PROXY_TICKET, proxyTicketId));
        assertEquals("Proxy ticket has wrong type", MoriaTicketType.PROXY_TICKET, store.getTicketFromStore(
                MoriaTicketType.PROXY_TICKET, proxyTicketId).getTicketType());
        assertEquals("Proxy ticket has wrong principal", targetPrincipal, store.getTicketFromStore(MoriaTicketType.PROXY_TICKET,
                proxyTicketId).getServicePrincipal());

        /* Assert that tgt and proxy ticket references same userdata object */
        assertEquals("Userdata for TGT and Proxy ticket do not match", store.getUserData(tgTicketId), store
                .getUserData(proxyTicketId));
    }

    /**
     * Test transient attribute storing.
     *
     * @throws InvalidTicketException
     */
    public void testSetTransientAttributes() throws InvalidTicketException {
        HashMap transAttributes = new HashMap();
        transAttributes.put("a", new String[] {"foo"});
        transAttributes.put("b", new String[] {"bar"});

        /* Illegal arguments */
        try {
            store.setTransientAttributes(null, transAttributes);
            fail("IllegalArgumentException should be raised, ticketId is null");
        }
        catch (IllegalArgumentException success) {
        }
        try {
            store.setTransientAttributes("", transAttributes);
            fail("IllegalArgumentException should be raised, ticketId is an empty string");
        }
        catch (IllegalArgumentException success) {
        }
        try {
            store.setTransientAttributes("bar", null);
            fail("IllegalArgumentException should be raised, transientAttributes is null");
        }
        catch (IllegalArgumentException success) {
        }

        /* Invalid ticket */
        try {
            store.setTransientAttributes("doesNotExist", transAttributes);
            fail("InvalidTicketException should be raised, ticket does not exist");
        }
        catch (InvalidTicketException success) {
        }

        HashMap cachedAttrs = new HashMap();
        cachedAttrs.put("a", "b");
        cachedAttrs.put("c", "d");

        /* Wrong ticket type */
        try {
            store.setTransientAttributes(store.cacheUserData(cachedAttrs), transAttributes);
            fail("InvalidTicketException should be raised, ticketId is null");
        }
        catch (InvalidTicketException success) {
        }

        /* Normal use */
        String loginTicketId = store.createAuthnAttempt(attributes, prefix, postfix, forceAuthn, principal);
        store.setTransientAttributes(loginTicketId, transAttributes);
        MoriaAuthnAttempt authnAttempt = store.getAuthnAttempt(loginTicketId, false);

        assertEquals("Transient attributes differs", transAttributes, authnAttempt.getTransientAttributes());
    }

    /**
     * Compares the content of two string arrays.
     *
     * @param arr1  array of strings to compare
     * @param arr2  array of strings to compare
     * @return true if the two arrays are identical
     */
    private boolean compareStringArrays(String[] arr1, String[] arr2) {
        boolean equals = true;
        if (arr1.length == arr2.length) {
            for (int i = 0; i < arr1.length; i++) {
                if (!arr1[i].equals(arr2[i])) {
                    equals = false;
                    break;
                }
            }
        }
        else {
            equals = false;
        }
        return equals;
    }

}