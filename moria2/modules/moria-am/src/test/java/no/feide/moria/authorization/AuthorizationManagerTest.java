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

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jdom.Element;

/**
 * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
 * @version $Revision$
 */
public class AuthorizationManagerTest extends TestCase {

    /**
     * Run all tests.
     *
     * @return The test suite to run.
     */
    public static Test suite() {
        return new TestSuite(AuthorizationManagerTest.class);
    }

    /**
     * Creates a client Element with valid children and attributes.
     *
     * @return The new Element
     */
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

        String[] queryList = new String[]{"foo", "bar", "foobar"};
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
     */
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
     * Test parsing of single attribute element.
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
        Assert.assertTrue("Expects an equal AuthenticationAttribute object", new AuthorizationAttribute("foo", false, 2).equals(authMan.parseAttributeElem(createAttrElem("foo", "false", "2"))));
    }

    /**
     * Test parsing of attributes element that contains attribute child
     * elements.
     *
     * @throws IllegalArgumentException
     * @throws IllegalConfigException
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
        Assert.assertEquals("Output and input should be of equal size", attributes.size(), parsedAttributes.size());

        Iterator it = attributes.keySet().iterator();
        while (it.hasNext()) {
            String attrName = (String) it.next();
            Assert.assertTrue("Generated attribute should be eqal to master", attributes.get(attrName).equals(parsedAttributes.get(attrName)));
        }

        /* Attributes element without children */
        attributesElem = new Element("Attributes");
        Assert.assertTrue("No attribute elements should result in empty map", authMan.parseAttributesElem(attributesElem).size() == 0);

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
     * Test parsing of a single child element. The child element is either an
     * 'Operation' or 'Organization', which are treated the same way.
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
            Assert.assertEquals("Input name attribute should be equal to returned value", "foobar", authMan.parseChildElem(operationElem));
        }
    }

    /**
     * Test parsing of operations element.
     */
    public void testParseListElem() throws IllegalArgumentException, IllegalConfigException {
        AuthorizationManager authMan = new AuthorizationManager();
        String elementType[] = new String[]{"Operations", "Affiliation"};
        String childType[] = new String[]{"Operation", "Organization"};

        for (int i = 0; i < elementType.length; i++) {
            Element operationsElem = new Element(elementType[i]);
            operationsElem.addContent(createChildElem(childType[i], "foo"));
            operationsElem.addContent(createChildElem(childType[i], "bar"));

            HashSet operations = new HashSet();
            operations.add("foo");
            operations.add("bar");

            /* Normal use */
            HashSet parsedOperations = authMan.parseListElem(operationsElem);
            Assert.assertEquals("Output and input should be of equal size", operations.size(), parsedOperations.size());

            Iterator it = operations.iterator();
            while (it.hasNext()) {
                Assert.assertTrue("Content of output is not equal to master", parsedOperations.contains((String) it.next()));
            }

            /* Attributes element without children */
            operationsElem = new Element(elementType[i]);
            Assert.assertTrue("No operation elements should result in empty map", authMan.parseListElem(operationsElem).size() == 0);

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
        Assert.assertTrue("Operation should  be allowed", client.allowOperations(new String[]{"localAuth", "directAuth"}));
        Assert.assertFalse("Operation should  not be allowed", client.allowOperations(new String[]{"localAuth", "wrongOperation"}));

        /* Check affiliation */
        Assert.assertTrue("Should have affiliation", client.hasAffiliation("uio.no"));
        Assert.assertTrue("Should have affiliation", client.hasAffiliation("uninett.no"));
        Assert.assertFalse("Should not have affiliation", client.hasAffiliation("wrong.no"));

        /* Check attributes */
        Assert.assertTrue("Should have access to", client.allowAccessTo(new String[]{"attr1", "attr2", "attr3"}));
        Assert.assertFalse("Should not have access to", client.allowAccessTo(new String[]{"attr3", "attr4"}));
        Assert.assertTrue("Should be allowed with SSO", client.allowSSOForAttributes(new String[]{"attr1", "attr2"}));
        Assert.assertFalse("Should not be allowed with SSO", client.allowSSOForAttributes(new String[]{"attr1", "attr2", "attr3"}));

        /* Check fields of generated object */
        Assert.assertEquals("Name differs", "client1", client.getName());
        Assert.assertEquals("DisplayName differs", "Foobar", client.getDisplayName());
        Assert.assertEquals("URL differs", "http://www.feide.no/", client.getURL());
        Assert.assertEquals("Language differs", "no", client.getLanguage());
        Assert.assertEquals("Home differs", "uio.no", client.getHome());

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
        Assert.assertFalse("Should fail, lacks one element", master.equals(authMan.parseRootElem(config)));
        config.addContent(client2);

        Assert.assertTrue("Should be equal", master.equals(authMan.parseRootElem(config)));

    }
}
