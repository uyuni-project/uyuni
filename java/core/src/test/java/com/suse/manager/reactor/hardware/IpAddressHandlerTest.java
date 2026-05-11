/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.reactor.hardware;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.ServerNetAddress4;
import com.redhat.rhn.domain.server.ServerNetAddress6;
import com.redhat.rhn.domain.server.ServerNetworkFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.salt.netapi.calls.modules.Network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * Tests for {@link IpAddressHandler}
 */
public class IpAddressHandlerTest extends BaseTestCaseWithUser {

    public static final String TEST_IPV4_ADDRESS = "192.168.1.100";
    public static final String TEST_IPV4_NETMASK = "255.255.255.0";
    public static final String TEST_IPV4_BROADCAST = "192.168.1.255";
    public static final String TEST_IPV6_ADDRESS = "fe80::1";
    public static final String TEST_IPV6_NETMASK = "128";
    public static final String TEST_IPV6_SCOPE = "host";
    private Long interfaceId;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        // Create a server + network interface in db
        MinionServer testMinionServer = MinionServerFactoryTest.createTestMinionServer(user);
        NetworkInterface testInterface = new NetworkInterface();
        testInterface.setName("eth0");
        testInterface.setHwaddr("aa:aa:aa");
        testInterface.setServer(testMinionServer);
        testMinionServer.getNetworkInterfaces().add(testInterface);

        // Save and flush to generate the interface ID
        TestUtils.saveAndFlush(testMinionServer);
        interfaceId = testInterface.getInterfaceId();
    }

    @Test
    void testSyncIPv4UpdatesExistingAddress() {
        //
        String newNetmask = "255.255.0.0";
        String newBroadcast = "192.168.255.255";

        // Setup - creating a IPv4 with the old values
        ServerNetAddress4 existingAddr = new ServerNetAddress4(interfaceId, TEST_IPV4_ADDRESS);
        existingAddr.setNetmask(TEST_IPV4_NETMASK);
        existingAddr.setBroadcast(TEST_IPV4_BROADCAST);
        ServerNetworkFactory.saveServerNetAddress4(existingAddr);

        // Create salt data with updated netmask and broadcast
        List<Network.INet> saltAddresses = new ArrayList<>();
        Network.INet saltAddr = new MockINet(TEST_IPV4_ADDRESS, newNetmask, newBroadcast);
        saltAddresses.add(saltAddr);

        // Sync
        IpAddressHandler.syncIPv4Addresses(interfaceId, saltAddresses);

        // Verify old values got updated
        List<ServerNetAddress4> dbAddresses = ServerNetworkFactory.findServerNetAddress4(interfaceId);
        assertEquals(1, dbAddresses.size());

        ServerNetAddress4 updated = dbAddresses.get(0);
        assertEquals(TEST_IPV4_ADDRESS, updated.getAddress());
        assertEquals(newNetmask, updated.getNetmask());
        assertEquals(newBroadcast, updated.getBroadcast());
    }

    @Test
    void testSyncIPv4InsertsNewAddress() {
        assertTrue(ServerNetworkFactory.findServerNetAddress4(interfaceId).isEmpty());

        // Create salt data with updated netmask and broadcast
        List<Network.INet> saltAddresses = new ArrayList<>();
        Network.INet saltAddr = new MockINet(TEST_IPV4_ADDRESS, TEST_IPV4_NETMASK, TEST_IPV4_BROADCAST);
        saltAddresses.add(saltAddr);

        // Sync
        IpAddressHandler.syncIPv4Addresses(interfaceId, saltAddresses);

        // Verify new record
        List<ServerNetAddress4> dbAddresses = ServerNetworkFactory.findServerNetAddress4(interfaceId);
        assertEquals(1, dbAddresses.size());

        ServerNetAddress4 address = dbAddresses.get(0);
        assertEquals(TEST_IPV4_ADDRESS, address.getAddress());
        assertEquals(TEST_IPV4_NETMASK, address.getNetmask());
        assertEquals(TEST_IPV4_BROADCAST, address.getBroadcast());
    }

    @Test
    void testSyncIPv4RemovesStaleAddress() {
        // Setup - creating a couple IPv4 addresses
        ServerNetworkFactory.saveServerNetAddress4(new ServerNetAddress4(interfaceId, TEST_IPV4_ADDRESS));
        ServerNetworkFactory.saveServerNetAddress4(new ServerNetAddress4(interfaceId, "192.168.1.101"));
        assertEquals(2, ServerNetworkFactory.findServerNetAddress4(interfaceId).size());

        // Sync
        IpAddressHandler.syncIPv4Addresses(interfaceId, new ArrayList<>());

        // Verify no IPv4 are associated to the interface
        assertTrue(ServerNetworkFactory.findServerNetAddress4(interfaceId).isEmpty());
    }

    @Test
    void testSyncIPv6UpdatesExistingAddress() {
        //
        String newPrefixLen = "64";
        String newScope = "link";

        // Setup - creating a IPv6 with the old values
        ServerNetAddress6 existingAddr = new ServerNetAddress6();
        existingAddr.setInterfaceId(interfaceId);
        existingAddr.setAddress(TEST_IPV6_ADDRESS);
        existingAddr.setNetmask(TEST_IPV6_NETMASK);
        existingAddr.setScope(TEST_IPV6_SCOPE);
        ServerNetworkFactory.saveServerNetAddress6(existingAddr);

        assertEquals(1, ServerNetworkFactory.findServerNetAddress6(interfaceId).size());

        // Create salt data with updated prefix and scope
        List<Network.INet6> saltAddresses = new ArrayList<>();
        Network.INet6 saltAddr = new MockINet6(TEST_IPV6_ADDRESS, newPrefixLen, newScope);
        saltAddresses.add(saltAddr);

        // Sync
        IpAddressHandler.syncIPv6Addresses(interfaceId, saltAddresses);

        // Verify old values got updated
        List<ServerNetAddress6> dbAddresses = ServerNetworkFactory.findServerNetAddress6(interfaceId);
        assertEquals(1, dbAddresses.size());

        ServerNetAddress6 updated = dbAddresses.get(0);
        assertEquals(TEST_IPV6_ADDRESS, updated.getAddress());
        assertEquals(newPrefixLen, updated.getNetmask());
        assertEquals(newScope, updated.getScope());
    }

    @Test
    void testSyncIPv6UpdatesExistingAddressUpdatingNetmaskOnly() {
        //
        String newPrefixLen = "64";

        // Setup - creating a IPv6 with the old values
        ServerNetAddress6 existingAddr = new ServerNetAddress6();
        existingAddr.setInterfaceId(interfaceId);
        existingAddr.setAddress(TEST_IPV6_ADDRESS);
        existingAddr.setNetmask(TEST_IPV6_NETMASK);
        existingAddr.setScope(TEST_IPV6_SCOPE);
        ServerNetworkFactory.saveServerNetAddress6(existingAddr);

        assertEquals(1, ServerNetworkFactory.findServerNetAddress6(interfaceId).size());

        // Create salt data with updated prefix and scope
        List<Network.INet6> saltAddresses = new ArrayList<>();
        Network.INet6 saltAddr = new MockINet6(TEST_IPV6_ADDRESS, newPrefixLen, TEST_IPV6_SCOPE);
        saltAddresses.add(saltAddr);

        // Sync
        IpAddressHandler.syncIPv6Addresses(interfaceId, saltAddresses);

        // Verify old values got updated
        List<ServerNetAddress6> dbAddresses = ServerNetworkFactory.findServerNetAddress6(interfaceId);
        assertEquals(1, dbAddresses.size());

        ServerNetAddress6 updated = dbAddresses.get(0);
        assertEquals(TEST_IPV6_ADDRESS, updated.getAddress());
        assertEquals(newPrefixLen, updated.getNetmask());
        assertEquals(TEST_IPV6_SCOPE, updated.getScope());
    }


    @Test
    void testSyncIPv6InsertsNewAddress() {
        assertTrue(ServerNetworkFactory.findServerNetAddress6(interfaceId).isEmpty());

        // Create salt data with updated prefix and scope
        List<Network.INet6> saltAddresses = new ArrayList<>();
        Network.INet6 saltAddr = new MockINet6(TEST_IPV6_ADDRESS, TEST_IPV6_NETMASK, TEST_IPV6_SCOPE);
        saltAddresses.add(saltAddr);

        // Sync
        IpAddressHandler.syncIPv6Addresses(interfaceId, saltAddresses);

        // Verify new record
        List<ServerNetAddress6> dbAddresses = ServerNetworkFactory.findServerNetAddress6(interfaceId);
        assertEquals(1, dbAddresses.size());

        ServerNetAddress6 address = dbAddresses.get(0);
        assertEquals(TEST_IPV6_ADDRESS, address.getAddress());
        assertEquals(TEST_IPV6_NETMASK, address.getNetmask());
        assertEquals(TEST_IPV6_SCOPE, address.getScope());
    }

    @Test
    void testSyncIPv6RemovesStaleAddress() {
        // Setup - creating a couple IPv6 addresses
        ServerNetAddress6 address1 = new ServerNetAddress6();
        address1.setInterfaceId(interfaceId);
        address1.setAddress(TEST_IPV6_ADDRESS);
        address1.setNetmask(TEST_IPV6_NETMASK);
        address1.setScope(TEST_IPV6_SCOPE);
        ServerNetworkFactory.saveServerNetAddress6(address1);

        ServerNetAddress6 address2 = new ServerNetAddress6();
        address2.setInterfaceId(interfaceId);
        address2.setAddress("2001:4860:4860::8888");
        address2.setNetmask(TEST_IPV6_NETMASK);
        address2.setScope(TEST_IPV6_SCOPE);
        ServerNetworkFactory.saveServerNetAddress6(address2);

        assertEquals(2, ServerNetworkFactory.findServerNetAddress6(interfaceId).size());

        // Sync
        IpAddressHandler.syncIPv6Addresses(interfaceId, new ArrayList<>());

        // Verify no IPv6 are associated to the interface
        assertTrue(ServerNetworkFactory.findServerNetAddress6(interfaceId).isEmpty());
    }

}
