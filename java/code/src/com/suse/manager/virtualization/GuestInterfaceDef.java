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

/**
 * Class representing the VM interface device XML definition.
 */
public class GuestInterfaceDef {

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
     * @return Returns the source (network, bridge, etc depending on the type).
     */
    public String getSource() {
        return source;
    }

    /**
     * @param sourceIn The source to set.
     */
    public void setSource(String sourceIn) {
        source = sourceIn;
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

    /**
     * Parse the &lt;interface&gt; element of a domain XML definition
     *
     * @param element the interface element
     * @return the created interface object
     *
     * @throws IllegalArgumentException if the input element is badly formatted
     */
    public static GuestInterfaceDef parse(Element element) throws IllegalArgumentException {
        GuestInterfaceDef iface = new GuestInterfaceDef();
        iface.setType(element.getAttributeValue("type"));
        Element source = element.getChild("source");
        if (iface.getType() == null || source == null) {
            throw new IllegalArgumentException("invalid interface XML definition");
        }

        if (iface.getType().equals("network")) {
            iface.setSource(source.getAttributeValue("network"));
        }

        // MAC address may be omitted even though libvirt always outputs one.
        Element mac = element.getChild("mac");
        if (mac != null) {
            iface.setMac(mac.getAttributeValue("address"));
        }

        return iface;
    }
}
