/*
 * Copyright (c) 2021 SUSE LLC
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

public class MaintenanceWindowDataJson {

    /** the event title */
    private String title;

    /** the start of the event */
    private String start;

    /** the end of the event */
    private String end;

    /**
     * Gets the name of the event
     *
     * @return name of the event
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the event
     *
     * @param titleIn the event title
     */
    public void setTitle(String titleIn) {
        this.title = titleIn;
    }

    /**
     * Gets the start of the event
     *
     * @return start of the event
     */
    public String getStart() {
        return start;
    }

    /**
     * Sets the start of the event
     *
     * @param startIn the event start
     */
    public void setStart(String startIn) {
        this.start = startIn;
    }

    /**
     * Gets the end of the event
     *
     * @return end of the event
     */
    public String getEnd() {
        return end;
    }

    /**
     * Sets the end of the event
     *
     * @param endIn the event end
     */
    public void setEnd(String endIn) {
        this.end = endIn;
    }
}
