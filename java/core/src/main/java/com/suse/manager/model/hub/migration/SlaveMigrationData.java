/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.model.hub.migration;

import java.util.Map;
import java.util.Objects;

public record SlaveMigrationData(String fqdn, String token, String rootCA) {

    /**
     * Builds an instance from the request data
     * @param dataMap the data as received from the request
     */
    public SlaveMigrationData(Map<String, String> dataMap) {
        this (
            Objects.requireNonNull(dataMap.get("fqdn"), "Missing slave fully qualified domain name"),
            Objects.requireNonNull(dataMap.get("token"), () -> "Missing access token for " + dataMap.get("fqdn")),
            dataMap.get("root_ca")
        );
    }

}
