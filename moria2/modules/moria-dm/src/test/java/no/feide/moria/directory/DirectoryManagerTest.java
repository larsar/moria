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
import junit.framework.Assert;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * JUnit tests for the DirectoryManager class.
 * @author Cato Olsen
 */
public class DirectoryManagerTest
extends TestCase {

    /** Internal representation of the configuration properties. */
    private Properties config;
    
    /** The user credentials used. */
    private static final Credentials goodCredentials = new Credentials("test@feide.no", "test");
    
    /** The attribute request used. */
    private static final String[] goodRequest = {"eduPersonAffiliation"};
    
    /** The expected attribute values. */
    private static final String[] goodValues = {"Affiliate"}; 


    /**
     * Returns the full test suite.
     * @return The test suite.
     */
    public static Test suite() {

        return new TestSuite(DirectoryManagerTest.class);
    }


    /**
     * Prepare.
     */
    public void setUp() {

        config = new Properties();
        config.setProperty("directoryConfiguration", "src/test/conf/DirectoryManagerConfiguration.xml");

    }


    /**
     * Clean up.
     */
    public void tearDown() {

        config = null;

    }


    /**
     * Test the <code>setConfig(Properties)</code> method.
     */
    public void testSetConfig() {

        try {
            DirectoryManager.setConfig(config);
        } catch (DirectoryManagerConfigurationException e) {
            Assert.fail("Unexpected DirectoryManagerConfigurationException");
        }

    }
    
    
    /**
     * Test the <code>authenticate(Credentials, String[])</code> method.
     */
    public void testAuthenticate() {
        
        // Authenticate.
        UserAttribute[] attributes = null;
        try {
            attributes = DirectoryManager.authenticate(goodCredentials, goodRequest);
        } catch (DirectoryManagerException e) {
            Assert.fail("Unexpected DirectoryManagerException");
        }
        
        // Verify attributes.
        Assert.assertNotNull("No attributes returned", attributes);
    	Assert.assertEquals("Unexpected number of attributes returned after authentication", goodRequest.length, attributes.length);
    	String[] values = attributes[0].getValues();
    	Assert.assertEquals("Unexpected number of attribute values returned after authentication", values.length, goodValues.length);
    	Assert.assertEquals("Attribute values doesn't match", values[0], goodValues[0]);
        
    }

}