package no.feide.moria.directory.index;

import junit.framework.*;

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
        String[] nonExisting = {null, "", "@", "user@another.realm", "anotheruser@some.realm"};
        for (int i = 0; i < nonExisting.length; i++)
            Assert.assertEquals("Reference should not exist", null, index.lookup(nonExisting[i]));

        // Look up the existing entry.
        Assert.assertEquals("Reference does not exist", "ldap://my.ldap.server:636/dc=search,dc=base", index.lookup("user@some.realm").get(0));

    }

}