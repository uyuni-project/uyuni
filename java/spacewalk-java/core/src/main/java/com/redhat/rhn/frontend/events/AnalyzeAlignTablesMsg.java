/*
 * Copyright (c) 2026 SUSE LLC
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
import com.redhat.rhn.domain.user.User;

import org.hibernate.Transaction;

/**
 * Post-transaction message to analyze CLM alignment tables.
 */
public class AnalyzeAlignTablesMsg implements EventDatabaseMessage {

    private final Transaction txn;
    private final User user;

    /**
     * Standard constructor.
     * @param userIn the user that initiates the message
     */
    public AnalyzeAlignTablesMsg(User userIn) {
        this.user = userIn;
        this.txn = HibernateFactory.getSession().getTransaction();
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
        return user.getId();
    }

    @Override
    public String toString() {
        return "AnalyzeAlignTablesMsg";
    }
}
