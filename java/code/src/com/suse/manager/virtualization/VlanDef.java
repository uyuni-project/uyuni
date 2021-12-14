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

import com.google.gson.annotations.SerializedName;

import org.jdom.Element;

import java.util.Optional;

/**
 * Represents a virtual network VLAN configuration
 */
public class VlanDef {
    private int tag;

    @SerializedName("native")
    private Optional<String> nativeMode = Optional.empty();

    /**
     * @return the VLAN tag
     */
    public int getTag() {
        return tag;
    }

    /**
     * @param tagIn the VLAN tag
     */
    public void setTag(int tagIn) {
        tag = tagIn;
    }

    /**
     * @return the native mode (tagged or untagged) for open vSwitch
     */
    public Optional<String> getNativeMode() {
        return nativeMode;
    }

    /**
     * @param nativeModeIn the native mode (tagged or untagged) for open vSwitch
     */
    public void setNativeMode(Optional<String> nativeModeIn) {
        nativeMode = nativeModeIn;
    }

    /**
     * Parse tag XML node
     *
     * @param node the non-null node
     * @return the parsed VlanDef
     */
    public static VlanDef parse(Element node) {
        VlanDef def = new VlanDef();
        def.setTag(Integer.parseInt(node.getAttributeValue("id")));
        def.setNativeMode(Optional.ofNullable(node.getAttributeValue("nativeMode")));
        return def;
    }
}
