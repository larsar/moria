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

/**
 * Used to signal a failed authentication attempt.
 */
public class AuthenticationFailedException
extends Exception {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 6223030794569477662L;


    /**
     * Constructor.
     * @param message
     *            The exception message.
     */
    public AuthenticationFailedException(final String message) {

        super(message);

    }

}
