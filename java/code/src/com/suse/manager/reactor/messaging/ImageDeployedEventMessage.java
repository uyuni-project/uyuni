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
import com.suse.manager.webui.utils.salt.ImageDeployedEvent;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Carrier of the image deployed event.
 */
public class ImageDeployedEventMessage implements EventMessage {

    private ImageDeployedEvent imageDeployedEvent;

    /**
     * Standard constructor.
     *
     * @param imageDeployedEventIn - 'image deployed' event
     */
    public ImageDeployedEventMessage(ImageDeployedEvent imageDeployedEventIn) {
        this.imageDeployedEvent = imageDeployedEventIn;
    }

    /**
     * Gets the 'image deployed' event.
     *
     * @return imageDeployedEvent
     */
    public ImageDeployedEvent getImageDeployedEvent() {
        return imageDeployedEvent;
    }

    @Override
    public String toText() {
        return toString();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("imageDeployedEvent", imageDeployedEvent)
                .toString();
    }

    @Override
    public Long getUserId() {
        return null;
    }
}
