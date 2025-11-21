/*
 * Copyright (c) 2020--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.domain.recurringactions;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.recurringactions.type.RecurringActionType;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.legacy.UserImpl;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.hibernate.annotations.Type;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

/**
 * Recurring Action base class
 */

@Entity
@Table(name = "suseRecurringAction")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "target_type")
public abstract class RecurringAction extends BaseDomainHelper {

    private Long id;
    private String name;
    private String cronExpr;
    private boolean active;
    private UserImpl creator;
    private RecurringActionType recurringActionType;
    private RecurringActionType.ActionType actionType;

    public static final String RECURRING_ACTION_PREFIX = "recurring-action-";

    /**
     * Recurring action target types
     */
    public enum TargetType {
        MINION,
        GROUP,
        ORG
    }

    /**
     * Standard constructor
     */
    protected RecurringAction() { }

    /**
     * Constructor
     *
     * @param recurringActionTypeIn the recurring action type
     * @param isActive if action is active
     * @param creatorIn the creator User
     */
    protected RecurringAction(RecurringActionType recurringActionTypeIn, boolean isActive, User creatorIn) {
        this.active = isActive;
        if (creatorIn instanceof UserImpl userImpl)  {
            this.creator = userImpl;
        }

        this.recurringActionType = recurringActionTypeIn;
        this.recurringActionType.setRecurringAction(this);
        this.actionType = recurringActionTypeIn.getActionType();
    }

    /**
     * Gets the list of minion servers
     *
     * @return list of minion servers
     */
    public abstract List<MinionServer> computeMinions();

    /**
     * Checks if the user can access the recurring action
     *
     * @param user the user to check
     * @return boolean indicating access
     */
    public abstract boolean canAccess(User user);

    /**
     * Gets the ID of underlying entity.
     *
     * @return the ID
     */
    @Transient
    public abstract Long getEntityId();

    /**
     * Returns the type of the entity
     *
     * @return the type of the entitiy
     */
    @Transient
    public abstract TargetType getTargetType();

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
    public void setId(Long actionId) {
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
     * Gets the cronExpr.
     *
     * @return cronExpr
     */
    @Column(name = "cron_expr")
    public String getCronExpr() {
        return cronExpr;
    }

    /**
     * Sets the cronExpr.
     *
     * @param cronExprIn the cronExpr
     */
    public void setCronExpr(String cronExprIn) {
        cronExpr = cronExprIn;
    }

    /**
     * Gets if action is active.
     *
     * @return active - if action is active
     */
    @Column
    @Type(type = "yes_no")
    public boolean isActive() {
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
    @ManyToOne
    @JoinColumn(name = "creator_id")
    public UserImpl getCreator() {
        return creator;
    }

    /**
     * Sets the creator.
     *
     * @param creatorIn the creator
     */
    public void setCreator(UserImpl creatorIn) {
        creator = creatorIn;
    }

    /**
     * Gets the RecurringActionType object
     *
     * @return the RecurringActionType
     */
    @OneToOne(mappedBy = "recurringAction", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    public RecurringActionType getRecurringActionType() {
        return recurringActionType;
    }

    /**
     * Sets the RecurringActionType
     *
     * @param recurringActionTypeIn the recurring action type
     */
    public void setRecurringActionType(RecurringActionType recurringActionTypeIn) {
        this.recurringActionType = recurringActionTypeIn;
    }

    /**
     * Gets the type of the Action
     *
     * @return the RecurringActionType.ActionType
     */
    @Column(name = "action_type")
    @Enumerated(EnumType.STRING)
    public RecurringActionType.ActionType getActionType() {
        return actionType;
    }

    /**
     * Sets the action type
     *
     * @param actionTypeIn the action type
     */
    public void setActionType(RecurringActionType.ActionType actionTypeIn) {
        actionType = actionTypeIn;
    }

    @Override
    public String toString() {
        return toStringBuilder().toString();
    }

    // to be used in subclasses
    protected ToStringBuilder toStringBuilder() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
                .append("id", id)
                .append("name", name);
    }
}
