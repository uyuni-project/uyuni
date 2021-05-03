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

/**
 * SCCSystemCredentialsJson
 */
public class SCCSystemCredentialsJson {

    private String login;
    private String password;
    private Long id;

    /**
     * Constructor
     * @param loginIn the login
     * @param passwordIn the password
     * @param idIn the scc ID
     */
    public SCCSystemCredentialsJson(String loginIn, String passwordIn, Long idIn) {
       this.login = loginIn;
       this.password = passwordIn;
       this.id = idIn;
    }

    /**
     * @return return the login
     */
    public String getLogin() {
        return login;
    }

    /**
     * @return return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * @return return the scc ID
     */
    public Long getId() {
        return id;
    }
}
