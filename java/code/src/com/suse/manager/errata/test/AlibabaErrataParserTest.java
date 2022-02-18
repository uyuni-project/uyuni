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

import com.suse.manager.errata.AlibabaErrataParser;
import com.suse.manager.errata.ErrataParsingException;

import java.net.URI;
import java.time.LocalDate;
import java.time.Month;

/**
 * Tests For {@link AlibabaErrataParser}
 */
public class AlibabaErrataParserTest extends BaseErrataTestCase {

    /**
     * Test to ensure correct parsing of the url and the id in a valid case.
     */
    public void testCanBuildValidLinkAndId() throws ErrataParsingException {

        final AlibabaErrataParser parser = new AlibabaErrataParser();
        final Errata errata = createErrata("ALINUX2-SA-2019:0019", ErrataFactory.ERRATA_TYPE_SECURITY,
                LocalDate.of(2019, Month.FEBRUARY, 8), 1L);

        final String id = parser.getAnnouncementId(errata);
        final URI uri = parser.getAdvisoryUri(errata);

        assertEquals("ALINUX2-SA-2019:0019", id);
        assertEquals("http://mirrors.aliyun.com/alinux/cve/alinux2-sa-20190019.xml", uri.toString());
    }

    /**
     * Test the behaviour when the advisory code is null.
     */
    public void testThrowsExceptionWhenAdvisoryIsNotAvailable() {
        final AlibabaErrataParser parser = new AlibabaErrataParser();
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
