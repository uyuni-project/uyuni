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

    private String token;

    private String rootCA;

    /**
     * Default constructor
     */
    public RegisterJson() {
        this(null, null);
    }

    /**
     * Builds a request instance
     * @param tokenIn the token
     * @param rootCAIn the root certificate
     */
    public RegisterJson(String tokenIn, String rootCAIn) {
        this.token = tokenIn;
        this.rootCA = rootCAIn;
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
        return Objects.equals(getToken(), that.getToken()) &&
                    Objects.equals(getRootCA(), that.getRootCA());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getToken(), getRootCA());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("RegisterJson{");
        sb.append("token='").append(token).append('\'');
        sb.append(", rootCA='").append(rootCA).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
