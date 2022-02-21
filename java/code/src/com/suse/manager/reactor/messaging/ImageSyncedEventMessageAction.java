/*
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
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.domain.server.ManagedServerGroup;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.ServerGroupFactory;

import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.manager.webui.utils.salt.custom.ImageSyncedEvent;

import org.apache.log4j.Logger;

import java.util.Optional;

/**
 * Logic responsible for reacting to 'image synced' event.
 */
public class ImageSyncedEventMessageAction implements MessageAction {

    private static final Logger LOG = Logger.getLogger(ImageSyncedEventMessageAction.class);


    @Override
    public void execute(EventMessage msg) {
        ImageSyncedEvent imageSyncedEvent = ((ImageSyncedEventMessage) msg).getImageSyncedEvent();

        String minionId = imageSyncedEvent.getMinionId();

        Optional<MinionServer> minionOpt = MinionServerFactory.findByMinionId(minionId);
        minionOpt.ifPresent(minion -> {
            ManagedServerGroup branchGroup = ServerGroupFactory.lookupByNameAndOrg(imageSyncedEvent.getBranch(),
                minion.getOrg());

            if (!minion.getGroups().contains(branchGroup)) {
                LOG.error("Branch server " + minionId + " is not in group " + imageSyncedEvent.getBranch());
                return;
            }

            String action = imageSyncedEvent.getAction();
            if (action.equals("add")) {
                SaltStateGeneratorService.INSTANCE.createImageSyncedPillar(branchGroup,
                    imageSyncedEvent.getImageName(), imageSyncedEvent.getImageVersion());
            }
            else if (action.equals("remove")) {
                SaltStateGeneratorService.INSTANCE.removeImageSyncedPillar(branchGroup,
                    imageSyncedEvent.getImageName(), imageSyncedEvent.getImageVersion());
            }
            else {
                LOG.warn("Unknown image_synced action: " + action);
            }
        });
    }

    @Override
    public boolean canRunConcurrently() {
        return true;
    }
}
