/*
 * Copyright (c) 2018 SUSE LLC
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

package com.redhat.rhn.domain.action.channel;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.channel.AccessToken;
import com.redhat.rhn.domain.channel.Channel;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Class representation of the table rhnActionSubChannels
 */
@Entity
@Table(name = "rhnActionSubChannels")
public class SubscribeChannelsActionDetails extends BaseDomainHelper {

    @Id
    @GeneratedValue(generator = "RHN_ACT_SUBSCR_CHNLS_ID_SEQ")
    @GenericGenerator(
            name = "RHN_ACT_SUBSCR_CHNLS_ID_SEQ",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "RHN_ACT_SUBSCR_CHNLS_ID_SEQ"),
                    @Parameter(name = "increment_size", value = "1")
            })
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_channel_id", updatable = false, nullable = true)
    private Channel baseChannel;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
        name = "rhnActionSubChannelsList",
        joinColumns = @JoinColumn(name = "subscribe_channels_id"),
        inverseJoinColumns = @JoinColumn(name = "channel_id"))
    private Set<Channel> channels = new HashSet<>();

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JoinTable(
        name = "rhnActionSubChannelsTokens",
        joinColumns = @JoinColumn(name = "subscribe_channels_id"),
        inverseJoinColumns = @JoinColumn(name = "token_id"))
    private Set<AccessToken> accessTokens = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id", updatable = false, nullable = false)
    private Action parentAction;

    /**
     * No arg constructor needed by Hibernate.
     */
    public SubscribeChannelsActionDetails() { }

    /**
     * Constructor.
     * @param baseChannelIn the base channel
     * @param channelsIn the child channels
     */
    public SubscribeChannelsActionDetails(Channel baseChannelIn, Set<Channel> channelsIn) {
        this.baseChannel = baseChannelIn;
        this.channels = channelsIn;
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param idIn to set
     */
    protected void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return the base channel to set
     */
    public Channel getBaseChannel() {
        return baseChannel;
    }

    /**
     * @param baseChannelIn to set
     */
    public void setBaseChannel(Channel baseChannelIn) {
        this.baseChannel = baseChannelIn;
    }

    /**
     * @return channels to set
     */
    public Set<Channel> getChannels() {
        return channels;
    }

    /**
     * @param channelsIn to set
     */
    public void setChannels(Set<Channel> channelsIn) {
        this.channels = channelsIn;
    }

    /**
     * @return newAccessTokens to get
     */
    public Set<AccessToken> getAccessTokens() {
        return accessTokens;
    }

    /**
     * Gets the parent Action associated with this ServerAction record
     * @return Returns the parentAction.
     */
    public Action getParentAction() {
        return parentAction;
    }

    /**
     * Sets the parent Action associated with this ServerAction record
     * @param parentActionIn The parentAction to set.
     */
    public void setParentAction(Action parentActionIn) {
        this.parentAction = parentActionIn;
    }

    /**
     * @param newAccessTokensIn to set
     */
    public void setAccessTokens(Set<AccessToken> newAccessTokensIn) {
        this.accessTokens = newAccessTokensIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof SubscribeChannelsActionDetails that)) {
            return false;
        }
        return new EqualsBuilder()
                .append(baseChannel, that.baseChannel)
                .append(channels, that.channels)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(baseChannel)
                .append(channels)
                .toHashCode();
    }
}
