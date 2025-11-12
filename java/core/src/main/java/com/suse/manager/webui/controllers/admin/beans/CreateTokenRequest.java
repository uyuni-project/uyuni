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

package com.suse.manager.webui.controllers.admin.beans;

import com.suse.manager.model.hub.TokenType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class CreateTokenRequest {

    private TokenType type;

    private String fqdn;

    private String token;

    public TokenType getType() {
        return type;
    }

    public void setType(TokenType typeIn) {
        this.type = typeIn;
    }

    public String getFqdn() {
        return fqdn;
    }

    public void setFqdn(String fqdnIn) {
        this.fqdn = fqdnIn;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String tokenIn) {
        this.token = tokenIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof CreateTokenRequest that)) {
            return false;
        }

        return new EqualsBuilder()
            .append(getType(), that.getType())
            .append(getFqdn(), that.getFqdn())
            .append(getToken(), that.getToken())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getType())
            .append(getFqdn())
            .append(getToken())
            .toHashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CreateTokenRequest{");
        sb.append("type=").append(type);
        sb.append(", fqdn='").append(fqdn).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
