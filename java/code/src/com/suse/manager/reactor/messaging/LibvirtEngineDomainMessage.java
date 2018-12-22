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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.Optional;

/**
 * Libvirt_events engine domain message to handle
 */
public abstract class LibvirtEngineDomainMessage extends AbstractLibvirtEngineMessage {

    private String domainName;
    private String domainId;
    private String domainUUID;

    /**
     * Create a domain message corresponding to the event type
     *
     * @param connection libvirt connection
     * @param eventType libvirt event type (lifecycle, refresh, etc)
     * @param minionId the minion ID or empty if the message comes from the master
     * @param timestamp the event time stamp
     * @param data the JSon data of the event
     *
     * @return a specialized object matching the event
     */
    public static AbstractLibvirtEngineMessage createDomainMessage(String connection,
            String eventType, Optional<String> minionId, String timestamp,
            JsonElement data) {
        if (eventType.equals("lifecycle")) {
            return new LibvirtEngineDomainLifecycleMessage(connection, minionId,
                                                           timestamp, data);
        }
        return null;
    }

    /**
     * @return name of the domain related to the event
     */
    public String getDomainName() {
        return domainName;
    }

    /**
     * @return id of the domain related to the event
     */
    public String getDomainId() {
        return domainId;
    }

    /**
     * @return uuid of the domain related to the event
     */
    public String getDomainUUID() {
        return domainUUID;
    }

    @Override
    public String toString() {
        return super.toString() + "[" + domainName + "]";
    }

    protected LibvirtEngineDomainMessage(String connection, Optional<String> minionId,
            String timestamp, JsonElement data) {
        super(connection, minionId, timestamp);

        JsonObject domain = data.getAsJsonObject().get("domain").getAsJsonObject();
        this.domainName = domain.get("name").getAsString();
        this.domainId = domain.get("id").getAsString();
        this.domainUUID = domain.get("uuid").getAsString();
    }
}
