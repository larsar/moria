/*
 * Copyright (c) 2004 UNINETT FAS
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
 *
 * $Id$
 */

package no.feide.moria.authorization;

import java.util.HashMap;
import java.util.HashSet;

/**
 * Represents a web service. A web service has a name, id, url and attributes.
 * The attributes are flattened (for optimization) from a set of profiles,
 * allowed and denied attributes.
 */
final class AuthorizationClient {

    /**
     * Cached hashCode.
     */
    private volatile int hashCode = 0;

    /**
     * Unique identifier (principal) for the client.
     */
    private final String name;

    /**
     * Common name of the service.
     */
    private final String displayName;

    /**
     * Home page URL for web service. Used for creating hyperlinks (together
     * with the name of the web service).
     */
    private final String url;

    /**
     * Language preferred by the web service.
     */
    private final String language;

    /**
     * The organization the webservice sets as default. Typically this is set to
     * the organization that the web service belongs to.
     */
    private final String home;

    /**
     * The organizations that the service belongs to.
     */
    private final HashSet affiliation;

    /**
     * The operations the client can perform.
     */
    private final HashSet operations;

    /**
     * The subsystems the client can use proxy authentication for.
     */
    private final HashSet subsystems;

    /**
     * Attributes the client can query.
     */
    private final HashMap attributes;

    /**
     * The properties of this object. Used to transport internal data outside of
     * the package.
     */
    private final HashMap properties = new HashMap();


    /**
     * Constructor.
     * @param name
     *            serviceID
     * @param displayName
     *            full name of service
     * @param url
     *            URL to the service main page
     * @param language
     *            default language for the service
     * @param home
     *            service home organization
     * @param affiliation
     *            the organizations affiliated to the service
     * @param operations
     *            operations that the service can perform
     * @param attributes
     *            attributes the service can access
     * @param subsystems
     *            subsystems the service can create proxy ticket for
     */
    AuthorizationClient(final String name, final String displayName, final String url, final String language, final String home, final HashSet affiliation, final HashSet operations, final HashSet subsystems, final HashMap attributes) {

        if (name == null || name.equals("")) { throw new IllegalArgumentException("Name must be a non empty string."); }

        if (displayName == null || displayName.equals("")) { throw new IllegalArgumentException("displayName must be a non empty string."); }

        if (url == null || url.equals("")) { throw new IllegalArgumentException("URL must be a non empty string."); }

        if (language == null || language.equals("")) { throw new IllegalArgumentException("Language must be a non empty string."); }

        if (home == null || home.equals("")) { throw new IllegalArgumentException("Home must be a non empty string."); }

        if (affiliation == null) { throw new IllegalArgumentException("Affiliation cannot be null."); }

        if (operations == null) { throw new IllegalArgumentException("Operations cannot be null."); }

        if (subsystems == null) { throw new IllegalArgumentException("Subsystems cannot be null."); }

        if (attributes == null) { throw new IllegalArgumentException("Attribtues cannot be null."); }

        this.name = name;
        this.displayName = displayName;
        this.url = url;
        this.language = language;
        this.home = home;
        this.affiliation = affiliation;
        this.operations = operations;
        this.subsystems = subsystems;
        this.attributes = attributes;

        // TODO: At least "language" is referenced directly in LoginServlet,
        // through constant in RequestUtil. Same constant should be used there
        // and here!
        properties.put("displayName", new String(displayName));
        properties.put("url", new String(url));
        properties.put("language", new String(language));
        properties.put("home", new String(home));
        properties.put("name", new String(name));

    }


    /**
     * Check all if all the requested attributes are legal for this web service.
     * @param requestedAttributes
     *            Names of all requested attributes.
     * @return true if access to the attributes is granted, else false
     */
    boolean allowAccessTo(final String[] requestedAttributes) {

        boolean allow = true;

        if (requestedAttributes == null) { throw new IllegalArgumentException("RequestedAttributes cannot be null"); }

        if (requestedAttributes.length == 0) { return true; }

        for (int i = 0; i < requestedAttributes.length; i++) {
            if (!attributes.containsKey(requestedAttributes[i])) {
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
     * @param requestedAttributes
     *            The names of all requested attributes
     * @return true if the attributes can be used with SSO, else false
     */
    boolean allowSSOForAttributes(final String[] requestedAttributes) {

        boolean allow = true;

        if (requestedAttributes == null) { throw new IllegalArgumentException("requestedAttributes cannot be null"); }

        for (int i = 0; i < requestedAttributes.length; i++) {
            final String attrName = requestedAttributes[i];
            if (!attributes.containsKey(attrName) || !((AuthorizationAttribute) attributes.get(attrName)).getAllowSSO()) {
                allow = false;
                break;
            }
        }
        return allow;
    }


    /**
     * Returns true if the supplied organization name is affiliated with the
     * client.
     * @param organization
     *            Name of the organization to match
     * @return True if the supplied organization name is affiliated with the
     *         client
     */
    boolean hasAffiliation(final String organization) {

        if (organization == null || organization.equals("")) { throw new IllegalArgumentException("Organization must be a non empty string"); }

        return affiliation.contains(organization);
    }


    /**
     * Returns true if all elements in the requestedOperations array is
     * represented in the objects operations set.
     * @param requestedOperations
     *            A string array of operation names
     * @return True if all operations are allowed, else false.
     */
    boolean allowOperations(final String[] requestedOperations) {

        if (requestedOperations == null) { throw new IllegalArgumentException("RequestedOperations cannot be null"); }

        if (requestedOperations.length == 0) { return true; }

        for (int i = 0; i < requestedOperations.length; i++) {
            if (!operations.contains(requestedOperations[i])) { return false; }
        }

        return true;
    }


    /**
     * Returns true if all elements in the requestedOperations array is
     * represented in the objects operations set.
     * @param requestedSubsystems
     *            A string array of operation names
     * @return True if all operations are allowed, else false.
     */
    boolean allowSubsystems(final String[] requestedSubsystems) {

        if (requestedSubsystems == null) { throw new IllegalArgumentException("RequestedSubsystems cannot be null"); }

        if (requestedSubsystems.length == 0) { return true; }

        for (int i = 0; i < requestedSubsystems.length; i++) {
            if (!subsystems.contains(requestedSubsystems[i])) { return false; }
        }

        return true;
    }


    /**
     * Compares object with another, returnes true if all fields are equal.
     * @param object
     *            The object to compare with
     * @return True if objects are equal
     */
    public boolean equals(final Object object) {

        if (object == this) { return true; }
        if (object instanceof AuthorizationClient) {
            final AuthorizationClient client = (AuthorizationClient) object;
            if (client.getName().equals(name) && client.getDisplayName().equals(displayName) && client.getURL().equals(url) && client.getLanguage().equals(language) && client.getHome().equals(home) && client.getAffiliation().equals(affiliation) && client.getOperations().equals(operations) && client.getSubsystems().equals(subsystems) && client.getAttributes().equals(attributes)) { return true; }
        }
        return false;
    }


    /**
     * Generate a hash code for the object. The hash code is computed from all
     * fields.
     * @return The hash code
     */
    public int hashCode() {

        if (hashCode == 0) {
            int result = 17;
            result = 37 * result + name.hashCode();
            result = 37 * result + displayName.hashCode();
            result = 37 * result + url.hashCode();
            result = 37 * result + language.hashCode();
            result = 37 * result + home.hashCode();
            result = 37 * result + affiliation.hashCode();
            result = 37 * result + operations.hashCode();
            result = 37 * result + subsystems.hashCode();
            result = 37 * result + attributes.hashCode();
            hashCode = result;
        }
        return hashCode;

    }


    /**
     * Returns a string representation of this object.
     * @return A string representation of this object: "Name: NAME DisplayName:
     *         DISPLAYNAME URL: URL Language: LANGUAGE Home: HOME Affiliations:
     *         AFFILIATION Operations: OPERATIONS Attributes: ATTRIBUTES
     */
    public String toString() {

        return "Name: " + name + " DisplayName: " + displayName + " URL: " + url + " Language: " + language + " Home: " + home + " Affiliation: " + affiliation + " Operations: " + operations + "Attributes: " + attributes;
    }


    /**
     * Returns the URL for this client.
     * @return The URL for the main page of the client service.
     */
    public String getURL() {

        return new String(url);
    }


    /**
     * Returns the principal of this client.
     * @return Clients principal.
     */
    public String getName() {

        return new String(name);
    }


    /**
     * Returns the display name for this client.
     * @return Name of the client, to be displayed to the user.
     */
    public String getDisplayName() {

        return new String(displayName);
    }


    /**
     * Returns the language for this client.
     * @return Language of the client service.
     */
    public String getLanguage() {

        return new String(language);
    }


    /**
     * Returns the home organization for this client.
     * @return The short for the home organization of the client service.
     */
    public String getHome() {

        return new String(home);
    }


    /**
     * Returns the affiliation for this client.
     * @return Returns the affiliation.
     */
    HashSet getAffiliation() {

        return new HashSet(affiliation);
    }


    /**
     * Returns the operations for this client.
     * @return Returns the operations.
     */
    HashSet getOperations() {

        return new HashSet(operations);
    }


    /**
     * Returns the subsystems for this client.
     * @return Returns the operations.
     */
    HashSet getSubsystems() {

        return new HashSet(subsystems);
    }


    /**
     * Returns the attributes for this client.
     * @return Returns the attributes.
     */
    HashMap getAttributes() {

        return new HashMap(attributes);
    }


    /**
     * Get the properties for this client. The properties object contains the
     * data that should be transferred to other packages.
     * @return the properties for this object
     */
    public HashMap getProperties() {

        return properties;
    }


    /**
     * Return the highest secLevel for the requested attributes.
     * @param requestedAttributes
     *            the requested attributes
     * @return the highest of the attributes seclevel, 0 if no attributes are
     *         requested
     * @throws UnknownAttributeException
     *             if one (or more) of the requested attributes are not present
     *             in the authorization client
     */
    int getSecLevel(final String[] requestedAttributes)
    throws UnknownAttributeException {

        if (requestedAttributes == null) { throw new IllegalArgumentException("requestedAttributes cannot be null"); }

        if (requestedAttributes.length == 0) { return 0; }

        int res = 0;
        for (int i = 0; i < requestedAttributes.length; i++) {
            final AuthorizationAttribute authzAttribute = (AuthorizationAttribute) attributes.get(requestedAttributes[i]);
            if (authzAttribute == null) { throw new UnknownAttributeException("Attribute '" + authzAttribute + "' does not exist."); }
            if (authzAttribute.getSecLevel() > res) {
                res = authzAttribute.getSecLevel();
            }
        }

        return res;
    }
}