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
 * $Id$
 */
package no.feide.moria.authorization;

import no.feide.moria.log.MessageLogger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * The AuthorizationManager class is used to parse and store authorization data.
 * The authorization data source is XML which is passed as a properties object
 * through the setConfig method. The config data must contain information about
 * every web service allowed to access Moria, and which attributes, operations
 * and subsystems the service can access. <br>
 * <br>
 * When a new set of data arrives, the authorization manager parses it and
 * replaces the old dataset if the parsing was successful. The authorization
 * manager can then be used to answer authorization questions, most likely from
 * the Moria controller. <br>
 * <br>
 * When the controller receives a request, it asks the authorization manager if
 * the web service is authorized to perform the request. Every request includes
 * the service principal.
 * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
 * @version $Revision$
 */
public final class AuthorizationManager {

    /**
     * For logging of error messages that cannot be sent to the calling layer.
     */
    private final MessageLogger messageLogger = new MessageLogger(AuthorizationManager.class);

    /**
     * List of client authorization objects. Must be synchronized.
     */
    private HashMap authzClients = new HashMap();

    /**
     * List of attributes that is allowed to be cached.
     */
    private HashSet cachableAttributes = new HashSet();

    /**
     * True if the authorization manager is ready to be used.
     */
    private boolean activated = false;


    /**
     * Parses an XML element and creates an AuthorizationAttribute object in
     * return. Throws an IllegalConfigException if there is something wrong with
     * the element or its attributes.
     * @param element
     *            The XML element to parse.
     * @return AuthorizationAttribute with same attributes as the supplied
     *        <code>element</code>.
     * @throws IllegalConfigException
     *             If the element's sso attribute is not <code>true</code> or
     *             <code>false</code>.
     * @throws IllegalArgumentException
     *             If the <code>AuthorizationAttribute</code> constructor
     *             throws an exception.
     */
    static AuthorizationAttribute parseAttributeElem(final Element element)
    throws IllegalConfigException {

        String name = null;
        String secLevel = null;
        final String allowSSOStr;

        if (element.getAttribute("name") != null) {
            name = element.getAttribute("name").getValue();
        }

        if (element.getAttribute("sso") == null) {
            throw new IllegalConfigException("allowSSO has to be set.");
        } else {
            allowSSOStr = element.getAttribute("sso").getValue();
            if (!(allowSSOStr.equals("true") || allowSSOStr.equals("false"))) { throw new IllegalConfigException("allowSSO has to be 'true' or 'false'"); }
        }

        if (element.getAttribute("secLevel") != null) {
            secLevel = element.getAttribute("secLevel").getValue();
        }

        try {
            return new AuthorizationAttribute(name, new Boolean(allowSSOStr).booleanValue(), new Integer(secLevel).intValue());
        } catch (IllegalArgumentException e) {
            throw new IllegalConfigException("Illegal attributes: " + e.getMessage());
        }
    }


    /**
     * Parses the content of an Attributes element. The element can contain 0
     * or more Attribute elements which will be transformed into
     * AuthorizationAttributes and returned in a HashMap with attribute name as
     * key.
     * @param element
     *            The DOM element that contains <code>Attribute</code> 
     *            child elements.
     * @return HashMap with AuthorizationAttributes as value and 
     *         attribute name as key.
     * @throws IllegalConfigException
     *             If <code>element</code> is not of type
     *             <code>Attributes</code>.
     * @throws IllegalArgumentException
     *             If <code>element</code> is <code>null</code>.
     */
    static HashMap parseAttributesElem(final Element element)
    throws IllegalConfigException {

        final HashMap attributes = new HashMap();

        /* Validate element */
        if (element == null) { throw new IllegalArgumentException("Element cannot be null."); }

        if (!element.getName().equalsIgnoreCase("attributes")) { throw new IllegalConfigException("Element isn't of type 'Attributes'"); }

        /* Create AuthorizationAttribute of all child elements */
        final Iterator it = (element.getChildren()).iterator();
        while (it.hasNext()) {
            final AuthorizationAttribute attribute = parseAttributeElem((Element) it.next());
            attributes.put(attribute.getName(), attribute);
        }

        return attributes;
    }


    /**
     * Parses 'operation' and 'organization' elements and returns the name
     * attribute.
     * @param element
     *            The operation element.
     * @return String containing the name attribute of the element.
     * @throws IllegalConfigException
     *             If the element is not of type <code>Operation</code>,
     *             <code>Subsystem</code> or <code>Organization</code> OR
     *             element's <code>name</code> attribute is not set.
     * @throws IllegalArgumentException
     *             If <code>element</code> is <code>null</code> or an empty 
     *             string.
     */
    static String parseChildElem(final Element element)
    throws IllegalConfigException {

        if (element == null) { throw new IllegalArgumentException("Element cannot be null"); }

        if (!element.getName().equalsIgnoreCase("Operation") && !element.getName().equalsIgnoreCase("Subsystem") && !element.getName().equalsIgnoreCase("Organization")) { throw new IllegalConfigException("Element must be of type 'Operation', 'Subsystem' or 'Organization'"); }

        if (element.getAttribute("name") == null) { throw new IllegalConfigException("Element's name attribute must be set."); }

        if (element.getAttributeValue("name").equalsIgnoreCase("")) { throw new IllegalConfigException("Element's name attribute cannot be an empty string."); }

        return element.getAttributeValue("name");
    }


    /**
     * Parses the content of an Attributes element. The element can contain 
     * 0 or more Attribute elements which will be transformed into
     * AuthorizationAttributes and returned in a HashMap with attribute name as
     * key.
     * @param element
     *            The DOM element that contains Attribute child elements.
     * @return HashMap with AuthorizationAttributes as value and attribute name
     *         as key.
     * @throws IllegalConfigException
     *             If element is not of type <code>Operations</code>,
     *             <code>Affiliation</code>, <code>Subsystems</code> or
     *             <code>OrgsAllowed</code>.
     * @throws IllegalArgumentException
     *             If <code>element</code> is <code>null</code>.
     */
    static HashSet parseListElem(final Element element)
    throws IllegalConfigException {

        final HashSet operations = new HashSet();

        /* Validate element */
        if (element == null) { throw new IllegalArgumentException("Element cannot be null."); }

        if (!element.getName().equalsIgnoreCase("Operations") && !element.getName().equalsIgnoreCase("Subsystems") && !element.getName().equalsIgnoreCase("Affiliation") && !element.getName().equalsIgnoreCase("OrgsAllowed")) { throw new IllegalConfigException("Element isn't of type 'Operations', 'Subsystems', 'Affiliation' or 'OrgsAllowed'"); }

        /* Create AuthorizationAttribute of all child elements */
        final Iterator it = (element.getChildren()).iterator();
        while (it.hasNext()) {
            final String operation = parseChildElem((Element) it.next());
            operations.add(operation);
        }

        return operations;
    }


    /**
     * Creates an AuthorizationClient object based on the supplied XML element.
     * @param element
     *            The XML element representing the client service.
     * @return A new object representing the client service.
     * @throws IllegalConfigException
     *             If the <code>name</code> attribute is not set for the given
     *             element, or if any of the following tags are missing:
     *             <ul><code>
     *             <li>DisplayName
     *             <li>URL
     *             <li>Language
     *             <li>Home
     *             <li>Attributes
     *             <li>Operations
     *             <li>Affiliation
     *             <li>OrgsAllowed
     *             </code></ul>
     * @throws IllegalArgumentException
     *             If <code>element</code> is <code>null</code>.
     */
    static AuthorizationClient parseClientElem(final Element element)
    throws IllegalConfigException, IllegalArgumentException {

        // Sanity check.
        if (element == null)
            throw new IllegalArgumentException("Client element cannot be null");

        // Prepare some variables for later use.
        final String name;
        final String displayName;
        final String url;
        final String language;
        final String home;
        final HashSet oper;
        final HashSet affil;
        final HashSet orgsAllowed;
        HashSet subsys = null;
        final HashMap attrs;

        // Get and check name.
        name = element.getAttributeValue("name");
        if (name == null || name.equals(""))
            throw new IllegalConfigException("Name attribute must be a non empty string.");

        // Get other content. Error logging is done in called method.
        displayName = getChildContent(element, "DisplayName");
        url = getChildContent(element, "URL");
        language = getChildContent(element, "Language");
        home = getChildContent(element, "Home");

        // Parse attributes element.
        Element child = element.getChild("Attributes");
        if (child == null)
            throw new IllegalConfigException("Attributes tag (Attributes) not found for client '" + name + "'");
        attrs = parseAttributesElem(child);

        // Parse operations element.
        child = element.getChild("Operations");
        if (child == null)
            throw new IllegalConfigException("Operations tag (Operations) not found for client '" + name + "'");
        oper = parseListElem(child);

        // Parse affiliation element.
        child = element.getChild("Affiliation");
        if (child == null)
            throw new IllegalConfigException("Affiliations tag (Affiliation) not found for client '" + name + "'");
        affil = parseListElem(child);

        // Parse allowed organizations element.
        child = element.getChild("OrgsAllowed");
        if (child == null)
            throw new IllegalConfigException("Organizations allowed tag (OrgsAllowed) not found for client '" + name + "'");
        orgsAllowed = parseListElem(child);

        // Parse subsystems element, if it exists.
        child = element.getChild("Subsystems");
        if (child != null)
            subsys = parseListElem(child);

        return new AuthorizationClient(name, displayName, url, language, home, affil, orgsAllowed, oper, subsys, attrs);
    }


    /**
     * Parses a configuration root element with client elements.
     * @param element
     *            The root element.
     * @return A HashMap containing AuthorizationClient objects.
     * @throws IllegalConfigException
     *             If the element is not of type 
     *             <code>ClientAuthorizationConfig</code>.
     * @throws IllegalArgumentException
     *             If <code>element</code> is <code>null</code>.
     * @see AuthorizationClient
     */
    static HashMap parseRootElem(final Element element)
    throws IllegalConfigException {

        final HashMap clients = new HashMap();

        if (element == null) { throw new IllegalArgumentException("Element cannot be null"); }

        if (!element.getName().equalsIgnoreCase("ClientAuthorizationConfig")) { throw new IllegalConfigException("Wrong type of element: " + element.getName()); }

        final List children = element.getChildren("Client");
        final Iterator it = children.iterator();
        while (it.hasNext()) {
            final AuthorizationClient client = parseClientElem((Element) it.next());
            clients.put(client.getName(), client);
        }

        return clients;
    }


    /**
     * Retrieves the content of an XML element.
     * @param element
     *            Parent element.
     * @param childName
     *            Name of the child node.
     * @return The content of the child element.
     * @throws IllegalConfigException
     *             If the content of the child element is null.
     */
    private static String getChildContent(final Element element, final String childName)
    throws IllegalConfigException {

        final String value = element.getChildText(childName);
        if (value == null) {
            throw new IllegalConfigException(childName + " tag not found");
        } else {
            return value;
        }
    }


    /**
     * Returns a client object for a given identifier.
     * @param servicePrincipal
     *            The client object identifier.
     * @return The client object for the identifier.
     * @throws NoConfigException
     *             If the authorization manager is not activated.
     * @throws IllegalArgumentException
     *             If <code>servicePrincipal</code> is <code>null</code> 
     *             or an empty string.
     */
    private AuthorizationClient getAuthzClient(final String servicePrincipal) {

        /* Is the manager activated? */
        if (!activated) { throw new NoConfigException(); }

        /* Validate input parameters */
        if (servicePrincipal == null || servicePrincipal.equals("")) { throw new IllegalArgumentException("servicePrincipal must be a non-empty string."); }

        return (AuthorizationClient) authzClients.get(servicePrincipal);
    }


    /**
     * Validates a request for access to attributes for a given client/service.
     * @param servicePrincipal
     *            The identifier of the client.
     * @param requestedAttributes
     *            The list of requested attributes.
     * @return true if the service is allowed access, false if not or the client
     *         does not exist.
     * @throws UnknownServicePrincipalException
     *             If the service principal does not exist.
     */
    public boolean allowAccessTo(final String servicePrincipal, final String[] requestedAttributes)
    throws UnknownServicePrincipalException {

        final AuthorizationClient authzClient = getAuthzClient(servicePrincipal);

        if (authzClient == null) { throw new UnknownServicePrincipalException("Service principal does not exist: '" + servicePrincipal + "'"); }

        return authzClient.allowAccessTo(requestedAttributes);
    }


    /**
     * Validates a request for access to SSO for a given client/service.
     * @param servicePrincipal
     *            The identifier of the client.
     * @param requestedAttributes
     *            The list of requested attributes.
     * @return true if the service is allowed access, false if not or the client
     *         does not exist.
     * @throws UnknownServicePrincipalException
     *             If the service principal does not exist.
     */
    public boolean allowSSOForAttributes(final String servicePrincipal, final String[] requestedAttributes)
    throws UnknownServicePrincipalException {

        final AuthorizationClient authzClient = getAuthzClient(servicePrincipal);

        if (authzClient == null) { throw new UnknownServicePrincipalException("Service principal does not exist: '" + servicePrincipal + "'"); }

        return authzClient.allowSSOForAttributes(requestedAttributes);
    }


    /**
     * Validates a request for access to operations for a given client/service.
     * @param servicePrincipal
     *            The identifier of the client.
     * @param requestedOperations
     *            The list of requested operations.
     * @return true if the service is allowed access, false if not or the client
     *         does not exist.
     * @throws UnknownServicePrincipalException
     *             If the servicePrincipal does not exist.
     */
    public boolean allowOperations(final String servicePrincipal, final String[] requestedOperations)
    throws UnknownServicePrincipalException {

        final AuthorizationClient authzClient = getAuthzClient(servicePrincipal);

        if (authzClient == null) { throw new UnknownServicePrincipalException("Service principal does not exist: '" + servicePrincipal + "'"); }

        return authzClient.allowOperations(requestedOperations);
    }


    /**
     * Checks if the organization is allowed to use the service.
     * @param servicePrincipal
     *            The identifier of the client.
     * @param userorg
     *            The user's organization.
     * @return true if the organization is allowed to use the service, false if
     *         the client does not exists, or if the organization is not allowed
     *         to use the service.
     * @throws UnknownServicePrincipalException
     *             If the servicePrincipal does not exist.
     */
    public boolean allowUserorg(final String servicePrincipal, final String userorg)
    throws UnknownServicePrincipalException {

        final AuthorizationClient authzClient = getAuthzClient(servicePrincipal);

        if (authzClient == null) { throw new UnknownServicePrincipalException("Service principal does not exist: '" + servicePrincipal + "'"); }

        return authzClient.allowUserorg(userorg);
    }


    /**
     * Swaps the old client database with the supplied HashMap.
     * @param newClients
     *            The new client database.
     * @throws IllegalArgumentException
     *             If <code>newClients</code> is <code>null</code>.
     */
    synchronized void setAuthzClients(final HashMap newClients) {

        if (newClients == null) { throw new IllegalArgumentException("newClients to be set cannot be null"); }

        /* Generate a list of attributes that is allowed to be cached */
        final HashSet newCachableAttributes = new HashSet();
        final Iterator clientIt = newClients.keySet().iterator();
        while (clientIt.hasNext()) {
            final AuthorizationClient authzClient = (AuthorizationClient) newClients.get(clientIt.next());

            final HashMap attributes = authzClient.getAttributes();
            final Iterator attrIt = attributes.keySet().iterator();
            while (attrIt.hasNext()) {
                final AuthorizationAttribute attr = (AuthorizationAttribute) attributes.get(attrIt.next());
                if (attr.getAllowSSO()) {
                    newCachableAttributes.add(attr.getName());
                }
            }
        }

        /* Set new authorization configuration */
        synchronized (authzClients) {
            authzClients = newClients;
            cachableAttributes = newCachableAttributes;
            activated = true;
        }
    }


    /**
     * Sets the configuration data for this manager.
     * @param properties
     *            The properties containing the authorization database.
     * @throws IllegalArgumentException
     *             If <code>properties</code> is <code>null</code>.
     */
    public void setConfig(final Properties properties) {

        // Sanity checks.
        if (properties == null)
            throw new IllegalArgumentException("Properties cannot be null");
        final String fileName = (String) properties.get("authorizationDatabase");
        if (fileName == null || fileName.equals("")) {
            messageLogger.logWarn("The 'authorizationDatabase' property is not set or an empty string");
            return;
        }
        File database = new File(fileName);
        if (!database.exists()) {
            messageLogger.logWarn("Authorization database file '" + fileName + "' does not exist");
            return;
        }

        // Parse authorization database file.
        final SAXBuilder builder = new SAXBuilder();
        try {

            final Document doc = builder.build(database);
            final HashMap newClients = parseRootElem(doc.getRootElement());
            setAuthzClients(newClients);

        } catch (JDOMException e) {
            messageLogger.logWarn("Error parsing authorization database file '" + fileName + " - using old database", e);
        } catch (IOException e) {
            messageLogger.logWarn("Error reading authorization database file '" + fileName + " - using old database", e);
        } catch (IllegalConfigException e) {
            messageLogger.logWarn("Error generating authorization database - using old database", e);
        }

    }


    /**
     * Returns the service properties for a given service.
     * @param servicePrincipal
     *            The principal of the service.
     * @return A hashmap with properties for a given service.
     * @throws UnknownServicePrincipalException
     *             If the service principal does not exist.
     * @throws IllegalArgumentException
     *             If <code>servicePrincipal</code> is <code>null</code> or
     *             an empty string.
     * @see AuthorizationClient#getProperties()
     */
    public HashMap getServiceProperties(final String servicePrincipal)
    throws UnknownServicePrincipalException {

        /* Validate parameters */
        if (servicePrincipal == null || servicePrincipal.equals("")) { throw new IllegalArgumentException("servicePrincipal must be a non-empty string"); }

        final AuthorizationClient authzClient = getAuthzClient(servicePrincipal);
        if (authzClient == null) { throw new UnknownServicePrincipalException("Service principal does not exist: '" + servicePrincipal + "'"); }

        return authzClient.getProperties();
    }


    /**
     * Returns the security level for a set of attributes for a given service.
     * @param servicePrincipal
     *            The service principal of the requested service.
     * @param requestedAttributes
     *            The requested attributes.
     * @return Security level - an integer >= 0.
     * @throws UnknownServicePrincipalException
     *             If the service principal does not exist.
     * @throws UnknownAttributeException
     *             If one or more of the requested attributes does not exist.
     * @throws IllegalArgumentException
     *             If <code>servicePrincipal</code> is <code>null</code> or
     *             an empty string, or if <code>requestedAttributes</code> is 
     *             <code>null</code>.
     * @see AuthorizationClient#getSecLevel(java.lang.String[])
     */
    public int getSecLevel(final String servicePrincipal, final String[] requestedAttributes)
    throws UnknownServicePrincipalException, UnknownAttributeException {

        /* Validate arguments */
        if (servicePrincipal == null || servicePrincipal.equals("")) { throw new IllegalArgumentException("servicePrincipal must be a non-empty string"); }
        if (requestedAttributes == null) { throw new IllegalArgumentException("requestedAttributes cannot be null"); }

        final AuthorizationClient authzClient = getAuthzClient(servicePrincipal);
        if (authzClient == null) { throw new UnknownServicePrincipalException("Service principal does not exist: '" + servicePrincipal + "'"); }

        /* Return lowest seclevel if no attributes are requested */
        if (requestedAttributes.length == 0) { return 0; }

        return authzClient.getSecLevel(requestedAttributes);
    }


    /**
     * Returns the configured attributes for a given service.
     * @param servicePrincipal
     *            The principal of the requested service.
     * @return A string array with the attribute names that is configured for
     *         the service.
     * @throws UnknownServicePrincipalException
     *             If the servicePrincipal does not exist.
     * @throws IllegalArgumentException
     *             If <code>servicePrincipal</code> is <code>null</code> or
     *             an empty string.
     * @see AuthorizationClient#getAttributes()
     */
    public HashSet getAttributes(final String servicePrincipal)
    throws UnknownServicePrincipalException {

        /* Validate argument */
        if (servicePrincipal == null || servicePrincipal.equals("")) { throw new IllegalArgumentException("servicePrincipal must be a non-empty string"); }

        final AuthorizationClient authzClient = getAuthzClient(servicePrincipal);
        if (authzClient == null) { throw new UnknownServicePrincipalException("Service principal does not exist: '" + servicePrincipal + "'"); }

        return new HashSet(authzClient.getAttributes().keySet());
    }


    /**
     * Returns the organizations that can use this service.
     * @param servicePrincipal
     *            The principal of the requested service.
     * @return A string array with the names of the allowed organizations for
     *         the service.
     * @throws UnknownServicePrincipalException
     *             If the servicePrincipal does not exist.
     * @throws IllegalArgumentException
     *             If <code>servicePrincipal</code> is <code>null</code> or
     *             an empty string.
     * @see AuthorizationClient#getOrgsAllowed()
     */
    public HashSet getOrgsAllowed(final String servicePrincipal)
    throws UnknownServicePrincipalException {

        /* Validate argument */
        if (servicePrincipal == null || servicePrincipal.equals("")) { throw new IllegalArgumentException("servicePrincipal must be a non-empty string"); }

        final AuthorizationClient authzClient = getAuthzClient(servicePrincipal);
        if (authzClient == null) { throw new UnknownServicePrincipalException("Service principal does not exist: '" + servicePrincipal + "'"); }

        return authzClient.getOrgsAllowed();
    }


    /**
     * Returns the configured subsystems for a given service.
     * @param servicePrincipal
     *            The principal of the requested service,
     * @return A string array with the subsystem names that is configured for
     *         the service.
     * @throws UnknownServicePrincipalException
     *             If the servicePrincipal does not exist.
     * @throws IllegalArgumentException
     *             If <code>servicePrincipal</code> is <code>null</code> or
     *             an empty string.
     * @see AuthorizationClient#getSubsystems()
     */
    public HashSet getSubsystems(final String servicePrincipal)
    throws UnknownServicePrincipalException {

        /* Validate argument */
        if (servicePrincipal == null || servicePrincipal.equals("")) { throw new IllegalArgumentException("servicePrincipal must be a non-empty string"); }

        final AuthorizationClient authzClient = getAuthzClient(servicePrincipal);
        if (authzClient == null) { throw new UnknownServicePrincipalException("Service principal does not exist: '" + servicePrincipal + "'"); }

        return authzClient.getSubsystems();
    }


    /**
     * Returns the configured operations for a given service.
     * @param servicePrincipal
     *            The principal of the requested service.
     * @return A string array with the operation names that is configured for
     *         the service.
     * @throws UnknownServicePrincipalException
     *             If the servicePrincipal does not exist.
     * @throws IllegalArgumentException
     *             If <code>servicePrincipal</code> is <code>null</code> or
     *             an empty string.
     * @see AuthorizationClient#getOperations()
     */
    public HashSet getOperations(final String servicePrincipal)
    throws UnknownServicePrincipalException {

        /* Validate argument */
        if (servicePrincipal == null || servicePrincipal.equals("")) { throw new IllegalArgumentException("servicePrincipal must be a non-empty string"); }

        final AuthorizationClient authzClient = getAuthzClient(servicePrincipal);
        if (authzClient == null) { throw new UnknownServicePrincipalException("Service principal does not exist: '" + servicePrincipal + "'"); }

        return authzClient.getOperations();
    }


    /**
     * Returns the set of SSO attributes names (the attributes that can be
     * cached).
     * @return A set of attributes that can be cached.
     */
    public HashSet getCachableAttributes() {

        return new HashSet(cachableAttributes);
    }
}
