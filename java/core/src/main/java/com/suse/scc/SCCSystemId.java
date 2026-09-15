/*
 * Copyright (c) 2023 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.scc;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * This is a custom key class that represents the combination of login and password which identifies a system in SCC.
 */
public class SCCSystemId {

    private final String login;
    private final String password;

    /**
     * Constructor
     *
     * @param loginIn
     * @param passwordIn
     */
    public SCCSystemId(String loginIn, String passwordIn) {
        login = loginIn;
        password = passwordIn;
    }

    public String getLogin() {
        return login;
    }

    public String getPassword() {
        return password;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SCCSystemId that = (SCCSystemId) o;

        return new EqualsBuilder()
                .append(getLogin(), that.getLogin())
                .append(getPassword(), that.getPassword())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getLogin())
                .append(getPassword())
                .toHashCode();
    }
}
