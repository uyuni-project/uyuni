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
package com.suse.manager.webui.utils.gson;

import com.google.gson.annotations.SerializedName;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * VirtualGuestsUpdate represents the JSON data for the virtual guests
 * update or create action. A create action just doesn't have any uuid
 * defined.
 */
public class VirtualGuestsUpdateActionJson extends VirtualGuestsBaseActionJson {
    private Long vcpu;
    private String type;
    private String name;
    private String osType;
    private String arch;
    private Long memory;
    private List<DiskData> disks;
    private List<InterfaceData> interfaces;
    private String graphicsType;
    private LocalDateTime earliest;
    private Optional<String> actionChain = Optional.empty();

    /**
     * @return the domain type (kvm, qemu, linux, xen...)
     */
    public String getType() {
        return type;
    }

    /**
     *
     * @param typeIn the domain type (kvm, qemu, linux, xen...)
     */
    public void setType(String typeIn) {
        type = typeIn;
    }

    /**
     * @return the VM name
     */
    public String getName() {
        return name;
    }

    /**
     * @param nameIn the VM name
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @return the VM type (HVM / PV)
     */
    public String getOsType() {
        return osType;
    }

    /**
     * @param osTypeIn the VM type (HVM / PV)
     */
    public void setOsType(String osTypeIn) {
        this.osType = osTypeIn;
    }

    /**
     * @return the VM CPU architecture
     */
    public String getArch() {
        return arch;
    }

    /**
     * @param archIn the VM CPU architecture
     */
    public void setArch(String archIn) {
        this.arch = archIn;
    }

    /**
     * @return number of VCPUs to set
     */
    public Long getVcpu() {
        return vcpu;
    }

    /**
     * @param vcpuIn number of VCPUs to set
     */
    public void setVcpu(Long vcpuIn) {
        vcpu = vcpuIn;
    }

    /**
     * @return amount of memory in KiB to set
     */
    public Long getMemory() {
        return memory;
    }

    /**
     * @param memoryIn amount of memory in MB to set
     */
    public void setMemory(Long memoryIn) {
        memory = memoryIn;
    }
    /**
     * @return the disks definitions for the VM
     */
    public List<DiskData> getDisks() {
        return disks;
    }

    /**
     * @param disksIn the disks definitions for the VM
     */
    public void setDisks(List<DiskData> disksIn) {
        this.disks = disksIn;
    }

    /**
     * @return the virtual NIC definitions for the VM
     */
    public List<InterfaceData> getInterfaces() {
        return interfaces;
    }

    /**
     * @param interfacesIn the virtual NIC definitions for the VM
     */
    public void setInterfaces(List<InterfaceData> interfacesIn) {
        this.interfaces = interfacesIn;
    }

    /**
     * @return the graphics type of the VM (VNC / Spice / None)
     */
    public String getGraphicsType() {
        return graphicsType;
    }

    /**
     * @param graphicsTypeIn the graphics type of the VM (VNC / Spice / None)
     */
    public void setGraphicsType(String graphicsTypeIn) {
        this.graphicsType = graphicsTypeIn;
    }

    /**
     * @return the earliest
     */
    public LocalDateTime getEarliest() {
        return earliest;
    }

    /**
     * @return actionChain to get
     */
    public Optional<String> getActionChain() {
        return actionChain;
    }

    /**
     * Class describing the JSON disk data
     */
    public class DiskData {
        private String type;
        private String device;
        private String template;
        private long size = 0;
        private String bus;
        private String pool;

        @SerializedName("source_file")
        private String sourceFile;

        /**
         * @return Returns the type.
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
         * @return Returns the device.
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
         * @return Returns the source file.
         */
        public String getSourceFile() {
            return sourceFile;
        }

        /**
         * @param sourceFileIn The source_file to set.
         */
        public void setSourceFile(String sourceFileIn) {
            sourceFile = sourceFileIn;
        }

        /**
         * @return the disk image template URI
         */
        public String getTemplate() {
            return template;
        }

        /**
         * @param templateIn the disk image template URI
         */
        public void setTemplate(String templateIn) {
            this.template = templateIn;
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
         * @return the disk bus type to use
         */
        public String getBus() {
            return bus;
        }

        /**
         * @param busIn the disk bus type to use
         */
        public void setBus(String busIn) {
            this.bus = busIn;
        }

        /**
         * @return where the disk will be located
         */
        public String getPool() {
            return pool;
        }

        /**
         * @param poolIn where the disk will be located
         */
        public void setPool(String poolIn) {
            pool = poolIn;
        }
    }

    /**
     * Class describing the JSON network data
     */
    public class InterfaceData {
        private String type;
        private String source;
        private String mac;

        /**
         * @return Returns the type.
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
         * @return the name of the defined network to use
         */
        public String getSource() {
            return source;
        }

        /**
         * @param networkIn the name of the defined network to use
         */
        public void setSource(String networkIn) {
            this.source = networkIn;
        }

        /**
         * @return Returns the mac.
         */
        public String getMac() {
            return mac;
        }

        /**
         * @param macIn The mac to set.
         */
        public void setMac(String macIn) {
            mac = macIn;
        }
    }
}
