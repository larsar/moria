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
 *
 */

package no.feide.moria.directory.backend;

import java.util.HashMap;

import no.feide.moria.directory.Credentials;
import no.feide.moria.directory.index.IndexedReference;

/**
 *
 */
public interface DirectoryManagerBackend {
    
    
    /**
     * The character set used when encoding attribute values.<br>
     * <br>
     * Current value is <code>"ISO-8859-1"</code>. 
     */
    public static final String ATTRIBUTE_VALUE_CHARSET = "ISO-8859-1";
    
    /**
     * The list of "virtual" attributes, that is, attributes that are generated
     * by Moria itself, and not read from any physical attribute through the
     * backend.<br>
     * <br>
     * Current value is <code>{"tgt"}</code>.
     */
    // TODO: Use the identical constant value from MoriaController instead?
    public static final String[] VIRTUAL_ATTRIBUTES = {"tgt"};

    /**
     * Opens a new backend connection.
     * @param references
     *            The backend references in question. Cannot be
     *            <code>null</code>, and must contain at least one reference.
     */
    void open(IndexedReference[] references);


    /**
     * Checks whether a given user actually exists.
     * @param username
     *            The username to check for.
     * @return <code>true</code> if we can find a user element with the given
     *         username, otherwise <code>false</code>.
     * @throws BackendException
     *             If there was a problem accessing the backend.
     */
    boolean userExists(final String username) throws BackendException;


    /**
     * Attempts to authenticate a user and retrieve a set of user
     * attributes.
     * @param userCredentials
     *            The user's credentials. Cannot be <code>null</code>.
     * @param attributeRequest
     *            A list of requested attributes from the user object. May be
     *            <code>null</code> or an empty array. Not case sensitive.
     * @return The requested user attributes, if any are requested and if they
     *         can be retrieved from the backend following a successful
     *         authentication. Otherwise, an empty <code>HashMap</code>.
     *         Attribute values should be encoded using ISO-8859-1.
     * @throws AuthenticationFailedException
     *             If the authentication fails.
     * @throws BackendException
     *             If there was a problem accessing the backend.
     */
    HashMap authenticate(final Credentials userCredentials, final String[] attributeRequest)
    throws AuthenticationFailedException, BackendException;


    /**
     * Closes the current backend and releases any resources.
     */
    void close();

}
