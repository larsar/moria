/*
 * Copyright (c) 2004 FEIDE This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 */
package no.feide.moria.directory;

import java.util.Properties;

import no.feide.moria.directory.backend.AuthenticationFailedException;
import no.feide.moria.directory.backend.BackendException;

import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the DirectoryManager class.
 * @author Cato Olsen
 */
public class DummyDirectoryManagerTest
extends TestCase {

    /** The user credentials used. */
    private static Credentials goodCredentials = new Credentials("user@some.realm", "password");
    
    /** Non-existing user credentials. */
    private static Credentials badCredentials = new Credentials("foo", "bar");

    /** The attribute request used. */
    private static final String[] goodRequest = {"someAttribute"};

    /** The expected attribute values. */
    private static final String[] goodValues = {"someValue"};
    
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
            
            // Test successful authentication.
            dm.setConfig(config);
            UserAttribute[] attributes = dm.authenticate(goodCredentials, new String[] {});
            
            // Verify attributes.
            Assert.assertEquals("Attributes were returned", attributes.length, 0);
         
            
        } catch (DirectoryManagerException e) {
            e.printStackTrace();
            Assert.fail("Unexpected DirectoryManagerException");
        }

    }
    
    
    /**
     * Successful authentication with attribute request.
     */
    public void testGoodAuthenticationWithAttributes() {

        // Set configuration properties.
        Properties config = new Properties();
        config.setProperty(DirectoryManagerConfiguration.CONFIGURATION_PROPERTY, goodConfiguration);

        try {
            
            // Test successful authentication.
            dm.setConfig(config);
            UserAttribute[] attributes = dm.authenticate(goodCredentials, goodRequest);
            
            // Verify attributes.
            Assert.assertNotNull("No attributes returned", attributes);
            Assert.assertEquals("Unexpected number of attributes returned after authentication", goodRequest.length, attributes.length);
            String[] values = attributes[0].getValues();
            Assert.assertEquals("Unexpected number of attribute values returned after authentication", values.length, goodValues.length);
            Assert.assertEquals("Attribute values doesn't match", values[0], goodValues[0]);
            
        } catch (DirectoryManagerException e) {
            e.printStackTrace();
            Assert.fail("Unexpected DirectoryManagerException");
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
            UserAttribute[] attributes = dm.authenticate(badCredentials, new String[] {});
            Assert.assertNull("Attributes were returned", attributes);
            Assert.fail("Bad authentication succeeded");
            
        } catch (AuthenticationFailedException e) {
            // Expected.
        } catch (DirectoryManagerException e) {
            e.printStackTrace();
            Assert.fail("Unexpected DirectoryManagerException");
        }

    }
    
    
    /**
     * Authentication attempt without configuration set.
     */
    public void testNoConfiguration() {

        try {
            
            // Test authentication without configuration.
            UserAttribute[] attributes = dm.authenticate(goodCredentials, goodRequest);
            Assert.assertNull("Attributes were returned", attributes);
            Assert.fail("Authentication without configuration succeeded");

        } catch (DirectoryManagerConfigurationException e) {
            // Expected.
        } catch (BackendException e) {
            e.printStackTrace();
            Assert.fail("Unexpected BackendException");
        }

    }
    
    
    /**
     * Test configuration with unknown index class.
     */
    public void testMissingIndexClass() {
        
        // Set configuration properties.
        Properties config = new Properties();
        config.setProperty(DirectoryManagerConfiguration.CONFIGURATION_PROPERTY, "src/test/conf/MissingIndexClassConfiguration.xml");

        try {
            
            // Test bogus config.
            dm.setConfig(config);
            Assert.fail("Managed to set up bad configuration");
         
            
        } catch (DirectoryManagerConfigurationException e) {
            // Expected.
        }
        
    }
    
    
    /**
     * Test configuration with missing index file.
     */
    public void testMissingIndexFile() {
        
        // Set configuration properties.
        Properties config = new Properties();
        config.setProperty(DirectoryManagerConfiguration.CONFIGURATION_PROPERTY, "src/test/conf/MissingIndexFileConfiguration.xml");

        try {
            
            // Test bogus config.
            dm.setConfig(config);
            Assert.fail("Managed to set up bad configuration");
         
            
        } catch (DirectoryManagerConfigurationException e) {
            // Expected.
        }
        
    }

}