/*
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
package com.redhat.rhn.common.messaging.test;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.common.messaging.MessageQueue;

public class TestAction implements MessageAction {

    private static MessageAction registered = new TestAction();

    public static void registerAction() {
        MessageQueue.registerAction(registered, TestEventMessage.class);
    }

    public static void deRegisterAction() {
        MessageQueue.deRegisterAction(registered, TestEventMessage.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(EventMessage msg) {
        TestEventMessage tm = (TestEventMessage) msg;
        tm.setMessageReceived(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean needsTransactionHandling() {
        return false;
    }
}


