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

package no.feide.moria.directory;

/**
 * Represents a set of user credentials, that is, a username/password pair. Used
 * for Moria authentication methods, and may be expanded to support other types
 * of credentials in a future version <br>
 * <br>
 * Note that this is a subset of the functionality offered by the Moria 1
 * <code>Credentials</code> class.
 */
public class Credentials {

    /** Internal representation of the username. */
    private final String username;

    /** Internal representation of the user's password. */
    private final String password;


    /**
     * Constructor. Creates a new set of credentials consisting of a
     * username/password pair.
     * @param username
     *            The username. May not be <code>null</code> or an empty
     *            string.
     * @param password
     *            The user's password. May not be <code>null</code> or an
     *            empty string.
     * @throws IllegalArgumentException
     *             If <code>username</code> or <code>password</code>
     *             is null or an empty string.
     */
    public Credentials(final String username, final String password) {

        // Sanity checks.
        if (username == null || username.length() == 0)
            throw new IllegalArgumentException("User name must be a non-empty string.");
        if (password == null || password.length() == 0)
            throw new IllegalArgumentException("Password must be a non-empty string.");

        this.username = username;
        this.password = password;

    }


    /**
     * Retrieves the username part of the credentials.
     * @return A newly allocated <code>String</code> containing the username.
     */
    public String getUsername() {

        return new String(username);

    }


    /**
     * Retrieves the password part of the credentials.
     * @return A newly allocated <code>String</code> containing the password.
     */
    public String getPassword() {

        return new String(password);

    }

}
