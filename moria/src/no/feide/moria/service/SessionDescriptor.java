package no.feide.moria.service;

// TODO:
// Obsolete.

/**
 * Represents a Moria session as first returned to Mellon upon a session
 * request.
 */
public class SessionDescriptor {
    
    /** The unique ID for this session .*/
    private String id;
    
    /** The redirect URl used for this session. */
    private String url;
    
    /**
     * Basic constructor. ID and redirect URL are not initialized. Included
     * to comply with JAX-RPC requirements.
     */
    public SessionDescriptor() {
    }
    
    /**
     * Constructor.
     * @param id Unique session ID.
     * @param url Redirect URL; where Mellon should redirect the client to do
     *            authentication.
     */
    public SessionDescriptor(String id, String url) {
        this.id = id;
        this.url = url;
    }
    
    /** 
     * Sets the unique session ID.
     * @param id Unique session ID.
     */
    public void setID(String id) {
        this.id = id;
    }
    
    /**
     * Gets the unique session ID.
     * @return The unique session ID, or <code>null</code> if not set.
     */
    public String getID() {
        return id;
    }
    
    /**
     * Sets the redirect URL.
     * @param url The redirect URL.
     */
    public void setURL(String url) {
        this.url = url;
    }
    
    /**
     * Gets the redirect URL.
     */
    public String getURL() {
        return url;
    }
    
}
