/*
 * Copyright (c) 2004 FEIDE
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

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Bj&oslash;rn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public class MoriaStoreFactoryTest extends TestCase {
    public MoriaStoreFactoryTest(String testName) {
        super(testName);
    }

    public static Test suite() {
        return new TestSuite(MoriaStoreFactoryTest.class);
    }

    /**
     * Simple test that verifies the creation of a MoriaStore instance as the
     * result of the method tested. 
     */
    public void testCreateMoriaStore() throws MoriaStoreException {
        MoriaStore store = MoriaStoreFactory.createMoriaStore();
        assertFalse(store == null);
        assertTrue(store instanceof MoriaStore);
    }
}
