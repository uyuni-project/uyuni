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
import com.redhat.rhn.manager.system.SystemManager;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * Recurring Action for minion server implementation
 */

@Entity
@DiscriminatorValue("minion")
public class MinionRecurringAction extends RecurringAction {

    private MinionServer minion;

    /**
     * Standard constructor
     */
    public MinionRecurringAction() {
    }

    /**
     * Constructor
     *
     * @param testMode if action is in test mode
     * @param active if action is active
     * @param minionServer minion affiliated with the action
     * @param creator the creator User
     */
    public MinionRecurringAction(boolean testMode, boolean active, MinionServer minionServer, User creator) {
        super(testMode, active, creator);
        this.minion = minionServer;
    }

    /**
     * Gets the list of minion servers
     *
     * @return list of minion servers
     */
    @Override
    public List<MinionServer> computeMinions() {
        return List.of(minion);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canAccess(User user) {
        return SystemManager.isAvailableToUser(user, minion.getId());
    }

    @Override
    @Transient
    public Long getEntityId() {
        return getMinion().getId();
    }

    @Override
    @Transient
    public Type getType() {
        return Type.MINION;
    }

    /**
     * Gets the minion
     *
     * @return the minion server
     */
    @ManyToOne
    @JoinColumn(name = "minion_id")
    public MinionServer getMinion() {
        return minion;
    }

    /**
     * Sets the minion
     *
     * @param minionServer the minion server
     */
    public void setMinion(MinionServer minionServer) {
        this.minion = minionServer;
    }

    @Override
    public String toString() {
        return super.toStringBuilder()
                .append("minion", minion == null ? null : minion.getId())
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

        MinionRecurringAction that = (MinionRecurringAction) o;

        return new EqualsBuilder()
                .append(getName(), that.getName())
                .append(minion, that.minion)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getName())
                .append(minion)
                .toHashCode();
    }
}
