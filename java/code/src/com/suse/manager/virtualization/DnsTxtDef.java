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

/**
 * Represents a virtual network DNS TXT record definition
 */
public class DnsTxtDef {
    private String name;
    private String value;

    /**
     * @return value of name
     */
    public String getName() {
        return name;
    }

    /**
     * @param nameIn value of name
     */
    public void setName(String nameIn) {
        name = nameIn;
    }

    /**
     * @return value of value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param valueIn value of value
     */
    public void setValue(String valueIn) {
        value = valueIn;
    }

    /**
     * Parse DNS TXT XML node
     * @param node the node to parse
     * @return the parsed TXT definition
     */
    public static DnsTxtDef parse(Element node) {
        DnsTxtDef def = new DnsTxtDef();
        def.setName(node.getAttributeValue("name"));
        def.setValue(node.getAttributeValue("value"));
        return def;
    }
}
