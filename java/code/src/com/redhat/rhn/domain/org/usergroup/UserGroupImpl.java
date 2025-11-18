/*
 * Copyright (c) 2025 SUSE LLC
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
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.role.Role;
import com.redhat.rhn.domain.role.RoleImpl;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * Class UserGroup that reflects the DB representation of RHNUSERGROUP
 * DB table: RHNUSERGROUP
 */
@Entity
@Table(name = "RHNUSERGROUP")
public class UserGroupImpl extends BaseDomainHelper implements UserGroup {

    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rhn_user_group_id_seq")
	@SequenceGenerator(name = "rhn_user_group_id_seq", sequenceName = "rhn_user_group_id_seq", allocationSize = 1)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 64)
    private String name;

    @Column(name = "description", length = 1024)
    private String description;

    @Column(name = "current_members", insertable = false, updatable = true)
    private Long currentMembers;

    @ManyToOne
    @JoinColumn(name = "org_id", nullable = false)
    private Org org;

    @ManyToOne
    @JoinColumn(name = "group_type", nullable = false)
    private RoleImpl role;

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
    public RoleImpl getRole() {
        return role;
    }

    /**
     * Setter for groupType
     * {@inheritDoc}
     */
    @Override
    public void setRole(Role roleIn) {
        if (roleIn instanceof RoleImpl roleImpl) {
            role = roleImpl;
        }
        else {
            role = null;
        }
    }

    /**
     * Getter for org
     * {@inheritDoc}
     */
    public Org getOrg() {
        return this.org;
    }

    /**
     * Setter for orgId
     * @param orgIn the org
     */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
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
                " desc: " + description + " orgid: " + (org != null ? org.getId() : "null");
    }
}
