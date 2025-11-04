/*
 * Copyright (c) 2025 SUSE LLC
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
package com.redhat.rhn.domain.channel;

import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 *
 * ProductName
 */
@Entity
@Table(name = "rhnProductName")
public class ProductName extends BaseDomainHelper {
    @Id
    @GeneratedValue(generator = "productname_seq")
    @GenericGenerator(
            name = "productname_seq",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "sequence_name", value = "rhn_productname_id_seq"),
                    @Parameter(name = "increment_size", value = "1")
            })
    private Long id;

    @Column
    private String label;

    @Column
    private String name;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }


    /**
     * @param val the id to set
     */
    protected void setId(Long val) {
        this.id = val;
    }

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param labelIn the label to set
     */
    public void setLabel(String labelIn) {
        this.label = labelIn;
    }

    /**
     * @return the name
     */
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
     *
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
       return new HashCodeBuilder().append(getId()).
                           append(getName()).append(getLabel()).toHashCode();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProductName that)) {
            return false;
        }
        return new EqualsBuilder().append(this.getId(), that.getId()).
                           append(this.getLabel(), that.getLabel()).
                           append(this.getName(), that.getName()).isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
            return new ToStringBuilder(this).append("id", getId())
                    .append("label", getLabel()).
                    append("name", getName()).toString();
    }
}
