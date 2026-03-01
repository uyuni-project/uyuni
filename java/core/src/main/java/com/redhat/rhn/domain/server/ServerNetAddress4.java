/*
 * Copyright (c) 2009--2011 Red Hat, Inc.
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

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

/**
 * ServerNetAddress4
 */

@Entity
@Table(name = "rhnServerNetAddress4")
@IdClass(ServerNetAddress4Id.class)
public class ServerNetAddress4 extends BaseDomainHelper implements Serializable {

    @Id
    @Column(name = "interface_id")
    private Long interfaceId;

    @Id
    private String address;

    @Column
    private String netmask;

    @Column
    private String broadcast;

    /**
     * Hibernate constructor
     */
    protected ServerNetAddress4() {
        // Nothing to do
    }

    /**
     * Create a new instance
     * @param interfaceIdIn the id of the {@link NetworkInterface}
     * @param addressIn the IPv4 address
     */
    public ServerNetAddress4(Long interfaceIdIn, String addressIn) {
        this.interfaceId = interfaceIdIn;
        this.address = addressIn;
    }

    /**
     * @return Returns the interfaceId.
     */
    public Long getInterfaceId() {
        return interfaceId;
    }

    /**
     * @param id Set the interfaceId.
     */
    protected void setInterfaceId(Long id) {
        this.interfaceId = id;
    }

    /**
     * @return Returns the address.
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param i The address to set.
     */
    public void setAddress(String i) {
        this.address = i;
    }

    /**
     * @return Returns the broadcast.
     */
    public String getBroadcast() {
        return broadcast;
    }

    /**
     * @param b The broadcast to set.
     */
    public void setBroadcast(String b) {
        this.broadcast = b;
    }

    /**
     * @return Returns the netmask.
     */
    public String getNetmask() {
        return netmask;
    }

    /**
     * @param n The netmask to set.
     */
    public void setNetmask(String n) {
        this.netmask = n;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ServerNetAddress4 castOther)) {
            return false;
        }

        return new EqualsBuilder().append(this.getAddress(), castOther.getAddress())
                                  .append(this.getBroadcast(), castOther.getBroadcast())
                                  .append(this.getNetmask(), castOther.getNetmask())
                                  .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.getAddress())
                                    .append(this.getBroadcast())
                                    .append(this.getNetmask())
                                    .toHashCode();
    }
}
