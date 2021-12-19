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

import com.redhat.rhn.domain.action.ActionChild;
import com.redhat.rhn.domain.channel.AccessToken;
import com.redhat.rhn.domain.channel.Channel;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashSet;
import java.util.Set;

/**
 * Class representation of the table rhnActionSubChannels
 */
public class SubscribeChannelsActionDetails extends ActionChild {

    private Long id;
    private Channel baseChannel;
    private Set<Channel> channels = new HashSet<>();
    private Set<AccessToken> accessTokens = new HashSet<>();

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
    public void setId(Long idIn) {
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

        if (!(o instanceof SubscribeChannelsActionDetails)) {
            return false;
        }

        SubscribeChannelsActionDetails that = (SubscribeChannelsActionDetails) o;

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
