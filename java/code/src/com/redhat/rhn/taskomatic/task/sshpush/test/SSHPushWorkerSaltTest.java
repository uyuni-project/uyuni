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
package com.redhat.rhn.taskomatic.task.sshpush.test;

import com.google.gson.JsonElement;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionStatus;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.taskomatic.task.sshpush.SSHPushWorkerSalt;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.controllers.utils.test.SSHMinionBootstrapperTest;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.salt.netapi.calls.LocalCall;
import org.apache.log4j.Logger;
import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;

import java.util.Collections;
import java.util.Optional;

import static com.redhat.rhn.domain.action.ActionFactory.STATUS_COMPLETED;
import static com.redhat.rhn.domain.action.ActionFactory.STATUS_FAILED;
import static com.redhat.rhn.domain.action.ActionFactory.STATUS_QUEUED;

/**
 * SSHPushWorkerSaltTest
 */
public class SSHPushWorkerSaltTest extends JMockBaseTestCaseWithUser {

    private Logger logger = Logger.getLogger(SSHMinionBootstrapperTest.class);

    private SSHPushWorkerSalt worker;
    private MinionServer minion;
    private SaltService saltServiceMock;
    private SaltUtils saltUtilsMock;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        setImposteriser(ClassImposteriser.INSTANCE);
        saltServiceMock = mock(SaltService.class);
        saltUtilsMock = mock(SaltUtils.class);
        worker = new SSHPushWorkerSalt(logger, null, saltServiceMock, saltUtilsMock);
        minion = MinionServerFactoryTest.createTestMinionServer(user);
    }

    /**
     * Tests that an attempt to execute action that has been already completed will not
     * invoke any salt calls and that the state of the action doesn't change.
     *
     * @throws Exception if anything goes wrong
     */
    public void testDontExecuteCompletedAction() throws Exception {
        expectNoSaltCalls();
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction serverAction = createChildServerAction(action, STATUS_COMPLETED, 5L);

        worker.executeAction(action, minion);

        assertEquals(STATUS_COMPLETED, serverAction.getStatus());
        assertEquals(Long.valueOf(5L), serverAction.getRemainingTries());
    }

    /**
     * Tests that an attempt to execute action that has already failed will not
     * invoke any salt calls.
     *
     * @throws Exception if anything goes wrong
     */
    public void testDontExecuteFailedAction() throws Exception {
        expectNoSaltCalls();
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction serverAction = createChildServerAction(action, STATUS_FAILED, 5L);

        worker.executeAction(action, minion);

        assertEquals(STATUS_FAILED, serverAction.getStatus());
        assertEquals(Long.valueOf(5L), serverAction.getRemainingTries());
    }

    /**
     * Tests that an action with no remaining tries will be set to the failed state
     * (with a corresponding message) and that it will not invoke any salt calls.
     *
     * @throws Exception if anything goes wrong
     */
    public void testExecuteActionNoRemainingTries() throws Exception {
        expectNoSaltCalls();
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction serverAction = createChildServerAction(action, STATUS_QUEUED, 0L);

        worker.executeAction(action, minion);

        assertEquals(STATUS_FAILED, serverAction.getStatus());
        assertEquals(
                "Action has been picked up multiple times" +
                        " without a successful transaction;" +
                        " This action is now failed for this system.",
                serverAction.getResultMsg());
        ActionFactory.getSession().flush();
        assertEquals(Long.valueOf(1L), action.getFailedCount());
    }

    /**
     * Tests that an action with a failed prerequisite will set be to the failed state
     * (with a corresponding message) and that it will not invoke any salt calls.
     *
     * @throws Exception if anything goes wrong
     */
    public void testDontExecuteActionWhenPrerequisiteFailed() throws Exception {
        expectNoSaltCalls();

        // prerequisite failed
        Action prereq = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        createChildServerAction(prereq, STATUS_FAILED, 0L);

        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        action.setPrerequisite(prereq);
        ServerAction serverAction = createChildServerAction(action, STATUS_QUEUED, 5L);

        worker.executeAction(action, minion);

        assertEquals(STATUS_FAILED, serverAction.getStatus());
        assertEquals("Prerequisite failed.", serverAction.getResultMsg());
        // this comes from the xmlrpc/queue.py
        assertEquals(Long.valueOf(-100L), serverAction.getResultCode());
        ActionFactory.getSession().flush();
        assertEquals(Long.valueOf(1L), action.getFailedCount());
    }

    /**
     * Tests that the successful execution of an action correctly sets the status and the
     * number of remaining tries.
     *
     * @throws Exception if anything goes wrong
     */
    public void testExecuteActionSuccess() throws Exception {
        worker = successWorker();

        context().checking(new Expectations() {{
            oneOf(saltServiceMock).callSync(
                    with(any(LocalCall.class)),
                    with(any(String.class)));
            Optional<JsonElement> result = Optional.of(mock(JsonElement.class));
            will(returnValue(result));
        }});

        // create action without servers
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction serverAction = createChildServerAction(action, STATUS_QUEUED, 5L);

        worker.executeAction(action, minion);

        assertEquals(Long.valueOf(4L), serverAction.getRemainingTries());
        assertEquals(STATUS_COMPLETED, serverAction.getStatus());
    }

    /**
     * Tests that an execution with empty result from salt keeps the action in the queued
     * state and decreases the number of tries.
     *
     * @throws Exception if anything goes wrong
     */
    public void testExecuteActionRetryOnEmptyResult() throws Exception {
        // expect salt returning empty result
        context().checking(new Expectations() {{
            oneOf(saltServiceMock).callSync(
                    with(any(LocalCall.class)),
                    with(any(String.class)));
            will(returnValue(Optional.empty()));
        }});
        assertActionWillBeRetried();
    }

    /**
     * Tests that an execution with exception from salt keeps the action in the queued
     * state and decreases the number of tries.
     *
     * @throws Exception if anything goes wrong
     */
    public void testExecuteActionRetryOnException() throws Exception {
        // expect salt returning empty result
        context().checking(new Expectations() {{
            oneOf(saltServiceMock).callSync(
                    with(any(LocalCall.class)),
                    with(any(String.class)));
            will(throwException(new RuntimeException()));
        }});
        try {
            assertActionWillBeRetried();
        } catch (RuntimeException e) {
            // expected
            return;
        }
        fail("Runtime exception should have been thrown.");
    }

    private void assertActionWillBeRetried() throws Exception {
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction serverAction = createChildServerAction(action, STATUS_QUEUED, 5L);

        worker.executeAction(action, minion);

        assertEquals(Long.valueOf(4L), serverAction.getRemainingTries());
        assertEquals(STATUS_QUEUED, serverAction.getStatus());
    }

    /**
     * Tests the following scenario:
     *  - execute an action, it fails (empty result from salt)
     *  - check that action is still queued and has decreased number of tries
     *  - execute the action again, now it succeeds
     *  - check that action is completed and has decreased number of tries
     *
     * @throws Exception if anything goes wrong
     */
    public void testSuccessRetryAfterEmptyResult() throws Exception {
        // firstly, let's simulate a unsuccessful call
        context().checking(new Expectations() {{
            oneOf(saltServiceMock).callSync(
                    with(any(LocalCall.class)),
                    with(any(String.class)));
            will(returnValue(Optional.empty()));
        }});
        successAfterRetryHelper();
    }

    /**
     * Tests the following scenario:
     *  - execute an action, it fails (exception from salt)
     *  - check that action is still queued and has decreased number of tries
     *  - execute the action again, now it succeeds
     *  - check that action is completed and has decreased number of tries
     *
     * @throws Exception if anything goes wrong
     */
    public void testSuccessRetryAfterException() throws Exception {
        // firstly, let's simulate a unsuccessful call
        context().checking(new Expectations() {{
            oneOf(saltServiceMock).callSync(
                    with(any(LocalCall.class)),
                    with(any(String.class)));
            will(throwException(new RuntimeException()));
        }});
        successAfterRetryHelper();
    }

    /**
     * Tests that execution skips server actions which still have queued prerequisite
     * server actions.
     *
     * @throws Exception if anything goes wrong
     */
    public void testSkipActionWhenPrerequisiteQueued() throws Exception {
        expectNoSaltCalls();
        worker = successWorker();

        // prerequisite is still queued
        Action prereq = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction prereqServerAction = createChildServerAction(prereq, STATUS_QUEUED, 5L);
        prereq.setServerActions(Collections.singleton(prereqServerAction));

        // action is queued as well
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        action.setPrerequisite(prereq);
        ServerAction serverAction = createChildServerAction(action, STATUS_QUEUED, 5L);

        worker.executeAction(action, minion);

        // both status and remaining tries should remain unchanged
        assertEquals(STATUS_QUEUED, serverAction.getStatus());
        assertEquals(Long.valueOf(5L), serverAction.getRemainingTries());
    }

    /**
     * Tests that execution skips server actions which still have queued prerequisite
     * server actions but after the prerequisite is executed (= it's in either completed or
     * failed state), the dependant server action is not skipped anymore.
     *
     * @throws Exception if anything goes wrong
     */
    public void testSkipActionComplex() throws Exception {
        expectNoSaltCalls();
        worker = successWorker();

        // prerequisite is still queued
        Action prereq = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction prereqServerAction = createChildServerAction(prereq, STATUS_QUEUED, 5L);

        // action is queued as well
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        action.setPrerequisite(prereq);
        ServerAction serverAction = createChildServerAction(action, STATUS_QUEUED, 5L);

        worker.executeAction(action, minion);

        // both status and remaining tries should remain unchanged
        assertEquals(STATUS_QUEUED, serverAction.getStatus());
        assertEquals(Long.valueOf(5L), serverAction.getRemainingTries());

        context().checking(new Expectations() {{
            exactly(2).of(saltServiceMock).callSync(
                    with(any(LocalCall.class)),
                    with(any(String.class)));
            Optional<JsonElement> result = Optional.of(mock(JsonElement.class));
            will(returnValue(result));
        }});

        worker.executeAction(prereq, minion);
        assertEquals(STATUS_COMPLETED, prereqServerAction.getStatus());

        // 2nd try
        worker.executeAction(action, minion);
        assertEquals(STATUS_COMPLETED, serverAction.getStatus());
    }

    private void successAfterRetryHelper() throws Exception {
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_SCRIPT_RUN);
        ServerAction serverAction = createChildServerAction(action, STATUS_QUEUED, 5L);

        try {
            worker.executeAction(action, minion);
        } catch (RuntimeException e) {
            // no-op
        }

        // should be still STATUS_QUEUED, number of tries is decreased
        assertEquals(Long.valueOf(4L), serverAction.getRemainingTries());
        assertEquals(STATUS_QUEUED, serverAction.getStatus());

        // we create a salt service that succeeds
        context().checking(new Expectations() {{
            oneOf(saltServiceMock).callSync(
                    with(any(LocalCall.class)),
                    with(any(String.class)));
            Optional<JsonElement> result = Optional.of(mock(JsonElement.class));
            will(returnValue(result));
        }});

        // repeat the execution with successful result
        worker = successWorker();
        worker.executeAction(action, minion);

        // should be still STATUS_COMPLETED, number of tries is decreased
        assertEquals(Long.valueOf(3L), serverAction.getRemainingTries());
        assertEquals(STATUS_COMPLETED, serverAction.getStatus());
    }

    private ServerAction createChildServerAction(Action action, ActionStatus status,
            long remainingTries) throws Exception {
        ServerAction serverAction = ActionFactoryTest.createServerAction(minion, action);
        serverAction.setStatus(status);
        serverAction.setRemainingTries(remainingTries);
        action.setServerActions(Collections.singleton(serverAction));
        return serverAction;
    }

    private void expectNoSaltCalls() {
        context().checking(new Expectations() {{
            // we never invoke call for actions that are out of remaining tries!
            never(saltServiceMock).callSync(
                    with(any(LocalCall.class)),
                    with(any(String.class)));
        }});
    }

    private SSHPushWorkerSalt successWorker() {
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
        return new SSHPushWorkerSalt(logger, null, saltServiceMock, saltUtils);
    }
}
