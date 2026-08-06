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
package com.suse.manager.reactor.messaging;

import com.redhat.rhn.common.messaging.EventMessage;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Requests an asynchronous transactional pending transaction check.
 */
public class CheckTransactionalPendingTransactionEventMessage implements EventMessage {

    private final Long serverId;

    /**
     * Standard constructor.
     *
     * @param serverIdIn target server
     */
    public CheckTransactionalPendingTransactionEventMessage(Long serverIdIn) {
        serverId = serverIdIn;
    }

    public Long getServerId() {
        return serverId;
    }

    @Override
    public String toText() {
        return toString();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("serverId", serverId)
                .toString();
    }

    @Override
    public Long getUserId() {
        return null;
    }
}
