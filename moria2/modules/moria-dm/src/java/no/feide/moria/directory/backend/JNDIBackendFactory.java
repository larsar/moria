package no.feide.moria.directory.backend;

import java.security.Security;
import java.util.Properties;

import no.feide.moria.log.MessageLogger;

/**
 * Factory class for dummy backends.
 */
public class JNDIBackendFactory
implements DirectoryManagerBackendFactory {

    /** The message logger. */
    private final static MessageLogger log = new MessageLogger(JNDIBackend.class);

    /** Did we already initialize this backend factory? */
    private static boolean initialized = false;

    /**
     * The property used to set the Moria truststore. Currently set to
     * <code>no.feide.moria.directory.backend.jndi.truststore</code>.
     */
    public static final String PROPERTIES_TRUSTSTORE = "no.feide.moria.directory.backend.jndi.truststore";

    /**
     * The property used to set the Moria truststore password. Currently set to
     * <code>no.feide.moria.directory.backend.jndi.truststorepassword</code>.
     */
    public static final String PROPERTIES_TRUSTSTORE_PASSWORD = "no.feide.moria.directory.backend.jndi.truststorepassword";


    /**
     * Used to set the factory-specific configuration. Must be called before
     * creating a new backend, or the backend will not work as intended. <br>
     * <br>
     * Note that the method will only take any action the very first time it is
     * executed, since modifying the global SSL settings after any backend
     * connections have been opened will result in an illegal state.
     * @param config
     *            The global configuration for this backend factory. May include
     *            the following properties:
     *            <ul>
     *            <li><code>PROPERTIES_TRUSTSTORE</code> value <br>
     *            Optional location of the Moria truststore file, which should
     *            contain the cerfiticates required to trust the external LDAP
     *            servers. Will be mapped to
     *            <code>javax.net.ssl.truststore</code>.
     *            <li><code>PROPERTIES_TRUSTSTORE_PASSWORD</code> value <br>
     *            Optional truststore password used to access the truststore
     *            file. Only relevant if the truststore file is set. Will be
     *            mapped to <code>javax.net.ssl.truststorepassword</code>.
     *            </ul>
     *            Note that <code>config</code> is not stored internally.
     * @throws IllegalArgumentException
     *             If <code>config</code> is null.
     * @see #PROPERTIES_TRUSTSTORE
     * @see #PROPERTIES_TRUSTSTORE_PASSWORD
     */
    public synchronized void setConfig(final Properties config) {

        // Sanity check.
        if (config == null)
            throw new IllegalArgumentException("Configuration cannot be NULL");

        // Did we already set the one-time configuration?
        if (initialized)
            return;
        
        // TODO: Add support for javax.net.ssl.keystore(password).

        // Get and verify some properties.
        String s = config.getProperty(PROPERTIES_TRUSTSTORE);
        if (s != null) {
            System.setProperty("javax.net.ssl.truststore", s);
            s = config.getProperty(PROPERTIES_TRUSTSTORE_PASSWORD);
            if (s != null)
                System.setProperty("javax.net.ssl.truststorepassword", s);
        }

        // Wrap up.
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
        initialized = true;

    }


    /**
     * Creates a new <code>DummyBackend</code> instance.
     * @see no.feide.moria.directory.backend.DirectoryManagerBackendFactory#createBackend()
     */
    public DirectoryManagerBackend createBackend() {

        return new JNDIBackend();

    }

}