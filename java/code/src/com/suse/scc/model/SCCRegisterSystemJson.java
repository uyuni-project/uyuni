/**
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
package com.suse.scc.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * This is a System Item send to SCC for registration.
 */
public class SCCRegisterSystemJson {

    private String login;
    private String password;
    private String hostname;
    private Map<String, String> hwinfo;
    private List<SCCMinProductJson> products;
    private List<String> regcodes = new LinkedList<>();

    /**
     * Constructor
     *
     * @param hostnameIn the hostname
     * @param hwinfoIn the hardware data
     * @param productsIn the products
     */
    public SCCRegisterSystemJson(String loginIn, String passwdIn, String hostnameIn,
            Map<String, String> hwinfoIn, List<SCCMinProductJson> productsIn) {
        login = loginIn;
        password = passwdIn;
        hostname = hostnameIn;
        hwinfo = hwinfoIn;
        products = productsIn;
    }

    /**
     * @return Returns the login.
     */
    public String getLogin() {
        return login;
    }

    /**
     * @return Returns the password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return Returns the hostname.
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @return Returns the hwinfo.
     */
    public Map<String, String> getHwinfo() {
        return hwinfo;
    }

    /**
     * @return Returns the products.
     */
    public List<SCCMinProductJson> getProducts() {
        return products;
    }

    /**
     * @return Returns the regcodes.
     */
    public List<String> getRegcodes() {
        return regcodes;
    }
}
