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

import org.hibernate.Transaction;

import java.util.List;

/**
 * Trigger actions whenever a server's channel assignments were changed. Execution of the
 * action will wait until the current transaction has been committed as we are implementing
 * {@link EventDatabaseMessage}).
 */
public class ChannelsChangedEventMessage implements EventDatabaseMessage {

    private final long serverId;
    private final Long userId;
    private final Transaction transaction;
    private List<Long> accessTokenIds;
    private boolean scheduleApplyChannelsState;

    /**
     * Constructor for creating a {@link ChannelsChangedEventMessage} for a given server.
     *
     * @param serverIdIn the server id
     * @param userIdIn the user id
     * @param accessTokenIdsIn id of the access token that's used for the new channels
     */
    public ChannelsChangedEventMessage(long serverIdIn, long userIdIn, List<Long> accessTokenIdsIn) {
        this.serverId = serverIdIn;
        this.userId = userIdIn;
        this.accessTokenIds = accessTokenIdsIn;
        this.transaction = HibernateFactory.getSession().getTransaction();
    }

    /**
     * Constructor for creating a {@link ChannelsChangedEventMessage} for a given server.
     *
     * @param serverIdIn the server id
     * @param userIdIn the user id
     * @param accessTokenIdsIn id of the access token that's used for the new channels
     * @param scheduleApplyChannelsStateIn whether to schedule applying the channels state
     * for Salt minions
     */
    public ChannelsChangedEventMessage(long serverIdIn, long userIdIn, List<Long> accessTokenIdsIn,
                                       boolean scheduleApplyChannelsStateIn) {
        this.serverId = serverIdIn;
        this.userId = userIdIn;
        this.accessTokenIds = accessTokenIdsIn;
        this.transaction = HibernateFactory.getSession().getTransaction();
        this.scheduleApplyChannelsState = scheduleApplyChannelsStateIn;
    }

    /**
     * Constructor for creating a {@link ChannelsChangedEventMessage} for a given server.
     *
     * @param serverIdIn the server id
     * @param userIdIn the user id
     */
    public ChannelsChangedEventMessage(long serverIdIn, long userIdIn) {
        this.serverId = serverIdIn;
        this.userId = userIdIn;
        this.transaction = HibernateFactory.getSession().getTransaction();
    }

    /**
     * Constructor for creating a {@link ChannelsChangedEventMessage} for a given server.
     *
     * @param serverIdIn the server id
     */
    public ChannelsChangedEventMessage(long serverIdIn) {
        this.serverId = serverIdIn;
        this.userId = null;
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

    /**
     * @return scheduleApplyChannelsState to get
     */
    public boolean isScheduleApplyChannelsState() {
        return scheduleApplyChannelsState;
    }

    /**
     * @param scheduleApplyChannelsStateIn to set
     */
    public void setScheduleApplyChannelsState(boolean scheduleApplyChannelsStateIn) {
        this.scheduleApplyChannelsState = scheduleApplyChannelsStateIn;
    }

    /**
     * @return accessTokenId to get
     */
    public List<Long> getAccessTokenIds() {
        return accessTokenIds;
    }

    @Override
    public String toString() {
        return "ChannelChangedEventMessage[serverId: " + serverId + "]";
    }
}
