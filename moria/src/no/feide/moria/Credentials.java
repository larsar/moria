package no.feide.moria;

/**
 * Represents a set of user authentication credentials. For now, only
 * username/password type credentials are supported.
 * @author Cato Olsen
 */
public class Credentials {
    
    /** Type of credentials is undefined. */
    public static final int UNDEFINED = 0;
    
    /** 
     * Type of credentials is username/password. <code>getIdentifier()</code>
     * returns the username as a <code>String</code>.
     * <code>getCredentials()</code> returns the password as a
     * <code>String</code>.
     */
    public static final int PASSWORD = 1;
    
    /**
     * Used to hold type of credentials. Set by the appropriate
     * constructor.
     */
    private int type;
    
    /** Used to hold the user's identifier. */
    private Object id;
    
    /** Used to hold the user's credentials. */
    private Object cred;
    
    
    /**
     * Empty constructor.
     */
    public Credentials() {
        type = UNDEFINED;
    }
    
    
    /**
     * Username/password style constructor.
     * @param username Username, used as unique identifier.
     * @param password Password.
     */
    public Credentials(String username, String password) {
        type = PASSWORD;
        id = username;
        cred = password;
    }
    
    
    /**
     * Set credentials on an existing object.
     */
    public void set(Credentials c) {
        type = c.getType();
        id = c.getIdentifier();
        cred = c.getCredentials();
    }
    
    
    /**
     * Returns the type of credentials.
     * @return Type of credentials.
     */
    public int getType() {
        return type;
    }
    
    /**
     * Sets the type of credentials.
     * @param type The new type.
     */
    public void getType(int type) {
        this.type = type;
    }
    
    
    /**
     * Returns the unique identifier.
     * @return The identifier.
     */
    public Object getIdentifier() {
        return (String)id;
    }
    
    
    /**
     * Sets the unique identifier.
     * @param id The new identifier.
     */
    public void getIdentifier(Object id) {
        this.id = id;
    }
    
    
    /**
     * Returns the credentials.
     * @return The credentials.
     */
    public Object getCredentials() {
        return (String)cred;
    }
    
    
    /**
     * Sets the credentials.
     * @param The new credentials.
     */
    public void setCredentials(Object cred) {
        this.cred = cred;
    }
    
    
    /**
     * Returns a string representation of the credentials.
     */
    public String toString() {
        return new String((String)id+':'+(String)cred);
    }
}
