/*
 * Copyright (c) 2021 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.domain.server.ansible;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

/**
 * Ansible Inventory Path
 */
@Entity
@DiscriminatorValue("inventory")
public class InventoryPath extends AnsiblePath {

    private Set<Server> inventoryServers;

    /**
     * Standard constructor
     */
    public InventoryPath() {
        inventoryServers = new HashSet<>();
    }

    /**
     * Standard constructor
     * @param minionServer the minion server
     */
    public InventoryPath(MinionServer minionServer) {
        super(minionServer);
        inventoryServers = new HashSet<>();
    }

    @Override
    @Transient
    public Type getEntityType() {
        return Type.INVENTORY;
    }

    /**
     * Gets the inventory servers
     *
     * @return the inventory servers
     */
    @OneToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "suseAnsibleInventoryServers",
            joinColumns = @JoinColumn(name = "inventory_id"),
            inverseJoinColumns = @JoinColumn(name = "server_id")
    )
    public Set<Server> getInventoryServers() {
        return inventoryServers;
    }

    /**
     * Sets the inventory server
     *
     * @param inventoryServersIn the inventory servers
     */
    public void setInventoryServers(Set<Server> inventoryServersIn) {
        inventoryServers = inventoryServersIn;
    }

    @Override
    public boolean equals(Object oIn) {
        if (this == oIn) {
            return true;
        }
        if (!(oIn instanceof InventoryPath that)) {
            return false;
        }
        return new EqualsBuilder().appendSuper(super.equals(oIn))
                .append(inventoryServers, that.inventoryServers).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .appendSuper(super.hashCode())
                .append(inventoryServers)
                .toHashCode();
    }
}
