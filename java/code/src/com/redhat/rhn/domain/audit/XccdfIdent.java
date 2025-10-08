/*
 * Copyright (c) 2017 SUSE LLC
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
package com.redhat.rhn.domain.audit;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * XccdfIdent - Class representation of the table rhnXccdfIdent.
 */
@Entity
@Table(name = "rhnXccdfIdent")
@Immutable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class XccdfIdent implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "rhn_xccdf_ident_seq")
    @SequenceGenerator(name = "rhn_xccdf_ident_seq", sequenceName = "rhn_xccdf_ident_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "identsystem_id")
    private XccdfIdentSystem identSystem;

    @Column
    private String identifier;

    /**
     * @return the id
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
     * @return the identSystem
     */
    public XccdfIdentSystem getIdentSystem() {
        return identSystem;
    }

    /**
     * @param identSystemIn to set
     */
    public void setIdentSystem(XccdfIdentSystem identSystemIn) {
        this.identSystem = identSystemIn;
    }

    /**
     * @return the identifier
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * @param identifierIn to set
     */
    public void setIdentifier(String identifierIn) {
        this.identifier = identifierIn;
    }
}
