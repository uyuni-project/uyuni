/**
 * Copyright (c) 2012 Novell
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

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.rhnpackage.PackageArch;

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
    private String channelFamilyId;
    private char productList;
    private int productId;

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
     * @return the channelFamilyId
     */
    public String getChannelFamilyId() {
        return channelFamilyId;
    }

    /**
     * @param channelFamilyIdIn the channelFamilyId to set
     */
    public void setChannelFamilyId(String channelFamilyIdIn) {
        this.channelFamilyId = channelFamilyIdIn;
    }

    /**
     * @return the productList
     */
    public char getProductList() {
        return productList;
    }

    /**
     * @param productListIn the productList to set
     */
    public void setProductList(char productListIn) {
        this.productList = productListIn;
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
}
