/**
 * Copyright (C) 2003 FEIDE
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package no.feide.moria;

/**
 * Used to throw exceptions from the backend.
 * @author Cato Olsen
 */
public class BackendException
extends java.lang.Exception {
    
    /**
     * Basic constructor.
     * @param message Exception message.
     */
    public BackendException(String message) {
        super(message);
    }
    
    /**
     * Constructor. Used to encapsulate another exception.
     * @param message Exception message.
     * @param cause Cause of exception.
     */
    public BackendException(Throwable cause) {
        super(cause);
    }
    
    /**
     * Constructor. Used to pass on another exception with a message.
     * @param message Exception message.
     * @param cause Cause of exception.
     */
    public BackendException(String message, Throwable cause) {
        super(message, cause);
    }
}
