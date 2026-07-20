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
package com.redhat.rhn.domain.action;

/**
 * Transactional execution flows supported by Uyuni actions.
 */
public enum TransactionalFlow {
    /**
     * Apply prerequisite states transactionally, reboot if needed, then run the actual state.
     */
    PREREQUISITE_THEN_STATE,

    /**
     * Apply the actual states transactionally and complete the action after reboot if needed.
     */
    APPLY_THEN_COMPLETE
}
