package com.suse.manager.webui.utils.gson;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Scap policy POST request object
 */
public class ScapPolicyJson {

    /** Formatter for LocalDateTime */
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    /** The policy id */
    private Integer id;

    /** The policy Name */
    private String policyName;

    /** The policy description */
    private String description;

    /** The earliest execution date (serialized as a String) */
    private String earliest; 

    /** The SCAP content ID */
    private Long scapContentId;

    /** The XCCDF profile ID */
    private String xccdfProfileId;

    /** The Tailoring file */
    private String tailoringFile;

    /** The profile ID from the selected tailoring file */
    private String tailoringProfileId;

    /** OVAL files (comma-separated) */
    private String ovalFiles;

    /** Advanced arguments for oscap command */
    private String advancedArgs;

    /** Whether to fetch remote resources during SCAP scan */
    private Boolean fetchRemoteResources;

    public Long getScapContentId() {
        return scapContentId;
    }

    public void setScapContentId(Long scapContentIdIn) {
        this.scapContentId = scapContentIdIn;
    }

    public String getXccdfProfileId() {
        return xccdfProfileId;
    }

    public void setXccdfProfileId(String xccdfProfileIdIn) {
        this.xccdfProfileId = xccdfProfileIdIn;
    }

    public String getTailoringFile() {
        return tailoringFile;
    }

    public void setTailoringFile(String tailoringFileIn) {
        this.tailoringFile = tailoringFileIn;
    }

    public String getTailoringProfileId() {
        return tailoringProfileId;
    }

    public void setTailoringProfileId(String tailoringProfileIdIn) {
        this.tailoringProfileId = tailoringProfileIdIn;
    }

    public String getPolicyName() {
        return policyName;
    }

    public void setPolicyName(String policyNameIn) {
        this.policyName = policyNameIn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String descriptionIn) {
        this.description = descriptionIn;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer idIn) {
        this.id = idIn;
    }

    /**
     * Gets the earliest execution date as a LocalDateTime.
     */
    public LocalDateTime getEarliest() {
        return earliest != null ? LocalDateTime.parse(earliest, FORMATTER) : null;
    }

    /**
     * Sets the earliest execution date as a LocalDateTime.
     */
    public void setEarliest(LocalDateTime earliest) {
        this.earliest = earliest != null ? earliest.format(FORMATTER) : null;
    }

    public String getOvalFiles() {
        return ovalFiles;
    }

    public void setOvalFiles(String ovalFilesIn) {
        this.ovalFiles = ovalFilesIn;
    }

    public String getAdvancedArgs() {
        return advancedArgs;
    }

    public void setAdvancedArgs(String advancedArgsIn) {
        this.advancedArgs = advancedArgsIn;
    }

    public Boolean getFetchRemoteResources() {
        return fetchRemoteResources;
    }

    public void setFetchRemoteResources(Boolean fetchRemoteResourcesIn) {
        this.fetchRemoteResources = fetchRemoteResourcesIn;
    }

    @Override
    public String toString() {
        return "ScapPolicyJson{" +
                "policyName='" + policyName + '\'' +
                ", earliest='" + earliest + '\'' +
                ", scapContentId=" + scapContentId +
                ", xccdfProfileId='" + xccdfProfileId + '\'' +
                ", tailoringFile='" + tailoringFile + '\'' +
                ", tailoringProfileId='" + tailoringProfileId + '\'' +
                '}';
    }
}