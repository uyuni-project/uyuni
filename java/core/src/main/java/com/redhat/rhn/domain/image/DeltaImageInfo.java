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
import com.redhat.rhn.domain.server.Pillar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;

/**
 * DeltaImageInfo
 */
@Entity
@IdClass(DeltaImageInfoKey.class)
@Table(name = "suseDeltaImageInfo")
public class DeltaImageInfo extends BaseDomainHelper {

    private ImageInfo sourceImageInfo;
    private ImageInfo targetImageInfo;
    private Pillar pillar;
    private String file;

    /**
     * @return the source image info
     */
    @Id
    @OneToOne
    @JoinColumn(name = "source_image_id", nullable = false)
    public ImageInfo getSourceImageInfo() {
        return sourceImageInfo;
    }

    /**
     * @param imageInfoIn the source image info
     */
    public void setSourceImageInfo(ImageInfo imageInfoIn) {
        this.sourceImageInfo = imageInfoIn;
    }

    /**
     * @return the target image info
     */
    @Id
    @OneToOne
    @JoinColumn(name = "target_image_id", nullable = false)
    public ImageInfo getTargetImageInfo() {
        return targetImageInfo;
    }

    /**
     * @param imageInfoIn the target image info
     */
    public void setTargetImageInfo(ImageInfo imageInfoIn) {
        this.targetImageInfo = imageInfoIn;
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
     * @return the pillar
     */
    @OneToOne
    @JoinColumn(name = "pillar_id")
    public Pillar getPillar() {
        return pillar;
    }

    /**
     * @param pillarIn pillar to set
     */
    public void setPillar(Pillar pillarIn) {
        this.pillar = pillarIn;
    }



}
