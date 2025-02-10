package com.suse.manager.webui.controllers.admin.beans;

public class PeripheralResponse {
    private String fqdn;
    private boolean allowSync;
    private boolean allowAllOrgs;
    private String nOfOrgsExported;

    public PeripheralResponse(String fqdnIn, boolean allowSyncIn, boolean allowAllOrgsIn, String nOfOrgsExportedIn) {
        fqdn = fqdnIn;
        allowSync = allowSyncIn;
        allowAllOrgs = allowAllOrgsIn;
        nOfOrgsExported = nOfOrgsExportedIn;
    }

    public PeripheralResponse() { }

    public String getFqdn() {
        return fqdn;
    }

    public void setFqdn(String fqdnIn) {
        fqdn = fqdnIn;
    }

    public boolean isAllowSync() {
        return allowSync;
    }

    public void setAllowSync(boolean allowSyncIn) {
        allowSync = allowSyncIn;
    }

    public boolean isAllowAllOrgs() {
        return allowAllOrgs;
    }

    public void setAllowAllOrgs(boolean allowAllOrgsIn) {
        allowAllOrgs = allowAllOrgsIn;
    }

    public String getNOfOrgsExported() {
        return nOfOrgsExported;
    }

    public void setNOfOrgsExported(String nOfOrgsExportedIn) {
        nOfOrgsExported = nOfOrgsExportedIn;
    }
}
