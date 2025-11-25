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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.frontend.xmlrpc.recurringaction.test;

import static com.redhat.rhn.domain.recurringactions.type.RecurringActionType.ActionType.CUSTOMSTATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.recurringactions.RecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;
import com.redhat.rhn.domain.recurringactions.state.RecurringStateConfig;
import com.redhat.rhn.domain.recurringactions.type.RecurringState;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.recurringaction.NoSuchStateException;
import com.redhat.rhn.frontend.xmlrpc.recurringaction.RecurringCustomStateHandler;
import com.redhat.rhn.manager.recurringactions.RecurringActionManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.testing.ConfigTestUtils;
import com.redhat.rhn.testing.JMockBaseTestCaseWithUser;

import org.jmock.Expectations;
import org.jmock.imposters.ByteBuddyClassImposteriser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

/**
 * Test for {@link RecurringCustomStateHandler}
 */
public class RecurringCustomStateHandlerTest extends JMockBaseTestCaseWithUser {

    private RecurringCustomStateHandler handler;
    private static final String TEST_CRON_EXPR = "0 * * * * ?";

    private TaskomaticApi taskomaticMock;
    {
        context().setImposteriser(ByteBuddyClassImposteriser.INSTANCE);
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

        user.addPermanentRole(RoleFactory.ORG_ADMIN);
        handler = new RecurringCustomStateHandler();

        // mocking
        taskomaticMock = context().mock(TaskomaticApi.class);
        RecurringActionManager.setTaskomaticApi(taskomaticMock);
        context().checking(new Expectations() { {
            allowing(taskomaticMock).scheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
            allowing(taskomaticMock).unscheduleRecurringAction(with(any(RecurringAction.class)), with(any(User.class)));
        } });
    }

    @Test
    public void testCreateInternalStates() {
        int actionId = handler.create(user, Map.of(
                "entity_id", user.getOrg().getId().intValue(),
                "entity_type", "org",
                "name", "test-action",
                "cron_expr", TEST_CRON_EXPR,
                "states", List.of("certs", "channels", "packages.profileupdate")
        ));
        RecurringAction action = RecurringActionFactory.lookupById(actionId).get();

        assertEquals(CUSTOMSTATE, action.getActionType());
        RecurringState state = (RecurringState) action.getRecurringActionType();

        assertEquals(3, state.getStateConfig().size());
        assertTrue(state.getStateConfig().stream().anyMatch(
                s ->"certs".equals(s.getStateName()) && s.getPosition().equals(1L)));
        assertTrue(state.getStateConfig().stream().anyMatch(
                s -> "channels".equals(s.getStateName()) && s.getPosition().equals(2L)));
        assertTrue(state.getStateConfig().stream().anyMatch(
                s -> "packages.profileupdate".equals(s.getStateName()) && s.getPosition().equals(3L)));
    }

    @Test
    public void testCreateNonExistingStates() {
        NoSuchStateException e = assertThrows(NoSuchStateException.class, () -> handler.create(user, Map.of(
                "entity_id", user.getOrg().getId().intValue(),
                "entity_type", "org",
                "name", "test-action",
                "cron_expr", TEST_CRON_EXPR,
                "states", List.of("certs", "foo")
        )));

        assertEquals("The state 'foo' does not exist or is not accessible to the user.", e.getMessage());
    }

    @Test
    public void testCreateConfigStates() {
        ConfigTestUtils.createConfigChannel(user.getOrg(), "My channel", "my-channel");
        int actionId = handler.create(user, Map.of(
                "entity_id", user.getOrg().getId().intValue(),
                "entity_type", "org",
                "name", "test-action",
                "cron_expr", TEST_CRON_EXPR,
                "states", List.of("certs", "my-channel", "channels")
        ));
        RecurringState state =
                (RecurringState) RecurringActionFactory.lookupById(actionId).get().getRecurringActionType();
        String configStateWithPrefix = "manager_org_" + user.getOrg().getId() + ".my-channel";

        assertEquals(3, state.getStateConfig().size());
        assertTrue(state.getStateConfig().stream().anyMatch(
                s ->"certs".equals(s.getStateName()) && s.getPosition().equals(1L)));
        assertTrue(state.getStateConfig().stream().anyMatch(
                s -> configStateWithPrefix.equals(s.getStateName()) && s.getPosition().equals(2L)));
        assertTrue(state.getStateConfig().stream().anyMatch(
                s -> "channels".equals(s.getStateName()) && s.getPosition().equals(3L)));
    }

    @Test
    public void testCreateInaccessibleStates() {
        Org org = OrgFactory.createOrg();
        org.setName("Not My Org");
        org = OrgFactory.save(org);
        ConfigTestUtils.createConfigChannel(org, "My channel", "my-channel");

        NoSuchStateException e = assertThrows(NoSuchStateException.class, () -> handler.create(user, Map.of(
                "entity_id", user.getOrg().getId().intValue(),
                "entity_type", "org",
                "name", "test-action",
                "cron_expr", TEST_CRON_EXPR,
                "states", List.of("certs", "my-channel", "channels")
        )));
        assertEquals("The state 'my-channel' does not exist or is not accessible to the user.", e.getMessage());
    }

    @Test
    public void testCreateWithStatesFromMultipleOrgs() {
        ConfigTestUtils.createConfigChannel(user.getOrg(), "My channel", "my-channel");

        Org org = OrgFactory.createOrg();
        org.setName("Not My Org");
        org = OrgFactory.save(org);
        ConfigTestUtils.createConfigChannel(org, "My channel", "my-channel");

        int actionId = handler.create(user, Map.of(
                "entity_id", user.getOrg().getId().intValue(),
                "entity_type", "org",
                "name", "test-action",
                "cron_expr", TEST_CRON_EXPR,
                "states", List.of("my-channel")
        ));
        RecurringState state =
                (RecurringState) RecurringActionFactory.lookupById(actionId).get().getRecurringActionType();

        assertEquals(1, state.getStateConfig().size());
        RecurringStateConfig stateConfig = state.getStateConfig().iterator().next();
        assertEquals("manager_org_" + user.getOrg().getId() + ".my-channel", stateConfig.getStateName());
    }

    @Test
    public void testUpdate() {
        ConfigTestUtils.createConfigChannel(user.getOrg(), "My channel", "my-channel");
        int actionId = handler.create(user, Map.of(
                "entity_id", user.getOrg().getId().intValue(),
                "entity_type", "org",
                "name", "test-action",
                "cron_expr", TEST_CRON_EXPR,
                "states", List.of("certs", "channels", "packages.profileupdate")
        ));

        handler.update(user, Map.of(
                "id", actionId,
                "states", List.of("packages.profileupdate", "my-channel")
        ));

        RecurringState state =
                (RecurringState) RecurringActionFactory.lookupById(actionId).get().getRecurringActionType();
        String configStateWithPrefix = "manager_org_" + user.getOrg().getId() + ".my-channel";

        assertEquals(2, state.getStateConfig().size());
        assertTrue(state.getStateConfig().stream().anyMatch(
                s ->"packages.profileupdate".equals(s.getStateName()) && s.getPosition().equals(1L)));
        assertTrue(state.getStateConfig().stream().anyMatch(
                s -> configStateWithPrefix.equals(s.getStateName()) && s.getPosition().equals(2L)));
    }

    @Test
    public void testListAvailable() {
        ConfigTestUtils.createConfigChannel(user.getOrg(), "My channel", "my-channel");

        Org org = OrgFactory.createOrg();
        org.setName("Not My Org");
        org = OrgFactory.save(org);
        ConfigTestUtils.createConfigChannel(org, "Not my channel", "not-my-channel");

        List<String> availableStates = handler.listAvailable(user);

        assertTrue(availableStates.contains("certs"));
        assertTrue(availableStates.contains("my-channel"));
        assertFalse(availableStates.contains("not-my-channel"));
    }
}
