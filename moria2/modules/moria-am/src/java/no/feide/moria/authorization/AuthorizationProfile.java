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
import java.util.logging.Logger;

/**
 * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
 * @version $Revision$
 */
public class AuthorizationProfile {
	/** Used for logging. */
	private static Logger log = Logger.getLogger(AuthorizationProfile.class.toString());

	/** A unique id */
	private String id;

	/** Name of the profile (unique) */
	private String name;

	/**
	 * List of all attrSSOPermissions. An attribute object is the key and
	 * Boolean is the value: true if the attribute is allowed to be used with
	 * Single Sign On (SSO), false if not.
	 */
	private HashMap attrSSOPermissions = new HashMap();

	/**
	 * Private constructor. Should not be called by any one.
	 */
	private AuthorizationProfile() {
		// Constructor not allowed to be called without parameters
	}

	/**
	 * Constructor. The name of the profile must be sent as parameter to the
	 * constructor and it must be a non-empty string.
	 * 
	 * @param name The profiles name
	 */
	public AuthorizationProfile(String name) throws IllegalArgumentException {
		if (name == null || name.equals(""))
			throw new IllegalArgumentException("Profile name must be a non-empty string.");
		this.name = name;
	}

	/**
	 * Check if attribute is allowed to use with SSO. Only if both the default
	 * value of the attribute AND the web services link to the attribute allows
	 * SSO, a web service can use the attribute with SSO.
	 * 
	 * @param attribute The profile-attribute to check for SSO-use.
	 * @return boolean
	 */
	boolean allowSSOForAttribute(AuthorizationAttribute attribute) {

		if (attribute == null || attrSSOPermissions.get(attribute) == null)
			return false;

		return (((Boolean) attrSSOPermissions.get(attribute)).booleanValue() && attribute.getAllowSSO());
	}

	/**
	 * @return attrSSOPermissions A HashMap with key (<code>Attribute</code>):
	 *         attribute, value: Allow SSO (<code>Boolean</code>).
	 */
	HashMap getAttrSSOPermissions() {
		return attrSSOPermissions;
	}

	/**
	 * @param attrSSOPermissions A HashMap with key (<code>Attribute</code>):
	 *            attribute, value: Allow SSO (<code>Boolean</code>).
	 */
	void setAttrSSOPermissions(HashMap attrSSOPermissions) {
		this.attrSSOPermissions = attrSSOPermissions;
	}

	/**
	 * @return Name of the profile
	 */
	public String getName() {
		return name;
	}

	/**
	 * All attributes the WebService has access to. 
	 * 
	 * @return A HashMap with key: attributename value: AuthorizationAttribute object
	 */
	public HashMap attributes() {
		HashMap attributes = new HashMap();
		
		if (attrSSOPermissions.size() == 0)
			return null;
		
		int i = 0;
		AuthorizationAttribute attribute;
		for (java.util.Iterator it = attrSSOPermissions.keySet().iterator(); it.hasNext();) {
			attribute = (AuthorizationAttribute) it.next();
			attributes.put(attribute.getName(), attribute);
		}
		return attributes;
	}
}