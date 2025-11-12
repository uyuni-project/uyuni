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

import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.org.CustomDataKey;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.legacy.UserImpl;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * CustomDataValue
 */
@Entity
@Table(name = "suseImageCustomDataValue")
public class ImageInfoCustomDataValue extends BaseDomainHelper {

    private Long id;
    private ImageInfo imageInfo;
    private CustomDataKey key;
    private String value;
    private User creator;
    private User lastModifier;

    /**
     * Instantiates a new image info custom data value.
     */
    public ImageInfoCustomDataValue() { }

    /**
     * Instantiates a new image info custom data value from a
     * {@link ProfileCustomDataValue} instance
     *
     * @param customDataValueIn the profile custom data value
     * @param imageInfoIn the image info
     */
    public ImageInfoCustomDataValue(
            ProfileCustomDataValue customDataValueIn, ImageInfo imageInfoIn) {
        this.setImageInfo(imageInfoIn);
        this.setKey(customDataValueIn.getKey());
        this.setValue(customDataValueIn.getValue());
        this.setCreator(customDataValueIn.getCreator());
        this.setLastModifier(customDataValueIn.getLastModifier());
    }

    /**
     * @return Returns the Id
     */
    @Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "icdv_seq")
	@SequenceGenerator(name = "icdv_seq", sequenceName = "suse_icdv_id_seq", allocationSize = 1)
    public Long getId() {
        return id;
    }
    /**
     * @return Returns the image info.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_info_id", nullable = false)
    public ImageInfo getImageInfo() {
        return imageInfo;
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
     * @param idIn the id to set
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }
    /**
     * @param imageInfoIn The image info to set.
     */
    public void setImageInfo(ImageInfo imageInfoIn) {
        this.imageInfo = imageInfoIn;
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
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ImageInfoCustomDataValue castOther)) {
            return false;
        }
        return new EqualsBuilder()
                .append(key, castOther.getKey())
                .append(imageInfo, castOther.getImageInfo())
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(key)
                .append(imageInfo)
                .toHashCode();
    }
}
