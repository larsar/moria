package no.feide.moria.directory.backend;

import java.util.HashMap;

import junit.framework.*;
import no.feide.moria.directory.Credentials;

/**
 * JUnit tests for the <code>DummyBackend</code> class.
 */
public class DummyBackendTest
extends TestCase {

    /** Internal representation of the backend. */
    private DummyBackend backend;

    /** A good attribute request. */
    private static final String[] goodRequest = {"someAttribute"};

    /** An array of empty attribute requests. */
    private static final String[][] noRequests = {null, {""}};

    /** An array of bad attribute requests. */
    private static final String[] badRequest = {"someNonExistingAttribute"};

    /** The expected resulting value from the good attribute request. */
    private static final String[] goodValues = {"someValue"};

    /** Working credentials. */
    private static final Credentials goodCredentials = new Credentials("user@some.realm", "password");

    /** Non-working credentials. */
    private static final Credentials[] badCredentials = {new Credentials("user@another.realm", "password"), new Credentials("test@feide.no", "Test"), null};


    /**
     * Returns the full test suite.
     * @return The test suite.
     */
    public static Test suite() {

        return new TestSuite(DummyBackendTest.class);

    }


    /**
     * Prepare.
     */
    public void setUp() {

        backend = new DummyBackend();
        Assert.assertNotNull("Failed to instantiate backend object", backend);
        backend.open("foobar"); // Does nothing.

    }


    /**
     * Clean up.
     */
    public void tearDown() {

        backend.close(); // Does nothing.

    }


    /**
     * Test unsuccessful authentication without attribute request.
     */
    public void testBadAuthenticationWithoutAttributes() {

        // Test unsuccessful authentication.
        for (int i = 0; i < badCredentials.length; i++)
            try {
                backend.authenticate(badCredentials[i], null);
                Assert.fail("Bad authentication succeeded");
            } catch (AuthenticationFailedException e) {
                // Expected.
            } catch (IllegalArgumentException e) {
                // Expected.
            }

    }


    /**
     * Test successful authentication without attribute request.
     */
    public void testGoodAuthenticationWithoutAttributes() {

        // Test successful authentication, without requested attributes.
        for (int i = 0; i < noRequests.length; i++)
            try {
                backend.authenticate(goodCredentials, noRequests[i]);
            } catch (AuthenticationFailedException e) {
                Assert.fail("Authentication failed (AuthenticationFailedException)");
            }

    }


    /**
     * Test successful authentication with bad attribute request.
     */
    public void testGoodAuthenticationWithBadAttributes() {

        // Test successful authentication, with non-existing requested
        // attributes.
        HashMap attributes = null;
        try {
            attributes = backend.authenticate(goodCredentials, badRequest);
        } catch (AuthenticationFailedException e) {
            Assert.fail("Authentication failed (AuthenticationFailedException)");
        }
        Assert.assertNotNull("No attributes returned", attributes);
        Assert.assertEquals("Non-existing attributes returned after authentication", 0, attributes.size());

    }


    /**
     * Test successful authentication with good attribute request.
     */
    public void testGoodAuthenticationWithGoodAttributes() {

        HashMap attributes = null;
        try {
            attributes = backend.authenticate(goodCredentials, goodRequest);
        } catch (AuthenticationFailedException e) {
            Assert.fail("Authentication failed (AuthenticationFailedException)");
        }
        HashMap goodAttributes = new HashMap();
        goodAttributes.put(goodRequest[0], goodValues);
        Assert.assertNotNull("No attributes returned", attributes);
        Assert.assertEquals("Unexpected number of attributes returned after authentication", goodAttributes.size(), attributes.size());
        String[] values = (String[]) attributes.get(goodRequest[0]);
        Assert.assertEquals("Unexpected number of attribute values returned after authentication", values.length, goodValues.length);
        Assert.assertEquals("Attribute values doesn't match", values[0], goodValues[0]);

    }

}