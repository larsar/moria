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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import java.util.Properties;
import java.util.logging.Logger;
import java.util.HashMap;
import java.util.Enumeration;


/**
 * Allows access to the Moria configuration.
 */
public class Configuration {
    
    /** Used for logging. */
    private static Logger log = Logger.getLogger(Configuration.class.toString());
    
    /** Private property container. */
    private static Properties props = null;

    /** Organization list, indexed on organization name */
    private static HashMap orgNameList = new HashMap();
    
    /** Organization list, indexed on organization short */
    private static HashMap orgShortList = new HashMap();
    
    /** Have we already initialized? */
    private static boolean initialized = false;  
    
    
    private final static String[] notNullProperties = new String[] {
        "no.feide.moria.SessionStoreInitMapSize",
        "no.feide.moria.SessionStoreMapLoadFactor",
        "no.feide.moria.AuthorizationTimerDelay",
        "no.feide.moria.SessionTimeout",
        "no.feide.moria.SessionSSOTimeout",
        "no.feide.moria.AuthenticatedSessionTimeout",
        "no.feide.moria.defaultLanguage",
        "no.feide.moria.backend.ldap.url",
        "no.feide.moria.backend.ldap.usernameAttribute"
    };


    /**
     * Read the configuration properties from file. Will read the Moria
     * property file named in the system property
     * <code>no.feide.moria.config.file</code>, or
     * <code>/moria.properties</code> if the property is not set. Note that
     * the configuration properties are only read once, first time
     * <code>init()</code> is called.
     * @throws ConfigurationException If a <code>IOException</code> or a
     *                                <code>FileNotFoundException</code> is
     *                                caught when reading the configuration
     *                                file, or if a sanity check fails on a
     *                                required property.
     */
    public synchronized static void init()
    throws ConfigurationException {
        log.finer("init()");
        
        // We only do this once.
        if (initialized)
            return;
        
        if (props != null)
            return;
            
        /* Read properties from file. */
        props = new Properties();
        try {
            if (System.getProperty("no.feide.moria.config.file") == null) {
                log.config("no.feide.moria.config.file not set; default is \"/moria.properties\"");
                props.load((new Configuration()).getClass().getResourceAsStream("/moria.properties"));
            }
            else {
                log.config("no.feide.moria.config.file set to \""+System.getProperty("no.feide.moria.config.file")+'\"');
                props.load((new Configuration()).getClass().getResourceAsStream("no.feide.moria.config.file"));
            }
        } catch (FileNotFoundException e) {
            log.severe("FileNotFoundException during system properties import");
            throw new ConfigurationException("FileNotFoundException caught", e);
        } catch (IOException e) {
            log.severe("IOException during system properties import");
            throw new ConfigurationException("IOException caught", e);
        }
        log.config("Configuration file read, contents are:\n"+props.toString());

        // All Moria configuration sanity checks should go here.
        for (int i = 0; i < notNullProperties.length; i++) {
            checkPropertyNotNull(notNullProperties[i]);
        }


        // Read organization list
        try {
            Properties orgList = new Properties();
            String filename = Configuration.getProperty("no.feide.moria.organizationNames");
            orgList.load(new FileInputStream(new File(filename)));
            updateOrgLists(orgList);
        }
        
        catch (FileNotFoundException e) {
            log.severe("FileNotFoundException while reading organization list.");
            throw new ConfigurationException("FileNotFoundException caught", e);
        } catch (IOException e) {
            log.severe("IOException during reading of organization list.");
            throw new ConfigurationException("IOException caught", e);
        }
        
        // Done.
        initialized = true;
    }



    private static void updateOrgLists(Properties orgList) {
        String default_lang = "nb";

        // Insert into two HashMaps (need lookup both ways)
        for (Enumeration e = orgList.propertyNames(); e.hasMoreElements(); ) {
            String orgShort = (String) e.nextElement();
            String orgName  = (String) orgList.getProperty(orgShort);
                
            int sep = orgShort.indexOf("_");
         
            if (sep == -1) 
                log.warning("No language for organization name: '"+orgShort+"'. Ignored.");

            else {
                String shortName = orgShort.substring(0,sep);
                String lang = orgShort.substring(sep+1,orgShort.length());

                HashMap nameList = (HashMap) orgNameList.get(lang);
                HashMap shortList= (HashMap) orgShortList.get(lang);

                if (nameList == null) {
                    nameList = new HashMap();
                    shortList = new HashMap();
                    orgNameList.put(lang, nameList);
                    orgShortList.put(lang, shortList);
                }
                
                nameList.put(orgName, shortName);
                shortList.put(shortName, orgName);
            }
        }
    }

    
    /**
     * Verify that a property is not null.
     */
    private static void checkPropertyNotNull(String propertyName) throws ConfigurationException {
        String errorMessage;
        if (getProperty(propertyName) == null) {
            errorMessage = "Missed required property: "+propertyName;
            log.severe(errorMessage);
            throw new ConfigurationException(errorMessage);
        }
    }


    /**
     * Get a configuration property.
     * @param key The property key.
     * @return <code>null</code> if the property key is unknown, otherwise
     *         the property value.
     * @throws ConfigurationException If there is a problem reading the
     *                                property file, or during sanity checks.
     */
    public static String getProperty(String key)
    throws ConfigurationException {
        log.finer("getProperty(String)");
        
        init();
        return props.getProperty(key);
    }
    
    
    /**
     * Get a configuration property, with a default value returned if the
     * key doesn't exist.
     * @param key The property key.
     * @param value The default value, should the key not exist.
     * @return <code>value</code> if the property key is unknown, otherwise
     *         the property value.
     * @throws ConfigurationException If there is a problem reading the
     *                                property file, or during sanity checks.
     */
    public static String getProperty(String key, String value)
    throws ConfigurationException {
        log.finer("getProperty(String)");
        
        init();
        return props.getProperty(key, value); 
    }

    public static String getOrgShort(String orgName, String language) {
        String orgShort = (String) orgShortList.get(orgName+"_"+language);

        if (orgShort == null)
            log.warning("Orgnaization name does not exist: "+orgShort+" "+language);
        return orgShort;
    }

    public static String getOrgName(String orgShort, String language) {
        String orgName = (String) orgNameList.get(orgShort+"_"+language);

        if (orgName == null)
            log.warning("Orgnaization shortname does not exist: "+orgName+" "+language);
        return orgName;
    }

    public static HashMap getOrgNames(String language) {
        return (HashMap) orgShortList.get(language);
    }
}
