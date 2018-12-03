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
package com.suse.scc.model;

import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * JSON representation of a channel
 */
public class SCCChannelJson {

    @SerializedName("root_product_id")
    private Long rootProductId;
    @SerializedName("product_id")
    private Long productId;
    @SerializedName("repository_id")
    private Long repositoryId;
    @SerializedName("channel_label")
    private String channelLabel;
    @SerializedName("parent_channel_label")
    private String parentChannelLabel;
    @SerializedName("update_tag")
    private String updateTag;

    // ??? not sure if we need this
    private String url;

    /**
     * @return the rootProductId
     */
    public Long getRootProductId() {
        return rootProductId;
    }

    /**
     * @param rootProductIdIn the rootProductId to set
     */
    public void setRootProductId(Long rootProductIdIn) {
        this.rootProductId = rootProductIdIn;
    }

    /**
     * @return the productId
     */
    public Long getProductId() {
        return productId;
    }

    /**
     * @param productIdIn the productId to set
     */
    public void setProductId(Long productIdIn) {
        this.productId = productIdIn;
    }

    /**
     * @return the repositoryId
     */
    public Long getRepositoryId() {
        return repositoryId;
    }

    /**
     * @param repositoryIdIn the repositoryId to set
     */
    public void setRepositoryId(Long repositoryIdIn) {
        this.repositoryId = repositoryIdIn;
    }

    /**
     * @return the channelLabel
     */
    public String getChannelLabel() {
        return channelLabel;
    }

    /**
     * @param channelLabelIn the channelLabel to set
     */
    public void setChannelLabel(String channelLabelIn) {
        this.channelLabel = channelLabelIn;
    }

    /**
     * @return the parentChannelLabel
     */
    public String getParentChannelLabel() {
        return parentChannelLabel;
    }

    /**
     * @param parentChannelLabelIn the parentChannelLabel to set
     */
    public void setParentChannelLabel(String parentChannelLabelIn) {
        this.parentChannelLabel = parentChannelLabelIn;
    }

    /**
     * @return the updateTag
     */
    public String getUpdateTag() {
        return updateTag;
    }

    /**
     * @param updateTagIn the updateTag to set
     */
    public void setUpdateTag(String updateTagIn) {
        this.updateTag = updateTagIn;
    }

    /**
     * @return the url
     */
    public String getUrl() {
        return url;
    }

    /**
     * @param urlIn the url to set
     */
    public void setUrl(String urlIn) {
        this.url = urlIn;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(productId)
                .append(rootProductId)
                .append(repositoryId)
                .toHashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SCCChannelJson)) {
            return false;
        }
        SCCChannelJson otherSCCChannelJson = (SCCChannelJson) obj;
        return new EqualsBuilder()
                .append(getProductId(), otherSCCChannelJson.getProductId())
                .append(getRootProductId(), otherSCCChannelJson.getRootProductId())
                .append(getRepositoryId(), otherSCCChannelJson.getRepositoryId())
                .isEquals();
    }

    @Override
    public String toString() {
        return "SCCChannelJson [rootProductId=" + rootProductId + ", productId=" + productId +
                ", repositoryId=" + repositoryId + ", channelLabel=" + channelLabel +
                ", parentChannelLabel=" + parentChannelLabel + "]";
    }
}
