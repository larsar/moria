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

import java.util.HashSet;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * @author Bjørn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public class RandomIdTest extends TestCase {

    public static Test suite() {
        return new TestSuite(RandomIdTest.class);
    }

    public void setUp() {
        /*
         * Set a mock value for the system property that the RandomId class
         * uses for node identifcation.
         */
        System.setProperty("no.feide.moria.store.randomid.nodeid", "no1");
    }

    /**
     * Test the base64 encoder
     */
    public void testPseudoBase64Encode() {
        byte[] input =
            {
                -127,
                -120,
                -110,
                -100,
                -90,
                -80,
                -70,
                -60,
                -50,
                -40,
                -30,
                -20,
                -10,
                -1,
                0,
                1,
                2,
                10,
                20,
                30,
                40,
                50,
                60,
                70,
                80,
                90,
                100,
                110,
                120,
                127 };

        // Stupid check of my own counting
        assertTrue(input.length == 30);

        String correctAnswer = "gYiSnKawusTO2OLs9v8AAQIKFB4oMjxGUFpkbnh-";
        String result = RandomId.pseudoBase64Encode(input);

        assertEquals(correctAnswer, result);
    }

    /**
     * Test for uniqueness in a high number of generated Ids
     */
    public void testNewId() {

        int noOfIds = 100000;
        HashSet ids = new HashSet(noOfIds);

        for (int i = 0; i < noOfIds; i++) {
            String id = RandomId.newId();
            assertNotNull("No ids should be null", id);

            if (!ids.contains(id)) {
                ids.add(id);
            }
        }

        assertTrue(
            "Size of id set ("
                + ids.size()
                + ") does not match number of generated ids, duplicate ids generated",
            ids.size() == noOfIds);
    }

    /**
     * Test conversion of 10-base long to byte array.
     *  
     */
    public void testLongToByteArray() {
        long input = 4410259550623765063L; // 0x3d3463b1da341247L;
        byte[] correctAnswer =
            {
                (byte) 0x3d,
                (byte) 0x34,
                (byte) 0x63,
                (byte) 0xb1,
                (byte) 0xda,
                (byte) 0x34,
                (byte) 0x12,
                (byte) 0x47 };

        byte[] result = RandomId.longToByteArray(input);

        for (int i = 0; i < 8; i++) {
            assertEquals("Array elements " + i + " does not match: ", correctAnswer[i], result[i]);
        }
    }
}