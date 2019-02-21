package com.redhat.rhn.taskomatic.task.gatherer.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.VirtualInstanceType;
import com.redhat.rhn.domain.server.test.GuestBuilder;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManager;
import com.redhat.rhn.domain.server.virtualhostmanager.VirtualHostManagerNodeInfo;
import com.redhat.rhn.taskomatic.task.gatherer.VirtualHostManagerProcessor;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.gatherer.HostJson;

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
        Map<String, HostJson> data = createHostData("esxi_host_1_id", null);

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
        Map<String, HostJson> data = createHostData("esxi_host_1_id", null);

        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        // check if a VirtualInstance is created
        Server host = ServerFactory
                .lookupForeignSystemByDigitalServerId("101-esxi_host_1_id");
        VirtualInstance virtualInstance = VirtualInstanceFactory.getInstance()
                .lookupHostVirtInstanceByHostId(host.getId());
        assertNotNull(virtualInstance);
        assertEquals(Long.valueOf(1L), virtualInstance.getConfirmed());
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
        Map<String, HostJson> data = createHostData("esxi_host_1_id", null);

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
        Map<String, HostJson> data = createHostData("existing_host_id", null);
        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        // check if a VirtualInstance is created
        VirtualInstance virtualInstance = VirtualInstanceFactory.getInstance()
                .lookupHostVirtInstanceByHostId(existingHost.getId());
        assertNotNull(virtualInstance);
        assertEquals(Long.valueOf(1L), virtualInstance.getConfirmed());
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
        Server existingHost =
                ServerTestUtils.createForeignSystem(user, "101-existing_host_id");
        // create a VI for host
        VirtualInstance virtualInstance = new VirtualInstance();
        virtualInstance.setConfirmed(1L);
        virtualInstance.setHostSystem(existingHost);
        VirtualInstanceFactory.getInstance().saveVirtualInstance(virtualInstance);

        // gatherer reports this host
        Map<String, HostJson> data = createHostData("existing_host_id", null);
        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        // check if a VirtualInstance of the host is the same after processing
        VirtualInstance virtualInstanceAfter = VirtualInstanceFactory.getInstance()
                .lookupHostVirtInstanceByHostId(existingHost.getId());
        assertEquals(virtualInstance, virtualInstanceAfter);
        assertEquals(virtualInstance.getId(), virtualInstanceAfter.getId());
        assertEquals(Long.valueOf(1L), virtualInstanceAfter.getConfirmed());
        assertNull(virtualInstanceAfter.getGuestSystem());
    }

    /**
     * Tests that VirtualHostManagerProcessor creates a new (guest) VirtualInstance
     * entity for VM(s) reported from gatherer and that this entity is correctly linked with
     * the host server.
     */
    public void testGuestVirtInstanceInserted() {
        Map<String, HostJson> data = createHostData("esxi_host_1",
                pairsToMap("myVM", "42309db29d991a2f681f74f4c851f4bd"));

        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        Server newHost = ServerFactory
                .lookupForeignSystemByDigitalServerId("101-esxi_host_1");
        List<VirtualInstance> guestsFromDb = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid("42309db29d991a2f681f74f4c851f4bd");
        assertEquals(1, guestsFromDb.size());

        VirtualInstance guestFromDb = guestsFromDb.iterator().next();
        assertNotNull(newHost);
        assertContains(newHost.getGuests(), guestFromDb);

        assertNotNull(guestFromDb);
        assertEquals(guestFromDb.getHostSystem(), newHost);
        assertNull(guestFromDb.getGuestSystem());
        assertEquals("42309db29d991a2f681f74f4c851f4bd", guestFromDb.getUuid());
        assertEquals(Long.valueOf(1L), guestFromDb.getConfirmed());
        assertEquals("myVM", guestFromDb.getName());
        assertEquals(VirtualInstanceFactory.getInstance().getUnknownState(),
                guestFromDb.getState());
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
        createRegisteredGuestWithForeignHost("42309db29d991a2f681f74f4c851f4bd", "101-existing_host_id");

        // do the mapping
        Map<String, HostJson> data = createHostData("existing_host_id",
                pairsToMap("myVM", "42309db29d991a2f681f74f4c851f4bd"));
        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        // verify guest is linked to the 1st host
        Server host = ServerFactory
                .lookupForeignSystemByDigitalServerId("101-existing_host_id");
        List<VirtualInstance> virtualInstances = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid("42309db29d991a2f681f74f4c851f4bd");
        assertEquals(1, virtualInstances.size());
        VirtualInstance guest = virtualInstances.iterator().next();
        assertContains(host.getGuests(), guest);
        assertEquals(host, guest.getHostSystem());

        // now our guest is reported by a different host
        data = createHostData("another_host_id",
                pairsToMap("myVM", "42309db29d991a2f681f74f4c851f4bd"));
        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        // after processing, the virtual instance should be mapped to "another_host"
        Server anotherHost = ServerFactory
                .lookupForeignSystemByDigitalServerId("101-another_host_id");
        virtualInstances = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid("42309db29d991a2f681f74f4c851f4bd");
        assertEquals(1, virtualInstances.size());
        guest = virtualInstances.iterator().next();
        assertContains(anotherHost.getGuests(), guest);
        assertEquals(anotherHost, guest.getHostSystem());
        // the original host shouldn't know about the guest anymore
        host = ServerFactory.lookupForeignSystemByDigitalServerId("101-existing_host_id");
        assertEquals("myVM", guest.getName());
        assertFalse(host.getGuests().contains(guest));
    }

    public void testGuestNameUpdated() {
        Map<String, HostJson> data = createHostData("my-host-id",
                pairsToMap("old name", "38a4e1c14d8e440780b3b59745ba9ce5"));
        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();
        data = createHostData("my-host-id", pairsToMap("new name", "38a4e1c14d8e440780b3b59745ba9ce5"));
        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        VirtualInstance guest = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid("38a4e1c14d8e440780b3b59745ba9ce5").iterator().next();
        assertEquals("new name", guest.getName());
    }

    /**
     * When the state of the guest virtual instance was other than 'unknown', the update
     * should set it to 'unknown'.
     *
     * @throws Exception - if anything goes wrong
     */
    public void testGuestStateSetToUnknown() throws Exception {
        VirtualInstance guest =
                createRegisteredGuestWithForeignHost("1d7d250e9fca4d3ebb04099fe9a3e129", "101-hostid");
        guest.setName("guestname");
        guest.setState(VirtualInstanceFactory.getInstance().getStoppedState());

        Map<String, HostJson> data = createHostData("hostid",
                pairsToMap("guestname", "1d7d250e9fca4d3ebb04099fe9a3e129"));
        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        VirtualInstance dbGuest = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid("1d7d250e9fca4d3ebb04099fe9a3e129").iterator().next();
        assertEquals(VirtualInstanceFactory.getInstance().getUnknownState(),
                dbGuest.getState());
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
        String vmUuid = "51283028dab94084b66117b5bf1d3661";
        VirtualInstance registeredGuest = new GuestBuilder(user)
                .createGuest()
                .withUuid(vmUuid)
                .inStoppedState()
                .asFullyVirtGuest()
                .build();
        assertNull(registeredGuest.getHostSystem());

        // gatherer reports this guest belonging to a host _after_ registration
        String foreignSystemId = "foreign_system_id" + TestUtils.randomString();
        Server hostServer = ServerTestUtils
                .createForeignSystem(user, "101-" + foreignSystemId);
        Map<String, HostJson> data = createHostData(foreignSystemId,
                pairsToMap("vm name", vmUuid));

        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        // verify that processor linked this guest to its host
        List<VirtualInstance> guestsFromDb = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid(vmUuid);
        assertEquals(1, guestsFromDb.size());
        VirtualInstance guestFromDb = guestsFromDb.iterator().next();
        assertEquals(hostServer, guestFromDb.getHostSystem());
        assertEquals(1, hostServer.getGuests().size());
        assertContains(hostServer.getGuests(), guestFromDb);
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
        Map<String, HostJson> data = createHostData("esxi_host_id",
                pairsToMap("myVM", "42309db29d991a2f681f74f4c851f4bd"));

        VirtualHostManager virtualHostManager2 = new VirtualHostManager();
        virtualHostManager2.setId(102L);
        virtualHostManager2.setLabel(virtualHostManager.getLabel());
        virtualHostManager2.setOrg(virtualHostManager.getOrg());
        assertEquals(virtualHostManager, virtualHostManager2);

        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();
        new VirtualHostManagerProcessor(virtualHostManager2, data).processMapping();

        List<VirtualInstance> guests = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid("42309db29d991a2f681f74f4c851f4bd");
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
        Map<String, HostJson> data = createHostData("esxi_host_id",
                pairsToMap("myVM", "42309db29d991a2f681f74f4c851f4bd"));

        // adjust virtualization type for the host in the data
        HostJson host = data.entrySet().iterator().next().getValue();
        host.setType(fullyVirtType.getLabel());

        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        Server newHost = ServerFactory
                .lookupForeignSystemByDigitalServerId("101-esxi_host_id");
        VirtualInstance hostVirtInstance = VirtualInstanceFactory.getInstance()
                .lookupHostVirtInstanceByHostId(newHost.getId());
        assertEquals(fullyVirtType, hostVirtInstance.getType());

        VirtualInstance guestFromDb = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid("42309db29d991a2f681f74f4c851f4bd").iterator().next();
        assertEquals(fullyVirtType, guestFromDb.getType());
    }

    /**
     * Tests that VirtualHostManagerProcessor removes hyphens from UUIDs from guests.
     */
    public void testUuidNormalization() {
        Map<String, HostJson> data = createHostData("foreign_system_id",
                pairsToMap("my vm", "06b6-0065-9810-4186b513b33bd6190360"));

        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        VirtualInstanceFactory factory = VirtualInstanceFactory.getInstance();
        assertTrue(factory.lookupVirtualInstanceByUuid("06b6-0065-9810-4186b513b33bd6190360").isEmpty());
        assertNotEmpty(factory.lookupVirtualInstanceByUuid("06b6006598104186b513b33bd6190360"));
    }

    /**
     * Tests the situation when the virtual host gatherer reports a VM but we already have
     * a guest VirtualInstances with this VM UUID in the database.
     *
     * In this corner case we want the VirtualHostManagerProcessor to update all
     * VirtualInstances and prevent from creating a duplicates for the same guest "uuid".
     *
     * @throws Exception - if anything goes wrong
     */
    public void testMultipleUuidInDb() throws Exception {
        String guestUuid = "00e0997d581a48ad8defc2c6769bedec";
        // create a GUEST virt. instances with same uuid
        createRegisteredGuestWithHost(guestUuid);
        createRegisteredGuestWithHost(guestUuid);

        String newVmName = "new name";
        Map<String, HostJson> data = createHostData("existing_host_id",
                pairsToMap(newVmName, guestUuid));

        // do the mapping
        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        Server newHost = ServerFactory
                .lookupForeignSystemByDigitalServerId("101-existing_host_id");

        // verify that only one VirtualInstance is created and belong to the same host server
        List<VirtualInstance> virtualInstances = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid(guestUuid);
        assertEquals(1, virtualInstances.size());
        virtualInstances.stream()
                .forEach(vi -> {
                    assertEquals(newVmName, vi.getName());
                    assertEquals(newHost, vi.getHostSystem());
                });
    }

    /**
     * Tests the situation when the virtual host gatherer reports a VM but we already have
     * a guest VirtualInstances with a swapped VM UUID in the database.
     *
     * In this corner case we want the VirtualHostManagerProcessor to update all
     * VirtualInstances and prevent from creating a duplicates with the swapped guest "uuid".
     *
     * @throws Exception - if anything goes wrong
     */
    public void testSwappedUuidInDb() throws Exception {
        String guestUuid = "420ea57f7035ee1de2c1e23fe29f5ca7";
        String swappedUuid = "7fa50e4235701deee2c1e23fe29f5ca7";
        // create a GUEST virt. instances with a swapped uuid
        createRegisteredGuestWithHost(swappedUuid);

        String newVmName = "new name";
        Map<String, HostJson> data = createHostData("existing_host_id",
                pairsToMap(newVmName, guestUuid));

        // do the mapping
        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        Server newHost = ServerFactory
                .lookupForeignSystemByDigitalServerId("101-existing_host_id");

        // No virtual instance created for guestUuid as there is already an instance with swappedUuid
        List<VirtualInstance> virtualInstances = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid(guestUuid);
        assertTrue(virtualInstances.isEmpty());

        // verify that only one VirtualInstance is created and belong to the same host server
        virtualInstances = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid(swappedUuid);
        assertEquals(1, virtualInstances.size());
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
        Map<String, HostJson> data = createHostData("esxi_host_1_id",
                pairsToMap("myVM", "42309db29d991a2f681f74f4c851f4bd"));

        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        // rename host
        Server host = ServerFactory
                .lookupForeignSystemByDigitalServerId("101-esxi_host_1_id");
        host.setName("other name");
        ServerFactory.save(host);
        HibernateFactory.getSession().clear();

        data = createHostData("esxi_host_1_id", pairsToMap("renamed vm", "42309db29d991a2f681f74f4c851f4bd"));
        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        // check that the guest is renamed and still belongs to the original server
        List<VirtualInstance> guests = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid("42309db29d991a2f681f74f4c851f4bd");
        assertEquals(1, guests.size());
        VirtualInstance guest = guests.iterator().next();
        assertEquals("renamed vm", guest.getName());
        assertEquals(host.getId(), guest.getHostSystem().getId());
    }

    /**
     * Tests that VirtualHostManagerProcessor removes VirtualInstance
     * entity for VM(s) when not anymore reported from gatherer.
     */
    public void testGuestRemoved() {

        HostJson myHost = createMinimalHost("esx_host_1",
                pairsToMap("vm1", "de5629cb8c5a4de485a8fc8d1b170412", "vm2", "6888aafa999048038bbb26afb9264db1"));
        Map<String, HostJson> data = new HashMap<>();
        data.put(TestUtils.randomString(), myHost);

        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        Server newHost = ServerFactory
                .lookupForeignSystemByDigitalServerId("101-esx_host_1");
        List<VirtualInstance> guestVM1 = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid("de5629cb8c5a4de485a8fc8d1b170412");
        assertEquals(1, guestVM1.size());
        assertEquals(guestVM1.get(0).getHostSystem(), newHost);

        List<VirtualInstance> guestVM2 = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid("6888aafa999048038bbb26afb9264db1");
        assertEquals(1, guestVM2.size());
        assertEquals(guestVM2.get(0).getHostSystem(), newHost);

        // vm2 was removed from this host
        myHost.setVms(pairsToMap("vm1", "de5629cb8c5a4de485a8fc8d1b170412"));

        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        newHost = ServerFactory
                .lookupForeignSystemByDigitalServerId("101-esx_host_1");
        guestVM1 = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid("de5629cb8c5a4de485a8fc8d1b170412");
        assertEquals(1, guestVM1.size());
        assertEquals(guestVM1.get(0).getHostSystem(), newHost);

        guestVM2 = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByUuid("6888aafa999048038bbb26afb9264db1");
        assertTrue(guestVM2.isEmpty());
    }

    /**
     * Tests that the VirtualHostManagerProcessor does not automatically create a new Server entity
     * for a Kubernetes virtual host manager.
     */
    public void testCreateNodeInfo() {
        Map<String, HostJson> data = createHostData("kubernetes_host_1_id", "Kubernetes",null);
        new VirtualHostManagerProcessor(virtualHostManager, data).processMapping();

        // check that aServer was not created
        Server host = ServerFactory
                .lookupForeignSystemByDigitalServerId("101-kubernetes_host_1_id");
        assertNull(host);
        // check nodeInfo
        assertEquals(1, virtualHostManager.getNodes().size());
        VirtualHostManagerNodeInfo nodeInfo = virtualHostManager.getNodes().stream().findFirst().get();
        assertEquals("kubernetes_host_1_id", nodeInfo.getIdentifier());
        assertEquals(data.keySet().stream().findFirst().get(), nodeInfo.getName());
        assertEquals("Windows", nodeInfo.getOs());
        assertEquals("Vista", nodeInfo.getOsVersion());
        assertEquals(new Integer(128), nodeInfo.getRam());
        assertEquals(new Integer(1), nodeInfo.getCpuCores());
        assertEquals(new Integer(1), nodeInfo.getCpuSockets());
        assertEquals("x86_64-redhat-linux", nodeInfo.getNodeArch().getLabel());
    }

    public void testRemoveNodeInfo() {
        Map<String, HostJson> dataCreate = new HashMap<>();
        dataCreate.putAll(createHostData("kubernetes_host_1_id", "Kubernetes",null));
        dataCreate.putAll(createHostData("kubernetes_host_2_id", "Kubernetes",null));
        new VirtualHostManagerProcessor(virtualHostManager, dataCreate).processMapping();

        // check that aServer was not created
        assertNull(ServerFactory
                .lookupForeignSystemByDigitalServerId("101-kubernetes_host_1_id"));
        assertNull(ServerFactory
                .lookupForeignSystemByDigitalServerId("101-kubernetes_host_2_id"));
        // check nodeInfo
        assertEquals(2, virtualHostManager.getNodes().size());
        assertTrue(virtualHostManager.getNodes().stream()
                .filter(node -> node.getIdentifier().equals("kubernetes_host_1_id"))
                .findFirst().isPresent());
        assertTrue(virtualHostManager.getNodes().stream()
                .filter(node -> node.getIdentifier().equals("kubernetes_host_2_id"))
                .findFirst().isPresent());

        Map<String, HostJson> dataUpdate = new HashMap<>();
        dataUpdate.putAll(createHostData("kubernetes_host_2_id", "Kubernetes",null));
        new VirtualHostManagerProcessor(virtualHostManager, dataUpdate).processMapping();

        assertEquals(1, virtualHostManager.getNodes().size());
        assertTrue(virtualHostManager.getNodes().stream()
                .filter(node -> node.getIdentifier().equals("kubernetes_host_2_id"))
                .findFirst().isPresent());
    }


    /**
     * Creates a map representing the parsed result from gatherer run on one virtual host
     * manager with one virtual host.
     *
     * @param hostIdentifier - host identifier of the virtual host
     * @param vms - virtual machines
     * @return map with corresponding data
     */
    private Map<String, HostJson> createHostData(String hostIdentifier,
                                                 Map<String, String> vms) {
        return createHostData(hostIdentifier, "para_virtualized", vms);
    }

    private Map<String, HostJson> createHostData(String hostIdentifier,
                                                 String type,
                                                 Map<String, String> vms) {
        Map<String, HostJson> data = new HashMap<>();
        data.put(TestUtils.randomString(), createMinimalHost(hostIdentifier, type, vms));
        return data;
    }

    /**
     * Create a HostJson instance filled with test data representing results from virtual
     * host gatherer.
     * @param hostId - host identifier
     * @param vms - map of virtual machines
     * @return HostJson instance
     */
    private HostJson createMinimalHost(String hostId, Map<String, String> vms) {
        return createMinimalHost(hostId, "para_virtualized", vms);
    }

    /**
     * Create a HostJson instance filled with test data representing results from virtual
     * host gatherer.
     * @param hostId - host identifier
     * @param type - type of the vhm
     * @param vms - map of virtual machinesty
     * @return HostJson instance
     */
    private HostJson createMinimalHost(String hostId, String type, Map<String, String> vms) {
        if (vms == null) {
            vms = new HashMap<>();
        }
        HostJson minimalHost = new HostJson();
        minimalHost.setHostIdentifier(hostId);
        minimalHost.setVms(vms);
        minimalHost.setType(type);
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
            .inUnknownState()
            .asFullyVirtGuest()
            .build();
    }

    private VirtualInstance createRegisteredGuestWithHost(String guestUuid) throws Exception {
        return new GuestBuilder(user)
            .createGuest()
            .withUuid(guestUuid)
            .withVirtHost()
            .inUnknownState()
            .asFullyVirtGuest()
            .build();
    }
}
