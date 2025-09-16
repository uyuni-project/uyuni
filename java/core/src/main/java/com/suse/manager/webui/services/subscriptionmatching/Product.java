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

import java.util.Set;

/**
 * Backing data for the Subscription Matching UI. Representation of Product.
 */
public class Product {

    private Long id;
    private String productName;
    private Integer unmatchedSystemCount;
    private Set<Long> unmatchedSystemIds;

    /**
     * Standard constructor
     *
     * @param idIn - product id
     * @param productNameIn - unmatched product name
     * @param unmatchedSystemIdsIn - ids of systems with unmatched product
     */
    public Product(Long idIn, String productNameIn, Set<Long> unmatchedSystemIdsIn) {
        id = idIn;
        productName = productNameIn;
        unmatchedSystemCount = unmatchedSystemIdsIn.size();
        unmatchedSystemIds = unmatchedSystemIdsIn;
    }

    /**
     * Gets the id.
     *
     * @return id
     */
    public Long getId() {
        return id;
    }

    /**
     * Sets the id.
     *
     * @param idIn - the id
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * Gets the productName.
     *
     * @return productName
     */
    public String getProductName() {
        return productName;
    }

    /**
     * Sets the productName.
     *
     * @param productNameIn - the productName
     */
    public void setProductName(String productNameIn) {
        productName = productNameIn;
    }

    /**
     * Gets the unmatchedSystemCount.
     *
     * @return unmatchedSystemCount
     */
    public Integer getUnmatchedSystemCount() {
        return unmatchedSystemCount;
    }

    /**
     * Sets the unmatchedSystemCount.
     *
     * @param unmatchedSystemCountIn - the unmatchedSystemCount
     */
    public void setUnmatchedSystemCount(Integer unmatchedSystemCountIn) {
        unmatchedSystemCount = unmatchedSystemCountIn;
    }

    /**
     * Gets the unmatchedSystemIds.
     *
     * @return unmatchedSystemIds
     */
    public Set<Long> getUnmatchedSystemIds() {
        return unmatchedSystemIds;
    }

    /**
     * Sets the unmatchedSystemIds.
     *
     * @param unmatchedSystemIdsIn - the unmatchedSystemIds
     */
    public void setUnmatchedSystemIds(Set<Long> unmatchedSystemIdsIn) {
        unmatchedSystemIds = unmatchedSystemIdsIn;
    }
}
