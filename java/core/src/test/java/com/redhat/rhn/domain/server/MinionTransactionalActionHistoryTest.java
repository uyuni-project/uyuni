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
package com.redhat.rhn.domain.server;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.redhat.rhn.domain.server.MinionTransactionalActionHistory.ProgressStatus;

import org.junit.jupiter.api.Test;

/**
 * Tests for {@link MinionTransactionalActionHistory}.
 */
public class MinionTransactionalActionHistoryTest {

    @Test
    void testSnapshotReconciliationWithRebootWaitsForReboot() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);

        history.recordTransactionalStateApplied();
        history.recordSnapshotReconciliation(true, true);

        assertEquals(1L, history.getMinionServerId());
        assertEquals(10L, history.getActionId());
        assertTrue(history.isWaitingForReboot());
    }

    @Test
    void testSnapshotReconciliationWithoutRebootDoesNotWaitForReboot() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);

        history.recordTransactionalStateApplied();
        history.recordSnapshotReconciliation(false, false);

        assertFalse(history.isWaitingForReboot());
        assertNull(history.getRebootPendingSince());
    }

    @Test
    void testAfterRebootScheduledClearsRebootWaitState() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);
        history.recordTransactionalStateApplied();
        history.recordSnapshotReconciliation(true, true);

        history.recordAfterRebootScheduled();

        assertFalse(history.isWaitingForReboot());
        assertNull(history.getRebootPendingSince());
        assertEquals(ProgressStatus.COMPLETED, history.getRebootStatus());
        assertEquals(ProgressStatus.SCHEDULED, history.getAfterRebootStatus());
    }

    @Test
    void testTransactionalApplyFailureClosesRemainingSteps() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);

        history.recordTransactionalApplyFailed();

        assertFalse(history.isWaitingForReboot());
        assertEquals(ProgressStatus.FAILED, history.getPrerequisiteStatus());
        assertEquals(ProgressStatus.NOT_NEEDED, history.getRebootStatus());
        assertEquals(ProgressStatus.NOT_NEEDED, history.getAfterRebootStatus());
        assertEquals(history.getPrerequisiteAt(), history.getRebootAt());
        assertEquals(history.getPrerequisiteAt(), history.getAfterRebootStatusAt());
    }

    @Test
    void testPrerequisiteResultIsStored() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);

        history.recordTransactionalStateApplied("salt result");

        assertEquals("salt result", history.getPrerequisiteResult());
    }

    @Test
    void testFailedPrerequisiteResultIsStored() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);

        history.recordTransactionalApplyFailed("salt failure");

        assertEquals("salt failure", history.getPrerequisiteResult());
    }

    @Test
    void testSnapshotReconciliationWithPendingSnapshotWaitsForReboot() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);

        history.recordTransactionalStateApplied();
        history.recordSnapshotReconciliation(true, false);

        assertTrue(history.isWaitingForReboot());
        assertEquals(ProgressStatus.COMPLETED, history.getPrerequisiteStatus());
        assertEquals(ProgressStatus.PENDING, history.getRebootStatus());
        assertEquals(ProgressStatus.PENDING, history.getAfterRebootStatus());
    }

    @Test
    void testSnapshotReconciliationWithoutPendingSnapshotCompletesAction() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);

        history.recordTransactionalStateApplied();
        history.recordSnapshotReconciliation(false, false);

        assertFalse(history.isWaitingForReboot());
        assertEquals(ProgressStatus.COMPLETED, history.getPrerequisiteStatus());
        assertEquals(ProgressStatus.NOT_NEEDED, history.getRebootStatus());
        assertEquals(ProgressStatus.COMPLETED, history.getAfterRebootStatus());
        assertNull(history.getRebootAt());
        assertTrue(history.getAfterRebootStatusAt().getTime() >= history.getPrerequisiteAt().getTime());
    }

    @Test
    void testSnapshotRefreshActionIdIsStored() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);

        history.recordSnapshotRefreshAction(20L);

        assertEquals(20L, history.getSnapshotRefreshActionId());
    }

    @Test
    void testSnapshotRefreshFailureClosesReconciliationSteps() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);

        history.recordTransactionalStateApplied();
        history.recordSnapshotRefreshFailed();

        assertFalse(history.isWaitingForReboot());
        assertEquals(ProgressStatus.COMPLETED, history.getPrerequisiteStatus());
        assertEquals(ProgressStatus.FAILED, history.getRebootStatus());
        assertEquals(ProgressStatus.FAILED, history.getAfterRebootStatus());
        assertTrue(history.getRebootAt().getTime() >= history.getPrerequisiteAt().getTime());
        assertTrue(history.getAfterRebootStatusAt().getTime() >= history.getPrerequisiteAt().getTime());
    }

    @Test
    void testTransactionalApplyFinalizedClearsRebootWaitState() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);
        history.recordTransactionalStateApplied();
        history.recordSnapshotReconciliation(true, false);

        history.recordTransactionalApplyFinalized();

        assertFalse(history.isWaitingForReboot());
        assertEquals(ProgressStatus.COMPLETED, history.getRebootStatus());
        assertEquals(ProgressStatus.COMPLETED, history.getAfterRebootStatus());
        assertTrue(history.getRebootAt().getTime() >= history.getPrerequisiteAt().getTime());
        assertTrue(history.getAfterRebootStatusAt().getTime() >= history.getPrerequisiteAt().getTime());
    }

}
