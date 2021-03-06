package no.feide.moria.servlet;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;

import javax.servlet.http.Cookie;
import java.util.ResourceBundle;
import java.util.MissingResourceException;
import java.util.Properties;
import java.util.TreeMap;
import java.io.IOException;


/**
 * Test case for the RequestUtil class.
 *
 * @author Lars Preben S. Arnesen &lt;lars.preben.arnesen@conduct.no&gt;
 * @version $Revision$
 */
public final class RequestUtilTest extends TestCase {

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
    public final void testGetCookieValue() {

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

        assertNull("Should be null",RequestUtil.getCookieValue("foo", null));

        /* Match */
        final String name = "name";
        final String value = "value";
        final Cookie[] cookies = new Cookie[]{new Cookie("foo", "bar"), new Cookie(name, value)};


        assertEquals("Should be equal input, normal use", value, RequestUtil.getCookieValue(name, cookies));
        assertEquals("Should be equal '', empty cookie", null, RequestUtil.getCookieValue(name, new Cookie[]{}));
        assertEquals("Should be equal '', wrong cookie", null, RequestUtil.getCookieValue("dontExist", cookies));
    }

    /**
     * Test the testCreateCookie method. Only test for illegal parameters are run,
     * due to the simplicity of the tested method.
     *
     * @see RequestUtil#createCookie(java.lang.String, java.lang.String, int)
     */
    public final void testCreateCookie() {

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

    }

    /**
     * Test getBundle method.
     *
     * @see RequestUtil#getBundle(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public final void testGetBundle() {
        ResourceBundle bundle;

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

        bundle = RequestUtil.getBundle("test", "", "bar", "", "", "nb");
        checkBundle("nb", bundle);

        bundle = RequestUtil.getBundle("test", "wrong", "wrong", "wrong", "wrong", "nb");
        checkBundle("nb", bundle);

        /* Browser specified language */
        final String acceptLang = "fo, nb;q=0.92, da;q=0.3, sv-se;q=0.81, sv;q=0.77";
        bundle = RequestUtil.getBundle("test", null, null, null, acceptLang, "wrong");
        checkBundle("nb", bundle);

        bundle = RequestUtil.getBundle("test", "", "bar", "", acceptLang, "wrong");
        checkBundle("nb", bundle);

        bundle = RequestUtil.getBundle("test", "wrong", "wrong", "wrong", acceptLang, "en");
        checkBundle("nb", bundle);

        /* Service specified language */
        bundle = RequestUtil.getBundle("test", null, null, "nb", null, "wrong");
        checkBundle("nb", bundle);

        bundle = RequestUtil.getBundle("test", "", "bar", "nb", "", "wrong");
        checkBundle("nb", bundle);

        bundle = RequestUtil.getBundle("test", "wrong", "wrong", "nb", "wrong", "wrong");
        checkBundle("nb", bundle);

        /* Cookie specified language */
        bundle = RequestUtil.getBundle("test", null, "nb", null, null, "wrong");
        checkBundle("nb", bundle);

        bundle = RequestUtil.getBundle("test", "", "nb", "", "", "wrong");
        checkBundle("nb", bundle);

        bundle = RequestUtil.getBundle("test", "wrong", "nb", "wrong", "wrong", "wrong");
        checkBundle("nb", bundle);

        /* URL parameter specified language */
        bundle = RequestUtil.getBundle("test", "nb", null, null, null, "wrong");
        checkBundle("nb", bundle);

        bundle = RequestUtil.getBundle("test", "nb", "bar", "", "", "wrong");
        checkBundle("nb", bundle);

        bundle = RequestUtil.getBundle("test", "nb", "wrong", "wrong", "wrong", "wrong");
        checkBundle("nb", bundle);


    }

    /**
     * Verify that the bundle has the correct language.
     *
     * @param language the expected language
     * @param bundle   the bundle to verify
     */
    private void checkBundle(final String language, final ResourceBundle bundle) {
        final String bundleLang = bundle.getLocale().getLanguage();
        final String bundleContentLang = (String) bundle.getObject("lang");
        assertEquals("Expected language differs from bundle content", language, bundleContentLang);
        assertEquals("Expected language differs from bundle language", language, bundleLang);
    };

    /**
     * Tests the sortedAcceptLang method.
     *
     * @see RequestUtil#sortedAcceptLang(java.lang.String)
     */
    public final void testSortedAcceptLang() {

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


        final String acceptLang = "en, sv;q=0.77, no;q=0.92, fo;err=0.88, da;q=0.3, no-nn;q=0.81";
        final String[] expectedLangList = new String[]{"en", "no", "nn", "sv", "da"};
        final String[] actualLangList = RequestUtil.sortedAcceptLang(acceptLang);

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

    /**
     * Test the organizationNames method.
     *
     * @throws IOException
     * @see RequestUtil#parseConfig(java.util.Properties, java.lang.String, java.lang.String)
     */
    public final void testParseConfig() throws IOException {
        Properties props = new Properties();
        props.load(this.getClass().getResourceAsStream("/web-test-valid.properties"));

        /* Illegal parameters */
        try {
            RequestUtil.parseConfig(null, "org", "en");
            fail("IllegalArgumentException should be raised, config is null");
        } catch (IllegalArgumentException success) {
        }
        try {
            RequestUtil.parseConfig(props, "org", null);
            fail("IllegalArgumentException should be raised, language is null");
        } catch (IllegalArgumentException success) {
        }
        try {
            RequestUtil.parseConfig(props, "org", "");
            fail("IllegalArgumentException should be raised, language is empty string");
        } catch (IllegalArgumentException success) {
        }
        try {
            RequestUtil.parseConfig(props, null, "en");
            fail("IllegalArgumentException should be raised, element is null");
        } catch (IllegalArgumentException success) {
        }
        try {
            RequestUtil.parseConfig(props, "", "en");
            fail("IllegalArgumentException should be raised, element is empty string");
        } catch (IllegalArgumentException success) {
        }

        /* No config */
        try {
            RequestUtil.parseConfig(new Properties(), "org", "en");
            fail("IllegalStateException should be raised, empty config.");
        } catch (IllegalStateException success) {
        }

        /* Invalid element */
        try {
            RequestUtil.parseConfig(new Properties(), "invalid", "en");
            fail("IllegalStateException should be raised, nonexisting element.");
        } catch (IllegalStateException success) {
        }

        /* Invalid element */
        try {
            RequestUtil.parseConfig(new Properties(), "org", "invalid");
            fail("IllegalStateException should be raised, nonexisting language.");
        } catch (IllegalStateException success) {
        }

        final TreeMap expected = new TreeMap();
        final TreeMap actual;
        expected.put("University of Oslo", "uio.no");
        expected.put("UNINETT", "uninett.no");
        expected.put("Oslo University College", "hio.no");
        expected.put("Finnmark University College", "hifm.no");

        /* Correct syntax */
        actual = RequestUtil.parseConfig(props, RequestUtil.PROP_ORG, "en");
        assertTrue("TreeMaps doesn't match. Might be mismatch between config file and test code.", expected.equals(actual));

        /* Wrong syntax "," */
        props = new Properties();
        props.load(this.getClass().getResourceAsStream("/web-test-invalid.properties"));
        try {
            RequestUtil.parseConfig(props, RequestUtil.PROP_ORG, "en");
            fail("Should raise IllegalStateException, config separation error ':'");
        } catch (IllegalStateException success) {
        }
        try {
            RequestUtil.parseConfig(props, RequestUtil.PROP_ORG, "en2");
            fail("Should raise IllegalStateException, config separation error ','");
        } catch (IllegalStateException success) {
        }
    }

    /**
     * Test the insertLink method.
     *
     * @see RequestUtil#insertLink(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public final void testInsertLink() {
        /* Illegal parameters */
        try {
            RequestUtil.insertLink(null, "data string", "Client", "http://moria.sf.net/");
            fail("IllegalArgumentException should be raised, token is null");
        } catch (IllegalArgumentException success) {
        }
        try {
            RequestUtil.insertLink("", "data string", "Client", "http://moria.sf.net/");
            fail("IllegalArgumentException should be raised, token is an empty string");
        } catch (IllegalArgumentException success) {
        }
        try {
            RequestUtil.insertLink("FOO", null, "Client", "http://moria.sf.net/");
            fail("IllegalArgumentException should be raised, data is null");
        } catch (IllegalArgumentException success) {
        }
        try {
            RequestUtil.insertLink("FOO", "", "Client", "http://moria.sf.net/");
            fail("IllegalArgumentException should be raised, data is an empty string");
        } catch (IllegalArgumentException success) {
        }
        try {
            RequestUtil.insertLink("FOO", "data string", null, "http://moria.sf.net/");
            fail("IllegalArgumentException should be raised, clientName is null");
        } catch (IllegalArgumentException success) {
        }
        try {
            RequestUtil.insertLink("FOO", "data string", "", "http://moria.sf.net/");
            fail("IllegalArgumentException should be raised, clientName is an empty string");
        } catch (IllegalArgumentException success) {
        }
        try {
            RequestUtil.insertLink("FOO", "data string", "Client", null);
            fail("IllegalArgumentException should be raised, clientURL is null");
        } catch (IllegalArgumentException success) {
        }
        try {
            RequestUtil.insertLink("FOO", "data string", "Client", "");
            fail("IllegalArgumentException should be raised, clientURL is an empty string");
        } catch (IllegalArgumentException success) {
        }

        final String link = "<a href=\"http://moria.sf.net/\">Client</a>";
        final String url = "http://moria.sf.net/";
        final String name = "Client";
        final String token = "CLIENT_LINK";
        String data;
        String expected;

        /* Token in the middle */
        data = "Foo CLIENT_LINK bar";
        expected = "Foo " + link + " bar";
        assertEquals("Hyperlink differs", expected, RequestUtil.insertLink(token, data, name, url));

        /* Token first */
        data = "CLIENT_LINK foobar";
        expected = link + " foobar";
        assertEquals("Hyperlink differs", expected, RequestUtil.insertLink(token, data, name, url));

        /* Token last */
        data = "Foobar CLIENT_LINK";
        expected = "Foobar " + link;
        assertEquals("Hyperlink differs", expected, RequestUtil.insertLink(token, data, name, url));

        /* Multiple tokens */
        data = "Foo CLIENT_LINK bar CLIENT_LINK foobar";
        expected = "Foo " + link + " bar " + link + " foobar";
        assertEquals("Hyperlink differs", expected, RequestUtil.insertLink(token, data, name, url));

        /* No tokens */
        data = "Foo bar";
        expected = data;
        assertEquals("Hyperlink differs", expected, RequestUtil.insertLink(token, data, name, url));
    }
}
