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

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.utils.MinionServerUtils;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * Recurring Action for organization implementation
 */

@Entity
@DiscriminatorValue("organization")
public class OrgRecurringAction extends RecurringAction {

    private Org organization;

    /**
     * Standard constructor
     */
    public OrgRecurringAction() {
    }

    /**
     * Constructor
     *
     * @param testMode if action is in test mode
     * @param active if action is active
     * @param org organization affiliated with the action
     * @param creator the creator User
     */
    public OrgRecurringAction(boolean testMode, boolean active, Org org, User creator) {
        super(testMode, active, creator);
        this.organization = org;
    }

    /**
     * Gets the list of minion servers
     *
     * @return list of minion servers
     */
    @Override
    public List<MinionServer> computeMinions() {
        return MinionServerUtils.filterSaltMinions(ServerFactory.listOrgSystems(organization.getId()))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canAccess(User user) {
        return user.hasRole(RoleFactory.ORG_ADMIN);
    }

    @Override
    @Transient
    public Long getEntityId() {
        return getOrg().getId();
    }

    @Override
    @Transient
    public Type getType() {
        return Type.ORG;
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

        OrgRecurringAction that = (OrgRecurringAction) o;

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
