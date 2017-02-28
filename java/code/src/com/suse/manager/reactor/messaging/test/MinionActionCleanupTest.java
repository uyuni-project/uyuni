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

import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesActionResult;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.manager.action.ActionManager;
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
import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tests for {@link JobReturnEventMessageAction}.
 */
public class MinionActionCleanupTest extends JMockBaseTestCaseWithUser {

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
            oneOf(saltServiceMock).jobsByMetadata(with(any(Object.class)));
            will(returnValue(jobsByMetadata("jobs.list_jobs.with_metadata.json", action.getId())));
            oneOf(saltServiceMock).listJob(with(equal("20160602085832364245")));
            will(returnValue(listJobResult));
        } });

        MinionActionUtils.cleanupMinionActions(saltServiceMock);

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

    private Jobs.Info listJob(String filename, long actionId) throws Exception {
        Path path = new File(TestUtils.findTestData(
                "/com/suse/manager/reactor/messaging/test/" + filename).getPath()).toPath();
        String eventString = Files.lines(path)
                .collect(Collectors.joining("\n"))
                .replaceAll("\"suma-action-id\": \\d+", "\"suma-action-id\": " + actionId);
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
}
