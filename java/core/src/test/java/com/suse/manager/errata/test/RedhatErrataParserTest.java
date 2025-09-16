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

import com.suse.manager.errata.ErrataParsingException;
import com.suse.manager.errata.RedhatErrataParser;

import org.junit.jupiter.api.Test;

import java.net.URI;
import java.time.LocalDate;
import java.time.Month;

/**
 * Tests for {@link RedhatErrataParser}
 */
public class RedhatErrataParserTest extends BaseErrataTestCase {

    /**
     * Test to ensure correct parsing of the url and the id in a valid case.
     */
    @Test
    public void testCanBuildValidLinkAndId() throws ErrataParsingException {

        final RedhatErrataParser parser = new RedhatErrataParser();
        final Errata errata = createErrata("RHBA-2021:3831", ErrataFactory.ERRATA_TYPE_BUG,
                LocalDate.of(2021, Month.OCTOBER, 20), 1L);

        final String id = parser.getAnnouncementId(errata);
        final URI uri = parser.getAdvisoryUri(errata);

        assertEquals("RHBA-2021:3831", id);
        assertEquals("https://access.redhat.com/errata/RHBA-2021:3831", uri.toString());
    }

    /**
     * Test the behaviour when the advisory code is null.
     */
    @Test
    public void testThrowsExceptionWhenAdvisoryIsNotAvailable() {
        final RedhatErrataParser parser = new RedhatErrataParser();
        final Errata errata = createErrata(null, ErrataFactory.ERRATA_TYPE_SECURITY,
                LocalDate.of(2019, Month.FEBRUARY, 8), 1L);

        try {
            parser.getAdvisoryUri(errata);
            fail("Expected ErrataParsingException was not thrown");
        }
        catch (ErrataParsingException e) {
            assertEquals("No advisory id found for errata", e.getMessage());
        }
    }
}
