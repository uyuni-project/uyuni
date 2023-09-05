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
    SUSE_LINUX_ENTERPRISE_MICRO("SUSE Linux Enterprise Micro", "sle-micro", "suse"),
    REDHAT_ENTERPRISE_LINUX("Red Hat Enterprise Linux", "enterprise_linux", "redhat"),
    UBUNTU("Ubuntu", "ubuntu", "canonical"),
    DEBIAN("Debian", "debian", "debian");

    private final String vendor;
    private final String fullname;
    // Should consist of all lower case characters
    private final String shortname;

    OsFamily(String fullnameIn, String shortnameIn, String vendorIn) {
        this.fullname = fullnameIn;
        this.shortname = shortnameIn;
        this.vendor = vendorIn;
    }
    OsFamily(String fullnameIn, String vendorIn) {
        this(fullnameIn, fullnameIn.toLowerCase(), vendorIn);
    }
}
