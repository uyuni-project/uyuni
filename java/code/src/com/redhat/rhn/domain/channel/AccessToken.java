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
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Channel access token giving a minion access to one or more channels.
 */
@Entity
@Table(name = "suseChannelAccessToken")
public class AccessToken implements Serializable {

    @Serial
    private static final long serialVersionUID = -6987986592710703016L;

    @Id
    @GeneratedValue(generator = "suse_chan_access_token_seq")
    @GenericGenerator(
        name = "suse_chan_access_token_seq",
        strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
        parameters = {
            @Parameter(name = "sequence_name", value = "suse_chan_access_token_id_seq"),
            @Parameter(name = "increment_size", value = "1")
        })
    private Long id;

    @Column
    private String token;

    @Column
    private Date expiration;

    @Column(name = "created")
    private Date start;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "minion_id")
    private MinionServer minion;

    @ManyToMany(cascade = {CascadeType.MERGE, CascadeType.PERSIST}, fetch = FetchType.LAZY)
    @JoinTable(
                name = "suseChannelAccessTokenChannel",
                joinColumns = @JoinColumn(name = "token_id"),
                inverseJoinColumns = @JoinColumn(name = "channel_id"))
    private Set<Channel> channels;

    @Column
    @Type(type = "yes_no")
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
    protected void setId(Long idIn) {
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
        if (validIn && !this.valid && this.minion == null) {
            throw new AccessTokenChangeException("Cannot set valid token when it's invalid and no minion is set");
        }
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
    @Override
    public boolean equals(final Object other) {
        if (other instanceof AccessToken o) {
            return new EqualsBuilder()
                    .append(getToken(), o.getToken())
                    .append(getExpiration(), o.getExpiration())
                    .append(getChannels(), o.getChannels())
                    .append(getMinion(), o.getMinion())
                    .isEquals();
        }
        return false;
    }

}
