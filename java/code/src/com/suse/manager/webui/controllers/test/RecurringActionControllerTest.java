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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.recurringactions.RecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.recurringactions.RecurringActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;
import com.redhat.rhn.testing.TestUtils;

import com.suse.manager.webui.controllers.RecurringActionController;

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
import spark.RequestResponseFactory;
import spark.Response;

/**
 * Test for {@link RecurringActionController}
 */
public class RecurringActionControllerTest extends BaseControllerTestCase {

    private Response response;

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

        // mocking
        taskomaticMock = context().mock(TaskomaticApi.class);
        RecurringActionManager.setTaskomaticApi(taskomaticMock);

        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
            allowing(taskomaticMock).unscheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
        } });
    }

    @Test
    public void testCreateAction() throws UnsupportedEncodingException {
        Long orgId = user.getOrg().getId();

        var actionJsonString = createActionJsonString(empty(), "test-schedule-123", of(orgId));
        var saveRequest = getPostRequestWithCsrfAndBody("/manager/api/recurringactions/save", actionJsonString);
        RecurringActionController.save(saveRequest, response, user);

        var listRequest = getRequestWithCsrf("/manager/api/recurringactions/:type/:id", "ORG", orgId);

        var list = GSON.fromJson(RecurringActionController.listByEntity(listRequest, response, user), List.class);
        assertEquals(1, list.size());
        Map<String, Object> action = (Map<String, Object>) list.iterator().next();
        assertNotNull(action.get("targetId"));
        assertEquals("test-schedule-123", action.get("scheduleName"));
        assertEquals(user.getLogin(), action.get("creatorLogin"));
    }

    @Test
    public void testCreateActionSameName() throws UnsupportedEncodingException {
        Long orgId = user.getOrg().getId();

        var actionJsonString = createActionJsonString(empty(), "test-schedule-123", of(orgId));
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
        var actionJsonString = createActionJsonString(empty(), "test-schedule-123", of(orgId));
        var createRequest = getPostRequestWithCsrfAndBody("/manager/api/recurringactions/save", actionJsonString);
        RecurringActionController.save(createRequest, response, user);

        var actionId = RecurringActionFactory.listOrgRecurringActions(orgId).iterator().next().getId();

        var updateJsonStr = createActionJsonString(of(actionId), "new-name-123", empty());
        var updateRequest = getPostRequestWithCsrfAndBody("/manager/api/recurringactions/save", updateJsonStr);
        RecurringActionController.save(updateRequest, response, user);

        var updated = RecurringActionFactory.listOrgRecurringActions(orgId).iterator().next();
        assertEquals("new-name-123", updated.getName());
    }

    private static String createActionJsonString(Optional<Long> actionId, String scheduleName,
                                                 Optional<Long> targetId) {
        return "{" +
                actionId.map(id -> "'recurringActionId': " + id + ",").orElse("") +
                "'scheduleName': '" + scheduleName + "'," +
                targetId.map(id -> "'targetId': " + id + ",").orElse("") +
                "'targetType': 'org'," +
                "'type': 'hourly'," +
                "'cronTimes': " +
                "  {'minute': '0'," +
                "   'hour': '0'," +
                "   'dayOfWeek': '0'," +
                "   'dayOfMonth': '0'" +
                "  }" +
                "}";
    }
}
