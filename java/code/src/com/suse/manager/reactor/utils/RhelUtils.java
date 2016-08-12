package com.suse.manager.reactor.utils;

import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.server.Server;
import org.apache.commons.lang.StringUtils;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities related to RHEL minions.
 */
public class RhelUtils {

    private static Pattern RHEL_RELEASE_MATCHER = Pattern.compile("(.+)\\srelease\\s([\\d.]+)\\s\\((.+)\\).*", Pattern.DOTALL);

    public static class RhelProduct {

        private Optional<SUSEProduct> suseProduct;
        private String name;
        private String version;
        private String release;
        private String arch;

        public RhelProduct(Optional<SUSEProduct> suseProduct, String name, String version, String release, String arch) {
            this.suseProduct = suseProduct;
            this.name = name;
            this.version = version;
            this.release = release;
            this.arch = arch;
        }

        public Optional<SUSEProduct> getSuseProduct() {
            return suseProduct;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        public String getRelease() {
            return release;
        }

        public String getArch() {
            return arch;
        }
    }

    public static class ReleaseFile {

        private String name;
        private String majorVersion;
        private String minorVersion;
        private String release;

        public ReleaseFile(String name, String majorVersion, String minorVersion, String release) {
            this.name = name;
            this.majorVersion = majorVersion;
            this.minorVersion = minorVersion;
            this.release = release;
        }

        public String getName() {
            return name;
        }

        public String getMajorVersion() {
            return majorVersion;
        }

        public String getMinorVersion() {
            return minorVersion;
        }

        public String getRelease() {
            return release;
        }
    }

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

    public static Optional<RhelProduct> detectRhelProduct(Server server, Optional<String> resReleasePackage,
                                                          Optional<String> rhelReleaseFile, Optional<String> centosReleaseFile) {
        String arch = server.getServerArch().getLabel().replace("-redhat-linux", "");

        // check first if it has RES channels assigned or the RES release package installed
        boolean hasRESChannels = server.getChannels().stream()
                .filter(ch -> ch.getProductName() != null && "RES".equalsIgnoreCase(ch.getProductName().getName()))
                .count() > 0;
        boolean hasRESReleasePackage = resReleasePackage
                .filter(pkg -> StringUtils.startsWith(pkg, "sles_es-release")).isPresent();
        if (hasRESChannels || hasRESReleasePackage) {
            // we got a RES. find the corresponding SUSE product
            Optional<ReleaseFile> releaseFile = rhelReleaseFile.flatMap(RhelUtils::parseReleaseFile);
            // Find the corresponding SUSEProduct in the database
            String name = releaseFile.map(ReleaseFile::getName).orElse("RES");
            String majorVersion = releaseFile.map(ReleaseFile::getMajorVersion).orElse("unknown");
            String release = releaseFile.map(ReleaseFile::getRelease).orElse("unknown");

            Optional<SUSEProduct> suseProduct = Optional.ofNullable(SUSEProductFactory
                    .findSUSEProduct("RES", majorVersion, release, arch, true));
            return Optional.of(new RhelProduct(suseProduct, name, majorVersion, release, arch));
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

    private static RhelProduct detectPlainRHEL(String releaseFileContent, String arch, String defaultName) {
        Optional<ReleaseFile> releaseFile = parseReleaseFile(releaseFileContent);
        String name = releaseFile.map(ReleaseFile::getName).orElse(defaultName);
        String majorVersion = releaseFile.map(ReleaseFile::getMajorVersion).orElse("unknown");
        String release = releaseFile.map(ReleaseFile::getRelease).orElse("unknown");
        return new RhelProduct(Optional.empty(), name, majorVersion, release, arch);
    }

}
