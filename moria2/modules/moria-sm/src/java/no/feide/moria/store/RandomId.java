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

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Date;

/**
 * Returns an id that's random and unique across a cluster of JVMs.
 * 
 * Each JVM needs to be configured with an unique node id, identifying each
 * node. This is done by setting the system property <code>no.feide.moria.store.randomid.nodeid</code>.
 * The value must be a ascii string of 3 character length.
 * 
 * The returned id is an encoded String (pseudo Base64, see method
 * documentation for details) constructed from the node id, the current time
 * and a random string. This should guarantee unique ids across the cluster and
 * node restarts.
 * 
 * @author Bj�rn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public class RandomId {

    private static byte[] nodeId;
    private static SecureRandom random;

    static {

        /* Initiate the node identificator */
        String property = System.getProperty("no.feide.moria.store.randomid.nodeid");

        if (property == null || property.length() != 3) {
            throw new RuntimeException("no.feide.moria.store.randomid.nodeid is null or has illegal value");
        } else {
            nodeId = property.getBytes();
        }

        /* Initiate the random generator */
        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Unable to get instance of SecureRandom", e);
        }
    }

    /* The characters used in our version of base64 */
    private static final byte[] CHAR_64 =
        "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789*-".getBytes();

    /* The number of random bits to get from the PRNG */
    private static final int NO_OF_RANDOM_BITS = 384;

    public static String newId() {

        /* Get timestamp */
        byte[] now = longToByteArray(new Date().getTime());

        /* Round up number of bytes if the bits don't divide by eight */
        int noOfRandomBytes = NO_OF_RANDOM_BITS / 8;

        if ((NO_OF_RANDOM_BITS % 8) != 0)
            noOfRandomBytes++;

        /* Get the randomness */
        byte[] randomBytes = new byte[noOfRandomBytes];
        random.nextBytes(randomBytes);

        /* Build the complete id */
        byte[] id = new byte[nodeId.length + now.length + randomBytes.length];
        System.arraycopy(nodeId, 0, id, 0, nodeId.length);
        System.arraycopy(now, 0, id, nodeId.length, now.length);
        System.arraycopy(randomBytes, 0, id, nodeId.length + now.length, randomBytes.length);

        /* Encode and return the id */
        return pseudoBase64Encode(id);
    }

    /**
     * Takes a byte array and returns a string encoded with a slightly modified
     * version of Base64.
     * 
     * The difference compared to standard Base64 is that the extra two chars; + & /
     * have been exchanged for the more url-friendly - & *, and the resulting
     * string is not padded with = as required by the spec (rfc 2045).
     * 
     * Parts of code copyright (c) 2003, Sverre H. Huseby
     * &lt;shh@thathost.com&gt;
     * 
     * @param bytes the data to convert
     * @return the encoded version of the input
     */
    static String pseudoBase64Encode(byte[] bytes) {

        /* The final id string, initial size is 4/3 larger than input */
        StringBuffer finalId = new StringBuffer((bytes.length * 4) / 3);

        /* Holds the current element offset of the input byte array */
        int byteOffset;
        /* Holds the value of the current element of the input byte array */
        int currentByte;
        /* Used to extract values from the character array */
        int charOffset;

        /*
         * Three input bytes will give four output characters by grouping six
         * and six bits
         */
        for (byteOffset = 0;;) {

            /* Six first bits of the first of three bytes */
            if (byteOffset >= bytes.length)
                break;
            currentByte = ((int) bytes[byteOffset++]) & 255;
            charOffset = currentByte >> 2;
            finalId.append((char) CHAR_64[charOffset]);

            /* Two last bits of the first byte and four bits of the second byte */
            charOffset = (currentByte & 3) << 4;
            if (byteOffset < bytes.length) {
                currentByte = ((int) bytes[byteOffset++]) & 255;
                charOffset |= currentByte >> 4;
                finalId.append((char) CHAR_64[charOffset]);
            } else {
                finalId.append((char) CHAR_64[charOffset]);
                break;
            }

            /* Four last bits of the second byte and two of the third */
            charOffset = (currentByte & 15) << 2;
            if (byteOffset < bytes.length) {
                currentByte = ((int) bytes[byteOffset++]) & 255;
                charOffset |= currentByte >> 6;
                finalId.append((char) CHAR_64[charOffset]);
            } else {
                finalId.append((char) CHAR_64[charOffset]);
                break;
            }

            /* Last six bits of the third and final byte */
            charOffset = currentByte & 63;
            finalId.append((char) CHAR_64[charOffset]);
        }

        return finalId.toString();
    }

    /**
     * Takes a long value (64 bit) and returns it as an eight element byte
     * array.
     * 
     * @param in the long value to be converted
     * @return a byte array representation of the long value given as input
     */
    static byte[] longToByteArray(long in) {

        /* Java long is 64 bits, 8 byte */
        final int LONG_SIZE = 8;
        byte[] out = new byte[LONG_SIZE];

        /*
         * "And" the long value with 255 to effectively reduce it to a byte,
         * then cast. As we operate on the least significant bits we populate
         * the array backwards. Right shift eight bits so they may be
         * "extracted" next iteration.
         */
        for (int i = 7; i > -1; i--) {
            out[i] = (byte) (in & 0xffL);
            in >>= 8;
        }

        return out;
    }
}