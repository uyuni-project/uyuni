/*
 * Copyright (c) 2021 SUSE LLC
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
package com.suse.manager.errata.test;

import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;

import com.suse.manager.errata.ErrataParsingException;
import com.suse.manager.errata.SUSEErrataParser;

import java.net.URI;
import java.time.LocalDate;
import java.time.Month;

/**
 * Tests class for {@link SUSEErrataParser}
 */
public class SUSEErrataParserTest extends BaseErrataTestCase {

    /**
     * Test to ensure correct parsing of the url and the id in a valid case.
     */
    public void testCanBuildValidLinkAndId() throws ErrataParsingException {

        final SUSEErrataParser parser = new SUSEErrataParser();
        final Errata errata = createErrata("avahi-13947", ErrataFactory.ERRATA_TYPE_SECURITY,
                LocalDate.of(2019, Month.FEBRUARY, 8), 1L);

        final String id = parser.getAnnouncementId(errata);
        final URI uri = parser.getAdvisoryUri(errata);

        assertEquals("SUSE-SU-2019:13947-1", id);
        assertEquals("https://www.suse.com/support/update/announcement/2019/suse-su-201913947-1/", uri.toString());
    }

    /**
     * Test to verify the required leading zeros are added
     */
    public void testAddsLeadingZeroWhenIdIsLessThanFourDigits() throws ErrataParsingException {

        final SUSEErrataParser parser = new SUSEErrataParser();
        final Errata errata = createErrata("SUSE-15-2020-683", ErrataFactory.ERRATA_TYPE_BUG,
                LocalDate.of(2020, Month.MARCH, 13), 1L);

        final String id = parser.getAnnouncementId(errata);
        final URI uri = parser.getAdvisoryUri(errata);

        assertEquals("SUSE-RU-2020:0683-1", id);
        assertEquals("https://www.suse.com/support/update/announcement/2020/suse-ru-20200683-1/", uri.toString());
    }

    /**
     * Test the behaviour when the issue date is null.
     */
    public void testThrowsExceptionWhenIssueDateIsNull() {

        final SUSEErrataParser parser = new SUSEErrataParser();
        final Errata errata = createErrata("SUSE-15-2020-683", ErrataFactory.ERRATA_TYPE_BUG, null, 1L);

        try {
            parser.getAdvisoryUri(errata);
            fail("Expected ErrataParsingException was not thrown");
        }
        catch (ErrataParsingException e) {
            assertEquals("Issue date is null", e.getMessage());
        }
    }

    /**
     * Test the behaviour when the advisory id is not a valid number
     */
    public void testThrowsExceptionWhenUnableToParseAdvisoryId() {

        final SUSEErrataParser parser = new SUSEErrataParser();
        final Errata errata = createErrata("wrong-advisory-format", ErrataFactory.ERRATA_TYPE_BUG,
                LocalDate.of(2020, Month.MARCH, 13), 1L);

        try {
            parser.getAdvisoryUri(errata);
            fail("Expected ErrataParsingException was not thrown");
        }
        catch (ErrataParsingException e) {
            assertEquals("Unable to parse the advisory id number from wrong-advisory-format", e.getMessage());
        }
    }

    /**
     * Test the behaviour when the format of the advisory code is not expected.
     */
    public void testThrowsExceptionWhenAdvisoryIsNotInTheExpectedFormat() {

        final SUSEErrataParser parser = new SUSEErrataParser();
        final Errata errata = createErrata("invalid", ErrataFactory.ERRATA_TYPE_BUG,
                LocalDate.of(2020, Month.MARCH, 13), 1L);

        try {
            parser.getAdvisoryUri(errata);
            fail("Expected ErrataParsingException was not thrown");
        }
        catch (ErrataParsingException e) {
            assertEquals("Unable to parse advisory id from invalid", e.getMessage());
        }
    }

    /**
     * Test the behaviour when the release number is not valid
     */
    public void testThrowsExceptionWhenReleaseIsInvalid() {

        final SUSEErrataParser parser = new SUSEErrataParser();
        final Errata errata = createErrata("SUSE-15-2020-683", ErrataFactory.ERRATA_TYPE_BUG,
                LocalDate.of(2020, Month.MARCH, 13), 0L);

        try {
            parser.getAdvisoryUri(errata);
            fail("Expected ErrataParsingException was not thrown");
        }
        catch (ErrataParsingException e) {
            assertEquals("Invalid advisory release number 0", e.getMessage());
        }
    }

    /**
     * Test the behaviour when the type of advisory is not valid.
     */
    public void testThrowsExceptionWhenTypeIsInvalid() {

        final SUSEErrataParser parser = new SUSEErrataParser();
        final Errata errata = createErrata("SUSE-15-2020-683", "invalidType",
                LocalDate.of(2020, Month.MARCH, 13), 1L);

        try {
            parser.getAdvisoryUri(errata);
            fail("Expected ErrataParsingException was not thrown");
        }
        catch (ErrataParsingException e) {
            assertEquals("Unsupported advisory type invalidType", e.getMessage());
        }
    }

    /**
     * Test the behaviour when the type of advisory is null.
     */
    public void testThrowsExceptionWhenTypeIsNull() {

        final SUSEErrataParser parser = new SUSEErrataParser();
        final Errata errata = createErrata("SUSE-15-2020-683", null,
                LocalDate.of(2020, Month.MARCH, 13), 1L);

        try {
            parser.getAdvisoryUri(errata);
            fail("Expected ErrataParsingException was not thrown");
        }
        catch (ErrataParsingException e) {
            assertEquals("Advisory type is null", e.getMessage());
        }
    }

    /**
     * Test the behaviour when the advisory is too old to correctly generate a link
     */
    public void testCanParseFirstValidIssueDate() throws ErrataParsingException {

        final SUSEErrataParser parser = new SUSEErrataParser();
        final Errata errata = createErrata("SUSE-2019-0002", ErrataFactory.ERRATA_TYPE_SECURITY,
                LocalDate.of(2019, Month.JANUARY, 1), 1L);

        final String id = parser.getAnnouncementId(errata);
        final URI uri = parser.getAdvisoryUri(errata);

        assertEquals("SUSE-SU-2019:0002-1", id);
        assertEquals("https://www.suse.com/support/update/announcement/2019/suse-su-20190002-1/", uri.toString());

    }

    /**
     * Test the behaviour when the advisory is too old to correctly generate a link
     */
    public void testThrowsExceptionWhenAdvisoryIsTooOld() {

        final SUSEErrataParser parser = new SUSEErrataParser();
        final Errata errata = createErrata("salt-201811-13898", ErrataFactory.ERRATA_TYPE_SECURITY,
                LocalDate.of(2018, Month.NOVEMBER, 26), 1L);

        try {
            parser.getAdvisoryUri(errata);
            fail("Expected ErrataParsingException was not thrown");
        }
        catch (ErrataParsingException e) {
            assertEquals("Unable to parse an advisory issued before 2019", e.getMessage());
        }
    }

}
