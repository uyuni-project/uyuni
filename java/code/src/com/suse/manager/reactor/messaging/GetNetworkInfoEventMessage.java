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

import com.suse.manager.reactor.utils.ValueMap;

import org.hibernate.Transaction;

/**
 * Triggers getting the network information from a minion.
 */
public class GetNetworkInfoEventMessage implements EventDatabaseMessage {

    private final Long serverId;
    private final ValueMap grains;
    private final Transaction txn;

    /**
     * The constructor.
     * @param serverIdIn the sever id
     */
    public GetNetworkInfoEventMessage(Long serverIdIn) {
        this(serverIdIn, null);
    }

    /**
     * The constructor.
     * @param serverIdIn the sever id
     * @param grainsIn the minion grains
     */
    public GetNetworkInfoEventMessage(Long serverIdIn, ValueMap grainsIn) {
        serverId = serverIdIn;
        grains = grainsIn;
        txn = HibernateFactory.getSession().getTransaction();
    }

    @Override
    public String toText() {
        return null;
    }

    @Override
    public Long getUserId() {
        return null;
    }

    /**
     * Get the minion grains.
     * @return a {@link ValueMap} containing the grains
     */
    public ValueMap getGrains() {
        return grains;
    }

    /**
     * Get server id.
     * @return the id
     */
    public Long getServerId() {
        return serverId;
    }

    @Override
    public Transaction getTransaction() {
        return txn;
    }
}
