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

import com.suse.mgrsync.MgrSyncStatus;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * A product, as listed by mgr-sync. This is conceptually different from
 * SCCProduct, which is a deserialized version of how SCC represents a product,
 * MgrSyncProduct, which is a deserialized version of how channels.xml encodes a
 * product, and SUSEProduct, which is a deserialization of what we have in the
 * database table suseProducts.
 */
public class ListedProduct implements Comparable<ListedProduct> {

    /** The name. */
    private String name;

    /** The id. */
    private Integer id;

    /** The version. */
    private String version;

    /** The status. */
    private MgrSyncStatus status;

    /** The arch. */
    private String arch;

    /**
     * Instantiates a new listed product.
     *
     * @param nameIn the name in
     * @param idIn the id in
     * @param versionIn the version in
     * @param statusIn the status in
     * @param archIn the arch in
     */
    public ListedProduct(String nameIn, Integer idIn, String versionIn,
            MgrSyncStatus statusIn, String archIn) {
        name = nameIn;
        id = idIn;
        version = versionIn;
        status = statusIn;
        arch = archIn;
    }

    /**
     * Gets the name.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the id.
     *
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * Gets the version.
     *
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Gets the status.
     *
     * @return the status
     */
    public MgrSyncStatus getStatus() {
        return status;
    }

    /**
     * Gets the arch.
     *
     * @return the arch
     */
    public String getArch() {
        return arch;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(ListedProduct other) {
        return new CompareToBuilder()
        .append(getName(), other.getName())
        .append(getVersion(), other.getVersion())
        .append(getArch(), other.getArch())
        .append(getId(), other.getId())
        .toComparison();
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
        return new EqualsBuilder().append(getId(), otherListedProduct.getId())
                .append(getArch(), otherListedProduct.getArch()).isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getId()).append(getArch()).hashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("name", name)
            .append("id", id)
            .append("version", version)
            .append("arch", arch)
            .toString();
    }
}
