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
package com.redhat.rhn.domain.user;


import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_ONLY;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.Immutable;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * State
 */
@Entity
@Table(name = "rhnWebContactChangeState")
@Immutable
@Cache(usage = READ_ONLY)
public class State implements Serializable {

    @Id
    private Long id;
    @Column
    private String label;


    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param i The id to set.
     */
    protected void setId(Long i) {
        this.id = i;
    }

    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param s The label to set.
     */
    public void setLabel(String s) {
        this.label = s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof State that)) {
            return false;
        }
        return label.equals(that.getLabel());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return label.hashCode();
    }
}
