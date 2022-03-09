/*
 * Copyright (c) 2021 SUSE LLC
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

import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * ImageFile
 */
@Entity
@Table(name = "suseImageFile")
public class ImageFile extends BaseDomainHelper {

    private Long id;
    private ImageInfo imageInfo;
    private String file;
    private String type;
    private boolean external;

    /**
     * @return the id
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "imgfile_seq")
    @SequenceGenerator(name = "imgfile_seq",
            sequenceName = "suse_image_file_id_seq")
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
    @ManyToOne
    @JoinColumn(name = "image_info_id", nullable = false)
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
    @Column(name = "file")
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
    @Column(name = "type")
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
    @Column(name = "external")
    @Type(type = "yes_no")
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
     * {@inheritDoc}
     */
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
    public int hashCode() {
        return new HashCodeBuilder()
                .append(imageInfo)
                .append(file)
                .append(type)
                .append(external)
                .toHashCode();
    }

}
