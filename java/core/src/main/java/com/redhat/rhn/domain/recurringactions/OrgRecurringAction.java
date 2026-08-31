/*
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

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.recurringactions.type.RecurringActionType;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.utils.MinionServerUtils;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

/**
 * Recurring Action for organization implementation
 */

@Entity
@DiscriminatorValue("organization")
public class OrgRecurringAction extends RecurringAction {


    @ManyToOne
    @JoinColumn(name = "org_id")
    private Org org;

    /**
     * Standard constructor
     */
    public OrgRecurringAction() {
    }

    /**
     * Constructor
     *
     * @param actionType the recurring action type
     * @param active if action is active
     * @param orgIn organization affiliated with the action
     * @param creator the creator User
     */
    public OrgRecurringAction(RecurringActionType actionType, boolean active, Org orgIn, User creator) {
        super(actionType, active, creator);
        this.org = orgIn;
    }

    /**
     * Gets the list of minion servers
     *
     * @return list of minion servers
     */
    @Override
    public List<MinionServer> computeMinions() {
        return MinionServerUtils.filterSaltMinions(ServerFactory.listOrgSystems(org.getId()))
                .collect(Collectors.toList());
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
    public Long getEntityId() {
        return getOrg().getId();
    }

    @Override
    public TargetType getTargetType() {
        return TargetType.ORG;
    }

    /**
     * Gets the organization
     *
     * @return the organization
     */
    public Org getOrg() {
        return org;
    }

    /**
     * Sets the organization
     *
     * @param orgIn the organization
     */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
    }

    @Override
    public String toString() {
        return super.toStringBuilder()
                .append("org", org)
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

        OrgRecurringAction that = (OrgRecurringAction) o;

        return new EqualsBuilder()
                .append(getName(), that.getName())
                .append(org, that.org)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getName())
                .append(org)
                .toHashCode();
    }
}
