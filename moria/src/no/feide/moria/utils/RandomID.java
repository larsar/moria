package no.feide.moria.utils;

import java.security.SecureRandom;

/**
 * A generator for cryptographically strong random identifiers.  Uses
 * Java's <code>java.security.SecureRandom</code> to provide
 * randomness.
 *
 * @author   Sverre H. Huseby
 *           &lt;<a href="mailto:shh@thathost.com">shh@thathost.com</a>&gt;
 * @version  $Id$
 */
public final class RandomID {
/*-----------------------------------------------------------------------+
|  PRIVATE PART                                                          |
+-----------------------------------------------------------------------*/
    private static SecureRandom random = null;
    private static final byte[] CHAR_64
        = ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
           + "0123456789+/").getBytes();
    private static final char PAD = '*';

    /**
     * Encodes a byte array according to the rules of BASE64.
     *
     * @param      a the array to encode
     * @param      len the number of bytes to encode
     * @return     a <code>String</code> string containing the BASE64
     *             version of the array
     * @author     Sverre H. Huseby
     */
    private static String b64(byte[] a, int len) {
        int b, x, idx;
        StringBuffer sb;

        sb = new StringBuffer(5 + (len * 4) / 3);
        /* three input bytes will give four output characters by
         * grouping six by six bits. */
        for (idx = 0; ; ) {
            /* part of the first of three bytes */
            if (idx >= len)
                break;
            b = a[idx++] & 255;
            x = b >> 2;
            sb.append((char) CHAR_64[x]);

            /* part of the first and part of the second of three bytes */
            x = (b & 3) << 4;
            if (idx < len) {
                b = a[idx++] & 255;
                x |= b >> 4;
                sb.append((char) CHAR_64[x]);
            } else {
                sb.append((char) CHAR_64[x]);
                sb.append(PAD);
                sb.append(PAD);
                break;
            }

            /* part of the second and part of the third of three bytes */ 
            x = (b & 15) << 2;
            if (idx < len) {
                b = a[idx++] & 255;
                x |= b >> 6;
                sb.append((char) CHAR_64[x]);
            } else {
                sb.append((char) CHAR_64[x]);
                sb.append(PAD);
                break;
            }

            /* part of the third of three bytes */
            x = b & 63;
            sb.append((char) CHAR_64[x]);
        }
        return sb.toString();
    }


    /**
     * Initializes this class by fetching an instance of the SHA1PRNG
     * random number generator.  May throw a
     * <code>RuntimeException</code> in the unlikely event that this
     * type of generator is not available.
     */
    private static void init() {
        if (random != null)
            return;

        try {
            random = SecureRandom.getInstance("SHA1PRNG");
        } catch (Exception e) {
            throw new RuntimeException(
                                "unable to get instance of SecureRandom");
        }
    }

/*-----------------------------------------------------------------------+
|  PUBLIC INTERFACE                                                      |
+-----------------------------------------------------------------------*/
    /**
     * Specifies a pseudo-random number generator to use when
     * generating the IDs.  If this method is not called, the class
     * will use SHA1PRNG.
     *
     * @param      prng the random number generator to use.
     * @author     Sverre H. Huseby
     */
    public static void setRandomGenerator(SecureRandom prng) {
        random = prng;
    }

    /**
     * Generates a new ID.  The ID is a <code>String</code> of
     * characters A-Z, a-z, 0-9 and + (plus), / (slash) and =
     * (equals).  The string matches the BASE64 representation of the
     * given number of bits.
     *
     * @param      minbits minimum number of random bits to base the
     *                 ID on.  the number will be rounded up to the
     *                 nearest multiple of 8.
     * @return     a string containing the BASE64 representation of
     *             the random sequence of bits.
     * @author     Sverre H. Huseby
     */
    public static String generateID(int minbits) {
        int numbytes;

        init();

        numbytes = minbits / 8;
        if ((minbits % 8) != 0)
            ++numbytes;
        byte[] bytes = new byte[numbytes];
        random.nextBytes(bytes);
        return b64(bytes, bytes.length);
    }

    /**
     * Generates a new ID.  The ID is a <code>String</code> of
     * characters A-Z, a-z, 0-9 and + (plus), / (slash) and =
     * (equals).  The string matches the BASE64 representation of the
     * given number of bits.  The ID will be generated from 144 random
     * bits, giving a string of 24 characters.
     *
     * @param      minbits minimum number of random bits to base the
     *                 ID on.  the number will be rounded up to the
     *                 nearest multiple of 8.
     * @return     a string containing the BASE64 representation of
     *             the random sequence of bits.
     * @author     Sverre H. Huseby
     */
    public static String generateID() {
        return generateID(144);
    }
}
