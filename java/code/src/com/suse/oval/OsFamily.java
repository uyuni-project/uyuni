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

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public enum OsFamily {
    LEAP("openSUSE Leap", "leap", "opensuse",
            oneOf("15.2", "15.3", "15.4", "15.5")),
    openSUSE_LEAP_MICRO("openSUSELeap Micro", "leap-micro", "opensuse",
            oneOf("5.2", "5.3")),
    SUSE_LINUX_ENTERPRISE_SERVER("SUSE Linux Enterprise Server", "sles", "suse",
            oneOf("11", "12", "15")),
    SUSE_LINUX_ENTERPRISE_DESKTOP("SUSE Linux Enterprise Desktop", "sled", "suse",
            oneOf("10", "11", "12", "15")),
    SUSE_LINUX_ENTERPRISE_MICRO("SUSE Linux Enterprise Micro", "sle-micro", "suse",
            oneOf("5.0", "5.1", "5.2", "5.3")),
    REDHAT_ENTERPRISE_LINUX("Red Hat Enterprise Linux", "enterprise_linux", "redhat",
            withPrefix("7.", "8.", "9.")),
    UBUNTU("Ubuntu", "ubuntu", "canonical", oneOf("18.04", "20.04", "22.04")),
    DEBIAN("Debian", "debian", "debian", oneOf("10", "11", "12"));

    private final String vendor;
    private final String fullname;
    // Should consist of all lower case characters
    private final String shortname;
    private final Pattern legalReleasePattern;

    OsFamily(String fullnameIn, String shortnameIn, String vendorIn, String legalReleaseRegex) {
        this.fullname = fullnameIn;
        this.shortname = shortnameIn;
        this.vendor = vendorIn;
        legalReleasePattern = Pattern.compile(legalReleaseRegex);
    }

    public String fullname() {
        return fullname;
    }

    public boolean isSupportedRelease(String release) {
        return legalReleasePattern.matcher(release).matches();
    }

    private static String oneOf(String ... legalReleases) {
        return "(" + String.join("|", escapePeriods(legalReleases)) + ")";
    }

    private static String withPrefix(String ... legalReleasePrefixes) {
        return "(" + Arrays.stream(escapePeriods(legalReleasePrefixes))
                .map(prefix -> prefix + ".*")
                .collect(Collectors.joining("|")) + ")";
    }

    private static String[] escapePeriods(String ... strings) {
        return Arrays.stream(strings).map(str -> str.replace(".", "\\.")).toArray(String[]::new);
    }
}
