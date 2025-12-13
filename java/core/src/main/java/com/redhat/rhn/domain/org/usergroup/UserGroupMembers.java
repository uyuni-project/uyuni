/*
 * Copyright (c) 2014 Red Hat, Inc.
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

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.user.legacy.UserImpl;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

/**
 * UserGroupMembers
 */
@Entity
@Table(name = "rhnUserGroupMembers", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "user_group_id"})
})
public class UserGroupMembers  extends BaseDomainHelper {
    @EmbeddedId
    private UserGroupMembersId id;
    /**
     * Default Constructor
     */
    public UserGroupMembers() {
        this.id = new UserGroupMembersId();
    }

    /**
     * Constructor with parameters
     * @param userIn The user
     * @param userGroupIn The user group
     * @param temporaryIn temporary
     */
    public UserGroupMembers(UserImpl userIn, UserGroupImpl userGroupIn, Boolean temporaryIn) {
        this.id = new UserGroupMembersId(userIn, userGroupIn, temporaryIn);
    }

    /**
     * @return Returns the user.
     */
    public UserImpl getUser() {
        return this.getId().getUser();
    }

    /**
     * @param userIn The user to set.
     */
    public void setUser(UserImpl userIn) {
        this.getId().setUser(userIn);
    }

    /**
     * @return Returns the userGroup.
     */
    public UserGroupImpl getUserGroup() {
        return this.getId().getUserGroup();
    }

    /**
     * @param userGroupIn The userGroup to set.
     */
    public void setUserGroup(UserGroupImpl userGroupIn) {
        this.getId().setUserGroup(userGroupIn);
    }

    /**
     * @return Returns the temporary.
     */
    @Type(type = "yes_no")
    public boolean isTemporary() {
        return this.getId().isTemporary();
    }

    /**
     * @param temporaryIn The temporary to set.
     */
    @Type(type = "yes_no")
    public void setTemporary(boolean temporaryIn) {
        this.getId().setTemporary(temporaryIn);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof UserGroupMembers other)) {
            return false;
        }
        return new EqualsBuilder()
                .append(this.getUser(), other.getUser())
                .append(this.getUserGroup(), other.getUserGroup())
                .append(this.isTemporary(), other.isTemporary())
                .append(this.getId(), other.getId())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(this.getUser())
                .append(this.getUserGroup())
                .append(this.isTemporary())
                .append(this.getId())
                .toHashCode();
    }

    public UserGroupMembersId getId() {
        return id;
    }

    public void setId(UserGroupMembersId idIn) {
        id = idIn;
    }
}
