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
 * @author Bj&oslash;rn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public class RandomIdTest extends TestCase {

    public static Test suite() {
        return new TestSuite(RandomIdTest.class);
    }

    /**
     * Test the base64 encoder
     */
    public void testPseudoBase64Encode() {
        byte[] input = {-127, -120, -110, -100, -90, -80, -70, -60, -50, -40, -30, -20, -10, -1, 0, 1, 2, 10, 20, 30, 40, 50, 60,
                        70, 80, 90, 100, 110, 120, 127};

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

        int noOfIds = 10000;
        HashSet ids = new HashSet(noOfIds);

        for (int i = 0; i < noOfIds; i++) {
            String id = RandomId.newId("127.0.0.1:42767");
            assertNotNull("No ids should be null", id);

            if (!ids.contains(id)) {
                ids.add(id);
            }
        }

        assertTrue("Size of id set (" + ids.size() + ") does not match number of generated ids, duplicate ids generated",
                ids.size() == noOfIds);
    }

    /**
     * 
     *
     */
    public void testNodeIdToByteArray() {
        final String input = "10.183.0.254:32975";
        final byte[] correctAnswer = {(byte) 0x8a, (byte) 0x37, (byte) 0x80, (byte) 0x7e, (byte) 0x80, (byte) 0xcf};

        final short resultLength = 6;
        byte[] result = RandomId.nodeIdToByteArray(input);

        assertEquals("Length of result array is not correct: ", resultLength, result.length);

        for (short i = 0; i < resultLength; i++) {
            assertEquals("Array elements " + i + " does not match: ", correctAnswer[i], result[i]);
        }
    }

    /**
     * Test conversion of 10-base long to byte array.
     *  
     */
    public void testLongToByteArray() {
        final long input = 4410259550623765063L; // 0x3d3463b1da341247L;
        final byte[] correctAnswer = {(byte) 0x3d, (byte) 0x34, (byte) 0x63, (byte) 0xb1, (byte) 0xda, (byte) 0x34, (byte) 0x12,
                                      (byte) 0x47};

        final short resultLength = 8;

        byte[] result = RandomId.longToByteArray(input);

        assertEquals("Length of result array is not correct: ", resultLength, result.length);

        for (short i = 0; i < resultLength; i++) {
            assertEquals("Array elements " + i + " does not match: ", correctAnswer[i], result[i]);
        }
    }

    /**
     * 
     *
     */
    public void testUnsignedIntToByteArray() {
        int input = 65536;

        try {
            byte[] result = RandomId.unsignedShortToByteArray(input);
            fail("IllegalArgumentException should have been thrown.");
        } catch (IllegalArgumentException success) {
        }

        input = -1;

        try {
            byte[] result = RandomId.unsignedShortToByteArray(input);
            fail("IllegalArgumentException should have been thrown.");
        } catch (IllegalArgumentException success) {
        }

        input = 45432;
        final byte[] correctAnswer = {(byte) 0xb1, (byte) 0x78};
        final short resultLength = 2;

        byte[] result = RandomId.unsignedShortToByteArray(input);

        assertEquals("Length of result array is not correct: ", resultLength, result.length);

        for (short i = 0; i < resultLength; i++) {
            assertEquals("Array elements " + i + " does not match: ", correctAnswer[i], result[i]);
        }
    }
}
