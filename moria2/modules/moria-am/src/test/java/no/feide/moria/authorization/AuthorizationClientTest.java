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
import java.util.HashMap;

/**
 * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
 * @version $Revision$
 */
public class AuthorizationClientTest extends TestCase {

	/**
	 * Initiate all tests.
	 * 
	 * @return Junit test suite.
	 */
	public static Test suite() {
		return new TestSuite(AuthorizationClientTest.class);
	}

	/**
	 * Test creation of AuthorizationClient.
	 */
	public void testNewAuthorizationClient() {
		// No name
		try {
			new AuthorizationClient(null);
			fail("Should raise IllegalArgumentException (id = null)");
			new AuthorizationClient("");
			fail("Should raise IllegalArgumentException (id = empty string)");
		} catch (IllegalArgumentException success) {
		}
	}

	public void testAllowSSOForAttributes() {
		AuthorizationClient client = new AuthorizationClient("foobar");

		/* No registered attributes */
		Assert.assertFalse(client.allowSSOForAttributes(new String[] { "foo", "bar" }));

		HashMap allowedAttributes = new HashMap();
		HashMap deniedAttributes = new HashMap();
		allowedAttributes.put("attr1", new AuthorizationAttribute("attr1", false, "MEDIUM"));
		allowedAttributes.put("attr2", new AuthorizationAttribute("attr2", true, "MEDIUM"));
		deniedAttributes.put("attr3", new AuthorizationAttribute("attr3", false, "MEDIUM"));
		deniedAttributes.put("attr4", new AuthorizationAttribute("attr4", true, "MEDIUM"));

		client.setAllowedAttributes(allowedAttributes);

		
		// TODO: Error in tests. Should separate allowSSO and allowAccess
		
		/* Allowed attributes */
		Assert.assertFalse(
			"All allowed attributes",
			client.allowSSOForAttributes(new String[] { "attr1", "attr2" }));
		Assert.assertFalse(
			"Subset of allowed attributes",
			client.allowSSOForAttributes(new String[] { "attr1" }));

		/* Not allowed */
		client.setDeniedAttributes(deniedAttributes);
		Assert.assertFalse("Nonexisting attribute", client.allowSSOForAttributes(new String[] { "attr3" }));
		Assert.assertFalse(
			"Allowed and denied attributes",
			client.allowSSOForAttributes(new String[] { "attr1", "attr2", "attr3" }));
	}

	// Allow SSO for requested attributes (names of attributes)
	// Generate attribute list (sjekke lokale variable etter generering)
	// Alter attributes (legge til, fjerne fra liste)
	// seclevelname for attributes
	// hasaffiliation
}
