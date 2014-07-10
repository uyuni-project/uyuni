/**
 * Copyright (c) 2014 SUSE
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

import java.io.Serializable;

/**
 * Object representation of a SUSE product channel relationship.
 */
public class SUSEProductChannel extends BaseDomainHelper implements Serializable {

    private SUSEProduct product;
    private Channel channel;
    private String channelLabel;
    private String parentChannelLabel;

    /**
     * @return the productId
     */
    public SUSEProduct getProduct() {
        return product;
    }
    /**
     * @param productId the productId to set
     */
    public void setProduct(SUSEProduct product) {
        this.product = product;
    }
    /**
     * @return the channelId
     */
    public Channel getChannel() {
        return channel;
    }
    /**
     * @param channelId the channelId to set
     */
    public void setChannel(Channel channel) {
        this.channel = channel;
    }
    /**
     * @return the channelLabel
     */
    public String getChannelLabel() {
        return channelLabel;
    }
    /**
     * @param channelLabel the channelLabel to set
     */
    public void setChannelLabel(String channelLabel) {
        this.channelLabel = channelLabel;
    }
    /**
     * @return the parentChannelLabel
     */
    public String getParentChannelLabel() {
        return parentChannelLabel;
    }
    /**
     * @param parentChannelLabel the parentChannelLabel to set
     */
    public void setParentChannelLabel(String parentChannelLabel) {
        this.parentChannelLabel = parentChannelLabel;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((channelLabel == null) ? 0 : channelLabel.hashCode());
        result = prime * result + ((product == null) ? 0 : product.hashCode());
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SUSEProductChannel other = (SUSEProductChannel) obj;
        if (channelLabel == null) {
            if (other.channelLabel != null) {
                return false;
            }
        }
        else if (!channelLabel.equals(other.channelLabel)) {
            return false;
        }
        if (product == null) {
            if (other.product != null) {
                return false;
            }
        }
        else if (!product.equals(other.product)) {
            return false;
        }
        return true;
    }
}
