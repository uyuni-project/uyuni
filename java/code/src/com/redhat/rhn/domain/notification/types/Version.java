/*
 * Copyright (c) 2024 SUSE LLC
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

package com.redhat.rhn.domain.notification.types;

import java.util.Objects;

/**
 * Represents a version of a SUMA or Uyuni instance.
 * Versioning in Uyuni and SUMA follows different formats:
 * SUMA versions are in format X.Y.Z - representing major, minor and patch
 * Uyuni versions are in the format X.Y - representing year and month.
 * In both formats, the version number is compared from left to right, with each component having a hierarchical
 * significance.
 * Only versions of the same type can be compared to each other.
 */
public class Version implements Comparable<Version> {
    private final boolean isUyuni;
    private final int major;
    private final int minor;
    private final int patch;

    /**
     * Base constructor
     *
     * @param versionStringIn the version string
     * @param isUyuniIn       true if the version is in Uyuni format
     */
    public Version(String versionStringIn, boolean isUyuniIn) {
        this.isUyuni = isUyuniIn;
        if (versionStringIn == null || versionStringIn.isEmpty()) {
            throw new IllegalArgumentException("Version string cannot be null");
        }

        String[] parts = versionStringIn.split("\\.");
        if (isUyuni) {
            // Uyuni format XXXX.YY
            if (parts.length != 2) {
                throw new IllegalArgumentException("Invalid Uyuni version format");
            }
            this.major = Integer.parseInt(parts[0]);
            this.minor = Integer.parseInt(parts[1]);
            this.patch = -1;  // NA for Uyuni
        }
        else {
            // SUMA format X.Y.Z
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid SUMA version format");
            }
            this.major = Integer.parseInt(parts[0]);
            this.minor = Integer.parseInt(parts[1]);
            this.patch = Integer.parseInt(parts[2]);
        }
    }


    @Override
    public int compareTo(Version o) {
        if (this.isUyuni != o.isUyuni) {
            throw new IllegalArgumentException("Cannot compare Uyuni and SUMA versions");
        }

        int majorCompare = Integer.compare(this.major, o.major);
        if (majorCompare != 0) {
            return majorCompare;
        }

        int minorCompare = Integer.compare(this.minor, o.minor);
        if (minorCompare != 0 || this.isUyuni) {
            return minorCompare;
        }

        return Integer.compare(this.patch, o.patch);
    }

    @Override
    public boolean equals(Object oIn) {
        if (this == oIn) {
            return true;
        }
        if (oIn == null || getClass() != oIn.getClass()) {
            return false;
        }
        Version other = (Version) oIn;
        if (this.isUyuni != other.isUyuni) {
            return false;
        }
        return this.major == other.major && this.minor == other.minor && this.patch == other.patch;
    }

    @Override
    public int hashCode() {
        return Objects.hash(isUyuni, major, minor, patch);
    }

    /**
     * Returns true if this version is newer than the given version.
     *
     * @param o the version to compare to
     * @return true if this version is newer
     */
    public boolean isNewerThan(Version o) {
        return this.compareTo(o) > 0;
    }

    @Override
    public String toString() {
        if (isUyuni) {
            return String.format("%04d.%02d", major, minor);
        }
        else {
            return String.format("%d.%d.%d", major, minor, patch);
        }
    }

    public boolean isUyuni() {
        return isUyuni;
    }

    public int getMajor() {
        return major;
    }

    public int getMinor() {
        return minor;
    }

    public int getPatch() {
        return patch;
    }

}
