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

import java.util.*;
import java.io.File;
import java.io.IOException;

import org.jdom.Element;
import org.jdom.Document;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;


/**
 * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
 * @version $Revision$
 */
public class AuthorizationManager {

    /**
     * List of client authorization objects. Must be synchronized.
     */
    private Map authzClients = Collections.synchronizedMap(new HashMap());

    /**
     * True if the authorization manager is ready to be used
     */
    private boolean activated = false;

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
            return new AuthorizationAttribute(name, new Boolean(allowSSOStr).booleanValue(), new Integer(secLevel).intValue());
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
     * Parses 'operation' and 'organization' elements and returns the name
     * attribute.
     *
     * @param element The operation element
     * @return String containing the name attribute of the element.
     * @throws IllegalArgumentException @trhows IllegalConfigException
     */
    String parseChildElem(Element element) throws IllegalArgumentException, IllegalConfigException {

        if (element == null)
            throw new IllegalArgumentException("Element cannot be null");

        if (!element.getName().equalsIgnoreCase("Operation") && !element.getName().equalsIgnoreCase("Subsystem") &&
                !element.getName().equalsIgnoreCase("Organization"))
            throw new IllegalConfigException("Element must be of type 'Operation', 'Subsystem' or 'Organization'");

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
    HashSet parseListElem(Element element) throws IllegalConfigException {
        HashSet operations = new HashSet();

        /* Validate element */
        if (element == null)
            throw new IllegalArgumentException("Element cannot be null.");

        if (!element.getName().equalsIgnoreCase("Operations") && !element.getName().equalsIgnoreCase("Subsystems") &&
                !element.getName().equalsIgnoreCase("Affiliation"))
            throw new IllegalConfigException("Element isn't of type 'Operations', 'Subsystems' or 'Affiliation'");

        /* Create AuthorizationAttribute of all child elements */
        Iterator it = (element.getChildren()).iterator();
        while (it.hasNext()) {
            String operation = parseChildElem((Element) it.next());
            operations.add(operation);
        }

        return operations;
    }

    /**
     * Creates a AuthorizationClient object based on the supplied XML element.
     *
     * @param element The XML element representing the client service
     * @return The object representing the client service
     * @throws IllegalConfigException
     */
    AuthorizationClient parseClientElem(Element element) throws IllegalConfigException {
        String name, displayName, url, language, home;
        HashSet oper, affil, subsys;
        HashMap attrs;

        if (element == null)
            throw new IllegalArgumentException("Element cannot be null");

        name = element.getAttributeValue("name");
        if (name == null || name.equals(""))
            throw new IllegalConfigException("Name attribute must be a non empty string.");

        displayName = getChildContent(element, "DisplayName");
        url = getChildContent(element, "URL");
        language = getChildContent(element, "Language");
        home = getChildContent(element, "Home");

        attrs = parseAttributesElem(element.getChild("Attributes"));
        oper = parseListElem(element.getChild("Operations"));
        subsys = parseListElem(element.getChild("Subsystems"));
        affil = parseListElem(element.getChild("Affiliation"));

        return new AuthorizationClient(name, displayName, url, language, home, affil, oper, subsys, attrs);
    }

    /**
     * Parses a configuration root element with client elements.
     *
     * @param element the root element
     * @return a HashMap containing AuthorizationClient objects
     * @see AuthorizationClient
     */
    HashMap parseRootElem(Element element) throws IllegalConfigException {
        HashMap clients = new HashMap();

        if (element == null)
            throw new IllegalArgumentException("Element cannot be null");

        if (!element.getName().equalsIgnoreCase("ClientAuthorizationConfig"))
            throw new IllegalConfigException("Wrong type of element: " + element.getName());

        List children = element.getChildren("Client");
        Iterator it = children.iterator();
        while (it.hasNext()) {
            AuthorizationClient client = parseClientElem((Element) it.next());
            clients.put(client.getName(), client);
        }

        return clients;
    }

    /**
     * Retrieves the content of a XML element.
     *
     * @param element   Parent element
     * @param childName Name of the child node
     * @return The content of the child element
     * @throws IllegalConfigException
     */
    private String getChildContent(Element element, String childName) throws IllegalConfigException {
        String value = element.getChildText(childName);
        if (value == null)
            throw new IllegalConfigException(childName + " cannot be null");
        else
            return value;
    }

    /**
     * Return a client object for a given identifier.
     *
     * @param servicePrincipal the client object identifier
     * @return the client object for the identifier
     * @throws IllegalArgumentException if the client identifier is null or ""
     */
    private AuthorizationClient getAuthzClient(String servicePrincipal) {
        /* Is the manager activated? */
        if (activated == false) {
            throw new NoConfigException("Authorization manager is not configured");
        }

        /* Validate input parameters */
        if (servicePrincipal == null || servicePrincipal.equals("")) {
            throw new IllegalArgumentException("servicePrincipal must be a non-empty string.");
        }

        return (AuthorizationClient) authzClients.get(servicePrincipal);
    }

    /**
     * Validates a request for access to attributes for a given client/service.
     *
     * @param servicePrincipal            the indentifier of the client
     * @param requestedAttributes the list of requested attributes
     * @return true if the service is allowed access, false if not or the client does not exist
     */
    public boolean allowAccessTo(String servicePrincipal, String[] requestedAttributes) throws UnknownServicePrincipalException {
        AuthorizationClient authzClient = getAuthzClient(servicePrincipal);

        if (authzClient == null) {
            throw new UnknownServicePrincipalException("Service principal does not exist: '" + servicePrincipal + "'");
        }

        return authzClient.allowAccessTo(requestedAttributes);
    }

    /**
     * Validates a request for access to SSO for a given client/service.
     *
     * @param servicePrincipal    the indentifier of the client
     * @param requestedAttributes the list of requested attributes
     * @return true if the service is allowed access, false if not or the client does not exist
     */
    public boolean allowSSOForAttributes(String servicePrincipal, String[] requestedAttributes) throws UnknownServicePrincipalException {
        AuthorizationClient authzClient = getAuthzClient(servicePrincipal);

        if (authzClient == null) {
            throw new UnknownServicePrincipalException("Service principal does not exist: '" + servicePrincipal + "'");
        }

        return authzClient.allowSSOForAttributes(requestedAttributes);
    }

    /**
     * Validates a request for access to operations for a given client/service.
     *
     * @param servicePrincipal    the indentifier of the client
     * @param requestedOperations the list of requested operations
     * @return true if the service is allowed access, false if not or the client does not exist
     */
    public boolean allowOperations(String servicePrincipal, String[] requestedOperations) throws UnknownServicePrincipalException {
        AuthorizationClient authzClient = getAuthzClient(servicePrincipal);

        if (authzClient == null) {
            throw new UnknownServicePrincipalException("Service principal does not exist: '" + servicePrincipal + "'");
        }

        return authzClient.allowOperations(requestedOperations);
    }

    /**
     * Swap the old client database with the supplied HashMap.
     *
     * @param newClients the new client database
     */
    synchronized void setAuthzClients(HashMap newClients) {
        if (newClients == null) {
            throw new IllegalArgumentException("newClients to be set cannot be null");
        }
        synchronized (authzClients) {
            authzClients = Collections.synchronizedMap(newClients);
            activated = true;
        }
    }

    /**
     * Set the configuration data for this manager.
     *
     * @param properties the properties containing the authorization database
     */
    public void setConfig(Properties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("Properties cannot be null");
        }

        String fileName = (String) properties.get("authorizationDatabase");
        if (fileName == null || fileName.equals("")) {
            // TODO: Log
            // MessageLogger.logWarning("The 'authorizationDatabase' property is not set (setConfig). Authorization database was NOT reloaded.");
            return;
        }

        SAXBuilder builder = new SAXBuilder();
        try {
            Document doc = builder.build(new File(fileName));
            HashMap newClients = parseRootElem(doc.getRootElement());
            setAuthzClients(newClients);
        } catch (JDOMException e) {
            // TODO: Log
            System.out.println("Error during parsing of authorization database file. Still using old database.");
            e.printStackTrace();
            // MessageLogger.logWarning("Error during parsing of authorization database file. Still using old database.", e);
        } catch (IOException e) {
            // TODO: Log
            System.out.println("IOException during parsing of authorization database file. Still using old database.");
            e.printStackTrace();
            // MessageLogger.logWarning("IOException during parsing of authorization database file. Still using old database.", e);
        } catch (IllegalConfigException e) {
            // TODO: Log
            System.out.println("Error during authorization database generation. Still using old database.");
            e.printStackTrace();
            // MessageLogger.logWarning("Error during authorization database generation. Still using old database.", e);
        }
    }

    /**
     * Returns the service properties for a given service.
     *
     * @param servicePrincipal
     * @return a hashmap with properties for a given service
     * @see AuthorizationClient#getProperties()
     */
    public HashMap getServiceProperties(String servicePrincipal) throws UnknownServicePrincipalException {
        /* Validate parameters */
        if (servicePrincipal == null || servicePrincipal.equals("")) {
            throw new IllegalArgumentException("servicePrincipal must be a non-empty string");
        }

        AuthorizationClient authzClient = getAuthzClient(servicePrincipal);
        if (authzClient == null) {
            throw new UnknownServicePrincipalException("Service principal does not exist: '" + servicePrincipal + "'");
        }

        return authzClient.getProperties();
    }

    /**
     * Returns the seclevel for a set of attributes for a given service.
     *
     * @param servicePrincipal
     * @param requestedAttributes
     * @return integer >= 0
     * @see AuthorizationClient#getSecLevel(java.lang.String[])
     */
    public int getSecLevel(String servicePrincipal, String[] requestedAttributes) throws UnknownServicePrincipalException {
        /* Validate arguments */
        if (servicePrincipal == null || servicePrincipal.equals("")) {
            throw new IllegalArgumentException("servicePrincipal must be a non-empty string");
        }
        if (requestedAttributes == null) {
            throw new IllegalArgumentException("requestedAttributes cannot be null");
        }

        AuthorizationClient authzClient = getAuthzClient(servicePrincipal);
        if (authzClient == null) {
            throw new UnknownServicePrincipalException("Service principal does not exist: '" + servicePrincipal + "'");
        }

        /* Return lowest seclevel if no attributes are requested */
        if (requestedAttributes.length == 0) {
            return 0;
        }

        return authzClient.getSecLevel(requestedAttributes);
    }

    /**
     * Returns the configured attributes for a given service.
     *
     * @param servicePrincipal
     * @return A string array with the attribute names that is configured for the service.
     * @throws UnknownServicePrincipalException
     *          if the servicePrincipal does not exist
     * @see AuthorizationClient#getAttributes()
     */
    public String[] getAttributes(String servicePrincipal) throws UnknownServicePrincipalException {
        /* Validate argument */
        if (servicePrincipal == null || servicePrincipal.equals("")) {
            throw new IllegalArgumentException("servicePrincipal must be a non-empty string");
        }

        AuthorizationClient authzClient = getAuthzClient(servicePrincipal);
        if (authzClient == null) {
            throw new UnknownServicePrincipalException("Service principal does not exist: '" + servicePrincipal + "'");
        }

        HashMap attributes = authzClient.getAttributes();
        return (String[]) attributes.keySet().toArray(new String[attributes.size()]);
    }
}
