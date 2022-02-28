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

import java.util.Objects;

/**
 * To carry proxy settings to the wizard UI.
 */
public class ProxySettingsDto {

    /** The hostname. */
    private String hostname;

    /** The username. */
    private String username;

    /** The password. */
    private String password;

    /**
     * Gets the hostname.
     * @return the hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * Set the proxy settings hostname.
     *
     * @param hostnameIn the hostname
     */
    public void setHostname(String hostnameIn) {
        this.hostname = hostnameIn != null ? hostnameIn : "";
    }

    /**
     * Gets the username.
     *
     * @return the proxy settings username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set the proxy settings username.
     *
     * @param usernameIn proxy username
     */
    public void setUsername(String usernameIn) {
        this.username = usernameIn != null ? usernameIn : "";
    }

    /**
     * Gets the password.
     *
     * @return proxy settings password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Set the proxy settings password.
     *
     * @param passwordIn The password
     */
    public void setPassword(String passwordIn) {
        this.password = passwordIn != null ? passwordIn : "";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + (this.hostname != null ? this.hostname.hashCode() : 0);
        hash = 53 * hash + (this.username != null ? this.username.hashCode() : 0);
        hash = 53 * hash + (this.password != null ? this.password.hashCode() : 0);
        return hash;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final ProxySettingsDto other = (ProxySettingsDto) obj;
        if (!Objects.equals(this.hostname, other.hostname)) {
            return false;
        }
        if (!Objects.equals(this.username, other.username)) {
            return false;
        }
        if (!Objects.equals(this.password, other.password)) {
            return false;
        }
        return true;
    }
}
