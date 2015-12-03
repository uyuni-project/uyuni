/**
 * Copyright (c) 2014 SUSE LLC
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
package com.redhat.rhn.domain.scc;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.credentials.Credentials;

import com.google.gson.annotations.SerializedName;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

import java.util.Date;

/**
 * This is a SUSE orderitem as parsed from JSON coming in from SCC.
 */
public class SCCOrderItem extends BaseDomainHelper {

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

    // ignored by gson
    private transient Long id;
    private transient Credentials credentials;

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
     * @param endDate the endDate to set
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * @return the startDate
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the subscriptionId
     */
    public Long getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * @return the SCC Subscription
     */
    public SCCSubscription getSubscription() {
        return SCCCachingFactory.lookupSubscriptionBySccId(subscriptionId);
    }
    /**
     * @param subscriptionId the subscriptionId to set
     */
    public void setSubscriptionId(Long subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    /**
     * @return the quantity
     */
    public Long getQuantity() {
        return quantity == null ? 0 : quantity;
    }

    /**
     * @param quantity the quantity to set
     */
    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }

    /**
     * Gets the id.
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     * @param idIn the new id
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * Get the mirror credentials.
     * @return the credentials
     */
    public Credentials getCredentials() {
        return credentials;
    }

    /**
     * Set the mirror credentials this repo can be retrieved with.
     * @param credentialsIn the credentials to set
     */
    public void setCredentials(Credentials credentialsIn) {
        this.credentials = credentialsIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SCCOrderItem)) {
            return false;
        }
        SCCOrderItem otherSCCOrderItem = (SCCOrderItem) other;
        return new EqualsBuilder()
            .append(getSccId(), otherSCCOrderItem.getSccId())
            .append(getSku(), otherSCCOrderItem.getSku())
            .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(getSccId())
            .append(getSku())
            .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
        .append("sccId", getSccId())
        .append("sku", getSku())
        .append("quantity", getQuantity())
        .append("end", getEndDate())
        .toString();
    }
}
