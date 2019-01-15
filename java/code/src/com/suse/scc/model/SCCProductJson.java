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

import com.redhat.rhn.domain.product.ProductType;
import com.redhat.rhn.domain.product.ReleaseStage;

import com.google.gson.annotations.SerializedName;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collections;
import java.util.List;

/**
 * This is a SUSE product as parsed from JSON coming in from SCC.
 */
public class SCCProductJson {

    /**
     * Product Builder
     */
    public class SCCProductJsonBuilder {
        private long id;
        private String name;
        private String identifier;
        private String version;
        private String releaseType;
        private String arch;
        private String friendlyName;
        private String productClass;
        private ReleaseStage releaseStage;
        private String cpe;
        private boolean free;
        private String description;
        private String eulaUrl;
        private List<SCCProductJson> extensions;
        private List<SCCRepositoryJson> repositories;
        private List<Long> onlinePredecessorIds;
        private List<Long> offlinePredecessorIds;
        private ProductType productType;
        private boolean recommended;

        /**
         * Constructor
         * @param json json object
         */
        public SCCProductJsonBuilder(SCCProductJson json) {
            this.id = json.getId();
            this.name = json.getName();
            this.identifier = json.getIdentifier();
            this.version = json.getVersion();
            this.releaseType = json.getReleaseType();
            this.arch = json.getArch();
            this.friendlyName = json.getFriendlyName();
            this.productClass = json.getProductClass();
            this.cpe = json.getCpe();
            this.releaseStage = json.getReleaseStage();
            this.free = json.isFree();
            this.description = json.getDescription();
            this.eulaUrl = json.getEulaUrl();
            this.extensions = json.getExtensions();
            this.repositories = json.getRepositories();
            this.onlinePredecessorIds = json.getOnlinePredecessorIds();
            this.offlinePredecessorIds = json.getOfflinePredecessorIds();
            this.productType = json.getProductType();
            this.recommended = json.isRecommended();
        }

        /**
         * Build a new Product
         * @return SCCProductJson
         */
        public SCCProductJson build() {
            return new SCCProductJson(
                    id, name, identifier, version, releaseType, arch, friendlyName, productClass, releaseStage,
                    cpe, free, description, eulaUrl, Collections.unmodifiableList(extensions),
                    Collections.unmodifiableList(repositories), Collections.unmodifiableList(onlinePredecessorIds),
                    Collections.unmodifiableList(offlinePredecessorIds), productType, recommended);
        }

        /**
         * @return the id
         */
        public long getId() {
            return id;
        }

        /**
         * @param idIn the id
         * @return the builder
         */
        public SCCProductJsonBuilder setId(long idIn) {
            this.id = idIn;
            return this;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param nameIn the name
         * @return the builder
         */
        public SCCProductJsonBuilder setName(String nameIn) {
            this.name = nameIn;
            return this;
        }

        /**
         * @return the identifier
         */
        public String getIdentifier() {
            return identifier;
        }

        /**
         * @param identifierIn the identifier
         * @return the builder
         */
        public SCCProductJsonBuilder setIdentifier(String identifierIn) {
            this.identifier = identifierIn;
            return this;
        }

        /**
         * @return the version
         */
        public String getVersion() {
            return version;
        }

        /**
         * @param versionIn the version
         * @return the builder
         */
        public SCCProductJsonBuilder setVersion(String versionIn) {
            this.version = versionIn;
            return this;
        }

        /**
         * @return the release type
         */
        public String getReleaseType() {
            return releaseType;
        }

        /**
         * @param releaseTypeIn the release type
         * @return the builder
         */
        public SCCProductJsonBuilder setReleaseType(String releaseTypeIn) {
            this.releaseType = releaseTypeIn;
            return this;
        }

        /**
         * @return the arch
         */
        public String getArch() {
            return arch;
        }

        /**
         * @param archIn the arch
         * @return the builder
         */
        public SCCProductJsonBuilder setArch(String archIn) {
            this.arch = archIn;
            return this;
        }

        /**
         * @return the friendly name
         */
        public String getFriendlyName() {
            return friendlyName;
        }

        /**
         * @param friendlyNameIn the friendly name
         * @return the builder
         */
        public SCCProductJsonBuilder setFriendlyName(String friendlyNameIn) {
            this.friendlyName = friendlyNameIn;
            return this;
        }

        /**
         * @return the product class
         */
        public String getProductClass() {
            return productClass;
        }

        /**
         * @param productClassIn the product class
         * @return the builder
         */
        public SCCProductJsonBuilder setProductClass(String productClassIn) {
            this.productClass = productClassIn;
            return this;
        }

        /**
         * @return the cpe
         */
        public String getCpe() {
            return cpe;
        }

        /**
         * @param cpeIn the cpe
         * @return the builder
         */
        public SCCProductJsonBuilder setCpe(String cpeIn) {
            this.cpe = cpeIn;
            return this;
        }

        /**
         * @return the free flag
         */
        public boolean isFree() {
            return free;
        }

        /**
         * @param freeIn the free flag
         * @return the builder
         */
        public SCCProductJsonBuilder setFree(boolean freeIn) {
            this.free = freeIn;
            return this;
        }

        /**
         * @return the description
         */
        public String getDescription() {
            return description;
        }

        /**
         * @param descriptionIn the description
         * @return the builder
         */
        public SCCProductJsonBuilder setDescription(String descriptionIn) {
            this.description = descriptionIn;
            return this;
        }

        /**
         * @return the eulaUrl
         */
        public String getEulaUrl() {
            return eulaUrl;
        }

        /**
         * @param eulaUrlIn the eulaUrl
         * @return the builder
         */
        public SCCProductJsonBuilder setEulaUrl(String eulaUrlIn) {
            this.eulaUrl = eulaUrlIn;
            return this;
        }

        /**
         * @return the extensions
         */
        public List<SCCProductJson> getExtensions() {
            return extensions;
        }

        /**
         * @param extensionsIn the extensions
         * @return the builder
         */
        public SCCProductJsonBuilder setExtensions(List<SCCProductJson> extensionsIn) {
            this.extensions = extensionsIn;
            return this;
        }

        /**
         * @return the repositories
         */
        public List<SCCRepositoryJson> getRepositories() {
            return repositories;
        }

        /**
         * @param repositoriesIn the repositories
         * @return the builder
         */
        public SCCProductJsonBuilder setRepositories(List<SCCRepositoryJson> repositoriesIn) {
            this.repositories = repositoriesIn;
            return this;
        }

        /**
         * @return the online predecessor ids
         */
        public List<Long> getOnlinePredecessorIds() {
            return onlinePredecessorIds;
        }

        /**
         * @param onlinePredecessorIdsIn the online predecessor ids
         * @return the builder
         */
        public SCCProductJsonBuilder setOnlinePredecessorIds(List<Long> onlinePredecessorIdsIn) {
            this.onlinePredecessorIds = onlinePredecessorIdsIn;
            return this;
        }

        /**
         * @return the offline predecessor ids
         */
        public List<Long> getOfflinePredecessorIds() {
            return offlinePredecessorIds;
        }

        /**
         * @param offlinePredecessorIdsIn the offline predecessor ids
         * @return the builder
         */
        public SCCProductJsonBuilder setOfflinePredecessorIds(List<Long> offlinePredecessorIdsIn) {
            this.offlinePredecessorIds = offlinePredecessorIdsIn;
            return this;
        }

        /**
         * @return the product type
         */
        public ProductType getProductType() {
            return productType;
        }

        /**
         * @param productTypeIn the product type
         * @return the builder
         */
        public SCCProductJsonBuilder setProductType(ProductType productTypeIn) {
            this.productType = productTypeIn;
            return this;
        }

        /**
         * @return the recommended flag
         */
        public boolean isRecommended() {
            return recommended;
        }

        /**
         * @param recommendedIn the recommended flag
         * @return the builder
         */
        public SCCProductJsonBuilder setRecommended(boolean recommendedIn) {
            this.recommended = recommendedIn;
            return this;
        }
    }

    private final long id;
    private final String name;
    private final String identifier;
    private final String version;
    @SerializedName("release_type")
    private final String releaseType;
    private final String arch;
    @SerializedName("friendly_name")
    private final String friendlyName;
    @SerializedName("product_class")
    private final String productClass;
    @SerializedName("release_stage")
    private final ReleaseStage releaseStage;
    private final String cpe;
    private final boolean free;
    private final String description;
    @SerializedName("eula_url")
    private final String eulaUrl;
    private final List<SCCProductJson> extensions;
    private final List<SCCRepositoryJson> repositories;
    @SerializedName("online_predecessor_ids")
    private final List<Long> onlinePredecessorIds;
    @SerializedName("offline_predecessor_ids")
    private final List<Long> offlinePredecessorIds;
    @SerializedName("product_type")
    private final ProductType productType;
    private final boolean recommended;

    /**
     * Constructor
     * @param idIn the scc id
     * @param nameIn the name
     * @param identifierIn the identifier
     * @param versionIn the version
     * @param releaseTypeIn the release type
     * @param archIn the architecture
     * @param friendlyNameIn the friendly name
     * @param productClassIn the product class
     * @param releaseStageIn the release stage
     * @param cpeIn the CPE number
     * @param freeIn a free product
     * @param descriptionIn the description
     * @param eulaUrlIn the EULA URL
     * @param extensionsIn list of extensions of this product
     * @param repositoriesIn list of repositories for this product
     * @param onlinePredecessorIdsIn list of online predecessor IDs
     * @param offlinePredecessorIdsIn list of offline predecessor IDs
     * @param productTypeIn the product type
     * @param recommendedIn is this product recommended
     */
    public SCCProductJson(
            long idIn, String nameIn, String identifierIn, String versionIn, String releaseTypeIn, String archIn,
            String friendlyNameIn, String productClassIn, ReleaseStage releaseStageIn, String cpeIn, boolean freeIn,
            String descriptionIn, String eulaUrlIn, List<SCCProductJson> extensionsIn,
            List<SCCRepositoryJson> repositoriesIn, List<Long> onlinePredecessorIdsIn,
            List<Long> offlinePredecessorIdsIn, ProductType productTypeIn, boolean recommendedIn) {
        this.id = idIn;
        this.name = nameIn;
        this.identifier = identifierIn;
        this.version = versionIn;
        this.releaseType = releaseTypeIn;
        this.arch = archIn;
        this.friendlyName = friendlyNameIn;
        this.productClass = productClassIn;
        this.releaseStage = releaseStageIn;
        this.cpe = cpeIn;
        this.free = freeIn;
        this.description = descriptionIn;
        this.eulaUrl = eulaUrlIn;
        this.extensions = extensionsIn;
        this.repositories = repositoriesIn;
        this.onlinePredecessorIds = onlinePredecessorIdsIn;
        this.offlinePredecessorIds = offlinePredecessorIdsIn;
        this.productType = productTypeIn;
        this.recommended = recommendedIn;
    }

    /**
     * @return the id
     */
    public long getId() {
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
    public List<SCCProductJson> getExtensions() {
        return extensions;
    }

    /**
     * @return the repositories
     */
    public List<SCCRepositoryJson> getRepositories() {
        return repositories;
    }

    /**
     * @return the online predecessor Ids
     */
    public List<Long> getOnlinePredecessorIds() {
        return onlinePredecessorIds;
    }

    /**
     * @return the offline predecessor Ids
     */
    public List<Long> getOfflinePredecessorIds() {
        return offlinePredecessorIds;
    }

    /**
     * @return the productType
     */
    public ProductType getProductType() {
        return productType;
    }

    /**
     * @return true if the product is a base product
     */
    public boolean isBaseProduct() {
        return productType == ProductType.base;
    }

    /**
     * @return the recommended
     */
    public boolean isRecommended() {
        return recommended;
    }

    /**
     * @return the release stage
     */
    public ReleaseStage getReleaseStage() {
        return releaseStage;
    }

    /**
     * @return a copy of this object
     */
    public SCCProductJsonBuilder copy() {
        return new SCCProductJsonBuilder(this);
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
        SCCProductJson other = (SCCProductJson) obj;
        return id == other.id && recommended == other.recommended;
    }

}
