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
package com.suse.manager.webui.controllers.virtualization.gson;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.util.List;

/**
 * Represents the virtual storage volume informations data to display in the UI.
 * Those data are directly passed over from Salt response.
 */
public class VirtualStorageVolumeInfoJson {

    private String name;
    private String type;
    private String key;
    private String path;
    private Long capacity;
    private Long allocation;
    private List<String> usedBy;

    /**
     * Build up an instance from the data returned from salt virt.volume_infos
     *
     * @param nameIn the name of the volume
     * @param values the JSON values returned by salt
     */
    @SuppressWarnings("serial")
    public VirtualStorageVolumeInfoJson(String nameIn, JsonObject values) {
        name = nameIn;
        type = values.get("type").getAsString();
        key = values.get("key").getAsString();
        path = values.get("path").getAsString();
        allocation = values.get("allocation").getAsLong();
        capacity = values.get("capacity").getAsLong();
        usedBy = (new Gson().fromJson(values.get("used_by").getAsJsonArray(),
                new TypeToken<List<String>>() { }.getType()));
    }


    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }


    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }


    /**
     * @return Returns the key.
     */
    public String getKey() {
        return key;
    }


    /**
     * @return Returns the path.
     */
    public String getPath() {
        return path;
    }


    /**
     * @return Returns the capacity.
     */
    public Long getCapacity() {
        return capacity;
    }


    /**
     * @return Returns the allocation.
     */
    public Long getAllocation() {
        return allocation;
    }


    /**
     * @return Returns the names of the domains using the volume (not exhaustive).
     */
    public List<String> getUsedBy() {
        return usedBy;
    }
}
