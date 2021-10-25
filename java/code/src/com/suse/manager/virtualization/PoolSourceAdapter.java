/*
 * Copyright (c) 2019 SUSE LLC
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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class representing the adapter parameters for SCSI sources.
 */
public class PoolSourceAdapter {
    private String type;
    private String name;
    private String parent;
    private boolean managed = false;
    private String wwnn;
    private String wwpn;
    private String parentWwnn;
    private String parentWwpn;
    private String parentFabricWwn;
    private String parentAddressUid;
    private String parentAddress;

    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }

    /**
     * @param typeIn The type to set. Should be one of 'scsi_host' or 'fc_host'.
     */
    public void setType(String typeIn) {
        type = typeIn;
    }

    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }

    /**
     * @param nameIn The name to set.
     */
    public void setName(String nameIn) {
        name = nameIn;
    }

    /**
     * @return Returns the parent.
     */
    public String getParent() {
        return parent;
    }

    /**
     * @param parentIn The parent to set.
     */
    public void setParent(String parentIn) {
        parent = parentIn;
    }

    /**
     * @return Returns true if the vHBA should be destroyed with the pool.
     */
    public boolean isManaged() {
        return managed;
    }

    /**
     * @param managedIn true if the vHBA should be destroyed with the pool.
     */
    public void setManaged(boolean managedIn) {
        managed = managedIn;
    }

    /**
     * @return Returns the wwnn.
     */
    public String getWwnn() {
        return wwnn;
    }

    /**
     * @param wwnnIn The wwnn to set.
     */
    public void setWwnn(String wwnnIn) {
        wwnn = wwnnIn;
    }

    /**
     * @return Returns the wwpn.
     */
    public String getWwpn() {
        return wwpn;
    }

    /**
     * @param wwpnIn The wwpn to set.
     */
    public void setWwpn(String wwpnIn) {
        wwpn = wwpnIn;
    }

    /**
     * @return Returns the parent wwnn.
     */
    public String getParentWwnn() {
        return parentWwnn;
    }

    /**
     * @param parentWwnnIn The parent wwnn to set.
     */
    public void setParentWwnn(String parentWwnnIn) {
        parentWwnn = parentWwnnIn;
    }

    /**
     * @return Returns the parent wwpn.
     */
    public String getParentWwpn() {
        return parentWwpn;
    }

    /**
     * @param parentWwpnIn The parent wwpn to set.
     */
    public void setParentWwpn(String parentWwpnIn) {
        parentWwpn = parentWwpnIn;
    }

    /**
     * @return Returns the parent fabric_wwn.
     */
    public String getParentFabricWwn() {
        return parentFabricWwn;
    }

    /**
     * @param parentFabricWwnIn The parent fabric_wwn to set.
     */
    public void setParentFabricWwn(String parentFabricWwnIn) {
        parentFabricWwn = parentFabricWwnIn;
    }

    /**
     * @return Returns the parent address unique id.
     */
    public String getParentAddressUid() {
        return parentAddressUid;
    }

    /**
     * @param parentAddressUidIn The parent address unique id to set.
     */
    public void setParentAddressUid(String parentAddressUidIn) {
        parentAddressUid = parentAddressUidIn;
    }

    /**
     * @return Returns the parent address domain part.
     */
    public String getParentAddress() {
        return parentAddress;
    }

    /**
     * @param parentAddressIn The parent PCI address.
     * @throws IllegalArgumentException if the value is not in format 0000:00:00.0
     */
    public void setParentAddress(String parentAddressIn) throws IllegalArgumentException {
        if (parentAddressIn != null &&
                !parentAddressIn.matches("^([0-9a-fA-F]{4}):([0-9a-fA-F]{2}):([0-9a-fA-F]{2}).([0-9a-fA-F])$")) {
            throw new IllegalArgumentException("Parent address not in format 0000:00:00.0: " + parentAddressIn);
        }
        parentAddress = parentAddressIn;
    }

    /**
     * @return Returns the parent PCI address parsed into a map with keys 'domain', 'bus', 'slot' and 'function'
     */
    public Map<String, String> getParentAddressParsed() {
        Matcher matcher = Pattern.compile("^([0-9a-fA-F]{4}):([0-9a-fA-F]{2}):([0-9a-fA-F]{2}).([0-9a-fA-F])$")
            .matcher(parentAddress);
        Map<String, String> result = new HashMap<>();
        result.put("domain", matcher.group(1));
        result.put("bus", matcher.group(2));
        result.put("slot", matcher.group(3));
        result.put("function", matcher.group(4));
        return result;
    }

    /**
     * Extract the data from the libvirt pool XML source adapter element.
     *
     * @param node the source adapter XML element
     * @return the created source adapter
     * @throws IllegalArgumentException if the node is missing required attributes or children
     */
    public static PoolSourceAdapter parse(Element node) throws IllegalArgumentException {
        PoolSourceAdapter result = null;
        if (node != null) {
            result = new PoolSourceAdapter();
            result.setType(node.getAttributeValue("type"));
            result.setName(node.getAttributeValue("name"));
            Element parentAddr = node.getChild("parentaddr");
            if (parentAddr != null) {
                result.setParentAddressUid(parentAddr.getAttributeValue("unique_id"));
                Element addr = parentAddr.getChild("address");
                if (addr == null) {
                    throw new IllegalArgumentException("Missing source adapter parent address PCI address");
                }
                result.setParentAddress(String.format("%s:%s:%s.%s",
                        addr.getAttributeValue("domain"),
                        addr.getAttributeValue("bus"),
                        addr.getAttributeValue("slot"),
                        addr.getAttributeValue("function")).replaceAll("0x", ""));
            }
            result.setParent(node.getAttributeValue("parent"));
            result.setManaged("yes".equals(node.getAttributeValue("managed")));
            result.setParentWwnn(node.getAttributeValue("parent_wwnn"));
            result.setParentWwpn(node.getAttributeValue("parent_wwpn"));
            result.setParentFabricWwn(node.getAttributeValue("parent_fabric_wwn"));
            result.setWwnn(node.getAttributeValue("wwnn"));
            result.setWwpn(node.getAttributeValue("wwpn"));

            if ("fc_host".equals(result.getType()) && (result.getWwnn() == null || result.getWwpn() == null)) {
                throw new IllegalArgumentException("Missing mandatory wwnn or wwpn in source adapter");
            }
        }
        return result;
    }
}
