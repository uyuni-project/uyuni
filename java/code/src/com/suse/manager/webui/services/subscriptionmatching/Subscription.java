/*
 * Copyright (c) 2016 SUSE LLC
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

package com.suse.manager.webui.services.subscriptionmatching;

import java.util.Date;

/**
 * Backing data for the Subscription Matching UI. Representation of a single Subscription.
 */
public class Subscription {

    private Long id;
    private String partNumber;
    private String description;
    private String policy;
    private Integer totalQuantity;
    private int matchedQuantity;
    private Date startDate;
    private Date endDate;

    /**
     * Standard constructor.
     *
     * @param idIn - id
     * @param partNumberIn - part number
     * @param productDescriptionIn - product description
     * @param policyIn - policy
     * @param totalQuantityIn - total quantity
     * @param matchedQuantityIn - matched quantity
     * @param startDateIn - start date
     * @param endDateIn - end date
     */
    public Subscription(Long idIn, String partNumberIn, String productDescriptionIn,
            String policyIn, Integer totalQuantityIn, int matchedQuantityIn,
            Date startDateIn, Date endDateIn) {
        id = idIn;
        partNumber = partNumberIn;
        description = productDescriptionIn;
        policy = policyIn;
        totalQuantity = totalQuantityIn;
        matchedQuantity = matchedQuantityIn;
        startDate = startDateIn;
        endDate = endDateIn;
    }

    /**
     * Gets the id.
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     * @param idIn - the id
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * Gets the part number.
     * @return the part number.
     */
    public String getPartNumber() {
        return partNumber;
    }

    /**
     * Sets the part number.
     * @param partNumberIn - the part number
     */
    public void setPartNumber(String partNumberIn) {
        partNumber = partNumberIn;
    }

    /**
     * Gets the description.
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description.
     * @param descriptionIn - the description
     */
    public void setDescription(String descriptionIn) {
        description = descriptionIn;
    }

    /**
     * Gets the policy description
     * @return the policy description
     */
    public String getPolicy() {
        return policy;
    }

    /**
     * Sets the policy description
     * @param policyIn - the policy description
     */
    public void setPolicy(String policyIn) {
        policy = policyIn;
    }

    /**
     * Gets the total quantity.
     * @return the total quantity
     */
    public Integer getTotalQuantity() {
        return totalQuantity;
    }

    /**
     * Sets the total quantity.
     * @param totalQuantityIn - total quantity
     */
    public void setTotalQuantity(Integer totalQuantityIn) {
        totalQuantity = totalQuantityIn;
    }

    /**
     * Gets the matched quantity.
     * @return the matched quantity
     */
    public int getMatchedQuantity() {
        return matchedQuantity;
    }

    /**
     * Sets the matched quantity
     * @param matchedQuantityIn - matched quantity
     */
    public void setMatchedQuantity(int matchedQuantityIn) {
        matchedQuantity = matchedQuantityIn;
    }

    /**
     * Gets the start date.
     * @return the start date
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Sets the start date.
     * @param startDateIn - the start date
     */
    public void setStartDate(Date startDateIn) {
        startDate = startDateIn;
    }

    /**
     * Gets the end date.
     * @return the end date
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Sets the end date.
     * @param endDateIn - the end date
     */
    public void setEndDate(Date endDateIn) {
        endDate = endDateIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "Subscription{" +
                "partNumber='" + partNumber + '\'' +
                ", description='" + description + '\'' +
                ", totalQuantity=" + totalQuantity +
                ", matchedQuantity=" + matchedQuantity +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                '}';
    }
}
