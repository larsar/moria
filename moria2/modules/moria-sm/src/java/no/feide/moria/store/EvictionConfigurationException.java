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
 * $Id$
 */

package no.feide.moria.store;

/**
 * Used to signal an exception regarding eviction policy configuration.
 *
 * @author Bj&oslash;rn Ola Smievoll &lt;b.o.smievoll@conduct.no&gt;
 * @version $Revision$
 */
public class EvictionConfigurationException extends RuntimeException {

    /**
     * Constructor. Creates a new exception with only an exception message.
     * @param message
     *            The exception message.
     */
    public EvictionConfigurationException(String message) {
        super(message);
    }

    /**
     * Constructor. Creates a new exception with only a cause.
     * @param cause
     *            The exception cause.
     */
    public EvictionConfigurationException(Throwable cause) {
        super(cause);
    }

    /**
     * Constructor. Creates a new exception with both an exception message and a
     * cause.
     * @param message
     *            The exception message.
     * @param cause
     *            The exception cause.
     */
    public EvictionConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }
}
