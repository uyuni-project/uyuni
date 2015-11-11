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
package com.suse.manager.reactor;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.EventDatabaseMessage;

import org.hibernate.Transaction;

/**
 * Trigger actions whenever a server's channel assignments were changed. Execution of the
 * action will wait until the current transaction has been committed as we are implementing
 * {@link EventDatabaseMessage}).
 */
public class ChannelChangedEvent implements EventDatabaseMessage {

    private final long serverId;
    private final long userId;
    private final Transaction transaction;

    /**
     * Constructor for creating a {@link ChannelChangedEvent} for a given server.
     *
     * @param serverId the server id
     * @param userId the user id
     */
    public ChannelChangedEvent(long serverId, long userId) {
        this.serverId = serverId;
        this.userId = userId;
        this.transaction = HibernateFactory.getSession().getTransaction();
    }

    /**
     * Return the server id.
     *
     * @return the server id
     */
    public Long getServerId() {
        return serverId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getUserId() {
        return this.userId;
    }

    /**
     * {@inheritDoc}
     */
    public Transaction getTransaction() {
        return this.transaction;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toText() {
        return toString();
    }

    @Override
    public String toString() {
        return "ChannelChangedEvent[serverId: " + serverId + "]";
    }
}
