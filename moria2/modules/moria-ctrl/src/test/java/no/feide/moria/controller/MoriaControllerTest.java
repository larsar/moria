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

package no.feide.moria.controller;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import junit.framework.Assert;
import no.feide.moria.store.MoriaCacheStore;

/**
 * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
 * @version $Revision$
 */
public class MoriaControllerTest extends TestCase {

    /**
     * Initiate all tests.
     *
     * @return Junit test suite.
     */
    public static Test suite() {
        return new TestSuite(MoriaControllerTest.class);
    }


    public void setUp() {
        /* Property needed by the RandomId class */
        if (System.getProperty("no.feide.moria.store.randomid.nodeid") == null)
            System.setProperty("no.feide.moria.store.randomid.nodeid", "no1");
    }

    /**
     * Test the init() method.
     *
     * @see MoriaController#init()
     */
    public void testInit() {
        MoriaController.init();
    }


    public void testInitiateMoriaAuthentication() throws AuthorizationException, MoriaControllerException {
        String validPrefix = "http://moria.sf.net/";
        String validPostfix = "&foo=bar";
        String validPrincipal = "test";
        String[] validAttrs = new String[]{"attr1", "attr2"};

        /* Controller not initialized */
        MoriaController.stop();
        try {
            MoriaController.initiateMoriaAuthentication(validPrincipal, validAttrs, validPrefix, validPostfix, false);
            fail("IllegalStateException should be raised, controller not initialized.");
        } catch (IllegalStateException success) {
        }

        MoriaController.init();

        /* Illegal paramenters */
        try {
            MoriaController.initiateMoriaAuthentication(null, validAttrs, validPrefix, validPostfix, false);
            fail("MoriaControllerException should be raised, principal is null");
        } catch (MoriaControllerException success) {
        }
        try {
            MoriaController.initiateMoriaAuthentication("", validAttrs, validPrefix, validPostfix, false);
            fail("MoriaControllerException should be raised, principal is an empty string");
        } catch (MoriaControllerException success) {
        }

        try {
            MoriaController.initiateMoriaAuthentication(validPrincipal, null, validPrefix, validPostfix, false);
            fail("MoriaControllerException should be raised, attributes is null");
        } catch (MoriaControllerException success) {
        }

        try {
            MoriaController.initiateMoriaAuthentication(validPrincipal, validAttrs, null, validPostfix, false);
            fail("MoriaControllerException should be raised, prefix is null");
        } catch (MoriaControllerException success) {
        }
        try {
            MoriaController.initiateMoriaAuthentication(validPrincipal, validAttrs, "", validPostfix, false);
            fail("MoriaControllerException should be raised, prefix is an empty string");
        } catch (MoriaControllerException success) {
        }

        try {
            MoriaController.initiateMoriaAuthentication(validPrincipal, validAttrs, validPrefix, null, false);
            fail("MoriaControllerException should be raised, postfix is null");
        } catch (MoriaControllerException success) {
        }
        try {
            MoriaController.initiateMoriaAuthentication(validPrincipal, validAttrs, validPrefix, "", false);
            fail("MoriaControllerException should be raised, postfix is an empty string");
        } catch (MoriaControllerException success) {
        }

        /* Illegal attribute request */
        String[] attrs = new String[]{"illegal1", "illegal2"};
        try {
            MoriaController.initiateMoriaAuthentication(validPrincipal, attrs, validPrefix, validPostfix, false);
            fail("AuthorizationException should be raised, illegal attributes request.");
        } catch (AuthorizationException success) {
        }

        /* Illegal URL */
        try {
            MoriaController.initiateMoriaAuthentication(validPrincipal, validAttrs, "foobar", validPostfix, false);
            fail("MoriaControllerException should be raised, illegal URL (no protocol).");
        } catch (MoriaControllerException success) {
        }

        /* Legal use */
        String ticket;
        ticket = MoriaController.initiateMoriaAuthentication(validPrincipal, validAttrs, validPrefix, validPostfix, false);
        Assert.assertTrue("Login ticket should be valid", MoriaController.validateLoginTicket(ticket));

        ticket = MoriaController.initiateMoriaAuthentication(validPrincipal, new String[]{}, validPrefix, validPostfix, false);
        Assert.assertTrue("Login ticket shsould be valid", MoriaController.validateLoginTicket(ticket));
    }

    public void testAttemptLogin() {
    }

    public void testAttemptSingleSignOn() {
    }

    public void testValidateLoginTicket() throws MoriaControllerException, AuthorizationException {
        /* Controller not initialized */
        MoriaController.stop();
        try {
            MoriaController.validateLoginTicket("foobar");
            fail("IllegalStateException should be raised, controller not initialized.");
        } catch (IllegalStateException success) {
        }


        /* Invalid parameters */
        MoriaController.init();
        try {
            MoriaController.validateLoginTicket(null);
            fail("MoriaControllerException should be raised, ticket is null.");
        } catch (IllegalArgumentException success) {
        }
        try {
            MoriaController.validateLoginTicket("");
            fail("MoriaControllerException should be raised, ticket is empty string.");
        } catch (IllegalArgumentException success) {
        }

        /* Normal use */
        String ticket;
        ticket = MoriaController.initiateMoriaAuthentication("test", new String[]{"attr1"}, "http://foo/", "/bar/", false);
        Assert.assertTrue("Login ticket should be valid", MoriaController.validateLoginTicket(ticket));
    }

    public void testGetUserAttributes() {
    }

    public void testisLegalURL() {
        // TODO: Test more illegal URL constructs
        MoriaController.init();

        /* Illegal parameters */
        try {
            MoriaController.isLegalURL(null);
            fail("IllegalArgumentException should be raised, null value.");
        } catch (IllegalArgumentException success) {
        }
        try {
            MoriaController.isLegalURL("");
            fail("IllegalArgumentException should be raised, empty string.");
        } catch (IllegalArgumentException success) {
        }

        /* Illegal URL content */
        Assert.assertFalse("URL should be rejected (no protocol)", MoriaController.isLegalURL("foobar"));
        Assert.assertFalse("URL should be rejected (newline)", MoriaController.isLegalURL("http://moria.sf.net\n"));
        Assert.assertFalse("URL should be rejected (wrong protocol)", MoriaController.isLegalURL("ftp://foo.bar.com"));

        /* Legal URL content */
        Assert.assertTrue("URL should be accepted", MoriaController.isLegalURL("http://moria.sf.net/"));
        Assert.assertTrue("URL should be accepted", MoriaController.isLegalURL("http://moria.sf.net/index.html?foo=bar&bar=foo"));
    }
}
