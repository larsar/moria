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

import no.feide.moria.log.MessageLogger;

/**
 * Represents a web service. A web service has a name, id, url and attributes.
 * The attributes are flattened (for optimization) from a set of profiles,
 * allowed and denied attributes.
 */
final class AuthorizationClient {

    /** Used for logging. */
    private final MessageLogger log = new MessageLogger(AuthorizationClient.class);

    /**
     * Cached hash code.
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
     * The organizations that can use the service.
     */
    private final HashSet orgsAllowed;

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
     * Constructor. Creates a new object describing a Moria service client, used
     * for authorization purposes.
     * @param name
     *            serviceID The unique client ID assigned to this service.
     *            Cannot be <code>null</code> or an empty string.
     * @param displayName
     *            Full name of the service, for display purposes. Cannot be
     *            <code>null</code> or an empty string.
     * @param url
     *            URL to the service main page, where information on the service
     *            should be found. Cannot be <code>null</code> or an empty
     *            string.
     * @param language
     *            Default language for the service. Must match one of the
     *            configured languages. Cannot be <code>null</code> or an
     *            empty string.
     * @param home
     *            Service home organization. Must match one of the configured
     *            organizations. Cannot be <code>null</code> or an empty
     *            string.
     * @param affiliation
     *            The organizations affiliated to the service. Cannot be
     *            <code>null</code>.
     * @param orgsAllowed
     *            The organizations that are allowed to use the service. Cannot be null.
     * @param operations
     *            Operations that the service can perform. Cannot be
     *            <code>null</code>.
     * @param subsystems
     *            Subsystems the service can create proxy tickets for. May be
     *            <code>null</code>.
     * @param attributes
     *            Attributes the service can access. Cannot be <code>null</code>.
     *
     * @throws IllegalArgumentException
     *             If any of <code>name</code>,<code>displayName</code>,
     *             <code>url</code>,<code>language</code>,
     *             <code>home</code>,<code>affiliation</code>,
     *                     <code>allowedOrg</code>,
     *             <code>operations</code>, or <code>attributes</code> are
     *             <code>null</code> or an empty string (where applicable).
     */
      AuthorizationClient(final String name, final String displayName, final String url, final String language, final String home, final HashSet affiliation, final HashSet orgsAllowed, final HashSet operations, final HashSet subsystems, final HashMap attributes) {

        if (name == null || name.equals("")) { throw new IllegalArgumentException("Name must be a non empty string."); }

        if (displayName == null || displayName.equals("")) { throw new IllegalArgumentException("displayName must be a non empty string."); }

        if (url == null || url.equals("")) { throw new IllegalArgumentException("URL must be a non empty string."); }

        if (language == null || language.equals("")) { throw new IllegalArgumentException("Language must be a non empty string."); }

        if (home == null || home.equals("")) { throw new IllegalArgumentException("Home must be a non empty string."); }

        if (affiliation == null) { throw new IllegalArgumentException("Affiliation cannot be null."); }

        if (orgsAllowed == null) { throw new IllegalArgumentException("OrgsAllowed cannot be null."); }

        if (operations == null) { throw new IllegalArgumentException("Operations cannot be null."); }

        if (attributes == null) { throw new IllegalArgumentException("Attributes cannot be null."); }

        // Assign.
        this.name = name;
        this.displayName = displayName;
        this.url = url;
        this.language = language;
        this.home = home;
        this.affiliation = affiliation;
        this.orgsAllowed = orgsAllowed;
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
     * Checks if all the requested attributes are legal for this web service.
     * @param requestedAttributes
     *            Names of all requested attributes.
     * @return true if access to the attributes is granted, else false.
     * @throws IllegalArgumentException
     *             If <code>requestedAttributes</code> is <code>null</code>.
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
     * Checks attributes for use with single sign-on (SSO). If all attributes
     * are registered in
     * the web service's attributes list and all attributes are allowed to use
     * with SSO, then so be it.
     * @param requestedAttributes
     *            The names of all requested attributes.
     * @return true if the attributes can be used with SSO, else false.
     * @throws IllegalArgumentException
     *             If <code>requestedAttributes</code> is <code>null</code>.
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
     *            Name of the organization to match.
     * @return true if the supplied organization name is affiliated with the
     *         client.
     * @throws IllegalArgumentException
     *             If <code>organization</code> is <code>null</code> or
     *             an empty string.
     */
    boolean hasAffiliation(final String organization) {

        if (organization == null || organization.equals("")) { throw new IllegalArgumentException("Organization must be a non empty string"); }

        return affiliation.contains(organization);
    }


    /**
     * Returns true if all elements in the requestedOperations array are
     * represented in the objects operations set.
     * @param requestedOperations
     *            A string array of operation names
     * @return true if all operations are allowed, else false.
     * @throws IllegalArgumentException
     *             If <code>requestedOperations</code> is <code>null</code>.
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
     * Returns true for the organizations that are allowed to use this service.
     *
     * @param organization
     *                          The organization requesting authorization.
     * @return true if the organization can use this service.
     * @throws IllegalArgumentException
     *             If <code>organization</code> is <code>null</code>.
     */
    boolean allowUserorg(final String organization) {

        //Sanity check.
        if (organization == null) {
            log.logInfo("organization = " + organization);
            throw new IllegalArgumentException("Organization cannot be null");
        }
        //If no allowed organizations are defined, then all denied
        if (orgsAllowed == null) {
            log.logInfo("orgsAllowed = " + orgsAllowed);
            return false;
        }
        return orgsAllowed.contains(organization);
    }


    /**
     * Used to decide whether subsystems are allowed for this particular client,
     * based on its configuration.
     * @param requestedSubsystems
     *            A string array of subsystem names. Cannot be <code>null</code>.
     * @return <code>true</code> if subsystems are allowed, otherwise
     *         <code>false</code>.
     * @throws IllegalArgumentException
     *             If <code>requestedSubsystems</code> is <code>null</code>.
     */
    boolean allowSubsystems(final String[] requestedSubsystems) {

        // Sanity checks.
        if (requestedSubsystems == null) {
            log.logInfo("requestedSubsystems = " + requestedSubsystems);
            throw new IllegalArgumentException("RequestedSubsystems cannot be null");
        }

        // No subsystems requested or defined? Then we won't allow 'em!
        if ((requestedSubsystems.length == 0) || (subsystems == null)) {
            log.logInfo("requestedSubsystems.length = " + requestedSubsystems.length);
            log.logInfo("subsystems = " + subsystems);
            return false;
        }

        // If all the requested subsystems are defined for this service, we're
        // allowing it.
        for (int i = 0; i < requestedSubsystems.length; i++)
            if (!subsystems.contains(requestedSubsystems[i]))
                return false; // Ouch! A requested subsystem wasn't defined!
        return true;
    }


    /**
     * Compares object with another, returnes true if all fields are equal.
     * @param object
     *            The object to compare with.
     * @return true if objects are equal.
     */
    public boolean equals(final Object object) {

        if (object == this) { return true; }
        if (object instanceof AuthorizationClient) {
            final AuthorizationClient client = (AuthorizationClient) object;
            if (client.getName().equals(name) && client.getDisplayName().equals(displayName) && client.getURL().equals(url) && client.getLanguage().equals(language) && client.getHome().equals(home) && client.getAffiliation().equals(affiliation) && client.getOrgsAllowed().equals(orgsAllowed) && client.getOperations().equals(operations) && client.getSubsystems().equals(subsystems) && client.getAttributes().equals(attributes)) { return true; }
        }
        return false;
    }


    /**
     * Generate a hash code for the object. The hash code is computed from all
     * fields.
     * @return The hash code.
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
            result = 37 * result + orgsAllowed.hashCode();
            result = 37 * result + operations.hashCode();
            result = 37 * result + subsystems.hashCode();
            result = 37 * result + attributes.hashCode();
            hashCode = result;
        }
        return hashCode;

    }


    /**
     * Returns a string representation of this object.
     * @return A string representation of this object:
     * Name: NAME DisplayName:
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
     * @return Client's principal.
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
     * @return The home organization of the client service.
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
     * Returns the organizations that are allowed to use the client.
     * @return The organizations.
     */
    HashSet getOrgsAllowed() {

        return new HashSet(orgsAllowed);
    }


    /**
     * Returns the operations for this client.
     * @return Returns the operations.
     */
    HashSet getOperations() {

        return new HashSet(operations);
    }


    /**
     * Returns the subsystems for this client, if any are defined.
     * @return A new <code>HashSet</code> object containing the defined
     *         subsystems, or <code>null</code> if no subsystems are defined
     *         for this client.
     */
    HashSet getSubsystems() {

        if (subsystems == null)
            return null;
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
     * Gets the properties for this client. The properties object contains the
     * data that should be transferred to other packages.
     * @return The properties for this object.
     */
    public HashMap getProperties() {

        return properties;
    }


    /**
     * Returns the highest secLevel of the requested attributes.
     * @param requestedAttributes
     *            The requested attributes.
     * @return The highest of the attributes seclevel, 0 if no attributes are
     *         requested.
     * @throws UnknownAttributeException
     *             if one (or more) of the requested attributes are not present
     *             in the authorization client.
     * @throws IllegalArgumentException
     *             If <code>requestedAttributes</code> is <code>null</code>.
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
