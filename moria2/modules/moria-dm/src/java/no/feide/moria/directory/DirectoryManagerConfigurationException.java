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

package no.feide.moria.directory;

/**
 * Used to signal an exception related to the Directory Manager's configuration.
 * <br>
 * <br>
 * Note that this exception is thrown whenever an unrecoverable internal error
 * is encountered, as this will invariably be related to faulty configuration of
 * the Directory Manager. Recoverable internal errors will generally
 * <em>not</em> result in a
 * <code>DirectoryManagerConfigurationException</code>, but in a log message
 * as the Directory Manager attempts to continue with its existing, presumably
 * working, configuration settings.
 */
public class DirectoryManagerConfigurationException
extends RuntimeException {

    /**
     * Constructor. Creates a new exception with only an exception message.
     * @param message
     *            The exception message.
     * @see Exception#Exception(java.lang.String)
     */
    public DirectoryManagerConfigurationException(final String message) {

        super(message);

    }


    /**
     * Constructor. Creates a new exception with both an exception message and a
     * cause.
     * @param message
     *            The exception message.
     * @param cause
     *            The exception cause.
     * @see Exception#Exception(java.lang.String, java.lang.Throwable)
     */
    public DirectoryManagerConfigurationException(final String message, final Throwable cause) {

        super(message, cause);

    }

}
