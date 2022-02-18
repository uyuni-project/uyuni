/*
 * Copyright (c) 2017 SUSE LLC
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
import com.redhat.rhn.domain.Identifiable;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * ServerFQDN - Class representation of the table rhnServerFQDN
 */
public class ServerFQDN extends BaseDomainHelper implements Identifiable {

    private Long id;
    private Server server;
    private String name;
    private boolean primary;

    /**
     * Constructor for the class ServerFQDN
     */
    public ServerFQDN() {
    }

    /**
     * Constructor for the class ServerFQDN
     * @param s the server which is associated to this FQDN
     * @param n the FQDN associated with this server (may be more than one)
     */
    public ServerFQDN(Server s, String n) {
        this.server = s;
        this.name = n;
    }

    /**
     * @return Returns the id.
     */
    @Override
    public Long getId() {
        return id;
    }
    /**
     * @param idIn The id to set.
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param nameIn The name to set.
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @return Boolean which indicates primary FQDN
     */
    public boolean isPrimary() {
        return primary;
    }

    /**
     * @param primaryIn Boolean which sets primary FQDN
     */
    public void setPrimary(boolean primaryIn) {
        primary = primaryIn;
    }

    /**
     * @return the server name
     */
    public Server getServer() {
        return server;
    }
    /**
     * @param serverIn The server to set.
     */
    public void setServer(Server serverIn) {
        this.server = serverIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (o instanceof ServerFQDN) {
            ServerFQDN toCompare = (ServerFQDN) o;
            return new EqualsBuilder()
                    .append(name, toCompare.name)
                    .append(server.getId(), toCompare.server.getId())
                    .isEquals();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(name)
                .append(server.getId())
                .toHashCode();
    }
}
