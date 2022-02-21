/*
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
package com.suse.manager.xmlrpc.maintenance.test;

import static java.util.Collections.emptyList;

import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.frontend.xmlrpc.test.BaseHandlerTestCase;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.maintenance.rescheduling.RescheduleResult;
import com.suse.manager.maintenance.test.MaintenanceTestUtils;
import com.suse.manager.model.maintenance.MaintenanceCalendar;
import com.suse.manager.model.maintenance.MaintenanceSchedule;
import com.suse.manager.xmlrpc.maintenance.MaintenanceHandler;
import com.suse.manager.xmlrpc.serializer.RescheduleResultSerializer;

import java.io.File;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import redstone.xmlrpc.XmlRpcSerializer;

public class MaintenanceHandlerTest extends BaseHandlerTestCase {

    private MaintenanceHandler handler = new MaintenanceHandler();
    private static final String EXCHANGE_MULTI1_ICS = "maintenance-windows-multi-exchange-1.ics";
    private static final String EXCHANGE_MULTI2_ICS = "maintenance-windows-multi-exchange-2.ics";
    private static final String TESTDATAPATH = "/com/suse/manager/maintenance/test/testdata";

    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    public void testMultiScheduleUpdate() throws Exception {
        File icalExM1 = new File(TestUtils.findTestData(
                new File(TESTDATAPATH,  EXCHANGE_MULTI1_ICS).getAbsolutePath()).getPath());
        File icalExM2 = new File(TestUtils.findTestData(
                new File(TESTDATAPATH,  EXCHANGE_MULTI2_ICS).getAbsolutePath()).getPath());

        /* setup test environment */
        Server sapServer = ServerTestUtils.createTestSystem(admin);
        Server coreServer = ServerTestUtils.createTestSystem(admin);

        MaintenanceCalendar mcal = handler.createCalendar(
                admin, "multicalendar", FileUtils.readStringFromFile(icalExM1.getAbsolutePath()));
        MaintenanceSchedule sapSchedule = handler.createSchedule(
                admin, "SAP Maintenance Window", "multi", mcal.getLabel());
        MaintenanceSchedule coreSchedule = handler.createSchedule(
                admin, "Core Server Window", "multi", mcal.getLabel());

        handler.assignScheduleToSystems(
                admin, sapSchedule.getName(), Collections.singletonList(sapServer.getId().intValue()), emptyList());
        handler.assignScheduleToSystems(
                admin, coreSchedule.getName(), Collections.singletonList(coreServer.getId().intValue()), emptyList());


        Action sapAction1 = MaintenanceTestUtils.createActionForServerAt(
                admin, ActionFactory.TYPE_ERRATA, sapServer, "2020-04-13T08:15:00+02:00"); //moved
        Action sapActionEx = MaintenanceTestUtils.createActionForServerAt(
                admin, ActionFactory.TYPE_VIRTUALIZATION_START, sapServer, "2020-04-13T08:15:00+02:00"); //moved
        Action sapAction2 = MaintenanceTestUtils.createActionForServerAt(
                admin, ActionFactory.TYPE_ERRATA, sapServer, "2020-04-27T08:15:00+02:00"); //stay
        Action coreAction1 = MaintenanceTestUtils.createActionForServerAt(
                admin, ActionFactory.TYPE_ERRATA, coreServer, "2020-04-30T09:15:00+02:00"); //stay
        Action coreActionEx = MaintenanceTestUtils.createActionForServerAt(
                admin, ActionFactory.TYPE_VIRTUALIZATION_START, coreServer, "2020-05-21T09:15:00+02:00"); //moved
        Action coreAction2 = MaintenanceTestUtils.createActionForServerAt(
                admin, ActionFactory.TYPE_ERRATA, coreServer, "2020-05-21T09:15:00+02:00"); //moved

        /* update the calendar */
        Map<String, String> details = new HashMap<>();
        details.put("ical", FileUtils.readStringFromFile(icalExM2.getAbsolutePath()));

        List<String> rescheduleStrategy = new LinkedList<>();
        rescheduleStrategy.add("Cancel");

        List<RescheduleResult> result = handler.updateCalendar(admin, mcal.getLabel(), details, rescheduleStrategy);

        /* check results */
        List<Action> sapActionsAfter = ActionFactory.listActionsForServer(admin, sapServer);
        List<Action> coreActionsAfter = ActionFactory.listActionsForServer(admin, coreServer);

        assertEquals(2, sapActionsAfter.size());
        assertEquals(2, coreActionsAfter.size());

        assertEquals(1, sapActionsAfter.stream().filter(a -> a.equals(sapAction2)).count());
        assertEquals(1, sapActionsAfter.stream()
                .filter(a -> a.equals(sapActionEx)).count()); //Action not tied to maintenance mode

        assertEquals(1, coreActionsAfter.stream().filter(a -> a.equals(coreAction1)).count());
        assertEquals(1, coreActionsAfter.stream()
                .filter(a -> a.equals(coreActionEx)).count()); //Action not tied to maintenance mode

        for (RescheduleResult r : result) {
            RescheduleResultSerializer serializer = new RescheduleResultSerializer();
            Writer output = new StringWriter();
            serializer.serialize(r, output, new XmlRpcSerializer());
            String actual = output.toString();

            if (r.getScheduleName().equals("SAP Maintenance Window")) {
                assertContains(actual, "<i4>" + sapServer.getId() + "</i4>");
                assertContains(actual, "<string>Patch Update</string>");
            }
            else if (r.getScheduleName().equals("Core Server Window")) {
                assertContains(actual, "<i4>" + coreServer.getId() + "</i4>");
                assertContains(actual, "<string>Patch Update</string>");
            }
            else {
                assertTrue("Not expected result set", false);
            }
        }
    }
}
