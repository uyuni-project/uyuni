/*
 * Copyright (c) 2014--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.product;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.channel.Channel;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * POJO for a suseProductChannel row.
 */
@Entity
@Table(name = "suseProductChannel")
public class SUSEProductChannel extends BaseDomainHelper implements Serializable {

    /** The id. */
    @Id
    @GeneratedValue(generator = "suse_product_channel_seq")
    @GenericGenerator(
            name = "suse_product_channel_seq",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "suse_product_channel_id_seq"),
                    @Parameter(name = "increment_size", value = "1")
            })
    private Long id;

    /** The product. */
    @ManyToOne
    @JoinColumn(name = "product_id")
    private SUSEProduct product;

    /** The channel. */
    @ManyToOne
    @JoinColumn(name = "channel_id")
    private Channel channel;

    @Column
    @Type(type = "yes_no")
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
        if (!(otherObject instanceof SUSEProductChannel other)) {
            return false;
        }
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
