/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.frontend.context.Context;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Test for {@link LocalizationService}.
 */

public class LocalizationServiceTest extends RhnBaseTestCase {

    private LocalizationService ls;

    /**
     * sets up the test
     */
    @BeforeEach
    public void setUp() throws Exception {
        ls = LocalizationService.getInstance();
        TestUtils.disableLocalizationLogging();
    }

    /**
     * test that makes sure we can instantiate the service
     */
    @Test
    public void testGetInstance() {
        assertNotNull(ls, "LocalizationService is null");

    }

    /**
     * {@inheritDoc}
     */
    @AfterEach
    public void tearDown() throws Exception {
        TestUtils.enableLocalizationLogging();
        super.tearDown();
    }


    /**
     * test a standard non-parameterized message call
     */
    @Test
    public void testGetMessageNoParams() {
        String received = ls.getMessage("testMessage");
        assertTrue(isMessageValid(received), "Message not valid");
        String expected = "this is a test of the emergency broadcast";
        assertEquals(expected, received);
    }

    /**
     * test a standard non-parameterized message call
     */
    @Test
    public void testGetMessageOneParams() {
        String expected = "this is a test of the LocalizationService";
        String received = ls.getMessage("testMessage.oneparam", "LocalizationService");
        assertEquals(expected, received);
    }

    /**
     * test a standard non-parameterized message call
     */
    @Test
    public void testGetMessagesMultipleFiles() {
        assertTrue(isMessageValid(ls.getMessage("testMessage")), "Message not valid");
        assertTrue(isMessageValid(ls.getMessage("jsp.testMessage")), "Message not valid");
    }

    /** Test forcing a call with a null locale
    */
    @Test
    public void testGetMessageNoLocale() {
        String message = ls.getMessage("testMessage", (Locale) null);
        assertTrue(isMessageValid(message));
    }

    /** Test calling with the parent of en_US, en
    */
    @Test
    public void testGetMessageNonDefaultLocale() {
        String message = ls.getMessage("testMessage", new Locale("en"));
        assertTrue(isMessageValid(message));
        message = ls.getMessage("testMessage", Locale.GERMAN);
        assertTrue(isMessageValid(message));
    }

    @Test
    public void testGetMessages() {
        String[] keys = new String[3];
        keys[0] = "testMessage";
        keys[1] = "testMessage";
        keys[2] = "testMessage";
        String[] l10ned = ls.getMessages(keys);
        for (int i = 0; i < keys.length; i++) {
            assertTrue(isMessageValid(l10ned[i]));
        }
    }

    @Test
    public void testHasMessage() {
        assertFalse(ls.hasMessage("somefakemessage" + TestUtils.randomString()));
    }


    /**
    * Test getting debug message
    */
    @Test
    public void testGetDebugMessage() {
        assertTrue(isMessageValid(ls.getDebugMessage("testMessage")), "Message not valid");
    }

    /**
     * test a standard non-parameterized message with
     * spaces in the key.  Currently this doesn't work.
     */
    @Test
    public void testGetMessageWithSpacesInKey() {
        assertTrue(isMessageValid(ls.getMessage("cant have spaces")), "Message not valid");
    }

    /**
     * test to exercise looking up an invalid message
     */
    @Test
    public void testGetInvalidMessage() {
        assertFalse(isMessageValid(ls.getMessage("no message with this key")),
                      "Didn't fetch an invalid message (we want to, in this test)");
        // java.l10n_missingmessage_exceptions
        boolean orig = Config.get().getBoolean("java.l10n_missingmessage_exceptions");
        Config.get().setBoolean("java.l10n_missingmessage_exceptions", "true");

        boolean caught = false;
        try {
            ls.getMessage("no message with this key");
        }
        catch (IllegalArgumentException iae) {
            caught = true;
        }
        assertTrue(caught);
        Config.get().setBoolean("java.l10n_missingmessage_exceptions",
                Boolean.toString(orig));

    }


    /**
    * Test formatDate
    * @throws Exception something bad happened
    */
    @Test
    public void testFormatDate() throws Exception {
        String date = ls.formatDate(new Date(), Locale.GERMAN);
        assertNotNull(date);
        // Make sure we translated it to a german date
        // German dates have commas (EN_US ones dont):
        // Date 13. Juli 2004 17:51:52 PDT
        assertTrue(date.indexOf('.') > 0);
        // check getBasicDate
        assertNotNull(ls.getBasicDate());

        // Here we test converting a Pacific Standard Time date from the RHN
        // Database format to the standard Java format + timezone in GMT.
        Date dt = new SimpleDateFormat(LocalizationService.RHN_DB_DATEFORMAT +
                " z").parse("2004-12-10 13:20:00 PST");

        DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
        df.setTimeZone(TimeZone.getTimeZone("GMT"));

        Context ctx = Context.getCurrentContext();
        ctx.setTimezone(TimeZone.getTimeZone("GMT"));
        String gmtDate = ls.formatDate(dt, Locale.ENGLISH);
        String expected = "12/10/04 9:20:00 PM GMT";
        assertEquals(expected, gmtDate);

        String shortGmtDate = ls.formatShortDate(dt, Locale.ENGLISH);
        expected = "12/10/04";
        assertEquals(expected, shortGmtDate);

        // Now test formatting it to German format in a DE TimeZone.
        ctx.setTimezone(TimeZone.getTimeZone("Europe/Paris"));
        String deDate = ls.formatDate(dt, Locale.GERMAN);
        expected = "10\\.12\\.04 22:20:00 (CET|MEZ)";
        assertTrue(deDate.matches(expected));

        String shortDeDate = ls.formatShortDate(dt, Locale.GERMAN);
        expected = "10.12.04";
        assertEquals(expected, shortDeDate);
    }


    /**
    * Test formatNumber
    */
    @Test
    public void testFormatNumber() {
        String number = ls.formatNumber(10000L, Locale.GERMAN);
        assertNotNull(number);
        // Make sure we translated it to a german number
        // German numbers have commas (EN_US ones dont):

        assertTrue(number.indexOf('.') > 0);

        number = ls.formatNumber(10, Locale.ENGLISH, 2);
        assertNotNull(number);
        assertEquals(3, number.length() - number.indexOf("."));
    }


    /**
    * Test the alphabet and digit functions
    */
    @Test
    public void testGetAlphabet() {
        assertTrue(ls.getAlphabet().contains("A"));
        assertTrue(ls.getAlphabet().contains("Z"));
    }

    /**
    * Test the alphabet and digit functions
    */
    @Test
    public void testGetDigits() {
        assertTrue(ls.getDigits().contains("1"));
        assertTrue(ls.getDigits().contains("5"));
        assertTrue(ls.getDigits().contains("0"));
    }

    /**
    * Test the prefix and country functions
    */
    @Test
    public void testGetCountriesPrefixes() {
        assertNotNull(ls.availableCountries().get("Peru"));
        assertTrue(ls.availablePrefixes().size() > 0);
    }

    /** Test to make sure debug mode works
     */
    @Test
    public void testDebugMessage() {
        TestUtils.enableLocalizationDebugMode();
        String received = ls.getMessage("testMessage");
        String marker = Config.get().getString("java.l10n_debug_marker", "$$$");
        assertTrue(received.startsWith(marker));
        assertTrue(received.endsWith(marker));
        // Reset it back
        TestUtils.disableLocalizationDebugMode();
    }

    @Test
    public void testSupportedLocales() {
        List locales = ls.getSupportedLocales();
        assertTrue(locales.size() > 0);
        assertTrue(ls.isLocaleSupported(Locale.US));
        assertTrue(ls.isLocaleSupported(Locale.TAIWAN));
    }

    @Test
    public void testNullMessage() {
        String nullmsg = ls.getMessage(null);
        assertNotNull(nullmsg);
    }


    /**
    * check to see if the fetched message is valid or not
    */
    private boolean isMessageValid(String value) {
        return !value.startsWith("**") || !value.endsWith("**");
    }

    @Test
    public void testPlainText() {
        String expected = "You do not have enough entitlements for" +
                                    " 5 systems (http://www.redhat.com).";
        String actual = ls.getPlainText("testMessage.html", 5);
        assertEquals(expected, actual);
    }
}
