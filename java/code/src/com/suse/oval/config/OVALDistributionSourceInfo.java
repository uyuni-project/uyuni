package com.suse.oval.config;

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class OVALDistributionSourceInfo {
    @SerializedName("content")
    public Map<String, OVALSourceInfo> content;

    public OVALDistributionSourceInfo() {

    }

    public Map<String, OVALSourceInfo> getContent() {
        return content == null ? Collections.emptyMap() : content;
    }

    public void setContent(Map<String, OVALSourceInfo> content) {
        this.content = content;
    }

    public Optional<OVALSourceInfo> getVersionSourceInfo(String version) {
        return Optional.ofNullable(getContent().get(version));
    }

    @Override
    public String toString() {
        return "OVALDistributionSourceInfo{" +
                "content=" + content +
                '}';
    }
}
