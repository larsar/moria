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

package no.feide.mellon.jaas;

import java.security.Principal;

/**
 * This class contains the principal name of a Moria user. 
 * @author Lars Preben S. Arnesen
 */
public class MoriaPrincipal implements Principal {
	private final String name;
	
	/**
	 * Constructor. Takes a pricipal name as argument.
	 * @param name PrincipalName
	 */
	public MoriaPrincipal(String name) {
		if(name == null) {
			throw new IllegalArgumentException("Null name");
		}
		this.name = name;
	}
	
	
	/**
	 * Return the principal name.
	 * @return name The principal name.
	 */
	public String getName() {
		return name;
	}
	
	
	/**
	 * Return a string representation of the object. The method returns the name
	 * with a debug prefix.
	 * @return output string
	 */
	public String toString() {
		return "MoriaPrincipal: "+name;
	}
	
	
	/**
	 * Returns true if the supplied object is equal to this.
	 * @return boolean
	 */
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if(obj == this) return true;
		if(!(obj instanceof MoriaPrincipal)) 
			return false;
		MoriaPrincipal another = (MoriaPrincipal) obj;
		return name.equals(another.getName());
	}
	
	
	/**
	 * Return the hascode for the principal name.
	 * @return int The hash code.
	 */
	public int hashCode() {
		return name.hashCode();
	}
}
