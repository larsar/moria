/*
 * Copyright (c) 2004 UNINETT FAS A/S
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
 * Represents an indexed element, both internally for explicitly indexed
 * elements (such as a user) or externally as output from the indexing
 * subsystem.
 * @author Cato Olsen
 */
public final class IndexedElement
extends Element {

    /* The internal representation of the element's affiliation. */
    private AffiliationElement affiliation;

    /* The internal representation of the element's search offset. */
    private String offset;

    /* The internal representation of the element's name. */
    private String name;


    /**
     * Creates a new object instance.
     * @param affiliation
     *            The affiliation for this element. May not be <code>null</code>.
     * @param offset
     *            The offset (relative to the base corresponding to the
     *            affiliation) of this element. May be <code>null</code>, in
     *            which case the element is not explicitly indexed, but can be
     *            found by a recursive search from the search base.
     * @param name
     *            The element's identifying (and indexed) name. May not be
     *            <code>null</code>.
     */
    IndexedElement(AffiliationElement affiliation, String offset, String name)
    throws IllegalArgumentException {

        // Check validity.
        if (affiliation == null)
            throw new IllegalArgumentException("Affiliation cannot be NULL");
        if (name == null)
            throw new IllegalArgumentException("Name may not be NULL");

        this.affiliation = affiliation;
        this.offset = offset;
        this.name = name;
    }


    /**
     * Get the element's affiliation.
     * @return The affiliation for this element. May not be <code>null</code>.
     */
    public AffiliationElement getAffiliation() {
        return affiliation;
    }


    /**
     * Get the search offset.
     * @return The physical search offset for this element, relative to the
     *         search base (derived from its affiliation. May be <code>null</code>,
     *         in which case the element is not explicitly indexed.
     */
    public String getOffset() {
        return offset;
    }


    /**
     * Get the element's identifying (and indexed) name.
     * @return The element's unique name. May not be <code>null</code>.
     */
    public String getName() {
        return name;
    }


    /**
     * Is the element explicitly indexed?
     * @return <code>true</code> if the element has been explicitly indexed;
     *         that is, if the search base combined with the search offset
     *         (implictly given by its affiliation) gives a direct reference to
     *         the physical element itself. If <code>false</code>, the
     *         element must be found by doing a recursive search from the
     *         search base.
     */
    public boolean isIndexed() {
        return offset == null;
    }

}
