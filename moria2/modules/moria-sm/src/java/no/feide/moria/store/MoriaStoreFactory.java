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

/**
 * Class implementing simple factory pattern returning implementations
 * of the MoriaStore interface.  Somewhat redundant as we only have one
 * implementation at the moment.
 *
 * @author Bj&oslash;rn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public final class MoriaStoreFactory {

    /**
     * Default private constructor.
     */
    private MoriaStoreFactory() {
    }

    /**
     * Static factory method creates an instance of MoriaStore.
     *
     * @return Instance of a MoriaStore implementation
     * @throws MoriaStoreException
     *         the constructor of returned implementation may throw this
     */
    public static MoriaStore createMoriaStore() throws MoriaStoreException {
        // TODO: Read class to instantiate from config
        return new MoriaCacheStore();
    }
}
