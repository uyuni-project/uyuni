/*
 * Copyright (c) 2009--2013 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.common.localization.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.localization.XmlMessages;
import com.redhat.rhn.testing.RhnBaseTestCase;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Locale;

/**
 * Test for {@link XmlMessages}.
 */
public class MessagesTest extends RhnBaseTestCase {
    private String getMessage;
    private String germanMessage;
    private String oneArg;
    private String twoArg;
    private String threeArg;
    private String quoteMsg;
    private String html;
    private Class clazz;
    private Locale locale;

    /**
     * sets up the test
     */
    @BeforeEach
    public void setUp() throws Exception {
        getMessage = "Get this";
        germanMessage = "Ich bin ein Berliner";
        oneArg = "one arg: fooboo";
        twoArg = "two arg: fooboo bubba";
        threeArg = "three arg: fooboo bubba booboo";
        quoteMsg = "You've got mail!";
        html = "<html><body>this is the body</body></html>";
        clazz = DummyClassForMessages.class;
        // Lame instantiation in order to
        // get JCoverage to shut up
        locale = new Locale("en", "US");
    }

    /*
     * Setup before each test.
     */
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
        getMessage = null;
        oneArg = null;
        twoArg = null;
        threeArg = null;
        quoteMsg = null;
        clazz = null;
        locale = null;
    }

    /**
     * test that it gets the right unformatted string
     */
    @Test
    public void testXmlGetString() {
        assertEquals(getMessage, XmlMessages.getInstance().getMessage(clazz, locale,
            "getMessage"));
    }

    /**
     * test that it gets the right unformatted string
     */
    @Test
    public void testXmlGetStringNoLocale() {
        assertEquals("some value", XmlMessages.getInstance().
            getMessage(clazz, null, "noLocale"));
    }


    /**
     * Test getting all the keys for the bundle
     */
    @Test
    public void testXmlGetKeys() {
        assertNotNull(XmlMessages.getInstance().getKeys(clazz, locale));
    }

    /**
     * test that it gets the right unformatted string
     */
    @Test
    public void testXmlGetGermanString() {

        String gmessage = XmlMessages.getInstance().getMessage(
            clazz, new Locale("de", "DE"), "getMessage");
        assertEquals(germanMessage, gmessage);
    }

    /**
     * Test that it formats a one-arg string correctly
     */
    @Test
    public void testXmlFormatOneArg() {
        assertEquals(oneArg, XmlMessages.getInstance().format(
            clazz, locale, "oneArg", "fooboo"));
    }
    /**
     * Test that it formats a two-arg string correctly
     */
    @Test
    public void testXmlFormatTwoArg() {
        assertEquals(twoArg, XmlMessages.getInstance().format(
            clazz, locale, "twoArg", "fooboo", "bubba"));
    }
    /**
     * Test that it formats a three-arg string correctly
     */
    @Test
    public void testXmlFormatThreeArg() {
        assertEquals(threeArg, XmlMessages.getInstance().format(
            clazz, locale, "threeArg", "fooboo", "bubba", "booboo"));
    }

    /**
     * Test that it escapes single quotes correctly.
     */
    @Test
    public void testXmlEscapeQuote() {
        String recieved = XmlMessages.getInstance().format(clazz, locale,
                "quotewitharg", "mail");
        assertEquals(quoteMsg, recieved);
    }

    /**
     * Test unescaping the HTML
     */
    @Test
    public void testUnescapeHtml4() {
        // htmltest
        String recieved = XmlMessages.getInstance().getMessage(clazz, locale,
            "htmltest");
        assertEquals(html, recieved);
    }

    /**
     * Make sure we fail if there's no resource bundle
     *
     */
    @Test
    public void testXmlNoResourceBundle() {
        try {
            XmlMessages.getInstance().format(
                String.class, locale, "bogus", "super-bogus");
            fail("Didn't get expected exception");
        }
        catch (java.util.MissingResourceException e) {
            //expected
        }
    }
}
