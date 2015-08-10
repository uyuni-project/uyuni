/**
 * Copyright (c) 2013 SUSE LLC
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
package com.redhat.rhn.frontend.action.kickstart.ssm.test;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.kickstart.test.KickstartIpTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.NetworkInterfaceTest;
import com.redhat.rhn.frontend.action.kickstart.ssm.SsmKSScheduleAction;
import com.redhat.rhn.frontend.taglibs.list.TagHelper;
import com.redhat.rhn.frontend.taglibs.list.helper.ListHelper;
import com.redhat.rhn.manager.kickstart.KickstartScheduleCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.redhat.rhn.manager.kickstart.test.KickstartScheduleCommandTest;
import com.redhat.rhn.testing.RhnMockStrutsTestCase;
import com.redhat.rhn.testing.ServerTestUtils;

import org.cobbler.CobblerConnection;
import org.cobbler.Distro;
import org.cobbler.Profile;
import org.cobbler.SystemRecord;

import servletunit.HttpServletRequestSimulator;

import java.util.Date;
import java.util.HashMap;

/**
 * Tests SsmKSScheduleAction.
 */
public class SsmKSScheduleActionTest extends RhnMockStrutsTestCase {

    /**
     * Tests creating Cobbler system records with a chosen profile from SSM.
     * @throws Exception if something goes wrong
     */
    public void testCreateSystemRecordsByProfile() throws Exception {
        CobblerConnection connection = CobblerXMLRPCHelper.getConnection(user.getLogin());
        Distro distro = new Distro.Builder()
                .setName("test-distro")
                .setKernel("kernel")
                .setInitrd("initrd")
                .setKsmeta(new HashMap<String, Object>())
                .build(connection);
        Profile profile = Profile.create(connection, "test-profile", distro);
        Server server1 = ServerTestUtils.createTestSystem(user);
        Server server2 = ServerTestUtils.createTestSystem(user);
        ServerTestUtils.addServersToSsm(user, server1.getId());
        ServerTestUtils.addServersToSsm(user, server2.getId());

        String listUniqueName = TagHelper.generateUniqueName(ListHelper.LIST);
        addRequestParameter("list_" + listUniqueName + "_radio", profile.getId());
        addSubmitted();
        addDispatchCall(SsmKSScheduleAction.CREATE_RECORDS_BUTTON);

        setRequestPathInfo("/systems/ssm/kickstart/ScheduleByProfile");
        request.setMethod(HttpServletRequestSimulator.POST);
        actionPerform();

        assertEquals(302, getMockResponse().getStatusCode());

        SystemRecord record1 = SystemRecord.lookupById(connection, server1.getCobblerId());
        assertNotNull(record1);
        assertEquals(profile.getId(), record1.getProfile().getId());

        SystemRecord record2 = SystemRecord.lookupById(connection, server2.getCobblerId());
        assertNotNull(record2);
        assertEquals(profile.getId(), record1.getProfile().getId());
    }

    /**
     * Tests creating Cobbler system records by IP range from SSM.
     * @throws Exception if something goes wrong
     */
    public void testCreateSystemRecordsByIp() throws Exception {
        CobblerConnection connection = CobblerXMLRPCHelper.getConnection(user.getLogin());

        KickstartData kickstartData = KickstartDataTest.createKickstartWithProfile(user);
        KickstartIpTest.addIpRangesToKickstart(kickstartData);

        Profile profile = Profile.lookupById(connection, kickstartData.getCobblerId());

        Server server1 = ServerTestUtils.createTestSystem(user);
        // this is comprised in ranges added by addIpRangesToKickstart()
        NetworkInterfaceTest.createTestNetworkInterface(server1, "server1", "192.168.2.2",
            "deadbeef");

        Server server2 = ServerTestUtils.createTestSystem(user);
        // this is not comprised in ranges added by addIpRangesToKickstart()
        NetworkInterfaceTest.createTestNetworkInterface(server2, "server2", "192.178.2.2",
            "deadbeef");

        ServerTestUtils.addServersToSsm(user, server1.getId());
        ServerTestUtils.addServersToSsm(user, server2.getId());

        String listUniqueName = TagHelper.generateUniqueName(ListHelper.LIST);
        addRequestParameter("ip", "true");
        addRequestParameter("list_" + listUniqueName + "_radio", profile.getId());
        addSubmitted();
        addDispatchCall(SsmKSScheduleAction.CREATE_RECORDS_BUTTON);

        setRequestPathInfo("/systems/ssm/kickstart/ScheduleByIp");
        request.setMethod(HttpServletRequestSimulator.POST);
        actionPerform();

        assertEquals(302, getMockResponse().getStatusCode());

        SystemRecord record1 = SystemRecord.lookupById(connection, server1.getCobblerId());
        assertNotNull(record1);
        assertEquals(profile.getId(), record1.getProfile().getId());

        SystemRecord record2 = SystemRecord.lookupById(connection, server2.getCobblerId());
        assertNull(record2);
    }

    /**
     * Tests a corner condition in which a profile does not exist.
     * @throws Exception if something goes wrong
     */
    public void testCreateSystemRecordsWithoutProfile() throws Exception {
        CobblerConnection connection = CobblerXMLRPCHelper.getConnection(user.getLogin());
        Server server = ServerTestUtils.createTestSystem(user);
        ServerTestUtils.addServersToSsm(user, server.getId());

        String listUniqueName = TagHelper.generateUniqueName(ListHelper.LIST);
        addRequestParameter("list_" + listUniqueName + "_radio", "non existing profile");
        addSubmitted();
        addDispatchCall(SsmKSScheduleAction.CREATE_RECORDS_BUTTON);

        setRequestPathInfo("/systems/ssm/kickstart/ScheduleByProfile");
        request.setMethod(HttpServletRequestSimulator.POST);
        actionPerform();

        assertEquals(302, getMockResponse().getStatusCode());

        SystemRecord record1 = SystemRecord.lookupById(connection, server.getCobblerId());
        assertNull(record1);
    }

    /**
     * Tests a corner condition in which an existing kickstart is present.
     * @throws Exception if something goes wrong
     */
    public void testCreateSystemRecordsWithExistingKickstart() throws Exception {
        CobblerConnection connection = CobblerXMLRPCHelper.getConnection(user.getLogin());

        KickstartData kickstartData = KickstartDataTest.createKickstartWithProfile(user);
        KickstartIpTest.addIpRangesToKickstart(kickstartData);

        Distro distro = new Distro.Builder()
                .setName("test-distro")
                .setKernel("kernel")
                .setInitrd("initrd")
                .setKsmeta(new HashMap<String, Object>())
                .build(connection);
        Profile profile = Profile.create(connection, "test-profile", distro);

        Server server = ServerTestUtils.createTestSystem(user);
        // this is comprised in ranges added by addIpRangesToKickstart()
        NetworkInterfaceTest.createTestNetworkInterface(server, "server1", "192.168.2.2",
            "deadbeef");

        KickstartScheduleCommand command = KickstartScheduleCommandTest.scheduleAKickstart(
            server, kickstartData);
        command.setScheduleDate(new Date());
        ValidatorError ve = command.store();

        ServerTestUtils.addServersToSsm(user, server.getId());

        String listUniqueName = TagHelper.generateUniqueName(ListHelper.LIST);
        addRequestParameter("ip", "true");
        addRequestParameter("list_" + listUniqueName + "_radio", profile.getId());
        addSubmitted();
        addDispatchCall(SsmKSScheduleAction.CREATE_RECORDS_BUTTON);

        setRequestPathInfo("/systems/ssm/kickstart/ScheduleByIp");
        request.setMethod(HttpServletRequestSimulator.POST);
        actionPerform();

        assertEquals(302, getMockResponse().getStatusCode());

        SystemRecord record = SystemRecord.lookupById(connection, server.getCobblerId());
        assertNotNull(record);
        assertNotSame(profile.getId(), record.getProfile().getId());
    }
}
