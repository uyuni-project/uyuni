/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.action.server;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionTypeEnum;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactoryTest;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.BaseTestCase;
import com.redhat.rhn.testing.SaltTestCaseExtension;
import com.redhat.rhn.testing.UserForTest;
import com.redhat.rhn.testing.UserForTestCaseExtension;

import com.suse.manager.maintenance.MaintenanceTestUtils;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Date;
import java.util.List;

@ExtendWith(UserForTestCaseExtension.class)
@ExtendWith(SaltTestCaseExtension.class)
public class ServerActionFactoryTest extends BaseTestCase {

    @UserForTest(useClassNameForOrg = true)
    protected User user;

    private Server testServer;
    private Server anotherTestServer;
    private final String testActionTypeName = "Patch Update";
    private Date createdBeforeTime;
    private Date createdLaterTime;

    private void createListServerActionsForServerTest() throws Exception {
        testServer = ServerFactoryTest.createTestServer(user);
        MaintenanceTestUtils.createActionForServerAt(user, ActionTypeEnum.TYPE_ERRATA, testServer,
                "2026-03-10T08:00:00.000Z");
        createdBeforeTime = new Date(System.currentTimeMillis() - 10_000);
        createdLaterTime = new Date(System.currentTimeMillis() + 10_000);

        anotherTestServer = ServerFactoryTest.createTestServer(user);
        MaintenanceTestUtils.createActionForServerAt(user, ActionTypeEnum.TYPE_ERRATA, anotherTestServer,
                "2026-03-10T08:00:00.000Z");
    }


    @Test
    @DisplayName("listServerActionsForServer is behaving correctly")
    void testListServerActionsForServer() throws Exception {
        createListServerActionsForServerTest();

        List<ServerAction> actionsForServer = ServerActionFactory.listServerActionsForServer(testServer);
        assertEquals(1, actionsForServer.size());
    }

    @Test
    @DisplayName("listServerActionsForServer filtered by action name and created date is behaving correctly")
    void testListServerActionsForServerWithActionNameAndCreatedDate() throws Exception {
        createListServerActionsForServerTest();

        List<ServerAction> actionsForServer =
                ServerActionFactory.listServerActionsForServer(testServer, testActionTypeName, createdBeforeTime);
        assertEquals(1, actionsForServer.size());
        actionsForServer =
                ServerActionFactory.listServerActionsForServer(testServer, "another type name", createdBeforeTime);
        assertEquals(0, actionsForServer.size());
        actionsForServer =
                ServerActionFactory.listServerActionsForServer(testServer, testActionTypeName, createdLaterTime);
        assertEquals(0, actionsForServer.size());
    }

    @Test
    @DisplayName("listServerActionsForServer filtered by action type is behaving correctly")
    void testListServerActionsForServerWithActionType() throws Exception {
        createListServerActionsForServerTest();

        List<ServerAction> actionsForServer =
                ServerActionFactory.listServerActionsForServer(testServer, ActionTypeEnum.TYPE_ERRATA);
        assertEquals(1, actionsForServer.size());
        actionsForServer = ServerActionFactory.listServerActionsForServer(testServer, ActionTypeEnum.TYPE_REBOOT);
        assertEquals(0, actionsForServer.size());
    }

    @Test
    @DisplayName("listServerActionsForServer filtered by action status list is behaving correctly")
    void testListServerActionsForServerWithActionStatus() throws Exception {
        createListServerActionsForServerTest();

        List<ServerAction> actionsForServer =
                ServerActionFactory.listServerActionsForServer(testServer, List.of(ActionFactory.STATUS_QUEUED));
        assertEquals(1, actionsForServer.size());
        actionsForServer =
                ServerActionFactory.listServerActionsForServer(testServer, ActionFactory.ALL_PENDING_STATUSES);
        assertEquals(1, actionsForServer.size());
        actionsForServer =
                ServerActionFactory.listServerActionsForServer(testServer, List.of(ActionFactory.STATUS_PICKED_UP));
        assertEquals(0, actionsForServer.size());
    }

    @Test
    @DisplayName("listServerActionsForServer filtered by action status and created date is behaving correctly")
    void testListServerActionsForServerWithActionStatusAndCreatedDate() throws Exception {
        createListServerActionsForServerTest();

        List<ServerAction> actionsForServer =
                ServerActionFactory.listServerActionsForServer(testServer, ActionFactory.ALL_STATUSES,
                        createdBeforeTime);
        assertEquals(1, actionsForServer.size());
        actionsForServer =
                ServerActionFactory.listServerActionsForServer(testServer, List.of(ActionFactory.STATUS_PICKED_UP),
                        createdBeforeTime);
        assertEquals(0, actionsForServer.size());
        actionsForServer =
                ServerActionFactory.listServerActionsForServer(testServer, ActionFactory.ALL_STATUSES,
                        createdLaterTime);
        assertEquals(0, actionsForServer.size());
    }

}
