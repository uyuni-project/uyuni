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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.server;

import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Immutable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * ClientCapability
 */
@Entity
@Table(name = "rhnClientCapability")
@Immutable
@IdClass(ClientCapabilityId.class)
public class ClientCapability extends BaseDomainHelper {

    @Id
    @ManyToOne
    @JoinColumn(name = "server_id")
    private Server server;

    @Id
    @ManyToOne
    @JoinColumn(name = "capability_name_id")
    private Capability capability;

    @Column(nullable = false)
    private long version;

    /**
     * No arg constructor needed by Hibernate.
     */
    public ClientCapability() {
    }

    /**
     * @param serverIn the server
     * @param capabilityIn the capability
     * @param versionIn the version
     */
    public ClientCapability(Server serverIn, Capability capabilityIn, long versionIn) {
        this.server = serverIn;
        this.capability = capabilityIn;
        this.version = versionIn;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server serverIn) {
        server = serverIn;
    }

    public Capability getCapability() {
        return capability;
    }

    public void setCapability(Capability capabilityIn) {
        capability = capabilityIn;
    }

    /**
     * @return the version
     */
    public long getVersion() {
        return version;
    }

    /**
     * @param versionIn set the version
     */
    public void setVersion(long versionIn) {
        this.version = versionIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ClientCapability that)) {
            return false;
        }
        return new EqualsBuilder()
                .append(getServer(), that.getServer())
                .append(getCapability(), that.getCapability())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getServer())
                .append(getCapability())
                .toHashCode();
    }

}
