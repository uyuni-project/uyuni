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

import java.io.Serializable;
import java.util.Set;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.rhnpackage.PackageArch;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Class representation of a SUSE product.
 */
public class SUSEProduct extends BaseDomainHelper implements Serializable {

    private static final long serialVersionUID = 7814344915621295270L;

    private long id;
    private String name;
    private String version;
    private String release;
    private PackageArch arch;
    private String friendlyName;
    private int productId;

    /** available extensions for this product */
    private Set<SUSEProduct> extensionFor;

    /** available extensions that this product is an extension for */
    private Set<SUSEProduct> extensionOf;

    /** available upgrades for this product; */
    private Set<SUSEProduct> upgrades;

    /** available products from which upgrade to this is possible */
    private Set<SUSEProduct> downgrades;

    /**
     * @return the id
     */
    public long getId() {
       return id;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(long idIn) {
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
     * @return the friendlyName
     */
    public String getFriendlyName() {
        return friendlyName;
    }

    /**
     * @param friendlyNameIn the friendlyName to set
     */
    public void setFriendlyName(String friendlyNameIn) {
        this.friendlyName = friendlyNameIn;
    }

    /**
     * @return the productId
     */
    public int getProductId() {
        return productId;
    }

    /**
     * @param productIdIn the productId to set
     */
    public void setProductId(int productIdIn) {
        this.productId = productIdIn;
    }

    /**
     * List base products for this product
     * @return list base products for this product
     */
    public Set<SUSEProduct> getExtensionOf() {
        return extensionOf;
    }

    /**
     * List extension products for this product
     * @return list extension products for this product
     */
    public Set<SUSEProduct> getExtensionFor() {
        return extensionFor;
    }

    /**
     * Set the list extension products for this product
     * @param extensionsIn list of extension products
     */
    public void setExtensionFor(Set<SUSEProduct> extensionsIn) {
        this.extensionFor = extensionsIn;
    }

    /**
     * Set the list base products for this product
     * @param basesIn list of base products
     */
    public void setExtensionOf(Set<SUSEProduct> basesIn) {
        this.extensionOf = basesIn;
    }

    /**
     * List available upgrade path for this product
     * @return list available upgrade path for this product
     */
    public Set<SUSEProduct> getUpgrades() {
        return upgrades;
    }

    /**
     * Set the list of available upgrade path for this product
     * @param upgradesIn the list of available upgrade path for this product
     */
    public void setUpgrades(Set<SUSEProduct> upgradesIn) {
        this.upgrades = upgradesIn;
    }

    /**
     * List products that can upgrade to this product
     * @return list products that can upgrade to this product
     */
    public Set<SUSEProduct> getDowngrades() {
        return downgrades;
    }

    /**
     * Sets the list of products that can upgrade to this product
     * @param downgradesIn list of products that can upgrade to this product
     */
    public void setDowngrades(Set<SUSEProduct> downgradesIn) {
        this.downgrades = downgradesIn;
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
