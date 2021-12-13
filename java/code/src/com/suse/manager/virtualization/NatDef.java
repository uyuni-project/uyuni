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
 * Represents virtual network NAT configuration
 */
public class NatDef {
    private Optional<Range<String>> address = Optional.empty();
    private Optional<Range<Integer>> port = Optional.empty();

    /**
     * @return NAT address range
     */
    public Optional<Range<String>> getAddress() {
        return address;
    }

    /**
     * @param addressIn NAT address range
     */
    public void setAddress(Optional<Range<String>> addressIn) {
        address = addressIn;
    }

    /**
     * @return NAT port range
     */
    public Optional<Range<Integer>> getPort() {
        return port;
    }

    /**
     * @param portIn NAT port range
     */
    public void setPort(Optional<Range<Integer>> portIn) {
        port = portIn;
    }

    /**
     * Parse the nat element of the network XML definition
     *
     * @param node the XML node
     * @return the created Nat definition
     */
    public static NatDef parseNat(Element node) {
        NatDef def = null;
        if (node != null) {
            def = new NatDef();
            def.setPort(Range.parse(node.getChild("port"), Integer::parseInt));
            def.setAddress(Range.parse(node.getChild("address")));
        }
        return def;
    }
}
