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
package com.suse.manager.webui.utils.gson;

import com.redhat.rhn.domain.audit.ScapPolicy;

/**
 * Response DTO for SCAP Policy details
 * Used for GET endpoints to return enriched policy data with computed fields
 */
public class ScapPolicyResponseJson {

    private Integer id;
    private String policyName;
    private String description;

    private Long scapContentId;
    private String xccdfProfileId;
    private String xccdfProfileTitle;

    private Long tailoringFileId;
    private String tailoringFileName;
    private String tailoringProfileId;
    private String tailoringProfileTitle;

    private String ovalFiles;
    private String advancedArgs;
    private Boolean fetchRemoteResources;

    /**
     * Default constructor.
     */
    public ScapPolicyResponseJson() {
    }

    /**
     * Constructor that populates from a ScapPolicy entity with computed titles
     * @param policyIn the ScapPolicy entity
     * @param xccdfTitleIn the computed XCCDF profile title
     * @param tailoringTitleIn the computed tailoring profile title
     */
    public ScapPolicyResponseJson(ScapPolicy policyIn, String xccdfTitleIn, String tailoringTitleIn) {
        this.id = policyIn.getId();
        this.policyName = policyIn.getPolicyName();
        this.description = policyIn.getDescription();
        this.xccdfProfileId = policyIn.getXccdfProfileId();
        this.tailoringProfileId = policyIn.getTailoringProfileId();
        this.ovalFiles = policyIn.getOvalFiles();
        this.advancedArgs = policyIn.getAdvancedArgs();
        this.fetchRemoteResources = policyIn.getFetchRemoteResources();
        // SCAP Content & Profile Title
        if (policyIn.getScapContent() != null) {
            this.scapContentId = policyIn.getScapContent().getId();
            this.xccdfProfileTitle = xccdfTitleIn;
        }
        // Tailoring File & Profile Title
        if (policyIn.getTailoringFile() != null) {
            this.tailoringFileId = policyIn.getTailoringFile().getId();
            this.tailoringFileName = policyIn.getTailoringFile().getDisplayFileName();
            this.tailoringProfileTitle = tailoringTitleIn;
        }
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @return the policyName
     */
    public String getPolicyName() {
        return policyName;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the scapContentId
     */
    public Long getScapContentId() {
        return scapContentId;
    }

    /**
     * @return the xccdfProfileId
     */
    public String getXccdfProfileId() {
        return xccdfProfileId;
    }

    /**
     * @return the xccdfProfileTitle
     */
    public String getXccdfProfileTitle() {
        return xccdfProfileTitle;
    }

    /**
     * @return the tailoringFileId
     */
    public Long getTailoringFileId() {
        return tailoringFileId;
    }

    /**
     * @return the tailoringFileName
     */
    public String getTailoringFileName() {
        return tailoringFileName;
    }

    /**
     * @return the tailoringProfileId
     */
    public String getTailoringProfileId() {
        return tailoringProfileId;
    }

    /**
     * @return the tailoringProfileTitle
     */
    public String getTailoringProfileTitle() {
        return tailoringProfileTitle;
    }

    /**
     * @return the ovalFiles
     */
    public String getOvalFiles() {
        return ovalFiles;
    }

    /**
     * @return the advancedArgs
     */
    public String getAdvancedArgs() {
        return advancedArgs;
    }

    /**
     * @return the fetchRemoteResources
     */
    public Boolean getFetchRemoteResources() {
        return fetchRemoteResources;
    }
}
