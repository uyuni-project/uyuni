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
import com.redhat.rhn.domain.server.MinionTransactionalActionHistory.ProgressStep;

import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * Tests for {@link MinionTransactionalActionHistory}.
 */
public class MinionTransactionalActionHistoryTest {

    @Test
    void testPrerequisitesWithChangesWaitForReboot() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);

        history.recordPrerequisitesApplied(true);

        assertEquals(1L, history.getMinionServerId());
        assertEquals(10L, history.getActionId());
        assertTrue(history.isWaitingForReboot());
    }

    @Test
    void testPrerequisitesWithoutChangesDoNotWaitForReboot() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);

        history.recordPrerequisitesApplied(false);

        assertFalse(history.isWaitingForReboot());
        assertNull(history.getRebootPendingSince());
    }

    @Test
    void testPostScheduledClearsRebootWaitState() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);
        history.recordPrerequisitesApplied(true);

        history.recordContinuationScheduled();

        assertFalse(history.isWaitingForReboot());
        assertNull(history.getRebootPendingSince());
        assertEquals(ProgressStatus.COMPLETED, history.getRebootStatus());
        assertEquals(ProgressStatus.SCHEDULED, history.getPostStatus());
    }

    @Test
    void testPendingRebootActionCanContinueOnlyAfterRecordedTime() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);

        history.recordPrerequisitesApplied(true);

        assertFalse(history.canContinueAfter(history.getRebootPendingSince().getTime()));
        assertTrue(history.canContinueAfter(history.getRebootPendingSince().getTime() + 1));
    }

    @Test
    void testPrerequisitesFailureClosesRemainingSteps() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);

        history.recordPrerequisitesFailed();

        assertFalse(history.isWaitingForReboot());
        assertEquals(ProgressStatus.FAILED, history.getPrerequisiteStatus());
        assertEquals(ProgressStatus.NOT_NEEDED, history.getRebootStatus());
        assertEquals(ProgressStatus.NOT_NEEDED, history.getPostStatus());
        assertEquals(history.getPrerequisiteAt(), history.getRebootAt());
        assertEquals(history.getPrerequisiteAt(), history.getPostAt());
    }

    @Test
    void testProgressEntriesAreReturnedInExecutionOrder() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);
        history.recordPrerequisitesApplied(false);
        history.recordContinuationScheduled();

        List<MinionTransactionalActionHistory.ProgressEntry> entries = history.getProgressEntries(false);

        assertEquals(3, entries.size());
        assertEquals(ProgressStep.PREREQUISITES, entries.get(0).getStep());
        assertEquals(ProgressStatus.COMPLETED, entries.get(0).getStatus());
        assertTrue(entries.get(0).isTimestamped());
        assertEquals(ProgressStep.REBOOT, entries.get(1).getStep());
        assertEquals(ProgressStatus.NOT_NEEDED, entries.get(1).getStatus());
        assertFalse(entries.get(1).isTimestamped());
        assertEquals(ProgressStep.ACTION_EXECUTION, entries.get(2).getStep());
        assertEquals(ProgressStatus.SCHEDULED, entries.get(2).getStatus());
        assertTrue(entries.get(2).isTimestamped());
    }

    @Test
    void testTransactionalApplyWithChangesWaitsForReboot() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);

        history.recordTransactionalApplyCompleted(true);

        assertTrue(history.isWaitingForReboot());
        assertEquals(ProgressStatus.COMPLETED, history.getPrerequisiteStatus());
        assertEquals(ProgressStatus.PENDING, history.getRebootStatus());
        assertEquals(ProgressStatus.PENDING, history.getPostStatus());
    }

    @Test
    void testTransactionalApplyWithoutChangesCompletesAction() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);

        history.recordTransactionalApplyCompleted(false);

        assertFalse(history.isWaitingForReboot());
        assertEquals(ProgressStatus.COMPLETED, history.getPrerequisiteStatus());
        assertEquals(ProgressStatus.NOT_NEEDED, history.getRebootStatus());
        assertEquals(ProgressStatus.COMPLETED, history.getPostStatus());
        assertEquals(history.getPrerequisiteAt(), history.getRebootAt());
        assertEquals(history.getPrerequisiteAt(), history.getPostAt());
    }

    @Test
    void testTransactionalApplyFinalizedClearsRebootWaitState() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);
        history.recordTransactionalApplyCompleted(true);

        history.recordTransactionalApplyFinalized();

        assertFalse(history.isWaitingForReboot());
        assertEquals(ProgressStatus.COMPLETED, history.getRebootStatus());
        assertEquals(ProgressStatus.COMPLETED, history.getPostStatus());
        assertTrue(history.getRebootAt().getTime() >= history.getPrerequisiteAt().getTime());
        assertTrue(history.getPostAt().getTime() >= history.getPrerequisiteAt().getTime());
    }

    @Test
    void testTransactionalApplyProgressEntriesUseApplyFlowSteps() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);
        history.recordTransactionalApplyCompleted(true);

        List<MinionTransactionalActionHistory.ProgressEntry> entries = history.getProgressEntries(true);

        assertEquals(3, entries.size());
        assertEquals(ProgressStep.TRANSACTIONAL_APPLY, entries.get(0).getStep());
        assertEquals(ProgressStatus.COMPLETED, entries.get(0).getStatus());
        assertEquals(ProgressStep.TRANSACTIONAL_REBOOT, entries.get(1).getStep());
        assertEquals(ProgressStatus.PENDING, entries.get(1).getStatus());
        assertEquals(ProgressStep.ACTION_FINALIZATION, entries.get(2).getStep());
        assertEquals(ProgressStatus.PENDING, entries.get(2).getStatus());
    }
}
