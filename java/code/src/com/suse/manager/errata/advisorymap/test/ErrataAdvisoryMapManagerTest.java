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

package com.suse.manager.errata.advisorymap.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.testing.RhnBaseTestCase;

import com.suse.manager.errata.advisorymap.ErrataAdvisoryMapManager;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

public class ErrataAdvisoryMapManagerTest extends RhnBaseTestCase {
    private static final String TEST_ADVISORY_MAP_CSV_FILE_NAME =
            "/com/suse/manager/errata/advisorymap/test/advisory-map.csv";
    private static final long TEST_ADVISORY_MAP_RECORDS_NUM = 140938L;

    private static final boolean PRINT_STD_OUTPUT = false;
    private static final boolean PERFORM_LONG_TESTS = false;
    private long startTimeMs;

    private final ErrataAdvisoryMapManager advisoryMapManager = new ErrataAdvisoryMapManager();

    @SuppressWarnings("java:S106")
    private static void testPrintOut(String arg) {
        if (!PRINT_STD_OUTPUT) {
            return;
        }
        System.out.println(arg);
    }

    private void startTimeMeasure(String message) {
        testPrintOut(message);
        startTimeMs = System.currentTimeMillis();
    }

    private void stopTimeMeasure(String message) {
        long estimatedTimeMs = System.currentTimeMillis() - startTimeMs;
        testPrintOut(message + String.format("%d seconds (%d ms)", estimatedTimeMs / 1000L, estimatedTimeMs));
    }

    @Test
    public void storeNewAdvisoryMapLongTest() throws IOException {
        if (PERFORM_LONG_TESTS) {
            //test time: about 20 seconds for 140K records
            assertEquals(0, advisoryMapManager.getAdvisoryMapSize());

            InputStream inputStream = this.getClass().getResourceAsStream(TEST_ADVISORY_MAP_CSV_FILE_NAME);

            startTimeMeasure(String.format("Populating EMPTY ErrataAdvisoryMap with %d records",
                    TEST_ADVISORY_MAP_RECORDS_NUM));
            advisoryMapManager.readAdvisoryMapPopulateDatabase(inputStream);
            stopTimeMeasure("Elapsed time: ");

            assertEquals(TEST_ADVISORY_MAP_RECORDS_NUM, advisoryMapManager.getAdvisoryMapSize());
        }
    }

    @Test
    public void syncErrataAdvisoryMapTest() throws IOException {
        if (PERFORM_LONG_TESTS) {
            //download time: about 5 seconds for about 140K records
            //populate records time: about 20 seconds for 140K records
            assertEquals(0, advisoryMapManager.getAdvisoryMapSize());

            startTimeMeasure("Synchronize ErrataAdvisoryMap and populate database");
            advisoryMapManager.syncErrataAdvisoryMap();
            stopTimeMeasure("Elapsed time: ");

            testPrintOut(String.format("Num records: %d", advisoryMapManager.getAdvisoryMapSize()));
            assertTrue(advisoryMapManager.getAdvisoryMapSize() > 140000);
        }
    }
}
