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

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.ArrayList;
import java.util.List;

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
 * ImageSyncProject
 */
@Entity
@Table(name = "suseImageSyncProject")
public class ImageSyncProject extends BaseDomainHelper {

    /** The id. */
    private Long id;

    private String name;

    private Org org;

    private ImageStore destStore;

    private Boolean scoped;

    private List<ImageSyncSource> syncSources = new ArrayList<>();

    /**
     * Standard Constructor
     */
    public ImageSyncProject() {
    }

    /**
     * Constructor
     * @param nameIn the project name
     * @param orgIn the organizatiom
     * @param destStoreIn the destination image store
     * @param scopedIn store images scoped with the store name
     */
    public ImageSyncProject(String nameIn, Org orgIn, ImageStore destStoreIn, Boolean scopedIn) {
        name = nameIn;
        org = orgIn;
        destStore = destStoreIn;
        scoped = scopedIn;
    }

    /**
     * @return the id
     */
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "imgsyncprj_seq")
    @SequenceGenerator(name = "imgsyncprj_seq", sequenceName = "suse_imgsync_prj_id_seq",
                       allocationSize = 1)
    public Long getId() {
        return id;
    }

    @Column
    public String getName() {
        return name;
    }

    /**
     * @return the org
     */
    @ManyToOne
    public Org getOrg() {
        return org;
    }


    /**
     * @return the destination image Store
     */
    @ManyToOne
    @JoinColumn(name = "dest_store_id")
    public ImageStore getDestinationImageStore() {
        return destStore;
    }

    /**
     * @return returns scoped
     */
    @Column
    public boolean isScoped() {
        return scoped;
    }

    /**
     * @return returns image sync sources
     */
    @OneToMany(fetch = FetchType.LAZY, mappedBy = "imageSyncProject", orphanRemoval = true)
    public List<ImageSyncSource> getSyncSources() {
        return syncSources;
    }

    /**
     * @param idIn the id to set
     */
    public void setId(Long idIn) {
        this.id = idIn;
    }

    /**
     * @param nameIn the name to set
     */
    public void setName(String nameIn) {
        name = nameIn;
    }

    /**
     * @param orgIn the org to set
     */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
    }

    /**
     * @param store the destination image store to set
     */
    public void setDestinationImageStore(ImageStore store) {
        destStore = store;
    }

    /**
     * @param scopedIn scoped
     */
    public void setScoped(boolean scopedIn) {
        scoped = scopedIn;
    }

    /**
     * @param sources the image sync sources to set
     */
    public void setSyncSources(List<ImageSyncSource> sources) {
        syncSources = sources;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(final Object other) {
        if (!(other instanceof ImageSyncProject)) {
            return false;
        }
        ImageSyncProject castOther = (ImageSyncProject) other;
        return new EqualsBuilder().append(name, castOther.name)
                                  .append(org, castOther.org)
                                  .append(destStore, castOther.destStore)
                                  .append(scoped, castOther.scoped)
                                  .isEquals();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(name)
                                    .append(org)
                                    .append(destStore)
                                    .append(scoped)
                                    .toHashCode();
    }
}
