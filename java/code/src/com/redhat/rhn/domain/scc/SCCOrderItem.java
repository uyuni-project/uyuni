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

import com.suse.scc.model.SCCOrderItemJson;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * This is a SUSE orderitem as parsed from JSON coming in from SCC.
 */
@Entity
@Table(name = "suseSCCOrderItem")
@NamedQueries
({
    @NamedQuery(name = "SCCOrderItem.deleteAll", query = "delete from com.redhat.rhn.domain.scc.SCCOrderItem"),
    @NamedQuery(name = "SCCOrderItem.deleteByCredential",
                query = "delete from com.redhat.rhn.domain.scc.SCCOrderItem as o where o.credentials = :creds")
 })
public class SCCOrderItem extends BaseDomainHelper {

    private Long id;
    private Credentials credentials;
    private Long sccId;
    private String sku;
    private Date endDate;
    private Date startDate;
    private Long subscriptionId;
    private Long quantity;

    /**
     * Default Constructor
     */
    public SCCOrderItem() { }

    /**
     * Constructor
     * @param j Json Order Item object
     * @param c Credentials object
     */
    public SCCOrderItem(SCCOrderItemJson j, Credentials c) {
        update(j, c);
    }

    /**
     * Update the Object with an SCCOrderItemJson fetched with Credentials
     * @param j the SCC Order Item Json object
     * @param c the credentials used to fetch it
     */
    public void update(SCCOrderItemJson j, Credentials c) {
        credentials = c;
        sccId = j.getSccId();
        sku = j.getSku();
        endDate = j.getEndDate();
        startDate = j.getStartDate();
        subscriptionId = j.getSubscriptionId();
        quantity = j.getQuantity();
    }

    /**
     * Gets the id.
     * @return the id
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sccorderitem_seq")
    @SequenceGenerator(name = "sccorderitem_seq", sequenceName = "suse_sccorder_id_seq",
                       allocationSize = 1)
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
     * @return the SCC id
     */
    @Column(name = "scc_id")
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
    @Column(name = "sku")
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
    @Column(name = "end_date")
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
    @Column(name = "start_date")
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
    @Column(name = "subscription_id")
    public Long getSubscriptionId() {
        return subscriptionId;
    }

    /**
     * @return the SCC Subscription
     */
    /*
    public SCCSubscription getSubscription() {
        return SCCCachingFactory.lookupSubscriptionBySccId(subscriptionId);
    }
    */
    /**
     * @param subscriptionIdIn the subscriptionId to set
     */
    public void setSubscriptionId(Long subscriptionIdIn) {
        this.subscriptionId = subscriptionIdIn;
    }

    /**
     * @return the quantity
     */
    @Column(name = "quantity")
    public Long getQuantity() {
        return quantity;
    }

    /**
     * @param quantityIn the quantity to set
     */
    public void setQuantity(Long quantityIn) {
        this.quantity = quantityIn;
    }

    /**
     * Get the mirror credentials.
     * @return the credentials
     */
    @ManyToOne
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
