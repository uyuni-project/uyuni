/**
 * Copyright (c) 2019 SUSE LLC
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
package com.suse.manager.webui.controllers.contentmanagement.response;

/**
 * JSON response wrapper for the sources of a content project.
 */
// After adding more project source types it might be handy to break this bean into 2
public class ProjectSourceResponse {

    private String id;
    private String sourceLabel;
    private String state;
    private String sourceType;

    public void setState(String stateIn) {
        this.state = stateIn;
    }

    public void setSourceType(String sourceTypeIn) {
        this.sourceType = sourceTypeIn;
    }

    public void setSourceLabel(String sourceLabelIn) {
        this.sourceLabel = sourceLabelIn;
    }

    public void setId(String idIn) {
        this.id = idIn;
    }
}

