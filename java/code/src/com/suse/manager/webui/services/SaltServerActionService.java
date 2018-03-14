/**
 * Copyright (c) 2016 SUSE LLC
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

import com.google.gson.reflect.TypeToken;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainEntry;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsAction;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsActionDetails;
import com.redhat.rhn.domain.action.config.ConfigAction;
import com.redhat.rhn.domain.action.config.ConfigRevisionAction;
import com.redhat.rhn.domain.action.dup.DistUpgradeAction;
import com.redhat.rhn.domain.action.dup.DistUpgradeChannelTask;
import com.redhat.rhn.domain.action.errata.ErrataAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageRemoveAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageUpdateAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesActionDetails;
import com.redhat.rhn.domain.action.salt.build.ImageBuildAction;
import com.redhat.rhn.domain.action.salt.build.ImageBuildActionDetails;
import com.redhat.rhn.domain.action.salt.inspect.ImageInspectAction;
import com.redhat.rhn.domain.action.salt.inspect.ImageInspectActionDetails;
import com.redhat.rhn.domain.action.scap.ScapAction;
import com.redhat.rhn.domain.action.scap.ScapActionDetails;
import com.redhat.rhn.domain.action.script.ScriptAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.channel.AccessToken;
import com.redhat.rhn.domain.channel.AccessTokenFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.image.DockerfileProfile;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.server.ErrataInfo;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.entitlement.EntitlementManager;
import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.utils.MinionServerUtils;
import com.suse.manager.webui.utils.SaltState;
import com.suse.manager.webui.utils.SaltModuleRun;
import com.suse.manager.webui.utils.SaltSystemReboot;
import com.suse.manager.webui.utils.salt.custom.Openscap;
import com.suse.manager.webui.utils.TokenBuilder;
import com.suse.manager.webui.utils.salt.custom.ScheduleMetadata;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.utils.Opt;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.log4j.Logger;
import org.jose4j.lang.JoseException;
import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.suse.manager.webui.services.SaltActionChainGeneratorService.ACTION_STATE_ID_PREFIX;
import static com.suse.manager.webui.services.SaltConstants.SCRIPTS_DIR;
import static com.suse.manager.webui.services.SaltConstants.SUMA_STATE_FILES_ROOT_PATH;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;

/**
 * Takes {@link Action} objects to be executed via salt.
 */
public class SaltServerActionService {

    /* Singleton instance of this class */
    public static final SaltServerActionService INSTANCE = new SaltServerActionService();

    /* Logger for this class */
    private static final Logger LOG = Logger.getLogger(SaltServerActionService.class);
    private static final String ACTIONCHAIN_START = "actionchains.start";
    public static final String PACKAGES_PKGINSTALL = "packages.pkginstall";
    private static final String PACKAGES_PKGDOWNLOAD = "packages.pkgdownload";
    private static final String PACKAGES_PATCHINSTALL = "packages.patchinstall";
    private static final String PACKAGES_PATCHDOWNLOAD = "packages.patchdownload";
    private static final String PACKAGES_PKGREMOVE = "packages.pkgremove";
    private static final String CONFIG_DEPLOY_FILES = "configuration.deploy_files";
    private static final String CONFIG_DIFF_FILES = "configuration.diff_files";
    private static final String PARAM_PKGS = "param_pkgs";
    private static final String PARAM_PATCHES = "param_patches";
    private static final String PARAM_FILES = "param_files";
    private static final String REMOTE_COMMANDS = "remotecommands";

    /** SLS pillar parameter name for the list of update stack patch names. */
    public static final String PARAM_UPDATE_STACK_PATCHES = "param_update_stack_patches";

    /** SLS pillar parameter name for the list of regular patch names. */
    public static final String PARAM_REGULAR_PATCHES = "param_regular_patches";

    private SaltActionChainGeneratorService saltActionChainGeneratorService =
            SaltActionChainGeneratorService.INSTANCE;


    /**
     * For a given action and list of minion servers return the salt call(s) that need to be
     * executed grouped by the list of targeted minions.
     *
     * @param actionIn the action to be executed
     * @param minions the list of minions to target
     * @return map of Salt local call to list of targeted minion servers
     */
    public Map<LocalCall<?>, List<MinionServer>> callsForAction(Action actionIn,
            List<MinionServer> minions) {
        ActionType actionType = actionIn.getActionType();
        if (ActionFactory.TYPE_ERRATA.equals(actionType)) {
            ErrataAction errataAction = (ErrataAction) actionIn;
            Set<Long> errataIds = errataAction.getErrata().stream()
                    .map(Errata::getId).collect(Collectors.toSet());
            return errataAction(minions, errataIds);
        }
        else if (ActionFactory.TYPE_PACKAGES_UPDATE.equals(actionType)) {
            return packagesUpdateAction(minions, (PackageUpdateAction) actionIn);
        }
        else if (ActionFactory.TYPE_PACKAGES_REMOVE.equals(actionType)) {
            return packagesRemoveAction(minions, (PackageRemoveAction) actionIn);
        }
        else if (ActionFactory.TYPE_PACKAGES_REFRESH_LIST.equals(actionType)) {
            return packagesRefreshListAction(minions);
        }
        else if (ActionFactory.TYPE_HARDWARE_REFRESH_LIST.equals(actionType)) {
            return hardwareRefreshListAction(minions);
        }
        else if (ActionFactory.TYPE_REBOOT.equals(actionType)) {
            return rebootAction(minions);
        }
        else if (ActionFactory.TYPE_CONFIGFILES_DEPLOY.equals(actionType)) {
            return deployFiles(minions, (ConfigAction) actionIn);
        }
        else if (ActionFactory.TYPE_CONFIGFILES_DIFF.equals(actionType)) {
            return diffFiles(minions, (ConfigAction) actionIn);
        }
        else if (ActionFactory.TYPE_SCRIPT_RUN.equals(actionType)) {
            return remoteCommandAction(minions, (ScriptAction) actionIn);
        }
        else if (ActionFactory.TYPE_APPLY_STATES.equals(actionType)) {
            ApplyStatesActionDetails actionDetails = ((ApplyStatesAction) actionIn).getDetails();
            return applyStatesAction(minions, actionDetails.getMods(), actionDetails.isTest());
        }
        else if (ActionFactory.TYPE_IMAGE_INSPECT.equals(actionType)) {
            ImageInspectAction iia = (ImageInspectAction) actionIn;
            ImageInspectActionDetails details = iia.getDetails();
            ImageStore store = ImageStoreFactory.lookupById(
                    details.getImageStoreId()).get();
            return imageInspectAction(minions, details.getVersion(), details.getName(),
                    store);
        }
        else if (ActionFactory.TYPE_IMAGE_BUILD.equals(actionType)) {
            ImageBuildAction imageBuildAction = (ImageBuildAction) actionIn;
            ImageBuildActionDetails details = imageBuildAction.getDetails();
            return ImageProfileFactory.lookupById(details.getImageProfileId())
                    .flatMap(ImageProfile::asDockerfileProfile).map(
                    ip -> imageBuildAction(
                            minions,
                            Optional.ofNullable(details.getVersion()),
                            ip,
                            imageBuildAction.getSchedulerUser())
            ).orElseGet(Collections::emptyMap);
        }
        else if (ActionFactory.TYPE_DIST_UPGRADE.equals(actionType)) {
            return distUpgradeAction((DistUpgradeAction) actionIn, minions);
        }
        else if (ActionFactory.TYPE_SCAP_XCCDF_EVAL.equals(actionType)) {
            ScapAction scapAction = (ScapAction)actionIn;
            return scapXccdfEvalAction(minions, scapAction.getScapActionDetails());
        }
        else if (ActionFactory.TYPE_SUBSCRIBE_CHANNELS.equals(actionType)) {
            SubscribeChannelsAction subscribeAction = (SubscribeChannelsAction)actionIn;
            return subscribeChanelsAction(minions, subscribeAction.getDetails());
        }
        else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Action type " +
                        (actionType != null ? actionType.getName() : "") +
                        " is not supported with Salt");
            }
            return Collections.emptyMap();
        }
    }



    private Optional<ServerAction> serverActionFor(Action actionIn, MinionServer minion) {
        return actionIn.getServerActions().stream()
                .filter(sa -> sa.getServerId().equals(minion.getId()))
                .findFirst();
    }

    /**
     * Execute a given {@link Action} via salt.
     *
     * @param actionIn the action to execute
     * @param forcePackageListRefresh add metadata to force a package list
     * refresh
     * @param isStagingJob whether the action is a staging of packages
     * action
     * @param stagingJobMinionServerId if action is a staging action it will
     * contain involved minionId(s)
     */
    public void execute(Action actionIn, boolean forcePackageListRefresh,
            boolean isStagingJob, Optional<Long> stagingJobMinionServerId) {

        List<MinionServer> minions = getMinionServers(actionIn);

        // now prepare each call
        for (Map.Entry<LocalCall<?>, List<MinionServer>> entry :
                callsForAction(actionIn, minions).entrySet()) {
            LocalCall<?> call = entry.getKey();
            final List<MinionServer> targetMinions;
            Map<Boolean, List<MinionServer>> results;

            if (isStagingJob) {
                targetMinions = new ArrayList<>();
                stagingJobMinionServerId
                        .ifPresent(serverId -> MinionServerFactory.lookupById(serverId)
                                .ifPresent(server -> targetMinions.add(server)));
                call = prepareStagingTargets(actionIn, targetMinions);
            }
            else {
                targetMinions = entry.getValue();
            }

            results = execute(actionIn, call, targetMinions, forcePackageListRefresh,
                    isStagingJob);

            results.get(true).forEach(minionServer -> {
                serverActionFor(actionIn, minionServer).ifPresent(serverAction -> {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Asynchronous call on minion: " +
                                minionServer.getMinionId());
                    }
                    if (!isStagingJob) {
                        serverAction.setStatus(ActionFactory.STATUS_PICKED_UP);
                        ActionFactory.save(serverAction);
                    }
                });
            });

            results.get(false).forEach(minionServer -> {
                serverActionFor(actionIn, minionServer).ifPresent(serverAction -> {
                    LOG.warn("Failed to schedule action for minion: " +
                            minionServer.getMinionId());
                    if (!isStagingJob) {
                        serverAction.setCompletionTime(new Date());
                        serverAction.setResultCode(-1L);
                        serverAction.setResultMsg("Failed to schedule action.");
                        serverAction.setStatus(ActionFactory.STATUS_FAILED);
                        ActionFactory.save(serverAction);
                    }
                });
            });
        }
    }

    /**
     * Call Salt to start the execution of the given action chain.
     *
     * @param actionChain the action chain to execute
     * @param minions a list containing target minions
     */
    private void startActionChainExecution(ActionChain actionChain, Set<MinionServer> minions) {
        // now prepare each call
        Pair<LocalCall<?>, Set<MinionServer>> entry =
                startActionChainCall(minions, actionChain.getId());

        LocalCall<?> call = entry.getKey();
        Set<MinionServer> targetMinions = entry.getValue();

        Map<Boolean, ? extends Collection<MinionServer>> results =
                callAsyncActionChainStart(actionChain.getId(), call, targetMinions);

        // collect all server actions
        List<ServerAction> serverActions =
                actionChain.getEntries().stream()
                        .map(ace -> ace.getAction())
                        .flatMap(a -> a.getServerActions().stream())
                        .collect(Collectors.toList());

        results.get(true).forEach(minionServer -> {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Asynchronous call on minion: " +
                        minionServer.getMinionId());
            }
            serverActions.stream()
                    .filter(sa -> sa.getServer().getId().equals(minionServer.getId()))
                    .forEach(sa -> {
                        sa.setStatus(ActionFactory.STATUS_PICKED_UP);
                        ActionFactory.save(sa);
                    });
        });

        results.get(false).forEach(minionServer -> {
            LOG.warn("Failed to schedule action chain for minion: " +
                    minionServer.getMinionId());
            serverActions.stream()
                    .filter(sa -> sa.getServer().getId().equals(minionServer.getId()))
                    .forEach(sa -> {
                        sa.setStatus(ActionFactory.STATUS_FAILED);
                        ActionFactory.save(sa);
                    });
        });
    }

    /**
     * Prepare and execute the action chain.
     *
     * @param actionChainId id of the action chain to execute
     */
    public void executeActionChain(long actionChainId) {
        ActionChain actionChain = ActionChainFactory
                .getActionChain(actionChainId)
                .orElseThrow(() -> new RuntimeException("Action chain id=" + actionChainId + " not found in db"));

        // for each minion populate a list of ServerActions with the corresponding Salt call(s)
        Map<MinionServer, List<Pair<ServerAction, List<LocalCall<?>>>>> minionCalls = new HashMap<>();

        actionChain.getEntries().stream()
                .sorted(Comparator.comparingInt(ActionChainEntry::getSortOrder))
                .map(ActionChainEntry::getAction)
                .forEach(actionIn -> {
                    // TODO extract to common method
                    List<MinionServer> minions = getMinionServers(actionIn);

                    if (minions.isEmpty()) {
                        return;
                    }

                    // get Salt calls for this action
                    Map<LocalCall<?>, List<MinionServer>> actionCalls = callsForAction(actionIn, minions);

                    // TODO how to handle staging jobs?

                    // Salt calls for each minion
                    Map<MinionServer, List<LocalCall<?>>> callsPerMinion =
                            actionCalls.values().stream().flatMap(m -> m.stream())
                                .collect(Collectors
                                        .toMap(Function.identity(),
                                                m -> actionCalls.entrySet()
                                                        .stream()
                                                        .filter(e -> e.getValue().contains(m))
                                                        .map(e -> e.getKey())
                                                        .collect(Collectors.toList())
                                ));

                    // append the Salt calls for this action to the list of calls of each minion
                    callsPerMinion.forEach((minion, calls) -> {
                        List<Pair<ServerAction, List<LocalCall<?>>>> currentCalls = minionCalls
                                .getOrDefault(minion, new ArrayList<>());
                        Optional<ServerAction> serverAction = actionIn.getServerActions().stream()
                                .filter(sa -> sa.getServer().getId().equals(minion.getId()))
                                .findFirst();
                        serverAction.ifPresent(sa -> {
                            Pair<ServerAction, List<LocalCall<?>>> serverActionCalls =
                                    new ImmutablePair<>(sa, calls);
                            currentCalls.add(serverActionCalls);
                        });
                        minionCalls.put(minion, currentCalls);
                    });

                });

        // render local calls to salt states
        minionCalls.forEach((minion, serverActionCalls) -> {
            List<SaltState> states = serverActionCalls.stream()
                    .flatMap(saCalls -> {
                        ServerAction sa = saCalls.getKey();
                        List<LocalCall<?>> calls = saCalls.getValue();
                        return convertToState(actionChain.getId(), sa, calls).stream();
                    }).collect(Collectors.toList());
            saltActionChainGeneratorService.createActionChainSLSFiles(actionChain,
                    minion, states);
        });

        startActionChainExecution(actionChain, minionCalls.keySet());
    }

    private List<MinionServer> getMinionServers(Action actionIn) {
        return Optional.ofNullable(actionIn.getServerActions())
                                .map(serverActions -> MinionServerUtils.filterSaltMinions(serverActions.stream()
                                        .map(ServerAction::getServer)
                                        .collect(Collectors.toList()))
                                        .filter(m -> m.hasEntitlement(EntitlementManager.SALT))
                                        .filter(m -> m.getContactMethod().getLabel().equals("default"))
                                        .collect(Collectors.toList()))
                                .orElse(new LinkedList<>());
    }

    private List<SaltState> convertToState(long actionChainId, ServerAction serverAction, List<LocalCall<?>> calls) {
        String stateId = ACTION_STATE_ID_PREFIX + actionChainId + "_action_" + serverAction.getParentAction().getId();

        return calls.stream().map(call -> {
            Map<String, Object> payload = call.getPayload();
            String fun = (String)payload.get("fun");
            Map<String, ?> kwargs = (Map<String, ?>)payload.get("kwarg");
            switch(fun) {
                case "state.apply":
                    List<String> mods = (List<String>)kwargs.get("mods");
                    return new SaltModuleRun(stateId,
                            "state.apply",
                            singletonMap("mods", mods),
                            singletonMap("pillar", kwargs.get("pillar")));
                case "system.reboot":
                    Integer time = (Integer)kwargs.get("at_time");
                    return new SaltSystemReboot(stateId, time);
                default:
                    throw new RuntimeException("TODO add code");
            }
        }).collect(Collectors.toList());
    }


    /**
     * This function will return a map with list of minions grouped by the
     * salt netapi local call that executes what needs to be executed on
     * those minions for the given errata ids and minions.
     *
     * @param minions list of minions
     * @param errataIds list of errata ids
     * @return minions grouped by local call
     */
    public Map<LocalCall<?>, List<MinionServer>> errataAction(List<MinionServer> minions,
            Set<Long> errataIds) {
        Set<Long> minionIds = minions.stream()
                .map(MinionServer::getId)
                .collect(Collectors.toSet());
        Map<Long, Map<Long, Set<ErrataInfo>>> errataInfos = ServerFactory
                .listErrataNamesForServers(minionIds, errataIds);

        // Group targeted minions by errata names
        Map<Set<ErrataInfo>, List<MinionServer>> collect = minions.stream()
                .collect(Collectors.groupingBy(minion -> errataInfos.get(minion.getId())
                        .entrySet().stream()
                        .map(Map.Entry::getValue)
                        .flatMap(Set::stream)
                        .collect(Collectors.toSet())
        ));

        // Convert errata names to LocalCall objects of type State.apply
        return collect.entrySet().stream()
            .collect(Collectors.toMap(entry -> {
                Map<String, Object> params = new HashMap<>();
                params.put(PARAM_REGULAR_PATCHES,
                    entry.getKey().stream()
                        .filter(e -> !e.isUpdateStack())
                        .map(e -> e.getName())
                        .sorted()
                        .collect(Collectors.toList())
                );
                params.put(PARAM_UPDATE_STACK_PATCHES,
                    entry.getKey().stream()
                        .filter(e -> e.isUpdateStack())
                        .map(e -> e.getName())
                        .sorted()
                        .collect(Collectors.toList())
                );
                return State.apply(
                        Arrays.asList(PACKAGES_PATCHINSTALL),
                        Optional.of(params),
                        Optional.of(true)
                );
            },
            Map.Entry::getValue));
    }

    private Map<LocalCall<?>, List<MinionServer>> packagesUpdateAction(
            List<MinionServer> minions, PackageUpdateAction action) {
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();
        Map<String, String> pkgs = action.getDetails().stream().collect(Collectors.toMap(
                d -> d.getPackageName().getName(), d -> d.getEvr().toString(),
                // See PR#2839
                (a, b)-> a));
        ret.put(State.apply(Arrays.asList(PACKAGES_PKGINSTALL),
                Optional.of(singletonMap(PARAM_PKGS, pkgs)),
                Optional.of(true)), minions);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionServer>> packagesRemoveAction(
            List<MinionServer> minions, PackageRemoveAction action) {
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();
        Map<String, String> pkgs = action.getDetails().stream().collect(Collectors.toMap(
                d -> d.getPackageName().getName(), d -> d.getEvr().toString(),
                // See PR#2839
                (a, b)-> a));
        ret.put(State.apply(Arrays.asList(PACKAGES_PKGREMOVE),
                Optional.of(singletonMap(PARAM_PKGS, pkgs)),
                Optional.of(true)), minions);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionServer>> packagesRefreshListAction(
            List<MinionServer> minions) {
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();
        ret.put(State.apply(Arrays.asList(ApplyStatesEventMessage.PACKAGES_PROFILE_UPDATE),
                Optional.empty(), Optional.of(true)), minions);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionServer>> hardwareRefreshListAction(
            List<MinionServer> minions) {
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();

        // salt-ssh minions in the 'true' partition
        // regular minions in the 'false' partition
        Map<Boolean, List<MinionServer>> partitionBySSHPush = minions.stream()
                .collect(Collectors.partitioningBy(MinionServerUtils::isSshPushMinion));

        // Separate SSH push minions from regular minions to apply different states
        List<MinionServer> sshPushMinions = partitionBySSHPush.get(true);
        List<MinionServer> regularMinions = partitionBySSHPush.get(false);

        if (!sshPushMinions.isEmpty()) {
            ret.put(State.apply(Arrays.asList(
                    ApplyStatesEventMessage.HARDWARE_PROFILE_UPDATE),
                    Optional.empty(), Optional.of(true)), sshPushMinions);
        }
        if (!regularMinions.isEmpty()) {
            ret.put(State.apply(Arrays.asList(ApplyStatesEventMessage.SYNC_CUSTOM_ALL,
                    ApplyStatesEventMessage.HARDWARE_PROFILE_UPDATE),
                    Optional.empty(), Optional.of(true)), regularMinions);
        }

        return ret;
    }

    private Map<LocalCall<?>, List<MinionServer>> rebootAction(List<MinionServer> minions) {
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();
        ret.put(com.suse.salt.netapi.calls.modules.System
                .reboot(Optional.of(3)), minions);
        return ret;
    }

    /**
     * Deploy files(files, directory, symlink) through state.apply
     *
     * @param minions target systems
     * @param action action which has all the revisions
     * @return minions grouped by local call
     */
    private Map<LocalCall<?>, List<MinionServer>> deployFiles(List<MinionServer> minions, ConfigAction action) {
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();
        Set<MinionServer> targetSet = new HashSet<>(minions);
        Map<MinionServer, Set<ConfigRevision>> serverConfigMap = action.getConfigRevisionActions()
                .stream()
                .filter(cra -> targetSet.contains(cra.getServer()))
                .collect(Collectors.groupingBy(
                        cra -> cra.getServer().asMinionServer().get(),
                        Collectors.mapping(ConfigRevisionAction::getConfigRevision, Collectors.toSet())));
        Map<Set<ConfigRevision>, Set<MinionServer>> revsServersMap = serverConfigMap.entrySet()
                .stream()
                .collect(Collectors.groupingBy(entry -> entry.getValue(),
                        Collectors.mapping(entry -> entry.getKey(), Collectors.toSet())));
        revsServersMap.forEach((configRevisions, selectedServers) -> {
            List<Map<String, Object>> fileStates = configRevisions
                    .stream()
                    .map(revision -> ConfigChannelSaltManager.getInstance().getStateParameters(revision))
                    .collect(Collectors.toList());
            ret.put(State.apply(Arrays.asList(CONFIG_DEPLOY_FILES),
                    Optional.of(Collections.singletonMap(PARAM_FILES, fileStates)),
                    Optional.of(true)), selectedServers.stream().collect(Collectors.toList()));
        });
        return ret;
    }

    /**
     * Deploy files(files, directory, symlink) through state.apply
     *
     * @param minions target systems
     * @param action action which has all the revisions
     * @return minions grouped by local call
     */
    private Map<LocalCall<?>, List<MinionServer>> diffFiles(List<MinionServer> minions, ConfigAction action) {
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();
        List<Map<String, Object>> fileStates = action.getConfigRevisionActions().stream()
                .map(revAction -> revAction.getConfigRevision())
                .filter(revision -> revision.isFile() ||
                        revision.isDirectory() ||
                        revision.isSymlink())
                .map(revision -> ConfigChannelSaltManager.getInstance().getStateParameters(revision))
                .collect(Collectors.toList());
        ret.put(com.suse.manager.webui.utils.salt.State.apply(
                Arrays.asList(CONFIG_DIFF_FILES),
                Optional.of(Collections.singletonMap(PARAM_FILES, fileStates)),
                Optional.of(true), Optional.of(true)), minions);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionServer>> remoteCommandAction(
            List<MinionServer> minions, ScriptAction scriptAction) {
        String script = scriptAction.getScriptActionDetails().getScriptContents();

        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();
//        // FIXME: This supports only bash at the moment
//        ret.put(Cmd.execCodeAll(
//                "bash",
//                // remove \r or bash will fail
//                script.replaceAll("\r\n", "\n")), minions);

        // write script to /srv/susemanager/salt/scripts/script_<id>.sh
        Path scriptsDir = Paths.get(SUMA_STATE_FILES_ROOT_PATH, SCRIPTS_DIR);
        Path scriptFile = scriptsDir.resolve("script_" + scriptAction.getId() + ".sh");
        try {
            FileUtils.forceMkdir(scriptsDir.toFile()); // TODO set permissions?
            FileUtils.writeStringToFile(scriptFile.toFile(),
                    script.replaceAll("\r\n", "\n"));
        }
        catch (IOException e) {
            LOG.error("Could not write script to file " + scriptFile, e);
        }

        // state.apply remotecommands
        Map<String, Object> pillar = new HashMap<>();
        pillar.put("mgr_remote_cmd_script",
                "salt://" + SCRIPTS_DIR + "/" + scriptFile.getFileName());
        pillar.put("mgr_remote_cmd_runas",
                scriptAction.getScriptActionDetails().getUsername());
        ret.put(com.suse.manager.webui.utils.salt.State.apply(
                Arrays.asList(REMOTE_COMMANDS),
                Optional.of(pillar),
                Optional.of(true), Optional.of(false)), minions);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionServer>> applyStatesAction(
            List<MinionServer> minions, List<String> mods, boolean test) {
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();
        ret.put(com.suse.manager.webui.utils.salt.State.apply(mods, Optional.empty(), Optional.of(true),
                test ? Optional.of(test) : Optional.empty()), minions);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionServer>> subscribeChanelsAction(
            List<MinionServer> minions, SubscribeChannelsActionDetails actionDetails) {
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();

        minions.forEach(minion -> {
            List<AccessToken> tokens = new ArrayList<>();

            // generate access tokens
            Set<Channel> allChannels = new HashSet<>();
            allChannels.addAll(actionDetails.getChannels());
            if (actionDetails.getBaseChannel() != null) {
                allChannels.add(actionDetails.getBaseChannel());
            }
            AccessToken newToken = AccessTokenFactory.generate(minion, allChannels)
                    .orElseThrow(() ->
                            new RuntimeException("Could not generate new token for " +
                                    minion.getMinionId()));
            newToken.setValid(false);
            // persist the access tokens to be activated by the Salt job return event
            // if the state.apply channels returns successfully
            actionDetails.getAccessTokens().add(newToken);

            tokens.add(newToken);

            tokens.forEach(accessToken -> {
                Map<String, Object> chanPillar = new HashMap<>();
                accessToken.getChannels().forEach(chan -> {
                    Map<String, Object> chanProps =
                            SaltStateGeneratorService.getChannelPillarData(minion, accessToken, chan);
                    chanPillar.put(chan.getLabel(), chanProps);
                });
                Map<String, Object> pillar = new HashMap<>();
                pillar.put("_mgr_channels_items_name", "mgr_channels_new");
                pillar.put("mgr_channels_new", chanPillar);

                ret.put(State.apply(Arrays.asList(ApplyStatesEventMessage.CHANNELS),
                        Optional.of(pillar), Optional.of(true)), Collections.singletonList(minion));

            });
        });
        // we must be sure that tokens and action Details are in the database
        // before we return and send the salt calls to update the minions.
        HibernateFactory.commitTransaction();

        return ret;
    }

    private static Map<String, Object> dockerRegPillar(List<ImageStore> stores) {
        Map<String, Object> dockerRegistries = new HashMap<>();
        stores.forEach(store -> {
            Optional.ofNullable(store.getCreds())
                    .ifPresent(credentials -> {
                        Map<String, Object> reg = new HashMap<>();
                        reg.put("email", "tux@example.com");
                        reg.put("password", credentials.getPassword());
                        reg.put("username", credentials.getUsername());
                        dockerRegistries.put(store.getUri(), reg);
                    });
        });
        return dockerRegistries;
    }

    private Map<LocalCall<?>, List<MinionServer>> imageInspectAction(
            List<MinionServer> minions, String version,
            String name, ImageStore store) {
        Map<String, Object> pillar = new HashMap<>();
        pillar.put("imagename", store.getUri() + "/" + name + ":" + version);
        Map<LocalCall<?>, List<MinionServer>> result = new HashMap<>();
        LocalCall<Map<String, State.ApplyResult>> apply = State.apply(
                Collections.singletonList("images.profileupdate"),
                Optional.of(pillar),
                Optional.of(true)
        );
        result.put(apply, minions);
        return result;
    }

    private Map<LocalCall<?>, List<MinionServer>> imageBuildAction(
            List<MinionServer> minions, Optional<String> version,
            DockerfileProfile profile, User user) {
        List<ImageStore> imageStores = new LinkedList<>();
        imageStores.add(profile.getTargetStore());
        String cert = "";
        try {
            //TODO: maybe from the database
            cert = Files.readAllLines(
                    Paths.get("/srv/www/htdocs/pub/RHN-ORG-TRUSTED-SSL-CERT"),
                    Charset.defaultCharset()
            ).stream().collect(Collectors.joining("\n\n"));
        }
        catch (IOException e) {
            LOG.error("Could not read certificate", e);
        }
        final String certificate = cert;

        //TODO: optimal scheduling would be to group by host and orgid
        Map<LocalCall<?>, List<MinionServer>> ret = minions.stream().collect(
                Collectors.toMap(minion -> {

                    //TODO: refactor ActivationKeyHandler.listChannels to share this logic
                    TokenBuilder tokenBuilder = new TokenBuilder(minion.getOrg().getId());
                    tokenBuilder.useServerSecret();
                    tokenBuilder.setExpirationTimeMinutesInTheFuture(
                            Config.get().getInt(
                                    ConfigDefaults.TEMP_TOKEN_LIFETIME
                            )
                    );
                    if (profile.getToken() != null) {
                        tokenBuilder.onlyChannels(profile.getToken().getChannels()
                                .stream().map(Channel::getLabel)
                                .collect(Collectors.toSet()));
                    }
                    String t = "";
                    try {
                        t = tokenBuilder.getToken();
                    }
                    catch (JoseException e) {
                        e.printStackTrace();
                    }
                    final String token = t;


                    Map<String, Object> pillar = new HashMap<>();
                    Map<String, Object> dockerRegistries = dockerRegPillar(imageStores);
                    pillar.put("docker-registries", dockerRegistries);
                    String repoPath = profile.getTargetStore().getUri() + "/" + profile.getLabel();
                    String tag = version.orElse("");
                    pillar.put("imagerepopath", repoPath);
                    pillar.put("imagetag", tag);
                    pillar.put("imagename", repoPath + ":" + tag);
                    pillar.put("builddir", profile.getPath());

                    String host = SaltStateGeneratorService.getChannelHost(minion);
                    String repocontent = "";
                    if (profile.getToken() != null) {
                        repocontent = profile.getToken().getChannels().stream().map(s ->
                        {
                            return "[susemanager:" + s.getLabel() + "]\n\n" +
                                    "name=" + s.getName() + "\n\n" +
                                    "enabled=1\n\n" +
                                    "autorefresh=1\n\n" +
                                    "baseurl=https://" + host +
                                    ":443/rhn/manager/download/" + s.getLabel() + "?" +
                                    token + "\n\n" +
                                    "type=rpm-md\n\n" +
                                    "gpgcheck=1\n\n" +
                                    "repo_gpgcheck=0\n\n" +
                                    "pkg_gpgcheck=1\n\n";
                        }).collect(Collectors.joining("\n\n"));
                    }
                    pillar.put("repo", repocontent);
                    pillar.put("cert", certificate);

                    return State.apply(
                            Collections.singletonList("images.docker"),
                            Optional.of(pillar),
                            Optional.of(true)
                    );
                },
                Collections::singletonList
        ));

        return ret;
    }

    private Map<LocalCall<?>, List<MinionServer>> distUpgradeAction(
            DistUpgradeAction action,
            List<MinionServer> minions) {
        Map<Boolean, List<Channel>> collect = action.getDetails().getChannelTasks()
                .stream().collect(Collectors.partitioningBy(
                        ct -> ct.getTask() == DistUpgradeChannelTask.SUBSCRIBE,
                        Collectors.mapping(DistUpgradeChannelTask::getChannel,
                                Collectors.toList())
                        ));

        List<Channel> subbed = collect.get(true);
        List<Channel> unsubbed = collect.get(false);

        action.getServerActions()
        .stream()
        .flatMap(s -> Opt.stream(s.getServer().asMinionServer()))
        .forEach(minion -> {
            Set<Channel> currentChannels = minion.getChannels();
            currentChannels.removeAll(unsubbed);
            currentChannels.addAll(subbed);
            ServerFactory.save(minion);
            SaltStateGeneratorService.INSTANCE.generatePillar(minion);
        });

        Map<String, Object> pillar = new HashMap<>();
        Map<String, Object> susemanager = new HashMap<>();
        pillar.put("susemanager", susemanager);
        Map<String, Object> distupgrade = new HashMap<>();
        susemanager.put("distupgrade", distupgrade);
        distupgrade.put("dryrun", action.getDetails().isDryRun());
        distupgrade.put("channels", subbed.stream()
                .sorted()
                .map(c -> "susemanager:" + c.getLabel())
                .collect(Collectors.toList()));

        LocalCall<Map<String, State.ApplyResult>> distUpgrade = State.apply(
                Collections.singletonList(ApplyStatesEventMessage.DISTUPGRADE),
                Optional.of(pillar),
                Optional.of(true)
                );
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();
        ret.put(distUpgrade, minions);

        return ret;
    }

    private Map<LocalCall<?>, List<MinionServer>> scapXccdfEvalAction(
            List<MinionServer> minions, ScapActionDetails scapActionDetails) {
        Map<LocalCall<?>, List<MinionServer>> ret = new HashMap<>();
        String parameters = "eval " +
            scapActionDetails.getParametersContents() + " " + scapActionDetails.getPath();
        ret.put(State.apply(singletonList("scap"),
                Optional.of(singletonMap("mgr_scap_params", (Object)parameters)),
                Optional.of(false)),
                minions);
        return ret;
    }

    /**
     * Prepare to execute staging job via Salt
     * @param actionIn the action
     * @param minions minions to target
     * @return a call with the impacted minions
     */
    private LocalCall<?> prepareStagingTargets(Action actionIn,
            List<MinionServer> minions) {
        LocalCall<?> call = null;
        if (actionIn.getActionType().equals(ActionFactory.TYPE_PACKAGES_UPDATE)) {
            Map<String, String> args =
                    ((PackageUpdateAction) actionIn).getDetails().stream()
                            .collect(Collectors.toMap(d -> d.getPackageName().getName(),
                                    // See PR#2839
                                    d -> d.getEvr().toString(), (a, b)-> a));
            call = State.apply(Arrays.asList(PACKAGES_PKGDOWNLOAD),
                    Optional.of(Collections.singletonMap(PARAM_PKGS, args)),
                    Optional.of(true));
            LOG.info("Executing staging of packages");
        }
        if (actionIn.getActionType().equals(ActionFactory.TYPE_ERRATA)) {
            Set<Long> errataIds = ((ErrataAction) actionIn).getErrata().stream()
                    .map(e -> e.getId()).collect(Collectors.toSet());
            Map<Long, Map<Long, Set<ErrataInfo>>> errataNames = ServerFactory
                    .listErrataNamesForServers(minions.stream().map(MinionServer::getId)
                            .collect(Collectors.toSet()), errataIds);
            List<String> errataArgs = errataNames.entrySet().stream()
                .flatMap(e -> e.getValue().entrySet().stream()
                     .flatMap(f -> f.getValue().stream()
                         .map(ErrataInfo::getName)
                     )
                )
                .collect(Collectors.toList());

            call = State.apply(Arrays.asList(PACKAGES_PATCHDOWNLOAD),
                    Optional.of(Collections.singletonMap(PARAM_PATCHES, errataArgs)),
                    Optional.of(true));
            LOG.info("Executing staging of patches");
        }
        return call;
    }

    private Pair<LocalCall<?>, Set<MinionServer>> startActionChainCall(
            Set<MinionServer> minions, Long actionChainId) {
        List<String> args = new ArrayList<>(1);
        args.add(Long.toString(actionChainId));
        LocalCall<?> call = new LocalCall("mgractionchains.start",
                Optional.of(args), Optional.empty(), new TypeToken<Map<String, State.ApplyResult>>() { });
        return new ImmutablePair<>(call, minions);
    }

    /**
     * @param actionIn the action
     * @param call the call
     * @param minions minions to target
     * @param forcePackageListRefresh add metadata to force a package list
     * refresh
     * @param isStagingJob if the job is a staging job
     * @return a map containing all minions partitioned by success
     */
    private Map<Boolean, List<MinionServer>> execute(Action actionIn, LocalCall<?> call,
            List<MinionServer> minions, boolean forcePackageListRefresh,
            boolean isStagingJob) {
        // Prepare the metadata
        Map<String, Object> metadata = new HashMap<>();

        if (!isStagingJob) {
            metadata.put(ScheduleMetadata.SUMA_ACTION_ID, actionIn.getId());
        }
        if (forcePackageListRefresh) {
            metadata.put(ScheduleMetadata.SUMA_FORCE_PGK_LIST_REFRESH, true);
        }

        List<String> minionIds = minions.stream().map(MinionServer::getMinionId)
                .collect(Collectors.toList());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing action for: " +
                    minionIds.stream().collect(Collectors.joining(", ")));
        }

        try {
            Map<Boolean, List<MinionServer>> result = new HashMap<>();

            List<String> results = SaltService.INSTANCE
                    .callAsync(call.withMetadata(metadata), new MinionList(minionIds))
                    .getMinions();

            result = minions.stream().collect(Collectors
                    .partitioningBy(minion -> results.contains(minion.getMinionId())));

            return result;
        }
        catch (SaltException ex) {
            LOG.debug("Failed to execute action: " + ex.getMessage());
            Map<Boolean, List<MinionServer>> result = new HashMap<>();
            result.put(true, Collections.emptyList());
            result.put(false, minions);
            return result;
        }
    }

    /**
     * @param actionChainId the id of the action chain
     * @param call the call
     * @param minions minions to target
     * @return a map containing all minions partitioned by success
     */
    private Map<Boolean, ? extends Collection<MinionServer>> callAsyncActionChainStart(Long actionChainId,
                                                                       LocalCall<?> call, Set<MinionServer> minions) {
        // Prepare the metadata
        Map<String, Object> metadata = new HashMap<>();
        metadata.put("suma-action-chain", true);

        List<String> minionIds = minions.stream().map(MinionServer::getMinionId)
                .collect(Collectors.toList());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing action chain for: " +
                    minionIds.stream().collect(Collectors.joining(", ")));
        }

        try {
            List<String> results = SaltService.INSTANCE
                    .callAsync(call.withMetadata(metadata), new MinionList(minionIds))
                    .getMinions();

            Map<Boolean, ? extends Collection<MinionServer>> result = minions.stream().collect(Collectors
                    .partitioningBy(minion -> results.contains(minion.getMinionId())));

            return result;
        }
        catch (SaltException ex) {
            LOG.debug("Failed to execute action chain: " + ex.getMessage());
            Map<Boolean, Set<MinionServer>> result = new HashMap<>();
            result.put(true, Collections.emptySet());
            result.put(false, minions);
            return result;
        }
    }

    /**
     * @param saltActionChainGeneratorServiceIn to set
     */
    public void setSaltActionChainGeneratorService(SaltActionChainGeneratorService
                                                           saltActionChainGeneratorServiceIn) {
        this.saltActionChainGeneratorService = saltActionChainGeneratorServiceIn;
    }
}
