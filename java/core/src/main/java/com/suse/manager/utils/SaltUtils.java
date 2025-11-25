/*
 * Copyright (c) 2016--2021 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.manager.utils;

import static com.suse.manager.webui.services.SaltConstants.SCRIPTS_DIR;
import static com.suse.manager.webui.services.SaltConstants.SUMA_STATE_FILES_ROOT_PATH;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.product.SUSEProduct;
import com.redhat.rhn.domain.product.SUSEProductFactory;
import com.redhat.rhn.domain.product.Tuple2;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnpackage.PackageFactory;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.rhnpackage.PackageType;
import com.redhat.rhn.domain.server.InstalledPackage;
import com.redhat.rhn.domain.server.InstalledProduct;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.frontend.action.common.BadParameterException;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.errata.ErrataManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.webui.controllers.bootstrap.BootstrapError;
import com.suse.manager.webui.controllers.bootstrap.SaltBootstrapError;
import com.suse.manager.webui.controllers.utils.ContactMethodUtil;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.SystemQuery;
import com.suse.manager.webui.services.pillar.MinionPillarManager;
import com.suse.manager.webui.utils.YamlHelper;
import com.suse.manager.webui.utils.salt.custom.SystemInfo;
import com.suse.salt.netapi.calls.modules.Pkg;
import com.suse.salt.netapi.calls.modules.Pkg.Info;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.calls.modules.Zypper.ProductInfo;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.errors.SaltError;
import com.suse.salt.netapi.parser.JsonParser;
import com.suse.salt.netapi.results.Change;
import com.suse.salt.netapi.results.Ret;
import com.suse.salt.netapi.results.SSHResult;
import com.suse.salt.netapi.results.StateApplyResult;
import com.suse.salt.netapi.utils.Xor;
import com.suse.utils.Json;
import com.suse.utils.Opt;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * SaltUtils
 */
public class SaltUtils {

    /** Package-affecting Salt state module names. */
    private static final List<String> PKG_STATE_MODULES = Arrays.asList(
        "pkg.group_installed", "pkg.installed", "pkg.latest", "pkg.patch_installed",
        "pkg.purged", "pkg.removed", "pkg.uptodate", "product.all_installed"
    );

    /** Package-affecting Salt execution module names. */
    private static final List<String> PKG_EXECUTION_MODULES = Arrays.asList(
        "pkg.group_install", "pkg.install", "pkg.purge", "pkg.remove", "pkg.upgrade"
    );

    private static final Logger LOG = LogManager.getLogger(SaltUtils.class);
    private static final TaskomaticApi TASKOMATIC_API = new TaskomaticApi();

    private Path scriptsDir = Paths.get(SUMA_STATE_FILES_ROOT_PATH, SCRIPTS_DIR);

    private final SystemQuery systemQuery;
    private final SaltApi saltApi;

    private static final LocalizationService LOCALIZATION = LocalizationService.getInstance();

    /**
     * Enumerates results of handlePackageChanges().
     */
    public enum PackageChangeOutcome {
        /**
         * Changed packages have been persisted in the database.
         */
        DONE,
        /**
         * A separate full refresh is necessary.
         */
        NEEDS_REFRESHING,
        /**
         * A separate full refresh is necessary, but salt was updated and we need delay refreshing
         */
        NEEDS_DELAYED_REFRESHING
    }

    /**
     * Constructor for testing purposes.
     *
     * @param systemQueryIn the system query
     * @param saltApiIn the salt api
     */
    public SaltUtils(SystemQuery systemQueryIn, SaltApi saltApiIn) {
        this.saltApi = saltApiIn;
        this.systemQuery = systemQueryIn;
    }

    /**
     * Figure out if the list of packages has changed based on the result of a Salt call
     * given as JsonElement. This information is used to decide if we should trigger a
     * package list refresh.
     *
     * @param function the Salt function that was used
     * @param callResult the result of the call
     * @return true if installed packages have changed or unparsable json, otherwise false
     */
    public boolean shouldRefreshPackageList(Optional<Xor<String[], String>> function,
            Optional<JsonElement> callResult) {
        List<String> functions = function.map(x -> x.fold(Arrays::asList, List::of)).orElseGet(ArrayList::new);
        if (functions.stream().anyMatch(PKG_EXECUTION_MODULES::contains)) {
            return true;
        }
        if (functions.contains("state.apply")) {
            return Opt.fold(
                callResult.flatMap(SaltUtils::jsonEventToStateApplyResults),
                () -> false,
                results -> results.entrySet().stream()
                    .anyMatch(result -> extractFunction(result.getKey())
                        .map(fn -> fn.equals("mgrcompat.module_run") ?
                            result.getValue().getName()
                                    .map(x -> x.fold(Arrays::asList, List::of))
                                    .orElseGet(ArrayList::new)
                                    .stream().anyMatch(PKG_EXECUTION_MODULES::contains) :
                            PKG_STATE_MODULES.contains(fn)
                        ).orElse(false) &&
                        !result.getValue().getChanges().isEmpty()
                    ));
        }

        return false;
    }

    /**
     * Handles package updates by applying delta information or scheduling a full
     * refresh if necessary.
     *
     * @param function salt function
     * @param callResult salt result
     * @param server server to update
     * @return an outcome
     */
    public PackageChangeOutcome handlePackageChanges(Optional<Xor<String[], String>> function,
            JsonElement callResult, Server server) {
        final PackageChangeOutcome outcome;

        List<String> functions = function.map(x -> x.fold(Arrays::asList, List::of)).orElseGet(List::of);

        if (functions.isEmpty()) {
            LOG.error("NULL function for: {}{}", server.getName(), callResult);
            throw new BadParameterException("function must not be NULL");
        }

        if (functions.stream().anyMatch(PKG_STATE_MODULES::contains)) {
            Map<String, Change<Xor<String, List<Pkg.Info>>>> delta = Json.GSON.fromJson(
                callResult,
                new TypeToken<Map<String, Change<Xor<String, List<Pkg.Info>>>>>() { }
                .getType()
            );
            ErrataManager.insertErrataCacheTask(server);
            outcome = applyChangesFromStateModule(delta, server);
        }
        else if (functions.contains("state.apply")) {
            Map<String, JsonElement> apply = Json.GSON.fromJson(
                callResult, new TypeToken<Map<String, JsonElement>>() { }.getType());
            ErrataManager.insertErrataCacheTask(server);
            outcome = applyChangesFromStateApply(apply, server);
        }
        else {
            outcome = PackageChangeOutcome.DONE;
        }

        return outcome;
    }

    /**
     * Extract salt function/module information from state apply string
     * @param value state apply string
     * @return salt function / module info
     */
    public static Optional<String> extractFunction(String value) {
        String[] split = value.split("_\\|-");
        if (split.length == 4) {
            String module = split[0];
            String function = split[3];
            return Optional.of(module + "." + function);
        }
        else {
            LOG.error("Could not parse Salt function call: {}", value);
            return Optional.empty();
        }
    }

    /**
     * applies the package changes to the server or schedules a package list
     * refresh if not enough data is present
     * @param changes map of packages changes
     * @param server server to update
     * @return an outcome
     */
    public static PackageChangeOutcome applyChangesFromStateModule(
            Map<String, Change<Xor<String, List<Pkg.Info>>>> changes,
            Server server) {
        boolean fullRefreshNeeded = changes.entrySet().stream().anyMatch(
            e ->
                e.getKey().endsWith("-release") ||
                // Live patching requires refresh to fetch the updated LP version
                e.getKey().startsWith("kernel-livepatch-") ||
                (e.getValue().getNewValue().isLeft() &&
                 e.getValue().getOldValue().isLeft())
        );

        if (fullRefreshNeeded) {
            boolean needDelay = changes.entrySet()
                    .stream().anyMatch(e ->
                            e.getKey().startsWith("salt") ||
                                    e.getKey().equals("venv-salt-minion"));
            if (needDelay) {
                return PackageChangeOutcome.NEEDS_DELAYED_REFRESHING;
            }
            return PackageChangeOutcome.NEEDS_REFRESHING;
        }
        else {
            HibernateFactory.doWithoutAutoFlushing(() -> applyDeltaPackageInfo(changes, server));
            return PackageChangeOutcome.DONE;
        }
    }

    /**
     * Applies a package changeset to a server.
     *
     * @param changes the changes
     * @param server the server
     */
    private static void applyDeltaPackageInfo(
            Map<String, Change<Xor<String, List<Pkg.Info>>>> changes, Server server) {
        // normalise salts type madness
        Map<String, Change<List<Pkg.Info>>> collect = changes.entrySet().stream()
                .collect(
                Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().map(
                                xor -> xor.getOrElse(Collections::emptyList))
                )
        );
        Map<String, InstalledPackage> currentPackages = server.getPackages().stream()
                .collect(Collectors.toMap(
                        SaltUtils::packageToKey,
                        Function.identity()
                ));
        collect.entrySet().stream().forEach(e -> {
            String name = e.getKey();
            Change<List<Info>> change = e.getValue();

            // Sometimes Salt lists the same NEVRA twice, only with different installation timestamps.
            // Use a merge function is to ignore these duplicate entries.
            Map<String, Info> newPackages = change.getNewValue().stream()
                    .collect(Collectors.toMap(info -> packageToKey(name, info), Function.identity(), (a, b) -> a));

            change.getOldValue().stream().forEach(info -> {
                String key = packageToKey(name, info);
                if (!newPackages.containsKey(key)) {
                    Optional.ofNullable(currentPackages.get(key))
                    .ifPresent(ip -> server.getPackages().remove(ip));
                }
            });

            List<InstalledPackage> packagesToAdd = newPackages.values().stream()
                    .map(info -> Optional.ofNullable(currentPackages.get(packageToKey(name, info))))
                    .filter(Optional::isPresent).map(Optional::get).collect(Collectors.toList());

            Map<String, Tuple2<String, Info>> packagesToCreate = newPackages.values().stream()
                    .filter(info -> !currentPackages.containsKey(packageToKey(name, info)))
                    .collect(Collectors.toMap(info -> packageToKey(name, info), info -> new Tuple2<>(name, info)));

            packagesToAdd.addAll(createPackagesFromSalt(packagesToCreate, server));
            server.getPackages().addAll(packagesToAdd);
        });
        SystemManager.updateSystemOverview(server.getId());
    }

    /**
     * Extracts and applies package delta information and updates the server with it.
     *
     * @param apply map of state apply results
     * @param server server to update
     * @return an outcome
     */
    public static PackageChangeOutcome applyChangesFromStateApply(
            Map<String, JsonElement> apply, Server server) {
        List<StateApplyResult<JsonElement>> collect =
                apply.entrySet().stream()
                        .flatMap(e -> extractFunction(e.getKey()).<Stream<StateApplyResult<JsonElement>>>map(fn -> {
                    if (fn.equals("mgrcompat.module_run")) {
                        StateApplyResult<JsonElement> ap = Json.GSON.fromJson(
                                e.getValue(),
                                new TypeToken<StateApplyResult<JsonElement>>() {
                                }.getType()
                        );
                        if (
                            ap.getName()
                                    .map(x -> x.fold(Arrays::asList, List::of))
                                    .orElseGet(ArrayList::new)
                                    .stream().anyMatch(PKG_EXECUTION_MODULES::contains)
                        ) {
                            return Stream.of(ap);
                        }
                        else {
                            return Stream.empty();
                        }
                    }
                    else if (PKG_STATE_MODULES.contains(fn)) {
                        return Stream.of(
                                Json.GSON.<StateApplyResult<JsonElement>>fromJson(e.getValue(),
                                        new TypeToken<StateApplyResult<JsonElement>>() {
                                        }.getType()
                                )
                        );
                    }
                    else {
                        return Stream.empty();
                    }
                }).orElseGet(Stream::empty))
                // we sort by run order process multiple package changing states right
                .sorted(Comparator.comparingInt(StateApplyResult::getRunNum))
                .toList();
        for (StateApplyResult<JsonElement> value : collect) {
            Map<String, Change<Xor<String, List<Pkg.Info>>>> delta = extractPackageDelta(value.getChanges());
            PackageChangeOutcome changeOutcome = applyChangesFromStateModule(delta, server);
            if (changeOutcome != PackageChangeOutcome.DONE) {
                return changeOutcome;
            }
        }
        return PackageChangeOutcome.DONE;
    }

    private static Map<String, Change<Xor<String, List<Info>>>> extractPackageDelta(
            JsonElement json) {
        if (json.getAsJsonObject().has("ret")) {
            return
            Json.GSON.<Ret<Map<String, Change<Xor<String, List<Pkg.Info>>>>>>fromJson(
                json,
                new TypeToken<Ret<Map<String, Change<Xor<String, List<Pkg.Info>>>>>>() { }
                        .getType()
            ).getRet();
        }
        else if (json.getAsJsonObject().has("installed") && json.getAsJsonObject().has("removed")) {
            var installedRemoved =
            Json.GSON.<InstalledRemoved<Map<String, Change<Xor<String, List<Info>>>>>>fromJson(
                json,
                new TypeToken<InstalledRemoved<Map<String, Change<Xor<String, List<Info>>>>>>() { }
                        .getType()
            );

            var delta = new HashMap<>(installedRemoved.getInstalled());
            delta.putAll(installedRemoved.getRemoved());
            return delta;
        }
        else {
            return Json.GSON.fromJson(
                json,
                new TypeToken<Map<String, Change<Xor<String, List<Pkg.Info>>>>>() { }
                    .getType()
            );
        }
    }

    /**
     * Wrapper object representing a "changes" element containing "installed" and "removed" elements inside:
     * "changes: { "installed": "pkg_name": { "new": "", "old": "1.7.9" } }
     *
     * @deprecated Temporarily here until available in a new version of salt-net-api.
     *
     * @param <T> the type that is wrapped
     */
    @Deprecated
    static class InstalledRemoved<T> {
        private T installed;
        private T removed;

        InstalledRemoved() {
            // default constructor
        }

        public T getInstalled() {
            return this.installed;
        }

        public T getRemoved() {
            return this.removed;
        }
    }

    /**
     * Update a given server action based on data from the corresponding job return event.
     *
     * @param serverAction the server action to update
     * @param retcode return code
     * @param success if the action was successful
     * @param jid salt job id for the action
     * @param jsonResult the result of the action as json
     * @param function salt function used for the action
     * @param endTime the time when the action was finished. If null, "now" is used
     */
    public void updateServerAction(ServerAction serverAction, long retcode, boolean success, String jid,
                                   JsonElement jsonResult, Optional<Xor<String[], String>> function, Date endTime) {
        serverAction.setCompletionTime(Optional.ofNullable(endTime).orElse(new Date()));

        // Set the result code defaulting to 0
        serverAction.setResultCode(retcode);

        // If the State was not executed due 'require' statement
        // we directly set the action to FAILED.
        if (jsonResult == null && function.isEmpty()) {
            serverAction.setStatusFailed();
            serverAction.setResultMsg("Prerequisite failed");
            return;
        }

        // Determine the final status of the action
        if (actionFailed(function, jsonResult, success, retcode)) {
            LOG.debug("Status of action {} being set to Failed.", serverAction.getParentAction().getId());
            serverAction.setStatusFailed();
            // check if the minion is locked (blackout mode)
            String output = getJsonResultWithPrettyPrint(jsonResult);
            if (output.startsWith("'ERROR") && output.contains("Minion in blackout mode")) {
                serverAction.setResultMsg(output);
                return;
            }
        }
        else {
            serverAction.setStatusCompleted();
        }

        Action.UpdateAuxArgs auxArgs = new Action.UpdateAuxArgs(retcode, success, jid, this,
                saltApi, systemQuery);

        Action action = HibernateFactory.unproxy(serverAction.getParentAction());

        action.handleUpdateServerAction(serverAction, jsonResult, auxArgs);

        LOG.debug("Finished update server action for action {}", action.getId());
    }

    /**
     * Return the path where scripts from Remote Commands Actions are stored.
     * @param scriptActionId the ID of the ScriptAction
     * @return a Path object to the storage.
     */
    public Path getScriptPath(Long scriptActionId) {
        return scriptsDir.resolve("script_" + scriptActionId + ".sh");
    }

    /**
     * Get the raw json result with pretty-print. If json result will be longer than 1024, it will
     * be trimmed.
     * @param jsonResult json result with pretty print
     * @return the pretty print
     */
    public static String getJsonResultWithPrettyPrint(JsonElement jsonResult) {
        return YamlHelper.INSTANCE.dump(Json.GSON.fromJson(jsonResult, Object.class));
    }

    /**
     * Check if an action is failed based on the return event data. The status depends on
     * the "success" and "retcode" attributes as well as on the single states results in
     * case we are looking at the results of a state.apply.
     *
     * @return true if the action has failed, false otherwise
     */
    private static boolean actionFailed(Optional<Xor<String[], String>> function, JsonElement rawResult,
            boolean success, long retcode) {
        // For state.apply based actions verify the result of each state
        if (function.map(x -> x.fold(Arrays::asList, List::of).contains("state.apply")).orElse(false)) {
            return Opt.fold(
                SaltUtils.jsonEventToStateApplyResults(rawResult),
                () -> true,
                results -> results.values().stream()
                        .anyMatch(result -> !result.isResult()));
        }
        return !(success && retcode == 0);
    }

    /**
     * Converts the json representation of an event to a map
     *
     * @param jsonResult json representation of an event
     * @return state apply results
     */
    public static Optional<Map<String, StateApplyResult<Map<String, Object>>>>
    jsonEventToStateApplyResults(JsonElement jsonResult) {
        TypeToken<Map<String, StateApplyResult<Map<String, Object>>>> typeToken = new TypeToken<>() { };
        Optional<Map<String, StateApplyResult<Map<String, Object>>>> results = Optional.empty();
        try {
             results = Optional.ofNullable(
                Json.GSON.fromJson(jsonResult, typeToken.getType()));
        }
        catch (JsonSyntaxException e) {
            LOG.error("JSON syntax error while decoding into a StateApplyResult:");
            LOG.error(jsonResult == null ? "NULL" : jsonResult.toString());
        }
        return results;
    }

    /**
     * Create a list of {@link InstalledPackage} for a  {@link Server} given the package names and package information.
     *
     * @param packageInfoAndNameBySaltPackageKey a map that contains a package name and a package info, by the package
     * key produced by salt
     * @param server server this package will be added to
     * @return a list of {@link InstalledPackage}
     */
    public static List<InstalledPackage> createPackagesFromSalt(
            Map<String, Tuple2<String, Pkg.Info>> packageInfoAndNameBySaltPackageKey, Server server) {
        List<String> names = packageInfoAndNameBySaltPackageKey.values().stream()
                .map(Tuple2::getA)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        Map<String, PackageName> packageNames = names.stream().collect(Collectors.toMap(Function.identity(),
                PackageFactory::lookupOrCreatePackageByName));

        Map<String, PackageEvr> packageEvrsBySaltPackageKey = packageInfoAndNameBySaltPackageKey.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey,
                        e -> {
                            Pkg.Info pkgInfo = e.getValue().getB();
                            return parsePackageEvr(pkgInfo.getEpoch(), pkgInfo.getVersion().orElseThrow(),
                                    pkgInfo.getRelease(), server.getPackageType());
                        }));

        return packageInfoAndNameBySaltPackageKey.entrySet().stream().map(e -> createInstalledPackage(
                packageNames.get(e.getValue().getA()),
                packageEvrsBySaltPackageKey.get(e.getKey()), e.getValue().getB(), server))
                .collect(Collectors.toList());
    }

    /**
     * Create a {@link InstalledPackage} object from package name, evr, package info and server and return it.
     *
     * @param packageName the package name
     * @param packageEvr the package evr
     * @param pkgInfo the package info
     * @param server server this package will be added to
     * @return the InstalledPackage object
     */
    private static InstalledPackage createInstalledPackage(PackageName packageName,
                                                           PackageEvr packageEvr,
                                                           Pkg.Info pkgInfo, Server server) {
        InstalledPackage pkg = new InstalledPackage();
        pkg.setEvr(packageEvr);
        pkg.setInstallTime(pkgInfo.getInstallDateUnixTime()
                .map(time -> new Date((time * 1000)))
                .orElse(null));
        pkg.setName(packageName);
        pkg.setServer(server);

        // Add -deb suffix to architectures for Debian systems
        String pkgArch = pkgInfo.getArchitecture().orElseThrow();
        if (server.getPackageType() == PackageType.DEB) {
            pkgArch += "-deb";
        }
        pkg.setArch(PackageFactory.lookupPackageArchByLabel(pkgArch));
        return pkg;
    }

    /**
     * Returns a key string that uniquely identifies an installed package (as a
     * Hibernated object)
     *
     * @param p the package
     * @return the key
     */
    public static String packageToKey(InstalledPackage p) {

        // name and EVR are never null due to DB constraints
        // see schema/spacewalk/common/tables/rhnServerPackage.sql

        return p.getName().getName() +
                "-" +
                p.getEvr().toUniversalEvrString() +
                "." +
                Optional.ofNullable(p.getArch()).map(PackageArch::toUniversalArchString).orElse("unknown");
    }

    /**
     * Returns a key string that uniquely identifies an installed package (as
     * returned by Salt)
     *
     * @param name the package name
     * @param info the package info
     * @return the key
     */
    public static String packageToKey(String name, Pkg.Info info) {

        StringBuilder sb = new StringBuilder();

        sb.append(name);
        sb.append("-");
        sb.append(
                new PackageEvr(
                        info.getEpoch().orElse(null),
                        info.getVersion().orElseThrow(),
                        info.getRelease().orElse("X"),
                        PackageType.RPM
                ).toUniversalEvrString()
        );
        sb.append(".");
        sb.append(info.getArchitecture().orElseThrow());

        return sb.toString();
    }

    /**
     * Returns a key string that uniquely identifies an installed package (as
     * returned by Salt)
     *
     * @param entry the package
     * @return the key
     */
    public static String packageToKey(Map.Entry<String, Pkg.Info> entry) {
        return packageToKey(entry.getKey(), entry.getValue());
    }

    /**
     * @param epoch
     * @param version
     * @param release
     * @param type
     * @return PackageEvr
     */
    public static PackageEvr parsePackageEvr(Optional<String> epoch, String version, Optional<String> release,
                                             PackageType type) {
        return switch (type) {
            case DEB -> PackageEvrFactory.lookupOrCreatePackageEvr(PackageEvr.parseDebian(version));
            case RPM -> PackageEvrFactory.lookupOrCreatePackageEvr(epoch.map(StringUtils::trimToNull).orElse(null),
                    version, release.orElse("0"), PackageType.RPM);
        };
    }

    /**
     * Convert a list of {@link ProductInfo} objects into a set of {@link InstalledProduct}
     * objects.
     *
     * @param productsIn list of products as received from Salt
     * @return set of installed products
     */
    public static Set<InstalledProduct> getInstalledProducts(
            List<ProductInfo> productsIn) {
        return productsIn.stream().flatMap(saltProduct -> {
            String name = saltProduct.getName();
            String version = saltProduct.getVersion();
            String release = saltProduct.getRelease();
            String arch = saltProduct.getArch();
            boolean isbase = saltProduct.getIsbase();

            // Find the corresponding SUSEProduct in the database, if any
            Optional<SUSEProduct> suseProduct = Optional.ofNullable(SUSEProductFactory
                    .findSUSEProduct(name, version, release, arch, true));
            if (suseProduct.isEmpty()) {
                LOG.warn(String.format("No product match found for: %s %s %s %s",
                        name, version, release, arch));
            }

            return Stream.of(SUSEProductFactory
                    .findInstalledProduct(name, version, release,
                            PackageFactory.lookupPackageArchByLabel(arch), isbase)
                    .orElseGet(() -> {
                        // Use installed product information from the client
                        InstalledProduct p = new InstalledProduct(name, version,
                                PackageFactory.lookupPackageArchByLabel(arch), release,
                                isbase);
                        ServerFactory.save(p);
                        return p;
                    }));
        }).collect(Collectors.toSet());
    }

    /**
     * Update the system info through grains and data returned by status.uptime
     *
     * @param jsonResult response from salt master against util.systeminfo state
     * @param minion the minion for which information should be updated
     */
    public void updateSystemInfo(JsonElement jsonResult, MinionServer minion) {
        SystemInfo systemInfo = Json.GSON.fromJson(jsonResult, SystemInfo.class);
        updateSystemInfo(systemInfo, minion);
    }

    /**
     * Update the minion connection path according to master/proxy hostname
     * @param minion the minion
     * @param master master/proxy hostname
     * @return true if the path has changed
     */
    public boolean updateMinionConnectionPath(MinionServer minion, String master) {
        boolean changed = minion.updateServerPaths(master);

        if (changed) {
            ServerFactory.save(minion);

            // Regenerate the pillar data
            MinionPillarManager.INSTANCE.generatePillar(minion);

            // push the changed pillar data to the minion
            saltApi.refreshPillar(new MinionList(minion.getMinionId()));

            ApplyStatesAction action = ActionManager.scheduleApplyStates(minion.getCreator(),
                    Collections.singletonList(minion.getId()),
                    Collections.singletonList(ApplyStatesEventMessage.CHANNELS),
                    new Date());
            try {
                TASKOMATIC_API.scheduleActionExecution(action, false);
            }
            catch (TaskomaticApiException e) {
                LOG.error("Could not schedule channels state application");
                LOG.error("Could not schedule channels refresh after proxy change. Old URLs remains on minion {}",
                        minion.getMinionId());
            }

        }
        return changed;
    }

    /**
     * Update the system info of the minion and set Reboot Actions to completed
     * @param systemInfo response from salt master against util.systeminfo state
     * @param minion  minion for which information should be updated
     */
    public void updateSystemInfo(SystemInfo systemInfo, MinionServer minion) {
        systemInfo.getKerneRelese().ifPresent(minion::setRunningKernel);
        systemInfo.getKernelLiveVersion().ifPresent(minion::setKernelLiveVersion);
        ServerFactory.save(minion);

        if (!ContactMethodUtil.isSSHPushContactMethod(minion.getContactMethod())) {
            systemInfo.getMaster().ifPresent(master -> updateMinionConnectionPath(minion, master));
        }

        //Update the uptime
        systemInfo.getUptimeSeconds().ifPresent(us-> handleUptimeUpdate(minion, us.longValue()));
    }

    /**
     * Handle the minion uptime update, that means:
     * - Set the time of the last boot according to the uptimeSeconds value and
     *   current time,
     * - cleanup old reboot actions.
     *
     * @param minion the minion
     * @param uptimeSeconds uptime time in seconds
     */
    public static void handleUptimeUpdate(MinionServer minion, Long uptimeSeconds) {
        if (uptimeSeconds == null) {
            return;
        }
        Date bootTime = new Date(
                System.currentTimeMillis() - (uptimeSeconds * 1000));
        LOG.debug("Set last boot for {} to {}", minion.getMinionId(), bootTime);
        minion.setLastBoot(bootTime.getTime() / 1000);

        // cleanup old reboot actions
        List<ServerAction> serverActions = ActionFactory.listServerActionsForServerAndTypes(minion,
                List.of(ActionFactory.TYPE_REBOOT));
        int actionsChanged = 0;
        for (ServerAction sa : serverActions) {
            Action action = sa.getParentAction();
            if (action.shouldCleanupAction(bootTime, sa)) {
                sa.setStatusCompleted();
                sa.setCompletionTime(new Date());
                sa.setResultMsg("Reboot completed.");
                sa.setResultCode(0L);
                ActionFactory.save(sa);
                actionsChanged += 1;
            }
        }
        if (actionsChanged > 0) {
            LOG.debug("{} reboot actions set to completed", actionsChanged);
        }
    }

    /**
     * Check recursively if there's prerequisite action of the given type in the completed state
     * @param action the action for which to check prerequisites
     * @param prereqType action type to check
     * @param systemId system id
     * @return true if there's prerequisite action of the given type in the completed state
     */
    public static boolean prerequisiteIsCompleted(Action action, Optional<ActionType> prereqType, long systemId) {
        if (action == null) {
            return false;
        }
        if ((prereqType.isEmpty() || prereqType.get().equals(action.getActionType())) &&
                action.getServerActions().stream()
                        .filter(sa -> sa.getServer().getId() == systemId)
                        .anyMatch(ServerAction::isStatusCompleted)) {
            return true;
        }
        return prerequisiteIsCompleted(action.getPrerequisite(), prereqType, systemId);
    }

    /**
     * Returns the same provided UUID string but representing the first
     * fields as little-endian according to RFC 4122.
     *
     * @param uuidIn the uuid string to transform without dashes "-"
     * @return the same UUID with first fields represented as little-endian
     */
    public static String uuidToLittleEndian(String uuidIn) {
        UUID uuidOrig = UUID.fromString(uuidIn.replaceAll(
                "(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})",
                "$1-$2-$3-$4-$5"));

        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuidOrig.getMostSignificantBits());
        bb.putLong(uuidOrig.getLeastSignificantBits());
        ByteBuffer source = ByteBuffer.wrap(bb.array());
        ByteBuffer target = ByteBuffer.allocate(16)
            .order(ByteOrder.LITTLE_ENDIAN)
            .putInt(source.getInt())
            .putShort(source.getShort())
            .putShort(source.getShort())
            .order(ByteOrder.BIG_ENDIAN)
            .putLong(source.getLong());
        target.rewind();

        UUID uuidSwap = new UUID(target.getLong(), target.getLong());
        return uuidSwap.toString().replaceAll("-", "");
    }

    private static JsonElement parseJsonError(SaltError message) {
        return message.fold(
            functionNotAvailable -> null,
            moduleNotSupported -> null,
            jsonParsingError -> jsonParsingError.getJson(),
            genericError -> null,
            saltSshError -> {
                try {
                    // Try parsing the error message as JSON
                    return JsonParser.GSON.fromJson("{" + saltSshError.getMessage() + "}", JsonElement.class);
                }
                catch (JsonParseException ex) {
                    LOG.warn("Unable to parse SaltSSHError message \"{}\"as json: {}",
                        saltSshError.getMessage(), ex.getMessage());

                    // Parsing as json has failed. Set the whole message as the "result" field, so it can be reported
                    JsonObject result = new JsonObject();
                    result.addProperty("result", saltSshError.getMessage());
                    return result;
                }
            }
        );
    }

    private static String extractStandardMessage(JsonElement json, String key) {
        if (json == null || !json.isJsonObject() || !json.getAsJsonObject().has(key)) {
            return null;
        }

        final JsonElement element = json.getAsJsonObject().get(key);
        if (element.isJsonPrimitive()) {
            return StringUtils.trimToNull(element.getAsJsonPrimitive().getAsString());
        }
        else if (element.isJsonArray()) {
            StringBuilder msg = new StringBuilder();
            element.getAsJsonArray().forEach(elem -> msg.append(elem.getAsString()));
            return StringUtils.trimToNull(msg.toString());
        }

        return null;
    }

    /**
     * Decode a {@link SaltError} to a {@link BootstrapError}.
     *
     * @param saltErr the Salt err
     * @return The parsed information from the error
     */
    public static BootstrapError decodeSaltErr(SaltError saltErr) {
        // Create a generic main message
        String detailMessage = saltErr.fold(
            err -> LOCALIZATION.getMessage("bootstrap.minion.error.salt.functionnotavailable", err.getFunctionName()),
            err -> LOCALIZATION.getMessage("bootstrap.minion.error.salt.modulenotsupported", err.getModuleName()),
            err -> LOCALIZATION.getMessage("bootstrap.minion.error.salt.jsonparsingerror"),
            err -> err.getMessage(),
            err -> LOCALIZATION.getMessage("bootstrap.minion.error.salt.saltssherror")
        );

        JsonElement jsonElement = SaltUtils.parseJsonError(saltErr);
        return new SaltBootstrapError(LOCALIZATION.getMessage("bootstrap.minion.error.salt.execution", detailMessage),
            SaltUtils.extractStandardMessage(jsonElement, "stdout"),
            SaltUtils.extractStandardMessage(jsonElement, "stderr"),
            SaltUtils.extractStandardMessage(jsonElement, "result"));
    }


    /**
     * Decode a collection of {@link State.ApplyResult} to a {@link BootstrapError}.
     *
     * @param result a map containing the result for each state applied
     * @return The parsed information from the error
     */
    public static BootstrapError decodeBootstrapSSHResult(SSHResult<Map<String, State.ApplyResult>> result) {
        String message = LOCALIZATION.getMessage("bootstrap.minion.error.salt.applystates", result.getRetcode());

        String standardOuput = result.getStdout().orElse(null);
        String standardError = result.getStderr().orElse(null);
        String resultText = StringUtils.trimToNull(
            result.getReturn().stream()
                  .flatMap(map -> map.entrySet().stream())
                  .filter(entry -> !entry.getValue().isResult())
                  .map(fail -> fail.getKey() + ": " + fail.getValue().getComment())
                  .collect(Collectors.joining("\n"))
        );

        return new SaltBootstrapError(message, standardOuput, standardError, resultText);
    }

    /**
     * Only used for unit tests.
     * @param scriptsDirIn to set
     */
    public void setScriptsDir(Path scriptsDirIn) {
        scriptsDir = scriptsDirIn;
    }

    /**
     * Return the scripts directory.
     * @return scripts directory
     */
    public Path getScriptsDir() {
        return scriptsDir;
    }

    /**
     * Returns a combined, printable string from output channels stderr and stdout.
     *
     * @param stderr the stderr message
     * @param stdout the stdout message
     * @return the string
     */
    public static String printStdMessages(String stderr, String stdout) {
        StringBuilder sb = new StringBuilder();
        if (StringUtils.isNotEmpty(stderr)) {
            sb.append("stderr:\n\n");
            sb.append(stderr);
            sb.append("\n");
            if (StringUtils.isNotEmpty(stdout)) {
                sb.append("stdout:\n\n");
                sb.append(stdout);
                sb.append("\n");
            }
        }
        else {
            sb.append(stdout);
            sb.append("\n");
        }
        return sb.toString();
    }
}
