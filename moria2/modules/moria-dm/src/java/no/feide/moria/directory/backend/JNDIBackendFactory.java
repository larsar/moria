package no.feide.moria.directory.backend;

import java.security.Security;

import no.feide.moria.directory.DirectoryManagerConfigurationException;

import org.jdom.Element;

/**
 * Factory class for dummy backends.
 */
public class JNDIBackendFactory
implements DirectoryManagerBackendFactory {

    /**
     * The number of seconds before a backend connection times out. Default is
     * 15 seconds.
     */
    private int backendTimeouts = 15;

    /**
     * Should the backend use SSL? Default is <code>false</code>.
     */
    private boolean useSSL = false;


    /**
     * Used to set the factory-specific configuration. Must be called before
     * creating a new backend, or the backend will not work as intended. <br>
     * <br>
     * Note that using this method with an updated configuration that modifies
     * any JVM global settings (currently all settings in the
     * <code>Security</code> element) will likely cause any open backend
     * connections to fail.
     * @param config
     *            The configuration for this backend factory. The root node must
     *            be a <code>JNDI</code> element, containing an optional
     *            <code>Security</code> element, which in turn may contain an
     *            optional <code>Truststore</code> element. If the
     *            <code>Truststore</code> exists, it must contain the
     *            attributes <code>filename</code> and <code>password</code>,
     *            giving the truststore file location and password,
     *            respectively. The <code>JNDI</code> element may contain an
     *            optional attribute <code>timeout</code>, which gives the
     *            number of seconds before a backend connection should time out.
     *            If this value is a negative number, the timeout value will be
     *            set to zero (meaning the connection will never time out).
     * @throws IllegalArgumentException
     *             If <code>config</code> is null.
     * @throws DirectoryManagerConfigurationException
     *             If the configuration element is not a <code>JNDI</code>
     *             element, or if the optional <code>Truststore</code> element
     *             is found, but without either of the <code>filename</code>
     *             or <code>password</code> attributes. Also thrown if the
     *             <code>timeout</code> attribute contains an illegal timeout
     *             value.
     * @see DirectoryManagerBackendFactory#setConfig(Element)
     */
    public synchronized void setConfig(final Element config)
    throws DirectoryManagerConfigurationException {

        // Sanity checks.
        if (config == null)
            throw new IllegalArgumentException("Configuration cannot be NULL");
        if (!config.getName().equalsIgnoreCase("Backend"))
            throw new DirectoryManagerConfigurationException("Cannot find backend configuration element");

        // Get JNDI element, with sanity check.
        final Element jndiElement = config.getChild("JNDI");
        if (jndiElement == null)
            throw new DirectoryManagerConfigurationException("Cannot find JNDI configuration element");

        // Get optional timeout value.
        String timeout = jndiElement.getAttributeValue("timeout");
        if (timeout != null)
            try {
                backendTimeouts = Integer.parseInt(timeout);
            } catch (NumberFormatException e) {
                throw new DirectoryManagerConfigurationException("\"" + timeout + "\" is not a legal timeout value", e);
            }
        if (backendTimeouts < 0)
            backendTimeouts = 0;

        // TODO: Add support for javax.net.ssl.keystore.

        // Get the optional Security element.
        final Element securityElement = jndiElement.getChild("Security");
        if (securityElement != null) {

            // Get the optional Truststore element.
            final Element trustStoreElement = securityElement.getChild("Truststore");
            if (trustStoreElement != null) {

                // Get truststore filename.
                String value = trustStoreElement.getAttributeValue("filename");
                if (value == null)
                    throw new DirectoryManagerConfigurationException("Attribute \"filename\" not found in Truststore element");
                System.setProperty("javax.net.ssl.truststore", value);

                // Get truststore password.
                value = trustStoreElement.getAttributeValue("password");
                if (value == null)
                    throw new DirectoryManagerConfigurationException("Attribute \"password\" not found in Truststore element");
                System.setProperty("javax.net.ssl.truststorepassword", value);
                
                // Now we're ready to use SSL.
                useSSL = true;

            }

        }

        // Wrap up.
        Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());

    }


    /**
     * Creates a new <code>DummyBackend</code> instance.
     * @see no.feide.moria.directory.backend.DirectoryManagerBackendFactory#createBackend()
     */
    public DirectoryManagerBackend createBackend() {

        return new JNDIBackend(backendTimeouts, useSSL);

    }

}