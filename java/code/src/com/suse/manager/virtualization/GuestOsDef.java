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

import org.jdom.Element;

import java.util.Arrays;
import java.util.List;

/**
 * Class representing the libvirt domain definition &lt;os&gt; element.
 */
public class GuestOsDef {

    private String arch;
    private String type;
    private String machine;

    /**
     * @return VM architecture
     */
    public String getArch() {
        return arch;
    }

    /**
     * Set the VM architecture.
     *
     * @param archIn the VM architecture
     */
    public void setArch(String archIn) {
        this.arch = archIn;
    }

    /**
     * @return Virtual Machine type. One of "hvm", "linux", "xen", "exe" or "uml"
     */
    public String getType() {
        return type;
    }

    /**
     * Set the virtual machine type.
     *
     * @param typeIn one of "hvm", "linux", "xen", "exe" or "uml"
     */
    public void setType(String typeIn) {
        List<String> types = Arrays.asList("hvm", "linux", "xen", "exe", "uml");
        if (types.contains(typeIn)) {
            this.type = typeIn;
        }
    }

    /**
     * @return VM machine
     */
    public String getMachine() {
        return machine;
    }

    /**
     * Set the machine VM ID. This value is depending on the underlying hypervisor.
     *
     * @param machineIn the machine type
     */
    public void setMachine(String machineIn) {
        this.machine = machineIn;
    }

    /**
     * Parse the libvirt &lt;os&gt; element
     *
     * @param element XML element
     *
     * @return the OS definition
     */
    public static GuestOsDef parse(Element element) {
        GuestOsDef def = new GuestOsDef();

        Element typeElement = element.getChild("type");
        def.setArch(typeElement.getAttributeValue("arch"));
        def.setType(typeElement.getTextTrim());
        def.setMachine(typeElement.getAttributeValue("machine"));

        return def;
    }

}
