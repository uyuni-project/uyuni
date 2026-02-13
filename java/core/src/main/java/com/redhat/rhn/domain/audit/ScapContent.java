/*
 * Copyright (c) 2025 SUSE LLC
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

import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * ScapContent entity representing SCAP content files.
 */
@Entity
@Table(name = "suseScapContent")
public class ScapContent extends BaseDomainHelper {

    private Long id;
    private String name;
    private String dataStreamFileName;
    private String xccdfFileName;
    private String description;

    /**
     * Default constructor.
     */
    public ScapContent() {
        // Default constructor
    }

    /**
     * @return the id
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * @return the name
     */
    @Column(name = "name")
    public String getName() {
        return name;
    }

    /**
     * @param nameIn the name to set
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @return the dataStreamFileName
     */
    @Column(name = "datastream_file_name")
    public String getDataStreamFileName() {
        return dataStreamFileName;
    }

    /**
     * @param dataStreamFileNameIn the dataStreamFileName to set
     */
    public void setDataStreamFileName(String dataStreamFileNameIn) {
        this.dataStreamFileName = dataStreamFileNameIn;
    }

    /**
     * @return the xccdfFileName
     */
    @Column(name = "xccdf_file_name")
    public String getXccdfFileName() {
        return xccdfFileName;
    }

    /**
     * @param xccdfFileNameIn the xccdfFileName to set
     */
    public void setXccdfFileName(String xccdfFileNameIn) {
        this.xccdfFileName = xccdfFileNameIn;
    }

    /**
     * @return the description
     */
    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    /**
     * @param descriptionIn the description to set
     */
    public void setDescription(String descriptionIn) {
        this.description = descriptionIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (this == other) {
            return true;
        }
        if (other == null || getClass() != other.getClass()) {
            return false;
        }
        ScapContent castOther = (ScapContent) other;
        return new EqualsBuilder()
                .append(name, castOther.name)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return super.toString();
    }
}

