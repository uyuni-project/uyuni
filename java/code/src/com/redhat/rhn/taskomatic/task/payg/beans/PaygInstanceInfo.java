/*
 * Copyright (c) 2021 SUSE LLC
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

package com.redhat.rhn.taskomatic.task.payg.beans;

import com.google.gson.annotations.SerializedName;

import java.util.List;
import java.util.Map;

/**
 * Class representing the payg information retrieved from the instance by the python script
 */
public class PaygInstanceInfo {
    private List<PaygProductInfo> products;
    @SerializedName("basic_auth")
    private Map<String, String> basicAuth;
    @SerializedName("header_auth")
    private String headerAuth;
    @SerializedName("rmt_host")
    private Map<String, String> rmtHost;

    /**
     * Constructor with all parameters
     * @param productsIn
     * @param basicAuthIn
     * @param headerAuthIn
     * @param rmtHostIn
     */
    public PaygInstanceInfo(List<PaygProductInfo> productsIn,
                            Map<String, String> basicAuthIn,
                            String headerAuthIn, Map<String, String> rmtHostIn) {
        this.products = productsIn;
        this.basicAuth = basicAuthIn;
        this.headerAuth = headerAuthIn;
        this.rmtHost = rmtHostIn;
    }

    public List<PaygProductInfo> getProducts() {
        return products;
    }

    public void setProducts(List<PaygProductInfo> productsIn) {
        this.products = productsIn;
    }

    public Map<String, String> getBasicAuth() {
        return basicAuth;
    }

    public void setBasicAuth(Map<String, String> basicAuthIn) {
        this.basicAuth = basicAuthIn;
    }

    public String getHeaderAuth() {
        return headerAuth;
    }

    public void setHeaderAuth(String headerAuthIn) {
        this.headerAuth = headerAuthIn;
    }

    public Map<String, String> getRmtHost() {
        return rmtHost;
    }

    public void setRmtHost(Map<String, String> rmtHostIn) {
        this.rmtHost = rmtHostIn;
    }
}
