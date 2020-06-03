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

package com.suse.manager.webui.controllers.clusters.response;

/**
 * Message view bean.
 */
public class MessageResponse {

    private String severity;
    private String text;

    /**
     * @param severityIn message severity
     * @param textIn message text
     */
    public MessageResponse(String severityIn, String textIn) {
        this.severity = severityIn;
        this.text = textIn;
    }

    /**
     * @return severity to get
     */
    public String getSeverity() {
        return severity;
    }

    /**
     * @param severityIn to set
     */
    public void setSeverity(String severityIn) {
        this.severity = severityIn;
    }

    /**
     * @return message to get
     */
    public String getText() {
        return text;
    }

    /**
     * @param textIn to set
     */
    public void setText(String textIn) {
        this.text = textIn;
    }
}
