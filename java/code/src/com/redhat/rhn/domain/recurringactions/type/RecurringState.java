/*
 * Copyright (c) 2023 SUSE LLC
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
import com.redhat.rhn.domain.recurringactions.state.RecurringStateConfig;

import org.hibernate.annotations.Type;

import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Recurring Action type for state implementation
 */

@Entity
@Table(name = "suseRecurringState")
public class RecurringState extends RecurringActionType {

    private boolean testMode;
    private Set<RecurringStateConfig> stateConfig;

    /**
     * Standard constructor
     */
    public RecurringState() {
    }

    /**
     * Constructor
     *
     * @param testModeIn if action is in testMode
     */
    public RecurringState(boolean testModeIn) {
        super();
        this.testMode = testModeIn;
    }

    /**
     * Constructor
     *
     * @param stateConfigIn the recurring state config
     * @param testModeIn if action is in testMode
     */
    public RecurringState(Set<RecurringStateConfig> stateConfigIn, boolean testModeIn) {
        super();
        this.testMode = testModeIn;
        this.stateConfig = stateConfigIn;
        this.stateConfig.forEach(config -> config.setRecurringState(this));
    }

    /**
     * Save a new State Config
     *
     * @param stateConfigIn the state config
     */
    public void saveStateConfig(Set<RecurringStateConfig> stateConfigIn) {
        if (getStateConfig() != null) {
            HibernateFactory.delete(getStateConfig(), RecurringStateConfig.class);
        }
        this.stateConfig = stateConfigIn;
        this.stateConfig.forEach(c -> c.setRecurringState(this));
    }

    @Override
    @Transient
    public ActionType getActionType() {
        return ActionType.CUSTOMSTATE;
    }

    /**
     * Gets if action is in testMode.
     *
     * @return testMode - if action is testMode
     */
    @Column(name = "test_mode")
    @Type(type = "yes_no")
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

    /**
     * Gets the Recurring State Config
     *
     * @return the Recurring State Config
     */
    @OneToMany(mappedBy = "recurringState", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    public Set<RecurringStateConfig> getStateConfig() {
        return this.stateConfig;
    }

    /**
     * Set the Recurring State Config
     *
     * @param stateConfigIn the Recurring State Config
     */
    protected void setStateConfig(Set<RecurringStateConfig> stateConfigIn) {
        this.stateConfig = stateConfigIn;
    }
}
