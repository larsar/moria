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

package no.feide.moria.directory.index;

/**
 * Represents an indexed reference to an external element. Used to
 * distinguish
 * between explicitly and implicitly indexed references.
 */
public class IndexedReference {

    /** External references. */
    private final String[] myReferences;

    /** true if this is a fully qualified reference to an external element. */
    private final boolean explicit;


    /**
     * Constructor. Creates a new indexed reference.
     * @param references
     *            One or more external references. Cannot be <code>null</code>.
     * @param explicitReference
     *            <code>true</code> if this is a fully qualified reference to
     *            an external element, otherwise <code>false</code>.
     * @throws NullPointerException
     *             If <code>references</code> is <code>null</code>.
     * @throws IllegalArgumentException
     *             If <code>references</code> is an an empty array.
     */
    public IndexedReference(String[] references, boolean explicitReference) {

        super();

        // Sanity check.
        if (references == null)
            throw new NullPointerException("References cannot be NULL");
        if (references.length == 0)
            throw new IllegalArgumentException("References cannot be an empty array");

        // Assignments.
        myReferences = (String[]) references.clone();
        explicit = explicitReference;

    }


    /**
     * Gets the external references.
     * @return One or more external references.
     */
    public String[] getReferences() {

        return (String[]) myReferences.clone();

    }


    /**
     * Checks whether this reference is an explicit reference to an external
     * element, or an implicit reference to a search base of some sort.
     * @return <code>true</code> if this is a fully qualified external
     *         reference to an element, otherwise <code>false</code>.
     */
    public boolean isExplicitlyIndexed() {

        return explicit;

    }

}
