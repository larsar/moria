package no.feide.moria.servlet;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import junit.framework.Assert;

import javax.servlet.http.HttpServletResponseWrapper;
import javax.servlet.http.Cookie;
import java.util.HashMap;
import java.util.ResourceBundle;
import java.util.MissingResourceException;


/**
 * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
 * @version $Revision$
 */
public class RequestUtilTest extends TestCase {

    /**
     * Initiate all tests.
     *
     * @return Junit test suite.
     */
    public static Test suite() {
        return new TestSuite(RequestUtilTest.class);
    }

    /**
     * Test the testCreateCookie method.
     *
     * @see RequestUtil#createCookie(java.lang.String, java.lang.String, int)
     */
    public void testGetCookieValue() {

        /* Illegal parameters */
        try {
            RequestUtil.getCookieValue(null, new Cookie[]{new Cookie("foo", "foobar")});
            fail("IllegalArgumentException should be raised, null value");
        } catch (IllegalArgumentException success) {
        }

        try {
            RequestUtil.getCookieValue("", new Cookie[]{new Cookie("foo", "foobar")});
            fail("IllegalArgumentException should be raised, empty string");
        } catch (IllegalArgumentException success) {
        }

        try {
            RequestUtil.getCookieValue("foo", null);
            fail("IllegalArgumentException should be raised, null value");
        } catch (IllegalArgumentException success) {
        }

        /* Match */
        String name = "name";
        String value = "value";
        Cookie[] cookies = new Cookie[]{new Cookie("foo", "bar"), new Cookie(name, value)};


        Assert.assertEquals("Should be equal input, normal use", value, RequestUtil.getCookieValue(name, cookies));
        Assert.assertEquals("Should be equal '', empty cookie", "", RequestUtil.getCookieValue(name, new Cookie[]{}));
        Assert.assertEquals("Should be equal '', wrong cookie", "", RequestUtil.getCookieValue("dontExist", cookies));
    }

    /**
     * Test the testCreateCookie method. Only test for illegal parameters are run,
     * due to the simplicity of the tested method.
     *
     * @see RequestUtil#createCookie(java.lang.String, java.lang.String, int)
     */
    public void testCreateCookie() {

        /* Illegal parameters */
        try {
            RequestUtil.createCookie(null, "bar", 0);
            fail("IllegalArgumentException should be raised, null value");
        } catch (IllegalArgumentException success) {
        }

        try {
            RequestUtil.createCookie("", "bar", 0);
            fail("IllegalArgumentException should be raised, empty string");
        } catch (IllegalArgumentException success) {
        }

        try {
            RequestUtil.createCookie("foo", null, 0);
            fail("IllegalArgumentException should be raised, null value");
        } catch (IllegalArgumentException success) {
        }

        try {
            RequestUtil.createCookie("foo", "", 0);
            fail("IllegalArgumentException should be raised, empty string");
        } catch (IllegalArgumentException success) {
        }

        try {
            RequestUtil.createCookie("foo", "bar", -1);
            fail("IllegalArgumentException should be raised, negative time to live");
        } catch (IllegalArgumentException success) {
        }

        try {
            RequestUtil.createCookie("foo", "bar", -10000);
            fail("IllegalArgumentException should be raised, negative time to live");
        } catch (IllegalArgumentException success) {
        }
    }

    /**
     * Test getBundle method.
     *
     * @see RequestUtil#getBundle(java.lang.String, java.lang.String, javax.servlet.http.Cookie[], java.lang.String, java.lang.String, java.lang.String)
     */
    public void testGetBundle() {
        ResourceBundle bundle;
        Cookie[] cookies;

        /* Illegal parameters */
        try {
            RequestUtil.getBundle(null, null, null, null, null, "en");
            fail("IllegalArgumentException should be raised, bundleName = null");
        } catch (IllegalArgumentException success) {
        }
        try {
            RequestUtil.getBundle("", null, null, null, null, "en");
            fail("IllegalArgumentException should be raised, bundleName = ''");
        } catch (IllegalArgumentException success) {
        }
        try {
            RequestUtil.getBundle("foobar", null, null, null, null, null);
            fail("IllegalArgumentException should be raised, moriaDefaultLang = null");
        } catch (IllegalArgumentException success) {
        }
        try {
            RequestUtil.getBundle("foobar", null, null, null, null, "");
            fail("IllegalArgumentException should be raised, moriaDefaultLang = ''");
        } catch (IllegalArgumentException success) {
        }

        /* Nonexisting bundle */
        try {
            RequestUtil.getBundle("doesNotExist", null, null, null, null, "nb");
            fail("MissingResourceException should be raised, no such bundle.");
        } catch (MissingResourceException success) {
        }

        /* Nonexisting language */
        try {
            RequestUtil.getBundle("test", null, null, null, null, "wrong");
            fail("MissingResourceException should be raised, no such language");
        } catch (MissingResourceException success) {
        }

        /* Moria default */
        bundle = RequestUtil.getBundle("test", null, null, null, null, "nb");
        checkBundle("nb", bundle);

        cookies = new Cookie[]{new Cookie("foo", "bar")};
        bundle = RequestUtil.getBundle("test", "", cookies, "", "", "nb");
        checkBundle("nb", bundle);

        cookies = new Cookie[]{new Cookie("lang", "wrong")};
        bundle = RequestUtil.getBundle("test", "wrong", cookies, "wrong", "wrong", "nb");
        checkBundle("nb", bundle);

        /* Browser specified language */
        String acceptLang = "fo, nb;q=0.92, da;q=0.3, sv-se;q=0.81, sv;q=0.77";
        bundle = RequestUtil.getBundle("test", null, null, null, acceptLang, "wrong");
        checkBundle("nb", bundle);

        cookies = new Cookie[]{new Cookie("foo", "bar")};
        bundle = RequestUtil.getBundle("test", "", cookies, "", acceptLang, "wrong");
        checkBundle("nb", bundle);

        cookies = new Cookie[]{new Cookie("lang", "wrong")};
        bundle = RequestUtil.getBundle("test", "wrong", cookies, "wrong", acceptLang, "en");
        checkBundle("nb", bundle);

        /* Service specified language */
        bundle = RequestUtil.getBundle("test", null, null, "nb", null, "wrong");
        checkBundle("nb", bundle);

        cookies = new Cookie[]{new Cookie("foo", "bar")};
        bundle = RequestUtil.getBundle("test", "", cookies, "nb", "", "wrong");
        checkBundle("nb", bundle);

        cookies = new Cookie[]{new Cookie("lang", "wrong")};
        bundle = RequestUtil.getBundle("test", "wrong", cookies, "nb", "wrong", "wrong");
        checkBundle("nb", bundle);

        /* Cookie specified language */
        cookies = new Cookie[]{new Cookie("lang", "nb")};
        bundle = RequestUtil.getBundle("test", null, cookies, null, null, "wrong");
        checkBundle("nb", bundle);

        bundle = RequestUtil.getBundle("test", "", cookies, "", "", "wrong");
        checkBundle("nb", bundle);

        bundle = RequestUtil.getBundle("test", "wrong", cookies, "wrong", "wrong", "wrong");
        checkBundle("nb", bundle);

        /* URL parameter specified language */
        bundle = RequestUtil.getBundle("test", "nb", null, null, null, "wrong");
        checkBundle("nb", bundle);

        cookies = new Cookie[]{new Cookie("foo", "bar")};
        bundle = RequestUtil.getBundle("test", "nb", cookies, "", "", "wrong");
        checkBundle("nb", bundle);

        cookies = new Cookie[]{new Cookie("lang", "wrong")};
        bundle = RequestUtil.getBundle("test", "nb", cookies, "wrong", "wrong", "wrong");
        checkBundle("nb", bundle);


    }

    /**
     * Verify that the bundle has the correct language.
     *
     * @param language the expected language
     * @param bundle   the bundle to verify
     */
    private void checkBundle(String language, ResourceBundle bundle) {
        String bundleLang = bundle.getLocale().getLanguage();
        String bundleContentLang = (String) bundle.getObject("lang");
        Assert.assertEquals("Expected language differs from bundle content", language, bundleContentLang);
        Assert.assertEquals("Expected language differs from bundle language", language, bundleLang);
    };

    /**
     * Tests the sortedAcceptLang method.
     *
     * @see RequestUtil#sortedAcceptLang(java.lang.String)
     */
    public void testSortedAcceptLang() {

        /* Illegal arguments */
        try {
            RequestUtil.sortedAcceptLang(null);
            fail("IllegalArgumentException should be raised, null value as parameter.");
        } catch (IllegalArgumentException success) {
        }
        try {
            RequestUtil.sortedAcceptLang("");
            fail("IllegalArgumentException should be raised, empty string as parameter.");
        } catch (IllegalArgumentException success) {
        }


        String acceptLang = "en, sv;q=0.77, no;q=0.92, fo;err=0.88, da;q=0.3, no-nn;q=0.81";
        String[] expectedLangList = new String[]{"en", "no", "nn", "sv", "da"};
        String[] actualLangList = RequestUtil.sortedAcceptLang(acceptLang);

        /* Check every element in the lists */
        if (expectedLangList.length != actualLangList.length) {
            fail("Length of lists differs.");
        } else {
            for (int i = 0; i < expectedLangList.length; i++) {
                if (!expectedLangList[i].equals(actualLangList[i])) {
                    fail("Element " + i + " differs.");
                }
            }
        }
    }
}
