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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * Composite id for {@link ServerPath}.
 */
public class ServerPathId implements Serializable {

    /** The server. */
    private Server server;

    /** The proxy server. */
    private Server proxyServer;

    /**
     * No arg costructor needed by Hibernate.
     */
    public ServerPathId() {
    }

    /**
     * @param serverIn the server
     * @param proxyServerIn the proxy server
     */
    public ServerPathId(Server serverIn, Server proxyServerIn) {
        this.server = serverIn;
        this.proxyServer = proxyServerIn;
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
     * @return the proxy server
     */
    public Server getProxyServer() {
        return proxyServer;
    }

    /**
     * @param proxyServerIn the proxy server
     */
    public void setProxyServer(Server proxyServerIn) {
        this.proxyServer = proxyServerIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getServer())
                .append(getProxyServer())
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ServerPathId)) {
            return false;
        }
        ServerPathId castOther = (ServerPathId) other;
        boolean equals1 = new EqualsBuilder().append(getServer(), castOther.getServer()).isEquals();
        boolean equals2 = new EqualsBuilder().append(getProxyServer(), castOther.getProxyServer()).isEquals();
        return equals1 && equals2;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("server", getServer())
                .append("proxyServer", getProxyServer())
                .toString();
    }

}
