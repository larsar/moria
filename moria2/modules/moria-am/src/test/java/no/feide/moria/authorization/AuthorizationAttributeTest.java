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

package no.feide.moria.authorization;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
 * @version $Revision$
 */
public class AuthorizationAttributeTest extends TestCase {

    /**
     * Initiate all tests.
     *
     * @return Junit test suite.
     */
    public static Test suite() {
        return new TestSuite(AuthorizationAttributeTest.class);
    }

    /**
     * Test constructor.
     *
     * @see AuthorizationAttribute#AuthorizationAttribute(java.lang.String, boolean, int)
     */
    public void testNewAuthorizationAttribute() {
        AuthorizationAttribute attribute;

        attribute = new AuthorizationAttribute("attr1", true, 1);

        /* Verify values of variables set in constructor */
        assertEquals("Name differs", "attr1", attribute.getName());
        assertTrue("Should allow SSO", attribute.getAllowSSO());
        assertEquals("SecLevel differs", 1, attribute.getSecLevel());

        /* Invalid secLevel */
        try {
            new AuthorizationAttribute("attr2", true, -1);
            fail("IllegalArgumentException should be raised.");
        } catch (IllegalArgumentException success) {
        }

        /* Invalid name */
        try {
            new AuthorizationAttribute("", true, 2);
            fail("Should raise IllegalArgumentException (name = empty string");
        } catch (IllegalArgumentException success) {
        }

        try {
            new AuthorizationAttribute("", true, 2);
            fail("Should raise IllegalArgumentException (name = empty string");
        } catch (IllegalArgumentException success) {
        }
    }

    /**
     * Test equality of objects.
     */
    public void testEquals() {
        AuthorizationAttribute masterAttr = new AuthorizationAttribute("foo", false, 2);

        assertTrue("Object should be identical", masterAttr.equals(new AuthorizationAttribute("foo", false, 2)));
        assertFalse("Name differs", masterAttr.equals(new AuthorizationAttribute("bar", false, 2)));
        assertFalse("AllowSSO differs", masterAttr.equals(new AuthorizationAttribute("foo", true, 2)));
        assertFalse("SecLevel differs", masterAttr.equals(new AuthorizationAttribute("foo", false, 0)));
    }

    /**
     * Test equality of objects.
     */
    public void testHashCode() {
        AuthorizationAttribute masterAttr = new AuthorizationAttribute("foo", false, 2);

        assertEquals("Object should be identical", masterAttr.hashCode(), new AuthorizationAttribute("foo", false, 2).hashCode());
        assertFalse("Name differs", masterAttr.hashCode() == new AuthorizationAttribute("bar", false, 2).hashCode());
        assertFalse("AllowSSO differs", masterAttr.hashCode() == new AuthorizationAttribute("foo", true, 2).hashCode());
        assertFalse("SecLevel differs", masterAttr.hashCode() == new AuthorizationAttribute("foo", false, 0).hashCode());
    }
}
