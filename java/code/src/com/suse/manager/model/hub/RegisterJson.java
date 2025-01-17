/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.model.hub;

import java.util.Objects;

public class RegisterJson {

    private IssRole role;

    private String token;

    private String rootCA;

    /**
     * Default constructor
     */
    public RegisterJson() {
        this(IssRole.HUB, null, null);
    }

    /**
     * Builds a request instance
     * @param roleIn the remote server role
     * @param tokenIn the token
     * @param rootCAIn the root certificate
     */
    public RegisterJson(IssRole roleIn, String tokenIn, String rootCAIn) {
        this.role = roleIn;
        this.token = tokenIn;
        this.rootCA = rootCAIn;
    }

    public IssRole getRole() {
        return role;
    }

    public void setRole(IssRole roleIn) {
        this.role = roleIn;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String tokenIn) {
        this.token = tokenIn;
    }

    public String getRootCA() {
        return rootCA;
    }

    public void setRootCA(String rootCAIn) {
        this.rootCA = rootCAIn;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof RegisterJson that)) {
            return false;
        }
        return Objects.equals(getRole(), that.getRole()) &&
                    Objects.equals(getToken(), that.getToken()) &&
                    Objects.equals(getRootCA(), that.getRootCA());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRole(), getToken(), getRootCA());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RegisterJson{");
        sb.append("role='").append(role).append('\'');
        sb.append(", token='").append(token).append('\'');
        sb.append(", rootCA='").append(rootCA).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
