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

import junit.framework.Assert;
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

	public void testSecLevel() {
		Assert.assertEquals("UNKNOWN", AuthorizationAttribute.secLevelName(0));
		Assert.assertEquals("LOW", AuthorizationAttribute.secLevelName(1));
		Assert.assertEquals("MEDIUM", AuthorizationAttribute.secLevelName(2));
		Assert.assertEquals("HIGH", AuthorizationAttribute.secLevelName(3));
		Assert.assertEquals("UNKNOWN", AuthorizationAttribute.secLevelName(4));
	}

	/**
	 * Test constructor.
	 */
	public void testNewAuthorizationAttribute() {
		AuthorizationAttribute attribute;
		String name = "foobar";
		String medium = "MEDIUM";
		int secHigh = 3;
		int secMed = 2;

		attribute = new AuthorizationAttribute(name, true, medium);

		// Verify values of variables set in constructor
		Assert.assertEquals(name, attribute.getName());
		Assert.assertTrue(attribute.getAllowSSO());
		Assert.assertEquals(secMed, attribute.getSecLevel());

		// Invalid secLevel, should default to HIGH
		attribute = new AuthorizationAttribute(name, true, null);
		Assert.assertEquals(secHigh, attribute.getSecLevel());

		attribute = new AuthorizationAttribute(name, true, "");
		Assert.assertEquals(secHigh, attribute.getSecLevel());

		// No name
		try {
			attribute = new AuthorizationAttribute(null, true, medium);
			fail("Should raise IllegalArgumentException (name = null)");
			attribute = new AuthorizationAttribute("", true, medium);
			fail("Should raise IllegalArgumentException (name = empty string");
		} catch (IllegalArgumentException success) {
		}
	}

	/**
	 * Test equality of objects.
	 */
	public void testEquals() {

		Assert.assertTrue(
			"Identical objects",
			new AuthorizationAttribute("foo", false, "HIGH").equals(
				new AuthorizationAttribute("foo", false, "HIGH")));
		Assert.assertFalse(
			"Different name",
			new AuthorizationAttribute("foo", false, "HIGH").equals(
				new AuthorizationAttribute("bar", false, "HIGH")));
		Assert.assertFalse(
			"Different allowSSO value",
			new AuthorizationAttribute("foo", false, "HIGH").equals(
				new AuthorizationAttribute("foo", true, "HIGH")));
		Assert.assertFalse(
			"Different secLevel",
			new AuthorizationAttribute("foo", false, "HIGH").equals(
				new AuthorizationAttribute("foo", false, "LOW")));
	}

}
