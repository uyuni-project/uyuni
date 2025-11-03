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
package com.redhat.rhn.domain.action.ansible;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.action.Action;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * InventoryActionDetails - Class representation of the table rhnActionInventory.
 */
@Entity
@Table(name = "rhnActionInventory")
public class InventoryActionDetails extends BaseDomainHelper {

    @Id
    @GeneratedValue(generator = "act_inventory_seq")
    @GenericGenerator(
            name = "act_inventory_seq",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "rhn_act_inventory_id_seq"),
                    @Parameter(name = "increment_size", value = "1")
            })
    private long id;

    @Column(name = "inventory_path")
    private String inventoryPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", updatable = false, nullable = false)
    private Action parentAction;

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param idIn the id to set
     */
    protected void setId(long idIn) {
        this.id = idIn;
    }

    public String getInventoryPath() {
        return inventoryPath;
    }

    public void setInventoryPath(String inventoryPathIn) {
        this.inventoryPath = inventoryPathIn;
    }

    /**
     * Gets the parent Action associated with this ServerAction record
     * @return Returns the parentAction.
     */
    public Action getParentAction() {
        return parentAction;
    }

    /**
     * Sets the parent Action associated with this ServerAction record
     * @param parentActionIn The parentAction to set.
     */
    public void setParentAction(Action parentActionIn) {
        this.parentAction = parentActionIn;
    }
}
