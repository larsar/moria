/*
 * Copyright (c) 2004 UNINETT FAS A/S This program is free software; you can
 * redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version. This program is distributed
 * in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details. You should have received
 * a copy of the GNU General Public License along with this program; if not,
 * write to the Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */
package no.feide.moria.directory;

import java.lang.reflect.Constructor;
import java.util.Properties;
import no.feide.moria.directory.backend.DirectoryManagerBackend;
import no.feide.moria.directory.backend.DirectoryManagerBackendFactory;
import no.feide.moria.directory.index.DirectoryManagerIndex;

/**
 * The Directory Manager (DM) component. Responsible for all backend operations,
 * that is, Authentication Server (LDAP) lookups and actual user authentication.
 * @author Cato Olsen
 */
public class DirectoryManager {

    /** Internal representation of the index. */
    private static DirectoryManagerIndex index;

    /** Internal representation of the backend factory. */
    private static DirectoryManagerBackendFactory backendFactory;


    /**
     * Utility method.
     * @param message
     * @param cause
     * @throws DirectoryManagerConfigurationException
     */
    protected static void error(String message, Throwable cause)
    throws DirectoryManagerConfigurationException {

        // Set up logging.
        // TODO: Make sure it works.
        //MessageLogger messageLog = new
        // MessageLogger(DirectoryManagerConfiguration.class);

        // TODO: Differ between critical and warning depending on existing
        // configuration.
        //messageLog.logCritical(message);
        throw new DirectoryManagerConfigurationException(message, cause);

    }


    /**
     * Set the directory manager's configuration.
     * @param config
     */
    public static void setConfig(Properties config)
    throws DirectoryManagerConfigurationException {

        // Pass on to the configuration handler.
        DirectoryManagerConfiguration.read(config);

        // Preparations.
        Class[] noParameters = {};
        Constructor constructor = null;

        // Set the index class.
        // TODO: Initialize index update.
        // TODO: Gracefully handle switch between index classes.
        try {
            constructor = DirectoryManagerConfiguration.getIndexClass().getConstructor(noParameters);
            index = (DirectoryManagerIndex) constructor.newInstance(noParameters);
        } catch (NoSuchMethodException e) {
            error("Cannot find index constructor", e);
        } catch (Exception e) {
            error("Unable to instantiate index object", e);
        }

        // Set the backend factory class.
        // TODO: Initialize backend configuration update.
        // TODO: Gracefully handle switch between backend factories.
        try {
            constructor = DirectoryManagerConfiguration.getBackendFactoryClass().getConstructor(noParameters);
            backendFactory = (DirectoryManagerBackendFactory) constructor.newInstance(noParameters);
        } catch (NoSuchMethodException e) {
            error("Cannot find backend factory constructor", e);
        } catch (Exception e) {
            error("Unable to instantiate backend factory object", e);
        }

        // Cleanup.
        noParameters = null;
        constructor = null;

    }


    /**
     * Forwards an authentication attempt to the underlying backend.
     * @param userCredentials
     * @param attributeRequest
     *            The list of attribute names requested for retrieval after
     *            authentication.
     * @return The user attributes matching the attribute request, if those were
     *         available. Otherwise an empty array, which still indicate a
     *         successful authentication.
     * @throws DirectoryManagerException
     */
    public static UserAttribute[] authenticate(Credentials userCredentials, String[] attributeRequest)
    throws DirectoryManagerException {
        
        // TODO: Implement a backend pool.
      
        // Do the call through a temporary backend instance.
        DirectoryManagerBackend backend = backendFactory.createBackend();
        backend.open(index.lookup(userCredentials.getUsername()));
        UserAttribute[] attributes = backend.authenticate(userCredentials, attributeRequest);
        backend.close();
        return attributes;

    }

}