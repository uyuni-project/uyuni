/*
 * Copyright (c) 2026 SUSE LLC
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

package com.redhat.rhn.frontend.xmlrpc.recurringaction;

import static com.redhat.rhn.domain.recurringactions.type.RecurringActionType.ActionType.CLM_BUILD;
import static com.redhat.rhn.domain.recurringactions.type.RecurringActionType.ActionType.CLM_PROMOTE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.contentmgmt.ContentEnvironment;
import com.redhat.rhn.domain.contentmgmt.ContentProject;
import com.redhat.rhn.domain.contentmgmt.ContentProjectFactory;
import com.redhat.rhn.domain.recurringactions.RecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;
import com.redhat.rhn.domain.recurringactions.type.RecurringContentBuildType;
import com.redhat.rhn.domain.recurringactions.type.RecurringContentPromoteType;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.InvalidArgsException;
import com.redhat.rhn.manager.recurringactions.RecurringActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Test for {@link RecurringContentLifecycleHandler}
 */
public class RecurringContentLifecycleHandlerTest extends JMockBaseTestCaseWithUser {

    private RecurringContentLifecycleHandler clmHandler;
    private static final String TEST_CRON_EXPR = "0 * * * * ?";
    private TaskomaticApi taskomaticMock;
    private ContentProject testProject;
    private ContentEnvironment testEnv;

    {
        context().setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    @BeforeEach
    public void setUp() throws Exception {
        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        clmHandler = new RecurringContentLifecycleHandler();

        testProject = new ContentProject("test-clm-proj", "Test Project", "Desc", user.getOrg());
        ContentProjectFactory.save(testProject);

        testEnv = new ContentEnvironment("dev", "Development", null, testProject);
        ContentProjectFactory.save(testEnv);
        testProject.setFirstEnvironment(testEnv);

        taskomaticMock = context().mock(TaskomaticApi.class);
        RecurringActionManager.setTaskomaticApi(taskomaticMock);
        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
            allowing(taskomaticMock).unscheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
        } });
    }

    @Test
    public void testCreateBuild() {
        int actionId = clmHandler.createBuild(user, Map.of(
                "project_label", testProject.getLabel(),
                "name", "test-build-action",
                "cron_expr", TEST_CRON_EXPR
        ));

        RecurringAction action = RecurringActionFactory.lookupById(actionId).orElseThrow();
        assertEquals(CLM_BUILD, action.getActionType());
        assertEquals("test-build-action", action.getName());
        assertEquals(RecurringAction.TargetType.NO_ENTITY, action.getTargetType());
        assertTrue(action.getRecurringActionType() instanceof RecurringContentBuildType);
        assertEquals(testProject.getId(),
                ((RecurringContentBuildType) action.getRecurringActionType()).getProject().getId());

        List<RecurringAction> projectActions = clmHandler.listByProject(user, testProject.getLabel());
        assertEquals(1, projectActions.size());
        assertEquals(actionId, projectActions.get(0).getId().intValue());
    }

    @Test
    public void testUpdateBuild() {
        int actionId = clmHandler.createBuild(user, Map.of(
                "project_label", testProject.getLabel(),
                "name", "test-build-update",
                "cron_expr", TEST_CRON_EXPR
        ));

        clmHandler.updateBuild(user, Map.of(
                "id", actionId,
                "name", "test-build-updated",
                "active", false
        ));

        RecurringAction action = RecurringActionFactory.lookupById(actionId).orElseThrow();
        assertEquals("test-build-updated", action.getName());
        assertFalse(action.isActive());
    }

    @Test
    public void testCreatePromote() {
        int actionId = clmHandler.createPromote(user, Map.of(
                "project_label", testProject.getLabel(),
                "environment_label", testEnv.getLabel(),
                "name", "test-promote-action",
                "cron_expr", TEST_CRON_EXPR
        ));

        RecurringAction action = RecurringActionFactory.lookupById(actionId).orElseThrow();
        assertEquals(CLM_PROMOTE, action.getActionType());
        assertEquals("test-promote-action", action.getName());
        assertEquals(RecurringAction.TargetType.NO_ENTITY, action.getTargetType());
        assertTrue(action.getRecurringActionType() instanceof RecurringContentPromoteType);
        RecurringContentPromoteType promoteType = (RecurringContentPromoteType) action.getRecurringActionType();
        assertEquals(testProject.getId(), promoteType.getProject().getId());
        assertEquals(testEnv.getId(), promoteType.getEnvironment().getId());

        List<RecurringAction> projActions = clmHandler.listByProject(user, testProject.getLabel());
        assertTrue(projActions.stream().anyMatch(a -> a.getId().intValue() == actionId));
    }

    @Test
    public void testUpdatePromote() {
        int actionId = clmHandler.createPromote(user, Map.of(
                "project_label", testProject.getLabel(),
                "environment_label", testEnv.getLabel(),
                "name", "test-promote-update",
                "cron_expr", TEST_CRON_EXPR
        ));

        clmHandler.updatePromote(user, Map.of(
                "id", actionId,
                "name", "test-promote-updated",
                "active", false
        ));

        RecurringAction action = RecurringActionFactory.lookupById(actionId).orElseThrow();
        assertEquals("test-promote-updated", action.getName());
        assertFalse(action.isActive());
    }

    @Test
    public void testCreateBuildMissingProject() {
        Map<String, Object> props = new HashMap<>();
        props.put("name", "missing-proj");
        props.put("cron_expr", TEST_CRON_EXPR);
        assertThrows(InvalidArgsException.class, () -> clmHandler.createBuild(user, props));
    }

    @Test
    public void testCreatePromoteMissingEnvironment() {
        Map<String, Object> props = new HashMap<>();
        props.put("project_label", testProject.getLabel());
        props.put("name", "missing-env");
        props.put("cron_expr", TEST_CRON_EXPR);
        assertThrows(InvalidArgsException.class, () -> clmHandler.createPromote(user, props));
    }
}
