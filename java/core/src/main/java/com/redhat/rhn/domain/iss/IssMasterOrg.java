/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2013 Red Hat, Inc.
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
package com.redhat.rhn.domain.iss;

import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.frontend.dto.BaseDto;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;


/**
 * IssMasterOrg - Class representation of the table rhnissmasterorgs.
 */
@Entity
@Table(name = "rhnIssMasterOrgs")
public class IssMasterOrg extends BaseDto {

    public static final Long NO_MAP_ID = -1L;

    @Id
    @Column(name = "ID")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "issmasterorgs_seq")
    @SequenceGenerator(name = "issmasterorgs_seq", sequenceName = "rhn_issmasterorgs_seq", allocationSize = 1)
    private Long id;

    @Column(name = "master_org_id")
    private Long masterOrgId;

    @Column(name = "master_org_name")
    private String masterOrgName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "local_org_id")
    private Org localOrg;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "master_id", nullable = false)
    private IssMaster master;

    /**
     * Getter for id
     * @return Long to get
    */
    @Override
    public Long getId() {
        return this.id;
    }

    /**
     * Setter for id
     * @param idIn to set
    */
    protected void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Getter for masterOrgId
     * @return Long to get
    */
    public Long getMasterOrgId() {
        return this.masterOrgId;
    }

    /**
     * Setter for masterOrgId
     * @param masterOrgIdIn to set
    */
    public void setMasterOrgId(Long masterOrgIdIn) {
        this.masterOrgId = masterOrgIdIn;
    }

    /**
     * Getter for masterOrgName
     * @return String to get
    */
    public String getMasterOrgName() {
        return this.masterOrgName;
    }

    /**
     * Setter for masterOrgName
     * @param masterOrgNameIn to set
    */
    public void setMasterOrgName(String masterOrgNameIn) {
        this.masterOrgName = masterOrgNameIn;
    }

    /**
     *
     * @return local org associated with this master-org
     */
    public Org getLocalOrg() {
        return this.localOrg;
    }

    /**
     * Map a local org to a specific master org
     * @param localOrgIn local org to be associated with this master org
     */
    public void setLocalOrg(Org localOrgIn) {
        this.localOrg = localOrgIn;
    }

    /**
     * What Master do we belong to?
     * @return the master
     */
    public IssMaster getMaster() {
        return this.master;
    }

    /**
     * Assign us to a Master
     * @param inMaster the master we should belong to
     */
    public void setMaster(IssMaster inMaster) {
        this.master = inMaster;
    }

    /**
     * @return hashCode based on master-org-id and master-org-name
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((masterOrgId == null) ? 0 : masterOrgId.hashCode());
        result = prime * result + ((masterOrgName == null) ? 0 : masterOrgName.hashCode());
        return result;
    }

    /**
     * Equality based on master-org-id and master-org-name - everything else is subject
     * to change
     * @param obj The Thing we're comparing against
     * @return true if obj.masterOrg{Id,name} equal our.masterorg{Id,Name}, false else
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        IssMasterOrg other = (IssMasterOrg) obj;
        if (masterOrgId == null) {
            if (other.masterOrgId != null) {
                return false;
            }
        }
        else if (!masterOrgId.equals(other.masterOrgId)) {
            return false;
        }
        if (masterOrgName == null) {
            if (other.masterOrgName != null) {
                return false;
            }
        }
        else if (!masterOrgName.equals(other.masterOrgName)) {
            return false;
        }
        return true;
    }

}
