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

import org.jdom.Element;

/**
 * The interface to the backend factory.
 */
public interface DirectoryManagerBackendFactory {

    /**
     * Set the backend factory configuration. Must be called before
     * <code>createBackend()</code> is used.
     * @param config
     *            The backend configuration. See details for the actual backend
     *            implementation used.
     */
    public void setConfig(final Element config);


    /**
     * Creates a new instance of a proper directory manager backend.
     * @param sessionTicket
     *            The session ticket belonging to instances of
     *            <code>DirectoryManagerBackend</code>. Used when logging.
     *            The actual implementation of
     *            <code>DirectoryManagerBackend</code> may choose to ignore
     *            this value, if logging is not an issue. May be
     *            <code>null</code> or an empty string.
     * @return A new instance of the backend, tied to the proper reference.
     */
    public DirectoryManagerBackend createBackend(final String sessionTicket);

}