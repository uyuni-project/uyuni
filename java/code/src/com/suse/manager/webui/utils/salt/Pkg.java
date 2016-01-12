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
package com.suse.manager.webui.utils.salt;


import java.util.LinkedHashMap;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.suse.saltstack.netapi.calls.LocalCall;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * TODO: Merge this method into the Pkg class in saltstack-netapi-client.
 */
public class Pkg {

    private Pkg() { }

    /**
     * Information about a package as returned by pkg.info_installed and
     * pkg.info_available
     */
    public static class Info {

        @SerializedName("arch")
        private Optional<String> architecture = Optional.empty();
        @SerializedName("build_date")
        private Optional<ZonedDateTime> buildDate = Optional.empty();
        @SerializedName("build_date_time_t")
        private Optional<Long> buildDateUnixTime = Optional.empty();
        @SerializedName("build_host")
        private Optional<String> buildHost = Optional.empty();
        private Optional<String> description = Optional.empty();
        private Optional<String> group = Optional.empty();
        @SerializedName("install_date")
        private Optional<ZonedDateTime> installDate = Optional.empty();
        @SerializedName("install_date_time_t")
        private Optional<Long> installDateUnixTime = Optional.empty();
        private Optional<String> license = Optional.empty();
        @SerializedName("new_features_have_been_added")
        private Optional<String> newFeaturesHaveBeenAdded = Optional.empty();
        private Optional<String> packager = Optional.empty();
        private Optional<String> release = Optional.empty();
        private Optional<String> relocations = Optional.empty();
        private Optional<String> signature = Optional.empty();
        private Optional<String> size = Optional.empty();
        @SerializedName("source")
        private Optional<String> source = Optional.empty();
        private Optional<String> summary = Optional.empty();
        private Optional<String> url = Optional.empty();
        private Optional<String> vendor = Optional.empty();
        private Optional<String> version = Optional.empty();
        private Optional<String> epoch = Optional.empty();

        public Optional<String> getArchitecture() {
            return architecture;
        }

        public Optional<ZonedDateTime> getBuildDate() {
            return buildDate;
        }

        public Optional<String> getBuildHost() {
            return buildHost;
        }

        public Optional<String> getGroup() {
            return group;
        }

        public Optional<String> getDescription() {
            return description;
        }

        public Optional<ZonedDateTime> getInstallDate() {
            return installDate;
        }

        public Optional<String> getLicense() {
            return license;
        }

        public Optional<String> getNewFeaturesHaveBeenAdded() {
            return newFeaturesHaveBeenAdded;
        }

        public Optional<String> getPackager() {
            return packager;
        }

        public Optional<String> getRelease() {
            return release;
        }

        public Optional<String> getRelocations() {
            return relocations;
        }

        public Optional<String> getSignature() {
            return signature;
        }

        public Optional<String> getSize() {
            return size;
        }

        public Optional<String> getSource() {
            return source;
        }

        public Optional<String> getSummary() {
            return summary;
        }

        public Optional<String> getUrl() {
            return url;
        }

        public Optional<String> getVendor() {
            return vendor;
        }

        public Optional<String> getVersion() {
            return version;
        }

        public Optional<String> getEpoch() {
            return epoch;
        }

        public Optional<Long> getBuildDateUnixTime() {
            return buildDateUnixTime;
        }

        public Optional<Long> getInstallDateUnixTime() {
            return installDateUnixTime;
        }
    }


    public static LocalCall<Map<String, Info>> infoInstalled(
            List<String> args, String... packages) {
        return new LocalCall<>("pkg.info_installed", Optional.of(Arrays.asList(packages)),
                Optional.empty(), new TypeToken<Map<String, Info>>(){});
    }

    /**
     * @param refresh refresh repos before installation
     * @param pkgs list of packages
     * @return the call
     */
    public static LocalCall<Map<String, Object>> install(boolean refresh,
            List<String> pkgs) {
        LinkedHashMap<String, Object> kwargs = new LinkedHashMap<>();
        kwargs.put("refresh", refresh);
        kwargs.put("pkgs", pkgs);
        return new LocalCall<>("pkg.install", Optional.empty(), Optional.of(kwargs),
                new TypeToken<Map<String, Object>>() { });
    }
}
