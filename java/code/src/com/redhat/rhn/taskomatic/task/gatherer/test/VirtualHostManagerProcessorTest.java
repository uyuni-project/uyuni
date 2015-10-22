package com.redhat.rhn.taskomatic.task.gatherer.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.VirtualInstanceType;
import com.redhat.rhn.domain.server.test.GuestBuilder;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.taskomatic.task.gatherer.VirtualHostManagerProcessor;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;

import com.suse.manager.gatherer.JSONHost;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test for VirtualHostManagerProcessor
 */
public class VirtualHostManagerProcessorTest extends BaseTestCaseWithUser {

    private VirtualHostManager virtualHostManager;
    private JSONHost minimalHost;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        virtualHostManager = new VirtualHostManager();
        virtualHostManager.setLabel("vhmanager");
        virtualHostManager.setOrg(user.getOrg());

        minimalHost = new JSONHost();
        minimalHost.setType("para_virtualized");
        minimalHost.setRamMb(128);
        minimalHost.setCpuArch("x86_64");
        minimalHost.setCpuMhz(400.0);
        minimalHost.setTotalCpuCores(1);
        minimalHost.setTotalCpuSockets(1);
        minimalHost.setOs("Windows");
        minimalHost.setOsVersion("Vista");
        minimalHost.setVms(new HashMap<>());
    }

    /**
     * Two equal VirtualHostManagers, run mapping for each of them, check that there are no
     * duplicate VirtualInstances. Similar situation can happen if the user adds one
     * VirtualHostManager for vCenter and one for ESXi host, which is part of this vCenter.
     * These VirtualHostManager are both processed, but no duplicate VirtualInstances should
     * be created in the database.
     */
    @SuppressWarnings("unchecked")
    public void testTwoEqualsVHMs() {
        minimalHost.getVms().put("myVM", "id_of_my_guest");
        Map<String, JSONHost> data = new HashMap<>();
        data.put("esxi_host_1", minimalHost);

        VirtualHostManager virtualHostManager2 = new VirtualHostManager();
        virtualHostManager2.setLabel(virtualHostManager.getLabel());
        virtualHostManager2.setOrg(virtualHostManager.getOrg());
        assertEquals(virtualHostManager, virtualHostManager2);

        VirtualHostManagerProcessor processor =
                new VirtualHostManagerProcessor(virtualHostManager, data);
        processor.processMapping();
        VirtualHostManagerProcessor processor2 =
                new VirtualHostManagerProcessor(virtualHostManager2, data);
        processor2.processMapping();

        List<VirtualInstance> allVirtInstances = HibernateFactory.getSession()
                .createCriteria(VirtualInstance.class)
                .list();
        System.out.println(allVirtInstances);
        assertEquals(2, allVirtInstances.size()); // one for host, one for guest, no dupes
    }

    /**
     * Tests that the VirtualHostManagerProcessor creates a new Server entity
     * for a host reported from gatherer.
     */
    public void testCreateServer() {
        Map<String, JSONHost> data = new HashMap<>();
        data.put("esxi_host_1", minimalHost);

        VirtualHostManagerProcessor processor =
                new VirtualHostManagerProcessor(virtualHostManager, data);
        processor.processMapping();

        // check if a Server is created
        Server host = ServerFactory.lookupForeignSystemByName("esxi_host_1");
        assertNotNull(host);
        assertNotNull(host.getServerInfo());
    }

    /**
     * Tests that the VirtualHostManagerProcessor creates a new VirtualInstance entity
     * for a host reported from gatherer.
     */
    public void testCreateVirtualInstance() {
        Map<String, JSONHost> data = new HashMap<>();
        data.put("esxi_host_1", minimalHost);

        VirtualHostManagerProcessor processor =
                new VirtualHostManagerProcessor(virtualHostManager, data);
        processor.processMapping();

        // check if a VirtualInstance is created
        Server host = ServerFactory.lookupForeignSystemByName("esxi_host_1");
        VirtualInstance virtualInstance = VirtualInstanceFactory.getInstance()
                .lookupHostVirtInstanceByHostId(host.getId());
        assertNotNull(virtualInstance);
        assertEquals(new Long(1L), virtualInstance.getConfirmed());
        assertNull(virtualInstance.getUuid());
        assertNull(virtualInstance.getGuestSystem());
        assertEquals(VirtualInstanceFactory.getInstance().getUnknownState().getLabel(),
                virtualInstance.getState().getLabel());
        assertEquals(VirtualInstanceFactory.getInstance().getParaVirtType(),
                virtualInstance.getType());
    }

    /**
     * Tests that the VirtualHostManager is linked with an corresponding (existing)
     * server (reported from gatherer) after processing.
     *
     * @throws Exception - if anything goes wrong
     */
    public void testServerExists() throws Exception {
        // create a host
        Server existingHost = ServerTestUtils.createForeignSystem(user, "existing_host");

        // gatherer reports this host
        Map<String, JSONHost> data = new HashMap<>();
        data.put("existing_host", minimalHost);

        VirtualHostManagerProcessor processor
                = new VirtualHostManagerProcessor(virtualHostManager, data);
        processor.processMapping();

        assertContains(virtualHostManager.getServers(), existingHost);
        assertEquals(existingHost,
                ServerFactory.lookupForeignSystemByName("existing_host"));
    }

    /**
     * Tests that VirtualHostManagerProcessor creates a new VirtualInstance entity for
     * an existing host (reported by gatherer).
     *
     * @throws Exception - if anything goes wrong
     */
    public void testCreateVirtInstanceWithExistingServer() throws Exception {
        // create a host
        Server existingHost = ServerTestUtils.createForeignSystem(user, "existing_host");

        // gatherer reports this host
        Map<String, JSONHost> data = new HashMap<>();
        data.put("existing_host", minimalHost);

        VirtualHostManagerProcessor processor =
                new VirtualHostManagerProcessor(virtualHostManager, data);
        processor.processMapping();

        // check if a VirtualInstance is created
        VirtualInstance virtualInstance = VirtualInstanceFactory.getInstance()
                .lookupHostVirtInstanceByHostId(existingHost.getId());
        assertNotNull(virtualInstance);
        assertEquals(new Long(1L), virtualInstance.getConfirmed());
        assertNull(virtualInstance.getUuid());
        assertNull(virtualInstance.getGuestSystem());
        assertEquals(VirtualInstanceFactory.getInstance().getUnknownState().getLabel(),
                virtualInstance.getState().getLabel());
        assertEquals(VirtualInstanceFactory.getInstance().getParaVirtType(),
                virtualInstance.getType());
    }

    /**
     * Tests that VirtualHostManagerProcessor updates link between host and its
     * VirtualInstance based on the data from gatherer.
     *
     * @throws Exception - if anything goes wrong
     */
    public void testExistingVirtInstanceWithExistingServer() throws Exception {
        // create a host
        Server existingHost = ServerTestUtils.createForeignSystem(user, "existing_host");
        // create a VI for host
        VirtualInstance virtualInstance = new VirtualInstance();
        virtualInstance.setConfirmed(1L);
        virtualInstance.setHostSystem(existingHost);
        VirtualInstanceFactory.getInstance().saveVirtualInstance(virtualInstance);

        // gatherer reports this host
        Map<String, JSONHost> data = new HashMap<>();
        data.put("existing_host", minimalHost);

        VirtualHostManagerProcessor processor =
                new VirtualHostManagerProcessor(virtualHostManager, data);
        processor.processMapping();

        // check if a VirtualInstance of the host is the same after processing
        VirtualInstance virtualInstanceAfter = VirtualInstanceFactory.getInstance()
                .lookupHostVirtInstanceByHostId(existingHost.getId());
        assertNotNull(virtualInstanceAfter);
        assertEquals(virtualInstance, virtualInstanceAfter);
        assertEquals(virtualInstance.getId(), virtualInstanceAfter.getId());
        assertEquals(new Long(1L), virtualInstanceAfter.getConfirmed());
        assertNull(virtualInstanceAfter.getGuestSystem());
    }

    /**
     * Tests that VirtualHostManagerProcessor creates a new (guest) VirtualInstance
     * entity for VM(s) reported from gatherer and that this entity is correctly linked with
     * the host server.
     */
    public void testGuestVirtInstanceInserted() {
        minimalHost.getVms().put("myVM", "id_of_my_guest");
        Map<String, JSONHost> data = new HashMap<>();
        data.put("esxi_host_1", minimalHost);

        VirtualHostManagerProcessor processor
                = new VirtualHostManagerProcessor(virtualHostManager, data);
        processor.processMapping();

        Server newHost = ServerFactory.lookupForeignSystemByName("esxi_host_1");
        VirtualInstance guestFromDb = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid("id_of_my_guest");

        assertNotNull(newHost);
        assertContains(newHost.getGuests(), guestFromDb);

        assertNotNull(guestFromDb);
        assertEquals(guestFromDb.getHostSystem(), newHost);
        assertNull(guestFromDb.getGuestSystem());
        assertEquals("id_of_my_guest", guestFromDb.getUuid());
        assertEquals(Long.valueOf(1L), guestFromDb.getConfirmed());
        assertEquals("myVM", guestFromDb.getName());
    }

    /**
     * Tests that VirtualHostManagerProcessor updates link between
     * (guest) VirtualInstance entity that is already linked to an existing host server
     * and the "new" host server.
     * This involves also checking removing the link to the VirtualInstance from the "old"
     * server.
     *
     * @throws Exception - if anything goes wrong
     */
    public void testGuestVirtInstanceUpdated() throws Exception {
        // create a host
        Server existingHost = ServerTestUtils.createForeignSystem(user, "existing_host");
        // create a GUEST virt. instance
        VirtualInstance guestVirtInstance = new GuestBuilder(user)
            .createGuest()
            .inStoppedState()
            .asFullyVirtGuest()
            .build();
        guestVirtInstance.setUuid("id_of_my_guest");
        existingHost.addGuest(guestVirtInstance);
        ServerFactory.save(existingHost);
        VirtualInstanceFactory.getInstance().saveVirtualInstance(guestVirtInstance);

        minimalHost.getVms().put("myVM", "id_of_my_guest");
        Map<String, JSONHost> data = new HashMap<>();
        data.put("existing_host", minimalHost);

        // do the mapping
        VirtualHostManagerProcessor processor =
                new VirtualHostManagerProcessor(virtualHostManager, data);
        processor.processMapping();

        // verify guest is linked to the 1st host
        Server host = ServerFactory.lookupForeignSystemByName("existing_host");
        VirtualInstance guest = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid("id_of_my_guest");
        assertContains(host.getGuests(), guest);
        assertEquals(host, guest.getHostSystem());

        // now our guest is reported by a different host
        data = new HashMap<>();
        data.put("another_host", minimalHost);
        processor = new VirtualHostManagerProcessor(virtualHostManager, data);
        processor.processMapping();

        // after processing, the virtual instance should be mapped to "another_host"
        Server anotherHost = ServerFactory.lookupForeignSystemByName("another_host");
        guest = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid("id_of_my_guest");
        assertContains(anotherHost.getGuests(), guest);
        assertEquals(anotherHost, guest.getHostSystem());
        // the original host shouldn't know about the guest anymore
        host = ServerFactory.lookupForeignSystemByName("existing_host");
        assertEquals("myVM", guest.getName());
        assertFalse(host.getGuests().contains(guest));
    }

    /**
     * Tests the scenario in which the guest's system is already registered and the
     * virtualization relations reporting happens after that. In this case,
     * VirtualHostManagerProcessor should update the link between already existing
     * VirtualInstance and host Server.
     *
     * @throws Exception - if anything goes wrong
     */
    public void testUpdateAlreadyRegisteredGuest() throws Exception {
        // guest already registered by usual registration process
        VirtualInstance registeredGuest = new GuestBuilder(user)
                .createGuest()
                .inStoppedState()
                .asFullyVirtGuest()
                .build();
        registeredGuest.setUuid("vm-uuid");
        VirtualInstanceFactory.getInstance().saveVirtualInstance(registeredGuest);
        assertNull(registeredGuest.getHostSystem());

        // gatherer reports this guest belonging to a host _after_ registration
        Server hostServer = ServerTestUtils.createForeignSystem(user, "foreign_system");
        Map<String, JSONHost> data = new HashMap<>();
        data.put("foreign_system", minimalHost);
        minimalHost.getVms().put("vm name", "vm-uuid");

        // verify that processor linked this guest to its host
        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();
        VirtualInstance guestFromDb =
                VirtualInstanceFactory.getInstance().lookupVirtualInstanceByUuid("vm-uuid");
        assertEquals(hostServer, guestFromDb.getHostSystem());
        // 2 VirtualInstances should be persisted
        // (one for the host system, one for the guest)
        assertEquals(2, HibernateFactory.getSession().createCriteria(VirtualInstance.class)
                .list().size());
    }

    /**
     * Tests that the virtual instance type is correctly set for the host and that its
     * guests inherit this virtualization type.
     */
    public void testGuestVirtualizationType() {
        VirtualInstanceType fullyVirtType =
                VirtualInstanceFactory.getInstance().getFullyVirtType();
        minimalHost.getVms().put("myVM", "id_of_my_guest");
        minimalHost.setType(fullyVirtType.getLabel());
        Map<String, JSONHost> data = new HashMap<>();
        data.put("esxi_host_1", minimalHost);

        VirtualHostManagerProcessor processor
                = new VirtualHostManagerProcessor(virtualHostManager, data);
        processor.processMapping();


        Server newHost = ServerFactory.lookupForeignSystemByName("esxi_host_1");
        VirtualInstance hostVirtInstance = VirtualInstanceFactory.getInstance()
                .lookupHostVirtInstanceByHostId(newHost.getId());
        assertEquals(fullyVirtType, hostVirtInstance.getType());

        VirtualInstance guestFromDb = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid("id_of_my_guest");
        assertEquals(fullyVirtType, guestFromDb.getType());
    }
}
