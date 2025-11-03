/*
 * Copyright (c) 2015--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.state;

import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.legacy.UserImpl;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.ListIndexBase;
import org.hibernate.annotations.Parameter;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderColumn;
import jakarta.persistence.Table;

/**
 * A generic state revision to be subclassed for instance as {@link ServerStateRevision}.
 */
@Entity
@Table(name = "suseStateRevision")
@Inheritance(strategy = InheritanceType.JOINED)
public class StateRevision {

    @Id
    @GeneratedValue(generator = "state_revision_seq")
    @GenericGenerator(
            name = "state_revision_seq",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "suse_state_revision_id_seq"),
                    @Parameter(name = "increment_size", value = "1")
            })
    private Long id;

    @OneToMany(mappedBy = "stateRevision", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Set<PackageState> packageStates = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(
            name = "suseStateRevisionConfigChannel",
            joinColumns = @JoinColumn(name = "state_revision_id"),
            inverseJoinColumns = @JoinColumn(name = "config_channel_id")
    )
    @OrderColumn(name = "position")
    @ListIndexBase(1)
    private List<ConfigChannel> configChannels = new ArrayList<>();

    @Column(name = "created", insertable = false, updatable = false)
    private Date created;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private UserImpl creator;

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
    public UserImpl getCreator() {
        return creator;
    }

    /**
     * @param creatorIn the creator to set
     */
    public void setCreator(User creatorIn) {
        if (creatorIn instanceof UserImpl userImpl) {
            this.creator = userImpl;
        }
        else {
            this.creator = null;
        }
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
        if (!(other instanceof StateRevision otherRevision)) {
            return false;
        }
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
