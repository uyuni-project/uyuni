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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsAction;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsActionDetails;
import com.redhat.rhn.domain.action.config.ConfigAction;
import com.redhat.rhn.domain.action.script.ScriptActionDetails;
import com.redhat.rhn.domain.channel.AccessToken;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.test.ChannelFactoryTest;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.ConfigTestUtils;
import com.redhat.rhn.testing.ErrataTestUtils;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.RhnBaseTestCase;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.webui.services.SaltActionChainGeneratorService;
import com.suse.manager.webui.services.SaltServerActionService;
import com.suse.manager.webui.utils.SaltModuleRun;
import com.suse.manager.webui.utils.SaltState;
import com.suse.manager.webui.utils.SaltSystemReboot;
import com.suse.salt.netapi.calls.LocalCall;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;

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
import java.util.stream.Collectors;


public class SaltServerActionServiceTest extends JMockBaseTestCaseWithUser {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ClassImposteriser.INSTANCE);
    }

    public void testPackageUpdate() throws Exception {
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        List<MinionServer> mins = new ArrayList<>();
        mins.add(minion);

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
        ActionManager.addPackageActionDetails(Arrays.asList(action), packageMaps);
        TestUtils.flushAndEvict(action);
        Action updateAction = ActionFactory.lookupById(action.getId());

        Map<LocalCall<?>, List<MinionServer>> result = SaltServerActionService.INSTANCE.callsForAction(updateAction, mins);
        RhnBaseTestCase.assertNotEmpty(result.values());
    }

    public void testDeployFiles() throws Exception {
        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer minion2 = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer minion3 = MinionServerFactoryTest.createTestMinionServer(user);
        MinionServer minion4 = MinionServerFactoryTest.createTestMinionServer(user);
        List<MinionServer> minions = Arrays.asList(minion1,minion2, minion3, minion4);

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
        Map<LocalCall<?>, List<MinionServer>> result = SaltServerActionService.INSTANCE.callsForAction(configAction, minions);
        assertEquals(result.size(), 3);
    }

    public void testExecuteActionChain() throws Exception {
        SaltActionChainGeneratorService generatorService = new SaltActionChainGeneratorService() {
            @Override
            public void createActionChainSLSFiles(ActionChain actionChain, MinionServer minion, List<SaltState> states) {
                assertEquals(3, states.size());
                SaltModuleRun scriptRun = (SaltModuleRun)states.get(0);
                SaltSystemReboot reboot = (SaltSystemReboot)states.get(1);
                SaltModuleRun highstate = (SaltModuleRun)states.get(2);

                long scriptActionId = actionChain.getEntries().stream()
                        .filter(ace -> ace.getServer().equals(minion) && ace.getAction().getActionType().equals(ActionFactory.TYPE_SCRIPT_RUN))
                        .map(ace -> ace.getActionId())
                        .findFirst().get();
                long rebootActionId = actionChain.getEntries().stream()
                        .filter(ace -> ace.getServer().equals(minion) && ace.getAction().getActionType().equals(ActionFactory.TYPE_REBOOT))
                        .map(ace -> ace.getActionId())
                        .findFirst().get();
                long highstateActionId = actionChain.getEntries().stream()
                        .filter(ace -> ace.getServer().equals(minion) && ace.getAction().getActionType().equals(ActionFactory.TYPE_APPLY_STATES))
                        .map(ace -> ace.getActionId())
                        .findFirst().get();
                assertEquals(SaltActionChainGeneratorService.ACTION_STATE_ID_PREFIX + actionChain.getId() + "_action_" + scriptActionId,
                        scriptRun.getId());
                assertEquals(SaltActionChainGeneratorService.ACTION_STATE_ID_PREFIX + actionChain.getId() + "_action_" + rebootActionId,
                        reboot.getId());
                assertEquals(SaltActionChainGeneratorService.ACTION_STATE_ID_PREFIX + actionChain.getId() + "_action_" + highstateActionId,
                        highstate.getId());
            }
        };

        SaltServerActionService.INSTANCE.setSaltActionChainGeneratorService(generatorService);
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
                "root", "root", new Long(10), "#!/bin/csh\necho hello");
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

        SaltServerActionService.INSTANCE.executeActionChain(actionChain.getId());
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

        SaltServerActionService.INSTANCE.setCommitTransaction(false);
        Map<LocalCall<?>, List<MinionServer>> calls = SaltServerActionService.INSTANCE.callsForAction(action, Arrays.asList(minion1));

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
}
