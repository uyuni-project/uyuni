/**
 * Copyright (c) 2017 SUSE LLC
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
package com.suse.manager.webui.services.test;

import static com.redhat.rhn.domain.action.ActionFactory.STATUS_COMPLETED;
import static com.redhat.rhn.domain.action.ActionFactory.STATUS_FAILED;
import static com.redhat.rhn.domain.action.ActionFactory.STATUS_PICKED_UP;
import static com.redhat.rhn.domain.action.ActionFactory.STATUS_QUEUED;

import com.google.gson.JsonObject;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionStatus;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsAction;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsActionDetails;
import com.redhat.rhn.domain.action.config.ConfigAction;
import com.redhat.rhn.domain.action.script.ScriptActionDetails;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.action.virtualization.BaseVirtualizationAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationRebootAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationShutdownAction;
import com.redhat.rhn.domain.channel.AccessToken;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.test.PackageEvrFactoryTest;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitlementManager;
import com.redhat.rhn.manager.system.entitling.SystemEntitler;
import com.redhat.rhn.manager.system.entitling.SystemUnentitler;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.task.checkin.SystemSummary;
import com.redhat.rhn.testing.ConfigTestUtils;
import com.redhat.rhn.testing.ErrataTestUtils;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.ServerTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.google.gson.JsonElement;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.SaltActionChainGeneratorService;
import com.suse.manager.webui.services.SaltServerActionService;
import com.suse.manager.webui.services.iface.TestVirtManager;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.utils.SaltModuleRun;
import com.suse.manager.webui.utils.SaltState;
import com.suse.manager.webui.utils.SaltSystemReboot;
import com.suse.salt.netapi.calls.LocalCall;

import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;

import java.io.IOException;
import java.nio.file.Files;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;


public class SaltServerActionServiceTest extends JMockBaseTestCaseWithUser {

    private MinionServer minion;
    private SaltServerActionService saltServerActionService;
    private SystemSummary sshPushSystemMock;
    private SystemEntitlementManager systemEntitlementManager;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ClassImposteriser.INSTANCE);

        VirtManager virtManager = new TestVirtManager() {
            @Override
            public void updateLibvirtEngine(MinionServer minion) {
            }
        };
        SystemQuery systemQuery = new SaltService() {
            @Override
            public Optional<JsonElement> rawJsonCall(LocalCall<?> call, String minionId) {
                return Optional.of(new JsonObject());
            }
        };
        minion = MinionServerFactoryTest.createTestMinionServer(user);

        saltServerActionService = new SaltServerActionService(systemQuery);
        saltServerActionService.setSkipCommandScriptPerms(true);
        systemEntitlementManager = new SystemEntitlementManager(
                new SystemUnentitler(),
                new SystemEntitler(systemQuery, virtManager)
        );

        sshPushSystemMock = mock(SystemSummary.class);
    }

    public void testPackageUpdate() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        List<MinionServer> mins = new ArrayList<>();
        mins.add(minion);

        List<MinionSummary> minionSummaries = mins.stream().
                map(MinionSummary::new).collect(Collectors.toList());

        Channel channel = ChannelFactoryTest.createTestChannel(user);
        Package p64 = ErrataTestUtils.createTestPackage(user, channel, "x86_64");
        Package p32 = ErrataTestUtils.createLaterTestPackage(user, null, channel, p64);
        p32.setPackageEvr(p64.getPackageEvr());
        p32.setPackageArch(PackageFactory.lookupPackageArchByLabel("i686"));
        TestUtils.saveAndFlush(p32);

        List<Map<String, Long>> packageMaps = new ArrayList<>();
        Map<String, Long> pkg32map = new HashMap<>();
        pkg32map.put("name_id", p32.getPackageName().getId());
        pkg32map.put("evr_id", p32.getPackageEvr().getId());
        pkg32map.put("arch_id", p32.getPackageArch().getId());
        packageMaps.add(pkg32map);
        Map<String, Long> pkg64map = new HashMap<>();
        pkg64map.put("name_id", p64.getPackageName().getId());
        pkg64map.put("evr_id", p64.getPackageEvr().getId());
        pkg64map.put("arch_id", p64.getPackageArch().getId());
        packageMaps.add(pkg64map);

        final ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        Action action = ActionManager.createAction(user, ActionFactory.TYPE_PACKAGES_UPDATE,
                "test action", Date.from(now.toInstant()));

        ActionFactory.addServerToAction(minion, action);

        ActionManager.addPackageActionDetails(Arrays.asList(action), packageMaps);
        TestUtils.flushAndEvict(action);
        Action updateAction = ActionFactory.lookupById(action.getId());

        Map<LocalCall<?>, List<MinionSummary>> result = saltServerActionService.callsForAction(updateAction, minionSummaries);
        RhnBaseTestCase.assertNotEmpty(result.values());
    }

    public void testPackageRemoveDebian() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setServerArch(ServerFactory.lookupServerArchByLabel("amd64-debian-linux"));
        List<MinionServer> mins = new ArrayList<>();
        mins.add(minion);

        List<MinionSummary> minionSummaries = mins.stream().
                map(MinionSummary::new).collect(Collectors.toList());

        Channel channel = ChannelFactoryTest.createTestChannel(user);
        Package p = ErrataTestUtils.createTestPackage(user, channel, "amd64-deb");
        p.setPackageEvr(PackageEvrFactoryTest.createTestPackageEvr(null, "1.0.0", "X"));

        Map<String, Long> pkgMap = new HashMap<>();
        pkgMap.put("name_id", p.getPackageName().getId());
        pkgMap.put("evr_id", p.getPackageEvr().getId());
        pkgMap.put("arch_id", p.getPackageArch().getId());

        final ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        Action action = ActionManager.createAction(user, ActionFactory.TYPE_PACKAGES_UPDATE,
                "test action", Date.from(now.toInstant()));

        ActionFactory.addServerToAction(minion, action);

        ActionManager.addPackageActionDetails(Arrays.asList(action), Collections.singletonList(pkgMap));
        TestUtils.flushAndEvict(action);
        Action updateAction = ActionFactory.lookupById(action.getId());

        Map<LocalCall<?>, List<MinionSummary>> result = saltServerActionService.callsForAction(updateAction,
                minionSummaries);
        assertEquals(1, result.size());
        LocalCall<?> resultCall = result.keySet().iterator().next();
        List<List<String>> resultPkgs = (List<List<String>>) ((Map) ((Map) resultCall.getPayload().get("kwarg")).get(
                "pillar")).get("param_pkgs");

        List<String> resultPkg =
                resultPkgs.stream().filter(pkg -> p.getPackageName().getName().equals(pkg.get(0))).findFirst().get();

        // Assert if the package EVRAs are sent to Salt correctly
        assertEquals("amd64", resultPkg.get(1));
        assertEquals("1.0.0", resultPkg.get(2));
    }

    public void testPackageUpdateDebian() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setServerArch(ServerFactory.lookupServerArchByLabel("amd64-debian-linux"));
        List<MinionServer> mins = new ArrayList<>();
        mins.add(minion);

        List<MinionSummary> minionSummaries = mins.stream().
                map(MinionSummary::new).collect(Collectors.toList());

        Channel channel = ChannelFactoryTest.createTestChannel(user);
        Package p1 = ErrataTestUtils.createTestPackage(user, channel, "amd64-deb");
        p1.setPackageEvr(PackageEvrFactoryTest.createTestPackageEvr(null, "1.0.0", "X"));
        Package p2 = ErrataTestUtils.createTestPackage(user, channel, "amd64-deb");
        p2.setPackageEvr(PackageEvrFactoryTest.createTestPackageEvr("1", "1.2", "1ubuntu1"));

        List<Map<String, Long>> packageMaps = new ArrayList<>();
        Map<String, Long> pkgMap = new HashMap<>();
        pkgMap.put("name_id", p1.getPackageName().getId());
        pkgMap.put("evr_id", p1.getPackageEvr().getId());
        pkgMap.put("arch_id", p1.getPackageArch().getId());
        packageMaps.add(pkgMap);

        pkgMap = new HashMap<>();
        pkgMap.put("name_id", p2.getPackageName().getId());
        pkgMap.put("evr_id", p2.getPackageEvr().getId());
        pkgMap.put("arch_id", p2.getPackageArch().getId());
        packageMaps.add(pkgMap);

        final ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        Action action = ActionManager.createAction(user, ActionFactory.TYPE_PACKAGES_UPDATE,
                "test action", Date.from(now.toInstant()));

        ActionFactory.addServerToAction(minion, action);

        ActionManager.addPackageActionDetails(Arrays.asList(action), packageMaps);
        TestUtils.flushAndEvict(action);
        Action updateAction = ActionFactory.lookupById(action.getId());

        Map<LocalCall<?>, List<MinionSummary>> result = saltServerActionService.callsForAction(updateAction,
                minionSummaries);
        assertEquals(1, result.size());
        LocalCall<?> resultCall = result.keySet().iterator().next();
        List<List<String>> resultPkgs = (List<List<String>>) ((Map) ((Map) resultCall.getPayload().get("kwarg")).get(
                "pillar")).get("param_pkgs");

        List<String> resultP1 =
                resultPkgs.stream().filter(pkg -> p1.getPackageName().getName().equals(pkg.get(0))).findFirst().get();
        List<String> resultP2 =
                resultPkgs.stream().filter(pkg -> p2.getPackageName().getName().equals(pkg.get(0))).findFirst().get();

        // Assert if the package EVRAs are sent to Salt correctly
        assertEquals("amd64", resultP1.get(1));
        assertEquals("1.0.0", resultP1.get(2));

        assertEquals("amd64", resultP2.get(1));
        assertEquals("1:1.2-1ubuntu1", resultP2.get(2));
    }

    public void testDeployFiles() throws Exception {
        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer minion2 = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer minion3 = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer minion4 = MinionServerFactoryTest.createTestMinionServer(user);

        final ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ConfigAction configAction = ActionManager.createConfigAction(user, ActionFactory.TYPE_CONFIGFILES_DEPLOY,
                Date.from(now.toInstant()));

        ActionFactory.addServerToAction(minion1, configAction);
        ActionFactory.addServerToAction(minion2, configAction);
        ActionFactory.addServerToAction(minion3, configAction);
        ActionFactory.addServerToAction(minion4, configAction);

        //create the revision, file, and channel.
        ConfigRevision revision1 = ConfigTestUtils.createConfigRevision(user.getOrg());
        revision1.getConfigFile().setLatestConfigRevision(revision1);
        ConfigRevision revision2 = ConfigTestUtils.createConfigRevision(user.getOrg());
        revision2.getConfigFile().setLatestConfigRevision(revision2);
        ConfigRevision revision3 = ConfigTestUtils.createConfigRevision(user.getOrg());
        revision3.getConfigFile().setLatestConfigRevision(revision3);

        ActionFactory.addConfigRevisionToAction(revision1, minion1, configAction);
        ActionFactory.addConfigRevisionToAction(revision2, minion1, configAction);
        ActionFactory.addConfigRevisionToAction(revision1, minion2, configAction);
        ActionFactory.addConfigRevisionToAction(revision2, minion2, configAction);

        ActionFactory.addConfigRevisionToAction(revision1, minion3, configAction);
        ActionFactory.addConfigRevisionToAction(revision3, minion4, configAction);

        TestUtils.saveAndReload(configAction);

        Map<LocalCall<?>, List<MinionSummary>> result =
                saltServerActionService.callsForAction(configAction);
        assertEquals(result.size(), 3);
    }

    public void testVirtActions() throws Exception {
        MinionServer minionHost = (MinionServer)ServerTestUtils.createVirtHostWithGuests(user, 1, true, systemEntitlementManager);
        List<MinionSummary> minions = Arrays.asList(new MinionSummary(minionHost));

        List<ActionType> actionTypes = Arrays.asList(ActionFactory.TYPE_VIRTUALIZATION_DELETE,
                                                     ActionFactory.TYPE_VIRTUALIZATION_REBOOT,
                                                     ActionFactory.TYPE_VIRTUALIZATION_RESUME,
                                                     ActionFactory.TYPE_VIRTUALIZATION_SET_MEMORY,
                                                     ActionFactory.TYPE_VIRTUALIZATION_SET_VCPUS,
                                                     ActionFactory.TYPE_VIRTUALIZATION_SHUTDOWN,
                                                     ActionFactory.TYPE_VIRTUALIZATION_START,
                                                     ActionFactory.TYPE_VIRTUALIZATION_SUSPEND);

        for (ActionType type : actionTypes) {
            Action action = ActionFactoryTest.createAction(user, type);
            BaseVirtualizationAction va = (BaseVirtualizationAction)action;
            va.setUuid(minionHost.getGuests().iterator().next().getUuid());
            ActionFactory.addServerToAction(minionHost, action);

            Map<LocalCall<?>, List<MinionSummary>> result = saltServerActionService.callsForAction(action, minions);
            assertEquals(1, result.size());
        }
    }

    public void testVirtForceoff() throws Exception {
        MinionServer minionHost = (MinionServer)ServerTestUtils.createVirtHostWithGuests(user, 1, true, systemEntitlementManager);
        List<MinionSummary> minions = Arrays.asList(new MinionSummary(minionHost));

        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_VIRTUALIZATION_SHUTDOWN);
        VirtualizationShutdownAction va = (VirtualizationShutdownAction)action;
        va.setUuid(minionHost.getGuests().iterator().next().getUuid());
        va.setForce(true);
        ActionFactory.addServerToAction(minionHost, action);

        Map<LocalCall<?>, List<MinionSummary>> result = saltServerActionService.callsForAction(action, minions);
        LocalCall<?> saltCall = result.keySet().iterator().next();
        assertStateApplyWithPillar("virt.statechange", "domain_state", "powered_off", saltCall);
    }

    public void testVirtReset() throws Exception {
        MinionServer minionHost = (MinionServer)ServerTestUtils.createVirtHostWithGuests(user, 1, true, systemEntitlementManager);
        List<MinionSummary> minions = Arrays.asList(new MinionSummary(minionHost));

        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_VIRTUALIZATION_REBOOT);
        VirtualizationRebootAction va = (VirtualizationRebootAction)action;
        va.setUuid(minionHost.getGuests().iterator().next().getUuid());
        va.setForce(true);
        ActionFactory.addServerToAction(minionHost, action);

        Map<LocalCall<?>, List<MinionSummary>> result = saltServerActionService.callsForAction(action, minions);
        LocalCall<?> saltCall = result.keySet().iterator().next();
        assertStateApply("virt.reset", saltCall);
    }

    private void assertStateApply(String expectedState, LocalCall<?> call) {
        assertStateApplyWithPillar(expectedState, null, null, call);
    }

    @SuppressWarnings("unchecked")
    private void assertStateApplyWithPillar(String expectedState, String pillarEntry, Object pillarValue, LocalCall<?> call) {
        assertEquals("state", call.getModuleName());
        assertEquals("apply", call.getFunctionName());
        Map<String, Object> kwargs = ((Map<String, Object>)call.getPayload().get("kwarg"));
        assertTrue(((List<String>)kwargs.get("mods")).contains(expectedState));
        if (pillarEntry != null) {
            assertEquals(pillarValue, ((Map<String, Object>)kwargs.get("pillar")).get(pillarEntry));
        }
    }

    public void testExecuteActionChain() throws Exception {
        SaltUtils.INSTANCE.setScriptsDir(Files.createTempDirectory("actionscripts"));

        SaltActionChainGeneratorService generatorService = new SaltActionChainGeneratorService() {
            @Override
            public Map<MinionSummary, Integer> createActionChainSLSFiles(ActionChain actionChain, MinionSummary minion,
                    List<SaltState> states, Optional<String> extraFileRefs) {
                assertEquals(3, states.size());
                SaltModuleRun scriptRun = (SaltModuleRun)states.get(0);
                SaltSystemReboot reboot = (SaltSystemReboot)states.get(1);
                SaltModuleRun highstate = (SaltModuleRun)states.get(2);

                long scriptActionId = actionChain.getEntries().stream()
                        .filter(ace -> ace.getServerId().equals(minion.getServerId()) && ace.getAction().getActionType().equals(ActionFactory.TYPE_SCRIPT_RUN))
                        .map(ace -> ace.getActionId())
                        .findFirst().get();
                long rebootActionId = actionChain.getEntries().stream()
                        .filter(ace -> ace.getServerId().equals(minion.getServerId()) && ace.getAction().getActionType().equals(ActionFactory.TYPE_REBOOT))
                        .map(ace -> ace.getActionId())
                        .findFirst().get();
                long highstateActionId = actionChain.getEntries().stream()
                        .filter(ace -> ace.getServerId().equals(minion.getServerId()) && ace.getAction().getActionType().equals(ActionFactory.TYPE_APPLY_STATES))
                        .map(ace -> ace.getActionId())
                        .findFirst().get();
                assertEquals(SaltActionChainGeneratorService.ACTION_STATE_ID_PREFIX + actionChain.getId() + "_action_" + scriptActionId,
                        scriptRun.getId());
                assertEquals(SaltActionChainGeneratorService.ACTION_STATE_ID_PREFIX + actionChain.getId() + "_action_" + rebootActionId,
                        reboot.getId());
                assertEquals(SaltActionChainGeneratorService.ACTION_STATE_ID_PREFIX + actionChain.getId() + "_action_" + highstateActionId,
                        highstate.getId());

                assertEquals(true, scriptRun.getKwargs().get("queue"));
                assertEquals(true, highstate.getKwargs().get("queue"));
                return null;
            }
        };

        saltServerActionService.setSaltActionChainGeneratorService(generatorService);
        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        ActionChainFactory.setTaskomaticApi(taskomaticMock);

        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        SystemManager.giveCapability(minion1.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);

        MinionServer minion2 = MinionServerFactoryTest.createTestMinionServer(user);
        SystemManager.giveCapability(minion2.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);

        Server server1 = ServerFactoryTest.createTestServer(user);
        SystemManager.giveCapability(server1.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);

        String label = TestUtils.randomString();
        ActionChain actionChain = ActionChainFactory.createActionChain(label, user);


        Date earliestAction = new Date();

        ScriptActionDetails sad = ActionFactory.createScriptActionDetails(
                "root", "root", 10L, "#!/bin/csh\necho hello");
        Set<Action> scriptActions = ActionChainManager.scheduleScriptRuns(user,
                Arrays.asList(minion1.getId(), minion2.getId(), server1.getId()),
                "script", sad, earliestAction, actionChain);

        Set<Long> allServerIds = new HashSet<>();
        Collections.addAll(allServerIds, minion1.getId(), minion2.getId(), server1.getId());

        Set<Action> rebootActions = ActionChainManager.scheduleRebootActions(user,
                allServerIds, earliestAction, actionChain);

        Set<Action> highstateActions = ActionChainManager.scheduleApplyStates(user,
                Arrays.asList(minion1.getId(), minion2.getId()), Optional.empty(),
                earliestAction, actionChain);

        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleActionChainExecution(with(any(ActionChain.class)));
        } });

        ActionChainFactory.schedule(actionChain, earliestAction);

        saltServerActionService.executeActionChain(actionChain.getId());
    }

    public void testSubscribeChannels() throws Exception {
        Channel base = ChannelFactoryTest.createBaseChannel(user);
        Channel ch1 = ChannelFactoryTest.createTestChannel(user.getOrg());
        ch1.setParentChannel(base);
        TestUtils.saveAndFlush(ch1);
        Channel ch2 = ChannelFactoryTest.createTestChannel(user.getOrg());
        ch2.setParentChannel(base);
        TestUtils.saveAndFlush(ch2);

        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);

        final ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        SubscribeChannelsAction action = (SubscribeChannelsAction)ActionManager.createAction(user, ActionFactory.TYPE_SUBSCRIBE_CHANNELS, "Subscribe to channels",
                Date.from(now.toInstant()));

        SubscribeChannelsActionDetails details = new SubscribeChannelsActionDetails();
        details.setBaseChannel(base);
        details.setChannels(Arrays.asList(ch1, ch2).stream().collect(Collectors.toSet()));
        action.setDetails(details);
        details.setParentAction(action);
        HibernateFactory.getSession().save(details);

        ActionFactory.addServerToAction(minion1, action);

        saltServerActionService.setCommitTransaction(false);
        Map<LocalCall<?>, List<MinionSummary>> calls = saltServerActionService.callsForAction(action);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        assertEquals(1, calls.size());

        Map<String, Object> pillar = (Map<String, Object>)((Map<String, Object>)calls.keySet().stream().findFirst().get().getPayload().get("kwarg")).get("pillar");
        assertEquals("mgr_channels_new", pillar.get("_mgr_channels_items_name"));
        Map<String, Object> channels = (Map<String, Object>)pillar.get("mgr_channels_new");
        assertEquals(3, channels.size());
        assertTrue(channels.keySet().contains(base.getLabel()));
        assertTrue(channels.keySet().contains(ch1.getLabel()));
        assertTrue(channels.keySet().contains(ch2.getLabel()));

        assertTokenPillarValue(base, action, channels);
        assertTokenPillarValue(ch1, action, channels);
        assertTokenPillarValue(ch2, action, channels);

        action = (SubscribeChannelsAction)ActionFactory.lookupById(action.getId());

        assertEquals(3,  action.getDetails().getAccessTokens().size());
        assertTrue(action.getDetails().getAccessTokens().stream()
                .allMatch(token ->
                        token.getStart().toInstant().isAfter(now.toInstant()) &&
                        token.getStart().toInstant().isBefore(now.toInstant().plus(10, ChronoUnit.SECONDS))));
        assertTrue(action.getDetails().getAccessTokens().stream().allMatch(token -> !token.getValid()));
        assertTokenExists(base, action);
        assertTokenExists(ch1, action);
        assertTokenExists(ch2, action);
    }

    private void assertTokenExists(Channel channel, SubscribeChannelsAction action) {
        assertEquals(1, action.getDetails().getAccessTokens().stream()
                .filter(token -> token.getChannels().size() == 1 && token.getChannels().contains(channel)).count());
    }

    private void assertTokenPillarValue(Channel channel, SubscribeChannelsAction action, Map<String, Object> channels) {
        AccessToken tokenForChannel = action.getDetails().getAccessTokens().stream()
                .filter(token -> token.getChannels().contains(channel)).findFirst().get();
        assertEquals(tokenForChannel.getToken(), ((Map<String, Object>)channels.get(channel.getLabel())).get("token"));
    }

    private SaltServerActionService countSaltActionCalls(AtomicInteger counter) {
        SystemQuery systemQuery = new SaltService() {
            @Override
            public Optional<JsonElement> rawJsonCall(LocalCall<?> call, String minionId) {
                counter.incrementAndGet();
                throw new RuntimeException();
            }
        };
        return new SaltServerActionService(systemQuery);
    }

    /**
     * Tests that execution skips server actions which still have queued prerequisite
     * server actions but after the prerequisite is executed (= it's in either completed or
     * failed state), the dependant server action is not skipped anymore.
     *
     * @throws Exception if anything goes wrong
     */
    public void testSkipActionComplex() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        SaltServerActionService saltServerActionService = countSaltActionCalls(counter);
        successWorker();

        // prerequisite is still queued
        Action prereq = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction prereqServerAction = createChildServerAction(prereq, STATUS_QUEUED, 5L);

        // action is queued as well
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        action.setPrerequisite(prereq);
        ServerAction serverAction = createChildServerAction(action, STATUS_QUEUED, 5L);

        saltServerActionService.executeSSHAction(action, minion);

        // both status and remaining tries should remain unchanged
        assertEquals(STATUS_QUEUED, serverAction.getStatus());
        assertEquals(Long.valueOf(5L), serverAction.getRemainingTries());

        AtomicInteger counter2 = new AtomicInteger();
        SystemQuery systemQuery = new SaltService() {
            @Override
            public Optional<JsonElement> rawJsonCall(LocalCall<?> call, String minionId) {
                counter2.incrementAndGet();
                return Optional.of(new JsonObject());
            }
        };
        saltServerActionService = new SaltServerActionService(systemQuery);

        saltServerActionService.executeSSHAction(prereq, minion);
        assertEquals(STATUS_COMPLETED, prereqServerAction.getStatus());

        // 2nd try
        saltServerActionService.executeSSHAction(action, minion);
        assertEquals(STATUS_COMPLETED, serverAction.getStatus());

        assertEquals(0, counter.get());
        assertEquals(2, counter2.get());
    }

    /**
     * Tests that an attempt to execute action that has been already completed will not
     * invoke any salt calls and that the state of the action doesn't change.
     *
     * @throws Exception if anything goes wrong
     */
    public void testDontExecuteCompletedAction() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        SaltServerActionService saltServerActionService = countSaltActionCalls(counter);
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction serverAction = createChildServerAction(action, STATUS_COMPLETED, 5L);

        saltServerActionService.executeSSHAction(action, minion);

        assertEquals(STATUS_COMPLETED, serverAction.getStatus());
        assertEquals(Long.valueOf(5L), serverAction.getRemainingTries());
        assertEquals(0, counter.get());
    }

    private ServerAction createChildServerAction(Action action, ActionStatus status,
                                                 long remainingTries) throws Exception {
        ServerAction serverAction = ActionFactoryTest.createServerAction(minion, action);
        serverAction.setStatus(status);
        serverAction.setRemainingTries(remainingTries);
        action.setServerActions(Collections.singleton(serverAction));
        return serverAction;
    }

    /**
     * Tests that an attempt to execute action that has already failed will not
     * invoke any salt calls.
     *
     * @throws Exception if anything goes wrong
     */
    public void testDontExecuteFailedAction() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        SaltServerActionService saltServerActionService = countSaltActionCalls(counter);
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction serverAction = createChildServerAction(action, STATUS_FAILED, 5L);

        saltServerActionService.executeSSHAction(action, minion);

        assertEquals(STATUS_FAILED, serverAction.getStatus());
        assertEquals(Long.valueOf(5L), serverAction.getRemainingTries());
        assertEquals(0, counter.get());
    }

    /**
     * Tests that an action with a failed prerequisite will set be to the failed state
     * (with a corresponding message) and that it will not invoke any salt calls.
     *
     * @throws Exception if anything goes wrong
     */
    public void testDontExecuteActionWhenPrerequisiteFailed() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        SaltServerActionService saltServerActionService = countSaltActionCalls(counter);

        // prerequisite failed
        Action prereq = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        createChildServerAction(prereq, STATUS_FAILED, 0L);

        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        action.setPrerequisite(prereq);
        ServerAction serverAction = createChildServerAction(action, STATUS_QUEUED, 5L);

        saltServerActionService.executeSSHAction(action, minion);

        assertEquals(STATUS_FAILED, serverAction.getStatus());
        assertEquals("Prerequisite failed.", serverAction.getResultMsg());
        // this comes from the xmlrpc/queue.py
        assertEquals(Long.valueOf(-100L), serverAction.getResultCode());
        ActionFactory.getSession().flush();
        assertEquals(Long.valueOf(1L), action.getFailedCount());
        assertEquals(0, counter.get());
    }

    /**
     * Tests that the successful execution of an action correctly sets the status and the
     * number of remaining tries.
     *
     * @throws Exception if anything goes wrong
     */
    public void testExecuteActionSuccess() throws Exception {
        successWorker();

        // create action without servers
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction serverAction = createChildServerAction(action, STATUS_QUEUED, 5L);

        saltServerActionService.executeSSHAction(action, minion);

        assertEquals(Long.valueOf(4L), serverAction.getRemainingTries());
        assertEquals(STATUS_COMPLETED, serverAction.getStatus());
    }

    /**
     * Tests that an execution with empty result from salt keeps the action in the queued
     * state and decreases the number of tries.
     *
     * @throws Exception if anything goes wrong
     */
    public void testActionFailedOnEmptyResult() throws Exception {
        // expect salt returning empty result

        SystemQuery systemQuery = new SaltService() {
            @Override
            public Optional<JsonElement> rawJsonCall(LocalCall<?> call, String minionId) {
                return Optional.empty();
            }
        };
        SaltServerActionService saltServerActionService = new SaltServerActionService(systemQuery);

        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction serverAction = createChildServerAction(action, STATUS_QUEUED, 5L);

        saltServerActionService.executeSSHAction(action, minion);

        assertEquals(STATUS_FAILED, serverAction.getStatus());
        assertEquals("Minion is down or could not be contacted.", serverAction.getResultMsg());
    }

    /**
     * Tests that an execution with exception from salt keeps the action in the queued
     * state and decreases the number of tries.
     *
     * @throws Exception if anything goes wrong
     */
    public void testActionFailedOnException() throws Exception {
        // expect salt returning empty result

        SystemQuery systemQuery = new SaltService() {
            @Override
            public Optional<JsonElement> rawJsonCall(LocalCall<?> call, String minionId) {
                throw new RuntimeException();
            }
        };
        SaltServerActionService saltServerActionService = new SaltServerActionService(systemQuery);
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction serverAction = createChildServerAction(action, STATUS_QUEUED, 5L);
        try {
            saltServerActionService.executeSSHAction(action, minion);
        } catch (RuntimeException e) {
            // expected
            assertEquals(STATUS_FAILED, serverAction.getStatus());
            assertTrue(serverAction.getResultMsg().startsWith("Error calling Salt: "));
            return;
        }
        fail("Runtime exception should have been thrown.");
    }

    /**
     * Tests that a successful execution of a reboot action will move this action to the
     * 'picked-up' state and the remaining tries counter decreases.
     *
     * @throws Exception if anything goes wrong
     */
    public void testRebootActionIsPickedUp() throws Exception {
        successWorker();
        SystemQuery systemQuery = new SaltService() {
            @Override
            public Optional<JsonElement> rawJsonCall(LocalCall<?> call, String minionId) {
                return Optional.of(new JsonObject());
            }
        };
        SaltServerActionService saltServerActionService = new SaltServerActionService(systemQuery);

        Action action = createRebootAction(new Date(1L));
        ServerAction serverAction = createChildServerAction(action, STATUS_QUEUED, 5L);

        saltServerActionService.executeSSHAction(action, minion);

        assertEquals(STATUS_PICKED_UP, serverAction.getStatus());
        assertEquals(Long.valueOf(4L), serverAction.getRemainingTries());
    }

    private Action createRebootAction(Date earliestAction) {
        Action action = ActionFactory.createAction(ActionFactory.TYPE_REBOOT);
        action.setOrg(user.getOrg());
        action.setEarliestAction(earliestAction);
        return action;
    }

    /**
     * Tests that execution skips server actions which still have queued prerequisite
     * server actions.
     *
     * @throws Exception if anything goes wrong
     */
    public void testSkipActionWhenPrerequisiteQueued() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        SaltServerActionService saltServerActionService = countSaltActionCalls(counter);
        successWorker();

        // prerequisite is still queued
        Action prereq = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction prereqServerAction = createChildServerAction(prereq, STATUS_QUEUED, 5L);
        prereq.setServerActions(Collections.singleton(prereqServerAction));

        // action is queued as well
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        action.setPrerequisite(prereq);
        ServerAction serverAction = createChildServerAction(action, STATUS_QUEUED, 5L);

        saltServerActionService.executeSSHAction(action, minion);

        // both status and remaining tries should remain unchanged
        assertEquals(STATUS_QUEUED, serverAction.getStatus());
        assertEquals(Long.valueOf(5L), serverAction.getRemainingTries());
        assertEquals(0, counter.get());
    }

    private void assertActionWillBeRetried() throws Exception {
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction serverAction = createChildServerAction(action, STATUS_QUEUED, 5L);

        saltServerActionService.executeSSHAction(action, minion);

        assertEquals(Long.valueOf(4L), serverAction.getRemainingTries());
        assertEquals(STATUS_QUEUED, serverAction.getStatus());
    }

    private void successWorker() throws IOException {
        SaltUtils saltUtils = new SaltUtils() {
            @Override
            public boolean shouldRefreshPackageList(String function,
                                                    Optional<JsonElement> callResult) {
                return false;
            }

            @Override
            public void updateServerAction(ServerAction serverAction, long retcode,
                                           boolean success, String jid, JsonElement jsonResult, String function) {
                serverAction.setStatus(STATUS_COMPLETED);
            }
        };
        saltUtils.setScriptsDir(Files.createTempDirectory("actionscripts"));
        saltServerActionService.setSaltUtils(saltUtils);
    }
}
