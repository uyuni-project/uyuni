/**
 * Copyright (c) 2018 SUSE LLC
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

package com.suse.manager.webui.controllers.test;

import static org.hamcrest.Matchers.containsString;

import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSetMemoryAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSetVcpusAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationShutdownAction;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.frontend.dto.ScheduledAction;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.manager.system.VirtualizationActionCommand;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;
import com.redhat.rhn.testing.ServerTestUtils;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suse.manager.webui.controllers.VirtualGuestsController;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.SparkTestUtils;
import com.suse.salt.netapi.calls.LocalCall;

import org.jmock.Expectations;
import org.jmock.lib.legacy.ClassImposteriser;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import spark.HaltException;
import spark.Request;
import spark.RequestResponseFactory;
import spark.Response;

/**
 * Tests for VirtualGuestsController
 */
public class VirtualGuestsControllerTest extends JMockBaseTestCaseWithUser {

    private Response response;
    private final String baseUri = "http://localhost:8080/rhn";
    private TaskomaticApi taskomaticMock;
    private SaltService saltServiceMock;
    private static final Gson GSON = new GsonBuilder().create();
    private Server host;

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public void setUp() throws Exception {
        super.setUp();

        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        setImposteriser(ClassImposteriser.INSTANCE);
        response = RequestResponseFactory.create(new RhnMockHttpServletResponse());

        taskomaticMock = mock(TaskomaticApi.class);
        ActionManager.setTaskomaticApi(taskomaticMock);
        VirtualizationActionCommand.setTaskomaticApi(taskomaticMock);
        context().checking(new Expectations() {{
            ignoring(taskomaticMock).scheduleActionExecution(with(any(Action.class)));
        }});

        saltServiceMock = context().mock(SaltService.class);
        context().checking(new Expectations() {{
            allowing(saltServiceMock).callSync(
                    with(any(LocalCall.class)),
                    with(containsString("serverfactorytest")));
        }});
        SystemManager.mockSaltService(saltServiceMock);

        host = ServerTestUtils.createVirtHostWithGuests(user, 2, true);
        host.asMinionServer().get().setMinionId("testminion.local");

        // Clean pending actions for easier checks in the tests
        DataResult<ScheduledAction> actions = ActionManager.allActions(user, null);
        for (ScheduledAction scheduledAction : actions) {
            ActionManager.failSystemAction(user, host.getId(), scheduledAction.getId(), "test clean up");
        }
    }

    /**
     * Test getting the data from a virtual host
     *
     * @throws Exception if anything unexpected happens during the test
     */
    @SuppressWarnings("unchecked")
    public void testData() throws Exception {
        int size = host.getGuests().size();
        VirtualInstance[] guests = host.getGuests().toArray(new VirtualInstance[size]);
        Long sid = host.getId();

        String json = VirtualGuestsController.data(
                getRequestWithCsrf("/manager/api/systems/details/virtualization/guests/:sid/data", sid),
                response, user);
        List<Map<String, Object>> model = GSON.fromJson(json, List.class);

        // Sort both actual and expected arrays to ease assertions
        Arrays.sort(guests, (VirtualInstance o1, VirtualInstance o2) -> o1.getUuid().compareTo(o2.getUuid()));
        model.sort((o1, o2) -> ((String)o1.get("uuid")).compareTo((String)o2.get("uuid")));

        assertEquals(size, model.size());
        assertEquals(guests[0].getUuid(), model.get(0).get("uuid"));
        assertEquals(guests[1].getUuid(), model.get(1).get("uuid"));

        Double vCpus = (Double)model.get(0).get("vcpus");
        assertEquals(guests[0].getNumberOfCPUs().intValue(), vCpus.intValue());
        assertEquals(guests[1].getState().getLabel(), model.get(1).get("stateLabel"));
    }

    /**
     * Test a VM state change action
     *
     * @throws Exception if anything unexpected happens during the test
     */
    @SuppressWarnings("unchecked")
    public void testStateChangeAction() throws Exception {
        VirtualInstance guest = host.getGuests().iterator().next();
        Long sid = host.getId();

        String json = VirtualGuestsController.action(
                getPostRequestWithCsrfAndBody("/manager/api/systems/details/virtualization/guests/:sid/:action",
                                              "{uuids: [\"" + guest.getUuid() + "\"]}",
                                              sid, "shutdown"),
                response, user);

        // Make sure the shutdown action was queued
        DataResult<ScheduledAction> actions = ActionManager.pendingActions(user, null);
        assertEquals(ActionFactory.TYPE_VIRTUALIZATION_SHUTDOWN.getName(),
                     actions.get(0).getTypeName());

        Action action = ActionManager.lookupAction(user, actions.get(0).getId());
        VirtualizationShutdownAction virtAction = (VirtualizationShutdownAction)action;
        assertEquals(guest.getUuid(), virtAction.getUuid());

        // Check the response
        Map<String, Long> model = GSON.fromJson(json, new TypeToken<Map<String, Long>>() {}.getType());
        assertEquals(action.getId(), model.get(guest.getUuid()));
    }


    /**
     * Test a VM vcpu change action
     *
     * @throws Exception if anything unexpected happens during the test
     */
    @SuppressWarnings("unchecked")
    public void testSetVcpuAction() throws Exception {
        VirtualInstance guest = host.getGuests().iterator().next();
        Long sid = host.getId();

        Integer vcpus = 3;
        String json = VirtualGuestsController.action(
                getPostRequestWithCsrfAndBody("/manager/api/systems/details/virtualization/guests/:sid/:action",
                                              "{uuids: [\"" + guest.getUuid() + "\"], value: " + vcpus + "}",
                                              sid, "setVcpu"),
                response, user);

        // Make sure the setVpu action was queued
        DataResult<ScheduledAction> actions = ActionManager.pendingActions(user, null);
        assertEquals(ActionFactory.TYPE_VIRTUALIZATION_SET_VCPUS.getName(),
                     actions.get(0).getTypeName());

        Action action = ActionManager.lookupAction(user, actions.get(0).getId());
        VirtualizationSetVcpusAction virtAction = (VirtualizationSetVcpusAction)action;
        assertEquals(vcpus, virtAction.getVcpu());

        // Check the response
        Map<String, Long> model = GSON.fromJson(json, new TypeToken<Map<String, Long>>() {}.getType());
        assertEquals(action.getId(), model.get(guest.getUuid()));
    }

    /**
     * Test a VM vcpu without value change action
     *
     * @throws Exception if anything unexpected happens during the test
     */
    public void testSetVcpuInvalidAction() throws Exception {
        VirtualInstance guest = host.getGuests().iterator().next();
        Long sid = host.getId();

        try {
            VirtualGuestsController.action(
                    getPostRequestWithCsrfAndBody("/manager/api/systems/details/virtualization/guests/:sid/:action",
                                                  "{uuids: [\"" + guest.getUuid() + "\"]}",
                                                  sid, "setVcpu"),
                    response, user);
            fail();
        }
        catch (HaltException e) {
            // Make sure the action was not queued
            assertTrue(ActionManager.pendingActions(user, null).isEmpty());
        }
    }

    /**
     * Test a memory change on several VMs
     *
     * @throws Exception if anything unexpected happens during the test
     */
    @SuppressWarnings("unchecked")
    public void testSetMemMultiAction() throws Exception {

        VirtualInstance[] guests = host.getGuests().toArray(new VirtualInstance[host.getGuests().size()]);
        Arrays.sort(guests, (VirtualInstance o1, VirtualInstance o2) -> o1.getUuid().compareTo(o2.getUuid()));
        Long sid = host.getId();

        Integer mem = 2048;
        String json = VirtualGuestsController.action(
                getPostRequestWithCsrfAndBody("/manager/api/systems/details/virtualization/guests/:sid/:action",
                                              "{uuids: [\"" + guests[0].getUuid() + "\", " +
                                                       "\"" + guests[1].getUuid() + "\"], " +
                                                      "value: " + mem + "}",
                                              sid, "setMemory"),
                response, user);

        // Make sure the setVpu action was queued
        DataResult<ScheduledAction> scheduledActions = ActionManager.pendingActions(user, null);
        ArrayList<VirtualizationSetMemoryAction> virtActions = new ArrayList<VirtualizationSetMemoryAction>();
        scheduledActions.stream().forEach(action -> virtActions.add(
                (VirtualizationSetMemoryAction)ActionManager.lookupAction(user, action.getId())));
        virtActions.sort((VirtualizationSetMemoryAction a1, VirtualizationSetMemoryAction a2) ->
                a1.getUuid().compareTo(a2.getUuid()));

        assertEquals(ActionFactory.TYPE_VIRTUALIZATION_SET_MEMORY.getName(),
                scheduledActions.get(0).getTypeName());
        assertEquals(Integer.valueOf(mem), virtActions.get(0).getMemory());
        assertEquals(guests[0].getUuid(), virtActions.get(0).getUuid());

        assertEquals(ActionFactory.TYPE_VIRTUALIZATION_SET_MEMORY.getName(),
                scheduledActions.get(1).getTypeName());
        assertEquals(Integer.valueOf(mem), virtActions.get(1).getMemory());
        assertEquals(guests[1].getUuid(), virtActions.get(1).getUuid());


        // Check the response
        Map<String, Long> model = GSON.fromJson(json, new TypeToken<Map<String, Long>>() {}.getType());
        assertEquals(virtActions.get(0).getId(), model.get(guests[0].getUuid()));
        assertEquals(virtActions.get(1).getId(), model.get(guests[1].getUuid()));
    }

    /**
     * Creates a request with csrf token.
     *
     * @param uri the uri
     * @param vars the vars
     * @return the request with csrf
     */
    private Request getRequestWithCsrf(String uri, Object... vars) {
        Request request = SparkTestUtils.createMockRequest(baseUri + uri, vars);
        request.session(true).attribute("csrf_token", "bleh");
        return request;
    }
    /**
     * Creates a request with csrf token.
     *
     * @param uri the uri
     * @param vars the vars
     * @return the request with csrf
     */
    private Request getPostRequestWithCsrfAndBody(String uri, String body,
                                                  Object... vars) throws UnsupportedEncodingException {
        Request request = SparkTestUtils.createMockRequestWithBody(baseUri + uri, Collections.emptyMap(), body, vars);
        request.session(true).attribute("csrf_token", "bleh");
        return request;
    }
}
