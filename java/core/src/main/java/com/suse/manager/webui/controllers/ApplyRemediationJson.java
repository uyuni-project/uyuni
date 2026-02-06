/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

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

    /**
     * @return the serverId
     */
    public Long getServerId() {
        return serverId;
    }

    /**
     * @param serverIdIn the serverId to set
     */
    public void setServerId(Long serverIdIn) {
        this.serverId = serverIdIn;
    }

    /**
     * @return the ruleIdentifier
     */
    public String getRuleIdentifier() {
        return ruleIdentifier;
    }

    /**
     * @param ruleIdentifierIn the ruleIdentifier to set
     */
    public void setRuleIdentifier(String ruleIdentifierIn) {
        this.ruleIdentifier = ruleIdentifierIn;
    }

    /**
     * @return the benchmarkId
     */
    public String getBenchmarkId() {
        return benchmarkId;
    }

    /**
     * @param benchmarkIdIn the benchmarkId to set
     */
    public void setBenchmarkId(String benchmarkIdIn) {
        this.benchmarkId = benchmarkIdIn;
    }

    /**
     * @return the scriptType
     */
    public String getScriptType() {
        return scriptType;
    }

    /**
     * @param scriptTypeIn the scriptType to set
     */
    public void setScriptType(String scriptTypeIn) {
        this.scriptType = scriptTypeIn;
    }

    /**
     * @return the remediationContent
     */
    public String getRemediationContent() {
        return remediationContent;
    }

    /**
     * @param remediationContentIn the remediationContent to set
     */
    public void setRemediationContent(String remediationContentIn) {
        this.remediationContent = remediationContentIn;
    }

    /**
     * @return the saveAsCustom
     */
    public boolean isSaveAsCustom() {
        return saveAsCustom;
    }

    /**
     * @param saveAsCustomIn the saveAsCustom to set
     */
    public void setSaveAsCustom(boolean saveAsCustomIn) {
        this.saveAsCustom = saveAsCustomIn;
    }
}

