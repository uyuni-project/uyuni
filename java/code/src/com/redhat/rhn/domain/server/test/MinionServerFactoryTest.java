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
package com.redhat.rhn.domain.server.test;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import java.util.List;
import java.util.Optional;

/**
 * MinionServerFactoryTest
 */
public class MinionServerFactoryTest extends BaseTestCaseWithUser {

    /**
     * Test for {@link MinionServerFactory#findByMachineId(String)}.
     */
    public void testFindByMachineId() throws Exception {
        MinionServer minionServer = createTestMinionServer(user);
        Optional<MinionServer> minion = MinionServerFactory
                .findByMachineId(minionServer.getMachineId());
        assertTrue(minion.isPresent());
        assertEquals(minionServer, minion.get());
    }

    /**
     * Test for {@link MinionServerFactory#findByMinionId(String)}.
     */
    public void testFindByMinionId() throws Exception {
        MinionServer minionServer = createTestMinionServer(user);
        Optional<MinionServer> minion = MinionServerFactory
                .findByMinionId(minionServer.getMinionId());
        assertTrue(minion.isPresent());
        assertEquals(minionServer, minion.get());
    }

    /**
     * Test for {@link MinionServerFactory#listMinions()}.
     */
    public void testListMinions() throws Exception {
        MinionServer minionServer = createTestMinionServer(user);
        List<MinionServer> minions = MinionServerFactory.listMinions();
        assertTrue(minions.contains(minionServer));
    }

    /**
     * Test for {@link MinionServerFactory#lookupById(Long)}.
     */
    public void testLookupById() throws Exception {
        MinionServer minionServer = createTestMinionServer(user);
        Optional<MinionServer> minion = MinionServerFactory
                .lookupById(minionServer.getId());
        assertTrue(minion.isPresent());
        assertEquals(minionServer, minion.get());
    }

    public void testListMinionIdsAndContactMethods() throws Exception  {
        MinionServer minionServer1 = createTestMinionServer(user);
        minionServer1.setContactMethod(ServerFactory.findContactMethodByLabel("ssh-push"));
        MinionServer minionServer2 = createTestMinionServer(user);
        minionServer2.setContactMethod(ServerFactory.findContactMethodByLabel("ssh-push-tunnel"));

        List<MinionServer> minions = MinionServerFactory.listSSHMinions();
        assertEquals("ssh-push", minions.stream()
                .filter(m -> m.getId().equals(minionServer1.getId()))
                .map(m -> minionServer1.getContactMethod().getLabel())
                .findFirst().get());
        assertEquals("ssh-push-tunnel", minions.stream()
                .filter(m -> m.getId().equals(minionServer2.getId()))
                .map(m -> minionServer2.getContactMethod().getLabel())
                .findFirst().get());
    }

    /**
     * Create a {@link MinionServer} for testing.
     *
     * @return the MinionServer object
     * @throws Exception in case of an error
     */
    public static MinionServer createTestMinionServer(User owner) throws Exception {
        return (MinionServer) ServerFactoryTest.createTestServer(owner, true,
                ServerConstants.getServerGroupTypeSaltEntitled(),
                ServerFactoryTest.TYPE_SERVER_MINION);
    }
}
