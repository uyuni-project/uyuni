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

public class ScapScanScheduleJson {
    /** Server id */
    private Set<Long> ids;

    /** The earliest execution date */
    private Optional<LocalDateTime> earliest = Optional.empty();

    /** The SCAP xccdf data stream name */
    private String  dataStreamName;

    /** The XCCDF profile ID */
    private String xccdfProfileId;

    /** The Tailoring file */
    private String tailoringFile;

    /** The profil ID from the selected tailoring file */
    private String tailoringProfileID;

    /**
     * @return the name of the selected scap data stream
     */
    public String getDataStreamName() {
        return dataStreamName.replaceAll("xccdf", "ds");
    }

    /**
     * @return the name of the selected profile
     */
    public String getXccdfProfileId() {
        return "--profile " + xccdfProfileId;
    }

    /**
     * @return the name of the selected tailoring file
     */
    public String getTailoringFile() {
        return "--tailoring-file " + tailoringFile;
    }

    /**
     * @return the name of the selected profile from the tailoring file
     */
    public String getTailoringProfileID() {
        // Only effective if tailoring file is selected
        if (!this.tailoringFile.isEmpty() && !tailoringProfileID.isEmpty()) {
            return "--profile " + tailoringProfileID;
        }
        return StringUtils.EMPTY;

    }

    /**
     * @return the server id
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
}
