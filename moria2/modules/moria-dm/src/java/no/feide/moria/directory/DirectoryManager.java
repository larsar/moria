package no.feide.moria.directory;

import java.lang.reflect.Constructor;
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
    private static DirectoryManagerIndex index;

    /** Internal representation of the backend factory. */
    private static DirectoryManagerBackendFactory backendFactory;

    /** The message logger. */
    private final static MessageLogger log = new MessageLogger(DirectoryManager.class);

    /** The current (valid) Directory Manager configuration. */
    private static DirectoryManagerConfiguration currentConfiguration = null;


    /**
     * Set the directory manager's configuration.
     * @param config
     *            The configuration. Must include the property
     *            <code>no.feide.moria.directory.configuration</code> that points to a file
     *            containing the Directory Manager configuration.
     */
    public static void setConfig(final Properties config) {

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

        // Set the index class and configure the index.
        // TODO: Initialize index update.
        // TODO: Gracefully handle switch between index classes?
        Constructor constructor = null;
        try {
            constructor = currentConfiguration.getIndexClass().getConstructor(null);
            index = (DirectoryManagerIndex) constructor.newInstance(null);
        } catch (NoSuchMethodException e) {
            throw new DirectoryManagerConfigurationException("Cannot find index constructor", e);
        } catch (Exception e) {
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
     *         available. Otherwise an empty array, which still indicate a
     *         successful authentication.
     * @throws BackendException
     *             Subclasses of <code>BackendException</code> is thrown if an
     *             error is encountered when operating the backend, including if
     *             the authentication fails.
     */
    public static UserAttribute[] authenticate(final Credentials userCredentials, final String[] attributeRequest)
    throws BackendException {

        // TODO: Implement a backend pool.

        // Do the call through a temporary backend instance.
        DirectoryManagerBackend backend = backendFactory.createBackend();
        // TODO: Use secondary lookup results as fallback.
        String reference = (String)index.lookup(userCredentials.getUsername()).get(0);
        if (reference != null)
            backend.open(reference);
        else
            throw new AuthenticationFailedException("User "+userCredentials.getUsername()+" is unknown");
        UserAttribute[] attributes = backend.authenticate(userCredentials, attributeRequest);
        backend.close();
        return attributes;

    }

}