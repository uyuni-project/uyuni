/*
 * Copyright (c) 2024 SUSE LLC
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
    private UserImpl userId;

    @ManyToOne
    @JoinColumn(name = "user_group_id", insertable = false, updatable = false)
    private UserGroupImpl userGroupId;

    @Column(name = "temporary", insertable = false, updatable = false)
    @Type(type = "yes_no")
    private boolean temporary;

    /**
     * default constructor
     */
    public UserGroupMembersId() { }

    /**
     * constructor
     * @param userIn user
     * @param userGroupIn user group
     * @param temporaryIn true if it's a temporary group member
     *
     */
    public UserGroupMembersId(UserImpl userIn, UserGroupImpl userGroupIn, boolean temporaryIn) {
        this.userId = userIn;
        this.userGroupId = userGroupIn;
        this.setTemporary(temporaryIn);
    }

    // Getters and setters
    public UserImpl getUserId() {
        return userId;
    }

    public void setUserId(UserImpl user) {
        this.userId = user;
    }

    public UserGroupImpl getUserGroupId() {
        return userGroupId;
    }

    public void setUserGroupId(UserGroupImpl userGroup) {
        this.userGroupId = userGroup;
    }

    @Type(type = "yes_no")
    public boolean isTemporary() {
        return temporary;
    }

    @Type(type = "yes_no")
    public void setTemporary(boolean temporaryIn) {
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
                userId.equals(that.userId) &&
                userGroupId.equals(that.userGroupId);
    }

    @Override
    public int hashCode() {
            return new HashCodeBuilder()
                    .append(this.getUserId())
                    .append(this.getUserGroupId())
                    .append(this.isTemporary())
                    .toHashCode();
    }
}
