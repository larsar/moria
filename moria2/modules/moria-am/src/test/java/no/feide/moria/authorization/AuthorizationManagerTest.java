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

import org.jdom.Element;
import org.jdom.Document;

/**
 * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
 * @version $Revision$
 */
public class AuthorizationManagerTest extends TestCase {

	Document xmlDoc;

	public void buildUp() {
	}

	public static Test suite() {
		return new TestSuite(AuthorizationManagerTest.class);
	}

	public void testParseAttribute() throws IllegalConfigException {
		AuthorizationManager authMan = new AuthorizationManager();

		Element authAttrElem = new Element("AuthAttribute");

		/*
		 * All attributes should be set, otherwise an exception should be
		 * thrown
		 */
		try {
			authMan.parseAuthAttrElement(authAttrElem);
			fail("Name not set, should raise IllegalConfigExcepion");

		} catch (IllegalConfigException success) {
		}

		try {
			authAttrElem.setAttribute("name", "attr1");
			authMan.parseAuthAttrElement(authAttrElem);
			fail("AllowSSO not set, should raise IllegalConfigException");
		} catch (IllegalConfigException success) {
		}

		try {
			authAttrElem.setAttribute("sso", "foo");
			authMan.parseAuthAttrElement(authAttrElem);
			fail("SecLevel not set, should raise IllegalConfigException");
		} catch (IllegalConfigException success) {
		}
		
		authAttrElem.setAttribute("name", "attr1");
		authAttrElem.setAttribute("sso", "false");
		authAttrElem.setAttribute("secLevel", "HIGH");
		
		Assert.assertTrue(
			"Expects an equal AuthenticationAttribute object",
			new AuthorizationAttribute("attr1", false, "HIGH").equals(
				authMan.parseAuthAttrElement(authAttrElem)));
	}

}
