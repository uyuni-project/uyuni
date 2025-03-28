/*
 * Copyright (c) 2024 SUSE LLC
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
package com.redhat.rhn.domain;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;


/**
 * A Label is essentially a constant because Label implementations are mapped to read-only,
 * reference tables where the Label objects/rows already exist. Reference tables like these
 * consist of (at least) the following columns:
 *
 * <ul>
 *   <li>ID</li>
 *   <li>NAME</li>
 *   <li>LABEL</li>
 *   <li>CREATED</li>
 *   <li>MODIFIED</li>
 * </ul>
 *
 * Examples of these types of tables include <code>rhnServerGroupType</code> and <code>
 * rhnVirtualInstanceType</code>. Additional columns can be mapped in subclasses.
 * <p>
 * {@link AbstractLabelNameHelper} is conceptually the same as this class; however, it is
 * not implemented as an immutable like this class.
 * <p>
 * For an example of how to implement a Label, take a look at VirtualInstanceType and
 * VirtualInstanceTypeFactory.
 *
 * @see com.redhat.rhn.domain.server.VirtualInstanceType
 *
 */
@MappedSuperclass
public abstract class Label extends BaseDomainHelper implements Labeled {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;
    @Column(name = "name", length = 128, nullable = false)
    private String name = "";
    @Column(name = "label", length = 128, nullable = false)
    private String label = "";

    /**
     * Returns the primary key.
     *
     * @return The primary key
     */
    public Long getId() {
        return id;
    }

    private void setId(Long newId) {
        id = newId;
    }

    /**
     * Returns the name of this label.
     *
     * @return The name of this label
     */
    public String getName() {
        return name;
    }

    private void setName(String newName) {
        name = newName;
    }

    /**
     * Returns the label text of this label.
     *
     * @return The label text of this label
     */
    @Override
    public String getLabel() {
        return label;
    }

    private void setLabel(String newLabel) {
        label = newLabel;
    }

    /**
     * Two labels are considered equal when they have the same name and label text.
     *
     * @param object The object to compare against this label
     *
     * @return <code>true</code> if <code>object</code> is a label and its
     * label text and name are the same as this label.
     */
    @Override
    public boolean equals(Object object) {
        if (object == null || object.getClass() != getClass()) {
            return false;
        }

        Label that = (Label)object;

        return new EqualsBuilder().append(this.getName(), that.getName())
                .append(this.getLabel(), that.getLabel()).isEquals();
    }

    /**
     *
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getName()).append(getLabel()).toHashCode();
    }

}
