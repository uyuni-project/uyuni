/*
 * Copyright (c) 2012 SUSE LLC
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
package com.redhat.rhn.domain.action.dup;

import com.redhat.rhn.domain.action.ActionChild;
import com.redhat.rhn.domain.product.SUSEProductUpgrade;

import java.util.HashSet;
import java.util.Set;

/**
 * DistUpgradeActionDetails - Class representation of the table rhnActionDup.
 */
public class DistUpgradeActionDetails extends ActionChild {

    private Long id;
    private boolean dryRun;
    private boolean allowVendorChange;
    private boolean fullUpdate;

    // Set of tasks to perform on single channels
    private Set<DistUpgradeChannelTask> channelTasks =
            new HashSet<>();

    // Set of product upgrades that will be performed
    // Note: product upgrades are relevant for SLE 10 only!
    private Set<SUSEProductUpgrade> productUpgrades = new HashSet<>();

    /**
     * Return the ID.
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * Set the ID.
     * @param idIn id
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return the dryRun as boolean
     */
    public boolean isDryRun() {
        return dryRun;
    }

    /**
     * @param dryRunIn the dryRun to set
     */
    public void setDryRun(boolean dryRunIn) {
        this.dryRun = dryRunIn;
    }

    /**
     * Set if vendor changed allowed or not.
     * @param allowVendorChangeIn boolean
     */
    public void setAllowVendorChange(boolean allowVendorChangeIn) {
        this.allowVendorChange = allowVendorChangeIn;
    }

    /**
     * @return the allowVendorChange as boolean
     */
    public boolean isAllowVendorChange() {
        return allowVendorChange;
    }

    /**
     * @return the fullUpdate
     */
    public boolean isFullUpdate() {
        return fullUpdate;
    }

    /**
     * @param fullUpdateIn the fullUpdate to set
     */
    public void setFullUpdate(boolean fullUpdateIn) {
        this.fullUpdate = fullUpdateIn;
    }

    /**
     * @return the channel tasks
     */
    public Set<DistUpgradeChannelTask> getChannelTasks() {
        return channelTasks;
    }

    /**
     * @param channelTasksIn the channel tasks to set
     */
    public void setChannelTasks(Set<DistUpgradeChannelTask> channelTasksIn) {
        this.channelTasks = channelTasksIn;
    }

    /**
     * Add a single {@link DistUpgradeChannelTask}.
     *
     * @param channelTask the task to add
     */
    public void addChannelTask(DistUpgradeChannelTask channelTask) {
        channelTask.setDetails(this);
        this.channelTasks.add(channelTask);
    }

    /**
     * @return the product upgrades
     */
    public Set<SUSEProductUpgrade> getProductUpgrades() {
        return productUpgrades;
    }

    /**
     * @param productUpgradesIn the productUpgrades to set
     */
    public void setProductUpgrades(Set<SUSEProductUpgrade> productUpgradesIn) {
        this.productUpgrades = productUpgradesIn;
    }

    /**
     * Add a single {@link SUSEProductUpgrade}.
     *
     * @param upgrade the product upgrade to add
     */
    public void addProductUpgrade(SUSEProductUpgrade upgrade) {
        upgrade.setDetails(this);
        this.productUpgrades.add(upgrade);
    }
}
