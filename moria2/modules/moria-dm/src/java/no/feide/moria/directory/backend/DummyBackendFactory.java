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

        DummyBackend newBackend = new DummyBackend(myConfig);
        return newBackend;

    }

}