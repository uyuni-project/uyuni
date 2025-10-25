/*
 * Copyright (c) 2012--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.action.dup;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.product.SUSEProductUpgrade;
import com.redhat.rhn.domain.server.Server;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;

/**
 * DistUpgradeActionDetails - Class representation of the table rhnActionDup.
 */
@Entity
@Table(name = "rhnActionDup")
public class DistUpgradeActionDetails extends BaseDomainHelper {

    @Id
    @GeneratedValue(generator = "RHN_ACTIONDUP_ID_SEQ")
    @GenericGenerator(
        name = "RHN_ACTIONDUP_ID_SEQ",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
                @Parameter(name = "sequence_name", value = "RHN_ACTIONDUP_ID_SEQ"),
                @Parameter(name = "increment_size", value = "1")
        })
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id")
    private Server server;

    @Column(name = "dry_run")
    @Type(type = "yes_no")
    private boolean dryRun;

    @Column(name = "allow_vendor_change")
    @Type(type = "yes_no")
    private boolean allowVendorChange;

    @Column(name = "full_update")
    @Type(type = "yes_no")
    private boolean fullUpdate;

    @Column(name = "missing_successors")
    private String missingSuccessors;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", updatable = false, nullable = false, insertable = false)
    private Action parentAction;


    // Set of tasks to perform on single channels
    @OneToMany(mappedBy = "details", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("task asc")
    private Set<DistUpgradeChannelTask> channelTasks = new HashSet<>();

    // Set of product upgrades that will be performed
    // Note: product upgrades are relevant for SLE 10 only!
    @OneToMany(mappedBy = "details", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("to_pdid asc")
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
    protected void setId(Long idIn) {
        this.id = idIn;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server serverIn) {
        this.server = serverIn;
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
     * @return the missingSuccessors as a comma separated String
     */
    public String getMissingSuccessors() {
        return missingSuccessors;
    }

    /**
     * @param missingSuccessorsIn a comma separated string
     */
    public void setMissingSuccessors(String missingSuccessorsIn) {
        this.missingSuccessors = missingSuccessorsIn;
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
