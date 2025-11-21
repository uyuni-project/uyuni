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

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;
import java.util.Set;
import java.util.StringJoiner;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * IssSlave - Class representation of the table rhnissslave.
 *
 */
@Entity
@Table(name = "rhnIssSlave")
public class IssSlave extends BaseDto {
    public static final long NEW_SLAVE_ID = -1L;
    public static final String ID = "id";
    public static final String SLAVE = "slave";
    public static final String ENABLED = "enabled";
    public static final String ALLOWED_ALL_ORGS = "allowAllOrgs";
    public static final String CREATED = "created";
    public static final String MODIFIED = "modified";

    /** slave-id parameter name */
    public static final String SID = "sid";

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "issslave_seq")
    @SequenceGenerator(name = "issslave_seq", sequenceName = "rhn_issslave_seq", allocationSize = 1)
    private Long id;

    @Column
    private String slave;

    @Column
    private String enabled;

    @Column(name = "allow_all_orgs")
    private String allowAllOrgs;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "rhnissslaveorgs",
            joinColumns = @JoinColumn(name = "slave_id"),
            inverseJoinColumns = @JoinColumn(name = "org_id"))
    @Fetch(FetchMode.SELECT)
    private Set<Org> allowedOrgs;

    @Column(name = "created", nullable = false, updatable = false)
    @CreationTimestamp
    private Date created = new Date();

    @Column(name = "modified", nullable = false)
    @UpdateTimestamp
    private Date modified = new Date();

    /**
     * Getter for id
     *
     * @return Long to get
     */
    @Override
    public Long getId() {
        return this.id;
    }

    /**
     * Setter for id
     *
     * @param idIn
     *            to set
     */
    protected void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * Getter for slave-name
     *
     * @return String to get
     */
    public String getSlave() {
        return this.slave;
    }

    /**
     * Setter for slave-name
     *
     * @param slaveIn
     *            to set
     */
    public void setSlave(String slaveIn) {
        this.slave = slaveIn;
    }

    /**
     * Getter for enabled
     *
     * @return true if enabled = 'Y', false otherwise
     */
    public String getEnabled() {
        return this.enabled;
    }

    /**
     * Setter for enabled
     *
     * @param enabledIn
     *            to set
     */
    public void setEnabled(String enabledIn) {
        this.enabled = enabledIn;
    }

    /**
     * Getter for allowAllOrgs
     *
     * @return String to get
     */
    public String getAllowAllOrgs() {
        return this.allowAllOrgs;
    }

    /**
     * Setter for allowAllOrgs
     *
     * @param allowAllOrgsIn
     *            to set
     */
    public void setAllowAllOrgs(String allowAllOrgsIn) {
        this.allowAllOrgs = allowAllOrgsIn;
    }

    /**
     * Getter for created
     *
     * @return Date to get
     */
    public Date getCreated() {
        return this.created;
    }

    /**
     * Setter for created
     *
     * @param createdIn
     *            to set
     */
    public void setCreated(Date createdIn) {
        this.created = createdIn;
    }

    /**
     * Getter for modified
     *
     * @return Date to get
     */
    public Date getModified() {
        return this.modified;
    }

    /**
     * Setter for modified
     *
     * @param modifiedIn to set
     */
    public void setModified(Date modifiedIn) {
        this.modified = modifiedIn;
    }

    /**
     * Getter for all orgs allowed to be visible to this slave
     * @return list of currently-mapped orgs
     */
    public Set<Org> getAllowedOrgs() {
        return allowedOrgs;
    }

    /**
     * Setter for allowed orgs
     * @param allowedOrgsIn get current orgs we can export to this slave
     */
    public void setAllowedOrgs(Set<Org> allowedOrgsIn) {
        this.allowedOrgs = allowedOrgsIn;
    }

    /**
     * How many of our orgs are allowed to be exported to this slave?
     * @return num allowed orgs
     */
    public int getNumAllowedOrgs() {
        return getAllowedOrgs().size();
    }

    /**
     * @return hashCode based on id
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    /**
     * Equality based on id
     * @param obj The Thing we're comparing against
     * @return true if obj.Id equal our.Id, false else
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
        IssSlave other = (IssSlave) obj;
        if (id == null) {
            if (other.id != null) {
                return false;
            }
        }
        else if (!id.equals(other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", IssSlave.class.getSimpleName() + "[", "]")
            .add("id=" + id)
            .add("slave='" + slave + "'")
            .add("enabled='" + enabled + "'")
            .add("allowAllOrgs='" + allowAllOrgs + "'")
            .toString();
    }
}
