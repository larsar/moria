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
import java.util.HashSet;
import java.util.Iterator;

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

	/**
	 * Run all tests.
	 * 
	 * @return The test suite to run.
	 */
	public static Test suite() {
		return new TestSuite(AuthorizationManagerTest.class);
	}

	/**
	 * Creates an Element object (Attribute) with the supplied attributs.
	 * 
	 * @param name
	 * @param sso
	 * @param secLevel
	 * @return Element object with attributes according to the paramteres.
	 */
	private Element createAttrElem(String name, String sso, String secLevel) {
		Element element = new Element("Attribute");

		if (name != null)
			element.setAttribute("name", name);
		if (sso != null)
			element.setAttribute("sso", sso);
		if (secLevel != null)
			element.setAttribute("secLevel", secLevel);

		return element;
	}

	/**
	 * Creates an Element object (Operation) with the supplied attributes.
	 * 
	 * @param name
	 * @return Element of type 'operation' with a name attribute.
	 */
	private Element createOperationElem(String name) {
		Element element = new Element("Operation");
		if (name != null)
			element.setAttribute("name", name);
		return element;
	}

	/**
	 * Test parsing of single attribute element.
	 */
	public void testParseAttribute() throws IllegalConfigException {
		AuthorizationManager authMan = new AuthorizationManager();

		/*
		 * All attributes should be set, otherwise an exception should be
		 * thrown.
		 */
		try {
			/* Null as name */
			authMan.parseAttributeElem(createAttrElem(null, null, null));
			fail("Name not set, should raise IllegalConfigExcepion");

		} catch (IllegalConfigException success) {
		}

		try {
			/* Null as sso parameter */
			authMan.parseAttributeElem(createAttrElem("foo", null, null));
			fail("AllowSSO not set, should raise IllegalConfigException");
		} catch (IllegalConfigException success) {
		}

		try {
			/* Null as seclevel */
			authMan.parseAttributeElem(createAttrElem("foo", "false", null));
			fail("SecLevel not set, should raise IllegalConfigException");
		} catch (IllegalConfigException success) {
		}

		try {
			/* Wrong element type */
			authMan.parseAttributeElem(new Element("WrongType"));
			fail("IllegalConfigException should be raised, wrong element type");
		} catch (IllegalConfigException success) {
		}

		/* Test equality of generated object */
		Assert.assertTrue(
			"Expects an equal AuthenticationAttribute object",
			new AuthorizationAttribute("foo", false, "HIGH").equals(
				authMan.parseAttributeElem(createAttrElem("foo", "false", "HIGH"))));
	}

	/**
	 * Test parsing of attributes element that contains attribute child
	 * elements.
	 * 
	 * @throws IllegalArgumentException
	 * @throws IllegalConfigException
	 */
	public void testParseAttributes() throws IllegalArgumentException, IllegalConfigException {
		AuthorizationManager authMan = new AuthorizationManager();

		Element attributesElem = new Element("Attributes");
		attributesElem.addContent(createAttrElem("foo", "false", "MEDIUM"));
		attributesElem.addContent(createAttrElem("bar", "true", "LOW"));
		attributesElem.addContent(createAttrElem("foobar", "false", "HIGH"));

		HashMap attributes = new HashMap();
		attributes.put("foo", new AuthorizationAttribute("foo", false, "MEDIUM"));
		attributes.put("bar", new AuthorizationAttribute("bar", true, "LOW"));
		attributes.put("foobar", new AuthorizationAttribute("foobar", false, "HIGH"));

		/* Normal use */
		HashMap parsedAttributes = authMan.parseAttributesElem(attributesElem);
		Assert.assertEquals(
			"Output and input should be of equal size",
			attributes.size(),
			parsedAttributes.size());

		Iterator it = attributes.keySet().iterator();
		while (it.hasNext()) {
			String attrName = (String) it.next();
			Assert.assertTrue(
				"Generated attribute should be eqal to master",
				attributes.get(attrName).equals(parsedAttributes.get(attrName)));
			}

		/* Attributes element without children */
		attributesElem = new Element("Attributes");
		Assert.assertTrue(
			"No attribute elements should result in empty map",
			authMan.parseAttributesElem(attributesElem).size() == 0);

		/* Null as parameter */
		try {
			authMan.parseAttributesElem(null);
			fail("IllegalArgumentException should be raised, null parameter");
		} catch (IllegalArgumentException success) {
		}

		/* Wrong type of element (not "Attributes") */
		try {
			authMan.parseAttributesElem(new Element("WrongType"));
			fail("IllegalConfigException should be raised, wrong element type");
		} catch (IllegalConfigException success) {
		}

		attributesElem = new Element("Attributes");
		attributesElem.addContent(new Element("Attribute"));
		attributesElem.addContent(new Element("WrongChildType"));
		try {
			authMan.parseAttributesElem(attributesElem);
			fail("IllegalConfigException should be raised, wrong child element type");
		} catch (IllegalConfigException success) {
		}
	}

	/**
	 * Test parsing of a single 'operation' element.
	 */
	public void testParseOperationElem() throws IllegalConfigException {
		AuthorizationManager authMan = new AuthorizationManager();

		/* Null element */
		try {
			authMan.parseOperationElem(null);
			fail("IllegalConfigException should be raised, null element");
		} catch (IllegalArgumentException success) {
		}

		/* Wrong type of element */
		try {
			authMan.parseOperationElem(new Element("WrongType"));
			fail("IllegalConfigException should be raised, wrong type of element");
		} catch (IllegalConfigException success) {
		}

		/* No name attribute */
		try {
			authMan.parseOperationElem(new Element("Operation"));
		} catch (IllegalConfigException success) {
		}

		Element operationElem = new Element("Operation");

		/* Name attribute is an empty string */
		try {
			operationElem.setAttribute("name", "");
			authMan.parseOperationElem(new Element("Operation"));
		} catch (IllegalConfigException success) {
		}

		/* Proper use */
		operationElem.setAttribute("name", "foobar");
		Assert.assertEquals(
			"Input name attribute should be equal to returned value",
			"foobar",
			authMan.parseOperationElem(operationElem));
	}

	/**
	 * Test parsing of operations element.
	 */
	public void testParseOperationsElem() throws IllegalArgumentException, IllegalConfigException {
		AuthorizationManager authMan = new AuthorizationManager();

		Element operationsElem = new Element("Operations");
		operationsElem.addContent(createOperationElem("localAuth"));
		operationsElem.addContent(createOperationElem("directAuth"));

		Element elemt2 = new Element("Operation");

		HashSet operations = new HashSet();
		operations.add("localAuth");
		operations.add("directAuth");

		/* Normal use */
		HashSet parsedOperations = authMan.parseOperationsElem(operationsElem);
		Assert.assertEquals(
			"Output and input should be of equal size",
			operations.size(),
			parsedOperations.size());

		Iterator it = operations.iterator();
		while (it.hasNext()) {
			Assert.assertTrue(
				"Content of output is not equal to master",
				parsedOperations.contains((String) it.next()));
		}

		/* Attributes element without children */
		operationsElem = new Element("Operations");
		Assert.assertTrue(
			"No operation elements should result in empty map",
			authMan.parseOperationsElem(operationsElem).size() == 0);

		/* Null as parameter */
		try {
			authMan.parseOperationsElem(null);
			fail("IllegalArgumentException should be raised, null parameter");
		} catch (IllegalArgumentException success) {
		}

		/* Wrong type of element (not "Operations") */
		try {
			authMan.parseOperationsElem(new Element("WrongType"));
			fail("IllegalConfigException should be raised, wrong element type");
		} catch (IllegalConfigException success) {
		}

		operationsElem = new Element("Operations");
		operationsElem.addContent(createOperationElem("localAuth"));
		operationsElem.addContent(new Element("WrongChildType"));
		try {
			authMan.parseOperationsElem(operationsElem);
			fail("IllegalConfigException should be raised, wrong child element type");
		} catch (IllegalConfigException success) {
		}

	}
}
