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

import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Iterator;

/**
 * This class represents a LDAP attribute and is used for authorization of a
 * web service. Both Profile and WebService have lists of attributes.
 * 
 * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
 * @version $Revision$
 */
public class AuthorizationAttribute {

	/** Used for logging. */
	private static Logger log = Logger.getLogger(AuthorizationAttribute.class.toString());

	/** Name of attribute */
	private String name = null;

	/** Is this attribute allowd in use with SSO */
	private Boolean allowSSO = null;

	/** Security level */
	private int secLevel = 3;

	/** Security level register */
	private static HashMap secLevels = initSecLevels();

	/**
	 * Private constructor. Should not be called by any one.
	 */
	private AuthorizationAttribute() {
		// Constructor not allowed to be called without parameters
		super();
	}

	/**
	 * Constructor. Name of attribute must be a non-empty string. Security
	 * level can be set to "LOW", "MEDIUM" or " "HIGH", it defaults to "HIGH".
	 * 
	 * @param name Name of attribute
	 * @param allowSSO Allow use of SSO with this attribute
	 */
	AuthorizationAttribute(String name, boolean sso, String secLevelStr) throws IllegalArgumentException {

		if (name == null || name.equals("")) {
			throw new IllegalArgumentException("Name must be a non-empty string.");
		}

		if (secLevelStr == null || secLevelStr.equals("")) {
			throw new IllegalArgumentException("SecLevel must be a non-empty string.");
		}

		if (!secLevels.containsKey(secLevelStr)) {
			log.warning("Invalid attribute secLevel: \"" + secLevelStr + "\" Set to default (HIGH).");
			secLevelStr = "HIGH";
		}

		secLevel = ((Integer) secLevels.get(secLevelStr)).intValue();
		this.allowSSO = new Boolean(sso);
		this.name = name;
	}

	/**
	 * Return true if the supplied object is identical to this one.
	 * 
	 * @return false if any of the attributes are different from the supplied
	 *         object.
	 */
	public boolean equals(Object object) {
		if (object instanceof AuthorizationAttribute) {
			AuthorizationAttribute attr = (AuthorizationAttribute) object;
			if (attr.getName().equals(name)
				&& attr.getAllowSSO() == getAllowSSO()
				&& attr.getSecLevel() == secLevel)
				return true;
		}
		return false;
	}

	/**
	 * Initialize security level register.
	 * 
	 * @return HashMap with seclevels
	 */
	private static HashMap initSecLevels() {
		HashMap secLevels = new HashMap();
		secLevels.put("HIGH", new Integer(3));
		secLevels.put("MEDIUM", new Integer(2));
		secLevels.put("LOW", new Integer(1));
		return secLevels;
	}

	/**
	 * Find the name for a given security level.
	 * 
	 * @param level Security level
	 * @return Security level name
	 */
	public static String secLevelName(int level) {

		for (Iterator it = secLevels.keySet().iterator(); it.hasNext();) {
			String key = (String) it.next();
			if (((Integer) secLevels.get(key)).intValue() == level)
				return key;
		}

		log.warning("Unknown security level: " + level);
		return "UNKNOWN";
	}

	/**
	 * @return secLevel
	 */
	public int getSecLevel() {
		return secLevel;
	}

	/**
	 * Get name of attribute.
	 * 
	 * @return Name of the attribute
	 */
	public String getName() {
		return name;
	}

	/**
	 * Is the attribute allowed in use with SSO?
	 * 
	 * @return True if the attribute can be used with SSO, else false
	 */
	public boolean getAllowSSO() {
		return allowSSO.booleanValue();
	}
}