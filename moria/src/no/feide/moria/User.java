/*
 * User.java
 *
 * Created on January 7, 2003, 2:52 PM
 */

package no.feide.moria;

import java.security.Security;
import java.util.*;
import java.util.logging.Logger;
import javax.naming.*;
import javax.naming.directory.*;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapReferralException;

import java.io.File;


/**
 * Represents a user in the backend. Used to authenticate users and retrieve
 * the associated attributes.
 */
public class User {
    
    /** Used for logging. */
    private static Logger log = Logger.getLogger(User.class.toString());
    
    /**
     * The LDAP context, initialized by
     * <code>authenticate(String, String)</code>. */
    private InitialLdapContext ldap;
    
    /** Used to store the user element's relative DN. */
    private String rdn;  
       
    /**
     * Constructor. Used internally in
     * <code>authenticate(String, String)</code> to return an authenticated
     * User object from which attributes can be retrieved.
     */
    protected User(InitialLdapContext ldap, String rdn) {
        this.ldap = ldap;
        this.rdn = rdn;
    }
    
    /**
     * Initializes the connection to the backend, and authenticates the user
     * using the supplied credentials.
     * @param c User's credentials. Only username/password type
     *          credentials are currently supported.
     * @return <code>null</code> if authentication was unsuccessful (bad
     *         or <code>null</code> username/password), otherwise an
     *         <code>User</code> element.
     * @throws BackendException If a NamingException is thrown, or if any of
     *                          the following properties cannot be found:
     *                          <ul>
     *                           <li>no.feide.moria.Backend.LDAP.Host
     *                           <li>no.feide.moria.Backend.LDAP.Port
     *                           <li>no.feide.moria.Backend.LDAP.Base
     *                           <li>no.feide.moria.Backend.LDAP.UIDAttribute
     *                          </ul>
     *                          Also thrown if the type of credentials is not
     *                          supported.
     */
    public static User authenticate(Credentials c)
    throws BackendException {
        log.finer("authenticate(Credentials c)");
        
        // Validate credentials.
        if (c.getType() != Credentials.PASSWORD) {
            log.severe("Unsupported credentials");
            throw new BackendException("Unsupported credentials");
        }
        String username = (String)c.getIdentifier();
        String password = (String)c.getCredentials();
        
        // Sanity check.
        if ( (username == null) || (password == null) ||
             (username.length() == 0) || (password.length() == 0) )
            return null;
        
        // Get and verify some properties.
        String keyStore = System.getProperty("no.feide.moria.backend.ldap.keystore");
        if (keyStore != null) {
            log.config("Key store is "+keyStore);
            System.setProperty("javax.net.ssl.keyStore", keyStore);
        }
        String keyStorePassword = System.getProperty("no.feide.moria.backend.ldap.keystorePassword");
        if (keyStorePassword != null)
                System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword); 
        String trustStore = System.getProperty("no.feide.moria.backend.ldap.trustStore");
        if (trustStore != null) {
            log.config("Trust store is "+trustStore);
            System.setProperty("javax.net.ssl.trustStore", trustStore);
        }
        String trustStorePassword = System.getProperty("no.feide.moria.backend.ldap.trustStorePassword");
        if (trustStorePassword != null)
                System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
        String usernameAttribute = System.getProperty("no.feide.moria.backend.ldap.usernameAttribute");
        if (usernameAttribute == null)
            throw new BackendException("Required property no.feide.moria.backend.ldap.usernameAttribute not set");
        log.config("User name attribute is "+usernameAttribute);
	String ldapURL = System.getProperty("no.feide.moria.backend.ldap.url");
	if (ldapURL == null)
	    throw new BackendException("Required property no.feide.moria.backend.ldap.url not set");
        log.config("LDAP URL is "+ldapURL);
               
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        try {
            
            // Prepare LDAP context and look up user element.
            Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, ldapURL);
            env.put(Context.REFERRAL, "throw");  // To catch referrals.
            InitialLdapContext ldap = new InitialLdapContext(env, null);
	    log.config("Connected to "+env.get(Context.PROVIDER_URL));
            ldap.addToEnvironment(Context.SECURITY_PRINCIPAL, "");
            ldap.addToEnvironment(Context.SECURITY_CREDENTIALS, "");
            String rdn = ldapSearch(ldap, usernameAttribute+'='+username);
            if (rdn == null) {
                // No user element found.
                log.fine("No subtree match for "+usernameAttribute+'='+username+" on "+ldap.getEnvironment().get(Context.PROVIDER_URL));
                return null;
            }
            log.fine("Found element at "+rdn+" on "+ldap.getEnvironment().get(Context.PROVIDER_URL));

            // Authenticate.
            ldap.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
            ldap.addToEnvironment(Context.SECURITY_PRINCIPAL, rdn+','+ldap.getNameInNamespace());
            ldap.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
            try {
                ldap.reconnect(null);
                log.fine("Authenticated as "+rdn+" on "+ldap.getEnvironment().get(Context.PROVIDER_URL));
                return new User(ldap, rdn);  // Success.
            } catch (AuthenticationException e) {
                log.fine("Failed to authenticate as "+rdn+" on "+ldap.getEnvironment().get(Context.PROVIDER_URL));
                return null;  // Failure.
            }
            
        } catch (NamingException e) {
            log.severe("NamingException caught and re-thrown as BackendException");
            throw new BackendException(e);
        }
    }
    
    
    /**
     * Do a subtree search for an element given a pattern. Only the first
     * element found is considered. Any referrals are followed recursively.
     * @param ldap The LDAP context.
     * @param pattern The search pattern.
     * @return The element's relative DN, or <code>null</code> if none was
     *         found.
     * @throws BackendException If a NamingException occurs.
     */
    private static String ldapSearch(InitialLdapContext ldap, String pattern)
    throws BackendException {
        log.finer("ldapSearch(String)");
        
        try {
            
            NamingEnumeration results = ldap.search("", pattern, new SearchControls(SearchControls.SUBTREE_SCOPE, 1, 0, new String[] {}, false, true));
            try {
                
                // Nothing matched the pattern.
                if (!results.hasMore()) {
                    log.warning("No match for "+pattern+" on "+ldap.getEnvironment().get(Context.PROVIDER_URL));
                    return null;
                }
                
            } catch (LdapReferralException e) {
                
                // We just caught a referral. Follow it recursively, enabling
                // SSL.
                log.info("Enabling SSL");
                Hashtable refEnv = new Hashtable();
                refEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
                refEnv.put(Context.REFERRAL, "throw");  // To catch referrals.
                refEnv.put(Context.SECURITY_PROTOCOL, "ssl");
                refEnv = e.getReferralContext(refEnv).getEnvironment();
                log.info("Matched "+pattern+" on "+ldap.getEnvironment().get(Context.PROVIDER_URL)+" to referral "+refEnv.get(Context.PROVIDER_URL));
                ldap.close();
                ldap = new InitialLdapContext(refEnv, null);
                return ldapSearch(ldap, pattern);
                
            }
            
            // We just found an element.
            SearchResult entry = (SearchResult)results.next();
            //assert entry != null : entry;  // Sanity check.
            String rdn = entry.getName();
            log.info("Matched "+pattern+" on "+ldap.getEnvironment().get(Context.PROVIDER_URL)+" to element "+rdn);
            return rdn;
            
        } catch (NamingException e) {
            e.printStackTrace();
            log.severe("NamingException caught and re-thrown as BackendException");
            throw new BackendException(e);
        }
    }
    
    
    
    /**
     * Retrieves a list of attributes from a user element.
     * @param attributes User element attribute names.
     * @return The requested user attributes.
     * @throws BackendException If a NamingException occurs.
     */
    public HashMap lookup(String[] attributes)
    throws BackendException {
        log.finer("lookup(String[])");
        
        try {
            // Translate from BasicAttribute to HashMap, using the original
            // attribute names from the request.
            HashMap newAttrs = new HashMap();
            Attributes oldAttrs = ldap.getAttributes(rdn, attributes);
            Attribute oldAttr = null;
            for (int i=0; i<attributes.length; i++) {
                oldAttr = oldAttrs.get(attributes[i]);
                Vector newValues = new Vector();
                for (int j=0; j<oldAttr.size(); j++)
                    newValues.add(new String((String)oldAttr.get(j)));
                newAttrs.put(attributes[i], newValues);
            }
            return newAttrs;
        }
        catch (NamingException e) {
            log.severe("NamingException caught and re-thrown as BackendException");
            throw new BackendException(e);
        }
    }
    
    
    /**
     * Closes the connection to the LDAP server, as per
     * <code>javax.naming.InitialContext.close()</code>.
     * @throws BackendException If a NamingException occurs.
     */
    public void close()
    throws BackendException {
        log.finer("close()");
        try {
            ldap.close();
        }
        catch (NamingException e) {
            log.severe("NamingException caught and re-thrown as BackendException");
            throw new BackendException(e);
        }
    }
    
}
