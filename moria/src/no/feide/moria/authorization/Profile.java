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

package no.feide.moria.authorization;

import java.util.HashMap;
import java.util.logging.Logger;

/** 
 * This class represents a profile with authorized attributes. A web
 * service might be assosiated to one or more profile and gets access
 * to all it's profiles attributes. */
public class Profile {

    /** Used for logging. */
    private static Logger log = Logger.getLogger(Profile.class.toString());

    /** Name of the profile (unique)*/
    private String name;

    /** 
     * List of all attributes. Attribute name (String) is the key and
     * Boolean is the value: true if the attribute is allowed to be
     * used with Single Sign On (SSO), false if not.*/
    private HashMap attributes = new HashMap();

    /** 
     * Constructor.
     * @param name The profiles name
     */
    public Profile(String name) {
        log.finer("Profile(String)");

        this.name = name;
    }

    /** 
     * Check if attribute is allowed to use with SSO. Only if both
     * the default value of the attribute AND the web services link to
     * the attribute allows SSO, a web service can use the attribute
     * with SSO.
     * @param attribute The profile-attribute to check for SSO-use.
     * @return boolean
     */
    protected boolean ssoForAttribute(Attribute attribute) {
        log.finer("ssoForAttribute(Attribute)");

        if (attribute == null || attributes.get(attribute) == null)
            retrn false;

        return (((Boolean) attributes.get(attribute)).booleanValue() && attribute.allowSso());
    }

    /** 
     * Get the profiles attributes. 
     * @return attributes 
     */
    protected HashMap getAttributes() {
        log.finer("getAttributes()");

        return attributes;
    }

    /**
     * Set the profiles attributes.
     * @param attributes A HashMap with attributes.
     */
    protected void setAttributes(HashMap attributes) {
        log.finer("setAttributes(HashMap)");

        this.attributes = attributes;
    }

    /**
     * Get the name of the profile.
     * @return name 
     */
    public String getName() {
        log.finer("getName()");

        return name;
    }

}
