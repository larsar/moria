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
 */

package no.feide.moria.directory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import no.feide.moria.directory.backend.AuthenticationFailedException;
import no.feide.moria.directory.backend.BackendException;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the DirectoryManager class using a dummy backend. Note that
 * the configuration file used must set up a proper dummy backend; browse the
 * code in advance.
 * @author Cato Olsen
 */
public class DummyDirectoryManagerTest
extends TestCase {

    /** Working credentials. */
    private static final Credentials goodCredentials = new Credentials("user@some.realm", "password");

    /** Non-working credentials. */
    private static final Credentials[] badCredentials = {new Credentials("user@another.realm", "password"), new Credentials("test@feide.no", "Test"), null};

    /** The Directory Manager instance. */
    private DirectoryManager dm;

    /** The dummy configuration file. */
    private static final String goodConfiguration = "src/test/conf/DummyConfiguration.xml";


    /**
     * Returns the full test suite.
     * @return The test suite.
     */
    public static Test suite() {

        return new TestSuite(DummyDirectoryManagerTest.class);
    }


    /**
     * Prepare.
     */
    public void setUp() {

        dm = new DirectoryManager();

    }


    /**
     * Clean up.
     */
    public void tearDown() {

        dm = null;

    }


    /**
     * Successful authentication without attribute request.
     */
    public void testGoodAuthenticationWithoutAttributes() {

        // Set configuration properties.
        Properties config = new Properties();
        config.setProperty(DirectoryManagerConfiguration.CONFIGURATION_PROPERTY, goodConfiguration);

        try {

            // Test successful authentication with empty array request.
            dm.setConfig(config);
            HashMap attributes = dm.authenticate(null, goodCredentials, new String[] {});

            // Verify attributes.
            Assert.assertEquals("Attributes were returned", attributes.size(), 0);

            // Test successful authentication with null request.
            attributes = dm.authenticate(null, goodCredentials, null);

            // Verify attributes.
            Assert.assertEquals("Attributes were returned", attributes.size(), 0);

        } catch (AuthenticationFailedException e) {
            e.printStackTrace();
            Assert.fail("Unexpected AuthenticationFailedException");
        } catch (BackendException e) {
            e.printStackTrace();
            Assert.fail("Unexpected BackendException");
        }

    }


    /**
     * Successful authentication with attribute request.
     */
    public void testGoodAuthenticationWithAttributes1() {

        // Set configuration properties.
        Properties config = new Properties();
        config.setProperty(DirectoryManagerConfiguration.CONFIGURATION_PROPERTY, goodConfiguration);

        // Requested attributes and expected values. Note that each attribute is
        // assumed to only return one value.
        final String[] requestedAttributes = {"attr1", "attr2", "attr3"};
        final String[] expectedValues = {"value1", "value2", "value3"};

        try {

            // Test successful authentication.
            dm.setConfig(config);
            HashMap attributes = dm.authenticate(null, goodCredentials, requestedAttributes);

            // Verify attributes.
            Assert.assertNotNull("No attributes returned", attributes);
            Assert.assertEquals("Unexpected number of attributes returned after authentication", requestedAttributes.length, attributes.size());
            for (int i = 0; i < requestedAttributes.length; i++) {
                String[] returnedValues = (String[]) attributes.get(requestedAttributes[i]);
                Assert.assertEquals("Unexpected number of attribute values returned after authentication", 1, returnedValues.length);
                Assert.assertEquals("Attribute values doesn't match", returnedValues[0], expectedValues[i]);
            }

        } catch (AuthenticationFailedException e) {
            e.printStackTrace();
            Assert.fail("Unexpected AuthenticationFailedException");
        } catch (BackendException e) {
            e.printStackTrace();
            Assert.fail("Unexpected BackendException");
        }

    }
    
    
    /**
     * Successful authentication with another attribute request.
     */
    public void testGoodAuthenticationWithAttributes2() {

        // Set configuration properties.
        Properties config = new Properties();
        config.setProperty(DirectoryManagerConfiguration.CONFIGURATION_PROPERTY, goodConfiguration);

        // Requested attributes.
        final String[] requestedAttributes = {"eduPersonAffiliation"};
        
        try {

            // Test successful authentication.
            dm.setConfig(config);
            HashMap attributes = dm.authenticate(null, new Credentials("user4@some.realm", "password"), requestedAttributes);

            // Verify attributes.
            Assert.assertNotNull("No attributes returned", attributes);
            Assert.assertEquals("Unexpected number of attributes returned after authentication", requestedAttributes.length, attributes.size());
            String[] returnedValues = (String[]) attributes.get(requestedAttributes[0]);
            ArrayList expectedValues = new ArrayList(3);
            expectedValues.add("employee");
            expectedValues.add("student");
            expectedValues.add("staff");
            Assert.assertEquals("Unexpected number of attribute values returned after authentication", expectedValues.size(), returnedValues.length);
            for (int i=0; i<returnedValues.length; i++)
                Assert.assertTrue("Returned unexpected value: "+returnedValues[i], expectedValues.contains(returnedValues[i]));
            

        } catch (AuthenticationFailedException e) {
            e.printStackTrace();
            Assert.fail("Unexpected AuthenticationFailedException");
        } catch (BackendException e) {
            e.printStackTrace();
            Assert.fail("Unexpected BackendException");
        }

    }    


    /**
     * Failed authentication without any attribute request.
     */
    public void testBadAuthenticationWithoutAttributes() {

        // Set configuration properties.
        Properties config = new Properties();
        config.setProperty(DirectoryManagerConfiguration.CONFIGURATION_PROPERTY, goodConfiguration);

        try {

            // Test unsuccessful authentication.
            dm.setConfig(config);
            for (int i = 0; i < badCredentials.length; i++) {
                HashMap attributes = null;
                attributes = dm.authenticate(null, badCredentials[i], new String[] {});
                Assert.assertNull("Attributes were returned", attributes);
                Assert.fail("Bad authentication succeeded");
            }

        } catch (AuthenticationFailedException e) {
            // Expected.
        } catch (BackendException e) {
            e.printStackTrace();
            Assert.fail("Unexpected BackendException");
        }

    }


    /**
     * Authentication attempt without configuration set.
     */
    public void testNoConfiguration() {

        try {

            // Test authentication without configuration.
            HashMap attributes = null;
            attributes = dm.authenticate(null, goodCredentials, null);
            Assert.assertNull("Attributes were returned", attributes);
            Assert.fail("Authentication without configuration succeeded");

        } catch (IllegalStateException e) {
            // Expected.
        } catch (AuthenticationFailedException e) {
            e.printStackTrace();
            Assert.fail("Unexpected AuthenticationFailedException");
        } catch (BackendException e) {
            e.printStackTrace();
            Assert.fail("Unexpected BackendException");
        }

    }


    /**
     * Test unsuccessful user lookup.
     */
    public void testBadUserExistence() {

        // Set configuration properties.
        Properties config = new Properties();
        config.setProperty(DirectoryManagerConfiguration.CONFIGURATION_PROPERTY, goodConfiguration);

        try {

            dm.setConfig(config);
            for (int i = 0; i < badCredentials.length; i++) {
                if (badCredentials[i] != null) {

                    // Test bad user existence.
                    Assert.assertFalse("User " + badCredentials[i].getUsername() + " should not exist", dm.userExists(null, badCredentials[i].getUsername()));

                }
            }

        } catch (BackendException e) {
            Assert.fail("Unexpected BackendException");
        }

    }


    /**
     * Test successful user lookup.
     */
    public void testGoodUserExistence() {

        // Set configuration properties.
        Properties config = new Properties();
        config.setProperty(DirectoryManagerConfiguration.CONFIGURATION_PROPERTY, goodConfiguration);

        try {

            // Test good user existence.
            dm.setConfig(config);
            Assert.assertTrue("User " + goodCredentials.getUsername() + " should exist", dm.userExists(null, goodCredentials.getUsername()));

        } catch (BackendException e) {
            Assert.fail("Unexpected BackendException");
        }

    }

}