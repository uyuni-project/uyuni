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

import java.io.Serializable;
import java.util.Objects;

/**
 * Composite id for transactional action history.
 */
public class MinionTransactionalActionHistoryId implements Serializable {

    private Long minionServerId;
    private Long actionId;

    /**
     * Default constructor required by Hibernate.
     */
    public MinionTransactionalActionHistoryId() {
    }

    /**
     * @param minionServerIdIn minion server id
     * @param actionIdIn action id
     */
    public MinionTransactionalActionHistoryId(Long minionServerIdIn, Long actionIdIn) {
        minionServerId = minionServerIdIn;
        actionId = actionIdIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof MinionTransactionalActionHistoryId that)) {
            return false;
        }
        return Objects.equals(minionServerId, that.minionServerId) &&
                Objects.equals(actionId, that.actionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minionServerId, actionId);
    }
}
