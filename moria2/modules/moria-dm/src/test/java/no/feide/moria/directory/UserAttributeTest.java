package no.feide.moria.directory;

import junit.framework.*;

/**
 * JUnit tests for the <code>UserAttribute</code> class.
 */
public class UserAttributeTest
extends TestCase {
    
    /** Legal attribute name. */
    private static final String legalName = "legalName";
    
    /** Illegal attribute values. */
    private static final String[] illegalValues = {"value1", "", "value3", null, "value4"};
    
    /** Legal attribute values. */
    private static final String[] legalValues = {"value1", "value2", "value3"};
    
    /** Internal representation of the user attribute. */
    private UserAttribute attribute;

    /**
     * Returns the full test suite.
     * @return The test suite.
     */
    public static Test suite() {

        return new TestSuite(UserAttributeTest.class);
        
    }
    
    /**
     * Prepare.
     */
    public void setUp() {

        // Create a few illegal attributes.
        String[] illegalNames = {"", null};
        for (int i=0; i<illegalNames.length; i++)
	        try {
	            Assert.assertNull("Successfully created an illegal attribute", new UserAttribute(illegalNames[i], new String[] {}));
	        } catch (IllegalArgumentException e) {
	            // Normal.
	        }
	    try {
	        Assert.assertNull("Successfully created an illegal attribute", new UserAttribute(legalName, illegalValues));
	    } catch (IllegalArgumentException e) {
	        // Normal.
	    }
        
        // Create a legal attribute.
	    attribute = new UserAttribute(legalName, legalValues);
        Assert.assertNotNull("Failed to create a legal attribute", attribute);

    }


    /**
     * Clean up.
     */
    public void tearDown() {

        attribute = null;

    }

    
    /**
     * Test <code>getName()</code>.
     */
    public void testGetName() {

        Assert.assertEquals("Attribute name is incorrect", attribute.getName(), legalName);

    }
    
    
    /**
     * Test <code>getAttributes()</code>.
     */
    public void testGetAttributes() {
        
        String[] values = attribute.getValues();
        Assert.assertEquals("Returned attribute array of different length", values.length, legalValues.length);
        for (int i=0; i<values.length; i++)
            Assert.assertEquals("Value "+i+" differs", legalValues[i], values[i]);
                
        
    }

}
