/*
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
package com.redhat.rhn.manager.setup;

import java.util.Date;

/**
 * DTO to be used for listing subscriptions in the UI.
 */
public class SubscriptionDto {
    private String productClass;
    private Integer consumed;
    private String name;
    private Date startDate;
    private Date endDate;

    /**
     * Constructor.
     *
     * @param nameIn the name
     * @param startDateIn the start date
     * @param endDateIn the end date
     */
    public SubscriptionDto(String nameIn, Date startDateIn, Date endDateIn) {
        this(nameIn, null, null, startDateIn, endDateIn);
    }

    /**
     * Constructor.
     *
     * @param nameIn the name
     * @param productClassIn the product class
     * @param consumedIn consumed
     * @param startDateIn the start date
     * @param endDateIn the end date
     */
    public SubscriptionDto(String nameIn, String productClassIn, Integer consumedIn,
            Date startDateIn, Date endDateIn) {
        this.productClass = productClassIn;
        this.consumed = consumedIn;
        this.name = nameIn;
        this.startDate = startDateIn;
        this.endDate = endDateIn;
    }

    /**
     * Constructor
     */
    public SubscriptionDto() { }

    /**
     * Set consumed.
     * @param consumedIn consumed
     */
    public void setConsumed(Integer consumedIn) {
        this.consumed = consumedIn;
    }

    /**
     * Get consumed.
     * @return number of consumed
     */
    public Integer getConsumed() {
        return consumed;
    }

    /**
     * Set product class.
     * @param productClassIn the product class
     */
    public void setProductClass(String productClassIn) {
        this.productClass = productClassIn;
    }

    /**
     * Get product class.
     * @return the product class
     */
    public String getProductClass() {
        return productClass;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }
    /**
     * @param nameIn the name to set
     */
    public void setName(String nameIn) {
        this.name = nameIn;
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
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        SubscriptionDto other = (SubscriptionDto) obj;
        if (endDate == null) {
            if (other.endDate != null) {
                return false;
            }
        }
        else if (!endDate.equals(other.endDate)) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        }
        else if (!name.equals(other.name)) {
            return false;
        }
        if (startDate == null) {
            if (other.startDate != null) {
                return false;
            }
        }
        else if (!startDate.equals(other.startDate)) {
            return false;
        }
        return true;
    }
}
