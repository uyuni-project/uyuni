/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.webui.utils.gson;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

/**
 * JSON DTO for scheduling SCAP audit scans
 */
public class AuditScanScheduleJson {

    /** Server IDs */
    private Set<Long> ids;

    /** The earliest execution date */
    private Optional<LocalDateTime> earliest = Optional.empty();

    /** The SCAP xccdf data stream name */
    @Deprecated
    private String dataStreamName;

    /** The SCAP content ID */
    private Long scapContentId;

    /** The XCCDF profile ID */
    private String xccdfProfileId;

    /** The Tailoring file name */
    @Deprecated
    private String tailoringFile;

    /** The Tailoring file ID */
    private Long tailoringFileId;

    /** The profile ID from the selected tailoring file */
    private String tailoringProfileID;

    /** OVAL files (comma-separated) */
    private String ovalFiles;

    /** Advanced arguments for oscap */
    private String advancedArgs;

    /** Whether to fetch remote resources */
    private Boolean fetchRemoteResources;

    /** Optional SCAP policy ID to link this scan to */
    private Integer policyId;

    /**
     * @return the server IDs
     */
    public Set<Long> getIds() {
        return ids;
    }

    /**
     * @return the date of earliest execution
     */
    public Optional<LocalDateTime> getEarliest() {
        return earliest;
    }

    /**
     * @return the raw data stream name
     */
    public String getDataStreamName() {
        return dataStreamName;
    }

    /**
     * @return the SCAP content ID
     */
    public Long getScapContentId() {
        return scapContentId;
    }

    /**
     * @return the XCCDF profile ID
     */
    public String getXccdfProfileId() {
        return xccdfProfileId;
    }

    /**
     * @return the OVAL files (comma-separated)
     */

    public String getOvalFiles() {
        return ovalFiles;
    }

    /**
     * @return the Tailoring file name
     */
    public String getTailoringFile() {
        return tailoringFile;
    }

    /**
     * @return the Tailoring profile ID
     */
    public String getTailoringProfileID() {
        return tailoringProfileID;
    }

    /**
     * @return the advanced arguments
     */
    public String getAdvancedArgs() {
        return advancedArgs;
    }

    /**
     * Builds the oscap parameters string
     * @param resolvedTailoringFile resolved tailoring file name
     * @return the formatted parameters for oscap command
     */
    public String buildOscapParameters(String resolvedTailoringFile) {
        StringBuilder params = new StringBuilder();
        
        // Profile is required
        params.append("--profile ").append(xccdfProfileId);
        
        // Tailoring file and profile (optional)
        if (StringUtils.isNotEmpty(resolvedTailoringFile)) {
            params.append(" --tailoring-file ").append(resolvedTailoringFile);
            if (StringUtils.isNotEmpty(tailoringProfileID)) {
                params.append(" --tailoring-profile-id ").append(tailoringProfileID);
            }
        } else if (StringUtils.isNotEmpty(tailoringFile)) {
             // Fallback to legacy field if no resolved file passed but field exists
            params.append(" --tailoring-file ").append(tailoringFile);
            if (StringUtils.isNotEmpty(tailoringProfileID)) {
                params.append(" --tailoring-profile-id ").append(tailoringProfileID);
            }
        }
        
        // Advanced arguments (optional)
        if (StringUtils.isNotEmpty(advancedArgs)) {
            params.append(" ").append(advancedArgs);
        }
        
        // Fetch remote resources (optional)
        if (fetchRemoteResources != null && fetchRemoteResources) {
            params.append(" --fetch-remote-resources");
        }
        
        return params.toString();
    }
    
    /**
     * Builds the oscap parameters string using internal tailoring file field
     * @deprecated use buildOscapParameters(String resolvedTailoringFile) instead
     * @return the formatted parameters for oscap command
     */
    @Deprecated
    public String buildOscapParameters() {
        return buildOscapParameters(null);
    }

    /**
     * Validates the required fields
     * @return error message if validation fails, null otherwise
     */
    public String validate() {
        if (ids == null || ids.isEmpty()) {
            return "No systems specified";
        }
        if (StringUtils.isEmpty(dataStreamName) && scapContentId == null) {
            return "SCAP content is required";
        }
        if (StringUtils.isEmpty(xccdfProfileId)) {
            return "XCCDF Profile is required";
        }
        return null;
    }

    /**
     * @return the policy ID (optional)
     */
    public Integer getPolicyId() {
        return policyId;
    }

    /**
     * @return the tailoring file ID
     */
    public Long getTailoringFileId() {
        return tailoringFileId;
    }
}
