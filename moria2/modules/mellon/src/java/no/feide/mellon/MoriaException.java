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

package no.feide.mellon;

import java.io.Serializable;

/**
 * Used to signal an exception from the client-side interface. Created to
 * closely match the original Mellon1 class
 * <code>no.feide.mellon.MoriaException</code>, but is also used in the
 * Moria2 client-side interface.
 */
public class MoriaException
extends Exception
implements Serializable {

    /**
     * Basic constructor.
     */
    public MoriaException() {

        super();

    }


    /**
     * Message constructor.
     * @param message
     *            The exception message.
     */
    public MoriaException(String message) {

        super(message);

    }


    /**
     * Message and throwable constructor.
     * @param message
     * @param cause
     */
    public MoriaException(String message, Throwable cause) {

        super(message, cause);

    }


    /**
     * Throwable constructor.
     * @param cause
     */
    public MoriaException(Throwable cause) {

        super(cause);

    }

}
