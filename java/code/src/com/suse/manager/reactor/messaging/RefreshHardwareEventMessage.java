/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.reactor.messaging;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.EventDatabaseMessage;
import com.redhat.rhn.domain.action.Action;
import org.hibernate.Transaction;

/**
 * Triggers the {@link RefreshHardwareEventMessageAction}
 */
public class RefreshHardwareEventMessage implements EventDatabaseMessage {

    private final long actionId;
    private final Long userId;
    private final String minionId;
    private final Transaction txn;

    /**
     * The constructor
     * @param minionIdIn the minion id
     * @param actionIn the scheduled action
     */
    public RefreshHardwareEventMessage(String minionIdIn, Action actionIn) {
        actionId = actionIn.getId();
        userId = actionIn.getSchedulerUser() != null ?
                actionIn.getSchedulerUser().getId() : null;
        txn = HibernateFactory.getSession().getTransaction();
        minionId = minionIdIn;
    }

    /**
     * @return the scheduled action id
     */
    public long getActionId() {
        return actionId;
    }

    /**
     * @return the minion id
     */
    public String getMinionId() {
        return minionId;
    }

    @Override
    public Transaction getTransaction() {
        return txn;
    }

    @Override
    public String toText() {
        return toString();
    }

    @Override
    public Long getUserId() {
        return userId;
    }
}
