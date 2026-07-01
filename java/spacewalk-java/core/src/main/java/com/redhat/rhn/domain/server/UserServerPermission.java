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
import com.redhat.rhn.domain.user.legacy.UserImpl;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.StringJoiner;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "rhnUserServerPerms")
@IdClass(UserServerPermissionId.class)
public class UserServerPermission {

    @Id
    @ManyToOne(targetEntity = UserImpl.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "server_id")
    private Server server;

    protected UserServerPermission() {
        // Constructor for Hibernate
    }

    public User getUser() {
        return user;
    }

    protected void setUser(User userIn) {
        this.user = userIn;
    }

    public Server getServer() {
        return server;
    }

    protected void setServer(Server serverIn) {
        this.server = serverIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof UserServerPermission that)) {
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
        return new StringJoiner(", ", UserServerPermission.class.getSimpleName() + "[", "]")
                .add("user=" + user)
                .add("server=" + server)
                .toString();
    }
}
