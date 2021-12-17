/*
 * Copyright (c) 2021 SUSE LLC
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

import java.util.Optional;

public class DhcpHostDef {
    private String ip;
    private Optional<String> mac = Optional.empty();
    private Optional<String> id = Optional.empty();
    private Optional<String> name = Optional.empty();

    /**
     * @return IP address
     */
    public String getIp() {
        return ip;
    }

    /**
     * @param ipIn IP address
     */
    public void setIp(String ipIn) {
        ip = ipIn;
    }

    /**
     * @return MAC address for IPv4 DHCP hosts
     */
    public Optional<String> getMac() {
        return mac;
    }

    /**
     * @param macIn MAC address for IPv4 DHCP hosts
     */
    public void setMac(Optional<String> macIn) {
        mac = macIn;
    }

    /**
     * @return DUID for DHCPv6 hosts
     */
    public Optional<String> getId() {
        return id;
    }

    /**
     * @param idIn DUID for DHCPv6 hosts
     */
    public void setId(Optional<String> idIn) {
        id = idIn;
    }

    /**
     * @return host name
     */
    public Optional<String> getName() {
        return name;
    }

    /**
     * @param nameIn host name
     */
    public void setName(Optional<String> nameIn) {
        name = nameIn;
    }

    /**
     * Parse DHCP host XML node
     * @param node node to parse
     * @return the parsed DHCP host definition
     */
    public static DhcpHostDef parse(Element node) {
        DhcpHostDef def = new DhcpHostDef();
        def.setIp(node.getAttributeValue("ip"));
        def.setId(Optional.ofNullable(node.getAttributeValue("id")));
        def.setMac(Optional.ofNullable(node.getAttributeValue("mac")));
        def.setName(Optional.ofNullable(node.getAttributeValue("name")));
        return def;
    }
}

