/**
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
package com.redhat.rhn.domain.server;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * ClientCapabilityId
 */
public class ClientCapabilityId implements Serializable {

    private Server server;
    private Capability capability;

    /**
     * No arg constructor needed by Hibernate.
     */
    public ClientCapabilityId() {
    }

    /**
     * @param serverIn the server
     * @param capabilityIn the capability
     */
    public ClientCapabilityId(Server serverIn, Capability capabilityIn) {
        this.server = serverIn;
        this.capability = capabilityIn;
    }

    /**
     * @return the server
     */
    public Server getServer() {
        return server;
    }

    /**
     * @param serverIn the server
     */
    public void setServer(Server serverIn) {
        this.server = serverIn;
    }

    /**
     * @return the capability
     */
    public Capability getCapability() {
        return capability;
    }

    /**
     * @param capabilityIn the capability
     */
    public void setCapability(Capability capabilityIn) {
        this.capability = capabilityIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClientCapabilityId)) {
            return false;
        }

        ClientCapabilityId that = (ClientCapabilityId) o;
        return new EqualsBuilder()
                .append(server.getId(), that.server.getId())
                .append(capability.getId(), that.getCapability().getId())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(server)
                .append(capability)
                .toHashCode();
    }
}
