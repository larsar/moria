package no.feide.moria.directory.backend;

import java.util.Properties;

import no.feide.moria.directory.DirectoryManagerConfigurationException;

import org.jdom.Element;

/**
 * Factory class for dummy backends.
 */
public class DummyBackendFactory
implements DirectoryManagerBackendFactory {

    /** Holds the dummy backend configuration. */
    private Element myConfig;


    /**
     * Configure use of the dummy backends. May be called repeatedly to update
     * used configuration. <br>
     * <br>
     * Note that much of the parsing of the configuration element is done in the
     * <code>DummyBackend</code> class.
     * @param config
     *            The new or updated configuration for the dummy backend. Cannot
     *            be <code>null</code>.
     * @throws IllegalArgumentException
     *             If <code>config</code> is <code>null</code>.
     * @throws DirectoryManagerConfigurationException
     *             If <code>config</code> is not a <code>Backend</code>
     *             element.
     * @see DirectoryManagerBackendFactory#setConfig(Properties)
     */
    public void setConfig(Element config) {

        // Sanity checks.
        if (config == null)
            throw new IllegalArgumentException("Backend configuration element cannot be NULL");
        if (!config.getName().equalsIgnoreCase("Backend"))
            throw new DirectoryManagerConfigurationException("Cannot find backend configuration element");
        
        myConfig = (Element)config.clone();

    }


    /**
     * Creates a new <code>DummyBackend</code> instance.
     * @see no.feide.moria.directory.backend.DirectoryManagerBackendFactory#createBackend()
     */
    public DirectoryManagerBackend createBackend() {

        DummyBackend newBackend = new DummyBackend();
        newBackend.setConfig(myConfig);
        return newBackend;

    }

}