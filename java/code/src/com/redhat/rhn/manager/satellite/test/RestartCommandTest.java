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
package com.redhat.rhn.manager.satellite.test;

import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.manager.satellite.Executor;
import com.redhat.rhn.manager.satellite.RestartCommand;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

/**
 * ConfigureSatelliteCommandTest - test for ConfigureSatelliteCommand
 * @version $Rev$
 */
public class RestartCommandTest extends BaseTestCaseWithUser {

    private RestartCommand cmd;

    public void testCreateCommand() throws Exception {
        user.addPermanentRole(RoleFactory.SAT_ADMIN);
        cmd = new RestartCommand(user) {
            protected Executor getExecutor() {
                return new TestExecutor();
            }
        };
        assertNotNull(cmd.getUser());
        assertNull(cmd.storeConfiguration());
    }

    /**
     * TestExecutor -
     * @version $Rev$
    */
    public class TestExecutor implements Executor {

        public int execute(String[] args) {
            if (args.length != 2) {
                return -1;
            }
            if (!args[0].equals("/usr/bin/sudo")) {
                return -2;
            }
            else if (!args[1].equals("/usr/sbin/rhn-sat-restart-silent")) {
                return -3;
            }
            else {
                return 0;
            }
        }

        public String getLastCommandOutput() {
            return null;
        }

        public String getLastCommandErrorMessage() {
            return null;
        }
    }

}

