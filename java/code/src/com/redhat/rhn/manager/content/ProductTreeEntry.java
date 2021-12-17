/*
 * Copyright (c) 2018 SUSE LLC
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

import com.redhat.rhn.domain.product.ProductType;
import com.redhat.rhn.domain.product.ReleaseStage;

import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * JSON representation of an entry in the suse products tree file
 */
public class ProductTreeEntry {

    @SerializedName("channel_label")
    private String channelLabel;

    @SerializedName("parent_channel_label")
    private Optional<String> parentChannelLabel = Optional.empty();

    @SerializedName("channel_name")
    private String channelName;

    @SerializedName("product_id")
    private long productId;

    @SerializedName("repository_id")
    private long repositoryId;

    @SerializedName("parent_product_id")
    private Optional<Long> parentProductId = Optional.empty();

    @SerializedName("root_product_id")
    private long rootProductId;

    @SerializedName("update_tag")
    private Optional<String> updateTag = Optional.empty();

    private boolean signed;

    private boolean mandatory;

    private boolean recommended;

    private String url;

    @SerializedName("release_stage")
    private ReleaseStage releaseStage;

    @SerializedName("product_type")
    private Optional<ProductType> productType = Optional.empty();

    private List<String> tags = Collections.emptyList();

    /**
     * Here only for gson
     */
    public ProductTreeEntry() {
    }

    /**
     *
     * @param channelLabelIn channel label
     * @param parentChannelLabelIn parent channel label
     * @param channelNameIn channel name
     * @param productIdIn product id
     * @param repositoryIdIn repository id
     * @param parentProductIdIn parent product id
     * @param rootProductIdIn root product id
     * @param updateTagIn update tag
     * @param signedIn signed flag
     * @param mandatoryIn mandatory flag
     * @param recommendedIn recommended flag
     * @param urlIn repo url
     * @param releaseStageIn release stage
     * @param productTypeIn product type
     * @param tagsIn tags
     */
    public ProductTreeEntry(String channelLabelIn, Optional<String> parentChannelLabelIn, String channelNameIn,
                            long productIdIn, long repositoryIdIn, Optional<Long> parentProductIdIn,
                            long rootProductIdIn, Optional<String> updateTagIn, boolean signedIn, boolean mandatoryIn,
                            boolean recommendedIn, String urlIn, ReleaseStage releaseStageIn,
                            Optional<ProductType> productTypeIn, List<String> tagsIn) {
        this.channelLabel = channelLabelIn;
        this.parentChannelLabel = parentChannelLabelIn;
        this.channelName = channelNameIn;
        this.productId = productIdIn;
        this.repositoryId = repositoryIdIn;
        this.parentProductId = parentProductIdIn;
        this.rootProductId = rootProductIdIn;
        this.updateTag = updateTagIn;
        this.signed = signedIn;
        this.mandatory = mandatoryIn;
        this.recommended = recommendedIn;
        this.url = urlIn;
        this.releaseStage = releaseStageIn;
        this.productType = productTypeIn;
        this.tags = tagsIn;
    }

    /**
     * @return the tags
     */
    public List<String> getTags() {
        return tags;
    }

    /**
     * @return the product type
     */
    public Optional<ProductType> getProductType() {
        return productType;
    }

    /**
     * @return the channel label
     */
    public String getChannelLabel() {
        return channelLabel;
    }

    /**
     * @return the parent channel label
     */
    public Optional<String> getParentChannelLabel() {
        return parentChannelLabel;
    }

    /**
     * @return the channel name
     */
    public String getChannelName() {
        return channelName;
    }

    /**
     * @return the product id
     */
    public long getProductId() {
        return productId;
    }

    /**
     * @return the repositoriy id
     */
    public long getRepositoryId() {
        return repositoryId;
    }

    /**
     * @return the parent product id
     */
    public Optional<Long> getParentProductId() {
        return parentProductId;
    }

    /**
     * @return the root product id
     */
    public long getRootProductId() {
        return rootProductId;
    }

    /**
     * @return the update tag
     */
    public Optional<String> getUpdateTag() {
        return updateTag;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @return the release stage
     */
    public ReleaseStage getReleaseStage() {
        return releaseStage;
    }

    /**
     * @return the mandatory flag
     */
    public boolean isMandatory() {
        return mandatory;
    }

    /**
     * @return the recommends flag
     */
    public boolean isRecommended() {
        return recommended;
    }

    /**
     * @return the signed flag
     */
    public boolean isSigned() {
        return signed;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this);
        builder.append("root", getRootProductId())
        .append("product", getProductId())
        .append("repo", getRepositoryId())
        .append("channel", getChannelLabel())
        .append("parent", getParentChannelLabel());
        return builder.toString();
    }
}
