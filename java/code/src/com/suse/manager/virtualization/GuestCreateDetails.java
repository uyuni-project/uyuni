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

import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.webui.controllers.virtualization.gson.VirtualGuestsUpdateActionJson;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import java.time.LocalDateTime;

/**
 * Represents the data stored for the VM create or update actions.
 */
public class GuestCreateDetails extends VirtualGuestsUpdateActionJson {

    private static final Gson GSON = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
            .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
            .serializeNulls()
            .create();

    @SerializedName("remove_disks")
    private boolean removeDisks;
    @SerializedName("remove_interfaces")
    private boolean removeInterfaces;
    @SerializedName("kickstart_host")
    private String kickstartHost;
    @SerializedName("cobbler_system")
    private String cobblerSystem;

    /**
     * Create object instance from JSON string
     *
     * @param json the json representation to be parsed
     * @return the created instance
     */
    public static GuestCreateDetails parse(String json) {
        return GSON.fromJson(json, new TypeToken<GuestCreateDetails>() { }.getType());
    }

    /**
     * @return JSON representation
     */
    public String toJson() {
        return GSON.toJson(this);
    }

    /**
     * @return value of removeDisks
     */
    public boolean isRemoveDisks() {
        return removeDisks;
    }

    /**
     * @param removeDisksIn value of removeDisks
     */
    public void setRemoveDisks(boolean removeDisksIn) {
        removeDisks = removeDisksIn;
    }

    /**
     * @return value of removeInterfaces
     */
    public boolean isRemoveInterfaces() {
        return removeInterfaces;
    }

    /**
     * @param removeInterfacesIn value of removeInterfaces
     */
    public void setRemoveInterfaces(boolean removeInterfacesIn) {
        removeInterfaces = removeInterfacesIn;
    }

    /**
     * @return value of kickstartHost
     */
    public String getKickstartHost() {
        return kickstartHost;
    }

    /**
     * @param kickstartHostIn value of kickstartHost
     */
    public void setKickstartHost(String kickstartHostIn) {
        kickstartHost = kickstartHostIn;
    }

    /**
     * @return value of cobblerSystem
     */
    public String getCobblerSystem() {
        return cobblerSystem;
    }

    /**
     * @param cobblerSystemIn value of cobblerSystem
     */
    public void setCobblerSystem(String cobblerSystemIn) {
        cobblerSystem = cobblerSystemIn;
    }
}
