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
 * Used to signal errors from the store.
 *
 * @author Bjørn Ola Smievoll &lt;b.o.smievoll@conduct.no&gt;
 * @version $Revision$
 */
public class MoriaStoreException extends Exception {

    /**
     * @see java.lang.Exception()
     */
    public MoriaStoreException() {
        super();
    }

    /**
     * @see java.lang.Exception(java.lang.String)
     */
    public MoriaStoreException(String message) {
        super(message);
    }

    /**
     * @see java.lang.Exception(java.lang.Throwable)
     */
    public MoriaStoreException(Throwable cause) {
        super(cause);
    }

    /**
     * @see java.lang.Exception(java.lang.String, java.lang.Throwable)
     */
    public MoriaStoreException(String message, Throwable cause) {
        super(message, cause);
    }
}
