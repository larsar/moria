package no.feide.moria.service;

import java.util.Vector;

// TODO:
// Obsolete, to be removed.
// Use a HashMap w/attribute values as Vector, attribute names as key.
/**
 * Represents a basic user attribute (JAX-RPC friendly).
 * @author Cato Olsen
 */
public class UserAttribute {
    
    /** Attribute name. */
    private String name;
    
    /** List of values. */
    private Vector values;
    
    /**
     * Basic constructor. Nothing is initialized.
     */
    public UserAttribute() {
    }
    
    /**
     * Constructor.
     * @param name The attribute name.
     * @param values The initial value list.
     */
    public UserAttribute(String name, Vector values) {
        this.name = name;
        this.values = values;
    }
    
    /**
     * Sets the attribute name.
     * @param name The new attribute name.
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * Returns the attribute name.
     * @return Attribute name.
     */
    public String getName() {
        return name;
    }
   
    /**
     * Set the entire list of values for this attribute, overwriting any
     * existing values.
     * @param values The new value list.
     */
    public void setValues(Vector values) {
        this.values = values;
    }
    
    /**
     * Get the list of attribute values.
     * @return The attribute values.
     */
    public Vector getValues() {
        return values;
    }
    
    /**
     * Returns a string representation of this attribute.
     * @return String representation.
     */ 
    public String toString() {
        return '('+name+": "+values.toString()+')';
    }
    
}
