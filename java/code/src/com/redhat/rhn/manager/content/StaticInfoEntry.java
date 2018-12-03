/**
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

import com.redhat.rhn.domain.product.ReleaseStage;

import com.google.gson.annotations.SerializedName;

import java.util.Optional;

/**
 * JSON representation of an entry in the suse products tree file
 */
public class StaticInfoEntry {

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
}
