package no.feide.moria.directory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Timer;

import no.feide.moria.directory.DirectoryManagerConfigurationException;
import no.feide.moria.directory.backend.AuthenticationFailedException;
import no.feide.moria.directory.backend.BackendException;
import no.feide.moria.directory.backend.DirectoryManagerBackend;
import no.feide.moria.directory.backend.DirectoryManagerBackendFactory;
import no.feide.moria.directory.index.DirectoryManagerIndex;
import no.feide.moria.log.MessageLogger;

/**
 * The Directory Manager (DM) component. Responsible for all backend operations,
 * that is, Authentication Server (LDAP) lookups and actual user authentication.
 */
public class DirectoryManager {

    /** Internal representation of the index. */
    private DirectoryManagerIndex index = null;

    /** Periodically calls updateIndex(). */
    private Timer indexUpdater = null;
    
    /** Timestamp of last index file read from file. */
    private long indexTimeStamp = 0;

    /** Internal representation of the backend factory. */
    private DirectoryManagerBackendFactory backendFactory = null;

    /** The message logger. */
    private final MessageLogger log = new MessageLogger(DirectoryManager.class);

    /** The current (valid) Directory Manager configuration. */
    private DirectoryManagerConfiguration currentConfiguration = null;
    
    
    /**
     * Destructor. Cancels the index updater task, if it has been initialized.
     */
    public void destroy() {
       
        if (indexUpdater != null)
            indexUpdater.cancel();
        
    }


    /**
     * Set the directory manager's configuration.
     * @param config
     *            The configuration. Must include the property
     *            <code>no.feide.moria.directory.configuration</code> that
     *            points to a file containing the Directory Manager
     *            configuration.
     */
    public void setConfig(final Properties config) {

        // Update current configuration.
        try {

            final DirectoryManagerConfiguration newConfiguration = new DirectoryManagerConfiguration(config);
            currentConfiguration = newConfiguration;

        } catch (Exception e) {

            // Something happened while updating the configuration; can we
            // recover?
            if (currentConfiguration == null) {

                // Critical error; we don't have a working configuration.
                throw new DirectoryManagerConfigurationException("Unable to set initial configuration", e);

            } else {

                // Non-critical error; we still have a working configuration.
                log.logWarn("Unable to update existing configuration", e);

            }

        }

        // Update the index; (re-)start the index updater.
        if (indexUpdater == null) {
            
            // Initial call to setConfig(); manually update the index.
            updateIndex();
            indexUpdater = new Timer(true); // Daemon.
        }
        else
            indexUpdater.cancel();
        long frequency = currentConfiguration.getIndexUpdateFrequency();
        indexUpdater.scheduleAtFixedRate(new IndexUpdater(this, currentConfiguration.getIndexFilename()), frequency, frequency);

        // Set the backend factory class.
        // TODO: Initialize backend configuration update.
        // TODO: Gracefully handle switch between backend factories?
        Constructor constructor = null;
        try {

            constructor = currentConfiguration.getBackendFactoryClass().getConstructor(null);
            backendFactory = (DirectoryManagerBackendFactory) constructor.newInstance(null);

        } catch (NoSuchMethodException e) {
            log.logCritical("Cannot find backend factory constructor", e);
            throw new DirectoryManagerConfigurationException("Cannot find backend factory constructor", e);
        } catch (Exception e) {
            log.logCritical("Unable to instantiate backend factory object", e);
            throw new DirectoryManagerConfigurationException("Unable to instantiate backend factory object", e);
        }

    }


    /**
     * Update the internal index structure.
     * @throws DirectoryManagerConfigurationException
     *             If unable to read the index from file, or instantiate the
     *             index object. Note that the exception is not thrown if a
     *             previous index has been initialized; instead, an error
     *             message is logged.
     */
    protected synchronized void updateIndex() {

        try {
            
            // Check if a new index file exists, with a newer timestamp than the one previously read.
            File indexFile = new File(currentConfiguration.getIndexFilename());
            if (!indexFile.isFile())
                if (index == null)
                    throw new DirectoryManagerConfigurationException("Index file "+currentConfiguration.getIndexFilename()+" does not exist");
                else
                    log.logCritical("Index file "+currentConfiguration.getIndexFilename()+" does not exist");
                    
            if (indexTimeStamp >= indexFile.lastModified())
            	return;  // No update necessary.
            indexTimeStamp = indexFile.lastModified();
            
            // Read the new index from file.
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(currentConfiguration.getIndexFilename()));
            DirectoryManagerIndex newIndex = (DirectoryManagerIndex) in.readObject();
            index = newIndex;
            
        } catch (IOException e) {
            if (index == null)
                throw new DirectoryManagerConfigurationException("Unable to read index from file " + currentConfiguration.getIndexFilename(), e);
            else
                log.logCritical("Unable to read index from file " + currentConfiguration.getIndexFilename(), e);
        } catch (ClassNotFoundException e) {
            if (index == null)
                throw new DirectoryManagerConfigurationException("Unable to instantiate index object", e);
            else
                log.logCritical("Unable to instantiate index object", e);
        }

    }


    /**
     * Forwards an authentication attempt to the underlying backend.
     * @param userCredentials
     * @param attributeRequest
     *            The list of attribute names requested for retrieval after
     *            authentication.
     * @return The user attributes matching the attribute request, if those were
     *         available. Otherwise an empty <code>HashMap</code>, which
     *         still indicates a successful authentication.
     * @throws BackendException
     *             Subclasses of <code>BackendException</code> is thrown if an
     *             error is encountered when operating the backend.
     * @throws AuthenticationFailedException
     *             If we managed to access the backend, and the authentication
     *             failed. In other words, the user credentials are incorrect.
     */
    public HashMap authenticate(final Credentials userCredentials, final String[] attributeRequest)
    throws AuthenticationFailedException, BackendException {

        // Sanity check.
        if (currentConfiguration == null)
            throw new DirectoryManagerConfigurationException("Configuration not set");

        // TODO: Implement a backend pool.

        // Do the call through a temporary backend instance.
        DirectoryManagerBackend backend = backendFactory.createBackend();
        List references = index.lookup(userCredentials.getUsername());
        if (references != null) {
            
            // Found a reference. Now open it.
            // TODO: Use secondary references as fallback.
            backend.open((String)references.get(0));
            
        }
        else
            throw new AuthenticationFailedException("User " + userCredentials.getUsername() + " is unknown");
        
        // Authenticate the user.
        HashMap attributes = backend.authenticate(userCredentials, attributeRequest);
        
        backend.close();
        return attributes;

    }

}