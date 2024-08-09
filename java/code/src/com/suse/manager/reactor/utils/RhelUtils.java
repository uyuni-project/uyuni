/*
 * Copyright (c) 2016--2021 SUSE LLC
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

import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.image.ImageInfo;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductChannel;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.server.Server;

import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utilities related to RHEL minions.
 */
public class RhelUtils {

    private RhelUtils() { }

    private static final Pattern RHEL_RELEASE_MATCHER =
            Pattern.compile("([\\d.]+)\\s*+\\(([^)]++)\\).*+", Pattern.DOTALL);
    private static final Pattern ORACLE_RELEASE_MATCHER =
            Pattern.compile("([\\d.]+).*", Pattern.DOTALL);
    private static final Pattern ALIBABA_RELEASE_MATCHER =
            Pattern.compile("([\\d.]+)\\s*+LTS\\s*+\\(([^)]++)\\).*+", Pattern.DOTALL);

    /**
     * Information about RHEL based OSes.
     */
    public static class RhelProduct {

        private Optional<SUSEProduct> suseBaseProduct;
        private Set<SUSEProduct> suseAdditionalProducts;
        private String name;
        private String version;
        private String release;

        /**
         * Constructor.
         * @param suseBaseProductIn the suse product that corresponds to this OS
         * @param suseAdditionalProductsIn any possible additional SUSE products like SLL ones
         * @param nameIn the name of the OS
         * @param versionIn the version
         * @param releaseIn the release name
         */
        public RhelProduct(Optional<SUSEProduct> suseBaseProductIn, Set<SUSEProduct> suseAdditionalProductsIn,
                           String nameIn, String versionIn, String releaseIn) {
            this.suseBaseProduct = suseBaseProductIn;
            this.suseAdditionalProducts = suseAdditionalProductsIn;
            this.name = nameIn;
            this.version = versionIn;
            this.release = releaseIn;
        }

        /**
         * @return the SUSE base product, if any.
         */
        public Optional<SUSEProduct> getSuseBaseProduct() {
            return suseBaseProduct;
        }

        /**
         * @return all the SUSE products if any.
         */
        public Set<SUSEProduct> getAllSuseProducts() {
            Set<SUSEProduct> set = new HashSet<>(suseAdditionalProducts);
            suseBaseProduct.ifPresent(set::add);
            return set;
        }

        /**
         * Add a product to the additional product set
         * @param productIn the product to add
         */
        public void addSuseAdditionalProducts(SUSEProduct productIn) {
            suseAdditionalProducts.add(productIn);
        }

        /**
         * @return the name of the OS (RedHatEnterpriseServer or Centos or OracleLinux)
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
     * The content of the /etc/redhat|centos|oracle|almalinux|alinux|system|rocky-release file.
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
     * Parse the /etc/redhat|centos|oracle|almalinux|alinux|system|rocky-release
     * @param releaseFile the content of the release file
     * @return the parsed content of the release file
     */
    public static Optional<ReleaseFile> parseReleaseFile(String releaseFile) {
        String[] parts = releaseFile.split("\\srelease\\s", 2);
        if (parts.length != 2) {
            return Optional.empty();
        }

        // We match here data from the system and try to find the product
        // how it is named in SCC or sumatoolbox. This requires sometimes
        // some changes on the string we parse.
        //
        // AlmaLinux and AmazonLinux are also matched by the RHEL matcher
        Matcher matcher = RHEL_RELEASE_MATCHER.matcher(parts[1]);
        if (matcher.matches()) {
            String name = parts[0].replaceAll("(?i)linux", "").replace(" ", "");
            if (name.startsWith("Alma") || name.startsWith("Amazon") || name.startsWith("Rocky")) {
                name = parts[0].replace(" ", "");
            }
            String majorVersion = StringUtils.substringBefore(matcher.group(1), ".");
            String minorVersion = StringUtils.substringAfter(matcher.group(1), ".");
            String release = matcher.group(2);
            return Optional.of(new ReleaseFile(name, majorVersion, minorVersion, release));
        }
        else {
            Matcher amatcher = ALIBABA_RELEASE_MATCHER.matcher(parts[1]);
            if (amatcher.matches()) {
                String name = parts[0].replaceAll("(?i)linux", "").replace(" ", "");
                String majorVersion = StringUtils.substringBefore(amatcher.group(1), ".");
                String minorVersion = StringUtils.substringAfter(amatcher.group(1), ".");
                String release = amatcher.group(2);
                return Optional.of(new ReleaseFile(name, majorVersion, minorVersion, release));
            }
            else {
                Matcher omatcher = ORACLE_RELEASE_MATCHER.matcher(parts[1]);
                if (omatcher.matches()) {
                    String name = parts[0].replaceAll("(?i)server", "").replace(" ", "");
                    String majorVersion = StringUtils.substringBefore(omatcher.group(1), ".");
                    String minorVersion = StringUtils.substringAfter(omatcher.group(1), ".");
                    return Optional.of(new ReleaseFile(name, majorVersion, minorVersion, ""));
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Checks if there is a RES/SLL channels assigned or the RES/SLL release package installed
     * @param channels channels assigned to the target
     * @param releasePackage the package that provides release files
     * @param productName the ES product name to search for
     * @param packagePrefix the package prefix specific for the given ES product name
     * @return true if an ES product is identified, false otherwise
     */
    static boolean checkForESProducts(
            Set<Channel> channels, Optional<String> releasePackage, String productName, String packagePrefix
    ) {
        boolean hasChannels = channels
                .stream()
                .anyMatch(ch -> ch.getProductName() != null &&
                        productName.equalsIgnoreCase(ch.getProductName().getName()));
        boolean hasReleasePackage = releasePackage
                .filter(pkg -> StringUtils.startsWith(pkg, packagePrefix))
                .isPresent();
        return hasChannels || hasReleasePackage;
    }

    /**
     * For an ES related product name find the corresponding SUSE product
     * @param rhelReleaseFile the content of /etc/redhat-release
     * @param centosReleaseFile the content of /etc/centos-release
     * @param arch the architecture string of the target
     * @param productName the ES product name to search for
     * @return the {@link RhelProduct}
     */
    static Optional<RhelProduct> getRHELBasedSUSEProduct(
            Optional<String> rhelReleaseFile, Optional<String> centosReleaseFile, String arch, String productName
    ) {
        Optional<ReleaseFile> releaseFile = rhelReleaseFile.or(() -> centosReleaseFile)
                .flatMap(RhelUtils::parseReleaseFile);
        // Find the corresponding SUSEProduct in the database
        String name = releaseFile.map(ReleaseFile::getName).orElse(productName);
        String majorVersion = releaseFile.map(ReleaseFile::getMajorVersion)
                .orElse("unknown");
        String release = releaseFile.map(ReleaseFile::getRelease).orElse("unknown");

        Optional<SUSEProduct> suseProduct = Optional.ofNullable(SUSEProductFactory
                .findSUSEProduct(productName, majorVersion, release, arch, true));

        Optional<SUSEProduct> suseBaseProduct = suseProduct.flatMap(p -> {
            if (p.isBase()) {
                return Optional.of(p);
            }
            else {
                return Optional.ofNullable(SUSEProductFactory
                        .findSUSEProduct(getBaseProductName(majorVersion), majorVersion, release, arch, true));
            }
        }).filter(SUSEProduct::isBase);

        return Optional.of(new RhelProduct(suseBaseProduct, suseProduct.stream().collect(Collectors.toSet()), name,
                majorVersion, release));
    }

    /**
     * Guess the SUSE product for the RedHat minion and parse the
     * /etc/redhat,centos-release file.
     * 1) if either the RES or the SLL channel or a clone of the RES/SLL channel is
     *    assigned to a system it is either a RES or an SLL system
     * 2) if either a RES or an SLL release package is installed it is a RES/SLL.
     * 3) otherwise it is not a RES/SLL system
     * 4) if /etc/oracle-release exists, it is OracleLinux
     * 5) is it a centos system? check if /etc/centos-release file exists
     * 6) finally we can say it is a original RHEL (maybe:-)
     *
     * @param channels channels assigned to the target
     * @param arch the architecture string of the target
     * @param resReleasePackage the package that provides 'sles_es-release'
     * @param libertyReleasePackage the package that provides 'sll-release'
     * @param rhelReleaseFile the content of /etc/redhat-release
     * @param centosReleaseFile the content of /etc/centos-release
     * @param oracleReleaseFile the content of /etc/oracle-release
     * @param alibabaReleaseFile the content of /etc/alinux-release
     * @param almaReleaseFile the content of /etc/almalinux-release
     * @param amazonReleaseFile the content of /etc/system-release
     * @param rockyReleaseFile the content of /etc/rocky-release
     * @return the {@link RhelProduct}
     */
    public static Optional<RhelProduct> detectRhelProduct(
            Set<Channel> channels, String arch, Optional<String> resReleasePackage,
            Optional<String> libertyReleasePackage, Optional<String> rhelReleaseFile,
            Optional<String> centosReleaseFile, Optional<String> oracleReleaseFile,
            Optional<String> alibabaReleaseFile, Optional<String> almaReleaseFile,
            Optional<String> amazonReleaseFile, Optional<String> rockyReleaseFile) {

        // check first if it has RES/SLL channels assigned or the RES/SLL release package installed
        var isRes = RhelUtils
                .checkForESProducts(channels, resReleasePackage, "RES", "sles_es-release");
        var isSLL = RhelUtils
                .checkForESProducts(channels, libertyReleasePackage, "SLL", "sll-release");

        Optional<RhelProduct> rhelProducts = Optional.empty();

        // if we got either RES or an SLL find the corresponding SUSE product
        if (isRes) {
            rhelProducts = RhelUtils.getRHELBasedSUSEProduct(rhelReleaseFile, centosReleaseFile, arch, "RES");
        }
        else if (isSLL) {
            rhelProducts = RhelUtils.getRHELBasedSUSEProduct(rhelReleaseFile, centosReleaseFile, arch, "SLL");
        }

        // next check if OracleLinux
        else if (oracleReleaseFile.filter(StringUtils::isNotBlank).isPresent()) {
            rhelProducts = oracleReleaseFile.map(v -> detectPlainRHEL(v, arch, "OracleLinux"));
        }

        // next check if Alibaba Cloud Linux
        else if (alibabaReleaseFile.filter(StringUtils::isNotBlank).isPresent()) {
            rhelProducts = alibabaReleaseFile.map(v -> detectPlainRHEL(v, arch, "Alibaba"));
        }

        // next check if AlmaLinux
        else if (almaReleaseFile.filter(StringUtils::isNotBlank).isPresent()) {
            rhelProducts = almaReleaseFile.map(v -> detectPlainRHEL(v, arch, "AlmaLinux"));
        }

        // next check if Amazon Linux
        else if (amazonReleaseFile.filter(StringUtils::isNotBlank).isPresent()) {
            rhelProducts = amazonReleaseFile.map(v -> detectPlainRHEL(v, arch, "AmazonLinux"));
        }

        // next check if Rocky Linux
        else if (rockyReleaseFile.filter(StringUtils::isNotBlank).isPresent()) {
            rhelProducts = rockyReleaseFile.map(v -> detectPlainRHEL(v, arch, "Rocky"));
        }

        // next check if Centos
        else if (centosReleaseFile.filter(StringUtils::isNotBlank).isPresent()) {
            rhelProducts = centosReleaseFile.map(v -> detectPlainRHEL(v, arch, "CentOS"));
        }

        // if neither RES nor Centos then we probably got a plain RHEL
        else if (rhelReleaseFile.filter(StringUtils::isNotBlank).isPresent()) {
            rhelProducts = rhelReleaseFile.map(v -> detectPlainRHEL(v, arch, "RedHatEnterprise"));
        }

        detectInstalledExtensionsOnRhel(rhelProducts, channels);

        return rhelProducts;
    }

    private static void detectInstalledExtensionsOnRhel(Optional<RhelProduct> rhelProductsIn, Set<Channel> channelsIn) {

        if (rhelProductsIn.isEmpty()  || rhelProductsIn.get().getSuseBaseProduct().isEmpty() || channelsIn.isEmpty()) {
            return;
        }
        SUSEProduct baseProduct = rhelProductsIn.get().getSuseBaseProduct().get();
        Set<SUSEProduct> extensions = channelsIn.stream()
                .flatMap(channel -> {
                    if (channel.isCloned()) {
                        return channel.originChain().filter(c -> !c.isCloned());
                    }
                    else {
                        return Stream.of(channel);
                    }
                })
                .flatMap(c -> c.getSuseProductChannels().stream().map(SUSEProductChannel::getProduct))
                .filter(p -> !p.equals(baseProduct))
                .collect(Collectors.toSet());
        rhelProductsIn.ifPresent(p -> extensions.forEach(p::addSuseAdditionalProducts));
    }

    /**
     * Guess the SUSE product for the RedHat minion and parse the
     * /etc/redhat,centos-release file.
     * 1) if either the RES or the SLL channel or a clone of the RES/SLL channel is
     *    assigned to a system it is either a RES or an SLL system
     * 2) if either a RES or an SLL release package is installed it is a RES/SLL.
     * 3) otherwise it is not a RES/SLL system
     * 4) if /etc/oracle-release exists, it is OracleLinux
     * 5) is it a centos system? check if /etc/centos-release file exists
     * 6) finally we can say it is a original RHEL (maybe:-)
     *
     * @param server the minion
     * @param resReleasePackage the package that provides 'sles_es-release'
     * @param libertyReleasePackage the package that provides 'sll-release'
     * @param rhelReleaseFile the content of /etc/redhat-release
     * @param centosReleaseFile the content of /etc/centos-release
     * @param oracleReleaseFile the content of /etc/oracle-release
     * @param alibabaReleaseFile the content of /etc/alinux-release
     * @param almaReleaseFile the content of /etc/almalinux-release
     * @param amazonReleaseFile the content of /etc/system-release
     * @param rockyReleaseFile the content of /etc/rocky-release
     * @return the {@link RhelProduct}
     */
    public static Optional<RhelProduct> detectRhelProduct(
            Server server, Optional<String> resReleasePackage,
            Optional<String> libertyReleasePackage, Optional<String> rhelReleaseFile,
            Optional<String> centosReleaseFile, Optional<String> oracleReleaseFile,
            Optional<String> alibabaReleaseFile, Optional<String> almaReleaseFile,
            Optional<String> amazonReleaseFile, Optional<String> rockyReleaseFile) {
        return detectRhelProduct(
                server.getChannels(),
                server.getServerArch().getLabel().replace("-redhat-linux", ""),
                resReleasePackage,
                libertyReleasePackage,
                rhelReleaseFile,
                centosReleaseFile,
                oracleReleaseFile,
                alibabaReleaseFile,
                almaReleaseFile,
                amazonReleaseFile,
                rockyReleaseFile
        );
    }

    /**
     * Guess the SUSE product for the RedHat minion and parse the
     * /etc/redhat,centos-release file.
     * 1) if the RES channel or a clone of the RES channel is
     *    assigned to a system it is a RES system
     * 2) if a RES release package (sles_es-release) is installed it is a RES.
     * 3) otherwise it is not a RES system
     * 4) if /etc/oracle-release exists it is a OracleLinux
     * 5) if /etc/alinlux-release exists it is an Alibaba Cloud Linux
     * 6) is it a centos system? check if /etc/centos-release file exists
     * 7) finally we can say it is a original RHEL (maybe:-)
     *
     * @param image the image
     * @param resReleasePackage the package that provides 'sles_es-release'
     * @param libertyReleasePackage the package that provides 'sll-release'
     * @param rhelReleaseFile the content of /etc/redhat-release
     * @param centosReleaseFile the content of /etc/centos-release
     * @param oracleReleaseFile the content of /etc/oracle-release
     * @param alibabaReleaseFile the content of /etc/alinux-release
     * @param almaReleaseFile the content of /etc/almalinux-release
     * @param amazonReleaseFile the content of /etc/system-release
     * @param rockyReleaseFile the content of /etc/rocky-release
     * @return the {@link RhelProduct}
     */
    public static Optional<RhelProduct> detectRhelProduct(
            ImageInfo image, Optional<String> resReleasePackage,
            Optional<String> libertyReleasePackage, Optional<String> rhelReleaseFile,
            Optional<String> centosReleaseFile, Optional<String> oracleReleaseFile,
            Optional<String> alibabaReleaseFile, Optional<String> almaReleaseFile,
            Optional<String> amazonReleaseFile, Optional<String> rockyReleaseFile) {
        return detectRhelProduct(
                image.getChannels(),
                image.getImageArch().getLabel().replace("-redhat-linux", ""),
                resReleasePackage,
                libertyReleasePackage,
                rhelReleaseFile,
                centosReleaseFile,
                oracleReleaseFile,
                alibabaReleaseFile,
                almaReleaseFile,
                amazonReleaseFile,
                rockyReleaseFile
        );
    }

    private static String getBaseProductName(String majorVersion) {
        Matcher versionMatcher = Pattern.compile("^(\\d+)").matcher(majorVersion);
        if (versionMatcher.matches() && Integer.parseInt(versionMatcher.group(1)) >= 9) {
            return "el-base";
        }
        return "rhel-base";
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
                        .findSUSEProduct(getBaseProductName(majorVersion), majorVersion, release, arch, true)) :
                Optional.ofNullable(SUSEProductFactory
                        .findSUSEProduct(name, majorVersion, release, arch, true));
        return new RhelProduct(suseProduct, new HashSet<>(), name, majorVersion, release);
    }

}
