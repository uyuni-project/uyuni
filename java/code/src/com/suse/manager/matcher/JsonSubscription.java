/**
 * Copyright (c) 2015 SUSE LLC
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

package com.suse.manager.matcher;

import com.redhat.rhn.domain.credentials.Credentials;
import com.redhat.rhn.domain.scc.SCCOrderItem;
import com.redhat.rhn.domain.scc.SCCSubscription;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;


/**
 * JSON representation of a Subscription
 */
public class JsonSubscription {

    /** SCC OrderItem sccId */
    private Long id;
    /** SCC OrderItem sku */
    private String partNumber;
    /** SCC OrderItem endDate */
    private Date expiresAt;
    /** SCC OrderItem startDate */
    private Date startsAt;
    /** SCC OrderItem quantity */
    private Long systemLimit;

    /** SCC Subscription name */
    private String name;

    /** SCC Subscription status */
    private String status;
    /** SCC Subscription regcode */
    private String regcode;
    /** SCC Subscription type */
    private String type;

    /** SCC Subscription products */
    private List<Long> productIds;

    private String sccOrgId;

    /**
     * Constructor
     * @param order an order item
     */
    public JsonSubscription(SCCOrderItem order) {
        id = order.getSccId();
        partNumber = order.getSku();
        expiresAt = order.getEndDate();
        startsAt = order.getStartDate();
        systemLimit = order.getQuantity();
        Credentials c = order.getCredentials();
        if (c != null) {
            sccOrgId = c.getUsername();
        }
        else {
            sccOrgId = "extFile";
        }
        SCCSubscription s = order.getSubscription();
        if (s != null) {
            name = s.getName();
            status = s.getStatus();
            regcode = s.getRegcode();
            type = s.getType();
            productIds = s.getProducts().stream()
                    .map(i -> i.getProductId())
                    .collect(Collectors.toList());
        }
        else {
            productIds = new LinkedList<>();
        }
    }

    /**
     * @return the Id
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the partNumber
     */
    public String getPartNumber() {
        return partNumber;
    }

    /**
     * @return the expiresAt
     */
    public Date getExpiresAt() {
        return expiresAt;
    }

    /**
     * @return the startsAt
     */
    public Date getStartsAt() {
        return startsAt;
    }

    /**
     * @return the systemLimit
     */
    public Long getSystemLimit() {
        return systemLimit;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @return the regcode
     */
    public String getRegcode() {
        return regcode;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @return the list of products ids
     */
    public List<Long> getProductIds() {
        return productIds;
    }

    /**
     * @return the credentials
     */
    public String getSccOrgId() {
        return sccOrgId;
    }

    /**
     * @param idIn the subscription Id to set
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @param partNumberIn the partNumber to set
     */
    public void setPartNumber(String partNumberIn) {
        this.partNumber = partNumberIn;
    }

    /**
     * @param expiresAtIn the expiresAt to set
     */
    public void setExpiresAt(Date expiresAtIn) {
        this.expiresAt = expiresAtIn;
    }

    /**
     * @param startsAtIn the startDate to set
     */
    public void setStartsAt(Date startsAtIn) {
        this.startsAt = startsAtIn;
    }

    /**
     * @param systemLimitIn the systemLimit to set
     */
    public void setQuantity(Long systemLimitIn) {
        this.systemLimit = systemLimitIn;
    }

    /**
     * @param nameIn the name to set
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @param statusIn the status to set
     */
    public void setStatus(String statusIn) {
        this.status = statusIn;
    }

    /**
     * @param regcodeIn the regcode to set
     */
    public void setRegcode(String regcodeIn) {
        this.regcode = regcodeIn;
    }

    /**
     * @param typeIn the type to set
     */
    public void setType(String typeIn) {
        this.type = typeIn;
    }

    /**
     * @param productsIdsIn the product ids to set
     */
    public void setProductIds(List<Long> productsIdsIn) {
        this.productIds = productsIdsIn;
    }

    /**
     * @param sccOrgIdIn the scc org id to set
     */
    public void setSccOrgId(String sccOrgIdIn) {
        this.sccOrgId = sccOrgIdIn;
    }
}
