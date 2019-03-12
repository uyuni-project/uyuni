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

import java.util.List;

/**
 * JSON response wrapper for the properties of a content project.
 */
public class ProjectPropertiesResponse {

    private String label;
    private String name;
    private String description;
    private List<ProjectHistoryEntryResponse> historyEntries;

    public void setLabel(String labelIn) {
        this.label = labelIn;
    }

    public void setName(String nameIn) {
        this.name = nameIn;
    }

    public void setDescription(String descriptionIn) {
        this.description = descriptionIn;
    }

    public void setHistoryEntries(List<ProjectHistoryEntryResponse> historyEntriesIn) {
        this.historyEntries = historyEntriesIn;
    }
}
