/**
 * Copyright (C) 2003 FEIDE This program is free software; you can redistribute
 * it and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Public License for more details. You should have received a
 * copy of the GNU General Public License along with this program; if not, write
 * to the Free Software Foundation, Inc., 59 Temple Place - Suite 330, Boston,
 * MA 02111-1307, USA.
 */

package no.feide.moria.directory.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.TimeLimitExceededException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import no.feide.moria.directory.Credentials;
import no.feide.moria.directory.index.IndexedReference;
import no.feide.moria.log.MessageLogger;

/**
 * Represents a user in the backend. Used to authenticate users and retrieve the
 * associated attributes.
 */
public class JNDIBackend
implements DirectoryManagerBackend {

    /** The message logger. */
    private final MessageLogger log = new MessageLogger(JNDIBackend.class);

    /** The external reference of this backend. */
    private IndexedReference myReference;

    /** The connection timeout used. */
    private final int myTimeout;

    /** Default initial LDAP context environment. */
    private Hashtable defaultEnv;

    /** The LDAP context. */
    private InitialLdapContext ldap;

    /** Used to store the user element's relative DN. */
    private String rdn;


    /**
     * Protected constructor. Will create an initial default context environment
     * and add support for referrals, a fix for OpenSSL aliases, and enable SSL
     * as default.
     * @param timeout
     *            The number of seconds before a connection attempt through this
     *            backend times out.
     * @throws IllegalArgumentException
     *             If <code>config</code> is <code>null</code>.
     */
    protected JNDIBackend(int timeout) throws IllegalArgumentException {

        // Sanity check.
        if (timeout < 0)
            throw new IllegalArgumentException("Timeout must be greater than zero");
        myTimeout = timeout;

        // Create initial context environment.
        defaultEnv = new Hashtable();
        defaultEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");

        // To catch referrals.
        defaultEnv.put(Context.REFERRAL, "throw");

        // Due to OpenSSL problems.
        defaultEnv.put("java.naming.ldap.derefAliases", "never");

        // SSL enabled by default.
        defaultEnv.put(Context.SECURITY_PROTOCOL, "ssl");

    }


    /**
     * Open this backend. Does not actually initialize the network connection to
     * the external LDAP.
     * @param reference
     *            The external reference to the LDAP server. Cannot be
     *            <code>null</code>.
     * @throws IllegalArgumentException
     *             If <code>reference</code> is <code>null</code>.
     */
    public void open(IndexedReference reference) {

        // Sanity check.
        if (reference == null)
            throw new IllegalArgumentException("Reference cannot be NULL");

        myReference = new IndexedReference(reference.getReferences(), reference.isExplicitlyIndexed());

    }


    /**
     * Check whether a user element exists, based on its username value.
     * @param username
     * @return <code>true</code> if the user can be looked up through JNDI,
     *         otherwise <code>false</code>.
     */
    public boolean userExists(final String username) {

        // TODO: Implement.
        return false;

    }


    /**
     * Authenticate the user using the supplied credentials and retrieve the
     * requested attributes.
     * @param userCredentials
     *            User's credentials. Cannot be <code>null</code>.
     * @param attributeRequest
     * @return <code>false</code> if authentication was unsuccessful (bad or
     *         <code>null</code> username/password), otherwise
     *         <code>true</code>.
     * @throws BackendException
     *             If a NamingException is thrown, if the type of credentials is
     *             not supported, or if a <code>ConfigurationException</code>
     *             is caught.
     * @throws IllegalArgumentException
     *             If <code>userCredentials</code> is <code>null</code>.
     */
    public HashMap authenticate(Credentials userCredentials, String[] attributeRequest)
    throws AuthenticationFailedException {

        // Sanity check.
        if (userCredentials == null)
            throw new IllegalArgumentException("Credentials cannot be NULL");

        // Validate credentials.
        String username = userCredentials.getUsername();
        if ((username == null) || (username.length() == 0))
            throw new AuthenticationFailedException("Username cannot be NULL or an empty string");
        String password = userCredentials.getPassword();
        if ((password == null) || (password.length() == 0))
            throw new AuthenticationFailedException("Password cannot be NULL or an empty string");

        // TODO: Implement.

        return null;

    }


    /**
     * Retrieves a list of attributes from an element.
     * @param attributes
     *            The requested attribute's names.
     * @return The requested attributes (<code>String</code> names and
     *         <code>String[]</code> values), if they did exist in the
     *         external backend. Otherwise will return an incomplete list of
     *         those attributes that could actually be read; this may be an
     *         empty <code>HashMap</code>. Will also return an empty
     *         <code>HashMap</code> if <code>attributes</code> is
     *         <code>null</code> or an empty array.
     * @throws BackendException
     *             If unable to read the attributes from the backend using
     *             <code>InitialDirContext.getAttributes(String, String[])</code>.
     * @see javax.naming.directory.InitialDirContext#getAttributes(java.lang.String,
     *      java.lang.String[])
     */
    private HashMap getAttributes(String[] attributes) throws BackendException {

        // Sanity check.
        if ((attributes == null) || (attributes.length == 0))
            return new HashMap();

        // Get the attributes from an already initialized LDAP connection.
        Attributes oldAttrs = null;
        try {
            oldAttrs = ldap.getAttributes(rdn, attributes);
        } catch (NamingException e) {
            throw new BackendException("Unable to read attributes", e);
        }

        // Translate retrieved attributes from Attributes to HashMap.
        HashMap newAttrs = new HashMap();
        for (int i = 0; i < attributes.length; i++) {

            // Did we get an attribute back at all?
            Attribute oldAttr = oldAttrs.get(attributes[i]);
            if (oldAttr == null)
                log.logWarn("Requested attribute " + attributes[i] + " not found");
            else {

                // Map the attribute values to String[].
                ArrayList newValues = new ArrayList(oldAttr.size());
                for (int j = 0; j < oldAttr.size(); j++)
                    try {
                        newValues.add(new String((String) oldAttr.get(j)));
                    } catch (NamingException e) {
                        throw new BackendException("Unable to read attribute value of " + oldAttr.getID(), e);
                    }
                newAttrs.put(attributes[i], (String[]) newValues.toArray(new String[] {}));

            }

        }
        return newAttrs;

    }


    /**
     * Close the LDAP connection of this backend. Will log a warning message if
     * unable to close the connection.
     */
    public void close() {

        try {
            ldap.close();
        } catch (NamingException e) {

            // Not being able to close the connection is a non-critical error.
            log.logWarn("Unable to close backend connection to " + myReference);

        }

    }


    /**
     * Do a subtree search for an element given a pattern. Only the first
     * element found is considered. Implemented as a separate method due to
     * recursive referral support (temporarily disabled). <em>Note:</em> The
     * default timeout when searching is 15 seconds, unless
     * <code>no.feide.moria.backend.ldap.timeout</code> is set.
     * @param pattern
     *            The search pattern. Must not include the character '*' or the
     *            substring '\2a' due to possible LDAP exploits.
     * @return The element's relative DN, or <code>null</code> if none was
     *         found. <code>null</code> is also returned if the search pattern
     *         contains an illegal character or substring.
     * @throws BackendException
     *             If there was a problem accessing the backend. Typical causes
     *             include timeouts.
     */
    private String ldapSearch(final String pattern) throws BackendException {

        // Check pattern for illegal content.
        String[] illegals = {"*", "\2a"};
        for (int i = 0; i < illegals.length; i++)
            if (pattern.indexOf(illegals[i]) > -1)
                return null;

        NamingEnumeration results;

        // Start counting the (milli)seconds.
        long searchStart = System.currentTimeMillis();
        try {

            results = ldap.search("", pattern, new SearchControls(SearchControls.SUBTREE_SCOPE, 1, 1000 * myTimeout, new String[] {}, false, false));
            if (!results.hasMore()) {
                log.logWarn("No match for " + pattern + " on " + ldap.getEnvironment().get(Context.PROVIDER_URL));
                return null;
            }

            // We just found an element.
            SearchResult entry = null;
            try {
                entry = (SearchResult) results.next();
            } catch (NamingException e) {
                throw new BackendException("Unexpected NamingException caught", e);
            }
            String rdn = entry.getName();
            log.logWarn("Matched " + pattern + " on " + ldap.getEnvironment().get(Context.PROVIDER_URL) + " to element " + rdn);
            return rdn;

        } catch (TimeLimitExceededException e) {
            throw new BackendException("Connection timed out after " + (System.currentTimeMillis() - searchStart) + "ms", e);
        } catch (NamingException e) {
            throw new BackendException("Unexpected NamingException caught", e);
        }

    }

}