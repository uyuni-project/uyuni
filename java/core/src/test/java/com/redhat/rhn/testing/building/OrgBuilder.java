/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.testing.building;

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.role.Role;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

public class OrgBuilder {

    private static final int MAX_ORG_NAME_LENGTH = 128;

    private static final String TRUNCATION_MARKER = "[...]";

    private String name = null;

    private String suffix = null;

    private List<Role> roles = List.of();

    /**
     * Set the orgName for the Org
     * @param nameIn the org name
     * @return this builder
     */
    public OrgBuilder withName(String nameIn) {
        this.name = nameIn;
        return this;
    }

    /**
     * Set suffix to be appended to the organization name
     * @param suffixIn the suffix to use
     * @return this builder
     */
    public OrgBuilder withSuffix(String suffixIn) {
        this.suffix = suffixIn;
        return this;
    }

    public OrgBuilder withRoles(Role... rolesIn) {
        this.roles = Arrays.asList(rolesIn);
        return this;
    }

    /**
     * Builds and persists the Org
     *
     * @return the created Org
     */
    public Org build() {
        String randomPart = RandomStringUtils.insecure().nextAlphanumeric(13);
        StringBuilder fullNameBuilder = new StringBuilder(name);

        if (suffix != null) {
            int maxSuffixLength = MAX_ORG_NAME_LENGTH - name.length() - randomPart.length() - 2;
            fullNameBuilder.append("-").append(truncateWithMarker(suffix, maxSuffixLength));
        }

        fullNameBuilder.append("-").append(randomPart);
        String fullName = fullNameBuilder.toString();

        Org org = new Org();

        org.setName(fullName);
        roles.forEach(org::addRole);

        return OrgFactory.save(org);
    }

    private static String truncateWithMarker(String value, int maxLength) {
        if (maxLength <= 0) {
            return "";
        }

        if (value.length() <= maxLength) {
            return value;
        }

        return StringUtils.truncate(value, maxLength - TRUNCATION_MARKER.length()) + TRUNCATION_MARKER;
    }
}
