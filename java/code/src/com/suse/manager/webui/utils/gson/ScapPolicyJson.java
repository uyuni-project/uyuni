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

    /** The earliest execution date (serialized as a String) */
    private String earliest; // Changed to String for manual transformation

    /** The SCAP xccdf data stream name */
    private String dataStreamName;

    /** The XCCDF profile ID */
    private String xccdfProfileId;

    /** The Tailoring file */
    private String tailoringFile;

    /** The profile ID from the selected tailoring file */
    private String tailoringProfileId;

    public String getDataStreamName() {
        return dataStreamName;
    }

    public void setDataStreamName(String dataStreamNameIn) {
        this.dataStreamName = dataStreamNameIn;
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

    @Override
    public String toString() {
        return "ScapPolicyJson{" +
                "policyName='" + policyName + '\'' +
                ", earliest='" + earliest + '\'' +
                ", dataStreamName='" + dataStreamName + '\'' +
                ", xccdfProfileId='" + xccdfProfileId + '\'' +
                ", tailoringFile='" + tailoringFile + '\'' +
                ", tailoringProfileId='" + tailoringProfileId + '\'' +
                '}';
    }
}