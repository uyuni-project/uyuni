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
 * JSON DTO for Custom Remediation requests.
 */
public class CustomRemediationJson {

    private String identifier;
    private String benchmarkId;
    private String scriptType;
    private String remediation;

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifierIn the identifier to set
     */
    public void setIdentifier(String identifierIn) {
        this.identifier = identifierIn;
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
     * @return the remediation
     */
    public String getRemediation() {
        return remediation;
    }

    /**
     * @param remediationIn the remediation to set
     */
    public void setRemediation(String remediationIn) {
        this.remediation = remediationIn;
    }
}

