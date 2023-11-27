/*
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.util.test.TimeUtilsTest;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionStatus;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.config.ConfigAction;
import com.redhat.rhn.domain.action.config.ConfigDateDetails;
import com.redhat.rhn.domain.action.config.ConfigDateFileAction;
import com.redhat.rhn.domain.action.config.ConfigRevisionAction;
import com.redhat.rhn.domain.action.config.ConfigRevisionActionResult;
import com.redhat.rhn.domain.action.config.ConfigUploadAction;
import com.redhat.rhn.domain.action.config.ConfigUploadMtimeAction;
import com.redhat.rhn.domain.action.config.DaemonConfigAction;
import com.redhat.rhn.domain.action.config.DaemonConfigDetails;
import com.redhat.rhn.domain.action.errata.ErrataAction;
import com.redhat.rhn.domain.action.kickstart.KickstartAction;
import com.redhat.rhn.domain.action.kickstart.KickstartActionDetails;
import com.redhat.rhn.domain.action.rhnpackage.PackageAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageActionDetails;
import com.redhat.rhn.domain.action.script.ScriptActionDetails;
import com.redhat.rhn.domain.action.script.ScriptRunAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.server.test.ServerActionTest;
import com.redhat.rhn.domain.action.virtualization.BaseVirtualizationGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSetMemoryGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSetVcpusGuestAction;
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
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
        assertEquals(a.getActionType(), ActionFactory.TYPE_HARDWARE_REFRESH_LIST);
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
        assertEquals(a.getActionType(), ActionFactory.TYPE_CONFIGFILES_DEPLOY);
        //complete it
        assertNotNull(a.getServerActions());
        for (ServerAction next : a.getServerActions()) {
            next.setCompletionTime(new Date());
            next.setStatus(ActionFactory.STATUS_COMPLETED);
        }
        ActionFactory.save(a);
        ConfigRevisionAction cra = a.
                                                        getConfigRevisionActions().
                                                            iterator().next();
        Server server = cra.getServer();

        Action action = ActionFactory.lookupLastCompletedAction(user,
                                            ActionFactory.TYPE_CONFIGFILES_DEPLOY,
                                                        server);
        assertEquals(a, action);
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
        assertTrue(a instanceof ScriptRunAction);
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
        assertTrue(newA instanceof ErrataAction);
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
        assertTrue(a instanceof DaemonConfigAction);
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
        assertEquals(sa.getStatus(), ActionFactory.STATUS_QUEUED);

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

        sa.setStatus(ActionFactory.STATUS_FAILED);
        sa.setRemainingTries(0L);
        ActionFactory.save(a1);

        ActionFactory.rescheduleSingleServerAction(a1, 5L, sa.getServerId());

        a1 = HibernateFactory.reload(a1);
        sa = HibernateFactory.reload(sa);

        assertEquals(sa.getStatus(), ActionFactory.STATUS_QUEUED);
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

        ServerAction sa1 = addServerAction(user, a1, ActionFactory.STATUS_FAILED);
        ServerAction sa2 = addServerAction(user, a1, ActionFactory.STATUS_COMPLETED);

        ActionFactory.save(a1);

        ActionFactory.rescheduleFailedServerActions(a1, 5L);
        sa1 = HibernateFactory.reload(sa1);

        assertEquals(sa1.getStatus(), ActionFactory.STATUS_QUEUED);
        assertTrue(sa1.getRemainingTries() > 0);

        assertEquals(sa2.getStatus(), ActionFactory.STATUS_COMPLETED);

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

        ServerAction sa1 = addServerAction(user, a1, ActionFactory.STATUS_FAILED);
        ServerAction sa2 = addServerAction(user, a1, ActionFactory.STATUS_COMPLETED);

        ActionFactory.save(a1);

        ActionFactory.rescheduleAllServerActions(a1, 5L);

        sa1 = HibernateFactory.reload(sa1);
        sa2 = HibernateFactory.reload(sa2);

        assertEquals(sa1.getStatus(), ActionFactory.STATUS_QUEUED);
        assertTrue(sa1.getRemainingTries() > 0);

        assertEquals(sa2.getStatus(), ActionFactory.STATUS_QUEUED);
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
        ServerAction sa1 = addServerAction(user, a1, ActionFactory.STATUS_FAILED);
        ServerAction sa2 = addServerAction(user, a1, ActionFactory.STATUS_QUEUED);

        ActionFactory.save(a1);
        flushAndEvict(sa1);
        flushAndEvict(sa2);

        List<Long> list = new ArrayList<>();
        list.add(sa1.getServerId());

        // Should NOT update if already in final state.
        ActionFactory.updateServerActionsPickedUp(a1, list);
        HibernateFactory.reload(sa1);
        assertEquals(sa1.getStatus(), ActionFactory.STATUS_FAILED);

        list.clear();
        list.add(sa2.getServerId());
        //Should update to STATUS_COMPLETED
        ActionFactory.updateServerActions(a1, list, ActionFactory.STATUS_COMPLETED);
        HibernateFactory.reload(sa2);
        assertEquals(sa2.getStatus(), ActionFactory.STATUS_COMPLETED);
    }

    @Test
    public void rejectScheduledActionsMarkPendingServerActionsAsFailed() {
        Action a1 = ActionFactoryTest.createEmptyAction(user, ActionFactory.TYPE_REBOOT);
        ServerAction sa1 = addServerAction(user, a1, ActionFactory.STATUS_COMPLETED);
        ServerAction sa2 = addServerAction(user, a1, ActionFactory.STATUS_QUEUED);

        Action a2 = ActionFactoryTest.createEmptyAction(user, ActionFactory.TYPE_APPLY_STATES);
        ServerAction sa3 = addServerAction(user, a2, ActionFactory.STATUS_QUEUED);
        ServerAction sa4 = addServerAction(user, a2, ActionFactory.STATUS_PICKED_UP);

        TestUtils.saveAndReload(a1);
        TestUtils.saveAndReload(a2);

        List<Long> actionIds = Stream.of(a1, a2).map(Action::getId).collect(Collectors.toList());
        ActionFactory.rejectScheduledActions(actionIds, "Test Rejection Reason");

        sa1 = HibernateFactory.reload(sa1);
        sa2 = HibernateFactory.reload(sa2);
        sa3 = HibernateFactory.reload(sa3);
        sa4 = HibernateFactory.reload(sa4);

        assertEquals(ActionFactory.STATUS_COMPLETED, sa1.getStatus());

        assertEquals(ActionFactory.STATUS_FAILED, sa2.getStatus());
        assertEquals("Test Rejection Reason", sa2.getResultMsg());
        assertEquals(-1, sa2.getResultCode());

        assertEquals(ActionFactory.STATUS_FAILED, sa3.getStatus());
        assertEquals("Test Rejection Reason", sa3.getResultMsg());
        assertEquals(-1, sa3.getResultCode());

        assertEquals(ActionFactory.STATUS_PICKED_UP, sa4.getStatus());
    }

    public static Action createAction(User user, ActionType type) throws Exception {
        Action newA = ActionFactory.createAction(type);
        Long orgId = user.getOrg().getId();
        newA.setSchedulerUser(user);
        if (type.equals(ActionFactory.TYPE_ERRATA)) {
            Errata e1 = ErrataFactoryTest.createTestErrata(orgId);
            Errata e2 = ErrataFactoryTest.createTestErrata(orgId);
            // add the errata
            ((ErrataAction) newA).addErrata(e1);
            ((ErrataAction) newA).addErrata(e2);
        }
        else if (type.equals(ActionFactory.TYPE_CONFIGFILES_MTIME_UPLOAD)) {
            ConfigUploadMtimeAction cua = (ConfigUploadMtimeAction) newA;
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
            cua.addServerAction(ServerActionTest.createServerAction(newS, newA));
            ConfigDateDetails cdd = new ConfigDateDetails();
            cdd.setCreated(new Date());
            cdd.setModified(new Date());
            cdd.setStartDate(new Date());
            cdd.setImportContents("Y");
            cdd.setParentAction(cua);
            cua.setConfigDateDetails(cdd);
        }
        else if (type.equals(ActionFactory.TYPE_CONFIGFILES_UPLOAD)) {
            ConfigUploadAction cua = (ConfigUploadAction) newA;
            Server newS = ServerFactoryTest.createTestServer(user);

            ConfigRevision cr = ConfigTestUtils.createConfigRevision(user.getOrg());
            cua.addConfigChannelAndServer(cr.getConfigFile().getConfigChannel(), newS);
            cua.addServerAction(ServerActionTest.createServerAction(newS, newA));

            ConfigFileName name1 =
                ConfigurationFactory.lookupOrInsertConfigFileName("/etc/foo");
            ConfigFileName name2 =
                ConfigurationFactory.lookupOrInsertConfigFileName("/etc/bar");
            cua.addConfigFileName(name1, newS);
            cua.addConfigFileName(name2, newS);
        }
        else if (type.equals(ActionFactory.TYPE_CONFIGFILES_DEPLOY)) {
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
            ConfigAction ca = (ConfigAction) newA;
            ca.addConfigRevisionAction(crad);
            ca.addServerAction(createServerAction(newS, newA));
        }
        else if (type.equals(ActionFactory.TYPE_SCRIPT_RUN)) {
            ScriptActionDetails sad = new ScriptActionDetails();
            sad.setUsername("AFTestTestUser");
            sad.setGroupname("AFTestTestGroup");
            String script = "#!/bin/csh\nls -al";
            sad.setScript(script.getBytes(StandardCharsets.UTF_8));
            sad.setTimeout(9999L);
            sad.setParentAction(newA);
            ((ScriptRunAction) newA).setScriptActionDetails(sad);
        }
        else if (type.equals(ActionFactory.TYPE_KICKSTART_INITIATE) ||
                type.equals(ActionFactory.TYPE_KICKSTART_SCHEDULE_SYNC)) {
            KickstartActionDetails ksad = new KickstartActionDetails();
            ksad.setStaticDevice("eth0");
            ksad.setParentAction(newA);
            ((KickstartAction) newA).setKickstartActionDetails(ksad);
        }
        else if (type.equals(ActionFactory.TYPE_PACKAGES_AUTOUPDATE) ||
                type.equals(ActionFactory.TYPE_PACKAGES_DELTA) ||
                type.equals(ActionFactory.TYPE_PACKAGES_REFRESH_LIST) ||
                type.equals(ActionFactory.TYPE_PACKAGES_REMOVE) ||
                type.equals(ActionFactory.TYPE_PACKAGES_RUNTRANSACTION) ||
                type.equals(ActionFactory.TYPE_PACKAGES_UPDATE) ||
                type.equals(ActionFactory.TYPE_PACKAGES_VERIFY)) {

            PackageActionDetails d = new PackageActionDetails();
            String parameter = "upgrade";
            d.setParameter(parameter);

            //create packageArch
            Long testid = 100L;
            String query = "PackageArch.findById";
            PackageArch arch = (PackageArch) TestUtils.lookupFromCacheById(testid, query);
            d.setArch(arch);

            //create packageName
            String testname = "Test Name " + TestUtils.randomString();
            PackageName name = new PackageName();
            name.setName(testname);
            d.setPackageName(name);
            TestUtils.saveAndFlush(name);

            //create packageEvr
            PackageEvr evr = PackageEvrFactory.lookupOrCreatePackageEvr("" +
                    System.currentTimeMillis(), "2.0", "1.0", PackageType.RPM);
            d.setEvr(evr);
            ((PackageAction) newA).addDetail(d);
        }
        // Here we specifically want to test the addition of the ServerAction details
        // objects.
        else if (type.equals(ActionFactory.TYPE_REBOOT)) {
            user.addPermanentRole(RoleFactory.ORG_ADMIN);
            addServerAction(user, newA, ActionFactory.STATUS_QUEUED);
        }
        else if (type.equals(ActionFactory.TYPE_DAEMON_CONFIG)) {
            DaemonConfigDetails dcd = new DaemonConfigDetails();
            dcd.setRestart("Y");
            dcd.setInterval(1440L);
            dcd.setDaemonConfigCreated(new Date());
            dcd.setDaemonConfigModified(new Date());
            dcd.setParentAction(newA);
            ((DaemonConfigAction) newA).setDaemonConfigDetails(dcd);
        }
        else if (type.equals(ActionFactory.TYPE_VIRTUALIZATION_DELETE) ||
                 type.equals(ActionFactory.TYPE_VIRTUALIZATION_DESTROY) ||
                 type.equals(ActionFactory.TYPE_VIRTUALIZATION_REBOOT) ||
                 type.equals(ActionFactory.TYPE_VIRTUALIZATION_RESUME) ||
                 type.equals(ActionFactory.TYPE_VIRTUALIZATION_SHUTDOWN) ||
                 type.equals(ActionFactory.TYPE_VIRTUALIZATION_START) ||
                 type.equals(ActionFactory.TYPE_VIRTUALIZATION_SUSPEND)) {
            BaseVirtualizationGuestAction va = (BaseVirtualizationGuestAction)newA;
            va.setUuid(RandomStringUtils.randomAlphanumeric(8));
        }
        else if (type.equals(ActionFactory.TYPE_VIRTUALIZATION_SET_MEMORY)) {
           VirtualizationSetMemoryGuestAction va = (VirtualizationSetMemoryGuestAction)newA;
            va.setUuid(RandomStringUtils.randomAlphanumeric(8));
           va.setMemory(1234);
        }
        else if (type.equals(ActionFactory.TYPE_VIRTUALIZATION_SET_VCPUS)) {
            VirtualizationSetVcpusGuestAction va = (VirtualizationSetVcpusGuestAction)newA;
            va.setUuid(RandomStringUtils.randomAlphanumeric(8));
            va.setVcpu(12);
        }

        newA.setName("RHN-JAVA Test Action");
        newA.setActionType(type);
        newA.setOrg(user.getOrg());
        newA.setEarliestAction(new Date());
        newA.setVersion(0L);
        newA.setArchived(0L);
        newA.setCreated(new Date());
        newA.setModified(new Date());
        ActionFactory.save(newA);
        return newA;
    }

    public static ServerAction addServerAction(User user, Action newA, ActionStatus status) {
        Server newS = ServerFactoryTest.createTestServer(user, true);
        ServerAction serverAction = ServerActionTest.createServerAction(newS, newA, status);
        newA.addServerAction(serverAction);
        return serverAction;
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
        return createServerAction(newS, newA, ActionFactory.STATUS_QUEUED);
    }

    /**
     * Create a new ServerAction
     * @param newS new system
     * @param newA new action
     * @param status the status
     * @return ServerAction created
     */
    public static ServerAction createServerAction(Server newS, Action newA, ActionStatus status) {
        ServerAction sa = new ServerAction();
        sa.setStatus(status);
        sa.setRemainingTries(10L);
        sa.setCreated(new Date());
        sa.setModified(new Date());
        sa.setServerWithCheck(newS);
        sa.setParentActionWithCheck(newA);
        return sa;
    }
}


