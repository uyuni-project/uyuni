/**
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.manager.reactor.messaging.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.script.ScriptActionDetails;
import com.redhat.rhn.domain.action.script.ScriptRunAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.google.gson.reflect.TypeToken;
import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.reactor.messaging.JobReturnEventMessageAction;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.MinionActionUtils;
import com.suse.manager.webui.utils.YamlHelper;
import com.suse.salt.netapi.calls.modules.SaltUtil;
import com.suse.salt.netapi.calls.runner.Jobs;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.parser.JsonParser;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.utils.Xor;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tests for {@link JobReturnEventMessageAction}.
 */
public class MinionActionCleanupTest extends JMockBaseTestCaseWithUser {

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ClassImposteriser.INSTANCE);
    }

    /**
     * Test the processing of packages.profileupdate job return event.
     *
     * @throws Exception in case of an error
     */
    public void testMinionActionCleanup() throws Exception {
        // Prepare test objects: minion servers, products and action
        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        minion1.setMinionId("minion1");
        MinionServer minion2 = MinionServerFactoryTest.createTestMinionServer(user);
        minion2.setMinionId("minion2");

        ApplyStatesAction action = ActionManager.scheduleApplyStates(
                user,
                Arrays.asList(minion1.getId(), minion2.getId()),
                Collections.singletonList(ApplyStatesEventMessage.PACKAGES),
                Date.from(Instant.now().minus(62, ChronoUnit.MINUTES)));
        action.addServerAction(ActionFactoryTest.createServerAction(minion1, action));
        action.addServerAction(ActionFactoryTest.createServerAction(minion2, action));

        Map<String, Result<List<SaltUtil.RunningInfo>>> running = new HashMap<>();
        running.put(minion1.getMinionId(), new Result<>(Xor.right(Collections.emptyList())));
        running.put(minion2.getMinionId(), new Result<>(Xor.right(Collections.emptyList())));

        Jobs.Info listJobResult = listJob("jobs.list_job.state.apply.json", action.getId());
        SaltService saltServiceMock = mock(SaltService.class);

        context().checking(new Expectations() { {
            allowing(saltServiceMock).running(with(any(MinionList.class)));
            will(returnValue(running));
            if (MinionActionUtils.POSTGRES) {
                never(saltServiceMock).jobsByMetadata(with(any(Object.class)));
                never(saltServiceMock).listJob(with(any(String.class)));
            }
            else {
                atLeast(1).of(saltServiceMock).jobsByMetadata(with(any(Object.class)));
                will(returnValue(Optional.of(jobsByMetadata("jobs.list_jobs.with_metadata.json", action.getId()))));
                atLeast(1).of(saltServiceMock).listJob(with(equal("20160602085832364245")));
                will(returnValue(Optional.of(listJobResult)));
            }
        } });

        MinionActionUtils.cleanupMinionActions(saltServiceMock);

        if (!MinionActionUtils.POSTGRES) {
            action.getServerActions().stream().forEach(sa -> {
                assertEquals(ActionFactory.STATUS_COMPLETED, sa.getStatus());
                assertEquals("Successfully applied state(s): [packages]", sa.getResultMsg());
                assertEquals(0L, sa.getResultCode().longValue());
            });

            String dump = YamlHelper.INSTANCE.dump(listJobResult.getResult(minion1.getMinionId(), Object.class).get());

            action.getDetails().getResults().stream().forEach(result -> {
                assertEquals(0L, result.getReturnCode().longValue());
                assertEquals(dump, new String(result.getOutput()));
            });
        }
    }

    private Jobs.Info listJob(String filename, long actionId) throws Exception {
        Path path = new File(TestUtils.findTestData(
                "/com/suse/manager/reactor/messaging/test/" + filename).getPath()).toPath();
        String eventString = Files.lines(path)
                .collect(Collectors.joining("\n"))
                .replaceAll("\"suma-action-id\": \\d+", "\"suma-action-id\": " + actionId);
        JsonParser<Jobs.Info> jsonParser = new JsonParser<>(new TypeToken<Jobs.Info>() {});
        return jsonParser.parse(eventString);
    }

    private Jobs.Info listJob(String filename, String minion1Id, List<String> actions1, String minion2Id, List<String> actions2) throws Exception {
        Path path = new File(TestUtils.findTestData(
                "/com/suse/manager/reactor/messaging/test/" + filename).getPath()).toPath();
        String eventString = Files.lines(path)
                .collect(Collectors.joining("\n"))
                .replaceAll("\\$minion_1", minion1Id)
                .replaceAll("\\$minion_2", minion2Id)
                .replaceAll("\\$action_1_1", actions1.get(0))
                .replaceAll("\\$action_1_2", actions1.get(1))
                .replaceAll("\\$action_2_1", actions2.get(0))
                .replaceAll("\\$action_2_2", actions2.get(1));

        JsonParser<Jobs.Info> jsonParser = new JsonParser<>(new TypeToken<Jobs.Info>() {});
        return jsonParser.parse(eventString);
    }

    private Map<String, Jobs.ListJobsEntry> jobsByMetadata(String filename, long actionId) throws Exception {
        Path path = new File(TestUtils.findTestData(
                "/com/suse/manager/reactor/messaging/test/" + filename).getPath()).toPath();
        String eventString = Files.lines(path)
                .collect(Collectors.joining("\n"))
                .replaceAll("\"suma-action-id\": \\d+", "\"suma-action-id\": " + actionId);
        JsonParser<Map<String, Jobs.ListJobsEntry>> jsonParser = new JsonParser<>(new TypeToken<Map<String, Jobs.ListJobsEntry>>() {});
        return jsonParser.parse(eventString);
    }

    public void testMinionActionChainCleanupAllCompleted() throws Exception {
        MinionServer minion1 = MinionServerFactoryTest.createTestMinionServer(user);
        SystemManager.giveCapability(minion1.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);

        MinionServer minion2 = MinionServerFactoryTest.createTestMinionServer(user);
        SystemManager.giveCapability(minion2.getId(), SystemManager.CAP_SCRIPT_RUN, 1L);

        TaskomaticApi taskomaticMock = mock(TaskomaticApi.class);
        SaltService saltServiceMock = mock(SaltService.class);

        ActionManager.setTaskomaticApi(taskomaticMock);
        ActionChainManager.setTaskomaticApi(taskomaticMock);
        ActionChainFactory.setTaskomaticApi(taskomaticMock);


        Date earliest = Date.from(ZonedDateTime.now()
                .minus(2, ChronoUnit.HOURS)
                .toInstant());

        String label = TestUtils.randomString();
        ActionChain actionChain = ActionChainFactory.getOrCreateActionChain(label, user);

        Set<Action> applyStates = ActionChainManager
                .scheduleApplyStates(user, Arrays.asList(minion1.getId(), minion2.getId()), Optional.of(false), earliest, actionChain);
        assertEquals(2, applyStates.size());

        ScriptActionDetails sad = ActionFactory.createScriptActionDetails(
                "root", "root", new Long(10), "#!/bin/csh\necho hello");
        Set<Action> scriptRun = ActionChainManager.scheduleScriptRuns(
                user, Arrays.asList(minion1.getId(), minion2.getId()), "Run script test", sad, earliest, actionChain);
        assertEquals(2, scriptRun.size());

        HibernateFactory.getSession().flush();

        Action action1_1 = actionChain.getEntries().stream()
                .filter(e -> e.getServer().equals(minion1))
                .filter(e -> e.getAction().getActionType().equals(ActionFactory.TYPE_APPLY_STATES))
                .map(e -> e.getAction())
                .findFirst().get();
        Action action1_2 = actionChain.getEntries().stream()
                .filter(e -> e.getServer().equals(minion1))
                .filter(e -> e.getAction().getActionType().equals(ActionFactory.TYPE_SCRIPT_RUN))
                .map(e -> e.getAction())
                .findFirst().get();
        Action action2_1 = actionChain.getEntries().stream()
                .filter(e -> e.getServer().equals(minion2))
                .filter(e -> e.getAction().getActionType().equals(ActionFactory.TYPE_APPLY_STATES))
                .map(e -> e.getAction())
                .findFirst().get();
        Action action2_2 = actionChain.getEntries().stream()
                .filter(e -> e.getServer().equals(minion2))
                .filter(e -> e.getAction().getActionType().equals(ActionFactory.TYPE_SCRIPT_RUN))
                .map(e -> e.getAction())
                .findFirst().get();

        context().checking(new Expectations() {
            {
                allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
                allowing(taskomaticMock).scheduleActionChainExecution(with(any(ActionChain.class)));

                allowing(saltServiceMock).jobsByMetadata(with(any(Object.class)), with(any(LocalDateTime.class)), with(any(LocalDateTime.class)));
                will(returnValue(Optional.of(jobsByMetadata("jobs.list_jobs.actionchains.json", 0))));

                mockListJob("20180316234939446951");
                mockListJob("20180317134012978804");
                mockListJob("20180317134013012209");
                mockListJob("20180317134233760065");
            }

            private void mockListJob(String jid) throws Exception {
                if (MinionActionUtils.POSTGRES) {
                    never(saltServiceMock).jobsByMetadata(with(any(Object.class)));
                    never(saltServiceMock).listJob(with(any(String.class)));
                }
                else {
                    atLeast(1).of(saltServiceMock).listJob(jid);
                    will(returnValue(Optional.of(listJob("jobs.list_jobs." + jid + ".json",
                            minion1.getMinionId(), Arrays.asList(action1_1.getId() + "", action1_2.getId() + ""),
                            minion2.getMinionId(), Arrays.asList(action2_1.getId() + "", action2_2.getId() + "")))));
                }
               
            }
        });

        ActionChainFactory.schedule(actionChain, earliest);

        ActionChainFactory.delete(actionChain);

        MinionActionUtils.cleanupMinionActionChains(saltServiceMock);
        
        if (!MinionActionUtils.POSTGRES) {
            assertActionCompleted(action1_1);
            assertActionCompleted(action1_2);
            assertActionCompleted(action2_1);
            assertActionCompleted(action2_2);
        }
    }

    private void assertActionCompleted(Action action) {
        assertEquals(1, action.getServerActions().size());
        assertEquals(ActionFactory.STATUS_COMPLETED, action.getServerActions().stream().findFirst().get().getStatus());
    }

}
