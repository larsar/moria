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
 *
 */

package no.feide.moria.directory.index;

/**
 * This is the interface used to access an underlying index implementation.
 */
public interface DirectoryManagerIndex {

    /**
     * Looks up one or more backend references from a given logical ID,
     * typically a username.
     * @param id
     *            The logical ID.
     * @return One or more backend references , or <code>null</code> if no
     *         such reference was found.
     */
    IndexedReference[] getReferences(String id);


    /**
     * Return the related realm of a given logical ID, typically a username.
     * @param id
     *            The logical ID.
     * @return The realm, or <code>null</code> if the realm could not be
     *         resolved.
     */
    String getRealm(String id);


    /**
     * Return the username associated with a certain search base.
     * @param base
     *            The search base.
     * @return The username. May be an empty string if <code>base</code> is
     *         unknown.
     */
    String getUsername(String base);


    /**
     * Return the password associated with a certain search base.
     * @param base
     *            The search base.
     * @return The password. May be an empty string if <code>base</code> is
     *         unknown.
     */
    String getPassword(String base);

}
