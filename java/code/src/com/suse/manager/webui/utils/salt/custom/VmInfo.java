/*
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
package com.suse.manager.webui.utils.salt.custom;

import com.google.gson.annotations.SerializedName;

/**
 * Represent VM info needed to update the database
 */
public class VmInfo {

    private final long time;

    @SerializedName("event_type")
    private final String eventType;

    @SerializedName("target_type")
    private final String targetType;

    @SerializedName("guest_properties")
    private final GuestProperties guestProperties;

    /**
     * Constructor
     *
     * @param timeIn creation time
     * @param eventTypeIn event type
     * @param targetTypeIn target type
     * @param guestPropertiesIn guest properties
     */
    public VmInfo(long timeIn, String eventTypeIn, String targetTypeIn,
            GuestProperties guestPropertiesIn) {
        this.time = timeIn;
        this.eventType = eventTypeIn;
        this.targetType = targetTypeIn;
        this.guestProperties = guestPropertiesIn;
    }

    /**
     * @return the time
     */
    public long getTime() {
        return time;
    }

    /**
     * @return the eventType
     */
    public String getEventType() {
        return eventType;
    }

    /**
     * @return the targetType
     */
    public String getTargetType() {
        return targetType;
    }

    /**
     * @return the guestProperties
     */
    public GuestProperties getGuestProperties() {
        return guestProperties;
    }
}
