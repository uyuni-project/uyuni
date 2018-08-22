/**
 * Copyright (c) 2018 SUSE LLC
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
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.suse.manager.webui.utils.salt.ImageDeployedEvent;
import org.apache.log4j.Logger;

import java.util.Optional;

/**
 * Logic responsible for reacting to 'image deployed' event.
 */
public class ImageDeployedEventMessageAction implements MessageAction {

    private static final Logger LOG = Logger.getLogger(ImageDeployedEventMessageAction.class);

    @Override
    public void execute(EventMessage msg) {
        ImageDeployedEvent imageDeployedEvent = ((ImageDeployedEventMessage) msg).getImageDeployedEvent();

        if (!imageDeployedEvent.getMachineId().isPresent()) {
            LOG.warn("Machine id grain is not present in event data: " + imageDeployedEvent +
                    " . Skipping post image-deploy actions.");
            return;
        }

        Optional<Boolean> saltbootInitrd = imageDeployedEvent.getSaltbootInitrd();
        if (!saltbootInitrd.isPresent() || saltbootInitrd.get() == false) {
            LOG.info("Saltboot initrd grain is false/not present in event data: " + imageDeployedEvent +
                    " . Skipping post image-deploy actions.");
            return;
        }

        Optional<MinionServer> minion = imageDeployedEvent.getMachineId()
                .flatMap(MinionServerFactory::findByMachineId);
        if (!minion.isPresent()) {
            LOG.warn("Minion id '" + imageDeployedEvent.getMachineId() +
                    "' not found. Skipping post-image deploy actions.");
            return;
        }

        // todo minion post image-deploy handling
    }
}
