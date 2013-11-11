/**
 * Copyright (c) 2013 SUSE
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
package com.redhat.rhn.manager.kickstart.cobbler.test;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerPowerSettingsUpdateCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerRebootCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerSystemCreateCommand;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;

import org.cobbler.CobblerConnection;
import org.cobbler.SystemRecord;
import org.cobbler.test.MockConnection;

/**
 * Tests Cobbler command to reboot a system.
 * @version $Rev$
 */
public class CobblerRebootCommandTest extends BaseTestCaseWithUser {

    /**
     * Tests the execution of this Cobbler command.
     * @throws Exception if unforeseen problems arise
     */
    public void testStore() throws Exception {
        CobblerConnection connection = CobblerXMLRPCHelper.getConnection("test");
        Server server = ServerTestUtils.createTestSystem(user);

        // test powering on without configuring first
        ValidatorError error = new CobblerRebootCommand(user, server).store();
        assertEquals(error.getKey(), "kickstart.powermanagement.not_configured");

        // test creating a new cobbler system profile first
        assertNull(new CobblerPowerSettingsUpdateCommand(user, server, "ipmi",
            "192.168.0.1", "user", "password", null).store());
        assertNull(new CobblerRebootCommand(user, server).store());

        String cobblerName = CobblerSystemCreateCommand.getCobblerSystemRecordName(server);
        SystemRecord systemRecord = SystemRecord.lookupByName(connection, cobblerName);
        assertEquals("power_system reboot " + systemRecord.getId(),
            MockConnection.getLatestPowerCommand());
    }
}
