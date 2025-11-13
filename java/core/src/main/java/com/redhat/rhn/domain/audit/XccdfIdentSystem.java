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
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Immutable;
import org.hibernate.annotations.Parameter;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * XccdfIdentSystem - Class representation of the table rhnXccdfIdentSystem.
 */
@Entity
@Table(name = "rhnXccdfIdentSystem")
@Immutable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class XccdfIdentSystem implements Serializable {

    @Id
    @GeneratedValue(generator = "rhn_xccdf_identsystem_seq")
    @GenericGenerator(
            name = "rhn_xccdf_identsystem_seq",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "rhn_xccdf_identsytem_id_seq"),
                    @Parameter(name = "increment_size", value = "1")
            })
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
