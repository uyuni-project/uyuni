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
 * ScapContent entity representing SCAP content files
 */
@Entity
@Table(name = "suseScapContent")
public class ScapContent extends BaseDomainHelper {

    private Long id;
    private String name;
    private String dataStreamFileName;
    private String xccdfFileName;
    private String description;

    public ScapContent() {
    }
    
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long getId() {
        return id;
    }

    public void setId(Long idIn) {
        id = idIn;
    }
    
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String nameIn) {
        this.name = nameIn;
    }

    @Column(name = "datastream_file_name")
    public String getDataStreamFileName() {
        return dataStreamFileName;
    }

    public void setDataStreamFileName(String dataStreamFileNameIn) {
        this.dataStreamFileName = dataStreamFileNameIn;
    }

    @Column(name = "xccdf_file_name")
    public String getXccdfFileName() {
        return xccdfFileName;
    }

    public void setXccdfFileName(String xccdfFileNameIn) {
        this.xccdfFileName = xccdfFileNameIn;
    }

    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String descriptionIn) {
        this.description = descriptionIn;
    }
   
   @Override
    public boolean equals(final Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;
        ScapContent castOther = (ScapContent) other;
        return new EqualsBuilder()
                .append(name, castOther.name)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .toHashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
