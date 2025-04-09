/*
 * Copyright (c) 2020 SUSE LLC
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

import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.recurringactions.RecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.test.MinionServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.recurringactions.RecurringActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;
import com.redhat.rhn.testing.ServerGroupTestUtils;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.webui.controllers.RecurringActionController;
import com.suse.manager.webui.utils.gson.RecurringActionDetailsDto;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import spark.HaltException;
import spark.Request;
import spark.RequestResponseFactory;
import spark.Response;

/**
 * Test for {@link RecurringActionController}
 */
public class RecurringActionControllerTest extends BaseControllerTestCase {

    private Response response;

    private MinionServer minionServer;

    private static final Gson GSON = new GsonBuilder().create();

    private TaskomaticApi taskomaticMock;

    {
        context().setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        user.addPermanentRole(RoleFactory.ORG_ADMIN);

        response = RequestResponseFactory.create(new RhnMockHttpServletResponse());

        minionServer = MinionServerFactoryTest.createTestMinionServer(user);

        // mocking
        taskomaticMock = context().mock(TaskomaticApi.class);
        RecurringActionManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
            allowing(taskomaticMock).unscheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
        } });
    }

    @Test
    public void testCreateOrgHighstateAction() throws UnsupportedEncodingException {
        Long orgId = user.getOrg().getId();
        String actionName = "org-highstate-1";
        createOrgHighstateAction(orgId, actionName);

        var list = listRecurringActions("ORG", orgId);
        assertEquals(1, list.size());

        Map<String, Object> action = list.iterator().next();
        var details = getDetails(action);

        assertEquals(orgId, extractTargetId(action));
        assertEquals(actionName, action.get("scheduleName"));
        assertEquals("HIGHSTATE", action.get("actionType"));
        assertEquals(user.getLogin(), details.getCreatorLogin());
    }

    @Test
    public void testCreateMinionHighstateAction() throws Exception {
        String actionName = "minion-highstate-1";
        createMinionHighstateAction(minionServer.getId(), actionName);
        var list = listRecurringActions("MINION", minionServer.getId());
        assertEquals(1, list.size());

        var action = list.get(0);
        assertEquals(minionServer.getId(), extractTargetId(action));
        assertEquals(actionName, action.get("scheduleName"));
        assertEquals("HIGHSTATE", action.get("actionType"));
    }

    @Test
    public void testCreateGroupHighstateAction() throws Exception {
        ManagedServerGroup group = ServerGroupFactory.create(
            ServerGroupTestUtils.NAME,
            ServerGroupTestUtils.DESCRIPTION,
            user.getOrg()
        );
        String actionName = "group-highstate-1";
        createGroupHighstateAction(group.getId(), actionName);
        var list = listRecurringActions("GROUP", group.getId());
        assertEquals(1, list.size());

        var action = list.get(0);
        assertEquals(group.getId(), extractTargetId(action));
        assertEquals(actionName, action.get("scheduleName"));
        assertEquals("HIGHSTATE", action.get("actionType"));
    }

    @Test
    public void testGetStatesConfigInternalStates() {
        var request = getRequestWithCsrf("/manager/api/recurringactions/states");
        var states = RecurringActionController.getStatesConfig(request, response, user);
        assertTrue(states.contains("hardware.profileupdate"));
        assertTrue(states.contains("packages.profileupdate"));
        assertTrue(states.contains("util.syncstates"));
        assertTrue(states.contains("util.syncgrains"));
        assertTrue(states.contains("util.syncmodules"));
        assertTrue(states.contains("uptodate"));
    }

    @Test
    public void testGetStatesConfigActionStates() throws Exception {
        String actionName = "minion-custom-state-1";
        String stateName = "packages.profileupdate";
        createMinionCustomStateAction(minionServer.getId(), actionName, stateName);
        var action = listRecurringActions("MINION", minionServer.getId()).get(0);
        var actionDetails = getDetails(action);
        assertEquals(1, actionDetails.getStates().size());
        assertEquals(stateName, actionDetails.getStates().iterator().next().getName());

        var params = Map.of("id", extractActionId(action).toString());
        var request = getRequestWithCsrfAndParams("/manager/api/recurringactions/states", params);
        var states = RecurringActionController.getStatesConfig(request, response, user);
        assertTrue(states.contains(stateName));
    }

    @Test
    public void testGetStatesConfigHighstateAction() throws UnsupportedEncodingException {
        Long orgId = user.getOrg().getId();
        createOrgHighstateAction(orgId, "org-highstate-3");
        Long actionId = extractActionId(listRecurringActions("ORG", orgId).get(0));

        // Should throw an exception when listing config states for a highstate action
        var params = Map.of("id", actionId.toString());
        var request = getRequestWithCsrfAndParams("/manager/api/recurringactions/states", params);
        try {
            RecurringActionController.getStatesConfig(request, response, user);
            fail("An exception should have been thrown");
        }
        catch (HaltException e) {
            assertEquals(400, e.statusCode());
        }
    }

    @Test
    public void testGetStatesConfigInvalidActionId() {
        var params = Map.of("id", "53");
        var request = getRequestWithCsrfAndParams("/manager/api/recurringactions/states", params);
        var states = RecurringActionController.getStatesConfig(request, response, user);

        // Trying to list config states of an action that doesn't exist.
        assertTrue(states.contains("Action 53 not found"));
    }

    @Test
    public void testCreateActionSameName() throws UnsupportedEncodingException {
        Long orgId = user.getOrg().getId();

        var actionJsonString = createActionJsonString(
            empty(),
            "test-schedule-123",
            of(orgId),
            "org",
            "HIGHSTATE"
        );
        var saveRequest = getPostRequestWithCsrfAndBody("/manager/api/recurringactions/save", actionJsonString);
        RecurringActionController.save(saveRequest, response, user);

        try {
            RecurringActionController.save(saveRequest, response, user);
            fail("An exception should have been thrown");
        }
        catch (HaltException e) {
            assertEquals(400, e.statusCode());
        }
    }

    @Test
    public void testUpdateAction() throws UnsupportedEncodingException {
        var org = OrgFactory.createOrg();
        org.setName("test org: " + TestUtils.randomString());
        org = OrgFactory.save(org);
        user.addPermanentRole(RoleFactory.SAT_ADMIN);

        var orgId = org.getId();
        var actionJsonString = createActionJsonString(
            empty(),
            "test-schedule-123",
            of(orgId),
            "org",
            "HIGHSTATE"
        );
        var createRequest = getPostRequestWithCsrfAndBody("/manager/api/recurringactions/save", actionJsonString);
        RecurringActionController.save(createRequest, response, user);

        var actionId = RecurringActionFactory.listOrgRecurringActions(orgId).iterator().next().getId();

        var updateJsonStr = createActionJsonString(
            of(actionId),
            "new-name-123", empty(),
            "org",
            "HIGHSTATE"
        );
        var updateRequest = getPostRequestWithCsrfAndBody("/manager/api/recurringactions/save", updateJsonStr);
        RecurringActionController.save(updateRequest, response, user);

        var updated = RecurringActionFactory.listOrgRecurringActions(orgId).iterator().next();
        assertEquals("new-name-123", updated.getName());
    }

    @Test
    public void testDeleteSchedule() throws Exception {
        // Should throw an exception when trying to delete an action that doesn't exist.
        var request = getRequestWithCsrf("/manager/api/recurringactions/:id/delete", "55");
        try {
            RecurringActionController.deleteSchedule(request, response, user);
            fail("An exception should have been thrown");
        }
        catch (HaltException e) {
            assertEquals(400, e.statusCode());
        }

        createMinionHighstateAction(minionServer.getId(), "minion-schedule-to-be-deleted");
        var list = listRecurringActions("MINION", minionServer.getId());
        assertEquals(1, list.size());

        var actionId = extractActionId(list.get(0));
        request = getRequestWithCsrf("/manager/api/recurringactions/:id/delete", actionId);
        RecurringActionController.deleteSchedule(request, response, user);

        list = listRecurringActions("MINION", minionServer.getId());
        assertTrue(list.isEmpty());
    }

    private Request makeSaveRequest(
        String name,
        String targetType,
        Optional<Long> targetId,
        String actionType,
        Optional<String> stateName
    ) throws UnsupportedEncodingException {
        var actionJsonString = createActionJsonString(empty(), name, targetId, targetType, actionType, stateName);
        return getPostRequestWithCsrfAndBody("/manager/api/recurringactions/save", actionJsonString);
    }

    private Request makeSaveRequest(
        String name,
        String targetType,
        Optional<Long> targetId,
        String actionType
    ) throws UnsupportedEncodingException {
        var actionJsonString = createActionJsonString(empty(), name, targetId, targetType, actionType);
        return getPostRequestWithCsrfAndBody("/manager/api/recurringactions/save", actionJsonString);
    }

    private static String createActionJsonString(
        Optional<Long> actionId,
        String scheduleName,
        Optional<Long> targetId,
        String targetType,
        String actionType
    ) {
        return createActionJsonString(actionId, scheduleName, targetId, targetType, actionType, Optional.empty());
    }

    private static String createActionJsonString(
        Optional<Long> actionId,
        String scheduleName,
        Optional<Long> targetId,
        String targetType,
        String actionType,
        Optional<String> stateName
    ) {
        return "{" +
                actionId.map(id -> "'recurringActionId': " + id + ",").orElse("") +
                "'scheduleName': '" + scheduleName + "'," +
                targetId.map(id -> "'targetId': " + id + ",").orElse("") +
                "'targetType': '" + targetType + "'," +
                "'actionType': '" + actionType + "'," +
                "'details': {" +
                    createStatesJsonString(actionType, stateName) +
                    "'type': 'hourly'," +
                    "'cronTimes': " +
                    "  {'minute': '0'," +
                    "   'hour': '0'," +
                    "   'dayOfWeek': '0'," +
                    "   'dayOfMonth': '0'" +
                    "  }" +
                "  }" +
                "}";
    }

    private static String createStatesJsonString(String actionType, Optional<String> stateName) {
        if ("HIGHSTATE".equals(actionType)) {
            return "";
        }

        return "'states': [" +
                    "{ 'type': 'internal_state', 'name': '" + stateName.get() + "', 'position': 0 }" +
                "],";
    }

    private List<Map<String, Object>> listRecurringActions(String type, Long targetId) {
        var listRequest = getRequestWithCsrf("/manager/api/recurringactions/:type/:id", type, targetId);
        return GSON.fromJson(RecurringActionController.listByEntity(listRequest, response, user), List.class);
    }

    private void createMinionHighstateAction(Long minionId, String name) throws Exception {
        var saveRequest = makeSaveRequest(name, "minion", of(minionId), "HIGHSTATE");
        RecurringActionController.save(saveRequest, response, user);
    }

    private void createMinionCustomStateAction(Long minionId, String name, String stateName) throws Exception {
        var saveRequest = makeSaveRequest(name, "minion", of(minionId), "CUSTOMSTATE", of(stateName));
        RecurringActionController.save(saveRequest, response, user);
    }

    private void createGroupHighstateAction(Long groupId, String name) throws Exception {
        var saveRequest = makeSaveRequest(name, "group", of(groupId), "HIGHSTATE");
        RecurringActionController.save(saveRequest, response, user);
    }
    private void createOrgHighstateAction(Long orgId, String name) throws UnsupportedEncodingException {
        var saveRequest = makeSaveRequest(name, "org", of(orgId), "HIGHSTATE");
        RecurringActionController.save(saveRequest, response, user);
    }

    private RecurringActionDetailsDto getDetails(Map<String, Object> action) {
        Long actionId = extractActionId(action);
        var request = getRequestWithCsrf("/manager/api/recurringactions/:id/details", actionId);
        return GSON.fromJson(
            RecurringActionController.getDetails(request, response, user), RecurringActionDetailsDto.class
        );
    }

    private Long extractTargetId(Map<String, Object> action) {
        return (long) Double.parseDouble(action.get("targetId").toString());
    }

    private Long extractActionId(Map<String, Object> action) {
        return (long) Double.parseDouble(action.get("recurringActionId").toString());
    }

}
