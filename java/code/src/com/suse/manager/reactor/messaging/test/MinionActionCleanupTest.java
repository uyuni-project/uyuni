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

import com.google.gson.reflect.TypeToken;
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
import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.reactor.messaging.JobReturnEventMessageAction;
import com.suse.manager.webui.services.SaltService;
import com.suse.manager.webui.utils.MinionActionUtils;
import com.suse.manager.webui.utils.YamlHelper;
import com.suse.salt.netapi.calls.modules.SaltUtil;
import com.suse.salt.netapi.calls.runner.Jobs;
import com.suse.salt.netapi.datatypes.target.Target;
import com.suse.salt.netapi.parser.JsonParser;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.utils.Xor;
import org.jmock.Expectations;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Date;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tests for {@link JobReturnEventMessageAction}.
 */
public class MinionActionCleanupTest extends JMockBaseTestCaseWithUser {

    /**
     * Test the processing of packages.profileupdate job return event.
     *
     * @throws Exception in case of an error
     */
    public void testMinionActionCleanup() throws Exception {
        // Prepare test objects: minion server, products and action
        MinionServer minion = MinionServerFactoryTest.createTestMinionServer(user);
        minion.setMinionId("minionsles12-suma3pg.vagrant.local");

        ApplyStatesAction action = ActionManager.scheduleApplyStates(
                user,
                Collections.singletonList(minion.getId()),
                Collections.singletonList(ApplyStatesEventMessage.PACKAGES),
                Date.from(Instant.now().minus(6, ChronoUnit.MINUTES)));
        action.addServerAction(ActionFactoryTest.createServerAction(minion, action));

        Map<String, Result<List<SaltUtil.RunningInfo>>> running = new HashMap<>();
        running.put(minion.getMinionId(), new Result<>(Xor.right(Collections.emptyList())));

        Jobs.Info listJobResult = listJob("jobs.list_job.state.apply.json", action.getId());
        SaltService saltServiceMock = mock(SaltService.class);

        context().checking(new Expectations() { {
            allowing(saltServiceMock).running(with(any(Target.class)));
            will(returnValue(running));
            allowing(saltServiceMock).jobsByMetadata(with(any(Object.class)));
            will(returnValue(jobsByMetadata("jobs.list_jobs.with_metadata.json", action.getId())));
            allowing(saltServiceMock).listJob(with(any(String.class)));
            will(returnValue(listJobResult));
        } });


        MinionActionUtils.cleanupMinionActions(saltServiceMock);

        ServerAction sa = action.getServerActions().stream().findFirst().get();
        assertEquals(ActionFactory.STATUS_COMPLETED, sa.getStatus());
        assertEquals("Successfully applied state(s): packages", sa.getResultMsg());
        assertEquals(0L, sa.getResultCode().longValue());

        String dump = YamlHelper.INSTANCE.dump(listJobResult.getResult(minion.getMinionId(), Object.class).get());

        ApplyStatesActionResult applyStatesActionResult = action.getDetails().getResults().stream().findFirst().get();
        assertEquals(0L, applyStatesActionResult.getReturnCode().longValue());
        assertEquals(dump, new String(applyStatesActionResult.getOutput()));

    }

    private Jobs.Info listJob(String filename, long actionId) throws Exception {
        Path path = new File(TestUtils.findTestData(
                "/com/suse/manager/reactor/messaging/test/" + filename).getPath()).toPath();
        String eventString = Files.lines(path)
                .collect(Collectors.joining("\n"))
                .replaceAll("\"suma-action-id\": \\d+", "\"suma-action-id\": " + actionId);
        JsonParser<Jobs.Info> jsonParser = new JsonParser(new TypeToken<Jobs.Info>() {});
        return jsonParser.parse(eventString);
    }

    private Map<String, Jobs.ListJobsEntry> jobsByMetadata(String filename, long actionId) throws Exception {
        Path path = new File(TestUtils.findTestData(
                "/com/suse/manager/reactor/messaging/test/" + filename).getPath()).toPath();
        String eventString = Files.lines(path)
                .collect(Collectors.joining("\n"))
                .replaceAll("\"suma-action-id\": \\d+", "\"suma-action-id\": " + actionId);
        JsonParser<Map<String, Jobs.ListJobsEntry>> jsonParser = new JsonParser(new TypeToken<Map<String, Jobs.ListJobsEntry>>() {});
        return jsonParser.parse(eventString);
    }

    private String readFile(String file) throws IOException, ClassNotFoundException {
        return Files.lines(new File(TestUtils.findTestData(
                "/com/suse/manager/reactor/messaging/test/" + file).getPath()
        ).toPath()).collect(Collectors.joining("\n"));
    }
}
