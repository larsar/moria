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

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Properties;

/**
 * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
 * @version $Revision$
 */
public class AuthorizationClientTest extends TestCase {

    HashSet emptySet;
    HashMap emptyMap;

    /**
     * Initiate all tests.
     *
     * @return Junit test suite.
     */
    public static Test suite() {
        return new TestSuite(AuthorizationClientTest.class);
    }

    /**
     * Create common test data
     */
    public void setUp() {
        emptySet = new HashSet();
        emptyMap = new HashMap();
    }

    /**
     * Remove common testdata
     */
    public void tearDown() {
        emptySet = null;
        emptyMap = null;
    }

    /**
     * Test creation of AuthorizationClient. AuthorizationClient is an
     * immutable object and all attributes must be submitted to the
     * constructor. The constructor does not allow null values. The first part
     * test for null and empty values, the last part test the string values.
     * The HashMap and HashSet are tested in other methods.
     */
    public void testNewAuthorizationClient() {

        /*
         * Illegal values
         */

        /* Name */
        try {
            new AuthorizationClient(null, "foo", "foo", "foo", "foo", emptySet, emptySet, emptySet, emptyMap);
            fail("Should raise IllegalArgumentException (name = null)");
        } catch (IllegalArgumentException success) {
        }

        try {
            new AuthorizationClient("", "foo", "foo", "foo", "foo", emptySet, emptySet, emptySet, emptyMap);
            fail("Should raise IllegalArgumentException (name = '')");
        } catch (IllegalArgumentException success) {
        }

        /* Display name */
        try {
            new AuthorizationClient("foo", null, "foo", "foo", "foo", emptySet, emptySet, emptySet, emptyMap);
            fail("Should raise IllegalArgumentException (displayName = null)");
        } catch (IllegalArgumentException success) {
        }

        try {
            new AuthorizationClient("foo", "", "foo", "foo", "foo", emptySet, emptySet, emptySet, emptyMap);
            fail("Should raise IllegalArgumentException (displayName = '')");
        } catch (IllegalArgumentException success) {
        }

        /* URL */
        // TODO: Test for illegal URL for instance ftp://foobar
        try {
            new AuthorizationClient("foo", "foo", "", "foo", "foo", emptySet, emptySet, emptySet, emptyMap);
            fail("Should raise IllegalArgumentException (url = null)");
        } catch (IllegalArgumentException success) {
        }

        try {
            new AuthorizationClient("foo", "foo", "", "foo", "foo", emptySet, emptySet, emptySet, emptyMap);
            fail("Should raise IllegalArgumentException (url = '')");
        } catch (IllegalArgumentException success) {
        }

        /* Language */
        try {
            new AuthorizationClient("foo", "foo", "foo", null, "foo", emptySet, emptySet, emptySet, emptyMap);
            fail("Should raise IllegalArgumentException (language = null)");
        } catch (IllegalArgumentException success) {
        }

        try {
            new AuthorizationClient("foo", "foo", "foo", null, "foo", emptySet, emptySet, emptySet, emptyMap);
            fail("Should raise IllegalArgumentException (language = '')");
        } catch (IllegalArgumentException success) {
        }

        /* Home */
        try {
            new AuthorizationClient("foo", "foo", "foo", "foo", null, emptySet, emptySet, emptySet, emptyMap);
            fail("Should raise IllegalArgumentException (home = null)");
        } catch (IllegalArgumentException success) {
        }

        try {
            new AuthorizationClient("foo", "foo", "foo", "foo", "", emptySet, emptySet, emptySet, emptyMap);
            fail("Should raise IllegalArgumentException (home = '')");
        } catch (IllegalArgumentException success) {
        }

        /* Affiliation */
        try {
            new AuthorizationClient("foo", "foo", "foo", "foo", "foo", null, emptySet, emptySet, emptyMap);
            fail("Should raise IllegalArgumentException (affiliation = null)");
        } catch (IllegalArgumentException success) {
        }

        /* Operations */
        try {
            new AuthorizationClient("foo", "foo", "foo", "foo", "foo", emptySet, null, emptySet, emptyMap);
            fail("Should raise IllegalArgumentException (operations = null)");
        } catch (IllegalArgumentException success) {
        }

        /* Subsystems */
        try {
            new AuthorizationClient("foo", "foo", "foo", "foo", "foo", emptySet, emptySet, null, emptyMap);
            fail("Should raise IllegalArgumentException (operations = null)");
        } catch (IllegalArgumentException success) {
        }

        /* Attributes */
        try {
            new AuthorizationClient("foo", "foo", "foo", "foo", "foo", emptySet, emptySet, emptySet, null);
            fail("Should raise IllegalArgumentException (attributes = null)");
        } catch (IllegalArgumentException success) {
        }

        HashMap attrs = new HashMap();
        attrs.put("attr1", new AuthorizationAttribute("attr1", false, 2));

        HashSet oper = new HashSet(), affil = new HashSet(), subsys = new HashSet();
        oper.add("oper1");
        oper.add("oper2");
        affil.add("org1");
        affil.add("org2");
        subsys.add("sub1");
        subsys.add("sub2");


        /* Verify a valid object */
        AuthorizationClient client = new AuthorizationClient("name", "display", "url", "lang", "home", affil, oper, subsys, attrs);

        assertEquals("Name differs", "name", client.getName());
        assertEquals("Display name differs", "display", client.getDisplayName());
        assertEquals("URL differs", "url", client.getURL());
        assertEquals("Language differs", "lang", client.getLanguage());
        assertEquals("Home differs", "home", client.getHome());
        assertEquals("Affiliation differs", affil, client.getAffiliation());
        assertEquals("Operations differs", oper, client.getOperations());
        assertEquals("Subsystems differs", subsys, client.getSubsystems());
        assertEquals("Attributes differs", attrs, client.getAttributes());

        Properties properties = client.getProperties();
        assertNotNull("Properties should not be null", properties);
        assertEquals("Number of elements is incorrect", 5, properties.size());
        assertEquals("Display name differs", "display", properties.get("displayName"));
        assertEquals("URL differs", "url", properties.get("url"));
        assertEquals("Language differs", "lang", properties.get("language"));
        assertEquals("Home differs", "home", properties.get("home"));
        assertEquals("Name differs", "name", properties.get("name"));


    }

    /**
     * Test allowSSOForAttributes method.
     */
    public void testAllowSSOForAttributes() {
        AuthorizationClient client = new AuthorizationClient("foo", "foo", "foo", "foo", "foo", emptySet, emptySet, emptySet, emptyMap);

        /* Illegal arguments */
        try {
            client.allowSSOForAttributes(null);
            fail("Should raise IllegalArgumentException, null value");
        } catch (IllegalArgumentException success) {
        }

        /* No registered attributes */
        assertFalse("Non existing attributes", client.allowSSOForAttributes(new String[]{"foo", "bar"}));

        HashMap attributes = new HashMap();
        attributes.put("attr1", new AuthorizationAttribute("attr1", false, 2));
        attributes.put("attr2", new AuthorizationAttribute("attr2", true, 2));
        attributes.put("attr3", new AuthorizationAttribute("attr3", true, 2));

        client = new AuthorizationClient("foo", "foo", "foo", "foo", "foo", emptySet, emptySet, emptySet, attributes);

        /* SSO for attributes */
        assertFalse("SSO should not be allowed", client.allowSSOForAttributes(new String[]{"attr1"}));
        assertFalse("SSO should not be allowed", client.allowSSOForAttributes(new String[]{"attr1", "attr2"}));
        assertFalse("SSO should not be allowed", client.allowSSOForAttributes(new String[]{"doesNotExist", "attr2"}));
        assertTrue("SSO should be allowed", client.allowSSOForAttributes(new String[]{"attr2", "attr3"}));
    }

    /**
     * Test accessTo method.
     */
    public void testAllowAccessTo() {
        AuthorizationClient client = new AuthorizationClient("foo", "foo", "foo", "foo", "foo", emptySet, emptySet, emptySet, emptyMap);

        /* No registered attributes */
        assertFalse("Non existing attributes", client.allowAccessTo(new String[]{"foo", "bar"}));

        HashMap attributes = new HashMap();
        attributes.put("attr1", new AuthorizationAttribute("attr1", false, 2));
        attributes.put("attr2", new AuthorizationAttribute("attr2", true, 2));

        client = new AuthorizationClient("foo", "foo", "foo", "foo", "foo", emptySet, emptySet, emptySet, attributes);

        /* Illegal arguments */
        try {
            client.allowAccessTo(null);
            fail("Should raise IllegalArgumentException, null value");
        } catch (IllegalArgumentException success) {
        }

        /* Access to attributes */
        assertTrue("Should get access (no attributes)", client.allowAccessTo(new String[]{}));
        assertTrue("Should get access", client.allowAccessTo(new String[]{"attr1"}));
        assertTrue("Should get access", client.allowAccessTo(new String[]{"attr1", "attr2"}));
        assertFalse("Should not get access", client.allowAccessTo(new String[]{"attr2", "attr3"}));
    }

    public void testAllowOperations() {
        HashSet operations = new HashSet();
        operations.add("localAuth");
        operations.add("directAuth");

        AuthorizationClient client = new AuthorizationClient("foo", "foo", "foo", "foo", "foo", emptySet, operations, emptySet, emptyMap);

        /* Illegal arguments */
        try {
            client.allowOperations(null);
            fail("Should raise IllegalArgumentException, null value");
        } catch (IllegalArgumentException success) {
        }


        /* Legal arguments */
        assertTrue("Should be allowed", client.allowOperations(new String[]{"localAuth", "directAuth"}));
        assertTrue("Should be allowed", client.allowOperations(new String[]{}));
        assertFalse("Should not be allowed", client.allowOperations(new String[]{"directAuth", "illegalOper"}));
    }

    public void testAllowSubsystems() {
        HashSet subsystems = new HashSet();
        subsystems.add("sub2");
        subsystems.add("sub1");

        AuthorizationClient client = new AuthorizationClient("foo", "foo", "foo", "foo", "foo", emptySet, emptySet, subsystems, emptyMap);

        /* Illegal arguments */
        try {
            client.allowSubsystems(null);
            fail("Should raise IllegalArgumentException, null value");
        } catch (IllegalArgumentException success) {
        }


        /* Legal arguments */
        assertTrue("Should be allowed", client.allowSubsystems(new String[]{"sub1", "sub2"}));
        assertTrue("Should be allowed", client.allowSubsystems(new String[]{"sub1"}));
        assertTrue("Should be allowed", client.allowSubsystems(new String[]{}));
        assertFalse("Should not be allowed", client.allowSubsystems(new String[]{"sub1", "illegalSubsystem"}));
    }

    public void testHasAffiliation() {
        HashSet affiliation = new HashSet();
        affiliation.add("uio.no");
        affiliation.add("uninett.no");

        AuthorizationClient client = new AuthorizationClient("foo", "foo", "foo", "foo", "foo", affiliation, emptySet, emptySet, emptyMap);

        /* Illegal arguments */
        try {
            client.hasAffiliation(null);
            fail("Should raise IllegalArgumentException, null value");
        } catch (IllegalArgumentException success) {
        }

        try {
            client.hasAffiliation("");
            fail("Should raise IllegalArgumentException, empty string");
        } catch (IllegalArgumentException success) {
        }

        /* Legal arguments */
        assertTrue("Should be affiliated with", client.hasAffiliation("uio.no"));
        assertFalse("Should not be affiliated with", client.hasAffiliation("wrong.no"));
    }

    public void testHashCode() {
        HashMap attrs = new HashMap();
        attrs.put("attr1", new AuthorizationAttribute("attr1", false, 2));

        HashSet oper = new HashSet(), affil = new HashSet(), subsys = new HashSet();
        oper.add("oper1");
        oper.add("oper2");
        affil.add("org1");
        affil.add("org2");
        subsys.add("sub1");
        subsys.add("sub2");

        AuthorizationClient master = new AuthorizationClient("foo", "foo", "foo", "foo", "foo", affil, oper, subsys, attrs);

        /* Identical */
        assertEquals("Should be identical", master.hashCode(),
                new AuthorizationClient("foo", "foo", "foo", "foo", "foo", affil, oper, subsys, attrs).hashCode());

        /* Name */
        assertFalse("Should not be identical",
                master.hashCode() == new AuthorizationClient("bar", "foo", "foo", "foo", "foo", affil, oper, subsys, attrs).hashCode());

        /* Display name */
        assertFalse("Should not be identical",
                master.hashCode() == new AuthorizationClient("foo", "bar", "foo", "foo", "foo", affil, oper, subsys, attrs).hashCode());

        /* URL */
        assertFalse("Should not be identical",
                master.hashCode() == new AuthorizationClient("foo", "foo", "bar", "foo", "foo", affil, oper, subsys, attrs).hashCode());

        /* Language */
        assertFalse("Should not be identical",
                master.hashCode() == new AuthorizationClient("foo", "foo", "foo", "bar", "foo", affil, oper, subsys, attrs).hashCode());

        /* Home */
        assertFalse("Should not be identical",
                master.hashCode() == new AuthorizationClient("foo", "foo", "foo", "foo", "bar", affil, oper, subsys, attrs).hashCode());

        /* Affiliation */
        assertFalse("Should not be identical",
                master.hashCode() == new AuthorizationClient("foo", "foo", "foo", "foo", "foo", emptySet, oper, subsys, attrs).hashCode());

        /* Operations */
        assertFalse("Should not be identical",
                master.hashCode() == new AuthorizationClient("foo", "foo", "foo", "foo", "foo", affil, emptySet, subsys, attrs).hashCode());

        /* Subsystems */
        assertFalse("Should not be identical",
                master.hashCode() == new AuthorizationClient("foo", "foo", "foo", "foo", "foo", affil, oper, emptySet, attrs).hashCode());

        /* Attributes */
        assertFalse("Should not be identical",
                master.hashCode() == new AuthorizationClient("foo", "foo", "foo", "foo", "foo", affil, oper, subsys, emptyMap).hashCode());
    }

    public void testEquals() {
        HashMap attrs = new HashMap();
        attrs.put("attr1", new AuthorizationAttribute("attr1", false, 2));

        HashSet oper = new HashSet(), affil = new HashSet(), subsys = new HashSet();
        oper.add("oper1");
        oper.add("oper2");
        affil.add("org1");
        affil.add("org2");
        subsys.add("sub1");
        subsys.add("sub2");

        AuthorizationClient master = new AuthorizationClient("foo", "foo", "foo", "foo", "foo", affil, oper, subsys, attrs);

        /* Identical */
        assertTrue("Should be identical",
                master.equals(new AuthorizationClient("foo", "foo", "foo", "foo", "foo", affil, oper, subsys, attrs)));

        /* Name */
        assertFalse("Should not be identical",
                master.equals(new AuthorizationClient("bar", "foo", "foo", "foo", "foo", affil, oper, subsys, attrs)));

        /* Display name */
        assertFalse("Should not be identical",
                master.equals(new AuthorizationClient("foo", "bar", "foo", "foo", "foo", affil, oper, subsys, attrs)));

        /* URL */
        assertFalse("Should not be identical",
                master.equals(new AuthorizationClient("foo", "foo", "bar", "foo", "foo", affil, oper, subsys, attrs)));

        /* Language */
        assertFalse("Should not be identical",
                master.equals(new AuthorizationClient("foo", "foo", "foo", "bar", "foo", affil, oper, subsys, attrs)));

        /* Home */
        assertFalse("Should not be identical",
                master.equals(new AuthorizationClient("foo", "foo", "foo", "foo", "bar", affil, oper, subsys, attrs)));

        /* Affiliation */
        assertFalse("Should not be identical",
                master.equals(new AuthorizationClient("foo", "foo", "foo", "foo", "foo", emptySet, oper, subsys, attrs)));

        /* Operations */
        assertFalse("Should not be identical",
                master.equals(new AuthorizationClient("foo", "foo", "foo", "foo", "foo", affil, emptySet, subsys, attrs)));

        /* Subsystems */
        assertFalse("Should not be identical",
                master.equals(new AuthorizationClient("foo", "foo", "foo", "foo", "foo", affil, oper, emptySet, attrs)));

        /* Attributes */
        assertFalse("Should not be identical",
                master.equals(new AuthorizationClient("foo", "foo", "foo", "foo", "foo", affil, oper, subsys, emptyMap)));
    }

}
