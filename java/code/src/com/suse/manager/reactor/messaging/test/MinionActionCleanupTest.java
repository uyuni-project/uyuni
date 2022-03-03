/*
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainEntry;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.script.ScriptActionDetails;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.reactor.messaging.JobReturnEventMessageAction;
import com.suse.manager.utils.SaltKeyUtils;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.SaltServerActionService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.MinionActionUtils;
import com.suse.salt.netapi.calls.modules.SaltUtil;
import com.suse.salt.netapi.calls.runner.Jobs;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.parser.JsonParser;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.utils.Xor;

import com.google.gson.reflect.TypeToken;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
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
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    /**
     * Test the processing of packages.profileupdate job return event.
     *
     * @throws Exception in case of an error
     */
    @Test
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
                Date.from(Instant.now().minus(6, ChronoUnit.MINUTES)));
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
            never(saltServiceMock).jobsByMetadata(with(any(Object.class)));
            never(saltServiceMock).listJob(with(any(String.class)));
        } });

        SaltUtils saltUtils = new SaltUtils(saltServiceMock, saltServiceMock);
        SaltServerActionService saltServerActionService = new SaltServerActionService(saltServiceMock, saltUtils,
                new SaltKeyUtils(saltServiceMock));
        MinionActionUtils minionActionUtils = new MinionActionUtils(saltServerActionService, saltServiceMock,
                saltUtils);
        minionActionUtils.cleanupMinionActions();
    }

    private Jobs.Info listJob(String filename, long actionId) throws Exception {
        Path path = new File(TestUtils.findTestData(
                "/com/suse/manager/reactor/messaging/test/" + filename).getPath()).toPath();
        String eventString = Files.lines(path)
                .collect(Collectors.joining("\n"))
                .replaceAll("\"suma-action-id\": \\d+", "\"suma-action-id\": " + actionId);
        JsonParser<Jobs.Info> jsonParser = new JsonParser<>(new TypeToken<>() {
        });
        return jsonParser.parse(eventString);
    }

    private Jobs.Info listJob(String filename, String minion1Id, List<String> actions1,
                              String minion2Id, List<String> actions2) throws Exception {
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

        JsonParser<Jobs.Info> jsonParser = new JsonParser<>(new TypeToken<>() { });
        return jsonParser.parse(eventString);
    }

    private Map<String, Jobs.ListJobsEntry> jobsByMetadata(String filename, long actionId) throws Exception {
        Path path = new File(TestUtils.findTestData(
                "/com/suse/manager/reactor/messaging/test/" + filename).getPath()).toPath();
        String eventString = Files.lines(path)
                .collect(Collectors.joining("\n"))
                .replaceAll("\"suma-action-id\": \\d+", "\"suma-action-id\": " + actionId);
        JsonParser<Map<String, Jobs.ListJobsEntry>> jsonParser = new JsonParser<>(new TypeToken<>() { });
        return jsonParser.parse(eventString);
    }

    @Test
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
                .scheduleApplyStates(user, Arrays.asList(minion1.getId(), minion2.getId()),
                        Optional.of(false), earliest, actionChain);
        assertEquals(2, applyStates.size());

        ScriptActionDetails sad = ActionFactory.createScriptActionDetails(
                "root", "root", 10L, "#!/bin/csh\necho hello");
        Set<Action> scriptRun = ActionChainManager.scheduleScriptRuns(
                user, Arrays.asList(minion1.getId(), minion2.getId()), "Run script test", sad, earliest, actionChain);
        assertEquals(2, scriptRun.size());

        HibernateFactory.getSession().flush();

        Action action11 = actionChain.getEntries().stream()
                .filter(e -> e.getServer().equals(minion1))
                .filter(e -> e.getAction().getActionType().equals(ActionFactory.TYPE_APPLY_STATES))
                .map(ActionChainEntry::getAction)
                .findFirst().get();
        Action action12 = actionChain.getEntries().stream()
                .filter(e -> e.getServer().equals(minion1))
                .filter(e -> e.getAction().getActionType().equals(ActionFactory.TYPE_SCRIPT_RUN))
                .map(ActionChainEntry::getAction)
                .findFirst().get();
        Action action21 = actionChain.getEntries().stream()
                .filter(e -> e.getServer().equals(minion2))
                .filter(e -> e.getAction().getActionType().equals(ActionFactory.TYPE_APPLY_STATES))
                .map(ActionChainEntry::getAction)
                .findFirst().get();
        Action action22 = actionChain.getEntries().stream()
                .filter(e -> e.getServer().equals(minion2))
                .filter(e -> e.getAction().getActionType().equals(ActionFactory.TYPE_SCRIPT_RUN))
                .map(ActionChainEntry::getAction)
                .findFirst().get();

        context().checking(new Expectations() {
            {
                allowing(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
                allowing(taskomaticMock).scheduleActionChainExecution(with(any(ActionChain.class)));

                allowing(saltServiceMock).jobsByMetadata(
                        with(any(Object.class)), with(any(LocalDateTime.class)), with(any(LocalDateTime.class)));
                will(returnValue(Optional.of(jobsByMetadata("jobs.list_jobs.actionchains.json", 0))));

                mockListJob("20180316234939446951");
                mockListJob("20180317134012978804");
                mockListJob("20180317134013012209");
                mockListJob("20180317134233760065");
            }

            private void mockListJob(String jid) {
                never(saltServiceMock).jobsByMetadata(with(any(Object.class)));
                never(saltServiceMock).listJob(with(any(String.class)));
            }
        });

        ActionChainFactory.schedule(actionChain, earliest);

        ActionChainFactory.delete(actionChain);

        SaltUtils saltUtils = new SaltUtils(saltServiceMock, saltServiceMock);
        SaltServerActionService saltServerActionService = new SaltServerActionService(saltServiceMock, saltUtils,
                new SaltKeyUtils(saltServiceMock));
        MinionActionUtils minionActionUtils = new MinionActionUtils(saltServerActionService, saltServiceMock,
                saltUtils);
    }
}
