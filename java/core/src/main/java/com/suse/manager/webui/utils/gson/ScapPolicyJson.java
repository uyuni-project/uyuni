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

    /**
     * @return the scapContentId
     */
    public Long getScapContentId() {
        return scapContentId;
    }

    /**
     * @param scapContentIdIn the scapContentId to set
     */
    public void setScapContentId(Long scapContentIdIn) {
        this.scapContentId = scapContentIdIn;
    }

    /**
     * @return the xccdfProfileId
     */
    public String getXccdfProfileId() {
        return xccdfProfileId;
    }

    /**
     * @param xccdfProfileIdIn the xccdfProfileId to set
     */
    public void setXccdfProfileId(String xccdfProfileIdIn) {
        this.xccdfProfileId = xccdfProfileIdIn;
    }

    /**
     * @return the tailoringFile
     */
    public String getTailoringFile() {
        return tailoringFile;
    }

    /**
     * @param tailoringFileIn the tailoringFile to set
     */
    public void setTailoringFile(String tailoringFileIn) {
        this.tailoringFile = tailoringFileIn;
    }

    /**
     * @return the tailoringProfileId
     */
    public String getTailoringProfileId() {
        return tailoringProfileId;
    }

    /**
     * @param tailoringProfileIdIn the tailoringProfileId to set
     */
    public void setTailoringProfileId(String tailoringProfileIdIn) {
        this.tailoringProfileId = tailoringProfileIdIn;
    }

    /**
     * @return the policyName
     */
    public String getPolicyName() {
        return policyName;
    }

    /**
     * @param policyNameIn the policyName to set
     */
    public void setPolicyName(String policyNameIn) {
        this.policyName = policyNameIn;
    }

    /**
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * @param descriptionIn the description to set
     */
    public void setDescription(String descriptionIn) {
        this.description = descriptionIn;
    }

    /**
     * @return the id
     */
    public Integer getId() {
        return id;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(Integer idIn) {
        this.id = idIn;
    }

    /**
     * Gets the earliest execution date as a LocalDateTime.
     * @return the earliest LocalDateTime
     */
    public LocalDateTime getEarliest() {
        return earliest != null ? LocalDateTime.parse(earliest, FORMATTER) : null;
    }

    /**
     * Sets the earliest execution date as a LocalDateTime.
     * @param earliestIn the earliest LocalDateTime to set
     */
    public void setEarliest(LocalDateTime earliestIn) {
        this.earliest = earliestIn != null ? earliestIn.format(FORMATTER) : null;
    }

    /**
     * @return the ovalFiles
     */
    public String getOvalFiles() {
        return ovalFiles;
    }

    /**
     * @param ovalFilesIn the ovalFiles to set
     */
    public void setOvalFiles(String ovalFilesIn) {
        this.ovalFiles = ovalFilesIn;
    }

    /**
     * @return the advancedArgs
     */
    public String getAdvancedArgs() {
        return advancedArgs;
    }

    /**
     * @param advancedArgsIn the advancedArgs to set
     */
    public void setAdvancedArgs(String advancedArgsIn) {
        this.advancedArgs = advancedArgsIn;
    }

    /**
     * @return the fetchRemoteResources
     */
    public Boolean getFetchRemoteResources() {
        return fetchRemoteResources;
    }

    /**
     * @param fetchRemoteResourcesIn the fetchRemoteResources to set
     */
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
