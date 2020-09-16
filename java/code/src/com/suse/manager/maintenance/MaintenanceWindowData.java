/**
 * Copyright (c) 2020 SUSE LLC
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

package com.suse.manager.maintenance;

import com.redhat.rhn.common.localization.LocalizationService;

import com.suse.manager.webui.utils.ViewHelper;

import java.time.Instant;

/**
 * POJO holding data about single maintenance window for rendering in the frontend.
 *
 * The dates stored in Strings is transformed according to the localization derived from the context.
 *
 */
public class MaintenanceWindowData {

    // maintenance window start date/time (human readable form)
    private String from;

    // maintenance window end date/time (human readable form)
    private String to;

    // maintenance window start date/time (milliseconds since epoch, used in struts pages)
    private long fromMilliseconds;

    // maintenance window start date/time (local date format, used in react pages)
    private String fromLocalDate;

    /**
     * Standard constructor
     *
     * @param fromIn the maintenance window start
     * @param toIn the maintenance window end
     */
    public MaintenanceWindowData(Instant fromIn, Instant toIn) {
        this.from = LocalizationService.getInstance().formatDate(fromIn);
        this.to = LocalizationService.getInstance().formatDate(toIn);
        this.fromMilliseconds = fromIn.toEpochMilli();
        this.fromLocalDate = ViewHelper.getInstance().renderDate(fromIn);
    }

    /**
     * Gets the from.
     *
     * @return from
     */
    public String getFrom() {
        return from;
    }

    /**
     * Gets the to date.
     *
     * @return to date
     */
    public String getTo() {
        return to;
    }

    /**
     * Gets the fromMilliseconds.
     *
     * @return fromMilliseconds
     */
    public long getFromMilliseconds() {
        return fromMilliseconds;
    }

    /**
     * Gets the fromLocalDate.
     *
     * @return fromLocalDate
     */
    public String getFromLocalDate() {
        return fromLocalDate;
    }
}
