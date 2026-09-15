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
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.manager.model.hub;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public class OrgInfoJson {

    private final Long orgId;

    private final String orgName;

    private final List<String> orgChannelLabels;

    /**
     * Constructor
     *
     * @param orgIdIn   the org id
     * @param orgNameIn the org name
     */
    public OrgInfoJson(long orgIdIn, String orgNameIn) {
        this(orgIdIn, orgNameIn, List.of());
    }

    /**
     * Constructor
     *
     * @param orgIdIn   the org id
     * @param orgNameIn the org name
     * @param orgChannelLabelsIn the list of channel labels belonging to the org
     */
    public OrgInfoJson(long orgIdIn, String orgNameIn, List<String> orgChannelLabelsIn) {
        orgId = orgIdIn;
        orgName = orgNameIn;
        orgChannelLabels = orgChannelLabelsIn;
    }

    /**
     * @return return the org id
     */
    public long getOrgId() {
        return orgId;
    }

    /**
     * @return return the org name
     */
    public String getOrgName() {
        return orgName;
    }

    /**
     * @return return the list of channel labels belonging to the org
     */
    public List<String> getOrgChannelLabels() {
        return orgChannelLabels;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof OrgInfoJson that)) {
            return false;
        }
        return Objects.equals(orgId, that.orgId) &&
                Objects.equals(orgName, that.orgName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orgId, orgName);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OrgInfoJson.class.getSimpleName() + "[", "]")
                .add("orgId='" + getOrgId() + "'")
                .add("orgName='" + getOrgName() + "'")
                .add("orgChannelLabels='" + getOrgChannelLabels() + "'")
                .toString();
    }
}
