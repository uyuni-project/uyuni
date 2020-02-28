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

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.system.ServerGroupManager;

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
 * Recurring Action for server group implementation
 */

@Entity
@DiscriminatorValue("group")
public class GroupRecurringAction extends RecurringAction {

    private ServerGroup group;

    /**
     * Standard constructor
     */
    public GroupRecurringAction() {
    }

    /**
     * Constructor
     *
     * @param testMode if action is in test mode
     * @param active if action is active
     * @param serverGroup group affiliated with the action
     * @param creator the creator User
     */
    public GroupRecurringAction(boolean testMode, boolean active, ServerGroup serverGroup, User creator) {
        super(testMode, active, creator);
        this.group = serverGroup;
    }

    /**
     * Gets the list of minion servers
     *
     * @return list of minion servers
     */
    @Override
    public List<MinionServer> computeMinions() {
        return MinionServerUtils.filterSaltMinions(ServerGroupFactory.listServers(group))
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canAccess(User user) {
        ServerGroupManager groupManager = ServerGroupManager.getInstance();
        if (!user.hasRole(RoleFactory.SYSTEM_GROUP_ADMIN)) {
            return false;
        }
        try {
            /* Check if user has permission to access the group */
            groupManager.lookup(group.getId(), user);
        }
        catch (LookupException e) {
            return false;
        }
        return true;
    }

    @Override
    @Transient
    public Long getEntityId() {
        return getGroup().getId();
    }

    @Override
    @Transient
    public Type getType() {
        return Type.GROUP;
    }

    /**
     * Gets the server group
     *
     * @return the server group
     */
    @ManyToOne
    @JoinColumn(name = "group_id")
    public ServerGroup getGroup() {
        return group;
    }

    /**
     * Sets the group
     *
     * @param serverGroup the server group
     */
    public void setGroup(ServerGroup serverGroup) {
        this.group = serverGroup;
    }

    @Override
    public String toString() {
        return super.toStringBuilder()
                .append("group", group)
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

        GroupRecurringAction that = (GroupRecurringAction) o;

        return new EqualsBuilder()
                .append(getName(), that.getName())
                .append(group, that.group)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getName())
                .append(group)
                .toHashCode();
    }
}
