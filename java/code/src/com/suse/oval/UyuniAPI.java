package com.suse.oval;

import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * This class offers mock implementations for the APIs in Uyuni. It does not provide real data or
 * perform any logic. When working on the integration with Uyuni, it is necessary to substitute calls to this class
 * with calls to the real APIs.
 */
public class UyuniAPI {

    public static Stream<CVEPatchStatus> listSystemsByPatchStatus(User user, String cveIdentifier) {

        return Stream.of(
                new CVEPatchStatus(1, Optional.of("libsoftokn3-hmac-32bit"),
                        Optional.of(PackageEvr.parseRpm("0:3.68.3-150400.1.7")), true),
                new CVEPatchStatus(1, Optional.of("libsha1detectcoll1"),
                        Optional.of(PackageEvr.parseRpm("0:1.0.3-2.18")), true),
                new CVEPatchStatus(1, Optional.of("libsha1detectcoll1"),
                        Optional.of(PackageEvr.parseRpm("0:1.0.0-2.18")), true),
                new CVEPatchStatus(1, Optional.of("libsha1detectcoll1"),
                        Optional.of(PackageEvr.parseRpm("0:1.0.10-2.18")), true)
        );
    }

    public static class CVEPatchStatus {

        private final long systemId;
        private final Optional<String> packageName;
        private final Optional<String> packageArch;
        private final Optional<PackageEvr> packageEvr;
        private final boolean packageInstalled;

        CVEPatchStatus(long systemIdIn, Optional<String> packageNameIn,
                       Optional<PackageEvr> evrIn, boolean packageInstalledIn, Optional<String> packageArch) {
            this.systemId = systemIdIn;
            this.packageName = packageNameIn;
            this.packageInstalled = packageInstalledIn;
            this.packageEvr = evrIn;
            this.packageArch = packageArch;
        }

        CVEPatchStatus(long systemIdIn, Optional<String> packageNameIn, Optional<PackageEvr> evrIn, boolean packageInstalledIn) {
            this(systemIdIn, packageNameIn, evrIn, packageInstalledIn, Optional.of("noarch"));
        }

        public long getSystemId() {
            return systemId;
        }

        public Optional<String> getPackageName() {
            return packageName;
        }

        public Optional<PackageEvr> getPackageEvr() {
            return packageEvr;
        }

        public boolean isPackageInstalled() {
            return packageInstalled;
        }

        public Optional<String> getPackageArch() {
            return packageArch;
        }
    }

    public static class PackageEvr implements Comparable<PackageEvr> {
        private static final RpmVersionComparator RPMVERCMP = new RpmVersionComparator();
        private static final DebVersionComparator DEBVERCMP = new DebVersionComparator();
        private Long id;
        private String epoch;
        private String version;
        private String release;
        private String type;


        public PackageEvr(String epochIn, String versionIn, String releaseIn, String typeIn) {
            id = null;
            epoch = epochIn;
            version = versionIn;
            release = releaseIn;
            type = typeIn;
        }


        public String getEpoch() {
            return epoch;
        }

        public void setEpoch(String e) {
            this.epoch = e;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long i) {
            this.id = i;
        }

        public String getRelease() {
            return release;
        }

        public String getType() {
            return type;
        }

        public void setType(String t) {
            this.type = t;
        }

        public void setRelease(String r) {
            this.release = r;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String v) {
            this.version = v;
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
                } else if (this.getPackageType() == PackageType.RPM) {
                    return rpmCompareTo(other);
                } else {
                    throw new RuntimeException("unhandled package type " + this.getPackageType());
                }
            } else {
                throw new RuntimeException("can not compare incompatible packageevr of type " + this.getPackageType() +
                        " with type " + other.getPackageType());
            }
        }

        private int epochAsInteger() {
            if (getEpoch() == null) {
                return 0;
            } else {
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
            builder.append(getEpoch()).append(':');
           /* if (StringUtils.isNumeric(getEpoch())) {
                builder.append(getEpoch()).append(':');
            }*/
            builder.append(getVersion()).append('-').append(getRelease());
            return builder.toString();
        }

        /**
         * Parses a Debian package version string to create a {@link PackageEvr} object.
         * <p>
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
         * <p>
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
            } else if (type.equals(PackageType.RPM.getDbString())) {
                return PackageType.RPM;
            } else {
                throw new RuntimeException("unsupported evr type: " + type);
            }
        }

        /**
         * Detect package type and call the correct parser for the version string.
         *
         * @param version the version string
         * @param type    package type
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

    public static class User {
        public static final User INSTANCE = new User();
    }

    public static enum PatchStatus {

        // Values sorted by seriousness
        AFFECTED_PATCH_INAPPLICABLE("Affected, patch available in unassigned channel", 0),
        AFFECTED_PATCH_APPLICABLE("Affected, patch available in assigned channel", 1),
        NOT_AFFECTED("Not affected", 2),
        PATCHED("Patched", 3),
        AFFECTED_PATCH_INAPPLICABLE_SUCCESSOR_PRODUCT("Affected, patch available in a Product Migration target", 4);

        /**
         * The lower the more severe
         */
        private int rank;
        private String description;

        PatchStatus(String descriptionIn, int rankIn) {
            this.description = descriptionIn;
            this.rank = rankIn;
        }

        public String getDescription() {
            return description;
        }

        public int getRank() {
            return rank;
        }
    }

    /**
     * Implement the rpmvercmp function provided by librpm
     * in Java. The comparator operates on two strings that
     * represent an RPM version or release.
     *
     * <p> This comparator is not perfectly antisymmetric for unequal versions,
     * but close enough to warrant being a comparator. For examples of asymmetry,
     * check the test.
     */
    private static class RpmVersionComparator implements Comparator<String> {

        /**
         * {@inheritDoc}
         */
        @Override
        public int compare(String o1, String o2) {
            if (o1 == null) {
                o1 = "";
            }
            if (o2 == null) {
                o2 = "";
            }
            // This method tries to mimick rpmvercmp.c as
            // closely as possible; it is deliberately doing things
            // in a more C-like manner
            if (o1 != null && o1.equals(o2)) {
                return 0;
            }

            String str1 = (String) o1;
            String str2 = (String) o2;
            int b1 = 0;
            int b2 = 0;

            /* loop through each version segment of str1 and str2 and compare them */
            while (true) {
                b1 = skipNonAlnum(str1, b1);
                b2 = skipNonAlnum(str2, b2);

                /* handle the tilde separator, it sorts before everything else */
                if (xchar(str1, b1) == '~' || xchar(str2, b2) == '~') {
                    if (xchar(str1, b1) == '\0' || xchar(str1, b1) != '~') {
                        return 1;
                    }
                    if (xchar(str2, b2) == '\0' || xchar(str2, b2) != '~') {
                        return -1;
                    }
                    b1++;
                    b2++;
                    continue;
                }
                /*
                 * Handle caret separator. Concept is the same as tilde,
                 * except that if one of the strings ends (base version),
                 * the other is considered as higher version.
                 */
                if (xchar(str1, b1) == '^' || xchar(str2, b2) == '^') {
                    if (xchar(str1, b1) == '\0') {
                        return -1;
                    }
                    if (xchar(str2, b2) == '\0') {
                        return 1;
                    }
                    if (xchar(str1, b1) != '^') {
                        return 1;
                    }
                    if (xchar(str2, b2) != '^') {
                        return -1;
                    }
                    b1++;
                    b2++;
                    continue;
                }
                if (b1 >= str1.length() || b2 >= str2.length()) {
                    break;
                }
                /* grab first completely alpha or completely numeric segment */
                /* str1.substring(b1, e1) and str2.substring(b2, e2) will */
                /* contain the segments */
                int e1, e2;
                boolean isnum;
                if (xisdigit(xchar(str1, b1))) {
                    e1 = skipDigits(str1, b1);
                    e2 = skipDigits(str2, b2);
                    isnum = true;
                } else {
                    e1 = skipAlpha(str1, b1);
                    e2 = skipAlpha(str2, b2);
                    isnum = false;
                }
                /* take care of the case where the two version segments are */
                /* different types: one numeric, the other alpha (i.e. empty) */
                if (b1 == e1) {
                    return -1;  /* arbitrary */
                }
                if (b2 == e2) {
                    return (isnum ? 1 : -1);
                }

                if (isnum) {
                    b1 = skipZeros(str1, b1, e1);
                    b2 = skipZeros(str2, b2, e2);

                    /* whichever number has more digits wins */
                    if (e1 - b1 > e2 - b2) {
                        return 1;
                    }
                    if (e2 - b2 > e1 - b1) {
                        return -1;
                    }
                }

                /* compareTo will return which one is greater - even if the two */
                /* segments are alpha or if they are numeric.  don't return  */
                /* if they are equal because there might be more segments to */
                /* compare */
                String seg1 = str1.substring(b1, e1);
                String seg2 = str2.substring(b2, e2);
                int rc = seg1.compareTo(seg2);
                if (rc != 0) {
                    return (rc < 0) ? -1 : 1;
                }
                //  Reinitilize
                b1 = e1;
                b2 = e2;
            }
            /* this catches the case where all numeric and alpha segments have */
            /* compared identically but the segment sepparating characters were */
            /* different */
            if (b1 == str1.length() && b2 == str2.length()) {
                return 0;
            }

            /* whichever version still has characters left over wins */
            if (b1 == str1.length()) {
                return -1;
            }
            return 1;
        }

        private int skipZeros(String s, int b, int e) {
            /* throw away any leading zeros - it's a number, right? */
            while (xchar(s, b) == '0' && b < e) {
                b++;
            }
            return b;
        }

        private int skipDigits(String s, int i) {
            while (i < s.length() && xisdigit(xchar(s, i))) {
                i++;
            }
            return i;
        }

        private int skipAlpha(String s, int i) {
            while (i < s.length() && xisalpha(xchar(s, i))) {
                i++;
            }
            return i;
        }

        private int skipNonAlnum(String s, int i) {
            while (i < s.length() && xchar(s, i) != '~' && xchar(s, i) != '^' && !xisalnum(xchar(s, i))) {
                i++;
            }
            return i;
        }

        private boolean xisalnum(char c) {
            return xisdigit(c) || xisalpha(c);
        }

        private boolean xisdigit(char c) {
            return Character.isDigit(c);
        }

        private boolean xisalpha(char c) {
            return Character.isLetter(c);
        }

        private char xchar(String s, int i) {
            return (i < s.length() ? s.charAt(i) : '\0');
        }
    }

    /**
     * DebVersionComparator
     */
    public static class DebVersionComparator implements Comparator<String> {

        /**
         * {@inheritDoc}
         * <p>
         * Compare two versions, *a* and *b*, and return an integer value which has
         * the same meaning as the built-in :func:`cmp` function's return value has,
         * see the following table for details.
         * <p>
         * .. table:: Return values
         * <p>
         * ===== =============================================
         * Value      Meaning
         * ===== =============================================
         * > 0   The version *a* is greater than version *b*.
         * = 0   Both versions are equal.
         * < 0   The version *a* is less than version *b*.
         * ===== =============================================
         * <p>
         * See: https://www.debian.org/doc/debian-policy/ch-controlfields.html#version
         * See: https://salsa.debian.org/dpkg-team/dpkg/blob/master/lib/dpkg/version.c#L140
         */
        @Override
        public int compare(String o1, String o2) {
            String version1 = o1, revision1 = null, version2 = o2, revision2 = null;
            /* Split version and revision: https://www.debian.org/doc/debian-policy/ch-controlfields.html#version */

            int hyphen = version1.lastIndexOf('-');
            if (hyphen > 0) {
                revision1 = version1.substring(hyphen + 1);
                version1 = version1.substring(0, hyphen);
            }
            hyphen = version2.lastIndexOf('-');
            if (hyphen > 0) {
                revision2 = version2.substring(hyphen + 1);
                version2 = version2.substring(0, hyphen);
            }
            int rc = verrevcmp(version1, version2);
            if (rc > 0) {
                return 1;
            } else if (rc < 0) {
                return -1;
            } else { /* (rc == 0) */
                int rv = verrevcmp(revision1, revision2);
                if (rv > 0) {
                    return 1;
                } else if (rv < 0) {
                    return -1;
                }
                return 0;
            }
        }

        private int order(int c) {
            if (Character.isDigit(c)) {
                return 0;
            } else if (Character.isLetter(c)) {
                return c;
            } else if (c == '~') {
                return -1;
            } else if (c != 0) {
                return c + 256;
            } else {
                return 0;
            }
        }

        private int verrevcmp(String a1, String b1) {
            char[] a, b;

            if (a1 == null) {
                a1 = "";
            }
            if (b1 == null) {
                b1 = "";
            }

            a = a1.toCharArray();
            b = b1.toCharArray();

            int i = 0;
            int j = 0;

            while (i < a.length || j < b.length) {
                int firstDiff = 0;

                while ((i < a.length && !Character.isDigit(a[i])) || (j < b.length && !Character.isDigit(b[j]))) {
                    int ac = i >= a.length ? 0 : order(a[i]);
                    int bc = j >= b.length ? 0 : order(b[j]);

                    if (ac != bc) {
                        return ac - bc;
                    }

                    i++;
                    j++;
                }
                while (i < a.length && a[i] == '0') {
                    i++;
                }
                while (j < b.length && b[j] == '0') {
                    j++;
                }
                while (i < a.length && j < b.length && Character.isDigit(a[i]) && Character.isDigit((b[j]))) {
                    if (firstDiff == 0) {
                        firstDiff = a[i] - b[j];
                    }
                    i++;
                    j++;
                }

                if (i < a.length && Character.isDigit(a[i])) {
                    return 1;
                }
                if (j < b.length && Character.isDigit(b[j])) {
                    return -1;
                }
                if (firstDiff != 0) {
                    return firstDiff;
                }
            }
            return 0;
        }
    }

    public static enum PackageType {
        RPM("rpm"),
        DEB("deb");

        private final String dbString;

        PackageType(String dbStringIn) {
            dbString = dbStringIn;
        }

        public String getDbString() {
            return dbString;
        }
    }


}
