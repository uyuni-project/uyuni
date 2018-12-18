/**
 * Copyright (c) 2018 SUSE LLC
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

package com.suse.manager.utils;

import com.redhat.rhn.domain.rhnpackage.Package;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;

/**
 * Utility methods for packages
 */
public class PackageUtils {

    private PackageUtils() { }

    /**
     * @param pkg the package to check
     * @return whether the package is an .rpm package or not
     */
    public static boolean isTypeRpm(Package pkg) {
        return "rpm".equals(getArchTypeLabel(pkg));
    }

    /**
     * @param pkg the package to check
     * @return whether the package is a .deb package or not
     */
    public static boolean isTypeDeb(Package pkg) {
        return "deb".equals(getArchTypeLabel(pkg));
    }

    /**
     * Parses a Debian package version string to create a {@link PackageEvr} object.
     *
     * Debian package versioning policy format: [epoch:]upstream_version[-debian_revision]
     * Additional ':' and '-' characters are allowed in 'upstream_version'
     * https://www.debian.org/doc/debian-policy/ch-controlfields.html#version
     *
     * @param version the package version string
     * @return the package EVR
     */
    public static PackageEvr parseDebianEvr(String version) {

        // repo-sync replaces empty releases with 'X'. We copy the same behavior.
        String release = "X";
        String epoch = null;

        int epochIndex = version.indexOf(':');
        if (epochIndex > 0) {
            // Strip away optional 'epoch'
            epoch = version.substring(0, epochIndex);
            version = version.substring(epochIndex + 1);
        }

        int releaseIndex = version.lastIndexOf('-');
        if (releaseIndex > 0) {
            // Strip away optional 'release'
            release = version.substring(releaseIndex + 1, version.length());
            version = version.substring(0, releaseIndex);
        }

        return new PackageEvr(epoch, version, release);
    }

    private static String getArchTypeLabel(Package pkg) {
        return pkg.getPackageArch().getArchType().getLabel();
    }
}
