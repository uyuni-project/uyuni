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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;

import com.suse.manager.errata.AmazonErrataParser;
import com.suse.manager.errata.ErrataParsingException;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.LocalDate;
import java.time.Month;

/**
 * Tests for {@link AmazonErrataParser}
 */
public class AmazonErrataParserTest extends BaseErrataTestCase {

    /**
     * Test to ensure correct parsing of the url and the id for Amazon Linux 2.
     */
    @Test
    public void testCanBuildValidLinkAndIdForAL2() throws ErrataParsingException {

        final AmazonErrataParser parser = new AmazonErrataParser();
        final Errata errata = createErrata("ALAS2-2021-1710", ErrataFactory.ERRATA_TYPE_SECURITY,
                LocalDate.of(2021, Month.SEPTEMBER, 30), 1L);

        final String id = parser.getAnnouncementId(errata);
        final URI uri = parser.getAdvisoryUri(errata);

        assertEquals("ALAS-2021-1710", id);
        assertEquals("https://alas.aws.amazon.com/AL2/ALAS-2021-1710.html", uri.toString());
    }

    /**
     * Test to ensure correct parsing of the url and the id for Amazon Linux 1.
     */
    @Test
    public void testCanBuildValidLinkAndIdForAL1() throws ErrataParsingException {

        final AmazonErrataParser parser = new AmazonErrataParser();
        final Errata errata = createErrata("ALAS-2021-1538", ErrataFactory.ERRATA_TYPE_SECURITY,
                LocalDate.of(2021, Month.SEPTEMBER, 30), 1L);

        final String id = parser.getAnnouncementId(errata);
        final URI uri = parser.getAdvisoryUri(errata);

        assertEquals("ALAS-2021-1538", id);
        assertEquals("https://alas.aws.amazon.com/ALAS-2021-1538.html", uri.toString());
    }

    /**
     * Test the behaviour when the advisory code is null.
     */
    @Test
    public void testThrowsExceptionWhenAdvisoryIsNull() {

        final AmazonErrataParser parser = new AmazonErrataParser();
        final Errata errata = createErrata("WRONGFORMAT123", ErrataFactory.ERRATA_TYPE_SECURITY,
                LocalDate.of(2012, Month.OCTOBER, 21), 1L);

        try {
            parser.getAdvisoryUri(errata);
            fail("Expected ErrataParsingException was not thrown");
        }
        catch (ErrataParsingException e) {
            assertEquals("Unsupported advisory format WRONGFORMAT123", e.getMessage());
        }
    }

    /**
     * Test the behaviour when the advisory code contains an invalid prefix
     */
    @Test
    public void testThrowsExceptionWhenPrefixIsUnknown() {

        final AmazonErrataParser parser = new AmazonErrataParser();
        final Errata errata = createErrata("WRONG-2012-1567", ErrataFactory.ERRATA_TYPE_SECURITY,
                LocalDate.of(2012, Month.OCTOBER, 21), 1L);

        try {
            parser.getAdvisoryUri(errata);
            fail("Expected ErrataParsingException was not thrown");
        }
        catch (ErrataParsingException e) {
            assertEquals("Unsupported advisory prefix WRONG", e.getMessage());
        }
    }

    /**
     * Test the behaviour when the advisory code format is wrong
     */
    @Test
    public void testThrowsExceptionWhenFormatIsUnknown() {

        final AmazonErrataParser parser = new AmazonErrataParser();
        final Errata errata = createErrata(null, ErrataFactory.ERRATA_TYPE_SECURITY,
                LocalDate.of(2012, Month.OCTOBER, 21), 1L);

        try {
            parser.getAdvisoryUri(errata);
            fail("Expected ErrataParsingException was not thrown");
        }
        catch (ErrataParsingException e) {
            assertEquals("No advisory id found for errata", e.getMessage());
        }
    }
}
