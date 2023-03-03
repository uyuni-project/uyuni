/*
 * Copyright (c) 2023 SUSE LLC
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
import com.redhat.rhn.domain.kickstart.KickstartData;
import com.redhat.rhn.domain.kickstart.test.KickstartDataTest;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.test.ServerFactoryTest;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerSystemCreateCommand;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CobblerSystemCreateCommandTest extends BaseTestCaseWithUser {
    @Test
    public void testConstructorDbPersistence() {
        // Arrange
        KickstartData k = KickstartDataTest.createTestKickstartData(user.getOrg());
        Server s = ServerFactoryTest.createTestServer(user, false);

        // Act
        new CobblerSystemCreateCommand(user, k, s);

        // Assert
    }

    @Test
    public void testConstructorUnknown1() {
        // Arrange
        KickstartData k = KickstartDataTest.createTestKickstartData(user.getOrg());
        Server s = ServerFactoryTest.createTestServer(user, false);

        // Act
        new CobblerSystemCreateCommand(user, s, k, "mediaPathIn", "activationKeysIn");

        // Assert
    }

    @Test
    public void testConstructorReactivation() {
        // Arrange
        KickstartData k = KickstartDataTest.createTestKickstartData(user.getOrg());
        Server s = ServerFactoryTest.createTestServer(user, false);

        // Act
        new CobblerSystemCreateCommand(user, s, "cobblerProfileName", k);

        // Assert
    }

    @Test
    public void testConstructorCobblerPassthrough() {
        // Arrange
        KickstartData k = KickstartDataTest.createTestKickstartData(user.getOrg());
        Server s = ServerFactoryTest.createTestServer(user, false);

        // Act
        new CobblerSystemCreateCommand(user, "cobblerProfileName", k, s.getName(), 0L);

        // Assert
    }

    @Test
    public void testConstructorUnknown2() {
        // Arrange
        Server s = ServerFactoryTest.createTestServer(user, false);

        // Act
        new CobblerSystemCreateCommand(user, s, "nameIn");

        // Assert
    }

    @Test
    public void testStore() {
        // Arrange
        Server s = ServerFactoryTest.createTestServer(user, false);
        CobblerSystemCreateCommand cobblerSystemCreateCommand = new CobblerSystemCreateCommand(user, s, "nameIn");

        // Act
        ValidatorError error = cobblerSystemCreateCommand.store();

        // Assert
        Assertions.assertNull(error);
    }
}
