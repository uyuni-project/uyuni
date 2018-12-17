/**
 * Copyright (c) 2018 SUSE LLC
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

/**
 * This is a SUSE orderitem as parsed from JSON coming in from SCC.
 */
public class SCCOrderItemJson {

    @SerializedName("id")
    private Long sccId;
    private String sku;
    @SerializedName("end_date")
    private Date endDate;
    @SerializedName("start_date")
    private Date startDate;
    @SerializedName("subscription_id")
    private Long subscriptionId;
    private Long quantity;

    /**
     * @return the SCC id
     */
    public Long getSccId() {
        return sccId;
    }

    /**
     * @param sccIdIn the SCC id to set
     */
    public void setSccId(Long sccIdIn) {
        this.sccId = sccIdIn;
    }

    /**
     * @return the sku
     */
    public String getSku() {
        return sku;
    }

    /**
     * @param skuIn the sku to set
     */
    public void setSku(String skuIn) {
        this.sku = skuIn;
    }

    /**
     * @return the endDate
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * @param endDateIn the endDate to set
     */
    public void setEndDate(Date endDateIn) {
        this.endDate = endDateIn;
    }

    /**
     * @return the startDate
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * @param startDateIn the startDate to set
     */
    public void setStartDate(Date startDateIn) {
        this.startDate = startDateIn;
    }

    /**
     * @return the subscriptionId
     */
    public Long getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * @param subscriptionIdIn the subscriptionId to set
     */
    public void setSubscriptionId(Long subscriptionIdIn) {
        this.subscriptionId = subscriptionIdIn;
    }

    /**
     * @return the quantity
     */
    public Long getQuantity() {
        return quantity;
    }

    /**
     * @param quantityIn the quantity to set
     */
    public void setQuantity(Long quantityIn) {
        this.quantity = quantityIn;
    }
}
