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

import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * POJO for a rhnServerPath row.
 */
public class ServerPath extends BaseDomainHelper {

    /** The id. */
    private Long id;

    /** The server. */
    private Server server;

    /** The proxy server. */
    private Server proxyServer;

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
     * Gets the id.
     *
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param idIn the new id
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * Gets the server.
     *
     * @return the server
     */
    public Server getServer() {
        return server;
    }

    /**
     * Sets the server.
     *
     * @param serverIn the new server
     */
    public void setServer(Server serverIn) {
        server = serverIn;
    }

    /**
     * Gets the proxy server.
     *
     * @return the proxy server
     */
    public Server getProxyServer() {
        return proxyServer;
    }

    /**
     * Sets the proxy server.
     *
     * @param proxyServerIn the new proxy server
     */
    public void setProxyServer(Server proxyServerIn) {
        proxyServer = proxyServerIn;
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
            .append(getServer(), otherServerPath.getServer())
            .append(getProxyServer(), otherServerPath.getProxyServer())
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
            .append(getServer())
            .append(getProxyServer())
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
            .append("server", getServer())
            .append("proxyServer", getProxyServer())
            .append("position", getPosition())
            .append("hostname", getHostname())
            .toString();
    }
}
