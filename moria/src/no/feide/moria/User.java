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

import java.net.ConnectException;
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
    
    /** Used to show whether the system-wide JNDI init has been done. */
    private static boolean initialized = false;
    
    /** The set of initial backend URLs. */
    private Vector initialURLs = new Vector();
    
    // The index of the currently used initial URL. All access should be
    // synchronized!
    private static Integer initialURLIndex = new Integer(0);
    
    /**
     * The number of referrals we've followed. Used to switch to a secondary
     * index server if needed.
     */
    private int referrals;
    
    
    /**
     * Constructor. Initializes the list of initial index server URLs.
     * @throws ConfigurationException If unable to read from configuration.
     **/
    private User()
    throws ConfigurationException {
        log.finer("User()");
        
        // Populate list of initial URLs.
        String url;
        for (int i=1; ; i++) {
            url = Configuration.getProperty("no.feide.moria.backend.ldap.url"+i);
            if (url == null)
                break; // No more URLs.
            initialURLs.add(url);
        }
    }
    
    /**
     * Factory method.
     * @return A new instance of <code>User</code>.
     * @throws BackendException If a new instance couldn't be created, or
     *                          if a <code>ConfigurationException</code> is
     *                          caught.
     */
    public static User getInstance() 
    throws BackendException {
        log.finer("getInstance()");
        
        try {
            if (!initialized)
                init();
            return new User();
        } catch (ConfigurationException e) {
            log.severe("ConfigurationException caught and re-thrown as BackendException");
            throw new BackendException("ConfigurationException caught", e);
        }
    }
    
    
    /**
     * Used to do one-time global JNDI initialization, the very first time
     * <code>authenticate(Credentials)</code> is called.
     * @throws ConfigurationException If one of the required properties
     *                                cannot be resolved.
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
    public boolean authenticate(Credentials c)
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

        /**
         * Do one-time JNDI initialization and some other configuration -
         * so we don't need to catch ConfigurationException later.
         */
        String pattern = null;
        try {
            if (!initialized)
                init();   
            pattern = Configuration.getProperty("no.feide.moria.backend.ldap.usernameAttribute")+'='+username;
        } catch (ConfigurationException e) {
            log.severe("ConfigurationException caught and re-thrown as BackendException");
            throw new BackendException("ConfigurationException caught", e);
        }
        
        try {
            
            // Try all initial (index) servers, if necessary.
            Hashtable env = new Hashtable();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.REFERRAL, "throw");  // To catch referrals.
            int failures = 0;
            synchronized (initialURLIndex) {
                do {
                    int index = initialURLIndex.intValue();
                    env.put(Context.PROVIDER_URL, (String)initialURLs.get(index));
                    try {
                        ldap = new InitialLdapContext(env, null);
                    } catch (CommunicationException e) {
                        if (e.getRootCause() instanceof ConnectException)

                            // Switch to another initial URL, if available.
                            failures++;
                            if (failures == initialURLs.size()) {
                                log.severe("Unable to connect to any initial (index) server; last URL was "+env.get(Context.PROVIDER_URL));
                                throw new BackendException("Unable to connect to any initial (index) server; last URL was "+env.get(Context.PROVIDER_URL));
                            }
                            index++;
                            if (index == initialURLs.size())
                                initialURLIndex = new Integer(0);
                            else
                                initialURLIndex = new Integer(index);
                            log.config("Unable to connect to "+env.get(Context.PROVIDER_URL)+", switching to "+(String)initialURLs.get(initialURLIndex.intValue()));

                        }
                } while (ldap == null);
            }

            // Search for user element.
	    log.config("Connected to "+env.get(Context.PROVIDER_URL));
            ldap.addToEnvironment(Context.SECURITY_PRINCIPAL, "");
            ldap.addToEnvironment(Context.SECURITY_CREDENTIALS, "");
            rdn = ldapSearch(pattern);
            if (rdn == null) {
                // No user element found.
                log.fine("No subtree match for "+pattern+" on "+ldap.getEnvironment().get(Context.PROVIDER_URL));
                return false;
            }
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
                return false;  // Failure.
            }
            
        } catch (NamingException e) {
            log.severe("NamingException caught and re-thrown as BackendException");
            throw new BackendException(e);
        }
    }
    
    
    /**
     * Do a subtree search for an element given a pattern. Only the first
     * element found is considered. Any referrals are followed recursively.
     * <em>Note:</em> The default timeout when searching is 10 seconds,
     *                unless
     *                <code>no.feide.moria.backend.ldap.timeout</code> is
     *                set.
     * @param pattern The search pattern.
     * @return The element's relative DN, or <code>null</code> if none was
     *         found.
     * @throws BackendException If a <code>NamingException occurs</code>, or
     *                          if a <code>ConfigurationException</code> is
     *                          caught.
     */
    private String ldapSearch(String pattern)
    throws BackendException {
        log.finer("ldapSearch(String)");
        
        try {
            
            NamingEnumeration results;
            try {
                // Search and destroy.
                try {
                    results = ldap.search("", pattern, new SearchControls(SearchControls.SUBTREE_SCOPE, 1, Integer.parseInt(Configuration.getProperty("no.feide.moria.backend.ldap.timeout", "15")), new String[] {}, false, true));
                } catch (ConfigurationException e) {
                    log.severe("ConfigurationException caught and re-thrown as BackendException");
                    throw new BackendException(e);
                }
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
                log.info("Matched "+pattern+" to referral "+refEnv.get(Context.PROVIDER_URL));
                ldap.close();
                ldap = new InitialLdapContext(refEnv, null);
                return ldapSearch(pattern);
                
            }
            
            // We just found an element.
            SearchResult entry = (SearchResult)results.next();
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
