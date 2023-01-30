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

    private Boolean scroped;


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
        return scroped;
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
    public void setDestinationImageStoreStore(ImageStore store) {
        destStore = store;
    }

    /**
     * @param scropedIn scoped
     */
    public void setScroped(boolean scropedIn) {
        scroped = scropedIn;
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
                                  .append(scroped, castOther.scroped)
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
                                    .append(scroped)
                                    .toHashCode();
    }
}
