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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.jdom.Element;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

/**
 * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
 * @version $Revision$
 */
public class AuthorizationManagerTest extends TestCase {

    private HashMap authzClients;

    /**
     * Run all tests.
     *
     * @return The test suite to run.
     */
    public static Test suite() {
        return new TestSuite(AuthorizationManagerTest.class);
    }

    /**
     * Build test data on startup.
     */
    public void setUp() {
        HashMap attributes = new HashMap();
        attributes.put("attr1", new AuthorizationAttribute("attr1", true, 1));
        attributes.put("attr2", new AuthorizationAttribute("attr2", false, 2));
        attributes.put("attr3", new AuthorizationAttribute("attr3", true, 3));

        HashSet operations = new HashSet();
        operations.add("localAuth");
        operations.add("directAuth");

        HashSet subsystems = new HashSet();
        subsystems.add("sub1");
        subsystems.add("sub2");

        HashSet affiliation = new HashSet();
        affiliation.add("uninett.no");
        affiliation.add("feide.no");

        /* Set configuration */
        AuthorizationClient authzClient = new AuthorizationClient("test", "testDisplay",
                "http://moria.sf.net/", "en", "feide.no", affiliation, operations, subsystems, attributes);
        authzClients = new HashMap();
        authzClients.put("test", authzClient);
    }

    /**
     * Remove test data on shut down.
     */
    public void tearDown() {
        authzClients = null;
    }

    /**
     * Creates a client Element with valid children and attributes.
     *
     * @return The new Element
     **/
    private Element createValidClientElem(String name) {
        Element clientElem = new Element("Client");
        Element child;

        /* Create legal element */
        clientElem.setAttribute("name", name);

        child = new Element("DisplayName");
        child.setText("Foobar");
        clientElem.addContent(child);

        child = new Element("URL");
        child.setText("http://www.feide.no/");
        clientElem.addContent(child);

        child = new Element("Language");
        child.setText("no");
        clientElem.addContent(child);

        child = new Element("Home");
        child.setText("uio.no");
        clientElem.addContent(child);

        /* Operations */
        Element operationsElem = new Element("Operations");
        operationsElem.addContent(createChildElem("Operation", "localAuth"));
        operationsElem.addContent(createChildElem("Operation", "directAuth"));
        clientElem.addContent(operationsElem);

        /* Subsystems */
        Element subsystemElem = new Element("Subsystems");
        subsystemElem.addContent(createChildElem("Subsystem", "sub1"));
        subsystemElem.addContent(createChildElem("Subsystem", "sub2"));
        clientElem.addContent(subsystemElem);

        /* Affiliation */
        Element affiliationElem = new Element("Affiliation");
        affiliationElem.addContent(createChildElem("Organization", "uio.no"));
        affiliationElem.addContent(createChildElem("Organization", "uninett.no"));
        clientElem.addContent(affiliationElem);

        /* Attributes */
        Element attributesElem = new Element("Attributes");
        attributesElem.addContent(createAttrElem("attr1", "true", "1"));
        attributesElem.addContent(createAttrElem("attr2", "true", "0"));
        attributesElem.addContent(createAttrElem("attr3", "false", "2"));

        clientElem.addContent(attributesElem);

        return clientElem;
    }

    /**
     * Creates an Element object (Attribute) with the supplied attributs.
     *
     * @param name
     * @param sso
     * @param secLevel
     * @return Element object with attributes according to the paramteres.
     **/
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
    private Element createChildElem(String type, String name) {
        Element element = new Element(type);
        if (name != null)
            element.setAttribute("name", name);
        return element;
    }

    /**
     * Test parseAttribute method
     *
     * @throws IllegalConfigException
     * @see AuthorizationManager#parseAttributeElem(org.jdom.Element)
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
            fail("Invalid secLevel, should raise IllegalConfigException");
        } catch (IllegalConfigException success) {
        }

        try {
            /* Wrong element type */
            authMan.parseAttributeElem(new Element("WrongType"));
            fail("IllegalConfigException should be raised, wrong element type");
        } catch (IllegalConfigException success) {
        }

        /* Test equality of generated object */
        assertTrue("Expects an equal AuthenticationAttribute object", new AuthorizationAttribute("foo", false, 2).equals(authMan.parseAttributeElem(createAttrElem("foo", "false", "2"))));
    }

    /**
     * Test parsing of attributes element that contains attribute child
     * elements. (testParseAttributes method)
     *
     * @throws IllegalArgumentException
     * @throws IllegalConfigException
     * @see AuthorizationManager#parseAttributesElem(org.jdom.Element)
     */
    public void testParseAttributes() throws IllegalArgumentException, IllegalConfigException {
        AuthorizationManager authMan = new AuthorizationManager();

        Element attributesElem = new Element("Attributes");
        attributesElem.addContent(createAttrElem("foo", "false", "1"));
        attributesElem.addContent(createAttrElem("bar", "true", "0"));
        attributesElem.addContent(createAttrElem("foobar", "false", "2"));

        HashMap attributes = new HashMap();
        attributes.put("foo", new AuthorizationAttribute("foo", false, 1));
        attributes.put("bar", new AuthorizationAttribute("bar", true, 0));
        attributes.put("foobar", new AuthorizationAttribute("foobar", false, 2));

        /* Normal use */
        HashMap parsedAttributes = authMan.parseAttributesElem(attributesElem);
        assertEquals("Output and input should be of equal size", attributes.size(), parsedAttributes.size());

        Iterator it = attributes.keySet().iterator();
        while (it.hasNext()) {
            String attrName = (String) it.next();
            assertTrue("Generated attribute should be eqal to master", attributes.get(attrName).equals(parsedAttributes.get(attrName)));
        }

        /* Attributes element without children */
        attributesElem = new Element("Attributes");
        assertTrue("No attribute elements should result in empty map", authMan.parseAttributesElem(attributesElem).size() == 0);

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
     * Test parseChildElem method. The child element is either an
     * 'Operation' or 'Organization', which are treated the same way.
     *
     * @throws IllegalConfigException
     * @see AuthorizationManager#parseChildElem(org.jdom.Element)
     */
    public void testParseChildElem() throws IllegalConfigException {
        AuthorizationManager authMan = new AuthorizationManager();
        String childType[] = new String[]{"Organization", "Operation"};

        for (int i = 0; i < childType.length; i++) {

            /* Null element */
            try {
                authMan.parseChildElem(null);
                fail("IllegalConfigException should be raised, null element");
            } catch (IllegalArgumentException success) {
            }

            /* Wrong type of element */
            try {
                authMan.parseChildElem(new Element("WrongType"));
                fail("IllegalConfigException should be raised, wrong type of element");
            } catch (IllegalConfigException success) {
            }

            /* No name attribute */
            try {
                authMan.parseChildElem(new Element(childType[i]));
            } catch (IllegalConfigException success) {
            }

            Element operationElem = new Element(childType[i]);

            /* Name attribute is an empty string */
            try {
                operationElem.setAttribute("name", "");
                authMan.parseChildElem(new Element(childType[i]));
            } catch (IllegalConfigException success) {
            }

            /* Proper use */
            operationElem.setAttribute("name", "foobar");
            assertEquals("Input name attribute should be equal to returned value", "foobar", authMan.parseChildElem(operationElem));
        }
    }

    /**
     * Test parsing of operations element.
     *
     * @throws IllegalArgumentException
     * @throws IllegalConfigException
     * @see AuthorizationManager#parseListElem(org.jdom.Element)
     */
    public void testParseListElem() throws IllegalArgumentException, IllegalConfigException {
        AuthorizationManager authMan = new AuthorizationManager();
        String elementType[] = new String[]{"Operations", "Affiliation", "Subsystems"};
        String childType[] = new String[]{"Operation", "Organization", "Subsystem"};

        for (int i = 0; i < elementType.length; i++) {
            Element operationsElem = new Element(elementType[i]);
            operationsElem.addContent(createChildElem(childType[i], "foo"));
            operationsElem.addContent(createChildElem(childType[i], "bar"));

            HashSet operations = new HashSet();
            operations.add("foo");
            operations.add("bar");

            /* Normal use */
            HashSet parsedOperations = authMan.parseListElem(operationsElem);
            assertEquals("Output and input should be of equal size", operations.size(), parsedOperations.size());

            Iterator it = operations.iterator();
            while (it.hasNext()) {
                assertTrue("Content of output is not equal to master", parsedOperations.contains((String) it.next()));
            }

            /* Attributes element without children */
            operationsElem = new Element(elementType[i]);
            assertTrue("No operation elements should result in empty map", authMan.parseListElem(operationsElem).size() == 0);

            /* Null as parameter */
            try {
                authMan.parseListElem(null);
                fail("IllegalArgumentException should be raised, null parameter");
            } catch (IllegalArgumentException success) {
            }

            /* Wrong type of element */
            try {
                authMan.parseListElem(new Element("WrongType"));
                fail("IllegalConfigException should be raised, wrong element type");
            } catch (IllegalConfigException success) {
            }

            operationsElem = new Element(elementType[i]);
            operationsElem.addContent(createChildElem(childType[i], "foobar"));
            operationsElem.addContent(new Element("WrongChildType"));
            try {
                authMan.parseListElem(operationsElem);
                fail("IllegalConfigException should be raised, wrong child element type");
            } catch (IllegalConfigException success) {
            }
        }
    }

    /**
     * Test parseClientElem method
     *
     * @throws IllegalConfigException
     * @see AuthorizationManager#parseClientElem(org.jdom.Element)
     */
    public void testParseClientElem() throws IllegalConfigException {
        AuthorizationManager authMan = new AuthorizationManager();
        Element clientElem = createValidClientElem("client1");

        /* Null parameter */
        try {
            authMan.parseClientElem(null);
            fail("IllegalArgumentException should be raised, null as parameter");
        } catch (IllegalArgumentException success) {
        }

        /* Name attribute is required */
        try {
            authMan.parseClientElem(new Element("Client"));
            fail("IllegalConfigException should be raised, name element not set");
        } catch (IllegalConfigException success) {
        }

        /* Parse valid object */
        AuthorizationClient client = authMan.parseClientElem(clientElem);

        /* Check operations */
        assertTrue("Operation should  be allowed", client.allowOperations(new String[]{"localAuth", "directAuth"}));
        assertFalse("Operation should  not be allowed", client.allowOperations(new String[]{"localAuth", "wrongOperation"}));

        /* Check subsystems */
        assertTrue("Subsystem should  be allowed", client.allowSubsystems(new String[]{"sub1", "sub2"}));
        assertFalse("Subsystem should  not be allowed", client.allowSubsystems(new String[]{"sub1", "wrongSubsystem"}));

        /* Check affiliation */
        assertTrue("Should have affiliation", client.hasAffiliation("uio.no"));
        assertTrue("Should have affiliation", client.hasAffiliation("uninett.no"));
        assertFalse("Should not have affiliation", client.hasAffiliation("wrong.no"));

        /* Check attributes */
        assertTrue("Should have access to", client.allowAccessTo(new String[]{"attr1", "attr2", "attr3"}));
        assertFalse("Should not have access to", client.allowAccessTo(new String[]{"attr3", "attr4"}));
        assertTrue("Should be allowed with SSO", client.allowSSOForAttributes(new String[]{"attr1", "attr2"}));
        assertFalse("Should not be allowed with SSO", client.allowSSOForAttributes(new String[]{"attr1", "attr2", "attr3"}));

        /* Check fields of generated object */
        assertEquals("Name differs", "client1", client.getName());
        assertEquals("DisplayName differs", "Foobar", client.getDisplayName());
        assertEquals("URL differs", "http://www.feide.no/", client.getURL());
        assertEquals("Language differs", "no", client.getLanguage());
        assertEquals("Home differs", "uio.no", client.getHome());

        /* Display name is required */
        clientElem.removeChild("URL");
        try {
            authMan.parseClientElem(clientElem);
            fail("IllegalConfigException should be raised, name element not set");
        } catch (IllegalConfigException success) {
        }

        /* URL is required */
        clientElem.removeChild("URL");
        try {
            authMan.parseClientElem(clientElem);
            fail("IllegalConfigException should be raised, URL element not set");
        } catch (IllegalConfigException success) {
        }

        /* Language is required */
        clientElem.removeChild("URL");
        try {
            authMan.parseClientElem(clientElem);
            fail("IllegalConfigException should be raised, language element not set");
        } catch (IllegalConfigException success) {
        }

        /* HomeOrganization is required */
        clientElem.removeChild("URL");
        try {
            authMan.parseClientElem(clientElem);
            fail("IllegalConfigException should be raised, home organization element not set");
        } catch (IllegalConfigException success) {
        }

        Element child = new Element("HomeOrganization");
        child.setText("uio.no");
        clientElem.addContent(child);
    }

    /**
     * Test parseRootElem method
     *
     * @throws IllegalConfigException
     * @see AuthorizationManager#parseRootElem(org.jdom.Element)
     */
    public void testParseRootElem() throws IllegalConfigException {
        AuthorizationManager authMan = new AuthorizationManager();
        Element config = new Element("ClientAuthorizationConfig");

        /* Null value */
        try {
            authMan.parseRootElem(null);
            fail("IllegalArgumentException should be raised, null value");
        } catch (IllegalArgumentException success) {
        }

        /* Wrong root */
        try {
            authMan.parseRootElem(new Element("WrongType"));
        } catch (IllegalConfigException success) {
        }

        Element client1, client2;
        client1 = createValidClientElem("client1");
        client2 = createValidClientElem("client2");

        HashMap master = new HashMap();
        master.put("client1", authMan.parseClientElem(client1));
        master.put("client2", authMan.parseClientElem(client2));

        config.addContent(client1);
        assertFalse("Should fail, lacks one element", master.equals(authMan.parseRootElem(config)));
        config.addContent(client2);

        assertTrue("Should be equal", master.equals(authMan.parseRootElem(config)));

    }

    /**
     * Test setConfig method. This requires that the test configuration file "am-data.xml" is
     * present in the classpath.
     *
     * @throws IllegalConfigException
     * @see AuthorizationManager#setConfig(java.util.Properties)
     */
    public void testSetConfig() throws IllegalConfigException {
        AuthorizationManager authMan = new AuthorizationManager();

        /* Illegal arguments */
        try {
            authMan.setConfig(null);
            fail("IllegalArgumentException should be raised, null value");
        } catch (IllegalArgumentException success) {
        }

        Properties props = new Properties();
        props.put("authorizationDatabase", this.getClass().getResource("/am-data.xml").getPath());
        authMan.setConfig(props);
    }

    /**
     * Test setAuthzClients method
     *
     * @see AuthorizationManager#setAuthzClients(java.util.HashMap)
     */
    public void testSetAuthzClients() {
        AuthorizationManager authMan = new AuthorizationManager();

         /* Illegal */
         try {
            authMan.setAuthzClients(null);
            fail("IllegalArgumentException should be raised, null value");
         } catch (IllegalArgumentException success) {
         }

         /* Legal */
        authMan.setAuthzClients(new HashMap());
        authMan.setAuthzClients(authzClients);
    }

    /**
     * Test allowSSOForAttributes method
     *
     * @see AuthorizationManager#allowAccessTo(java.lang.String, java.lang.String[])
     */
    public void testAllowAccessTo() throws UnknownServicePrincipalException {
        AuthorizationManager authMan = new AuthorizationManager();

        /* No configuration set */
        try {
            authMan.allowAccessTo("test", new String[]{});
            fail("NoConfigException should be raised");
        } catch (NoConfigException success) {
        }

        authMan.setAuthzClients(authzClients);

        /* Null as service identifier */
        try {
            authMan.allowAccessTo(null, new String[]{});
            fail("IllegalArgumentException should be raised, null as service identifier");
        } catch (IllegalArgumentException success) {
        }

        /* Empty string as identitifier */
        try {
            authMan.allowAccessTo("", new String[]{});
            fail("IllegalArgumentException should be raised, empty string as service identifier");
        } catch (IllegalArgumentException success) {
        }

        /* Null as requested attributes */
        try {
            authMan.allowAccessTo("test", null);
            fail("IllegalArgumentException should be raised, null as requested attributes");
        } catch (IllegalArgumentException success) {
        }


        /* Nonexisting client */
        try {
            authMan.allowAccessTo("doesNotExist", new String[]{});
            fail("UnknownServicePrincipalException should be raised, non-existing principal");
        } catch (UnknownServicePrincipalException success) {
        }
        //assertFalse("Should not be allowed to access", authMan.allowAccessTo("doesNotExist", new String[]{}));

        /* No attributes requested */
        assertTrue("Should be allowed to get access", authMan.allowAccessTo("test", new String[]{}));

        /* Allowed attributes */
        assertTrue("Should be allowed to get access", authMan.allowAccessTo("test", new String[]{"attr1", "attr2"}));
        assertTrue("Should be allowed to get access", authMan.allowAccessTo("test", new String[]{"attr1", "attr2", "attr3"}));

        /* Illegal attributes */
        assertFalse("Should not be allowed to get access", authMan.allowAccessTo("test", new String[]{"attr1", "illegal"}));


    }

    /**
     * Test allowSSOForAttributes method
     *
     * @see AuthorizationManager#allowSSOForAttributes(java.lang.String, java.lang.String[])
     */
    public void testAllowSSOForAttributes() throws UnknownServicePrincipalException {
        AuthorizationManager authMan = new AuthorizationManager();

        /* No configuration set */
        try {
            authMan.allowSSOForAttributes("test", new String[]{});
            fail("NoConfigException should be raised");
        } catch (NoConfigException success) {
        }

        authMan.setAuthzClients(authzClients);

        /* Null as service identifier */
        try {
            authMan.allowSSOForAttributes(null, new String[]{});
            fail("IllegalArgumentException should be raised, null as service identifier");
        } catch (IllegalArgumentException success) {
        }

        /* Empty string as identitifier */
        try {
            authMan.allowSSOForAttributes("", new String[]{});
            fail("IllegalArgumentException should be raised, empty string as service identifier");
        } catch (IllegalArgumentException success) {
        }

        /* Null as requested attributes */
        try {
            authMan.allowSSOForAttributes("test", null);
            fail("IllegalArgumentException should be raised, null as requested attributes");
        } catch (IllegalArgumentException success) {
        }


        /* Nonexisting client */
        try {
            authMan.allowSSOForAttributes("doesNotExist", new String[]{});
            fail("UnknownServicePrincipalException should be raised, non-existing principal");
        } catch (UnknownServicePrincipalException success) {
        }
        //assertFalse("SSO should not be allowed", authMan.allowSSOForAttributes("doesNotExist", new String[]{}));

        /* No attributes requested */
        assertTrue("SSO should be allowed", authMan.allowSSOForAttributes("test", new String[]{}));

        /* Allowed attributes */
        assertTrue("SSO should be allowed", authMan.allowSSOForAttributes("test", new String[]{"attr1"}));
        assertTrue("SSO should be allowed", authMan.allowSSOForAttributes("test", new String[]{"attr1", "attr3"}));

        /* Illegal attributes */
        assertFalse("SSO should not be allowed", authMan.allowSSOForAttributes("test", new String[]{"attr1", "illegal"}));
        assertFalse("SSO should not be allowed", authMan.allowSSOForAttributes("test", new String[]{"attr1", "attr2"}));
        assertFalse("SSO should not be allowed", authMan.allowSSOForAttributes("test", new String[]{"attr2"}));
    }

    /**
     * Test allowSSOForAttributes method
     *
     * @see AuthorizationManager#allowSSOForAttributes(java.lang.String, java.lang.String[])
     */
    public void testAllowOperations() throws UnknownServicePrincipalException {
        AuthorizationManager authMan = new AuthorizationManager();

        /* No configuration set */
        try {
            authMan.allowOperations("test", new String[]{});
            fail("NoConfigException should be raised");
        } catch (NoConfigException success) {
        }

        authMan.setAuthzClients(authzClients);

        /* Null as service identifier */
        try {
            authMan.allowOperations(null, new String[]{});
            fail("IllegalArgumentException should be raised, null as service identifier");
        } catch (IllegalArgumentException success) {
        }

        /* Empty string as identitifier */
        try {
            authMan.allowOperations("", new String[]{});
            fail("IllegalArgumentException should be raised, empty string as service identifier");
        } catch (IllegalArgumentException success) {
        }

        /* Null as requested operations */
        try {
            authMan.allowOperations("test", null);
            fail("IllegalArgumentException should be raised, null as requested operations");
        } catch (IllegalArgumentException success) {
        }


        /* Nonexisting client */
        try {
            authMan.allowOperations("doesNotExist", new String[]{});
            fail("UnknownServicePrincipalException should be raised, non-existing principal");
        } catch (UnknownServicePrincipalException success) {
        }
        //assertFalse("Should not be allowed access to operations", authMan.allowOperations("doesNotExist", new String[]{}));

        /* No operations requested */
        assertTrue("Should be allowed access to operations", authMan.allowOperations("test", new String[]{}));

        /* Allowed operations */
        assertTrue("Should be allowed access to operations", authMan.allowOperations("test", new String[]{"localAuth"}));
        assertTrue("Should be allowed access to operations", authMan.allowOperations("test", new String[]{"localAuth", "directAuth"}));

        /* Illegal attributes */
        assertFalse("Should not be allowed access to operations", authMan.allowOperations("test", new String[]{"localAuth", "illegal"}));
        assertFalse("Should not be allowed access to operations", authMan.allowOperations("test", new String[]{"illegal"}));
    }

    /**
     * Test the getServiceProperties method.
     *
     * @see AuthorizationManager#getServiceProperties(java.lang.String)
     */
    public void testGetServiceProperties() throws UnknownServicePrincipalException {
        AuthorizationManager authMan = new AuthorizationManager();

        Properties props = new Properties();
        props.put("authorizationDatabase", this.getClass().getResource("/am-data.xml").getPath());
        authMan.setConfig(props);

        /* Invalid arguments */
        try {
            authMan.getServiceProperties(null);
            fail("IllegalArgumentException should be raised, null value");
        } catch (IllegalArgumentException success) {
        }
        try {
            authMan.getServiceProperties("");
            fail("IllegalArgumentException should be raised, empty string");
        } catch (IllegalArgumentException success) {
        }

        /* Illegal principal */
        try {
            authMan.getServiceProperties("doesNotExist");
            fail("UnknownServicePrincipalException should be raised, non-existing principal");
        } catch (UnknownServicePrincipalException success) {
        }

        assertNotNull("Properties should not be null", authMan.getServiceProperties("test"));
    }

    /**
     * Test the getSecLevel method.
     *
     * @see AuthorizationManager#getSecLevel(java.lang.String, java.lang.String[])
     */
    public void testGetSecLevel() throws UnknownServicePrincipalException, UnknownAttributeException {
        AuthorizationManager authMan = new AuthorizationManager();

        Properties props = new Properties();
        props.put("authorizationDatabase", this.getClass().getResource("/am-data.xml").getPath());
        authMan.setConfig(props);

        /* Invalid arguments */
        try {
            authMan.getSecLevel(null, new String[]{});
            fail("IllegalArgumentException should be raised, servicePrincipal is null");
        } catch (IllegalArgumentException success) {
        }
        try {
            authMan.getSecLevel("", new String[]{});
            fail("IllegalArgumentException should be raised, servicePrincipal is empty string");
        } catch (IllegalArgumentException success) {
        }
        try {
            authMan.getSecLevel("foo", null);
            fail("IllegalArgumentException should be raised, attributes is null");
        } catch (IllegalArgumentException success) {
        }

        /* Illegal principal */
        try {
            authMan.getSecLevel("doesNotExist", new String[]{});
            fail("UnknownServicePrincipalException should be raised, non-existing principal");
        } catch (UnknownServicePrincipalException success) {
        }

        /* Illegal attributes */
        try {
            authMan.getSecLevel("test", new String[]{"doesNotExist"});
        } catch (UnknownAttributeException success) {
        }

        /* SecLevel 0 */
        String[] requestedAttributes = new String[]{"attr1"};
        assertEquals("SecLevel differs", 0, authMan.getSecLevel("test", requestedAttributes));

        /* SecLevel 1 */
        requestedAttributes = new String[]{"attr2"};
        assertEquals("SecLevel differs", 1, authMan.getSecLevel("test", requestedAttributes));
        requestedAttributes = new String[]{"attr2", "attr1"};
        assertEquals("SecLevel differs", 1, authMan.getSecLevel("test", requestedAttributes));

        /* SecLevel 2 */
        requestedAttributes = new String[]{"attr3"};
        assertEquals("SecLevel differs", 2, authMan.getSecLevel("test", requestedAttributes));
        requestedAttributes = new String[]{"attr3", "attr1"};
        assertEquals("SecLevel differs", 2, authMan.getSecLevel("test", requestedAttributes));
        requestedAttributes = new String[]{"attr2", "attr1", "attr3"};
        assertEquals("SecLevel differs", 2, authMan.getSecLevel("test", requestedAttributes));
    }

    /**
     * Test the getAttributes method.
     *
     * @see AuthorizationManager#getAttributes(java.lang.String)
     * @throws UnknownServicePrincipalException
     */
    public void testGetAttributes() throws UnknownServicePrincipalException {
        AuthorizationManager authMan = new AuthorizationManager();

        Properties props = new Properties();
        props.put("authorizationDatabase", this.getClass().getResource("/am-data.xml").getPath());
        authMan.setConfig(props);

        /* Invalid arguments */
        try {
            authMan.getAttributes(null);
            fail("IllegalArgumentException should be raised, servicePrincipal is null");
        } catch (IllegalArgumentException success) {
        }
        try {
            authMan.getAttributes("");
            fail("IllegalArgumentException should be raised, servicePrincipal is empty string");
        } catch (IllegalArgumentException success) {
        }

        /* Non-existing principal */
        try {
            authMan.getAttributes("doesNotExist");
            fail("UnknownServicePrincipalException should be raised, invalid servicePrincipal");
        } catch (UnknownServicePrincipalException e) {
        }

        HashSet expected = new HashSet();
        expected.add("attr1");
        expected.add("attr2");
        expected.add("attr3");

        HashSet actual = authMan.getAttributes("test");
        compareHashSets("Attribute", expected, actual);
    }

    /**
     * Test the getSubsystems method.
     *
     * @see AuthorizationManager#getSubsystems(java.lang.String)
     * @throws UnknownServicePrincipalException
     */
    public void testGetSubsystems() throws UnknownServicePrincipalException {
        AuthorizationManager authMan = new AuthorizationManager();

        Properties props = new Properties();
        props.put("authorizationDatabase", this.getClass().getResource("/am-data.xml").getPath());
        authMan.setConfig(props);

        /* Invalid arguments */
        try {
            authMan.getSubsystems(null);
            fail("IllegalArgumentException should be raised, servicePrincipal is null");
        } catch (IllegalArgumentException success) {
        }
        try {
            authMan.getSubsystems("");
            fail("IllegalArgumentException should be raised, servicePrincipal is empty string");
        } catch (IllegalArgumentException success) {
        }

        /* Non-existing principal */
        try {
            authMan.getSubsystems("doesNotExist");
            fail("UnknownServicePrincipalException should be raised, invalid servicePrincipal");
        } catch (UnknownServicePrincipalException e) {
        }

        HashSet expected = new HashSet();
        expected.add("sub1");
        expected.add("sub2");

        HashSet actual = authMan.getSubsystems("test");
        compareHashSets("Subsystem", expected, actual);
    }

    /**
     * Test the getOperations method.
     *
     * @see AuthorizationManager#getOperations(java.lang.String)
     * @throws UnknownServicePrincipalException
     */
    public void testGetOperations() throws UnknownServicePrincipalException {
        AuthorizationManager authMan = new AuthorizationManager();

        Properties props = new Properties();
        props.put("authorizationDatabase", this.getClass().getResource("/am-data.xml").getPath());
        authMan.setConfig(props);

        /* Invalid arguments */
        try {
            authMan.getOperations(null);
            fail("IllegalArgumentException should be raised, servicePrincipal is null");
        } catch (IllegalArgumentException success) {
        }
        try {
            authMan.getOperations("");
            fail("IllegalArgumentException should be raised, servicePrincipal is empty string");
        } catch (IllegalArgumentException success) {
        }

        /* Non-existing principal */
        try {
            authMan.getOperations("doesNotExist");
            fail("UnknownServicePrincipalException should be raised, invalid servicePrincipal");
        } catch (UnknownServicePrincipalException e) {
        }
        HashSet expected = new HashSet();
        expected.add("LocalAuth");
        expected.add("InteractiveAuth");

        HashSet actual = authMan.getOperations("test");
        compareHashSets("Operation", expected, actual);
    }

    /**
     * Compares two HashSets and expects them to contain equal set of strings.
     *
     * @param type The name of the element in the HashSet.
     * @param expected The HashSet to compare to 'actual'
     * @param actual The HashSet to compare to 'expected'
     */
    private void compareHashSets(String type, HashSet expected, HashSet actual) {
        assertEquals("HashSet size differs", expected.size(), actual.size());
        Iterator it = expected.iterator();
        while (it.hasNext()) {
            String element = (String) it.next();
            assertTrue(type+" differs. '"+element+"' was not found.", actual.contains(element));
        }
    }


}
