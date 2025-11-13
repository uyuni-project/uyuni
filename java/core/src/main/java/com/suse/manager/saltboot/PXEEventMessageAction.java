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
import com.redhat.rhn.domain.notification.NotificationMessage;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.notification.types.PXEEventFailed;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cobbler.XmlRpcException;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class PXEEventMessageAction implements MessageAction {
    private static final Logger LOG = LogManager.getLogger(PXEEventMessageAction.class);

    @Override
    public void execute(EventMessage msg) {
        PXEEvent pxeEvent = ((PXEEventMessage) msg).getPXEEventMessage();
        String minionId = pxeEvent.getMinionId();
        LOG.debug("Processing PXEEvent for minion {}", minionId);

        MinionServer minion = MinionServerFactory.findByMinionId(pxeEvent.getMinionId()).orElseThrow(
                () -> new SaltbootException("Unable to find minion entry for minion id " + pxeEvent.getMinionId()));

        try {
            // Part 1 - update PXE entries
            if (pxeEvent.getRoot().isEmpty()) {
                throw new SaltbootException("Root device not specified in PXE event for minion " +
                        pxeEvent.getMinionId());
            }
            String kernelParameters = "root=" + pxeEvent.getRoot();

            Optional<String> saltDevice = pxeEvent.getSaltDevice();
            if (saltDevice.isPresent()) {
                kernelParameters += " salt_device=" + saltDevice.get();
            }

            Optional<String> kernelParams = pxeEvent.getKernelParameters();
            if (kernelParams.isPresent()) {
                kernelParameters += " " + kernelParams.get();
            }

            SaltbootUtils.createSaltbootSystem(minion, pxeEvent.getBootImage(),
                    pxeEvent.getSaltbootGroup(), pxeEvent.getHwAddresses(), kernelParameters);
        }
        catch (SaltbootException | XmlRpcException e) {
            LOG.error("Error during processing saltboot system entry for minion {}: {}",
                    pxeEvent.getMinionId(), e.getMessage());
            NotificationMessage nm = UserNotificationFactory.createNotificationMessage(
                    new PXEEventFailed(minionId, e.getMessage()));

            Set<User> admins = new HashSet<>(ServerFactory.listAdministrators(minion));
            UserNotificationFactory.storeForUsers(nm, admins);
        }

        // Part 2 - reset pillar data "saltboot:force_*" or "custom_info:saltboot_force_*" if present
        SaltbootUtils.resetSaltbootRedeployFlags(pxeEvent.getMinionId());
    }

    @Override
    public boolean canRunConcurrently() {
        return true;
    }
}
