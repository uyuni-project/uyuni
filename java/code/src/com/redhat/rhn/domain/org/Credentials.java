/**
 * Copyright (c) 2012 Novell
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

package com.redhat.rhn.domain.org;

import com.redhat.rhn.domain.BaseDomainHelper;

/**
 * Credentials - Java representation of the table SUSECREDENTIALS.
 *
 * This table contains pairs of credentials used for communicating
 * with 3rd party systems, e.g. API usernames and keys.
 */
public class Credentials extends BaseDomainHelper {

    public static String TYPE_STUDIO = "STUDIO";

    private Long id;
    private Org org;
    private String type;
    private String hostname;
    private String username;
    private String password;

    /**
     * Get the ID of this object.
     * @return ID
     */
    public Long getId() {
        return this.id;
    }

    /**
     * Set the ID of this object.
     * @param id
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Get this object's Organization.
     * @return Organization associated with this pair of credentials
     */
    public Org getOrg() {
        return this.org;
    }

    /**
     * Set the Org on this object.
     * @param org the Org we want to set as the parent of this object
     */
    public void setOrg(Org org) {
        this.org = org;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * @return the hostname
     */
    public String getHostname() {
        return hostname;
    }

    /**
     * @param hostname the hostname to set
     */
    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    /**
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }
}
