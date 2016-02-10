package com.suse.manager.webui.services.subscriptionmatching;

import com.google.gson.annotations.SerializedName;

/**
 * JSON representation of a pinned match for the matcher UI
 * todo javadocs
 */
public class PinnedMatch {

    private Long id;
    @SerializedName("subscription_name")
    private String subscriptionName;
    @SerializedName("system_name")
    private String systemName;
    private int match;

    public PinnedMatch(Long id, String subscriptionName, String systemName, int match) {
        this.id = id;
        this.subscriptionName = subscriptionName;
        this.systemName = systemName;
        this.match = match;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSubscriptionName() {
        return subscriptionName;
    }

    public void setSubscriptionName(String subscriptionName) {
        this.subscriptionName = subscriptionName;
    }

    public String getSystemName() {
        return systemName;
    }

    public void setSystemName(String systemName) {
        this.systemName = systemName;
    }

    public int getMatch() {
        return match;
    }

    public void setMatch(int match) {
        this.match = match;
    }
}
