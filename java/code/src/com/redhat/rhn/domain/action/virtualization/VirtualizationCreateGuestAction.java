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

import java.util.List;
import java.util.Objects;

/**
 * CreateAction - Class representing TYPE_VIRTUALIZATION_CREATE
 */
public class VirtualizationCreateGuestAction extends BaseVirtualizationGuestAction {

    private static final long serialVersionUID = 5911199267745279497L;

    private Long id;
    private String type;
    private String guestName;
    private String osType;
    private Long memory;
    private Long vcpus;
    private String arch;
    private List<VirtualizationCreateActionDiskDetails> disks;
    private boolean removeDisks;
    private List<VirtualizationCreateActionInterfaceDetails> interfaces;
    private boolean removeInterfaces;
    private String graphicsType;
    private String cobblerSystem;
    private String kickstartHost;
    private String kernelOptions;

    /**
     * @return the ID in the DB
     */
    @Override
    public Long getId() {
        return id;
    }

    /**
     * @param idIn the action details ID in the DB
     */
    @Override
    public void setId(Long idIn) {
        id = idIn;
    }

    /**
     * @return the domain type (kvm, qemu, linux, xen...)
     */
    public String getType() {
        return type;
    }

    /**
     * @param typeIn the domain type (kvm, qemu, linux, xen...)
     */
    public void setType(String typeIn) {
        type = typeIn;
    }

    /**
     * @return the virtual machine name
     */
    public String getGuestName() {
        return guestName;
    }

    /**
     * @param vmNameIn the virtual machine name
     */
    public void setGuestName(String vmNameIn) {
        this.guestName = vmNameIn;
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
     * @return the amount of memory to allocate to the VM
     */
    public Long getMemory() {
        return memory;
    }

    /**
     * @param memoryIn the amount of memory to allocate to the VM
     */
    public void setMemory(Long memoryIn) {
        this.memory = memoryIn;
    }

    /**
     * @return the number of virtual CPUs of the VM
     */
    public Long getVcpus() {
        return vcpus;
    }

    /**
     * @param vcpusIn the number of virtual CPUs of the VM
     */
    public void setVcpus(Long vcpusIn) {
        this.vcpus = vcpusIn;
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
     * @return the disks definitions for the VM
     */
    public List<VirtualizationCreateActionDiskDetails> getDisks() {
        return disks;
    }

    /**
     * @param disksIn the disks definitions for the VM
     */
    public void setDisks(List<VirtualizationCreateActionDiskDetails> disksIn) {
        this.disks = disksIn;
    }

    /**
     * @return true to remove disks if there are no disks definitions.
     */
    public boolean isRemoveDisks() {
        return removeDisks;
    }

    /**
     * Since the database can't distinguish between an empty list and null,
     * we need to set a flag to differentiate those cases.
     *
     * @param remove whether to remove all disks or not
     */
    public void setRemoveDisks(boolean remove) {
        removeDisks = remove;
    }

    /**
     * @return the virtual network interface definitions for the VM
     */
    public List<VirtualizationCreateActionInterfaceDetails> getInterfaces() {
        return interfaces;
    }

    /**
     * @param interfacesIn the virtual network interface definitions for the VM
     */
    public void setInterfaces(
            List<VirtualizationCreateActionInterfaceDetails> interfacesIn) {
        this.interfaces = interfacesIn;
    }

    /**
     * @return true to remove interfaces if there are no interfaces definitions.
     */
    public boolean isRemoveInterfaces() {
        return removeInterfaces;
    }

    /**
     * Since the Database can't distinguish between an empty list and null,
     * we need to set a flag to differentiate those cases.
     *
     * @param remove whether to remove all network interfaces or not
     */
    public void setRemoveInterfaces(boolean remove) {
        removeInterfaces = remove;
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
     * @return the ID of the cobbler profile to use to create the VM
     */
    public String getCobblerSystem() {
        return cobblerSystem;
    }

    /**
     * @param cobblerSystemIn the ID of the cobbler profile to use to create the VM
     */
    public void setCobblerSystem(String cobblerSystemIn) {
        cobblerSystem = cobblerSystemIn;
    }

    /**
     * @return the kickstart host URL
     */
    public String getKickstartHost() {
        return kickstartHost;
    }

    /**
     * @param kickstartHostIn the kickstart host URL
     */
    public void setKickstartHost(String kickstartHostIn) {
        kickstartHost = kickstartHostIn;
    }

    /**
     * @return the kernel options to use with cobbler profile
     */
    public String getKernelOptions() {
        return kernelOptions;
    }

    /**
     * @param kernelOptionsIn the kernel options to use with cobbler profile
     */
    public void setKernelOptions(String kernelOptionsIn) {
        kernelOptions = kernelOptionsIn;
    }

    @Override
    public boolean equals(Object other) {
        boolean result = false;
        if (other instanceof VirtualizationCreateGuestAction) {
            VirtualizationCreateGuestAction otherAction = (VirtualizationCreateGuestAction)other;
            result = Objects.equals(getType(), otherAction.getType()) &&
                    Objects.equals(getGuestName(), otherAction.getGuestName()) &&
                    Objects.equals(getOsType(), otherAction.getOsType()) &&
                    getMemory().longValue() == otherAction.getMemory().longValue() &&
                    getVcpus().longValue() == otherAction.getVcpus().longValue() &&
                    Objects.equals(getArch(), otherAction.getArch()) &&
                    Objects.equals(getCobblerSystem(), otherAction.getCobblerSystem()) &&
                    Objects.equals(getKickstartHost(), otherAction.getKickstartHost()) &&
                    Objects.equals(getKernelOptions(), otherAction.getKernelOptions()) &&
                    isRemoveDisks() == otherAction.isRemoveDisks() &&
                    Objects.equals(getDisks(), otherAction.getDisks()) &&
                    isRemoveInterfaces() == otherAction.isRemoveInterfaces() &&
                    Objects.equals(getInterfaces(), otherAction.getInterfaces());
        }
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, guestName, osType, memory, vcpus, arch, removeDisks, disks, removeInterfaces,
                interfaces, cobblerSystem, kickstartHost, kernelOptions);
    }
}
