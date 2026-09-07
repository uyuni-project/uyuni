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
package com.redhat.rhn.domain.image;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.io.Serial;
import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;


/**
 * ImageStoreType
 */
@Entity
@Table(name = "suseImageStoreType")
public class ImageStoreType implements Serializable {

    @Serial
    private static final long serialVersionUID = -1703542510456137197L;

    /** The id. */

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "imgstoretype_seq")
    @SequenceGenerator(name = "imgstoretype_seq", sequenceName = "suse_imgstore_type_id_seq", allocationSize = 1)
    private Long id;


    @Column(name = "label")
    private String label;


    @Column(name = "name")
    private String name;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @param labelIn the label to set
     */
    public void setLabel(String labelIn) {
        this.label = labelIn;
    }

    /**
     * @param nameIn the name to set
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ImageStoreType castOther)) {
            return false;
        }
        return new EqualsBuilder().append(label, castOther.label)
                                  .append(name, castOther.name)
                                  .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(label)
                                    .append(name)
                                    .toHashCode();
    }
}
