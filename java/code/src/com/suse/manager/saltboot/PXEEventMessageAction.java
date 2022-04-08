/*
 * Copyright (c) 2022 SUSE LLC
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

package com.suse.manager.saltboot;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;

import com.suse.manager.reactor.messaging.ImageSyncedEventMessageAction;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PXEEventMessageAction implements MessageAction {
    private static final Logger LOG = LogManager.getLogger(ImageSyncedEventMessageAction.class);

    @Override
    public void execute(EventMessage msg) {
        PXEEvent pxeEvent = ((PXEEventMessage) msg).getPXEEventMessage();

        if (pxeEvent.getRoot().isEmpty()) {
            LOG.error("Root device not specified in PXE event for minion {}. Ignoring event", pxeEvent.getMinionId());
            return;
        }
        String kernelParameters = "root=" + pxeEvent.getRoot();

        if (pxeEvent.getSaltDevice().isPresent()) {
            kernelParameters += " salt_device=" + pxeEvent.getSaltDevice().get();
        }
        if (pxeEvent.getKernelParameters().isPresent()) {
            kernelParameters += " " + pxeEvent.getKernelParameters().get();
        }

        SaltbootUtils.createSaltbootSystem(pxeEvent.getMinionId(), pxeEvent.getBootImage(), pxeEvent.getSaltbootGroup(),
                pxeEvent.getHwAddresses(), kernelParameters);
    }

    @Override
    public boolean canRunConcurrently() {
        return true;
    }
}
