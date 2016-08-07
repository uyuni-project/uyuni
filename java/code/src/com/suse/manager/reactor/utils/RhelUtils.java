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

    private static Pattern RHEL_RELEASE_MATCHER = Pattern.compile("(.*)\\srelease\\s([\\d.]+)\\s\\((.*)\\)");

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

    public static Optional<RhelProduct> getProductForRhel(Server server, String rhelReleaseContent) {
        Matcher matcher = RHEL_RELEASE_MATCHER.matcher(rhelReleaseContent);
        if (matcher.matches()) {
            String name = matcher.groupCount() > 0 ? matcher.group(1) : "";
            name = name.replaceAll("(?i)linux", "").replaceAll(" ", "");
            if (name.toLowerCase().startsWith("redhat") ||
                    name.toLowerCase().startsWith("centos") ||
                    name.toLowerCase().startsWith("slesexpandedsupportplatform")) {
                name = "RES";
            }
            String osname = name;
            String versionGroup = matcher.groupCount() > 1 ? matcher.group(2) : "";
            String version = StringUtils.substringBefore(versionGroup, ".");
            String release = matcher.groupCount() > 2 ? matcher.group(3) : "";
            String arch = server.getServerArch().getLabel().replace("-redhat-linux", "");

            // Find the corresponding SUSEProduct in the database
            Optional<SUSEProduct> suseProduct = Optional.ofNullable(SUSEProductFactory
                    .findSUSEProduct(name, version, release, arch, true));
            return Optional.of(new RhelProduct(suseProduct, osname, version, release, arch));
        }
        return Optional.empty();
    }

}
