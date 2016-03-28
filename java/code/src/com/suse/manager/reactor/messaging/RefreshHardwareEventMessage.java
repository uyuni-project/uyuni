package com.suse.manager.reactor.messaging;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.messaging.EventDatabaseMessage;
import com.redhat.rhn.domain.action.Action;
import org.hibernate.Transaction;

/**
 * Created by matei on 3/28/16.
 */
public class RefreshHardwareEventMessage implements EventDatabaseMessage {

    private final long actionId;
    private final Long userId;
    private final String minionId;
    private final Transaction txn;

    public RefreshHardwareEventMessage(String minionIdIn, Action actionIn) {
        actionId = actionIn.getId();
        userId = actionIn.getSchedulerUser() != null ?
                actionIn.getSchedulerUser().getId() : null;
        txn = HibernateFactory.getSession().getTransaction();
        minionId = minionIdIn;
    }

    public long getActionId() {
        return actionId;
    }

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
