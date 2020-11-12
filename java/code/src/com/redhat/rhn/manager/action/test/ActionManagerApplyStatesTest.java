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
package com.redhat.rhn.manager.action.test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesActionDetails;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Unit tests for the code that is used to schedule {@link ApplyStatesAction}.
 */
public class ActionManagerApplyStatesTest extends BaseTestCaseWithUser {

    /**
     * Schedule state application for a test server.
     *
     * @throws Exception in case of an error
     */
    @Test
    public void testScheduleApplyStates() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user);
        Date earliestAction = new Date();
        List<String> mods = Arrays.asList(
                ApplyStatesEventMessage.CHANNELS,
                ApplyStatesEventMessage.PACKAGES);
        ApplyStatesAction action = ActionManager.scheduleApplyStates(
                user,
                Arrays.asList(server.getId()),
                mods,
                earliestAction);

        // Look it up and verify
        ApplyStatesAction savedAction = (ApplyStatesAction) ActionFactory
                .lookupByUserAndId(user, action.getId());
        assertNotNull(savedAction);
        assertEquals(ActionFactory.TYPE_APPLY_STATES, savedAction.getActionType());
        assertEquals(earliestAction, savedAction.getEarliestAction());

        // Verify the details
        ApplyStatesActionDetails details = savedAction.getDetails();
        assertNotNull(details);
        assertEquals("channels,packages", details.getStates());
        assertEquals(2, details.getMods().size());
        assertEquals(ApplyStatesEventMessage.CHANNELS, details.getMods().get(0));
        assertEquals(ApplyStatesEventMessage.PACKAGES, details.getMods().get(1));

        // FIXME: Verifying server actions is a problem because plain SQL is used
        // assertEquals(1, savedAction.getServerActions().size());
    }

    /**
     * Schedule a state application with an empty list of modules.
     *
     * @throws Exception in case of an error
     */
    @Test
    public void testScheduleApplyStatesHighstate() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user);
        Date earliestAction = new Date();
        ApplyStatesAction action = ActionManager.scheduleApplyStates(
                user,
                Arrays.asList(server.getId()),
                new ArrayList<>(),
                earliestAction);

        // Look it up and verify
        ApplyStatesAction savedAction = (ApplyStatesAction) ActionFactory
                .lookupByUserAndId(user, action.getId());
        assertNotNull(savedAction);
        assertEquals(ActionFactory.TYPE_APPLY_STATES, savedAction.getActionType());
        assertEquals(earliestAction, savedAction.getEarliestAction());

        // Verify the details
        ApplyStatesActionDetails details = savedAction.getDetails();
        assertNotNull(details);
        assertNull(details.getStates());
        assertEquals(0, details.getMods().size());
        assertFalse(details.isTest());
    }

    /**
     * Schedule a state application in test-mode with an empty list of modules.
     *
     * @throws Exception in case of an error
     */
    @Test
    public void testScheduleApplyStatesHighstateTest() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user);
        Date earliestAction = new Date();
        ApplyStatesAction action = ActionManager.scheduleApplyStates(
                user,
                Arrays.asList(server.getId()),
                new ArrayList<>(),
                earliestAction,
                Optional.of(true));

        // Look up the action and verify the details
        ApplyStatesAction savedAction = (ApplyStatesAction) ActionFactory.lookupByUserAndId(user, action.getId());
        assertNotNull(savedAction);
        assertEquals(ActionFactory.TYPE_APPLY_STATES, savedAction.getActionType());
        assertEquals(earliestAction, savedAction.getEarliestAction());

        ApplyStatesActionDetails details = savedAction.getDetails();
        assertNotNull(details);
        assertNull(details.getStates());
        assertEquals(0, details.getMods().size());
        assertTrue(details.isTest());
    }

    /**
     * Schedule a state application with an empty list of modules.
     *
     * @throws Exception in case of an error
     */
    @Test
    public void testScheduleApplyHighstate() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user);
        Date earliestAction = new Date();
        ApplyStatesAction action = ActionManager.scheduleApplyHighstate(user,
                Collections.singletonList(server.getId()), earliestAction, Optional.empty());

        // Look it up and verify
        ApplyStatesAction savedAction = (ApplyStatesAction) ActionFactory
                .lookupByUserAndId(user, action.getId());
        assertNotNull(savedAction);
        assertEquals(ActionFactory.TYPE_APPLY_STATES, savedAction.getActionType());
        assertEquals(earliestAction, savedAction.getEarliestAction());

        // Verify the details
        ApplyStatesActionDetails details = savedAction.getDetails();
        assertNotNull(details);
        assertNull(details.getStates());
        assertEquals(0, details.getMods().size());
        assertFalse(details.isTest());
    }

    /**
     * Schedule a state application in test-mode with an empty list of modules.
     *
     * @throws Exception in case of an error
     */
    @Test
    public void testScheduleApplyHighstateTest() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user);
        Date earliestAction = new Date();
        ApplyStatesAction action = ActionManager.scheduleApplyHighstate(user,
                Collections.singletonList(server.getId()), earliestAction, Optional.of(true));

        // Look up the action and verify the details
        ApplyStatesAction savedAction = (ApplyStatesAction) ActionFactory.lookupByUserAndId(user, action.getId());
        assertNotNull(savedAction);
        assertEquals(ActionFactory.TYPE_APPLY_STATES, savedAction.getActionType());
        assertEquals(earliestAction, savedAction.getEarliestAction());

        ApplyStatesActionDetails details = savedAction.getDetails();
        assertNotNull(details);
        assertNull(details.getStates());
        assertEquals(0, details.getMods().size());
        assertTrue(details.isTest());
    }
}
