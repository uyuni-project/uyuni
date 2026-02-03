package com.suse.manager.webui.utils.gson;

import com.redhat.rhn.domain.audit.ScapPolicy;

/**
 * Response DTO for SCAP Policy details
 * Used for GET endpoints to return enriched policy data with computed fields
 */
public class ScapPolicyResponseJson {
    
    // Core policy fields
    private Integer id;
    private String policyName;
    private String description;
    
    // SCAP content reference
    private Long scapContentId;
    private String xccdfProfileId;
    private String xccdfProfileTitle;  // Computed from SCAP content XML
    
    // Tailoring file reference
    private Long tailoringFileId;
    private String tailoringFileName;      // Display filename from TailoringFile entity
    private String tailoringProfileId;
    private String tailoringProfileTitle;  // Computed from tailoring file XML
    
    // Additional configuration
    private String ovalFiles;
    private String advancedArgs;
    private Boolean fetchRemoteResources;
    
    /**
     * Default constructor for GSON
     */
    public ScapPolicyResponseJson() {
    }
    
    /**
     * Constructor that populates from a ScapPolicy entity with computed titles
     * @param policy the ScapPolicy entity
     * @param xccdfTitle the computed XCCDF profile title
     * @param tailoringTitle the computed tailoring profile title
     */
    public ScapPolicyResponseJson(ScapPolicy policy, String xccdfTitle, String tailoringTitle) {
        // Core policy fields
        this.id = policy.getId();
        this.policyName = policy.getPolicyName();
        this.description = policy.getDescription();
        this.xccdfProfileId = policy.getXccdfProfileId();
        this.tailoringProfileId = policy.getTailoringProfileId();
        this.ovalFiles = policy.getOvalFiles();
        this.advancedArgs = policy.getAdvancedArgs();
        this.fetchRemoteResources = policy.getFetchRemoteResources();
        // SCAP Content & Profile Title
        if (policy.getScapContent() != null) {
            this.scapContentId = policy.getScapContent().getId();
            this.xccdfProfileTitle = xccdfTitle;
        }
        // Tailoring File & Profile Title
        if (policy.getTailoringFile() != null) {
            this.tailoringFileId = policy.getTailoringFile().getId();
            this.tailoringFileName = policy.getTailoringFile().getDisplayFileName();
            this.tailoringProfileTitle = tailoringTitle;
        }
    }
    
    // Getters only - this is a response DTO, no mutation needed
    
    public Integer getId() {
        return id;
    }
    
    public String getPolicyName() {
        return policyName;
    }
    
    public String getDescription() {
        return description;
    }
    
    public Long getScapContentId() {
        return scapContentId;
    }
    
    public String getXccdfProfileId() {
        return xccdfProfileId;
    }
    
    public String getXccdfProfileTitle() {
        return xccdfProfileTitle;
    }
    
    public Long getTailoringFileId() {
        return tailoringFileId;
    }
    
    public String getTailoringFileName() {
        return tailoringFileName;
    }
    
    public String getTailoringProfileId() {
        return tailoringProfileId;
    }
    
    public String getTailoringProfileTitle() {
        return tailoringProfileTitle;
    }
    
    public String getOvalFiles() {
        return ovalFiles;
    }
    
    public String getAdvancedArgs() {
        return advancedArgs;
    }
    
    public Boolean getFetchRemoteResources() {
        return fetchRemoteResources;
    }
}
