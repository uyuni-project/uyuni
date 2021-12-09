/*
 * Copyright (c) 2015 SUSE LLC
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
package com.redhat.rhn.domain.state;

import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.user.User;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A generic state revision to be subclassed for instance as {@link ServerStateRevision}.
 */
public class StateRevision {

    private long id;
    private Date created;
    private User creator;
    private Set<PackageState> packageStates = new HashSet<>();
    private List<ConfigChannel> configChannels = new ArrayList<>();

    /**
     * @return the id
     */
    public long getId() {
        return id;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(long idIn) {
        this.id = idIn;
    }

    /**
     * @return the created
     */
    public Date getCreated() {
        return created;
    }

    /**
     * @param createdIn the created to set
     */
    public void setCreated(Date createdIn) {
        this.created = createdIn;
    }

    /**
     * @return the creator
     */
    public User getCreator() {
        return creator;
    }

    /**
     * @param creatorIn the creator to set
     */
    public void setCreator(User creatorIn) {
        this.creator = creatorIn;
    }

    /**
     * @return the packageStates
     */
    public Set<PackageState> getPackageStates() {
        return packageStates;
    }

    /**
     * @param packageStatesIn the packageStates to set
     */
    public void setPackageStates(Set<PackageState> packageStatesIn) {
        this.packageStates = packageStatesIn;
    }

    /**
     * @param packageState the packageState to add
     */
    public void addPackageState(PackageState packageState) {
        this.packageStates.add(packageState);
    }

    /**
     * @return the config channels assigned to this server
     */
    public List<ConfigChannel> getConfigChannels() {
        return configChannels;
    }

    /**
     * @param configChannelsIn the config channels to assign to this server
     */
    public void setConfigChannels(List<ConfigChannel> configChannelsIn) {
        this.configChannels = configChannelsIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof StateRevision)) {
            return false;
        }
        StateRevision otherRevision = (StateRevision) other;
        return new EqualsBuilder()
                .append(getCreated(), otherRevision.getCreated())
                .append(getCreator(), otherRevision.getCreator())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getCreated())
                .append(getCreator())
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("created", getCreated())
                .append("creator", getCreator())
                .toString();
    }
}
