/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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

import com.redhat.rhn.domain.org.Org;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.io.Serializable;

/**
 * DistChannelMap - Class representation of the table rhnDistChannelMap.
 */
public class DistChannelMap implements Serializable {

    private static final long serialVersionUID = 4083273166300423729L;

    private Long id;
    private String os;
    private String release;
    private ChannelArch channelArch;
    private Org org;
    private Channel channel;

    /**
     * default Constructor
     */
    public DistChannelMap() {
    }

    /**
     * Constructor
     * @param orgIn organization
     * @param osIn operaing system
     * @param releaseIn release
     * @param chaIn channel architecture
     * @param channelIn channel
     */
    public DistChannelMap(Org orgIn, String osIn, String releaseIn,
            ChannelArch chaIn, Channel channelIn) {
        this.org = orgIn;
        this.os = osIn;
        this.release = releaseIn;
        this.channelArch = chaIn;
        this.channel = channelIn;
    }

    /**
     * Getter for os
     * @return String to get
    */
    public String getOs() {
        return this.os;
    }

    /**
     * Setter for os
     * @param osIn to set
    */
    public void setOs(String osIn) {
        this.os = osIn;
    }

    /**
     * Getter for release
     * @return String to get
    */
    public String getRelease() {
        return this.release;
    }

    /**
     * Setter for release
     * @param releaseIn to set
    */
    public void setRelease(String releaseIn) {
        this.release = releaseIn;
    }

    /**
     * @return the channelArch
     */
    public ChannelArch getChannelArch() {
        return channelArch;
    }

    /**
     * @param channelArchIn The channelArch to set.
     */
    public void setChannelArch(ChannelArch channelArchIn) {
        this.channelArch = channelArchIn;
    }

    /**
     * @return the channel
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * @param channelIn The channel to set.
     */
    public void setChannel(Channel channelIn) {
        this.channel = channelIn;
    }

    /**
     * @return Returns the org.
     */
    public Org getOrg() {
        return org;
    }

    /**
     * @param orgIn The org to set.
     */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
    }

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param idIn The id to set.
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof DistChannelMap)) {
            return false;
        }
        DistChannelMap castOther = (DistChannelMap) other;
        return new EqualsBuilder().append(getOs(), castOther.getOs()).append(getRelease(),
                castOther.getRelease()).append(getChannelArch(), castOther.getChannelArch())
                .append(getChannel(), castOther.getChannel()).isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getOs()).append(getRelease()).append(
                getChannelArch()).append(getChannel()).toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("os", os).append("release",
                release).append("channelArch", channelArch).append("channel",
                channel).toString();
    }
}
