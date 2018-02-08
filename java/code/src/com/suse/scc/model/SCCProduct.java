/**
 * Copyright (c) 2014--2015 SUSE LLC
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
package com.suse.scc.model;

import com.redhat.rhn.domain.scc.SCCRepository;

import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.List;

/**
 * This is a SUSE product as parsed from JSON coming in from SCC.
 */
public class SCCProduct {

    private int id;
    private String name;
    private String identifier;
    private String version;
    @SerializedName("release_type")
    private String releaseType;
    private String arch;
    @SerializedName("friendly_name")
    private String friendlyName;
    @SerializedName("enabled_repositories")
    private List<Integer> enabledRepositories;
    @SerializedName("product_class")
    private String productClass;
    private String cpe;
    private boolean free;
    private String description;
    @SerializedName("eula_url")
    private String eulaUrl;
    private List<SCCProduct> extensions;
    private List<SCCRepository> repositories;
    @SerializedName("predecessor_ids")
    private List<Integer> predecessorIds;
    @SerializedName("online_predecessor_ids")
    private List<Integer> onlinePredecessorIds;
    @SerializedName("offline_predecessor_ids")
    private List<Integer> offlinePredecessorIds;
    @SerializedName("product_type")
    private String productType;
    private boolean recommended;

    /**
     * Default constructor.
     */
    public SCCProduct() {
    }

    /**
     * Standard constructor.
     *
     * @param idIn the id
     * @param identifierIn the identifier
     * @param versionIn the version
     * @param releaseTypeIn the release type
     * @param archIn the arch
     * @param friendlyNameIn the friendly name
     * @param productClassIn the product class
     */
    public SCCProduct(int idIn, String identifierIn, String versionIn,
            String releaseTypeIn, String archIn, String friendlyNameIn,
            String productClassIn) {
        id = idIn;
        identifier = identifierIn;
        version = versionIn;
        releaseType = releaseTypeIn;
        arch = archIn;
        friendlyName = friendlyNameIn;
        productClass = productClassIn;
        productType = "extension";
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @return the version
     */
    public String getVersion() {
        return version;
    }

    /**
     * @return the release_type
     */
    public String getReleaseType() {
        return releaseType;
    }

    /**
     * @return the arch
     */
    public String getArch() {
        return arch;
    }

    /**
     * @return the friendly_name
     */
    public String getFriendlyName() {
        return friendlyName;
    }

    /**
     * @return the enabled_repositories
     */
    public List<Integer> getEnabledRepositories() {
        return enabledRepositories;
    }

    /**
     * @return the product_class
     */
    public String getProductClass() {
        return productClass;
    }

    /**
     * @return the cpe
     */
    public String getCpe() {
        return cpe;
    }

    /**
     * @return the free
     */
    public boolean isFree() {
        return free;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the eula_url
     */
    public String getEulaUrl() {
        return eulaUrl;
    }

    /**
     * @return the extensions
     */
    public List<SCCProduct> getExtensions() {
        return extensions;
    }

    /**
     * @return the repositories
     */
    public List<SCCRepository> getRepositories() {
        return repositories;
    }

    /**
     * @return the predecessor Ids
     */
    public List<Integer> getPredecessorIds() {
        return predecessorIds;
    }

    /**
     * @return the online predecessor Ids
     */
    public List<Integer> getOnlinePredecessorIds() {
        return onlinePredecessorIds;
    }

    /**
     * @return the offline predecessor Ids
     */
    public List<Integer> getOfflinePredecessorIds() {
        return offlinePredecessorIds;
    }

    /**
     * @return the productType
     */
    public String getProductType() {
        return productType;
    }

    /**
     * @return true if the product is a base product
     */
    public boolean isBaseProduct() {
        return "base".equals(productType);
    }

    /**
     * @return the recommended
     */
    public boolean isRecommended() {
        return recommended;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(id)
                .append(recommended)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
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
        SCCProduct other = (SCCProduct) obj;
        if (id != other.id || recommended != other.recommended) {
            return false;
        }
        return true;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(int idIn) {
        this.id = idIn;
    }

    /**
     * @param nameIn the name to set
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @param friendlyNameIn the friendly name to set
     */
    public void setFriendlyName(String friendlyNameIn) {
        this.friendlyName = friendlyNameIn;
    }

    /**
     * @param versionIn the version to set
     */
    public void setVersion(String versionIn) {
        this.version = versionIn;
    }

    /**
     * @param releaseTypeIn the release type to set
     */
    public void setReleaseType(String releaseTypeIn) {
        this.releaseType = releaseTypeIn;
    }

    /**
     * @param archIn the arch to set
     */
    public void setArch(String archIn) {
        this.arch = archIn;
    }

    /**
     * @param productClassIn the product class to set
     */
    public void setProductClass(String productClassIn) {
        this.productClass = productClassIn;
    }

    /**
     * @param identifierIn the identifier to set
     */
    public void setIdentifier(String identifierIn) {
        this.identifier = identifierIn;
    }

    /**
     * @param predecessorIdsIn the predecessor Ids to set
     */
    public void setPredecessorIds(List<Integer> predecessorIdsIn) {
        this.predecessorIds = predecessorIdsIn;
    }

    /**
     * @param onlinePredecessorIdsIn the online predecessor Ids to set
     */
    public void setOnlinePredecessorIds(List<Integer> onlinePredecessorIdsIn) {
        this.onlinePredecessorIds = onlinePredecessorIdsIn;
    }

    /**
     * @param productTypeIn the productType to set
     */
    public void setProductType(String productTypeIn) {
        this.productType = productTypeIn;
    }
}
