/**
 * Copyright (C) 2003 FEIDE
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package no.feide.moria.authorization;

import java.util.HashMap;
import java.util.Iterator;
import java.util.logging.Logger;

/**
 * Represents a web service. A web service has a name, id, url and attributes.
 * The attributes are flattened (for optimization) from a set of profiles,
 * allowed and denied attributes.
 */
public class AuthorizationClient {

	/** Used for logging. */
	private static Logger log = Logger.getLogger(AuthorizationClient.class.toString());

	/** A unique id */
	private String id;

	/**
	 * List of attributes that the web service is associated with. Each profile
	 * is connected with a set of attributes and the web serivce are allowed to
	 * use all attributes in it's profiles. Overridden by allowedAttributes and
	 * deniedAttributes.
	 */
	private HashMap profiles = new HashMap();

	/**
	 * List of attributes that the web service is allowe to use. Overridden by
	 * deniedAttributes.
	 */
	private HashMap allowedAttributes = new HashMap();

	/**
	 * List of attributes that the web service is prohibited from using. These
	 * overrides both the attributes given from the profiles and the web
	 * service's allowedAttributes.
	 */
	private HashMap deniedAttributes = new HashMap();

	/**
	 * Combined list of attributes based on: all profiles attributes +
	 * allowedAttributes - deniedAttributes.
	 */
	private HashMap attributes = new HashMap();

	/** Name of web service */
	private String name;

	/**
	 * Home page URL for web service. Used for creating hyperlinks (together
	 * with the name of the web service).
	 */
	private String url;

	/** Language preferred by the web service. */
	private String defaultLang;

	/**
	 * The organization the webservice sets as default. Typically this is set
	 * to the organization that the web service belongs to.
	 */
	private String defaultOrg;

	/** The organizations that the service belongs to. */
	private String[] affiliations = new String[] {
	};

	/** Flag if the web service allows local authentication. */
	private boolean allowLocalAuth = false;

	/** Is direct authentication allowed by this web service? */
	private boolean directAuthenticationAllowed = false;

	/**
	 * Private constructor. Should not be called by any one.
	 */
	private AuthorizationClient() {
		// Constructor not allowed to be called without parameters
	}

	/**
	 * Constructor
	 * 
	 * @param id Unique id for the web service.
	 */
	AuthorizationClient(String id) {
		
		if (id == null || id.equals("")) 
			throw new IllegalArgumentException("Id must be a non empty string.");
		this.id = id;
	}

	/**
	 * Check all if all the requested attributes are legal for this web
	 * service.
	 * 
	 * @param requestedAttributes Names of all requested attributes.
	 */
	public boolean allowAccessToAttributes(String requestedAttributes[]) {
		boolean allow = true;

		for (int i = 0; i < requestedAttributes.length; i++) {
			if (!attributes.containsKey(requestedAttributes[i])) {
				log.warning(
					"Service "
						+ id
						+ "can access attributes"
						+ attributes.keySet()
						+ " only, not ["
						+ requestedAttributes[i]
						+ ']');
				allow = false;
				break;
			}
		}
		return allow;
	}

	/**
	 * Check attributes for use with SSO. If all attributes are registered in
	 * the web services's attributes list and all attributes are allowed to use
	 * with SSO, then so be it.
	 * 
	 * @param requestedAttributes The names of all requested attributes
	 */
	public boolean allowSSOForAttributes(String requestedAttributes[]) {
		boolean allow = true;
		for (int i = 0; i < requestedAttributes.length; i++) {
			String attrName = requestedAttributes[i];
			if (!attributes.containsKey(attrName)
				|| !((AuthorizationAttribute) attributes.get(attrName)).getAllowSSO()) {
				allow = false;
				break;
			}
		}
		return allow;
	}

	/**
	 * Flatten all attributes into one HashMap (profiles.attributes +
	 * allowedAttributes - deniedAttributes
	 */
	void generateAttributeList(HashMap allAttributes) {

		/* Profiles */
		for (Iterator profIt = profiles.keySet().iterator(); profIt.hasNext();) {
			AuthorizationProfile profile = (AuthorizationProfile) profiles.get(profIt.next());
			HashMap profileAttrs = profile.attributes();

			alterAttributes(allAttributes, profileAttrs, true);
		}

		/* Allowed attributes */
		alterAttributes(allAttributes, allowedAttributes, true);

		/* Denied attributes */
		alterAttributes(allAttributes, deniedAttributes, false);

		/* Delete old datastructure to release memory. */
		allowedAttributes = null;
		deniedAttributes = null;
		profiles = null;
	}

	/**
	 * Adds or removes attributes from the flattened datastructure.
	 * 
	 * @param allAttributes The hashmap to add or remove from
	 * @param changes The hashmap with the changes to be committed
	 * @param add true=add, false=remove
	 */
	private void alterAttributes(HashMap allAttributes, HashMap changes, boolean add) {
		for (Iterator attrIt = changes.keySet().iterator(); attrIt.hasNext();) {
			String attrName = (String) attrIt.next();

			/* Add */
			if (add) {
				AuthorizationAttribute addAttr = (AuthorizationAttribute) changes.get(attrName);
				AuthorizationAttribute origAttr = (AuthorizationAttribute) allAttributes.get(attrName);

				/*
				 * If attribute's secLevel is higher than the previously
				 * defined, then set it to the new value.
				 */
				int secLevel = origAttr.getSecLevel();
				if (addAttr.getSecLevel() > secLevel)
					secLevel = addAttr.getSecLevel();

				attributes.put(
					attrName,
					new AuthorizationAttribute(
						attrName,
						(addAttr.getAllowSSO() && origAttr.getAllowSSO()),
						AuthorizationAttribute.secLevelName(secLevel)));
			}

			/* Remove */
			else {
				attributes.remove(attrName);
			}
		}
	}

	/**
	 * Return name of security level for a given set of attributes.
	 * 
	 * @param requestedAttributes Names of all requested attributes.
	 */
	public String secLevelNameForAttributes(String requestedAttributes[]) {
		int highestLevel = 1;

		for (int i = 0; i < requestedAttributes.length; i++) {
			String attrName = requestedAttributes[i];
			int attrSecLevel = ((AuthorizationAttribute) attributes.get(attrName)).getSecLevel();
			if (attributes.containsKey(attrName) && attrSecLevel > highestLevel) {
				highestLevel = attrSecLevel;
			}
		}
		return AuthorizationAttribute.secLevelName(highestLevel);
	}

	/**
	 * Set web service's allowed attributes
	 * 
	 * @param allowed Allowed attributes
	 */
	void setAllowedAttributes(HashMap allowed) {
		allowedAttributes = allowed;
	}

	/**
	 * Set web service's denied attributes
	 * 
	 * @param denied Denied attributes
	 */
	void setDeniedAttributes(HashMap denied) {
		deniedAttributes = denied;
	}

	/**
	 * Set web service's profiles
	 * 
	 * @param profiles Profiles associated with the web service
	 */
	void setProfiles(HashMap profiles) {
		this.profiles = profiles;
	}

	/**
	 * Set web service's name
	 * 
	 * @param name The name of the web service
	 */
	void setName(String name) {
		this.name = name;
	}

	/**
	 * Set web service's home page url
	 * 
	 * @param url URL for the home page of the web service
	 */
	void setUrl(String url) {
		this.url = url;
	}

	/**
	 * Get home page URL
	 */
	public String getUrl() {
		return url;
	}

	/**
	 * Get web service name
	 */
	public String getName() {
		return name;
	}

	/**
	 * List of all attributes a web service is allowed to use.
	 */
	public HashMap getAttributes() {
		return attributes;
	}

	/**
	 * Get web service's unique id.
	 */
	public String getId() {
		return id;
	}

	/**
	 * @return Returns the defaultLang.
	 */
	public String getDefaultLang() {
		return defaultLang;
	}

	/**
	 * @param defaultLang The defaultLang to set.
	 */
	void setDefaultLang(String preferredLanguage) {
		this.defaultLang = preferredLanguage;
	}

	/**
	 * @return Returns the defaultOrg.
	 */
	public String getDefaultOrg() {
		return defaultOrg;
	}

	/**
	 * @param defaultOrg The defaultOrg to set.
	 */
	void setDefaultOrg(String defaultOrg) {
		this.defaultOrg = defaultOrg;
	}

	/**
	 * @return Return true if the web service allows the use of secondary
	 *         authentication agains a another authentication mechanism.
	 */
	public boolean allowsLocalAuth() {
		return allowLocalAuth;
	}

	/**
	 * Return true if the supplied organization name is in the affiliation
	 * list.
	 * 
	 * @param affiliation The name of the affiliation
	 * @return boolean
	 */
	public boolean hasAffiliation(String affiliation) {

		if (affiliation == null)
			return false;

		for (int i = 0; i < affiliations.length; i++) {
			if (affiliations[i].equals(affiliation))
				return true;
		}

		return false;
	}

	/**
	 * @param allowLocalAuth True if the web service allows secondary
	 *            authentication methods, else false.
	 */
	void setAllowLocalAuth(boolean allowLocalAuth) {
		this.allowLocalAuth = allowLocalAuth;
	}

	/**
	 * @return Returns the affiliations.
	 */
	String[] getAffiliations() {
		return affiliations;
	}

	/**
	 * @param affiliations The affiliations to set.
	 */
	void setAffiliations(String[] affiliation) {
		this.affiliations = affiliation;
	}

	/**
	 * Is this web service allowed to do direct user authentication?
	 * 
	 * @return <code>true</code> if yes, otherwise <code>false</code>.
	 */
	public boolean allowDirectAuthentication() {
		return directAuthenticationAllowed;
	}

	/**
	 * Specifies if this web service may use direct authentication or not.
	 * Default is <code>false</code>.
	 * 
	 * @param allowed Is the web service allowed to use direct authentication?
	 */
	public void setDirectAuthenticationAllowed(boolean allowed) {
		directAuthenticationAllowed = allowed;
	}

}
