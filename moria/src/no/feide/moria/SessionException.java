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

public class SessionException extends Exception{
    
     public SessionException(String message) {
        super(message);
    }
    
    /**
     * Constructor that takes a cause.
     * @param cause The cause to encapsulate.
     */
    public SessionException(Throwable cause) {
        super(cause);
    }
    
    
    /**
     * Description and cause constructor.
     * @param msg The exception message.
     * @param cause The exception cause.
     */
    public SessionException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
