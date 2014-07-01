/**
 * Copyright (c) 2014 SUSE
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

/**
 * This is a subscription as parsed from JSON coming in from SCC.
 */
public class SCCSubscription {

    private int id;
    private String regcode;
    private String name;
    private String type;
    private String status;
    @SerializedName("starts_at")
    private String startsAt;
    @SerializedName("expires_at")
    private String expiresAt;
    @SerializedName("system_limit")
    private Integer systemLimit;
    @SerializedName("systems_count")
    private Integer systemsCount;
    @SerializedName("virtual_count")
    private Integer virtualCount;
    @SerializedName("product_classes")
    private List<String> productClasses;
    private List<SCCSystem> systems;

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @return the regcode
     */
    public String getRegcode() {
        return regcode;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return the startsAt
     */
    public String getStartsAt() {
        return startsAt;
    }

    /**
     * @return the expiresAt
     */
    public String getExpiresAt() {
        return expiresAt;
    }

    /**
     * @return the systemLimit
     */
    public Integer getSystemLimit() {
        return systemLimit;
    }

    /**
     * @return the systemsCount
     */
    public Integer getSystemsCount() {
        return systemsCount;
    }

    /**
     * @return the virtualCount
     */
    public Integer getVirtualCount() {
        return virtualCount;
    }

    /**
     * @return the productClasses
     */
    public List<String> getProductClasses() {
        return productClasses;
    }

    /**
     * @return the systems
     */
    public List<SCCSystem> getSystems() {
        return systems;
    }
}
