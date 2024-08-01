/*
 * Copyright (c) 2022 SUSE LLC
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

/**
 * This is a System Item send to SCC for minimal update registration.
 */
public class SCCUpdateSystemJson {

    private String login;
    private String password;
    @SerializedName("last_seen_at")
    private Date lastSeenAt;
    @SerializedName("online_at")
    private String onlineAt;

    /**
     * Constructor
     * @param loginIn the login
     * @param passwdIn the password
     * @param lastSeenIn last seen date
     * @param onlineAtIn the system online data
     */
    public SCCUpdateSystemJson(String loginIn, String passwdIn, Date lastSeenIn,
            String onlineAtIn) {
        login = loginIn;
        password = passwdIn;
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
     * @return Returns lastSeenAt.
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
