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
 * Represents the virtual network informations data to display in the UI.
 * Those data are directly passed over from Salt response.
 */
public class VirtualNetworkInfoJson {

    private String uuid;
    private String name;
    private String bridge;
    private boolean active;
    private boolean autostart;
    private boolean persistent;

    /**
     * Build up an instance from the data returned from salt virt.network_info
     *
     * @param nameIn the name of the network
     * @param values the JSON values returned by salt
     */
    public VirtualNetworkInfoJson(String nameIn, JsonObject values) {
        setName(nameIn);
        setUuid(values.get("uuid").getAsString());
        setBridge(values.get("bridge").getAsString());
        setActive(values.get("active").getAsInt() == 1);
        setAutostart(values.get("autostart").getAsInt() == 1);
        setPersistent(values.get("persistent").getAsInt() == 1);
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
     * @return Returns the bridge.
     */
    public String getBridge() {
        return bridge;
    }

    /**
     * @param bridgeIn The bridge to set.
     */
    public void setBridge(String bridgeIn) {
        bridge = bridgeIn;
    }

    /**
     * @return Returns the active.
     */
    public boolean isActive() {
        return active;
    }

    /**
     * @param activeIn The active to set.
     */
    public void setActive(boolean activeIn) {
        active = activeIn;
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
}
