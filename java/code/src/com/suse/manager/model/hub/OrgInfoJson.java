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

package com.suse.manager.model.hub;

import java.util.Objects;
import java.util.StringJoiner;

public class OrgInfoJson {

    private final Long orgId;

    private final String orgName;

    /**
     * Constructor
     *
     * @param orgIdIn   the org id
     * @param orgNameIn the org name
     */
    public OrgInfoJson(long orgIdIn, String orgNameIn) {
        orgId = orgIdIn;
        orgName = orgNameIn;
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
                .toString();
    }
}
