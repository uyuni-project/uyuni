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
package com.suse.manager.virtualization;

import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.server.VirtualInstanceType;
import com.redhat.rhn.frontend.dto.VirtualSystemOverview;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Class representing the VM XML Definition.
 */
public class GuestDefinition {

    private static final Logger LOG = Logger.getLogger(GuestDefinition.class);

    private String type;
    private String name;
    private String uuid;
    private long memory;
    private long maxMemory;

    private GuestVcpuDef vcpu;
    private GuestOsDef os;
    private GuestGraphicsDef graphics;
    private List<GuestInterfaceDef> interfaces;
    private List<GuestDiskDef> disks;


    /**
     * Create a guest definition from a virtual system overview.
     *
     * This is to be used for guests hosted on traditional hosts.
     * Obviously only a minimal set of the definition data will be set
     * since the overview doesn't hold them all.
     *
     * @param guest the guest to read data from.
     */
    public GuestDefinition(VirtualSystemOverview guest) {
        String guid = guest.getUuid();
        setUuid(guid.replaceAll("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5"));
        setName(guest.getName());
        setMemory(guest.getMemory());
        setMaxMemory(guest.getMemory());

        GuestVcpuDef vcpuDef = new GuestVcpuDef();
        vcpuDef.setCurrent(guest.getVcpus().intValue());
        vcpuDef.setMax(guest.getVcpus().intValue());
        setVcpu(vcpuDef);
    }

    /**
     * @return the machine type (hypervisor dependent)
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
     * @return domain name
     */
    public String getName() {
        return name;
    }

    /**
     * Set the guest name
     *
     * @param nameIn guest name
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @return domain UUID
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @param uuidIn The uuid to set.
     */
    public void setUuid(String uuidIn) {
        uuid = uuidIn;
    }

    /**
     * @return current amount of memory in KiB.
     */
    public long getMemory() {
        return memory;
    }

    /**
     * Set the current amount of memory in KiB.
     *
     * Don't forget to change the maximum before raising the current amount
     * of memory or it will be capped.
     *
     * @param memoryIn current amount of memory
     */
    public void setMemory(long memoryIn) {
        this.memory = Math.min(this.maxMemory, memoryIn);
    }

    /**
     * @return the maximum amount of memory allocated to the guest in KiB
     */
    public long getMaxMemory() {
        return maxMemory;
    }

    /**
     * Set the maximum number of memory in KiB.
     *
     * If setting a value smaller than the current amount of memory, the
     * latter will be adjusted.
     *
     * @param maxIn maximum number of Virtual CPUs
     */
    public void setMaxMemory(long maxIn) {
        this.maxMemory = maxIn;
        this.memory = Math.min(this.maxMemory, this.memory);
    }

    /**
     * @return Vcpu definition
     */
    public GuestVcpuDef getVcpu() {
        return vcpu;
    }

    /**
     * @param vcpuIn The vcpu to set.
     */
    public void setVcpu(GuestVcpuDef vcpuIn) {
        vcpu = vcpuIn;
    }

    /**
     * @return OS definition
     */
    public GuestOsDef getOs() {
        return os;
    }

    /**
     * @param osIn The os to set.
     */
    public void setOs(GuestOsDef osIn) {
        os = osIn;
    }

    /**
     * Note that we are only storing one graphics device definition for the guest
     * even if libvirt schema allows more. I couldn't get libvirt to do anything
     * useful with a second one: this limitation should make sense.
     *
     * @return the graphic device definition
     */
    public GuestGraphicsDef getGraphics() {
        return graphics;
    }

    /**
     * Set the graphic device definition
     *
     * @param graphicsIn the definition to set
     */
    public void setGraphics(GuestGraphicsDef graphicsIn) {
        graphics = graphicsIn;
    }

    /**
     * @return Returns the interfaces.
     */
    public List<GuestInterfaceDef> getInterfaces() {
        return interfaces;
    }

    /**
     * @param interfacesIn The interfaces to set.
     */
    public void setInterfaces(List<GuestInterfaceDef> interfacesIn) {
        interfaces = interfacesIn;
    }

    /**
     * @return Returns the disks.
     */
    public List<GuestDiskDef> getDisks() {
        return disks;
    }

    /**
     * @param disksIn The disks to set.
     */
    public void setDisks(List<GuestDiskDef> disksIn) {
        disks = disksIn;
    }

    /**
     * Compute the virtual instance type from the VM OS definition.
     *
     * @return the VirtualInstanceType
     */
    public VirtualInstanceType getVirtualInstanceType() {
        // FIXME This logic is actually not perfect, but matches the legacy one
        VirtualInstanceType virtType = VirtualInstanceFactory.getInstance().getFullyVirtType();
        if (!getOs().getType().equals("hvm")) {
            virtType = VirtualInstanceFactory.getInstance().getParaVirtType();
        }
        return virtType;
    }

    /**
     * Create a new VM definition from a libvirt XML description.
     *
     * @param xmlDef libvirt XML domain definition
     * @return parsed definition or {@code null}
     */
    @SuppressWarnings("unchecked")
    public static GuestDefinition parse(String xmlDef) {
        GuestDefinition def = null;
        SAXBuilder builder = new SAXBuilder();
        builder.setValidation(false);

        try {
            Document doc = builder.build(new StringReader(xmlDef));
            def = new GuestDefinition();

            Element domainElement = doc.getRootElement();
            def.type = domainElement.getAttributeValue("type");
            def.setName(domainElement.getChildText("name"));
            def.uuid = domainElement.getChildText("uuid");
            def.setMemory(parseMemory(domainElement.getChild("currentMemory")));
            def.setMaxMemory(parseMemory(domainElement.getChild("memory")));

            def.vcpu = GuestVcpuDef.parse(domainElement.getChild("vcpu"));
            def.os = GuestOsDef.parse(domainElement.getChild("os"));

            Element devices = domainElement.getChild("devices");
            def.graphics = GuestGraphicsDef.parse(devices.getChild("graphics"));

            def.interfaces = ((List<Element>)devices.getChildren("interface")).stream()
                    .map(node -> GuestInterfaceDef.parse(node)).collect(Collectors.toList());
            def.disks = ((List<Element>)devices.getChildren("disk")).stream()
                    .map(node -> GuestDiskDef.parse(node)).collect(Collectors.toList());
        }
        catch (Exception e) {
            LOG.error("failed to parse libvirt XML definition: " + e.getMessage());
        }

        return def;
    }

    /**
     * Convert the memory value + unit pair into a KiB value
     *
     * @param element XML element with unit attribute and value as text
     * @return the amount in KiB
     */
    private static long parseMemory(Element element) {
        String unit = element.getAttributeValue("unit", "KiB");
        long value = Long.parseLong(element.getText());

        List<Character> prefixes = Arrays.asList('K', 'M', 'G', 'T');
        int multiplier = 1000;
        if (unit.endsWith("iB")) {
            multiplier = 1024;
        }

        char prefix = unit.charAt(0);
        int rank = prefixes.indexOf(prefix) + 1;
        long bytes = value * (long)Math.pow(multiplier, rank);

        return bytes / 1024;
    }

    /**
     * Default constructor shouldn't not be used. Either construct from
     * an XML definition to parse or a VirtualSystemOverview instance.
     */
    private GuestDefinition() {
    }
}
