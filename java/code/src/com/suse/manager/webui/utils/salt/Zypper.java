package com.suse.manager.webui.utils.salt;

import com.google.gson.reflect.TypeToken;
import com.suse.salt.netapi.calls.LocalCall;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Optional;

/**
 * salt.modules.zypper
 *
 * https://docs.saltstack.com/en/latest/ref/modules/all/salt.modules.zypper.html
 */
public class Zypper {

    private Zypper() { }

    /**
     * The product information as returned by listProducts().
     */
    public static class ProductInfo {
        private final String name;
        private final String arch;
        private final String description;
        private final String eol;
        private final String epoch;
        private final String flavor;
        private final boolean installed;
        private final boolean isbase;
        private final String productline;
        private Optional<String> registerrelease = Optional.empty();
        private final String release;
        private final String repo;
        private final String shortname;
        private final String summary;
        private final String vendor;
        private final String version;

        public ProductInfo(String name, String arch, String description, String eol,
                String epoch, String flavor, boolean installed, boolean isbase,
                String productline, Optional<String> registerrelease, String release,
                String repo, String shortname, String summary, String vendor,
                String version) {
            this.name = name;
            this.arch = arch;
            this.description = description;
            this.eol = eol;
            this.epoch = epoch;
            this.flavor = flavor;
            this.installed = installed;
            this.isbase = isbase;
            this.productline = productline;
            this.registerrelease = registerrelease;
            this.release = release;
            this.repo = repo;
            this.shortname = shortname;
            this.summary = summary;
            this.vendor = vendor;
            this.version = version;
        }

        public String getName() {
            return name;
        }

        public String getVersion() {
            return version;
        }

        public String getArch() {
            return arch;
        }

        public String getDescription() {
            return description;
        }

        public String getEol() {
            return eol;
        }

        public String getEpoch() {
            return epoch;
        }

        public String getFlavor() {
            return flavor;
        }

        public boolean getInstalled() {
            return installed;
        }

        public boolean getIsbase() {
            return isbase;
        }

        public String getProductline() {
            return productline;
        }

        public Optional<String> getRegisterrelease() {
            return registerrelease;
        }

        public String getRelease() {
            return release;
        }

        public String getRepo() {
            return repo;
        }

        public String getShortname() {
            return shortname;
        }

        public String getSummary() {
            return summary;
        }

        public String getVendor() {
            return vendor;
        }
    }

    public static LocalCall<List<ProductInfo>> listProducts(boolean all) {
        LinkedHashMap<String, Object> args = new LinkedHashMap<>();
        args.put("all", all);
        return new LocalCall<>("pkg.list_products", Optional.empty(), Optional.of(args),
                new TypeToken<List<ProductInfo>>() { });
    }
}
