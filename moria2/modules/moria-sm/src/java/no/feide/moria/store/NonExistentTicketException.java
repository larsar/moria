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
 * Raised whenever a requested ticket is not found in the store.
 *
 * @author Bjørn Ola Smievoll &lt;b.o.smievoll@conduct.no&gt;
 * @version $Revision$
 */
public class NonExistentTicketException extends Exception {

    /**
     * @see java.lang.Exception#Exception()
     */
    public NonExistentTicketException() {
        super("Ticket does not exist.");
    }

    /**
     * @see java.lang.Exception(java.lang.String)
     */
    public NonExistentTicketException(String message) {
        super(message);
    }

    /**
     * @see java.lang.Exception(java.lang.Throwable)
     */
    public NonExistentTicketException(Throwable cause) {
        super(cause);
    }

    /**
     * @see java.lang.Exception(java.lang.String, java.lang.Throwable)
     */
    public NonExistentTicketException(String message, Throwable cause) {
        super(message, cause);
    }
}
