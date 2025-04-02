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
import javax.persistence.Embeddable;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * ServerNetAddress6
 */
@Entity
@Table(name = "rhnServerNetAddress6")
public class ServerNetAddress6 extends BaseDomainHelper implements Serializable {

    @Embeddable
    public static class ServerNetAddress6Id implements Serializable {

        @Column(name = "interface_id")
        private Long interfaceId;

        @Column(name = "address", length = 45)
        private String address;

        @Column(name = "scope", length = 64)
        private String scope;

        // Getters and setters for interfaceId, address, and scope

        public Long getInterfaceId() {
            return interfaceId;
        }

        public void setInterfaceId(Long interfaceIdIn) {
            this.interfaceId = interfaceIdIn;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String addressIn) {
            this.address = addressIn;
        }

        public String getScope() {
            return scope;
        }

        public void setScope(String scopeIn) {
            this.scope = scopeIn;
        }

        @Override
        public boolean equals(Object oIn) {
            if (!(oIn instanceof ServerNetAddress6Id that)) {
                return false;
            }
            return Objects.equals(interfaceId, that.interfaceId) &&
                    Objects.equals(address, that.address) &&
                    Objects.equals(scope, that.scope);
        }

        @Override
        public int hashCode() {
            return Objects.hash(interfaceId, address, scope);
        }
    }

    @EmbeddedId
    private ServerNetAddress6Id id = new ServerNetAddress6.ServerNetAddress6Id();

    @Column(name = "netmask", length = 49)
    private String netmask;

    public ServerNetAddress6Id getId() {
        return id;
    }

    public void setId(ServerNetAddress6Id idIn) {
        id = idIn;
    }


    /**
     * @return Returns the interfaceId.
     */
    public Long getInterfaceId() {
        return this.id.getInterfaceId();
    }

    /**
     * @param idIn Set the interfaceId.
     */
    public void setInterfaceId(Long idIn) {
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
     * @return Returns the scope.
     */
    public String getScope() {
        return this.id.getScope();
    }

    /**
     * @param s The scope to set.
     */
    public void setScope(String s) {
        this.id.setScope(s);
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
        if (!(other instanceof ServerNetAddress6 castOther)) {
            return false;
        }

        return new EqualsBuilder().append(this.getAddress(), castOther.getAddress())
                                  .append(this.getScope(), castOther.getScope())
                                  .append(this.getNetmask(), castOther.getNetmask())
                                  .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.getAddress())
                                    .append(this.getScope())
                                    .append(this.getNetmask())
                                    .toHashCode();
    }
}
