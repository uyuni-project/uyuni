/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.redhat.rhn.domain.server;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;

public class ServerNetAddress4Id implements Serializable {

    @Serial
    private static final long serialVersionUID = 5312840630951522745L;

    private Long interfaceId;

    private String address;

    /**
     * Constructor
     */
    public ServerNetAddress4Id() {
    }

    /**
     * Constructor
     *
     * @param interfaceIdIn the input interfaceId
     * @param addressIn     the input address
     */
    public ServerNetAddress4Id(Long interfaceIdIn, String addressIn) {
        interfaceId = interfaceIdIn;
        address = addressIn;
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

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof ServerNetAddress4Id that)) {
            return false;
        }

        return new EqualsBuilder()
                .append(interfaceId, that.interfaceId)
                .append(address, that.address)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(interfaceId)
                .append(address)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "ServerNetAddress4Id{" +
                "interfaceId=" + interfaceId +
                ", address='" + address + '\'' +
                '}';
    }
}
