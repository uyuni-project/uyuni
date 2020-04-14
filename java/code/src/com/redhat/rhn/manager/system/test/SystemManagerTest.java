/**
 * Copyright (c) 2009--2017 Red Hat, Inc.
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

import static com.redhat.rhn.domain.formula.FormulaFactory.PROMETHEUS_EXPORTERS;
import static com.redhat.rhn.manager.action.test.ActionManagerTest.assertNotEmpty;
import static com.redhat.rhn.testing.RhnBaseTestCase.reload;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.Optional.empty;
import static java.util.Optional.of;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.common.validator.ValidatorResult;
import com.redhat.rhn.common.validator.ValidatorWarning;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.test.ServerActionTest;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelArch;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.entitlement.Entitlement;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.formula.FormulaFactory;
import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.test.PackageEvrFactoryTest;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.rhnset.SetCleanup;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.CPU;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.NetworkInterface;
import com.redhat.rhn.domain.server.Note;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerArch;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.test.CPUTest;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerGroupTest;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.test.ActivationKeyTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.dto.ActivationKeyDto;
import com.redhat.rhn.frontend.dto.CustomDataKeyOverview;
import com.redhat.rhn.frontend.dto.EmptySystemProfileOverview;
import com.redhat.rhn.frontend.dto.EssentialServerDto;
import com.redhat.rhn.frontend.dto.SystemOverview;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.errata.cache.ErrataCacheManager;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.redhat.rhn.manager.kickstart.cobbler.test.MockXMLRPCInvoker;
import com.redhat.rhn.manager.rhnpackage.test.PackageManagerTest;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.SystemsExistException;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.manager.user.UserManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.ChannelTestUtils;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerGroupTestUtils;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestStatics;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.webui.controllers.utils.ContactMethodUtil;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.impl.SaltService;

import org.cobbler.test.MockConnection;
import org.hibernate.Session;
import org.hibernate.type.IntegerType;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;


public class SystemManagerTest extends JMockBaseTestCaseWithUser {

    public static final Long NUM_CPUS = 5L;
    public static final int HOST_RAM_MB = 2048;
    public static final int HOST_SWAP_MB = 1024;

    private SaltService saltServiceMock;
    protected Path tmpPillarRoot;
    protected Path tmpSaltRoot;
    protected Path metadataDirOfficial;
    private SystemEntitlementManager systemEntitlementManager;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Config.get().setString(CobblerXMLRPCHelper.class.getName(),
                MockXMLRPCInvoker.class.getName());
        MockConnection.clear();
        setImposteriser(ClassImposteriser.INSTANCE);
        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);
        saltServiceMock = mock(SaltService.class);
        tmpSaltRoot = Files.createTempDirectory("salt");
        FormulaFactory.setDataDir(tmpSaltRoot.toString());
        SystemManager.mockSaltService(saltServiceMock);
        context().checking(new Expectations() {
            {
                allowing(taskomaticMock)
                    .scheduleActionExecution(with(any(Action.class)));
            }
        });
        SaltService saltService = new SaltService();
        systemEntitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(),
                new SystemEntitler(saltService, new VirtManagerSalt(saltService))
        );
    }

    public void testSnapshotServer() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server server = ServerFactoryTest.createTestServer(user, true);
        Long id = server.getId();

        assertTrue(SystemManager.serverHasFeature(id, "ftr_snapshotting"));
        assertEquals(Integer.valueOf(0), numberOfSnapshots(id));
        SystemManager.snapshotServer(server, "Testing snapshots");
        assertEquals(Integer.valueOf(1), numberOfSnapshots(id));
    }

    /*
     * I know this is ugly, but since we haven't got the sever snapshotting feature fully
     * worked out in java yet, just do a sql query to make sure the stored proc worked.
     */
    private Integer numberOfSnapshots(Long sid) {
        Session session = HibernateFactory.getSession();
        return (Integer) session.createSQLQuery("Select count(*) as cnt " +
                                                         "  from rhnSnapshot " +
                                                         " where server_id = " + sid)
                                         .addScalar("cnt", IntegerType.INSTANCE)
                                         .uniqueResult();
    }

    public void testDeleteServer() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server s = ServerFactoryTest.createTestServer(user, true);
        Long id = s.getId();

        Server test = SystemManager.lookupByIdAndUser(id, user);
        assertNotNull(test);

        SystemManager.deleteServer(user, id);

        try {
            test = SystemManager.lookupByIdAndUser(id, user);
            fail("Found deleted server");
        }
        catch (LookupException e) {
            //success
        }
    }

    /**
     * Tests that deleting a server cleans up its formula data and formula assignment.
     *
     * @throws java.lang.Exception if anything goes wrong
     */
    public void testFormulaDataCleanUp() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        String minionId = minion.getMinionId();

        String formulaName = "test-formula";
        File formulaValues = Paths.get(FormulaFactory.getPillarDir(), minionId + "_" + formulaName + ".json").toFile();
        FormulaFactory.saveServerFormulas(minionId, singletonList(formulaName));
        FormulaFactory.saveServerFormulaData(singletonMap("fooKey", "barVal"), minionId, formulaName);

        assertNotEmpty(FormulaFactory.getFormulasByMinionId(minionId));
        assertTrue(FormulaFactory.getFormulaValuesByNameAndMinionId(formulaName, minionId).isPresent());
        // Test the filesystem part:
        assertTrue(formulaValues.exists());

        context().checking(new Expectations() {{
            allowing(saltServiceMock).deleteKey(minionId);
        }});
        SystemManager.deleteServer(user, minion.getId());

        assertTrue(FormulaFactory.getFormulasByMinionId(minionId).isEmpty());
        assertFalse(FormulaFactory.getFormulaValuesByNameAndMinionId(formulaName, minionId).isPresent());
        assertFalse(formulaValues.exists());
    }

    public void testEmptyFormulaDataCleanUp() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        String minionId = minion.getMinionId();

        String formulaName = "test-formula";
        File formulaValues = Paths.get(FormulaFactory.getPillarDir(), minionId + "_" + formulaName + ".json").toFile();
        FormulaFactory.saveServerFormulas(minionId, singletonList(formulaName));
        try (FileChannel outChan = new FileOutputStream(new File(FormulaFactory.getServerDataFile()), true).getChannel()) {
            outChan.truncate(0);
        }

        context().checking(new Expectations() {{
            allowing(saltServiceMock).deleteKey(minionId);
        }});
        SystemManager.deleteServer(user, minion.getId());

        assertTrue(FormulaFactory.getFormulasByMinionId(minionId).isEmpty());
        assertFalse(FormulaFactory.getFormulaValuesByNameAndMinionId(formulaName, minionId).isPresent());
        assertFalse(formulaValues.exists());
        assertEquals("{}", FileUtils.readStringFromFile(FormulaFactory.getServerDataFile()));
    }

    public void testDeleteVirtualServer() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server host = ServerTestUtils.createVirtHostWithGuests(user, 1, systemEntitlementManager);
        Server guest = (host.getGuests().iterator().next()).
            getGuestSystem();
        Long sid = guest.getId();

        Server test = SystemManager.lookupByIdAndUser(sid, user);
        assertNotNull(test);

        SystemManager.deleteServer(user, sid);

        try {
            test = SystemManager.lookupByIdAndUser(sid, user);
            fail("Found deleted server");
        }
        catch (LookupException e) {
            // expected
        }

    }

    public void testDeleteVirtualServerHostDeleted() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server host = ServerTestUtils.createVirtHostWithGuests(user, 1, systemEntitlementManager);
        Server guest = (host.getGuests().iterator().next()).
            getGuestSystem();
        Long sid = guest.getId();

        Server test = SystemManager.lookupByIdAndUser(sid, user);
        assertNotNull(test);

        // Delete the host first:
        SystemManager.deleteServer(user, host.getId());
        TestUtils.flushAndEvict(host);

        SystemManager.deleteServer(user, sid);
        TestUtils.flushAndEvict(guest);

        try {
            test = SystemManager.lookupByIdAndUser(sid, user);
            fail("Found deleted server");
        }
        catch (LookupException e) {
            // expected
        }

    }

    public void testSystemsNotInSg() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        user.addPermanentRole(RoleFactory.ORG_ADMIN);

        // Create a test server so we have one in the list.
        Server s = ServerFactoryTest.createTestServer(user, true);
        ManagedServerGroup sg = ServerGroupTestUtils.createManaged(user);

        DataResult<SystemOverview> systems = SystemManager.
                                          systemsNotInGroup(user, sg, null);
        assertNotNull(systems);
        assertFalse(systems.isEmpty());
        assertTrue(serverInList(s, systems));


        SystemManager.addServerToServerGroup(s, sg);
        systems = SystemManager.systemsNotInGroup(user, sg, null);
        assertFalse(serverInList(s, systems));
    }

    private boolean serverInList(Server s, List<SystemOverview> servers) {
        for (SystemOverview dto : servers) {
            if (dto.getId().equals(s.getId())) {
                return true;
            }
        }
        return false;
    }

    public void testSystemList() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        user.addPermanentRole(RoleFactory.ORG_ADMIN);

        // Create a test server so we have one in the list.
        ServerFactoryTest.createTestServer(user, true);

        DataResult<SystemOverview> systems = SystemManager.systemList(user, null);
        assertNotNull(systems);
        assertFalse(systems.isEmpty());
        assertTrue(systems.size() > 0);
    }

    public void testSystemWithFeature() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        PageControl pc = new PageControl();
        pc.setStart(1);
        pc.setPageSize(20);
        DataResult<SystemOverview> systems = SystemManager.systemsWithFeature(user,
                ServerConstants.FEATURE_KICKSTART, pc);
        int origCount = systems.size();

        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        // Create a test server so we have one in the list.
        Server s = ServerFactoryTest.createTestServer(user, true);
        ServerFactory.save(s);

        systems = SystemManager.systemsWithFeature(user, ServerConstants.FEATURE_KICKSTART,
                pc);
        int newCount = systems.size();
        assertNotNull(systems);

        assertFalse(systems.isEmpty());
        assertTrue(systems.size() > 0);
        assertTrue(newCount > origCount);
        assertTrue(systems.size() <= 20);
    }


    public void testSystemsInGroup() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        user.addPermanentRole(RoleFactory.ORG_ADMIN);

        Server server = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
        ServerGroup group = ServerGroupTestUtils.createManaged(user);
        int origCount = SystemManager.systemsInGroup(group.getId(), null).size();

        group.setOrg(server.getOrg());
        ServerFactory.save(server);
        ServerFactory.addServerToGroup(server, group);

        DataResult<SystemOverview> systems =
                SystemManager.systemsInGroup(group.getId(), null);
        assertNotNull(systems);
        assertFalse(systems.isEmpty());
        assertTrue(systems.size() > origCount);
        boolean found = false;
        Iterator<SystemOverview> i = systems.iterator();
        while (i.hasNext()) {
            SystemOverview so = i.next();
            if (so.getId().longValue() ==
                server.getId().longValue()) {
                found = true;
            }
        }
        assertTrue(found);
    }


    public void testCountActions() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        Server server = ServerFactoryTest.createTestServer(user);

        assertEquals(0, SystemManager.countActions(server.getId()));

        Action action = ActionFactoryTest.createAction(user,
                ActionFactory.TYPE_CONFIGFILES_UPLOAD);
        ServerActionTest.createServerAction(server, action);
        ActionFactory.save(action);

        assertEquals(1, SystemManager.countActions(server.getId()));

        Action action2 = ActionFactoryTest.createAction(user,
                ActionFactory.TYPE_CONFIGFILES_UPLOAD);
        ServerActionTest.createServerAction(server, action2);
        ActionFactory.save(action);

        assertEquals(2, SystemManager.countActions(server.getId()));
    }

    public void testCountPackageActions() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        Server server = ServerFactoryTest.createTestServer(user);

        assertEquals(0, SystemManager.countActions(server.getId()));

        Action action = ActionFactoryTest.createAction(user,
                ActionFactory.TYPE_PACKAGES_DELTA);
        ServerActionTest.createServerAction(server, action);
        ActionFactory.save(action);

        assertEquals(1, SystemManager.countActions(server.getId()));

        Action action2 = ActionFactoryTest.createAction(user,
                ActionFactory.TYPE_PACKAGES_AUTOUPDATE);
        ServerActionTest.createServerAction(server, action2);
        ActionFactory.save(action);

        assertEquals(2, SystemManager.countActions(server.getId()));

    }

    public void testUnscheduledErrata() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server server = ServerFactoryTest.createTestServer(user, true);
        PageControl pc = new PageControl();
        pc.setStart(1);
        pc.setPageSize(20);

        DataResult<Errata> errata =
                SystemManager.unscheduledErrata(user, server.getId(), pc);
        assertNotNull(errata);
        assertTrue(errata.isEmpty());
        assertTrue(errata.size() == 0);
        assertFalse(SystemManager.hasUnscheduledErrata(user, server.getId()));

        Errata e = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        for (Iterator<Package> itr = e.getPackages().iterator(); itr.hasNext();) {
            Package pkg = itr.next();
            ErrataCacheManager.insertNeededErrataCache(server.getId(),
                    e.getId(), pkg.getId());
        }

        errata = SystemManager.unscheduledErrata(user, server.getId(), pc);
        assertNotNull(errata);
        assertFalse(errata.isEmpty());
        assertTrue(errata.size() == 1);
        assertTrue(SystemManager.hasUnscheduledErrata(user, server.getId()));
    }


    public void testGetServerEntitlement() throws Exception {
        // create a new server
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        Server server = ServerFactoryTest.createTestServer(user);
        List<Entitlement> entitlements =
                SystemManager.getServerEntitlements(server.getId());
        assertFalse(entitlements.isEmpty());
        assertTrue(entitlements.contains(EntitlementManager.MANAGEMENT));
    }

    public void testClientCapability() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        Server server = ServerFactoryTest.createTestServer(user);
        Long ver = 1L;
        giveCapability(server.getId(), SystemManager.CAP_PACKAGES_VERIFY, ver);
        assertTrue(SystemManager.clientCapable(server.getId(),
                SystemManager.CAP_PACKAGES_VERIFY));
    }


    /**
     * This utility method associates a particular system with a given
     * capability.  This is backend code that has not yet been implemented
     * in Java. This type of code should NEVER EVER be seen outside of a test.
     * @param sid Server id
     * @param capability Capability to add
     * @param version version number
     * @throws SQLException thrown if there's a problem which should cause
     * the test to fail.
     */
    public static void giveCapability(Long sid, String capability, Long version)
        throws SQLException {

        WriteMode m = ModeFactory.getWriteMode("test_queries",
                                                    "add_to_client_capabilities");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("sid", sid);
        params.put("capability", capability);
        params.put("version", version);
        m.executeUpdate(params);
    }

    public void testCompatibleWithServer() throws Exception {

        /*
         * here we create a user as an org admin.
         * then we create two (minimum) Servers owned by the user and
         * which are enterprise_entitled.
         * We add the test channel to each of the servers.  This allows
         * us to test the compatibleWithServer method.
         */
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server srvr = ServerFactoryTest.createTestServer(user, true,
                ServerFactory.lookupServerGroupTypeByLabel("enterprise_entitled"));

        Server srvr1 = ServerFactoryTest.createTestServer(user, true,
                ServerFactory.lookupServerGroupTypeByLabel("enterprise_entitled"));
        Channel channel = ChannelFactoryTest.createTestChannel(user);
        srvr.addChannel(channel);
        srvr1.addChannel(channel);
        TestUtils.saveAndFlush(srvr);
        TestUtils.saveAndFlush(srvr1);
        UserManager.storeUser(user);


        // Ok let's finally test what we came here for.
        List<Map<String, Object>> list = SystemManager.compatibleWithServer(user, srvr);
        assertNotNull("List is null", list);
        assertFalse("List is empty", list.isEmpty());
        boolean found = false;
        for (Iterator<Map<String, Object>> itr = list.iterator(); itr.hasNext();) {
            Map<String, Object> o = itr.next();
            if (srvr1.getName().equals(o.get("name"))) {
                found = true;
            }
        }
        assertTrue("Didn't get back the expected values", found);

    }

    public void testGetSsmSystemsSubscribedToChannel() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        UserFactory.save(user);

        Server s = ServerTestUtils.createTestSystem(user);

        RhnSetDecl.SYSTEMS.clear(user);
        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        set.addElement(s.getId());
        RhnSetManager.store(set);

        List<Map<String, Object>> systems =
                SystemManager.getSsmSystemsSubscribedToChannel(user,
                s.getBaseChannel().getId());
        assertEquals(1, systems.size());
        Map<String, Object> result1 = systems.get(0);
        assertEquals(s.getName(), result1.get("name"));
        assertEquals(s.getId(), result1.get("id"));
    }

    public void testNoBaseChannelInSet() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        UserFactory.save(user);

        // Get ourselves a system
        Server s = ServerTestUtils.createTestSystem(user);
        SystemManager.unsubscribeServerFromChannel(user, s, s.getBaseChannel());

        // insert sys into system-set
        RhnSetDecl.SYSTEMS.clear(user);
        RhnSet set = RhnSetDecl.SYSTEMS.get(user);
        set.addElement(s.getId());
        RhnSetManager.store(set);

        // ask for the base channels of all systems in the system-set for the test user
        DataResult<EssentialServerDto> dr =
                SystemManager.systemsWithoutBaseChannelsInSet(user);
        assertNotNull(dr);
        assertEquals(dr.size(), 1);
        EssentialServerDto m = dr.get(0);
        Long id = m.getId();
        assertTrue(s.getId().equals(id));

        // Create a new no-base-channel-server
        Server s2 = ServerTestUtils.createTestSystem(user);
        SystemManager.unsubscribeServerFromChannel(user, s2, s2.getBaseChannel());

        // We should NOT see it yet
        dr = SystemManager.systemsWithoutBaseChannelsInSet(user);
        assertNotNull(dr);
        assertEquals(dr.size(), 1);

        // Add it to the SSM set and look again
        set.addElement(s2.getId());
        RhnSetManager.store(set);
        dr = SystemManager.systemsWithoutBaseChannelsInSet(user);
        assertNotNull(dr);
        assertEquals(dr.size(), 2);
    }

    public void testRegisteredList() throws Exception {
        User user = UserTestUtils.findNewUser(TestStatics.TESTUSER, TestStatics.TESTORG);
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server server = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
        ServerGroup group = ServerGroupTest
                .createTestServerGroup(user.getOrg(), null);
        SystemManager.addServerToServerGroup(server, group);
        ServerFactory.save(server);

        DataResult<SystemOverview> dr = SystemManager.registeredList(user, null, 0);
        assertNotEmpty(dr);
    }

    public void testDeactivateProxy() throws Exception {
        User user = UserTestUtils.findNewUser(TestStatics.TESTUSER, TestStatics.TESTORG);
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server server = ServerFactoryTest.createTestProxyServer(user, true);
        assertTrue(server.isProxy());
        server = SystemManager.deactivateProxy(server);
        ServerFactory.save(server);
        server = reload(server);
        assertFalse(server.isProxy());
    }

    private void addCpuToServer(Server s) {
        CPU cpu = new CPU();
        cpu.setArch(ServerFactory.lookupCPUArchByName(CPUTest.ARCH_NAME));
        cpu.setServer(s);
        cpu.setFamily(CPUTest.FAMILY);
        cpu.setMHz(CPUTest.MHZ);
        cpu.setModel(CPUTest.MODEL);
        cpu.setNrCPU(NUM_CPUS);
        TestUtils.saveAndFlush(cpu);
        TestUtils.reload(s);
    }

    public void testVcpuSettingExceeds32() throws Exception {
        Server host = setupHostWithGuests(1);
        VirtualInstance vi = host.getGuests().iterator().next();

        // Currently 32 is the maximum supported number of vcpus on both 32 and 64-bit
        // systems:
        ValidatorResult result = SystemManager.validateVcpuSetting(vi.getId(), 33);
        List<ValidatorError> errors = result.getErrors();
        assertEquals(1, errors.size());
        assertEquals("systems.details.virt.vcpu.limit.msg", errors.get(0).getKey());
    }

    public void testVcpuSettingExceedsPhysicalCpus() throws Exception {
        Server host = setupHostWithGuests(1);
        VirtualInstance vi = host.getGuests().iterator().next();

        // Warning should result from attempting to set vcpus greater than the
        // physical hosts cpus:
        ValidatorResult result = SystemManager.validateVcpuSetting(vi.getId(), 6);
        assertEquals(0, result.getErrors().size());

        List<ValidatorWarning> warnings = result.getWarnings();
        assertEquals(2, warnings.size());
        assertEquals("systems.details.virt.vcpu.exceeds.host.cpus", warnings.get(0)
                .getKey());
    }

    // Increasing the vCPUs should create a warning that if the new setting exceeds
    // what the guest was booted with, it will require a reboot to take effect.
    public void testVcpuIncreaseWarning() throws Exception {
        Server host = setupHostWithGuests(1);
        VirtualInstance vi = host.getGuests().iterator().next();

        ValidatorResult result = SystemManager.validateVcpuSetting(vi.getId(), 3);
        assertEquals(0, result.getErrors().size());

        List<ValidatorWarning> warnings = result.getWarnings();
        assertEquals(1, warnings.size());
        assertEquals("systems.details.virt.vcpu.increase.warning",
                warnings.get(0).getKey());
    }

    public void testMemoryChangeWarnings() throws Exception {
        Server host = setupHostWithGuests(1);

        List<Long> guestIds = new LinkedList<Long>();
        VirtualInstance vi = host.getGuests().iterator().next();
        guestIds.add(vi.getId());

        ValidatorResult result = SystemManager.validateGuestMemorySetting(guestIds,
            512);
        List<ValidatorError> errors = result.getErrors();
        assertEquals(0, errors.size());
        List<ValidatorWarning> warnings = result.getWarnings();
        assertEquals(2, warnings.size());
    }

    private Server setupHostWithGuests(int numGuests) throws Exception {
        Server host = ServerTestUtils.createVirtHostWithGuests(numGuests, systemEntitlementManager);
        host.setRam(HOST_RAM_MB);
        addCpuToServer(host);
        User user = host.getCreator();
        UserTestUtils.addVirtualization(user.getOrg());

        for (Iterator<VirtualInstance> it = host.getGuests().iterator(); it.hasNext();) {
            VirtualInstance vi = it.next();
            Server guest = vi.getGuestSystem();
            guest.addChannel(ChannelTestUtils.createBaseChannel(user));
            ServerTestUtils.addVirtualization(user, guest);
        }
        return host;
    }

    public void testListCustomKeys() throws Exception {
        User admin = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        admin.addPermanentRole(RoleFactory.ORG_ADMIN);


        CustomDataKey key = new CustomDataKey();
        key.setCreator(admin);
        key.setLabel("testdsfd");
        key.setDescription("test desc");
        key.setOrg(admin.getOrg());
        key.setLastModifier(admin);
        HibernateFactory.getSession().save(key);


        List<CustomDataKeyOverview> list = SystemManager.listDataKeys(admin);
        assertTrue(1 == list.size());
        CustomDataKeyOverview dataKey = list.get(0);
        assertEquals(key.getLabel(), dataKey.getLabel());
    }

    /**
     * Note: This test tests multiple calls in SystemManager.
     *
     * @throws Exception something bad happened
     */
    public void testErrataCountsForSystem() throws Exception {

        // Setup
        User admin = UserTestUtils.findNewUser("errataUser1", "errataOrg1");
        Org org = admin.getOrg();

        Server server = ServerTestUtils.createTestSystem(admin);
        ServerFactory.save(server);
        TestUtils.flushAndEvict(server);

        // Will be used for both errata types. Represents an upgraded version of a package
        // that comes with the errata.
        PackageEvr upgradedPackageEvr =
            PackageEvrFactory.lookupOrCreatePackageEvr("1", "1.0.0", "2");
        upgradedPackageEvr =
            TestUtils.saveAndReload(upgradedPackageEvr);

        ServerTestUtils.populateServerErrataPackages(org, server,
            upgradedPackageEvr, ErrataFactory.ERRATA_TYPE_SECURITY);
        ServerTestUtils.populateServerErrataPackages(org, server,
            upgradedPackageEvr, ErrataFactory.ERRATA_TYPE_BUG);

        // Test
        int criticalCount =
            SystemManager.countCriticalErrataForSystem(admin, server.getId());
        int nonCriticalCount =
            SystemManager.countNoncriticalErrataForSystem(admin, server.getId());

        // Verify
        assertEquals(1, criticalCount);
        assertEquals(1, nonCriticalCount);
    }

    public void testSsmSystemPackagesToRemove() throws Exception {

        // Setup
        User admin = UserTestUtils.findNewUser("ssmUser1", "ssmOrg1");
        Org org = admin.getOrg();

        //    Create Test Servers
        Server server1 = ServerTestUtils.createTestSystem(admin);
        ServerFactory.save(server1);

        Server server2 = ServerTestUtils.createTestSystem(admin);
        ServerFactory.save(server2);

        //    Create Test Packages
        Package installedPackage1 = PackageTest.createTestPackage(org);
        Package installedPackage2 = PackageTest.createTestPackage(org);

        //    Associate the servers and packages
        PackageManagerTest.associateSystemToPackageWithArch(server1, installedPackage1);
        PackageManagerTest.associateSystemToPackageWithArch(server1, installedPackage2);

        PackageManagerTest.associateSystemToPackageWithArch(server2, installedPackage1);

        //    Add the servers to the SSM set
        RhnSet ssmSet = RhnSetManager.findByLabel(admin.getId(),
            RhnSetDecl.SYSTEMS.getLabel(), SetCleanup.NOOP);
        if (ssmSet == null) {
            ssmSet = RhnSetManager.createSet(admin.getId(),
                RhnSetDecl.SYSTEMS.getLabel(), SetCleanup.NOOP);
        }

        assert ssmSet != null;

        ssmSet.addElement(server1.getId());
        ssmSet.addElement(server2.getId());
        RhnSetManager.store(ssmSet);

        ssmSet = RhnSetManager.findByLabel(admin.getId(),
            RhnSetDecl.SYSTEMS.getLabel(), SetCleanup.NOOP);
        assert ssmSet != null;

        //    Add the servers to the SSM set
        ServerTestUtils.addServersToSsm(admin, server1.getId(), server2.getId());

        //    Simulate the user selecting every package in the list
        RhnSet packagesSet =
            RhnSetManager.createSet(admin.getId(),
                RhnSetDecl.SSM_REMOVE_PACKAGES_LIST.getLabel(), SetCleanup.NOOP);

        packagesSet.addElement(installedPackage1.getPackageName().getId(),
            installedPackage1.getPackageEvr().getId(),
            installedPackage1.getPackageArch().getId());

        packagesSet.addElement(installedPackage2.getPackageName().getId(),
            installedPackage2.getPackageEvr().getId(),
            installedPackage2.getPackageArch().getId());

        RhnSetManager.store(packagesSet);

        packagesSet = RhnSetManager.findByLabel(admin.getId(),
            RhnSetDecl.SSM_REMOVE_PACKAGES_LIST.getLabel(), SetCleanup.NOOP);
        assert packagesSet != null;

        assertNotNull(packagesSet);

        // Test
        DataResult<Map<String, Object>> result =
            SystemManager.ssmSystemPackagesToRemove(admin, packagesSet.getLabel(), false);
        assertNotNull(result);

        //   Need explicit elaborate call here; list tag will do this in the UI
        result.elaborate();

        // Verify
        assertEquals(2, result.size());

        for (Map<String, Object> map : result) {

            if (map.get("id").equals(server1.getId())) {
                assertEquals(server1.getName(), map.get("system_name"));

                assertTrue(map.get("elaborator0") instanceof List);
                List result1Packages = (List)map.get("elaborator0");
                assertEquals(2, result1Packages.size());
            }
            else if (map.get("id").equals(server2.getId())) {
                assertEquals(server2.getName(), (map.get("system_name")));

                assertTrue(map.get("elaborator0") instanceof List);
                List result2Packages = (List)map.get("elaborator0");
                assertEquals(1, result2Packages.size());
            }
            else {
                fail("Found ID that wasn't expected: " + map.get("id"));
            }
        }
    }

    public void testDeleteNote() throws Exception {
        // Setup
        User admin = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        Server server = ServerTestUtils.createTestSystem(admin);
        int sizeBefore = server.getNotes().size();
        server.addNote(admin, "Test Subject", "Test Body");
        ServerFactory.save(server);
        TestUtils.flushAndEvict(server);

        server = ServerFactory.lookupById(server.getId());
        int sizeAfter = server.getNotes().size();
        assertTrue(sizeAfter == (sizeBefore + 1));

        Note deleteMe = server.getNotes().iterator().next();

        // Test
        SystemManager.deleteNote(admin, server.getId(), deleteMe.getId());

        // Verify
        server = ServerFactory.lookupById(server.getId());
        int sizeAfterDelete = server.getNotes().size();
        assertEquals(sizeBefore, sizeAfterDelete);
    }

    public void testDeleteNotes() throws Exception {
        // Setup
        User admin = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        Server server = ServerTestUtils.createTestSystem(admin);
        int sizeBefore = server.getNotes().size();
        server.addNote(admin, "Test Subject 1", "Test Body");
        server.addNote(admin, "Test Subject 2", "Test Body");
        server.addNote(admin, "Test Subject 3", "Test Body");
        server.addNote(admin, "Test Subject 4", "Test Body");
        ServerFactory.save(server);
        TestUtils.flushAndEvict(server);

        server = ServerFactory.lookupById(server.getId());
        int sizeAfter = server.getNotes().size();
        assertTrue(sizeAfter == (sizeBefore + 4));

        // Test
        SystemManager.deleteNotes(admin, server.getId());

        // Verify
        server = ServerFactory.lookupById(server.getId());
        int sizeAfterDelete = server.getNotes().size();
        assertEquals(0, sizeAfterDelete);

    }


    public void testHasPackageAvailable() throws Exception {
        User admin = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        Server server = ServerTestUtils.createTestSystem(admin);

        Package pack = PackageTest.createTestPackage(admin.getOrg());
        assertFalse(SystemManager.hasPackageAvailable(server,
                pack.getPackageName().getId(), pack.getPackageArch().getId(),
                pack.getPackageEvr().getId()));

        assertFalse(SystemManager.hasPackageAvailable(server,
                pack.getPackageName().getId(), null,
                pack.getPackageEvr().getId()));

        server.getBaseChannel().addPackage(pack);
        TestUtils.saveAndFlush(pack);
        assertTrue(SystemManager.hasPackageAvailable(server,
                pack.getPackageName().getId(), pack.getPackageArch().getId(),
                pack.getPackageEvr().getId()));
        assertTrue(SystemManager.hasPackageAvailable(server,
                pack.getPackageName().getId(), null,
                pack.getPackageEvr().getId()));

    }
    public void testListSystemsWithNeededPackage() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server server = ServerFactoryTest.createTestServer(user, true);
        PageControl pc = new PageControl();
        pc.setStart(1);
        pc.setPageSize(20);

        DataResult<Errata> errata =
                SystemManager.unscheduledErrata(user, server.getId(), pc);
        assertNotNull(errata);
        assertTrue(errata.isEmpty());
        assertTrue(errata.size() == 0);
        assertFalse(SystemManager.hasUnscheduledErrata(user, server.getId()));

        Errata e = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        for (Iterator<Package> itr = e.getPackages().iterator(); itr.hasNext();) {
            Package pkg = itr.next();
            ErrataCacheManager.insertNeededErrataCache(server.getId(),
                    e.getId(), pkg.getId());
            List<SystemOverview> systems =
                SystemManager.listSystemsWithNeededPackage(user, pkg.getId());
            assertTrue(systems.size() == 1);
            SystemOverview so = systems.get(0);
            assertEquals(so.getId(), server.getId());
        }

        errata = SystemManager.unscheduledErrata(user, server.getId(), pc);
        assertNotNull(errata);
        assertFalse(errata.isEmpty());
        assertTrue(errata.size() == 1);
        assertTrue(SystemManager.hasUnscheduledErrata(user, server.getId()));
    }

    public void testListInstalledPackage() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        Server s = ServerFactoryTest.createTestServer(user);

        List<Map<String, Long>> list = SystemManager.listInstalledPackage("kernel", s);
        assertTrue(list.isEmpty());

        InstalledPackage p = new InstalledPackage();
        p.setArch(PackageFactory.lookupPackageArchByLabel("x86_64"));
        p.setName(PackageFactory.lookupOrCreatePackageByName("kernel"));
        p.setEvr(PackageEvrFactoryTest.createTestPackageEvr());
        p.setServer(s);
        Set<InstalledPackage> set = s.getPackages();
        set.add(p);

        ServerFactory.save(s);

        list = SystemManager.listInstalledPackage("kernel", s);
        assertTrue(list.size() == 1);
        assertEquals(list.get(0).get("name_id"), p.getName().getId());
        assertEquals(list.get(0).get("evr_id"), p.getEvr().getId());

    }

    public void testInSet() throws Exception {
        User usr = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        RhnSet newrs = RhnSetManager.createSet(usr.getId(), "test_systems_list",
                SetCleanup.NOOP);

        for (int i = 0; i < 5; i++) {
            Server mySystem = ServerFactoryTest.createTestServer(usr, true);
            newrs.addElement(mySystem.getId());
        }

        RhnSetManager.store(newrs);

        List<SystemOverview> dr = SystemManager.inSet(usr, newrs.getLabel());
        assertEquals(5, dr.size());
        assertTrue(dr.iterator().hasNext());

        SystemOverview m = (dr.iterator().next());
        assertNotNull(m.getName());
    }

    public void testFindByName() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());
        Server s = ServerFactoryTest.createTestServer(user, true);
        List<SystemOverview> list = SystemManager.listSystemsByName(user, s.getName());
        assertTrue(list.size() == 1);
        assertEquals(list.get(0).getId(), s.getId());

    }

    public void testListDuplicatesByHostname() throws Exception {
        User user = UserTestUtils.findNewUser("testUser",
                "testOrg" + this.getClass().getSimpleName());

        String[] hostnames = {"DUPHOST", "notADup", "duphost"};
        for (String name : hostnames) {
            Server s1 = ServerFactoryTest.createTestServer(user, true);
            s1.setHostname(name);
        }

        List<SystemOverview> list = SystemManager.listDuplicatesByHostname(user, "duphost");
        assertTrue(list.size() == 2);

        DataResult<SystemOverview> dr = SystemManager.systemList(user, null);
        assertTrue(dr.size() == 3);

    }

    public void testFindCompatibleChannelArch() {
        // x86_64
        ServerArch serverArch = ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux");
        ChannelArch channelArch = ServerFactory.findCompatibleChannelArch(serverArch);
        assertEquals("channel-x86_64", channelArch.getLabel());
        assertEquals("x86_64", channelArch.getName());
        serverArch = ServerFactory.lookupServerArchByLabel("ia32e-redhat-linux");
        channelArch = ServerFactory.findCompatibleChannelArch(serverArch);
        assertEquals("channel-x86_64", channelArch.getLabel());
        assertEquals("x86_64", channelArch.getName());

        // IA-32
        serverArch = ServerFactory.lookupServerArchByLabel("i386-redhat-linux");
        channelArch = ServerFactory.findCompatibleChannelArch(serverArch);
        assertEquals("channel-ia32", channelArch.getLabel());
        assertEquals("IA-32", channelArch.getName());
        serverArch = ServerFactory.lookupServerArchByLabel("i686-redhat-linux");
        channelArch = ServerFactory.findCompatibleChannelArch(serverArch);
        assertEquals("channel-ia32", channelArch.getLabel());
        assertEquals("IA-32", channelArch.getName());

        // s390
        serverArch = ServerFactory.lookupServerArchByLabel("s390-redhat-linux");
        channelArch = ServerFactory.findCompatibleChannelArch(serverArch);
        assertEquals("channel-s390", channelArch.getLabel());
        assertEquals("s390", channelArch.getName());
    }

    public void testGetActivationKeys() throws Exception {
        User user = UserTestUtils.findNewUser("testUser", "testOrg" +
            this.getClass().getSimpleName());

        ActivationKey activationKey = ActivationKeyTest.createTestActivationKey(user);
        Server server = activationKey.getServer();
        activationKey.getToken().getActivatedServers().add(server);
        DataResult<ActivationKeyDto> result = SystemManager.getActivationKeys(server);
        assertEquals(1, result.size());
    }

    public void testGetBootstrapActivationKeys() throws Exception {
        User user = UserTestUtils.findNewUser("testUser", "testOrg" +
            this.getClass().getSimpleName());

        ActivationKey activationKey = ActivationKeyTest.createTestActivationKey(user);
        activationKey.setBootstrap("Y");
        Server server = activationKey.getServer();
        activationKey.getToken().getActivatedServers().add(server);
        DataResult<ActivationKeyDto> result = SystemManager.getActivationKeys(server);
        assertEquals(0, result.size());
    }

    public void testCountSystemsInSetWithoutEntitlement() throws Exception {
        User user = UserTestUtils.findNewUser("testUser", "testOrg" +
            this.getClass().getSimpleName());

        String setLabel = TestUtils.randomString();
        List<String> entitlements = new ArrayList<String>();
        entitlements.add(EntitlementManager.ENTERPRISE_ENTITLED);
        int actual = SystemManager.countSystemsInSetWithoutEntitlement(user, setLabel,
                entitlements);
        assertEquals(0, actual);

        Server server = ServerFactoryTest.createTestServer(user, true,
            ServerConstants.getServerGroupTypeEnterpriseEntitled());

        RhnSet set = RhnSetManager.createSet(user.getId(), setLabel, SetCleanup.NOOP);
        set.addElement(server.getId());
        RhnSetManager.store(set);

        actual = SystemManager.countSystemsInSetWithoutEntitlement(user, setLabel,
                entitlements);
        assertEquals(0, actual);

        Server unentitledServer = ServerFactoryTest.createUnentitledTestServer(user, true,
                        ServerFactoryTest.TYPE_SERVER_NORMAL, new Date());
        set.addElement(unentitledServer.getId());
        RhnSetManager.store(set);

        actual = SystemManager.countSystemsInSetWithoutEntitlement(user, setLabel,
                entitlements);
        assertEquals(1, actual);

        actual = SystemManager.countSystemsInSetWithoutEntitlement(user, "non matching",
                entitlements);
        assertEquals(0, actual);
    }

    public void testCountSystemsInSetWithoutFeature() throws Exception {
        User user = UserTestUtils.findNewUser("testUser", "testOrg" +
            this.getClass().getSimpleName());

        String setLabel = TestUtils.randomString();
        List<String> entitlements = new ArrayList<String>();
        entitlements.add(EntitlementManager.ENTERPRISE_ENTITLED);
        int actual = SystemManager.countSystemsInSetWithoutEntitlement(user, setLabel,
                entitlements);
        assertEquals(0, actual);

        Server server = ServerFactoryTest.createTestServer(user, true,
            ServerConstants.getServerGroupTypeEnterpriseEntitled());

        RhnSet set = RhnSetManager.createSet(user.getId(), setLabel, SetCleanup.NOOP);
        set.addElement(server.getId());
        RhnSetManager.store(set);

        actual = SystemManager.countSystemsInSetWithoutFeature(user, setLabel,
                        "ftr_kickstart");
        assertEquals(0, actual);

        Server unentitledServer = ServerFactoryTest.createUnentitledTestServer(user, true,
                        ServerFactoryTest.TYPE_SERVER_NORMAL, new Date());
        set.addElement(unentitledServer.getId());
        RhnSetManager.store(set);

        actual = SystemManager.countSystemsInSetWithoutFeature(user, setLabel,
                        "ftr_kickstart");
        assertEquals(1, actual);

        actual = SystemManager.countSystemsInSetWithoutFeature(user, "non matching",
                        "ftr_kickstart");
        assertEquals(0, actual);
    }

    public void testUpdateServerChannels() throws Exception {
        User user = UserTestUtils.findNewUser("testUser", "testOrg" +
                this.getClass().getSimpleName());
        Server server = ServerFactoryTest.createTestServer(user, true);

        Channel base1 = ChannelFactoryTest.createBaseChannel(user);
        Channel ch1_1 = ChannelFactoryTest.createTestChannel(user.getOrg());

        ch1_1.setParentChannel(base1);

        server.addChannel(base1);
        server.addChannel(ch1_1);

        Channel base2 = ChannelFactoryTest.createBaseChannel(user);
        Channel ch2_1 = ChannelFactoryTest.createTestChannel(user.getOrg());
        Channel ch2_2 = ChannelFactoryTest.createTestChannel(user.getOrg());
        ch2_1.setParentChannel(base2);
        ch2_2.setParentChannel(base2);

        HibernateFactory.getSession().flush();

        SystemManager.updateServerChannels(user, server, of(base2), Arrays.asList(ch2_1, ch2_2), null);

        assertEquals(base2.getId(), server.getBaseChannel().getId());
        assertEquals(2, server.getChildChannels().size());
        assertTrue(server.getChildChannels().stream().anyMatch(cc -> cc.getId().equals(ch2_1.getId())));
        assertTrue(server.getChildChannels().stream().anyMatch(cc -> cc.getId().equals(ch2_2.getId())));
    }

    public void testUpdateServerChannelsNoChildren() throws Exception {
        User user = UserTestUtils.findNewUser("testUser", "testOrg" +
                this.getClass().getSimpleName());
        Server server = ServerFactoryTest.createTestServer(user, true);

        Channel base1 = ChannelFactoryTest.createBaseChannel(user);
        Channel ch1_1 = ChannelFactoryTest.createTestChannel(user.getOrg());

        ch1_1.setParentChannel(base1);

        server.addChannel(base1);
        server.addChannel(ch1_1);

        Channel base2 = ChannelFactoryTest.createBaseChannel(user);
        Channel ch2_1 = ChannelFactoryTest.createTestChannel(user.getOrg());
        Channel ch2_2 = ChannelFactoryTest.createTestChannel(user.getOrg());
        ch2_1.setParentChannel(base2);
        ch2_2.setParentChannel(base2);

        HibernateFactory.getSession().flush();

        SystemManager.updateServerChannels(user, server, of(base2), Collections.emptyList(), null);

        assertEquals(base2.getId(), server.getBaseChannel().getId());
        assertEquals(0, server.getChildChannels().size());
    }

    public void testUpdateServerChannelsNoBase() throws Exception {
        User user = UserTestUtils.findNewUser("testUser", "testOrg" +
                this.getClass().getSimpleName());
        Server server = ServerFactoryTest.createTestServer(user, true);

        Channel base1 = ChannelFactoryTest.createBaseChannel(user);
        Channel ch1_1 = ChannelFactoryTest.createTestChannel(user.getOrg());

        ch1_1.setParentChannel(base1);

        server.addChannel(base1);
        server.addChannel(ch1_1);

        Channel base2 = ChannelFactoryTest.createBaseChannel(user);
        Channel ch2_1 = ChannelFactoryTest.createTestChannel(user.getOrg());
        Channel ch2_2 = ChannelFactoryTest.createTestChannel(user.getOrg());
        ch2_1.setParentChannel(base2);
        ch2_2.setParentChannel(base2);

        HibernateFactory.getSession().flush();

        SystemManager.updateServerChannels(user, server, empty(), Arrays.asList(ch2_1, ch2_2), null);

        assertNull(server.getBaseChannel());
        assertEquals(0, server.getChildChannels().size());
    }

    /**
     * Tests creating an empty system profile.
     */
    public void testCreateSystemProfile() {
        String hwAddr = "be:b0:bc:a3:a7:ad";
        Map<String, Object> data = singletonMap("hwAddress", hwAddr);
        MinionServer minion = SystemManager.createSystemProfile(user, "test system", data);
        Server minionFromDb = SystemManager.lookupByIdAndOrg(minion.getId(), user.getOrg());

        // flush & refresh iface because generated="insert"
        // on interfaceId does not seem to work
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().refresh(minionFromDb);

        assertEquals("test system", minionFromDb.getName());
        assertEquals("_" + hwAddr, minion.getMinionId());
        assertEquals("_" + hwAddr, minion.getMachineId());
        assertEquals("_" + hwAddr, minion.getDigitalServerId());
        assertEquals("(unknown)", minion.getOs());
        assertEquals("(unknown)", minion.getOsFamily());
        assertEquals("(unknown)", minion.getRelease());
        assertEquals(ContactMethodUtil.DEFAULT, minion.getContactMethod().getLabel());
        assertEquals("N", minion.getAutoUpdate());
        assertEquals("x86_64-redhat-linux", minion.getServerArch().getLabel());
        assertEquals(1, minionFromDb.getNetworkInterfaces().size());

        NetworkInterface networkInterface = minionFromDb.getNetworkInterfaces().iterator().next();
        assertEquals("unknown", networkInterface.getName());
        assertEquals(hwAddr, networkInterface.getHwaddr());
        assertTrue(minion.hasEntitlement(EntitlementManager.BOOTSTRAP));
    }

    /**
     * Tests listing empty system profile.
     *
     * @throws java.lang.Exception if anything goes wrong
     */
    public void testListSystemProfile() throws Exception {
        UserTestUtils.addUserRole(user, RoleFactory.ORG_ADMIN);
        String hwAddr = "be:b0:bc:a3:a7:ad";
        MinionServer emptyProfileMinion = SystemManager.createSystemProfile(user, "test system",
                singletonMap("hwAddress", hwAddr));
        HibernateFactory.getSession().evict(emptyProfileMinion);

        ServerTestUtils.createTestSystem(user);
        MinionServerFactoryTest.createTestMinionServer(user);

        DataResult<EmptySystemProfileOverview> emptyProfiles = SystemManager.listEmptySystemProfiles(user, null);
        DataResult<SystemOverview> systemOverviews = SystemManager.systemList(user, null);

        assertEquals(1, emptyProfiles.getTotalSize());
        EmptySystemProfileOverview emptyProfile = emptyProfiles.iterator().next();
        assertEquals(emptyProfileMinion.getId(), emptyProfile.getId());

        List<String> macs = emptyProfile.getMacs();
        assertEquals(1, macs.size());
        assertEquals(hwAddr, macs.iterator().next());

        assertEquals(3, systemOverviews.getTotalSize());
    }

    /**
     * Tests that listing empty system profile doesn't contain traditional system.
     */
    public void testListSystemProfileTradSystem() {
        UserTestUtils.addUserRole(user, RoleFactory.ORG_ADMIN);
        String hwAddr = "be:b0:bc:a3:a7:ad";
        MinionServer emptyProfileMinion = SystemManager.createSystemProfile(user, "test system",
                singletonMap("hwAddress", hwAddr));
        HibernateFactory.getSession().createNativeQuery("DELETE FROM suseMinionInfo").executeUpdate();
        HibernateFactory.getSession().evict(emptyProfileMinion);

        DataResult<EmptySystemProfileOverview> emptyProfiles = SystemManager.listEmptySystemProfiles(user, null);
        assertTrue(emptyProfiles.isEmpty());
    }

    /**
     * Tests listing empty system profile for users from various organizations.
     */
    public void testListSystemProfileCrossOrg() {
        User foreignUser = UserTestUtils.findNewUser("testUser", "anotherTestOrg" + this.getClass().getSimpleName());
        UserTestUtils.addUserRole(foreignUser, RoleFactory.ORG_ADMIN);
        UserTestUtils.addUserRole(user, RoleFactory.ORG_ADMIN);
        String hwAddr = "be:b0:bc:a3:a7:ad";
        SystemManager.createSystemProfile(user, "test system", singletonMap("hwAddress", hwAddr));

        assertEquals(1, SystemManager.listEmptySystemProfiles(user, null).getTotalSize());
        assertEquals(0, SystemManager.listEmptySystemProfiles(foreignUser, null).getTotalSize());
    }

    /**
     * Tests creating an empty system profile when there is already another profile with a NIC
     * that has given HW address.
     */
    public void testCreateSystemProfileExistingHwAddress() {
        String hwAddr = "be:b0:bc:a3:a7:ad";
        Map<String, Object> data = singletonMap("hwAddress", hwAddr);
        MinionServer profile = SystemManager.createSystemProfile(user, "test system", data);
        try {
            SystemManager.createSystemProfile(user, "test system 2", data);
            fail("System creation should have failed!");
        } catch (SystemsExistException e) {
            assertEquals(singletonList(profile.getId()), e.getSystemIds());
        }
    }

    /**
     * Tests finding an empty profile by hostname and HW addresses with "empty" arguments.
     */
    public void testFindByHostnameAndHwAddrsEmptyArgs() {
        assertTrue(SystemManager.findMatchingEmptyProfiles(empty(), emptySet()).isEmpty());
    }

    /**
     * Tests finding an empty profile with no NICS.
     */
    public void testFindByHostnameNoHwAddrs() throws Exception {
        MinionServer minion = createEmptyProfile(of("myhost"), empty());

        List<MinionServer> fromDb = SystemManager.findMatchingEmptyProfiles(of("myhost"), emptySet());
        assertEquals(1, fromDb.size());
        assertEquals(minion, fromDb.get(0));

        // minion with a HW address will also match
        List<MinionServer> fromDb2 = SystemManager.findMatchingEmptyProfiles(of("myhost"), singleton("11:22:33:44:55:66"));
        assertEquals(1, fromDb2.size());
        assertEquals(minion, fromDb2.get(0));
    }

    /**
     * Tests finding an empty profile with no hostname.
     */
    public void testFindByHostnameNoHostname() throws Exception {
        String hwAddr = "11:22:33:44:55:66";
        MinionServer minion = createEmptyProfile(empty(), of(hwAddr));

        List<MinionServer> fromDb = SystemManager.findMatchingEmptyProfiles(empty(), singleton(hwAddr));
        assertEquals(1, fromDb.size());
        assertEquals(minion, fromDb.get(0));

        // minion with a hostname will also match
        List<MinionServer> fromDb2 = SystemManager.findMatchingEmptyProfiles(of("myhost"), singleton(hwAddr));
        assertEquals(1, fromDb2.size());
        assertEquals(minion, fromDb2.get(0));
    }

    /**
     * Tests finding an empty profile by hostname and HW addresses.
     */
    public void testFindByHostnameAndHwAddrs() throws Exception {
        String hwAddr = "11:22:33:44:55:66";
        MinionServer minion = createEmptyProfile(of("myhost"), of(hwAddr));

        // lookup by hostname should match
        List<MinionServer> fromDb = SystemManager.findMatchingEmptyProfiles(of("myhost"), emptySet());
        assertEquals(1, fromDb.size());
        assertEquals(minion, fromDb.get(0));

        // lookup by HW addr should match
        List<MinionServer> fromDb2 = SystemManager.findMatchingEmptyProfiles(empty(), singleton(hwAddr));
        assertEquals(1, fromDb2.size());
        assertEquals(minion, fromDb2.get(0));

        // lookup by hostname and HW addr should match
        List<MinionServer> fromDb3 = SystemManager.findMatchingEmptyProfiles(of("myhost"), singleton(hwAddr));
        assertEquals(1, fromDb3.size());
        assertEquals(minion, fromDb3.get(0));

        // lookup by hostname and HW addrs should match
        Set<String> moreAddrs = new HashSet<>();
        moreAddrs.add(hwAddr);
        moreAddrs.add("11:22:33:44:55:77");
        List<MinionServer> fromDb4 = SystemManager.findMatchingEmptyProfiles(of("myhost"), moreAddrs);
        assertEquals(1, fromDb4.size());
        assertEquals(minion, fromDb4.get(0));
    }

    /**
     * Tests finding system (with multiple NICs) by hostname and HW addresses.
     */
    public void testFindByHostnameAndHwAddrsMoreNics() throws Exception {
        String hwAddr = "11:22:33:44:55:66";
        String hwAddr2 = "11:22:33:44:55:77";
        MinionServer minion = createEmptyProfile(of("myhost"), of(hwAddr));
        // add an extra iface with a different hw addr
        NetworkInterface netInterface = new NetworkInterface();
        netInterface.setHwaddr(hwAddr2);
        netInterface.setServer(minion);
        netInterface.setName("unknown2");
        ServerFactory.saveNetworkInterface(netInterface);

        // lookup by hostname should match
        List<MinionServer> fromDb = SystemManager.findMatchingEmptyProfiles(of("myhost"), emptySet());
        assertEquals(1, fromDb.size());
        assertEquals(minion, fromDb.get(0));

        // lookup by multiple HW addrs should match
        Set<String> hwAddrs = new HashSet<>();
        hwAddrs.add(hwAddr);
        hwAddrs.add(hwAddr2);
        List<MinionServer> fromDb2 = SystemManager.findMatchingEmptyProfiles(empty(), hwAddrs);
        assertEquals(1, fromDb2.size());
        assertEquals(minion, fromDb2.get(0));

        // lookup by single HW addr should match
        String fstAddr = hwAddrs.iterator().next();
        List<MinionServer> fromDb3 = SystemManager.findMatchingEmptyProfiles(empty(), singleton(fstAddr));
        assertEquals(1, fromDb3.size());
        assertEquals(minion, fromDb3.get(0));
    }

    /**
     * Tests lookup of (non-matching) system by hostname and HW address.
     */
    public void testFindByHostnameAndHwAddrsNoMatch() throws Exception {
        createEmptyProfile(of("myhost"), empty());
        List<MinionServer> fromDb = SystemManager.findMatchingEmptyProfiles(empty(), singleton("00:11:22:33:44:55"));
        assertTrue(fromDb.isEmpty());
        List<MinionServer> fromDb2 = SystemManager.findMatchingEmptyProfiles(of("otherhost"), emptySet());
        assertTrue(fromDb2.isEmpty());
    }

    private MinionServer createEmptyProfile(Optional<String> hostName, Optional<String> hwAddr) throws Exception {
        Map<String, Object> data = new HashMap<>();
        hostName.ifPresent(n -> data.put("hostname", n));
        hwAddr.ifPresent(a -> data.put("hwAddress", a));
        return SystemManager.createSystemProfile(user, hostName.orElse("test system"), data);
    }

    /**
     * Test for handling the monitoring entitlement via addServerToGroup() and removeServerFromGroup().
     * @throws Exception in case of an error
     */
    public void testAddServerToServerGroupWithMonitoring() throws Exception {
        // A new test server, no monitoring entitlement
        User user = UserTestUtils.findNewUser(TestStatics.TESTUSER, TestStatics.TESTORG);
        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        server.setServerArch(ServerFactory.lookupServerArchByLabel("x86_64-redhat-linux"));
        assertFalse(SystemManager.hasEntitlement(server.getId(), EntitlementManager.MONITORING));

        // Create a group and enable monitoring
        ServerGroup group = ServerGroupTest.createTestServerGroup(user.getOrg(), null);
        FormulaFactory.saveGroupFormulas(group.getId(), Arrays.asList(PROMETHEUS_EXPORTERS), user.getOrg());
        Map<String, Object> formulaData = new HashMap<>();
        formulaData.put("node_exporter", Collections.singletonMap("enabled", true));
        formulaData.put("apache_exporter", Collections.singletonMap("enabled", false));
        formulaData.put("postgres_exporter", Collections.singletonMap("enabled", false));
        FormulaFactory.saveGroupFormulaData(formulaData, group.getId(), user.getOrg(), PROMETHEUS_EXPORTERS);

        // Server should have a monitoring entitlement after being added to the group
        SystemManager.addServerToServerGroup(server, group);
        assertTrue(SystemManager.hasEntitlement(server.getId(), EntitlementManager.MONITORING));

        // Remove server from group, entitlement should be removed
        SystemManager.removeServersFromServerGroup(Arrays.asList(server), group);
        assertFalse(SystemManager.hasEntitlement(server.getId(), EntitlementManager.MONITORING));
    }
}
