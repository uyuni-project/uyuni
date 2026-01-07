/*
 * Copyright (c) 2025 SUSE LLC
 * Copyright (c) 2012 Red Hat, Inc.
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
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * XccdfProfile - Class representation of the table rhnXccdfProfile.
 */
@Entity
@Table(name = "rhnXccdfProfile")
@Immutable
@Cache(usage = CacheConcurrencyStrategy.READ_ONLY)
public class XccdfProfile implements Serializable {

    @Id
    private Long id;
    @Column
    private String identifier;
    @Column
    private String title;

    /**
     * Getter for id
     * @return Long to get
    */
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
     * Getter for identifier
     * @return String to get
    */
    public String getIdentifier() {
        return this.identifier;
    }

    /**
     * Setter for identifier
     * @param identifierIn to set
    */
    public void setIdentifier(String identifierIn) {
        this.identifier = identifierIn;
    }

    /**
     * Getter for title
     * @return String to get
     */
    public String getTitle() {
        return this.title;
    }

    /**
     * Setter for title
     * @param titleIn to set
     */
    public void setTitle(String titleIn) {
        this.title = titleIn;
    }
}
