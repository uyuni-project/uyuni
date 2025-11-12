/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.channel;

import com.redhat.rhn.common.util.DynamicComparator;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;


/**
 * ReleaseChannelMap
 */
@Entity
@Table(name = "rhnReleaseChannelMap")
@IdClass(ReleaseChannelMapId.class)
public class ReleaseChannelMap implements Serializable, Comparable<ReleaseChannelMap> {

    private static final long serialVersionUID = 1L;

    @Id
    private String product;

    @Id
    private String version;

    @Id
    private String release;

    @Id
    @ManyToOne(targetEntity = ChannelArch.class)
    @JoinColumn(name = "channel_arch_id")
    private ChannelArch channelArch;

    @Id
    @ManyToOne(targetEntity = Channel.class)
    @JoinColumn(name = "channel_id")
    private Channel channel;

    /**
     * @return Returns the product.
     */
    public String getProduct() {
        return product;
    }

    /**
     * @param productIn The product to set.
     */
    public void setProduct(String productIn) {
        this.product = productIn;
    }

    /**
     * @return Returns the version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param versionIn The version to set.
     */
    public void setVersion(String versionIn) {
        this.version = versionIn;
    }

    /**
     * @return Returns the release.
     */
    public String getRelease() {
        return release;
    }

    /**
     * @param releaseIn The release to set.
     */
    public void setRelease(String releaseIn) {
        this.release = releaseIn;
    }

    /**
     * @return Returns the channelArch.
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
     * @return Returns the channel.
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
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ReleaseChannelMap castOther)) {
            return false;
        }
        return new EqualsBuilder().append(getProduct(), castOther.getProduct()).
            append(getRelease(), castOther.getRelease()).
            append(getVersion(), castOther.getVersion()).
            append(getChannelArch(), castOther.getChannelArch()).
            append(getChannel(), castOther.getChannel()).isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getProduct()).append(getVersion()).append(
                getRelease()).append(getChannelArch()).append(getChannel()).toHashCode();
    }

    /**
     * compare to ReleaseChannelMap
     * @param o the other object
     * @return the compare return
     */
    @SuppressWarnings("unchecked")
    @Override
    public int compareTo(ReleaseChannelMap o) {
        List<Comparator<ReleaseChannelMap>> compar = new ArrayList<>();

        compar.add(new DynamicComparator<>("channel", true));
        compar.add(new DynamicComparator<>("channelArch", true));
        compar.add(new DynamicComparator<>("product", true));
        compar.add(new DynamicComparator<>("version", true));
        compar.add(new DynamicComparator<>("release", true));

        Comparator<ReleaseChannelMap> com = ComparatorUtils.chainedComparator(compar.toArray(new Comparator[0]));
        return com.compare(this, o);
    }

}
