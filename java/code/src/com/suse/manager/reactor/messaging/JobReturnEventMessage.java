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

import com.suse.salt.netapi.event.JobReturnEvent;

/**
 * Event message to handle job return events via the MessageQueue as we get them from the
 * salt event bus.
 */
public class JobReturnEventMessage implements EventMessage {

    /* The underlying job return event as we get it from salt */
    private JobReturnEvent jobReturnEvent;

    /**
     * Constructor that takes the JobReturnEvent object from salt.
     *
     * @param jobReturnEventIn the job return event from salt
     */
    public JobReturnEventMessage(JobReturnEvent jobReturnEventIn) {
        jobReturnEvent = jobReturnEventIn;
    }

    /**
     * @return the underlying JobReturnEvent from salt
     */
    public JobReturnEvent getJobReturnEvent() {
        return jobReturnEvent;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getUserId() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toText() {
        return toString();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "JobReturnEventMessage[minionId: " + jobReturnEvent.getMinionId() + "]";
    }
}
