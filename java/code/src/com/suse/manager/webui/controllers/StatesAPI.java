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
package com.suse.manager.webui.controllers;

import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.common.messaging.MessageQueue;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.state.PackageState;
import com.redhat.rhn.domain.state.PackageStates;
import com.redhat.rhn.domain.state.CustomState;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.state.VersionConstraints;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.rhnpackage.PackageManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.apache.http.HttpStatus;

import com.suse.manager.webui.utils.gson.JSONSaltState;
import com.suse.manager.webui.utils.gson.JSONServerSaltStates;
import com.suse.salt.netapi.datatypes.target.Grains;
import org.apache.log4j.Logger;

import com.suse.manager.webui.services.StateRevisionService;
import com.suse.manager.webui.services.SaltService;
import com.suse.manager.webui.services.impl.SaltAPIService;
import com.suse.manager.webui.utils.RepoFileUtils;
import com.suse.manager.webui.utils.SaltCustomState;
import com.suse.manager.webui.utils.SaltPkgInstalled;
import com.suse.manager.webui.utils.SaltPkgLatest;
import com.suse.manager.webui.utils.SaltPkgRemoved;
import com.suse.manager.webui.utils.SaltStateGenerator;
import com.suse.manager.webui.utils.gson.JSONPackageState;
import com.suse.manager.webui.utils.gson.JSONSaltState;
import com.suse.manager.webui.utils.gson.JSONServerApplyStates;
import com.suse.manager.webui.utils.gson.JSONServerPackageStates;
import com.suse.manager.webui.utils.gson.JSONServerSaltStates;
import com.suse.salt.netapi.calls.LocalAsyncResult;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import spark.Request;
import spark.Response;
import spark.Spark;

import static com.suse.manager.webui.utils.SparkApplicationHelper.json;

/**
 * Controller class providing the backend for API calls to work with states.
 */
public class StatesAPI {

    /** Logger */
    private static final Logger LOG = Logger.getLogger(StatesAPI.class);

    private static final Gson GSON = new GsonBuilder().create();
    private static final SaltService SALT_SERVICE = SaltAPIService.INSTANCE;
    public static final String SALT_PACKAGE_FILES = "packages";
    public static final String SALT_CUSTOM_STATES = "custom";

    private StatesAPI() { }

    /**
     * Query the current list of package states for a given server.
     *
     * @param request the request object
     * @param response the response object
     * @return JSON result of the API call
     */
    public static String packages(Request request, Response response) {
        String serverId = request.queryParams("sid");
        Server server = ServerFactory.lookupById(Long.valueOf(serverId));

        response.type("application/json");
        return GSON.toJson(latestPackageStatesJSON(server));
    }

    /**
     * Find matches among this server's current packages states as well as among the
     * available packages and convert to JSON.
     *
     * @param request the request object
     * @param response the response object
     * @return JSON result of the API call
     */
    public static String matchPackages(Request request, Response response) {
        String target = request.queryParams("target");
        String targetLowerCase = target.toLowerCase();
        String serverId = request.queryParams("sid");

        // Find matches among this server's current packages states
        Server server = ServerFactory.lookupById(Long.valueOf(serverId));
        Set<JSONPackageState> matching = latestPackageStatesJSON(server).stream()
                .filter(p -> p.getName().toLowerCase().contains(targetLowerCase))
                .collect(Collectors.toSet());

        // Find matches among available packages and convert to JSON objects
        Set<JSONPackageState> matchingAvailable = PackageManager
                .systemTotalPackages(Long.valueOf(serverId), null).stream()
                .filter(p -> p.getName().toLowerCase().contains(targetLowerCase))
                .map(p -> new JSONPackageState(p.getName(), new PackageEvr(
                        p.getEpoch(), p.getVersion(), p.getRelease()), p.getArch()))
                .collect(Collectors.toSet());

        response.type("application/json");
        matching.addAll(matchingAvailable);
        return GSON.toJson(matching);
    }

    /**
     * Find matches among this server's current custom states as well as among the
     * custom states of the organization and convert to JSON.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return JSON result of the API call
     */
    public static String matchStates(Request request, Response response, User user) {
        String target = request.queryParams("target");
        String targetLowerCase = target != null ? target.toLowerCase() : "";
        String serverId = request.queryParams("sid");

        Server server = ServerFactory.lookupById(Long.valueOf(serverId));

        // Find matches among this server's current salt states
        Set<JSONSaltState> result = new HashSet<>(); // use a set to avoid duplicates
        Optional<Set<CustomState>> saltStates = StateFactory.latestCustomSaltStates(server);
        result.addAll(saltStates.orElseGet(Collections::<CustomState>emptySet).stream()
                .filter(s -> s.getStateName().toLowerCase().contains(targetLowerCase))
                .map(s -> new JSONSaltState(s.getStateName(), true))
                .collect(Collectors.toList()));

        // Find matches among available organization states
        result.addAll(SaltAPIService.INSTANCE.getOrgStates(user.getId()).stream()
                .filter(s -> s.toLowerCase().contains(targetLowerCase))
                .map(s -> new JSONSaltState(s, false))
                .collect(Collectors.toList()));

        return json(response, result);
    }

    /**
     * Save a new state revision for a server based on the latest existing revision and an
     * incoming list of changed custom states assignments.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return null to make spark happy
     */
    public static String saveStatesAssignment(Request request, Response response,
                                              User user) {
        JSONServerSaltStates json = GSON.fromJson(request.body(),
                JSONServerSaltStates.class);
        Server server = ServerFactory.lookupById(json.getServerId());
        checkUserHasPermissionsOnServer(server, user);

        // clone any existing package states
        ServerStateRevision newRevision = StateRevisionService.INSTANCE
                .cloneLatest(server, user, true, false);

        // Merge the latest salt states with the changes
        Set<String> toAssign = json.getSaltStates().stream()
                .filter(s -> s.isAssigned()).map(s -> s.getName())
                .collect(Collectors.toSet());
        Set<String> toRemove = json.getSaltStates().stream()
                .filter(s -> !s.isAssigned()).map(s -> s.getName())
                .collect(Collectors.toSet());

        StateFactory.latestCustomSaltStates(server).ifPresent(oldStates -> {
            for (CustomState oldState : oldStates) {
                if (!toRemove.contains(oldState.getStateName())) {
                    newRevision.getCustomStates().add(oldState);
                    toAssign.remove(oldState.getStateName()); // state already assigned
                }
            }
        });

        for (String newStateName : toAssign) {
            CustomState newState = StateFactory.getCustomSaltStateByName(newStateName);
            newRevision.getCustomStates().add(newState);
        }

        try {
            StateFactory.save(newRevision);
            generateServerCustomState(newRevision);
            return json(response, newRevision.getCustomStates()
                    .stream().map(s -> new JSONSaltState(s.getStateName(), true))
                    .collect(Collectors.toSet()));
        }
        catch (Throwable t) {
            response.status(500);
            return "{}";
        }
    }

    private static void generateServerCustomState(ServerStateRevision stateRevision) {
        Server server = stateRevision.getServer();
        LOG.debug("Generating custom state SLS file for: " + server.getId());

        Set<String> stateNames = stateRevision.getCustomStates()
                .stream().map(s -> s.getStateName())
                .collect(Collectors.toSet());

        stateNames = SaltAPIService.INSTANCE.resolveOrgStates(
                server.getOrg().getId(), stateNames);

        try {
            Path baseDir = Paths.get(
                    RepoFileUtils.GENERATED_SLS_ROOT, SALT_CUSTOM_STATES);
            Files.createDirectories(baseDir);
            Path filePath = baseDir.resolve(
                    "custom_" + server.getDigitalServerId() + ".sls");
            SaltStateGenerator saltStateGenerator =
                    new SaltStateGenerator(filePath.toFile());
            saltStateGenerator.generate(new SaltCustomState(server.getId(), stateNames));
        }
        catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }

    }

    private static void checkUserHasPermissionsOnServer(Server server, User user) {
        if (!server.getOrg().getId().equals(user.getOrg().getId())) {
            // TODO any other checks needed here ?
            Spark.halt(HttpStatus.SC_FORBIDDEN);
        }
    }

    /**
     * Save a new state revision for a server based on the latest existing revision and an
     * incoming list of changed package states in the request body.
     *
     * @param request the request object
     * @param response the response object
     * @param user the user
     * @return null to make spark happy
     */
    public static Object savePackages(Request request, Response response, User user) {
        response.type("application/json");
        JSONServerPackageStates json = GSON.fromJson(request.body(),
                JSONServerPackageStates.class);
        Server server = ServerFactory.lookupById(json.getServerId());
        checkUserHasPermissionsOnServer(server, user);

        // Create a new state revision for this server
        ServerStateRevision state = StateRevisionService.INSTANCE
                .cloneLatest(server, user, false, true);

        // Merge the latest package states with the changes (converted to JSON objects)
        json.getPackageStates().addAll(latestPackageStatesJSON(server));

        // Add only valid states to the new revision, unmanaged packages will be skipped
        json.getPackageStates().stream().forEach(pkgState ->
                 pkgState.convertToPackageState().ifPresent(s -> {
                     s.setStateRevision(state);
                     state.addPackageState(s);
                 }));

        try {
            StateFactory.save(state);
            generateServerPackageState(server);
            return GSON.toJson(convertToJSON(state.getPackageStates()));
        }
        catch (Throwable t) {
            response.status(500);
            return "{}";
        }
    }

    /**
     * Apply a list of states to a minion and return the results as JSON.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return JSON results of state application
     */
    public static Object apply(Request request, Response response, User user) {
        JSONServerApplyStates json = GSON.fromJson(request.body(),
                JSONServerApplyStates.class);
        Server server = ServerFactory.lookupById(json.getServerId());
        checkUserHasPermissionsOnServer(server, user);
        LocalAsyncResult<Map<String, Object>> results = SALT_SERVICE.applyState(
                new Grains("machine_id", server.getDigitalServerId()), json.getStates());

        response.type("application/json");
        return GSON.toJson(results.getJid());
    }

    /**
     * Get the current set of package states for a given server as {@link JSONPackageState}
     * objects ready for serialization.
     *
     * @param server the server
     * @return the current set of package states
     */
    private static Set<JSONPackageState> latestPackageStatesJSON(Server server) {
        return convertToJSON(StateFactory.latestPackageStates(server)
                .orElse(Collections.emptySet()));
    }

    /**
     * Convert a given set of {@link PackageState} objects into a set of
     * {@link JSONPackageState} objects.
     *
     * @param packageStates the set of package states
     * @return set of JSON objects
     */
    private static Set<JSONPackageState> convertToJSON(Set<PackageState> packageStates) {
        return packageStates.stream().map(state ->
            new JSONPackageState(
                    state.getName().getName(),
                    Optional.ofNullable(state.getEvr())
                            .orElse(new PackageEvr("", "", "")),
                    Optional.ofNullable(state.getArch())
                            .map(PackageArch::getLabel).orElse(""),
                    Optional.of(state.getPackageStateTypeId()),
                    Optional.of(state.getVersionConstraintId())
            )
        ).collect(Collectors.toSet());
    }

    /**
     * Generate the package states into an SLS file for a given server.
     *
     * @param server the server
     */
    public static void generateServerPackageState(Server server) {
        LOG.debug("Generating package state SLS file for: " + server.getId());
        Set<PackageState> packageStates = StateFactory
                .latestPackageStates(server)
                .orElseGet(HashSet::new);
        SaltPkgInstalled pkgInstalled = new SaltPkgInstalled();
        SaltPkgRemoved pkgRemoved = new SaltPkgRemoved();
        SaltPkgLatest pkgLatest = new SaltPkgLatest();

        for (PackageState state : packageStates) {
            if (state.getPackageState() == PackageStates.INSTALLED) {
                if (state.getVersionConstraint() == VersionConstraints.LATEST) {
                    pkgLatest.addPackage(state.getName().getName());
                }
                else {
                    pkgInstalled.addPackage(state.getName().getName());
                }
            }
            else if (state.getPackageState() == PackageStates.REMOVED) {
                pkgRemoved.addPackage(state.getName().getName());
            }
        }

        try {
            Path baseDir = Paths.get(
                    RepoFileUtils.GENERATED_SLS_ROOT, SALT_PACKAGE_FILES);
            Files.createDirectories(baseDir);
            Path filePath = baseDir.resolve(
                    "packages_" + server.getDigitalServerId() + ".sls");
            SaltStateGenerator saltStateGenerator =
                    new SaltStateGenerator(filePath.toFile());
            saltStateGenerator.generate(pkgInstalled, pkgRemoved, pkgLatest);
        }
        catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Import all currently installed packages as package states for a given server.
     *
     * @param server the server
     */
    @SuppressWarnings("unused")
    private void importPackageStates(Server server) {
        ServerStateRevision serverRev =  new ServerStateRevision();
        serverRev.setServer(server);
        Stream<PackageState> packageStateStream = server.getPackages().stream().map(pkg -> {
            PackageState packageState = new PackageState();
            packageState.setStateRevision(serverRev);
            packageState.setName(pkg.getName());
            packageState.setEvr(pkg.getEvr());
            packageState.setArch(pkg.getArch());
            packageState.setPackageState(PackageStates.INSTALLED);
            packageState.setVersionConstraint(VersionConstraints.ANY);
            return packageState;
        });
        serverRev.setPackageStates(packageStateStream.collect(Collectors.toSet()));
        StateFactory.save(serverRev);
    }
}
