/*
 * Copyright (c) 2024 SUSE LLC
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

package com.suse.manager.webui.utils;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Scap policy POST request object
 */
public class ScapPolicyJson {


    /** The policy Name */
    private String policyName;

    /** The earliest execution date */
    private Optional<LocalDateTime> earliest = Optional.empty();

    /** The SCAP xccdf data stream name */
    private String  dataStreamName;

    /** The XCCDF profile ID */
    private String xccdfProfileId;

    /** The Tailoring file */
    private String tailoringFile;
    /** The profil ID from the selected tailoring file */
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

    /**
     * @return the date of earliest execution
     */
    public Optional<LocalDateTime> getEarliest() {
        return earliest;
    }
}
