package no.feide.moria.directory;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
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
    private DirectoryManagerIndex index;

    /** Internal representation of the backend factory. */
    private DirectoryManagerBackendFactory backendFactory;

    /** The message logger. */
    private final MessageLogger log = new MessageLogger(DirectoryManager.class);

    /** The current (valid) Directory Manager configuration. */
    private DirectoryManagerConfiguration currentConfiguration = null;


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

        // TODO: Move index updates to external TimerTask.
        Constructor constructor = null;
        try {
            ObjectInputStream in = new ObjectInputStream(new FileInputStream(currentConfiguration.getIndexFilename()));
            index = (DirectoryManagerIndex) in.readObject();
        } catch (IOException e) {
            throw new DirectoryManagerConfigurationException("Unable to read index from file " + currentConfiguration.getIndexFilename(), e);
        } catch (ClassNotFoundException e) {
            throw new DirectoryManagerConfigurationException("Unable to instantiate index object", e);
        }

        // Set the backend factory class.
        // TODO: Initialize backend configuration update.
        // TODO: Gracefully handle switch between backend factories?
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
     *             error is encountered when operating the backend, including if
     *             the authentication fails.
     */
    public HashMap authenticate(final Credentials userCredentials, final String[] attributeRequest)
    throws BackendException {

        // Sanity check.
        if (currentConfiguration == null)
            throw new DirectoryManagerConfigurationException("Configuration not set");

        // TODO: Implement a backend pool.

        // Do the call through a temporary backend instance.
        DirectoryManagerBackend backend = backendFactory.createBackend();
        // TODO: Use secondary lookup results as fallback.
        List references = index.lookup(userCredentials.getUsername());
        if (references != null)
            backend.open((String) references.get(0));
        else
            throw new AuthenticationFailedException("User " + userCredentials.getUsername() + " is unknown");
        HashMap attributes = backend.authenticate(userCredentials, attributeRequest);
        backend.close();
        return attributes;

    }

}