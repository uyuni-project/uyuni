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
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.org.usergroup;

import com.redhat.rhn.domain.user.legacy.UserImpl;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
@Embeddable
public class UserGroupMembersId implements Serializable {

    @ManyToOne
    @JoinColumn(name = "user_id", insertable = false, updatable = false)
    private UserImpl user;

    @ManyToOne
    @JoinColumn(name = "user_group_id", insertable = false, updatable = false)
    private UserGroupImpl userGroup;

    @Column(name = "temporary", insertable = false, updatable = false)
    @Type(type = "yes_no")
    private boolean temporary;

    /**
     * default constructor
     */
    public UserGroupMembersId() {
        this.user = new UserImpl();
        this.userGroup = new UserGroupImpl();
        this.setTemporary(false);
    }

    /**
     * constructor
     * @param userIn user
     * @param userGroupIn user group
     * @param temporaryIn true if it's a temporary group member
     *
     */
    public UserGroupMembersId(UserImpl userIn, UserGroupImpl userGroupIn, Boolean temporaryIn) {
        this.user = userIn;
        this.userGroup = userGroupIn;
        this.setTemporary(temporaryIn);
    }

    // Getters and setters
    public UserImpl getUser() {
        return user;
    }

    public void setUser(UserImpl userIn) {
        this.user = userIn;
    }

    public UserGroupImpl getUserGroup() {
        return userGroup;
    }

    public void setUserGroup(UserGroupImpl userGroupIn) {
        this.userGroup = userGroupIn;
    }

    public Boolean isTemporary() {
        return temporary;
    }

    public void setTemporary(Boolean temporaryIn) {
        this.temporary = temporaryIn;
    }

    // Override equals and hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        UserGroupMembersId that = (UserGroupMembersId) o;
        return temporary == that.temporary &&
                user.equals(that.user) &&
                userGroup.equals(that.userGroup);
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.getUser())
                .append(this.getUserGroup())
                .append(this.isTemporary())
                .toHashCode();
    }

    @Override
    public String toString() {
        return "UserGroupMembersId{" +
                "user=" + user +
                ", userGroup=" + userGroup +
                ", temporary=" + temporary +
                '}';
    }
}
