/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.manager.model.hub;

import java.util.Objects;

public class SCCCredentialsJson {

    private String username;

    private String password;

    /**
     * Default constructor
     */
    public SCCCredentialsJson() {
        this(null, null);
    }

    /**
     * Builds a request instance
     * @param usernameIn the username
     * @param passwordIn the password
     */
    public SCCCredentialsJson(String usernameIn, String passwordIn) {
        this.username = usernameIn;
        this.password = passwordIn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String usernameIn) {
        this.username = usernameIn;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String passwordIn) {
        this.password = passwordIn;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof SCCCredentialsJson that)) {
            return false;
        }
        return Objects.equals(getUsername(), that.getUsername()) && Objects.equals(getPassword(),
            that.getPassword());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUsername(), getPassword());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SCCCredentialsJson{");
        sb.append("username='").append(username).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
