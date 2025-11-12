/*
 * Copyright (c) 2019--2025 SUSE LLC
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
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

/**
 * Entity bean for rhnPackageExtraTagKey.
 */
@Entity
@Table(name = "rhnPackageExtraTagKey")
public class PackageExtraTagsKeys implements Serializable {

    @Serial
    private static final long serialVersionUID = 8834129583627927866L;

    private Long id;
    private String name;
    private Date created;

    private Set<PackageExtraTag> tags;


    /**
     * @return id to get
     */
    @Id
    @Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pkgxtratagkeys_seq")
	@SequenceGenerator(name = "pkgxtratagkeys_seq", sequenceName = "rhn_package_extra_tags_keys_id_seq", allocationSize = 1)
    public Long getId() {
        return id;
    }

    /**
     * @param idIn to set
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return return the tag
     */
    @OneToMany(mappedBy = "key", cascade = CascadeType.ALL, orphanRemoval = true)
    public Set<PackageExtraTag> getTags() {
        return tags;
    }

    /**
     * @param tagsIn the tag to set
     */
    public void setTags(Set<PackageExtraTag> tagsIn) {
        tags = tagsIn;
    }

    /**
     * @return name to get
     */
    @Column(name = "name")
    public String getName() {
        return name;
    }

    /**
     * @param nameIn to set
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @return created to get
     */
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "created")
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

        PackageExtraTagsKeys that = (PackageExtraTagsKeys) o;

        return new EqualsBuilder()
                .append(name, that.name)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(name)
                .toHashCode();
    }
}
