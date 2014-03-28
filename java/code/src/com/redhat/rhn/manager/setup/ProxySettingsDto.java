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
        this.hostname = hostname != null ? hostname : "";
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
        this.username = username != null ? username : "";
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
        this.password = password != null ? password : "";
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.hostname != null ? this.hostname.hashCode() : 0);
        hash = 53 * hash + (this.username != null ? this.username.hashCode() : 0);
        hash = 53 * hash + (this.password != null ? this.password.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProxySettingsDto other = (ProxySettingsDto) obj;
        if ((this.hostname == null) ? (other.hostname != null) : !this.hostname.equals(other.hostname)) {
            return false;
        }
        if ((this.username == null) ? (other.username != null) : !this.username.equals(other.username)) {
            return false;
        }
        if ((this.password == null) ? (other.password != null) : !this.password.equals(other.password)) {
            return false;
        }
        return true;
    }
}
