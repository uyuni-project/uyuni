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

import static com.suse.manager.reactor.hardware.HardwareConstants.GRAIN_CPU_ARCH;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFQDN;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import com.suse.manager.reactor.utils.ValueMap;
import com.suse.salt.netapi.calls.modules.Network;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;


public class HardwareMapperTest extends BaseTestCaseWithUser {

    // mainframe sysinfo as returned by the salt module mainframesysinfo.read_values
    private static final String S390_SYSINFO =
            """
            Type: 2827
            Sequence Code: 0000000000061A23
            CPUs Total: 45
            VM00 Control Program: z/VM    6.3.0
            """;

    private static final String S390_DIGITAL_SERVER_ID = "Z-0000000000061A23";

    private MinionServer testServer;

    @BeforeEach
    public void setUp() throws Exception {
        this.testServer = MinionServerFactoryTest.createTestMinionServer(user);
    }

    private HardwareMapper mapperWithGrains(Map<String, String> grains) {
        return new HardwareMapper(testServer, new ValueMap(grains));
    }

    @Test
    @DisplayName("mapDmiInfo delegates to the DMI mapper without collecting errors")
    public void mapDmiInfoDelegatesToDmiMapper() {
        HardwareMapper hwMapper = mapperWithGrains(Map.of());

        hwMapper.mapDmiInfo(
                Map.of("vendor", "SeaBIOS"),
                Map.of("product_name", "Standard PC"),
                Map.of("manufacturer", "Intel"),
                Map.of("asset_tag", "tag-1")
        );

        assertTrue(hwMapper.getErrors().isEmpty());
        assertNotNull(testServer.getDmi());
        assertEquals("Standard PC", testServer.getDmi().getSystem());
        assertEquals("SeaBIOS", testServer.getDmi().getVendor());
        assertEquals("Intel", testServer.getDmi().getBoard());
        assertEquals("(chassis: ) (chassis: tag-1) (board: ) (system: )", testServer.getDmi().getAsset());
    }

    @Test
    @DisplayName("mapSysinfo delegates to the mainframe sysinfo mapper without collecting errors")
    public void mapSysinfoDelegatesToSysinfoMapper() {
        HardwareMapper hwMapper = mapperWithGrains(Map.of(GRAIN_CPU_ARCH, "s390x"));

        hwMapper.mapSysinfo(S390_SYSINFO);

        assertTrue(hwMapper.getErrors().isEmpty());
        Server zhost = ServerFactory.lookupForeignSystemByDigitalServerId(S390_DIGITAL_SERVER_ID);
        assertNotNull(zhost);
        assertEquals("IBM Mainframe z12 2827 0000000000061A23", zhost.getName());
        assertEquals("z/VM", zhost.getOs());
    }

    @Test
    public void testMapNetworkInfoSetsPrimaryFqdnFromMinionId() {
        MinionServer testMinionServer = MinionServerFactoryTest.createTestMinionServer(user);
        String minionId = testMinionServer.getMinionId();
        assertNotNull(minionId);

        HardwareMapper hwMapper = new HardwareMapper(testMinionServer, new ValueMap(new HashMap<>()));

        List<String> fqdns = List.of(minionId, "other-fqdn.com");

        Map<String, Network.Interface> interfaces = new HashMap<>();
        Network.Interface iface = new Network.Interface();
        interfaces.put("eth0", iface);

        hwMapper.mapNetworkInfo(interfaces, Optional.empty(), new HashMap<>(), fqdns);

        ServerFQDN primaryFqdn = testMinionServer.findPrimaryFqdn();
        assertNotNull(primaryFqdn);
        assertEquals(minionId, primaryFqdn.getName());
    }

}
