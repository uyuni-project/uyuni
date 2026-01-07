/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.errata.test;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;

import com.suse.manager.errata.ErrataParsingException;
import com.suse.manager.errata.SUSEAdvisoryMapErrataParser;
import com.suse.manager.errata.advisorymap.ErrataAdvisoryMapManager;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.Month;

public class SUSEAdvisoryMapErrataParserTest extends BaseErrataTestCase {

    public static void createTestAdvisoryMapDatabase() throws IOException {

        String testAdvisoryMapCsv =
            """
            # patchid, notice id, url
            SUSE-SLE-Live-Patching-12-SP5-2025-1901,SUSE-SU-2025:01901-1,\
            https://www.suse.com/support/update/announcement/2025/suse-su-202501901-1/
            SUSE-SLE-Module-Basesystem-15-SP7-2025-1638,SUSE-SU-2025:01638-2,\
            https://www.suse.com/support/update/announcement/2025/suse-su-202501638-2/
            SUSE-Manager-Tools-For-SL-Micro-6-5,SUSE-RU-2025:20291-1,\
            https://www.suse.com/support/update/announcement/2025/suse-ru-202520291-1/
            SUSE-SLE-Module-Live-Patching-15-SP3-2025-1112,SUSE-SU-2025:1119-1,\
            https://www.suse.com/support/update/announcement/2025/suse-su-20251119-1/
            SUSE-SLE-Module-Basesystem-15-SP6-2025-1562,SUSE-OU-2025:1562-1,\
            https://www.suse.com/support/update/announcement/2025/suse-ou-20251562-1/
            SUSE-SLE-Module-Packagehub-Subpackages-15-SP6-2025-37,SUSE-FU-2025:0037-1,\
            https://www.suse.com/support/update/announcement/2025/suse-fu-20250037-1/
            SUSE-SLE-Manager-Tools-For-Micro-5-2025-1319,SUSE-RU-2025:1319-1,\
            https://www.suse.com/support/update/announcement/2025/suse-ru-20251319-1/
            SUSE-SLE-Module-Development-Tools-15-SP6-2025-990,SUSE-RU-2025:0990-1,\
            https://www.suse.com/support/update/announcement/2025/suse-ru-20250990-1/
            """;

        InputStream inputStream = new ByteArrayInputStream(testAdvisoryMapCsv.getBytes(StandardCharsets.UTF_8));
        ErrataAdvisoryMapManager amm = new ErrataAdvisoryMapManager();
        amm.readAdvisoryMapPopulateDatabase(inputStream);
    }

    private Errata createTestErrata(String updateTag, String advisory) {
        return createErrataWithUpdateTag(updateTag, advisory, ErrataFactory.ERRATA_TYPE_SECURITY,
                LocalDate.of(2025, Month.FEBRUARY, 8), 1L);
    }

     /**
     * Test to ensure correct parsing of the url and the id in a valid case.
     */
    @Test
    public void testCanBuildValidLinkAndId() throws ErrataParsingException, IOException {
        createTestAdvisoryMapDatabase();
        final SUSEAdvisoryMapErrataParser parser = new SUSEAdvisoryMapErrataParser();


        Errata errata = createTestErrata("SLE-Live-Patching", "SUSE-12-SP5-2025-1901");
        assertTrue(parser.getAnnouncementId(errata).equals("SUSE-SU-2025:01901-1"));
        assertTrue(parser.getAdvisoryUri(errata).toString()
                .equals("https://www.suse.com/support/update/announcement/2025/suse-su-202501901-1/"));

        errata = createTestErrata("SLE-Module-Basesystem", "SUSE-15-SP7-2025-1638");
        assertTrue(parser.getAnnouncementId(errata).equals("SUSE-SU-2025:01638-2"));
        assertTrue(parser.getAdvisoryUri(errata).toString()
                .equals("https://www.suse.com/support/update/announcement/2025/suse-su-202501638-2/"));


        errata = createTestErrata("Manager-Tools-For", "SUSE-SL-Micro-6-5");
        assertTrue(parser.getAnnouncementId(errata).equals("SUSE-RU-2025:20291-1"));
        assertTrue(parser.getAdvisoryUri(errata).toString()
                .equals("https://www.suse.com/support/update/announcement/2025/suse-ru-202520291-1/"));

        errata = createTestErrata("", "SUSE-Manager-Tools-For-SL-Micro-6-5");
        assertTrue(parser.getAnnouncementId(errata).equals("SUSE-RU-2025:20291-1"));
        assertTrue(parser.getAdvisoryUri(errata).toString()
                .equals("https://www.suse.com/support/update/announcement/2025/suse-ru-202520291-1/"));


        errata = createTestErrata("SLE-Module-Live-Patching", "SUSE-15-SP3-2025-1112");
        assertTrue(parser.getAnnouncementId(errata).equals("SUSE-SU-2025:1119-1"));
        assertTrue(parser.getAdvisoryUri(errata).toString()
                .equals("https://www.suse.com/support/update/announcement/2025/suse-su-20251119-1/"));


        errata = createTestErrata("SLE-Module-Basesystem", "SUSE-15-SP6-2025-1562");
        assertTrue(parser.getAnnouncementId(errata).equals("SUSE-OU-2025:1562-1"));
        assertTrue(parser.getAdvisoryUri(errata).toString()
                .equals("https://www.suse.com/support/update/announcement/2025/suse-ou-20251562-1/"));

        errata = createTestErrata("SLE-Module-Packagehub-Subpackages", "SUSE-15-SP6-2025-37");
        assertTrue(parser.getAnnouncementId(errata).equals("SUSE-FU-2025:0037-1"));
        assertTrue(parser.getAdvisoryUri(errata).toString()
                .equals("https://www.suse.com/support/update/announcement/2025/suse-fu-20250037-1/"));


        errata = createTestErrata("SLE-Manager-Tools-For-Micro", "SUSE-5-2025-1319");
        assertTrue(parser.getAnnouncementId(errata).equals("SUSE-RU-2025:1319-1"));
        assertTrue(parser.getAdvisoryUri(errata).toString()
                .equals("https://www.suse.com/support/update/announcement/2025/suse-ru-20251319-1/"));

        errata = createTestErrata("SLE-Module-Development-Tools", "SUSE-15-SP6-2025-990");
        assertTrue(parser.getAnnouncementId(errata).equals("SUSE-RU-2025:0990-1"));
        assertTrue(parser.getAdvisoryUri(errata).toString()
                .equals("https://www.suse.com/support/update/announcement/2025/suse-ru-20250990-1/"));

    }
}
