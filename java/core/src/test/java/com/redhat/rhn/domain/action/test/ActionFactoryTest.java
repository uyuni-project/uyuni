/*
 * Copyright (c) 2026 SUSE LCC
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.domain.action.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.util.test.TimeUtilsTest;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.HardwareRefreshAction;
import com.redhat.rhn.domain.action.RebootAction;
import com.redhat.rhn.domain.action.VirtualInstanceRefreshAction;
import com.redhat.rhn.domain.action.config.ConfigAction;
import com.redhat.rhn.domain.action.config.ConfigDateDetails;
import com.redhat.rhn.domain.action.config.ConfigDateFileAction;
import com.redhat.rhn.domain.action.config.ConfigDeployAction;
import com.redhat.rhn.domain.action.config.ConfigRevisionAction;
import com.redhat.rhn.domain.action.config.ConfigRevisionActionResult;
import com.redhat.rhn.domain.action.config.ConfigUploadAction;
import com.redhat.rhn.domain.action.config.ConfigUploadMtimeAction;
import com.redhat.rhn.domain.action.config.DaemonConfigAction;
import com.redhat.rhn.domain.action.config.DaemonConfigDetails;
import com.redhat.rhn.domain.action.errata.ErrataAction;
import com.redhat.rhn.domain.action.kickstart.KickstartAction;
import com.redhat.rhn.domain.action.kickstart.KickstartActionDetails;
import com.redhat.rhn.domain.action.kickstart.KickstartInitiateAction;
import com.redhat.rhn.domain.action.kickstart.KickstartScheduleSyncAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageActionDetails;
import com.redhat.rhn.domain.action.rhnpackage.PackageAutoUpdateAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageDeltaAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageRefreshListAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageRemoveAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageRunTransactionAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageUpdateAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageVerifyAction;
import com.redhat.rhn.domain.action.script.ScriptActionDetails;
import com.redhat.rhn.domain.action.script.ScriptRunAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.server.test.ServerActionTest;
import com.redhat.rhn.domain.config.ConfigFileName;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.config.ConfigurationFactory;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.rhnpackage.PackageType;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ConfigTestUtils;
import com.redhat.rhn.testing.TestUtils;

import org.apache.commons.lang3.RandomStringUtils;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ActionFactoryTest
 */
public class ActionFactoryTest extends BaseTestCaseWithUser {

    /**
     * Test fetching an Action
     * @throws Exception something bad happened
     */
    @Test
    public void testLookup() throws Exception {

        Action a = createAction(user, ActionFactory.TYPE_HARDWARE_REFRESH_LIST);
        assertInstanceOf(HardwareRefreshAction.class, a);
        Long id = a.getId();
        Action a2 = ActionFactory.lookupById(id);
        assertNotNull(a2);
        assertTrue(a2.getName().startsWith("RHN-JAVA Test Action"));
    }

    /**
     * Test fetching an Action
     * @throws Exception something bad happened
     */
    @Test
    public void testLookupLastCompletedAction() throws Exception {
        ConfigAction a = (ConfigAction) createAction(user, ActionFactory.TYPE_CONFIGFILES_DEPLOY);
        assertInstanceOf(ConfigDeployAction.class, a);
        //complete it
        assertNotNull(a.getServerActions());
        for (ServerAction next : a.getServerActions()) {
            next.setCompletionTime(new Date());
            next.setStatusCompleted();
        }
        ActionFactory.save(a);
        ConfigRevisionAction cra = a.getConfigRevisionActions().iterator().next();
        Server server = cra.getServer();

        Action action = ActionFactory.lookupLastCompletedAction(user, ActionFactory.TYPE_CONFIGFILES_DEPLOY, server);
        assertEquals(a, action);
    }

    /**
     * Test listing of pending actions
     * @throws Exception exceptions
     */
    @Test
    public void testListPendingActions() throws Exception {
        VirtualInstanceRefreshAction a = (VirtualInstanceRefreshAction) createAction(user,
                ActionFactory.TYPE_VIRT_PROFILE_REFRESH);
        assertInstanceOf(VirtualInstanceRefreshAction.class, a);
        //complete it
        assertNotNull(a.getServerActions());
        Date earliest = Date.from(LocalDateTime.now().minusHours(1).atZone(ZoneId.systemDefault()).toInstant());
        a.setEarliestAction(earliest);

        List<ServerAction> sa = ActionFactory.listPendingServerActionsByTypes(
                List.of(ActionFactory.TYPE_VIRT_PROFILE_REFRESH));

        assertEquals(1, sa.size());
    }

    /**
     * Test fetching an Action with the logged in User
     * @throws Exception something bad happened
     */
    @Test
    public void testLookupWithLoggedInUser() throws Exception {
        Action a = createAction(user, ActionFactory.TYPE_HARDWARE_REFRESH_LIST);
        Long id = a.getId();
        Action a2 = ActionFactory.lookupByUserAndId(user, id);
        assertNotNull(a2);
        // Check to make sure it returns NULL
        // if we lookup with a User who isnt part of the
        // Org that owns that Action.  Ignore for
        // Sat mode since there is only one Org.
    }

    /**
     * Test fetching a ScriptAction
     * @throws Exception something bad happened
     */
    @Test
    public void testLookupScriptAction() throws Exception {
        Action newA = createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        Long id = newA.getId();
        Action a = ActionFactory.lookupById(id);

        assertNotNull(a);
        assertInstanceOf(ScriptRunAction.class, a);
        ScriptRunAction s = (ScriptRunAction) a;
        assertNotNull(s.getScriptActionDetails().getUsername());
        assertNotNull(s.getEarliestAction());
    }


    /**
     * Test fetching a ScriptAction
     * @throws Exception something bad happened
     */
    @Test
    public void testSchedulerUser() throws Exception {
        Action newA = createAction(user, ActionFactory.TYPE_REBOOT);
        newA.setSchedulerUser(user);
        ActionFactory.save(newA);

        assertNotNull(newA.getSchedulerUser());
    }

    /**
     * Test fetching a ConfigRevisionAction
     * @throws Exception something bad happened
     */
    @Test
    public void testLookupErrataAction() throws Exception {
        Action newA = createAction(user, ActionFactory.TYPE_ERRATA);
        assertNotNull(newA.getId());
        assertInstanceOf(ErrataAction.class, newA);
        ErrataAction ea = (ErrataAction) newA;
        assertNotNull(ea.getErrata());
        assertNotNull(((Errata) ea.getErrata().toArray()[0]).getId());
    }

    /**
     * Test fetching a DaemonConfigDetails
     * @throws Exception something bad happened
     */
    @Test
    public void testLookupDaemonConfig() throws Exception {
        Action newA = createAction(user, ActionFactory.TYPE_DAEMON_CONFIG);
        Long id = newA.getId();
        Action a = ActionFactory.lookupById(id);
        assertNotNull(a);
        assertInstanceOf(DaemonConfigAction.class, a);
        DaemonConfigAction dca = (DaemonConfigAction) a;
        assertNotNull(dca.getId());
        assertNotNull(dca.getDaemonConfigDetails());
        assertNotNull(dca.getDaemonConfigDetails().getActionId());
    }

    @Test
    public void testAddServerToAction() throws Exception {
        Server s = ServerFactoryTest.createTestServer(user);
        Action a = createAction(user, ActionFactory.TYPE_ERRATA);
        ActionFactory.addServerToAction(s.getId(), a);

        assertNotNull(a.getServerActions());
        assertEquals(a.getServerActions().size(), 1);
        Object[] array = a.getServerActions().toArray();
        ServerAction sa = (ServerAction)array[0];
        assertTrue(TimeUtilsTest.timeEquals(sa.getCreated().getTime(),
                sa.getModified().getTime()));
        assertTrue(sa.isStatusQueued());

        assertEquals(sa.getServer(), s);
    }

    @Test
    public void testLookupConfigRevisionAction() {
        Action newA = ActionFactory.createAction(ActionFactory.TYPE_CONFIGFILES_DIFF);
        newA.setOrg(user.getOrg());

        newA.setSchedulerUser(user);

        Server newS = ServerFactoryTest.createTestServer(user, true);
        ConfigRevisionAction crad = new ConfigRevisionAction();

        crad.setParentAction(newA);
        crad.setServer(newS);
        crad.setCreated(new Date());
        crad.setModified(new Date());

        // Create ConfigRevision
        ConfigRevision cr = ConfigTestUtils.createConfigRevision(user.getOrg());
        crad.setConfigRevision(cr);
        ConfigAction ca = (ConfigAction) newA;
        ca.addConfigRevisionAction(crad);
        ca.addServerAction(createServerAction(newS, newA));
        ActionFactory.save(ca);
        Long id = crad.getId();
        ConfigRevisionAction result = ActionFactory.lookupConfigRevisionAction(id);
        assertEquals(crad.getId(), result.getId());
        assertEquals(crad.getServer(), result.getServer());
        assertEquals(crad.getCreated(), result.getCreated());

    }

    @Test
    public void testLookupConfigRevisionResult() {
        Action newA = ActionFactory.createAction(ActionFactory.TYPE_CONFIGFILES_DIFF);
        newA.setOrg(user.getOrg());

        newA.setSchedulerUser(user);

        Server newS = ServerFactoryTest.createTestServer(user, true);
        ConfigRevisionAction crad = new ConfigRevisionAction();

        crad.setParentAction(newA);
        crad.setServer(newS);
        crad.setCreated(new Date());
        crad.setModified(new Date());

        // Setup the CRAResult
        ConfigRevisionActionResult cresult = new ConfigRevisionActionResult();
        cresult.setCreated(new Date());
        cresult.setModified(new Date());
        byte [] text = "Differed In Foo ".getBytes(StandardCharsets.UTF_8);
        cresult.setResult(text);
        cresult.setConfigRevisionAction(crad);
        crad.setConfigRevisionActionResult(cresult);
        // Create ConfigRevision
        ConfigRevision cr = ConfigTestUtils.createConfigRevision(user.getOrg());
        crad.setConfigRevision(cr);
        ConfigAction ca = (ConfigAction) newA;
        ca.addConfigRevisionAction(crad);
        ca.addServerAction(createServerAction(newS, newA));
        ActionFactory.save(ca);
        Long id = crad.getId();
        ConfigRevisionActionResult newResult = ActionFactory.
                                lookupConfigActionResult(id);

        assertEquals(cresult.getResultContents(), newResult.getResultContents());
        assertEquals(cresult.getActionConfigRevisionId(),
                                newResult.getActionConfigRevisionId());
    }

    @Test
    public void rescheduleSingleActionUpdatesEarliestDate() throws Exception {
        Instant testStartInstant = ZonedDateTime.now().toInstant();
        Instant originalInstant = testStartInstant.minus(1, ChronoUnit.DAYS);

        Action a1 = ActionFactoryTest.createAction(user, ActionFactory.TYPE_REBOOT);
        a1.setEarliestAction(Date.from(originalInstant));
        ServerAction sa = (ServerAction) a1.getServerActions().toArray()[0];

        sa.setStatusFailed();
        sa.setRemainingTries(0L);
        ActionFactory.save(a1);

        ActionFactory.rescheduleSingleServerAction(a1, 5L, sa.getServerId());

        a1 = TestUtils.reload(a1);
        sa = TestUtils.reload(sa);

        assertTrue(sa.isStatusQueued());
        assertTrue(sa.getRemainingTries() > 0);

        Instant newEarliestInstant = a1.getEarliestAction().toInstant();
        assertTrue(originalInstant.isBefore(newEarliestInstant));
        assertFalse(testStartInstant.isAfter(newEarliestInstant));
    }

    @Test
    public void testRescheduleFailedServerActions() {
        Instant testStartInstant = ZonedDateTime.now().toInstant();
        Instant originalInstant = testStartInstant.minus(1, ChronoUnit.DAYS);

        Action a1 = ActionFactoryTest.createEmptyAction(user, ActionFactory.TYPE_REBOOT);
        a1.setEarliestAction(Date.from(originalInstant));

        ServerAction sa1 = addServerAction(user, a1, ServerAction::setStatusFailed);
        ServerAction sa2 = addServerAction(user, a1, ServerAction::setStatusCompleted);

        ActionFactory.save(a1);

        ActionFactory.rescheduleFailedServerActions(a1, 5L);
        sa1 = TestUtils.reload(sa1);

        assertTrue(sa1.isStatusQueued());
        assertTrue(sa1.getRemainingTries() > 0);

        assertTrue(sa2.isStatusCompleted());

        Instant newEarliestInstant = a1.getEarliestAction().toInstant();
        assertTrue(originalInstant.isBefore(newEarliestInstant));
        assertFalse(testStartInstant.isAfter(newEarliestInstant));
    }

    @Test
    public void testRescheduleAllServerActions() {
        Instant testStartInstant = ZonedDateTime.now().toInstant();
        Instant originalInstant = testStartInstant.minus(1, ChronoUnit.DAYS);

        Action a1 = ActionFactoryTest.createEmptyAction(user, ActionFactory.TYPE_REBOOT);
        a1.setEarliestAction(Date.from(originalInstant));

        ServerAction sa1 = addServerAction(user, a1, ServerAction::setStatusFailed);
        ServerAction sa2 = addServerAction(user, a1, ServerAction::setStatusCompleted);

        ActionFactory.save(a1);

        ActionFactory.rescheduleAllServerActions(a1, 5L);

        sa1 = TestUtils.reload(sa1);
        sa2 = TestUtils.reload(sa2);

        assertTrue(sa1.isStatusQueued());
        assertTrue(sa1.getRemainingTries() > 0);

        assertTrue(sa2.isStatusQueued());
        assertTrue(sa2.getRemainingTries() > 0);
    }

    @Test
    public void testCreateAction() throws Exception {
        Action a = createAction(user, ActionFactory.TYPE_HARDWARE_REFRESH_LIST);
        assertNotNull(a);
    }

    @Test
    public void testCheckActionArchType() throws Exception {
        Action newA = createAction(user, ActionFactory.TYPE_PACKAGES_VERIFY);
        assertTrue(ActionFactory.checkActionArchType(newA, "verify"));
    }

    @Test
    public void testUpdateServerActions() {
        Action a1 = ActionFactoryTest.createEmptyAction(user, ActionFactory.TYPE_REBOOT);
        ServerAction sa1 = addServerAction(user, a1, ServerAction::setStatusFailed);
        ServerAction sa2 = addServerAction(user, a1, ServerAction::setStatusQueued);

        ActionFactory.save(a1);
        TestUtils.flushSession();
        HibernateFactory.getSession().evict(sa1);
        HibernateFactory.getSession().evict(sa2);
        HibernateFactory.getSession().evict(a1);

        List<Long> list = new ArrayList<>();
        list.add(sa1.getServerId());

        // Should NOT update if already in final state.
        ActionFactory.updateServerActionsPickedUp(a1, list);
        sa1 = TestUtils.reload(sa1);
        assertTrue(sa1.isStatusFailed());

        list.clear();
        list.add(sa2.getServerId());
        //Should update to STATUS_COMPLETED
        ActionFactory.updateServerActions(a1, list, ActionFactory.STATUS_COMPLETED);
        sa2 = TestUtils.reload(sa2);
        assertTrue(sa2.isStatusCompleted());
    }

    @Test
    public void rejectScheduledActionsMarkPendingServerActionsAsFailed() {
        Action a1 = ActionFactoryTest.createEmptyAction(user, ActionFactory.TYPE_REBOOT);
        ServerAction sa1 = addServerAction(user, a1, ServerAction::setStatusCompleted);
        ServerAction sa2 = addServerAction(user, a1, ServerAction::setStatusQueued);

        Action a2 = ActionFactoryTest.createEmptyAction(user, ActionFactory.TYPE_APPLY_STATES);
        ServerAction sa3 = addServerAction(user, a2, ServerAction::setStatusQueued);
        ServerAction sa4 = addServerAction(user, a2, ServerAction::setStatusPickedUp);

        a1 = TestUtils.saveAndReload(a1);
        a2 = TestUtils.saveAndReload(a2);

        List<Long> actionIds = Stream.of(a1, a2).map(Action::getId).collect(Collectors.toList());
        ActionFactory.rejectScheduledActions(actionIds, "Test Rejection Reason");

        sa1 = TestUtils.reload(sa1);
        sa2 = TestUtils.reload(sa2);
        sa3 = TestUtils.reload(sa3);
        sa4 = TestUtils.reload(sa4);

        assertTrue(sa1.isStatusCompleted());

        assertTrue(sa2.isStatusFailed());
        assertEquals("Test Rejection Reason", sa2.getResultMsg());
        assertEquals(-1, sa2.getResultCode());

        assertTrue(sa3.isStatusFailed());
        assertEquals("Test Rejection Reason", sa3.getResultMsg());
        assertEquals(-1, sa3.getResultCode());

        assertTrue(sa4.isStatusPickedUp());
    }

    public static Action createAction(User user, ActionType type) throws Exception {
        Action newA = ActionFactory.createAction(type);
        Long orgId = user.getOrg().getId();
        newA.setSchedulerUser(user);

        if (newA instanceof ErrataAction newAction) {
            setupTestErrataAction(newAction, orgId);
        }
        else if (newA instanceof ConfigUploadMtimeAction newAction) {
            setupTestConfigUploadMtimeAction(newAction, user);
        }
        else if (newA instanceof ConfigUploadAction newAction) {
            setupTestConfigUploadAction(newAction, user);
        }
        else if (newA instanceof ConfigDeployAction newAction) {
            setupTestConfigDeployAction(newAction, user);
        }
        else if (newA instanceof ScriptRunAction newAction) {
            setupTestScriptRunAction(newAction);
        }
        else if (newA instanceof KickstartScheduleSyncAction newAction) {
            setupTestKickstartAction(newAction);
        }
        else if (newA instanceof KickstartInitiateAction newAction) {
            setupTestKickstartAction(newAction);
        }
        else if (newA instanceof PackageAutoUpdateAction newAction) {
            setupTestPackageAction(newAction);
        }
        else if (newA instanceof PackageDeltaAction newAction) {
            setupTestPackageAction(newAction);
        }
        else if (newA instanceof PackageRefreshListAction newAction) {
            setupTestPackageAction(newAction);
        }
        else if (newA instanceof PackageRemoveAction newAction) {
            setupTestPackageAction(newAction);
        }
        else if (newA instanceof PackageRunTransactionAction newAction) {
            setupTestPackageAction(newAction);
        }
        else if (newA instanceof PackageUpdateAction newAction) {
            setupTestPackageAction(newAction);
        }
        else if (newA instanceof PackageVerifyAction newAction) {
            setupTestPackageAction(newAction);
        }
        // Here we specifically want to test the addition of the ServerAction details
        // objects.
        else if (newA instanceof RebootAction newAction) {
            setupTestRebootAction(newAction, user);
        }
        else if (newA instanceof DaemonConfigAction newAction) {
            setupTestDaemonConfigAction(newAction);
        }
        else if (newA instanceof VirtualInstanceRefreshAction newAction) {
            setupTestVirtualInstRefAction(newAction, user);
        }

        newA.setName("RHN-JAVA Test Action");
        newA.setActionType(type);
        newA.setOrg(user.getOrg());
        newA.setEarliestAction(new Date());
        newA.setVersion(0L);
        newA.setArchived(0L);
        newA.setCreated(new Date());
        newA.setModified(new Date());
        return ActionFactory.save(newA);
    }


    private static void setupTestErrataAction(ErrataAction newA, Long orgId) throws Exception {
        Errata e1 = ErrataFactoryTest.createTestErrata(orgId);
        Errata e2 = ErrataFactoryTest.createTestErrata(orgId);
        // add the errata
        newA.addErrata(e1);
        newA.addErrata(e2);
    }

    private static void setupTestConfigUploadMtimeAction(ConfigUploadMtimeAction cua, User user) {
        ConfigDateFileAction cfda = new ConfigDateFileAction();
        cfda.setFileName("/tmp/rhn-java-" + TestUtils.randomString());
        cfda.setFileType("W");
        cfda.setCreated(new Date());
        cfda.setModified(new Date());
        cua.addConfigDateFileAction(cfda);

        Server newS = ServerFactoryTest.createTestServer(user);
        ConfigRevision cr = ConfigTestUtils.createConfigRevision(user.getOrg());
        cua.addConfigChannelAndServer(cr.getConfigFile().getConfigChannel(), newS);
        // rhnActionConfigChannel requires a ServerAction to exist
        cua.addServerAction(ServerActionTest.createServerAction(newS, cua));
        ConfigDateDetails cdd = new ConfigDateDetails();
        cdd.setCreated(new Date());
        cdd.setModified(new Date());
        cdd.setStartDate(new Date());
        cdd.setImportContents("Y");
        cdd.setParentAction(cua);
        cua.setConfigDateDetails(cdd);
    }

    private static void setupTestConfigUploadAction(ConfigUploadAction cua, User user) {
        Server newS = ServerFactoryTest.createTestServer(user);

        ConfigRevision cr = ConfigTestUtils.createConfigRevision(user.getOrg());
        cua.addConfigChannelAndServer(cr.getConfigFile().getConfigChannel(), newS);
        cua.addServerAction(ServerActionTest.createServerAction(newS, cua));

        ConfigFileName name1 =
                ConfigurationFactory.lookupOrInsertConfigFileName("/etc/foo");
        ConfigFileName name2 =
                ConfigurationFactory.lookupOrInsertConfigFileName("/etc/bar");
        cua.addConfigFileName(name1, newS);
        cua.addConfigFileName(name2, newS);
    }

    private static void setupTestVirtualInstRefAction(VirtualInstanceRefreshAction newA, User userIn) {
        Server newS = ServerFactoryTest.createTestServer(userIn, true);
        newA.addServerAction(ServerActionTest.createServerAction(newS, newA));
    }

    private static void setupTestConfigDeployAction(ConfigDeployAction newA, User user) {
        Server newS = ServerFactoryTest.createTestServer(user, true);
        ConfigRevisionAction crad = new ConfigRevisionAction();
        crad.setParentAction(newA);
        crad.setServer(newS);
        crad.setCreated(new Date());
        crad.setModified(new Date());

        // Setup the CRAResult
        ConfigRevisionActionResult cresult = new ConfigRevisionActionResult();
        cresult.setCreated(new Date());
        cresult.setModified(new Date());
        cresult.setConfigRevisionAction(crad);
        crad.setConfigRevisionActionResult(cresult);
        // Create ConfigRevision
        ConfigRevision cr = ConfigTestUtils.createConfigRevision(user.getOrg());
        crad.setConfigRevision(cr);
        newA.addConfigRevisionAction(crad);
        newA.addServerAction(createServerAction(newS, newA));
    }

    private static void setupTestScriptRunAction(ScriptRunAction newA) {
        ScriptActionDetails sad = new ScriptActionDetails();
        sad.setUsername("AFTestTestUser");
        sad.setGroupname("AFTestTestGroup");
        String script = "#!/bin/csh\nls -al";
        sad.setScript(script.getBytes(StandardCharsets.UTF_8));
        sad.setTimeout(9999L);
        sad.setParentAction(newA);
        newA.setScriptActionDetails(sad);
    }

    private static void setupTestKickstartAction(KickstartAction newA) {
        KickstartActionDetails ksad = new KickstartActionDetails();
        ksad.setStaticDevice("eth0");
        ksad.setParentAction(newA);
        newA.setKickstartActionDetails(ksad);
    }

    private static void setupTestPackageAction(PackageAction newA) {
        PackageActionDetails d = new PackageActionDetails();
        String parameter = "upgrade";
        d.setParameter(parameter);

        //create packageArch
        Long testid = 100L;
        PackageArch arch = HibernateFactory.getSession().createNativeQuery("""
                SELECT p.* from rhnPackageArch as p WHERE p.id = :id
                """, PackageArch.class).setParameter("id", testid).getSingleResult();

        d.setArch(arch);

        //create packageName
        String testname = "Test Name " + TestUtils.randomString();
        PackageName name = new PackageName();
        name.setName(testname);
        d.setPackageName(name);
        name = TestUtils.saveAndFlush(name);

        //create packageEvr
        PackageEvr evr = PackageEvrFactory.lookupOrCreatePackageEvr("" +
                System.currentTimeMillis(), "2.0", "1.0", PackageType.RPM);
        d.setEvr(evr);
        newA.addDetail(d);
    }

    private static void setupTestRebootAction(RebootAction newA, User user) {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        addServerAction(user, newA, ServerAction::setStatusQueued);
    }

    private static void setupTestDaemonConfigAction(DaemonConfigAction newA) {
        DaemonConfigDetails dcd = new DaemonConfigDetails();
        dcd.setRestart("Y");
        dcd.setInterval(1440L);
        dcd.setCreated(new Date());
        dcd.setModified(new Date());
        dcd.setParentAction(newA);
        newA.setDaemonConfigDetails(dcd);
    }


    public static ServerAction addServerAction(User user, Action newA, Consumer<ServerAction> statusSetter) {
        Server newS = ServerFactoryTest.createTestServer(user, true);
        return ServerActionTest.createServerAction(newS, newA, statusSetter);
    }

    public static Action createEmptyAction(User user, ActionType type) {
        Action newA = ActionFactory.createAction(type);
        newA.setSchedulerUser(user);
        newA.setName("RHN-JAVA Test Action #" + RandomStringUtils.randomAlphanumeric(16));
        newA.setActionType(type);
        newA.setOrg(user.getOrg());
        newA.setEarliestAction(new Date());
        newA.setVersion(0L);
        newA.setArchived(0L);
        newA.setCreated(new Date());
        newA.setModified(new Date());
        return newA;
    }

    /**
     * Create a new ServerAction
     * @param newS new system
     * @param newA new action
     * @return ServerAction created
     */
    public static ServerAction createServerAction(Server newS, Action newA) {
        return createServerAction(newS, newA, ServerAction::setStatusQueued);
    }

    /**
     * Create a new ServerAction
     * @param newS new system
     * @param newA new action
     * @param statusSetter the status setter
     * @return ServerAction created
     */
    public static ServerAction createServerAction(Server newS, Action newA, Consumer<ServerAction> statusSetter) {
        ServerAction sa = new ServerAction();
        statusSetter.accept(sa);
        sa.setRemainingTries(10L);
        sa.setCreated(new Date());
        sa.setModified(new Date());
        sa.setServerWithCheck(newS);
        sa.setParentActionWithCheck(newA);
        return sa;
    }
}
