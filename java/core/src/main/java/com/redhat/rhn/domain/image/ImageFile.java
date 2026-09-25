/*
 * Copyright (c) 2021--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.redhat.rhn.domain.image;

import com.redhat.rhn.domain.BaseDomainHelper;
import com.redhat.rhn.domain.common.Checksum;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.type.YesNoConverter;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * ImageFile
 */
@Entity
@Table(name = "suseImageFile")
public class ImageFile extends BaseDomainHelper {


    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "imgfile_seq")
    @SequenceGenerator(name = "imgfile_seq", sequenceName = "suse_image_file_id_seq", allocationSize = 1)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_info_id", nullable = false)
    private ImageInfo imageInfo;

    @Column(name = "file")
    private String file;

    @Column(name = "type")
    private String type;

    @Column(name = "external")
    @Convert(converter = YesNoConverter.class)
    private boolean external;

    @ManyToOne
    @JoinColumn(name = "checksum_id")
    private Checksum checksum;

    /**
     * @return the id
     */
    public Long getId() {
        return id;
    }

    /**
     * @param idIn the id in
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return the image info
     */
    public ImageInfo getImageInfo() {
        return imageInfo;
    }

    /**
     * @param imageInfoIn the image info
     */
    public void setImageInfo(ImageInfo imageInfoIn) {
        this.imageInfo = imageInfoIn;
    }


    /**
     * @return the file
     */
    public String getFile() {
        return file;
    }

    /**
     * @param fileIn file to set
     */
    public void setFile(String fileIn) {
        this.file = fileIn;
    }

    /**
     * @return the type
     */
    public String getType() {
        return type;
    }

    /**
     * @param typeIn file to set
     */
    public void setType(String typeIn) {
        this.type = typeIn;
    }


    /**
     * @return true if the file is not managed
     */
    public boolean isExternal() {
        return external;
    }


    /**
     * @param externalIn the external file
     */
    public void setExternal(boolean externalIn) {
        this.external = externalIn;
    }

    /**
     * @return the checksum of the file
     */
    public Checksum getChecksum() {
        return checksum;
    }

    /**
     * @param checksumIn checksum to set
     */
    public void setChecksum(Checksum checksumIn) {
        this.checksum = checksumIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ImageFile)) {
            return false;
        }
        ImageFile castOther = (ImageFile) other;
        return new EqualsBuilder()
                .append(imageInfo, castOther.imageInfo)
                .append(file, castOther.file)
                .append(type, castOther.type)
                .append(external, castOther.external)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(imageInfo)
                .append(file)
                .append(type)
                .append(external)
                .toHashCode();
    }

}
