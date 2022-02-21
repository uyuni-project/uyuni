/*
 * Copyright (c) 2016 SUSE LLC
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
package com.redhat.rhn.domain.channel;

import com.redhat.rhn.domain.server.MinionServer;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;
import java.util.Set;

/**
 * Channel access token giving a minion access to one or more channels.
 */
public class AccessToken {

    private Long id;
    private String token;
    private Date expiration;
    private Date start;
    private MinionServer minion;
    private Set<Channel> channels;
    private boolean valid = true;


    /**
     * @return the accessToken id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return the token
     */
    public String getToken() {
        return token;
    }

    /**
     * @param tokenIn the token to set
     */
    public void setToken(String tokenIn) {
        this.token = tokenIn;
    }

    /**
     * @return the expiration date
     */
    public Date getExpiration() {
        return expiration;
    }

    /**
     * @param expirationIn the expiration date to set
     */
    public void setExpiration(Date expirationIn) {
        this.expiration = expirationIn;
    }

    /**
     * @return the minion
     */
    public MinionServer getMinion() {
        return minion;
    }

    /**
     * @param minionIn the minion to set
     */
    public void setMinion(MinionServer minionIn) {
        this.minion = minionIn;
    }

    /**
     * @param validIn the new valid flag
     */
    public void setValid(boolean validIn) {
        this.valid = validIn;
    }

    /**
     * @return the valid flag
     */
    public boolean getValid() {
        return valid;
    }

    /**
     * @return the channels
     */
    public Set<Channel> getChannels() {
        return channels;
    }

    /**
     * @param channelsIn the channels to set
     */
    public void setChannels(Set<Channel> channelsIn) {
        this.channels = channelsIn;
    }

    /**
     * @return the start date
     */
    public Date getStart() {
        return start;
    }

    /**
     * @param startIn the start date
     */
    public void setStart(Date startIn) {
        this.start = startIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(getToken())
                .append(getExpiration())
                .append(getChannels())
                .append(getMinion())
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object other) {
        if (other instanceof AccessToken) {
            AccessToken o = (AccessToken)other;
            return new EqualsBuilder()
                    .append(getToken(), o.getToken())
                    .append(getExpiration(), o.getExpiration())
                    .append(getChannels(), o.getChannels())
                    .append(getMinion(), o.getMinion())
                    .isEquals();
        }
        else {
            return false;
        }

    }

}
