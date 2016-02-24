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
package com.redhat.rhn.manager.action.test;

import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesActionDetails;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;

import java.util.Arrays;
import java.util.Date;

/**
 * Unit tests for the code that is used to schedule {@link ApplyStatesAction}.
 */
public class ActionManagerApplyStatesTest extends BaseTestCaseWithUser {

    /**
     * Schedule state application for a test server.
     *
     * @throws Exception in case of an error
     */
    public void testScheduleApplyStates() throws Exception {
        Server server = ServerFactoryTest.createTestServer(user);
        Date earliestAction = new Date();
        ApplyStatesAction action = ActionManager.scheduleApplyState(
                user,
                Arrays.asList(server.getId()),
                Arrays.asList(ApplyStatesEventMessage.CHANNELS),
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
        assertEquals(ApplyStatesEventMessage.CHANNELS, details.getStates());

        // FIXME: Verifying server actions is a problem because plain SQL is used
        // assertEquals(1, savedAction.getServerActions().size());
    }
}
