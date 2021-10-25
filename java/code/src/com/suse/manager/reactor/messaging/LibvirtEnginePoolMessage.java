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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Optional;

/**
 * Libvirt_events engine pool message to handle
 */
public class LibvirtEnginePoolMessage extends AbstractLibvirtEngineMessage {
    private String poolName;
    private String poolUUID;

    /**
     * Create a pool message corresponding to the event type
     *
     * @param connection libvirt connection
     * @param eventType libvirt event type (lifecycle, refresh)
     * @param minionId the minion ID or empty if the message comes from the master
     * @param timestamp the event time stamp
     * @param data the JSon data of the event
     *
     * @return a specialized object matching the event
     */
    public static AbstractLibvirtEngineMessage createPoolMessage(String connection, String eventType,
                                                                 Optional<String> minionId, String timestamp,
                                                                 JsonElement data) {
        if (eventType.equals("lifecycle")) {
            return new LibvirtEnginePoolLifecycleMessage(connection, minionId,
                    timestamp, data);
        }
        else if (eventType.equals("refresh")) {
            return new LibvirtEnginePoolRefreshMessage(connection, minionId,
                    timestamp, data);
        }
        return null;
    }

    /**
     * @return name of the pool related to the event
     */
    public String getPoolName() {
        return poolName;
    }

    /**
     * @return uuid of the pool related to the event
     */
    public String getPoolUUID() {
        return poolUUID;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + poolName + "]";
    }

    protected LibvirtEnginePoolMessage(String connection, Optional<String> minionId,
                                       String timestamp, JsonElement data) {
        super(connection, minionId, timestamp);

        JsonObject pool = data.getAsJsonObject().get("pool").getAsJsonObject();
        this.poolName = pool.get("name").getAsString();
        this.poolUUID = pool.get("uuid").getAsString();
    }
}
