/**
 * Copyright (c) 2014 SUSE LLC
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
package com.redhat.rhn.frontend.events;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.EventDatabaseMessage;

import org.hibernate.Transaction;

import java.util.List;

/**
 * Publish this event to sync a repo for a channel given by label.
 */
public class ScheduleRepoSyncEvent implements EventDatabaseMessage {

    private List<String> channelLabels;
    private Long userId;
    private Transaction txn;

    /**
     * Constructor expecting a channel label.
     * @param channelLabelsIn ids of the channels to sync
     * @param userIdIn the user requesting the sync
     */
    public ScheduleRepoSyncEvent(List<String> channelLabelsIn, Long userIdIn) {
        channelLabels = channelLabelsIn;
        userId = userIdIn;
        txn = HibernateFactory.getSession().getTransaction();
    }

    /**
     * @return Returns the channel labels
     */
    public List<String> getChannelLabels() {
        return channelLabels;
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public Transaction getTransaction() {
        return txn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getUserId() {
        return userId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toText() {
        return "";
    }
}
