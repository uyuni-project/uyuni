/**
 * Copyright (c) 2012 SUSE LLC
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
import com.redhat.rhn.domain.rhnpackage.PackageArch;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

import java.io.Serializable;

/**
 * POJO for a suseProducts row.
 */
public class SUSEProduct extends BaseDomainHelper implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 7814344915621295270L;

    /** The id. */
    private long id;

    /** The name. */
    private String name;

    /** The version. */
    private String version;

    /** The release. */
    private String release;

    /** The arch. */
    private PackageArch arch;

    /** The friendly name. */
    private String friendlyName;

    /** The product id. */
    private long productId;

    /**
     * Gets the id.
     * @return the id
     */
    public long getId() {
       return id;
    }

    /**
     * Sets the id.
     * @param idIn the new id
     */
    public void setId(long idIn) {
       id = idIn;
    }

    /**
     * Gets the name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * @param nameIn the new name
     */
    public void setName(String nameIn) {
        name = nameIn;
    }

    /**
     * Gets the version.
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Sets the version.
     * @param versionIn the new version
     */
    public void setVersion(String versionIn) {
        version = versionIn;
    }

    /**
     * Gets the release.
     * @return the release
     */
    public String getRelease() {
        return release;
    }

    /**
     * Sets the release.
     * @param releaseIn the new release
     */
    public void setRelease(String releaseIn) {
        release = releaseIn;
    }

    /**
     * Gets the arch.
     * @return the arch
     */
    public PackageArch getArch() {
        return arch;
    }

    /**
     * Sets the arch.
     * @param archIn the new arch
     */
    public void setArch(PackageArch archIn) {
        arch = archIn;
    }

    /**
     * Gets the friendly name.
     * @return the friendly name
     */
    public String getFriendlyName() {
        return friendlyName;
    }

    /**
     * Sets the friendly name.
     * @param friendlyNameIn the new friendly name
     */
    public void setFriendlyName(String friendlyNameIn) {
        friendlyName = friendlyNameIn;
    }

    /**
     * Gets the product id.
     * @return the product id
     */
    public long getProductId() {
        return productId;
    }

    /**
     * Sets the product id.
     * @param productIdIn the new product id
     */
    public void setProductId(long productIdIn) {
        productId = productIdIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object otherObject) {
        if (!(otherObject instanceof SUSEProduct)) {
            return false;
        }
        SUSEProduct other = (SUSEProduct) otherObject;
        return new EqualsBuilder()
            .append(getName(), other.getName())
            .append(getVersion(), other.getVersion())
            .append(getRelease(), other.getRelease())
            .append(getArch(), other.getArch())
            .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getName())
            .append(getVersion())
            .append(getRelease())
            .append(getArch())
            .toHashCode();
    }
}
