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
import java.util.Map;
import java.util.Objects;

/**
 *
 * VirtualizationCreateAction - Class representing TYPE_VIRTUALIZATION_CREATE
 */
public class VirtualizationCreateAction extends BaseVirtualizationAction {

    private static final long serialVersionUID = 5911199267745279497L;
    public static final String TYPE = "type";
    public static final String NAME = "name";
    public static final String OS_TYPE = "ostype";
    public static final String MEMORY = "memory";
    public static final String VCPUS = "vcpus";
    public static final String ARCH = "arch";
    public static final String GRAPHICS = "graphics";
    public static final String DISKS = "disks";
    public static final String INTERFACES = "interfaces";

    private Long id;
    private String type;
    private String name;
    private String osType;
    private Long memory;
    private Long vcpus;
    private String arch;
    private List<VirtualizationCreateActionDiskDetails> disks;
    private boolean removeDisks;
    private List<VirtualizationCreateActionInterfaceDetails> interfaces;
    private boolean removeInterfaces;
    private String graphicsType;

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
    @Override
    public String getName() {
        return name;
    }

    /**
     * @param vmNameIn the virtual machine name
     */
    @Override
    public void setName(String vmNameIn) {
        this.name = vmNameIn;
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
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public void extractParameters(Map context) {
        setType((String)context.get(TYPE));
        // So far the salt virt.update function doesn't allow renaming a guest,
        // and that is only possible for the KVM driver.
        setName((String)context.get(NAME));
        setOsType((String) context.get(OS_TYPE));
        setMemory((Long) context.get(MEMORY));
        setVcpus((Long) context.get(VCPUS));
        setArch((String) context.get(ARCH));
        setGraphicsType((String) context.get(GRAPHICS));

        List<VirtualizationCreateActionDiskDetails> disksParam =
                (List<VirtualizationCreateActionDiskDetails>) context.get(DISKS);
        if (disksParam != null) {
            disksParam.stream().forEach(detail -> detail.setAction(this));
            setDisks(disksParam);
        }
        setRemoveDisks(disksParam != null && disksParam.isEmpty());

        List<VirtualizationCreateActionInterfaceDetails> interfacesParam =
                (List<VirtualizationCreateActionInterfaceDetails>) context.get(INTERFACES);
        if (interfacesParam != null) {
            interfacesParam.stream().forEach(detail -> detail.setAction(this));
            setInterfaces(interfacesParam);
        }
        setRemoveInterfaces(interfacesParam != null && interfacesParam.isEmpty());
    }

    @Override
    public boolean equals(Object other) {
        boolean result = false;
        if (other instanceof VirtualizationCreateAction) {
            VirtualizationCreateAction otherAction = (VirtualizationCreateAction)other;
            result = Objects.equals(getType(), otherAction.getType()) &&
                    Objects.equals(getName(), otherAction.getName()) &&
                    Objects.equals(getOsType(), otherAction.getOsType()) &&
                    getMemory().longValue() == otherAction.getMemory().longValue() &&
                    getVcpus().longValue() == otherAction.getVcpus().longValue() &&
                    Objects.equals(getArch(), otherAction.getArch()) &&
                    isRemoveDisks() == otherAction.isRemoveDisks() &&
                    Objects.equals(getDisks(), otherAction.getDisks()) &&
                    isRemoveInterfaces() == otherAction.isRemoveInterfaces() &&
                    Objects.equals(getInterfaces(), otherAction.getInterfaces());
        }
        return result;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, name, osType, memory, vcpus, arch, removeDisks, disks, removeInterfaces, interfaces);
    }
}
