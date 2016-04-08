/**
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.manager.webui.utils.salt.custom;

import com.google.gson.annotations.SerializedName;

/**
 * Object representation of SUSE Manager metadata to be added to Salt jobs.
 */
public class ScheduleMetadata {

    @SerializedName("suma-action-id")
    private long sumaActionId = 0L;

    /**
     * @param sumaActionIdIn the action id
     */
    public ScheduleMetadata(long sumaActionIdIn) {
        sumaActionId = sumaActionIdIn;
    }

    /**
     * @return the action id
     */
    public long getSumaActionId() {
        return sumaActionId;
    }
}
