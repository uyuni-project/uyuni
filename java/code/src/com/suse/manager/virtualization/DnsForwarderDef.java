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

/**
 * Represents a virtual network DNS forwarder definition
 */
public class DnsForwarderDef {
    private Optional<String> domain;
    private Optional<String> address;

    /**
     * @return value of domain
     */
    public Optional<String> getDomain() {
        return domain;
    }

    /**
     * @param domainIn value of domain
     */
    public void setDomain(Optional<String> domainIn) {
        domain = domainIn;
    }

    /**
     * @return value of address
     */
    public Optional<String> getAddress() {
        return address;
    }

    /**
     * @param addressIn value of address
     */
    public void setAddress(Optional<String> addressIn) {
        address = addressIn;
    }

    /**
     * Parse dns forwarder node
     *
     * @param node the node to parse
     * @return the parsed forwarder definition
     */
    public static DnsForwarderDef parse(Element node) {
        DnsForwarderDef def = new DnsForwarderDef();
        def.setAddress(Optional.ofNullable(node.getAttributeValue("addr")));
        def.setDomain(Optional.ofNullable(node.getAttributeValue("domain")));
        return def;
    }
}
