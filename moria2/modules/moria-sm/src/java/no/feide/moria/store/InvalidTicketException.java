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
 * This exception should be thrown whenever a ticket is considered erroneous.
 *
 * @author Bj�rn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public class InvalidTicketException extends Exception {

    /**
     * @see java.lang.Exception#Exception()
     */
    public InvalidTicketException() {
        super();
    }

    /**
     * @see java.lang.Exception#Exception(java.lang.String)
     */
    public InvalidTicketException(final String message) {
        super(message);
    }

    /**
     * @see java.lang.Exception#Exception(java.lang.String, java.lang.Throwable)
     */
    public InvalidTicketException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * @see java.lang.Exception#Exception(java.lang.Throwable)
     */
    public InvalidTicketException(final Throwable cause) {
        super(cause);
    }
}
