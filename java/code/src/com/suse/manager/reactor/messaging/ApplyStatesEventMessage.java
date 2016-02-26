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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * An event to signal that a set of states is dirty and needs
 * to be applied to a particular server
 */
public class ApplyStatesEventMessage implements EventDatabaseMessage {

    public static final String CERTIFICATE = "certs";
    public static final String PACKAGES = "packages";
    public static final String CHANNELS = "channels";

    private final long serverId;
    private final Long userId;
    private final List<String> stateNames;
    private final Transaction txn;

    /**
     * Constructor for creating a {@link ApplyStatesEventMessage} for a given server.
     *
     * @param serverIdIn the server id
     * @param userIdIn the user id
     * @param stateNamesIn state names that need to be applied to the server
     */
    public ApplyStatesEventMessage(long serverIdIn, long userIdIn, String... stateNamesIn) {
        this.serverId = serverIdIn;
        this.userId = userIdIn;
        this.stateNames = Arrays.asList(stateNamesIn);
        this.txn = HibernateFactory.getSession().getTransaction();
    }

    /**
     * Constructor for creating a {@link ApplyStatesEventMessage} for a given server.
     *
     * @param serverIdIn the server id
     * @param stateNamesIn state names that need to be applied to the server
     */
    public ApplyStatesEventMessage(long serverIdIn, String... stateNamesIn) {
        this.serverId = serverIdIn;
        this.userId = null;
        this.stateNames = Arrays.asList(stateNamesIn);
        this.txn = HibernateFactory.getSession().getTransaction();
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
     * Return the list of states that need to be updated.
     *
     * @return the server id
     */
    public List<String> getStateNames() {
        return stateNames;
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
    @Override
    public String toText() {
        return toString();
    }

    @Override
    public String toString() {
        return "StateDirtyEvent[serverId: " + serverId + ", stateNames: " +
                stateNames.stream().collect(Collectors.joining(", ")) + "]";
    }

    @Override
    public Transaction getTransaction() {
        return txn;
    }
}
