/**
 * Copyright (c) 2020 SUSE LLC
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

package com.redhat.rhn.domain.recurringactions;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.legacy.UserImpl;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.Type;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * Recurring Action base class
 */

@Entity
@Table(name = "suseRecurringAction") // TODO: Drop the suse prefix?
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "target_type")
public abstract class RecurringAction {

    private Long id;
    private String name;
    private boolean testMode;
    private boolean active;
    private User creator;

    public static final String RECURRING_ACTION_PREFIX = "recurring-action-";

    /**
     * Standard constructor
     */
    public RecurringAction() { }

    /**
     * Constructor
     *
     * @param test if action is in testMode
     * @param isActive if action is active
     * @param creatorIn the creator User
     */
    public RecurringAction(boolean test, boolean isActive, User creatorIn) {
        this.testMode = test;
        this.active = isActive;
        this.creator = creatorIn;
    }

    /**
     * Gets the list of minion servers
     *
     * @return list of minion servers
     */
    public abstract List<MinionServer> computeMinions();

    /**
     * Gets the name of the TaskoSchedule entry based on the entity id.
     * For this to work, the entity must have an ID (e.g. hibernate 'save' has been done)!
     *
     * @return the TaskoSchedule name
     */
    public String computeTaskoScheduleName() {
        if (getId() == null) {
            throw new IllegalArgumentException("ID attribute must not be null");
        }
        return RECURRING_ACTION_PREFIX + getId();
    }

    /**
     * Gets the id.
     *
     * @return id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "recurring_action_seq")
    @SequenceGenerator(name = "recurring_action_seq", sequenceName = "suse_recurring_action_id_seq", allocationSize = 1)
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param actionId - the id of the action
     */
    public void setId(long actionId) {
        this.id = actionId;
    }

    /**
     * Gets the name.
     *
     * @return name
     */
    @Column
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param nameIn the name
     */
    public void setName(String nameIn) {
        name = nameIn;
    }

    /**
     * Gets if action is testMode.
     *
     * @return testMode - if action is testMode
     */
    @Column(name = "test_mode")
    @Type(type = "yes_no")
    public boolean isTestMode() {
        return testMode;
    }

    /**
     * Sets testMode.
     *
     * @param test - testMode
     */
    public void setTestMode(boolean test) {
        this.testMode = test;
    }

    /**
     * Gets if action is active.
     *
     * @return active - if action is active
     */
    @Column
    @Type(type = "yes_no")
    public boolean isActive() { // TODO: Set schema type to boolean
        return active;
    }

    /**
     * Sets if action is active
     *
     * @param isActive - active
     */
    public void setActive(boolean isActive) {
        this.active = isActive;
    }

    /**
     * Gets the creator.
     *
     * @return creator
     */
    @ManyToOne(targetEntity = UserImpl.class)
    @JoinColumn(name = "creator_id")
    public User getCreator() {
        return creator;
    }

    /**
     * Sets the creator.
     *
     * @param creatorIn the creator
     */
    public void setCreator(User creatorIn) {
        creator = creatorIn;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("id", id)
                .toString();
    }
}
