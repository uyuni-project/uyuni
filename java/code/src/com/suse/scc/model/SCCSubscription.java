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
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @param regcode the regcode to set
     */
    public void setRegcode(String regcode) {
        this.regcode = regcode;
    }

    /**
     * @param name the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * @param startsAt the startsAt to set
     */
    public void setStartsAt(String startsAt) {
        this.startsAt = startsAt;
    }

    /**
     * @param expiresAt the expiresAt to set
     */
    public void setExpiresAt(String expiresAt) {
        this.expiresAt = expiresAt;
    }

    /**
     * @param systemLimit the systemLimit to set
     */
    public void setSystemLimit(Integer systemLimit) {
        this.systemLimit = systemLimit;
    }

    /**
     * @param systemsCount the systemsCount to set
     */
    public void setSystemsCount(Integer systemsCount) {
        this.systemsCount = systemsCount;
    }

    /**
     * @param virtualCount the virtualCount to set
     */
    public void setVirtualCount(Integer virtualCount) {
        this.virtualCount = virtualCount;
    }

    /**
     * @param productClasses the productClasses to set
     */
    public void setProductClasses(List<String> productClasses) {
        this.productClasses = productClasses;
    }

    /**
     * @param systems the systems to set
     */
    public void setSystems(List<SCCSystem> systems) {
        this.systems = systems;
    }
}
