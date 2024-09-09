/*
 * Copyright (c) 2009--2015 Red Hat, Inc.
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
package com.redhat.rhn.domain.server.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.test.ServerActionTest;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.channel.ChannelProduct;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.channel.test.ChannelFamilyFactoryTest;
import com.redhat.rhn.domain.common.ProvisionState;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.ReportDBCredentials;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.test.CustomDataKeyTest;
import com.redhat.rhn.domain.product.Tuple2;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.rhnpackage.PackageType;
import com.redhat.rhn.domain.rhnpackage.test.PackageEvrFactoryTest;
import com.redhat.rhn.domain.rhnpackage.test.PackageNameTest;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Device;
import com.redhat.rhn.domain.server.Dmi;
import com.redhat.rhn.domain.server.EntitlementServerGroup;
import com.redhat.rhn.domain.server.ErrataInfo;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MgrServerInfo;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.Note;
import com.redhat.rhn.domain.server.ProxyInfo;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.ServerGroupType;
import com.redhat.rhn.domain.server.ServerHistoryEvent;
import com.redhat.rhn.domain.server.ServerInfo;
import com.redhat.rhn.domain.server.ServerPath;
import com.redhat.rhn.domain.server.ServerSnapshot;
import com.redhat.rhn.domain.server.ServerSnapshotTagLink;
import com.redhat.rhn.domain.server.SnapshotTag;
import com.redhat.rhn.domain.server.SnapshotTagName;
import com.redhat.rhn.domain.server.UndefinedCustomDataKeyException;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.xmlrpc.ServerNotInGroupException;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.formula.FormulaMonitoringManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.manager.user.UserManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.ConfigTestUtils;
import com.redhat.rhn.testing.PackageTestUtils;
import com.redhat.rhn.testing.ServerGroupTestUtils;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.maintenance.MaintenanceManager;
import com.suse.manager.model.maintenance.MaintenanceSchedule;
import com.suse.manager.utils.SaltKeyUtils;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.webui.services.SaltServerActionService;
import com.suse.manager.webui.services.iface.MonitoringManager;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.test.TestSaltApi;
import com.suse.manager.webui.services.test.TestSystemQuery;
import com.suse.salt.netapi.calls.LocalCall;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ServerFactoryTest
 */
public class ServerFactoryTest extends BaseTestCaseWithUser {
    private Server server;
    public static final int TYPE_SERVER_MGR = 0;
    public static final int TYPE_SERVER_PROXY = 1;
    public static final int TYPE_SERVER_NORMAL = 2; // bootstrap or foreign
    public static final int TYPE_SERVER_VIRTUAL = 3;
    public static final int TYPE_SERVER_MINION = 4;
    public static final String RUNNING_KERNEL = "2.6.9-55.EL";
    public static final String HOSTNAME = "foo.bar.com";

    private static final SystemQuery SYSTEM_QUERY = new TestSystemQuery();
    private static final SaltApi SALT_API = new TestSaltApi() {
        public void updateLibvirtEngine(MinionServer minion) {
        }
        public <R> Optional<R> callSync(LocalCall<R> call, String minionId) {
            return Optional.empty();
        }
    };
    private static final ServerGroupManager SERVER_GROUP_MANAGER = new ServerGroupManager(SALT_API);
    private static final SaltUtils SALT_UTILS = new SaltUtils(SYSTEM_QUERY, SALT_API);
    private static final SaltKeyUtils SALT_KEY_UTILS = new SaltKeyUtils(SALT_API);
    private static final SaltServerActionService SALT_SERVER_ACTION_SERVICE = new SaltServerActionService(
            SALT_API,
            SALT_UTILS,
            SALT_KEY_UTILS
    );
    private static final VirtManager VIRT_MANAGER = new VirtManagerSalt(SALT_API);
    private static final MonitoringManager MONITORING_MANAGER = new FormulaMonitoringManager(SALT_API);
    private static final SystemEntitlementManager SYSTEM_ENTITLEMENT_MANAGER = new SystemEntitlementManager(
            new SystemUnentitler(VIRT_MANAGER, MONITORING_MANAGER, SERVER_GROUP_MANAGER),
            new SystemEntitler(SALT_API, VIRT_MANAGER, MONITORING_MANAGER, SERVER_GROUP_MANAGER)
    );

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        server = createTestServer(user);
        assertNotNull(server.getId());
    }

    @Test
    public void testListConfigEnabledSystems() throws Exception {
        //Only Config Admins can use this manager function.
        //Making the user a config admin will also automatically
        UserTestUtils.addUserRole(user, RoleFactory.CONFIG_ADMIN);

        //That is not enough though, the user must also have a server that is
        //a member of the config channel and have access to the server as well.
        Server s = ServerFactoryTest.createTestServer(user, true);
        ConfigTestUtils.giveConfigCapabilities(s);
        List<Server> systems = ServerFactory.listConfigEnabledSystems();
        assertNotNull(systems);
        assertTrue(systems.contains(s));
    }

    @Test
    public void testServerGroupMembers() throws Exception {
        Server s = createTestServer(user);
        assertNotNull(s.getEntitledGroups());
        assertFalse(s.getEntitledGroups().isEmpty());
    }

    @Test
    public void testCustomDataValues() throws Exception {
        Org org = user.getOrg();
        Server testServer = createTestServer(user);

        // make sure we dont' have anything defined for this server yet
        Set vals = testServer.getCustomDataValues();
        assertEquals(0, vals.size());

        // create a test key and add to org
        CustomDataKey testKey = CustomDataKeyTest.createTestCustomDataKey(user);
        org.addCustomDataKey(testKey);
        assertTrue(org.hasCustomDataKey(testKey.getLabel()));
        assertNull(testServer.getCustomDataValue(testKey));

        // add the test key to the server and make sure we can get to it.
        testServer.addCustomDataValue(testKey.getLabel(), "foo", user);
        assertNotNull(testServer.getCustomDataValue(testKey));
        assertFalse(testServer.getCustomDataValues().isEmpty());

        // try sending null for key
        int numVals = testServer.getCustomDataValues().size();
        try {
            testServer.addCustomDataValue(new CustomDataKey(), "foo", user);
            fail("server.addCustomDataValue() allowed a value set for an undefined key.");
        }
        catch (UndefinedCustomDataKeyException e) {
            //success
        }
        assertEquals(numVals, testServer.getCustomDataValues().size());

    }

    @Test
    public void testServerLookup() {
        assertNull(ServerFactory.lookupByIdAndOrg(-1234L, user.getOrg()));
        assertNotNull(ServerFactory.lookupByIdAndOrg(server.getId(),
                user.getOrg()));
    }

    @Test
    public void testServerArchLookup() {
        assertNull(ServerFactory.lookupServerArchByLabel("8dafs8320921kfgbzz"));
        assertNotNull(ServerFactory.lookupServerArchByLabel("i386-redhat-linux"));
    }

    @Test
    public void testServerGroupType() {
        //let's hope nobody calls their server group this
        assertNull(ServerFactory.lookupServerGroupTypeByLabel("8dafs8320921kfgbzz"));
        assertNotNull(ServerConstants.getServerGroupTypeEnterpriseEntitled());
        assertNotNull(ServerFactory.lookupServerGroupTypeByLabel(
                ServerConstants.getServerGroupTypeEnterpriseEntitled().getLabel()));
    }

    @Test
    public void testCreateServer() throws Exception {
        Server newS = createTestServer(user);
        newS.getNetworkInterfaces().clear();
        // make sure our many-to-one mappings were set and saved
        assertNotNull(newS.getOrg());
        assertNotNull(newS.getCreator());
        assertNotNull(newS.getServerArch());
        assertNotNull(newS.getProvisionState());

        Note note1 = NoteTest.createTestNote();
        Note note2 = NoteTest.createTestNote();
        newS.addNote(note1);
        newS.addNote(note2);

        NetworkInterface netint1 = NetworkInterfaceTest.createTestNetworkInterface();
        NetworkInterface netint2 = NetworkInterfaceTest.createTestNetworkInterface();
        newS.addNetworkInterface(netint1);
        newS.addNetworkInterface(netint2);

        ServerFactory.save(newS);

        Server server2 = ServerFactory.lookupByIdAndOrg(newS.getId(), user.getOrg());

        Set<Note> notes = server2.getNotes();
        assertEquals(2, notes.size());
        assertTrue(notes.stream().allMatch(n -> server2.equals(n.getServer())));

        Set<NetworkInterface> interfaces = server2.getNetworkInterfaces();
        assertEquals(2, interfaces.size());
        assertTrue(interfaces.stream().allMatch(i -> server2.equals(i.getServer())));
    }

    /**
     * Test editing a server group.
     */
    @Test
    public void testServerGroups() {
        Long id = server.getId();

        Collection servers = new ArrayList<>();
        servers.add(server);
        user.addPermanentRole(RoleFactory.SYSTEM_GROUP_ADMIN);
        ManagedServerGroup sg1 = SERVER_GROUP_MANAGER.create(user, "FooFooFOO", "Foo Description");
        SERVER_GROUP_MANAGER.addServers(sg1, servers, user);

        server = reload(server);
        assertEquals(1, server.getEntitledGroupTypes().size());
        assertEquals(1, server.getManagedGroups().size());


        String changedName = "The group name has been changed" +
            TestUtils.randomString();
        sg1.setName(changedName);

        ServerFactory.save(server);

        //Evict from session to make sure that we get a fresh server
        //from the db.
        HibernateFactory.getSession().evict(server);

        Server server2 = ServerFactory.lookupByIdAndOrg(id, user.getOrg());
        assertEquals(1, server2.getManagedGroups().size());
        sg1 = server2.getManagedGroups().iterator().next();

        assertEquals(changedName, sg1.getName());

    }

    @Test
    public void testAddOrRemoveServersToOrFromGroup() throws Exception {
        User user1 = UserTestUtils.findNewUser("userForAddingServers1", "orgForAddingServers1" +
                this.getClass().getSimpleName());

        Server testServer1 = createTestServer(user1);
        Server testServer2 = createTestServer(user1);

        ManagedServerGroup serverGroup = ServerGroupTestUtils.createManaged(user1);
        Long serverGroupId = serverGroup.getId();
        TestUtils.flushAndEvict(serverGroup);

        serverGroup = ServerGroupFactory.lookupByIdAndOrg(serverGroup.getId(), user1.getOrg());
        assertEquals(serverGroup.getId(), serverGroupId);
        assertTrue(serverGroup.getServers().isEmpty());
        assertEquals(serverGroup.getCurrentMembers().longValue(), 0L);

        //add 2 servers in empty group
        TestUtils.flushAndEvict(serverGroup);
        serverGroup = ServerGroupFactory.lookupByIdAndOrg(serverGroup.getId(), user1.getOrg());

        List<Server> serversToAdd = Arrays.asList(testServer1, testServer2);
        ServerFactory.addServersToGroup(serversToAdd, serverGroup);

        serverGroup = ServerGroupFactory.lookupByIdAndOrg(serverGroup.getId(), user1.getOrg());
        assertTrue(serverGroup.getServers().stream().allMatch(serversToAdd::contains));
        assertEquals(serverGroup.getServers().size(), 2);
        assertEquals(serverGroup.getCurrentMembers().longValue(), 2L);

        //try to add one of the servers again, nothing should happen
        TestUtils.flushAndEvict(serverGroup);
        serverGroup = ServerGroupFactory.lookupByIdAndOrg(serverGroup.getId(), user1.getOrg());

        ServerFactory.addServersToGroup(Arrays.asList(testServer1), serverGroup);

        serverGroup = ServerGroupFactory.lookupByIdAndOrg(serverGroup.getId(), user1.getOrg());
        assertTrue(serverGroup.getServers().stream().allMatch(serversToAdd::contains));
        assertEquals(serverGroup.getServers().size(), 2);
        assertEquals(serverGroup.getCurrentMembers().longValue(), 2L);

        //try to add a server from a different Org, should not be added
        TestUtils.flushAndEvict(serverGroup);
        serverGroup = ServerGroupFactory.lookupByIdAndOrg(serverGroup.getId(), user1.getOrg());

        User user2 = UserTestUtils.findNewUser("userForAddingServers2", "orgForAddingServers2" +
                this.getClass().getSimpleName());

        Server testServerDifferentOrg = createTestServer(user2);
        ServerFactory.addServersToGroup(Arrays.asList(testServerDifferentOrg), serverGroup);

        serverGroup = ServerGroupFactory.lookupByIdAndOrg(serverGroup.getId(), user1.getOrg());
        assertTrue(serverGroup.getServers().stream().allMatch(serversToAdd::contains));
        assertEquals(serverGroup.getServers().size(), 2);
        assertEquals(serverGroup.getCurrentMembers().longValue(), 2L);

        //try to add an empty server collection
        ServerFactory.addServersToGroup(new ArrayList<>(), serverGroup);

        serverGroup = ServerGroupFactory.lookupByIdAndOrg(serverGroup.getId(), user1.getOrg());
        assertTrue(serverGroup.getServers().stream().allMatch(serversToAdd::contains));
        assertEquals(serverGroup.getServers().size(), 2);
        assertEquals(serverGroup.getCurrentMembers().longValue(), 2L);

        //remove 1 server that is a member of the group
        TestUtils.flushAndEvict(serverGroup);
        serverGroup = ServerGroupFactory.lookupByIdAndOrg(serverGroup.getId(), user1.getOrg());

        ServerFactory.removeServersFromGroup(Arrays.asList(testServer1), serverGroup);

        serverGroup = ServerGroupFactory.lookupByIdAndOrg(serverGroup.getId(), user1.getOrg());
        assertFalse(serverGroup.getServers().contains(testServer1));
        assertEquals(serverGroup.getServers().size(), 1);
        assertEquals(serverGroup.getCurrentMembers().longValue(), 1L);

        //remove 1 server that is NOT a member of the group
        Server testNonMemberServer = createTestServer(user1);

        TestUtils.flushAndEvict(serverGroup);
        serverGroup = ServerGroupFactory.lookupByIdAndOrg(serverGroup.getId(), user1.getOrg());

        try {
            ServerFactory.removeServersFromGroup(Arrays.asList(testNonMemberServer), serverGroup);
            fail();
        }
        catch (ServerNotInGroupException e) {
        }

        assertFalse(serverGroup.getServers().contains(testServer1));
        assertEquals(serverGroup.getServers().size(), 1);
        assertEquals(serverGroup.getCurrentMembers().longValue(), 1L);
    }

    @Test
    public void testAddRemove() throws Exception {

        //Test adding/removing server from group
        ServerGroupTestUtils.createManaged(user);
        Server testServer = createTestServer(user);
        Org org = user.getOrg();

        ManagedServerGroup group = org.getManagedServerGroups().
            iterator().next();

        assertNotNull(group);
        ServerGroupFactory.save(group);
        Long membersBefore = group.getCurrentMembers();

        ServerFactory.addServerToGroup(testServer, group);
        //HibernateFactory.getSession().refresh(group);
        Long membersAfter = group.getCurrentMembers();

        assertTrue(membersBefore.intValue() < membersAfter.intValue());

        ServerFactory.removeServerFromGroup(testServer, group);
        group = reload(group);

        Long membersFinally = group.getCurrentMembers();
        assertEquals(membersBefore, membersFinally);

    }

    @Test
    public void testAddNoteToServer() {
        Set notes = server.getNotes();
        assertNotNull(notes);
        assertTrue(notes.isEmpty());

        Note note = new Note();
        note.setCreator(user);
        note.setSubject("Test Note subject");
        note.setNote("Body text");
        Note note2 = new Note();
        note2.setCreator(user);
        note2.setSubject("Test Note 2 subject");
        note2.setNote("Body of note");

        server.addNote(note);
        server.addNote(note2);
        server.addNote(user, "Test Note 3 subject", "Boddy of note");
        ServerFactory.save(server);
        //Evict from session to make sure that we get a fresh server
        //from the db.
        flushAndEvict(server);
        Server server2 = ServerFactory.lookupByIdAndOrg(server.getId(),
                user.getOrg());
        notes = server2.getNotes();
        assertNotNull(notes);
        assertFalse(notes.isEmpty());
        assertEquals(3, notes.size());
    }

    @Test
    public void testAddDeviceToServer() {

        Set devs = server.getDevices();
        assertNotNull(devs);
        assertTrue(devs.isEmpty());

        // create two devices
        Device audio = new Device();
        audio.setBus(Device.BUS_PCI);
        audio.setDeviceClass(Device.CLASS_AUDIO);
        audio.setProp1("Zeus Vendor");

        Device usb = new Device();
        usb.setBus(Device.BUS_USB);
        usb.setDeviceClass(Device.CLASS_USB);
        usb.setProp1("Some property");

        // add devices to the server and store
        server.addDevice(audio);
        server.addDevice(usb);
        ServerFactory.save(server);

        //Evict from session to make sure that we get a fresh server
        //from the db.
        flushAndEvict(server);

        Server server2 = ServerFactory.lookupByIdAndOrg(server.getId(),
                user.getOrg());
        devs = server2.getDevices();
        assertNotNull(devs);
        assertFalse(devs.isEmpty());
        assertEquals(2, devs.size());
    }

    @Test
    public void testAddingRamToServer() {
        server.setRam(1024);
        assertEquals(1024, server.getRam());

        server.setSwap(256);
        assertEquals(256, server.getSwap());

        ServerFactory.save(server);
        //Evict from session to make sure that we get a fresh server
        //from the db.
        flushAndEvict(server);

        Server server2 = ServerFactory.lookupByIdAndOrg(server.getId(),
                user.getOrg());
        assertEquals(1024, server2.getRam());
        assertEquals(256, server2.getSwap());
    }

    @Test
    public void testAddingDmiToServer() {

        Dmi dmi = new Dmi();
        dmi.setServer(server);
        dmi.setVendor("ZEUS computers");
        dmi.setSystem("1234UKX");
        dmi.setProduct("1234UKX");
        dmi.setBios("IBM", "PDKT28AUS", "10/21/1999");
        dmi.setAsset("(board: CNR780A1K11) (system: 23N7011)");
        dmi.setBoard("MSI");

        server.setDmi(dmi);

        assertEquals(dmi, server.getDmi());

        ServerFactory.save(server);
        //Evict from session to make sure that we get a fresh server
        //from the db.
        flushAndEvict(server);

        Server server2 = ServerFactory.lookupByIdAndOrg(server.getId(),
                user.getOrg());
        assertEquals(dmi, server2.getDmi());
    }

    /**
     * Test making two Servers.
     * @throws Exception something bad happened
     */
    @Test
    public void testTwoServers() throws Exception {
        Server s1 = createTestServer(user);
        Server s2 = createTestServer(user);
        assertNotNull(s1);
        assertNotNull(s2);
    }

    @Test
    public void testGetChildChannels() throws Exception {
        Server s1 = ServerTestUtils.createTestSystem(user);
        assertTrue(s1.getChildChannels().isEmpty());

        s1.addChannel(ChannelTestUtils.createChildChannel(user, s1.getBaseChannel()));
        s1.addChannel(ChannelTestUtils.createChildChannel(user, s1.getBaseChannel()));
        assertEquals(2, s1.getChildChannels().size());
    }

    /**
     * Test that server has a specific entitlement.
     * @throws Exception something bad happened
     */
    public void aTestServerHasSpecificEntitlement() throws Exception {
        Server s = createTestServer(user);
        SYSTEM_ENTITLEMENT_MANAGER.addEntitlementToServer(s, EntitlementManager.VIRTUALIZATION);
        assertTrue(s.hasEntitlement(EntitlementManager.VIRTUALIZATION));
    }

    /**
     * Test that server does not have a specific entitlement.
     * @throws Exception something bad happened
     */
    @Test
    public void testServerDoesNotHaveSpecificEntitlement() throws Exception {

        // The default test server should not have a virtualization entitlement.

        Server s = createTestServer(user);
        assertFalse(s.hasEntitlement(EntitlementManager.VIRTUALIZATION));
    }

    /**
     * Create a test Server and commit it to the DB.
     * Create a x86_64 Minion Server
     * @param owner the owner of this Server
     * @return Server that was created
     */
    public static Server createTestServer(User owner) {
        return createTestServer(owner, false);
    }

    public static Server createTestServer(User owner, boolean ensureOwnerAccess, ServerGroupType type) {
        return createTestServer(owner, ensureOwnerAccess, type, TYPE_SERVER_NORMAL, new Date());
    }

    public static Server createTestServer(User owner, boolean ensureOwnerAccess, ServerGroupType type, int stype) {
        return createTestServer(owner, ensureOwnerAccess, type, stype, new Date());
    }

    private static Server createTestServer(User owner, boolean ensureOwnerAccess,
            ServerGroupType type, int stype, Date dateCreated) {

        if (type.getAssociatedEntitlement().equals(EntitlementManager.SALT) && stype == TYPE_SERVER_NORMAL) {
            stype = TYPE_SERVER_MINION;
        }
        Server newS = createUnentitledTestServer(owner, ensureOwnerAccess, stype,
                dateCreated);

        if (!type.getAssociatedEntitlement().isBase()) {
            EntitlementServerGroup mgmt = ServerGroupFactory.lookupEntitled(
                    EntitlementManager.SALT, owner.getOrg());
            if (mgmt == null) {
                newS = TestUtils.saveAndReload(newS);
                mgmt = ServerGroupFactory.lookupEntitled(
                        EntitlementManager.SALT,
                        owner.getOrg());
                newS = ServerFactory.lookupById(newS.getId());
            }
            assertNotNull(mgmt);
            assertNotNull(mgmt.getGroupType().getAssociatedEntitlement());
            SYSTEM_ENTITLEMENT_MANAGER.addEntitlementToServer(newS, mgmt.getGroupType().getAssociatedEntitlement());
        }

        EntitlementServerGroup sg = ServerGroupTestUtils.createEntitled(owner.getOrg(), type);

        SYSTEM_ENTITLEMENT_MANAGER.addEntitlementToServer(newS, sg.getGroupType().getAssociatedEntitlement());
        return TestUtils.saveAndReload(newS);
    }

    /**
     * Create a test Server and commit it to the DB.
     * @param owner the owner of this Server
     * @param ensureOwnerAccess this flag will make sure the owner passed in has
     *                          access to the new server.
     * @param stype the server type
     * @param dateCreated the create date for the server
     * @return Server that was created
     */
    public static Server createUnentitledTestServer(User owner, boolean ensureOwnerAccess,
                    int stype, Date dateCreated) {
        Server newS = createServer(stype);
        // We have to commit this change manually since
        // ServerGroups aren't actually mapped from within
        // the Server class.
        TestUtils.saveAndFlush(owner);

        populateServer(newS, owner, stype);
        createProvisionState(newS, "Test Description", "Test Label");
        createServerInfo(newS, dateCreated, 0L);

        NetworkInterface netint = new NetworkInterface();
        netint.setHwaddr("AA:AA:BB:BB:CC:CC");
        netint.setModule("test");

        netint.setName(TestUtils.randomString());

        netint.setServer(newS);
        newS.addNetworkInterface(netint);

        ServerFactory.save(newS);
        newS = TestUtils.saveAndReload(newS);


        /* Since we added a server to the Org we need
         * to update the User's permissions as associated with
         * that server (if the caller wants us to)
         *
         * Here is a diagram of the table structure.  We want to update USP, but that
         * happens indirectly through rhn_cache.update_perms_for_user.  Therefore, we
         * have to update SGM and USGP in order to connect the dots.
         * SGM happened with ServerFactory.addServerToGroup(newS, sg).  Now we update
         * USGP with UserManager.grantServerGroupPermission(owner, sg.getId().longValue()).
         *
         * |-----|                 |-----|
         * | USP |------|   |------|USGP |                  USP = rhnUserServerPerms
         * |-----|      |   |      |-----|                  USGP = rhnUserServerGroupPerms
         *    |         |   |         |                     S = rhnServer
         *    |         |   |         |                     WC = web_contact
         *    v         v   v         v                     SG = rhnServerGroup
         * |-----|     |-----|     |-----|                  SGM = rhnServerGroupMembers
         * |  S  |     | WC  |     | SG  |
         * |-----|     |-----|     |-----|
         *    ^                       ^
         *    |                       |
         *    |        |-----|        |
         *    |--------| SGM |--------|
         *             |-----|
         */
        if (ensureOwnerAccess) {
            ManagedServerGroup sg2 = ServerGroupTestUtils.createManaged(owner);
            ServerFactory.addServerToGroup(newS, sg2);
            TestUtils.saveAndFlush(sg2);
        }

        Long id = newS.getId();
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().evict(newS);
        newS = ServerFactory.lookupByIdAndOrg(id, owner.getOrg());
        assertNotNull(newS.getEntitledGroupTypes());
        assertNotNull(newS.getManagedGroups());
        assertNotNull(newS.getServerInfo());
        assertNotNull(newS.getServerInfo().getCheckinCounter());
        return newS;
    }

    private static void populateServer(Server s, User owner, int type) {
        s.setCreator(owner);
        s.setOrg(owner.getOrg());
        s.setDigitalServerId("ID-" + TestUtils.randomString());
        s.setOs("SUSE Linux");
        s.setRunningKernel(RUNNING_KERNEL);
        s.setName("serverfactorytest" + TestUtils.randomString() + ".example.com");
        s.setRelease("15");
        s.setSecret("1234567890123456789012345678901234567890123456789012345678901234");
        s.setAutoUpdate("N");
        s.setLastBoot(System.currentTimeMillis() / 1000);
        s.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        s.setCreated(new Date());
        s.setModified(new Date());
        s.setRam(1024);
        s.setContactMethod(ServerFactory.findContactMethodById(0L));

        if (type == TYPE_SERVER_MGR) {
            // a Mgr Server is also a Minion
            MinionServer minionServer = (MinionServer) s;
            minionServer.setMinionId(s.getName());
            minionServer.setOsFamily("Suse");
            minionServer.setMachineId(TestUtils.randomString());

            ReportDBCredentials reportCredentials = CredentialsFactory.createReportCredentials("pythia", "secret");
            CredentialsFactory.storeCredentials(reportCredentials);

            MgrServerInfo info = new MgrServerInfo();
            info.setVersion(PackageEvrFactory.lookupOrCreatePackageEvr(null, "2022.03", "0", s.getPackageType()));
            info.setReportDbName("reportdb");
            info.setReportDbCredentials(reportCredentials);
            info.setReportDbHost("localhost");
            info.setReportDbPort(5432);
            info.setServer(minionServer);
            minionServer.setMgrServerInfo(info);
        }
        else if (type == TYPE_SERVER_PROXY) {
            // a Proxy Server is also a Minion
            MinionServer minionServer = (MinionServer) s;
            minionServer.setMinionId(s.getName());
            minionServer.setOsFamily("Suse");
            minionServer.setMachineId(TestUtils.randomString());

            ProxyInfo info = new ProxyInfo();
            info.setVersion(PackageEvrFactory.lookupOrCreatePackageEvr("10", "10", "10", s.getPackageType()));
            info.setServer(s);
            s.setProxyInfo(info);
        }
        else if (type == TYPE_SERVER_MINION) {
            MinionServer minionServer = (MinionServer) s;
            minionServer.setMinionId(s.getName());
            minionServer.setOsFamily("Suse");
            minionServer.setMachineId(TestUtils.randomString());
        }
    }

    private static ProvisionState createProvisionState(Server srvr,
            String description, String label) {
        // Create/Set provisionState
        ProvisionState p = new ProvisionState();
        p.setDescription(description);
        p.setLabel(label + TestUtils.randomString());
        srvr.setProvisionState(p);

        return p;
    }

    private static ServerInfo createServerInfo(Server srvr, Date checkin, Long cnt) {
        ServerInfo si = new ServerInfo();
        si.setCheckin(checkin);
        si.setCheckinCounter(cnt);
        si.setServer(srvr);
        srvr.setServerInfo(si);
        return si;
    }

    public static Server createTestServer(User owner, boolean ensureOwnerAccess) {
        return createTestServer(owner, ensureOwnerAccess,
                ServerConstants.getServerGroupTypeSaltEntitled());
    }

    private static Server createServer(int type) {
        switch(type) {
            case TYPE_SERVER_NORMAL:
                return ServerFactory.createServer();
            case TYPE_SERVER_PROXY, TYPE_SERVER_MGR, TYPE_SERVER_MINION:
                return new MinionServer();

            default:
                return null;
        }
    }

    // This may be busted , can comment out
    @Test
    public void testCompatibleWithServer() throws Exception {
        /*
         * here we create a user as an org admin.
         * then we create two (minimum) Servers owned by the user and
         * which are enterprise_entitled.
         * We add the test channel to each of the servers.  This allows
         * us to test the compatibleWithServer method.
         */
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        UserManager.storeUser(user);

        Server srvr = createTestServer(user, true,
                ServerFactory.lookupServerGroupTypeByLabel("enterprise_entitled"));

        Server srvr1 = createTestServer(user, true,
                ServerFactory.lookupServerGroupTypeByLabel("enterprise_entitled"));
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        srvr.addChannel(channel);
        srvr1.addChannel(channel);
        ServerFactory.save(srvr);
        ServerFactory.save(srvr1);
        flushAndEvict(srvr1);
        srvr = reload(srvr);
        // Ok let's finally test what we came here for.
        List<Row> list = ServerFactory.compatibleWithServer(user, srvr);
        assertNotNull(list, "List is null");
        assertFalse(list.isEmpty(), "List is empty");
        boolean found = false;
        for (Row s : list) {
            assertNotNull(s, "List contains something other than Profiles");
            if (srvr1.getName().equals(s.get("name"))) {
                found = true;
            }
        }
        assertTrue(found, "Didn't get back the expected values");
    }

    @Test
    public void testListAdministrators() {

        //The org admin user
        User admin = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        admin.addPermanentRole(RoleFactory.ORG_ADMIN);

        //the non-orgadmin user who is a member of the group
        User regular =   UserTestUtils.createUser("testUser2", admin.getOrg().getId());
        regular.removePermanentRole(RoleFactory.ORG_ADMIN);

        //a user who shouldn't be able to admin the system
        User nonGroupAdminUser = UserTestUtils.createUser(
                 "testUser3", admin.getOrg().getId());
        nonGroupAdminUser.removePermanentRole(RoleFactory.ORG_ADMIN);

        ManagedServerGroup group = ServerGroupTestUtils.createManaged(admin);

        //create server set and add it to the group
        Server serverToSearch = ServerFactoryTest.createTestServer(admin, true);
        Set servers = new HashSet<>();
        servers.add(serverToSearch);
        SERVER_GROUP_MANAGER.addServers(group, servers, admin);
        assertFalse(group.getServers().isEmpty());
        //create admins set and add it to the grup
        Set admins = new HashSet<>();
        admins.add(regular);
        SERVER_GROUP_MANAGER.associateAdmins(group, admins, admin);
        assertTrue(SERVER_GROUP_MANAGER.canAccess(regular, group));
        ServerGroupFactory.save(group);
        group = reload(group);
        UserFactory.save(admin);
        admin = reload(admin);
        UserFactory.save(regular);
        regular = reload(regular);
        UserFactory.save(nonGroupAdminUser);
        nonGroupAdminUser = reload(nonGroupAdminUser);

        List<User> users = ServerFactory.listAdministrators(serverToSearch);
        System.out.println(users);
        System.out.println("regular->" + regular);
        System.out.println("Admins->" + admins);
        boolean containsAdmin = false;
        boolean containsRegular = false;
        boolean containsNonGroupAdmin = false;  //we want this to be false to pass

        for (User user : users) {
              if (user.getLogin().equals(admin.getLogin())) {
                  containsAdmin = true;
              }
              if (user.getLogin().equals(regular.getLogin())) {
                  containsRegular = true;
              }
              if (user.getLogin().equals(nonGroupAdminUser.getLogin())) {
                  containsNonGroupAdmin = true;
              }
        }
         assertTrue(containsAdmin);
         assertTrue(containsRegular);
         assertFalse(containsNonGroupAdmin);
      }

    @Test
    public void testGetServerHistory() throws Exception {

        Server serverTest = ServerFactoryTest.createTestServer(user);
        ServerHistoryEvent event1 = new ServerHistoryEvent();
        event1.setSummary("summary1");
        event1.setDetails("details1");
        event1.setServer(serverTest);

        Set history = serverTest.getHistory();
        history.add(event1);

        ServerFactory.save(serverTest);
        TestUtils.saveAndFlush(event1);
        Long eventId = event1.getId();
        Long sid = serverTest.getId();

        HibernateFactory.getSession().clear();
        serverTest = ServerFactory.lookupById(sid);
        boolean hasEvent = false;
        for (ServerHistoryEvent she : serverTest.getHistory()) {
            if (eventId.equals(she.getId())) {
                hasEvent = true;
                break;
            }
        }
        assertTrue(hasEvent);
    }


    /**
     * Creates a true proxy server by creating a test system, creating a base channel,
     *      subscribing the system to that base channel, creating a child channel,
     *      setting all the values of that child channel to make it a proxy channel,
     *      and then activating the system as a proxy
     * @param owner user that is creating the proxy
     * @param ensureOwnerAccess if set to true, a Server Group will be created for that
     *          user and system
     * @return the created proxy server
     * @throws Exception something bad happened
     */
    public static Server createTestProxyServer(User owner, boolean ensureOwnerAccess)
                throws Exception {
        Server server = createTestServer(owner, ensureOwnerAccess);
        Channel baseChan = ChannelFactoryTest.createBaseChannel(owner);
        server.addChannel(baseChan);

        Channel proxyChan = ChannelFactoryTest.createTestChannel(owner);
        Set chanFamilies = new HashSet<>();

        ChannelFamily proxyFam = ChannelFamilyFactory.lookupByLabel(
                ChannelFamilyFactory.PROXY_CHANNEL_FAMILY_LABEL, owner.getOrg());
        if (proxyFam == null) {
            proxyFam = ChannelFamilyFactoryTest.createTestChannelFamily(owner);
            proxyFam.setLabel(ChannelFamilyFactory.PROXY_CHANNEL_FAMILY_LABEL);
            ChannelFamilyFactory.save(proxyFam);
        }
        chanFamilies.add(proxyFam);

        ChannelProduct product = new ChannelProduct();
        product.setProduct("proxy" + TestUtils.randomString());
        product.setVersion("1.1");
        product.setBeta(false);
        proxyChan.setProduct(product);
        proxyChan.setChannelFamilies(chanFamilies);
        proxyChan.setParentChannel(baseChan);

        ChannelFactory.save(baseChan);
        ChannelFactory.save(proxyChan);
        product = TestUtils.saveAndReload(product);

        SystemManager.activateProxy(server, "1.1");
        SystemManager.storeServer(server);
        return server;
    }


    @Test
    public void testUnsubscribeFromAllChannels() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        ChannelFactoryTest.createBaseChannel(user);
        Server serverIn = ServerFactoryTest.createTestServer(user);

        server  = ServerFactory.unsubscribeFromAllChannels(user, serverIn);
        ServerFactory.commitTransaction();
        commitHappened();

        assertEquals(0, server.getChannels().size());
    }

    @Test
    public void testSet() {
        Server serverIn = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        set.addElement(serverIn.getId(), null);
        RhnSetManager.store(set);
        List<Server> servers = ServerFactory.listSystemsInSsm(user);
        assertEquals(1, servers.size());
        assertEquals(serverIn, servers.get(0));
    }

    private ServerSnapshot generateSnapshot(Server server2) {
        ServerSnapshot snap = new ServerSnapshot();
        snap.setServer(server2);
        snap.setOrg(server2.getOrg());
        snap.setReason("blah");
        return snap;
    }


    @Test
    public void testListSnapshotsForServer() {
        Server server2 = ServerFactoryTest.createTestServer(user, true);
        ServerSnapshot snap = generateSnapshot(server2);
        ServerGroup grp = ServerGroupTestUtils.createEntitled(server2.getOrg(),
         ServerConstants.getServerGroupTypeEnterpriseEntitled());
        snap.addGroup(grp);

        TestUtils.saveAndFlush(snap);
        List<ServerSnapshot> list = ServerFactory.listSnapshots(server2.getOrg(),
                server2, null, null);
        assertContains(list, snap);
        assertContains(snap.getGroups(), grp);
    }

    @Test
    public void testLookupSnapshotById() {
        Server server2 = ServerFactoryTest.createTestServer(user, true);
        ServerSnapshot snap = generateSnapshot(server2);
        TestUtils.saveAndFlush(snap);

        ServerSnapshot snap2 = ServerFactory.lookupSnapshotById(snap.getId().intValue());
        assertEquals(snap, snap2);
    }


    @Test
    public void testDeleteSnapshot() {
        Server server2 = ServerFactoryTest.createTestServer(user, true);
        ServerSnapshot snap = generateSnapshot(server2);
        TestUtils.saveAndFlush(snap);
        ServerFactory.deleteSnapshot(snap);
        boolean lost = false;
        ServerSnapshot snap2 = ServerFactory.lookupSnapshotById(
            snap.getId().intValue());
        assertNull(snap2);
    }


    @Test
    public void testGetSnapshotTags() {
        Server server2 = ServerFactoryTest.createTestServer(user, true);
        ServerSnapshot snap = generateSnapshot(server2);

        SnapshotTag tag = new SnapshotTag();
        SnapshotTagName name = new SnapshotTagName();
        name.setName("blah");
        tag.setName(name);
        tag.setOrg(server2.getOrg());

        ServerSnapshotTagLink link = new ServerSnapshotTagLink();
        link.setServer(server2);
        link.setSnapshot(snap);
        link.setTag(tag);

        TestUtils.saveAndFlush(tag);
        TestUtils.saveAndFlush(snap);
        TestUtils.saveAndFlush(link);

        List<SnapshotTag> tags = ServerFactory.getSnapshotTags(snap);
        assertContains(tags, tag);
    }

    @Test
    public void testErrataAction() throws Exception {
        PackageName p1Name = PackageNameTest.createTestPackageName("testPackage1-" + TestUtils.randomString());

        PackageArch parch1 = (PackageArch) TestUtils.lookupFromCacheById(100L, "PackageArch.findById");

        Package zypper = new Package();
        PackageTest.populateTestPackage(zypper, user.getOrg(),  PackageFactory.lookupOrCreatePackageByName("zypper"),
                PackageEvrFactoryTest.createTestPackageEvr("1", "1.0.0", "1", PackageType.RPM), parch1);
        TestUtils.saveAndFlush(zypper);

        Package p1v1 = new Package();
        PackageTest.populateTestPackage(p1v1, user.getOrg(), p1Name,
                PackageEvrFactoryTest.createTestPackageEvr("1", "1.0.0", "1", PackageType.RPM), parch1);
        TestUtils.saveAndFlush(p1v1);

        Package p1v2 = new Package();
        PackageTest.populateTestPackage(p1v2, user.getOrg(), p1Name,
                PackageEvrFactoryTest.createTestPackageEvr("1", "2.0.0", "1", PackageType.RPM), parch1);
        TestUtils.saveAndFlush(p1v2);

        InstalledPackage p1v1InNZ = new InstalledPackage();
        p1v1InNZ.setEvr(p1v1.getPackageEvr());
        p1v1InNZ.setArch(p1v1.getPackageArch());
        p1v1InNZ.setName(p1v1.getPackageName());

        InstalledPackage p1v1InZ = new InstalledPackage();
        p1v1InZ.setEvr(p1v1.getPackageEvr());
        p1v1InZ.setArch(p1v1.getPackageArch());
        p1v1InZ.setName(p1v1.getPackageName());

        InstalledPackage zypperIn = new InstalledPackage();
        zypperIn.setEvr(zypper.getPackageEvr());
        zypperIn.setArch(zypper.getPackageArch());
        zypperIn.setName(zypper.getPackageName());

        Channel baseChan = ChannelFactoryTest.createBaseChannel(user);
        final String updateTag = "SLE-SERVER";
        baseChan.setUpdateTag(updateTag);

        MinionServer nonZypperSystem = MinionServerFactoryTest.createTestMinionServer(user);
        nonZypperSystem.addChannel(baseChan);
        p1v1InNZ.setServer(nonZypperSystem);
        nonZypperSystem.getPackages().add(p1v1InNZ);

        MinionServer zypperSystem = MinionServerFactoryTest.createTestMinionServer(user);
        zypperSystem.addChannel(baseChan);
        p1v1InZ.setServer(zypperSystem);
        zypperIn.setServer(zypperSystem);
        zypperSystem.getPackages().add(p1v1InZ);
        zypperSystem.getPackages().add(zypperIn);

        Errata e1 = ErrataFactoryTest.createTestErrata(user.getId());
        baseChan.addErrata(e1);
        e1.setAdvisoryName("SUSE-2016-1234");
        e1.getPackages().add(p1v2);

        ChannelFactory.save(baseChan);

        TestUtils.saveAndFlush(e1);

        List<MinionServer> minions = Arrays.asList(zypperSystem, nonZypperSystem);
        List<MinionSummary> minionSummaries = minions.stream().map(MinionSummary::new).collect(Collectors.toList());

        Map<LocalCall<?>, List<MinionSummary>> localCallListMap =
                SALT_SERVER_ACTION_SERVICE.errataAction(minionSummaries, Collections.singleton(e1.getId()), false);

        assertEquals(1, localCallListMap.size());
        localCallListMap.forEach((call, value) -> {
            assertEquals(2, value.size());
            assertEquals("state.apply", call.getPayload().get("fun"));
            Map<String, Object> kwarg = (Map<String, Object>) call.getPayload().get("kwarg");
            assertEquals(Collections.singletonList("packages.patchinstall"), kwarg.get("mods"));
            Map<String, Object> pillar = (Map<String, Object>) kwarg.get("pillar");
            Collection<String> regularPatches = (Collection<String>) pillar
                    .get(SaltServerActionService.PARAM_REGULAR_PATCHES);
            assertEquals(1, regularPatches.size());
            assertTrue(regularPatches.contains("SUSE-" + updateTag + "-2016-1234"));

            Collection<String> updateStackPatches = (Collection<String>) pillar
                    .get(SaltServerActionService.PARAM_UPDATE_STACK_PATCHES);
            assertEquals(0, updateStackPatches.size());
        });
    }

    @Test
    public void testlistNewestPkgsForServerErrata() throws Exception {
        Server srv = ServerFactoryTest.createTestServer(user, true);
        PackageName p1Name = PackageNameTest.createTestPackageName("testPackage1-" + TestUtils.randomString());
        PackageName p2Name = PackageNameTest.createTestPackageName("testPackage2-" + TestUtils.randomString());

        PackageArch parch1 = (PackageArch) TestUtils.lookupFromCacheById(100L, "PackageArch.findById");
        PackageArch parch2 = (PackageArch) TestUtils.lookupFromCacheById(101L, "PackageArch.findById");

        Package p1v1 = new Package();
        PackageTest.populateTestPackage(p1v1, user.getOrg(), p1Name,
                PackageEvrFactoryTest.createTestPackageEvr("1", "1.0.0", "1", srv.getPackageType()), parch1);
        TestUtils.saveAndFlush(p1v1);

        Package p1v2 = new Package();
        PackageTest.populateTestPackage(p1v2, user.getOrg(), p1Name,
                PackageEvrFactoryTest.createTestPackageEvr("1", "2.0.0", "1", srv.getPackageType()), parch1);
        TestUtils.saveAndFlush(p1v2);

        PackageEvr v3 = PackageEvrFactoryTest.createTestPackageEvr("1", "3.0.0", "1",
                srv.getPackageType());

        Package p1v3 = new Package();
        PackageTest.populateTestPackage(p1v3, user.getOrg(), p1Name, v3, parch1);
        TestUtils.saveAndFlush(p1v3);

        Package p1v4 = new Package();
        PackageTest.populateTestPackage(p1v4, user.getOrg(), p1Name,
                PackageEvrFactoryTest.createTestPackageEvr("1", "3.0.0", "1",
                        srv.getPackageType()), parch1);
        TestUtils.saveAndFlush(p1v4);

        Package p1v3arch2 = new Package();
        PackageTest.populateTestPackage(p1v3arch2, user.getOrg(), p1Name, v3, parch2);
        TestUtils.saveAndFlush(p1v3arch2);

        Package p2v4 = new Package();
        PackageTest.populateTestPackage(p2v4, user.getOrg(), p2Name,
                PackageEvrFactoryTest.createTestPackageEvr("1", "4.0.0", "1",
                        srv.getPackageType()), parch1);
        TestUtils.saveAndFlush(p2v4);


        InstalledPackage p1v1In = new InstalledPackage();
        p1v1In.setEvr(p1v1.getPackageEvr());
        p1v1In.setArch(p1v1.getPackageArch());
        p1v1In.setName(p1v1.getPackageName());

        Set<Long> serverIds = new HashSet<>();
        Set<Long> errataIds = new HashSet<>();

        serverIds.add(srv.getId());
        Channel baseChan = ChannelFactoryTest.createBaseChannel(user);
        srv.addChannel(baseChan);
        p1v1In.setServer(srv);
        srv.getPackages().add(p1v1In);

        Channel childChan = ChannelFactoryTest.createTestChannel(user);
        childChan.setParentChannel(baseChan);

        Errata e1 = ErrataFactoryTest.createTestErrata(user.getId());
        errataIds.add(e1.getId());
        baseChan.addErrata(e1);
        e1.getPackages().add(p1v2);
        e1.getPackages().add(p2v4);
        baseChan.getPackages().add(p1v2);
        baseChan.getPackages().add(p2v4);

        Errata e2 = ErrataFactoryTest.createTestErrata(user.getId());
        errataIds.add(e2.getId());
        baseChan.addErrata(e2);
        e2.getPackages().add(p1v3);
        baseChan.getPackages().add(p1v3);

        Errata e3 = ErrataFactoryTest.createTestErrata(user.getId());
        errataIds.add(e3.getId());
        baseChan.addErrata(e3);
        e3.getPackages().add(p1v3arch2);
        baseChan.getPackages().add(p1v3arch2);

        Errata e4 = ErrataFactoryTest.createTestErrata(user.getId());
        errataIds.add(e4.getId());
        childChan.addErrata(e4);
        e4.getPackages().add(p1v2);
        childChan.getPackages().add(p1v2);

        Errata e5 = ErrataFactoryTest.createTestErrata(user.getId());
        childChan.addErrata(e4);
        e4.getPackages().add(p1v4);
        childChan.getPackages().add(p1v4);

        ChannelFactory.save(baseChan);
        ChannelFactory.save(childChan);

        TestUtils.saveAndFlush(e1);
        TestUtils.saveAndFlush(e2);
        TestUtils.saveAndFlush(e3);
        TestUtils.saveAndFlush(e4);
        TestUtils.saveAndFlush(e5);

        Map<Long, Map<String, Tuple2<String, String>>> out =
                ServerFactory.listNewestPkgsForServerErrata(serverIds, errataIds);
        Map<String, Tuple2<String, String>> packages = out.get(srv.getId());
        assertEquals(1, packages.size());
        assertEquals(p1v3.getPackageEvr().toString(), packages.get(p1v3.getPackageName().getName()).getB());
    }

    @Test
    public void testListErrataNamesForServer() throws Exception {
        Set<Long> serverIds = new HashSet<>();
        Set<Long> errataIds = new HashSet<>();

        Server srv = ServerFactoryTest.createTestServer(user, true);
        serverIds.add(srv.getId());
        Channel baseChan = ChannelFactoryTest.createBaseChannel(user);
        baseChan.setUpdateTag("SLE-SERVER");
        srv.addChannel(baseChan);

        Errata e = ErrataFactoryTest.createTestErrata(user.getId());
        errataIds.add(e.getId());
        e.setAdvisoryName("SUSE-2016-1234");
        baseChan.addErrata(e);

        Errata ce = ErrataFactoryTest.createTestErrata(user.getId());
        errataIds.add(ce.getId());
        ce.setAdvisoryName("CL-SUSE-2016-1234");
        baseChan.addErrata(ce);

        ChannelFactory.save(baseChan);

        TestUtils.saveAndFlush(e);

        Map<Long, Map<Long, Set<ErrataInfo>>> out =
                ServerFactory.listErrataNamesForServers(serverIds, errataIds);
        Set<ErrataInfo> errataName = out.get(srv.getId()).get(e.getId());
        assertContains(errataName, new ErrataInfo("SUSE-SLE-SERVER-2016-1234", false, false));

        errataName = out.get(srv.getId()).get(ce.getId());
        assertContains(errataName, new ErrataInfo("CL-SUSE-SLE-SERVER-2016-1234", false, false));
    }

    @Test
    public void testListErrataNamesForServerSLE11() throws Exception {
        Set<Long> serverIds = new HashSet<>();
        Set<Long> errataIds = new HashSet<>();

        Server srv = ServerFactoryTest.createTestServer(user, true);
        serverIds.add(srv.getId());
        Channel baseChan = ChannelFactoryTest.createBaseChannel(user);
        baseChan.setUpdateTag("slessp4");
        srv.addChannel(baseChan);

        Errata e = ErrataFactoryTest.createTestErrata(user.getId());
        errataIds.add(e.getId());
        e.setAdvisoryName("ecryptfs-utils-12379");
        baseChan.addErrata(e);

        Errata ce = ErrataFactoryTest.createTestErrata(user.getId());
        errataIds.add(ce.getId());
        ce.setAdvisoryName("CL-ecryptfs-utils-12379");
        baseChan.addErrata(ce);

        ChannelFactory.save(baseChan);

        TestUtils.saveAndFlush(e);

        Map<Long, Map<Long, Set<ErrataInfo>>> out =
                ServerFactory.listErrataNamesForServers(serverIds, errataIds);
        Set<ErrataInfo> errataName = out.get(srv.getId()).get(e.getId());
        assertContains(errataName, new ErrataInfo("slessp4-ecryptfs-utils-12379", false, false));

        errataName = out.get(srv.getId()).get(ce.getId());
        assertContains(errataName,
                new ErrataInfo("slessp4-CL-ecryptfs-utils-12379", false, false));
    }

    /**
     * Tests assignment of a server path to a minion.
     * @throws Exception - if anything goes wrong.
     */
    @Test
    public void testAddRemoveServerPath() throws Exception {
        Server minion = ServerTestUtils.createTestSystem();
        Server proxy = ServerTestUtils.createTestSystem();
        String proxyHostname = "proxyHostname";
        Set<ServerPath> serverPaths = ServerFactory.createServerPaths(minion, proxy, proxyHostname);
        minion.getServerPaths().addAll(serverPaths);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();
        ServerFactory.lookupById(proxy.getId());

        Server s = ServerFactory.lookupById(minion.getId());
        assertEquals(serverPaths.stream().findFirst().get(),
                s.getServerPaths().stream().findFirst().get());

        s.getServerPaths().remove(s.getServerPaths().stream().findFirst().get());
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        s = ServerFactory.lookupById(minion.getId());
        assertTrue(s.getServerPaths().isEmpty());
    }

    /**
     * Tests that the position of server path of a server behind a proxied proxy is correctly
     * set.
     * @throws Exception - if anything goes wrong.
     */
    @Test
    public void testNestedProxyPosition() throws Exception {
        Server proxiedProxy = ServerTestUtils.createTestSystem();
        Server proxy = ServerTestUtils.createTestSystem();
        Set<ServerPath> proxyPaths = ServerFactory.createServerPaths(proxiedProxy, proxy, "proxy1");
        proxiedProxy.getServerPaths().addAll(proxyPaths);

        assertEquals(1, proxyPaths.size());
        assertEquals(Long.valueOf(0L), proxyPaths.iterator().next().getPosition());
        assertEquals(proxiedProxy, proxyPaths.iterator().next().getId().getServer());
        assertEquals(proxy, proxyPaths.iterator().next().getId().getProxyServer());
        assertEquals("proxy1", proxyPaths.iterator().next().getHostname());

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();
        HibernateFactory.getSession().refresh(proxiedProxy);

        Server minion = ServerTestUtils.createTestSystem();
        Set<ServerPath> serverPath1 = ServerFactory.createServerPaths(minion, proxiedProxy, "proxy2");
        minion.getServerPaths().addAll(serverPath1);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();
        HibernateFactory.getSession().refresh(minion);

        proxyPaths = minion.getServerPaths();
        assertEquals(2, proxyPaths.size());

        ServerPath first = minion.getFirstServerPath().get();
        assertEquals(Long.valueOf(0L), first.getPosition());
        assertEquals(minion, first.getId().getServer());
        assertEquals(proxiedProxy, first.getId().getProxyServer());
        assertEquals("proxy2", first.getHostname());

        ServerPath second = serverPath1.stream().filter(p -> p.getPosition() == 1L).findFirst().get();
        assertEquals(Long.valueOf(1L), second.getPosition());
        assertEquals(minion, second.getId().getServer());
        assertEquals(proxy, second.getId().getProxyServer());
        assertEquals("proxy1", second.getHostname());
    }

    /**
     * Tests looking up of a proxy server, assuming the proxy's FQDN is
     * in rhnServer.
     * @throws Exception - if anything goes wrong.
     */
    @Test
    public void testLookupProxyServer() throws Exception {
        Server s = createTestServer(user,
                false,
                ServerConstants.getServerGroupTypeEnterpriseEntitled(),
                TYPE_SERVER_PROXY);
        s.setHostname(HOSTNAME);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        // FQDN: precise lookup
        assertEquals(s, ServerFactory.lookupProxyServer(HOSTNAME).get());
        // plain hostname: imprecise lookup
        String simpleHostname = HOSTNAME.split("\\.")[0];
        assertEquals(s, ServerFactory.lookupProxyServer(simpleHostname).get());
    }

    /**
     * Tests looking up of a proxy server, assuming the proxy's FQDN is
     * in rhnServer and FQDN name have different cases.
     * @throws Exception - if anything goes wrong.
     */
    @Test
    public void testLookupProxyServerFQDNWithCaseName() throws Exception {
        Server s = createTestServer(user,
                false,
                ServerConstants.getServerGroupTypeEnterpriseEntitled(),
                TYPE_SERVER_PROXY);
        String hostCaseName = "fooBeer.bar.com";
        s.setHostname(hostCaseName);
        s.addFqdn(hostCaseName);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        // FQDN: precise lookup
        assertEquals(s, ServerFactory.lookupProxyServer(hostCaseName).get());
    }

    /**
     * Tests looking up of a proxy server, assuming the proxy's FQDN is
     * in rhnServer and FQDN name with case different from the used in query.
     * @throws Exception - if anything goes wrong.
     */
    @Test
    public void testLookupProxyServerFQDNIgnoreCase() throws Exception {
        Server s = createTestServer(user,
                false,
                ServerConstants.getServerGroupTypeEnterpriseEntitled(),
                TYPE_SERVER_PROXY);
        String hostCaseName = "fooBeer.bar.com";
        s.setHostname(hostCaseName);
        s.addFqdn(hostCaseName);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        // FQDN: precise lookup
        assertEquals(s, ServerFactory.lookupProxyServer(hostCaseName.toLowerCase()).get());
    }

    /**
     * Tests looking up of a proxy server, assuming the proxy's simple name is
     * in rhnServer.
     * @throws Exception - if anything goes wrong.
     */
    @Test
    public void testLookupProxyServerWithSimpleName() throws Exception {
        Server s = createTestServer(user,
                false,
                ServerConstants.getServerGroupTypeEnterpriseEntitled(),
                TYPE_SERVER_PROXY);
        String fullyQualifiedDomainName = HOSTNAME;
        String simpleHostname = HOSTNAME.split("\\.")[0];
        s.setHostname(simpleHostname);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        // FQDN: imprecise lookup
        assertEquals(s, ServerFactory.lookupProxyServer(fullyQualifiedDomainName).get());
        // plain hostname: precise lookup
        assertEquals(s, ServerFactory.lookupProxyServer(simpleHostname).get());
    }

    @Test
    public void testFindServersInSetByChannel() throws Exception {
        Server srv = ServerFactoryTest.createTestServer(user, true);
        Channel parent = ChannelFactoryTest.createTestChannel(user);
        parent.setParentChannel(null);

        Channel child = ChannelFactoryTest.createTestChannel(user);
        child.setParentChannel(parent);

        srv.addChannel(parent);
        srv.addChannel(child);

        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        set.addElement(srv.getId() + "");
        RhnSetManager.store(set);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        List<Long> servers = ServerFactory.findServersInSetByChannel(user, srv.getBaseChannel().getId());
        assertEquals(1, servers.size());
        assertEquals(srv.getId(), servers.stream().findFirst().get());

    }

    @Test
    public void testFilterSystemsWithMaintOnlyActions() throws Exception {
        Server systemWith = MinionServerFactoryTest.createTestMinionServer(user);
        Server systemWithout = MinionServerFactoryTest.createTestMinionServer(user);

        // non-offending action
        Action allowedAction = ActionFactoryTest.createAction(user, ActionFactory.TYPE_HARDWARE_REFRESH_LIST);
        // assign it to both systems
        ServerActionTest.createServerAction(systemWith, allowedAction);
        ServerActionTest.createServerAction(systemWithout, allowedAction);

        // offending action
        Action disallowedAction = ActionFactoryTest.createAction(user, ActionFactory.TYPE_APPLY_STATES);
        // assign it to one system only
        ServerActionTest.createServerAction(systemWith, disallowedAction);

        Set<Long> filtered = ServerFactory
                .filterSystemsWithPendingMaintOnlyActions(Set.of(systemWith.getId(), systemWithout.getId()));
        assertEquals(Set.of(systemWith.getId()), filtered);
    }

    /**
     * Test assigning maintenance windows to systems
     *
     * @throws Exception
     */
    @Test
    public void testSetMaintenanceWindowToSystems() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        MaintenanceSchedule schedule = new MaintenanceManager().createSchedule(
                user, "test-schedule-1", MaintenanceSchedule.ScheduleType.SINGLE, Optional.empty());

        Server sys1 = MinionServerFactoryTest.createTestMinionServer(user);
        Server sys2 = MinionServerFactoryTest.createTestMinionServer(user);

        ServerFactory.setMaintenanceScheduleToSystems(schedule, Set.of(sys1.getId(), sys2.getId()));

        assertEquals(schedule, HibernateFactory.reload(sys1).getMaintenanceScheduleOpt().get());
        assertEquals(schedule, HibernateFactory.reload(sys2).getMaintenanceScheduleOpt().get());
    }

    @Test
    public void testQuerySlesSystems() {
        Server s1 = createTestServer(user, true);
        s1.setName("first-system");
        s1.setOs("SLES");
        Server s2 = createTestServer(user, false);
        s2.setName("second-system");
        s2.setOs("SLES");
        Server s3 = createTestServer(user, true);
        s3.setName("third-system");
        s3.setOs("not-SLES");

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        List<Server> result = ServerFactory.querySlesSystems("", 20, user).collect(Collectors.toList());

        assertEquals(1, result.size());
        assertEquals("first-system", result.get(0).getName());
    }

    @Test
    public void testQuerySlesSystemsWithQueryString() {
        Server s1 = createTestServer(user, true);
        s1.setName("my-foo-system-1");
        s1.setOs("SLES");
        Server s2 = createTestServer(user, true);
        s2.setName("my-foo-system-2");
        s2.setOs("SLES");
        Server s3 = createTestServer(user, true);
        s3.setName("my-bar-system");
        s3.setOs("SLES");

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        List<Server> result = ServerFactory.querySlesSystems("foo", 20, user).collect(Collectors.toList());

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(s -> "my-foo-system-1".equals(s.getName())));
        assertTrue(result.stream().anyMatch(s -> "my-foo-system-2".equals(s.getName())));
    }

    @Test
    public void testGetInstalledKernelVersions() throws Exception {
        Server srv = createTestServer(user);
        PackageArch pkgArch = (PackageArch) TestUtils.lookupFromCacheById(100L, "PackageArch.findById");

        Package pkg1 = PackageTest.createTestPackage(null);
        PackageEvr pkgEvr1 = PackageEvrFactoryTest.createTestPackageEvr(null, "1.1.0", "1", PackageType.RPM);
        PackageName pkgName1 = PackageNameTest.createTestPackageName("kernel-default");
        PackageTest.populateTestPackage(pkg1, null, pkgName1, pkgEvr1, pkgArch);

        Package pkg2 = PackageTest.createTestPackage(null);
        PackageEvr pkgEvr2 = PackageEvrFactoryTest.createTestPackageEvr(null, "1.0.0", "1", PackageType.RPM);
        PackageName pkgName2 = PackageNameTest.createTestPackageName("not-a-kernel");
        PackageTest.populateTestPackage(pkg2, null, pkgName2, pkgEvr2, pkgArch);

        Package pkg3 = PackageTest.createTestPackage(null);
        PackageEvr pkgEvr3 = PackageEvrFactoryTest.createTestPackageEvr(null, "1.1.2", "1", PackageType.RPM);
        PackageName pkgName3 = PackageNameTest.createTestPackageName("kernel-default");
        PackageTest.populateTestPackage(pkg3, null, pkgName3, pkgEvr3, pkgArch);

        PackageTestUtils.installPackageOnServer(pkg1, srv);
        PackageTestUtils.installPackageOnServer(pkg2, srv);
        PackageTestUtils.installPackageOnServer(pkg3, srv);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        List<PackageEvr> result = ServerFactory.getInstalledKernelVersions(srv).collect(Collectors.toList());

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(pkgEvr1::equals));
        assertTrue(result.stream().anyMatch(pkgEvr3::equals));
    }

    @Test
    public void canIdentifyIfPtfUninstallationIsSupported() {
        Package zypperNoSupport = PackageTestUtils.createZypperPackage("1.14.50", user);
        Package zypperWithSupport = PackageTestUtils.createZypperPackage("1.14.59", user);

        Server noPtfSupport = createTestServer(user);
        noPtfSupport.setOs(ServerConstants.ALMA);

        Server ptfSupportNoUninstall = createTestServer(user);
        ptfSupportNoUninstall.setOs(ServerConstants.SLES);
        ptfSupportNoUninstall.setRelease("15.3");
        PackageTestUtils.installPackagesOnServer(List.of(zypperNoSupport), ptfSupportNoUninstall);

        Server ptfFullSupport = createTestServer(user);
        ptfFullSupport.setOs(ServerConstants.SLES);
        ptfFullSupport.setRelease("15.3");
        PackageTestUtils.installPackagesOnServer(List.of(zypperWithSupport), ptfFullSupport);

        noPtfSupport = TestUtils.reload(noPtfSupport);
        ptfSupportNoUninstall = TestUtils.reload(ptfSupportNoUninstall);
        ptfFullSupport = TestUtils.reload(ptfFullSupport);

        assertFalse(noPtfSupport.doesOsSupportPtf());
        assertFalse(ServerFactory.isPtfUninstallationSupported(noPtfSupport));

        assertTrue(ptfSupportNoUninstall.doesOsSupportPtf());
        assertFalse(ServerFactory.isPtfUninstallationSupported(ptfSupportNoUninstall));

        assertTrue(ptfFullSupport.doesOsSupportPtf());
        assertTrue(ServerFactory.isPtfUninstallationSupported(ptfFullSupport));
    }

}
