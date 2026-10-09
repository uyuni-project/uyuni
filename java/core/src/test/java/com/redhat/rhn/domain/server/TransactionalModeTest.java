/*
 * Copyright (c) 2026 SUSE LLC
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

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

public class TransactionalModeTest {

    @Test
    public void testDefaultsToUnknown() {
        MinionServer minion = new MinionServer();

        assertEquals(TransactionalMode.UNKNOWN, minion.getTransactionalMode());
    }

    @Test
    public void testValidObservationUpdatesInBothDirections() {
        MinionServer minion = new MinionServer();

        minion.updateTransactionalMode(Optional.of(true));
        assertEquals(TransactionalMode.TRANSACTIONAL, minion.getTransactionalMode());

        minion.updateTransactionalMode(Optional.of(false));
        assertEquals(TransactionalMode.NON_TRANSACTIONAL, minion.getTransactionalMode());

        minion.updateTransactionalMode(Optional.of(true));
        assertEquals(TransactionalMode.TRANSACTIONAL, minion.getTransactionalMode());
    }

    @Test
    public void testMissingObservationPreservesTransactional() {
        MinionServer minion = new MinionServer();
        minion.updateTransactionalMode(Optional.of(true));

        minion.updateTransactionalMode(Optional.empty());

        assertEquals(TransactionalMode.TRANSACTIONAL, minion.getTransactionalMode());
    }

    @Test
    public void testMissingObservationPreservesNonTransactional() {
        MinionServer minion = new MinionServer();
        minion.updateTransactionalMode(Optional.of(false));

        minion.updateTransactionalMode(Optional.empty());

        assertEquals(TransactionalMode.NON_TRANSACTIONAL, minion.getTransactionalMode());
    }

    @Test
    public void testMissingObservationPreservesUnknown() {
        MinionServer minion = new MinionServer();

        minion.updateTransactionalMode(Optional.empty());

        assertEquals(TransactionalMode.UNKNOWN, minion.getTransactionalMode());
    }

    @Test
    public void testObservationPrevailsOverOperatingSystem() {
        // SLES 16.1 can be installed in both modes, so the observation has to win over the OS name
        assertTrue(classify(ServerConstants.SLES, TransactionalMode.TRANSACTIONAL));
        assertFalse(classify(ServerConstants.SLEMICRO, TransactionalMode.NON_TRANSACTIONAL));
    }

    @Test
    public void testUnknownFallsBackToTheCompleteOsHeuristic() {
        assertTrue(classify(ServerConstants.SLEMICRO, TransactionalMode.UNKNOWN));
        assertTrue(classify(ServerConstants.SLMICRO, TransactionalMode.UNKNOWN));
        assertTrue(classify(ServerConstants.LEAPMICRO, TransactionalMode.UNKNOWN));
        assertTrue(classify(ServerConstants.OPENSUSEMICROOS, TransactionalMode.UNKNOWN));

        assertFalse(classify(ServerConstants.SLES, TransactionalMode.UNKNOWN));
        assertFalse(classify(ServerConstants.UBUNTU, TransactionalMode.UNKNOWN));
    }

    @Test
    public void testSameDistributionWithDifferentObservedStates() {
        assertTrue(classify(ServerConstants.SLES, TransactionalMode.TRANSACTIONAL));
        assertFalse(classify(ServerConstants.SLES, TransactionalMode.NON_TRANSACTIONAL));
        assertFalse(classify(ServerConstants.SLES, TransactionalMode.UNKNOWN));
    }

    @Test
    public void testUnknownWithNullOsFallsBackToNonTransactional() {
        assertFalse(classify(null, TransactionalMode.UNKNOWN));
    }

    @Test
    public void testEntityAndSummaryAgreeOnTheClassification() {
        for (String os : List.of(ServerConstants.SLES, ServerConstants.SLEMICRO, ServerConstants.SLMICRO,
                ServerConstants.LEAPMICRO, ServerConstants.OPENSUSEMICROOS, ServerConstants.UBUNTU)) {
            for (TransactionalMode status : TransactionalMode.values()) {
                MinionServer minion = minionWith(os, status);

                assertEquals(minion.isTransactionalUpdate(), new MinionSummary(minion).isTransactionalUpdate(),
                        "entity and summary disagree for os=" + os + " status=" + status);
            }
        }
    }

    @Test
    public void testSummaryBuiltFromThePersistedStatusMatchesTheEntity() {
        for (String os : List.of(ServerConstants.SLES, ServerConstants.SLEMICRO, ServerConstants.LEAPMICRO)) {
            for (TransactionalMode status : TransactionalMode.values()) {
                MinionServer minion = minionWith(os, status);
                // this is the constructor used by the HQL projection
                MinionSummary projected = new MinionSummary(1L, "minion", null, null, null, os, status);

                assertEquals(minion.isTransactionalUpdate(), projected.isTransactionalUpdate(),
                        "projection disagrees with the entity for os=" + os + " status=" + status);
            }
        }
    }

    private boolean classify(String os, TransactionalMode status) {
        return minionWith(os, status).isTransactionalUpdate();
    }

    private MinionServer minionWith(String os, TransactionalMode status) {
        MinionServer minion = new MinionServer();
        minion.setOs(os);
        minion.setTransactionalMode(status);
        return minion;
    }
}
