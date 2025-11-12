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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class UpdateRootCARequest {

    private String rootCA;

    public String getRootCA() {
        return rootCA;
    }

    public void setRootCA(String rootCAIn) {
        this.rootCA = rootCAIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof UpdateRootCARequest that)) {
            return false;
        }

        return new EqualsBuilder()
            .append(getRootCA(), that.getRootCA())
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(getRootCA())
            .toHashCode();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UpdateRootCARequest{");
        sb.append("rootCA='").append(rootCA).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
