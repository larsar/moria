/**
 * Copyright (C) 2003 FEIDE
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package no.feide.moria;

import java.security.Security;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Vector;
import java.util.logging.Logger;

import javax.naming.AuthenticationException;
import javax.naming.CommunicationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.TimeLimitExceededException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import no.feide.moria.authorization.WebService;


/**
 * Represents a user in the backend. Used to authenticate users and retrieve
 * the associated attributes.
 */
public class Backend {
    
    /** Used for logging. */
    private static Logger log = Logger.getLogger(Backend.class.toString());
    
    /**
     * The LDAP context, initialized by
     * <code>authenticate(String, String)</code>. */
    private InitialLdapContext ldap;
    
    /** Used to store the user element's relative DN. */
    private String rdn;
    
    /** Used to show whether the system-wide JNDI init has been done. */
    private static boolean initialized = false;
    
    /** Default initial hash table for LDAP context environment. */
    private Hashtable defaultEnv;
    
    
    /**
     * Constructor. Initializes the list of initial LDAP server URLs.
     * @throws ConfigurationException If unable to read from configuration.
     **/
    private Backend()
    throws ConfigurationException {
        log.finer("Backend()");
        
        // Create initial context environment.
        defaultEnv = new Hashtable();
        defaultEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        defaultEnv.put(Context.REFERRAL, "throw");  // To catch referrals.
        defaultEnv.put("java.naming.ldap.derefAliases", "never");  // Due to OpenSSL problems.
		defaultEnv.put(Context.SECURITY_PROTOCOL, "ssl");  // SSL enabled by default.
        
    }
    
    /**
     * Factory method.
     * @return A new instance of <code>Backend</code>.
     * @throws BackendException If a new instance couldn't be created, or
     *                          if a <code>ConfigurationException</code> is
     *                          caught.
     */
    public static Backend getInstance() 
    throws BackendException {
        log.finer("getInstance()");
        
        try {
            if (!initialized)
                init();
            return new Backend();
        } catch (ConfigurationException e) {
            log.severe("ConfigurationException caught and re-thrown as BackendException");
            throw new BackendException("ConfigurationException caught", e);
        }
    }
    
    
    /**
     * Used to do one-time global JNDI initialization, the very first time
     * <code>authenticate(Credentials)</code> is called.
     * @throws ConfigurationException If one of the required properties
     *                                 cannot be resolved.
     */
    private static void init()
    throws BackendException, ConfigurationException {
        log.finer("init()");
        
        // Get and verify some properties.
        String keyStore = Configuration.getProperty("no.feide.moria.backend.ldap.keystore");
        if (keyStore != null) {
            log.config("Key store is "+keyStore);
            System.setProperty("javax.net.ssl.keyStore", keyStore);
        }
        String keyStorePassword = Configuration.getProperty("no.feide.moria.backend.ldap.keystorePassword");
        if (keyStorePassword != null)
                System.setProperty("javax.net.ssl.keyStorePassword", keyStorePassword); 
        String trustStore = Configuration.getProperty("no.feide.moria.backend.ldap.trustStore");
        if (trustStore != null) {
            log.config("Trust store is "+trustStore);
            System.setProperty("javax.net.ssl.trustStore", trustStore);
        }
        String trustStorePassword = Configuration.getProperty("no.feide.moria.backend.ldap.trustStorePassword");
        if (trustStorePassword != null)
                System.setProperty("javax.net.ssl.trustStorePassword", trustStorePassword);
        
        // Wrap up.
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        initialized = true;
    }
  
    
    /**
     * Initializes the connection to the backend, and authenticates the user
     * using the supplied credentials.
     * @param s The session using this particular instance of the backend.
     * @param c User's credentials. Only username/password type
     *          credentials are currently supported.
     * @return <code>false</code> if authentication was unsuccessful (bad
     *         or <code>null</code> username/password), otherwise
     *         <code>true</code>.
     * @throws BackendException If a NamingException is thrown, if the type
     *                          of credentials is not supported, or if a
     *                          <code>ConfigurationException</code> is
     *                          caught.
     */
    public boolean authenticateUser(Session s, Credentials c)
    throws BackendException {
        log.finer("authenticate(Credentials c)");
        
        // Validate credentials.
        if (c.getType() != Credentials.PASSWORD) {
            log.severe("Unsupported credentials");
            throw new BackendException("Unsupported credentials");
        }
        String username = (String)c.getIdentifier();
        String password = (String)c.getCredentials();
        if ( (username == null) || (password == null) ||
             (username.length() == 0) || (password.length() == 0) ) {
            log.fine("Illegal username/password ("+username+'/'+password+')');
            return false;
        }

        try {
            
            // Map user ID domain to LDAP URL and connect to server.
        	String pattern = jndiReset(username);
			Hashtable env = new Hashtable(defaultEnv);
			String urls[] = BackendIndex.lookup(c.getIdentifier().toString());
			
			// Try each backend URL in turn.
			for (int urlIndex = 0; urlIndex < urls.length; urlIndex++) {
				
				env.put(Context.PROVIDER_URL, urls[urlIndex]);
				try {
					ldap = new InitialLdapContext(env, null);
				} catch (CommunicationException e) {
					log.severe("Unable to connect to "+env.get(Context.PROVIDER_URL));
					throw new BackendException("Unable to connect to "+env.get(Context.PROVIDER_URL));
				}
	
	            // Search for user element.
		        log.config("Connected to "+env.get(Context.PROVIDER_URL));
	            ldap.addToEnvironment(Context.SECURITY_PRINCIPAL, "");
	            ldap.addToEnvironment(Context.SECURITY_CREDENTIALS, "");
	            rdn = ldapSearch(pattern);
	            if (rdn == null) {
	            	
	                // No user element found. Try to guess the DN anyway.
	                log.fine("No subtree match for "+pattern+" on "+ldap.getEnvironment().get(Context.PROVIDER_URL));
	                rdn = c.getIdentifier().toString();
	                rdn = "uid="+rdn.substring(0, rdn.indexOf('@'));
	                log.info("Guessing on RDN "+rdn);
	                
	            } else
	            	log.fine("Found element at "+rdn+" on "+ldap.getEnvironment().get(Context.PROVIDER_URL));
	
	            // Authenticate.
	            ldap.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
	            ldap.addToEnvironment(Context.SECURITY_PRINCIPAL, rdn+','+ldap.getNameInNamespace());
	            ldap.addToEnvironment(Context.SECURITY_CREDENTIALS, password);
	            try {
	                ldap.reconnect(null);
	                log.fine("Authenticated as "+rdn+" on "+ldap.getEnvironment().get(Context.PROVIDER_URL));
	                return true;  // Success.
	            } catch (AuthenticationException e) {
	                log.fine("Failed to authenticate as "+rdn+" on "+ldap.getEnvironment().get(Context.PROVIDER_URL));
	                
	                // Do we have any more URLs to try at all?
					if (urlIndex == urls.length) {
						log.warning("No more URLs available for authentication");
						return false;
					}
					
					// Is this service allowed to use multiple URLs for this particular user?
					WebService ws = s.getWebService();
	                if ( (!ws.allowsLocalAuth()) ||
	                     (!ws.hasAffiliation(BackendIndex.getDomain(username))) ){
	                	log.warning("More URLs available for authentication, but not allowed for "+ws.getName());
	                	return false;
	                }
	            }
	            
			}
            
        } catch (NamingException e) {
            log.severe("NamingException caught and re-thrown as BackendException");
            throw new BackendException(e);
        } catch (ConfigurationException e) {
        	log.severe("ConfigurationException caught and re-thrown as BackendException");
        	throw new BackendException(e);
        }
        
        // Should never be reached; returns false in earlier catch.
        return false;
    }
    
    
    /**
     * Do a subtree search for an element given a pattern. Only the first
     * element found is considered. Implemented as a separate method due to
     * recursive referral support (temporarily disabled).
     * <em>Note:</em> The default timeout when searching is 15 seconds,
     *                unless
     *                <code>no.feide.moria.backend.ldap.timeout</code> is
     *                set.
     * @param pattern The search pattern. Must not include the character
     *                 '*' or the substring '\2a' due to possible LDAP
     *                 exploits.
     * @return The element's relative DN, or <code>null</code> if none was
     *          found. <code>null</code> is also returned if the search
     *          pattern contains an illegal character or substring.
     * @throws BackendException If a <code>NamingException</code> occurs, or
     *          if a <code>ConfigurationException</code> is
     *          caught.
     */
    private String ldapSearch(String pattern)
    throws BackendException {
        log.finer("ldapSearch(String)");
        
        // Check for illegal content.
        String[] illegals = {"*", "\2a"};
        for (int i=0; i< illegals.length; i++)
            if (pattern.indexOf(illegals[i]) > -1)
                return null;
        
        try {
            
            NamingEnumeration results;
                        
			// Start counting the (milli)seconds.
			long searchStart = System.currentTimeMillis();
			try {
				results = ldap.search("", pattern, new SearchControls(SearchControls.SUBTREE_SCOPE, 1, 1000*Integer.parseInt(Configuration.getProperty("no.feide.moria.backend.ldap.timeout", "15")), new String[] {}, false, false));
				if (!results.hasMore()) {
					log.warning("No match for "+pattern+" on "+ldap.getEnvironment().get(Context.PROVIDER_URL));
					return null;
				}
			} catch (TimeLimitExceededException e) {
				log.severe("TimelimitExceededException caught after "+(System.currentTimeMillis()-searchStart)+"ms and re-thrown as BackendException");
				throw new BackendException(e);
			}
            
            // We just found an element.
            try {
                log.info("Looking for search results");
                SearchResult entry = (SearchResult)results.next();
                log.info("Getting entry name");
                String rdn = entry.getName();
                log.info("Matched "+pattern+" on "+ldap.getEnvironment().get(Context.PROVIDER_URL)+" to element "+rdn);
                return rdn;
            } catch (TimeLimitExceededException e) {
                log.severe("TimeLimitExceededException caught (when reading search results) and re-thrown as BackendException");
                throw new BackendException(e);
            }
                
        } catch (ConfigurationException e) {
            log.severe("ConfigurationException caught and re-thrown as BackendException");
            throw new BackendException(e);
        } catch (NamingException e) {
            e.printStackTrace();
            log.severe("NamingException caught and re-thrown as BackendException");
            throw new BackendException(e);
        }
    }
    
    
    
    /**
     * Internal method to do JNDI initialization.
     * @param username
     * @return The initial search pattern.
     * @throws ConfigurationException If the property
     *                                <code>no.feide.moria.backend.ldap.usernameAttribute</code>
     *                                is not set.
     * @throws BackendException If thrown by <code>init</code>.
     */
    private String jndiReset(String username)
    throws ConfigurationException, BackendException {
    	log.finer("jndiInit(String)");
    	
    	String pattern = new String();
		if (!initialized)
			init();   
		pattern = Configuration.getProperty("no.feide.moria.backend.ldap.usernameAttribute")+'='+username;
    	return pattern;
    }
    
    
    /**
     * Wrapper for private method <code>ldapSearch</code>, used as a simple
     * check that a given user name exists in the underlying backend.
     * @param username The user name.
     * @return <code>true</code> if the user name exists, otherwise
     *         <code>false</code>.
     * @throws BackendException Passed on from <code>ldapSearch</code> or
     *                          <code>BackendIndex.lookup</code>, or thrown
     *                          locally if unable to connect to the
     *                          authentication server.
     */
    public boolean userExists(String username)
    throws BackendException {
    	log.finer("userExists(String");
   	
    	// Map user ID domain to LDAP URL and connect to server.
    	Hashtable env = new Hashtable(defaultEnv);
    	try {

			// Try each backend URL in turn.
    		String pattern = jndiReset(username);
    		String urls[] = BackendIndex.lookup(username);
			for (int urlIndex = 0; urlIndex < urls.length; urlIndex++) {
				
				env.put(Context.PROVIDER_URL, urls[urlIndex]);
				try {
					ldap = new InitialLdapContext(env, null);
				} catch (CommunicationException e) {
					log.severe("Unable to connect to "+env.get(Context.PROVIDER_URL));
					throw new BackendException("Unable to connect to "+env.get(Context.PROVIDER_URL));
				}
				
				// Search for user element.
				log.config("Connected to "+env.get(Context.PROVIDER_URL));
				ldap.addToEnvironment(Context.SECURITY_PRINCIPAL, "");
				ldap.addToEnvironment(Context.SECURITY_CREDENTIALS, "");
				if (ldapSearch(pattern) != null)
					return true;  // Found a user element.
			}
			return false;  // No user element found.
    	
	    } catch (ConfigurationException e) {
	    	log.severe("ConfigurationException caught and re-thrown as BackendException");
	    	throw new BackendException("ConfigurationException caught and re-thrown as BackendException");
	    } catch (NamingException e) {
	    	log.severe("NamingException caught and re-thrown as BackendException");
	    	throw new BackendException("NamingException caught and re-thrown as BackendException");
	    }
    }
    
    
    /**
     * Retrieves a list of attributes from a user element.
     * @param attributes User element attribute names.
     * @return The requested user attributes.
     * @throws BackendException If a NamingException occurs.
     */
    public HashMap getAttributes(String[] attributes)
    throws BackendException {
        log.finer("getAttributes(String[])");
        
        try {
            // Translate from BasicAttribute to HashMap, using the original
            // attribute names from the request.
            HashMap newAttrs = new HashMap();
            Attributes oldAttrs = ldap.getAttributes(rdn, attributes);
            Attribute oldAttr = null;
            for (int i=0; i<attributes.length; i++) {
                oldAttr = oldAttrs.get(attributes[i]);
                
                // Did we get an attribute back at all?
                if (oldAttr != null) {
                    Vector newValues = new Vector();
                    for (int j=0; j<oldAttr.size(); j++)
                        newValues.add(new String((String)oldAttr.get(j)));
                    newAttrs.put(attributes[i], newValues);
                } else
                    log.fine("Requested attribute "+attributes[i]+" not found");
                
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
