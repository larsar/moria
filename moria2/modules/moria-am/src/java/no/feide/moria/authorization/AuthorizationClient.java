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
import java.util.HashSet;
import java.util.logging.Logger;

/**
 * Represents a web service. A web service has a name, id, url and attributes.
 * The attributes are flattened (for optimization) from a set of profiles,
 * allowed and denied attributes.
 */
public class AuthorizationClient {

    /**
     * Used for logging.
     */
    private static Logger log = Logger.getLogger(AuthorizationClient.class.toString());

    /**
     * Cached hashCode
     */
    private volatile int hashCode = 0;

    /**
     * Unique identifier (principal) for the client
     */
    private String name;

    /**
     * Common name of the service
     */
    private String displayName;

    /**
     * Home page URL for web service. Used for creating hyperlinks (together
     * with the name of the web service).
     */
    private String url;

    /**
     * Language preferred by the web service.
     */
    private String language;

    /**
     * The organization the webservice sets as default. Typically this is set
     * to the organization that the web service belongs to.
     */
    private String home;

    /**
     * The organizations that the service belongs to.
     */
    private HashSet affiliation;

    /**
     * The operations the client can perform
     */
    private HashSet operations;

    /**
     * Attributes the client can query
     */
    private HashMap attributes = new HashMap();

    /**
     * Private constructor. Should not be called by any one.
     */
    private AuthorizationClient() {
        // Constructor is not allowed to be called without parameters.
    }

    /**
     * Constructor
     *
     * @param id Unique id for the web service.
     */
    AuthorizationClient(String name, String displayName, String url, String language, String home, HashSet affiliation, HashSet operations, HashMap attributes) {

        if (name == null || name.equals(""))
            throw new IllegalArgumentException("Name must be a non empty string.");

        if (displayName == null || displayName.equals(""))
            throw new IllegalArgumentException("displayName must be a non empty string.");

        if (url == null || url.equals(""))
            throw new IllegalArgumentException("URL must be a non empty string.");

        if (language == null || language.equals(""))
            throw new IllegalArgumentException("Language must be a non empty string.");

        if (home == null || home.equals(""))
            throw new IllegalArgumentException("Home must be a non empty string.");

        if (affiliation == null)
            throw new IllegalArgumentException("Affiliation cannot be null.");

        if (operations == null)
            throw new IllegalArgumentException("Operations cannot be null.");

        if (attributes == null)
            throw new IllegalArgumentException("Attribtues cannot be null.");

        this.name = name;
        this.displayName = displayName;
        this.url = url;
        this.language = language;
        this.home = home;
        this.affiliation = affiliation;
        this.operations = operations;
        this.attributes = attributes;
    }

    /**
     * Check all if all the requested attributes are legal for this web
     * service.
     *
     * @param requestedAttributes Names of all requested attributes.
     */
    public boolean allowAccessTo(String requestedAttributes[]) {
        boolean allow = true;

        if (requestedAttributes == null)
            throw new IllegalArgumentException("RequestedAttributes cannot be null");

        if (requestedAttributes.length == 0)
            throw new IllegalArgumentException("RequestedAttributes cannot be empty");

        for (int i = 0; i < requestedAttributes.length; i++) {
            if (!attributes.containsKey(requestedAttributes[i])) {
                log.warning("Service " + name + "can access attributes" + attributes.keySet() + " only, not [" + requestedAttributes[i] + ']');
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
     *
     * @param organization Name of the organization to match
     * @return True if the supplied organization name is affiliated with the
     *         client
     */
    public boolean hasAffiliation(String organization) {
        if (organization == null || organization.equals(""))
            throw new IllegalArgumentException("Organization must be a non empty string");
        return affiliation.contains(organization);
    }

    /**
     * Returns true if all elements in the requestedOperations array is
     * represented in the objects operations set.
     *
     * @param requestedOperations A string array of operation names
     * @return True if all operations are allowed, else false.
     */
    public boolean allowOperations(String[] requestedOperations) {

        if (requestedOperations == null)
            throw new IllegalArgumentException("RequestedOperations cannot be null");

        if (requestedOperations.length == 0)
            throw new IllegalArgumentException("RequestedOperations cannot be empty");

        for (int i = 0; i < requestedOperations.length; i++) {
            if (!operations.contains(requestedOperations[i])) {
                log.warning("Service " + name + "can perform operations" + operations + " only, not [" + requestedOperations[i] + ']');

                return false;
            }
        }

        return true;
    }

    /**
     * Compares object with another, returnes true if all fields are equal.
     *
     * @param Object The object to compare with
     * @return True if objects are equal
     */
    public boolean equals(Object object) {
        if (object == this)
            return true;
        if (object instanceof AuthorizationClient) {
            AuthorizationClient client = (AuthorizationClient) object;
            if (client.getName().equals(name) && client.getDisplayName().equals(displayName) && client.getURL().equals(url) && client.getLanguage().equals(language) && client.getHome().equals(home) && client.getAffiliation().equals(affiliation) && client.getOperations().equals(operations) && client.getAttributes().equals(attributes))
                return true;
        }
        return false;
    }

    /**
     * Generate a hash code for the object. The hash code is computed from all
     * fields.
     *
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
            result = 37 * result + attributes.hashCode();
            hashCode = result;
        }
        return hashCode;

    }

    public String toString() {
        return "Name: " + name + " DisplayName: " + displayName + " URL: " + url + " Language: " + language + " Home: " + home + " Affiliation: " + affiliation + " Operations: " + operations + "Attributes: " + attributes;
    }

    /**
     * @return The URL for the main page of the client service.
     */
    public String getURL() {
        return new String(url);
    }

    /**
     * @return Clients principal
     */
    public String getName() {
        return new String(name);
    }

    /**
     * @return Name of the client, to be displayed to the user
     */
    public String getDisplayName() {
        return new String(displayName);
    }

    /**
     * @return Language of the client service
     */
    public String getLanguage() {
        return new String(language);
    }

    /**
     * @return The short for the home organization of the client service.
     */
    public String getHome() {
        return new String(home);
    }

    /**
     * @return Returns the affiliation.
     */
    HashSet getAffiliation() {
        return new HashSet(affiliation);
    }

    /**
     * @return Returns the operations.
     */
    HashSet getOperations() {
        return new HashSet(operations);
    }

    /**
     * @return Returns the attributes.
     */
    HashMap getAttributes() {
        return new HashMap(attributes);
    }

}
