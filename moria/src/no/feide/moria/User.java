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
 * @author Cato Olsen
 */
public class User {
    
    /** Used for logging. */
    private static Logger log = Logger.getLogger(User.class.toString());
    
    /**
     * The LDAP context, initialized by
     * <code>authenticate(String, String)</code>. */
    private static InitialLdapContext ldap;
    
    /**
     * Used to store the user element's relative DN, as found by
     * <code>findRDN(String)</code>.
     */
    private static String rdn;
    
    
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
        
        // Get and verify properties.
        String keyStore = System.getProperty("no.feide.moria.Backend.LDAP.keyStore");
        String keyStorePassword = System.getProperty("no.feide.moria.Backend.LDAP.keyStorePassword", null);
        String trustStore = System.getProperty("no.feide.moria.Backend.LDAP.trustStore");
        String trustStorePassword = System.getProperty("no.feide.moria.Backend.LDAP.trustStorePassword", null);
        String ldapHost = System.getProperty("no.feide.moria.Backend.LDAP.Host");
        if (ldapHost == null)
            throw new BackendException("Required preference no.feide.moria.Backend.LDAP.Host not set");
        String ldapPort = System.getProperty("no.feide.moria.Backend.LDAP.Port");
        if (ldapPort == null)
            throw new BackendException("Required preference no.feide.moria.Backend.LDAP.Port not set");
        String ldapBase = System.getProperty("no.feide.moria.Backend.LDAP.Base");
        if (ldapBase == null)
            throw new BackendException("Required preference no.feide.moria.Backend.LDAP.Base not set");
        String ldapUid = System.getProperty("no.feide.moria.Backend.LDAP.UIDAttribute");
        if (ldapUid == null)
            throw new BackendException("Required preference no.feide.moria.Backend.LDAP.UIDAttribute not set");
               
        // Only enable SSL if all necessary info is available.
        Hashtable env = new Hashtable();
        if ( /*(keyStore != null) &&
             (keyStorePassword != null) &&*/
             (trustStore != null) &&
             (trustStorePassword != null) ) {
            log.config("SSL enabled");
            env.put(Context.SECURITY_PROTOCOL, "ssl");
            Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
            /*
            System.setProperty("javax.net.ssl.keyStore", keyStore);
            System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword); 
            */
            System.setProperty("javax.net.ssl.trustStore", trustStore);
            System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
        }
        else
            log.config("SSL disabled");
        
        try {

            // Prepare LDAP context and look up user element.
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, "ldap://"+ldapHost+':'+Integer.valueOf(ldapPort)+'/'+ldapBase);
            env.put(Context.REFERRAL, "throw");  // To catch referrals.
	    log.info("DEBUG 1");  // DEBUG
            ldap = new InitialLdapContext(env, null);
	    log.info("DEBUG 2");  // DEBUG
            ldap.addToEnvironment(Context.SECURITY_PRINCIPAL, "");
            ldap.addToEnvironment(Context.SECURITY_CREDENTIALS, "");
	    log.info("DEBUG 3");  // DEBUG
            rdn = findRDN(ldapUid+'='+username);
	    log.info("DEBUG 4");  // DEBUG
            if (rdn == null) {
                // No user element found.
                log.fine("No subtree match for "+ldapUid+'='+username+" at "+ldap.getNameInNamespace()+ " on "+ldap.getEnvironment().get(Context.PROVIDER_URL));
                return null;
            }

            // Authenticate.
            ldap.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
            ldap.addToEnvironment(Context.SECURITY_PRINCIPAL, rdn+','+ldap.getNameInNamespace());
            ldap.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
	    log.info("DEBUG 5");  // DEBUG
            try {
                ldap.reconnect(null);
		log.info("DEBUG 6");  // DEBUG
                log.fine("Authenticated "+rdn+','+ldap.getNameInNamespace()+" on "+ldap.getEnvironment().get(Context.PROVIDER_URL));
                return new User(ldap, rdn);  // Success.
            } catch (AuthenticationException e) {
                log.fine("Failed to authenticate "+rdn+','+ldap.getNameInNamespace()+" on "+ldap.getEnvironment().get(Context.PROVIDER_URL));
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
     * @param pattern The search pattern.
     * @return The element's relative (to the context's search base) DN, or
     *         <code>null</code> if none was found.
     * @throws BackendException If a NamingException occurs.
     */
    private static String findRDN(String pattern)
    throws BackendException {
        log.finer("findRDN("+pattern+')');
        
        try {
            
            log.info("DEBUG a");
            NamingEnumeration results = ldap.search("", pattern, new SearchControls(SearchControls.SUBTREE_SCOPE, 1, 0, new String[] {}, false, true));
            log.info("DEBUG b");
            try {
                
                // Nothing matched the pattern.
                if (!results.hasMore()) {
                    log.fine("No match for "+pattern+" on "+ldap.getEnvironment().get(Context.PROVIDER_URL));
                    return null;
                }
                
            } catch (LdapReferralException e) {
                
                // We just caught a referral. Follow it recursively.
                Hashtable refEnv = e.getReferralContext().getEnvironment();
                log.fine("Matched "+pattern+" on "+ldap.getEnvironment().get(Context.PROVIDER_URL)+" to referral "+refEnv.get(Context.PROVIDER_URL));
                ldap.close();
                ldap = new InitialLdapContext(refEnv, null);
                return findRDN(pattern);
                
            }
            
            // We just found an element.
            SearchResult entry = (SearchResult)results.next();
            //            assert entry != null : entry;  // Sanity check.
            log.fine("Matched "+pattern+" on "+ldap.getEnvironment().get(Context.PROVIDER_URL)+" to element "+entry.getName()+','+ldap.getNameInNamespace());
            return entry.getName();
            
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
            // Translate from BasicAttribute to UserAttribute.
            HashMap newAttrs = new HashMap();
            NamingEnumeration oldAttrs = ldap.getAttributes(rdn, attributes).getAll();
            BasicAttribute oldAttr = null;
            while (oldAttrs.hasMore()) {
                oldAttr = (BasicAttribute)oldAttrs.next();
                Vector newValues = new Vector();
                for (int i=0; i<oldAttr.size(); i++)
                    newValues.add(new String((String)oldAttr.get(i)));
                newAttrs.put(oldAttr.getID(), newValues);
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
