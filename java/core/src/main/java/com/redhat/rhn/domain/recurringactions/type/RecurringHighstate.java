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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.redhat.rhn.domain.recurringactions.type;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Recurring Action type for highstate implementation
 */

@Entity
@Table(name = "suseRecurringHighstate")
public class RecurringHighstate extends RecurringActionType {

    private boolean testMode;

    /**
     * Standard constructor
     */
    public RecurringHighstate() {
    }

    /**
     * Constructor
     *
     * @param testModeIn if action is in testMode
     */
    public RecurringHighstate(boolean testModeIn) {
        super();
        setTestMode(testModeIn);
    }

    @Override
    @Transient
    public ActionType getActionType() {
        return ActionType.HIGHSTATE;
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
}
