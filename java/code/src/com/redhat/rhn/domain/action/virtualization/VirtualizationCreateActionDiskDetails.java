/**
 * Copyright (c) 2018 SUSE LLC
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
package com.redhat.rhn.domain.action.virtualization;

import java.util.Objects;

/**
 * Represents the disk configuration options when creating a Virtual machine
 */
public class VirtualizationCreateActionDiskDetails {

    private Long id;
    private VirtualizationCreateAction action;
    private String type;
    private String device;
    private String template;
    private long size = 0;
    private String bus;
    private String pool;
    private String sourceFile;

    /**
     * @return Returns the disk type (file, network, etc).
     */
    public String getType() {
        return type;
    }

    /**
     * @param typeIn The type to set.
     */
    public void setType(String typeIn) {
        type = typeIn;
    }

    /**
     * @return Returns the device (disk, cdrom, floppy, lun).
     */
    public String getDevice() {
        return device;
    }

    /**
     * @param deviceIn The device to set.
     */
    public void setDevice(String deviceIn) {
        device = deviceIn;
    }

    /**
     * @return the disk details ID in the DB
     */
    public Long getId() {
        return id;
    }

    /**
     * @param idIn the disk details ID in the DB
     */
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * @return the associated virtualization create action details
     */
    public VirtualizationCreateAction getAction() {
        return action;
    }

    /**
     * @param actionIn the associated virtualization create action details
     */
    public void setAction(VirtualizationCreateAction actionIn) {
        action = actionIn;
    }

    /**
     * @return the disk image template to use
     */
    public String getTemplate() {
        return template;
    }

    /**
     * @param templateIn the disk image template to use
     */
    public void setTemplate(String templateIn) {
        template = templateIn;
    }

    /**
     * @return Returns the size.
     */
    public long getSize() {
        return size;
    }

    /**
     * @param sizeIn The size to set.
     */
    public void setSize(long sizeIn) {
        size = sizeIn;
    }

    /**
     * @return the disk bus type
     */
    public String getBus() {
        return bus;
    }

    /**
     * @param busIn the disk bus type
     */
    public void setBus(String busIn) {
        bus = busIn;
    }

    /**
     * @return the pool where the disk image will located
     */
    public String getPool() {
        return pool;
    }

    /**
     * @param poolIn the source where the disk image will located
     */
    public void setPool(String poolIn) {
        pool = poolIn;
    }

    /**
     * @return Returns the sourceFile
     */
    public String getSourceFile() {
        return sourceFile;
    }

    /**
     * @param sourceFileIn The sourceFile to set.
     */
    public void setSourceFile(String sourceFileIn) {
        sourceFile = sourceFileIn;
    }

    @Override
    public boolean equals(Object other) {
        boolean result = false;
        if (other instanceof VirtualizationCreateActionDiskDetails) {
            VirtualizationCreateActionDiskDetails otherDisk = (VirtualizationCreateActionDiskDetails) other;
            result = Objects.equals(getType(), otherDisk.getType()) &&
                    Objects.equals(getDevice(), otherDisk.getDevice()) &&
                    Objects.equals(getTemplate(), otherDisk.getTemplate()) &&
                    getSize() == otherDisk.getSize() &&
                    Objects.equals(getBus(), otherDisk.getBus()) &&
                    Objects.equals(getPool(), otherDisk.getPool()) &&
                    Objects.equals(getSourceFile(), otherDisk.getSourceFile());
        }
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, device, template, size, bus, pool, sourceFile);
    }
}
