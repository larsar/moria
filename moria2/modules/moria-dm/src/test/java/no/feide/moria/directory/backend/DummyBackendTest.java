package no.feide.moria.directory.backend;

import junit.framework.*;
import no.feide.moria.directory.Credentials;
import no.feide.moria.directory.IllegalAttributeException;
import no.feide.moria.directory.UserAttribute;

/**
 * JUnit tests for the <code>DummyBackend</code> class.
 */
public class DummyBackendTest
extends TestCase {
    
    /** Internal representation of the backend. */
    private DummyBackend backend;

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
        // Should call open(), but does nothing.

    }


    /**
     * Clean up.
     */
    public void tearDown() {

        // Should call close(), but does nothing.
        backend = null;
        
    }
    
    
    /**
     * 
     */
    public void testAuthenticate() {
        
        // Prepare.
        Credentials goodCredentials = new Credentials("test@feide.no", "test");
        Credentials[] badCredentials = {new Credentials("test@feide.nO", "test"), new Credentials("test@feide.no", "Test"), null};
        String[] goodRequest = {"eduPersonAffiliation"};
        String[][] noRequests = {null, {""}};
        String[] badRequest = {"someNonExistingAttribute"};
        String[] goodValues = {"Affiliate"};
        UserAttribute goodAttribute = null;
        try {
            goodAttribute = new UserAttribute("eduPersonAffiliation", goodValues);
        } catch (IllegalAttributeException e) {
            Assert.fail("Unexpected IllegalAttributeException");
        }
        Assert.assertNotNull("User attribute was not instantiated", goodAttribute);
        UserAttribute[] goodAttributes = {goodAttribute};
       
        // Test unsuccessful authentication.
        for (int i=0; i<badCredentials.length; i++)
	        try {
	            backend.authenticate(badCredentials[i], goodRequest);
	            Assert.fail("Bad authentication succeeded");
	        } catch (AuthenticationFailedException e) {
	            // Expected.
	        } catch (BackendException e) {
	            Assert.fail("Unexpected BackendException");
	        }
        
        // Test successful authentication, without requested attributes.
	    for (int i=0; i<noRequests.length; i++)
	        try {
	        	backend.authenticate(goodCredentials, noRequests[i]);
	        } catch (AuthenticationFailedException e) {
	            Assert.fail("Authentication failed (AuthenticationFailedException)");
	        } catch (BackendException e) {
	            Assert.fail("Unexpected BackendException");
	        }
	        
	    // Test successful authentication, with non-existing requested attributes.
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
