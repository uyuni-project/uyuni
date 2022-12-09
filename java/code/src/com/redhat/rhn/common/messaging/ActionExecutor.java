/*
 * Copyright (c) 2009--2017 Red Hat, Inc.
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
package com.redhat.rhn.common.messaging;

import com.redhat.rhn.frontend.events.TransactionHelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class which encapsulates the logic necessary to dispatch actions.
 */
class ActionExecutor implements Runnable {

    private static final Logger LOG = LogManager.getLogger(ActionExecutor.class);

    private EventMessage msg;
    private List<MessageAction> actionHandlers = new ArrayList<>();

    /**
     * Constructor
     * @param handlers list of event handlers to dispatch to
     * @param eventMsg message published to queue
     */
    ActionExecutor(List<MessageAction> handlers, EventMessage eventMsg) {
        actionHandlers.addAll(handlers);
        msg = eventMsg;
    }

    /**
     * Iterates over the list of handlers and dispatches
     * the message to each
     */
    @Override
    public void run() {
        for (MessageAction action : actionHandlers) {
            LOG.debug("run() - got action: {}", action.getClass().getName());
            try {
                if (msg instanceof EventDatabaseMessage) {
                    EventDatabaseMessage evtdb = (EventDatabaseMessage) msg;
                    LOG.debug("Got a EventDatabaseMessage");
                    while (evtdb.getTransaction().isActive()) {
                        if (LOG.isDebugEnabled()) {
                            LOG.debug("DB message, waiting for txn: active: {}", evtdb.getTransaction().isActive());
                        }
                        Thread.sleep(10);
                    }
                    LOG.debug("Transaction finished.  Executing");
                }
                if (action.needsTransactionHandling()) {
                    TransactionHelper.handlingTransaction(() -> action.execute(msg), action.getExceptionHandler());
                }
                else {
                    action.execute(msg);
                }
            }
            catch (Throwable t) {
                LOG.error(t);
            }
        }
    }

    /**
     * Return true if all message actions in this executor can run concurrently, else false.
     *
     * @return true if this action handler can run concurrently, else false
     */
    public boolean canRunConcurrently() {
        return actionHandlers.stream().allMatch(MessageAction::canRunConcurrently);
    }

    @Override
    public String toString() {
        return "ActionExecutor[message=" + msg.getClass().getSimpleName() + "]";
    }
}
