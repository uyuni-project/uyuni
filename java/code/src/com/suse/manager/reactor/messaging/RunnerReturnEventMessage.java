/**
 * Copyright (c) 2020 SUSE LLC
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
import com.suse.salt.netapi.event.RunnerReturnEvent;

public class RunnerReturnEventMessage implements EventMessage {

    private RunnerReturnEvent runnerReturnEvent;

    public RunnerReturnEventMessage(RunnerReturnEvent event) {
        this.runnerReturnEvent = event;
    }

    /**
     * @return runnerReturnEvent to get
     */
    public RunnerReturnEvent getRunnerReturnEvent() {
        return runnerReturnEvent;
    }

    @Override
    public String toText() {
        return toString();
    }

    @Override
    public Long getUserId() {
        return null;
    }

    @Override
    public String toString() {
        return "RunnerReturnEventMessage[jid: " + runnerReturnEvent.getJobId() + "]";
    }
}
