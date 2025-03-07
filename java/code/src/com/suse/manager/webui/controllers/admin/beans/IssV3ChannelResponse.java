package com.suse.manager.webui.controllers.admin.beans;

import java.util.List;

public class IssV3ChannelResponse {

    private final Long channelId;
    private final String channelName;
    private final String channelLabel;
    private final String channelArch;
    private final ChannelOrgResponse channelOrg;
    private final String parentChannelLabel;
    private final String originalChannelLabel;
    private final List<IssV3ChannelResponse> children;
    private final List<IssV3ChannelResponse> clones;

    /**
     * Response with the channel info to fill the channel sync section of the ui
     * @param channelIdIn the channel id
     * @param channelNameIn the channel name
     * @param channelLabelIn the channel label
     * @param channelArchIn the channel architecture
     * @param channelOrgIn the organization model (null if this is a vendor channel)
     * @param parentChannelLabelIn the parent channel (null if this is a root channel)
     * @param originalChannelLabelIn the original channel (null if this is not a clone)
     * @param childrenIn the channel children
     * @param clonesIn the channel clones
     */
    public IssV3ChannelResponse(
            Long channelIdIn,
            String channelNameIn,
            String channelLabelIn,
            String channelArchIn,
            ChannelOrgResponse channelOrgIn,
            String parentChannelLabelIn,
            String originalChannelLabelIn,
            List<IssV3ChannelResponse> childrenIn,
            List<IssV3ChannelResponse> clonesIn
    ) {
        channelId = channelIdIn;
        channelName = channelNameIn;
        channelLabel = channelLabelIn;
        channelArch = channelArchIn;
        channelOrg = channelOrgIn;
        parentChannelLabel = parentChannelLabelIn;
        originalChannelLabel = originalChannelLabelIn;
        children = childrenIn;
        clones = clonesIn;
    }

    public Long getChannelId() {
        return channelId;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getChannelLabel() {
        return channelLabel;
    }

    public String getChannelArch() {
        return channelArch;
    }

    public ChannelOrgResponse getChannelOrg() {
        return channelOrg;
    }

    public String getParentChannelLabel() {
        return parentChannelLabel;
    }

    public String getOriginalChannelLabel() {
        return originalChannelLabel;
    }

    public List<IssV3ChannelResponse> getChildren() {
        return children;
    }

    public List<IssV3ChannelResponse> getClones() {
        return clones;
    }

    public static class ChannelOrgResponse {
        private final Long orgId;
        private final String orgName;

        /**
         * Model for the channel organization
         * @param orgIdIn
         * @param orgNameIn
         */
        public ChannelOrgResponse(Long orgIdIn, String orgNameIn) {
            orgId = orgIdIn;
            orgName = orgNameIn;
        }

        public Long getOrgId() {
            return orgId;
        }

        public String getOrgName() {
            return orgName;
        }
    }
}
