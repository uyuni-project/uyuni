/**
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
package com.suse.manager.reactor.messaging;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.EventDatabaseMessage;
import com.redhat.rhn.domain.action.Action;

import org.hibernate.Transaction;

/**
 * Event message for the {@link com.redhat.rhn.common.messaging.MessageQueue} to signal
 * that a new {@link com.redhat.rhn.domain.action.Action} has been scheduled.
 *
 * This event would then be handled by {@link ActionScheduledEventMessageAction} to act on
 * it and execute the action using Salt.
 */
public class ActionScheduledEventMessage implements EventDatabaseMessage {

    private final long actionId;
    private final Long userId;
    private final boolean forcePackageListRefresh;
    private final Transaction txn;

    /**
     * Create a new event about a recently scheduled action.
     *
     * @param actionIn the action that has been scheduled
     */
    public ActionScheduledEventMessage(Action actionIn) {
        this(actionIn, false);
    }

    /**
     * Create a new event about a recently scheduled action.
     *
     * @param actionIn the action that has been scheduled
     * @param forcePackageListRefreshIn set true to request a package list refresh
     */
    public ActionScheduledEventMessage(Action actionIn, boolean forcePackageListRefreshIn) {
        actionId = actionIn.getId();
        userId = actionIn.getSchedulerUser() != null ?
                actionIn.getSchedulerUser().getId() : null;
        forcePackageListRefresh = forcePackageListRefreshIn;
        txn = HibernateFactory.getSession().getTransaction();
    }

    /**
     * Get the ID of the scheduled action.
     *
     * @return the actionId
     */
    public long getActionId() {
        return actionId;
    }


    /**
     * Return true if a package list refresh is requested, otherwise false.
     *
     * @return true if a package list refresh is requested, otherwise false
     */
    public boolean forcePackageListRefresh() {
        return forcePackageListRefresh;
    }

    @Override
    public Long getUserId() {
        return userId;
    }

    @Override
    public String toText() {
        return toString();
    }

    @Override
    public String toString() {
        return "ActionScheduledEventMessage[actionId=" + actionId + "]";
    }

    @Override
    public Transaction getTransaction() {
        return txn;
    }
}
