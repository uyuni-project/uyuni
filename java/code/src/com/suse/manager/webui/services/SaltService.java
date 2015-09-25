/**
 * Copyright (c) 2015 SUSE LLC
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
package com.suse.manager.webui.services;

import com.suse.saltstack.netapi.calls.wheel.Key;
import com.suse.saltstack.netapi.event.EventStream;

import java.util.List;
import java.util.Map;

/**
 * Service interface for accessing salt via the API.
 */
public interface SaltService {

    /**
     * Get the minion keys from salt with their respective status.
     *
     * @return the keys with their respective status as returned from salt
     */
    Key.Names getKeys();

    /**
     * Get the grains for a given minion.
     *
     * @param minionId id of the target minion
     * @return map containing the grains
     */
    Map<String, Object> getGrains(String minionId);

    /**
     * Get the machine id for a given minion.
     *
     * @param minionId id of the target minion
     * @return the machine id as a string
     */
    String getMachineId(String minionId);

    /**
     * Query all present minions according to salt's presence detection.
     *
     * @return the list of minion keys that are present
     */
    List<String> present();

    /**
     * Get all installed packages from a given minion.
     *
     * @param minionId id of the target minion
     * @return a map from package names to list of version strings
     */
    Map<String, List<String>> getPackages(String minionId);

    /**
     * Accept a given minion's key.
     *
     * @param minionId id of the minion
     */
    void acceptKey(String minionId);

    /**
     * Delete a given minion's key.
     *
     * @param minionId id of the minion
     */
    void deleteKey(String minionId);

    /**
     * Reject a given minion's key.
     *
     * @param minionId id of the minion
     */
    void rejectKey(String minionId);

    /**
     * Return the stream of events happening in salt.
     *
     * @return the event stream
     */
    EventStream getEventStream();
}
