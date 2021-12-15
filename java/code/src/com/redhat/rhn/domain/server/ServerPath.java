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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.server;

import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * POJO for a rhnServerPath row.
 */
public class ServerPath extends BaseDomainHelper {

    /** The id. */
    private ServerPathId id;

    /** The position. */
    private Long position;

    /** The hostname. */
    private String hostname;

    /**
     * Default constructor.
     */
    public ServerPath() {
    }

    /**
     * @param positionIn the server position in the path chain
     * @param hostnameIn the hostname of the server
     */
    public ServerPath(Long positionIn, String hostnameIn) {
        this.position = positionIn;
        this.hostname = hostnameIn;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public ServerPathId getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param idIn the new id
     */
    public void setId(ServerPathId idIn) {
        id = idIn;
    }

    /**
     * Gets the position.
     *
     * @return the position
     */
    public Long getPosition() {
        return position;
    }

    /**
     * Sets the position.
     *
     * @param positionIn the new position
     */
    public void setPosition(Long positionIn) {
        position = positionIn;
    }

    /**
     * Gets the hostname.
     *
     * @return the hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Sets the hostname.
     *
     * @param hostnameIn the new hostname
     */
    public void setHostname(String hostnameIn) {
        hostname = hostnameIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ServerPath)) {
            return false;
        }
        ServerPath otherServerPath = (ServerPath) other;

        return new EqualsBuilder()
            .append(getId(), otherServerPath.getId())
            .append(getPosition(), otherServerPath.getPosition())
            .append(getHostname(), otherServerPath.getHostname())
            .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getId())
            .append(getPosition())
            .append(getHostname())
            .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", getId())
            .append("position", getPosition())
            .append("hostname", getHostname())
            .toString();
    }
}
