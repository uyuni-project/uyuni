/*
 * Copyright (c) 2021 SUSE LLC
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
package com.suse.manager.webui.websocket.json;

import java.util.Optional;

/**
 * Describes the payload on message sent to VirtNotifications
 */
public class VirtNotificationMessage {
    private Optional<Long> sid = Optional.empty();
    private Optional<String> guestUuid = Optional.empty();

    /**
     * @return value of sid
     */
    public Optional<Long> getSid() {
        return sid;
    }

    /**
     * @param sidIn value of sid
     */
    public void setSid(Optional<Long> sidIn) {
        sid = sidIn;
    }

    /**
     * @return value of guestUuid
     */
    public Optional<String> getGuestUuid() {
        return guestUuid;
    }

    /**
     * @param guestUuidIn value of guestUuid
     */
    public void setGuestUuid(Optional<String> guestUuidIn) {
        guestUuid = guestUuidIn;
    }
}
