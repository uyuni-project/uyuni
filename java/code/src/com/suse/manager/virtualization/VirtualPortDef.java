/**
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

import java.util.Optional;

/**
 * Represents the virtual network virtualport configuration
 */
public class VirtualPortDef {
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
}
