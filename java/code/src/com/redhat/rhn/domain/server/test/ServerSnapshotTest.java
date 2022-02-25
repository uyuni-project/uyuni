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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.server.test;

import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.server.ServerSnapshot;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.ServerTestUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * Test class for ServerSnapshot
 */
public class ServerSnapshotTest extends BaseTestCaseWithUser {

    public void testRollbackGroups() throws Exception {
        Server server = ServerTestUtils.createTestSystem(user);

        assertNotEmpty(server.getGroups());

        ServerSnapshot snapshot = new ServerSnapshot();
        snapshot.setServer(server);
        snapshot.setOrg(server.getOrg());
        snapshot.setReason("snapshotReason");

        Set<ServerGroup> serverGroupsForSnapshot = new HashSet<>();
        serverGroupsForSnapshot.add(ServerGroupFactory.create("serverGroupName1", "serverGroupDescription1",
                server.getOrg()));
        serverGroupsForSnapshot.add(ServerGroupFactory.create("serverGroupName2", "serverGroupDescription2",
                server.getOrg()));
        snapshot.setGroups(serverGroupsForSnapshot);

        assertFalse(serverGroupsForSnapshot.equals(server.getGroups()));

        snapshot.rollbackGroups();

        assertEquals(server.getGroups(), serverGroupsForSnapshot);
    }

}
