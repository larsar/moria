package no.feide.moria.directory.backend;

import junit.framework.*;
import no.feide.moria.directory.Credentials;
import no.feide.moria.directory.UserAttribute;

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
        // Should really call open(), but won't do anything.

    }


    /**
     * Clean up.
     */
    public void tearDown() {

        // Should really call close(), but won't do anything.
        backend = null;

    }


    /**
     * Test the <code>authenticate(Credentials, String[])</code> method.
     */
    public void testAuthenticate() {

        // Prepare.
        Credentials goodCredentials = new Credentials("user@some.realm", "password");
        Credentials[] badCredentials = {new Credentials("user@another.realm", "password"), new Credentials("test@feide.no", "Test"), null};

        UserAttribute goodAttribute = null;
        goodAttribute = new UserAttribute("someAttribute", goodValues);
        Assert.assertNotNull("User attribute was not instantiated", goodAttribute);
        UserAttribute[] goodAttributes = {goodAttribute};

        // Test unsuccessful authentication.
        for (int i = 0; i < badCredentials.length; i++)
            try {
                backend.authenticate(badCredentials[i], goodRequest);
                Assert.fail("Bad authentication succeeded");
            } catch (AuthenticationFailedException e) {
                // Expected.
            } catch (IllegalArgumentException e) {
                // Expected.
            } catch (BackendException e) {
                Assert.fail("Unexpected BackendException");
            }

        // Test successful authentication, without requested attributes.
        for (int i = 0; i < noRequests.length; i++)
            try {
                backend.authenticate(goodCredentials, noRequests[i]);
            } catch (AuthenticationFailedException e) {
                Assert.fail("Authentication failed (AuthenticationFailedException)");
            } catch (BackendException e) {
                Assert.fail("Unexpected BackendException");
            }

        // Test successful authentication, with non-existing requested
        // attributes.
        UserAttribute[] attributes = null;
        try {
            attributes = backend.authenticate(goodCredentials, badRequest);
        } catch (AuthenticationFailedException e) {
            Assert.fail("Authentication failed (AuthenticationFailedException)");
        } catch (BackendException e) {
            Assert.fail("Unexpected BackendException");
        }
        Assert.assertNotNull("No attributes returned", attributes);
        Assert.assertEquals("Non-existing attributes returned after authentication", 0, attributes.length);

        // Test successful authentication, with requested attributes.
        attributes = null;
        try {
            attributes = backend.authenticate(goodCredentials, goodRequest);
        } catch (AuthenticationFailedException e) {
            Assert.fail("Authentication failed (AuthenticationFailedException)");
        } catch (BackendException e) {
            Assert.fail("Unexpected BackendException");
        }
        Assert.assertNotNull("No attributes returned", attributes);
        Assert.assertEquals("Unexpected number of attributes returned after authentication", goodAttributes.length, attributes.length);
        String[] values = attributes[0].getValues();
        Assert.assertEquals("Unexpected number of attribute values returned after authentication", values.length, goodValues.length);
        Assert.assertEquals("Attribute values doesn't match", values[0], goodValues[0]);

    }

}