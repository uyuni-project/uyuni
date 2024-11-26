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

import com.redhat.rhn.domain.user.legacy.UserImpl;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;


/**
 * UserGroupMembers
 */
@Entity
@Table(name = "rhnUserGroupMembers", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "user_group_id"})
})
public class UserGroupMembers implements Serializable {
    @EmbeddedId
    private UserGroupMembersId id;

    @Column(name = "user_id", insertable = false)
    private UserImpl user;

    @Column(name = "user_group_id", insertable = false)
    private UserGroupImpl userGroup;

    @Column(name = "temporary", insertable = false, updatable = false, nullable = false)
    @Convert(converter = org.hibernate.type.YesNoConverter.class)
    private boolean temporary = false; // default value to avoid nulls

    /**
     * Constructor
     */
    public UserGroupMembers() {
        temporary = false;
    }

    /**
     * Constructor
     * @param userIn user
     * @param ugIn user group
     */
    public UserGroupMembers(UserImpl userIn, UserGroupImpl ugIn) {
        id = new UserGroupMembersId(userIn, ugIn, false);
        user = userIn;
        userGroup = ugIn;
        this.setTemporary(false);
    }

    /**
     * Constructor
     * @param userIn user
     * @param ugIn user group
     * @param tempIn temporary flag
     */
    public UserGroupMembers(UserImpl userIn, UserGroupImpl ugIn, boolean tempIn) {
        id = new UserGroupMembersId(userIn, ugIn, tempIn);
        user = userIn;
        userGroup = ugIn;
        this.setTemporary(temporary);
    }

    /**
     * @return Returns the user.
     */
    public UserImpl getUser() {
        return user;
    }

    /**
     * @param userIn The user to set.
     */
    public void setUser(UserImpl userIn) {
        user = userIn;
    }

    /**
     * @return Returns the userGroup.
     */
    public UserGroupImpl getUserGroup() {
        return userGroup;
    }

    /**
     * @param userGroupIn The userGroup to set.
     */
    public void setUserGroup(UserGroupImpl userGroupIn) {
        userGroup = userGroupIn;
    }

    /**
     * @return Returns the temporary.
     */
    @Convert(converter = org.hibernate.type.YesNoConverter.class)
    public boolean isTemporary() {
        return temporary;
    }

    /**
     * @param temporaryIn The temporary to set.
     */
    @Convert(converter = org.hibernate.type.YesNoConverter.class)
    public void setTemporary(boolean temporaryIn) {
        temporary = temporaryIn;
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
            .toHashCode();
    }

    public UserGroupMembersId getId() {
        return id;
    }

    public void setId(UserGroupMembersId idIn) {
        id = idIn;
    }

}
