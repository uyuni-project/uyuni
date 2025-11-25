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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.channel;

import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * POJO for a rhnPublicChannelFamily row.
 */
@Entity
@Table(name = "rhnPublicChannelFamily")
public class PublicChannelFamily extends BaseDomainHelper {

    /** The id, which is also the associated channel family id */
    @Id
    @Column(name = "channel_family_id")
    private long id;

    /** The channel family. */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "channel_family_id")
    @MapsId
    private ChannelFamily channelFamily;

    /**
     * Gets the channel family id.
     * @return the channel family id.
     */
    public long getId() {
        return id;
    }

    /**
     * Sets the channel family id.
     * @param idIn The channel family id to set.
     */
    public void setId(long idIn) {
        this.id = idIn;
    }

    /**
     * Gets the channel family.
     * @return returns the channel family.
     */
    public ChannelFamily getChannelFamily() {
        return channelFamily;
    }

    /**
     * Sets the channel family.
     * @param channelFamilyIn The channel family to set.
     */
    public void setChannelFamily(ChannelFamily channelFamilyIn) {
        this.channelFamily = channelFamilyIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof PublicChannelFamily otherFamily)) {
            return false;
        }
        return new EqualsBuilder()
            .append(getChannelFamily(), otherFamily.getChannelFamily())
            .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getChannelFamily())
            .toHashCode();
    }
}
