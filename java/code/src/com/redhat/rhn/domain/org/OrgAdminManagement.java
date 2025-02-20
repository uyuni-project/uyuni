/*
 * Copyright (c) 2013--2015 Red Hat, Inc.
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

package com.redhat.rhn.domain.org;

import com.redhat.rhn.domain.BaseDomainHelper;

import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.MapsId;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * OrgAdminManagement
 */
@Entity
@Table(name = "rhnOrgAdminManagement")
public class OrgAdminManagement extends BaseDomainHelper {

    @Id
    @Column(name = "org_id", nullable = false)
    private Long orgId;

    @OneToOne
    @MapsId // Maps the primary key of OrgAdminManagement to the primary key of Org
    @JoinColumn(name = "org_id") // The foreign key column
    private Org org;

    @Column(name = "enabled", nullable = false)
    @Type(type = "yes_no")
    private boolean enabled;

    /**
     * Gets the current value of org
     * @return Returns the value of org
     */
    public Org getOrg() {
        return org;
    }

    /**
     * Sets the value of org to new value
     * @param orgIn New value for org
     */
    public void setOrg(Org orgIn) {
        org = orgIn;
    }

    /**
     * @return Returns the orgId.
     */
    public Long getOrgId() {
        return orgId;
    }

    /**
     * @param orgIdIn The orgId to set.
     */
    public void setOrgId(Long orgIdIn) {
        this.orgId = orgIdIn;
    }

    /**
     * @return Returns the enabled.
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * @param enabledIn The enabled to set.
     */
    public void setEnabled(boolean enabledIn) {
        this.enabled = enabledIn;
    }
}
