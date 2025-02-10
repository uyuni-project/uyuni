package com.suse.manager.webui.controllers.admin.beans;

public class HubResponse {
    private String fqdn;
    private boolean defaultHub;
    private int knownOrgs;
    private int unmappedOrgs;

    public HubResponse(String fqdnIn, boolean defaultHubIn, int knownOrgsIn, int unmappedOrgsIn) {
        fqdn = fqdnIn;
        defaultHub = defaultHubIn;
        knownOrgs = knownOrgsIn;
        unmappedOrgs = unmappedOrgsIn;
    }

    public HubResponse() {}

    public String getFqdn() {
        return fqdn;
    }

    public void setFqdn(String fqdnIn) {
        fqdn = fqdnIn;
    }

    public boolean isDefaultHub() {
        return defaultHub;
    }

    public void setDefaultHub(boolean defaultHubIn) {
        defaultHub = defaultHubIn;
    }

    public int getKnownOrgs() {
        return knownOrgs;
    }

    public void setKnownOrgs(int knownOrgsIn) {
        knownOrgs = knownOrgsIn;
    }

    public int getUnmappedOrgs() {
        return unmappedOrgs;
    }

    public void setUnmappedOrgs(int unmappedOrgsIn) {
        unmappedOrgs = unmappedOrgsIn;
    }
}
