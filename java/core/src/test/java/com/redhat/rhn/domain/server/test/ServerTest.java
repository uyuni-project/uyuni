/*
 * Copyright (c) 2009--2017 Red Hat, Inc.
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
package com.redhat.rhn.domain.server.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.rhnset.RhnSetFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.ClientCapability;
import com.redhat.rhn.domain.server.EntitlementServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerInfo;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.manager.system.test.SystemManagerTest;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ConfigTestUtils;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.test.TestSaltApi;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Optional;

/**
 * ServerTest
 */
public class ServerTest extends BaseTestCaseWithUser {

    private final SaltApi saltApi = new TestSaltApi();
    private final ServerGroupManager serverGroupManager = new ServerGroupManager(saltApi);
    private final SystemUnentitler systemUnentitler = new SystemUnentitler(saltApi);
    private final SystemEntitlementManager systemEntitlementManager = new SystemEntitlementManager(
            systemUnentitler, new SystemEntitler(saltApi)
    );

    @Test
    public void testSsmForSubscribe() throws Exception {
        Server s = ServerTestUtils.createTestSystem(user);
        s.setMachineId("themachineid");

        ConfigChannel channel1 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 1", "cfg-channel-1");
        ConfigChannel channel2 = ConfigTestUtils.createConfigChannel(user.getOrg(),
                "Channel 2", "cfg-channel-2");
        s.subscribeConfigChannels(List.of(channel1, channel2), user);
        long sid = s.getId();
        TestUtils.saveAndFlush(s);
        RhnSetDecl.SYSTEMS.get(user).addElement(sid);
        RhnSet ssm = RhnSetDecl.SYSTEMS.get(user);
        ssm.addElement(sid);
        RhnSetFactory.save(ssm);
        List<Server> inSSM = ServerFactory.listSystemsInSsm(user);
        assertEquals(1, inSSM.size());

        List<Server> servers = ServerFactory.getSsmSystemsForSubscribe(user);
        assertEquals(1, servers.size());
        assertNull(servers.get(0).getName());
        assertEquals(2, servers.get(0).getConfigChannelCount());
    }

    @Test
    public void testChangeConfigChannelsRankingOrder() throws Exception {
        //create server and subscribe configuration channels in order 1,2,3
        Server s = ServerTestUtils.createTestSystem(user);
        ConfigChannel channel1 = ConfigTestUtils.createConfigChannel(user.getOrg(), "Ch 1", "cfg-channel-1");
        ConfigChannel channel2 = ConfigTestUtils.createConfigChannel(user.getOrg(), "Ch 2", "cfg-channel-2");
        ConfigChannel channel3 = ConfigTestUtils.createConfigChannel(user.getOrg(), "Ch 3", "cfg-channel-3");
        s.subscribeConfigChannels(List.of(channel1, channel2, channel3), user);

        //save and reload server, check configuration channels order
        TestUtils.saveAndFlush(s);
        Server s1 = ServerFactory.lookupByIdAndOrg(s.getId(), user.getOrg());
        assertNotNull(s1);
        List<ConfigChannel> result1ConfigChannelList = s1.getConfigChannelList();
        assertEquals(3, result1ConfigChannelList.size());
        assertEquals("cfg-channel-1", result1ConfigChannelList.get(0).getLabel());
        assertEquals("cfg-channel-2", result1ConfigChannelList.get(1).getLabel());
        assertEquals("cfg-channel-3", result1ConfigChannelList.get(2).getLabel());

        //change configuration channels ranking order to 3,1,2
        s1.subscribeConfigChannels(List.of(channel3, channel1, channel2), user);

        //save and reload server, check configuration channels order
        TestUtils.saveAndFlush(s1);
        Server s2 = ServerFactory.lookupByIdAndOrg(s.getId(), user.getOrg());
        assertNotNull(s2);
        List<ConfigChannel> result2ConfigChannelList = s2.getConfigChannelList();
        assertEquals(3, result2ConfigChannelList.size());
        assertEquals("cfg-channel-3", result2ConfigChannelList.get(0).getLabel());
        assertEquals("cfg-channel-1", result2ConfigChannelList.get(1).getLabel());
        assertEquals("cfg-channel-2", result2ConfigChannelList.get(2).getLabel());
    }

    @Test
    public void testIsInactive() {
        Server s = ServerFactory.createServer();
        s.setServerInfo(new ServerInfo());
        Calendar pcal = Calendar.getInstance();
        pcal.setTime(new Timestamp(System.currentTimeMillis()));
        pcal.roll(Calendar.MINUTE, -5);
        s.getServerInfo().setCheckin(pcal.getTime());
        assertFalse(s.isInactive());
    }

    @Test
    public void testSetBaseEntitlement() throws Exception {
        Server s = ServerTestUtils.createTestSystem(user);
        systemUnentitler.removeAllServerEntitlements(s);
        UserTestUtils.addManagement(s.getCreator().getOrg());
        HibernateFactory.getSession().clear();
        s = ServerFactory.lookupById(s.getId());
        systemEntitlementManager.setBaseEntitlement(s, EntitlementManager.MANAGEMENT);
        TestUtils.saveAndFlush(s);
        s = reload(s);
        assertEquals(s.getBaseEntitlement(), EntitlementManager.MANAGEMENT);
    }

    @Test
    public void testCapabilities() throws Exception {
        Server s = ServerFactoryTest.createTestServer(user, true);
        SystemManagerTest.giveCapability(s.getId(),
                SystemManager.CAP_CONFIGFILES_DEPLOY, 1L);
        assertFalse(s.getCapabilities().isEmpty());
        boolean containsDeploy = false;
        for (ClientCapability c : s.getCapabilities()) {
            if (SystemManager.CAP_CONFIGFILES_DEPLOY.equals(c.getCapability().getName())) {
                containsDeploy = true;
                break;
            }
        }
        assertTrue(containsDeploy);
    }

    @Test
    public void testRemoveCapability() throws Exception {
        Server s = ServerFactoryTest.createTestServer(user, true);
        SystemManagerTest.giveCapability(s.getId(),
                SystemManager.CAP_CONFIGFILES_DEPLOY, 1L);
        SystemManagerTest.giveCapability(s.getId(),
                SystemManager.CAP_SCRIPT_RUN, 2L);
        assertEquals(2, s.getCapabilities().size());
        Optional<ClientCapability> cap1 = s.getCapabilities()
                .stream().filter(c -> c.getCapability().getName().equals(SystemManager.CAP_SCRIPT_RUN))
                .findFirst();
        s.getCapabilities().clear();
        s.getCapabilities().add(cap1.get());
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();
        s = ServerFactory.lookupById(s.getId());
        assertEquals(1, s.getCapabilities().size());
    }

    @Test
    public void testNetworkInterfaces() throws Exception {
        Server s = ServerTestUtils.createTestSystem(user);
        NetworkInterfaceTest.createTestNetworkInterface(s);
        TestUtils.saveAndReload(s);
        Server s2 = ServerTestUtils.createTestSystem(user);
        s2 = TestUtils.saveAndReload(s2);
        NetworkInterfaceTest.createTestNetworkInterface(s2);
        TestUtils.saveAndReload(s2);
        assertTrue(true, "we didnt make it to the end");
    }
    /**
     * Test for {@link Server#doesOsSupportsContainerization()}.
     */
    @Test
    public void testOsSupportsContainerization() throws Exception {
        Server s = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeSaltEntitled(),
                ServerFactoryTest.TYPE_SERVER_MINION);
        s.setOs("SLES");
        s.setRelease("12.1");
        assertTrue(s.doesOsSupportsContainerization());
    }
    /**
     * Test for {@link Server#doesOsSupportsContainerization()}.
     */
    @Test
    public void testOsDoesNotSupportsContainerization() throws Exception {
        Server s = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeSaltEntitled(),
                ServerFactoryTest.TYPE_SERVER_MINION);
        s.setOs("SLES");
        s.setRelease("11.4");
        assertFalse(s.doesOsSupportsContainerization());
    }

    /**
     * Test for {@link Server#doesOsSupportsOSImageBuilding()}.
     */
    @Test
    public void testOsSupportsOSImageBuilding() throws Exception {
        Server s = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeSaltEntitled(),
                ServerFactoryTest.TYPE_SERVER_MINION);
        s.setOsFamily(ServerConstants.OS_FAMILY_SUSE);
        s.setOs("SLES");
        s.setRelease("12.1");
        assertTrue(s.doesOsSupportsOSImageBuilding());
    }

    /**
     * Test for {@link Server#doesOsSupportsMonitoring()} for SLES.
     */
    @Test
    public void testOsSupportsMonitoring() throws Exception {
        Server s = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeSaltEntitled(),
                ServerFactoryTest.TYPE_SERVER_MINION);
        s.setOs("SLES");
        s.setRelease("12.1");
        assertTrue(s.doesOsSupportsMonitoring());
    }

    /**
     * Test for {@link Server#doesOsSupportsMonitoring()} for Leap.
     */
    @Test
    public void testOsSupportsMonitoringLeap() throws Exception {
        Server s = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeSaltEntitled(),
                ServerFactoryTest.TYPE_SERVER_MINION);
        s.setOs("Leap");
        s.setRelease("15.0");
        assertTrue(s.doesOsSupportsMonitoring());
    }

    /**
     * Test for {@link Server#doesOsSupportsMonitoring()} for Ubuntu.
     */
    @Test
    public void testOsSupportsMonitoringUbuntu() throws Exception {
        Server s = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeSaltEntitled(),
                ServerFactoryTest.TYPE_SERVER_MINION);
        s.setOs("Ubuntu");
        s.setRelease("18.04");
        assertTrue(s.doesOsSupportsMonitoring());
    }

    /**
     * Test for {@link Server#doesOsSupportsMonitoring()} for RedHat 6.
     */
    @Test
    public void testOsSupportsMonitoringRedHat6() throws Exception {
        MinionServer s = (MinionServer) ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeSaltEntitled(),
                ServerFactoryTest.TYPE_SERVER_MINION);
        s.setOsFamily(ServerConstants.OS_FAMILY_REDHAT);
        s.setRelease("6");
        assertTrue(s.doesOsSupportsMonitoring());
    }

    /**
     * Test for {@link Server#doesOsSupportsMonitoring()} for RedHat 7.
     */
    @Test
    public void testOsSupportsMonitoringRedHat7() throws Exception {
        MinionServer s = (MinionServer) ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeSaltEntitled(),
                ServerFactoryTest.TYPE_SERVER_MINION);
        s.setOsFamily(ServerConstants.OS_FAMILY_REDHAT);
        s.setRelease("7");
        assertTrue(s.doesOsSupportsMonitoring());
    }

    /**
     * Test for {@link Server#doesOsSupportsMonitoring()} for RedHat 87.
     */
    @Test
    public void testOsSupportsMonitoringRedHat8() throws Exception {
        MinionServer s = (MinionServer) ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeSaltEntitled(),
                ServerFactoryTest.TYPE_SERVER_MINION);
        s.setOsFamily(ServerConstants.OS_FAMILY_REDHAT);
        s.setRelease("8");
        assertTrue(s.doesOsSupportsMonitoring());
    }

    /**
     * Test for {@link Server#doesOsSupportsMonitoring()} for RedHat 9.
     */
    @Test
    public void testOsSupportsMonitoringRedHat9() throws Exception {
        MinionServer s = (MinionServer) ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeSaltEntitled(),
                ServerFactoryTest.TYPE_SERVER_MINION);
        s.setOsFamily(ServerConstants.OS_FAMILY_REDHAT);
        s.setRelease("9");
        assertTrue(s.doesOsSupportsMonitoring());
    }

    /**
     * Test for {@link Server#doesOsSupportsOSImageBuilding()}.
     */
    @Test
    public void testOsDoesNotSupportsOSImageBuilding() throws Exception {
        Server s = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeSaltEntitled(),
                ServerFactoryTest.TYPE_SERVER_MINION);
        s.setOsFamily(ServerConstants.OS_FAMILY_DEBIAN);
        s.setOs(ServerConstants.UBUNTU);
        s.setRelease("18.04");
        assertFalse(s.doesOsSupportsOSImageBuilding());
    }

    /**
     * Test for {@link Server#doesOsSupportsMonitoring()}.
     */
    @Test
    public void testOsDoesNotSupportsMonitoring() throws Exception {
        Server s = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeSaltEntitled(),
                ServerFactoryTest.TYPE_SERVER_MINION);
        s.setOs("SLES");
        s.setRelease("11.4");
        assertFalse(s.doesOsSupportsMonitoring());
    }

    @Test
    public void testGetIpAddress() throws Exception {
        Server s = ServerTestUtils.createTestSystem(user);
        assertNull(s.getIpAddress());


        String hwAddr = "AA:AA:BB:BB:CC:CC";
        String ipAddr = "172.31.1.102";

        NetworkInterfaceTest.createTestNetworkInterface(s, "aaa",
                ipAddr, hwAddr);

        NetworkInterfaceTest.createTestNetworkInterface(s, "bbb",
                ipAddr, hwAddr);

        NetworkInterfaceTest.createTestNetworkInterface(s, "zzz",
                ipAddr, hwAddr);

        NetworkInterfaceTest.createTestNetworkInterface(s, "eth0",
                ipAddr, hwAddr);

        NetworkInterfaceTest.createTestNetworkInterface(s, "eth1",
                ipAddr, hwAddr);

        s = TestUtils.saveAndReload(s);

        assertNotNull(s.getIpAddress());

        NetworkInterface lo = NetworkInterfaceTest.createTestNetworkInterface(s, "lo",
                "127.0.0.1", null);
        s.addNetworkInterface(lo);

        NetworkInterface virbr0 = NetworkInterfaceTest.
            createTestNetworkInterface(s, "virbr0",
                "172.31.2.1", "AA:FF:CC:DD:DD");
        s.addNetworkInterface(virbr0);

        NetworkInterface ni = s.findPrimaryNetworkInterface();
        assertEquals(ipAddr, ni.getIPv4Addresses().get(0).getAddress());

        assertEquals(ipAddr, s.getIpAddress());
        assertEquals(hwAddr, s.getHardwareAddress());

    }


    public void xxxtestServerWithVirtEntitlementIsVirtualHost() {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server server = new VirtEntitledServer(user);
        server = TestUtils.saveAndReload(server);
        assertTrue(server.isVirtualHost());
    }

    public void xxtestServerWithGuestsIsVirtualHost() {
        Server server = new ServerWithGuests();
        server.setOrg(user.getOrg());

        assertTrue(server.isVirtualHost());
    }

    private class VirtEntitledServer extends Server {
        VirtEntitledServer(User user) {
            setOrg(user.getOrg());
            EntitlementServerGroup group = serverGroupManager.
                        lookupEntitled(EntitlementManager.VIRTUALIZATION, user);
            List servers = new ArrayList<>();
            servers.add(this);
            serverGroupManager.addServers(group, servers, user);
        }
    }

    private class ServerWithGuests extends Server {
        ServerWithGuests() {
            VirtualInstance vi = new VirtualInstance();
            vi.setUuid(TestUtils.randomString());
            addGuest(vi);
        }
    }
}
