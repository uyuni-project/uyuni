/*
 * Copyright (c) 2023 SUSE LLC
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

package com.suse.oval.vulnerablepkgextractor;

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

public class VulnerablePackage {
    private String name;
    private String fixVersion;

    public String getName() {
        return name;
    }

    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * Returns the fix version of the package if exist.
     *
     * @return the fix version or {@code Optional.empty} if package is unpatched.
     * */
    public Optional<String> getFixVersion() {
        if (StringUtils.isEmpty(fixVersion)) {
            return Optional.empty();
        }
        return Optional.ofNullable(fixVersion);
    }

    public void setFixVersion(String fixedVersionIn) {
        this.fixVersion = fixedVersionIn;
    }

    @Override
    public String toString() {
        return name + "-" + fixVersion;
    }
}
