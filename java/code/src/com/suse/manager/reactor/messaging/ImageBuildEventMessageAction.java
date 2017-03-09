/**
 * Copyright (c) 2015 SUSE LLC
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
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.action.salt.build.ImageBuildAction;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.image.ImageInfoCustomDataValue;
import com.redhat.rhn.domain.image.ImageInfoFactory;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.events.AbstractDatabaseAction;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;

/**
 * Applies states to a server
 */
public class ImageBuildEventMessageAction extends AbstractDatabaseAction {

    private static final Logger LOG = Logger.getLogger(ImageBuildEventMessageAction.class);

    /**
     * Default constructor.
     */
    public ImageBuildEventMessageAction() {
    }

    @Override
    public void doExecute(EventMessage event) {
        ImageBuildEventMessage imageBuildEvent = (ImageBuildEventMessage) event;
        Server server = ServerFactory.lookupById(imageBuildEvent.getServerId());
        ImageProfile imageProfile = ImageProfileFactory.lookupById(
                imageBuildEvent.getImageProfileId()).get();

        // Apply states only for salt systems
        if (server != null && server.hasEntitlement(
                EntitlementManager.CONTAINER_BUILD_HOST)) {
            LOG.debug("Schedule image.build for " + server.getName() + ": " +
                    imageProfile.getLabel() + " " +
                    imageBuildEvent.getTag());

            // The scheduling user can be null
            User scheduler = event.getUserId() != null ?
                    UserFactory.lookupById(event.getUserId()) : null;

            // Schedule a "image.build" action to happen right now
            ImageBuildAction action = ActionManager.scheduleImageBuild(
                    scheduler,
                    Collections.singletonList(server.getId()),
                    imageBuildEvent.getTag(),
                    imageProfile,
                    new Date());
            MessageQueue.publish(new ActionScheduledEventMessage(action,
                    false));

            ImageInfoFactory
                    .lookupByName(imageProfile.getLabel(), imageBuildEvent.getTag(),
                            imageProfile.getTargetStore().getId())
                    .ifPresent(ImageInfoFactory::delete);

            ImageInfo info = new ImageInfo();
            info.setName(imageProfile.getLabel());
            info.setVersion(imageBuildEvent.getTag().isEmpty() ? "latest" :
                    imageBuildEvent.getTag());
            info.setStore(imageProfile.getTargetStore());
            info.setOrg(server.getOrg());
            info.setAction(action);
            info.setProfile(imageProfile);
            info.setBuildServer((MinionServer) server);
            info.setChannels(new HashSet<>(imageProfile.getToken().getChannels()));

            // Image arch should be the same as the build host
            info.setImageArch(server.getServerArch());

            // Checksum will be available from inspect

            // Copy custom data values from image profile
            if (imageProfile.getCustomDataValues() != null) {
                imageProfile.getCustomDataValues().forEach(cdv -> info.getCustomDataValues()
                        .add(new ImageInfoCustomDataValue(cdv, info)));
            }

            ImageInfoFactory.save(info);
        }
    }
}
