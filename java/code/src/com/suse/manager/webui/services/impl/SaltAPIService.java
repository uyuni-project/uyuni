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

import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.webui.services.SaltService;
import com.suse.manager.webui.services.SaltCustomStateStorageManager;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.manager.webui.utils.salt.Zypper;
import com.suse.manager.webui.utils.salt.LocalCallWithMetadata;
import com.suse.manager.webui.utils.salt.Timezone;
import com.suse.manager.webui.utils.salt.custom.MainframeSysinfo;
import com.suse.manager.webui.utils.salt.custom.SumaUtil;
import com.suse.manager.webui.utils.salt.custom.Udevdb;
import com.suse.salt.netapi.AuthModule;
import com.suse.salt.netapi.calls.LocalAsyncResult;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.WheelResult;
import com.suse.salt.netapi.calls.modules.Cmd;
import com.suse.salt.netapi.calls.modules.Grains;
import com.suse.salt.netapi.calls.modules.Match;
import com.suse.salt.netapi.calls.modules.Network;
import com.suse.salt.netapi.calls.modules.Pkg;
import com.suse.salt.netapi.calls.modules.SaltUtil;
import com.suse.salt.netapi.calls.modules.Schedule;
import com.suse.salt.netapi.calls.modules.Smbios;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.calls.modules.Status;
import com.suse.salt.netapi.calls.modules.Test;
import com.suse.salt.netapi.calls.wheel.Key;
import com.suse.salt.netapi.client.SaltClient;
import com.suse.salt.netapi.config.ClientConfig;
import com.suse.salt.netapi.datatypes.target.Glob;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.datatypes.target.Target;
import com.suse.salt.netapi.event.EventStream;
import com.suse.salt.netapi.exception.SaltException;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.HashSet;
import java.util.stream.Stream;

/**
 * Singleton class acting as a service layer for accessing the salt API.
 */
public enum SaltAPIService implements SaltService {

    // Singleton instance of this class
    INSTANCE;

    // Logger
    private static final Logger LOG = Logger.getLogger(SaltAPIService.class);

    // Salt properties
    private final URI SALT_MASTER_URI = URI.create("http://localhost:9080");
    private final String SALT_USER = "admin";
    private final String SALT_PASSWORD = "";
    private final AuthModule AUTH_MODULE = AuthModule.AUTO;
    // JSON serializer
    private static final Gson GSON = new GsonBuilder().create();

    // Shared salt client instance
    private final SaltClient SALT_CLIENT = new SaltClient(SALT_MASTER_URI);

    private SaltCustomStateStorageManager customSaltStorageManager =
            new SaltCustomStateStorageManager();

    // Prevent instantiation
    SaltAPIService() {
        // Set timeout to 30 seconds
        SALT_CLIENT.getConfig().put(ClientConfig.SOCKET_TIMEOUT, 30000);
    }

    /**
     * {@inheritDoc}
     */
    public <R> Optional<R> syncSingleMinion(LocalCall<R> call, String minionId) {
        try {
            Map<String, R> stringRMap = call.callSync(SALT_CLIENT, new MinionList(minionId),
                    SALT_USER, SALT_PASSWORD, AUTH_MODULE);
            Optional<R> result = Optional.ofNullable(stringRMap.get(minionId));
            if (!result.isPresent()) {
                LOG.warn("Got no result for " + call.getPayload().get("fun") +
                        " on minion " + minionId + " (minion did not respond in time)");
            }
            return result;
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Key.Names getKeys() {
        try {
            WheelResult<Key.Names> result = Key.listAll()
                    .callSync(SALT_CLIENT, SALT_USER, SALT_PASSWORD, AUTH_MODULE);
            return result.getData().getResult();
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Key.Fingerprints getFingerprints() {
        try {
            WheelResult<Key.Fingerprints> result = Key.finger("*")
                    .callSync(SALT_CLIENT, SALT_USER, SALT_PASSWORD, AUTH_MODULE);
            return result.getData().getResult();
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Object> getGrains(String minionId) {
        try {
            Map<String, Map<String, Object>> grains = Grains.items(false).callSync(
                    SALT_CLIENT, new MinionList(minionId),
                    SALT_USER, SALT_PASSWORD, AUTH_MODULE);
            return grains.getOrDefault(minionId, new HashMap<>());
        }
        catch (SaltException e) {
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
    public Map<String, String> getTimezoneOffsets(Target<?> target) {
        try {
            Map<String, String> offsets = Timezone.getOffset().callSync(SALT_CLIENT, target,
                    SALT_USER, SALT_PASSWORD, AUTH_MODULE);
            return offsets;
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, List<String>> getPackages(String minionId) {
        try {
            Map<String, Map<String, List<String>>> packages = Pkg.listPkgs().callSync(
                    SALT_CLIENT, new MinionList(minionId),
                    SALT_USER, SALT_PASSWORD, AUTH_MODULE);
            return packages.get(minionId);
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void acceptKey(String match) {
        try {
            Key.accept(match).callSync(SALT_CLIENT,
                    SALT_USER, SALT_PASSWORD, AUTH_MODULE);
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteKey(String minionId) {
        try {
            Key.delete(minionId).callSync(SALT_CLIENT,
                    SALT_USER, SALT_PASSWORD, AUTH_MODULE);
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void rejectKey(String minionId) {
        try {
            Key.reject(minionId).callSync(SALT_CLIENT,
                    SALT_USER, SALT_PASSWORD, AUTH_MODULE);
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public LocalAsyncResult<Map<String, Object>> applyState(Target<?> target,
                                                       List<String> mods) {
        try {
            return State.apply(mods).callAsync(SALT_CLIENT, target,
                    SALT_USER, SALT_PASSWORD, AUTH_MODULE);
        }
        catch (SaltException e) {
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
            SaltClient client = new SaltClient(SALT_MASTER_URI);
            client.login(SALT_USER, SALT_PASSWORD, AUTH_MODULE);
            client.getConfig().put(ClientConfig.SOCKET_TIMEOUT, 0);
            return client.events();
        }
        catch (SaltException e) {
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
            Map<String, Map<String, Object>> grains = Grains.item(true, grain).callSync(
                    SALT_CLIENT, new MinionList(minionId),
                    SALT_USER, SALT_PASSWORD, AUTH_MODULE);
            return grains.getOrDefault(minionId, new HashMap<>()).get(grain);
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Pkg.Info>
            getInstalledPackageDetails(String minionId, List<String> attributes) {
        try {
            Map<String, Map<String, Pkg.Info>> packages =
                Pkg.infoInstalled(attributes, true).callSync(
                    SALT_CLIENT,
                    new MinionList(minionId),
                    SALT_USER, SALT_PASSWORD, AUTH_MODULE
                );
            return packages.get(minionId);
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, String> runRemoteCommand(Target<?> target, String cmd) {
        try {
            Map<String, String> result = Cmd.run(cmd).callSync(
                    SALT_CLIENT, target,
                    SALT_USER, SALT_PASSWORD, AuthModule.AUTO);
            return result;
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Boolean> match(String target) {
        try {
            Map<String, Boolean> result = Match.glob(target).callSync(
                    SALT_CLIENT, new Glob(target),
                    SALT_USER, SALT_PASSWORD, AuthModule.AUTO);
            return result;
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean sendEvent(String tag, Object data) {
        try {
            SaltClient client = new SaltClient(SALT_MASTER_URI);
            client.login(SALT_USER, SALT_PASSWORD, AUTH_MODULE);
            client.getConfig().put(ClientConfig.SOCKET_TIMEOUT, 30000);
            return client.sendEvent(tag, GSON.toJson(data));
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Object> getCpuInfo(String minionId) {
        try {
            Map<String, Map<String, Object>> stats = Status.cpuinfo().callSync(
                    SALT_CLIENT, new MinionList(minionId),
                    SALT_USER, SALT_PASSWORD, AuthModule.AUTO);
            return stats.get(minionId);
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Optional<Map<String, Object>> getDmiRecords(String minionId,
            Smbios.RecordType recordType) {
        return syncSingleMinion(Smbios.records(recordType), minionId).map(col ->
                col.isEmpty() ? Collections.emptyMap() : col.get(0).getData()
        );
    }

    /**
     * {@inheritDoc}
     */
    public void syncGrains(String target) {
        try {
            SaltUtil.syncGrains(Optional.empty(), Optional.empty()).callSync(SALT_CLIENT,
                    new Glob(target), SALT_USER, SALT_PASSWORD, AuthModule.AUTO);
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void syncModules(String target) {
        try {
            SaltUtil.syncModules(Optional.empty(), Optional.empty()).callSync(SALT_CLIENT,
                    new Glob(target), SALT_USER, SALT_PASSWORD, AuthModule.AUTO);
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public Optional<List<Map<String, Object>>> getUdevdb(String minionId) {
        return syncSingleMinion(Udevdb.exportdb(), minionId);
    }

    /**
     * {@inheritDoc}
     */
    public Optional<String> getFileContent(String minionId, String path) {
        return syncSingleMinion(SumaUtil.cat(path), minionId);
    }

    /**
     * {@inheritDoc}
     */
    public Optional<String> getMainframeSysinfoReadValues(String minionId) {
        return syncSingleMinion(MainframeSysinfo.readValues(), minionId);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Schedule.Result> schedule(String name,
            LocalCall<?> call, Target<?> target, ZonedDateTime scheduleDate,
            Map<String, ?> metadata) throws SaltException {
        // We do one Salt call per timezone: group minions by their timezone offsets
        Map<String, String> minionOffsets = getTimezoneOffsets(target);
        Map<String, List<String>> offsetMap = minionOffsets.keySet().stream()
                .collect(Collectors.groupingBy(minionOffsets::get));
        if (LOG.isDebugEnabled()) {
            LOG.debug("Minions grouped by timezone offsets: " + offsetMap);
        }

        // The return type is a map of minion ids to their schedule results
        return offsetMap.entrySet().stream().flatMap(entry -> {
            LocalDateTime targetScheduleDate = scheduleDate.toOffsetDateTime()
                    .withOffsetSameInstant(ZoneOffset.of(entry.getKey())).toLocalDateTime();
            try {
                Target<?> timezoneTarget = new MinionList(entry.getValue());
                Map<String, Schedule.Result> result = Schedule
                        .add(name, call, targetScheduleDate, metadata)
                        .callSync(SALT_CLIENT, timezoneTarget,
                                SALT_USER, SALT_PASSWORD, AuthModule.AUTO);
                return result.entrySet().stream();
            }
            catch (SaltException e) {
                LOG.error(String.format("Error scheduling actions: %s", e.getMessage()));
                return Stream.empty();
            }
        }).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * {@inheritDoc}
     */
    public <T> Map<String, T> callSync(LocalCall<T> call, Target<?> target,
            Map<String, ?> metadata) throws SaltException {
        LocalCallWithMetadata<T> callWithMetadata =
                new LocalCallWithMetadata<>(call, metadata);
        return callWithMetadata
                .callSync(SALT_CLIENT, target, SALT_USER, SALT_PASSWORD, AuthModule.AUTO);
    }

    /**
     * {@inheritDoc}
     */
    public <T> LocalAsyncResult<T> callAsync(LocalCall<T> call, Target<?> target,
            Map<String, ?> metadata) throws SaltException {
        LocalCallWithMetadata<T> callWithMetadata =
                new LocalCallWithMetadata<>(call, metadata);
        return callWithMetadata
                .callAsync(SALT_CLIENT, target, SALT_USER, SALT_PASSWORD, AuthModule.AUTO);
    }

    /**
     * {@inheritDoc}
     */
    public Map<String, Schedule.Result> deleteSchedule(String name, Target<?> target) {
        try {
            return Schedule.delete(name).callSync(
                    SALT_CLIENT, target,
                    SALT_USER, SALT_PASSWORD, AuthModule.AUTO);
        }
        catch (SaltException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * {@inheritDoc}
     */
    public Optional<Map<String, Network.Interface>> getNetworkInterfacesInfo(
            String minionId) {
        return syncSingleMinion(Network.interfaces(), minionId);
    }

    /**
     * {@inheritDoc}
     */
    public Optional<Map<SumaUtil.IPVersion, SumaUtil.IPRoute>> getPrimaryIps(
            String minionId) {
        return syncSingleMinion(SumaUtil.primaryIps(), minionId);
    }

    /**
     * {@inheritDoc}
     */
    public Optional<Map<String, String>> getNetModules(String minionId) {
        return syncSingleMinion(SumaUtil.getNetModules(), minionId);
    }

    /**
     * {@inheritDoc}
     */
    public Optional<List<Zypper.ProductInfo>> getInstalledProducts(String minionId) {
        return syncSingleMinion(Zypper.listProducts(false), minionId);
    }

    /**
     * {@inheritDoc}
     */
    public Set<String> getAllowedMinions(User user, String target) {
        Set<String> saltMatches = match(target).keySet();
        Set<String> allowed = new HashSet<>(saltMatches);

        List<String> minionIds = MinionServerFactory
                .findMinionIdsByOrgId(user.getOrg().getId());
        allowed.retainAll(minionIds);

        return allowed;
    }

    /**
     * {@inheritDoc}
     */
    public void saveCustomState(long orgId, String name, String content,
                                String oldName, String oldChecksum) {
        try {
            customSaltStorageManager.storeState(orgId, name, content, oldName, oldChecksum);
            if (customSaltStorageManager.isRename(oldName, name)) {
                // for some reason the following native query does not trigger a flush
                // and the new name is not yet in the db
                StateFactory.getSession().flush();

                SaltStateGeneratorService.INSTANCE.regenerateCustomStates(orgId, name);
            }
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public void deleteCustomState(long orgId, String name) {
        try {
            SaltStateGeneratorService.INSTANCE.regenerateCustomStates(orgId, name);
            customSaltStorageManager.deleteState(orgId, name);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<String> getCatalogStates(long orgId) {
        return customSaltStorageManager.listByOrg(orgId);
    }

    /**
     * {@inheritDoc}
     */
    public Optional<String> getOrgStateContent(long orgId, String name) {
        try {
            return customSaltStorageManager.getContent(orgId, name);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    public boolean orgStateExists(long orgId, String name) {
        return customSaltStorageManager.exists(orgId, name);
    }

    /**storageManager
     * {@inheritDoc}
     */
    public Set<String> resolveOrgStates(long orgId, Set<String> states) {
        return states.stream().map(state -> customSaltStorageManager
                .getOrgNamespace(orgId) + "." + state)
                .collect(Collectors.toSet());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Boolean> ping(Target<?> targetIn) throws SaltException {
        return callSync(
            Test.ping(),
            targetIn,
            Collections.emptyMap()
        );
    }
}
