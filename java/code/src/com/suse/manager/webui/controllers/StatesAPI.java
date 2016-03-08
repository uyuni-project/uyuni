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
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.state.OrgStateRevision;
import com.redhat.rhn.domain.state.PackageState;
import com.redhat.rhn.domain.state.PackageStates;
import com.redhat.rhn.domain.state.CustomState;
import com.redhat.rhn.domain.state.ServerGroupStateRevision;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.state.StateRevision;
import com.redhat.rhn.domain.state.VersionConstraints;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.rhnpackage.PackageManager;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.redhat.rhn.manager.system.SystemManager;
import com.suse.manager.reactor.messaging.ActionScheduledEventMessage;
import org.apache.http.HttpStatus;

import org.apache.log4j.Logger;

import com.suse.manager.webui.services.StateRevisionService;
import com.suse.manager.webui.services.impl.SaltAPIService;
import com.suse.manager.webui.utils.RepoFileUtils;
import com.suse.manager.webui.utils.SaltCustomState;
import com.suse.manager.webui.utils.SaltPkgInstalled;
import com.suse.manager.webui.utils.SaltPkgLatest;
import com.suse.manager.webui.utils.SaltPkgRemoved;
import com.suse.manager.webui.utils.SaltStateGenerator;
import com.suse.manager.webui.utils.YamlHelper;
import com.suse.manager.webui.utils.gson.JSONPackageState;
import com.suse.manager.webui.utils.gson.JSONCustomState;
import com.suse.manager.webui.utils.gson.JSONServerApplyStates;
import com.suse.manager.webui.utils.gson.JSONServerPackageStates;
import com.suse.manager.webui.utils.gson.JSONServerCustomStates;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.exception.SaltException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
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
        // TODO add org,group support

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
        String type = request.queryParams("type");
        long id = Long.valueOf(request.queryParams("id"));

        Optional<Set<CustomState>> saltStates = null;
        if ("server".equals(type)) {
            Server server = ServerFactory.lookupById(id);
            saltStates = StateFactory.latestCustomStates(server);

        } else if ("org".equals(type)) {
            Org org = OrgFactory.lookupById(id);
            saltStates = StateFactory.latestCustomStates(org);

        } else if ("group".equals(type)) {
            ServerGroup group = ServerGroupFactory.lookupByIdAndOrg(id,
                    user.getOrg()); // TODO is org really needed here?
            saltStates = StateFactory.latestCustomStates(group);
        }

        // Find matches among this currently assigned salt states
        Set<JSONCustomState> result = new HashSet<>(); // use a set to avoid duplicates

        result.addAll(saltStates.orElseGet(Collections::emptySet).stream()
                .filter(s -> s.getStateName().toLowerCase().contains(targetLowerCase))
                .map(s -> new JSONCustomState(s.getStateName(), true))
                .collect(Collectors.toList()));

        // Find matches among available catalog states
        result.addAll(SaltAPIService.INSTANCE.getCatalogStates(user.getId()).stream()
                .filter(s -> s.toLowerCase().contains(targetLowerCase))
                .map(s -> new JSONCustomState(s, false))
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
    public static String saveCustomStates(Request request, Response response,
                                          User user) {
        JSONServerCustomStates json = GSON.fromJson(request.body(),
                JSONServerCustomStates.class);

        // Merge the latest salt states with the changes
        Set<String> toAssign = json.getSaltStates().stream()
                .filter(s -> s.isAssigned())
                .map(s -> s.getName())
                .collect(Collectors.toSet());
        Set<String> toRemove = json.getSaltStates().stream()
                .filter(s -> !s.isAssigned())
                .map(s -> s.getName())
                .collect(Collectors.toSet());


        StateRevision newRevision = null;
        if ("server".equals(json.getTargetType())) {
            Server server = ServerFactory.lookupById(json.getTargetId());
            checkUserHasPermissionsOnServer(server, user);

            // clone any existing package states
            ServerStateRevision newServerRevision = StateRevisionService.INSTANCE
                    .cloneLatest(server, user, true, false);

            // merge existing states with incoming selections
            mergeStates(newServerRevision,
                    StateFactory.latestCustomStates(server),
                    toRemove,
                    toAssign);
            // assign any remaining new selection
            assignNewStates(newServerRevision, toAssign);
            generateServerCustomState(newServerRevision);
            newRevision = newServerRevision;

        } else if ("group".equals(json.getTargetType())) {
            ServerGroup group = ServerGroupFactory.lookupByIdAndOrg(
                    json.getTargetId(), user.getOrg()); // TODO is org really needed here ?
            checkUserHasPermissionsOnServerGroup(group);

            ServerGroupStateRevision newGroupRevision = StateRevisionService.INSTANCE
                    .cloneLatest(group, user, true, false);

            mergeStates(newGroupRevision,
                    StateFactory.latestCustomStates(group),
                    toRemove,
                    toAssign);
            assignNewStates(newGroupRevision, toAssign);
            // TODO generate state
            newRevision = newGroupRevision;

        } else if ("org".equals(json.getTargetType())) {
            Org org = OrgFactory.lookupById(json.getTargetId());
            checkUserHasPermissionsOnOrg(org);

            OrgStateRevision newOrgRevision = StateRevisionService.INSTANCE
                    .cloneLatest(org, user, true, false);

            mergeStates(newOrgRevision,
                    StateFactory.latestCustomStates(org),
                    toRemove,
                    toAssign);
            assignNewStates(newOrgRevision, toAssign);
            generateOrgCustomState(newOrgRevision);
            newRevision = newOrgRevision;

        } else {
            throw new IllegalArgumentException("Invalid targetType value");
        }

        try {
            StateFactory.save(newRevision);

            return json(response, newRevision.getCustomStates()
                    .stream().map(s -> new JSONCustomState(s.getStateName(), true))
                    .collect(Collectors.toSet()));
        }
        catch (Throwable t) {
            response.status(500);
            return "{}";
        }
    }


    private static void checkUserHasPermissionsOnServerGroup(ServerGroup group) {
        // TODO
    }

    private static void checkUserHasPermissionsOnOrg(Org org) {
        // TODO
    }

    private static void mergeStates(StateRevision newRevision,
                                    Optional<Set<CustomState>> latestStates,
                                    Set<String> toRemove,
                                    Set<String> toAssign) {
        latestStates.ifPresent(oldStates -> {
            for (CustomState oldState : oldStates) {
                if (!toRemove.contains(oldState.getStateName())) {
                    newRevision.getCustomStates().add(oldState);
                    toAssign.remove(oldState.getStateName()); // state already assigned
                }
            }
        });
    }

    private static void assignNewStates(StateRevision newRevision, Set<String> toAssign) {
        for (String newStateName : toAssign) {
            Optional<CustomState> newState = StateFactory
                    .getCustomStateByName(newStateName);
            newState.ifPresent(s -> newRevision.getCustomStates().add(s));
        }
    }

    private static void generateServerCustomState(ServerStateRevision stateRevision) {
        Server server = stateRevision.getServer();
        LOG.debug("Generating custom state SLS file for server: " + server.getId());

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

    private static void generateOrgCustomState(OrgStateRevision stateRevision) {

    }


    private static void checkUserHasPermissionsOnServer(Server server, User user) {
        if (!SystemManager.isAvailableToUser(user, server.getId())) {
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
     * Schedule an {@link ApplyStatesAction} and return its id.
     *
     * @param request the request object
     * @param response the response object
     * @param user the user
     * @return the id of the scheduled action
     */
    public static Object apply(Request request, Response response, User user) {
        // Can we use the ECMAScriptDateAdapter throughout this whole class?
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
                .serializeNulls()
                .create();
        JSONServerApplyStates json = gson.fromJson(request.body(),
                JSONServerApplyStates.class);


        ApplyStatesAction scheduledAction = handleTarget(json.getTargetType(), json.getTargetId(),
                (serverId) -> {
                    Server server = ServerFactory.lookupById(json.getTargetId());
                    checkUserHasPermissionsOnServer(server, user);
                    // Schedule an ApplyStatesAction to happen right now
                    ApplyStatesAction action = ActionManager.scheduleApplyStates(user,
                            Arrays.asList(json.getTargetId()), json.getStates(),
                            json.getEarliest() != null ? json.getEarliest() : new Date());
                    return action;
                },
                (groupId) -> {
                    // TODO group
                    return null;
                },
                (orgId) -> {
                    // TODO org
                    return null;
                }
                );

//        ApplyStatesAction action = null;
//        if ("server".equals(json.getTargetType())) {
//            Server server = ServerFactory.lookupById(json.getTargetId());
//            checkUserHasPermissionsOnServer(server, user);
//            // Schedule an ApplyStatesAction to happen right now
//            action = ActionManager.scheduleApplyStates(user,
//                    Arrays.asList(json.getTargetId()), json.getStates(), new Date());
//
//        } else if ("group".equals(json.getTargetType())) {
//
//        } else if ("org".equals(json.getTargetType())) {
//
//        } else {
//            throw new IllegalArgumentException("Invalid targetType value");
//        }

        MessageQueue.publish(new ActionScheduledEventMessage(scheduledAction));

        response.type("application/json");
        return GSON.toJson(scheduledAction.getId());
    }

    public static <R> R handleTarget(String targetType, long targetId,
                                     Function<Long, R> serverHandler,
                                     Function<Long, R> groupHandler,
                                     Function<Long, R> orgHandler) {
        if ("server".equals(targetType)) {
            return serverHandler.apply(targetId);
        } else if ("group".equals(targetType)) {
            return groupHandler.apply(targetId);
        } else if ("org".equals(targetType)) {
            return orgHandler.apply(targetId);
        } else {
            throw new IllegalArgumentException("Invalid targetType value");
        }
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

    /**
     * Call state.show_highstate for a given minion and return the output as JSON.
     *
     * @param request the request object
     * @param response the response object
     * @return JSON result of state.show_highstate
     */
    public static String showHighstate(Request request, Response response) {
        Optional<MinionServer> minionServer = MinionServerFactory
                .lookupById(Long.valueOf(request.queryParams("sid")));
        String ret = "Server not found.";

        if (minionServer.isPresent()) {
            String minionId = minionServer.get().getMinionId();
            LOG.debug("minionId is: " + minionId);
            MinionList target = new MinionList(minionId);

            Map<String, Object> result;
            try {
                result = SaltAPIService.INSTANCE.callSync(
                        com.suse.manager.webui.utils.salt.State.showHighstate(),
                        target, null);
                ret = YamlHelper.INSTANCE.dump(result.get(minionId));
            }
            catch (SaltException e) {
                response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                ret = e.getMessage();
            }
        }

        response.type("text/yaml");
        return ret;
    }
}
