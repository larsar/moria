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
import org.jdom.Element;

/**
 * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
 * @version $Revision$
 */
public class AuthorizationManager {

	/**
	 * Parses a XML element and creates a AuthorizationAttribute object in
	 * return. Throws an IllegalConfigException if there is something wrong
	 * with the element or it´s attributes.
	 * 
	 * @param element
	 * @return AuthorizationAttribute with same attributes as the supplied
	 *         Element
	 * @throws IllegalConfigException
	 */
	AuthorizationAttribute parseAttributeElem(Element element) throws IllegalConfigException {
		String name = null, secLevel = null, allowSSOStr = null;

		if (element.getAttribute("name") != null)
			name = element.getAttribute("name").getValue();

		if (element.getAttribute("sso") == null) {
			throw new IllegalConfigException("allowSSO has to be set.");
		} else {
			allowSSOStr = element.getAttribute("sso").getValue();
			if (!(allowSSOStr.equals("true") || allowSSOStr.equals("false")))
				throw new IllegalConfigException("allowSSO has to be 'true' or 'false'");
		}

		if (element.getAttribute("secLevel") != null)
			secLevel = element.getAttribute("secLevel").getValue();

		try {
			return new AuthorizationAttribute(name, new Boolean(allowSSOStr).booleanValue(), secLevel);
		} catch (IllegalArgumentException e) {
			throw new IllegalConfigException("Illegal attributes: " + e.getMessage());
		}
	}

	/**
	 * Parse the content of an Attributes element. The element can contain 0 or
	 * more Attribute elements which will be transformed into
	 * AuthorizationAttributes and returned in a HashMap with attribute name as
	 * key.
	 * 
	 * @param element The DOM element that contains Attribute child elements.
	 * @return HashMap with AuthorizationAttributes as value and attribute name
	 *         as key.
	 * @throws IllegalConfigException
	 * @throws IllegalArgumentException
	 */
	HashMap parseAttributesElem(Element element) throws IllegalConfigException, IllegalArgumentException {
		HashMap attributes = new HashMap();

		/* Validate element */
		if (element == null)
			throw new IllegalArgumentException("Element cannot be null.");

		if (!element.getName().equalsIgnoreCase("attributes"))
			throw new IllegalConfigException("Element isn't of type 'Attributes'");

		/* Create AuthorizationAttribute of all child elements */
		Iterator it = (element.getChildren()).iterator();
		while (it.hasNext()) {
			AuthorizationAttribute attribute = parseAttributeElem((Element) it.next());
			attributes.put(attribute.getName(), attribute);
		}

		return attributes;
	}
	
	/**
	 * Parses an 'operation' element and returns the name attribute. 
	 * 
	 * @param element The operation element
	 * @return String containing the name attribute of the element.
	 * @throws IllegalArgumentException 
	 * @trhows IllegalConfigException 
	 */
	String parseOperationElem(Element element) throws IllegalArgumentException, IllegalConfigException{
		
		if (element == null) 
			throw new IllegalArgumentException("Element cannot be null");
		
		if (!element.getName().equalsIgnoreCase("Operation"))
			throw new IllegalConfigException("Element must be of type 'Operation'");
	
		
		if (element.getAttribute("name") == null)
			throw new IllegalConfigException("Element's name attribute must be set.");
		
		if (element.getAttributeValue("name").equalsIgnoreCase(""))
			throw new IllegalConfigException("Element's name attribute cannot be an empty string.");
		
		return element.getAttributeValue("name");
	}
	
	/**
	 * Parse the content of an Attributes element. The element can contain 0 or
	 * more Attribute elements which will be transformed into
	 * AuthorizationAttributes and returned in a HashMap with attribute name as
	 * key.
	 * 
	 * @param element The DOM element that contains Attribute child elements.
	 * @return HashMap with AuthorizationAttributes as value and attribute name
	 *         as key.
	 * @throws IllegalConfigException
	 * @throws IllegalArgumentException
	 */
	HashSet parseOperationsElem(Element element) throws IllegalConfigException, IllegalArgumentException {
		HashSet operations = new HashSet();

		/* Validate element */
		if (element == null)
			throw new IllegalArgumentException("Element cannot be null.");

		if (!element.getName().equalsIgnoreCase("operations"))
			throw new IllegalConfigException("Element isn't of type 'Operations'");

		/* Create AuthorizationAttribute of all child elements */
		Iterator it = (element.getChildren()).iterator();
		while (it.hasNext()) {
			String operation = parseOperationElem((Element) it.next());
			operations.add(operation);
		}

		return operations;
	}	
}
