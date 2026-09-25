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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.domain.recurringactions.type;

import com.redhat.rhn.common.hibernate.HibernateFactory;

import org.hibernate.type.YesNoConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Recurring Action type for state implementation
 */

@Entity
@Table(name = "suseRecurringPlaybook")
public class RecurringPlaybook extends RecurringActionType {


    @Column(name = "extra_vars")
    private byte[] extraVars;

    @Column(name = "flush_cache")
    @Convert(converter = YesNoConverter.class)
    private boolean flushCache;

    @Column(name = "inventory_path")
    private String inventoryPath;

    @Column(name = "playbook_path")
    private String playbookPath;

    @Column(name = "test_mode")
    @Convert(converter = YesNoConverter.class)
    private boolean testMode;

    /**
     * Standard constructor
     */
    public RecurringPlaybook() {
    }

    /**
     * Constructor
     *
     * @param testModeIn if action is in testMode
     */
    public RecurringPlaybook(boolean testModeIn) {
        super();
        this.testMode = testModeIn;
    }

    @Override
    public ActionType getActionType() {
        return ActionType.PLAYBOOK;
    }

    /**
     * Gets the extra vars
     *
     * @return the extra vars
     */
    public byte[] getExtraVars() {
        return extraVars;
    }

    /**
     * Sets the extra vars
     *
     * @param extraVarsIn the extra vars
     */
    public void setExtraVars(byte[] extraVarsIn) {
        extraVars = extraVarsIn;
    }

    /**
     * @return String version of the Script contents
     */
    public String getExtraVarsContents() {
        return HibernateFactory.getByteArrayContents(getExtraVars());
    }

    /**
     * Gets if the cache should be flushed
     *
     * @return flushCache - if the cache should be flushed
     */
    public boolean isFlushCache() {
        return flushCache;
    }

    /**
     * Sets if the cache should be flushed
     *
     * @param flushCacheIn if the cache should be flushed
     */
    public void setFlushCache(boolean flushCacheIn) {
        flushCache = flushCacheIn;
    }

    /**
     * Gets the inventory path
     *
     * @return the inventory path
     */
    public String getInventoryPath() {
        return inventoryPath;
    }

    /**
     * Sets the inventory path
     *
     * @param inventoryPathIn the inventory path
     */
    public void setInventoryPath(String inventoryPathIn) {
        inventoryPath = inventoryPathIn;
    }

    /**
     * Gets the playbook path
     *
     * @return the playbook path
     */
    public String getPlaybookPath() {
        return playbookPath;
    }

    /**
     * Sets the playbook path
     *
     * @param playbookPathIn the playbook path
     */
    public void setPlaybookPath(String playbookPathIn) {
        playbookPath = playbookPathIn;
    }

    /**
     * Gets if action is in testMode.
     *
     * @return testMode - if action is testMode
     */
    public boolean isTestMode() {
        return this.testMode;
    }

    /**
     * Sets testMode.
     *
     * @param testModeIn - testMode
     */
    public void setTestMode(boolean testModeIn) {
        this.testMode = testModeIn;
    }
}
