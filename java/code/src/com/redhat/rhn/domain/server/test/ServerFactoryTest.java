/**
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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.channel.ChannelFamily;
import com.redhat.rhn.domain.channel.ChannelFamilyFactory;
import com.redhat.rhn.domain.channel.ChannelProduct;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.channel.test.ChannelFamilyFactoryTest;
import com.redhat.rhn.domain.common.ProvisionState;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.test.CustomDataKeyTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.PackageName;
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
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.Note;
import com.redhat.rhn.domain.server.ProxyInfo;
import com.redhat.rhn.domain.server.SatelliteServer;
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
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.user.UserManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.ConfigTestUtils;
import com.redhat.rhn.testing.ServerGroupTestUtils;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;
import com.suse.manager.webui.services.SaltServerActionService;
import com.suse.salt.netapi.calls.LocalCall;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ServerFactoryTest
 */
public class ServerFactoryTest extends BaseTestCaseWithUser {
    private Server server;
    public static final int TYPE_SERVER_SATELLITE = 0;
    public static final int TYPE_SERVER_PROXY = 1;
    public static final int TYPE_SERVER_NORMAL = 2;
    public static final int TYPE_SERVER_VIRTUAL = 3;
    public static final int TYPE_SERVER_MINION = 4;
    public static final String RUNNING_KERNEL = "2.6.9-55.EL";
    public static final String HOSTNAME = "foo.bar.com";

    @Override
    public void setUp() throws Exception {
        super.setUp();
        server = createTestServer(user);
        assertNotNull(server.getId());
    }

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

    public void testServerGroupMembers() throws Exception {
        Server s = createTestServer(user);
        assertNotNull(s.getEntitledGroups());
        assertTrue(s.getEntitledGroups().size() > 0);
    }

    public void aTestChannels() throws Exception {
        System.out.println(
                "FIXME ASAP: rhnuser NEEDS access to rhnChannelCloned for this to work");
        Server testServer = createTestServer(user);
        Channel parent = ChannelFactoryTest.createTestChannel(user);
        parent.setParentChannel(null);

        Channel child = ChannelFactoryTest.createTestChannel(user);
        child.setParentChannel(parent);

        testServer.addChannel(parent);
        testServer.addChannel(child);

        Channel test = testServer.getBaseChannel();
        assertEquals(parent.getId(), test.getId());

        assertEquals(2, testServer.getChannels().size());
    }

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
        assertTrue(testServer.getCustomDataValues().size() > 0);

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

    public void testServerLookup() {
        assertNull(ServerFactory.lookupByIdAndOrg(-1234L, user.getOrg()));
        assertNotNull(ServerFactory.lookupByIdAndOrg(server.getId(),
                user.getOrg()));
    }

    public void testServerArchLookup() {
        assertNull(ServerFactory.lookupServerArchByLabel("8dafs8320921kfgbzz"));
        assertNotNull(ServerFactory.lookupServerArchByLabel("i386-redhat-linux"));
    }

    public void testServerGroupType() throws Exception {
        //let's hope nobody calls their server group this
        assertNull(ServerFactory.lookupServerGroupTypeByLabel("8dafs8320921kfgbzz"));
        assertNotNull(ServerConstants.getServerGroupTypeEnterpriseEntitled());
        assertNotNull(ServerFactory.lookupServerGroupTypeByLabel(
                ServerConstants.getServerGroupTypeEnterpriseEntitled().getLabel()));
    }

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
     * @throws Exception something bad happened
     */
    public void testServerGroups() throws Exception {
        Long id = server.getId();

        Collection servers = new ArrayList();
        servers.add(server);
        ServerGroupManager manager = ServerGroupManager.getInstance();
        user.addPermanentRole(RoleFactory.SYSTEM_GROUP_ADMIN);
        ManagedServerGroup sg1 = manager.create(user, "FooFooFOO", "Foo Description");
        manager.addServers(sg1, servers, user);

        server = reload(server);
        assertTrue(server.getEntitledGroups().size() == 1);
        assertTrue(server.getManagedGroups().size() == 1);


        String changedName = "The group name has been changed" +
            TestUtils.randomString();
        sg1.setName(changedName);

        ServerFactory.save(server);

        //Evict from session to make sure that we get a fresh server
        //from the db.
        HibernateFactory.getSession().evict(server);

        Server server2 = ServerFactory.lookupByIdAndOrg(id, user.getOrg());
        assertTrue(server2.getManagedGroups().size() == 1);
        sg1 = server2.getManagedGroups().iterator().next();

        assertEquals(changedName, sg1.getName());

    }

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

        ServerFactory.removeServerFromGroup(testServer.getId(), group.getId());
        group = reload(group);

        Long membersFinally = group.getCurrentMembers();
        assertEquals(membersBefore, membersFinally);

    }

    public void testAddNoteToServer() throws Exception {
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

    public void testAddDeviceToServer() throws Exception {

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

    public void testAddingRamToServer() throws Exception {
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

    public void testAddingDmiToServer() throws Exception {

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
    public void testTwoServers() throws Exception {
        Server s1 = createTestServer(user);
        Server s2 = createTestServer(user);
        assertNotNull(s1);
        assertNotNull(s2);
    }

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
        SystemManager.entitleServer(s, EntitlementManager.VIRTUALIZATION);
        assertTrue(s.hasEntitlement(EntitlementManager.VIRTUALIZATION));
    }

    /**
     * Test that server does not have a specific entitlement.
     * @throws Exception something bad happened
     */
    public void testServerDoesNotHaveSpecificEntitlement() throws Exception {

        // The default test server should not have a virtualization entitlement.

        Server s = createTestServer(user);
        assertFalse(s.hasEntitlement(EntitlementManager.VIRTUALIZATION));
    }

    /**
     * Create a test Server and commit it to the DB.
     * @param owner the owner of this Server
     * @return Server that was created
     * @throws Exception something bad happened
     */
    public static Server createTestServer(User owner) throws Exception {
        return createTestServer(owner, false);
    }

    public static Server createTestServer(User owner, boolean ensureOwnerAccess,
            ServerGroupType type) {
        return createTestServer(owner, ensureOwnerAccess, type, TYPE_SERVER_NORMAL,
                                new Date());
    }


    public static Server createTestServer(User owner, boolean ensureOwnerAccess,
            ServerGroupType type, int stype) throws Exception {
        return createTestServer(owner, ensureOwnerAccess, type, stype, new Date());
    }


    private static Server createTestServer(User owner, boolean ensureOwnerAccess,
            ServerGroupType type, int stype, Date dateCreated) {

        Server newS = createUnentitledTestServer(owner, ensureOwnerAccess, stype,
                dateCreated);

        if (!type.getAssociatedEntitlement().isBase()) {
            EntitlementServerGroup mgmt = ServerGroupFactory.lookupEntitled(
                    EntitlementManager.MANAGEMENT, owner.getOrg());
            if (mgmt == null) {
                newS = TestUtils.saveAndReload(newS);
                mgmt = ServerGroupFactory.lookupEntitled(
                        EntitlementManager.MANAGEMENT,
                        owner.getOrg());
                newS = ServerFactory.lookupById(newS.getId());
            }
            assertNotNull(mgmt);
            assertNotNull(mgmt.getGroupType().getAssociatedEntitlement());
            SystemManager.entitleServer(newS,
                    mgmt.getGroupType().getAssociatedEntitlement());
        }


        EntitlementServerGroup sg = ServerGroupTestUtils.createEntitled(owner.getOrg(),
                                                                        type);

        SystemManager.entitleServer(newS, sg.getGroupType().getAssociatedEntitlement());
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
        TestUtils.saveAndReload(newS);


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

        /*
         * Since adding a server to a group is done by a stored proc, the
         * server object at this point doesn't know it has any groups; ie.,
         * newS.getGroups() == null. To fix this, we need to evict newS
         * from the session and look it back up.
         * This shouldn't be a problem in prod, just something we have to do
         * in our test code until we move to hib3 and can work with stored
         * procs.
         */
        // commitAndCloseSession();
        // System.out.println("COMMITED SESSION!\n\n");


        Long id = newS.getId();
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().evict(newS);
        newS = ServerFactory.lookupByIdAndOrg(id, owner.getOrg());
        assertNotNull(newS.getEntitledGroups());
        assertNotNull(newS.getManagedGroups());
        assertNotNull(newS.getServerInfo());
        assertNotNull(newS.getServerInfo().getCheckinCounter());
        return newS;
    }

    private static void populateServer(Server s, User owner, int type) {
        s.setCreator(owner);
        s.setOrg(owner.getOrg());
        s.setDigitalServerId("ID-" + TestUtils.randomString());
        s.setOs("Red Hat Linux");
        s.setRunningKernel(RUNNING_KERNEL);
        s.setName("serverfactorytest" + TestUtils.randomString() + ".rhn.redhat.com");
        s.setRelease("9");
        s.setSecret("12345678901234567890123456789012");
        s.setAutoUpdate("N");
        s.setLastBoot(System.currentTimeMillis() / 1000);
        s.setServerArch(ServerFactory.lookupServerArchByLabel("i386-redhat-linux"));
        s.setCreated(new Date());
        s.setModified(new Date());
        s.setRam(1024);
        s.setContactMethod(ServerFactory.findContactMethodById(0L));

        if (type == TYPE_SERVER_SATELLITE) {
            SatelliteServer ss = (SatelliteServer) s;
            ss.setProduct("SPACEWALK-001");
            ss.setOwner("Spacewalk Test Cert");
            ss.setIssued("2007-07-13 00:00:00");
            ss.setExpiration("2020-07-13 00:00:00");
            ss.setVersion("4.0");
        }
        else if (type == TYPE_SERVER_PROXY) {
            ProxyInfo info = new ProxyInfo();
            info.setVersion("10", "10", "10");
            info.setServer(s);
            s.setProxyInfo(info);
        }
        else if (type == TYPE_SERVER_MINION) {
            MinionServer minionServer = (MinionServer) s;
            minionServer.setMinionId(s.getName());
            minionServer.setOsFamily("RedHat");
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
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
    }

    private static Server createServer(int type) {
        switch(type) {
            case TYPE_SERVER_SATELLITE:
                return new SatelliteServer();
            case TYPE_SERVER_PROXY:
            case TYPE_SERVER_NORMAL:
                return ServerFactory.createServer();
            case TYPE_SERVER_MINION:
                return new MinionServer();

            default:
                return null;
        }
    }

    // This may be busted , can comment out
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
        List list = ServerFactory.compatibleWithServer(user, srvr);
        assertNotNull("List is null", list);
        assertFalse("List is empty", list.isEmpty());
        boolean found = false;
        for (Iterator itr = list.iterator(); itr.hasNext();) {
            Object o = itr.next();
            assertEquals("List contains something other than Profiles",
                    HashMap.class, o.getClass());
            Map s = (Map) o;
            if (srvr1.getName().equals(s.get("name"))) {
                found = true;
            }
        }
        assertTrue("Didn't get back the expected values", found);
    }

    public void testListAdministrators() throws Exception {

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
        Set servers = new HashSet();
        servers.add(serverToSearch);
        ServerGroupManager manager = ServerGroupManager.getInstance();
        manager.addServers(group, servers, admin);
        assertTrue(group.getServers().size() > 0);
        //create admins set and add it to the grup
        Set admins = new HashSet();
        admins.add(regular);
        manager.associateAdmins(group, admins, admin);
        assertTrue(manager.canAccess(regular, group));
        ServerGroupFactory.save(group);
        group = reload(group);
        UserFactory.save(admin);
        admin = reload(admin);
        UserFactory.save(regular);
        regular = reload(regular);
        UserFactory.save(nonGroupAdminUser);
        nonGroupAdminUser = reload(nonGroupAdminUser);

        List <User> users = ServerFactory.listAdministrators(serverToSearch);
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
        Set chanFamilies = new HashSet();

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


    public void testUnsubscribeFromAllChannels() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        ChannelFactoryTest.createBaseChannel(user);
        Server serverIn = ServerFactoryTest.createTestServer(user);

        server  = ServerFactory.unsubscribeFromAllChannels(user, serverIn);
        ServerFactory.commitTransaction();
        commitHappened();

        assertEquals(0, server.getChannels().size());
    }

    public void testSet() throws Exception {
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


    public void testListSnapshotsForServer() throws Exception {
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

    public void testLookupSnapshotById() throws Exception {
        Server server2 = ServerFactoryTest.createTestServer(user, true);
        ServerSnapshot snap = generateSnapshot(server2);
        TestUtils.saveAndFlush(snap);

        ServerSnapshot snap2 = ServerFactory.lookupSnapshotById(snap.getId().intValue());
        assertEquals(snap, snap2);
    }


    public void testDeleteSnapshot() throws Exception {
        Server server2 = ServerFactoryTest.createTestServer(user, true);
        ServerSnapshot snap = generateSnapshot(server2);
        TestUtils.saveAndFlush(snap);
        ServerFactory.deleteSnapshot(snap);
        boolean lost = false;
        ServerSnapshot snap2 = ServerFactory.lookupSnapshotById(
            snap.getId().intValue());
        assertNull(snap2);
    }


    public void testGetSnapshotTags() throws Exception {
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

    public void testErrataAction() throws Exception {
        PackageName p1Name = PackageNameTest.createTestPackageName("testPackage1-" + TestUtils.randomString());

        PackageArch parch1 = (PackageArch) TestUtils.lookupFromCacheById(100L, "PackageArch.findById");

        Package zypper = new Package();
        PackageTest.populateTestPackage(zypper, user.getOrg(),  PackageFactory.lookupOrCreatePackageByName("zypper"), PackageEvrFactoryTest.createTestPackageEvr("1", "1.0.0", "1"), parch1);
        TestUtils.saveAndFlush(zypper);

        Package p1v1 = new Package();
        PackageTest.populateTestPackage(p1v1, user.getOrg(), p1Name, PackageEvrFactoryTest.createTestPackageEvr("1", "1.0.0", "1"), parch1);
        TestUtils.saveAndFlush(p1v1);

        Package p1v2 = new Package();
        PackageTest.populateTestPackage(p1v2, user.getOrg(), p1Name, PackageEvrFactoryTest.createTestPackageEvr("1", "2.0.0", "1"), parch1);
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

        Errata e1 = ErrataFactoryTest.createTestPublishedErrata(user.getId());
        baseChan.addErrata(e1);
        e1.setAdvisoryName("SUSE-2016-1234");
        e1.getPackages().add(p1v2);

        ChannelFactory.save(baseChan);

        TestUtils.saveAndFlush(e1);

        List<MinionServer> minions = Arrays.asList(zypperSystem, nonZypperSystem);
        List<MinionSummary> minionSummaries = minions.stream().map(MinionSummary::new).collect(Collectors.toList());

        Map<LocalCall<?>, List<MinionSummary>> localCallListMap =
                SaltServerActionService.INSTANCE.errataAction(minionSummaries, Collections.singleton(e1.getId()));

        assertEquals(1, localCallListMap.size());
        localCallListMap.entrySet().forEach(result -> {
            assertEquals(2, result.getValue().size());
            final LocalCall<?> call = result.getKey();
            assertEquals("state.apply", call.getPayload().get("fun"));
            Map<String, Object> kwarg = (Map<String, Object>)call.getPayload().get("kwarg");
            assertEquals(Collections.singletonList("packages.patchinstall"), kwarg.get("mods"));
            Map<String, Object> pillar = (Map<String, Object>)kwarg.get("pillar");
            Collection<String> regularPatches = (Collection<String>) pillar
                    .get(SaltServerActionService.PARAM_REGULAR_PATCHES);
            assertEquals(1, regularPatches.size());
            assertEquals(true, regularPatches.contains("SUSE-" + updateTag + "-2016-1234"));

            Collection<String> updateStackPatches = (Collection<String>) pillar
                    .get(SaltServerActionService.PARAM_UPDATE_STACK_PATCHES);
            assertEquals(0, updateStackPatches.size());
        });
    }

    public void testlistNewestPkgsForServerErrata() throws Exception {
        PackageName p1Name = PackageNameTest.createTestPackageName("testPackage1-" + TestUtils.randomString());
        PackageName p2Name = PackageNameTest.createTestPackageName("testPackage2-" + TestUtils.randomString());

        PackageArch parch1 = (PackageArch) TestUtils.lookupFromCacheById(100L, "PackageArch.findById");
        PackageArch parch2 = (PackageArch) TestUtils.lookupFromCacheById(101L, "PackageArch.findById");

        Package p1v1 = new Package();
        PackageTest.populateTestPackage(p1v1, user.getOrg(), p1Name, PackageEvrFactoryTest.createTestPackageEvr("1", "1.0.0", "1"), parch1);
        TestUtils.saveAndFlush(p1v1);

        Package p1v2 = new Package();
        PackageTest.populateTestPackage(p1v2, user.getOrg(), p1Name, PackageEvrFactoryTest.createTestPackageEvr("1", "2.0.0", "1"), parch1);
        TestUtils.saveAndFlush(p1v2);

        PackageEvr v3 = PackageEvrFactoryTest.createTestPackageEvr("1", "3.0.0", "1");

        Package p1v3 = new Package();
        PackageTest.populateTestPackage(p1v3, user.getOrg(), p1Name, v3, parch1);
        TestUtils.saveAndFlush(p1v3);

        Package p1v4 = new Package();
        PackageTest.populateTestPackage(p1v4, user.getOrg(), p1Name, PackageEvrFactoryTest.createTestPackageEvr("1", "3.0.0", "1"), parch1);
        TestUtils.saveAndFlush(p1v4);

        Package p1v3arch2 = new Package();
        PackageTest.populateTestPackage(p1v3arch2, user.getOrg(), p1Name, v3, parch2);
        TestUtils.saveAndFlush(p1v3arch2);

        Package p2v4 = new Package();
        PackageTest.populateTestPackage(p2v4, user.getOrg(), p2Name, PackageEvrFactoryTest.createTestPackageEvr("1", "4.0.0", "1"), parch1);
        TestUtils.saveAndFlush(p2v4);


        InstalledPackage p1v1In = new InstalledPackage();
        p1v1In.setEvr(p1v1.getPackageEvr());
        p1v1In.setArch(p1v1.getPackageArch());
        p1v1In.setName(p1v1.getPackageName());

        Set<Long> serverIds = new HashSet<>();
        Set<Long> errataIds = new HashSet<>();

        Server server = ServerFactoryTest.createTestServer(user, true);
        serverIds.add(server.getId());
        Channel baseChan = ChannelFactoryTest.createBaseChannel(user);
        server.addChannel(baseChan);
        p1v1In.setServer(server);
        server.getPackages().add(p1v1In);

        Channel childChan = ChannelFactoryTest.createTestChannel(user);
        childChan.setParentChannel(baseChan);

        Errata e1 = ErrataFactoryTest.createTestPublishedErrata(user.getId());
        errataIds.add(e1.getId());
        baseChan.addErrata(e1);
        e1.getPackages().add(p1v2);
        e1.getPackages().add(p2v4);

        Errata e2 = ErrataFactoryTest.createTestPublishedErrata(user.getId());
        errataIds.add(e2.getId());
        baseChan.addErrata(e2);
        e2.getPackages().add(p1v3);

        Errata e3 = ErrataFactoryTest.createTestPublishedErrata(user.getId());
        errataIds.add(e3.getId());
        baseChan.addErrata(e3);
        e3.getPackages().add(p1v3arch2);

        Errata e4 = ErrataFactoryTest.createTestPublishedErrata(user.getId());
        errataIds.add(e4.getId());
        childChan.addErrata(e4);
        e4.getPackages().add(p1v2);

        Errata e5 = ErrataFactoryTest.createTestPublishedErrata(user.getId());
        childChan.addErrata(e4);
        e4.getPackages().add(p1v4);

        ChannelFactory.save(baseChan);
        ChannelFactory.save(childChan);

        TestUtils.saveAndFlush(e1);
        TestUtils.saveAndFlush(e2);
        TestUtils.saveAndFlush(e3);
        TestUtils.saveAndFlush(e4);
        TestUtils.saveAndFlush(e5);

        Map<Long, Map<String, String>> out = ServerFactory.listNewestPkgsForServerErrata(serverIds, errataIds);
        Map<String, String> packages = out.get(server.getId());
        assertEquals(1, packages.size());
        assertEquals(p1v3.getPackageEvr().toString(), packages.get(p1v3.getPackageName().getName()));
    }

    public void testListErrataNamesForServer() throws Exception {
        Set<Long> serverIds = new HashSet<Long>();
        Set<Long> errataIds = new HashSet<Long>();

        Server server = ServerFactoryTest.createTestServer(user, true);
        serverIds.add(server.getId());
        Channel baseChan = ChannelFactoryTest.createBaseChannel(user);
        baseChan.setUpdateTag("SLE-SERVER");
        server.addChannel(baseChan);

        Errata e = ErrataFactoryTest.createTestPublishedErrata(user.getId());
        errataIds.add(e.getId());
        e.setAdvisoryName("SUSE-2016-1234");
        baseChan.addErrata(e);

        Errata ce = ErrataFactoryTest.createTestPublishedErrata(user.getId());
        errataIds.add(ce.getId());
        ce.setAdvisoryName("CL-SUSE-2016-1234");
        baseChan.addErrata(ce);

        ChannelFactory.save(baseChan);

        TestUtils.saveAndFlush(e);

        Map<Long, Map<Long, Set<ErrataInfo>>> out =
                ServerFactory.listErrataNamesForServers(serverIds, errataIds);
        Set<ErrataInfo> errataName = out.get(server.getId()).get(e.getId());
        assertContains(errataName, new ErrataInfo("SUSE-SLE-SERVER-2016-1234", false, false));

        errataName = out.get(server.getId()).get(ce.getId());
        assertContains(errataName, new ErrataInfo("CL-SUSE-SLE-SERVER-2016-1234", false, false));
    }

    public void testListErrataNamesForServerSLE11() throws Exception {
        Set<Long> serverIds = new HashSet<Long>();
        Set<Long> errataIds = new HashSet<Long>();

        Server server = ServerFactoryTest.createTestServer(user, true);
        serverIds.add(server.getId());
        Channel baseChan = ChannelFactoryTest.createBaseChannel(user);
        baseChan.setUpdateTag("slessp4");
        server.addChannel(baseChan);

        Errata e = ErrataFactoryTest.createTestPublishedErrata(user.getId());
        errataIds.add(e.getId());
        e.setAdvisoryName("ecryptfs-utils-12379");
        baseChan.addErrata(e);

        Errata ce = ErrataFactoryTest.createTestPublishedErrata(user.getId());
        errataIds.add(ce.getId());
        ce.setAdvisoryName("CL-ecryptfs-utils-12379");
        baseChan.addErrata(ce);

        ChannelFactory.save(baseChan);

        TestUtils.saveAndFlush(e);

        Map<Long, Map<Long, Set<ErrataInfo>>> out =
                ServerFactory.listErrataNamesForServers(serverIds, errataIds);
        Set<ErrataInfo> errataName = out.get(server.getId()).get(e.getId());
        assertContains(errataName, new ErrataInfo("slessp4-ecryptfs-utils-12379", false, false));

        errataName = out.get(server.getId()).get(ce.getId());
        assertContains(errataName,
                new ErrataInfo("slessp4-CL-ecryptfs-utils-12379", false, false));
    }

    /**
     * Tests assignment of a server path to a minion.
     * @throws Exception - if anything goes wrong.
     */
    public void testAddRemoveServerPath() throws Exception {
        Server minion = ServerTestUtils.createTestSystem();
        Server proxy = ServerTestUtils.createTestSystem();
        String proxyHostname = "proxyHostname";
        Set<ServerPath> serverPaths = ServerFactory.createServerPaths(minion, proxy, proxyHostname);
        minion.getServerPaths().addAll(serverPaths);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

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
     * Tests looking up of a proxy server, assuming the proxy's simple name is
     * in rhnServer.
     * @throws Exception - if anything goes wrong.
     */
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

    public void testFindServersInSetByChannel() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user, true);
        Channel parent = ChannelFactoryTest.createTestChannel(user);
        parent.setParentChannel(null);

        Channel child = ChannelFactoryTest.createTestChannel(user);
        child.setParentChannel(parent);

        server.addChannel(parent);
        server.addChannel(child);

        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        set.addElement(server.getId() + "");
        RhnSetManager.store(set);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        List<Long> servers = ServerFactory.findServersInSetByChannel(user, server.getBaseChannel().getId());
        assertEquals(1, servers.size());
        assertEquals(server.getId(), servers.stream().findFirst().get());

    }
}
