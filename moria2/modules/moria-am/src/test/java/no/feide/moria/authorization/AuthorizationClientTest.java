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

import java.util.HashMap;
import java.util.HashSet;

/**
 * This class tests the AuthorizationClient class.
 *
 * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
 * @version $Revision$
 */
public final class AuthorizationClientTest extends TestCase {

    private HashSet emptySet;
    private HashMap emptyMap;

    /**
     * Initiate all tests.
     *
     * @return Junit test suite.
     */
    public static Test suite() {
        return new TestSuite(AuthorizationClientTest.class);
    }

    /**
     * Create common test data.
     */
    public void setUp() {
        emptySet = new HashSet();
        emptyMap = new HashMap();
    }

    /**
     * Remove common testdata.
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
         * Illegal arguments
         */

        /* Name */
        try {
            new AuthorizationClient(null, "foo", "foo", "foo", "foo", emptySet, emptySet, emptySet, emptySet, emptyMap);
            fail("Should raise IllegalArgumentException (name = null)");
        } catch (IllegalArgumentException success) {
        }

        try {
            new AuthorizationClient("", "foo", "foo", "foo", "foo", emptySet, emptySet, emptySet, emptySet, emptyMap);
            fail("Should raise IllegalArgumentException (name = '')");
        } catch (IllegalArgumentException success) {
        }

        /* Display name */
        try {
            new AuthorizationClient("foo", null, "foo", "foo", "foo", emptySet, emptySet, emptySet, emptySet, emptyMap);
            fail("Should raise IllegalArgumentException (displayName = null)");
        } catch (IllegalArgumentException success) {
        }

        try {
            new AuthorizationClient("foo", "", "foo", "foo", "foo", emptySet, emptySet,emptySet, emptySet, emptyMap);
            fail("Should raise IllegalArgumentException (displayName = '')");
        } catch (IllegalArgumentException success) {
        }

        /* URL */
        try {
            new AuthorizationClient("foo", "foo", "", "foo", "foo", emptySet, emptySet, emptySet, emptySet, emptyMap);
            fail("Should raise IllegalArgumentException (url = null)");
        } catch (IllegalArgumentException success) {
        }

        try {
            new AuthorizationClient("foo", "foo", "", "foo", "foo", emptySet, emptySet, emptySet, emptySet, emptyMap);
            fail("Should raise IllegalArgumentException (url = '')");
        } catch (IllegalArgumentException success) {
        }

        /* Language */
        try {
            new AuthorizationClient("foo", "foo", "foo", null, "foo", emptySet, emptySet, emptySet, emptySet, emptyMap);
            fail("Should raise IllegalArgumentException (language = null)");
        } catch (IllegalArgumentException success) {
        }

        try {
            new AuthorizationClient("foo", "foo", "foo", null, "foo", emptySet, emptySet, emptySet, emptySet, emptyMap);
            fail("Should raise IllegalArgumentException (language = '')");
        } catch (IllegalArgumentException success) {
        }

        /* Home */
        try {
            new AuthorizationClient("foo", "foo", "foo", "foo", null, emptySet, emptySet, emptySet, emptySet, emptyMap);
            fail("Should raise IllegalArgumentException (home = null)");
        } catch (IllegalArgumentException success) {
        }

        try {
            new AuthorizationClient("foo", "foo", "foo", "foo", "", emptySet, emptySet, emptySet, emptySet, emptyMap);
            fail("Should raise IllegalArgumentException (home = '')");
        } catch (IllegalArgumentException success) {
        }

        /* Affiliation */
        try {
            new AuthorizationClient("foo", "foo", "foo", "foo", "foo", null, emptySet, emptySet, emptySet, emptyMap);
            fail("Should raise IllegalArgumentException (affiliation = null)");
        } catch (IllegalArgumentException success) {
        }
        
        /* Organizations */
        try {
            new AuthorizationClient("foo", "foo", "foo", "foo", "foo", emptySet, null, emptySet, emptySet, emptyMap);
            fail("Should raise IllegalArgumentException (orgsAllowed = null)");
        } catch (IllegalArgumentException success) {
        }

        /* Operations */
        try {
            new AuthorizationClient("foo", "foo", "foo", "foo", "foo", emptySet, emptySet, null, emptySet, emptyMap);
            fail("Should raise IllegalArgumentException (operations = null)");
        } catch (IllegalArgumentException success) {
        }

        /* Attributes */
        try {
            new AuthorizationClient("foo", "foo", "foo", "foo", "foo", emptySet, emptySet, emptySet, emptySet, null);
            fail("Should raise IllegalArgumentException (attributes = null)");
        } catch (IllegalArgumentException success) {
        }

        final HashMap attrs = new HashMap();
        attrs.put("attr1", new AuthorizationAttribute("attr1", false, 2));

        final HashSet oper = new HashSet();
        final HashSet affil = new HashSet();
        final HashSet subsys = new HashSet();
        final HashSet orgs = new HashSet();
        oper.add("oper1");
        oper.add("oper2");
        affil.add("org1");
        affil.add("org2");
        orgs.add("allowedOrg1");
        orgs.add("allowedOrg2");
        subsys.add("sub1");
        subsys.add("sub2");


        /* Verify a valid object */
        final AuthorizationClient client = new AuthorizationClient("name", "display", "url", "lang", "home", affil, orgs, oper, subsys, attrs);

        assertEquals("Name differs", "name", client.getName());
        assertEquals("Display name differs", "display", client.getDisplayName());
        assertEquals("URL differs", "url", client.getURL());
        assertEquals("Language differs", "lang", client.getLanguage());
        assertEquals("Home differs", "home", client.getHome());
        assertEquals("Affiliation differs", affil, client.getAffiliation());
        assertEquals("OrgsAllowed differs", orgs, client.getOrgsAllowed());
        assertEquals("Operations differs", oper, client.getOperations());
        assertEquals("Subsystems differs", subsys, client.getSubsystems());
        assertEquals("Attributes differs", attrs, client.getAttributes());

        final HashMap properties = client.getProperties();
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
        AuthorizationClient client = new AuthorizationClient("foo", "foo", "foo", "foo", "foo", emptySet, emptySet, emptySet, emptySet, emptyMap);

        /* Illegal arguments */
        try {
            client.allowSSOForAttributes(null);
            fail("Should raise IllegalArgumentException, null value");
        } catch (IllegalArgumentException success) {
        }

        /* No registered attributes */
        assertFalse("Non existing attributes", client.allowSSOForAttributes(new String[]{"foo", "bar"}));

        final HashMap attributes = new HashMap();
        attributes.put("attr1", new AuthorizationAttribute("attr1", false, 2));
        attributes.put("attr2", new AuthorizationAttribute("attr2", true, 2));
        attributes.put("attr3", new AuthorizationAttribute("attr3", true, 2));

        client = new AuthorizationClient("foo", "foo", "foo", "foo", "foo", emptySet, emptySet, emptySet, emptySet, attributes);

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
        AuthorizationClient client = new AuthorizationClient("foo", "foo", "foo", "foo", "foo", emptySet, emptySet, emptySet, emptySet, emptyMap);

        /* No registered attributes */
        assertFalse("Non existing attributes", client.allowAccessTo(new String[]{"foo", "bar"}));

        final HashMap attributes = new HashMap();
        attributes.put("attr1", new AuthorizationAttribute("attr1", false, 2));
        attributes.put("attr2", new AuthorizationAttribute("attr2", true, 2));

        client = new AuthorizationClient("foo", "foo", "foo", "foo", "foo", emptySet, emptySet, emptySet, emptySet, attributes);

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
    
    /**
     * Test the allowUserorg method
     *
     */
    public void testAllowUserorg() {
        final HashSet organization= new HashSet();
        organization.add("testorg2.no");
        organization.add("testorg1.no");

        final AuthorizationClient client = new AuthorizationClient("foo", "foo", "foo", "foo", "foo", emptySet, organization, emptySet, emptySet, emptyMap);

        /* Illegal arguments */
        try {
            client.allowUserorg(null);
            fail("Should raise IllegalArgumentException, null value");
        } catch (IllegalArgumentException success) {
        }

        /* Legal arguments */
        assertTrue("Should be allowed for ", client.allowUserorg("testorg2.no"));
        assertTrue("Should be allowed for ", client.allowUserorg("testorg1.no"));
        assertFalse("Should not be allowed for ", client.allowUserorg("wrong.no"));
    }

    /**
     * Test the allowOperations method.
     *
     * @see AuthorizationClient#allowOperations(java.lang.String[])
     */
    public void testAllowOperations() {
        final HashSet operations = new HashSet();
        operations.add("localAuth");
        operations.add("directAuth");

        final AuthorizationClient client = new AuthorizationClient("foo", "foo", "foo", "foo", "foo", emptySet, emptySet, operations, emptySet, emptyMap);

        /* Illegal argument */
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

    /**
     * Test the allowSubsystems method.
     *
     * @see AuthorizationClient#allowSubsystems(java.lang.String[])
     */
    public void testAllowSubsystems() {
        final HashSet subsystems = new HashSet();
        subsystems.add("sub2");
        subsystems.add("sub1");

        final AuthorizationClient client = new AuthorizationClient("foo", "foo", "foo", "foo", "foo", emptySet, emptySet, emptySet, subsystems, emptyMap);

        /* Illegal arguments */
        try {
            client.allowSubsystems(null);
            fail("Should raise IllegalArgumentException, null value");
        } catch (IllegalArgumentException success) {
        }


        /* Legal arguments */
        assertTrue("Should be allowed", client.allowSubsystems(new String[]{"sub1", "sub2"}));
        assertTrue("Should be allowed", client.allowSubsystems(new String[]{"sub1"}));
        assertFalse("Should not be allowed", client.allowSubsystems(new String[]{"sub1", "illegalSubsystem"}));
    }

    /** Tests the hasAffiliation() method.
     *
     * @see AuthorizationClient#hasAffiliation(java.lang.String)
     */
    public void testHasAffiliation() {
        final HashSet affiliation = new HashSet();
        affiliation.add("testorg2.no");
        affiliation.add("testorg1.no");

        final AuthorizationClient client = new AuthorizationClient("foo", "foo", "foo", "foo", "foo", affiliation, emptySet, emptySet, emptySet, emptyMap);

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
        assertTrue("Should be affiliated with", client.hasAffiliation("testorg2.no"));
        assertFalse("Should not be affiliated with", client.hasAffiliation("wrong.no"));
    }

    /**
     * Test the hashCode method.
     *
     * @see AuthorizationClient#hashCode()
     */
    public void testHashCode() {
        final HashMap attrs = new HashMap();
        attrs.put("attr1", new AuthorizationAttribute("attr1", false, 2));

        final HashSet oper = new HashSet();
        final HashSet affil = new HashSet();
        final HashSet subsys = new HashSet();
        final HashSet orgs = new HashSet();
        oper.add("oper1");
        oper.add("oper2");
        affil.add("org1");
        affil.add("org2");
        orgs.add("allowedOrg1");
        orgs.add("allowedOrg2");
        subsys.add("sub1");
        subsys.add("sub2");

        final AuthorizationClient master = new AuthorizationClient("foo", "foo", "foo", "foo", "foo", affil, orgs,  oper, subsys, attrs);

        /* Identical */
        assertEquals("Should be identical", master.hashCode(),
                new AuthorizationClient("foo", "foo", "foo", "foo", "foo", affil, orgs, oper, subsys, attrs).hashCode());

        /* Name */
        assertFalse("Should not be identical",
                master.hashCode() == new AuthorizationClient("bar", "foo", "foo", "foo", "foo", affil, orgs, oper, subsys, attrs).hashCode());

        /* Display name */
        assertFalse("Should not be identical",
                master.hashCode() == new AuthorizationClient("foo", "bar", "foo", "foo", "foo", affil, orgs, oper, subsys, attrs).hashCode());

        /* URL */
        assertFalse("Should not be identical",
                master.hashCode() == new AuthorizationClient("foo", "foo", "bar", "foo", "foo", affil, orgs, oper, subsys, attrs).hashCode());

        /* Language */
        assertFalse("Should not be identical",
                master.hashCode() == new AuthorizationClient("foo", "foo", "foo", "bar", "foo", affil, orgs, oper, subsys, attrs).hashCode());

        /* Home */
        assertFalse("Should not be identical",
                master.hashCode() == new AuthorizationClient("foo", "foo", "foo", "foo", "bar", affil, orgs, oper, subsys, attrs).hashCode());

        /* Affiliation */
        assertFalse("Should not be identical",
                master.hashCode() == new AuthorizationClient("foo", "foo", "foo", "foo", "foo", emptySet, orgs, oper, subsys, attrs).hashCode());

        /* Allowed Organizations */
        assertFalse("Should not be identical",
                master.hashCode() == new AuthorizationClient("foo", "foo", "foo", "foo", "foo", affil, emptySet, oper, subsys, attrs).hashCode());

        /* Operations */
        assertFalse("Should not be identical",
                master.hashCode() == new AuthorizationClient("foo", "foo", "foo", "foo", "foo", affil, orgs, emptySet, subsys, attrs).hashCode());

        /* Subsystems */
        assertFalse("Should not be identical",
                master.hashCode() == new AuthorizationClient("foo", "foo", "foo", "foo", "foo", affil, orgs, oper, emptySet, attrs).hashCode());

        /* Attributes */
        assertFalse("Should not be identical",
                master.hashCode() == new AuthorizationClient("foo", "foo", "foo", "foo", "foo", affil, orgs, oper, subsys, emptyMap).hashCode());
    }

    /**
     * Test the equals() method.
     *
     * @see AuthorizationClient#equals(java.lang.Object)
     */
    public void testEquals() {
        final HashMap attrs = new HashMap();
        attrs.put("attr1", new AuthorizationAttribute("attr1", false, 2));

        final HashSet oper = new HashSet();
        final HashSet affil = new HashSet();
        final HashSet subsys = new HashSet();
        final HashSet orgs = new HashSet();
        oper.add("oper1");
        oper.add("oper2");
        affil.add("org1");
        affil.add("org2");
        orgs.add("allowedOrg1");
        orgs.add("allowedOrg2");
        subsys.add("sub1");
        subsys.add("sub2");

        final AuthorizationClient master = new AuthorizationClient("foo", "foo", "foo", "foo", "foo", affil, orgs, oper, subsys, attrs);

        /* Identical */
        assertTrue("Should be identical",
                master.equals(new AuthorizationClient("foo", "foo", "foo", "foo", "foo", affil, orgs, oper, subsys, attrs)));

        /* Name */
        assertFalse("Should not be identical",
                master.equals(new AuthorizationClient("bar", "foo", "foo", "foo", "foo", affil, orgs, oper, subsys, attrs)));

        /* Display name */
        assertFalse("Should not be identical",
                master.equals(new AuthorizationClient("foo", "bar", "foo", "foo", "foo", affil, orgs, oper, subsys, attrs)));

        /* URL */
        assertFalse("Should not be identical",
                master.equals(new AuthorizationClient("foo", "foo", "bar", "foo", "foo", affil, orgs, oper, subsys, attrs)));

        /* Language */
        assertFalse("Should not be identical",
                master.equals(new AuthorizationClient("foo", "foo", "foo", "bar", "foo", affil, orgs, oper, subsys, attrs)));

        /* Home */
        assertFalse("Should not be identical",
                master.equals(new AuthorizationClient("foo", "foo", "foo", "foo", "bar", affil, orgs, oper, subsys, attrs)));

        /* Affiliation */
        assertFalse("Should not be identical",
                master.equals(new AuthorizationClient("foo", "foo", "foo", "foo", "foo", emptySet, orgs, oper, subsys, attrs)));

        /* Allowed Organizations */
        assertFalse("Should not be identical",
                master.equals(new AuthorizationClient("foo", "foo", "foo", "foo", "foo", affil, emptySet, oper, subsys, attrs)));

        /* Operations */
        assertFalse("Should not be identical",
                master.equals(new AuthorizationClient("foo", "foo", "foo", "foo", "foo", affil, orgs, emptySet, subsys, attrs)));

        /* Subsystems */
        assertFalse("Should not be identical",
                master.equals(new AuthorizationClient("foo", "foo", "foo", "foo", "foo", affil, orgs, oper, emptySet, attrs)));

        /* Attributes */
        assertFalse("Should not be identical",
                master.equals(new AuthorizationClient("foo", "foo", "foo", "foo", "foo", affil, orgs, oper, subsys, emptyMap)));
    }

    /**
     * Test the getSecLevel method.
     *
     * @see AuthorizationClient#getSecLevel(java.lang.String[])
     * @throws UnknownAttributeException
     */
    public void testGetSecLevell() throws UnknownAttributeException {
        /* Invalid arguments */
        AuthorizationClient client = new AuthorizationClient("name", "display", "url", "lang", "home", emptySet, emptySet, 
                emptySet, emptySet, emptyMap);
        try {
            client.getSecLevel(null);
            fail("IllegalArgumentException should be raised, null as requestedAttributes");
        } catch (IllegalArgumentException success) {
        }

        final HashMap attrs = new HashMap();
        attrs.put("attr0", new AuthorizationAttribute("attr0", false, 0));
        attrs.put("attr1", new AuthorizationAttribute("attr1", true, 1));
        attrs.put("attr2", new AuthorizationAttribute("attr2", false, 2));

        client = new AuthorizationClient("name", "display", "url", "lang", "home", emptySet, emptySet, 
                emptySet, emptySet, attrs);

        /* Empty set of requested attributes */
        assertEquals("No requested attributes, secLevel should be lowest(0)", 0, client.getSecLevel(new String[]{}));

        /* Illegal attributes */
        try {
            client.getSecLevel(new String[]{"doesNotExist"});
            fail("UnknownAttributeException should be raised, non-existing attributes");
        } catch (UnknownAttributeException success) {
        }

        /* Normal use */
        assertEquals("secLevel differs", 0, client.getSecLevel(new String[]{"attr0"}));
        assertEquals("secLevel differs", 1, client.getSecLevel(new String[]{"attr1"}));
        assertEquals("secLevel differs", 1, client.getSecLevel(new String[]{"attr1", "attr0"}));
        assertEquals("secLevel differs", 2, client.getSecLevel(new String[]{"attr2"}));
        assertEquals("secLevel differs", 2, client.getSecLevel(new String[]{"attr2", "attr1"}));
        assertEquals("secLevel differs", 2, client.getSecLevel(new String[]{"attr1", "attr2"}));
        assertEquals("secLevel differs", 2, client.getSecLevel(new String[]{"attr1", "attr0", "attr2"}));
    }

}
