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

import static com.redhat.rhn.domain.server.NetworkInterfaceTest.TEST_MAC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.ServerFQDN;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.utils.salt.custom.SumaUtil;
import com.suse.salt.netapi.calls.modules.Network;

import org.apache.commons.lang3.SerializationUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Tests for {@link NetworkMapper}
 */
public class NetworkMapperTest extends BaseTestCaseWithUser {

    public static final String ETH_0 = "eth0";
    public static final String ETH_1 = "eth1";
    public static final String TEST_EXAMPLE_COM_FQDN = "test.example.suse";
    public static final String TEST_HW_ADDR = "00:11:22:33:44:55";

    private NetworkMapper mapper;
    private MinionServer testServer;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        this.testServer = MinionServerFactoryTest.createTestMinionServer(user);

        Map<String, Object> grainsMap = new HashMap<>();
        grainsMap.put("fqdn", TEST_EXAMPLE_COM_FQDN);
        ValueMap grains = new ValueMap(grainsMap);
        this.mapper = new NetworkMapper(testServer, grains);
    }

    @Test
    void testMapNetworkInfoWithEmptyInterfaces() {
        Optional<String> error = mapper.mapNetworkInfo(
                new HashMap<>(),
                Optional.empty(),
                new HashMap<>(),
                new ArrayList<>()
        );

        assertTrue(error.isPresent());
        assertTrue(error.get().contains("'network.interfaces' returned an empty value"));
    }

    /**
     * Tests {@link NetworkMapper#mapNetworkInfo(Map, Optional, Map, List)} such that it expects server to set its
     * hostname based on the 'fqdn' grain value
     */
    @Test
    void testMapNetworkInfoSetsHostnameFromGrains() {
        // Pre-conditions
        assertNull(testServer.getHostname());

        // Minimal settings
        String defaultInterfaceName = testServer.getNetworkInterfaces().iterator().next().getName();
        Map<String, Network.Interface> interfaces = new HashMap<>();
        MockInterface mockInterface = new MockInterface(TEST_HW_ADDR, null, null);
        interfaces.put(defaultInterfaceName, mockInterface);

        Map<String, Optional<String>> netModules = new HashMap<>();
        netModules.put(defaultInterfaceName, Optional.empty());

        //
        Optional<String> error = mapper.mapNetworkInfo(
                interfaces,
                Optional.empty(),
                netModules,
                new ArrayList<>()
        );

        assertFalse(error.isPresent());

        // Reload from database
        TestUtils.flushAndEvict(testServer);
        MinionServer reloaded = TestUtils.reload(testServer);

        assertEquals(TEST_EXAMPLE_COM_FQDN, reloaded.getHostname());
    }

    /**
     * Tests {@link NetworkMapper#mapNetworkInfo(Map, Optional, Map, List)} such that it updates existing interface
     * instead of creating a new one when the name matches, and updates the module information from the provided map
     */
    @Test
    void testMapNetworkInfoUpdatesExistingInterface() {
        String expectedModule = "e1000e";

        // Pre-conditions
        NetworkInterface original = SerializationUtils.clone(
                testServer.getNetworkInterfaces().iterator().next()
        );
        assertEquals(TEST_MAC, original.getHwaddr());
        assertEquals("test", original.getModule());

        //
        MockInterface mockInterface = new MockInterface(TEST_HW_ADDR, null, null);
        Map<String, Network.Interface> interfaces = new HashMap<>();
        interfaces.put(original.getName(), mockInterface);

        Map<String, Optional<String>> netModules = new HashMap<>();
        netModules.put(original.getName(), Optional.of(expectedModule));

        Optional<String> error = mapper.mapNetworkInfo(
                interfaces,
                Optional.empty(),
                netModules,
                new ArrayList<>()
        );

        assertFalse(error.isPresent());

        // Verify interface was updated
        TestUtils.flushAndEvict(testServer);
        MinionServer reloaded = TestUtils.reload(testServer);

        assertEquals(1, reloaded.getNetworkInterfaces().size());
        NetworkInterface updated = reloaded.getNetworkInterfaces().iterator().next();

        assertEquals(original.getInterfaceId(), updated.getInterfaceId());
        assertEquals(original.getName(), updated.getName());
        assertEquals(TEST_HW_ADDR, updated.getHwaddr());
        assertEquals(expectedModule, updated.getModule());
    }


   /**
    * Tests {@link NetworkMapper#mapNetworkInfo(Map, Optional, Map, List)} such that it replaces the existing
    * non used interface for nww ones provided
    */
    @Test
    void testMapNetworkInfoReplacesInterfaces() {
        // Pre-conditions
        Set<NetworkInterface> networkInterfaces = testServer.getNetworkInterfaces();
        assertEquals(1, networkInterfaces.size());
        NetworkInterface original = SerializationUtils.clone(
                testServer.getNetworkInterfaces().iterator().next()
        );

        // Create two interfaces with IPv4 addresses
        List<Network.INet> eth0Addresses = new ArrayList<>();
        eth0Addresses.add(new MockINet("192.168.1.100", null, null));

        List<Network.INet> eth1Addresses = new ArrayList<>();
        eth1Addresses.add(new MockINet("10.0.0.1", null, null));

        Map<String, Network.Interface> interfaces = new HashMap<>();
        interfaces.put(ETH_0, new MockInterface(TEST_HW_ADDR, eth0Addresses, null));
        interfaces.put(ETH_1, new MockInterface("11:22:33:44:55:66", eth1Addresses, null));

        Map<String, Optional<String>> netModules = new HashMap<>();
        netModules.put(ETH_0, Optional.empty());
        netModules.put(ETH_1, Optional.empty());

        Optional<String> error = mapper.mapNetworkInfo(
                interfaces,
                Optional.empty(),
                netModules,
                new ArrayList<>()
        );

        assertFalse(error.isPresent());

        // Verify original interface was removed and there are two new ones
        TestUtils.flushAndEvict(testServer);
        MinionServer reloaded = TestUtils.reload(testServer);

        Set<NetworkInterface> newInterfaces = reloaded.getNetworkInterfaces();
        assertEquals(2, newInterfaces.size());
        assertFalse(newInterfaces.stream().anyMatch(i -> i.getInterfaceId().equals(original.getInterfaceId())));
        assertTrue(newInterfaces.stream().anyMatch(i -> ETH_0.equals(i.getName())));
        assertTrue(newInterfaces.stream().anyMatch(i -> ETH_1.equals(i.getName())));
    }

    /**
     * Tests {@link NetworkMapper#mapNetworkInfo(Map, Optional, Map, List)} such that it sets the primary interface
     * based on the provided primary IPs, preferring IPv4 over IPv6 when both are provided
     */
    @Test
    void testMapNetworkInfoSetsPrimaryInterfaceBasedOnIPv4() {
        String address = "10.0.0.1";

        // Pre-conditions
        assertFalse(
                testServer.getNetworkInterfaces().stream().anyMatch(i -> "Y".equals(i.getPrimary()))
        );

        // Create three interfaces with IPv4 addresses
        List<Network.INet> eth0Addresses = new ArrayList<>();
        eth0Addresses.add(new MockINet("192.168.1.100", null, null));

        List<Network.INet> eth1Addresses = new ArrayList<>();
        eth1Addresses.add(new MockINet(address, null, null));

        Map<String, Network.Interface> interfaces = new HashMap<>();
        interfaces.put(ETH_0, new MockInterface(TEST_HW_ADDR, eth0Addresses, null));
        interfaces.put(ETH_1, new MockInterface("11:22:33:44:55:66", eth1Addresses, null));

        Map<String, Optional<String>> netModules = new HashMap<>();
        netModules.put(ETH_0, Optional.empty());
        netModules.put(ETH_1, Optional.empty());

        // Set primary IPv4 to match eth1
        Map<SumaUtil.IPVersion, SumaUtil.IPRoute> primaryIps = new HashMap<>();
        primaryIps.put(SumaUtil.IPVersion.IPV4, new MockIPRoute(address));

        Optional<String> error = mapper.mapNetworkInfo(
                interfaces,
                Optional.of(primaryIps),
                netModules,
                new ArrayList<>()
        );

        assertFalse(error.isPresent());

        // Verify eth1 is marked as primary
        TestUtils.flushAndEvict(testServer);
        MinionServer reloaded = TestUtils.reload(testServer);

        assertEquals(2, reloaded.getNetworkInterfaces().size());
        Optional<NetworkInterface> primaryInterface = reloaded.getNetworkInterfaces().stream()
                .filter(i -> "Y".equals(i.getPrimary()))
                .findFirst();
        assertTrue(primaryInterface.isPresent());
        assertEquals(ETH_1, primaryInterface.get().getName());
    }


    /**
     * Tests {@link NetworkMapper#mapNetworkInfo(Map, Optional, Map, List)} such that it sets the primary interface
     * to an IPv6 as fallback when provided IPv4 doesn't match any of the interfaces
     */
    @Test
    void testMapNetworkInfoSetsPrimaryInterfaceBasedOnIPv6WhenNoIPv4Match() {
        String ipv6Address = "fe80::1";

        // Pre-conditions
        assertFalse(
                testServer.getNetworkInterfaces().stream().anyMatch(i -> "Y".equals(i.getPrimary()))
        );

        // Create two interfaces - one with IPv4, one with IPv6
        List<Network.INet> eth0Addresses = new ArrayList<>();
        eth0Addresses.add(new MockINet("192.168.1.100", null, null));

        List<Network.INet6> eth1Addresses = new ArrayList<>();
        eth1Addresses.add(new MockINet6(ipv6Address, null, null));

        Map<String, Network.Interface> interfaces = new HashMap<>();
        interfaces.put(ETH_0, new MockInterface(TEST_HW_ADDR, eth0Addresses, null));
        interfaces.put(ETH_1, new MockInterface("11:22:33:44:55:66", null, eth1Addresses));

        Map<String, Optional<String>> netModules = new HashMap<>();
        netModules.put(ETH_0, Optional.empty());
        netModules.put(ETH_1, Optional.empty());

        // Set primary IPv4 to a non-matching address, and IPv6 to match eth1
        Map<SumaUtil.IPVersion, SumaUtil.IPRoute> primaryIps = new HashMap<>();
        primaryIps.put(SumaUtil.IPVersion.IPV4, new MockIPRoute("10.0.0.100"));
        primaryIps.put(SumaUtil.IPVersion.IPV6, new MockIPRoute(ipv6Address));

        Optional<String> error = mapper.mapNetworkInfo(
                interfaces,
                Optional.of(primaryIps),
                netModules,
                new ArrayList<>()
        );

        assertFalse(error.isPresent());

        // Verify eth1 is marked as primary as fallback to IPv6
        TestUtils.flushAndEvict(testServer);
        MinionServer reloaded = TestUtils.reload(testServer);

        assertEquals(2, reloaded.getNetworkInterfaces().size());

        Optional<NetworkInterface> primaryIface = reloaded.getNetworkInterfaces().stream()
                .filter(iface -> "Y".equals(iface.getPrimary()))
                .findFirst();

        assertTrue(primaryIface.isPresent());
        assertEquals(ETH_1, primaryIface.get().getName());
    }

    /**
     * Tests {@link NetworkMapper#mapNetworkInfo(Map, Optional, Map, List)} such that it doesn't create any FQDNs
     */
    @Test
    void testMapNetworkInfoWithEmptyFqdnsList() {
        // Pre-conditions
        assertTrue(testServer.getFqdns().isEmpty());

        // Minimal settings
        String defaultInterfaceName = testServer.getNetworkInterfaces().iterator().next().getName();
        Map<String, Network.Interface> interfaces = new HashMap<>();
        MockInterface mockInterface = new MockInterface(TEST_HW_ADDR, null, null);
        interfaces.put(defaultInterfaceName, mockInterface);

        Map<String, Optional<String>> netModules = new HashMap<>();
        netModules.put(defaultInterfaceName, Optional.empty());

        //
        Optional<String> error = mapper.mapNetworkInfo(
                interfaces,
                Optional.empty(),
                netModules,
                new ArrayList<>()
        );

        assertFalse(error.isPresent());

        // Save and verify no FQDNs were created
        ServerFactory.save(testServer);
        TestUtils.flushAndEvict(testServer);
        MinionServer reloaded = TestUtils.reload(testServer);

        assertTrue(reloaded.getFqdns().isEmpty());
    }

    /**
     * Tests {@link NetworkMapper#mapNetworkInfo(Map, Optional, Map, List)} such that it syncs the server FQDNs
     * with the provided list and sets the primary FQDN
     */
    @Test
    void testMapNetworkInfoUpdatesFqdns() {
        // Pre-conditions
        assertTrue(testServer.getFqdns().isEmpty());

        String host1 = "host1.example.suse";
        String host2 = "host2.example.suse";

        testServer.getFqdns().add(new ServerFQDN(testServer, host1));
        testServer.getFqdns().add(new ServerFQDN(testServer, host2));
        testServer.getFqdns().add(new ServerFQDN(testServer, TEST_EXAMPLE_COM_FQDN));

        // Minimal settings
        String defaultInterfaceName = testServer.getNetworkInterfaces().iterator().next().getName();
        Map<String, Network.Interface> interfaces = new HashMap<>();
        MockInterface mockInterface = new MockInterface(TEST_HW_ADDR, null, null);
        interfaces.put(defaultInterfaceName, mockInterface);

        Map<String, Optional<String>> netModules = new HashMap<>();
        netModules.put(defaultInterfaceName, Optional.empty());

        Optional<String> error = mapper.mapNetworkInfo(
                interfaces,
                Optional.empty(),
                netModules,
                Arrays.asList(host1, TEST_EXAMPLE_COM_FQDN)
        );

        assertFalse(error.isPresent());

        // Save and verify FQDN host2 was removed, the other 2 were added and primaryFqdn was set
        ServerFactory.save(testServer);
        TestUtils.flushAndEvict(testServer);
        MinionServer reloaded = TestUtils.reload(testServer);

        assertEquals(2, reloaded.getFqdns().size());
        assertTrue(reloaded.getFqdns().stream().anyMatch(f -> f.getName().equals(host1)));
        assertFalse(reloaded.getFqdns().stream().anyMatch(f -> f.getName().equals(host2)));
        assertTrue(reloaded.getFqdns().stream().anyMatch(f -> f.getName().equals(TEST_EXAMPLE_COM_FQDN)));

        Optional<ServerFQDN> primaryFqdn = reloaded.getFqdns().stream()
                .filter(ServerFQDN::isPrimary)
                .findFirst();
        assertTrue(primaryFqdn.isPresent());
        assertEquals(TEST_EXAMPLE_COM_FQDN, primaryFqdn.get().getName());
    }

    /**
     * Mock implementation of Network.Interface for testing
     */
    private static class MockInterface extends Network.Interface {
        private final String hwAddr;
        private final List<Network.INet> inet;
        private final List<Network.INet6> inet6;

        MockInterface(String hwAddrIn, List<Network.INet> inetIn, List<Network.INet6> inet6In) {
            this.hwAddr = hwAddrIn;
            this.inet = inetIn != null ? inetIn : new ArrayList<>();
            this.inet6 = inet6In != null ? inet6In : new ArrayList<>();
        }

        @Override
        public String getHWAddr() {
            return hwAddr;
        }

        @Override
        public List<Network.INet> getInet() {
            return inet;
        }

        @Override
        public List<Network.INet6> getInet6() {
            return inet6;
        }
    }

    /**
     * Mock implementation of SumaUtil.IPRoute for testing primary IP detection
     */
    private static class MockIPRoute extends SumaUtil.IPRoute {
        private final String source;

        MockIPRoute(String sourceIn) {
            this.source = sourceIn;
        }

        @Override
        public String getSource() {
            return source;
        }
    }

}
