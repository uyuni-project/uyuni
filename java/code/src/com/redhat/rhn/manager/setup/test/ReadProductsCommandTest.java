/**
 * Copyright (c) 2014 SUSE
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
package com.redhat.rhn.manager.setup.test;

import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.manager.satellite.ReadProductsCommand;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import java.util.LinkedList;
import java.util.List;

/**
 * Tests for {@link ReadProductsCommand}.
 */
public class ReadProductsCommandTest extends BaseTestCaseWithUser {
    public void testCommandLine() throws Exception {
        user.addRole(RoleFactory.SAT_ADMIN);
        ReadProductsCommand cmd = new ReadProductsCommand(user);
        List<String> cmdLine = new LinkedList<String>();
        cmdLine.add("/usr/bin/sudo");
        cmdLine.add("/usr/sbin/mgr-ncc-sync");
        cmdLine.add("--list-products-xml");

        assertEquals(cmdLine, cmd.commandLine());
    }
}
