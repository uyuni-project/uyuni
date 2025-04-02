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
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.ConstraintMode;
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * ServerNetAddress4
 */
@Entity
@Table(name = "rhnServerNetAddress4")
public class ServerNetAddress4 extends BaseDomainHelper implements Serializable {
    @Embeddable
    public static class ServerNetAddress4Id implements Serializable {

        @Column(name = "interface_id", nullable = false)
        private long interfaceId;

        @Column(name = "address", length = 64)
        private String address;

        /**
         * Default Constructor.
         */
        public ServerNetAddress4Id() {
        }

        /**
         * Constructor.
         * @param interfaceIdIn the interface ID
         * @param addressIn the address ID
         */
        public ServerNetAddress4Id(Long interfaceIdIn, String addressIn) {
            this.interfaceId = interfaceIdIn;
            this.address = addressIn;
        }

        public long getInterfaceId() {
            return interfaceId;
        }

        public void setInterfaceId(long interfaceIdIn) {
            this.interfaceId = interfaceIdIn;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String addressIn) {
            this.address = addressIn;
        }

        @Override
        public boolean equals(Object oIn) {
            if (!(oIn instanceof ServerNetAddress4Id that)) {
                return false;
            }
            return Objects.equals(interfaceId, that.interfaceId) && Objects.equals(address, that.address);
        }

        @Override
        public int hashCode() {
            return Objects.hash(interfaceId, address);
        }
    }

    @EmbeddedId
    private ServerNetAddress4Id id = new ServerNetAddress4Id();

    @ManyToOne
    @JoinColumn(name = "interface_id", referencedColumnName = "id",
            insertable = false, updatable = false,
            foreignKey = @ForeignKey(name = "rhn_srv_net_iaddress4_iid_fk", value = ConstraintMode.CONSTRAINT))
    private NetworkInterface interfaceRef;

    public NetworkInterface getInterfaceRef() {
        return interfaceRef;
    }

    public void setInterfaceRef(NetworkInterface interfaceRefIn) {
        interfaceRef = interfaceRefIn;
    }

    @Column(name = "netmask", length = 64)
    private String netmask;

    @Column(name = "broadcast", length = 64)
    private String broadcast;

    /**
     * Default Constructor.
     */
    public ServerNetAddress4() {
    }

    /**
     * @return Returns the id.
     */
    public ServerNetAddress4Id getId() {
        return id;
    }

    /**
     * @param idIn Set the id.
     */
    public void setId(ServerNetAddress4Id idIn) {
        id = idIn;
    }

    /**
     * @return Returns the interfaceId.
     */
    public long getInterfaceId() {
        return id.getInterfaceId();
    }

    /**
     * @param idIn Set the interfaceId.
     */
    public void setInterfaceId(long idIn) {
        this.id.setInterfaceId(idIn);
    }

    /**
     * @return Returns the address.
     */
    public String getAddress() {
        return this.id.getAddress();
    }

    /**
     * @param i The address to set.
     */
    public void setAddress(String i) {
        this.id.setAddress(i);
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
