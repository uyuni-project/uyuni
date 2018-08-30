/**
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

import java.util.function.Consumer;

/**
 * A interface representing a class that can act on a EventMessage
 */
public interface MessageAction {

    /**
     * Perform the action on the EventMessage
     * @param msg EventMessage to execute.
     */
    void execute(EventMessage msg);

    /**
     * Return true in case this action can run concurrently with others. Depending on this
     * flag the action will either be executed in the main message queue dispatcher thread
     * and thus block other messages from being dispatched, or (in case of true) there will
     * be a background thread executing this action while the main dispatcher thread can go
     * on dispatching other messages.
     *
     * @return true if this action can run concurrently with others, else false
     */
    default boolean canRunConcurrently() {
        return false;
    }

    /**
     * Return true in case this action needs Hibernate session and transaction handling.
     *
     * @return true if this action operates on the database
     */
    default boolean needsTransactionHandling() {
        return true;
    }

    /**
     * Returns code to be executed after a rollback in case of unexpected errors.
     *
     * @return a consumer
     */
    default Consumer<Exception> getExceptionHandler() {
        return e -> { };
    }
}


