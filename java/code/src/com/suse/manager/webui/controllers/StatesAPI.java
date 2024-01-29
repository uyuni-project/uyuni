/*
 * Copyright (c) 2015--2021 SUSE LLC
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

import static com.suse.manager.webui.utils.SparkApplicationHelper.asJson;
import static com.suse.manager.webui.utils.SparkApplicationHelper.withUser;
import static spark.Spark.get;
import static spark.Spark.post;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageType;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerGroup;
import com.redhat.rhn.domain.server.ServerGroupFactory;
import com.redhat.rhn.domain.state.PackageState;
import com.redhat.rhn.domain.state.PackageStates;
import com.redhat.rhn.domain.state.ServerStateRevision;
import com.redhat.rhn.domain.state.StateFactory;
import com.redhat.rhn.domain.state.StateRevision;
import com.redhat.rhn.domain.state.VersionConstraints;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.context.Context;
import com.redhat.rhn.manager.action.ActionChainManager;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.configuration.ConfigurationManager;
import com.redhat.rhn.manager.configuration.SaltConfigurable;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.manager.system.ServerGroupManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.reactor.utils.LocalDateTimeISOAdapter;
import com.suse.manager.reactor.utils.OptionalTypeAdapterFactory;
import com.suse.manager.utils.MinionServerUtils;
import com.suse.manager.webui.errors.NotFoundException;
import com.suse.manager.webui.services.ConfigChannelSaltManager;
import com.suse.manager.webui.services.SaltConstants;
import com.suse.manager.webui.services.SaltStateGeneratorService;
import com.suse.manager.webui.services.StateRevisionService;
import com.suse.manager.webui.services.StateSourceService;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.utils.SaltInclude;
import com.suse.manager.webui.utils.SaltPkgInstalled;
import com.suse.manager.webui.utils.SaltPkgLatest;
import com.suse.manager.webui.utils.SaltPkgRemoved;
import com.suse.manager.webui.utils.SaltStateGenerator;
import com.suse.manager.webui.utils.SparkApplicationHelper;
import com.suse.manager.webui.utils.YamlHelper;
import com.suse.manager.webui.utils.gson.ConfigChannelJson;
import com.suse.manager.webui.utils.gson.PackageStateJson;
import com.suse.manager.webui.utils.gson.ServerApplyHighstateJson;
import com.suse.manager.webui.utils.gson.ServerApplyStatesJson;
import com.suse.manager.webui.utils.gson.ServerConfigChannelsJson;
import com.suse.manager.webui.utils.gson.ServerPackageStatesJson;
import com.suse.manager.webui.utils.gson.StateTargetType;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.Result;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import spark.Request;
import spark.Response;
import spark.Spark;

/**
 * Controller class providing the backend for API calls to work with states.
 */
public class StatesAPI {

    /** Logger */
    private static final Logger LOG = LogManager.getLogger(StatesAPI.class);
    private final TaskomaticApi taskomaticApi;
    private final SaltApi saltApi;
    private final ServerGroupManager serverGroupManager;

    private static final Gson GSON = new GsonBuilder().create();

    /** ID of the state that installs the SUSE Manager repo file in SUSE systems. */
    public static final String ZYPPER_SUMA_CHANNEL_REPO_FILE
            = "/etc/zypp/repos.d/susemanager:channels.repo";

    /** ID of the state that installs the SUSE Manager repo file in Red Hat systems. */
    public static final String YUM_SUMA_CHANNEL_REPO_FILE
            = "/etc/yum.repos.d/susemanager:channels.repo";

    /**
     * @param saltApiIn instance to use.
     * @param taskomaticApiIn instance to use.
     * @param serverGroupManagerIn instance to use.
     */
    public StatesAPI(SaltApi saltApiIn, TaskomaticApi taskomaticApiIn,
                     ServerGroupManager serverGroupManagerIn) {
        this.taskomaticApi = taskomaticApiIn;
        this.saltApi = saltApiIn;
        this.serverGroupManager = serverGroupManagerIn;
    }

    /**
     * Invoked from Router. Initialize routes for Systems Views.
     */
    public void initRoutes() {
        post("/manager/api/states/apply", asJson(withUser(this::apply)));
        post("/manager/api/states/applyall", asJson(withUser(this::applyHighstate)));
        get("/manager/api/states/match", asJson(withUser(this::matchStates)));
        post("/manager/api/states/save", asJson(withUser(this::saveConfigChannels)));
        get("/manager/api/states/packages", asJson(this::packages));
        post("/manager/api/states/packages/save", asJson(withUser(this::savePackages)));
        get("/manager/api/states/packages/match", asJson(this::matchPackages));
        get("/manager/api/states/highstate", this::showHighstate);
        get("/manager/api/states/summary", asJson(this::listSummary));
        get("/manager/api/states/:channelId/content", withUser(this::stateContent));
    }

    /**
     * Query the current list of package states for a given server.
     *
     * @param request the request object
     * @param response the response object
     * @return JSON result of the API call
     */
    public String packages(Request request, Response response) {
        String serverId = request.queryParams("sid");
        MinionServer server = getEntityIfExists(MinionServerFactory.lookupById(Long.valueOf(serverId)));
        return GSON.toJson(latestPackageStatesJSON(server));
    }

    /**
     * Get the content of the state for the given channel.
     * @param request the http request
     * @param response the http response
     * @param user the current user
     * @return the content of the state as a string
     */
    public String stateContent(Request request, Response response, User user) {
        Long channelId = Long.valueOf(request.params("channelId"));
        ConfigChannel channel = ConfigurationManager.getInstance().lookupConfigChannel(user, channelId);
        String content = ConfigChannelSaltManager.getInstance().getChannelStateContent(channel);
        response.type("text/plain");
        return content;
    }

    /**
     * Find matches among this server's current packages states as well as among the
     * available packages and convert to JSON.
     *
     * @param request the request object
     * @param response the response object
     * @return JSON result of the API call
     */
    public String matchPackages(Request request, Response response) {
        String target = request.queryParams("target");
        String targetLowerCase = target.toLowerCase();
        String serverId = request.queryParams("sid");
        // TODO add org,group support

        // Find matches among this server's current packages states
        MinionServer server = getEntityIfExists(MinionServerFactory.lookupById(Long.valueOf(serverId)));
        Set<PackageStateJson> matching = latestPackageStatesJSON(server).stream()
                .filter(p -> p.getName().toLowerCase().contains(targetLowerCase))
                .collect(Collectors.toSet());

        // Find matches among available packages and convert to JSON objects
        Set<PackageStateJson> matchingAvailable = PackageManager
                .systemTotalPackages(Long.valueOf(serverId), null).stream()
                .filter(p -> p.getName().toLowerCase().contains(targetLowerCase))
                .map(p -> new PackageStateJson(p.getName(), new PackageEvr(
                        p.getEpoch(), p.getVersion(), p.getRelease(), server.getPackageType()), p.getArch()))
                .collect(Collectors.toSet());

        matching.addAll(matchingAvailable);
        return GSON.toJson(matching);
    }

    /**
     * Find matches among this server's current config channels as well as among the
     * config channels of the organization and convert to JSON.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return JSON result of the API call
     */
    public String matchStates(Request request, Response response, User user) {
        String target = request.queryParams("target");
        String targetLowerCase = target != null ? target.toLowerCase() : "";
        StateTargetType type = StateTargetType.valueOf(request.queryParams("type"));
        long id = Long.parseLong(request.queryParams("id"));

        // Lookup assigned states
        List<ConfigChannel> assignedStates = handleTarget(type, id,
                (serverId) -> {
                    MinionServer server = getEntityIfExists(MinionServerFactory.lookupById(id));
                    return StateFactory.latestConfigChannels(server);
                },
                (groupId) -> {
                    ServerGroup group = getEntityIfExists(ServerGroupFactory.lookupByIdAndOrg(id, user.getOrg()));
                    return StateFactory.latestConfigChannels(group);
                },
                (orgId) -> {
                    Org org = getEntityIfExists(OrgFactory.lookupById(id));
                    return StateFactory.latestConfigChannels(org);
                }
        ).orElseGet(Collections::emptyList).stream()
                .filter(c -> c.getName().toLowerCase().contains(targetLowerCase))
                .collect(Collectors.toList());

        // Find matches among this currently assigned salt states
        Set<ConfigChannelJson> result = new HashSet<>(); // use a set to avoid duplicates

        result.addAll(ConfigChannelJson.listOrdered(assignedStates));

        // Find matches among available channels
        ConfigurationManager.getInstance().listGlobalChannels(user).stream()
                .filter(s -> s.getName().toLowerCase().contains(targetLowerCase))
                .map(ConfigChannelJson::new)
                .forEach(result::add);

        return SparkApplicationHelper.json(response, result, new TypeToken<>() { });
    }

    /**
     * Save a new state revision for a server based on the latest existing revision and an
     * incoming list of changed config channel assignments.
     *
     * @param request the request object
     * @param response the response object
     * @param user the current user
     * @return null to make spark happy
     */
    public String saveConfigChannels(Request request, Response response, User user) {
        ConfigurationManager configManager = ConfigurationManager.getInstance();
        ServerConfigChannelsJson json = GSON.fromJson(request.body(), ServerConfigChannelsJson.class);

        List<ConfigChannel> channels = json.getChannels().stream()
                .sorted(Comparator.comparingInt(ConfigChannelJson::getPosition))
                .map(j -> configManager.lookupConfigChannel(user, j.getId()))
                .collect(Collectors.toList());

        try {
            SaltConfigurable entity = handleTarget(json.getTargetType(), json.getTargetId(),
                (serverId) -> {
                        MinionServer server = getEntityIfExists(MinionServerFactory.lookupById(serverId));
                        checkUserHasPermissionsOnServer(server, user);
                        return server;
                },
                (groupId) -> {
                        ServerGroup group =
                                getEntityIfExists(ServerGroupFactory.lookupByIdAndOrg(groupId, user.getOrg()));
                        checkUserHasPermissionsOnServerGroup(user, group);
                        return group;
                },
                (orgId) -> {
                        Org org = getEntityIfExists(OrgFactory.lookupById(json.getTargetId()));
                        checkUserHasPermissionsOnOrg(org);
                        return org;
                }
            );
            entity.setConfigChannels(channels, user);
            StateRevision revision = StateRevisionService.INSTANCE.getLatest(entity).get();
            return SparkApplicationHelper.json(response, ConfigChannelJson.listOrdered(revision.getConfigChannels()),
                    new TypeToken<>() { });
        }
        catch (Throwable t) {
            LOG.error(t.getMessage(), t);
            response.status(500);
            return "{}";
        }
    }

    /**
     * Checks if the given {@link Optional Optional&lt;T&gt;} has value and unwraps it,
     * otherwise halts Spark with a 404 status.
     *
     * @param entity entity to check
     * @param <T> type of the entity
     * @return unwrapped entity of type T
     */
    private <T> T getEntityIfExists(Optional<T> entity) {
        return entity.orElseGet(() -> {
            throw new NotFoundException();
        });
    }

    /**
     * Checks if the given {@link T} is not null and returns it for chaining, otherwise halts Spark with a 404 status.
     * @param entity entity to check
     * @param <T> type of the entity
     * @return given entity of type {@link T}
     */
    private <T> T getEntityIfExists(T entity) {
        if (entity == null) {
            throw new NotFoundException();
        }
        return entity;
    }

    private void checkUserHasPermissionsOnServerGroup(User user, ServerGroup group) {
        try {
            serverGroupManager.validateAccessCredentials(user, group,
                    group.getName());
            serverGroupManager.validateAdminCredentials(user);
        }
        catch (PermissionException | LookupException e) {
            Spark.halt(HttpStatus.SC_FORBIDDEN);
        }
    }

    private void checkUserHasPermissionsOnServer(MinionServer server, User user) {
        if (!SystemManager.isAvailableToUser(user, server.getId())) {
            Spark.halt(HttpStatus.SC_FORBIDDEN);
        }
    }

    private void checkUserHasPermissionsOnOrg(Org org) {
        // TODO
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
    public Object savePackages(Request request, Response response, User user) {
        ServerPackageStatesJson json = GSON.fromJson(request.body(),
                ServerPackageStatesJson.class);
        MinionServer server = getEntityIfExists(MinionServerFactory.lookupById(json.getServerId()));
        checkUserHasPermissionsOnServer(server, user);

        // Create a new state revision for this server
        ServerStateRevision state = StateRevisionService.INSTANCE
                .cloneLatest(server, user, false, true);

        // Merge the latest package states with the changes (converted to JSON objects)
        json.getPackageStates().addAll(latestPackageStatesJSON(server));

        // Add only valid states to the new revision, unmanaged packages will be skipped
        json.getPackageStates().forEach(pkgState ->
                pkgState.convertToPackageState(server.getPackageType()).ifPresent(s -> {
                    s.setStateRevision(state);
                    state.addPackageState(s);
                })
        );
        try {
            StateFactory.save(state);
            generateServerPackageState(server);
            return GSON.toJson(convertToJSON(state.getPackageStates()));
        }
        catch (Throwable t) {
            response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return "{}";
        }
    }

    /**
     * Schedule an {@link ApplyStatesAction} to apply a highstate for multiple minions
     * and return its id.
     *
     * @param req the request object
     * @param res the response object
     * @param user the user
     * @return the id of the scheduled action
     */
    public Object applyHighstate(Request req, Response res, User user) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
                .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
                .create();

        ServerApplyHighstateJson json = gson.fromJson(req.body(),
                ServerApplyHighstateJson.class);

        try {
            List<Long> minionIds =
                    MinionServerFactory.lookupByIds(json.getIds()).map(server -> {
                        checkUserHasPermissionsOnServer(server, user);
                        return server.getId();
                    }).collect(Collectors.toList());

            ActionChain actionChain = json.getActionChain()
                    .filter(StringUtils::isNotEmpty)
                    .map(label -> ActionChainFactory.getOrCreateActionChain(label, user))
                    .orElse(null);

            Set<Action> actions = ActionChainManager.scheduleApplyStates(user, minionIds,
                    Optional.of(json.isTest()), getScheduleDate(json), actionChain);

            if (actionChain != null) {
                return GSON.toJson(actionChain.getId());
            }
            return GSON.toJson(actions.stream().findFirst().map(Action::getId)
                    .orElseThrow(() -> new RuntimeException("No action in schedule result")));
        }
        catch (Exception e) {
            LOG.error("Could not apply highstate", e);
            res.status(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return "{}";
        }
    }

    /**
     * Schedule an {@link ApplyStatesAction} and return its id.
     * In the case of server groups the "custom.group_[id]" state
     * is applied instead of "custom_groups".
     * This is because the user wants to apply only the
     * config channels assigned to one group, not all the states of all
     * the groups of which a minion might be member of.
     *
     * @param request the request object
     * @param response the response object
     * @param user the user
     * @return the id of the scheduled action
     */
    public Object apply(Request request, Response response, User user) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeISOAdapter())
                .registerTypeAdapterFactory(new OptionalTypeAdapterFactory())
                .create();
        ServerApplyStatesJson json = gson.fromJson(request.body(),
                ServerApplyStatesJson.class);
        try {
            ApplyStatesAction scheduledAction = handleTarget(json.getTargetType(),
                    json.getTargetId(),
                    (serverId) -> {
                        MinionServer server = getEntityIfExists(MinionServerFactory.lookupById(json.getTargetId()));
                        checkUserHasPermissionsOnServer(server, user);
                        ApplyStatesAction action = ActionManager.scheduleApplyStates(user,
                                Arrays.asList(json.getTargetId()), json.getStates(),
                                getScheduleDate(json));
                        return action;
                    },
                    (groupId) -> {
                        ServerGroup group = getEntityIfExists(
                                ServerGroupFactory.lookupByIdAndOrg(json.getTargetId(), user.getOrg()));
                        checkUserHasPermissionsOnServerGroup(user, group);
                        List<Long> minionServerIds = MinionServerUtils
                                .filterSaltMinions(ServerGroupFactory.listServers(group))
                                .map(Server::getId)
                                .collect(Collectors.toList());

                        List<String> states = json.getStates();
                        if (states.size() == 1 && "custom_groups".equals(states.get(0))) {
                            String state = SaltStateGeneratorService.INSTANCE
                                    .getServerGroupGeneratedStateName(groupId);
                            states = Arrays.asList(state);
                        }

                        ApplyStatesAction action = ActionManager.scheduleApplyStates(user,
                                minionServerIds, states,
                                getScheduleDate(json));

                        return action;
                    },
                    (orgId) -> {
                        Org org = getEntityIfExists(OrgFactory.lookupById(json.getTargetId()));
                        checkUserHasPermissionsOnOrg(org);
                        List<Long> minionServerIds = MinionServerFactory
                                .lookupByOrg(org.getId()).stream()
                                .map(MinionServer::getId)
                                .collect(Collectors.toList());

                        ApplyStatesAction action = ActionManager.scheduleApplyStates(user,
                                minionServerIds, json.getStates(), getScheduleDate(json));

                        return action;
                    }
            );

            taskomaticApi.scheduleActionExecution(scheduledAction);

            return GSON.toJson(scheduledAction.getId());
        }
        catch (Exception e) {
            response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            return "{}";
        }
    }

    private Date getScheduleDate(ServerApplyStatesJson json) {
        ZoneId zoneId = Context.getCurrentContext().getTimezone().toZoneId();
        return Date.from(
            json.getEarliest().map(t -> t.atZone(zoneId).toInstant())
                .orElseGet(Instant::now)
        );
    }

    private Date getScheduleDate(ServerApplyHighstateJson json) {
        ZoneId zoneId = Context.getCurrentContext().getTimezone().toZoneId();
        return Date.from(
            json.getEarliest().orElseGet(LocalDateTime::now).atZone(zoneId).toInstant()
        );
    }

    private <R> R handleTarget(StateTargetType targetType, long targetId,
                                      Function<Long, R> serverHandler,
                                      Function<Long, R> groupHandler,
                                      Function<Long, R> orgHandler) {
        switch (targetType) {
            case SERVER:
                return serverHandler.apply(targetId);
            case GROUP:
                return groupHandler.apply(targetId);
            case ORG:
                return orgHandler.apply(targetId);
            default:
                // should not happen
                throw new IllegalArgumentException("Invalid targetType value");
        }
    }

    /**
     * Get the current set of package states for a given server as {@link PackageStateJson}
     * objects ready for serialization.
     *
     * @param server the server
     * @return the current set of package states
     */
    private Set<PackageStateJson> latestPackageStatesJSON(MinionServer server) {
        return convertToJSON(StateFactory.latestPackageStates(server)
                .orElse(Collections.emptySet()));
    }

    /**
     * Convert a given set of {@link PackageState} objects into a set of
     * {@link PackageStateJson} objects.
     *
     * @param packageStates the set of package states
     * @return set of JSON objects
     */
    private Set<PackageStateJson> convertToJSON(Set<PackageState> packageStates) {
        return packageStates.stream().map(state ->
            new PackageStateJson(
                    state.getName().getName(),
                    Optional.ofNullable(state.getEvr())
                            .orElse(new PackageEvr("", "", "", PackageType.RPM)),
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
    public static void generateServerPackageState(MinionServer server) {
        LOG.debug("Generating package state SLS file for: {}", server.getId());
        Set<PackageState> packageStates = StateFactory
                .latestPackageStates(server)
                .orElseGet(HashSet::new);

        SaltPkgInstalled pkgInstalled = new SaltPkgInstalled();
        SaltPkgRemoved pkgRemoved = new SaltPkgRemoved();
        SaltPkgLatest pkgLatest = new SaltPkgLatest();

        MinionServerFactory.lookupById(server.getId()).ifPresent(minion -> {
            if (minion.getOsFamily().equals("Suse")) {
                pkgInstalled.addRequire("file", ZYPPER_SUMA_CHANNEL_REPO_FILE);
                pkgRemoved.addRequire("file", ZYPPER_SUMA_CHANNEL_REPO_FILE);
                pkgLatest.addRequire("file", ZYPPER_SUMA_CHANNEL_REPO_FILE);
            }
            else if (minion.getOsFamily().equals("RedHat")) {
                pkgInstalled.addRequire("file", YUM_SUMA_CHANNEL_REPO_FILE);
                pkgRemoved.addRequire("file", YUM_SUMA_CHANNEL_REPO_FILE);
                pkgLatest.addRequire("file", YUM_SUMA_CHANNEL_REPO_FILE);
            }
        });

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
                    SaltConstants.SUMA_STATE_FILES_ROOT_PATH, SaltConstants.SALT_PACKAGES_STATES_DIR);
            Files.createDirectories(baseDir);
            Path filePath = baseDir.resolve(getPackagesSlsName(server.getMachineId()));
            SaltStateGenerator saltStateGenerator =
                    new SaltStateGenerator(filePath.toFile());
            saltStateGenerator.generate(new SaltInclude(ApplyStatesEventMessage.CHANNELS),
                    pkgInstalled, pkgRemoved, pkgLatest);
        }
        catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Remove package state file for the given minion
     *
     * @param machineId the salt machineId
     */
    public static void removePackageState(String machineId) {
        Path baseDir = Paths.get(
                SaltConstants.SUMA_STATE_FILES_ROOT_PATH, SaltConstants.SALT_PACKAGES_STATES_DIR);
        Path filePath = baseDir.resolve(getPackagesSlsName(machineId));

        try {
            Files.deleteIfExists(filePath);
        }
        catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    /**
     * Get the name of the package sls file.
     * @param machineId the salt machineId
     * @return the name of the package sls file
     */
    public static String getPackagesSlsName(String machineId) {
        return SaltConstants.SALT_SERVER_PACKAGES_STATE_FILE_PREFIX + machineId + ".sls";
    }

    /**
     * Import all currently installed packages as package states for a given server.
     *
     * @param server the server
     */
    @SuppressWarnings("unused")
    private void importPackageStates(MinionServer server) {
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
    public String showHighstate(Request request, Response response) {
        response.type("text/yaml");
        return MinionServerFactory
                .lookupById(Long.valueOf(request.queryParams("sid")))
                .map(minion -> {
                    final String minionId = minion.getMinionId();
                    try {
                        MinionList minions = new MinionList(minionId);
                        // sync to minion before showing highstate
                        saltApi.syncAll(minions);

                        Map<String, Result<Object>> result = saltApi.showHighstate(minionId);

                        return Optional.ofNullable(result.get(minionId))
                                .map(r -> r.fold(
                                        err -> err.fold(
                                                Object::toString,
                                                Object::toString,
                                                e -> "Error during state.show_highstate",
                                                Object::toString,
                                                Object::toString
                                        ),
                                        YamlHelper.INSTANCE::dump
                                ))
                                .orElse("No reply from minion: " + minionId);
                    }
                    catch (SaltException e) {
                        response.status(HttpStatus.SC_INTERNAL_SERVER_ERROR);
                        LOG.debug("showHighstate failed with: {}", e.getMessage());
                        return e.getMessage();
                    }
                }).orElse("Server not found.");
    }

    /**
     * Collect a summary of all the Salt states assigned or inherited to a system, and their origin information
     *
     * @param request the request object
     * @param response the response object
     * @return JSON array of state origin objects
     */
    public String listSummary(Request request, Response response) {
        return MinionServerFactory
                .lookupById(Long.valueOf(request.queryParams("sid")))
                .map(StateSourceService::getSystemStateSources)
                .map(stateSources -> SparkApplicationHelper.json(response, stateSources, new TypeToken<>() { }))
                .orElse("Server not found.");
    }
}
