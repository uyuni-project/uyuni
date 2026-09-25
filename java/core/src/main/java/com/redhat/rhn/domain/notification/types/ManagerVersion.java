/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.notification.types;

import com.redhat.rhn.common.conf.ConfigDefaults;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents the manager version, which can be in one of two formats:
 * - Semantic versioning (X.Y.Z): where X is the major version, Y is the minor version, and Z is the patch version.
 * Build metadata is ignored.
 * - Date-based versioning (X.Y): where X represents the year and Y represents the month.
 * Both formats are compared left to right, with each component having hierarchical significance.
 * Only versions of the same format can be compared to each other.
 */
public class ManagerVersion implements Comparable<ManagerVersion>, Serializable {

    private final boolean isUyuni;
    private final int major;
    private final int minor;
    private final int patch;

    /**
     * Default constructor
     */
    public ManagerVersion() {
        this(ConfigDefaults.get().getProductVersion(), ConfigDefaults.get().isUyuni());
    }

    /**
     * Custom product version constructor
     *
     * @param versionStringIn the version string
     */
    public ManagerVersion(String versionStringIn) {
        this(versionStringIn, ConfigDefaults.get().isUyuni());
    }

    /**
     * Full constructor
     *
     * @param versionStringIn the version string
     * @param isUyuniIn       true if the version is in Uyuni format
     */
    public ManagerVersion(String versionStringIn, boolean isUyuniIn) {
        this.isUyuni = isUyuniIn;
        if (versionStringIn == null || versionStringIn.isEmpty()) {
            throw new IllegalArgumentException("Version string cannot be null");
        }

        String[] parts = versionStringIn.split("[. ]");
        if (isUyuni) {
            if (parts.length != 2) {
                throw new IllegalArgumentException(
                        String.format("Invalid %s version format", ConfigDefaults.get().getProductName())
                );
            }
            this.major = Integer.parseInt(parts[0]);
            this.minor = Integer.parseInt(parts[1]);
            this.patch = -1;  // neutral value for comparing this format
        }
        else {
            if (parts.length < 3) {
                throw new IllegalArgumentException(
                        String.format("Invalid %s version format", ConfigDefaults.get().getProductName())
                );
            }
            this.major = Integer.parseInt(parts[0]);
            this.minor = Integer.parseInt(parts[1]);
            this.patch = parsePatch(parts[2]);
        }
    }

    /**
     * Parses the patch version from the given string.
     *
     * @param patchPart the patch part of the version string
     * @return the parsed patch version
     */
    private int parsePatch(String patchPart) {
        String digitPrefix = patchPart.replaceFirst("\\D.*", "");
        if (digitPrefix.isEmpty()) {
            throw new IllegalArgumentException(
                    String.format("Invalid %s version format: patch is not numeric",
                            ConfigDefaults.get().getProductName())
            );
        }
        return Integer.parseInt(digitPrefix);
    }


    @Override
    public int compareTo(ManagerVersion o) {
        if (this.isUyuni != o.isUyuni) {
            throw new IllegalArgumentException("Cannot compare different version formats");
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
        ManagerVersion other = (ManagerVersion) oIn;
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
    public boolean isNewerThan(ManagerVersion o) {
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
