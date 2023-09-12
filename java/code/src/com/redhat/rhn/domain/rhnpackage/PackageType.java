/*
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
package com.redhat.rhn.domain.rhnpackage;

import java.util.EnumSet;

public enum PackageType {
    RPM("rpm"),
    DEB("deb");

    private final String dbString;

    PackageType(String dbStringIn) {
        dbString = dbStringIn;
    }

    public String getDbString() {
        return dbString;
    }

    public static PackageType fromDbString(String dbString) {
        return EnumSet.allOf(PackageType.class).stream()
                .filter(packageType -> packageType.dbString.equals(dbString))
                .findAny().orElseThrow();
    }
}
