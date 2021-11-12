/**
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
package com.redhat.rhn.domain.image;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;


/**
 * ImageStoreType
 */
@Entity
@Table(name = "suseImageStoreType")
public class ImageStoreType {

    /** The id. */
    private Long id;

    private String label;

    private String name;

    /**
     * @return the id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "imgstoretype_seq")
    @SequenceGenerator(name = "imgstoretype_seq",
            sequenceName = "suse_imgstore_type_id_seq", allocationSize = 1)
    public Long getId() {
        return id;
    }

    /**
     * @return the label
     */
    @Column(name = "label")
    public String getLabel() {
        return label;
    }

    /**
     * @return the name
     */
    @Column(name = "name")
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
    public boolean equals(final Object other) {
        if (!(other instanceof ImageStoreType)) {
            return false;
        }
        ImageStoreType castOther = (ImageStoreType) other;
        return new EqualsBuilder().append(label, castOther.label)
                                  .append(name, castOther.name)
                                  .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return new HashCodeBuilder().append(label)
                                    .append(name)
                                    .toHashCode();
    }
}
