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

import com.redhat.rhn.domain.server.Server;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This enum defines the operating system families for which we can retrieve OVAL data.
 * */
public enum OsFamily {
    LEAP("openSUSE Leap", "Leap", "opensuse",
            oneOf("15.2", "15.3", "15.4", "15.5")),
    LEAP_MICRO("openSUSELeap Micro", "openSUE Leap Micro", "opensuse",
            oneOf("5.2", "5.3")),
    SUSE_LINUX_ENTERPRISE_SERVER("SUSE Linux Enterprise Server", "SLES", "suse",
            oneOf("11", "12", "15")),
    SUSE_LINUX_ENTERPRISE_DESKTOP("SUSE Linux Enterprise Desktop", "SLED", "suse",
            oneOf("10", "11", "12", "15")),
    SUSE_LINUX_ENTERPRISE_MICRO("SUSE Linux Enterprise Micro", "SLE Micro", "suse",
            oneOf("5.0", "5.1", "5.2", "5.3")),
    REDHAT_ENTERPRISE_LINUX("Red Hat Enterprise Linux", "Red Hat Enterprise Linux", "redhat",
            withPrefix("7.", "8.", "9.")),
    DEBIAN("Debian", "Debian", "debian", oneOf("10", "11", "12"));

    private final String vendor;
    private final String fullname;
    /**
     * Should consist of the same values as {@link Server#getOs()}
     * */
    private final String os;
    private final Pattern legalReleasePattern;

    OsFamily(String fullnameIn, String osIn, String vendorIn, String legalReleaseRegex) {
        this.fullname = fullnameIn;
        this.os = osIn;
        this.vendor = vendorIn;
        legalReleasePattern = Pattern.compile(legalReleaseRegex);
    }

    /**
     * Returns the full name of the OS family.
     *
     * @return OS family fullname
     * */
    public String fullname() {
        return fullname;
    }

    /**
     * Checks if the given OS release version is valid for this OS family.
     *
     * @param release the release version to check
     * @return weather the given OS release version is valid for this OS family.
     * */
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

    /**
     * Creates an {@code OsFamily} object from the given os name.
     *
     * @param osName the os name to convert (it should consist of the same values as {@link Server#getOs()})
     * @return the os family that correspond to the given os name.
     * */
    public static Optional<OsFamily> fromOsName(String osName) {
        return Arrays.stream(values()).filter(osFamily -> osFamily.os.equalsIgnoreCase(osName)).findFirst();
    }
}
