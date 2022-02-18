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

import com.redhat.rhn.domain.BaseDomainHelper;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

/**
 * ImageBuildHistory
 */
@Entity
@Table(name = "suseImageBuildHistory")
public class ImageBuildHistory extends BaseDomainHelper {

    private Long id;
    private int revisionNumber;
    private ImageInfo imageInfo;
    private Set<ImageRepoDigest> repoDigests = new HashSet<>();

    /**
     * @return the id
     */
    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "imgbuildhistory_seq")
    @SequenceGenerator(name = "imgbuildhistory_seq",
            sequenceName = "suse_img_buildhistory_id_seq")
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
     * @return the revision number
     */
    @Column(name = "revision_num")
    public int getRevisionNumber() {
        return revisionNumber;
    }

    /**
     * @param revisionNumberIn the revision number
     */
    public void setRevisionNumber(int revisionNumberIn) {
        this.revisionNumber = revisionNumberIn;
    }

    /**
     * @return the image info
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
     * @return the repo digests
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "buildHistory", cascade = CascadeType.ALL)
    public Set<ImageRepoDigest> getRepoDigests() {
        return repoDigests;
    }

    /**
     * @param repoDigestsIn the repo digests
     */
    public void setRepoDigests(Set<ImageRepoDigest> repoDigestsIn) {
        this.repoDigests = repoDigestsIn;
    }

    /**
     * {@inheritDoc}
     */
    public boolean equals(final Object other) {
        if (!(other instanceof ImageBuildHistory)) {
            return false;
        }
        ImageBuildHistory castOther = (ImageBuildHistory) other;
        return new EqualsBuilder()
                .append(imageInfo, castOther.imageInfo)
                .append(revisionNumber, castOther.revisionNumber)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return new HashCodeBuilder()
                .append(imageInfo)
                .append(revisionNumber)
                .toHashCode();
    }

}
