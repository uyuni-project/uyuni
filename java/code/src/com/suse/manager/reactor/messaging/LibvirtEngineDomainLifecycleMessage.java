/*
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

import com.google.gson.JsonElement;

import java.util.Optional;

/**
 *
 * LibvirtEngineDomainLifecycleMessage
 */
public class LibvirtEngineDomainLifecycleMessage extends LibvirtEngineDomainMessage {

    private String event;
    private String detail;

    /**
     * @return the domain lifecycle event type (start, destroy, etc)
     */
    public String getEvent() {
        return event;
    }

    /**
     * @return the domain lifecycle event detail, mostly indicating the
     *         reason of the event
     */
    public String getDetail() {
        return detail;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + event + "]";
    }

    protected LibvirtEngineDomainLifecycleMessage(String connection,
            Optional<String> minionId, String timestamp, JsonElement data) {
        super(connection, minionId, timestamp, data);

        this.event = data.getAsJsonObject().get("event").getAsString();
        this.detail = data.getAsJsonObject().get("detail").getAsString();
    }
}
