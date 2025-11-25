/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.domain.action;

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_ONLY;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * ActionType
 */
@Entity
@Table(name = "rhnActionType")
@Immutable
@Cache(usage = READ_ONLY)
public class ActionType implements Serializable {

    @Id
    private Integer id;
    @Column
    private String label;
    @Column
    private String name;
    @Column(name = "trigger_snapshot")
    private Character triggersnapshot;
    @Column(name = "unlocked_only")
    private Character unlockedonly;
    @Column(name = "maintenance_mode_only")
    private boolean maintenancemodeOnly;

    /**
     * @return Returns the id.
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param i The id to set.
     */
    protected void setId(Integer i) {
        this.id = i;
    }

    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param l The label to set.
     */
    public void setLabel(String l) {
        this.label = l;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param n The name to set.
     */
    public void setName(String n) {
        this.name = n;
    }

    /**
     * @return Returns the triggersnapshot.
     */
    public Character getTriggersnapshot() {
        return triggersnapshot;
    }

    /**
     * @param t The triggersnapshot to set.
     */
    public void setTriggersnapshot(Character t) {
        this.triggersnapshot = t;
    }

    /**
     * @return Returns the unlockedonly.
     */
    public Character getUnlockedonly() {
        return unlockedonly;
    }

    /**
     * @param u The unlockedonly to set.
     */
    public void setUnlockedonly(Character u) {
        this.unlockedonly = u;
    }

    /**
     * @return return maintenance mode only
     */
    public boolean isMaintenancemodeOnly() {
        return maintenancemodeOnly;
    }

    /**
     * Set maintenance mode only flag
     *
     * @param maintenancemodeOnlyIn maintenance mode only
     */
    public void setMaintenancemodeOnly(boolean maintenancemodeOnlyIn) {
        this.maintenancemodeOnly = maintenancemodeOnlyIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof ActionType other)) {
            return false;
        }
        return new EqualsBuilder().append(this.getId(), other.getId())
                                  .append(this.getName(), other.getName())
                                  .append(this.getLabel(), other.getLabel())
                                  .append(this.getTriggersnapshot(),
                                          other.getTriggersnapshot())
                                  .append(this.getUnlockedonly(), other.getUnlockedonly())
                                  .append(this.isMaintenancemodeOnly(), other.isMaintenancemodeOnly())
                                  .isEquals();
    }

    /**
     * Output ActionType to string
     * @return Returns ActionType as a String
     */
    @Override
    public String toString() {
        return label + " : " + name;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getId())
                                    .append(getName())
                                    .append(getLabel())
                                    .append(getTriggersnapshot())
                                    .append(getUnlockedonly())
                                    .append(isMaintenancemodeOnly())
                                    .toHashCode();
    }

    /**
     * Returns a name string from an Action type
     * @return a name
     */
    public String getPackageActionName() {
        if (equals(ActionFactory.TYPE_PACKAGES_REMOVE)) {
            return "Package Removal";
        }
        else if (equals(ActionFactory.TYPE_PACKAGES_UPDATE)) {
            return "Package Install/Upgrade";
        }
        else if (equals(ActionFactory.TYPE_PACKAGES_VERIFY)) {
            return "Package Verify";
        }
        else if (equals(ActionFactory.TYPE_PACKAGES_REFRESH_LIST)) {
            return "Package List Refresh";
        }
        else if (equals(ActionFactory.TYPE_PACKAGES_DELTA)) {
            return "Package Synchronization";
        }
        else if (equals(ActionFactory.TYPE_PACKAGES_LOCK)) {
            return "Lock packages";
        }
        return "";
    }
}
