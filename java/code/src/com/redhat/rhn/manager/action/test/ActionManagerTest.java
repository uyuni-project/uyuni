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

package com.redhat.rhn.manager.action.test;

import static com.redhat.rhn.testing.ImageTestUtils.createActivationKey;
import static com.redhat.rhn.testing.ImageTestUtils.createImageProfile;
import static com.redhat.rhn.testing.ImageTestUtils.createImageStore;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionStatus;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsAction;
import com.redhat.rhn.domain.action.kickstart.KickstartAction;
import com.redhat.rhn.domain.action.kickstart.KickstartActionDetails;
import com.redhat.rhn.domain.action.kickstart.KickstartGuestAction;
import com.redhat.rhn.domain.action.kickstart.KickstartGuestActionDetails;
import com.redhat.rhn.domain.action.rhnpackage.PackageAction;
import com.redhat.rhn.domain.action.salt.build.ImageBuildAction;
import com.redhat.rhn.domain.action.script.ScriptActionDetails;
import com.redhat.rhn.domain.action.script.ScriptRunAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.server.test.ServerActionTest;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.KickstartSession;
import com.redhat.rhn.domain.kickstart.KickstartSessionHistory;
import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.kickstart.test.KickstartSessionTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.test.PackageTest;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.rhnset.SetCleanup;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.dto.ActionedSystem;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.frontend.dto.PackageMetadata;
import com.redhat.rhn.frontend.listview.PageControl;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.action.ActionIsChildException;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.redhat.rhn.manager.kickstart.ProvisionVirtualInstanceCommand;
import com.redhat.rhn.manager.profile.ProfileManager;
import com.redhat.rhn.manager.profile.test.ProfileManagerTest;
import com.redhat.rhn.manager.rhnset.RhnSetDecl;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.test.SystemManagerTest;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import com.suse.salt.netapi.calls.modules.Schedule;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.utils.Xor;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;

import java.util.ArrayList;
import java.util.Collection;
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
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Tests for {@link ActionManager}.
 */
public class ActionManagerTest extends JMockBaseTestCaseWithUser {
    private static Logger log = Logger.getLogger(ActionManagerTest.class);

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ClassImposteriser.INSTANCE);
        Config.get().setString("server.secret_key",
                DigestUtils.sha256Hex(TestUtils.randomString()));
    }

    public void testGetSystemGroups() throws Exception {
        ActionFactoryTest.createAction(user, ActionFactory.TYPE_REBOOT);
        ActionFactoryTest.createAction(user, ActionFactory.TYPE_REBOOT);

        PageControl pc = new PageControl();
        pc.setIndexData(false);
        pc.setFilterColumn("earliest");
        pc.setStart(1);
        DataResult dr = ActionManager.pendingActions(user, pc);
        assertNotNull(dr);
        assertTrue(dr.size() > 0);
    }

    public void testLookupAction() throws Exception {
        Action a1 = ActionFactoryTest.createAction(user, ActionFactory.TYPE_REBOOT);
        Long actionId = a1.getId();

        //Users must have access to a server for the action to lookup the action
        Server s = ServerFactoryTest.createTestServer(user, true);
        a1.addServerAction(ServerActionTest.createServerAction(s, a1));
        ActionManager.storeAction(a1);

        Action a2 = ActionManager.lookupAction(user, actionId);
        assertNotNull(a2);
    }

    public void testFailedActions() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Action parent = ActionFactoryTest.createAction(user, ActionFactory.TYPE_ERRATA);
        ServerAction child = ServerActionTest.createServerAction(ServerFactoryTest
                .createTestServer(user), parent);

        child.setStatus(ActionFactory.STATUS_FAILED);

        parent.addServerAction(child);
        ActionFactory.save(parent);
        UserFactory.save(user);

        DataResult dr = ActionManager.failedActions(user, null);
        assertNotEmpty(dr);
    }

    public void testPendingActions() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Action parent = ActionFactoryTest.createAction(user, ActionFactory.TYPE_ERRATA);
        ServerAction child = ServerActionTest.createServerAction(ServerFactoryTest
                .createTestServer(user), parent);

        child.setStatus(ActionFactory.STATUS_QUEUED);

        parent.addServerAction(child);
        ActionFactory.save(parent);
        UserFactory.save(user);

        DataResult dr = ActionManager.pendingActions(user, null);

        Long actionid = new Long(parent.getId().longValue());
        TestUtils.arraySearch(dr.toArray(), "getId", actionid);
        assertNotEmpty(dr);
    }

    private Action createActionWithServerActions(User user, int numServerActions)
        throws Exception {
        Action parent = ActionFactoryTest.createAction(user, ActionFactory.TYPE_ERRATA);
        Channel baseChannel = ChannelFactoryTest.createTestChannel(user);
        baseChannel.setParentChannel(null);
        for (int i = 0; i < numServerActions; i++) {
            Server server = ServerFactoryTest.createTestServer(user, true);
            server.addChannel(baseChannel);
            TestUtils.saveAndFlush(server);

            ServerAction child = ServerActionTest.createServerAction(server, parent);
            child.setStatus(ActionFactory.STATUS_QUEUED);
            TestUtils.saveAndFlush(child);

            parent.addServerAction(child);
        }
        ActionFactory.save(parent);
        return parent;
    }

    private Action createActionWithMinionServerActions(User user, ActionStatus status, int numServerActions)
            throws Exception {
        return createActionWithMinionServerActions(user, status, numServerActions,
                i -> {
                    try {
                        return MinionServerFactoryTest.createTestMinionServer(user);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
    }

    private Action createActionWithMinionServerActions(User user, ActionStatus status, int numServerActions,
                                                       Function<Integer, ? extends Server> serverFactory
                                                       )
            throws Exception {
        Action parent = ActionFactoryTest.createAction(user, ActionFactory.TYPE_ERRATA);
        Channel baseChannel = ChannelFactoryTest.createTestChannel(user);
        baseChannel.setParentChannel(null);
        for (int i = 0; i < numServerActions; i++) {
            Server server = serverFactory.apply(i);
            server.addChannel(baseChannel);
            TestUtils.saveAndFlush(server);

            ServerAction child = ServerActionTest.createServerAction(server, parent);
            child.setStatus(status);
            TestUtils.saveAndFlush(child);

            parent.addServerAction(child);
        }
        ActionFactory.save(parent);
        return parent;
    }

    private List<Action> createActionList(User user, Action... actions) {
        List<Action> returnList = new LinkedList<>();

        for (int i = 0; i < actions.length; i++) {
            returnList.add(actions[i]);
        }

        return returnList;
    }

    private List<ServerAction> getServerActions(Action parentAction) {
        Session session = HibernateFactory.getSession();
        Query query = session.createQuery("from ServerAction sa where " +
            "sa.parentAction = :parent_action");
        query.setEntity("parent_action", parentAction);
        return query.list();
    }

    private void assertServerActionCount(Action parentAction, int expected) {
        assertEquals(expected, getServerActions(parentAction).size());
    }

    private void assertServerActionStatus(Action parentAction, Server server, ActionStatus expectedStatus) {
        boolean found = false;
        for (ServerAction sa : getServerActions(parentAction)) {
            if (server.equals(sa.getServer())) {
                assertEquals(expectedStatus, sa.getStatus());
                found = true;
            }
        }
        if (!found) {
            fail("Server not found: " + server.getName());
        }
    }

    public void assertServerActionCount(User user, int expected) {
        Session session = HibernateFactory.getSession();
        Query query = session.createQuery("from ServerAction sa where " +
            "sa.parentAction.schedulerUser = :user");
        query.setEntity("user", user);
        List results = query.list();
        int initialSize = results.size();
        assertEquals(expected, initialSize);
    }

    public void assertActionsForUser(User user, int expected) throws Exception {
        Session session = HibernateFactory.getSession();
        Query query = session.createQuery("from Action a where a.schedulerUser = :user");
        query.setEntity("user", user);
        List results = query.list();
        int initialSize = results.size();
        assertEquals(expected, initialSize);
    }

    public void testSimpleCancelActions() throws Exception {
        Action parent = createActionWithServerActions(user, 1);
        List actionList = createActionList(user, new Action [] {parent});

        assertServerActionCount(parent, 1);
        assertActionsForUser(user, 1);
        ActionManager.cancelActions(user, actionList);
        assertServerActionCount(parent, 0);
        assertActionsForUser(user, 1); // shouldn't have been deleted
    }

    public void testSimpleCancelMinionActions() throws Exception {
        Action parent = createActionWithMinionServerActions(user, ActionFactory.STATUS_QUEUED, 3);
        List actionList = createActionList(user, new Action [] {parent});

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);

        ServerAction[] sa = parent.getServerActions().toArray(new ServerAction[3]);
        Map<String, Result<Schedule.Result>> result = new HashMap<>();
        result.put(sa[0].getServer().asMinionServer().get().getMinionId(),
                new Result<>(Xor.right(new Schedule.Result(null, true))));
        result.put(sa[1].getServer().asMinionServer().get().getMinionId(),
                new Result<>(Xor.right(new Schedule.Result("Job 123 does not exist.", false))));

        Set<Server> servers = new HashSet<>();
        servers.add(sa[0].getServer());
        servers.add(sa[1].getServer());
        servers.add(sa[2].getServer());

        Map<Action, Set<Server>> actionMap = Collections.singletonMap(parent, servers);

        context().checking(new Expectations() { {
            allowing(taskomaticMock).deleteScheduledActions(with(equal(actionMap)));
        } });

        assertServerActionCount(parent, 3);
        assertActionsForUser(user, 1);

        ActionManager.cancelActions(user, actionList);

        assertServerActionCount(parent, 0);
        assertActionsForUser(user, 1); // shouldn't have been deleted
        context().assertIsSatisfied();
    }

    /**
     * An action that is PICKEDUP should be set to FAILED when canceled, but COMPLETED or FAILED server actions should
     * not be affected of a cancellation (bsc#1098993).
     */
    public void testCancelMinionActionsMixedStatus() throws Exception {
        Action action = createActionWithMinionServerActions(user, ActionFactory.STATUS_PICKEDUP, 3);

        // Set first server action to COMPLETED
        Iterator<ServerAction> iterator = action.getServerActions().iterator();
        ServerAction completed = iterator.next();
        Server serverCompleted = completed.getServer();
        completed.setStatus(ActionFactory.STATUS_COMPLETED);

        // Set second server action to FAILED
        ServerAction failed = iterator.next();
        Server serverFailed = failed.getServer();
        failed.setStatus(ActionFactory.STATUS_FAILED);

        // Third server action stays in PICKEDUP
        ServerAction pickedUp = iterator.next();
        Server serverPickedUp = pickedUp.getServer();

        List<Action> actionList = createActionList(user, action);
        ActionManager.cancelActions(user, actionList);

        assertServerActionCount(action, 3);
        assertServerActionStatus(action, serverCompleted, ActionFactory.STATUS_COMPLETED);
        assertServerActionStatus(action, serverFailed, ActionFactory.STATUS_FAILED);
        assertServerActionStatus(action, serverPickedUp, ActionFactory.STATUS_FAILED);
    }

    public void testSimpleCancelMixedActions() throws Exception {
        Action parent = createActionWithMinionServerActions(user, ActionFactory.STATUS_QUEUED, 4,
                i -> {
                    try {
                        if (i < 3) {
                            return MinionServerFactoryTest.createTestMinionServer(user);
                        } else {
                            return ServerFactoryTest.createTestServer(user, true);
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
        List actionList = createActionList(user, new Action [] {parent});

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);

        List<ServerAction> sa = parent.getServerActions().stream()
                .filter(s -> s.getServer().asMinionServer().isPresent())
                .collect(Collectors.toList());
        Map<String, Result<Schedule.Result>> result = new HashMap<>();
        result.put(sa.get(0).getServer().asMinionServer().get().getMinionId(),
                new Result<>(Xor.right(new Schedule.Result(null, true))));
        result.put(sa.get(1).getServer().asMinionServer().get().getMinionId(),
                new Result<>(Xor.right(new Schedule.Result("Job 123 does not exist.", false))));

        Set<Server> servers = new HashSet<>();
        servers.add(sa.get(0).getServer());
        servers.add(sa.get(1).getServer());
        servers.add(sa.get(2).getServer());

        Map<Action, Set<Server>> actionMap = Collections.singletonMap(parent, servers);

        context().checking(new Expectations() { {
            allowing(taskomaticMock).deleteScheduledActions(with(equal(actionMap)));
        } });
        Optional<ServerAction> traditionalServerAction = parent.getServerActions().stream()
                .filter(s -> !s.getServer().asMinionServer().isPresent())
                .findFirst();

        assertServerActionCount(parent, 4);
        assertActionsForUser(user, 1);

        ActionManager.cancelActions(user, actionList);

        assertServerActionCount(parent, 0);
        assertActionsForUser(user, 1); // shouldn't have been deleted
        context().assertIsSatisfied();
    }

    public void testCancelActionWithChildren() throws Exception {
        Action parent = createActionWithServerActions(user, 1);
        Action child = createActionWithServerActions(user, 1);
        child.setPrerequisite(parent);
        List actionList = createActionList(user, new Action [] {parent});

        assertServerActionCount(parent, 1);
        assertActionsForUser(user, 2);
        ActionManager.cancelActions(user, actionList);
        assertServerActionCount(parent, 0);
        assertActionsForUser(user, 2); // shouldn't have been deleted
    }

    public void testCancelActionWithMultipleServerActions() throws Exception {
        Action parent = createActionWithServerActions(user, 2);
        List<Action> actionList = Collections.singletonList(parent);

        assertServerActionCount(parent, 2);
        assertActionsForUser(user, 1);
        ActionManager.cancelActions(user, actionList);
        assertServerActionCount(parent, 0);
        assertActionsForUser(user, 1); // shouldn't have been deleted
    }

    public void testCancelActionWithParentFails() throws Exception {
        Action parent = createActionWithServerActions(user, 1);
        Action child = createActionWithServerActions(user, 1);
        child.setPrerequisite(parent);
        List actionList = createActionList(user, new Action [] {child});

        try {
            ActionManager.cancelActions(user, actionList);
            fail("Exception not thrown when deleting action with a prerequisite.");
        }
        catch (ActionIsChildException e) {
            // expected
        }
    }

    public void testComplexHierarchy() throws Exception {
        Action parent1 = createActionWithServerActions(user, 3);
        for (int i = 0; i < 9; i++) {
            Action child = createActionWithServerActions(user, 2);
            child.setPrerequisite(parent1);
        }
        Action parent2 = createActionWithServerActions(user, 3);
        for (int i = 0; i < 9; i++) {
            Action child = createActionWithServerActions(user, 2);
            child.setPrerequisite(parent2);
        }
        assertServerActionCount(user, 42);

        List actionList = createActionList(user, new Action [] {parent1, parent2});

        assertServerActionCount(parent1, 3);
        assertActionsForUser(user, 20);

        ActionManager.cancelActions(user, actionList);
        assertServerActionCount(parent1, 0);
        assertActionsForUser(user, 20); // shouldn't have been deleted
        assertServerActionCount(user, 0);

    }

    public void testCancelKickstartAction() throws Exception {
        Session session = HibernateFactory.getSession();
        Action parentAction = createActionWithServerActions(user, 1);
        Server server = parentAction.getServerActions().iterator().next()
            .getServer();
        ActionFactory.save(parentAction);

        KickstartDataTest.setupTestConfiguration(user);
        KickstartData ksData = KickstartDataTest.createKickstartWithOptions(user.getOrg());
        KickstartSession ksSession = KickstartSessionTest.createKickstartSession(server,
                ksData, user, parentAction);
        TestUtils.saveAndFlush(ksSession);
        ksSession = RhnBaseTestCase.reload(ksSession);

        List actionList = createActionList(user, new Action [] {parentAction});

        Query kickstartSessions = session.createQuery(
                "from KickstartSession ks where ks.action = :action");
        kickstartSessions.setEntity("action", parentAction);
        List results = kickstartSessions.list();
        assertEquals(1, results.size());

        assertEquals(1, ksSession.getHistory().size());
        KickstartSessionHistory history =
            (KickstartSessionHistory)ksSession.getHistory().iterator().next();
        assertEquals("created", history.getState().getLabel());

        ActionManager.cancelActions(user, actionList);

        // New history entry should have been created:
        assertEquals(2, ksSession.getHistory().size());

        // Test that the kickstart wasn't deleted but rather marked as failed:
        assertEquals("failed", ksSession.getState().getLabel());
    }

    public void testCompletedActions() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Action parent = ActionFactoryTest.createAction(user, ActionFactory.TYPE_ERRATA);
        ServerAction child = ServerActionTest.createServerAction(ServerFactoryTest
                .createTestServer(user), parent);

        child.setStatus(ActionFactory.STATUS_COMPLETED);

        parent.addServerAction(child);
        ActionFactory.save(parent);
        UserFactory.save(user);

        DataResult dr = ActionManager.completedActions(user, null);
        assertNotEmpty(dr);
    }

    public void testRecentlyScheduledActions() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Action parent = ActionFactoryTest.createAction(user, ActionFactory.TYPE_ERRATA);
        ServerAction child = ServerActionTest.createServerAction(ServerFactoryTest
                .createTestServer(user), parent);

        child.setStatus(ActionFactory.STATUS_COMPLETED);
        child.setCreated(new Date(System.currentTimeMillis()));

        parent.addServerAction(child);
        ActionFactory.save(parent);
        UserFactory.save(user);

        DataResult dr = ActionManager.recentlyScheduledActions(user, null, 30);
        assertNotEmpty(dr);
    }

    public void testLookupFailLookupAction() throws Exception {
        try {
            ActionManager.lookupAction(user, new Long(-1));
            fail("Expected to fail");
        }
        catch (LookupException le) {
            assertTrue(true);
        }
    }

    public void testRescheduleAction() throws Exception {
        Action a1 = ActionFactoryTest.createAction(user, ActionFactory.TYPE_REBOOT);
        ServerAction sa = (ServerAction) a1.getServerActions().toArray()[0];

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
        } });

        sa.setStatus(ActionFactory.STATUS_FAILED);
        sa.setRemainingTries(new Long(0));
        ActionFactory.save(a1);

        ActionManager.rescheduleAction(a1);
        sa = (ServerAction) ActionFactory.reload(sa);
        assertTrue(sa.getStatus().equals(ActionFactory.STATUS_QUEUED));
        assertTrue(sa.getRemainingTries().longValue() > 0);
    }

    public void testInProgressSystems() throws Exception {
        Action a1 = ActionFactoryTest.createAction(user, ActionFactory.TYPE_REBOOT);
        ServerAction sa = (ServerAction) a1.getServerActions().toArray()[0];

        sa.setStatus(ActionFactory.STATUS_QUEUED);
        ActionFactory.save(a1);
        DataResult dr = ActionManager.inProgressSystems(user, a1, null);
        assertTrue(dr.size() > 0);
        assertTrue(dr.get(0) instanceof ActionedSystem);
        ActionedSystem as = (ActionedSystem) dr.get(0);
        as.setSecurityErrata(new Long(1));
        assertNotNull(as.getSecurityErrata());
    }

    public void testFailedSystems() throws Exception {
        Action a1 = ActionFactoryTest.createAction(user, ActionFactory.TYPE_REBOOT);
        ServerAction sa = (ServerAction) a1.getServerActions().toArray()[0];

        sa.setStatus(ActionFactory.STATUS_FAILED);
        ActionFactory.save(a1);

        assertTrue(ActionManager.failedSystems(user, a1, null).size() > 0);
    }

    public void testCreateErrataAction() throws Exception {
        Errata errata = ErrataFactoryTest.createTestErrata(user.getOrg().getId());
        Action a = ActionManager.createErrataAction(user.getOrg(), errata);
        assertNotNull(a);
        assertNotNull(a.getOrg());
        a = ActionManager.createErrataAction(user, errata);
        assertNotNull(a);
        assertNotNull(a.getOrg());
        assertTrue(a.getActionType().equals(ActionFactory.TYPE_ERRATA));
    }

    public void testAddServerToAction() throws Exception {
        User usr = UserTestUtils.createUser("testUser",
                UserTestUtils.createOrg("testOrg" + this.getClass().getSimpleName()));
        Server s = ServerFactoryTest.createTestServer(usr);
        Action a = ActionFactoryTest.createAction(usr, ActionFactory.TYPE_ERRATA);
        ActionManager.addServerToAction(s.getId(), a);

        assertNotNull(a.getServerActions());
        assertEquals(a.getServerActions().size(), 1);
        Object[] array = a.getServerActions().toArray();
        ServerAction sa = (ServerAction)array[0];
        assertTrue(sa.getStatus().equals(ActionFactory.STATUS_QUEUED));
        assertTrue(sa.getServer().equals(s));
    }

    public void testSchedulePackageRemoval() throws Exception {
        Server srvr = ServerFactoryTest.createTestServer(user, true);
        RhnSet set = RhnSetManager.createSet(user.getId(), "removable_package_list",
                SetCleanup.NOOP);
        assertNotNull(srvr);
        assertNotNull(set);

        Package pkg = PackageTest.createTestPackage(user.getOrg());

        set.addElement(pkg.getPackageName().getId(), pkg.getPackageEvr().getId(),
                pkg.getPackageArch().getId());
        RhnSetManager.store(set);

        PackageAction pa = ActionManager.schedulePackageRemoval(user, srvr,
            set, new Date());
        assertNotNull(pa);
        assertNotNull(pa.getId());
        PackageAction pa1 = (PackageAction) ActionManager.lookupAction(user, pa.getId());
        assertNotNull(pa1);
        assertEquals(pa, pa1);
    }

    public void testSchedulePackageVerify() throws Exception {
        Server srvr = ServerFactoryTest.createTestServer(user, true);
        RhnSet set = RhnSetManager.createSet(user.getId(), "verify_package_list",
                SetCleanup.NOOP);
        assertNotNull(srvr);
        assertNotNull(set);

        Package pkg = PackageTest.createTestPackage(user.getOrg());

        set.addElement(pkg.getPackageName().getId(), pkg.getPackageEvr().getId(),
                pkg.getPackageArch().getId());
        RhnSetManager.store(set);

        PackageAction pa = ActionManager.schedulePackageVerify(user, srvr, set, new Date());
        assertNotNull(pa);
        assertNotNull(pa.getId());
        PackageAction pa1 = (PackageAction) ActionManager.lookupAction(user, pa.getId());
        assertNotNull(pa1);
        assertEquals(pa, pa1);
    }

    public void testScheduleScriptRun() throws Exception {
        Server srvr = ServerFactoryTest.createTestServer(user, true);
        SystemManagerTest.giveCapability(srvr.getId(), "script.run", new Long(1));
        assertNotNull(srvr);

        List<Long> serverIds = new ArrayList<>();
        serverIds.add(srvr.getId());

        ScriptActionDetails sad = ActionFactory.createScriptActionDetails(
                "root", "root", new Long(10), "#!/bin/csh\necho hello");
        assertNotNull(sad);
        ScriptRunAction sra = ActionManager.scheduleScriptRun(
                user, serverIds, "Run script test", sad, new Date());
        assertNotNull(sra);
        assertNotNull(sra.getId());
        ScriptRunAction pa1 = (ScriptRunAction)
                ActionManager.lookupAction(user, sra.getId());
        assertNotNull(pa1);
        assertEquals(sra, pa1);
        ScriptActionDetails sad1 = pa1.getScriptActionDetails();
        assertNotNull(sad1);
        assertEquals(sad, sad1);
    }

    public void testScheduleKickstart() throws Exception {
        Server srvr = ServerFactoryTest.createTestServer(user, true);
        assertNotNull(srvr);
        KickstartDataTest.setupTestConfiguration(user);
        KickstartData testKickstartData
            = KickstartDataTest.createKickstartWithChannel(user.getOrg());

        KickstartAction ka
            = ActionManager.scheduleKickstartAction(testKickstartData,
                                                    user,
                                                    srvr,
                                                    new Date(System.currentTimeMillis()),
                                                    "",
                                                    "localhost");
        assertNotNull(ka);
        TestUtils.saveAndFlush(ka);
        assertNotNull(ka.getId());
        KickstartActionDetails kad = ka.getKickstartActionDetails();
        KickstartAction ka2 = (KickstartAction)
            ActionManager.lookupAction(user, ka.getId());
        assertNotNull(ka2);
        assertEquals(ka, ka2);
        KickstartActionDetails kad2 = ka2.getKickstartActionDetails();
        assertNotNull(kad);
        assertEquals(kad, kad2);
    }

    public void testScheduleGuestKickstart() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        Server srvr = ServerFactoryTest.createTestServer(user, true);
        assertNotNull(srvr);
        KickstartDataTest.setupTestConfiguration(user);
        KickstartData testKickstartData
            = KickstartDataTest.createKickstartWithChannel(user.getOrg());

        KickstartSession ksSession =
            KickstartSessionTest.createKickstartSession(srvr,
                                                        testKickstartData,
                                                        user);
        TestUtils.saveAndFlush(ksSession);

        String kickstartHost = "localhost.localdomain";
        ProvisionVirtualInstanceCommand command =
            new ProvisionVirtualInstanceCommand(srvr.getId(),
                                                testKickstartData.getId(),
                                                user,
                                                new Date(System.currentTimeMillis()),
                                                kickstartHost);

        command.setGuestName("testGuest1");
        command.setMemoryAllocation(256L);
        command.setLocalStorageSize(2L);
        command.setVirtualCpus(2L);
        command.setKickstartSession(ksSession);
        KickstartGuestAction ka =
            ActionManager.scheduleKickstartGuestAction(command, ksSession.getId());
        assertEquals(kickstartHost,
                ka.getKickstartGuestActionDetails().getKickstartHost());

        assertNotNull(ka);
        TestUtils.saveAndFlush(ka);
        assertNotNull(ka.getId());
        KickstartGuestActionDetails kad =
            ka.getKickstartGuestActionDetails();
        KickstartGuestAction ka2 = (KickstartGuestAction)
            ActionManager.lookupAction(user, ka.getId());
        assertNotNull(ka2);
        assertNotNull(kad.getCobblerSystemName());
        assertEquals(ka, ka2);
        KickstartGuestActionDetails kad2 =
            ka2.getKickstartGuestActionDetails();
        assertNotNull(kad);
        assertEquals(kad, kad2);

        assertEquals("256", kad.getMemMb().toString());
        assertEquals("2", kad.getVcpus().toString());
        assertEquals("testGuest1", kad.getGuestName());
        assertEquals("2", kad.getDiskGb().toString());
    }

    @SuppressWarnings("rawtypes")
    public void testSchedulePackageDelta() throws Exception {
        Server srvr = ServerFactoryTest.createTestServer(user, true);

        List<PackageListItem> profileList = new ArrayList<>();
        profileList.add(ProfileManagerTest.
                createPackageListItem("kernel-2.4.23-EL-mmccune", 500341));
        profileList.add(ProfileManagerTest.
                createPackageListItem("kernel-2.4.24-EL-mmccune", 500341));
        profileList.add(ProfileManagerTest.
                createPackageListItem("kernel-2.4.25-EL-mmccune", 500341));
        //profileList.add(ProfileManagerTest.
        //        createPackageListItem("other-2.1.0-EL-mmccune", 500400));

        List<PackageListItem> systemList = new ArrayList<>();
        systemList.add(ProfileManagerTest.
                createPackageListItem("kernel-2.4.23-EL-mmccune", 500341));


        RhnSetDecl.PACKAGES_FOR_SYSTEM_SYNC.get(user);


        List<PackageMetadata> pkgs = ProfileManager.comparePackageLists(new DataResult<PackageListItem>(profileList),
                new DataResult<PackageListItem>(systemList), "foo");

        Action action = ActionManager.schedulePackageRunTransaction(user, srvr, pkgs,
                new Date());
        assertTrue(action instanceof PackageAction);
        PackageAction pa = (PackageAction) action;

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("action_id", pa.getId());
        DataResult dr = TestUtils.runTestQuery("package_install_list", params);
        assertEquals(2, dr.size());
    }

    public void testScheduleSubscribeChannels() throws Exception {
        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionChainManager.setTaskomaticApi(taskomaticMock);
        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleSubscribeChannels(with(any(User.class)), with(any(SubscribeChannelsAction.class)));
        } });

        MinionServer srvr = MinionServerFactoryTest.createTestMinionServer(user);
        Channel base = ChannelFactoryTest.createBaseChannel(user);
        Channel ch1 = ChannelFactoryTest.createTestChannel(user.getOrg());
        Channel ch2 = ChannelFactoryTest.createTestChannel(user.getOrg());

        Optional<Channel> baseChannel = Optional.of(base);
        Set<Channel> channels = new HashSet<>();
        channels.add(ch1);
        channels.add(ch2);
        Set<Action> actions = ActionChainManager.scheduleSubscribeChannelsAction(user,
                Collections.singleton(srvr.getId()),
                baseChannel,
                channels,
                new Date(), null);

        Action action = actions.stream().findFirst().get();

        assertTrue(action instanceof SubscribeChannelsAction);
        SubscribeChannelsAction sca = (SubscribeChannelsAction)action;

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("details_id", sca.getDetails().getId());
        DataResult dr = TestUtils.runTestQuery("action_subscribe_channels_list", params);
        assertEquals(2, dr.size());

        Action action2 = ActionFactory.lookupById(action.getId());
        assertTrue(action2 instanceof SubscribeChannelsAction);
        SubscribeChannelsAction sca2 = (SubscribeChannelsAction)action2;
        assertEquals(base.getId(), sca2.getDetails().getBaseChannel().getId());
        assertEquals(2, sca2.getDetails().getChannels().size());
        sca2.getDetails().getChannels().stream().anyMatch(c -> c.getId().equals(ch1.getId()));
        sca2.getDetails().getChannels().stream().anyMatch(c -> c.getId().equals(ch2.getId()));
        // tokens are generated right when executing the action
        assertEquals(0, sca2.getDetails().getAccessTokens().size());
        assertEquals(1, action2.getServerActions().size());
    }

    public void testScheduleImageBuild() throws Exception {
        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionChainManager.setTaskomaticApi(taskomaticMock);
        ImageInfoFactory.setTaskomaticApi(taskomaticMock);

        MinionServer server = MinionServerFactoryTest.createTestMinionServer(user);
        SystemManager.entitleServer(server, EntitlementManager.CONTAINER_BUILD_HOST);
        ImageStore store = createImageStore("registry.reg", user);
        ActivationKey ak = createActivationKey(user);
        ImageProfile prof = createImageProfile("myprofile", store, ak, user);
        ActionChain actionChain = ActionChainFactory.createActionChain("my-test-ac", user);

        ImageBuildAction action = ActionChainManager.scheduleImageBuild(server.getId(),
                "1.0.0",
                prof,
                new Date(),
                actionChain, user);

        assertTrue(action != null);
        assertEquals("Build an Image Profile", action.getActionType().getName());
    }

    public static void assertNotEmpty(Collection coll) {
        assertNotNull(coll);
        if (coll.size() == 0) {
            fail(null);
        }
    }

    public void aTestSchedulePackageDelta() throws Exception {
        Server srvr = ServerFactory.lookupById(new Long(1005385254));
        RhnSetDecl.PACKAGES_FOR_SYSTEM_SYNC.get(user);

        List<PackageListItem> a = new ArrayList<>();
        PackageListItem pli = new PackageListItem();
        pli.setIdCombo("3427|195967");
        pli.setEvrId(new Long(195967));
        pli.setName("apr");
        pli.setRelease("0.4");
        pli.setNameId(new Long(3427));
        pli.setEvr("0.9.5-0.4");
        pli.setVersion("0.9.5");
        pli.setEpoch(null);
        a.add(pli);

        pli = new PackageListItem();
        pli.setIdCombo("23223|196372");
        pli.setEvrId(new Long(196372));
        pli.setName("bcel");
        pli.setRelease("1jpp_2rh");
        pli.setNameId(new Long(23223));
        pli.setEvr("5.1-1jpp_2rh:0");
        pli.setVersion("5.1");
        pli.setEpoch("0");
        a.add(pli);

        pli = new PackageListItem();
        pli.setIdCombo("500000103|250840");
        pli.setEvrId(new Long(250840));
        pli.setName("aspell");
        pli.setRelease("25.1");
        pli.setNameId(new Long(500000103));
        pli.setEvr("0.33.7.1-25.1:2");
        pli.setVersion("0.33.7.1");
        pli.setEpoch("2");
        a.add(pli);

        List<PackageListItem> b = new ArrayList<>();
        pli = new PackageListItem();
        pli.setIdCombo("26980|182097");
        pli.setEvrId(new Long(182097));
        pli.setName("asm");
        pli.setRelease("2jpp");
        pli.setNameId(new Long(26980));
        pli.setEvr("1.4.1-2jpp:0");
        pli.setVersion("1.4.1");
        pli.setEpoch("0");
        b.add(pli);

        pli = new PackageListItem();
        pli.setIdCombo("500000103|271970");
        pli.setEvrId(new Long(271970));
        pli.setName("aspell");
        pli.setRelease("25.3");
        pli.setNameId(new Long(500000103));
        pli.setEvr("0.33.7.1-25.3:2");
        pli.setVersion("0.33.7.1");
        pli.setEpoch("2");
        b.add(pli);

        pli = new PackageListItem();
        pli.setIdCombo("23223|700004953");
        pli.setEvrId(new Long(700004953));
        pli.setName("bcel");
        pli.setRelease("10");
        pli.setNameId(new Long(23223));
        pli.setEvr("5.0-10");
        pli.setVersion("5.0");
        pli.setEpoch(null);
        b.add(pli);

        List<PackageMetadata> pkgs = ProfileManager.comparePackageLists(new DataResult<PackageListItem>(a),
                new DataResult<PackageListItem>(b), "foo");

        for (Iterator<PackageMetadata> itr = pkgs.iterator(); itr.hasNext();) {
            PackageMetadata pm = itr.next();
            log.warn("pm [" + pm.toString() + "] compare [" +
                    pm.getComparison() + "] release [" +
                    (pm.getSystem() != null ? pm.getSystem().getRelease() :
                        pm.getOther().getRelease()) + "]");
        }
//        assertEquals(1, diff.size());
//        PackageMetadata pm = (PackageMetadata) diff.get(0);
//        assertNotNull(pm);
//        assertEquals(PackageMetadata.KEY_OTHER_NEWER, pm.getComparisonAsInt());
//        assertEquals("kernel-2.4.22-27.EL-bretm", pm.getProfileEvr());
//        assertEquals("kernel-2.4.21-27.EL", pm.getSystemEvr());

        Action action = ActionManager.schedulePackageRunTransaction(user, srvr, pkgs,
                new Date());
        System.out.println("Action is an [" + action.getClass().getName() + "]");
        //1005385254&set_label=packages_for_system_sync&prid=6110jjj
        /*
         * INSERT INTO rhnPackageDeltaElement
  (package_delta_id, transaction_package_id)
VALUES
  (:delta_id,
   lookup_transaction_package(:operation, :n, :e, :v, :r, :a))

         */
    }

    //schedulePackageDelta
}
