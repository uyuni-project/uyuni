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

import com.redhat.rhn.domain.org.CustomDataKey;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;

public class CustomDataValueId implements Serializable {

    @Serial
    private static final long serialVersionUID = -442954424471296708L;

    private Server server;

    private CustomDataKey key;

    /**
     * Constructor
     */
    public CustomDataValueId() {
    }

    /**
     * Constructor
     *
     * @param serverIn the input server
     * @param keyIn    the input key
     */
    public CustomDataValueId(Server serverIn, CustomDataKey keyIn) {
        server = serverIn;
        key = keyIn;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server serverIn) {
        server = serverIn;
    }

    public CustomDataKey getKey() {
        return key;
    }

    public void setKey(CustomDataKey keyIn) {
        key = keyIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (!(oIn instanceof CustomDataValueId that)) {
            return false;
        }

        return new EqualsBuilder()
                .append(server, that.server)
                .append(key, that.key)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(server)
                .append(key)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "CustomDataValue{" +
                "server=" + server +
                ", key=" + key +
                '}';
    }
}
