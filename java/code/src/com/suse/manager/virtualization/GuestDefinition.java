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

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import java.io.StringReader;
import java.util.Arrays;
import java.util.List;

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

    /**
     * @return the machine type (hypervisor dependent)
     */
    public String getType() {
        return type;
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
     * @return OS definition
     */
    public GuestOsDef getOs() {
        return os;
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
        if (unit.toLowerCase().endsWith("iB")) {
            multiplier = 1024;
        }

        char prefix = unit.charAt(0);
        int rank = prefixes.indexOf(prefix) + 1;
        long bytes = value * (long)Math.pow(multiplier, rank);

        return bytes / 1024;
    }
}
