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
 * Represents a set of user authentication credentials. For now, only
 * username/password type credentials are supported.
 * @author Cato Olsen
 */
// TODO: Add logging. Sanity checks here?
public class Credentials {
    
    /** Type of credentials is undefined. */
    public static final int UNDEFINED = 0;
    
    /** 
     * Type of credentials is username/password. <code>getIdentifier()</code>
     * returns the username as a <code>String</code>.
     * <code>getCredentials()</code> returns the password as a
     * <code>String</code>.
     */
    public static final int PASSWORD = 1;
    
    /**
     * Used to hold type of credentials. Set by the appropriate
     * constructor.
     */
    private int type;
    
    /** Used to hold the user's identifier. */
    private Object id;
    
    /** Used to hold the user's credentials. */
    private Object cred;
    
    
    /**
     * Empty constructor.
     */
    public Credentials() {
        type = UNDEFINED;
    }
    
    
    /**
     * Username/password style constructor.
     * @param username Username, used as unique identifier.
     * @param password Password.
     */
    public Credentials(String username, String password) {
        type = PASSWORD;
        id = username;
        cred = password;
    }
    
    
    /**
     * Set credentials on an existing object.
     */
    public void set(Credentials c) {
        type = c.getType();
        id = c.getIdentifier();
        cred = c.getCredentials();
    }
    
    
    /**
     * Returns the type of credentials.
     * @return Type of credentials.
     */
    public int getType() {
        return type;
    }
    
    /**
     * Sets the type of credentials.
     * @param type The new type.
     */
    public void getType(int type) {
        this.type = type;
    }
    
    
    /**
     * Returns the unique identifier.
     * @return The identifier.
     */
    public Object getIdentifier() {
        return (String)id;
    }
    
    
    /**
     * Sets the unique identifier.
     * @param id The new identifier.
     */
    public void getIdentifier(Object id) {
        this.id = id;
    }
    
    
    /**
     * Returns the credentials.
     * @return The credentials.
     */
    public Object getCredentials() {
        return (String)cred;
    }
    
    
    /**
     * Sets the credentials.
     * @param The new credentials.
     */
    public void setCredentials(Object cred) {
        this.cred = cred;
    }
    
    
    /**
     * Returns a string representation of the credentials.
     */
    public String toString() {
        return new String((String)id+':'+(String)cred);
    }
}
