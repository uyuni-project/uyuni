/*
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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.manager.satellite.ConfigureBootstrapCommand;
import com.redhat.rhn.manager.satellite.Executor;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import org.junit.jupiter.api.Test;

/**
 * ConfigureBootstrapCommandTest - test for ConfigureBootstrapCommand
 */
public class ConfigureBootstrapCommandTest extends BaseTestCaseWithUser {

    private ConfigureBootstrapCommand cmd;

    @Test
    public void testCreateCommand() {
        user.addPermanentRole(RoleFactory.SAT_ADMIN);
        cmd = new ConfigureBootstrapCommand(user) {
            @Override
            protected Executor getExecutor() {
                return new TestExecutor();
            }
        };

        assertNotNull(cmd.getUser());
        cmd.setHostname("localhost");
        cmd.setSslPath("/tmp/somepath.cert");
        cmd.setEnableGpg(Boolean.FALSE);
        cmd.setHttpProxy("proxy-host.redhat.com");
        cmd.setHttpProxyUsername("username");
        cmd.setHttpProxyPassword("password");
        assertNull(cmd.storeConfiguration());
    }

    /**
     * TestExecutor -
    */
    public class TestExecutor implements Executor {

        @Override
        public int execute(String[] args) {
            if (args.length != 9) {
                return -1;
            }
            if (!args[0].equals("/usr/bin/sudo")) {
                return -2;
            }
            else if (!args[1].equals("/usr/bin/rhn-bootstrap")) {
                return -3;
            }
            else if (!args[2].startsWith("--no-gpg")) {
                return -4;
            }
            else if (!args[3].startsWith("--hostname=localhost")) {
                return -5;
            }
            else if (!args[4].startsWith("--traditional")) {
                return -6;
            }
            else if (!args[5].startsWith("--ssl-cert=/tmp/somepath.cert")) {
                return -7;
            }
            else if (!args[6].startsWith("--http-proxy=proxy-host.redhat.com")) {
                return -8;
            }
            else if (!args[7].startsWith("--http-proxy-username=username")) {
                return -9;
            }
            else if (!args[8].startsWith("--http-proxy-password=password")) {
                return -10;
            }
            else {
                return 0;
            }
        }

        @Override
        public String getLastCommandOutput() {
            return null;
        }

        @Override
        public String getLastCommandErrorMessage() {
            return null;
        }
    }

}

