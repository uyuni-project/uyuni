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
package com.redhat.rhn.manager.content;

import com.suse.mgrsync.MgrSyncChannel;
import com.suse.mgrsync.MgrSyncStatus;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A product, as listed by mgr-sync. This is conceptually different from
 * SCCProduct, which is a deserialized version of how SCC represents a product,
 * MgrSyncProduct, which is a deserialized version of how channels.xml encodes a
 * product, and SUSEProduct, which is a deserialization of what we have in the
 * database table suseProducts.
 */
public class ListedProduct implements Comparable<ListedProduct> {

    /** The friendly name. */
    private String friendlyName;

    /** The id. */
    private Integer id;

    /** The version. */
    private String version;

    /** The arch. */
    private String arch;

    /** The status. */
    private MgrSyncStatus status;

    /** The channels that make up this product. */
    private Set<MgrSyncChannel> channels;

    /** The extensions products of this product. */
    private SortedSet<ListedProduct> extensions;

    /**
     * Instantiates a new listed product.
     *
     * @param friendlyNameIn the friendly name
     * @param idIn the id
     * @param versionIn the version
     * @param archIn the arch
     */
    public ListedProduct(String friendlyNameIn, Integer idIn, String versionIn,
            String archIn) {
        friendlyName = friendlyNameIn;
        id = idIn;
        version = versionIn;
        arch = archIn;
        status = MgrSyncStatus.AVAILABLE;
        channels = new HashSet<MgrSyncChannel>();
        extensions = new TreeSet<ListedProduct>();
    }

    /**
     * Gets the friendly name.
     * @return the friendly name
     */
    public String getFriendlyName() {
        return friendlyName;
    }

    /**
     * Gets the id.
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * Gets the version.
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the arch.
     * @return the arch
     */
    public String getArch() {
        return arch;
    }

    /**
     * Gets the status.
     * @return the status
     */
    public MgrSyncStatus getStatus() {
        return status;
    }

    /**
     * Sets the status
     *
     * @param statusIn the new status
     */
    public void setStatus(MgrSyncStatus statusIn) {
       status = statusIn;
    }

    /**
     * Gets the channels that make up this product.
     *
     * @return the channels
     */
    public Set<MgrSyncChannel> getChannels() {
        return channels;
    }

    /**
     * Adds a channel.
     *
     * @param channel the channel
     */
    public void addChannel(MgrSyncChannel channel) {
        channels.add(channel);
    }

    /**
     * Gets the extensions.
     * @return the extensions
     */
    public SortedSet<ListedProduct> getExtensions() {
        return extensions;
    }

    /**
     * Adds an extension to this product.
     *
     * @param extension the extension product
     */
    public void addExtension(ListedProduct extension) {
        extensions.add(extension);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(ListedProduct other) {
        return new CompareToBuilder()
        .append(getNormalizedName(), other.getNormalizedName())
        .append(getVersion(), other.getVersion())
        .append(getArch(), other.getArch())
        .append(getId(), other.getId())
        .toComparison();
    }

    /**
     * Gets the normalized name for sorting.
     *
     * @return the normalized name
     */
    private String getNormalizedName() {
        return getFriendlyName().toLowerCase().replace(' ', '-');
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ListedProduct)) {
            return false;
        }
        ListedProduct otherListedProduct = (ListedProduct) other;
        return new EqualsBuilder()
                .append(getNormalizedName(), otherListedProduct.getNormalizedName())
                .append(getVersion(), otherListedProduct.getVersion())
                .append(getArch(), otherListedProduct.getArch())
                .append(getId(), otherListedProduct.getId())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getId())
            .append(getArch())
            .hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("name", friendlyName)
            .append("id", id)
            .append("version", version)
            .append("arch", arch)
            .toString();
    }
}
