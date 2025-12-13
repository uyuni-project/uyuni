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

import org.hibernate.annotations.Type;
import org.hibernate.type.YesNoConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

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
    @Convert(converter = YesNoConverter.class)
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
