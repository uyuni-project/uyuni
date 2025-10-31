/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.rhnpackage;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Entity bean for rhnPackageExtraTagKey.
 */
@Entity
@Table(name = "rhnPackageExtraTag")
@IdClass(PackageExtraTagId.class)
public class PackageExtraTag implements Serializable {

    @Serial
    private static final long serialVersionUID = 7249923893801607164L;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, cascade =  CascadeType.ALL)
    @JoinColumn(name = "package_id")
    private Package pack;

    @Id
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "key_id")
    private PackageExtraTagsKeys key;

    @Column
    private String value;

    @Column
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date created;

    /**
     * @return return the package
     */
    public Package getPack() {
        return pack;
    }

    /**
     * Set the Package
     * @param packIn the package
     */
    public void setPack(Package packIn) {
        pack = packIn;
    }

    /**
     * @return return the extra key
     */
    public PackageExtraTagsKeys getKey() {
        return key;
    }

    /**
     * Set the extra key
     * @param keyIn the extra key
     */
    public void setKey(PackageExtraTagsKeys keyIn) {
        key = keyIn;
    }

    /**
     * @return return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * Set the value
     * @param valueIn the value
     */
    public void setValue(String valueIn) {
        value = valueIn;
    }

    /**
     * @return created to get
     */
    public Date getCreated() {
        return created;
    }

    /**
     * @param createdIn to set
     */
    public void setCreated(Date createdIn) {
        this.created = createdIn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        PackageExtraTag that = (PackageExtraTag) o;

        return new EqualsBuilder()
                .append(key, that.key)
                .append(pack, that.pack)
                .append(value, that.value)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(key)
                .append(pack)
                .append(value)
                .toHashCode();
    }
}
