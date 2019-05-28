/**
 * Copyright (c) 2016 SUSE LLC
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
package com.suse.manager.reactor.utils;

import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.server.Server;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities related to RHEL minions.
 */
public class RhelUtils {

    private RhelUtils() { }

    private static final Pattern RHEL_RELEASE_MATCHER =
            Pattern.compile("(.+)\\srelease\\s([\\d.]+)\\s\\((.+)\\).*", Pattern.DOTALL);

    /**
     * Information about RHEL based OSes.
     */
    public static class RhelProduct {

        private Optional<SUSEProduct> suseProduct;
        private String name;
        private String version;
        private String release;

        /**
         * Constructor.
         * @param suseProductIn the suse product that corresponds to this OS
         * @param nameIn the name of the OS
         * @param versionIn the version
         * @param releaseIn the release name
         * @param archIn the arch
         */
        public RhelProduct(Optional<SUSEProduct> suseProductIn, String nameIn,
                           String versionIn, String releaseIn, String archIn) {
            this.suseProduct = suseProductIn;
            this.name = nameIn;
            this.version = versionIn;
            this.release = releaseIn;
        }

        /**
         * @return the SUSE product, if any.
         */
        public Optional<SUSEProduct> getSuseProduct() {
            return suseProduct;
        }

        /**
         * @return the name of the OS (RedHatEnterpriseServer or Centos)
         */
        public String getName() {
            return name;
        }

        /**
         * @return the OS major version.
         */
        public String getVersion() {
            return version;
        }

        /**
         * @return the release name
         */
        public String getRelease() {
            return release;
        }

    }

    /**
     * The content of the /etc/redhat|centos-release file.
     */
    public static class ReleaseFile {

        private String name;
        private String majorVersion;
        private String minorVersion;
        private String release;

        /**
         * All arg constructor.
         * @param nameIn the name
         * @param majorVersionIn the major version
         * @param minorVersionIn the minor version
         * @param releaseIn the release name
         */
        public ReleaseFile(String nameIn, String majorVersionIn,
                           String minorVersionIn, String releaseIn) {
            this.name = nameIn;
            this.majorVersion = majorVersionIn;
            this.minorVersion = minorVersionIn;
            this.release = releaseIn;
        }

        /**
         * @return the name of the OS
         */
        public String getName() {
            return name;
        }

        /**
         * @return the OS major version
         */
        public String getMajorVersion() {
            return majorVersion;
        }

        /**
         * @return the minor version
         */
        public String getMinorVersion() {
            return minorVersion;
        }

        /**
         * @return the release name
         */
        public String getRelease() {
            return release;
        }
    }

    /**
     * Parse the /etc/redhat|centos-release
     * @param releaseFile the content of the release file
     * @return the parsed content of the release file
     */
    public static Optional<ReleaseFile> parseReleaseFile(String releaseFile) {
        Matcher matcher = RHEL_RELEASE_MATCHER.matcher(releaseFile);
        if (matcher.matches()) {
            String name =
                    matcher.group(1).replaceAll("(?i)linux", "").replaceAll(" ", "");
            String majorVersion = StringUtils.substringBefore(matcher.group(2), ".");
            String minorVersion = StringUtils.substringAfter(matcher.group(2), ".");
            String release = matcher.group(3);
            return Optional.of(new ReleaseFile(name, majorVersion, minorVersion, release));
        }
        return Optional.empty();
    }

    /**
     * Guess the SUSE product for the RedHat minion and parse the
     * /etc/redhat,centos-release file.
     * 1) if the RES channel or a clone of the RES channel is
     *    assigned to a system it is a RES system
     * 2) if a RES release package (sles_es-release) is installed it is a RES.
     * 3) otherwise it is not a RES system
     * 4) is it a centos system? check if /etc/centos-release file exists
     * 5) if it is not a centos we can say it is a original RHEL (maybe:-)
     *
     * @param server the minion
     * @param resReleasePackage the package that provides 'sles_es-release'
     * @param rhelReleaseFile the content of /etc/redhat-release
     * @param centosReleaseFile the content of /etc/centos-release
     * @return the {@link RhelProduct}
     */
    public static Optional<RhelProduct> detectRhelProduct(
            Server server, Optional<String> resReleasePackage,
            Optional<String> rhelReleaseFile, Optional<String> centosReleaseFile) {
        String arch = server.getServerArch().getLabel().replace("-redhat-linux", "");

        // check first if it has RES channels assigned or the RES release package installed
        boolean hasRESChannels = server.getChannels().stream()
                .filter(ch -> ch.getProductName() != null &&
                        "RES".equalsIgnoreCase(ch.getProductName().getName()))
                .count() > 0;
        boolean hasRESReleasePackage = resReleasePackage
                .filter(pkg -> StringUtils.startsWith(pkg, "sles_es-release")).isPresent();
        if (hasRESChannels || hasRESReleasePackage) {
            // we got a RES. find the corresponding SUSE product
            Optional<ReleaseFile> releaseFile = rhelReleaseFile
                    .flatMap(RhelUtils::parseReleaseFile);
            // Find the corresponding SUSEProduct in the database
            String name = releaseFile.map(ReleaseFile::getName).orElse("RES");
            String majorVersion = releaseFile.map(ReleaseFile::getMajorVersion)
                    .orElse("unknown");
            String release = releaseFile.map(ReleaseFile::getRelease).orElse("unknown");

            Optional<SUSEProduct> suseProduct = Optional.ofNullable(SUSEProductFactory
                    .findSUSEProduct("RES", majorVersion, release, arch, true));

            return Optional.of(new RhelProduct(suseProduct, name,
                    majorVersion, release, arch));
        }

        // next check if Centos
        if (centosReleaseFile.filter(StringUtils::isNotBlank).isPresent()) {
            return centosReleaseFile.map(v -> detectPlainRHEL(v, arch, "CentOS"));
        }

        // if neither RES nor Centos then we probably got a plain RHEL
        if (rhelReleaseFile.filter(StringUtils::isNotBlank).isPresent()) {
            return rhelReleaseFile.map(v -> detectPlainRHEL(v, arch, "RedHatEnterprise"));
        }

        return Optional.empty();
    }

    /**
     * Guess the SUSE product for the RedHat minion and parse the
     * /etc/redhat,centos-release file.
     * 1) if the RES channel or a clone of the RES channel is
     *    assigned to a system it is a RES system
     * 2) if a RES release package (sles_es-release) is installed it is a RES.
     * 3) otherwise it is not a RES system
     * 4) is it a centos system? check if /etc/centos-release file exists
     * 5) if it is not a centos we can say it is a original RHEL (maybe:-)
     *
     * @param image the image
     * @param resReleasePackage the package that provides 'sles_es-release'
     * @param rhelReleaseFile the content of /etc/redhat-release
     * @param centosReleaseFile the content of /etc/centos-release
     * @return the {@link RhelProduct}
     */
    public static Optional<RhelProduct> detectRhelProduct(
            ImageInfo image, Optional<String> resReleasePackage,
            Optional<String> rhelReleaseFile, Optional<String> centosReleaseFile) {
        String arch = image.getImageArch().getLabel().replace("-redhat-linux", "");

        // check first if it has RES channels assigned or the RES release package installed
        boolean hasRESChannels = image.getChannels().stream()
                .filter(ch -> ch.getProductName() != null &&
                        "RES".equalsIgnoreCase(ch.getProductName().getName()))
                .count() > 0;
        boolean hasRESReleasePackage = resReleasePackage
                .filter(pkg -> StringUtils.startsWith(pkg, "sles_es-release")).isPresent();
        if (hasRESChannels || hasRESReleasePackage) {
            // we got a RES. find the corresponding SUSE product
            Optional<ReleaseFile> releaseFile = rhelReleaseFile
                    .flatMap(RhelUtils::parseReleaseFile);
            // Find the corresponding SUSEProduct in the database
            String name = releaseFile.map(ReleaseFile::getName).orElse("RES");
            String majorVersion = releaseFile.map(ReleaseFile::getMajorVersion)
                    .orElse("unknown");
            String release = releaseFile.map(ReleaseFile::getRelease).orElse("unknown");

            Optional<SUSEProduct> suseProduct = Optional.ofNullable(SUSEProductFactory
                    .findSUSEProduct("RES", majorVersion, release, arch, true));
            return Optional.of(new RhelProduct(suseProduct, name,
                    majorVersion, release, arch));
        }

        // next check if Centos
        if (centosReleaseFile.filter(StringUtils::isNotBlank).isPresent()) {
            return centosReleaseFile.map(v -> detectPlainRHEL(v, arch, "CentOS"));
        }

        // if neither RES nor Centos then we probably got a plain RHEL
        if (rhelReleaseFile.filter(StringUtils::isNotBlank).isPresent()) {
            return rhelReleaseFile.map(v -> detectPlainRHEL(v, arch, "RedHatEnterprise"));
        }

        return Optional.empty();
    }

    private static RhelProduct detectPlainRHEL(String releaseFileContent,
                                               String arch, String defaultName) {
        Optional<ReleaseFile> releaseFile = parseReleaseFile(releaseFileContent);
        String name = releaseFile.map(ReleaseFile::getName).orElse(defaultName);
        String majorVersion = releaseFile.map(ReleaseFile::getMajorVersion)
                .orElse("unknown");
        String release = releaseFile.map(ReleaseFile::getRelease).orElse("unknown");
        Optional<SUSEProduct> suseProduct = defaultName.equals("RedHatEnterprise") ?
                Optional.ofNullable(SUSEProductFactory
                        .findSUSEProduct("rhel-base", majorVersion, release, arch, true)) :
                Optional.empty();
        return new RhelProduct(suseProduct, name, majorVersion, release, arch);
    }

}
