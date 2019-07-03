/**
 * Copyright (c) 2015 SUSE LLC
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

package com.redhat.rhn.domain.server;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.rhnpackage.PackageArch;

/**
 * Installed Product
 */
public class InstalledProduct extends BaseDomainHelper {

    private Long id;
    private String name;
    private String version;
    private PackageArch arch;
    private String release;
    private boolean baseproduct;

    /**
     * Constructor
     */
    public InstalledProduct() {
        super();
    }

    /**
     * Instantiates a new installed product.
     *
     * @param nameIn the name in
     * @param versionIn the version in
     * @param archIn the arch in
     * @param releaseIn the release in
     * @param isBaseProductIn the is base product in
     */
    public InstalledProduct(String nameIn, String versionIn,
            PackageArch archIn, String releaseIn, Boolean isBaseProductIn) {
        name = nameIn;
        version = versionIn;
        arch = archIn;
        release = releaseIn;
        baseproduct = isBaseProductIn;
    }

    /**
     * Instantiates a new installed product from a {@link SUSEProduct}.
     *
     * @param product SUSE product
     */
    public InstalledProduct(SUSEProduct product) {
        this(product.getName(), product.getVersion(), product.getArch(), product.getRelease(), product.isBase());
    }

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param nameIn the name to set
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param versionIn the version to set
     */
    public void setVersion(String versionIn) {
        this.version = versionIn;
    }

    /**
     * @return the arch
     */
    public PackageArch getArch() {
        return arch;
    }

    /**
     * @param archIn the arch to set
     */
    public void setArch(PackageArch archIn) {
        this.arch = archIn;
    }

    /**
     * @return the release
     */
    public String getRelease() {
        return release;
    }

    /**
     * @param releaseIn the release to set
     */
    public void setRelease(String releaseIn) {
        this.release = releaseIn;
    }

    /**
     * @return the isBaseproduct
     */
    public boolean isBaseproduct() {
        return baseproduct;
    }

    /**
     * @return a SUSE Product if one could be found or null otherwise
     */
    public SUSEProduct getSUSEProduct() {
        PackageArch archType = getArch();
        return SUSEProductFactory.findSUSEProduct(getName(), getVersion(), getRelease(),
                archType == null ? null : archType.getLabel(), true);
    }

    /**
     * @param isBaseproductIn the isBaseproduct to set
     */
    public void setBaseproduct(boolean isBaseproductIn) {
        this.baseproduct = isBaseproductIn;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object other) {
        if (!(other instanceof InstalledProduct)) {
            return false;
        }
        InstalledProduct castOther = (InstalledProduct) other;
        return new EqualsBuilder().append(getName(), castOther.getVersion())
                .append(getVersion(), castOther.getVersion())
                .append(getArch(), castOther.getArch())
                .append(getRelease(), castOther.getRelease())
                .append(isBaseproduct(), castOther.isBaseproduct())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return new HashCodeBuilder().append(getName())
                .append(getVersion())
                .append(getArch())
                .append(getVersion())
                .append(isBaseproduct())
                .toHashCode();
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this).append("id", getId())
                .append("name", getName())
                .append("version", getVersion())
                .append("release", getRelease())
                .append("arch", getArch())
                .append("isBaseproduct", isBaseproduct())
                .toString();
    }
}
