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
import java.util.Iterator;
import java.util.logging.Logger;


/**
 * This class represents a LDAP attribute and is used for
 * authorization of a web service. Both Profile and WebService have
 * lists of attributes.
 */
public class Attribute {

    /** Used for logging. */
    private static Logger log = Logger.getLogger(Attribute.class.toString());

    /** Name of attribute */
    private String name = null;

    /** Is this attribute allowd in use with SSO */
    private Boolean sso = null;

    /** Security level */
    private int secLevel = 1;
    
    /** Security level register */
    private static HashMap secLevels = initSecLevels();
    

    /**
     * Constructor
     * @param name Attribute name
     * @param sso Allow use of SSO with this attribute
     */
     Attribute(String name, String ssoStr, String secLevelStr) {
        log.finer("Attribute(String, boolean, String)");

        /* Set security level */
        if (secLevelStr == null || secLevelStr.equals("")) {
            log.warning("Attribute secLevel not set. Defaults to HIGH.");
            secLevelStr = "HIGH";
        }

        if (!secLevels.containsKey(secLevelStr)) {
            log.warning("Invalid attribute secLevel: \""+secLevelStr+"\" Set to default (HIGH).");
            secLevelStr = "HIGH";
        }


        if (ssoStr == null)
            ssoStr = "false";

        secLevel = ((Integer) secLevels.get(secLevelStr)).intValue();
        sso = new Boolean(ssoStr.equals("true"));
        this.name = name;
    }



    /** 
     * Constructor
     * @param name Name of attribute
     * @param sso  Allow use of SSO
     * @param secLevel Sensitivity/security level of attribute
     */
     Attribute(String name, boolean sso, int secLevel) {
        this.name = name;
        this.sso  = new Boolean(sso);
        this.secLevel = secLevel;
    }
    


    /**
     * Constructor
     * @param name Name of attribute
     */
    public Attribute(String name) {
        log.finer("Attribute(String)");

        this.name = name;
        this.sso = null;
    }



    /** 
     * Return security level
     */
    public int getSecLevel() {
        return secLevel;
    }



    /** 
     * Initialize security level register. 
     * @return HashMap with seclevels
     */
    private static HashMap initSecLevels() {
        HashMap secLevels = new HashMap();
        secLevels.put("HIGH", new Integer(3));
        secLevels.put("MEDIUM", new Integer(2));
        secLevels.put("LOW", new Integer(1));
        return  secLevels;
    }



    /**
     * Find the name for a given security level.
     * @param level Security level
     * @return Security level name
     */
    public static String secLevelName(int level) {

        for (Iterator it = secLevels.keySet().iterator(); it.hasNext(); ) {
            String key = (String) it.next();
            if (((Integer)secLevels.get(key)).intValue() == level)
                return key;
        }
        
        log.warning("Unknown security level: "+level);
        return "UNKNOWN";
    }


    
    /**
     * Get name of attribute.
     * @return The attribute name
     */
    public String getName() {
        log.finer("getName()");

        return name;
    }



    /**
     * Is the attribute allowed in use with SSO?
     * @return boolean value - allows SSO or not.
     */
    public boolean allowSso() {
        log.finer("allowSso()");

        return sso.booleanValue();
    }


}
