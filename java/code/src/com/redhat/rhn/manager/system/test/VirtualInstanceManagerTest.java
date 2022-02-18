/*
 * Copyright (c) 2016--2021 SUSE LLC
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
package com.redhat.rhn.manager.system.test;

import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.VirtualInstanceType;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.VirtualInstanceManager;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.webui.utils.salt.custom.GuestProperties;
import com.suse.manager.webui.utils.salt.custom.VmInfo;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * VirtualInstanceManagerTest
 */
public class VirtualInstanceManagerTest extends RhnBaseTestCase {

    private static final String EVENT_TYPE_FULLREPORT = "fullreport";
    private static final String EVENT_TYPE_EXISTS = "exists";
    private static final String EVENT_TYPE_REMOVED = "removed";

    private static final String TARGET_DOMAIN = "domain";

    private static final String VIRTTYPE_PARA = "para_virtualized";
    private static final String VIRTTYPE_FULL = "fully_virtualized";

    private static final String STATE_RUNNING = "running";
    private static final String STATE_PAUSED = "paused";
    private static final String STATE_STOPPED = "stopped";
    private static final String STATE_CRASHED = "crashed";
    private static final String STATE_UNKNOWN = "unknown";

    private User user;
    private Server server;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        server = ServerFactoryTest.createTestServer(user, true);
    }

    public void testInitialPlanExec() throws Exception {

        Long id = server.getId();
        List<VmInfo> plan = new LinkedList<>();

        plan.add(new VmInfo(1479479686, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(2048, "vm1", STATE_RUNNING, "38a4e1c14d8e440780b3b59745ba9ce5", 2, VIRTTYPE_PARA)));
        plan.add(new VmInfo(1479479686, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(4096, "vm2", STATE_RUNNING, "a4f100d349954f50a24f80fec75e3f5d", 4, VIRTTYPE_FULL)));

        VirtualInstanceManager.updateGuestsVirtualInstances(server, plan);

        Server test = SystemManager.lookupByIdAndUser(id, user);
        assertNotNull(test);
        assertEquals(2, test.getGuests().size());
    }

    public void testAddPlanExec() throws Exception {

        Long id = server.getId();
        List<VmInfo> plan = new LinkedList<>();

        plan.add(new VmInfo(1479479686, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(2048, "vm1", STATE_RUNNING, "38a4e1c14d8e440780b3b59745ba9ce5", 2, VIRTTYPE_PARA)));
        plan.add(new VmInfo(1479479686, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(4096, "vm2", STATE_RUNNING, "a4f100d349954f50a24f80fec75e3f5d", 4, VIRTTYPE_PARA)));

        VirtualInstanceManager.updateGuestsVirtualInstances(server, plan);

        Server test = SystemManager.lookupByIdAndUser(id, user);
        assertNotNull(test);
        assertEquals(2, test.getGuests().size());

        plan = new LinkedList<>();

        plan.add(new VmInfo(1479479699, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(1024, "vm3", STATE_RUNNING, "420ea57f7035ee1de2c1e23fe29f5ca7", 1, VIRTTYPE_PARA)));

        VirtualInstanceManager.updateGuestsVirtualInstances(server, plan);

        test = SystemManager.lookupByIdAndUser(id, user);
        assertNotNull(test);
        assertEquals(3, test.getGuests().size());
    }

    public void testUpdatePlanExec() throws Exception {

        Long id = server.getId();
        List<VmInfo> plan = new LinkedList<>();

        plan.add(new VmInfo(1479479686, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(2048, "vm1", STATE_RUNNING, "38a4e1c14d8e440780b3b59745ba9ce5", 2, VIRTTYPE_PARA)));
        plan.add(new VmInfo(1479479686, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(4096, "vm2", STATE_RUNNING, "a4f100d349954f50a24f80fec75e3f5d", 4, VIRTTYPE_PARA)));

        VirtualInstanceManager.updateGuestsVirtualInstances(server, plan);

        Server test = SystemManager.lookupByIdAndUser(id, user);
        assertNotNull(test);
        assertEquals(2, test.getGuests().size());

        plan = new LinkedList<>();

        plan.add(new VmInfo(1479479699, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(2048, "vm1", STATE_PAUSED, "38a4e1c14d8e440780b3b59745ba9ce5", 2, VIRTTYPE_PARA)));
        plan.add(new VmInfo(1479479699, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(1024, "vmRename", STATE_STOPPED, "a4f100d349954f50a24f80fec75e3f5d",
                        1, VIRTTYPE_PARA)));

        VirtualInstanceManager.updateGuestsVirtualInstances(server, plan);

        test = SystemManager.lookupByIdAndUser(id, user);
        assertNotNull(test);
        assertEquals(2, test.getGuests().size());

        for (VirtualInstance guest : test.getGuests()) {
            if (guest.getName().equals("vm1")) {
                assertEquals(STATE_PAUSED, guest.getState().getLabel());
            }
            else if (guest.getName().equals("vmRename")) {
                assertEquals(STATE_STOPPED, guest.getState().getLabel());
                assertEquals(1, guest.getNumberOfCPUs().intValue());
                assertEquals(1024, guest.getTotalMemory().longValue());
            }
            else {
                assertTrue(false);
            }
        }
    }

    public void testRemovePlanExec() throws Exception {

        Long id = server.getId();
        List<VmInfo> plan = new LinkedList<>();

        plan.add(new VmInfo(1479479686, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(2048, "vm1", STATE_RUNNING, "38a4e1c14d8e440780b3b59745ba9ce5", 2, VIRTTYPE_PARA)));
        plan.add(new VmInfo(1479479686, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(4096, "vm2", STATE_RUNNING, "a4f100d349954f50a24f80fec75e3f5d", 4, VIRTTYPE_PARA)));

        VirtualInstanceManager.updateGuestsVirtualInstances(server, plan);

        Server test = SystemManager.lookupByIdAndUser(id, user);
        assertNotNull(test);
        assertEquals(2, test.getGuests().size());

        plan = new LinkedList<>();

        plan.add(new VmInfo(1479479699, EVENT_TYPE_REMOVED, TARGET_DOMAIN,
                new GuestProperties(2048, "vm1", STATE_RUNNING, "38a4e1c14d8e440780b3b59745ba9ce5", 2, VIRTTYPE_PARA)));

        VirtualInstanceManager.updateGuestsVirtualInstances(server, plan);

        test = SystemManager.lookupByIdAndUser(id, user);
        assertNotNull(test);
        assertEquals(1, test.getGuests().size());

        VirtualInstance guest = test.getGuests().iterator().next();
        assertEquals("vm2", guest.getName());
        assertEquals("a4f100d349954f50a24f80fec75e3f5d", guest.getUuid());
    }

    public void testRefreshPlanExec() throws Exception {

        Long id = server.getId();
        List<VmInfo> plan = new LinkedList<>();

        plan.add(new VmInfo(1479479686, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(2048, "vm1", STATE_RUNNING, "38a4e1c14d8e440780b3b59745ba9ce5", 2, VIRTTYPE_PARA)));
        plan.add(new VmInfo(1479479686, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(4096, "vm2", STATE_RUNNING, "a4f100d349954f50a24f80fec75e3f5d", 4, VIRTTYPE_PARA)));
        plan.add(new VmInfo(1479479699, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(1024, "vm3", STATE_RUNNING, "420ea57f7035ee1de2c1e23fe29f5ca7", 1, VIRTTYPE_PARA)));

        VirtualInstanceManager.updateGuestsVirtualInstances(server, plan);

        Server test = SystemManager.lookupByIdAndUser(id, user);
        assertNotNull(test);
        assertEquals(3, test.getGuests().size());

        plan = new LinkedList<>();
        plan.add(new VmInfo(1479479799, EVENT_TYPE_FULLREPORT, TARGET_DOMAIN, null));
        plan.add(new VmInfo(1479479799, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(1024, "vm3", STATE_CRASHED, "420ea57f7035ee1de2c1e23fe29f5ca7", 1, VIRTTYPE_PARA)));

        VirtualInstanceManager.updateGuestsVirtualInstances(server, plan);

        test = SystemManager.lookupByIdAndUser(id, user);
        assertNotNull(test);
        assertEquals(1, test.getGuests().size());

        VirtualInstance guest = test.getGuests().iterator().next();
        assertEquals("vm3", guest.getName());
        assertEquals("420ea57f7035ee1de2c1e23fe29f5ca7", guest.getUuid());
        assertEquals(STATE_CRASHED, guest.getState().getLabel());
    }

    public void testUnlinkVirtualInstanceFromHost() throws Exception {

        Long id = server.getId();
        List<VmInfo> plan = new LinkedList<>();

        plan.add(new VmInfo(1479479686, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(4096, "vm2", STATE_RUNNING, "a4f100d349954f50a24f80fec75e3f5d", 4, VIRTTYPE_PARA)));

        VirtualInstanceManager.updateGuestsVirtualInstances(server, plan);

        Server test = SystemManager.lookupByIdAndUser(id, user);
        assertNotNull(test);
        assertEquals(1, test.getGuests().size());
        VirtualInstance vinst = test.getGuests().iterator().next();
        Server server2 = ServerFactoryTest.createTestServer(user, true);
        vinst.setGuestSystem(server2);
        VirtualInstanceFactory.getInstance().saveVirtualInstance(vinst);

        plan = new LinkedList<>();
        plan.add(new VmInfo(1479479799, EVENT_TYPE_FULLREPORT, TARGET_DOMAIN, null));
        plan.add(new VmInfo(1479479799, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(1024, "vm3", STATE_CRASHED, "420ea57f7035ee1de2c1e23fe29f5ca7", 1, VIRTTYPE_PARA)));

        VirtualInstanceManager.updateGuestsVirtualInstances(server, plan);

        VirtualInstance guest = test.getGuests().iterator().next();
        assertEquals("vm3", guest.getName());
        assertEquals("420ea57f7035ee1de2c1e23fe29f5ca7", guest.getUuid());
        assertEquals(STATE_CRASHED, guest.getState().getLabel());

        Long id2 = server2.getId();
        test = SystemManager.lookupByIdAndUser(id2, user);
        assertNotNull(test);
        assertNotNull(test.getVirtualInstance());
        assertEquals("a4f100d349954f50a24f80fec75e3f5d", test.getVirtualInstance().getUuid());
    }

    public void testSwappedUuidPlanExec() throws Exception {

        Long id = server.getId();
        List<VmInfo> plan = new LinkedList<>();

        plan.add(new VmInfo(1479479686, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(2048, "vm1", STATE_RUNNING, "38a4e1c14d8e440780b3b59745ba9ce5", 2, VIRTTYPE_PARA)));
        plan.add(new VmInfo(1479479686, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(1024, "vm3", STATE_RUNNING, "420ea57f7035ee1de2c1e23fe29f5ca7", 1, VIRTTYPE_PARA)));

        VirtualInstanceManager.updateGuestsVirtualInstances(server, plan);

        Server test = SystemManager.lookupByIdAndUser(id, user);
        assertNotNull(test);
        assertEquals(2, test.getGuests().size());

        plan = new LinkedList<>();
        plan.add(new VmInfo(1479479799, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(1024, "vm3", STATE_CRASHED, "7fa50e4235701deee2c1e23fe29f5ca7", 1, VIRTTYPE_PARA)));

        VirtualInstanceManager.updateGuestsVirtualInstances(server, plan);

        test = SystemManager.lookupByIdAndUser(id, user);
        assertNotNull(test);
        assertEquals(2, test.getGuests().size());

        for (VirtualInstance guest : test.getGuests()) {
            if (guest.getName().equals("vm1")) {
                assertEquals("38a4e1c14d8e440780b3b59745ba9ce5", guest.getUuid());
            }
            else if (guest.getName().equals("vm3")) {
                assertEquals("420ea57f7035ee1de2c1e23fe29f5ca7", guest.getUuid());
                assertEquals(STATE_CRASHED, guest.getState().getLabel());
            }
            else {
                assertTrue(false);
            }
        }
    }

    public void testUpdateGuestVirtualInstancesFromJSON() throws Exception {

        Long id = server.getId();
        Map<String, String> vms = new HashMap<>();
        Map<String, Map<String, String>> optionalVmData = new HashMap<>();
        VirtualInstanceType type = VirtualInstanceFactory.getInstance()
                .getVirtualInstanceType("vmware");

        vms.put("SUSE-Manager-Test-VM-1", "564d09ec-41b9-c894-566b-30248333e6d3");
        vms.put("SUSE-Manager-Test-VM-2", "564d3f2d-4a22-723a-839f-5fd8912ca2ca");
        vms.put("SUSE-Manager-Test-VM-3", "564db0e5-d359-31f7-2bae-755c960e3fd6");
        vms.put("SUSE-Manager-Test-VM-4", "564d638d-95c1-c9ab-f838-6bb7295b8f37");
        Map<String, String> vmRunningState = new HashMap<>();
        vmRunningState.put("vmState", "running");
        Map<String, String> vmStoppedState = new HashMap<>();
        vmStoppedState.put("vmState", "stopped");
        Map<String, String> vmNoState = new HashMap<>();
        vmNoState.put("foo", "bar");

        optionalVmData.put("SUSE-Manager-Test-VM-1", vmRunningState);
        optionalVmData.put("SUSE-Manager-Test-VM-2", vmStoppedState);
        optionalVmData.put("SUSE-Manager-Test-VM-3", vmNoState);

        VirtualInstanceManager.updateGuestsVirtualInstances(server, type, vms, optionalVmData);

        Server test = SystemManager.lookupByIdAndUser(id, user);
        assertNotNull(test);
        assertEquals(4, test.getGuests().size());

        for (VirtualInstance guest : test.getGuests()) {
            if (guest.getName().equals("SUSE-Manager-Test-VM-1")) {
                assertEquals(STATE_RUNNING, guest.getState().getLabel());
            }
            else if (guest.getName().equals("SUSE-Manager-Test-VM-2")) {
                assertEquals(STATE_STOPPED, guest.getState().getLabel());
            }
            else if (guest.getName().equals("SUSE-Manager-Test-VM-3")) {
                assertEquals(STATE_UNKNOWN, guest.getState().getLabel());
            }
            else if (guest.getName().equals("SUSE-Manager-Test-VM-4")) {
                assertEquals(STATE_UNKNOWN, guest.getState().getLabel());
            }
        }
    }

    public void testUpdateGuestVirtualInstancesFromJSONWithNoAdditionalVmData() throws Exception {

        Long id = server.getId();
        Map<String, String> vms = new HashMap<>();
        Map<String, Map<String, String>> optionalVmData = new HashMap<>();
        VirtualInstanceType type = VirtualInstanceFactory.getInstance()
                .getVirtualInstanceType("vmware");

        vms.put("SUSE-Manager-Test-VM-1", "564d09ec-41b9-c894-566b-30248333e6d3");
        vms.put("SUSE-Manager-Test-VM-2", "564d3f2d-4a22-723a-839f-5fd8912ca2ca");
        vms.put("SUSE-Manager-Test-VM-3", "564db0e5-d359-31f7-2bae-755c960e3fd6");

        VirtualInstanceManager.updateGuestsVirtualInstances(server, type, vms, optionalVmData);

        Server test = SystemManager.lookupByIdAndUser(id, user);
        assertNotNull(test);
        assertEquals(3, test.getGuests().size());

        for (VirtualInstance guest : test.getGuests()) {
            if (guest.getName().equals("SUSE-Manager-Test-VM-1")) {
                assertEquals(STATE_UNKNOWN, guest.getState().getLabel());
            }
            else if (guest.getName().equals("SUSE-Manager-Test-VM-2")) {
                assertEquals(STATE_UNKNOWN, guest.getState().getLabel());
            }
            else if (guest.getName().equals("SUSE-Manager-Test-VM-3")) {
                assertEquals(STATE_UNKNOWN, guest.getState().getLabel());
            }
        }
    }
}
