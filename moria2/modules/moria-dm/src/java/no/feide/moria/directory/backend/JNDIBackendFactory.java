/*
 * Copyright (c) 2004 UNINETT FAS
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the Free
 * Software Foundation; either version 2 of the License, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 59 Temple
 * Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package no.feide.moria.directory.backend;

import java.io.File;
import java.security.Security;

import no.feide.moria.directory.DirectoryManagerConfigurationException;

import org.jdom.Element;

/**
 * Factory class for JNDI backends.
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

    /** The name of the attribute containing the username. */
    private String usernameAttribute;

    /**
     * The name of the attribute used to guess the user element's (R)DN, if it
     * cannot be found by searching.
     */
    private String guessedAttribute;


    /**
     * Sets the factory-specific configuration. Must be called before
     * creating a new backend, or the backend will not work as intended. <br>
     * <br>
     * Note that using this method with an updated configuration that modifies
     * any JVM global settings (currently all settings in the
     * <code>Security</code> element) is likely to cause any open backend
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
     *            Also, the <code>JNDI</code> element may contain an attribute
     *            <code>guessedAttribute</code>, which should give the name
     *            of an attribute used to construct "guessed" user element
     *            (R)DNs if the actual element cannot be found by searching (the
     *            default value is <code>uid</code>). Finally the
     *            <code>JNDI</code> element must contain an attribute
     *            <code>usernameAttribute</code>, which should give the name
     *            of the attribute holding the username.
     * @throws IllegalArgumentException
     *             If <code>config</code> is null.
     * @throws DirectoryManagerConfigurationException
     *             If the configuration element is not a <code>JNDI</code>
     *             <code>Backend</code> element, or if the optional
     *             <code>Truststore</code> element
     *             is found, but without either of the <code>filename</code>
     *             or <code>password</code> attributes. Also thrown if the
     *             <code>timeout</code> attribute contains an illegal timeout
     *             value, if the <code>username</code> attribute does not 
     *             exist, if the <code>guess</code> attribute does not exist,
     *             or if the <code>filename</code> file does not exist.
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

        // Get username attribute and guessed attribute.
        usernameAttribute = jndiElement.getAttributeValue("usernameAttribute");
        if (usernameAttribute == null)
            throw new DirectoryManagerConfigurationException("Attribute \"usernameAttribute\" not found in JNDI element");
        guessedAttribute = jndiElement.getAttributeValue("guessedAttribute", "uid");

        // TODO: Add support for javax.net.ssl.keystore.

        // Get the optional Security element.
        final Element securityElement = jndiElement.getChild("Security");
        if (securityElement != null) {

            // Get the optional Truststore element.
            final Element trustStoreElement = securityElement.getChild("Truststore");
            if (trustStoreElement != null) {

                // Get truststore filename and check that it exists.
                String value = trustStoreElement.getAttributeValue("filename");
                if (value == null)
                    throw new DirectoryManagerConfigurationException("Attribute \"filename\" not found in Truststore element");
                if (!(new File(value).exists()))
                    throw new DirectoryManagerConfigurationException("Truststore file " + value + " does not exist");
                System.setProperty("javax.net.ssl.trustStore", value);

                // Get truststore password.
                value = trustStoreElement.getAttributeValue("password");
                if (value == null)
                    throw new DirectoryManagerConfigurationException("Attribute \"password\" not found in Truststore element");
                System.setProperty("javax.net.ssl.trustStorePassword", value);

                // Now we're ready to use SSL.
                Security.addProvider(new com.sun.net.ssl.internal.ssl.Provider());
                useSSL = true;

            }

        }

    }


    /**
     * Creates a new <code>JNDIBackend</code> instance.
     * @param sessionTicket
     *            The session ticket passed on to instances of
     *            <code>DirectoryManagerBackend</code> (actually
     *            <code>JNDIBackend</code> instances) for logging purposes.
     *            May be <code>null</code> or an empty string.
     * @return    The new JNDIBackend.
     * @see DirectoryManagerBackendFactory#createBackend(String)
     */
    public DirectoryManagerBackend createBackend(final String sessionTicket) {

        return new JNDIBackend(sessionTicket, backendTimeouts, useSSL, usernameAttribute, guessedAttribute);

    }

}
