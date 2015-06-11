/**
 * Copyright (c) 2014 SUSE LLC
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
package com.redhat.rhn.domain.product;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.channel.Channel;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * Object representation of a SUSE product channel relationship.
 */
public class SUSEProductChannel extends BaseDomainHelper implements Serializable {

    private Long id;
    private SUSEProduct product;
    private Channel channel;
    private String channelLabel;
    private String parentChannelLabel;

    /**
     * Gets the id.
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     * @param idIn the new id
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * @return the product
     */
    public SUSEProduct getProduct() {
        return product;
    }

    /**
     * @param productIn the product to set
     */
    public void setProduct(SUSEProduct productIn) {
        this.product = productIn;
    }

    /**
     * @return the channel
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * @param channelIn the channel to set
     */
    public void setChannel(Channel channelIn) {
        this.channel = channelIn;
    }

    /**
     * @return the channelLabel
     */
    public String getChannelLabel() {
        return channelLabel;
    }

    /**
     * @param channelLabelIn the channelLabel to set
     */
    public void setChannelLabel(String channelLabelIn) {
        this.channelLabel = channelLabelIn;
    }

    /**
     * @return the parentChannelLabel
     */
    public String getParentChannelLabel() {
        return parentChannelLabel;
    }

    /**
     * @param parentChannelLabelIn the parentChannelLabel to set
     */
    public void setParentChannelLabel(String parentChannelLabelIn) {
        this.parentChannelLabel = parentChannelLabelIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object otherObject) {
        if (!(otherObject instanceof SUSEProductChannel)) {
            return false;
        }
        SUSEProductChannel other = (SUSEProductChannel) otherObject;
        return new EqualsBuilder()
            .append(getProduct(), other.getProduct())
            .append(getChannelLabel(), other.getChannelLabel())
            .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getProduct())
            .append(getChannelLabel())
            .toHashCode();
    }
}
