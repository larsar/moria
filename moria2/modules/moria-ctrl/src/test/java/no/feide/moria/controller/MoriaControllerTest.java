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

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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

    /**
     * Thest the initiateMoriaAuthentication method.
     *
     * @see MoriaController#initiateMoriaAuthentication(java.lang.String, java.lang.String[], java.lang.String, java.lang.String, boolean)
     * @throws AuthorizationException
     * @throws MoriaControllerException
     */
    public void testInitiateMoriaAuthentication() throws AuthorizationException, MoriaControllerException {
        String validPrefix = "http://moria.sf.net/";
        String validPostfix = "&foo=bar";
        String validPrincipal = "test";
        String[] validAttrs = new String[]{"attr1", "attr2"};

        /* Controller not initialized */
        MoriaController.stop();
        try {
            MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false, validPrincipal);
            fail("IllegalStateException should be raised, controller not initialized.");
        } catch (IllegalStateException success) {
        }

        MoriaController.init();

        /* Illegal paramenters */
        try {
            MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false, null);
            fail("MoriaControllerException should be raised, principal is null");
        } catch (MoriaControllerException success) {
        }
        try {
            MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false, "");
            fail("MoriaControllerException should be raised, principal is an empty string");
        } catch (MoriaControllerException success) {
        }

        try {
            MoriaController.initiateAuthentication(null, validPrefix, validPostfix, false, validPrincipal);
            fail("MoriaControllerException should be raised, attributes is null");
        } catch (MoriaControllerException success) {
        }

        try {
            MoriaController.initiateAuthentication(validAttrs, null, validPostfix, false, validPrincipal);
            fail("MoriaControllerException should be raised, prefix is null");
        } catch (MoriaControllerException success) {
        }
        try {
            MoriaController.initiateAuthentication(validAttrs, "", validPostfix, false, validPrincipal);
            fail("MoriaControllerException should be raised, prefix is an empty string");
        } catch (MoriaControllerException success) {
        }

        try {
            MoriaController.initiateAuthentication(validAttrs, validPrefix, null, false, validPrincipal);
            fail("MoriaControllerException should be raised, postfix is null");
        } catch (MoriaControllerException success) {
        }
        try {
            MoriaController.initiateAuthentication(validAttrs, validPrefix, "", false, validPrincipal);
            fail("MoriaControllerException should be raised, postfix is an empty string");
        } catch (MoriaControllerException success) {
        }

        /* Illegal attribute request */
        String[] attrs = new String[]{"illegal1", "illegal2"};
        try {
            MoriaController.initiateAuthentication(attrs, validPrefix, validPostfix, false, validPrincipal);
            fail("AuthorizationException should be raised, illegal attributes request.");
        } catch (AuthorizationException success) {
        }

        /* Illegal URL */
        try {
            MoriaController.initiateAuthentication(validAttrs, "foobar", validPostfix, false, validPrincipal);
            fail("MoriaControllerException should be raised, illegal URL (no protocol).");
        } catch (MoriaControllerException success) {
        }

        /* Legal use */
        String ticket;
        ticket = MoriaController.initiateAuthentication(validAttrs, validPrefix, validPostfix, false, validPrincipal);
        Assert.assertTrue("Login ticket should be valid", MoriaController.validateLoginTicket(ticket));

        ticket = MoriaController.initiateAuthentication(new String[]{}, validPrefix, validPostfix, false, validPrincipal);
        Assert.assertTrue("Login ticket shsould be valid", MoriaController.validateLoginTicket(ticket));
    }

    public void testAttemptLogin() {
        // TODO: Implement
    }

    public void testAttemptSingleSignOn() {
        // TODO: Implement
    }

    /**
     * Test the validateLoginTicket method.
     *
     * @see MoriaController#validateLoginTicket(java.lang.String)
     * @throws MoriaControllerException
     * @throws AuthorizationException
     */
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
        ticket = MoriaController.initiateAuthentication(new String[]{"attr1"}, "http://foo/", "/bar/", false, "test");
        Assert.assertTrue("Login ticket should be valid", MoriaController.validateLoginTicket(ticket));
    }

    public void testGetUserAttributes() {
        // TODO: Implement
    }

    /**
     * Test the isLegalURL method.
     *
     * @see MoriaController#isLegalURL(java.lang.String) 
     */
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
