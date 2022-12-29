/*
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

import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.legacy.UserImpl;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * CustomDataValue
 */
@Entity
@Table(name = "suseProfileCustomDataValue")

public class ProfileCustomDataValue {

    private Long id;
    private ImageProfile profile;
    private CustomDataKey key;
    private String value;
    private User creator;
    private User lastModifier;
    private Date created;
    private Date modified;

    /**
     * @return Returns the Id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "pcdv_seq")
    @SequenceGenerator(name = "pcdv_seq", sequenceName = "suse_pcdv_id_seq",
                       allocationSize = 1)
    public Long getId() {
        return id;
    }
    /**
     * @return Returns the image profile.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    public ImageProfile getProfile() {
        return profile;
    }
    /**
     * @return Returns the key.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "key_id", nullable = false)
    public CustomDataKey getKey() {
        return key;
    }
    /**
     * @return Returns the value.
     */
    @Column
    public String getValue() {
        return value;
    }
    /**
     * @return Returns the creator.
     */
    @ManyToOne(targetEntity = UserImpl.class)
    @JoinColumn(name = "created_by", nullable = true)
    public User getCreator() {
        return creator;
    }
    /**
     * @return Returns the lastModifier.
     */
    @ManyToOne(targetEntity = UserImpl.class)
    @JoinColumn(name = "last_modified_by", nullable = true)
    public User getLastModifier() {
        return lastModifier;
    }
    /**
     * @return Returns the created.
     */
    @Column(insertable = false, updatable = false)
    public Date getCreated() {
        return created;
    }
    /**
     * @return Returns the modified.
     */
    @Column(insertable = false, updatable = false)
    public Date getModified() {
        return modified;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }
    /**
     * @param profileIn The image profile to set.
     */
    public void setProfile(ImageProfile profileIn) {
        this.profile = profileIn;
    }
    /**
     * @param keyIn The key to set.
     */
    public void setKey(CustomDataKey keyIn) {
        this.key = keyIn;
    }
    /**
     * @param valueIn The value to set.
     */
    public void setValue(String valueIn) {
        this.value = StringUtil.webToLinux(valueIn);
    }
    /**
     * @param creatorIn The creator to set.
     */
    public void setCreator(User creatorIn) {
        this.creator = creatorIn;
    }
    /**
     * @param lastModifierIn The lastModifier to set.
     */
    public void setLastModifier(User lastModifierIn) {
        this.lastModifier = lastModifierIn;
    }
    /**
     * @param createdIn The created to set.
     */
    public void setCreated(Date createdIn) {
        this.created = createdIn;
    }
    /**
     * @param modifiedIn The modified to set.
     */
    public void setModified(Date modifiedIn) {
        this.modified = modifiedIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (other == null || !(other instanceof ProfileCustomDataValue)) {
            return false;
        }
        ProfileCustomDataValue castOther = (ProfileCustomDataValue) other;
        return new EqualsBuilder()
                .append(key, castOther.getKey())
                .append(profile, castOther.getProfile())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(key)
                .append(profile)
                .toHashCode();
    }
}
