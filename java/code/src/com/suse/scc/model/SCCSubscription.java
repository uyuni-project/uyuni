/**
 * Copyright (c) 2014--2015 SUSE LLC
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
import java.util.Date;
import java.util.List;
import javax.xml.bind.DatatypeConverter;

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
    public Date getStartsAt() {
        // The date-time supposed to be strictly ISO-8601.
        // Note that SimpleDateFormat is not ISO-8601 compliant on up to Java 6 inclusive.
        return this.startsAt == null ? null :
               DatatypeConverter.parseDateTime(this.startsAt).getTime();
    }

    /**
     * @return the expiresAt
     */
    public Date getExpiresAt() {
        // The date-time supposed to be strictly ISO-8601.
        // Note that SimpleDateFormat is not ISO-8601 compliant on up to Java 6 inclusive.
        return this.expiresAt == null ? null :
               DatatypeConverter.parseDateTime(this.expiresAt).getTime();
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

    // Setters are only for JUnit tests

    /**
     * @param idIn the id to set
     */
    public void setId(int idIn) {
        this.id = idIn;
    }

    /**
     * @param regcodeIn the regcode to set
     */
    public void setRegcode(String regcodeIn) {
        this.regcode = regcodeIn;
    }

    /**
     * @param nameIn the name to set
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @param typeIn the type to set
     */
    public void setType(String typeIn) {
        this.type = typeIn;
    }

    /**
     * @param statusIn the status to set
     */
    public void setStatus(String statusIn) {
        this.status = statusIn;
    }

    /**
     * @param startsAtIn the startsAt to set
     */
    public void setStartsAt(String startsAtIn) {
        this.startsAt = startsAtIn;
    }

    /**
     * @param expiresAtIn the expiresAt to set
     */
    public void setExpiresAt(String expiresAtIn) {
        this.expiresAt = expiresAtIn;
    }

    /**
     * @param systemLimitIn the systemLimit to set
     */
    public void setSystemLimit(Integer systemLimitIn) {
        this.systemLimit = systemLimitIn;
    }

    /**
     * @param systemsCountIn the systemsCount to set
     */
    public void setSystemsCount(Integer systemsCountIn) {
        this.systemsCount = systemsCountIn;
    }

    /**
     * @param virtualCountIn the virtualCount to set
     */
    public void setVirtualCount(Integer virtualCountIn) {
        this.virtualCount = virtualCountIn;
    }

    /**
     * @param productClassesIn the productClasses to set
     */
    public void setProductClasses(List<String> productClassesIn) {
        this.productClasses = productClassesIn;
    }

    /**
     * @param systemsIn the systems to set
     */
    public void setSystems(List<SCCSystem> systemsIn) {
        this.systems = systemsIn;
    }
}
