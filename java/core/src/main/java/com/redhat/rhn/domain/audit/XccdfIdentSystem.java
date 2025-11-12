/*
 * Copyright (c) 2017--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.audit;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * XccdfIdentSystem - Class representation of the table rhnXccdfIdentSystem.
 */
@Entity
@Table(name = "rhnXccdfIdentSystem")
@Immutable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class XccdfIdentSystem implements Serializable {

    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rhn_xccdf_identsystem_seq")
	@SequenceGenerator(name = "rhn_xccdf_identsystem_seq", sequenceName = "rhn_xccdf_identsytem_id_seq", allocationSize = 1)
    private Long id;

    @Column
    private String system;

    /**
     * @return id to get
     */
    public Long getId() {
        return id;
    }

    /**
     * @param idIn to set
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return system to get
     */
    public String getSystem() {
        return system;
    }

    /**
     * @param systemIn to set
     */
    public void setSystem(String systemIn) {
        this.system = systemIn;
    }
}
