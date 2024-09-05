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

import com.google.gson.annotations.SerializedName;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

/**
 * Encapsulates information about the supported Linux distributions and their corresponding sources of OVAL data.
 * */
public class OVALDistributionSourceInfo {
    @SerializedName("content")
    private Map<String, OVALSourceInfo> content;

    public Map<String, OVALSourceInfo> getContent() {
        return content == null ? Collections.emptyMap() : content;
    }

    /**
     * Returns the version source info
     *
     * @param version the OS version
     *
     * @return version source info
     * */
    public Optional<OVALSourceInfo> getVersionSourceInfo(String version) {
        return Optional.ofNullable(getContent().get(version));
    }

    @Override
    public String toString() {
        return "OVALDistributionSourceInfo{" +
                "content=" + content +
                '}';
    }
}
