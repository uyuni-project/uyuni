/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.action.config;

import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.server.Server;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Date;

public class ConfigChannelAssociationId implements Serializable {

    private Server server;
    private ConfigChannel configChannel;
    private Date created;
    private Date modified;

    /**
     * Constructor
     */
    public ConfigChannelAssociationId() {
        created = new Date();
        modified = new Date();
    }

    /**
     * Constructor
     * @param serverIn the server
     * @param channelIn the config channel
     * @param createdIn the created date
     * @param modifiedIn the modified date
     */
    public ConfigChannelAssociationId(Server serverIn, ConfigChannel channelIn, Date createdIn, Date modifiedIn) {
        server = serverIn;
        configChannel = channelIn;
        created = createdIn;
        modified = modifiedIn;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server serverIn) {
        server = serverIn;
    }

    public ConfigChannel getConfigChannel() {
        return configChannel;
    }

    public void setConfigChannel(ConfigChannel configChannelIn) {
        configChannel = configChannelIn;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date createdIn) {
        created = createdIn;
    }

    public Date getModified() {
        return modified;
    }

    public void setModified(Date modifiedIn) {
        modified = modifiedIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (this == oIn) {
            return true;
        }

        if (!(oIn instanceof ConfigChannelAssociationId that)) {
            return false;
        }

        return new EqualsBuilder()
                .append(server, that.server)
                .append(configChannel, that.configChannel)
                .append(created, that.created)
                .append(modified, that.modified)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(server)
                .append(configChannel)
                .append(created)
                .append(modified)
                .toHashCode();
    }
}
