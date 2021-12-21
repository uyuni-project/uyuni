/*
 * Copyright (c) 2020 SUSE LLC
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
package com.suse.manager.virtualization;

import com.google.gson.annotations.SerializedName;

/**
 * Represents the informations for a disk in result from a Salt virt.vm_info or virt.get_disks call.
 */
public class VmInfoDiskJson {
    private String file;
    private String type;

    @SerializedName("file format")
    private String format;

    @SerializedName("disk size")
    private Long size;

    @SerializedName("virtual size")
    private Long virtualSize;

    /**
     * @return the file for the disk. Can also be in the [pool]/[volume] form.
     */
    public String getFile() {
        return file;
    }

    /**
     * @param fileIn the file for the disk. Can also be in the [pool]/[volume] form.
     */
    public void setFile(String fileIn) {
        file = fileIn;
    }

    /**
     * @return one of "disk", "cdrom", "floppy"
     */
    public String getType() {
        return type;
    }

    /**
     * @param typeIn one of "disk", "cdrom", "floppy"
     */
    public void setType(String typeIn) {
        type = typeIn;
    }

    /**
     * @return the disk format
     */
    public String getFormat() {
        return format;
    }

    /**
     * @param formatIn the disk format
     */
    public void setFormat(String formatIn) {
        format = formatIn;
    }

    /**
     * @return the physical disk size
     */
    public Long getSize() {
        return size;
    }

    /**
     * @param sizeIn the physical disk size
     */
    public void setSize(Long sizeIn) {
        size = sizeIn;
    }

    /**
     * @return the size of the disk as seen by the OS
     */
    public Long getVirtualSize() {
        return virtualSize;
    }

    /**
     * @param virtualSizeIn the size of the disk as seen by the OS
     */
    public void setVirtualSize(Long virtualSizeIn) {
        virtualSize = virtualSizeIn;
    }
}
