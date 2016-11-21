package com.redhat.rhn.manager.system.test;

import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.VirtualInstanceManager;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.webui.utils.salt.custom.GuestProperties;
import com.suse.manager.webui.utils.salt.custom.VmInfo;

import java.util.LinkedList;
import java.util.List;

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
                new GuestProperties(2048, "vm1", STATE_RUNNING, "1234567890", 2, VIRTTYPE_PARA)));
        plan.add(new VmInfo(1479479686, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(4096, "vm2", STATE_RUNNING, "2345678901", 4, VIRTTYPE_FULL)));

        VirtualInstanceManager.updateGuestsVirtualInstances(server, plan);

        Server test = SystemManager.lookupByIdAndUser(id, user);
        assertNotNull(test);
        assertEquals(2, test.getGuests().size());
    }

    public void testAddPlanExec() throws Exception {

        Long id = server.getId();
        List<VmInfo> plan = new LinkedList<>();

        plan.add(new VmInfo(1479479686, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(2048, "vm1", STATE_RUNNING, "1234567890", 2, VIRTTYPE_PARA)));
        plan.add(new VmInfo(1479479686, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(4096, "vm2", STATE_RUNNING, "2345678901", 4, VIRTTYPE_PARA)));

        VirtualInstanceManager.updateGuestsVirtualInstances(server, plan);

        Server test = SystemManager.lookupByIdAndUser(id, user);
        assertNotNull(test);
        assertEquals(2, test.getGuests().size());

        plan = new LinkedList<>();

        plan.add(new VmInfo(1479479699, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(1024, "vm3", STATE_RUNNING, "3456789012", 1, VIRTTYPE_PARA)));

        VirtualInstanceManager.updateGuestsVirtualInstances(server, plan);

        test = SystemManager.lookupByIdAndUser(id, user);
        assertNotNull(test);
        assertEquals(3, test.getGuests().size());
    }

    public void testUpdatePlanExec() throws Exception {

        Long id = server.getId();
        List<VmInfo> plan = new LinkedList<>();

        plan.add(new VmInfo(1479479686, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(2048, "vm1", STATE_RUNNING, "1234567890", 2, VIRTTYPE_PARA)));
        plan.add(new VmInfo(1479479686, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(4096, "vm2", STATE_RUNNING, "2345678901", 4, VIRTTYPE_PARA)));

        VirtualInstanceManager.updateGuestsVirtualInstances(server, plan);

        Server test = SystemManager.lookupByIdAndUser(id, user);
        assertNotNull(test);
        assertEquals(2, test.getGuests().size());

        plan = new LinkedList<>();

        plan.add(new VmInfo(1479479699, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(2048, "vm1", STATE_PAUSED, "1234567890", 2, VIRTTYPE_PARA)));
        plan.add(new VmInfo(1479479699, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(1024, "vmRename", STATE_STOPPED, "2345678901", 1, VIRTTYPE_PARA)));

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
                new GuestProperties(2048, "vm1", STATE_RUNNING, "1234567890", 2, VIRTTYPE_PARA)));
        plan.add(new VmInfo(1479479686, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(4096, "vm2", STATE_RUNNING, "2345678901", 4, VIRTTYPE_PARA)));

        VirtualInstanceManager.updateGuestsVirtualInstances(server, plan);

        Server test = SystemManager.lookupByIdAndUser(id, user);
        assertNotNull(test);
        assertEquals(2, test.getGuests().size());

        plan = new LinkedList<>();

        plan.add(new VmInfo(1479479699, EVENT_TYPE_REMOVED, TARGET_DOMAIN,
                new GuestProperties(2048, "vm1", STATE_RUNNING, "1234567890", 2, VIRTTYPE_PARA)));

        VirtualInstanceManager.updateGuestsVirtualInstances(server, plan);

        test = SystemManager.lookupByIdAndUser(id, user);
        assertNotNull(test);
        assertEquals(1, test.getGuests().size());

        VirtualInstance guest = test.getGuests().iterator().next();
        assertEquals("vm2", guest.getName());
        assertEquals("2345678901", guest.getUuid());
    }

    public void testRefreshPlanExec() throws Exception {

        Long id = server.getId();
        List<VmInfo> plan = new LinkedList<>();

        plan.add(new VmInfo(1479479686, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(2048, "vm1", STATE_RUNNING, "1234567890", 2, VIRTTYPE_PARA)));
        plan.add(new VmInfo(1479479686, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(4096, "vm2", STATE_RUNNING, "2345678901", 4, VIRTTYPE_PARA)));
        plan.add(new VmInfo(1479479699, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(1024, "vm3", STATE_RUNNING, "3456789012", 1, VIRTTYPE_PARA)));

        VirtualInstanceManager.updateGuestsVirtualInstances(server, plan);

        Server test = SystemManager.lookupByIdAndUser(id, user);
        assertNotNull(test);
        assertEquals(3, test.getGuests().size());

        plan = new LinkedList<>();
        plan.add(new VmInfo(1479479799, EVENT_TYPE_FULLREPORT, TARGET_DOMAIN, null));
        plan.add(new VmInfo(1479479799, EVENT_TYPE_EXISTS, TARGET_DOMAIN,
                new GuestProperties(1024, "vm3", STATE_CRASHED, "3456789012", 1, VIRTTYPE_PARA)));

        VirtualInstanceManager.updateGuestsVirtualInstances(server, plan);

        test = SystemManager.lookupByIdAndUser(id, user);
        assertNotNull(test);
        assertEquals(1, test.getGuests().size());

        VirtualInstance guest = test.getGuests().iterator().next();
        assertEquals("vm3", guest.getName());
        assertEquals("3456789012", guest.getUuid());
        assertEquals(STATE_CRASHED, guest.getState().getLabel());
    }
}
