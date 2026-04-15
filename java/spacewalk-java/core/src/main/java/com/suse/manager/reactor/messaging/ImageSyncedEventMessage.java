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

import com.suse.manager.webui.utils.salt.custom.ImageSyncedEvent;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Carrier of the image synced event.
 */
public class ImageSyncedEventMessage implements EventMessage {

    private ImageSyncedEvent imageSyncedEvent;

    /**
     * Standard constructor.
     *
     * @param imageSyncedEventIn - 'image synced' event
     */
    public ImageSyncedEventMessage(ImageSyncedEvent imageSyncedEventIn) {
        this.imageSyncedEvent = imageSyncedEventIn;
    }

    /**
     * Gets the 'image synced' event.
     *
     * @return ImageSyncedEvent
     */
    public ImageSyncedEvent getImageSyncedEvent() {
        return imageSyncedEvent;
    }

    @Override
    public String toText() {
        return toString();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("ImageSyncedEvent", imageSyncedEvent)
                .toString();
    }

    @Override
    public Long getUserId() {
        return null;
    }
}
