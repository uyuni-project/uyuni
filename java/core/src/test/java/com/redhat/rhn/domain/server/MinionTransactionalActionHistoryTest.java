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

import org.junit.jupiter.api.Test;

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

        history.recordPostScheduled();

        assertFalse(history.isWaitingForReboot());
        assertNull(history.getRebootPendingSince());
    }

    @Test
    void testPendingRebootActionCanContinueOnlyAfterRecordedTime() {
        MinionTransactionalActionHistory history = MinionTransactionalActionHistory.create(1L, 10L);

        history.recordPrerequisitesApplied(true);

        assertFalse(history.canContinueAfter(history.getRebootPendingSince().getTime()));
        assertTrue(history.canContinueAfter(history.getRebootPendingSince().getTime() + 1));
    }
}
