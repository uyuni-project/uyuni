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

import java.util.List;
import java.util.Set;

/**
 * Represents a system in the Subscription Matcher UI.
 */
public class System {

    /** The id. */
    private Long id;

    /** The name. */
    private String name;

    /** The cpu count. */
    private Integer cpuCount;

    /** The product ids. */
    private Set<Long> productIds;

    /** The system type. */
    private String type;

    /** The possible subscription ids. */
    private List<Long> possibleSubscriptionIds;

    /**
     * Standard constructor.
     *
     * @param idIn - id
     * @param nameIn - name
     * @param cpuCountIn - cpu count
     * @param productIdsIn - product ids
     * @param typeIn - type
     * @param possibleSubscriptionIdsIn - possible subscription ids
     */
    public System(Long idIn, String nameIn, Integer cpuCountIn, Set<Long> productIdsIn,
            String typeIn, List<Long> possibleSubscriptionIdsIn) {
        id = idIn;
        name = nameIn;
        cpuCount = cpuCountIn;
        productIds = productIdsIn;
        type = typeIn;
        setPossibleSubscriptionIds(possibleSubscriptionIdsIn);
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
     * Gets the name.
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     *
     * @param nameIn - the name
     */
    public void setName(String nameIn) {
        name = nameIn;
    }

    /**
     * Gets the cpuCount.
     *
     * @return cpuCount
     */
    public Integer getCpuCount() {
        return cpuCount;
    }

    /**
     * Sets the cpuCount.
     *
     * @param cpuCountIn - the cpuCount
     */
    public void setCpuCount(Integer cpuCountIn) {
        cpuCount = cpuCountIn;
    }

    /**
     * Gets the product ids.
     *
     * @return the product ids
     */
    public Set<Long> getProductIds() {
        return productIds;
    }

    /**
     * Sets the product ids.
     *
     * @param productIdsIn the new product ids
     */
    public void setProductIds(Set<Long> productIdsIn) {
        productIds = productIdsIn;
    }

    /**
     * Gets the system type.
     *
     * @return the system type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the system type.
     *
     * @param typeIn the new system type
     */
    public void setType(String typeIn) {
        type = typeIn;
    }

    /**
     * Gets the possible subscription ids.
     *
     * @return the possible subscription ids
     */
    public List<Long> getPossibleSubscriptionIds() {
        return possibleSubscriptionIds;
    }

    /**
     * Sets the possible subscription ids.
     *
     * @param possibleSubscriptionIdsIn the new possible subscription ids
     */
    public void setPossibleSubscriptionIds(List<Long> possibleSubscriptionIdsIn) {
        possibleSubscriptionIds = possibleSubscriptionIdsIn;
    }
}
