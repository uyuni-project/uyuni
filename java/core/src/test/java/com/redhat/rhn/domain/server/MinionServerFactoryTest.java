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
package com.redhat.rhn.domain.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactoryTest;
import com.redhat.rhn.domain.action.ActionTypeEnum;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.testing.BaseTestCaseWithUser;
import com.redhat.rhn.testing.TestUtils;
import com.redhat.rhn.testing.UserTestUtils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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
        Action action = ActionFactoryTest.createAction(user, ActionTypeEnum.TYPE_REBOOT);
        Set<MinionServer> minionServer = action.getServerActions().stream()
                .map(ServerAction::getServer)
                .map(s -> s.asMinionServer().orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());


        ServerAction failed = ActionFactoryTest.createServerAction(minion1, action, ServerAction::setStatusFailed);
        ServerAction completed = ActionFactoryTest.createServerAction(minion2, action,
                ServerAction::setStatusCompleted);
        ServerAction queued = ActionFactoryTest.createServerAction(minion3, action, ServerAction::setStatusQueued);

        action.setServerActions(new HashSet<>(Set.of(failed, completed, queued)));

        List<MinionSummary> allSummariesExpected = Stream.concat(
                minionServer.stream(), Stream.of(minion1, minion2, minion3))
                .map(MinionSummary::new)
                .toList();
        List<MinionSummary> allSummariesActual = MinionServerFactory.findAllMinionSummaries(action.getId());

        assertEquals(allSummariesExpected.size(), allSummariesActual.size());
        assertTrue(allSummariesExpected.containsAll(allSummariesActual));
        assertTrue(allSummariesActual.containsAll(allSummariesExpected));

        List<MinionSummary> queuedSummariesExpected = new ArrayList<>(List.of(new MinionSummary(minion3)));
        queuedSummariesExpected.addAll(minionServer.stream().map(MinionSummary::new).toList());
        List<MinionSummary> queuedSummariesActual = MinionServerFactory.findQueuedMinionSummaries(action.getId());

        assertEquals(queuedSummariesExpected.size(), queuedSummariesActual.size());
        assertTrue(queuedSummariesExpected.containsAll(queuedSummariesActual));
        assertTrue(queuedSummariesActual.containsAll(queuedSummariesExpected));
    }

    @Test
    public void testFindQueuedMinionSummariesCarriesTransactionalMode() throws Exception {
        MinionServer observedTransactional = createTestMinionServer(user);
        observedTransactional.setOs(ServerConstants.SLES);
        observedTransactional.setTransactionalMode(TransactionalMode.TRANSACTIONAL);

        MinionServer observedConventional = createTestMinionServer(user);
        observedConventional.setOs(ServerConstants.SLES);
        observedConventional.setTransactionalMode(TransactionalMode.NON_TRANSACTIONAL);

        // never observed: the OS based compatibility fallback still applies
        MinionServer notObserved = createTestMinionServer(user);
        notObserved.setOs(ServerConstants.SLEMICRO);
        notObserved.setTransactionalMode(TransactionalMode.UNKNOWN);

        Action action = ActionFactoryTest.createAction(user, ActionTypeEnum.TYPE_REBOOT);
        Set<ServerAction> serverActions = new HashSet<>(action.getServerActions());
        for (MinionServer minion : List.of(observedTransactional, observedConventional, notObserved)) {
            serverActions.add(ActionFactoryTest.createServerAction(minion, action, ServerAction::setStatusQueued));
        }
        action.setServerActions(serverActions);

        Long transactionalId = observedTransactional.getId();
        Long conventionalId = observedConventional.getId();
        Long notObservedId = notObserved.getId();

        TestUtils.flushAndEvict(observedTransactional);
        TestUtils.flushAndEvict(observedConventional);
        TestUtils.flushAndEvict(notObserved);

        Map<Long, MinionSummary> summaries = MinionServerFactory.findQueuedMinionSummaries(action.getId()).stream()
                .collect(Collectors.toMap(MinionSummary::getServerId, Function.identity()));

        assertTrue(summaries.get(transactionalId).isTransactionalUpdate(),
                "observed TRANSACTIONAL on SLES must be classified as transactional");
        assertFalse(summaries.get(conventionalId).isTransactionalUpdate(),
                "observed NON_TRANSACTIONAL on SLES must be classified as conventional");
        assertTrue(summaries.get(notObservedId).isTransactionalUpdate(),
                "UNKNOWN must keep the OS based fallback");
    }

    @Test
    public void testHasTransactionalMinionsWithoutEligibleMinions() {
        Server traditional = ServerFactoryTest.createTestServer(user, true,
                ServerConstants.getServerGroupTypeEnterpriseEntitled());
        traditional.setOs(ServerConstants.SLEMICRO);
        TestUtils.flushAndEvict(traditional);

        assertTrue(traditional.asMinionServer().isEmpty(), "the fixture must not be a minion");
        assertFalse(MinionServerFactory.hasTransactionalMinions(user.getOrg().getId()),
                "a traditional system must not be considered, even on a transactional OS");
    }

    @Test
    public void testHasTransactionalMinionsWithObservedTransactionalOnSles() {
        createTransactionalModeMinion(user, ServerConstants.SLES, TransactionalMode.TRANSACTIONAL, true);

        assertTrue(MinionServerFactory.hasTransactionalMinions(user.getOrg().getId()),
                "the persisted observation must win over the OS name");
    }

    @Test
    public void testHasTransactionalMinionsWithObservedConventionalOnMicro() {
        createTransactionalModeMinion(user, ServerConstants.SLEMICRO, TransactionalMode.NON_TRANSACTIONAL, false);

        assertFalse(MinionServerFactory.hasTransactionalMinions(user.getOrg().getId()),
                "the persisted observation must win over the OS name");
    }

    @Test
    public void testHasTransactionalMinionsWithUnknownOnSles() {
        createTransactionalModeMinion(user, ServerConstants.SLES, TransactionalMode.UNKNOWN, false);

        assertFalse(MinionServerFactory.hasTransactionalMinions(user.getOrg().getId()),
                "a never observed minion on a conventional OS must not be considered");
    }

    @ParameterizedTest
    @ValueSource(strings = {ServerConstants.SLEMICRO, ServerConstants.SLMICRO, ServerConstants.LEAPMICRO,
            ServerConstants.OPENSUSEMICROOS})
    public void testHasTransactionalMinionsWithUnknownOnTransactionalOs(String os) {
        createTransactionalModeMinion(user, os, TransactionalMode.UNKNOWN, true);

        assertTrue(MinionServerFactory.hasTransactionalMinions(user.getOrg().getId()),
                "a never observed minion on " + os + " must keep the OS based fallback");
    }

    @Test
    public void testHasTransactionalMinionsIsLimitedToTheGivenOrg() {
        User otherOrgUser = UserTestUtils.createUser("otherOrgUser", "otherOrg", this);
        createTransactionalModeMinion(otherOrgUser, ServerConstants.SLEMICRO, TransactionalMode.TRANSACTIONAL, true);

        assertTrue(MinionServerFactory.hasTransactionalMinions(otherOrgUser.getOrg().getId()),
                "the transactional minion belongs to the other organization");
        assertFalse(MinionServerFactory.hasTransactionalMinions(user.getOrg().getId()),
                "a transactional minion of another organization must not leak into this one");
    }

    private static void createTransactionalModeMinion(User owner, String os, TransactionalMode mode,
                                                      boolean expectedTransactional) {
        MinionServer minion = createTestMinionServer(owner);
        minion.setOs(os);
        minion.setTransactionalMode(mode);
        TestUtils.flushAndEvict(minion);

        // the query must answer the same as the classification done in Java by the entity itself
        MinionServer reloaded = MinionServerFactory.lookupById(minion.getId()).orElseThrow();
        assertEquals(mode, reloaded.getTransactionalMode());
        assertEquals(expectedTransactional, reloaded.isTransactionalUpdate());
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
