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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.scc.SCCCachingFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.VirtualInstanceType;
import com.redhat.rhn.manager.system.VirtualInstanceManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.utils.SaltUtils;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Tests for {@link VirtualInstanceSynchronizer}
 */
public class VirtualInstanceSynchronizerTest extends BaseTestCaseWithUser {

    // 32 hex chars, as reported by the 'uuid' grain
    private static final String TEST_UUID = "4c4c4544004d1010805cb4c04f435331";
    private static final String OTHER_UUID = "1a2b3c4d5e6f708192a3b4c5d6e7f809";
    private static final String HYPERVISOR_NAME = "guest-as-known-by-the-hypervisor";

    private static final int INITIAL_VCPUS = 1;
    private static final int VCPUS = 8;
    private static final long MEMORY = 4096L;
    private static final long HYPERVISOR_MEMORY = 1024L;

    private MinionServer testServer;
    private VirtualInstanceSynchronizer virtualInstanceSynchronizer;

    @BeforeEach
    public void setUp() {
        this.testServer = MinionServerFactoryTest.createTestMinionServer(user);
        this.virtualInstanceSynchronizer = new VirtualInstanceSynchronizer(testServer);
    }

    @Test
    public void testSynchronizeWhenUuidDoesNotExist() {
        assertNull(lookupSingleByUuid(TEST_UUID));

        virtualInstanceSynchronizer.synchronize(TEST_UUID, qemuType(), VCPUS, MEMORY, false);

        VirtualInstance created = lookupSingleByUuid(TEST_UUID);
        assertNotNull(created);
        assertEquals(testServer, created.getGuestSystem());
        assertEquals(testServer.getName(), created.getName());
        assertEquals(qemuType(), created.getType());
        assertEquals(VCPUS, created.getNumberOfCPUs());
        assertEquals(MEMORY, created.getTotalMemory());
        assertEquals(VirtualInstanceFactory.getInstance().getRunningState(), created.getState());
    }

    /**
     * Tests synchronizing a virtual instance updates the virtual instance information.
     * In this test, the memory and name are already set, so only the vCPUs should be updated.
     */
    @Test
    public void testSynchronizeOnlyUpdatesVcpus() {
        final Long initialMemory = 2L;
        addGuest(TEST_UUID, HYPERVISOR_NAME, qemuType(), null, initialMemory);

        VirtualInstance created = lookupSingleByUuid(TEST_UUID);
        assertNotNull(created);
        assertEquals(initialMemory, created.getTotalMemory());
        assertEquals(INITIAL_VCPUS, created.getNumberOfCPUs());

        virtualInstanceSynchronizer.synchronize(TEST_UUID, qemuType(), VCPUS, MEMORY, false);

        VirtualInstance updated = lookupSingleByUuid(TEST_UUID);
        assertNotNull(updated);
        assertEquals(HYPERVISOR_NAME, updated.getName());
        assertEquals(initialMemory, updated.getTotalMemory());
        assertEquals(VCPUS, updated.getNumberOfCPUs());
    }

    /**
     * Tests synchronizing a virtual instance updates the virtual instance information.
     * In this test, vCPUs, memory and name should be updated.
     */
    @Test
    public void testSynchronizeUpdatesVcpusAndNameAndMemory() {
        final  Long initialMemory = 0L;
        addGuest(TEST_UUID, null, qemuType(), null, initialMemory);

        VirtualInstance created = lookupSingleByUuid(TEST_UUID);
        assertNotNull(created);
        assertNull(created.getName());
        assertEquals(initialMemory, created.getTotalMemory());
        assertEquals(1, created.getNumberOfCPUs());

        virtualInstanceSynchronizer.synchronize(TEST_UUID, qemuType(), VCPUS, MEMORY, false);

        VirtualInstance updated = lookupSingleByUuid(TEST_UUID);
        assertNotNull(updated);
        assertEquals(testServer.getName(), updated.getName());
        assertEquals(MEMORY, updated.getTotalMemory());
        assertEquals(VCPUS, updated.getNumberOfCPUs());
    }

    /**
     * Foreign hosts (s390 and virtual host managers) don't report memory, so the guest value is used.
     */
    @Test
    public void testSynchronizeUsesGuestMemoryForForeignHost() throws Exception {
        Server foreignHost = ServerTestUtils.createForeignSystem(user, TestUtils.randomString());
        addGuest(TEST_UUID, HYPERVISOR_NAME, qemuType(), foreignHost, HYPERVISOR_MEMORY);

        VirtualInstance created = lookupSingleByUuid(TEST_UUID);
        assertNotNull(created);
        assertEquals(HYPERVISOR_MEMORY, created.getTotalMemory());
        assertEquals(INITIAL_VCPUS, created.getNumberOfCPUs());

        virtualInstanceSynchronizer.synchronize(TEST_UUID, qemuType(), VCPUS, MEMORY, false);

        VirtualInstance updated = lookupSingleByUuid(TEST_UUID);
        assertNotNull(updated);
        assertEquals(MEMORY, updated.getTotalMemory());
    }

    /**
     * The UUID of a VM may change, in which case the existing instance is re-keyed instead of duplicated.
     */
    @Test
    public void testSynchronizeReKeysInstanceFoundByGuestId() {
        addGuest(OTHER_UUID, HYPERVISOR_NAME, qemuType(), null, HYPERVISOR_MEMORY);

        assertNotNull(lookupSingleByUuid(OTHER_UUID));
        assertNull(lookupSingleByUuid(TEST_UUID));

        virtualInstanceSynchronizer.synchronize(TEST_UUID, qemuType(), VCPUS, MEMORY, false);

        assertNull(lookupSingleByUuid(OTHER_UUID));
        VirtualInstance updated = lookupSingleByUuid(TEST_UUID);
        assertNotNull(updated);
        assertEquals(testServer, updated.getGuestSystem());
    }

    /**
     * Synchronizing a virtual instance with a different type should
     * update the type and mark the instance as requiring re-registration.
     */
    @Test
    public void testSynchronizeChangesTypeAndRequiresReregWhenTypeDiffers() {
        VirtualInstanceType paraVirtType = VirtualInstanceFactory.getInstance().getParaVirtType();
        addGuest(TEST_UUID, HYPERVISOR_NAME, paraVirtType, null, HYPERVISOR_MEMORY);

        virtualInstanceSynchronizer.synchronize(TEST_UUID, qemuType(), VCPUS, MEMORY, false);

        VirtualInstance updated = lookupSingleByUuid(TEST_UUID);
        assertNotNull(updated);
        assertEquals(qemuType(), updated.getType());
        assertTrue(SCCCachingFactory.lookupCacheItemByServer(testServer)
                .orElseThrow(() -> new AssertionError("No SCC cache item for the guest"))
                .isSccRegistrationRequired());
    }

    /**
     * Test that the SLE 11 UUID fix is applied correctly, recreating the virtual instance with the fixed UUID.
     */
    @Test
    public void testSynchronizeAppliesSle11UuidFix() {
        String swappedUuid = SaltUtils.uuidToLittleEndian(TEST_UUID);
        addGuest(TEST_UUID, HYPERVISOR_NAME, qemuType(), null, HYPERVISOR_MEMORY);

        assertNotNull(lookupSingleByUuid(TEST_UUID));
        assertNull(lookupSingleByUuid(swappedUuid));

        virtualInstanceSynchronizer.synchronize(TEST_UUID, qemuType(), VCPUS, MEMORY, true);

        assertNull(lookupSingleByUuid(TEST_UUID));
        VirtualInstance fixed = lookupSingleByUuid(swappedUuid);
        assertNotNull(fixed);
        assertEquals(testServer, fixed.getGuestSystem());
        assertEquals(HYPERVISOR_NAME, fixed.getName());
    }

    // Class specific util methods
    private VirtualInstanceType qemuType() {
        return VirtualInstanceFactory.getInstance().getVirtualInstanceType("qemu");
    }

    private void addGuest(String uuid, String name, VirtualInstanceType type, Server host, long memory) {
        VirtualInstanceManager.addGuestVirtualInstance(uuid, name, type,
                VirtualInstanceFactory.getInstance().getRunningState(), host, testServer, INITIAL_VCPUS, memory);
    }

    private VirtualInstance lookupSingleByUuid(String uuid) {
        List<VirtualInstance> found = VirtualInstanceFactory.getInstance().lookupVirtualInstanceByUuid(uuid);
        if (found.isEmpty()) {
            return null;
        }
        assertEquals(1, found.size(), "Expected exactly one virtual instance for uuid " + uuid);
        return found.get(0);
    }
}
