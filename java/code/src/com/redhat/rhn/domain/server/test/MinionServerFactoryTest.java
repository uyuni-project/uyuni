/*
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.test.ActionFactoryTest;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.ServerConstants;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.BaseTestCaseWithUser;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * MinionServerFactoryTest
 */
public class MinionServerFactoryTest extends BaseTestCaseWithUser {

    /**
     * Test for {@link MinionServerFactory#findByMachineId(String)}.
     */
    @Test
    public void testFindByMachineId() {
        MinionServer minionServer = createTestMinionServer(user);
        Optional<MinionServer> minion = MinionServerFactory.findByMachineId(minionServer.getMachineId());
        assertEquals(minionServer, minion.orElse(null));
    }

    /**
     * Test for {@link MinionServerFactory#findByMinionId(String)}.
     */
    @Test
    public void testFindByMinionId() {
        MinionServer minionServer = createTestMinionServer(user);
        Optional<MinionServer> minion = MinionServerFactory.findByMinionId(minionServer.getMinionId());
        assertEquals(minionServer, minion.orElse(null));
    }

    /**
     * Test for {@link MinionServerFactory#listMinions()}.
     */
    @Test
    public void testListMinions() {
        MinionServer minionServer = createTestMinionServer(user);
        List<MinionServer> minions = MinionServerFactory.listMinions();
        assertTrue(minions.contains(minionServer));
    }

    /**
     * Test for {@link MinionServerFactory#lookupById(Long)}.
     */
    @Test
    public void testLookupById() {
        MinionServer minionServer = createTestMinionServer(user);
        Optional<MinionServer> minion = MinionServerFactory.lookupById(minionServer.getId());
        assertEquals(minionServer, minion.orElse(null));
    }

    @Test
    public void testListMinionIdsAndContactMethods() {
        MinionServer minionServer1 = createTestMinionServer(user);
        minionServer1.setContactMethod(ServerFactory.findContactMethodByLabel("ssh-push"));
        MinionServer minionServer2 = createTestMinionServer(user);
        minionServer2.setContactMethod(ServerFactory.findContactMethodByLabel("ssh-push-tunnel"));

        List<MinionServer> minions = MinionServerFactory.listSSHMinions();
        assertEquals("ssh-push", minions.stream()
                .filter(m -> m.getId().equals(minionServer1.getId()))
                .map(m -> minionServer1.getContactMethod().getLabel())
                .findFirst().orElse(null));
        assertEquals("ssh-push-tunnel", minions.stream()
                .filter(m -> m.getId().equals(minionServer2.getId()))
                .map(m -> minionServer2.getContactMethod().getLabel())
                .findFirst().orElse(null));
    }
    @Test
    public void testListMinionsByActions() throws Exception {
        MinionServer minion1 = createTestMinionServer(user);
        MinionServer minion2 = createTestMinionServer(user);
        MinionServer minion3 = createTestMinionServer(user);

        // ActionFactoryTest.createAction() for TYPE_REBOOT create another minion Server
        // we have 4 minions in this test
        Action action = ActionFactoryTest.createAction(user, ActionFactory.TYPE_REBOOT);
        Set<MinionServer> minionServer = action.getServerActions().stream()
                .map(ServerAction::getServer)
                .map(s -> s.asMinionServer().orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());


        ServerAction failed = ActionFactoryTest.createServerAction(minion1, action, ActionFactory.STATUS_FAILED);
        ServerAction completed = ActionFactoryTest.createServerAction(minion2, action, ActionFactory.STATUS_COMPLETED);
        ServerAction queued = ActionFactoryTest.createServerAction(minion3, action, ActionFactory.STATUS_QUEUED);

        action.setServerActions(new HashSet<>(Set.of(failed, completed, queued)));

        List<MinionSummary> allSummariesExpected = Stream.concat(
                minionServer.stream(), Stream.of(minion1, minion2, minion3))
                .map(MinionSummary::new)
                .collect(Collectors.toList());
        List<MinionSummary> allSummariesActual = MinionServerFactory.findAllMinionSummaries(action.getId());

        assertEquals(allSummariesExpected.size(), allSummariesActual.size());
        assertTrue(allSummariesExpected.containsAll(allSummariesActual));
        assertTrue(allSummariesActual.containsAll(allSummariesExpected));

        List<MinionSummary> queuedSummariesExpected = new ArrayList<>(List.of(new MinionSummary(minion3)));
        queuedSummariesExpected.addAll(minionServer.stream().map(MinionSummary::new).collect(Collectors.toList()));
        List<MinionSummary> queuedSummariesActual = MinionServerFactory.findQueuedMinionSummaries(action.getId());

        assertEquals(queuedSummariesExpected.size(), queuedSummariesActual.size());
        assertTrue(queuedSummariesExpected.containsAll(queuedSummariesActual));
        assertTrue(queuedSummariesActual.containsAll(queuedSummariesExpected));
    }

    /**
     * Create a {@link MinionServer} for testing.
     *
     * @param owner the user owning the server
     * @return the MinionServer object
     */
    public static MinionServer createTestMinionServer(User owner) {
        return ServerFactoryTest.createTestServer(owner, true,
               ServerConstants.getServerGroupTypeSaltEntitled(),
               ServerFactoryTest.TYPE_SERVER_MINION).asMinionServer().orElseThrow();
    }
}
