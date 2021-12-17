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

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Simple DTO for playbook identification
 */
public class AnsiblePlaybookIdJson {

    private Long pathId;
    private String playbookRelPathStr;

    /**
     * Gets the pathId.
     *
     * @return pathId
     */
    public Long getPathId() {
        return pathId;
    }

    /**
     * Gets the playbookRelPathStr.
     *
     * @return playbookRelPathStr
     */
    public String getPlaybookRelPathStr() {
        return playbookRelPathStr;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("pathId", pathId)
                .append("playbookRelPathStr", playbookRelPathStr)
                .toString();
    }
}
