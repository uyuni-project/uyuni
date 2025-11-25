/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2009--2010 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.action.config;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.config.ConfigFileName;
import com.redhat.rhn.domain.server.Server;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;


/**
 * ConfigFileNameAssociation -- Represents DB table, rhnActionConfigFileName
 */
@Entity
@Table(name = "rhnActionConfigFileName")
@IdClass(ConfigFileNameAssociationId.class)
public class ConfigFileNameAssociation implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "config_file_name_id", nullable = false)
    private ConfigFileName configFileName;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "action_id", nullable = false)
    private Action parentAction;

    @Id
    @Column(name = "created", nullable = false, updatable = false)
    private Date created = new Date();

    @Id
    @Column(name = "modified", nullable = false)
    private Date modified = new Date();

    // replace @CreationTimestamp on created which does not work on composite keys
    // https://discourse.hibernate.org/t/
    // java-exception-when-using-creationtimestamp-or-updatetimestamp-annotation-in-embeddable/699/10
    /**
     * replace @CreationTimestamp
     */
    @PrePersist
    public void prePersist() {
        created = new Date();
    }

    // replace @UpdateTimestamp on modified which does not work in composite keys
    // https://discourse.hibernate.org/t/
    // java-exception-when-using-creationtimestamp-or-updatetimestamp-annotation-in-embeddable/699/10
    /**
     * replace @UpdateTimestamp
     */
    @PreUpdate
    public void preUpdate() {
        modified = new Date();
    }

    /**
     * @return Returns the configFileName.
     */
    public ConfigFileName getConfigFileName() {
        return configFileName;
    }

    /**
     * @param configFileNameIn The configFileName to set.
     */
    public void setConfigFileName(ConfigFileName configFileNameIn) {
        configFileName = configFileNameIn;
    }

    /**
     * @return Returns the server.
     */
    public Server getServer() {
        return server;
    }

    /**
     * @param serverIn The server to set.
     */
    public void setServer(Server serverIn) {
        server = serverIn;
    }

    /**
     * Gets the parent Action associated with this ServerAction record
     * @return Returns the parentAction.
     */
    public Action getParentAction() {
        return parentAction;
    }

    /**
     * Sets the parent Action associated with this ServerAction record
     * @param parentActionIn The parentAction to set.
     */
    public void setParentAction(Action parentActionIn) {
        this.parentAction = parentActionIn;
    }

    /**
     * Gets the current value of created
     * @return Date the current value
     */
    public Date getCreated() {
        return this.created;
    }

    /**
     * Sets the value of created to new value
     * @param createdIn New value for created
     */
    public void setCreated(Date createdIn) {
        this.created = createdIn;
    }

    /**
     * Gets the current value of modified
     * @return Date the current value
     */
    public Date getModified() {
        return this.modified;
    }

    /**
     * Sets the value of modified to new value
     * @param modifiedIn New value for modified
     */
    public void setModified(Date modifiedIn) {
        this.modified = modifiedIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (this == oIn) {
            return true;
        }

        if (!(oIn instanceof ConfigFileNameAssociation that)) {
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
