/*
 * Copyright (c) 2026 SUSE LLC
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

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.recurringactions.type.RecurringActionType;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.user.User;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collections;
import java.util.List;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;

/**
 * Recurring Action for actions that do not target an entity (e.g. CLM actions)
 */
@Entity
@DiscriminatorValue("no-entity")
public class NoEntityRecurringAction extends RecurringAction {

    private Org organization;

    /**
     * Standard constructor
     */
    public NoEntityRecurringAction() {
    }

    /**
     * Constructor
     *
     * @param actionType the recurring action type
     * @param active if action is active
     * @param org organization affiliated with the action
     * @param creator the creator User
     */
    public NoEntityRecurringAction(RecurringActionType actionType, boolean active, Org org, User creator) {
        super(actionType, active, creator);
        this.organization = org;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MinionServer> computeMinions() {
        return Collections.emptyList();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canAccess(User user) {
        return (user.hasRole(RoleFactory.ORG_ADMIN) && user.getOrg().equals(getOrg())) ||
                user.hasRole(RoleFactory.SAT_ADMIN);
    }

    @Override
    @Transient
    public Long getEntityId() {
        return getOrg().getId();
    }

    @Override
    @Transient
    public TargetType getTargetType() {
        return TargetType.NO_ENTITY;
    }

    /**
     * Gets the organization
     *
     * @return the organization
     */
    @ManyToOne
    @JoinColumn(name = "org_id")
    public Org getOrg() {
        return organization;
    }

    /**
     * Sets the organization
     *
     * @param org the organization
     */
    public void setOrg(Org org) {
        this.organization = org;
    }

    @Override
    public String toString() {
        return super.toStringBuilder()
                .append("org", organization)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        NoEntityRecurringAction that = (NoEntityRecurringAction) o;

        return new EqualsBuilder()
                .append(getName(), that.getName())
                .append(organization, that.organization)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getName())
                .append(organization)
                .toHashCode();
    }
}
