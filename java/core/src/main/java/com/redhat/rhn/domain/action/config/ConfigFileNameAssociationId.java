/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.redhat.rhn.domain.action.config;

import com.redhat.rhn.domain.config.ConfigFileName;
import com.redhat.rhn.domain.server.Server;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.Date;

public class ConfigFileNameAssociationId implements Serializable {

    private ConfigFileName configFileName;
    private Server server;
    private Date created;
    private Date modified;

    /**
     * Constructor
     */
    public ConfigFileNameAssociationId() {
        created = new Date();
        modified = new Date();
    }

    /**
     * Constructor
     * @param fileNameIn the config file name
     * @param serverIn the server
     * @param createdIn the created date
     * @param modifiedIn the modified date
     */
    public ConfigFileNameAssociationId(ConfigFileName fileNameIn, Server serverIn, Date createdIn, Date modifiedIn) {
        configFileName = fileNameIn;
        server = serverIn;
        created = createdIn;
        modified = modifiedIn;
    }

    public ConfigFileName getConfigFileName() {
        return configFileName;
    }

    public void setConfigFileName(ConfigFileName configFileNameIn) {
        configFileName = configFileNameIn;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server serverIn) {
        server = serverIn;
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

        if (!(oIn instanceof ConfigFileNameAssociationId that)) {
            return false;
        }

        return new EqualsBuilder()
                .append(configFileName, that.configFileName)
                .append(server, that.server)
                .append(created, that.created)
                .append(modified, that.modified)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(configFileName)
                .append(server)
                .append(created)
                .append(modified)
                .toHashCode();
    }
}


