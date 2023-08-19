package com.suse.oval.config;

import com.google.gson.annotations.SerializedName;

public class OVALSourceInfo {
    @SerializedName("vulnerability")
    private String vulnerabilitiesInfoSource;
    @SerializedName("patch")
    private String patchInfoSource;

    public OVALSourceInfo() {
    }

    public String getVulnerabilitiesInfoSource() {
        return vulnerabilitiesInfoSource;
    }

    public void setVulnerabilitiesInfoSource(String vulnerabilitiesInfoSource) {
        this.vulnerabilitiesInfoSource = vulnerabilitiesInfoSource;
    }

    public String getPatchInfoSource() {
        return patchInfoSource;
    }

    public void setPatchInfoSource(String patchInfoSource) {
        this.patchInfoSource = patchInfoSource;
    }

    @Override
    public String toString() {
        return "OVALSourceInfo{" +
                "vulnerabilitiesInfoSource='" + vulnerabilitiesInfoSource + '\'' +
                ", patchInfoSource='" + patchInfoSource + '\'' +
                '}';
    }
}
