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

import java.util.HashMap;
import java.util.Iterator;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
 * @version $Revision$
 */
public class AuthorizationProfileTest extends TestCase {

	/**
	 * Initiate all tests.
	 * 
	 * @return Junit test suite.
	 */
	public static Test suite() {
		return new TestSuite(AuthorizationProfileTest.class);
	}

	/**
	 * Test creation of object. Name should not be allowed to be null.
	 *  
	 */
	public void testNewAuthorizationProfile() {
		String name = "foobar";

		AuthorizationProfile authProfile = new AuthorizationProfile(name);

		/* Variables set by constructor */
		Assert.assertEquals(authProfile.getName(), name);

		/* Empty project name */
		try {
			authProfile = new AuthorizationProfile(null);
			fail("Should raise IllegalArgumentException (name = null)");
			authProfile = new AuthorizationProfile("");
			fail("Should raise IllegalArgumentException (name = empty string");
		} catch (IllegalArgumentException success) {
		}

	}

	public void testGetAttributes() {
		AuthorizationProfile authProfile;
		HashMap attrSSOPermissions = new HashMap();
		
		attrSSOPermissions.put(
			new AuthorizationAttribute("attr1", false, "MEDIUM"),
			new Boolean(false));
		attrSSOPermissions.put(
			new AuthorizationAttribute("attr2", false, "MEDIUM"),
			new Boolean(true));
		
		/* No attributes */
		authProfile = new AuthorizationProfile("foobar");
		Assert.assertNull(authProfile.attributes());
		
		authProfile.setAttrSSOPermissions(attrSSOPermissions);
		HashMap attributes;
		
		/* Size of returned data */
		attributes = authProfile.attributes();
		Assert.assertEquals(attrSSOPermissions.size(),attributes.size());	
		
		/* Correct content, all keys and correct value instance */
		AuthorizationAttribute attribute;
		for (Iterator it = attrSSOPermissions.keySet().iterator(); it.hasNext(); ) {
			attribute = (AuthorizationAttribute) it.next();
			Assert.assertTrue(attributes.containsKey(attribute.getName()));
			Assert.assertTrue(attributes.get(attribute.getName()) instanceof AuthorizationAttribute);
		}
	}

	/**
	 * Test SSO permission. Each AuthorizationAttribute is configured to be
	 * allowed with SSO and by comparing these with the profiles permissions
	 * Moria can decide whether SSO is allowed or not.
	 */
	public void testSSOForAttribute() {
		
		AuthorizationAttribute attr1, attr2, attr3, attr4;
		attr1 = new AuthorizationAttribute("attr1", false, "MEDIUM");
		attr2 = new AuthorizationAttribute("attr2", false, "MEDIUM");
		attr3 = new AuthorizationAttribute("attr3", true, "MEDIUM");
		attr4 = new AuthorizationAttribute("attr4", true, "MEDIUM");

		HashMap attrSSOPermissions = new HashMap();
		attrSSOPermissions.put(attr1, new Boolean(false)); // false+false=false
		attrSSOPermissions.put(attr2, new Boolean(true)); // true+false=false
		attrSSOPermissions.put(attr3, new Boolean(false)); // false+true=false
		attrSSOPermissions.put(attr4, new Boolean(true)); // true+true=true

		AuthorizationProfile authProfile = new AuthorizationProfile("foobar");
		authProfile.setAttrSSOPermissions(attrSSOPermissions);
		Assert.assertFalse(authProfile.allowSSOForAttribute(attr1));
		Assert.assertFalse(authProfile.allowSSOForAttribute(attr2));
		Assert.assertFalse(authProfile.allowSSOForAttribute(attr3));
		Assert.assertTrue(authProfile.allowSSOForAttribute(attr4));
	}
}
