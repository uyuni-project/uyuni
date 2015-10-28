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
import com.redhat.rhn.testing.TestUtils;
import com.suse.manager.gatherer.JSONHost;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test for VirtualHostManagerProcessor
 */
public class VirtualHostManagerProcessorTest extends BaseTestCaseWithUser {

    private VirtualHostManager virtualHostManager;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();

        virtualHostManager = new VirtualHostManager();
        virtualHostManager.setId(101L);
        virtualHostManager.setLabel("vhmanager");
        virtualHostManager.setOrg(user.getOrg());
    }

    /**
     * Tests that the VirtualHostManagerProcessor creates a new Server entity
     * for a host reported from gatherer. Digital server id of this Server bears information
     * about the id of the Virtual Host Manager and the hostIdentifier (reported from
     * gatherer).
     */
    public void testCreateServer() {
        Map<String, JSONHost> data = createHostData("esxi_host_1_id", null);

        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        // check if a Server is created
        Server host = ServerFactory
                .lookupForeignSystemByDigitalServerId("101-esxi_host_1_id");
        assertNotNull(host);
        assertNotNull(host.getServerInfo());
    }

    /**
     * Tests that the VirtualHostManagerProcessor creates a new VirtualInstance entity
     * for a host reported from gatherer.
     */
    public void testCreateVirtualInstance() {
        Map<String, JSONHost> data = createHostData("esxi_host_1_id", null);

        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        // check if a VirtualInstance is created
        Server host = ServerFactory
                .lookupForeignSystemByDigitalServerId("101-esxi_host_1_id");
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
        Server existingHost = ServerTestUtils
                .createForeignSystem(user, "101-esxi_host_1_id");

        // gatherer reports this host (name even doesn't have to be the same, important
        // thing is the digital server id is equal)
        Map<String, JSONHost> data = createHostData("esxi_host_1_id", null);

        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();
        assertContains(virtualHostManager.getServers(), existingHost);
    }

    /**
     * Tests that VirtualHostManagerProcessor creates a new VirtualInstance entity for
     * an existing host (reported by gatherer).
     *
     * @throws Exception - if anything goes wrong
     */
    public void testCreateVirtInstanceWithExistingServer() throws Exception {
        // create a host
        Server existingHost = ServerTestUtils
                .createForeignSystem(user, "101-existing_host_id");

        // gatherer reports this host
        Map<String, JSONHost> data = createHostData("existing_host_id", null);
        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

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
        Server existingHost = ServerTestUtils.createForeignSystem(user, "existing_host_id");
        // create a VI for host
        VirtualInstance virtualInstance = new VirtualInstance();
        virtualInstance.setConfirmed(1L);
        virtualInstance.setHostSystem(existingHost);
        VirtualInstanceFactory.getInstance().saveVirtualInstance(virtualInstance);

        // gatherer reports this host
        Map<String, JSONHost> data = createHostData("existing_host_id", null);
        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        // check if a VirtualInstance of the host is the same after processing
        VirtualInstance virtualInstanceAfter = VirtualInstanceFactory.getInstance()
                .lookupHostVirtInstanceByHostId(existingHost.getId());
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
        Map<String, JSONHost> data = createHostData("esxi_host_1",
                pairsToMap("myVM", "id_of_my_guest"));

        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        Server newHost = ServerFactory
                .lookupForeignSystemByDigitalServerId("101-esxi_host_1");
        List<VirtualInstance> guestsFromDb = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid("id_of_my_guest");
        assertEquals(1, guestsFromDb.size());

        VirtualInstance guestFromDb = guestsFromDb.iterator().next();
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
        createRegisteredGuestWithForeignHost("id_of_my_guest", "existing_host_id");

        // do the mapping
        Map<String, JSONHost> data = createHostData("existing_host_id",
                pairsToMap("myVM", "id_of_my_guest"));
        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        // verify guest is linked to the 1st host
        Server host = ServerFactory
                .lookupForeignSystemByDigitalServerId("101-existing_host_id");
        List<VirtualInstance> virtualInstances = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid("id_of_my_guest");
        assertEquals(1, virtualInstances.size());
        VirtualInstance guest = virtualInstances.iterator().next();
        assertContains(host.getGuests(), guest);
        assertEquals(host, guest.getHostSystem());

        // now our guest is reported by a different host
        data = createHostData("another_host_id",
                pairsToMap("myVM", "id_of_my_guest"));
        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        // after processing, the virtual instance should be mapped to "another_host"
        Server anotherHost = ServerFactory
                .lookupForeignSystemByDigitalServerId("101-another_host_id");
        virtualInstances = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid("id_of_my_guest");
        assertEquals(1, virtualInstances.size());
        guest = virtualInstances.iterator().next();
        assertContains(anotherHost.getGuests(), guest);
        assertEquals(anotherHost, guest.getHostSystem());
        // the original host shouldn't know about the guest anymore
        host = ServerFactory.lookupForeignSystemByDigitalServerId("101-existing_host_id");
        assertEquals("myVM", guest.getName());
        assertFalse(host.getGuests().contains(guest));
    }

    /**
     * Tests the scenario in which the guest's system is already registered and the
     * virtualization mapping reporting happens after that. In this case,
     * VirtualHostManagerProcessor should update the link between already existing
     * VirtualInstance and host Server.
     *
     * @throws Exception - if anything goes wrong
     */
    public void testUpdateAlreadyRegisteredGuest() throws Exception {
        // guest already registered by usual registration process
        VirtualInstance registeredGuest = new GuestBuilder(user)
                .createGuest()
                .withUuid("vmuuid")
                .inStoppedState()
                .asFullyVirtGuest()
                .build();
        assertNull(registeredGuest.getHostSystem());

        // gatherer reports this guest belonging to a host _after_ registration
        Server hostServer = ServerTestUtils
                .createForeignSystem(user, "101-foreign_system_id");
        Map<String, JSONHost> data = createHostData("foreign_system_id",
                pairsToMap("vm name", "vmuuid"));

        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        // verify that processor linked this guest to its host
        VirtualInstance guestFromDb = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid("vmuuid").iterator().next();
        assertEquals(hostServer, guestFromDb.getHostSystem());
        // 2 VirtualInstances should be persisted
        // (one for the host system, one for the guest)
        assertEquals(2, HibernateFactory.getSession().createCriteria(VirtualInstance.class)
                .list().size());
    }

    /**
     * Two different VirtualHostManagers reporting same VM. Run mapping for each of them,
     * check that there are no duplicate VirtualInstances for
     * the guest.
     *
     * The situation is not very likely, but can happen (User adds one VirtualHostManager
     * for vCenter and one for ESXi host, which is part of this vCenter. These
     * VirtualHostManagers are both processed, but no duplicate VirtualInstances should
     * be created for the guest in the database.).
     */
    @SuppressWarnings("unchecked")
    public void testTwoVHMsSameVM() {
        Map<String, JSONHost> data = createHostData("esxi_host_id",
                pairsToMap("myVM", "id_of_my_guest"));

        VirtualHostManager virtualHostManager2 = new VirtualHostManager();
        virtualHostManager2.setId(102L);
        virtualHostManager2.setLabel(virtualHostManager.getLabel());
        virtualHostManager2.setOrg(virtualHostManager.getOrg());
        assertEquals(virtualHostManager, virtualHostManager2);

        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();
        new VirtualHostManagerProcessor(virtualHostManager2, data).processMapping();

        List<VirtualInstance> guests = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid("id_of_my_guest");
        // the host created in the second mapping
        Server host = ServerFactory
            .lookupForeignSystemByDigitalServerId("102-esxi_host_id");

        assertEquals(1, guests.size());
        assertEquals(host, guests.iterator().next().getHostSystem());
    }

    /**
     * Tests that the virtual instance type is correctly set for the host and that its
     * guests inherit this virtualization type.
     */
    public void testGuestVirtualizationType() {
        VirtualInstanceType fullyVirtType =
                VirtualInstanceFactory.getInstance().getFullyVirtType();
        Map<String, JSONHost> data = createHostData("esxi_host_id",
                pairsToMap("myVM", "id_of_my_guest"));

        // adjust virtualization type for the host in the data
        JSONHost host = data.entrySet().iterator().next().getValue();
        host.setType(fullyVirtType.getLabel());

        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        Server newHost = ServerFactory
                .lookupForeignSystemByDigitalServerId("101-esxi_host_id");
        VirtualInstance hostVirtInstance = VirtualInstanceFactory.getInstance()
                .lookupHostVirtInstanceByHostId(newHost.getId());
        assertEquals(fullyVirtType, hostVirtInstance.getType());

        VirtualInstance guestFromDb = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid("id_of_my_guest").iterator().next();
        assertEquals(fullyVirtType, guestFromDb.getType());
    }

    /**
     * Tests that VirtualHostManagerProcessor removes hyphens from UUIDs from guests.
     */
    public void testUuidNormalization() {
        Map<String, JSONHost> data = createHostData("foreign_system_id",
                pairsToMap("my vm", "my-uuid"));

        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        VirtualInstanceFactory factory = VirtualInstanceFactory.getInstance();
        assertTrue(factory.lookupVirtualInstanceByUuid("my-uuid").isEmpty());
        assertNotEmpty(factory.lookupVirtualInstanceByUuid("myuuid"));
    }

    /**
     * Tests the situation when the virtual host gatherer reports a VM but we already have
     * multiple guest VirtualInstances with this VM UUID in the database.
     *
     * In this corner case we want the VirtualHostManagerProcessor to update all
     * VirtualInstances.
     *
     * @throws Exception - if anything goes wrong
     */
    public void testMultipleUuidInDb() throws Exception {
        String guestUuid = "guestUuid";
        // create a GUEST virt. instances with same uuid
        createRegisteredGuestWithHost(guestUuid);
        createRegisteredGuestWithHost(guestUuid);

        String newVmName = "new name";
        Map<String, JSONHost> data = createHostData("existing_host_id",
                pairsToMap(newVmName, guestUuid));

        // do the mapping
        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        Server newHost = ServerFactory
                .lookupForeignSystemByDigitalServerId("101-existing_host_id");

        // verify that both have a new name and both belong to the same host server
        List<VirtualInstance> virtualInstances = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid(guestUuid);
        assertEquals(2, virtualInstances.size());
        virtualInstances.stream()
                .forEach(vi -> {
                    assertEquals(newVmName, vi.getName());
                    assertEquals(newHost, vi.getHostSystem());
                });
    }

    /**
     * Test scenario when gatherer reports a guest under a host and this host is then
     * renamed. Then a new host with the original name is created and
     * VirtualHostManagerProcessor is run again on the same gatherer data. We check that the
     * guest still belongs to the first host and the name of this host is unchanged.
     */
    public void testRenameServer() {
        Map<String, JSONHost> data = createHostData("esxi_host_1_id",
                pairsToMap("myVM", "id_of_my_guest"));

        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        // rename host
        Server host = ServerFactory
                .lookupForeignSystemByDigitalServerId("101-esxi_host_1_id");
        host.setName("other name");
        ServerFactory.save(host);
        HibernateFactory.getSession().clear();

        // todo renaming doesn't work - it triggers only when the server id differs in VHMP
        // is fixed in a follow up patch
        data = createHostData("esxi_host_1_id", pairsToMap("renamed vm", "id_of_my_guest"));
        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        // check that the guest is renamed and still belongs to the original server
        List<VirtualInstance> guests = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid("id_of_my_guest");
        assertEquals(1, guests.size());
        VirtualInstance guest = guests.iterator().next();
        assertEquals("renamed vm", guest.getName());
        assertEquals(host.getId(), guest.getHostSystem().getId());
    }

    /**
     * Creates a map representing the parsed result from gatherer run on one virtual host
     * manager with one virtual host.
     *
     * @param hostIdentifier - host identifier of the virtual host
     * @param vms - virtual machines
     * @return map with corresponding data
     */
    private Map<String, JSONHost> createHostData(String hostIdentifier,
            Map<String, String> vms) {
        Map<String, JSONHost> data = new HashMap<>();
        data.put(TestUtils.randomString(), createMinimalHost(hostIdentifier, vms));
        return data;
    }

    /**
     * Create a JSONHost instance filled with test data representing results from virtual
     * host gatherer.
     * @param hostId - host identifier
     * @param vms - map of virtual machines
     * @return JSONHost instance
     */
    private JSONHost createMinimalHost(String hostId, Map<String, String> vms) {
        if (vms == null) {
            vms = new HashMap<>();
        }
        JSONHost minimalHost = new JSONHost();
        minimalHost.setHostIdentifier(hostId);
        minimalHost.setVms(vms);
        minimalHost.setType("para_virtualized");
        minimalHost.setRamMb(128);
        minimalHost.setCpuArch("x86_64");
        minimalHost.setCpuMhz(400.0);
        minimalHost.setTotalCpuCores(1);
        minimalHost.setTotalCpuSockets(1);
        minimalHost.setOs("Windows");
        minimalHost.setOsVersion("Vista");
        return minimalHost;
    }

    /**
     * Helper method for converting list of string to a map.
     * From the parameters keeps taking two values at a time and fills the resulting map
     * (first value in the pair is the key, second value is the value).
     * @param pairs strings
     * @return Map of key-values constructed from the strings
     */
    private Map<String, String> pairsToMap(String... pairs) {
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < pairs.length; i += 2) {
            result.put(pairs[i], pairs[i + 1]);
        }
        return result;
    }

    private VirtualInstance createRegisteredGuestWithForeignHost(String guestUuid,
            String digitalServerId) throws Exception {
        return new GuestBuilder(user)
            .createGuest()
            .withUuid(guestUuid)
            .withForeignEntitledHost(digitalServerId)
            .inStoppedState()
            .asFullyVirtGuest()
            .build();
    }

    private VirtualInstance createRegisteredGuestWithHost(String guestUuid) throws Exception {
        return new GuestBuilder(user)
            .createGuest()
            .withUuid(guestUuid)
            .withVirtHost()
            .inStoppedState()
            .asFullyVirtGuest()
            .build();
    }
}
