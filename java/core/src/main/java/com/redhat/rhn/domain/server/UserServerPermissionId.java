/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.domain.server;

import com.redhat.rhn.domain.user.User;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.StringJoiner;

public class UserServerPermissionId {

    private User user;

    private Server server;

    public User getUser() {
        return user;
    }

    public void setUser(User userIn) {
        this.user = userIn;
    }

    public Server getServer() {
        return server;
    }

    public void setServer(Server serverIn) {
        this.server = serverIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof UserServerPermissionId that)) {
            return false;
        }

        return new EqualsBuilder()
                .append(getUser(), that.getUser())
                .append(getServer(), that.getServer())
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(getUser())
                .append(getServer())
                .toHashCode();
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", UserServerPermissionId.class.getSimpleName() + "[", "]")
                .add("user=" + user)
                .add("server=" + server)
                .toString();
    }
}
