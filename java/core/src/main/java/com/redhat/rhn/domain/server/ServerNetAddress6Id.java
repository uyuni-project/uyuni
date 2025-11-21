/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */


package com.redhat.rhn.domain.server;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;

public class ServerNetAddress6Id implements Serializable {

    @Serial
    private static final long serialVersionUID = -8659842664069262911L;

    private Long interfaceId;

    private String address;

    private String scope;

    /**
     * Constructor
     */
    public ServerNetAddress6Id() {
    }

    /**
     * Constructor
     *
     * @param interfaceIdIn the input interfaceId
     * @param addressIn     the input address
     * @param scopeIn       the input scope
     */
    public ServerNetAddress6Id(Long interfaceIdIn, String addressIn, String scopeIn) {
        interfaceId = interfaceIdIn;
        address = addressIn;
        scope = scopeIn;
    }

    public Long getInterfaceId() {
        return interfaceId;
    }

    public void setInterfaceId(Long interfaceIdIn) {
        interfaceId = interfaceIdIn;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String addressIn) {
        address = addressIn;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scopeIn) {
        scope = scopeIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof ServerNetAddress6Id that)) {
            return false;
        }

        return new EqualsBuilder()
                .append(interfaceId, that.interfaceId)
                .append(address, that.address)
                .append(scope, that.scope)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(interfaceId)
                .append(address)
                .append(scope)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "ServerNetAddress6Id{" +
                "interfaceId=" + interfaceId +
                ", address='" + address + '\'' +
                ", scope='" + scope + '\'' +
                '}';
    }
}
