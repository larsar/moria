package no.feide.moria.directory;

import junit.framework.*;
import no.feide.moria.directory.index.*;

/**
 * JUnit tests for the <code>DummyIndex</code> class.
 */
public class DummyIndexTest
extends TestCase {
    
    /** Internal representation of the index. */
    private DummyIndex index;

    /**
     * Returns the full test suite.
     * @return The test suite.
     */
    public static Test suite() {

        return new TestSuite(DummyIndexTest.class);
        
    }
    
    /**
     * Prepare.
     */
    public void setUp() {

        index = new DummyIndex();

    }


    /**
     * Clean up.
     */
    public void tearDown() {

        index = null;

    }

    
    /**
     * Test dummy index lookups.
     */
    public void testLookup() {

        // Look up some non-existing entries.
        String[] nonExisting = {null, "", "@", "test@feide.nø", "TEST@FEIDE.NO", "Test@feide.no"};
        for (int i=0; i<nonExisting.length; i++)
            Assert.assertEquals("Reference should not exist", null, index.lookup(nonExisting[i]));
        
        // Look up the existing entry.
        Assert.assertEquals("Reference should exist", "ldap://ldap.feide.no:636/ou=people,dc=feide,dc=no", index.lookup("test@feide.no"));

    }

}
