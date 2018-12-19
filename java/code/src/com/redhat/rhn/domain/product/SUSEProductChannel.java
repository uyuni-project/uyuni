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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * POJO for a suseProductChannel row.
 */
public class SUSEProductChannel extends BaseDomainHelper implements Serializable {

    /** The id. */
    private Long id;

    /** The product. */
    private SUSEProduct product;

    /** The channel. */
    private Channel channel;

    private boolean mandatory;

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
     * Gets the product.
     *
     * @return the product
     */
    public SUSEProduct getProduct() {
        return product;
    }

    /**
     * Sets the product.
     * @param productIn the new product
     */
    public void setProduct(SUSEProduct productIn) {
        product = productIn;
    }

    /**
     * Gets the channel.
     * @return the channel
     */
    public Channel getChannel() {
        return channel;
    }

    /**
     * Sets the channel.
     * @param channelIn the new channel
     */
    public void setChannel(Channel channelIn) {
        channel = channelIn;
    }

    /**
     * Set if this is mandatory
     * @param mandatoryIn mandatory
     * @return the new {@link SUSEProductChannel}
     */
    public SUSEProductChannel setMandatory(boolean mandatoryIn) {
        this.mandatory = mandatoryIn;
        return this;
    }

    /**
     * @return true if the channel for that product is mandatory
     */
    public boolean isMandatory() {
        return mandatory;
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
            .append(getChannel(), other.getChannel())
            .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getProduct())
            .append(getChannel())
            .toHashCode();
    }
}
