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

import java.util.HashMap;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Bjørn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public class CachedUserDataTest extends TestCase {

    HashMap attributes;

    public static Test suite() {
        return new TestSuite(CachedUserDataTest.class);
    }

    public void setUp() {
        attributes = new HashMap();
        attributes.put("", "");
    }
    public void testConstructor() {

        try {
            CachedUserData cachedUserData = new CachedUserData(null);
            fail("IllegalArgumentException should have been thrown when argument is null");
        } catch (IllegalArgumentException e) {
        }

        try {
            CachedUserData cachedUserData = new CachedUserData(attributes);
        } catch (Exception e) {
            fail("Object creation should succseed. Exception thrown: " + e.toString());
        }
    }

    public void testGetAttributes() throws IllegalArgumentException {
        CachedUserData cachedUserData = new CachedUserData(attributes);
        assertEquals("", attributes, cachedUserData.getAttributes());
    }
}
