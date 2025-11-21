/*
 * Copyright (c) 2025 SUSE LLC
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
package com.suse.scc.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class SAPJson {

    @SerializedName("system_id")
    private String systemId;

    @SerializedName("instance_types")
    private List<String> instanceTypes;

    /**
     * Constructor
     * @param systemIdIn - the SAP system id
     * @param instanceTypesIn - the array of SAP instance types
     */
    public SAPJson(String systemIdIn, List<String> instanceTypesIn) {
        systemId = systemIdIn;
        instanceTypes = instanceTypesIn;
    }

    public String getSystemId() {
        return systemId;
    }

    public List<String> getInstanceTypes() {
        return instanceTypes;
    }
}
