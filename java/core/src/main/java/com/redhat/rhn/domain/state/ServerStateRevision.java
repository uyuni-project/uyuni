/*
 * Copyright (c) 2015 SUSE LLC
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
package com.redhat.rhn.domain.state;

import com.redhat.rhn.domain.server.Server;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Table;

/**
 * A state revision that is assigned to a server.
 */
@Entity
@Table(name = "suseServerStateRevision")
@PrimaryKeyJoinColumn(name = "state_revision_id")
public class ServerStateRevision extends StateRevision {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id", nullable = false)
    private Server server;

    /**
     * Instantiates a new Server state revision.
     */
    public ServerStateRevision() {
        server = new Server();
    }

    /**
     * Instantiates a new Server state revision.
     *
     * @param serverIn the server
     */
    public ServerStateRevision(Server serverIn) {
        this.setServer(serverIn);
    }

    /**
     * @return the server
     */
    public Server getServer() {
        return server;
    }

    /**
     * @param serverIn the server to set
     */
    public void setServer(Server serverIn) {
        this.server = serverIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ServerStateRevision otherRevision)) {
            return false;
        }
        return new EqualsBuilder()
                .appendSuper(super.equals(otherRevision))
                .append(getServer(), otherRevision.getServer())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .appendSuper(super.hashCode())
                .append(getServer())
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .appendSuper(super.toString())
                .append("server", getServer())
                .toString();
    }
}
