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
 * Represents an exception caused by illegal or missing Moria configuration.
 */
public class ConfigurationException
extends Exception {
       
    /**
     * Message constructor.
     * @param msg Exception description.
     */
    public ConfigurationException(String msg) {
        super(msg);
    }
    
    
    /**
     * Message and cause constructor.
     * @param msg Exception description.
     * @param cause Exception cause.
     */
    public ConfigurationException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
