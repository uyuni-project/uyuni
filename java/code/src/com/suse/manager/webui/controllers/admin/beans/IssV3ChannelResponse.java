package com.suse.manager.webui.controllers.admin.beans;

public class IssV3ChannelResponse {

    private final Long channelId;
    private final String channelName;
    private final String channelLabel;

    /**
     * Response with the channel info to fill the channel sync section of the ui
     * @param channelIdIn the channel id
     * @param channelNameIn the channel name
     * @param channelLabelIn the channel label
     */
    public IssV3ChannelResponse(Long channelIdIn, String channelNameIn, String channelLabelIn) {
        channelId = channelIdIn;
        channelName = channelNameIn;
        channelLabel = channelLabelIn;
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
}
