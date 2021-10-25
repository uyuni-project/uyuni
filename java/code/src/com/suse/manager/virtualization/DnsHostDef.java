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

import java.util.ArrayList;
import java.util.List;

public class DnsHostDef {
    private String address;
    private List<String> names = new ArrayList<>();

    /**
     * @return value of address
     */
    public String getAddress() {
        return address;
    }

    /**
     * @param addressIn value of address
     */
    public void setAddress(String addressIn) {
        address = addressIn;
    }

    /**
     * @return value of names
     */
    public List<String> getNames() {
        return names;
    }

    /**
     * @param namesIn value of names
     */
    public void setNames(List<String> namesIn) {
        names = namesIn;
    }

    /**
     * Parse DNS host node
     * @param node the node to parse
     * @return the parsed host definition
     */
    public static DnsHostDef parse(Element node) {
        DnsHostDef def = new DnsHostDef();
        def.setAddress(node.getAttributeValue("ip"));
        for (Object child : node.getChildren("hostname")) {
            def.names.add(((Element)child).getTextTrim());
        }
        return def;
    }
}
