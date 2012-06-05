/**
 * Copyright (c) 2012 Novell
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

import java.util.HashSet;
import java.util.Set;

import com.redhat.rhn.domain.action.ActionChild;
import com.redhat.rhn.domain.product.SUSEProductUpgrade;

/**
 * DistUpgradeActionDetails - Class representation of the table rhnActionDup.
 */
public class DistUpgradeActionDetails extends ActionChild {

    private Long id;
    private char dryRun;
    private char fullUpdate;

    // Set of tasks to perform on single channels
    private Set<DistUpgradeChannelTask> channelTasks = new HashSet<DistUpgradeChannelTask>();

    // Set of product upgrades that will be performed
    // Note: product upgrades are relevant for SLE 10 only!
    private Set<SUSEProductUpgrade> productUpgrades = new HashSet<SUSEProductUpgrade>();

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
     * @return the dryRun
     */
    public char getDryRun() {
        return dryRun;
    }

    /**
     * @param dryRun the dryRun to set
     */
    public void setDryRun(char dryRun) {
        this.dryRun = dryRun;
    }

    /**
     * @return the fullUpdate
     */
    public char getFullUpdate() {
        return fullUpdate;
    }

    /**
     * @param fullUpdate the fullUpdate to set
     */
    public void setFullUpdate(char fullUpdate) {
        this.fullUpdate = fullUpdate;
    }

    /**
     * @return the channel tasks
     */
    public Set<DistUpgradeChannelTask> getChannelTasks() {
        return channelTasks;
    }

    /**
     * @param channelTasks the channel tasks to set
     */
    public void setChannelTasks(Set<DistUpgradeChannelTask> channelTasks) {
        this.channelTasks = channelTasks;
    }

    /**
     * Add a single {@link DistUpgradeChannelTask}.
     *
     * @param task the task to add
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
     * @param productUpgrades the productUpgrades to set
     */
    public void setProductUpgrades(Set<SUSEProductUpgrade> productUpgrades) {
        this.productUpgrades = productUpgrades;
    }

    /**
     * Add a single {@link SUSEProductUpgrade}.
     *
     * @param productUpgrade the productUpgrade to add
     */
    public void addProductUpgrade(SUSEProductUpgrade upgrade) {
        upgrade.setDetails(this);
        this.productUpgrades.add(upgrade);
    }
}
