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
import java.util.Locale;


/**
 * Allows access to the Moria configuration.
 */
public class Configuration {
    
    /** Used for logging. */
    private static Logger log = Logger.getLogger(Configuration.class.toString());
    
    /** Private property container. */
    private static Properties props = null;

    /** Organization list, indexed on organization name. The HashMap
     * will contain language as index an a new HashMap for every
     * entry. */
    private static HashMap orgNameList = new HashMap();
    
    /** Organization list, indexed on organization short. The HashMap
     * will contain language as index an a new HashMap for every
     * entry.*/
    private static HashMap orgShortList = new HashMap();
    
    /** Available languages for the login page */
    private static HashMap languages = new HashMap();

    /** Have we already initialized? */
    private static boolean initialized = false;  
    
    /** Properties that cannot be null */
    private final static String[] notNullProperties = new String[] {
        "no.feide.moria.SessionStoreInitMapSize",
        "no.feide.moria.SessionStoreMapLoadFactor",
        "no.feide.moria.AuthorizationTimerDelay",
        "no.feide.moria.SessionTimeout",
        "no.feide.moria.SessionSSOTimeout",
        "no.feide.moria.AuthenticatedSessionTimeout",
        "no.feide.moria.defaultLanguage",
        "no.feide.moria.backend.ldap.url1",
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


            /* Languages */
            String[] langStrings = getProperty("no.feide.moria.availableLanguages").split(",");
            languages = new HashMap();

            for (int i = 0; i < langStrings.length; i++) {
                String[] lang = langStrings[i].split(":");
                languages.put(lang[0], lang[1]);
            }


        } catch (FileNotFoundException e) {
            log.severe("FileNotFoundException during system properties import");
            throw new ConfigurationException("FileNotFoundException caught", e);
        } catch (IOException e) {
            log.severe("IOException during system properties import");
            throw new ConfigurationException("IOException caught", e);
        }
        log.config("Configuration file read, contents are:\n"+props.toString());

        /* Sanity checks of attributes */
        for (int i = 0; i < notNullProperties.length; i++) {
            checkPropertyNotNull(notNullProperties[i]);
        }


        /* Read organization list */
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
        

        /* Set default locale */
        Locale.setDefault(new Locale(getProperty("no.feide.moria.defaultLanguage")));


        // Done.
        initialized = true;
    }



    /**
     * Update the of all organization names for all languages.
     * orgNameList and orgShortList are generated from a set of
     * properties. Each HashMaps are indexed on languages and each
     * entry contains a HashMap of shortName->fullName or
     * fullName->shortName. Double set of HashMaps are required for
     * lookup by both full name and short name.
     * @param orgList The properties containing the data
     */
    private static void updateOrgLists(Properties orgList) {

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
                
                shortList.put(orgName, shortName);
                nameList.put(shortName, orgName);
            }
        }
    }


    
    /**
     * Verify that a property is not null. Throw exception if null.
     * @param propertyName Name of the property that cannot be null.
     */
    private static void checkPropertyNotNull(String propertyName)
    throws ConfigurationException {
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
     * Get all properties
     * @return The properties read from the configuration file
     * @throws ConfigurationException If there is a problem reading the
     *                                property file, or during sanity checks.
     */
    public static Properties getProperties() throws ConfigurationException {
        log.finer("getProperties()");
        
        init();
        return props;
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
    


    /**
     * Return the short name of an organization.
     * @param orgName The name of the organization
     * @param language The language for the given organization name
     * @return The short name for the organization
     */
    public static String getOrgShort(String orgName, String language) {
        init();
        String orgShort = (String) orgShortList.get(orgName+"_"+language);

        if (orgShort == null)
            log.warning("Orgnaization name does not exist: "+orgShort+" "+language);
        return orgShort;
    }



    /**
     * Return the full name of an organization.
     * @param orgShort The short for the organization
     * @param language What language should be used for the full name
     * @return Full organization name in the selected language
     */
    public static String getOrgName(String orgShort, String language) {
        init();
        String orgName = (String) orgNameList.get(orgShort+"_"+language);

        if (orgName == null)
            log.warning("Orgnaization shortname does not exist: "+orgName+" "+language);
        return orgName;
    }



    /**
     * Return all organization names.
     * @param lanugage Language for the organization names
     * @return A HashMap of all organization names for the requested language
     */
    public static HashMap getOrgNames(String language) {
        init();
        return (HashMap) orgNameList.get(language);
    }



    /**
     * Return all organization short names.
     * @param lanugage Language for the organization names
     * @return A HashMap of all organization short names for the
     * requested language
     */
    public static HashMap getOrgShorts(String language) {
        init();
        return (HashMap) orgShortList.get(language);
    }



    /**
     * Return all configured languages (for the login page)
     * @return HashMap of all languages indexed on language code
     */
    public static HashMap getLanguages() {
        init();
        return languages;
    }
}
