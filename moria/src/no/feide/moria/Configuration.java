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
import java.util.Properties;
import java.util.logging.Logger;


/**
 * Allows access to the Moria configuration.
 */
public class Configuration {
    
    /** Used for logging. */
    private static Logger log = Logger.getLogger(Configuration.class.toString());
    
    /** Private property container. */
    private static Properties props = null;
    
    /** Have we already initialized? */
    private static boolean initialized = false;  
    
    
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
        String s = props.getProperty("no.feide.moria.SessionStoreInitMapSize");
        if (s == null) {
            log.severe("Missing required property: no.feide.moria.SessionStoreInitMapSize");
            throw new ConfigurationException("Missing required property: no.feide.moria.SessionStoreInitMapSize");
        }
        s = props.getProperty("no.feide.moria.SessionStoreMapLoadFactor");
        if (s == null) {
            log.severe("Missing required property: no.feide.moria.SessionStoreMapLoadFactor");
            throw new ConfigurationException("Missing required property: no.feide.moria.SessionStoreMapLoadFactor");
        }
        s = props.getProperty("no.feide.moria.AuthorizationTimerDelay");
        if (s == null) {
            log.severe("Missed required property: no.feide.moria.AuthorizationTimerDelay");
            throw new ConfigurationException("Missed required property: no.feide.moria.AuthorizationTimerDelay");
        }
        s = props.getProperty("no.feide.moria.SessionTimeout");
        if (s == null) {
            log.severe("Missed required property: no.feide.moria.SessionTimeout");
            throw new ConfigurationException("Missed required property: no.feide.moria.SessionTimeout");
        }
        s = props.getProperty("no.feide.moria.SessionSSOTimeout");
        if (s == null) {
            log.severe("Missed required property: no.feide.moria.SessionSSOTimeout");
            throw new ConfigurationException("Missed required property: no.feide.moria.SessionSSOTimeout");
        }
        s = Configuration.getProperty("no.feide.moria.AuthenticatedSessionTimeout");
        if (s == null) {
            log.severe("Missed required property: no.feide.moria.AuthenticatedSessionTimeout");
            throw new ConfigurationException("Missed required property: no.feide.moria.AuthenticatedSessionTimeout");
        }
        
        // Done.
        initialized = true;
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
}
