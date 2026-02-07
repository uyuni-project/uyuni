/*
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainEntry;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ansible.PlaybookAction;
import com.redhat.rhn.domain.action.ansible.PlaybookActionDetails;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsAction;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsActionDetails;
import com.redhat.rhn.domain.action.config.ConfigAction;
import com.redhat.rhn.domain.action.script.ScriptActionDetails;
import com.redhat.rhn.domain.action.script.ScriptRunAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.errata.AdvisoryStatus;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.test.ErrataFactoryTest;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.PackageType;
import com.redhat.rhn.domain.rhnpackage.test.PackageEvrFactoryTest;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.ConfigTestUtils;
import com.redhat.rhn.testing.ErrataTestUtils;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.reactor.messaging.JobReturnEventMessage;
import com.suse.manager.reactor.messaging.JobReturnEventMessageAction;
import com.suse.manager.utils.SaltKeyUtils;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.controllers.utils.ContactMethodUtil;
import com.suse.manager.webui.services.SaltActionChainGeneratorService;
import com.suse.manager.webui.services.SaltServerActionService;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.SaltModuleRun;
import com.suse.manager.webui.utils.SaltState;
import com.suse.manager.webui.utils.SaltSystemReboot;
import com.suse.salt.netapi.calls.LocalAsyncResult;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.datatypes.Event;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.datatypes.target.Target;
import com.suse.salt.netapi.event.JobReturnEvent;
import com.suse.salt.netapi.parser.JsonParser;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.utils.Xor;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class SaltServerActionServiceTest extends JMockBaseTestCaseWithUser {

    private MinionServer minion;
    private SaltServerActionService saltServerActionService;
    private TaskomaticApi taskomaticMock;
    private SaltService saltService;

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);

        saltService = new SaltService() {
            @Override
            public Optional<Result<JsonElement>> rawJsonCall(LocalCall<?> call, String minionId) {
                return Optional.of(Result.success(new JsonObject()));
            }

            @Override
            public void refreshPillar(MinionList minionList) {
                //do nothing
            }
        };
        minion = MinionServerFactoryTest.createTestMinionServer(user);
        saltServerActionService = createSaltServerActionService(saltService, saltService);

        taskomaticMock = mock(TaskomaticApi.class);
        saltServerActionService.setTaskomaticApi(taskomaticMock);
    }

    private SaltServerActionService createSaltServerActionService(SystemQuery systemQuery, SaltApi saltApi) {
        SaltUtils saltUtils = new SaltUtils(systemQuery, saltApi);
        SaltServerActionService service = new SaltServerActionService(saltApi, saltUtils, new SaltKeyUtils(saltApi));
        ScriptRunAction.setSkipCommandScriptPerms(true);
        return service;
    }

    @Test
    public void testPackageUpdate() throws Exception {
        MinionServer testMinionServer = MinionServerFactoryTest.createTestMinionServer(user);
        List<MinionServer> mins = new ArrayList<>();
        mins.add(testMinionServer);

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
        Action action = ActionFactory.createAndSaveAction(ActionFactory.TYPE_PACKAGES_UPDATE, user,
                "test action", Date.from(now.toInstant()));

        ActionFactory.addServerToAction(testMinionServer, action);

        ActionManager.addPackageActionDetails(Arrays.asList(action), packageMaps);
        TestUtils.flushAndEvict(action);
        Action updateAction = ActionFactory.lookupById(action.getId());

        Map<LocalCall<?>, List<MinionSummary>> result = saltServerActionService.callsForAction(
                updateAction, minionSummaries);
        assertEquals(1, result.values().size());
        assertStateApplyWithPillar("packages.pkginstall", null, null, result.keySet().iterator().next());
        MinionSummary minionSummary = result.values().iterator().next().iterator().next();
        assertEquals(new MinionSummary(testMinionServer), minionSummary);
    }

    @Test
    public void testPackageFullUpdate() throws Exception {
        MinionServer testMinionServer = MinionServerFactoryTest.createTestMinionServer(user);
        List<MinionServer> mins = new ArrayList<>();
        mins.add(testMinionServer);

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
        Action action = ActionFactory.createAndSaveAction(ActionFactory.TYPE_PACKAGES_UPDATE, user,
                "test action", Date.from(now.toInstant()));

        ActionFactory.addServerToAction(testMinionServer, action);

        TestUtils.flushAndEvict(action);
        Action updateAction = ActionFactory.lookupById(action.getId());

        Map<LocalCall<?>, List<MinionSummary>> result = saltServerActionService.callsForAction(
                updateAction, minionSummaries);
        assertEquals(1, result.values().size());
        assertStateApplyWithPillar("packages.pkgupdate", null, null, result.keySet().iterator().next());
        MinionSummary minionSummary = result.values().iterator().next().iterator().next();
        assertEquals(new MinionSummary(testMinionServer), minionSummary);
    }

    @Test
    public void testRetractedPackageInstall() throws Exception {
        MinionServer testMinionServer = MinionServerFactoryTest.createTestMinionServer(user);
        List<MinionServer> mins = new ArrayList<>();
        mins.add(testMinionServer);

        List<MinionSummary> minionSummaries = mins.stream().
                map(MinionSummary::new).collect(Collectors.toList());

        Channel channel = ChannelFactoryTest.createTestChannel(user);
        SystemManager.subscribeServerToChannel(user, testMinionServer, channel);
        Package p64 = ErrataTestUtils.createTestPackage(user, channel, "x86_64");
        Errata retracted = ErrataFactoryTest.createTestErrata(null);
        retracted.addChannel(channel);
        retracted.setAdvisoryStatus(AdvisoryStatus.RETRACTED);
        Package p32 = ErrataTestUtils.createLaterTestPackage(user, retracted, channel, p64);
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
        Action action = ActionFactory.createAndSaveAction(ActionFactory.TYPE_PACKAGES_UPDATE, user,
                "test action", Date.from(now.toInstant()));

        ActionFactory.addServerToAction(testMinionServer, action);

        ActionManager.addPackageActionDetails(Arrays.asList(action), packageMaps);
        TestUtils.flushAndEvict(action);
        Action updateAction = ActionFactory.lookupById(action.getId());

        Map<LocalCall<?>, List<MinionSummary>> result = saltServerActionService.callsForAction(
                updateAction, minionSummaries);
        assertEquals(1, result.values().size());
        List<MinionSummary> summaries = result.values().iterator().next();
        assertTrue(summaries.isEmpty());
        ServerAction serverAction = HibernateFactory.reload(action.getServerActions().iterator().next());
        assertTrue(serverAction.isStatusFailed());
    }

    @Test
    public void testPackageRemoveDebian() throws Exception {
        MinionServer testMinionServer = MinionServerFactoryTest.createTestMinionServer(user);
        testMinionServer.setServerArch(ServerFactory.lookupServerArchByLabel("amd64-debian-linux"));
        List<MinionServer> mins = new ArrayList<>();
        mins.add(testMinionServer);

        List<MinionSummary> minionSummaries = mins.stream().
                map(MinionSummary::new).collect(Collectors.toList());

        Channel channel = ChannelFactoryTest.createTestChannel(user);
        Package p = ErrataTestUtils.createTestPackage(user, channel, "amd64-deb");
        p.setPackageEvr(PackageEvrFactoryTest.createTestPackageEvr(null, "1.0.0", "X", PackageType.DEB));

        Map<String, Long> pkgMap = new HashMap<>();
        pkgMap.put("name_id", p.getPackageName().getId());
        pkgMap.put("evr_id", p.getPackageEvr().getId());
        pkgMap.put("arch_id", p.getPackageArch().getId());

        final ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        Action action = ActionFactory.createAndSaveAction(ActionFactory.TYPE_PACKAGES_UPDATE, user,
                "test action", Date.from(now.toInstant()));

        ActionFactory.addServerToAction(testMinionServer, action);

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

    @Test
    public void testPackageUpdateDebian() throws Exception {
        MinionServer testMinionServer = MinionServerFactoryTest.createTestMinionServer(user);
        testMinionServer.setServerArch(ServerFactory.lookupServerArchByLabel("amd64-debian-linux"));
        List<MinionServer> mins = new ArrayList<>();
        mins.add(testMinionServer);

        List<MinionSummary> minionSummaries = mins.stream().
                map(MinionSummary::new).collect(Collectors.toList());

        Channel channel = ChannelFactoryTest.createTestChannel(user);
        Package p1 = ErrataTestUtils.createTestPackage(user, channel, "amd64-deb");
        p1.setPackageEvr(PackageEvrFactoryTest.createTestPackageEvr(null, "1.0.0", "X", PackageType.DEB));
        Package p2 = ErrataTestUtils.createTestPackage(user, channel, "amd64-deb");
        p2.setPackageEvr(PackageEvrFactoryTest.createTestPackageEvr("1", "1.2", "1ubuntu1", PackageType.DEB));

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
        Action action = ActionFactory.createAndSaveAction(ActionFactory.TYPE_PACKAGES_UPDATE, user,
                "test action", Date.from(now.toInstant()));

        ActionFactory.addServerToAction(testMinionServer, action);

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

    @Test
    public void testPackageRemoveDuplicates() throws Exception {
        MinionServer testMinion = MinionServerFactoryTest.createTestMinionServer(user);
        List<MinionServer> mins = new ArrayList<>();
        mins.add(testMinion);

        List<MinionSummary> minionSummaries = mins.stream().
                map(MinionSummary::new).collect(Collectors.toList());

        Channel channel = ChannelFactoryTest.createTestChannel(user);
        Package p1 = ErrataTestUtils.createTestPackage(user, channel, "x86_64");
        Package p2 = ErrataTestUtils.createTestPackage(user, channel, "x86_64");
        p1.getPackageName().setName("test-package-duplicated-name");
        p2.setPackageName(p1.getPackageName());
        p1.setPackageEvr(PackageEvrFactoryTest.createTestPackageEvr(null, "1.0.0", "X", PackageType.RPM));
        p2.setPackageEvr(PackageEvrFactoryTest.createTestPackageEvr(null, "1.0.1", "X", PackageType.RPM));

        List<Map<String, Long>> packageMaps = new ArrayList<>();
        Map<String, Long> p1map = new HashMap<>();
        p1map.put("name_id", p1.getPackageName().getId());
        p1map.put("evr_id", p1.getPackageEvr().getId());
        p1map.put("arch_id", p1.getPackageArch().getId());
        packageMaps.add(p1map);
        Map<String, Long> p2map = new HashMap<>();
        p2map.put("name_id", p2.getPackageName().getId());
        p2map.put("evr_id", p2.getPackageEvr().getId());
        p2map.put("arch_id", p2.getPackageArch().getId());
        packageMaps.add(p2map);

        final ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        Action action = ActionFactory.createAndSaveAction(ActionFactory.TYPE_PACKAGES_REMOVE, user,
                "test remove action", Date.from(now.toInstant()));

        ActionFactory.addServerToAction(testMinion, action);

        ActionManager.addPackageActionDetails(Arrays.asList(action), packageMaps);
        TestUtils.flushAndEvict(action);
        Action removeAction = ActionFactory.lookupById(action.getId());

        Map<LocalCall<?>, List<MinionSummary>> result =
                saltServerActionService.callsForAction(removeAction, minionSummaries);
        assertEquals(1, result.values().size());
        LocalCall<?> resultCall = result.keySet().iterator().next();
        List<List<String>> resultPkgs1 = (List<List<String>>) ((Map) ((Map) resultCall.getPayload().get("kwarg")).get(
                "pillar")).get("param_pkgs");
        List<List<String>> resultPkgs2 = (List<List<String>>) ((Map) ((Map) resultCall.getPayload().get("kwarg")).get(
                "pillar")).get("param_pkgs_duplicates");

        List<String> resultPkg1 =
                resultPkgs1.stream().filter(pkg -> p1.getPackageName().getName().equals(pkg.get(0))).findFirst().get();
        List<String> resultPkg2 =
                resultPkgs2.stream().filter(pkg -> p2.getPackageName().getName().equals(pkg.get(0))).findFirst().get();

        // Assert packages are sent to Salt correctly
        assertEquals(1, resultPkgs1.size());
        assertEquals(1, resultPkgs2.size());
        assertEquals("x86_64", resultPkg1.get(1));
        assertEquals("x86_64", resultPkg2.get(1));
        assertTrue(Arrays.asList("1.0.0", "1.0.1").contains(resultPkg1.get(2)));
        assertTrue(Arrays.asList("1.0.0", "1.0.1").contains(resultPkg2.get(2)));
        assertNotSame(resultPkg1.get(2), resultPkg2.get(2));
    }

    @Test
    public void testDeployFiles() {
        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer minion2 = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer minion3 = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer minion4 = MinionServerFactoryTest.createTestMinionServer(user);

        final ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
        ConfigAction configAction = (ConfigAction) ActionFactory.createAction(ActionFactory.TYPE_CONFIGFILES_DEPLOY,
                user, Date.from(now.toInstant()));

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

    @SuppressWarnings("unchecked")
    private void assertStateApplyWithPillar(String expectedState, String pillarEntry,
                                            Object pillarValue, LocalCall<?> call) {
        assertEquals("state", call.getModuleName());
        assertEquals("apply", call.getFunctionName());
        Map<String, Object> kwargs = ((Map<String, Object>)call.getPayload().get("kwarg"));
        assertTrue(((List<String>)kwargs.get("mods")).contains(expectedState),
                "State does not call: " + expectedState);
        if (pillarEntry != null) {
            assertEquals(pillarValue, ((Map<String, Object>)kwargs.get("pillar")).get(pillarEntry));
        }
    }

    private Set<Action> testHighstateActions;
    private ActionChain testActionChain;
    private MinionServer testMinion1;
    private MinionServer testMinion2;
    private Server testServer1;
    private SaltUtils testSaltUtils;

    @Test
    public void testExecuteActionChain() throws Exception {
        SystemQuery systemQuery = new TestSystemQuery();
        SaltApi saltApi = new TestSaltApi();
        testSaltUtils = new SaltUtils(systemQuery, saltApi);
        testSaltUtils.setScriptsDir(Files.createTempDirectory("actionscripts"));

        SaltActionChainGeneratorService generatorService = new SaltActionChainGeneratorService() {
            @Override
            public Map<MinionSummary, Integer> createActionChainSLSFiles(ActionChain actionChain,
                        MinionSummary minionServer, List<SaltState> states, Optional<String> extraFileRefs) {
                assertEquals(3, states.size());
                SaltModuleRun scriptRun = (SaltModuleRun)states.get(0);
                SaltSystemReboot reboot = (SaltSystemReboot)states.get(1);
                SaltModuleRun highstate = (SaltModuleRun)states.get(2);

                long scriptActionId = actionChain.getEntries().stream()
                        .filter(ace -> ace.getServerId().equals(minionServer.getServerId()) &&
                                ace.getAction().getActionType().equals(ActionFactory.TYPE_SCRIPT_RUN))
                        .map(ActionChainEntry::getActionId)
                        .findFirst().get();
                long rebootActionId = actionChain.getEntries().stream()
                        .filter(ace -> ace.getServerId().equals(minionServer.getServerId()) &&
                                ace.getAction().getActionType().equals(ActionFactory.TYPE_REBOOT))
                        .map(ActionChainEntry::getActionId)
                        .findFirst().get();
                long highstateActionId = actionChain.getEntries().stream()
                        .filter(ace -> ace.getServerId().equals(minionServer.getServerId()) &&
                                ace.getAction().getActionType().equals(ActionFactory.TYPE_APPLY_STATES))
                        .map(ActionChainEntry::getActionId)
                        .findFirst().get();
                assertEquals(SaltActionChainGeneratorService.ACTION_STATE_ID_PREFIX + actionChain.getId() +
                                "_action_" + scriptActionId,
                        scriptRun.getId());
                assertEquals(SaltActionChainGeneratorService.ACTION_STATE_ID_PREFIX + actionChain.getId() +
                                "_action_" + rebootActionId,
                        reboot.getId());
                assertEquals(SaltActionChainGeneratorService.ACTION_STATE_ID_PREFIX + actionChain.getId() +
                                "_action_" + highstateActionId,
                        highstate.getId());

                assertEquals(true, scriptRun.getKwargs().get("queue"));
                assertEquals(true, highstate.getKwargs().get("queue"));
                return null;
            }
        };

        saltServerActionService.setSaltActionChainGeneratorService(generatorService);
        ActionChainFactory.setTaskomaticApi(taskomaticMock);

        testMinion1 = MinionServerFactoryTest.createTestMinionServer(user);
        SystemManager.giveCapability(testMinion1.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);

        testMinion2 = MinionServerFactoryTest.createTestMinionServer(user);
        SystemManager.giveCapability(testMinion2.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);

        testServer1 = ServerFactoryTest.createTestServer(user, false,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
        SystemManager.giveCapability(testServer1.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);

        String label = TestUtils.randomString();
        testActionChain = ActionChainFactory.createActionChain(label, user);


        Date earliestAction = new Date();

        ScriptActionDetails sad = ActionFactory.createScriptActionDetails(
                "root", "root", 10L, "#!/bin/csh\necho hello");
        ActionChainManager.scheduleScriptRuns(user,
                Arrays.asList(testMinion1.getId(), testMinion2.getId(), testServer1.getId()),
                "script", sad, earliestAction, testActionChain);

        Set<Long> allServerIds = new HashSet<>();
        Collections.addAll(allServerIds, testMinion1.getId(), testMinion2.getId(), testServer1.getId());

        ActionChainManager.scheduleRebootActions(user, allServerIds, earliestAction, testActionChain);

        testHighstateActions = ActionChainManager.scheduleApplyStates(user,
                Arrays.asList(testMinion1.getId(), testMinion2.getId()), Optional.empty(),
                earliestAction, testActionChain);

        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleActionChainExecution(with(any(ActionChain.class)));
        } });

        ActionChainFactory.schedule(testActionChain, earliestAction);

        saltServerActionService.executeActionChain(testActionChain.getId());
    }

    private Event getJobReturnEvent(String filename, long actionId, Map<String, String> placeholders) throws Exception {
        Path path = new File(TestUtils.findTestData(
                "/com/suse/manager/reactor/messaging/test/" + filename).getPath()).toPath();
        String eventString = Files.lines(path)
                .collect(Collectors.joining("\n"))
                .replaceAll("\"suma-action-id\": \\d+", "\"suma-action-id\": " + actionId);
        if (placeholders != null) {
            for (Map.Entry<String, String> entries : placeholders.entrySet()) {
                String placeholder = entries.getKey();
                String value = entries.getValue();
                eventString = StringUtils.replace(eventString, placeholder, value);
            }
        }

        JsonParser<Event> eventsJsonParser = new JsonParser<>(new TypeToken<>() {
        });
        return eventsJsonParser.parse(eventString);
    }

    @Test
    public void testExecuteActionChainWithJobReturnEvent() throws Exception {
        //setup: do the same operations as in testExecuteActionChain
        testExecuteActionChain();

        //then inspire from JobReturnEventMessageActionTest.testActionChainPackageRefreshNeeded
        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("${minion-id}", minion.getMinionId());

        long highstateFirstActionId = testHighstateActions.stream()
                .findFirst()
                .map(Action::getId)
                .orElse(0L);

        placeholders.put("${action1-id}", highstateFirstActionId + "");
        placeholders.put("${actionchain-id}", testActionChain.getId() + "");

        Optional<JobReturnEvent> event = JobReturnEvent.parse(
                getJobReturnEvent("action.chain.job.return.json", highstateFirstActionId, placeholders));
        JobReturnEventMessage message = new JobReturnEventMessage(event.get());

        //Process the event message
        JobReturnEventMessageAction messageAction = new JobReturnEventMessageAction(saltServerActionService,
                testSaltUtils);
        messageAction.execute(message);

        assertEquals(3, ActionFactory.listActionsForServer(user, testMinion1).size(),
                "3 actions have been scheduled for minion 1");
        assertEquals(3, ActionFactory.listActionsForServer(user, testMinion2).size(),
                "3 actions have been scheduled for minion 2");
        assertEquals(2, ActionFactory.listActionsForServer(user, testServer1).size(),
                "2 actions have been scheduled for server 1");
    }

    @Test
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
        SubscribeChannelsAction action = (SubscribeChannelsAction) ActionFactory.createAndSaveAction(
                ActionFactory.TYPE_SUBSCRIBE_CHANNELS, user, "Subscribe to channels", Date.from(now.toInstant()));
        action.setSaltApi(saltService);

        SubscribeChannelsActionDetails details = new SubscribeChannelsActionDetails();
        details.setBaseChannel(base);
        details.setChannels(Arrays.asList(ch1, ch2).stream().collect(Collectors.toSet()));
        action.setDetails(details);
        details.setParentAction(action);
        HibernateFactory.getSession().persist(details);

        ActionFactory.addServerToAction(minion1, action);

        Map<LocalCall<?>, List<MinionSummary>> calls = saltServerActionService.callsForAction(action);

        HibernateFactory.getSession().flush();
        HibernateFactory.getSession().clear();

        assertEquals(1, calls.size());

        Map<String, Object> payload = calls.keySet().stream()
                .findFirst()
                .get().getPayload();

        assertEquals("state.apply", payload.get("fun"));
        assertEquals("channels", ((List<String>) ((Map<String, Object>) payload.get("kwarg")).get("mods")).get(0));

        minion1 = TestUtils.reload(minion1);
        assertEquals(3, minion1.getChannels().size());
        assertEquals(base.getId(), minion1.getBaseChannel().getId());
        assertEquals(2, minion1.getChildChannels().size());
        assertTrue(minion1.getChildChannels().stream().anyMatch(cc -> cc.getId().equals(ch1.getId())));
        assertTrue(minion1.getChildChannels().stream().anyMatch(cc -> cc.getId().equals(ch2.getId())));

        assertEquals(3, minion1.getAccessTokens().size());
        assertTokenChannel(minion1, base);
        assertTokenChannel(minion1, ch1);
        assertTokenChannel(minion1, ch2);

        // teardown
        cleanupServers(minion, minion1);
    }

    private void assertTokenChannel(MinionServer minionIn, Channel channel) {
        assertTrue(minionIn.getAccessTokens().stream()
                .anyMatch(token -> token.getChannels().size() == 1 && token.getChannels().contains(channel)),
                channel.getLabel());
    }

    private SaltServerActionService countSaltActionCalls(AtomicInteger counter) {
        SaltApi saltApi = new TestSaltApi() {
            @Override
            public Optional<Result<JsonElement>> rawJsonCall(LocalCall<?> call, String minionId) {
                counter.incrementAndGet();
                throw new RuntimeException();
            }
        };
        return createSaltServerActionService(new TestSystemQuery(), saltApi);
    }

    /**
     * Tests that execution skips server actions which still have queued prerequisite
     * server actions but after the prerequisite is executed (= it's in either completed or
     * failed state), the dependant server action is not skipped anymore.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testSkipActionComplex() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        SaltServerActionService testService = countSaltActionCalls(counter);
        successWorker();

        // prerequisite is still queued
        Action prereq = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction prereqServerAction = createChildServerAction(prereq, ServerAction::setStatusQueued, 5L);

        // action is queued as well
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        action.setPrerequisite(prereq);
        ServerAction serverAction = createChildServerAction(action, ServerAction::setStatusQueued, 5L);

        testService.executeSSHAction(action, minion);

        // both status and remaining tries should remain unchanged
        assertTrue(serverAction.isStatusQueued());
        assertEquals(Long.valueOf(5L), serverAction.getRemainingTries());

        AtomicInteger counter2 = new AtomicInteger();
        SaltApi saltApi = new TestSaltApi() {
            @Override
            public Optional<Result<JsonElement>> rawJsonCall(LocalCall<?> call, String minionId) {
                counter2.incrementAndGet();
                return Optional.of(Result.success(new JsonObject()));
            }
        };
        testService = createSaltServerActionService(new TestSystemQuery(), saltApi);

        testService.executeSSHAction(prereq, minion);
        assertTrue(prereqServerAction.isStatusCompleted());

        // 2nd try
        testService.executeSSHAction(action, minion);
        assertTrue(serverAction.isStatusCompleted());

        assertEquals(0, counter.get());
        assertEquals(2, counter2.get());
    }

    /**
     * Tests that an attempt to execute action that has been already completed will not
     * invoke any salt calls and that the state of the action doesn't change.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testDontExecuteCompletedAction() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        SaltServerActionService testService = countSaltActionCalls(counter);
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction serverAction = createChildServerAction(action, ServerAction::setStatusCompleted, 5L);

        testService.executeSSHAction(action, minion);

        assertTrue(serverAction.isStatusCompleted());
        assertEquals(Long.valueOf(5L), serverAction.getRemainingTries());
        assertEquals(0, counter.get());
    }

    private ServerAction createChildServerAction(Action action, Consumer<ServerAction> statusSetter,
                                                 long remainingTries) {
        return createChildServerAction(action, statusSetter, minion, remainingTries);
    }

    private ServerAction createChildServerAction(Action action, Consumer<ServerAction> statusSetter,
                                                 MinionServer minionIn,
                                                 long remainingTries) {
        ServerAction serverAction = ActionFactoryTest.createServerAction(minionIn, action);
        statusSetter.accept(serverAction);
        serverAction.setRemainingTries(remainingTries);
        if (action.getServerActions() == null) {
            Set<ServerAction> set = new HashSet<>();
            set.add(serverAction);
            action.setServerActions(set);
        }
        else {
            action.getServerActions().add(serverAction);
        }
        return serverAction;
    }

    /**
     * Tests that an attempt to execute action that has already failed will not
     * invoke any salt calls.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testDontExecuteFailedAction() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        SaltServerActionService testService = countSaltActionCalls(counter);
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction serverAction = createChildServerAction(action, ServerAction::setStatusFailed, 5L);

        testService.executeSSHAction(action, minion);

        assertTrue(serverAction.isStatusFailed());
        assertEquals(Long.valueOf(5L), serverAction.getRemainingTries());
        assertEquals(0, counter.get());
    }

    /**
     * Tests that an action with a failed prerequisite will set be to the failed state
     * (with a corresponding message) and that it will not invoke any salt calls.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testDontExecuteActionWhenPrerequisiteFailed() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        SaltServerActionService testService = countSaltActionCalls(counter);

        // prerequisite failed
        Action prereq = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        createChildServerAction(prereq, ServerAction::setStatusFailed, 0L);

        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        action.setPrerequisite(prereq);
        ServerAction serverAction = createChildServerAction(action, ServerAction::setStatusQueued, 5L);

        testService.executeSSHAction(action, minion);

        assertTrue(serverAction.isStatusFailed());
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
    @Test
    public void testExecuteActionSuccess() throws Exception {
        successWorker();

        // create action without servers
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction serverAction = createChildServerAction(action, ServerAction::setStatusQueued, 5L);

        saltServerActionService.executeSSHAction(action, minion);

        assertEquals(Long.valueOf(4L), serverAction.getRemainingTries());
        assertTrue(serverAction.isStatusCompleted());
    }

    /**
     * Tests that an execution with empty result from salt keeps the action in the queued
     * state and decreases the number of tries.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testActionFailedOnEmptyResult() throws Exception {
        // expect salt returning empty result

        SaltApi saltApi = new TestSaltApi() {
            @Override
            public Optional<Result<JsonElement>> rawJsonCall(LocalCall<?> call, String minionId) {
                return Optional.empty();
            }
        };
        SaltServerActionService testService = createSaltServerActionService(new TestSystemQuery(), saltApi);

        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction serverAction = createChildServerAction(action, ServerAction::setStatusQueued, 5L);

        testService.executeSSHAction(action, minion);

        assertTrue(serverAction.isStatusFailed());
        assertEquals("Minion is down or could not be contacted.", serverAction.getResultMsg());
    }

    /**
     * Tests that an execution with exception from salt keeps the action in the queued
     * state and decreases the number of tries.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testActionFailedOnException() throws Exception {
        // expect salt returning empty result

        SaltApi saltApi = new TestSaltApi() {
            @Override
            public Optional<Result<JsonElement>> rawJsonCall(LocalCall<?> call, String minionId) {
                throw new RuntimeException();
            }
        };
        SaltServerActionService testService = createSaltServerActionService(new TestSystemQuery(), saltApi);
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction serverAction = createChildServerAction(action, ServerAction::setStatusQueued, 5L);
        try {
            testService.executeSSHAction(action, minion);
        }
        catch (RuntimeException e) {
            fail("Runtime exception should not have been thrown.");
        }

        assertTrue(serverAction.isStatusFailed());
        assertTrue(serverAction.getResultMsg().startsWith("Error calling Salt: "));
    }


    /**
     * Tests that a successful execution of a reboot action will move this action to the
     * 'picked-up' state and the remaining tries counter decreases.
     *
     * @throws Exception if anything goes wrong
     */
    @Test
    public void testRebootActionIsPickedUp() throws Exception {
        successWorker();
        SaltApi saltApi = new TestSaltApi() {
            @Override
            public Optional<Result<JsonElement>> rawJsonCall(LocalCall<?> call, String minionId) {
                return Optional.of(Result.success(new JsonObject()));
            }
        };
        SaltServerActionService testService = createSaltServerActionService(new TestSystemQuery(), saltApi);

        Action action = createRebootAction(new Date(1L));
        ServerAction serverAction = createChildServerAction(action, ServerAction::setStatusQueued, 5L);

        testService.executeSSHAction(action, minion);

        assertTrue(serverAction.isStatusPickedUp());
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
    @Test
    public void testSkipActionWhenPrerequisiteQueued() throws Exception {
        AtomicInteger counter = new AtomicInteger();
        SaltServerActionService testService = countSaltActionCalls(counter);
        successWorker();

        // prerequisite is still queued
        Action prereq = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction prereqServerAction = createChildServerAction(prereq, ServerAction::setStatusQueued, 5L);
        prereq.setServerActions(Collections.singleton(prereqServerAction));

        // action is queued as well
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        action.setPrerequisite(prereq);
        ServerAction serverAction = createChildServerAction(action, ServerAction::setStatusQueued, 5L);

        testService.executeSSHAction(action, minion);

        // both status and remaining tries should remain unchanged
        assertTrue(serverAction.isStatusQueued());
        assertEquals(Long.valueOf(5L), serverAction.getRemainingTries());
        assertEquals(0, counter.get());
    }

    @Test
    public void testExectueSSHAction() throws Exception {
        MinionServer testMinionServer = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer sshMinion = MinionServerFactoryTest.createTestMinionServer(user);
        sshMinion.setContactMethod(ServerFactory.findContactMethodByLabel(ContactMethodUtil.SSH_PUSH));
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_REBOOT);

        // set the auto generated server action to failed
        action.getServerActions().forEach(sa -> sa.fail("not needed"));
        action.setServerActions(null);

        createChildServerAction(action, ServerAction::setStatusQueued, sshMinion, 5L);
        createChildServerAction(action, ServerAction::setStatusQueued, testMinionServer, 5L);
        HibernateFactory.getSession().flush();

        SaltService saltServiceMock = mock(SaltService.class);
        SaltServerActionService testService = createSaltServerActionService(saltServiceMock, saltServiceMock);
        testService.setTaskomaticApi(taskomaticMock);
        context().checking(new Expectations() { {
            oneOf(taskomaticMock).scheduleSSHActionExecution(action, sshMinion, false);
            oneOf(saltServiceMock).callAsync(
                    with(any(LocalCall.class)), with(any(Target.class)), with(any(Optional.class)));
            LocalAsyncResult<?> result = new LocalAsyncResult() {
                @Override
                public List<String> getMinions() {
                    return Arrays.asList(testMinionServer.getMinionId());
                }
            };
            will(returnValue(Optional.of(result)));
        } });

        testService.execute(action, false, false, Optional.empty());

    }

    @Test
    public void testDoNotReExecuteDoneActions() throws Exception {
        MinionServer firstMinion = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer secondMinion = MinionServerFactoryTest.createTestMinionServer(user);

        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_REBOOT);

        // set the auto generated server action to failed
        action.getServerActions().forEach(sa -> sa.fail("not needed"));
        action.setServerActions(null);
        createChildServerAction(action, ServerAction::setStatusCompleted, firstMinion, 5L);
        createChildServerAction(action, ServerAction::setStatusQueued, secondMinion, 5L);

        HibernateFactory.getSession().flush();

        SaltService saltServiceMock = mock(SaltService.class);
        SaltServerActionService testService = createSaltServerActionService(saltServiceMock, saltServiceMock);
        testService.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            oneOf(saltServiceMock).callAsync(
                with(any(LocalCall.class)),
                with((new MinionListMatcher(List.of(secondMinion.getMinionId())))),
                with(any(Optional.class))
            );
            LocalAsyncResult<?> result = new LocalAsyncResult<>() {
                @Override
                public List<String> getMinions() {
                    return List.of(secondMinion.getMinionId());
                }
            };
            will(returnValue(Optional.of(result)));
        } });

        testService.execute(action, false, false, Optional.empty());
    }

    private void successWorker() throws IOException {
        SystemQuery systemQuery = new TestSystemQuery();
        SaltApi saltApi = new TestSaltApi();
        SaltUtils saltUtils = new SaltUtils(systemQuery, saltApi) {

            @Override
            public boolean shouldRefreshPackageList(Optional<Xor<String[], String>> function,
                                                    Optional<JsonElement> callResult) {
                return false;
            }

            @Override
            public void updateServerAction(ServerAction serverAction, long retcode, boolean success, String jid,
                                           JsonElement jsonResult, Optional<Xor<String[], String>> function,
                                           Date endTime) {
                serverAction.setStatusCompleted();
            }
        };
        saltUtils.setScriptsDir(Files.createTempDirectory("actionscripts"));
        saltServerActionService.setSaltUtils(saltUtils);
    }

    @Test
    public void testAnsiblePlaybookAction() throws Exception {
        MinionServer controlNode = MinionServerFactoryTest.createTestMinionServer(user);

        PlaybookAction action = (PlaybookAction) ActionFactoryTest.createAction(user, ActionFactory.TYPE_PLAYBOOK);
        PlaybookActionDetails details = new PlaybookActionDetails();
        details.setInventoryPath("/path/to/my/hosts");
        details.setPlaybookPath("/path/to/myplaybook.yml");
        action.setDetails(details);

        ActionFactory.addServerToAction(controlNode, action);

        Map<LocalCall<?>, List<MinionSummary>> result = saltServerActionService.callsForAction(action,
                Collections.singletonList(new MinionSummary(controlNode)));

        LocalCall<?> saltCall = result.keySet().iterator().next();
        assertStateApplyWithPillar("ansible.runplaybook", "playbook_path", "/path/to/myplaybook.yml", saltCall);
        assertStateApplyWithPillar("ansible.runplaybook", "rundir", "/path/to", saltCall);
        assertStateApplyWithPillar("ansible.runplaybook", "inventory_path", "/path/to/my/hosts", saltCall);
    }

    private static class MinionListMatcher extends BaseMatcher<MinionList> {

        private final List<String> expectedMinionIds;

        private MinionListMatcher(List<String> minionIds) {
            this.expectedMinionIds = minionIds;
        }

        @Override
        public boolean matches(Object actualValue) {
            if (!(actualValue instanceof MinionList)) {
                return false;
            }

            MinionList actualMinionList = (MinionList) actualValue;
            return Objects.equals(expectedMinionIds, actualMinionList.getTarget());

        }

        @Override
        public void describeTo(Description description) {
            description.appendText("MinionList").appendValue(this.expectedMinionIds);
        }

        @Override
        public void describeMismatch(Object item, Description description) {
            if (item instanceof MinionList) {
                MinionList minionList = (MinionList) item;
                description.appendText("was MinionList").appendValue(minionList.getTarget());
            }
            else {
                description.appendText(" was not ")
                           .appendText(MinionList.class.getName())
                           .appendText(" but ")
                           .appendText(item.getClass().getName())
                           .appendValue(item);
            }
        }
    }
}
