/*
 * Copyright (c) 2004 UNINETT FAS
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package no.feide.moria.directory.backend;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import no.feide.moria.directory.Credentials;
import no.feide.moria.directory.DirectoryManagerConfigurationException;
import no.feide.moria.directory.index.IndexedReference;

import org.jdom.Element;

/**
 * Hard-coded dummy backend, for testing. Does not require an actual backend
 * source.
 */
public class DummyBackend
implements DirectoryManagerBackend {

    /**
     * Maps user names (converted to lowercase) to <code>DummyUser</code>
     * elements.
     */
    private HashMap users;


    /**
     * Protected constructor. Sets the configuration used by this instance.
     * @param config
     *            A <code>Backend</code> configuration element. Must contain a
     *            <code>Dummy</code> element (if more than one only the first
     *            is considered), which is expected to contain one or more
     *            <code>User</code> elements, each with one or more
     *            <code>Attribute</code> elements, which again have
     *            <code>Value</code> elements with exactly one value child.
     *            Allows for easy configuration of test cases, without having to
     *            rely on an external backend source. See the supplied dummy
     *            configuration for a workable example. Note that attribute and
     *            user names are case insensitive, while attribute values are
     *            stored as specified in the configuration.
     * @throws DirectoryManagerConfigurationException
     *            If config lacks a mandatory element.
     */
    protected DummyBackend(final Element config) {

        // Get Dummy element, with sanity check.
        final Element dummy = config.getChild("Dummy");
        if (dummy == null)
            throw new DirectoryManagerConfigurationException("Missing Dummy element");

        // Parse any user elements.
        users = new HashMap();
        if (dummy.getChildren("User") == null)
            throw new DirectoryManagerConfigurationException("Missing User element(s)");
        final Iterator userElements = dummy.getChildren("User").iterator();
        while (userElements.hasNext()) {

            // Parse any attribute elements.
            HashMap attributes = new HashMap();
            final Element user = (Element) userElements.next();
            final Iterator attributeElements = user.getChildren("Attribute").iterator();
            while (attributeElements.hasNext()) {

                // Parse any value elements.
                ArrayList values = new ArrayList();
                final Element attribute = (Element) attributeElements.next();
                final Iterator valueElements = attribute.getChildren("Value").iterator();
                while (valueElements.hasNext()) {

                    // Parse the attribute values.
                    final Element value = (Element) valueElements.next();
                    values.add(value.getText());

                }

                // Map an attribute to its values.
                attributes.put(attribute.getAttributeValue("name").toLowerCase(), values);

            }

            // Add a new user.
            DummyUser newUser = new DummyUser(user.getAttributeValue("name"), user.getAttributeValue("password"), attributes);
            users.put(user.getAttributeValue("name").toLowerCase(), newUser);

        }

    }


    /**
     * Does nothing, but needed to fulfill the
     * <code>DirectoryManagerBackend</code> interface.
     * @param references
     *            Ignored.
     * @see DirectoryManagerBackend#open(IndexedReference[])
     */
    public void open(final IndexedReference[] references) {

        // Does nothing.

    }


    /**
     * Checks whether a user exists.
     * @param username
     *            The username. Case is ignored.
     * @see DirectoryManagerBackend#userExists(String)
     */
    public boolean userExists(final String username) {

        if (username == null)
            return false;
        return users.containsKey(username.toLowerCase());

    }


    /**
     * Authenticates a user, if the user exists and the username equals the
     * password.
     * @param userCredentials
     *            The user's credentials. Cannot be <code>null</code>.
     * @param attributeRequest
     *            A list of requested attributes from the user object. May be
     *            <code>null</code> or an empty array. Not case sensitive.
     * @throws AuthenticationFailedException
     *             If the authentication fails.
     * @throws IllegalArgumentException
     *             If userCredentials are <code>null</code>.
     * @see DirectoryManagerBackend#authenticate(Credentials, String[])
     */
    public HashMap authenticate(final Credentials userCredentials, final String[] attributeRequest)
    throws AuthenticationFailedException {

        // Sanity check.
        if (userCredentials == null)
            throw new IllegalArgumentException("Credentials cannot be NULL");

        // Find and authenticate user.
        DummyUser user = (DummyUser) users.get(userCredentials.getUsername());
        if ((user != null) && (user.authenticate(userCredentials.getUsername(), userCredentials.getPassword()))) {

            // Successful authentication; return any requested user attributes.
            return user.getAttributes(attributeRequest);

        } else {

            // Bad authentication.
            throw new AuthenticationFailedException("User \"" + userCredentials.getUsername() + "\" failed authentication");

        }

    }


    /**
     * Does nothing, but needed to fulfill the
     * <code>DirectoryManagerBackend</code> interface.
     * @see DirectoryManagerBackend#close()
     */
    public void close() {

        // Does nothing.

    }

}
