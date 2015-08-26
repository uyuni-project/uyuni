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

import com.redhat.rhn.domain.user.User;
import com.suse.saltstack.netapi.AuthModule;
import com.suse.saltstack.netapi.calls.WheelResult;
import com.suse.saltstack.netapi.calls.modules.Grains;
import com.suse.saltstack.netapi.calls.modules.Pkg;
import com.suse.saltstack.netapi.calls.runner.Manage;
import com.suse.saltstack.netapi.calls.wheel.Key;
import com.suse.saltstack.netapi.client.SaltStackClient;
import com.suse.saltstack.netapi.config.ClientConfig;
import com.suse.saltstack.netapi.datatypes.Keys;
import com.suse.saltstack.netapi.datatypes.target.Glob;
import com.suse.saltstack.netapi.exception.SaltStackException;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class containing functions for accessing data needed by MinionsController.
 */
public class MinionsModel {

    // The salt URI as string
    private static final URI saltMasterURI = URI.create("http://localhost:9080");

    private MinionsModel() { }

    /**
     * Get the minion keys from salt-api with their respective status.
     *
     * @param user the user to use for connecting to salt-api
     * @return the keys with their respective status as returned from salt-api
     */
    public static Keys getKeys(User user) {
        SaltStackClient client;
        try {
            client = new SaltStackClient(saltMasterURI);
            // FIXME: Pass on actual user credentials as soon as it is supported
            WheelResult<Keys> result = client.callSync(Key.listAll(), "admin", "", AuthModule.AUTO);
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
    public static Map<String, Object> grains(String minionKey) {
        SaltStackClient client;
        try {
            client = new SaltStackClient(saltMasterURI);
            client.getConfig().put(ClientConfig.SOCKET_TIMEOUT, 0);
            // FIXME: Pass on actual user credentials as soon as it is supported
            Map<String, Map<String, Object>> grains = client.callSync(Grains.items(true), new Glob(minionKey), "admin", "", AuthModule.AUTO);
            return grains.getOrDefault(minionKey, new HashMap<>());
        }
        catch (SaltStackException e) {
            throw new RuntimeException(e);
        }
    }

    public static List<String> up() {
        SaltStackClient client;
        try {
            client = new SaltStackClient(saltMasterURI);
            client.getConfig().put(ClientConfig.SOCKET_TIMEOUT, 0);
            // FIXME: Pass on actual user credentials as soon as it is supported
            List<String> up = client.callSync(Manage.up(), "admin", "", AuthModule.AUTO);
            return up;
        }
        catch (SaltStackException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Query all present minions according to salts presence detection
     *
     * @return the list of minion keys that are present
     */
    public static List<String> present() {
        SaltStackClient client;
        try {
            client = new SaltStackClient(saltMasterURI);
            client.getConfig().put(ClientConfig.SOCKET_TIMEOUT, 0);
            // FIXME: Pass on actual user credentials as soon as it is supported
            List<String> present = client.callSync(Manage.present(), "admin", "", AuthModule.AUTO);
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
    public static Map<String, List<String>> packages(String minionKey) {
        SaltStackClient client;
        try {
            client = new SaltStackClient(saltMasterURI);
            client.getConfig().put(ClientConfig.SOCKET_TIMEOUT, 0);
            // FIXME: Pass on actual user credentials as soon as it is supported
            Map<String, Map<String, List<String>>> packages = client.callSync(Pkg.listPkgs(), new Glob(minionKey), "admin", "", AuthModule.AUTO);
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
    public static void accept(String minionKey) {
        SaltStackClient client;
        try {
            client = new SaltStackClient(saltMasterURI);
            // FIXME: Pass on actual user credentials as soon as it is supported
            client.callSync(Key.accept(minionKey), "admin", "", AuthModule.AUTO);
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
    public static void delete(String minionKey) {
        SaltStackClient client;
        try {
            client = new SaltStackClient(saltMasterURI);
            // FIXME: Pass on actual user credentials as soon as it is supported
            client.callSync(Key.delete(minionKey), "admin", "", AuthModule.AUTO);
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
    public static void reject(String minionKey) {
        SaltStackClient client;
        try {
            client = new SaltStackClient(saltMasterURI);
            // FIXME: Pass on actual user credentials as soon as it is supported
            client.callSync(Key.reject(minionKey), "admin", "", AuthModule.AUTO);
        }
        catch (SaltStackException e) {
            throw new RuntimeException(e);
        }
    }
}
