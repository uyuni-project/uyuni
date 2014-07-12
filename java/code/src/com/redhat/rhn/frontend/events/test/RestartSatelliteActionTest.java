/**
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.frontend.events.test;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.events.RestartSatelliteAction;
import com.redhat.rhn.frontend.events.RestartSatelliteEvent;
import com.redhat.rhn.manager.satellite.RestartCommand;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

public class RestartSatelliteActionTest extends BaseTestCaseWithUser {

    private TestRestartCommand command;

    public void testAction() throws Exception {
        user.addPermanentRole(RoleFactory.SAT_ADMIN);
        command = new TestRestartCommand(user);
        RestartSatelliteEvent event = new RestartSatelliteEvent(user);
        RestartSatelliteAction action = new RestartSatelliteAction() {
            protected RestartCommand getCommand(User currentUser) {
                return command;
            }

        };
        action.execute(event);
        assertTrue(command.stored);
    }

    public class TestRestartCommand extends RestartCommand {
        private boolean stored = false;
        public TestRestartCommand(User userIn) {
            super(userIn);
        }

        /**
         * {@inheritDoc}
         */
        public ValidatorError[] storeConfiguration() {
            stored = true;
            return null;
        }

    }
}
