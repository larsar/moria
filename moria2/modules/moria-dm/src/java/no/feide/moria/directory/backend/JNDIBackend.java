/*
 * Copyright (c) 2004 UNINETT FAS This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package no.feide.moria.directory.backend;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Timer;
import java.util.Vector;

import javax.naming.AuthenticationException;
import javax.naming.AuthenticationNotSupportedException;
import javax.naming.ConfigurationException;
import javax.naming.Context;
import javax.naming.NameNotFoundException;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.SizeLimitExceededException;
import javax.naming.TimeLimitExceededException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;
import javax.naming.ldap.InitialLdapContext;

import no.feide.moria.directory.Credentials;
import no.feide.moria.directory.index.IndexedReference;
import no.feide.moria.log.MessageLogger;

import org.apache.commons.codec.binary.Base64;

/**
 * Java Naming and Directory Interface (JNDI) backend. Used to authenticate
 * users and retrieve the associated attributes.
 */
public final class JNDIBackend implements DirectoryManagerBackend {

    /** The message logger. */
    private final MessageLogger log = new MessageLogger(JNDIBackend.class);

    /** The external reference of this backend. */
    private IndexedReference[] myReferences;

    /** The connection timeout used. */
    private final int myTimeout;

    /** Default initial LDAP context environment. */
    private Hashtable<String, String> defaultEnv;

    /** The name of the attribute holding the username. */
    private String usernameAttribute;

    /** The name of the attribute used to guess a user's (R)DN. */
    private String guessedAttribute;

    /** The session ticket used when logging from this instance. */
    private String mySessionTicket = null;

    /**
     * Protected constructor. Creates an initial default context environment and
     * adds support for referrals, a fix for OpenSSL aliases, and enables SSL as
     * default.
     * @param sessionTicket
     *            The session ticket for this instance, used when logging. May
     *            be <code>null</code> (which is treated as an empty string)
     *            or an empty string.
     * @param timeout
     *            The number of seconds before a connection attempt through this
     *            backend times out.
     * @param ssl
     *            <code>true</code> if SSL is to be used, otherwise
     *            <code>false</code>.
     * @param usernameAttributeName
     *            The name of the attribute holding the username. Cannot be
     *            <code>null</code>.
     * @param guessedAttributeName
     *            If we search but cannot find a user element (for example, if
     *            it is not searchable), we will guess that the (R)DN starts
     *            with the substring
     *            <code><i>guessedAttributeName</i>=<i>usernamePrefix</i></code>,
     *            where <code><i>usernamePrefix</i></code> is the part of the
     *            username preceding the 'at' character. Cannot be
     *            <code>null</code>.
     * @throws IllegalArgumentException
     *             If <code>timeout</code> is less than zero.
     * @throws NullPointerException
     *             If <code>guessedAttributeName</code> or
     *             <code>usernameAttribute</code> is <code>null</code>.
     */
    protected JNDIBackend(final String sessionTicket,
                          final int timeout,
                          final boolean ssl,
                          final String usernameAttributeName,
                          final String guessedAttributeName)
    throws IllegalArgumentException, NullPointerException {

        // Assignments, with sanity checks.
        if (usernameAttributeName == null)
            throw new NullPointerException("Username attribute name cannot be NULL");
        usernameAttribute = usernameAttributeName;
        if (guessedAttributeName == null)
            throw new NullPointerException("Guessed attribute name cannot be NULL");
        guessedAttribute = guessedAttributeName;
        if (timeout < 0)
            throw new IllegalArgumentException("Timeout must be greater than zero");
        myTimeout = timeout;
        mySessionTicket = sessionTicket;
        if (mySessionTicket == null)
            mySessionTicket = "";

        // Create initial context environment.
        defaultEnv = new Hashtable<String, String>();
        defaultEnv.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");

        // To catch referrals.
        defaultEnv.put(Context.REFERRAL, "throw");

        // Due to OpenSSL problems.
        defaultEnv.put("java.naming.ldap.derefAliases", "never");

        // Use LDAP v3.
        defaultEnv.put("java.naming.ldap.version", "3");

        // Add timeout value for connection attempts (not searches).
        defaultEnv.put("com.sun.jndi.ldap.connect.timeout", String.valueOf(1000 * timeout));

        // Should we enable SSL?
        if (ssl)
            defaultEnv.put(Context.SECURITY_PROTOCOL, "ssl");

    }

    /**
     * Opens this backend. Does not actually initialize the network connection
     * to the external LDAP.
     * @param references
     *            The external reference to the LDAP server. Cannot be
     *            <code>null</code>, and must contain at least one reference.
     * @throws IllegalArgumentException
     *             If <code>reference</code> is <code>null</code>, or an
     *             empty array.
     */
    public final void open(final IndexedReference[] references) {

        // Sanity check.
        if ((references == null) || (references.length == 0))
            throw new IllegalArgumentException("Reference cannot be NULL or an empty array");

        // Create a local copy of the references.
        ArrayList<IndexedReference> newReferences = new ArrayList<IndexedReference>(references.length);
        for (int i = 0; i < references.length; i++)
            newReferences.add(references[i]);
        myReferences = newReferences.toArray(new IndexedReference[] {});

    }

    /**
     * Checks whether a user element exists, based on its username value.
     * @param username
     *            User name.
     * @return <code>true</code> if the user can be looked up through JNDI,
     *         otherwise <code>false</code>.
     * @throws BackendException
     *             If there is a problem accessing the backend.
     */
    public final boolean userExists(final String username)
    throws BackendException {

        // Sanity checks.
        if ((username == null) || (username.length() == 0))
            return false;

        // The search pattern.
        String pattern = usernameAttribute + '=' + username;

        // Go through all references.
        InitialLdapContext ldap = null;
        for (int i = 0; i < myReferences.length; i++) {
            String[] references = myReferences[i].getReferences();
            final String[] usernames = myReferences[i].getUsernames();
            final String[] passwords = myReferences[i].getPasswords();
            for (int j = 0; j < references.length; j++) {
                
                try {
                    
                    // Context for this reference.
                    try {
                        ldap = connect(references[j]);
                    } catch (NamingException e) {
                        // Connection failed, but we might have other sources.
                        log.logWarn("Unable to access the backend on '" + references[j] + "' to verify existence of '" + username + "': " + e.getClass().getName(), mySessionTicket, e);
                        continue;
                    }
                
                    // Anonymous search or not?
                    ldap.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
                    if ((usernames[j].length() == 0) && (passwords[j].length() > 0))
                        log.logWarn("Search username is empty but search password is not - possible index problem", mySessionTicket);
                    else if ((passwords[j].length() == 0) && (usernames[j].length() > 0))
                        log.logWarn("Search password is empty but search username is not - possible index problem", mySessionTicket);
                    else if ((passwords[j].length() == 0) && (usernames[j].length() == 0)) {
                        log.logDebug("Anonymous search for user element DN on " + references[j], mySessionTicket);
                        ldap.removeFromEnvironment(Context.SECURITY_AUTHENTICATION);
                    } else
                        log.logDebug("Non-anonymous search to verify existence of '" + username + "' on " + references[j], mySessionTicket);
                    ldap.addToEnvironment(Context.SECURITY_PRINCIPAL, usernames[j]);
                    ldap.addToEnvironment(Context.SECURITY_CREDENTIALS, passwords[j]);

                    // Search this reference.
                    if (ldapSearch(ldap, pattern) != null)
                        return true;
                    
                } catch (NamingException e) {
                    
                    // Unable to connect, but we might have other sources.
                    log.logWarn("Unable to access the backend on '" + references[j] + "' to verify existence of '" + username + "': " + e.getClass().getName(), mySessionTicket, e);
                    continue;
                    
                } finally {

                    // Close the LDAP connection.
                    if (ldap != null) {
                        try {
                            ldap.close();
                        } catch (NamingException e) {
                            // Ignored.
                            log.logWarn("Unable to close the backend connection to '" + references[j] + "': " + e.getClass().getName(), mySessionTicket, e);
                        }
                    }
                }

            }
        }

        // Still no match.
        return false;

    }

    /**
     * Authenticates the user using the supplied credentials and retrieves the
     * requested attributes.
     * @param userCredentials
     *            User's credentials. Cannot be <code>null</code>.
     * @param attributeRequest
     *            Requested attributes.
     * @return The requested attributes (<code>String</code> names and
     *         <code>String[]</code> values), if they did exist in the
     *         external backend. Otherwise returns those attributes that could
     *         actually be read, this may be an empty <code>HashMap</code>.
     *         Returns an empty <code>HashMap</code> if
     *         <code>attributeRequest</code> is <code>null</code> or an
     *         empty array.
     * @throws AuthenticationFailedException
     *             If the authentication fails.
     * @throws BackendException
     *             If there is a problem accessing the backend.
     * @throws IllegalArgumentException
     *             If <code>userCredentials</code> is <code>null</code>.
     */
    public final HashMap<String, String[]> authenticate(final Credentials userCredentials,
                                                        final String[] attributeRequest)
    throws AuthenticationFailedException, BackendException {

        // Sanity check.
        if (userCredentials == null)
            throw new IllegalArgumentException("Credentials cannot be NULL");

        // Go through all references.
        for (int i = 0; i < myReferences.length; i++) {
            final String[] references = myReferences[i].getReferences();
            final String[] usernames = myReferences[i].getUsernames();
            final String[] passwords = myReferences[i].getPasswords();
            for (int j = 0; j < references.length; j++) {

                // For the benefit of the finally block below.
                InitialLdapContext ldap = null;

                try {

                    // Context for this reference.
                    try {
                        ldap = connect(references[j]);
                    } catch (NamingException e) {
                        // Connection failed, but we might have other sources.
                        log.logWarn("Unable to access the backend on '" + references[j] + "': " + e.getClass().getName(), mySessionTicket, e);
                        continue;
                    }

                    // Skip search phase if the reference(s) are explicit.
                    String rdn = "";
                    if (myReferences[i].isExplicitlyIndexed()) {

                        // Add the explicit reference; no search phase, no RDN.
                        ldap.addToEnvironment(Context.SECURITY_PRINCIPAL, references[j].substring(references[j].lastIndexOf('/') + 1));

                    } else {

                        // Anonymous search or not?
                        ldap.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
                        if ((usernames[j].length() == 0) && (passwords[j].length() > 0))
                            log.logWarn("Search username is empty but search password is not - possible index problem", mySessionTicket);
                        else if ((passwords[j].length() == 0) && (usernames[j].length() > 0))
                            log.logWarn("Search password is empty but search username is not - possible index problem", mySessionTicket);
                        else if ((passwords[j].length() == 0) && (usernames[j].length() == 0)) {
                            log.logDebug("Anonymous search for user element DN on " + references[j], mySessionTicket);
                            ldap.removeFromEnvironment(Context.SECURITY_AUTHENTICATION);
                        } else
                            log.logDebug("Non-anonymous search for user element DN on " + references[j], mySessionTicket);
                        ldap.addToEnvironment(Context.SECURITY_PRINCIPAL, usernames[j]);
                        ldap.addToEnvironment(Context.SECURITY_CREDENTIALS, passwords[j]);

                        // Search using the implicit reference.
                        String pattern = usernameAttribute + '=' + userCredentials.getUsername();
                        rdn = ldapSearch(ldap, pattern);
                        if (rdn == null) {

                            // No user element found. Try to guess the RDN.
                            rdn = userCredentials.getUsername();
                            rdn = guessedAttribute + '=' + rdn.substring(0, rdn.indexOf('@'));
                            log.logDebug("No subtree match for " + pattern + " on " + references[j] + " - guessing on RDN " + rdn, mySessionTicket);

                        } else
                            log.logDebug("Matched " + pattern + " to " + rdn + ',' + ldap.getNameInNamespace(), mySessionTicket);
                        ldap.addToEnvironment(Context.SECURITY_PRINCIPAL, rdn + ',' + ldap.getNameInNamespace());
                    }

                    // Authenticate and get attributes.
                    ldap.addToEnvironment(Context.SECURITY_AUTHENTICATION, "simple");
                    ldap.addToEnvironment(Context.SECURITY_CREDENTIALS, userCredentials.getPassword());
                    try {
                        ldap.reconnect(null);
                        log.logDebug("Successfully authenticated " + userCredentials.getUsername() + " on " + references[j], mySessionTicket);
                        return getAttributes(ldap, rdn, attributeRequest); // Success.
                    } catch (AuthenticationException e) {

                        // Authentication failed, but we may have other
                        // references.
                        log.logDebug("Failed to authenticate user " + userCredentials.getUsername() + " on " + references[j] + " - authentication failed", mySessionTicket);
                        continue;

                    } catch (AuthenticationNotSupportedException e) {

                        // Password authentication not supported for the DN.
                        // We may still have other references.
                        log.logDebug("Failed to authenticate user " + userCredentials.getUsername() + " on " + references[j] + " - authentication not supported", mySessionTicket);
                        continue;

                    }

                } catch (ConfigurationException e) {
                    throw new BackendException("Backend configuration problem with " + references[j], e);
                } catch (NamingException e) {
                    throw new BackendException("Unable to access the backend on " + references[j], e);
                } finally {

                    // Close the LDAP connection.
                    if (ldap != null) {
                        try {
                            ldap.close();
                        } catch (NamingException e) {
                            // Ignored.
                            log.logWarn("Unable to close the backend connection to " + references[j] + " - ignoring", mySessionTicket, e);
                        }
                    }
                }

            }
        }

        // No user was found.
        throw new AuthenticationFailedException("Failed to authenticate user " + userCredentials.getUsername() + " - no user found");

    }

    /**
     * Retrieves a list of attributes from an element.
     * @param ldap
     *            A prepared LDAP context. Cannot be <code>null</code>.
     * @param rdn
     *            The relative DN (to the DN in the LDAP context
     *            <code>ldap</code>). Cannot be <code>null</code>.
     * @param attributes
     *            The requested attribute's names. Also indirectly referenced
     *            attributes on the form
     *            <code>someReferenceAttribute:someIndirectAttribute</code>,
     *            where the DN in the reference attribute
     *            <code>someReferenceAttribute</code> is followed to look up
     *            <code>someIndirectAttribute</code> from another element.
     * @return The requested attributes (<code>String</code> names and
     *         <code>String[]</code> values), if they did exist in the
     *         external backend. Otherwise returns those attributes that could
     *         actually be read, this may be an empty <code>HashMap</code>.
     *         Returns an empty <code>HashMap</code> if
     *         <code>attributes</code> is <code>null</code> or an empty
     *         array. Note that attribute values are mapped to
     *         <code>String</code> using ISO-8859-1.
     * @throws BackendException
     *             If unable to read the attributes from the backend.
     * @throws NullPointerException
     *             If <code>ldap</code> or <code>rdn</code> is
     *             <code>null</code>.
     * @see javax.naming.directory.InitialDirContext#getAttributes(java.lang.String,
     *      java.lang.String[])
     */
    private HashMap<String, String[]> getAttributes(final InitialLdapContext ldap,
                                                    final String rdn,
                                                    final String[] attributes)
    throws BackendException {

        // Sanity checks.
        if (ldap == null)
            throw new NullPointerException("LDAP context cannot be NULL");
        if (rdn == null)
            throw new NullPointerException("RDN cannot be NULL");
        if ((attributes == null) || (attributes.length == 0))
            return new HashMap<String, String[]>();
            
        // Used to remember attributes to be read through references later on.
        Hashtable<String, Vector> attributeReferences = new Hashtable<String, Vector>(); 

        // Strip down request, resolving references and removing duplicates.
        Vector<String> strippedAttributeRequest = new Vector<String>();
        for (int i = 0; i < attributes.length; i++) {
            int indexOfSplitCharacter = attributes[i].indexOf(DirectoryManagerBackend.ATTRIBUTE_REFERENCE_SEPARATOR);
            if (indexOfSplitCharacter == -1) {

                // A regular attribute request.
                if (!strippedAttributeRequest.contains(attributes[i]))
                    strippedAttributeRequest.add(attributes[i]);

            } else {

                // A referenced attribute request.
                final String referencingAttribute = attributes[i].substring(0, indexOfSplitCharacter);
                if (!strippedAttributeRequest.contains(referencingAttribute))
                    strippedAttributeRequest.add(referencingAttribute);
                
                // Add to list of attributes to be read through each reference.
                if (!attributeReferences.containsKey(referencingAttribute)) {
                    
                    // Add new reference.
                    Vector<String> referencedAttribute = new Vector<String>();
                    referencedAttribute.add(attributes[i].substring(indexOfSplitCharacter + 1));
                    attributeReferences.put(referencingAttribute, referencedAttribute);
                    
                } else {
                    
                    // Update existing reference.
                    Vector<String> referencedAttribute = attributeReferences.get(referencingAttribute);
                    if (!referencedAttribute.contains(attributes[i].substring(indexOfSplitCharacter + 1)))
                        referencedAttribute.add(attributes[i].substring(indexOfSplitCharacter + 1));
                    
                }

            }

        }

        // The context provider URL and DN, for later logging.
        String url = "unknown backend";
        String dn = "unknown dn";

        // Get the attributes from an already initialized LDAP connection.
        Attributes rawAttributes = null;
        try {

            // Remember the URL and bind DN, for later logging.
            final Hashtable environment = ldap.getEnvironment();
            url = (String) environment.get(Context.PROVIDER_URL);
            dn = (String) environment.get(Context.SECURITY_PRINCIPAL);

            // Get the attributes.
            rawAttributes = ldap.getAttributes(rdn, strippedAttributeRequest.toArray(new String[] {}));

        } catch (NameNotFoundException e) {

            // Successful authentication but missing user element; no attributes
            // returned and the event is logged.
            log.logWarn("No LDAP element found (DN was '" + dn + "')", mySessionTicket);
            rawAttributes = new BasicAttributes();

        } catch (NamingException e) {
            String a = new String();
            for (int i = 0; i < attributes.length; i++)
                a = a + attributes[i] + ", ";
            throw new BackendException("Unable to read attribute(s) '" + a.substring(0, a.length() - 2) + "' from '" + rdn + "' on '" + url + "'", e);
        }

        // Translate retrieved attributes from Attributes to HashMap.
        HashMap<String, String[]> convertedAttributes = new HashMap<String, String[]>();
        for (int i = 0; i < attributes.length; i++) {

            // Did we get any attribute back at all?
            final String requestedAttribute = attributes[i];
            Attribute rawAttribute = rawAttributes.get(requestedAttribute);
            if (rawAttribute == null) {

                // Attribute was not returned.
                log.logDebug("Requested attribute '" + requestedAttribute + "' not found on '" + url + "'", mySessionTicket);

            } else {

                // Map the attribute values to String[].
                ArrayList<String> convertedAttributeValues = new ArrayList<String>(rawAttribute.size());
                for (int j = 0; j < rawAttribute.size(); j++) {
                    try {

                        // We either have a String or a byte[].
                        String convertedAttributeValue = null;
                        try {

                            // Encode String.
                            convertedAttributeValue = new String(((String) rawAttribute.get(j)).getBytes(), DirectoryManagerBackend.ATTRIBUTE_VALUE_CHARSET);
                        } catch (ClassCastException e) {

                            // Encode byte[] to String.
                            convertedAttributeValue = new String(Base64.encodeBase64((byte[]) rawAttribute.get(j)), DirectoryManagerBackend.ATTRIBUTE_VALUE_CHARSET);

                        }
                        convertedAttributeValues.add(convertedAttributeValue);

                    } catch (NamingException e) {
                        throw new BackendException("Unable to read attribute value of '" + rawAttribute.getID() + "' from '" + url + "'", e);
                    } catch (UnsupportedEncodingException e) {
                        throw new BackendException("Unable to use " + DirectoryManagerBackend.ATTRIBUTE_VALUE_CHARSET + " encoding", e);
                    }
                }
                convertedAttributes.put(requestedAttribute, convertedAttributeValues.toArray(new String[] {}));

            }

        }
        
        // Follow references to look up any indirectly referenced attributes.
        Enumeration<String> keys = attributeReferences.keys();
        while (keys.hasMoreElements()) {
            
            // Do we have a reference? 
            final String referencingAttribute = keys.nextElement();
            final String[] referencingValues = convertedAttributes.get(referencingAttribute);
            if (referencingValues == null) {
                
                // No reference was found in this attribute.
                log.logDebug("Found no DN references in attribute '" + referencingAttribute + "'", mySessionTicket);
                
            } else {
                
                // One (or more) references was found in this attribute.
                if (referencingValues.length > 1)
                    log.logDebug("Found " + referencingValues.length + " DN references in attribute '" + referencingAttribute + "'; ignoring all but first", mySessionTicket);
                log.logDebug("Following reference '" + referencingValues[0] + "' found in '"  + referencingAttribute + "' to look up attribute(s) '" + attributeReferences.get(referencingAttribute).toString(), mySessionTicket);
                String providerURL = null;  // To be used later.
                try {
                    
                    // Follow the reference.
                    providerURL = (String) ldap.getEnvironment().get(Context.PROVIDER_URL);
                    providerURL = providerURL.substring(0, providerURL.lastIndexOf("/") + 1) + referencingValues[0];
                    ldap.addToEnvironment(Context.PROVIDER_URL, providerURL);
                    
                } catch (NamingException e) {
                    throw new BackendException("Unable to update provider URL in LDAP environment", e);
                }
                
                // Add any referenced attributes returned.
                HashMap additionalAttributes = getAttributes(ldap, providerURL, (String[]) attributeReferences.get(referencingAttribute).toArray(new String[] {}));
                Iterator i = additionalAttributes.keySet().iterator();
                while (i.hasNext()) {
                    String attributeName = (String) i.next();
                    convertedAttributes.put(referencingAttribute + DirectoryManagerBackend.ATTRIBUTE_REFERENCE_SEPARATOR + attributeName, (String[]) additionalAttributes.get(attributeName));
                }
                    
            }
            
        }

        return convertedAttributes;

    }

    /**
     * Does nothing, but needed to fulfill the
     * <code>DirectoryManagerBackend</code> interface. Actual backend
     * connections are closed after each use.
     * @see DirectoryManagerBackend#close()
     */
    public void close() {

        // Does nothing.

    }

    /**
     * Does a subtree search for an element given a pattern. Only the first
     * element found is considered, and all references are searched in order
     * until either a match is found or no more references are left to search.
     * @param ldap
     *            A prepared LDAP context.
     * @param pattern
     *            The search pattern. Must not include the character '*' or the
     *            substring '\2a' to prevent possible LDAP exploits.
     * @return The element's relative DN, or <code>null</code> if none was
     *         found. <code>null</code> is also returned if the search pattern
     *         contains an illegal character or substring.
     * @throws BackendException
     *             If there was a problem accessing the backend. Typical causes
     *             include timeouts.
     */
    private String ldapSearch(final InitialLdapContext ldap,
                              final String pattern) throws BackendException {

        // Check pattern for illegal content.
        String[] illegals = { "*", "\\2a" };
        for (int i = 0; i < illegals.length; i++) {
            if (pattern.indexOf(illegals[i]) > -1)
                return null;
        }

        // The context provider URL, for later logging.
        String url = "unknown backend";

        // Start counting the (milli)seconds and prepare for timeouts.
        long searchStart = System.currentTimeMillis();
        JNDISearchInterruptor interruptTask = new JNDISearchInterruptor(ldap, mySessionTicket);
        NamingEnumeration results;
        try {

            // Remember the URL, for later logging.
            url = (String) ldap.getEnvironment().get(Context.PROVIDER_URL);
            interruptTask.setURL(url);

            // Start timeout interruptor and perform the search.
            Timer interruptTimer = new Timer();
            interruptTimer.schedule(interruptTask, (1000 * myTimeout));
            results = ldap.search("", pattern, new SearchControls(SearchControls.SUBTREE_SCOPE, 0, 1000 * myTimeout, new String[] {}, false, false));
            interruptTimer.cancel();
            if (!results.hasMore())
                return null;

        } catch (TimeLimitExceededException e) {

            // The search timed out.
            log.logWarn("Search on " + url + " for " + pattern + " timed out after ~" + (System.currentTimeMillis() - searchStart) + "ms", mySessionTicket);
            return null;

        } catch (SizeLimitExceededException e) {

            // The search returned too many results.
            log.logWarn("Search on " + url + " for " + pattern + " returned too many results", mySessionTicket);
            return null;

        } catch (NameNotFoundException e) {

            // Element not found. Possibly non-existing reference.
            log.logDebug("Could not find " + pattern + " on " + url, mySessionTicket); // Necessary?
            return null;

        } catch (AuthenticationException e) {

            // Search failed authentication; check non-anonymous search config.
            try {
                final String searchUser = (String) ldap.getEnvironment().get(Context.SECURITY_PRINCIPAL);
                final String errorMessage;
                if ((searchUser == null) || searchUser.equals(""))
                    errorMessage = "Anonymous search failed authentication on " + url;
                else
                    errorMessage = "Could not authenticate search user " + searchUser + " on " + url;
                log.logDebug(errorMessage, mySessionTicket);
                throw new BackendException(errorMessage, e);
            } catch (NamingException f) {

                // Should not happen!
                log.logCritical("Unable to read LDAP environment", mySessionTicket, f);
                throw new BackendException("Unable to read LDAP environment", f);

            }

        } catch (NamingException e) {

            // Did we interrupt the search ourselves?
            if (interruptTask.finished()) {
                final long elapsed = System.currentTimeMillis() - searchStart;
                log.logWarn("Search on " + url + " for " + pattern + " timed out after ~" + elapsed + "ms", mySessionTicket);
                throw new BackendException("Search on " + url + " for " + pattern + " timed out after ~" + elapsed + "ms; connection terminated");
            }

            // All other exceptions.
            log.logWarn("Search on " + url + " for " + pattern + " failed", mySessionTicket, e);
            return null;

        }

        // We just found at least one element. Did we get an ambigious result?
        SearchResult entry = null;
        try {
            entry = (SearchResult) results.next();
            String buffer = new String();
            while (results.hasMoreElements())
                buffer = buffer + ", " + ((SearchResult) results.next()).getName();
            if (!buffer.equals(""))
                log.logWarn("Search on " + url + " for " + pattern + " gave ambiguous result: [" + entry.getName() + buffer + "]", mySessionTicket);
            // TODO: Throw BackendException, or a subclass, or just (as now)
            // pick the first and hope for the best?
            buffer = null;
        } catch (NamingException e) {
            throw new BackendException("Unable to read search results", e);
        }
        return entry.getName(); // Relative DN (to the reference).

    }

    /**
     * Creates a new connection to a given backend provider URL.
     * @param url
     *            The backend provider URL.
     * @return The opened backend connection.
     * @throws NamingException
     *             If unable to connect to the provider given by
     *             <code>url</code>.
     */
    private InitialLdapContext connect(final String url) throws NamingException {

        // Prepare connection.
        Hashtable<String, String> env = new Hashtable<String, String>(defaultEnv);
        env.put(Context.PROVIDER_URL, url);
        return new InitialLdapContext(env, null);

    }
}
