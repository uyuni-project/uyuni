package com.suse.manager.webui.controllers.admin.beans;

public class IssV3ChannelResponse {

    private final Long channelId;
    private final String channelName;
    private final String channelLabel;
    private final String channelArch;
    private final ChannelOrgResponse channelOrg;
    /**
     * Response with the channel info to fill the channel sync section of the ui
     * @param channelIdIn the channel id
     * @param channelNameIn the channel name
     * @param channelLabelIn the channel label
     * @param channelArchIn the channel architecture
     * @param channelOrgIn the organization model, if null is a vendor channel
     */
    public IssV3ChannelResponse(
            Long channelIdIn,
            String channelNameIn,
            String channelLabelIn,
            String channelArchIn,
            ChannelOrgResponse channelOrgIn
    ) {
        channelId = channelIdIn;
        channelName = channelNameIn;
        channelLabel = channelLabelIn;
        channelArch = channelArchIn;
        channelOrg = channelOrgIn;
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
