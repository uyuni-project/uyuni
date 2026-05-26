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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.webui.services.SaltGrains;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;


public class HardwareMapperTest extends BaseTestCaseWithUser {

    // the salt state hardware/profileupdate.sls runs the module mainframesysinfo.read_values whose implementation,
    // in turn, gets the results of "/usr/bin/read_values -s" or, alternatively of "cat /proc/sysinfo"
    // the following simulated strings come from the salt state response:
    //
    //"module_|-mainframe-sysinfo_|-mainframesysinfo.read_values_|-run": {
    //    "changes": {
    //        "ret": [SIMULATED STRING]

    private static final String SERVER_1_DIGITAL_SERVER_ID = "Z-0000000000061A23";

    // usr/bin/read_values -s
    private static final String SERVER_1_STATE_CHANGES_RET_READ_VALUES =
            """
            Version: 1.0.0\n
            Type: 2827\n
            Sequence Code: 0000000000061A23\n
            CPUs Total: 45\n
            CPUs IFL: 45\n
            LPAR Number: 10\n
            LPAR Name: LPA\n
            LPAR Characteristics: Shared\n
            LPAR CPUs Total: 6\n
            LPAR CPUs IFL: 6\n
            VM00 Name: LINUX123\n
            VM00 Control Program: z/VM    6.3.0   \n
            VM00 CPUs Total: 1\n
            VM00 IFLs: 1
            """;


    private static final String SERVER_2_DIGITAL_SERVER_ID = "Z-00000000000612A3";

    // cat /proc/sysinfo
    private static final String SERVER_2_STATE_CHANGES_RET_PROC_SYSINFO =
            """
            Manufacturer:         IBM\n
            Type:                 8561\n
            LIC Identifier:       012345ab6789012c\n
            Model:                400              LT1\n
            Sequence Code:        00000000000612A3\n
            Plant:                02\n
            Model Capacity:       400              00000000\n
            Capacity Adj. Ind.:   100\n
            Capacity Ch. Reason:  0\n
            Capacity Transient:   0\n
            Type 1 Percentage:    0\n
            Type 2 Percentage:    0\n
            Type 3 Percentage:    0\n
            Type 4 Percentage:    0\n
            Type 5 Percentage:    0\n
            \n
            CPUs Total:           71\n
            CPUs Configured:      0\n
            CPUs Standby:         0\n
            CPUs Reserved:        71\n
            CPUs G-MTID:          0\n
            CPUs S-MTID:          1\n
            Capability:           3085\n
            Nominal Capability:   3085\n
            Secondary Capability: 416\n
            Adjustment 02-way:    62750\n
            Adjustment 70-way:    46037\n
            Adjustment 71-way:    46037\n
            \n
            LPAR Number:          65\n
            LPAR Characteristics: Shared\n
            LPAR Name:            ZL41\n
            LPAR Adjustment:      28\n
            LPAR CPUs Total:      2\n
            LPAR CPUs Configured: 2\n
            LPAR CPUs Standby:    0\n
            LPAR CPUs Reserved:   0\n
            LPAR CPUs Dedicated:  0\n
            LPAR CPUs Shared:     2\n
            LPAR CPUs G-MTID:     0\n
            LPAR CPUs S-MTID:     1\n
            LPAR CPUs PS-MTID:    0\n
            \n
            VM00 Name:            DUMMYSRV\n
            VM00 Control Program: z/VM    7.4.0\n
            VM00 Adjustment:      500\n
            VM00 CPUs Total:      1\n
            VM00 CPUs Configured: 1\n
            VM00 CPUs Standby:    0\n
            VM00 CPUs Reserved:   0\n
            """;

    private static final String SERVER_3_DIGITAL_SERVER_ID = "Z-00000000000987F6";

    // usr/bin/read_values -s
    private static final String SERVER_3_STATE_CHANGES_RET_READ_VALUES =
            """
            Version: 1.0.6\n
            Type : 3932\n
            Type Name : IBM z16 AGZ\n
            Sequence Code : 00000000000987F6\n
            LPAR Number : 21\n
            LPAR Name : GHI1LM23\n
            LPAR Characteristics : Shared\n
            LPAR CPUs Total : 16\n
            LPAR CPUs IFL : 16\n
            VM00 Name : k8s_9ab8\n
            VM00 Control Program : KVM/Linux\n
            VM00 CPUs Total : 1\n
            VM00 IFLs : 1\n
            """;

    // cat /proc/sysinfo
    private static final String SERVER_3_STATE_CHANGES_RET_PROC_SYSINFO =
            """
            Manufacturer:         IBM            \n
            Type:                 3932\n
            LIC Identifier:       012345ab678c90d1\n
            Model:                A00              AGZ            \n
            Sequence Code:        00000000000987F6\n
            Plant:                02 \n
            Model Capacity:       A00              00000000\n
            Capacity Adj. Ind.:   100\n
            Capacity Ch. Reason:  0\n
            Capacity Transient:   0\n
            Type 1 Percentage:    0\n
            Type 2 Percentage:    0\n
            Type 3 Percentage:    0\n
            Type 4 Percentage:    0\n
            Type 5 Percentage:    0\n
            \n
            CPUs Total:           68\n
            CPUs Configured:      0\n
            CPUs Standby:         0\n
            CPUs Reserved:        68\n
            CPUs G-MTID:          0\n
            CPUs S-MTID:          1\n
            Capability:           7992\n
            Nominal Capability:   7992\n
            Secondary Capability: 425\n
            Adjustment 02-way:    62422\n
            Adjustment 67-way:    57815\n
            Adjustment 68-way:    57815\n
            \n
            LPAR Number:          21\n
            LPAR Characteristics: Shared\s\n
            LPAR Name:            XYZ1AB23\n
            LPAR Adjustment:      235\n
            LPAR CPUs Total:      16\n
            LPAR CPUs Configured: 16\n
            LPAR CPUs Standby:    0\n
            LPAR CPUs Reserved:   0\n
            LPAR CPUs Dedicated:  0\n
            LPAR CPUs Shared:     16\n
            LPAR CPUs G-MTID:     0\n
            LPAR CPUs S-MTID:     1\n
            LPAR CPUs PS-MTID:    1\n
            LPAR Extended Name:  \s\n
            LPAR UUID:            01a2b345-67cd-89e0-1f23-456789012345\n
            \n
            VM00 Name:            k8s_9ab8\n
            VM00 Control Program: KVM/Linux      \s\n
            VM00 Adjustment:      1000\n
            VM00 CPUs Total:      1\n
            VM00 CPUs Configured: 1\n
            VM00 CPUs Standby:    0\n
            VM00 CPUs Reserved:   0\n
            VM00 Extended Name:   k8s_101a2b345101a2b345101a2b345101a2_67cd_89e01f23-4567-8901-2345-89e01f23dc4f\n
            VM00 UUID:            01a2b345-67cd-89e0-1f23-456789012345\n
            """;

    private HardwareMapper createTestS390HwMapper() {
        MinionServer testMinionServer = MinionServerFactoryTest.createTestMinionServer(user);
        Map<String, String> grains = new HashMap<>() {{
            put(SaltGrains.CPUARCH.getValue(), "s390x");
        }};
        return new HardwareMapper(testMinionServer, new ValueMap(grains));
    }

    private void assertHostOsString(String expectedOsString, String digitalServerId) {
        Server zHost = ServerFactory.lookupForeignSystemByDigitalServerId(digitalServerId);
        assertEquals(expectedOsString, zHost.getOs());
    }

    private void assertComputeOsString(String expectedOsString, HardwareMapper hwMapper, String readValuesOutput) {
        Map<String, String> sysValues = hwMapper.getSysValuesMap(readValuesOutput);
        String os = hwMapper.computeOsStringForS390Arch(sysValues);
        assertEquals(expectedOsString, os);
    }


    private static Stream<Arguments> allServersInfo() {
        return Stream.of(
                Arguments.of("z/VM", SERVER_1_DIGITAL_SERVER_ID, SERVER_1_STATE_CHANGES_RET_READ_VALUES),
                Arguments.of("z/VM", SERVER_2_DIGITAL_SERVER_ID, SERVER_2_STATE_CHANGES_RET_PROC_SYSINFO),
                Arguments.of("KVM/Linux", SERVER_3_DIGITAL_SERVER_ID, SERVER_3_STATE_CHANGES_RET_READ_VALUES),
                Arguments.of("KVM/Linux", SERVER_3_DIGITAL_SERVER_ID, SERVER_3_STATE_CHANGES_RET_PROC_SYSINFO)
        );
    }

    @ParameterizedTest
    @MethodSource("allServersInfo")
    @DisplayName("mapSysinfo correctly showing OS value on s390 hypervisors")
    public void mapSysinfoShowingCorrectValues(String expectedOsString, String digitalServerId,
                                                           String readValuesOutput) {
        HardwareMapper hwMapper = createTestS390HwMapper();
        assertNull(ServerFactory.lookupForeignSystemByDigitalServerId(digitalServerId));

        hwMapper.mapSysinfo(readValuesOutput);

        assertNotNull(ServerFactory.lookupForeignSystemByDigitalServerId(digitalServerId));
        assertComputeOsString(expectedOsString, hwMapper, readValuesOutput);
        assertHostOsString(expectedOsString, digitalServerId);
    }

}
