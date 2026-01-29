/*
 * Copyright (c) 2023 SUSE LLC
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

package com.redhat.rhn.domain.recurringactions.state;

import org.hibernate.annotations.Immutable;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Class representation of the table suseInternalState.
 */

@Entity
@Immutable
@Table(name = "suseInternalState")
public class InternalState implements Serializable {

    private Long id;
    private String name;
    private String label;

    /**
     * Gets the id
     *
     * @return the id
     */
    @Id
    public Long getId() {
        return id;
    }

    /**
     * Sets the id
     *
     * @param idIn the id
     */
    protected void setId(Long idIn) {
        id = idIn;
    }

    /**
     * Gets the name
     *
     * @return the name
     */
    @Column
    public String getName() {
        return name;
    }

    /**
     * Sets the name
     *
     * @param nameIn the name
     */
    protected void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * Gets the label
     *
     * @return the label
     */
    @Column
    public String getLabel() {
        return label;
    }

    /**
     * Sets the label
     *
     * @param labelIn the label
     */
    protected void setLabel(String labelIn) {
        this.label = labelIn;
    }
}
