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

import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * ImageRepoDigest
 */
@Entity
@Table(name = "suseImageRepoDigest")
public class ImageRepoDigest extends BaseDomainHelper {

    private Long id;
    private String repoDigest;
    private ImageInfo imageInfo;

    /**
     * @return the id
     */
    @Id
    @Column(name = "id")
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "imgrepodigest_seq")
	@SequenceGenerator(name = "imgrepodigest_seq", sequenceName = "suse_img_repodigest_id_seq", allocationSize = 1)
    public Long getId() {
        return id;
    }

    /**
     * @param idIn the id
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @return the repo digest
     */
    @Column(name = "repo_digest")
    public String getRepoDigest() {
        return repoDigest;
    }

    /**
     * @param repoDigestIn the repo digest
     */
    public void setRepoDigest(String repoDigestIn) {
        this.repoDigest = repoDigestIn;
    }

    /**
     * @return the build history
     */
    @ManyToOne
    @JoinColumn(name = "image_info_id")
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
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ImageRepoDigest castOther)) {
            return false;
        }
        return new EqualsBuilder()
                .append(imageInfo, castOther.imageInfo)
                .append(repoDigest, castOther.repoDigest)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(imageInfo)
                .append(repoDigest)
                .toHashCode();
    }
}
