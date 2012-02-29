/**
 * Copyright (c) 2012 Novell
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.image;

import com.redhat.rhn.frontend.dto.BaseDto;

/**
 * Images for deployment to virtual host systems. Currently such image objects
 * are not being persisted, but they rather exist in memory only.
 */
public class Image extends BaseDto implements Comparable<Image> {

    private Long id;
    private String name;
    private String version;
    private String arch;
    private String imageSize;
    private String imageType;
    private String downloadUrl;
    private String editUrl;
    private boolean selectable = true;

    public Long getId() {
        return this.id;
    }

    public void setId(Long inId) {
        this.id = inId;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String inName) {
        this.name = inName;
    }

    public String getVersion() {
        return this.version;
    }

    public void setVersion(String inVersion) {
        this.version = inVersion;
    }

    public String getArch() {
        return this.arch;
    }

    public void setArch(String inArch) {
        this.arch = inArch;
    }

    public String getImageSize() {
        return this.imageSize;
    }

    public void setImageSize(String inImageSize) {
        this.imageSize = inImageSize;
    }

    public String getImageType() {
        return this.imageType;
    }

    public void setImageType(String inImageType) {
        this.imageType = inImageType;
    }

    public String getDownloadUrl() {
        return this.downloadUrl;
    }

    public void setDownloadUrl(String inDownloadUrl) {
        this.downloadUrl = inDownloadUrl;
    }

    public String getEditUrl() {
        return this.editUrl;
    }

    public void setEditUrl(String inEditUrl) {
        this.editUrl = inEditUrl;
    }

    public void setSelectable(boolean value) {
        this.selectable = value;
    }

    @Override
    public boolean isSelectable() {
        return selectable;
    }

    @Override
    public String getSelectionKey() {
        return String.valueOf(getId());
    }

    @Override
    public boolean equals(Object obj) {
        boolean ret = false;
        if (obj instanceof Image) {
            Image image = (Image) obj;
            if (this.getId().equals(image.getId())) {
                ret = true;
            }
        }
        return ret;
    }

    @Override
    public int compareTo(Image image) {
        int ret = 0;
        if (!this.name.equals(image.getName())) {
            ret = this.name.compareTo(image.name);
        } else if (!this.version.equals(image.getVersion())) {
            ret = this.version.compareTo(image.version);
        } else if (!this.arch.equals(image.getArch())) {
            ret = this.arch.compareTo(image.getArch());
        } else if (!this.imageType.equals(image.getImageType())) {
            ret = this.imageType.compareTo(image.getImageType());
        }
        return ret;
    }
}
