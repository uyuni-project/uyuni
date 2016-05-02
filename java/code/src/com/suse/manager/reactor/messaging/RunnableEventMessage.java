/**
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

import com.redhat.rhn.common.messaging.EventMessage;

import java.util.Optional;

/**
 * Easy way to create a message/action for the message queue by specifying
 * user, name and action as parameters.
 */
public class RunnableEventMessage implements EventMessage {

    private final Optional<Long> userId;
    private final Runnable action;
    private final String name;

    /**
     * Constructor without user
     *
     * @param nameIn name of the message
     * @param actionIn action to execute
     */
    public RunnableEventMessage(String nameIn, Runnable actionIn) {
        this.userId = Optional.empty();
        this.action = actionIn;
        this.name = nameIn;
    }

    /**
     * Constructor with user
     *
     * @param userIdIn the user for that message
     * @param nameIn name of the message
     * @param actionIn action to execute
     */
    public RunnableEventMessage(long userIdIn, String nameIn, Runnable actionIn) {
        this.userId = Optional.of(userIdIn);
        this.action = actionIn;
        this.name = nameIn;
    }

    /**
     * @return the action
     */
    public Runnable getAction() {
        return action;
    }

    @Override
    public String toString() {
        return "RunnableEventMessage[name:" + name + "]";
    }

    @Override
    public String toText() {
        return toString();
    }

    @Override
    public Long getUserId() {
        return userId.orElse(null);
    }
}
