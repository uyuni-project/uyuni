/*
 * Copyright (c) 2020 SUSE LLC
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

import com.suse.manager.webui.utils.gson.ScheduledRequestJson;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * VirtualVolumeBaseAction represents the generic virtual volume action request body structure.
 */
public class VirtualVolumeBaseActionJson extends ScheduledRequestJson {

    private Map<String, List<String>> volumes;


    /**
     * The result maps each pool names to a list of volumes to act on within that pool
     *
     * @return Returns the volumes map.
     */
    public Map<String, List<String>> getVolumes() {
        return volumes;
    }


    /**
     * @param volumesIn The volumes to set.
     */
    public void setVolumes(Map<String, List<String>> volumesIn) {
        volumes = volumesIn;
    }

    /**
     * @return the list of volumes in a &lt;poolName&gt;/&lt;volumeName&gt; form
     */
    public List<String> getVolumesPath() {
        return getVolumes().entrySet().stream()
                .flatMap(entry -> entry.getValue().stream().map(volume -> entry.getKey() + "/" + volume))
                .collect(Collectors.toList());
    }
}
