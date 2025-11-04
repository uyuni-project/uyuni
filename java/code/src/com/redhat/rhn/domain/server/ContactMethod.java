/*
 * Copyright (c) 2013 SUSE LLC
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
package com.redhat.rhn.domain.server;

import static org.hibernate.annotations.CacheConcurrencyStrategy.READ_ONLY;

import com.redhat.rhn.common.localization.LocalizationService;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.Immutable;

import javax.persistence.Access;
import javax.persistence.AccessType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Class representation of the table suseServerContactMethod.
 */
@Entity
@Table(name = "suseServerContactMethod")
@Immutable
@Cache(usage = READ_ONLY)
public class ContactMethod {

    @Id
    @Column
    private Long id;

    @Column
    private String label;

    // AccessType.PROPERTY is needed for this field, so that the method setName() is called when loading the object
    // with annotations, otherwise, the default is AccessType.FIELD, meaning that the field is filled directly
    // by hibernate without calling the setter
    @Column
    @Access(AccessType.PROPERTY)
    private String name;

    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }

    /**
     * @param i The id to set.
     */
    public void setId(Long i) {
        this.id = i;
    }

    /**
     * @return Returns the label.
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param l The label to set.
     */
    public void setLabel(String l) {
        this.label = l;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * Set the localized name.
     * @param key the key to look up the localized name
     * */
    public void setName(String key) {
        this.name = LocalizationService.getInstance().getMessage(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(this.getId())
                .append(this.getName())
                .append(this.getLabel())
                .toHashCode();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object oth) {
        if (!(oth instanceof ContactMethod other)) {
            return false;
        }
        return new EqualsBuilder().append(this.getId(), other.getId())
                .append(this.getName(), other.getName())
                .append(this.getLabel(), other.getLabel())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return getClass().getName() + " : id: " + getId();
    }
}
