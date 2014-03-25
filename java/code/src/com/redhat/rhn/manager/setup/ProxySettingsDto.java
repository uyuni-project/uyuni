/**
 * Copyright (c) 2014 SUSE
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

/**
 *
 * To carry proxy settings to the wizard UI
 */
public class ProxySettingsDto {

    private String hostname;
    private String username;
    private String password;

    public String getHostname() {
        return hostname;
    }

    /**
     * Set the proxy settings hostname
     * @param hostname the hostname
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * @return the proxy settings hostname
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the proxy settings username
     * @param username proxy username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return proxy settings password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the proxy settings password
     * @param password The password
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
