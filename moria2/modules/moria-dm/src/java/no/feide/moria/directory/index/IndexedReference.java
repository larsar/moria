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
 * Represents an indexed reference to an external element. Used to differ
 * between explicitly and implicitly indexed references.
 */
public class IndexedReference {

    /** External references for this instance. */
    private final String[] myReferences;

    /** Is this an explicitly indexed reference? */
    private final boolean explicit;

    /** The realm this reference belongs to. */
    private final String realm;


    /**
     * Constructor. Creates a new indexed reference.
     * @param references
     *            One or more external references. Cannot be <code>null</code>.
     * @param explicitReference
     *            <code>true</code> if this is a fully qualified reference to
     *            an external element, otherwise <code>false</code>.
     * @param realm
     *            The actual realm this indexed reference belongs to. Must be a
     *            non-empty string.
     * @throws NullPointerException
     *             If <code>references</code> or <code>realm</code> is <code>null</code>.
     * @throws IllegalArgumentException
     *             If <code>references</code> is an an empty array, or if <code>realm</code> is an empty string.
     */
    public IndexedReference(final String[] references, final boolean explicitReference, final String realm) {

        super();

        // Sanity check.
        if (references == null)
            throw new NullPointerException("References cannot be null");
        if (references.length == 0)
            throw new IllegalArgumentException("References cannot be an empty array");
        if (realm == null)
            throw new NullPointerException("Realm cannot be null");
        if (realm.length() == 0)
            throw new IllegalArgumentException("Realm cannot be an empty string");

        // Assignments.
        myReferences = (String[]) references.clone();
        explicit = explicitReference;
        this.realm = realm;

    }


    /**
     * Get the external references.
     * @return One or more external references.
     */
    public String[] getReferences() {

        return (String[]) myReferences.clone();

    }


    /**
     * Check whether this reference is an explicit reference to an external
     * element, or an implicit reference to a search base of some sort.
     * @return <code>true</code> if this is a fully qualified external
     *         reference to an element, otherwise <code>false</code>.
     */
    public boolean isExplicitlyIndexed() {

        return explicit;

    }


    /**
     * Get the associated realm.
     * @return The realm for this indexed reference. Note that the realm may not
     *         be apparent from the identifier for any reference; use this
     *         method to extract the actual realm.
     */
    public String getRealm() {

        return realm;

    }

}