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
public class DummyDirectoryManagerTest
extends TestCase {

    /** The user credentials used. */
    private static Credentials goodCredentials = new Credentials("user@some.realm", "password");

    /** The attribute request used. */
    private static final String[] goodRequest = {"someAttribute"};

    /** The expected attribute values. */
    private static final String[] goodValues = {"someValue"};
    
    /** The Directory Manager instance. */
    private DirectoryManager dm;


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
     * Test the <code>authenticate(Credentials, String[])</code> method.
     */
    public void testAuthentication() {

        // Authenticate.
        Properties config = new Properties();
        config.setProperty("no.feide.moria.directory.configuration", "src/test/conf/DummyConfiguration.xml");
        UserAttribute[] attributes = null;
        try {
            dm.setConfig(config);
            attributes = dm.authenticate(goodCredentials, goodRequest);
        } catch (DirectoryManagerException e) {
            e.printStackTrace();
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