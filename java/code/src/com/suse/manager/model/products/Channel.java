/**
 * Copyright (c) 2013 SUSE
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

package com.suse.manager.model.products;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.simpleframework.xml.Attribute;

/**
 * A software Channel in a Product.
 */
public class Channel {

    /** Status attributed to channels that are installed. */
    public static final String STATUS_INSTALLED = "P";
    /** Status attributed to channels that are not installed. */
    public static final String STATUS_NOT_INSTALLED = ".";

    /** The label. */
    @Attribute
    private String label;

    /** The status. */
    @Attribute
    private String status;

    /**
     * Default constructor.
     */
    public Channel() {
        // required by Simple XML
    }

    /**
     * Instantiates a new channel.
     * @param labelIn the label in
     * @param statusIn the status in
     */
    public Channel(String labelIn, String statusIn) {
        label = labelIn;
        status = statusIn;
    }

    /**
     * Gets the label.
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets the status.
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * Check if this channel is installed according to mgr-ncc-sync (P).
     * @return true if this channel is installed, otherwise false
     */
    public boolean isInstalled() {
        return STATUS_INSTALLED.equals(status);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Channel)) {
            return false;
        }
        Channel otherChannel = (Channel) other;
        return new EqualsBuilder()
            .append(getLabel(), otherChannel.getLabel())
            .append(getStatus(), otherChannel.getStatus())
            .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getLabel())
            .append(getStatus())
            .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
        .append("label", getLabel())
        .append("status", getStatus())
        .toString();
    }

}
