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
import com.redhat.rhn.common.messaging.MessageAction;

import org.apache.log4j.Logger;

/**
 * Action for executing RunnableEventMessageAction
 */
public class RunnableEventMessageAction implements MessageAction {

    /* Logger for this class */
    private static final Logger LOG = Logger
            .getLogger(RunnableEventMessageAction.class);

    @Override
    public void execute(EventMessage msg) {
       if (msg instanceof RunnableEventMessage) {
            RunnableEventMessage event = (RunnableEventMessage) msg;
            event.getAction().run();
       }
       else {
          LOG.warn("RunnableEventMessageAction got " + msg.getClass().getName() +
                  " as message but requires " + RunnableEventMessage.class.getName());
       }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRunConcurrently() {
        return true;
    }
}
