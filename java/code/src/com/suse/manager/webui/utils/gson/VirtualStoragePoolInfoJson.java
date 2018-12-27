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
package com.suse.manager.webui.utils.gson;

import com.google.gson.JsonObject;

/**
 * Represents the virtual storage pool informations data to display in the UI.
 * Those data are directly passed over from Salt response.
 */
public class VirtualStoragePoolInfoJson {

    private String uuid;
    private String name;
    private boolean autostart;
    private boolean persistent;
    private String state;
    private String type;
    private String targetPath;
    private Long allocation;
    private Long capacity;
    private Long free;

    /**
     * Build up an instance from the data returned from salt virt.pool_info
     *
     * @param nameIn the name of the pool
     * @param values the JSON values returned by salt
     */
    public VirtualStoragePoolInfoJson(String nameIn, JsonObject values) {
        setName(nameIn);
        setUuid(values.get("uuid").getAsString());
        setAutostart(values.get("autostart").getAsInt() == 1);
        setPersistent(values.get("persistent").getAsInt() == 1);
        setState(values.get("state").getAsString());
        setType(values.get("type").getAsString());
        setTargetPath(values.get("target_path").isJsonNull() ? null : values.get("target_path").getAsString());
        setAllocation(values.get("allocation").getAsLong());
        setCapacity(values.get("capacity").getAsLong());
        setFree(values.get("free").getAsLong());
    }


    /**
     * @return Returns the uuid.
     */
    public String getUuid() {
        return uuid;
    }


    /**
     * @param uuidIn The uuid to set.
     */
    public void setUuid(String uuidIn) {
        uuid = uuidIn;
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
     * @return Returns the autostart.
     */
    public boolean isAutostart() {
        return autostart;
    }


    /**
     * @param autostartIn The autostart to set.
     */
    public void setAutostart(boolean autostartIn) {
        autostart = autostartIn;
    }


    /**
     * @return Returns the persistent.
     */
    public boolean isPersistent() {
        return persistent;
    }


    /**
     * @param persistentIn The persistent to set.
     */
    public void setPersistent(boolean persistentIn) {
        persistent = persistentIn;
    }


    /**
     * @return Returns the state.
     */
    public String getState() {
        return state;
    }


    /**
     * @param stateIn The state to set.
     */
    public void setState(String stateIn) {
        state = stateIn;
    }


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
     * @return Returns the targetPath.
     */
    public String getTargetPath() {
        return targetPath;
    }


    /**
     * @param targetPathIn The targetPath to set.
     */
    public void setTargetPath(String targetPathIn) {
        targetPath = targetPathIn;
    }


    /**
     * @return Returns the allocation.
     */
    public Long getAllocation() {
        return allocation;
    }


    /**
     * @param allocationIn The allocation to set.
     */
    public void setAllocation(Long allocationIn) {
        allocation = allocationIn;
    }


    /**
     * @return Returns the capacity.
     */
    public Long getCapacity() {
        return capacity;
    }


    /**
     * @param capacityIn The capacity to set.
     */
    public void setCapacity(Long capacityIn) {
        capacity = capacityIn;
    }


    /**
     * @return Returns the free.
     */
    public Long getFree() {
        return free;
    }


    /**
     * @param freeIn The free to set.
     */
    public void setFree(Long freeIn) {
        free = freeIn;
    }
}
