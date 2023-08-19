package com.suse.oval.config;

import com.google.gson.annotations.SerializedName;
import com.suse.oval.OsFamily;

import java.util.Map;
import java.util.Optional;

public class OVALConfig {
    @SerializedName("sources")
    private Map<OsFamily, OVALDistributionSourceInfo> sources;

    public OVALConfig() {
    }

    public Map<OsFamily, OVALDistributionSourceInfo> getSources() {
        return sources;
    }

    public void setSources(Map<OsFamily, OVALDistributionSourceInfo> sources) {
        this.sources = sources;
    }

    public Optional<OVALSourceInfo> lookupSourceInfo(OsFamily osFamily, String version) {
        OVALDistributionSourceInfo distributionSources = sources.get(osFamily);
        if (distributionSources != null) {
            return distributionSources.getVersionSourceInfo(version);
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "OVALConfig{" +
                "sources=" + sources +
                '}';
    }
}
