/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.api.test.contract;

import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ActionedSystem;
import com.redhat.rhn.frontend.dto.ScheduledAction;
import com.redhat.rhn.frontend.xmlrpc.schedule.ScheduleHandler;

import org.jmock.Expectations;
import org.junit.jupiter.api.Test;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ScheduleHandlerContractTest extends BaseOpenApiTest {

    @Override
    protected String getApiNamespace() {
        return "schedule";
    }

    @Override
    protected Class<ScheduleHandler> getHandlerClass() {
        return ScheduleHandler.class;
    }

    private ScheduleHandler handler() {
        return (ScheduleHandler) handlerMock;
    }

    private ScheduledAction scheduledAction() {
        ScheduledAction action = new ScheduledAction();
        action.setId(10L);
        action.setActionName("Package Install");
        action.setTypeName("Package Install");
        action.setSchedulerName("admin");
        action.setEarliest(new Date());
        action.setPrerequisite(9L);
        action.setCompletedSystems(2L);
        action.setFailedSystems(0L);
        action.setInProgressSystems(1L);
        return action;
    }

    private ActionedSystem actionedSystem() {
        ActionedSystem system = new ActionedSystem();
        system.setId(1000010000L);
        system.setServerName("test-system.example.com");
        system.setChannelLabels("sle-product-sles15-sp6-pool-x86_64");
        system.setDisplayDate(new Date());
        system.setMessage("Action completed successfully");
        return system;
    }

    @Test
    public void testListAllActions() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listAllActions(with(mockUser));
            will(returnValue(new Object[] {scheduledAction()}));
        }});

        validateApiContract("/schedule/listAllActions", "GET")
                .onHandlerMethod("listAllActions", User.class);
    }

    @Test
    public void testListCompletedActions() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listCompletedActions(with(mockUser));
            will(returnValue(new Object[] {scheduledAction()}));
        }});

        validateApiContract("/schedule/listCompletedActions", "GET")
                .onHandlerMethod("listCompletedActions", User.class);
    }

    @Test
    public void testListInProgressActions() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listInProgressActions(with(mockUser));
            will(returnValue(new Object[] {scheduledAction()}));
        }});

        validateApiContract("/schedule/listInProgressActions", "GET")
                .onHandlerMethod("listInProgressActions", User.class);
    }

    @Test
    public void testListFailedActions() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listFailedActions(with(mockUser));
            will(returnValue(new Object[] {scheduledAction()}));
        }});

        validateApiContract("/schedule/listFailedActions", "GET")
                .onHandlerMethod("listFailedActions", User.class);
    }

    @Test
    public void testListArchivedActions() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listArchivedActions(with(mockUser));
            will(returnValue(new Object[] {scheduledAction()}));
        }});

        validateApiContract("/schedule/listArchivedActions", "GET")
                .onHandlerMethod("listArchivedActions", User.class);
    }

    @Test
    public void testListAllArchivedActions() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listAllArchivedActions(with(mockUser));
            will(returnValue(new Object[] {scheduledAction()}));
        }});

        validateApiContract("/schedule/listAllArchivedActions", "GET")
                .onHandlerMethod("listAllArchivedActions", User.class);
    }

    @Test
    public void testListAllCompletedActions() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listAllCompletedActions(with(mockUser));
            will(returnValue(new Object[] {scheduledAction()}));
        }});

        validateApiContract("/schedule/listAllCompletedActions", "GET")
                .onHandlerMethod("listAllCompletedActions", User.class);
    }

    @Test
    public void testListCompletedSystems() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listCompletedSystems(with(mockUser), with(10));
            will(returnValue(new Object[] {actionedSystem()}));
        }});

        validateApiContract("/schedule/listCompletedSystems", "GET")
                .withParams(Map.of("actionId", new String[] {"10"}))
                .onHandlerMethod("listCompletedSystems", User.class, Integer.class);
    }

    @Test
    public void testListInProgressSystems() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listInProgressSystems(with(mockUser), with(10));
            will(returnValue(new Object[] {actionedSystem()}));
        }});

        validateApiContract("/schedule/listInProgressSystems", "GET")
                .withParams(Map.of("actionId", new String[] {"10"}))
                .onHandlerMethod("listInProgressSystems", User.class, Integer.class);
    }

    @Test
    public void testListFailedSystems() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).listFailedSystems(with(mockUser), with(10));
            will(returnValue(new Object[] {actionedSystem()}));
        }});

        validateApiContract("/schedule/listFailedSystems", "GET")
                .withParams(Map.of("actionId", new String[] {"10"}))
                .onHandlerMethod("listFailedSystems", User.class, Integer.class);
    }

    @Test
    public void testCancelActions() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).cancelActions(with(mockUser), with(List.of(10, 11)));
            will(returnValue(1));
        }});

        validateApiContract("/schedule/cancelActions", "POST")
                .withBody(Map.of("actionIds", List.of(10, 11)))
                .onHandlerMethod("cancelActions", User.class, List.class);
    }

    @Test
    public void testArchiveActions() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).archiveActions(with(mockUser), with(List.of(10)));
            will(returnValue(1));
        }});

        validateApiContract("/schedule/archiveActions", "POST")
                .withBody(Map.of("actionIds", List.of(10)))
                .onHandlerMethod("archiveActions", User.class, List.class);
    }

    @Test
    public void testDeleteActions() throws Exception {
        context.checking(new Expectations() {{
            oneOf(handler()).deleteActions(with(mockUser), with(List.of(10)));
            will(returnValue(1));
        }});

        validateApiContract("/schedule/deleteActions", "POST")
                .withBody(Map.of("actionIds", List.of(10)))
                .onHandlerMethod("deleteActions", User.class, List.class);
    }

    @Test
    public void testRescheduleActions() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("actionIds", List.of(10));
        body.put("onlyFailed", true);

        context.checking(new Expectations() {{
            oneOf(handler()).rescheduleActions(with(mockUser), with(List.of(10)), with(true));
            will(returnValue(1));
        }});

        validateApiContract("/schedule/rescheduleActions", "POST")
                .withBody(body)
                .onHandlerMethod("rescheduleActions", User.class, List.class, boolean.class);
    }

    /**
     * message is optional, so a request carrying only the required properties must still
     * satisfy the documented schema. RouteFactory dispatches on the exact set of body keys,
     * so this body reaches the shorter of the two handler overloads.
     */
    @Test
    public void testFailSystemActionWithoutMessage() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sid", 1000010000);
        body.put("actionId", 10);

        context.checking(new Expectations() {{
            oneOf(handler()).failSystemAction(with(mockUser), with(1000010000), with(10));
            will(returnValue(1));
        }});

        validateApiContract("/schedule/failSystemAction", "POST")
                .withBody(body)
                .onHandlerMethod("failSystemAction", User.class, Integer.class, Integer.class);
    }

    @Test
    public void testFailSystemActionWithMessage() throws Exception {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("sid", 1000010000);
        body.put("actionId", 10);
        body.put("message", "failed by the administrator");

        context.checking(new Expectations() {{
            oneOf(handler()).failSystemAction(with(mockUser), with(1000010000), with(10),
                    with("failed by the administrator"));
            will(returnValue(1));
        }});

        validateApiContract("/schedule/failSystemAction", "POST")
                .withBody(body)
                .onHandlerMethod("failSystemAction", User.class, Integer.class, Integer.class, String.class);
    }
}
