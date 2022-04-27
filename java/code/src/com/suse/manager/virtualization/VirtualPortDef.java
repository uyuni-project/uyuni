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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jdom.Attribute;
import org.jdom.Element;

import java.util.Optional;

/**
 * Represents the virtual network virtualport configuration
 */
public class VirtualPortDef {
    private static final Logger LOG = LogManager.getLogger(VirtualPortDef.class);

    private String type;

    @SerializedName("profileid")
    private Optional<String> profileId = Optional.empty();

    @SerializedName("interfaceid")
    private Optional<String> interfaceId = Optional.empty();

    @SerializedName("managerid")
    private Optional<String> managerId = Optional.empty();

    @SerializedName("typeid")
    private Optional<String> typeId = Optional.empty();

    @SerializedName("typeidversion")
    private Optional<String> typeIdVersion = Optional.empty();

    @SerializedName("instanceid")
    private Optional<String> instanceId = Optional.empty();

    /**
     * @return value of type
     */
    public String getType() {
        return type;
    }

    /**
     * @param typeIn value of type
     */
    public void setType(String typeIn) {
        type = typeIn;
    }

    /**
     * @return the profile Id
     */
    public Optional<String> getProfileId() {
        return profileId;
    }

    /**
     * @param profileIdIn the profile Id
     */
    public void setProfileId(Optional<String> profileIdIn) {
        profileId = profileIdIn;
    }

    /**
     * @return the interface Id
     */
    public Optional<String> getInterfaceId() {
        return interfaceId;
    }

    /**
     * @param interfaceIdIn the interface Id
     */
    public void setInterfaceId(Optional<String> interfaceIdIn) {
        interfaceId = interfaceIdIn;
    }

    /**
     * @return the manager Id
     */
    public Optional<String> getManagerId() {
        return managerId;
    }

    /**
     * @param managerIdIn the manager Id
     */
    public void setManagerId(Optional<String> managerIdIn) {
        managerId = managerIdIn;
    }

    /**
     * @return the type Id
     */
    public Optional<String> getTypeId() {
        return typeId;
    }

    /**
     * @param typeIdIn the type Id
     */
    public void setTypeId(Optional<String> typeIdIn) {
        typeId = typeIdIn;
    }

    /**
     * @return the type Id version
     */
    public Optional<String> getTypeIdVersion() {
        return typeIdVersion;
    }

    /**
     * @param typeIdVersionIn the type Id version
     */
    public void setTypeIdVersion(Optional<String> typeIdVersionIn) {
        typeIdVersion = typeIdVersionIn;
    }

    /**
     * @return the instance Id
     */
    public Optional<String> getInstanceId() {
        return instanceId;
    }

    /**
     * @param instanceIdIn the instance Id
     */
    public void setInstanceId(Optional<String> instanceIdIn) {
        instanceId = instanceIdIn;
    }

    /**
     * Extract information from virtualport JDOM XML Node
     *
     * @param node the node to parse
     *
     * @return the virtual port definition
     */
    public static Optional<VirtualPortDef> parse(Element node) {
        if (node == null) {
            return Optional.empty();
        }
        VirtualPortDef def = new VirtualPortDef();
        def.setType(node.getAttributeValue("type"));
        Element parametersNode = node.getChild("parameters");
        if (parametersNode != null) {
            for (Object attribute : parametersNode.getAttributes()) {
                Attribute attr = (Attribute)attribute;
                switch (attr.getName()) {
                    case "interfaceid":     def.setInterfaceId(Optional.of(attr.getValue())); break;
                    case "instanceid":      def.setInstanceId(Optional.of(attr.getValue())); break;
                    case "managerid":       def.setManagerId(Optional.of(attr.getValue())); break;
                    case "profileid":       def.setProfileId(Optional.of(attr.getValue())); break;
                    case "typeid":          def.setTypeId(Optional.of(attr.getValue())); break;
                    case "typeidversion":   def.setTypeIdVersion(Optional.of(attr.getValue())); break;
                    default:
                        LOG.error("Unexpected virtual port attribute: {}", attr.getName());
                }
            }
        }
        return Optional.of(def);
    }
}
