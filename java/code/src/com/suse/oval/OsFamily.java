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

package com.suse.oval;

public enum OsFamily {
    openSUSE_LEAP("openSUSE Leap", "leap", "opensuse"),
    SUSE_LINUX_ENTERPRISE_SERVER("SUSE Linux Enterprise Server", "sles", "suse"),
    SUSE_LINUX_ENTERPRISE_DESKTOP("SUSE Linux Enterprise Desktop", "sled", "suse"),
    REDHAT_ENTERPRISE_LINUX("Red Hat Enterprise Linux", "enterprise_linux", "redhat"),
    UBUNTU("Ubuntu", "ubuntu", "canonical"),
    DEBIAN("Debian", "debian", "debian");

    private final String vendor;
    private final String fullname;
    // Should consist of all lower case characters
    private final String shortname;


    OsFamily(String fullname, String shortname, String vendor) {
        this.fullname = fullname;
        this.shortname = shortname;
        this.vendor = vendor;
    }
    OsFamily(String fullname, String vendor) {
        this(fullname, fullname.toLowerCase(), vendor);
    }


    public String fullname() {
        return fullname;
    }

    public String shortname() {
        return shortname;
    }

    public String vendor() {
        return vendor;
    }
}
