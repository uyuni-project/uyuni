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
package com.suse.manager.action;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Transactional execution policy for a formula.
 *
 * @param mode formula transactional execution mode
 * @param liveStateIds state IDs that must run only in the live post-transactional pass
 */
record FormulaTransactionalPolicy(FormulaTransactionalMode mode, List<String> liveStateIds) {

    FormulaTransactionalPolicy {
        if (mode == null) {
            throw new IllegalArgumentException("Formula transactional policy mode is required");
        }
        if (liveStateIds == null) {
            throw new IllegalArgumentException("Formula transactional policy live state IDs are required");
        }
        Set<String> seenIds = new LinkedHashSet<>();
        for (String liveStateId : liveStateIds) {
            if (liveStateId == null || liveStateId.trim().isEmpty()) {
                throw new IllegalArgumentException("Formula transactional policy live state IDs must be non-empty");
            }
            if (!seenIds.add(liveStateId)) {
                throw new IllegalArgumentException("Formula transactional policy live state ID is duplicated: " +
                        liveStateId);
            }
        }
        liveStateIds = List.copyOf(liveStateIds);
        if (mode == FormulaTransactionalMode.TRANSACTIONAL_THEN_LIVE && liveStateIds.isEmpty()) {
            throw new IllegalArgumentException("TRANSACTIONAL_THEN_LIVE formula policy requires live state IDs");
        }
        if (mode != FormulaTransactionalMode.TRANSACTIONAL_THEN_LIVE && !liveStateIds.isEmpty()) {
            throw new IllegalArgumentException(mode + " formula policy must not define live state IDs");
        }
    }

    static FormulaTransactionalPolicy transactional() {
        return new FormulaTransactionalPolicy(FormulaTransactionalMode.TRANSACTIONAL, List.of());
    }

    static FormulaTransactionalPolicy live() {
        return new FormulaTransactionalPolicy(FormulaTransactionalMode.LIVE, List.of());
    }

    static FormulaTransactionalPolicy transactionalThenLive(List<String> liveStateIds) {
        return new FormulaTransactionalPolicy(FormulaTransactionalMode.TRANSACTIONAL_THEN_LIVE, liveStateIds);
    }

    static FormulaTransactionalPolicy unsupported() {
        return new FormulaTransactionalPolicy(FormulaTransactionalMode.UNSUPPORTED, List.of());
    }
}
