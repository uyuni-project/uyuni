/**
 * Copyright (c) 2020 SUSE LLC
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

package com.suse.manager.maintenance.test;

import static java.util.Optional.empty;

import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.maintenance.MaintenanceManager;

import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.io.FileReader;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

import junit.framework.TestCase;
import net.fortuna.ical4j.model.Calendar;

/**
 * Tests focusing on maintenance windows computation
 */
public class MaintenanceManagerWindowsTest extends TestCase {

    private MaintenanceManager maintenanceManager = MaintenanceManager.instance();

    private static final String TESTDATAPATH = "/com/suse/manager/maintenance/test/testdata";
    private static final String GOOGLE_ICS = "maintenance-windows-google-multizones.ics";
    private Calendar multiZonesCal;

    /**
     * {@inheritDoc}
     */
    public void setUp() throws Exception {
        super.setUp();

        File ical = new File(TestUtils.findTestData(new File(TESTDATAPATH, GOOGLE_ICS).getAbsolutePath()).getPath());
        multiZonesCal = maintenanceManager.parseCalendar(new FileReader(ical)).get();
    }

    /**
     * Tests calculating upcoming windows from 2 different timezones.
     * Both of the test times represent same point in time, therefore the list of maint. windows should be equal.
     */
    public void testSameMomentDifferentTimezones() {
        ZonedDateTime newYorkStart = ZonedDateTime.parse("2020-06-09T08:00:00-04:00"); // NY
        ZonedDateTime tokyoStart = ZonedDateTime.parse("2020-06-09T21:00:00+09:00"); // Japan (same moment in time!)

        List<Pair<Instant, Instant>> listNewYork = maintenanceManager.calculateUpcomingPeriods(multiZonesCal, empty(),
                newYorkStart.toInstant(), 5).collect(Collectors.toList());

        List<Pair<Instant, Instant>> listTokyo = maintenanceManager.calculateUpcomingPeriods(multiZonesCal, empty(),
                tokyoStart.toInstant(), 5).collect(Collectors.toList());

        assertEquals(listNewYork, listTokyo);
    }

    /**
     * Test calculating upcoming windows at:
     * - 8:00 in New York
     * - 11:00 in Sri Lanka
     *
     * Since 11:00 in Sri Lanka equals to 1:30 in NY, we should see the maintenance window starting at 8:00 NY time.
     */
    public void testSameLocalTimeAhead() {
        ZonedDateTime newYorkStart = ZonedDateTime.parse("2020-06-08T08:00:00-04:00"); // NY

        List<Pair<Instant, Instant>> listNewYork = maintenanceManager.calculateUpcomingPeriods(multiZonesCal, empty(),
                newYorkStart.toInstant(), 5).collect(Collectors.toList());

        // Sri Lanka is ahead of NYC, so at 11:00 in Sri Lanka, we still see maint. windows starting at 8:00 in NY
        ZonedDateTime sriLankaAlike = ZonedDateTime.parse("2020-06-08T11:00:00+05:30");
        List<Pair<Instant, Instant>> listSriLanka = maintenanceManager.calculateUpcomingPeriods(multiZonesCal, empty(),
                sriLankaAlike.toInstant(), 5).collect(Collectors.toList());
        assertEquals(listNewYork, listSriLanka);
    }

    /**
     * Opposite scenario to testSameLocalTimeAhead. Test calculating windows at:
     * - 8:00 in New York
     * - 8:00 on Tahiti
     *
     * At 8:00 Tahitian time, the maintenance window starting at 8:00 and ending at 10:00 NY time is already over,
     * so we shouldn't see it in the list.
     */
    public void testSameLocalTimeBehind() {
        ZonedDateTime newYorkStart = ZonedDateTime.parse("2020-06-08T08:00:00-04:00"); // NY

        List<Pair<Instant, Instant>> listNewYork = maintenanceManager.calculateUpcomingPeriods(multiZonesCal, empty(),
                newYorkStart.toInstant(), 5).collect(Collectors.toList());

        // Tahiti, on the other hand is behind NYC, so at 8:00 Tahitian time, the maintenance windows at 8:00 NY time
        // is already over and we shouldn't see it
        ZonedDateTime tahitiLike = ZonedDateTime.parse("2020-06-08T08:00:00-10:00");
        List<Pair<Instant, Instant>> listTahiti = maintenanceManager.calculateUpcomingPeriods(multiZonesCal, empty(),
                tahitiLike.toInstant(), 4).collect(Collectors.toList());

        List<Pair<Instant, Instant>> newYorkButFirst = listNewYork.stream().skip(1).collect(Collectors.toList());
        // the Tahitian windows should match the NYC ones minus the first one
        assertEquals(newYorkButFirst, listTahiti);
    }
}
