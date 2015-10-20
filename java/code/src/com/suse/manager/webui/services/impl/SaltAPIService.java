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
package com.suse.manager.webui.services.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.suse.manager.webui.services.SaltService;
import com.suse.saltstack.netapi.AuthModule;
import com.suse.saltstack.netapi.calls.WheelResult;
import com.suse.saltstack.netapi.calls.modules.Cmd;
import com.suse.saltstack.netapi.calls.modules.Grains;
import com.suse.saltstack.netapi.calls.modules.Match;
import com.suse.saltstack.netapi.calls.modules.Pkg;
import com.suse.saltstack.netapi.calls.runner.Manage;
import com.suse.saltstack.netapi.calls.wheel.Key;
import com.suse.saltstack.netapi.client.SaltStackClient;
import com.suse.saltstack.netapi.config.ClientConfig;
import com.suse.saltstack.netapi.datatypes.target.Glob;
import com.suse.saltstack.netapi.datatypes.target.MinionList;
import com.suse.saltstack.netapi.datatypes.target.Target;
import com.suse.saltstack.netapi.event.EventStream;
import com.suse.saltstack.netapi.exception.SaltStackException;

import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton class acting as a service layer for accessing the salt API.
 */
public enum SaltAPIService implements SaltService {

    // Singleton instance of this class
    INSTANCE;

    // Salt properties
    private final URI SALT_MASTER_URI = URI.create("http://localhost:9080");
    private final String SALT_USER = "admin";
    private final String SALT_PASSWORD = "";
    private final AuthModule AUTH_MODULE = AuthModule.AUTO;
    // JSON serializer
    private static final Gson GSON = new GsonBuilder().create();

    // Shared salt client instance
    private final SaltStackClient SALT_CLIENT = new SaltStackClient(SALT_MASTER_URI);

    // Prevent instantiation
    SaltAPIService() {
        // Set timeout to 30 seconds
        SALT_CLIENT.getConfig().put(ClientConfig.SOCKET_TIMEOUT, 30000);
    }

    /**
     * {@inheritDoc}
     */
    public Key.Names getKeys() {
        try {
            WheelResult<Key.Names> result = SALT_CLIENT.callSync(Key.listAll(),
                    SALT_USER, SALT_PASSWORD, AUTH_MODULE);
            return result.getData().getResult();
        }
        catch (SaltStackException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Object> getGrains(String minionId) {
        try {
            Map<String, Map<String, Object>> grains = SALT_CLIENT.callSync(
                    Grains.items(true), new MinionList(minionId),
                    SALT_USER, SALT_PASSWORD, AUTH_MODULE);
            return grains.getOrDefault(minionId, new HashMap<>());
        }
        catch (SaltStackException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getMachineId(String minionId) {
        return (String) getGrain(minionId, "machine_id");
    }

    /**
     * {@inheritDoc}
     */
    public List<String> present() {
        try {
            List<String> present = SALT_CLIENT.callSync(Manage.present(),
                    SALT_USER, SALT_PASSWORD, AUTH_MODULE);
            return present;
        }
        catch (SaltStackException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, List<String>> getPackages(String minionId) {
        try {
            Map<String, Map<String, List<String>>> packages = SALT_CLIENT.callSync(
                    Pkg.listPkgs(), new MinionList(minionId),
                    SALT_USER, SALT_PASSWORD, AUTH_MODULE);
            return packages.get(minionId);
        }
        catch (SaltStackException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void acceptKey(String minionId) {
        try {
            SALT_CLIENT.callSync(Key.accept(minionId),
                    SALT_USER, SALT_PASSWORD, AUTH_MODULE);
        }
        catch (SaltStackException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteKey(String minionId) {
        try {
            SALT_CLIENT.callSync(Key.delete(minionId),
                    SALT_USER, SALT_PASSWORD, AUTH_MODULE);
        }
        catch (SaltStackException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void rejectKey(String minionId) {
        try {
            SALT_CLIENT.callSync(Key.reject(minionId),
                    SALT_USER, SALT_PASSWORD, AUTH_MODULE);
        }
        catch (SaltStackException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     *
     * Do not use the shared client object here, so we can disable the timeout (set to 0).
     */
    public EventStream getEventStream() {
        try {
            SaltStackClient client = new SaltStackClient(SALT_MASTER_URI);
            client.login(SALT_USER, SALT_PASSWORD, AUTH_MODULE);
            client.getConfig().put(ClientConfig.SOCKET_TIMEOUT, 0);
            return client.events();
        }
        catch (SaltStackException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Get a given grain's value from a given minion.
     *
     * @param minionId id of the target minion
     * @param grain name of the grain
     * @return the grain value
     */
    private Object getGrain(String minionId, String grain) {
        try {
            Map<String, Map<String, Object>> grains = SALT_CLIENT.callSync(
                    Grains.item(true, grain), new MinionList(minionId),
                    SALT_USER, SALT_PASSWORD, AUTH_MODULE);
            return grains.getOrDefault(minionId, new HashMap<>()).get(grain);
        }
        catch (SaltStackException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Pkg.Info> getInstalledPackageDetails(String minionId) {
        try {
            Map<String, Map<String, Pkg.Info>> packages = SALT_CLIENT.callSync(
                    Pkg.infoInstalled(), new MinionList(minionId),
                    SALT_USER, SALT_PASSWORD, AUTH_MODULE);
            return packages.get(minionId);
        }
        catch (SaltStackException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> runRemoteCommand(Target<?> target, String cmd) {
        try {
            Map<String, String> result = SALT_CLIENT.callSync(
                    Cmd.run(cmd), target,
                    SALT_USER, SALT_PASSWORD, AuthModule.AUTO);
            return result;
        }
        catch (SaltStackException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Boolean> match(String target) {
        try {
            Map<String, Boolean> result = SALT_CLIENT.callSync(
                    Match.glob(target), new Glob(target),
                    SALT_USER, SALT_PASSWORD, AuthModule.AUTO);
            return result;
        }
        catch (SaltStackException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean sendEvent(String tag, Object data) {
        try {
            SaltStackClient client = new SaltStackClient(SALT_MASTER_URI);
            client.login(SALT_USER, SALT_PASSWORD, AUTH_MODULE);
            client.getConfig().put(ClientConfig.SOCKET_TIMEOUT, 30000);
            return client.sendEvent(tag, GSON.toJson(data));
        }
        catch (SaltStackException e) {
            throw new RuntimeException(e);
        }
    }
}
