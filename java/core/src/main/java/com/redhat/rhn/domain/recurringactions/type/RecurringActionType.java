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

import com.redhat.rhn.domain.recurringactions.RecurringAction;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Transient;

/**
 * Recurring Action Type base class
 */
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class RecurringActionType implements Serializable {

    private long id;
    private RecurringAction recurringAction;

    public enum ActionType {
        HIGHSTATE("Highstate"),
        CUSTOMSTATE("Custom state"),
        PLAYBOOK("Ansible Playbook");
        private final String description;
        ActionType(String descriptionIn) {
            this.description = descriptionIn;
        }
        public String getDescription() {
            return this.description;
        }
    }

    /**
     * Standard constructor
     */
    protected RecurringActionType() {
    }

    /**
     * Returns the recurring action type
     *
     * @return recurring action type
     */
    @Transient
    public abstract ActionType getActionType();

    /**
     * Gets the recurring action id
     *
     * @return recurring action id
     */
    @Id
    public long getId() {
        return id;
    }

    /**
     * Sets the recurring action id
     *
     * @param idIn recurring action id
     */
    public void setId(long idIn) {
        id = idIn;
    }

    /**
     * Gets the RecurringAction object
     *
     * @return RecurringAction object
     */
    @OneToOne(optional = false)
    @JoinColumn(name = "rec_id")
    @MapsId
    public RecurringAction getRecurringAction() {
        return this.recurringAction;
    }

    /**
     * Sets the RecurringAction object
     *
     * @param recurringActionIn the RecurringAction object
     */
    public void setRecurringAction(RecurringAction recurringActionIn) {
        this.recurringAction = recurringActionIn;
    }
}
