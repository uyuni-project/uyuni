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

package com.suse.oval.config;

import com.suse.oval.OsFamily;

import com.google.gson.annotations.SerializedName;

import java.util.Map;
import java.util.Optional;

public class OVALConfig {
    @SerializedName("sources")
    private Map<OsFamily, OVALDistributionSourceInfo> sources;

    public Map<OsFamily, OVALDistributionSourceInfo> getSources() {
        return sources;
    }

    /**
     * Finds the location or URL of the OVAL vulnerability and/or patch files for the given OVAL product
     * identified by the given os family and version.
     *
     * @param osFamily the os family of the OS to find OVAL data for
     * @param version the version of the OS to find OVAL data for
     * @return the locations of OVAL vulnerability and/or patch files for the given OS
     * */
    public Optional<OVALSourceInfo> lookupSourceInfo(OsFamily osFamily, String version) {
        OVALDistributionSourceInfo distributionSources = sources.get(osFamily);
        if (distributionSources != null) {
            return distributionSources.getVersionSourceInfo(version);
        }
        return Optional.empty();
    }

    @Override
    public String toString() {
        return "OVALConfig{" +
                "sources=" + sources +
                '}';
    }
}
