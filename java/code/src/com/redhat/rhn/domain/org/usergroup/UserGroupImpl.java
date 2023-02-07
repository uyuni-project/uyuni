/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
import com.redhat.rhn.domain.role.Role;

/**
 * Class UserGroup that reflects the DB representation of RHNUSERGROUP
 * DB table: RHNUSERGROUP
 */
public class UserGroupImpl extends BaseDomainHelper implements UserGroup {

    private Long id;
    private String name;
    private String description;
    private Long currentMembers;
    private Long orgId;
    private Role role;

    /**
     * Getter for id
     * {@inheritDoc}
     */
    @Override
    public Long getId() {
        return this.id;
    }

    /**
     * Setter for id
     * {@inheritDoc}
     */
    @Override
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Getter for name
     * {@inheritDoc}
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Setter for name
     * {@inheritDoc}
     */
    @Override
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * Getter for description
     * {@inheritDoc}
     */
    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * Setter for description
     * {@inheritDoc}
     */
    @Override
    public void setDescription(String descriptionIn) {
        this.description = descriptionIn;
    }

    /**
     * Getter for currentMembers
     * {@inheritDoc}
     */
    @Override
    public Long getCurrentMembers() {
        return this.currentMembers;
    }

    /**
     * Setter for currentMembers
     * {@inheritDoc}
     */
    @Override
    public void setCurrentMembers(Long currentMembersIn) {
        this.currentMembers = currentMembersIn;
    }

    /**
     * Getter for groupType
     * {@inheritDoc}
     */
    @Override
    public Role getRole() {
        return role;
    }

    /**
     * Setter for groupType
     * {@inheritDoc}
     */
    @Override
    public void setRole(Role roleIn) {
        role = roleIn;
    }

    /**
     * Getter for orgId
     * {@inheritDoc}
     */
    @Override
    public Long getOrgId() {
        return this.orgId;
    }

    /**
     * Setter for orgId
     * {@inheritDoc}
     */
    @Override
    public void setOrgId(Long orgIdIn) {
        this.orgId = orgIdIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAssociatedRole(Role rin) {
        return (rin.equals(role));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "ID: " + id + " name: " + name +
                  " desc: " + description + " orgid: " + orgId;
    }
}
