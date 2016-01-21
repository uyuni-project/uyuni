/**
 * Copyright (c) 2016 SUSE LLC
 * <p>
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 * <p>
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.manager.webui.services.subscriptionmatching;

import java.util.List;

/**
 * Created by fkobzik-local on 1/21/16.
 * todo maybe rename to UnmatchedSystem
 * todo implement comparable + sort it in the usage
 */
public class System {

    private String name;
    private Integer cpuCount;
    private List<String> products;

    /**
     * Standard constructor.
     *
     * @param nameIn
     * @param cpuCountIn
     * @param productsIn
     */
    public System(String nameIn, Integer cpuCountIn, List<String> productsIn) {
        name = nameIn;
        cpuCount = cpuCountIn;
        products = productsIn;
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
     * Gets the products.
     *
     * @return products
     */
    public List<String> getProducts() {
        return products;
    }

    /**
     * Sets the products.
     *
     * @param productsIn - the products
     */
    public void setProducts(List<String> productsIn) {
        products = productsIn;
    }
}
