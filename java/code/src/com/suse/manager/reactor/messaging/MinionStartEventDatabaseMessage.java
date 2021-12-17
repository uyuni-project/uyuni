/*
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

import org.hibernate.Transaction;

/**
 * A version of {@link MinionStartEventMessage} that waits for the current transaction
 * (i.e. at message creation time) to be finished before being handled.
 */
public class MinionStartEventDatabaseMessage extends MinionStartEventMessage
        implements EventDatabaseMessage {

    private final Transaction txn;

    /**
     * @param minionIdIn the id of the minion that was started
     */
    public MinionStartEventDatabaseMessage(String minionIdIn) {
        super(minionIdIn);
        txn = HibernateFactory.getSession().getTransaction();
    }

    @Override
    public Transaction getTransaction() {
        return txn;
    }
}
