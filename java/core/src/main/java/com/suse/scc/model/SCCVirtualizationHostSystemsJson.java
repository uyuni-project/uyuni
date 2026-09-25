/*
 * Copyright (c) 2023 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.scc.model;

import com.redhat.rhn.domain.server.VirtualInstance;

import java.util.HashMap;
import java.util.Map;

/**
 * SCCVirtualizationHostSystemsJson
 */
public class SCCVirtualizationHostSystemsJson {

    private String uuid;

    private Map<String, String> properties;

    /**
     * Contsructor
     * @param uuidIn the UUID
     * @param propertiesIn system properties like vm_name and vm_state
     */
    public SCCVirtualizationHostSystemsJson(String uuidIn, Map<String, String> propertiesIn) {
        uuid = uuidIn;
        properties = propertiesIn;
    }

    /**
     * Constructor
     * @param vi the virtual instance
     */
    public SCCVirtualizationHostSystemsJson(VirtualInstance vi) {
        uuid = vi.getUuid()
                // SCC require UUID in the RFC format
                .replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
        properties = new HashMap<>();
        properties.put("vm_name", vi.getName());
        properties.put("vm_state", vi.getState().getLabel());
    }

    /**
     * @return the UUID
     */
    public String getUuid() {
        if (uuid == null) {
            return "";
        }
        return uuid;
    }

    /**
     * @return the properties
     */
    public Map<String, String> getProperties() {
        return properties;
    }
}
