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

import com.redhat.rhn.domain.audit.ScapPolicy;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Recurring Action type for scap policy implementation
 */

@Entity
@Table(name = "suseRecurringScapPolicy")
public class RecurringScapPolicy extends RecurringActionType {

    private boolean testMode;
    private ScapPolicy scapPolicy;

    /**
     * Standard constructor
     */
    public RecurringScapPolicy() {
    }

    /**
     * Constructor
     *
     * @param testModeIn if action is in testMode
     */
    public RecurringScapPolicy(boolean testModeIn) {
        super();
        this.testMode = testModeIn;
    }

    /**
     * Constructor
     *
     * @param scapPolicyIn the scap policy
     * @param testModeIn if action is in testMode
     */
    public RecurringScapPolicy(ScapPolicy scapPolicyIn, boolean testModeIn) {
        super();
        this.testMode = testModeIn;
        this.scapPolicy = scapPolicyIn;
    }

    @Override
    @Transient
    public ActionType getActionType() {
        return ActionType.SCAPPOLICY;
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
     * Gets the related Scap policy
     *
     * @return the Scap Policy
     */
    @ManyToOne
    @JoinColumn(name = "scap_policy_id", nullable = false)
    public ScapPolicy getScapPolicy() {
        return this.scapPolicy;
    }

    /**
     * Set the related Scap Policy
     *
     * @param scapPolicyIn the scap policy
     */
    public void setScapPolicy(ScapPolicy scapPolicyIn) {
        this.scapPolicy = scapPolicyIn;
    }
}
