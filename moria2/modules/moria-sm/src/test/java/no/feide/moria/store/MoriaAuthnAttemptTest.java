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

import java.util.HashMap;

/**
 * Test class for MoriaAuthnAttempt
 *
 * @author Bj&oslash;rn Ola Smievoll &lt;b.o@smievoll.no&gt;
 * @version $Revision$
 */
public class MoriaAuthnAttemptTest extends TestCase {

    public static Test suite() {
        return new TestSuite(MoriaAuthnAttemptTest.class);
    }

    /**
     * Since this class is a really simple data container we only do one simple
     * test
     *
     */
    public void testConstructor() {
        String[] attributes = { "foo", "bar" };
        String prefix = "http://example.org/?MoriaId=";
        String postfix = "";
        String servicePrincipal = "servicePrincipal";
        boolean forceAuthn = false;
        HashMap transientAttributes = new HashMap();

        transientAttributes.put("foo", new String[]{"bar"});
        transientAttributes.put("bar", new String[]{"foo"});

        MoriaAuthnAttempt authnAttempt = new MoriaAuthnAttempt(attributes, prefix, postfix, forceAuthn, servicePrincipal);
        assertNotNull("Object creation failed", authnAttempt);

        authnAttempt.setTransientAttributes(transientAttributes);

        assertTrue("Attributes do not match", compareStringArrays(attributes, authnAttempt.getRequestedAttributes()));

        assertEquals("URL prefix does not match", prefix, authnAttempt.getReturnURLPrefix());
        assertEquals("URL postfix does not match", postfix, authnAttempt.getReturnURLPostfix());
        assertEquals("Force authentication does not match", forceAuthn, authnAttempt.isForceInterativeAuthentication());
        assertEquals("Transient attributes does not match", transientAttributes, authnAttempt.getTransientAttributes());
        assertEquals("Principal does not match", servicePrincipal, authnAttempt.getServicePrincipal());
    }

    /**
     * Compares the content of two string arrays.
     *
     * @param arr1  array of strings to compare
     * @param arr2  array of strings to compare
     * @return true if the two arrays are identical
     */
    private boolean compareStringArrays(String[] arr1, String[] arr2) {
        boolean equals = true;
        if (arr1.length == arr2.length) {
            for (int i = 0; i < arr1.length; i++) {
                if (!arr1[i].equals(arr2[i])) {
                    System.out.println("Strings differ: '"+arr1[i]+"' and '"+arr2[i]+"'");
                    equals = false;
                    break;
                }
            }
        } else {
            equals = false;
        }
        return equals;
    }

}
