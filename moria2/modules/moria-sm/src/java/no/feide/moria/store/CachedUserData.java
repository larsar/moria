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
 * $Id$
 */

package no.feide.moria.store;

import java.util.HashMap;

/**
 * This class is a container for userdata that persists across logins (for
 * SSO Light and Proxy authentication).
 *
 * @author Bj&oslash;rn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public final class CachedUserData implements MoriaStoreData {

    /** The internal hashmap used to store the attributes. */
    private final HashMap attributes;

    /**
     * Constructs a new instance encapsulating the userdata given as argument.
     *
     * @param attributes
     *          A hashmap containing the user attributes.
     */
    public CachedUserData(final HashMap attributes) {
        if (attributes == null)
            throw new IllegalArgumentException("Argument can not be null");
        this.attributes = attributes;
    }

    /**
     * Adds an attribute. Needed to add ticket granting tickets to cache.
     *
     * @param name Attribute name.
     * @param data Attribute data.
     */
    public void addAttribute(final String name, final Object data) {
        this.attributes.put(name, data);
    }

    /**
     * Gets a map containing the attributes.
     *
     * @return A clone of the internal attribute map.
     */
    public HashMap getAttributes() {
        return (HashMap) attributes.clone();
    }
}
