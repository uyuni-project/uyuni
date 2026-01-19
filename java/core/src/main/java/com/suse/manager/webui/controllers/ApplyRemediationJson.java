package com.suse.manager.webui.controllers;

/**
 * JSON DTO for Apply Remediation requests.
 */
public class ApplyRemediationJson {
    private Long serverId;
    private String ruleIdentifier;
    private String benchmarkId;
    private String scriptType;
    private String remediationContent;
    private boolean saveAsCustom;

    public Long getServerId() {
        return serverId;
    }

    public void setServerId(Long serverId) {
        this.serverId = serverId;
    }

    public String getRuleIdentifier() {
        return ruleIdentifier;
    }

    public void setRuleIdentifier(String ruleIdentifier) {
        this.ruleIdentifier = ruleIdentifier;
    }

    public String getBenchmarkId() {
        return benchmarkId;
    }

    public void setBenchmarkId(String benchmarkId) {
        this.benchmarkId = benchmarkId;
    }

    public String getScriptType() {
        return scriptType;
    }

    public void setScriptType(String scriptType) {
        this.scriptType = scriptType;
    }

    public String getRemediationContent() {
        return remediationContent;
    }

    public void setRemediationContent(String remediationContent) {
        this.remediationContent = remediationContent;
    }

    public boolean isSaveAsCustom() {
        return saveAsCustom;
    }

    public void setSaveAsCustom(boolean saveAsCustom) {
        this.saveAsCustom = saveAsCustom;
    }
}
