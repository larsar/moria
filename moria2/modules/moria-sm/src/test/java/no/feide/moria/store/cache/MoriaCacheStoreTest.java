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

package no.feide.moria.store.cache;

import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import no.feide.moria.store.CachedUserData;
import no.feide.moria.store.InvalidTicketException;
import no.feide.moria.store.MoriaAuthnAttempt;
import no.feide.moria.store.MoriaStore;
import no.feide.moria.store.MoriaTicket;

/**
 * Test class for classes implementing the MoriaStore interface
 * 
 * @author Bjørn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public class MoriaCacheStoreTest extends TestCase {

    MoriaStore store;

    String[] attributes = { "eduPersonOrgDN", "eduPersonAffiliation" };
    String prefix = "http://demo.feide.no/mellon-demo/Demo?moriaId=";
    String postfix = "";
    String principal = "no.feide.demo";
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
     * Test creation + retrival and removal
     * 
     * @throws InvalidTicketException
     */
    public void testCreateAuthnAttempt() throws InvalidTicketException {

        MoriaTicket loginTicket =
            store.createAuthnAttempt(attributes, prefix, postfix, principal, forceAuthn);

        assertNotNull("Login ticket should not be null", loginTicket);
        assertEquals("", principal, loginTicket.getServicePrincipal());

        MoriaAuthnAttempt authnAttempt = store.getAuthnAttempt(loginTicket, false);

        assertNotNull("Authentication attempt should not be null", authnAttempt);

        assertEquals("Attribute array does not match", attributes, authnAttempt.getRequestedAttributes());
        assertEquals("URL prefix does not match", prefix, authnAttempt.getReturnURLPrefix());
        assertEquals("URL postfix does not match", postfix, authnAttempt.getReturnURLPostfix());
        assertEquals(
            "Forced authenetication does not match",
            forceAuthn,
            authnAttempt.isForceInterativeAuthentication());

        try {
            loginTicket = store.createAuthnAttempt(null, prefix, postfix, principal, forceAuthn);
            fail("Null value for attributes did not cause exception to be thrown");
        } catch (IllegalArgumentException success) {
        }

        /* Emtpy string array should work */
        loginTicket = store.createAuthnAttempt(new String[] { null }, prefix, postfix, principal, forceAuthn);
        store.getAuthnAttempt(loginTicket, false);

        try {
            loginTicket = store.createAuthnAttempt(attributes, null, postfix, principal, forceAuthn);
            fail("Null value for prefix did not cause exception to be thrown");
        } catch (IllegalArgumentException success) {
        }

        try {
            loginTicket = store.createAuthnAttempt(attributes, prefix, null, principal, forceAuthn);
            fail("Null value for postfix did not cause exception to be thrown");
        } catch (IllegalArgumentException success) {
        }

        try {
            loginTicket = store.createAuthnAttempt(attributes, prefix, postfix, null, forceAuthn);
            fail("Null value for pricipal did not cause exception to be thrown");
        } catch (IllegalArgumentException success) {
        }

        try {
            loginTicket = store.createAuthnAttempt(attributes, prefix, postfix, "", forceAuthn);
            fail("Empty principal did not cause exception to be thrown");
        } catch (IllegalArgumentException success) {
        }
    }

    /**
     * Test SSO ticket creation
     * 
     * @throws InvalidTicketException
     */
    public void testSSOTicketCreation() throws InvalidTicketException {
        HashMap cachedAttrs = new HashMap();

        cachedAttrs.put("a", "b");
        cachedAttrs.put("c", "d");

        MoriaTicket ssoTicket = store.cacheUserData(cachedAttrs);

        assertNotNull("SSO ticket should not be null", ssoTicket);
        assertEquals("SSO ticket has wrong type", MoriaTicket.SSO_TICKET, ssoTicket.getTicketType());
    }

    /**
     * Test Service ticket creation
     * 
     * @throws InvalidTicketException
     */
    public void testServiceTicketCreation() throws InvalidTicketException {
        MoriaTicket loginTicket =
            store.createAuthnAttempt(attributes, prefix, postfix, principal, forceAuthn);

        MoriaTicket serviceTicket = store.createServiceTicket(loginTicket);

        assertNotNull("Service ticket should not be null", serviceTicket);
        assertEquals(
            "Service ticket has wrong type",
            MoriaTicket.SERVICE_TICKET,
            serviceTicket.getTicketType());
    }

    /**
     * Test user data retrival
     * 
     * @throws InvalidTicketException
     */
    public void testGetUserData() throws InvalidTicketException {
        HashMap cachedAttrs = new HashMap();

        cachedAttrs.put("a", "b");
        cachedAttrs.put("c", "d");

        MoriaTicket ssoTicket = store.cacheUserData(cachedAttrs);

        CachedUserData userData = store.getUserData(ssoTicket);

        assertNotNull("User data should not be null", userData);
        assertEquals("Returned user data doesn't match", "b", userData.getAttributes().get("a"));
        assertEquals("Returned user data doesn't match", "d", userData.getAttributes().get("c"));

        /* Test with invalid (non-existent) ticket */

        ssoTicket = new MoriaTicket(MoriaTicket.SSO_TICKET, null, 30);
        userData = store.getUserData(ssoTicket);
        assertNull("No user data should be null for an invalid ticket", userData);
    }

    /**
     * Test TGT creation
     * 
     * @throws InvalidTicketException
     */
    public void testTGTicketCreation() throws InvalidTicketException {
        HashMap cachedAttrs = new HashMap();

        cachedAttrs.put("a", "b");
        cachedAttrs.put("c", "d");

        MoriaTicket ssoTicket = store.cacheUserData(cachedAttrs);

        MoriaTicket tgTicket = store.createTicketGrantingTicket(ssoTicket, principal);

        assertNotNull("TGT should not be null", tgTicket);
        assertEquals("TGT has wrong type", MoriaTicket.TICKET_GRANTING_TICKET, tgTicket.getTicketType());
        assertEquals("TGT has wrong principal", principal, tgTicket.getServicePrincipal());

        /* Test with invalid (non-existent) ticket */

        ssoTicket = new MoriaTicket(MoriaTicket.SSO_TICKET, null, 30);
        try {
            tgTicket = store.createTicketGrantingTicket(ssoTicket, principal);
            fail("");
        } catch (InvalidTicketException success) {
        }
        assertNull("TGT should be null for an invalid ticket", tgTicket);
    }

    /**
     * Test Proxy ticket creation
     * 
     * @throws InvalidTicketException
     */
    public void testProxyTicketCreation() throws InvalidTicketException {
        HashMap cachedAttrs = new HashMap();
        String principal2 = "no.feide.test1";

        cachedAttrs.put("a", "b");
        cachedAttrs.put("c", "d");

        MoriaTicket ssoTicket = store.cacheUserData(cachedAttrs);

        MoriaTicket tgTicket = store.createTicketGrantingTicket(ssoTicket, principal);

        MoriaTicket proxyTicket = store.createProxyTicket(tgTicket, principal2);

        assertNotNull("Proxy ticket should not be null", proxyTicket);
        assertEquals("Proxy ticket has wrong type", MoriaTicket.PROXY_TICKET, proxyTicket.getTicketType());
        assertEquals("Proxy ticket has wrong principal", principal2, proxyTicket.getServicePrincipal());

        /* Assert that tgt and proxy ticket references same userdata object */
        assertEquals(
            "Userdata for TGT and Proxy ticket do not match",
            store.getUserData(tgTicket),
            store.getUserData(proxyTicket));
    }
}
