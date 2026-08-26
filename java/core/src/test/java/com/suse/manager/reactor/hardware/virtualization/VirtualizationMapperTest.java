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
package com.suse.manager.reactor.hardware.virtualization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import com.suse.manager.reactor.hardware.HardwareConstants;
import com.suse.manager.reactor.utils.ValueMap;
import com.suse.manager.utils.SaltUtils;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Tests for {@link VirtualizationMapper}
 */
public class VirtualizationMapperTest extends BaseTestCaseWithUser {

    private static final String DASHED_UUID = "4c4c4544-004d-1010-805c-b4c04f435331";
    private static final String PLAIN_UUID = "4c4c4544004d1010805cb4c04f435331";
    private static final String INSTANCE_ID = "i-0123456789abcdef0";
    private static final String VIRTUAL_UUID = "i0123456789abcdef0";

    private MinionServer testServer;

    @BeforeEach
    public void setUp() throws Exception {
        this.testServer = MinionServerFactoryTest.createTestMinionServer(user);
    }

    @Test
    public void testMapVirtualizationInfoIgnoresPhysicalSystem() {
        Map<String, Object> grains = grains("physical", "");
        grains.put("uuid", DASHED_UUID);

        assertFalse(getMapVirtualizationInfo(grains, Optional.empty()).isPresent());
        assertNull(guestInstance());
    }

    @Test
    public void testMapVirtualizationInfoIgnoresXenDom0() {
        Map<String, Object> grains = grains("xen", "Xen Dom0");
        grains.put("uuid", DASHED_UUID);

        assertFalse(getMapVirtualizationInfo(grains, Optional.empty()).isPresent());
        assertNull(guestInstance());
    }

    @Test
    public void testMapVirtualizationInfoIgnoresGuestWithoutUuid() {
        Map<String, Object> grains = grains("kvm", "");

        assertFalse(getMapVirtualizationInfo(grains, Optional.empty()).isPresent());
        assertNull(guestInstance());
    }

    @Test
    public void testMapVirtualizationInfoCreatesGuestAndStripsDashesFromUuid() {
        Map<String, Object> grains = grains("kvm", "");
        grains.put("uuid", DASHED_UUID);
        grains.put("total_num_cpus", 4L);
        grains.put("mem_total", 2048L);

        assertFalse(getMapVirtualizationInfo(grains, Optional.empty()).isPresent());

        VirtualInstance guest = guestInstance();
        assertEquals(PLAIN_UUID, guest.getUuid());
        assertEquals(VirtualInstanceFactory.getInstance().getVirtualInstanceType("qemu"), guest.getType());
        assertEquals(4, guest.getNumberOfCPUs());
        assertEquals(2048L, guest.getTotalMemory());
    }

    @Test
    public void testMapVirtualizationInfoPrefersInstanceIdOverUuid() {
        Map<String, Object> grains = grains("nitro", "Amazon EC2");
        grains.put("uuid", DASHED_UUID);
        grains.put("instance_id", INSTANCE_ID);

        assertFalse(getMapVirtualizationInfo(grains, Optional.empty()).isPresent());

        VirtualInstance guest = guestInstance();
        assertEquals(VIRTUAL_UUID, guest.getUuid());
        assertEquals(VirtualInstanceFactory.getInstance().getVirtualInstanceType("aws_nitro"), guest.getType());
    }


    // Hitachi HVM LPARs not detected through the grains but through DMI
    @Test
    public void testMapVirtualizationInfoDetectsVirtageThroughDmi() {
        Map<String, Object> grains = grains("physical", "");
        grains.put("uuid", PLAIN_UUID);

        assertFalse(getMapVirtualizationInfo(grains,
                Optional.of(dmiSystem("Hitachi", "Compute Blade HVM LPAR"))).isPresent());

        VirtualInstance guest = guestInstance();
        assertEquals(PLAIN_UUID, guest.getUuid());
        assertEquals(VirtualInstanceFactory.getInstance().getVirtualInstanceType("virtage"), guest.getType());
    }

    @Test
    public void testMapVirtualizationInfoUsesFlexGuestUuidForVirtageWithoutUuid() {
        assertFalse(getMapVirtualizationInfo(grains("physical", ""),
                Optional.of(dmiSystem("HITACHI", "Compute Blade HVM LPAR"))).isPresent());

        assertEquals("flex-guest", guestInstance().getUuid());
    }

    /**
     * Tests other dmi manufacturers other than Hitachi are ignored
     */
    @Test
    public void testMapVirtualizationInfoIgnoresOtherDmiManufacturers() {
        assertFalse(getMapVirtualizationInfo(grains("physical", ""),
                Optional.of(dmiSystem("Dell Inc.", "PowerEdge R640"))).isPresent());
        assertNull(guestInstance());
    }

    @Test
    public void testMapVirtualizationInfoSwapsUuidOnSle11() {
        Map<String, Object> grains = grains("xen", "Xen PV DomU");
        grains.put("uuid", DASHED_UUID);
        grains.put("os_family", ServerConstants.OS_FAMILY_SUSE);
        grains.put("osrelease", "11.4");

        assertFalse(getMapVirtualizationInfo(grains, Optional.empty()).isPresent());

        assertEquals(SaltUtils.uuidToLittleEndian(PLAIN_UUID), guestInstance().getUuid());
    }

    /**
     * An 'instance_id' is always correct, so it must never be swapped even on SLE 11.
     */
    @Test
    public void testMapVirtualizationInfoDoesNotSwapInstanceIdOnSle11() {
        Map<String, Object> grains = grains("nitro", "Amazon EC2");
        grains.put("instance_id", INSTANCE_ID);
        grains.put("os_family", ServerConstants.OS_FAMILY_SUSE);
        grains.put("osrelease", "11.4");

        assertFalse(getMapVirtualizationInfo(grains, Optional.empty()).isPresent());

        assertEquals(VIRTUAL_UUID, guestInstance().getUuid());
    }

    @Test
    public void testMapVirtualizationInfoReportsMissingVirtualGrain() {
        Map<String, Object> grains = new HashMap<>();
        grains.put("uuid", DASHED_UUID);

        Optional<String> error = getMapVirtualizationInfo(grains, Optional.empty());

        assertEquals(Optional.of(HardwareConstants.GRAIN_VIRTUAL_HAS_NO_VALUE), error);
        assertNull(guestInstance());
    }

    /**
     * A missing 'virtual' grain is reported, but the mapping carries on: DMI may still identify a guest.
     */
    @Test
    public void testMapVirtualizationInfoReportsMissingVirtualGrainAndStillMapsDmi() {
        Map<String, Object> grains = new HashMap<>();
        grains.put("uuid", PLAIN_UUID);

        Optional<String> error = getMapVirtualizationInfo(grains,
                Optional.of(dmiSystem("HITACHI", "Compute Blade HVM LPAR")));

        assertEquals(Optional.of(HardwareConstants.GRAIN_VIRTUAL_HAS_NO_VALUE), error);
        assertEquals(VirtualInstanceFactory.getInstance().getVirtualInstanceType("virtage"),
                guestInstance().getType());
    }

    /**
     * A UUID that can't be swapped must be reported as an error instead of failing the whole hardware refresh.
     */
    @Test
    public void testMapVirtualizationInfoReturnsErrorOnUnexpectedFailure() {
        Map<String, Object> grains = grains("kvm", "");
        grains.put("uuid", "not-a-uuid");
        grains.put("os_family", ServerConstants.OS_FAMILY_SUSE);
        grains.put("osrelease", "11.4");

        Optional<String> error = getMapVirtualizationInfo(grains, Optional.empty());

        assertTrue(error.isPresent());
        assertTrue(error.get().startsWith("Virtualization mapping failed:"), error.orElse(""));
    }

    @ParameterizedTest
    @MethodSource("needsSle11UuidFixData")
    public void testNeedsSle11UuidFix(String osFamily, String osRelease, String instanceId, boolean expected) {
        Map<String, Object> grains = new HashMap<>();
        grains.put("os_family", osFamily);
        grains.put("osrelease", osRelease);
        VirtualizationMapper virtualizationMapper = new VirtualizationMapper(testServer, new ValueMap(grains));

        assertEquals(expected, virtualizationMapper.needsSle11UuidFix(instanceId));
    }

    private static Stream<Arguments> needsSle11UuidFixData() {
        return Stream.of(
                Arguments.of(null, null, null, false),
                Arguments.of(StringUtils.EMPTY, StringUtils.EMPTY, StringUtils.EMPTY, false),
                Arguments.arguments(ServerConstants.OS_FAMILY_SUSE, "11.4", StringUtils.EMPTY, true),
                Arguments.arguments(ServerConstants.OS_FAMILY_SUSE, "11", StringUtils.EMPTY, true),
                Arguments.arguments(ServerConstants.OS_FAMILY_SUSE, "11.4", INSTANCE_ID, false),
                Arguments.arguments(ServerConstants.OS_FAMILY_SUSE, "12.5", StringUtils.EMPTY, false),
                Arguments.arguments(ServerConstants.OS_FAMILY_SUSE, StringUtils.EMPTY, StringUtils.EMPTY, false),
                Arguments.arguments(ServerConstants.OS_FAMILY_SUSE, "RedHat", StringUtils.EMPTY, false),
                Arguments.arguments(StringUtils.EMPTY, "RedHat", StringUtils.EMPTY, false)
        );
    }

    // Class specific util methods
    private Optional<String> getMapVirtualizationInfo(
            Map<String, Object> grains,
            Optional<Map<String, Object>> smbiosRecordsSystem
    ) {
        return new VirtualizationMapper(testServer, new ValueMap(grains)).mapVirtualizationInfo(smbiosRecordsSystem);
    }

    private static Map<String, Object> grains(String virtual, String virtualSubtype) {
        Map<String, Object> grains = new HashMap<>();
        grains.put("virtual", virtual);
        grains.put("virtual_subtype", virtualSubtype);
        return grains;
    }

    private static Map<String, Object> dmiSystem(String manufacturer, String productName) {
        Map<String, Object> dmi = new HashMap<>();
        dmi.put("manufacturer", manufacturer);
        dmi.put("product_name", productName);
        return dmi;
    }

    private VirtualInstance guestInstance() {
        return VirtualInstanceFactory.getInstance().lookupByGuestId(testServer.getId());
    }

}
