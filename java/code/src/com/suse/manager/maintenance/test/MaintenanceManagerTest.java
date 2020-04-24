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

import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.maintenance.MaintenanceManager;
import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.model.maintenance.MaintenanceSchedule;
import com.suse.manager.model.maintenance.MaintenanceSchedule.ScheduleType;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class MaintenanceManagerTest extends BaseTestCaseWithUser {

    private static final String TESTDATAPATH = "/com/suse/manager/maintenance/test/testdata";
    private static final String KDE_ICS = "maintenance-windows-kde.ics";
    private static final String EXCHANGE_ICS = "maintenance-windows-exchange.ics";

    public void testCreateSchedule() throws Exception {
        MaintenanceManager mm = MaintenanceManager.instance();
        mm.createMaintenanceSchedule(user, "test server", ScheduleType.SINGLE, Optional.empty());

        List<String> names = mm.listScheduleNamesByUser(user);
        assertEquals(1, names.size());
        assertContains(names, "test server");

        Optional<MaintenanceSchedule> dbScheduleOpt = mm.lookupMaintenanceScheduleByUserAndName(user, "test server");
        assertNotNull(dbScheduleOpt.orElse(null));
        MaintenanceSchedule dbSchedule = dbScheduleOpt.get();

        assertEquals(user.getOrg(), dbSchedule.getOrg());
        assertEquals("test server", dbSchedule.getName());
        assertEquals(ScheduleType.SINGLE, dbSchedule.getScheduleType());
        assertTrue(dbSchedule.getCalendarOpt().isEmpty());

    }

    public void testCreateCalendar() throws Exception {
        File ical = new File(TestUtils.findTestData(
                new File(TESTDATAPATH,  KDE_ICS).getAbsolutePath()).getPath());

        MaintenanceManager mm = MaintenanceManager.instance();
        mm.createMaintenanceCalendar(user, "testcalendar", FileUtils.readStringFromFile(ical.getAbsolutePath()));

        List<String> labels = mm.listCalendarLabelsByUser(user);
        assertEquals(1, labels.size());
        assertContains(labels, "testcalendar");

        Optional<MaintenanceCalendar> dbCalOpt = mm.lookupCalendarByUserAndLabel(user, "testcalendar");
        assertNotNull(dbCalOpt.orElse(null));
        MaintenanceCalendar dbCal = dbCalOpt.get();

        assertEquals(user.getOrg(), dbCal.getOrg());
        assertEquals("testcalendar", dbCal.getLabel());
        assertEquals(FileUtils.readStringFromFile(ical.getAbsolutePath()), dbCal.getIcal());
        assertNull(dbCal.getUrlOpt().orElse(null));
    }

    public void testCreateScheduleWithCalendar() throws Exception {
        File ical = new File(TestUtils.findTestData(
                new File(TESTDATAPATH,  KDE_ICS).getAbsolutePath()).getPath());
        MaintenanceManager mm = MaintenanceManager.instance();

        MaintenanceCalendar mc = mm.createMaintenanceCalendar(user, "testcalendar", FileUtils.readStringFromFile(ical.getAbsolutePath()));
        mm.createMaintenanceSchedule(user, "test server", ScheduleType.SINGLE, Optional.of(mc));

        List<String> names = mm.listScheduleNamesByUser(user);
        assertEquals(1, names.size());
        assertContains(names, "test server");

        Optional<MaintenanceSchedule> dbScheduleOpt = mm.lookupMaintenanceScheduleByUserAndName(user, "test server");
        assertNotNull(dbScheduleOpt.orElse(null));
        MaintenanceSchedule dbSchedule = dbScheduleOpt.get();

        assertEquals(user.getOrg(), dbSchedule.getOrg());
        assertEquals("test server", dbSchedule.getName());
        assertEquals(ScheduleType.SINGLE, dbSchedule.getScheduleType());
        assertNotNull(dbSchedule.getCalendarOpt().orElse(null));

        MaintenanceCalendar dbCal = dbSchedule.getCalendarOpt().get();
        assertEquals(user.getOrg(), dbCal.getOrg());
        assertEquals("testcalendar", dbCal.getLabel());
        assertEquals(FileUtils.readStringFromFile(ical.getAbsolutePath()), dbCal.getIcal());
        assertNull(dbCal.getUrlOpt().orElse(null));
    }

    public void testUpdateScheduleWithCalendarURL() throws Exception {
        File icalKde = new File(TestUtils.findTestData(
                new File(TESTDATAPATH,  KDE_ICS).getAbsolutePath()).getPath());
        File icalEx = new File(TestUtils.findTestData(
                new File(TESTDATAPATH,  EXCHANGE_ICS).getAbsolutePath()).getPath());
        MaintenanceManager mm = new MaintenanceManager() {
            @Override
            protected String fetchCalendarData(String url) {
                return FileUtils.readStringFromFile(icalEx.getAbsolutePath());
            }
        };

        MaintenanceCalendar mc = mm.createMaintenanceCalendar(user, "testcalendar", FileUtils.readStringFromFile(icalKde.getAbsolutePath()));
        mm.createMaintenanceSchedule(user, "test server", ScheduleType.SINGLE, Optional.of(mc));

        Optional<MaintenanceSchedule> dbScheduleOpt = mm.lookupMaintenanceScheduleByUserAndName(user, "test server");
        assertNotNull(dbScheduleOpt.orElse(null));
        MaintenanceSchedule dbSchedule = dbScheduleOpt.get();
        assertNotNull(dbSchedule.getCalendarOpt().orElse(null));

        MaintenanceCalendar dbCal = dbSchedule.getCalendarOpt().get();
        assertEquals(user.getOrg(), dbCal.getOrg());
        assertEquals("testcalendar", dbCal.getLabel());
        assertEquals(FileUtils.readStringFromFile(icalKde.getAbsolutePath()), dbCal.getIcal());
        assertNull(dbCal.getUrlOpt().orElse(null));

        Map<String, String> details = new HashMap<>();
        details.put("url", "http://dummy.domain.top/exchange");

        mm.updateCalendar(user, "testcalendar", details);

        dbCal = mm.lookupCalendarByUserAndLabel(user, "testcalendar").orElseThrow(() -> new RuntimeException("Cannot find testcalendar"));
        assertEquals(user.getOrg(), dbCal.getOrg());
        assertEquals("testcalendar", dbCal.getLabel());
        assertEquals(FileUtils.readStringFromFile(icalEx.getAbsolutePath()), dbCal.getIcal());
        assertNotNull(dbCal.getUrlOpt().orElse(null));
        assertEquals("http://dummy.domain.top/exchange", dbCal.getUrlOpt().get());

        Map<String, String> sDetails = new HashMap<>();
        sDetails.put("type", "multi");

        mm.updateMaintenanceSchedule(user, "test server", sDetails);

        dbScheduleOpt = mm.lookupMaintenanceScheduleByUserAndName(user, "test server");
        assertNotNull(dbScheduleOpt.orElse(null));
        dbSchedule = dbScheduleOpt.get();

        assertEquals(user.getOrg(), dbSchedule.getOrg());
        assertEquals("test server", dbSchedule.getName());
        assertEquals(ScheduleType.MULTI, dbSchedule.getScheduleType());
        assertEquals(dbCal, dbSchedule.getCalendarOpt().orElse(null));
    }
}
