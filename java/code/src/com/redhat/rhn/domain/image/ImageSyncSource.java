/*
 * Copyright (c) 2023 SUSE LLC
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
import com.redhat.rhn.domain.org.Org;

import com.vladmihalcea.hibernate.type.json.JsonType;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import java.util.LinkedList;
import java.util.List;

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
 * ImageSyncSource
 */
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonType.class)
})
@Entity
@Table(name = "suseImageSyncSource")
public class ImageSyncSource extends BaseDomainHelper {

    /** The id. */
    private Long id;

    private ImageSyncProject imageSyncProject;

    private Org org;

    private ImageStore srcStore;

    private String srcRepository;

    private List<String> srcTags = new LinkedList<>();

    private String srcTagsRegexp;

    /**
     * Standard Constructor
     */
    public ImageSyncSource() {
    }

    /**
     * Constructor
     * @param imageSyncProjectIn the project
     * @param orgIn the organization
     * @param srcStoreIn the source image store
     * @param srcRepositoryIn the repository
     * @param srcTagsIn list of static tags
     * @param srcTagsRegexpIn regexp to match tags
     */
    public ImageSyncSource(ImageSyncProject imageSyncProjectIn, Org orgIn, ImageStore srcStoreIn,
                           String srcRepositoryIn, List<String> srcTagsIn, String srcTagsRegexpIn) {
        imageSyncProject = imageSyncProjectIn;
        org = orgIn;
        srcStore = srcStoreIn;
        srcRepository = srcRepositoryIn;
        srcTags = srcTagsIn;
        srcTagsRegexp = srcTagsRegexpIn;
    }

    /**
     * Constructor
     * @param imageSyncProjectIn the project
     * @param orgIn the organization
     * @param srcStoreIn the source image store
     * @param srcRepositoryIn the repository
     * @param srcTagsIn list of static tags
     */
    public ImageSyncSource(ImageSyncProject imageSyncProjectIn, Org orgIn, ImageStore srcStoreIn,
                           String srcRepositoryIn, List<String> srcTagsIn) {
        imageSyncProject = imageSyncProjectIn;
        org = orgIn;
        srcStore = srcStoreIn;
        srcRepository = srcRepositoryIn;
        srcTags = srcTagsIn;
    }

    /**
     * Constructor
     * @param imageSyncProjectIn the project
     * @param orgIn the organization
     * @param srcStoreIn the source image store
     * @param srcRepositoryIn the repository
     * @param srcTagsRegexpIn regexp to match tags
     */
    public ImageSyncSource(ImageSyncProject imageSyncProjectIn, Org orgIn, ImageStore srcStoreIn,
                           String srcRepositoryIn, String srcTagsRegexpIn) {
        imageSyncProject = imageSyncProjectIn;
        org = orgIn;
        srcStore = srcStoreIn;
        srcRepository = srcRepositoryIn;
        srcTagsRegexp = srcTagsRegexpIn;
    }

    /**
     * Constructor - sync all available tags
     * @param imageSyncProjectIn the project
     * @param orgIn the organization
     * @param srcStoreIn the source image store
     * @param srcRepositoryIn the repository
     */
    public ImageSyncSource(ImageSyncProject imageSyncProjectIn, Org orgIn, ImageStore srcStoreIn,
                           String srcRepositoryIn) {
        imageSyncProject = imageSyncProjectIn;
        org = orgIn;
        srcStore = srcStoreIn;
        srcRepository = srcRepositoryIn;
    }

    /**
     * @return the id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "imgsyncsrc_seq")
    @SequenceGenerator(name = "imgsyncsrc_seq", sequenceName = "suse_imgsync_src_id_seq",
                       allocationSize = 1)
    public Long getId() {
        return id;
    }

    /**
     * @return the image sync project
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sync_proj_id")
    public ImageSyncProject getImageSyncProject() {
        return imageSyncProject;
    }

    /**
     * @return the org
     */
    @ManyToOne
    public Org getOrg() {
        return org;
    }

    /**
     * @return the source repository
     */
    @Column(name = "src_repository")
    public String getSrcRepository() {
        return srcRepository;
    }

    /**
     * @return the source tags
     */
    @Type(type = "json")
    @Column(columnDefinition = "jsonb", name = "src_tags")
    public List<String> getSrcTags() {
        return srcTags;
    }

    /**
     * @return return tag regexp if set
     */
    @Column(name = "src_tags_regex")
    public String getSrcTagsRegexp() {
        return srcTagsRegexp;
    }

    /**
     * @return the source image store
     */
    @ManyToOne
    @JoinColumn(name = "src_store_id")
    public ImageStore getSrcStore() {
        return srcStore;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @param projectIn the project
     */
    public void setImageSyncProject(ImageSyncProject projectIn) {
        imageSyncProject = projectIn;
    }

    /**
     * @param orgIn the org to set
     */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
    }

    /**
     * @param srcStoreIn the store to set
     */
    public void setSrcStore(ImageStore srcStoreIn) {
        srcStore = srcStoreIn;
    }

    /**
     * @param srcRepositoryIn the repository
     */
    public void setSrcRepository(String srcRepositoryIn) {
        srcRepository = srcRepositoryIn;
    }

    /**
     * @param srcTagsIn the list of tags
     */
    public void setSrcTags(List<String> srcTagsIn) {
        srcTags = srcTagsIn;
    }

    /**
     * @param srcTagsRegexpIn tags regular expression
     */
    public void setSrcTagsRegexp(String srcTagsRegexpIn) {
        srcTagsRegexp = srcTagsRegexpIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ImageSyncSource)) {
            return false;
        }
        ImageSyncSource castOther = (ImageSyncSource) other;
        return new EqualsBuilder()
                .append(imageSyncProject, castOther.imageSyncProject)
                .append(srcStore, castOther.srcStore)
                .append(org, castOther.org)
                .append(srcRepository, castOther.srcRepository)
                .append(srcTags, castOther.srcTags)
                .append(srcTagsRegexp, castOther.srcTagsRegexp)
                .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(imageSyncProject)
                .append(srcStore)
                .append(org)
                .append(srcRepository)
                .append(srcTags)
                .append(srcTagsRegexp)
                .toHashCode();
    }
}
