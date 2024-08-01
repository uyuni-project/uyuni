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
package com.suse.scc.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

/**
 * This is a System Item send to SCC for registration.
 */
public class SCCRegisterSystemJson {

    private String login;
    private String password;
    private String hostname;
    private SCCHwInfoJson hwinfo;
    private List<SCCMinProductJson> products;
    private List<String> regcodes = new LinkedList<>();
    @SerializedName("last_seen_at")
    private Date lastSeenAt;
    @SerializedName("online_at")
    private String onlineAt;

    /**
     * Constructor
     * @param loginIn the login
     * @param passwdIn the password
     * @param hostnameIn the hostname
     * @param hwinfoIn the hardware data
     * @param productsIn the products
     * @param lastSeenIn the last seen date
     * @param onlineAtIn the system online data
     */
    public SCCRegisterSystemJson(String loginIn, String passwdIn, String hostnameIn,
            SCCHwInfoJson hwinfoIn, List<SCCMinProductJson> productsIn, Date lastSeenIn,
            String onlineAtIn) {
        login = loginIn;
        password = passwdIn;
        hostname = hostnameIn;
        hwinfo = hwinfoIn;
        products = productsIn;
        lastSeenAt = lastSeenIn;
        onlineAt = onlineAtIn;
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
    public SCCHwInfoJson getHwinfo() {
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
    /**
     * @return Returns the last seen date.
     */
    public Date getLastSeenAt() {
        return lastSeenAt;
    }
    /**
     * @return Returns the system online data.
     */
    public String getOnlineAt() {
        return onlineAt;
    }
}
