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
package com.suse.manager.webui.services.iface;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.redhat.rhn.domain.server.MinionServer;
import com.suse.manager.virtualization.GuestDefinition;
import com.suse.manager.virtualization.PoolCapabilitiesJson;
import com.suse.manager.virtualization.PoolDefinition;

import java.util.Map;
import java.util.Optional;

/**
 * Service providing utility functions to handle virtual machines.
 */
public interface VirtManager {

    /**
     * Query virtual machine definition
     *
     * @param minionId the host minion ID
     * @param domainName the domain name to look for
     * @return the XML definition or an empty Optional
     */
    Optional<GuestDefinition> getGuestDefinition(String minionId, String domainName);

    /**
     * Query virtual host and domains capabilities.
     *
     * @param minionId the salt minion virtual host to ask about
     * @return the output of the salt virt.all_capabilities call in JSON
     */
    Optional<Map<String, JsonElement>> getCapabilities(String minionId);

    /**
     * Query virtual storage pool capabilities
     *
     * @param minionId the salt minion virtual host to ask about
     * @return the output of the salt virt.pool_capabilities call
     */
    Optional<PoolCapabilitiesJson> getPoolCapabilities(String minionId);

    /**
     * Query virtual storage pool definition
     *
     * @param minionId the host minion ID
     * @param poolName the domain name to look for
     * @return the XML definition or an empty Optional
     */
    Optional<PoolDefinition> getPoolDefinition(String minionId, String poolName);

    /**
     * Query the list of virtual networks defined on a salt minion.
     *
     * @param minionId the minion to ask about
     * @return a list of the network names
     */
    Map<String, JsonObject> getNetworks(String minionId);

    /**
     * Query the list of virtual storage pools defined on a salt minion.
     *
     * @param minionId the minion to ask about
     * @return a map associating pool names with their informations as Json elements
     */
    Map<String, JsonObject> getPools(String minionId);

    /**
     * Query the list of virtual storage volumes defined on a salt minion.
     *
     * @param minionId the minion to ask about
     * @return a map associating pool names with the list of volumes it contains mapped by their names
     */
    Map<String, Map<String, JsonObject>> getVolumes(String minionId);

    /**
     * Update libvirt engine on a given minion.
     * @param minion to update.
     */
    void updateLibvirtEngine(MinionServer minion);

}
