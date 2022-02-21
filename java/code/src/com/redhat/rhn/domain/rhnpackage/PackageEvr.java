/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

import com.redhat.rhn.common.util.DebVersionComparator;
import com.redhat.rhn.common.util.RpmVersionComparator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * PackageEvr
 */
public class PackageEvr implements Comparable<PackageEvr> {

    private static final RpmVersionComparator RPMVERCMP = new RpmVersionComparator();
    private static final DebVersionComparator DEBVERCMP = new DebVersionComparator();

    private Long id;
    private String epoch;
    private String version;
    private String release;
    private String type;

    /**
     * Null constructor, needed for hibernate
     */
    public PackageEvr() {
        id = null;
        epoch = null;
        version = null;
        release = null;
        type = null;
    }

    /**
     * Complete constructor. Use PackageEvrFactory to create PackageEvrs if you
     * want to persist them to the Database. ONLY USE for non-persisting evr
     * objects.
     * @param epochIn epoch
     * @param versionIn version
     * @param releaseIn release
     * @param typeIn type
     */
    public PackageEvr(String epochIn, String versionIn, String releaseIn, String typeIn) {
        id = null;
        epoch = epochIn;
        version = versionIn;
        release = releaseIn;
        type = typeIn;
    }

    /**
     * Complete constructor. Use PackageEvrFactory to create PackageEvrs if you
     * want to persist them to the Database. ONLY USE for non-persisting evr
     * objects.
     * @param epochIn epoch
     * @param versionIn version
     * @param releaseIn release
     * @param typeIn type
     */
    public PackageEvr(String epochIn, String versionIn, String releaseIn, PackageType typeIn) {
        id = null;
        epoch = epochIn;
        version = versionIn;
        release = releaseIn;
        type = typeIn.getDbString();
    }

    /**
     * Copy constructor that creates a new instance copying the provided object. Use PackageEvrFactory to create
     * PackageEvrs if you want to persist them to the Database. ONLY USE for non-persisting evr objects.
     * @param other the evr object to copy from
     */
    public PackageEvr(PackageEvr other) {
        this(other.getEpoch(), other.getVersion(), other.getRelease(), other.getType());
    }

    /**
     * @return Returns the epoch.
     */
    public String getEpoch() {
        return epoch;
    }

    /**
     * @param e The epoch to set.
     */
    public void setEpoch(String e) {
        this.epoch = e;
    }

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param i The id to set.
     */
    public void setId(Long i) {
        this.id = i;
    }

    /**
     * @return Returns the release.
     */
    public String getRelease() {
        return release;
    }

    /**
     * @return package type
     */
    public String getType() {
        return type;
    }

    /**
     * @param t package type
     */
    public void setType(String t) {
        this.type = t;
    }

    /**
     * @param r The release to set.
     */
    public void setRelease(String r) {
        this.release = r;
    }

    /**
     * @return Returns the version.
     */
    public String getVersion() {
        return version;
    }

    /**
     * @param v The version to set.
     */
    public void setVersion(String v) {
        this.version = v;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object obj) {
        if (!(obj instanceof PackageEvr)) {
            return false;
        }

        PackageEvr evr = (PackageEvr) obj;

        return new EqualsBuilder().append(this.getId(), evr.getId()).append(
                this.getEpoch(), evr.getEpoch())
                .append(this.getVersion(), evr.getVersion()).append(this.getRelease(),
                        evr.getRelease()).isEquals();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return new HashCodeBuilder().append(this.getId()).append(this.getEpoch()).append(
                this.getVersion()).append(this.getRelease()).toHashCode();
    }

    private int rpmCompareTo(PackageEvr other) {
        // This method mirrors the perl function RHN::Manifest::vercmp
        // There is another perl function, RHN::DB::Package::vercmp which
        // does almost the same, but has a subtle difference when it comes
        // to null epochs (the RHN::DB::Package version does not treat null
        // epochs the same as epoch == 0, but sorts them as Integer.MIN_VALUE)
        int result = Integer.compare(epochAsInteger(), other.epochAsInteger());
        if (result != 0) {
            return result;
        }
        if (getVersion() == null || other.getVersion() == null) {
            throw new IllegalStateException(
                    "To compare PackageEvr, both must have non-null versions");
        }
        result = RPMVERCMP.compare(getVersion(), other.getVersion());
        if (result != 0) {
            return result;
        }
        // The perl code doesn't check for null releases, so we won't either
        // In the long run, a check might be in order, though
        return RPMVERCMP.compare(getRelease(), other.getRelease());
    }

    private int debCompareTo(PackageEvr other) {
        int result = Integer.compare(epochAsInteger(), other.epochAsInteger());
        if (result != 0) {
            return result;
        }
        if (getVersion() == null || other.getVersion() == null) {
            throw new IllegalStateException(
                    "To compare PackageEvr, both must have non-null versions");
        }
        result = DEBVERCMP.compare(getVersion(), other.getVersion());
        if (result != 0) {
            return result;
        }
        // The perl code doesn't check for null releases, so we won't either
        // In the long run, a check might be in order, though
        return DEBVERCMP.compare(getRelease(), other.getRelease());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(PackageEvr other) {
        if (this.getPackageType() == other.getPackageType()) {
            if (this.getPackageType() == PackageType.DEB) {
                return debCompareTo(other);
            }
            else if (this.getPackageType() == PackageType.RPM) {
                return rpmCompareTo(other);
            }
            else {
                throw new RuntimeException("unhandled package type " + this.getPackageType());
            }
        }
        else {
            throw new RuntimeException("can not compare incompatible packageevr of type " + this.getPackageType() +
                    " with type " + other.getPackageType());
        }
    }

    private int epochAsInteger() {
        if (getEpoch() == null) {
            return 0;
        }
        else {
            return Integer.parseInt(getEpoch());
        }
    }

    /**
     * Return a string representation in the format "[epoch:]version-release".
     *
     * @return string representation of epoch, version and release
     */
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        if (StringUtils.isNumeric(getEpoch())) {
            builder.append(getEpoch()).append(':');
        }
        builder.append(getVersion()).append('-').append(getRelease());
        return builder.toString();
    }

    /**
     * Return an EVR string representation in the format "[epoch:]version-release",
     * stripping away any dummy release strings (e.g. "-X"). The universal string is
     * meant to be recognized in the whole linux ecosystem.
     *
     * @return string representation of epoch, version and release
     */
    public String toUniversalEvrString() {
        StringBuilder builder = new StringBuilder();

        if (StringUtils.isNumeric(getEpoch())) {
            builder.append(getEpoch()).append(':');
        }
        builder.append(getVersion());

        // Strip dummy release string
        if (!getRelease().equals("X")) {
            builder.append('-').append(getRelease());
        }

        return builder.toString();
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
    public static PackageEvr parseDebian(String version) {

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
            release = version.substring(releaseIndex + 1);
            version = version.substring(0, releaseIndex);
        }

        return new PackageEvr(epoch, version, release, "deb");
    }

    /**
     * Parses a RPM package version string to create a {@link PackageEvr} object.
     *
     * RPM package version policy format: [epoch:]version[-release]
     *
     * @param version the package version string
     * @return the package EVR
     */
    public static PackageEvr parseRpm(String version) {
        String release = "";
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
            release = version.substring(releaseIndex + 1);
            version = version.substring(0, releaseIndex);
        }

        return new PackageEvr(epoch, version, release, "rpm");
    }

    /**
     * @return package type
     */
    public PackageType getPackageType() {
        if (type.equals(PackageType.DEB.getDbString())) {
            return PackageType.DEB;
        }
        else if (type.equals(PackageType.RPM.getDbString())) {
            return PackageType.RPM;
        }
        else {
            throw new RuntimeException("unsupported evr type: " + type);
        }
    }

    /**
     * Detect package type and call the correct parser for the version string.
     *
     * @param version the version string
     * @param type package type
     * @return parsed PackageEvr object
     */
    public static PackageEvr parsePackageEvr(PackageType type, String version) {
        switch (type) {
            case RPM:
                return parseRpm(version);
            case DEB:
                return parseDebian(version);
            default:
                throw new RuntimeException("unreachable");
        }
    }

}
