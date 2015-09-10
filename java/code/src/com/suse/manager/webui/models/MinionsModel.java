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
package com.suse.manager.webui.models;

import com.suse.saltstack.netapi.AuthModule;
import com.suse.saltstack.netapi.calls.WheelResult;
import com.suse.saltstack.netapi.calls.modules.Grains;
import com.suse.saltstack.netapi.calls.modules.Pkg;
import com.suse.saltstack.netapi.calls.runner.Manage;
import com.suse.saltstack.netapi.calls.wheel.Key;
import com.suse.saltstack.netapi.client.SaltStackClient;
import com.suse.saltstack.netapi.config.ClientConfig;
import com.suse.saltstack.netapi.datatypes.Keys;
import com.suse.saltstack.netapi.datatypes.target.MinionList;
import com.suse.saltstack.netapi.event.EventStream;
import com.suse.saltstack.netapi.exception.SaltStackException;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class containing functions for accessing data needed by MinionsController.
 */
public class MinionsModel {

    // Salt properties
    private static final URI SALT_MASTER_URI = URI.create("http://localhost:9080");
    private static final String ADMIN_NAME = "admin";
    private static final String ADMIN_PASSWORD = "";
    private static final AuthModule AUTH_MODULE = AuthModule.AUTO;

    // Shared salt client instance
    private static final SaltStackClient SALT_CLIENT = new SaltStackClient(SALT_MASTER_URI);

    // Singleton instance of this class
    private static final MinionsModel INSTANCE = new MinionsModel();

    // Prevent instantiation
    private MinionsModel() {
        // Set timeout to 30 seconds
        SALT_CLIENT.getConfig().put(ClientConfig.SOCKET_TIMEOUT, 30000);
    }

    /**
     * Get the singleton instance of MinionsModel.
     *
     * @return singleton instance
     */
    public static MinionsModel getInstance() {
        return INSTANCE;
    }

    /**
     * Get the minion keys from salt-api with their respective status.
     *
     * @return the keys with their respective status as returned from salt-api
     */
    public Keys getKeys() {
        try {
            WheelResult<Keys> result = SALT_CLIENT.callSync(Key.listAll(),
                    ADMIN_NAME, ADMIN_PASSWORD, AUTH_MODULE);
            return result.getData().getResult();
        }
        catch (SaltStackException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the grains from a minion.
     *
     * @param minionKey key of the target minion
     * @return a map containing the grains
     */
    public Map<String, Object> grains(String minionKey) {
        try {
            Map<String, Map<String, Object>> grains = SALT_CLIENT.callSync(
                    Grains.items(true), new MinionList(minionKey),
                    ADMIN_NAME, ADMIN_PASSWORD, AUTH_MODULE);
            return grains.getOrDefault(minionKey, new HashMap<>());
        }
        catch (SaltStackException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the "machine_id" for a given minionId.
     *
     * @param minionId id of the target minion
     * @return a map containing the "machine_id" grain
     */
    public String getMachineId(String minionId) {
        return (String) getGrain(minionId, "machine_id");
    }

    /**
     * Query all present minions according to salts presence detection
     *
     * @return the list of minion keys that are present
     */
    public List<String> present() {
        try {
            List<String> present = SALT_CLIENT.callSync(Manage.present(),
                    ADMIN_NAME, ADMIN_PASSWORD, AUTH_MODULE);
            return present;
        }
        catch (SaltStackException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *  Get the installed packages from a minion
     *
     * @param minionKey key of the target minion
     * @return a map from package names to list of version strings
     */
    public Map<String, List<String>> packages(String minionKey) {
        try {
            Map<String, Map<String, List<String>>> packages = SALT_CLIENT.callSync(
                    Pkg.listPkgs(), new MinionList(minionKey),
                    ADMIN_NAME, ADMIN_PASSWORD, AUTH_MODULE);
            return packages.get(minionKey);
        }
        catch (SaltStackException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Accept a pending minion key
     *
     * @param minionKey key of the target minion
     */
    public void accept(String minionKey) {
        try {
            SALT_CLIENT.callSync(Key.accept(minionKey),
                    ADMIN_NAME, ADMIN_PASSWORD, AUTH_MODULE);
        }
        catch (SaltStackException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Delete a minion key from the master
     *
     * @param minionKey key of the target minion
     */
    public void delete(String minionKey) {
        try {
            SALT_CLIENT.callSync(Key.delete(minionKey),
                    ADMIN_NAME, ADMIN_PASSWORD, AUTH_MODULE);
        }
        catch (SaltStackException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reject a pending minion key
     *
     * @param minionKey key of the target minion
     */
    public void reject(String minionKey) {
        try {
            SALT_CLIENT.callSync(Key.reject(minionKey),
                    ADMIN_NAME, ADMIN_PASSWORD, AUTH_MODULE);
        }
        catch (SaltStackException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get the event stream from the /ws endpoint. Do not use the shared client object here,
     * so we can set the timeout to 0 (no timeout).
     *
     * @return the event stream
     */
    public EventStream getEventStream() {
        try {
            SaltStackClient client = new SaltStackClient(SALT_MASTER_URI);
            client.login(ADMIN_NAME, ADMIN_PASSWORD, AUTH_MODULE);
            client.getConfig().put(ClientConfig.SOCKET_TIMEOUT, 0);
            return client.events();
        }
        catch (SaltStackException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a given grain's value for a given minionId.
     *
     * @param minionId id of the target minion
     * @param grain name of the grain
     * @return the grain value
     */
    private Object getGrain(String minionId, String grain) {
        try {
            Map<String, Map<String, Object>> grains = SALT_CLIENT.callSync(
                    Grains.item(true, grain), new MinionList(minionId),
                    ADMIN_NAME, ADMIN_PASSWORD, AUTH_MODULE);
            return grains.getOrDefault(minionId, new HashMap<>()).get(grain);
        }
        catch (SaltStackException e) {
            throw new RuntimeException(e);
        }
    }
}
