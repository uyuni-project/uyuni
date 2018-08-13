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

import static com.redhat.rhn.domain.server.MinionServerFactory.findRegularMinionIds;
import static com.suse.utils.Opt.fold;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static java.util.Optional.ofNullable;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
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
import com.redhat.rhn.domain.server.MinionIds;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.TokenBuilder;
import com.suse.manager.webui.utils.salt.State;
import com.suse.manager.webui.utils.salt.custom.Openscap;
import com.suse.manager.webui.utils.salt.custom.ScheduleMetadata;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.Cmd;
import com.suse.salt.netapi.calls.modules.State.ApplyResult;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.utils.Opt;
import org.apache.log4j.Logger;
import org.jose4j.lang.JoseException;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Takes {@link Action} objects to be executed via salt.
 */
public class SaltServerActionService {

    /* Singleton instance of this class */
    public static final SaltServerActionService INSTANCE = new SaltServerActionService();

    /* Logger for this class */
    private static final Logger LOG = Logger.getLogger(SaltServerActionService.class);
    private static final String PACKAGES_PKGINSTALL = "packages.pkginstall";
    private static final String PACKAGES_PKGDOWNLOAD = "packages.pkgdownload";
    private static final String PACKAGES_PATCHINSTALL = "packages.patchinstall";
    private static final String PACKAGES_PATCHDOWNLOAD = "packages.patchdownload";
    private static final String PACKAGES_PKGREMOVE = "packages.pkgremove";
    private static final String CONFIG_DEPLOY_FILES = "configuration.deploy_files";
    private static final String CONFIG_DIFF_FILES = "configuration.diff_files";
    private static final String PARAM_PKGS = "param_pkgs";
    private static final String PARAM_PATCHES = "param_patches";
    private static final String PARAM_FILES = "param_files";



    /** SLS pillar parameter name for the list of update stack patch names. */
    public static final String PARAM_UPDATE_STACK_PATCHES = "param_update_stack_patches";

    /** SLS pillar parameter name for the list of regular patch names. */
    public static final String PARAM_REGULAR_PATCHES = "param_regular_patches";

    private boolean commitTransaction = true;

    /**
     * For a given action return the salt call(s) that need to be
     * executed for regular (non-SSH) targeted minions.
     *
     * @param actionIn the action to be executed
     * @return map of Salt local call to list of targeted minion ids
     */
    public Map<LocalCall<?>, List<MinionIds>> callsForAction(Action actionIn) {
        return callsForAction(actionIn, empty());
    }

    /**
     * For a given action return the salt call(s) that need to be executed for regular (non-SSH) targeted minions,
     * or optionally, for a single Salt-SSH minion.
     *
     * @param actionIn the action to be executed
     * @param limitToSSHPushMinion if action is meant for one single SSH-Push minion, limit outputted calls
     *                             to that minion
     * @return map of Salt local call to list of targeted minion ids
     */
    public Map<LocalCall<?>, List<MinionIds>> callsForAction(Action actionIn,
            Optional<MinionIds> limitToSSHPushMinion) {

        List<MinionIds> minionIds = fold(
                limitToSSHPushMinion,
                () -> findRegularMinionIds(actionIn.getId()),
                sshPushMinion -> asList(sshPushMinion)
        );

        ActionType actionType = actionIn.getActionType();
        if (ActionFactory.TYPE_ERRATA.equals(actionType)) {
            ErrataAction errataAction = (ErrataAction) actionIn;
            Set<Long> errataIds = errataAction.getErrata().stream()
                    .map(Errata::getId).collect(toSet());
            return errataAction(minionIds, errataIds);
        }
        else if (ActionFactory.TYPE_PACKAGES_UPDATE.equals(actionType)) {
            return packagesUpdateAction(minionIds, (PackageUpdateAction) actionIn);
        }
        else if (ActionFactory.TYPE_PACKAGES_REMOVE.equals(actionType)) {
            return packagesRemoveAction(minionIds, (PackageRemoveAction) actionIn);
        }
        else if (ActionFactory.TYPE_PACKAGES_REFRESH_LIST.equals(actionType)) {
            return packagesRefreshListAction(minionIds);
        }
        else if (ActionFactory.TYPE_HARDWARE_REFRESH_LIST.equals(actionType)) {
            return hardwareRefreshListAction(minionIds, limitToSSHPushMinion);
        }
        else if (ActionFactory.TYPE_REBOOT.equals(actionType)) {
            return rebootAction(minionIds);
        }
        else if (ActionFactory.TYPE_CONFIGFILES_DEPLOY.equals(actionType)) {
            return deployFiles(minionIds, (ConfigAction) actionIn);
        }
        else if (ActionFactory.TYPE_CONFIGFILES_DIFF.equals(actionType)) {
            return diffFiles(minionIds, (ConfigAction) actionIn);
        }
        else if (ActionFactory.TYPE_SCRIPT_RUN.equals(actionType)) {
            ScriptAction scriptAction = (ScriptAction) actionIn;
            String script = scriptAction.getScriptActionDetails().getScriptContents();
            return remoteCommandAction(minionIds, script);
        }
        else if (ActionFactory.TYPE_APPLY_STATES.equals(actionType)) {
            ApplyStatesActionDetails actionDetails = ((ApplyStatesAction) actionIn).getDetails();
            return applyStatesAction(minionIds, actionDetails.getMods(), actionDetails.isTest());
        }
        else if (ActionFactory.TYPE_IMAGE_INSPECT.equals(actionType)) {
            ImageInspectAction iia = (ImageInspectAction) actionIn;
            ImageInspectActionDetails details = iia.getDetails();
            ImageStore store = ImageStoreFactory.lookupById(
                    details.getImageStoreId()).get();
            return imageInspectAction(minionIds, details.getVersion(), details.getName(),
                    store);
        }
        else if (ActionFactory.TYPE_IMAGE_BUILD.equals(actionType)) {
            ImageBuildAction imageBuildAction = (ImageBuildAction) actionIn;
            ImageBuildActionDetails details = imageBuildAction.getDetails();

            return ImageProfileFactory.lookupById(details.getImageProfileId())
                    .flatMap(ImageProfile::asDockerfileProfile).map(
                    ip -> imageBuildAction(
                            minionIds,
                            ofNullable(details.getVersion()),
                            ip,
                            imageBuildAction.getSchedulerUser())
            ).orElseGet(Collections::emptyMap);
        }
        else if (ActionFactory.TYPE_DIST_UPGRADE.equals(actionType)) {
            return distUpgradeAction((DistUpgradeAction) actionIn, minionIds);
        }
        else if (ActionFactory.TYPE_SCAP_XCCDF_EVAL.equals(actionType)) {
            ScapAction scapAction = (ScapAction)actionIn;
            return scapXccdfEvalAction(minionIds, scapAction.getScapActionDetails());
        }
        else if (ActionFactory.TYPE_SUBSCRIBE_CHANNELS.equals(actionType)) {
            SubscribeChannelsAction subscribeAction = (SubscribeChannelsAction)actionIn;
            return subscribeChanelsAction(minionIds, subscribeAction.getDetails());
        }
        else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Action type " +
                        (actionType != null ? actionType.getName() : "") +
                        " is not supported with Salt");
            }
            return emptyMap();
        }
    }

    private Optional<ServerAction> serverActionFor(Action actionIn, MinionIds minion) {
        return actionIn.getServerActions().stream()
                .filter(sa -> sa.getServerId().equals(minion.getServerId()))
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

        // now prepare each call
        for (Map.Entry<LocalCall<?>, List<MinionIds>> entry : callsForAction(actionIn).entrySet()) {
            LocalCall<?> call = entry.getKey();
            final List<MinionIds> targetMinions;
            Map<Boolean, List<MinionIds>> results;

            if (isStagingJob) {
                targetMinions = new ArrayList<>();
                stagingJobMinionServerId
                        .ifPresent(serverId -> MinionServerFactory.lookupById(serverId)
                                .ifPresent(server -> targetMinions.add(new MinionIds(server))));
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
     * This function will return a map with list of minions grouped by the
     * salt netapi local call that executes what needs to be executed on
     * those minions for the given errata ids and minions.
     *
     * @param minionIds list of minions ids
     * @param errataIds list of errata ids
     * @return minionsIds grouped by local call
     */
    public Map<LocalCall<?>, List<MinionIds>> errataAction(List<MinionIds> minionIds, Set<Long> errataIds) {
        Set<Long> minionServerIds = minionIds.stream()
                .map(MinionIds::getServerId)
                .collect(toSet());

        Map<Long, Map<Long, Set<ErrataInfo>>> errataInfos =
                ServerFactory.listErrataNamesForServers(minionServerIds, errataIds);

        // Group targeted minions by errata names
        Map<Set<ErrataInfo>, List<MinionIds>> collect = minionIds.stream()
                .collect(Collectors.groupingBy(minionId -> errataInfos.get(minionId.getServerId())
                        .entrySet().stream()
                        .map(Map.Entry::getValue)
                        .flatMap(Set::stream)
                        .collect(toSet())
        ));

        // Convert errata names to LocalCall objects of type State.apply
        return collect.entrySet().stream()
            .collect(toMap(entry -> {
                Map<String, Object> params = new HashMap<>();
                params.put(PARAM_REGULAR_PATCHES,
                    entry.getKey().stream()
                        .filter(e -> !e.isUpdateStack())
                        .map(e -> e.getName())
                        .sorted()
                        .collect(toList())
                );
                params.put(PARAM_UPDATE_STACK_PATCHES,
                    entry.getKey().stream()
                        .filter(e -> e.isUpdateStack())
                        .map(e -> e.getName())
                        .sorted()
                        .collect(toList())
                );
                return State.apply(
                        asList(PACKAGES_PATCHINSTALL),
                        of(params)
                );
            },
            Map.Entry::getValue));
    }

    private Map<LocalCall<?>, List<MinionIds>> packagesUpdateAction(List<MinionIds> minionIds,
            PackageUpdateAction action) {
        Map<LocalCall<?>, List<MinionIds>> ret = new HashMap<>();

        List<List<String>> pkgs = action.getDetails().stream().map(
                d -> asList(d.getPackageName().getName(), d.getArch().getName(), d.getEvr().toString())
        ).collect(toList());

        ret.put(State.apply(asList(PACKAGES_PKGINSTALL), of(singletonMap(PARAM_PKGS, pkgs))), minionIds);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionIds>> packagesRemoveAction(List<MinionIds> minionIds,
            PackageRemoveAction action) {
        Map<LocalCall<?>, List<MinionIds>> ret = new HashMap<>();

        List<List<String>> pkgs = action.getDetails().stream().map(
                d -> asList(d.getPackageName().getName(), d.getArch().getName(), d.getEvr().toString())
        ).collect(toList());

        ret.put(State.apply(asList(PACKAGES_PKGREMOVE), of(singletonMap(PARAM_PKGS, pkgs))), minionIds);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionIds>> packagesRefreshListAction(List<MinionIds> minionIds) {
        Map<LocalCall<?>, List<MinionIds>> ret = new HashMap<>();

        ret.put(State.apply(asList(ApplyStatesEventMessage.PACKAGES_PROFILE_UPDATE), empty()), minionIds);

        return ret;
    }

    private Map<LocalCall<?>, List<MinionIds>> hardwareRefreshListAction(List<MinionIds> minionIds,
            Optional<MinionIds> limitToSSHPushMinion) {
        Map<LocalCall<?>, List<MinionIds>> ret = new HashMap<>();

        if (limitToSSHPushMinion.isPresent()) {
            ret.put(State.apply(asList(
                    ApplyStatesEventMessage.HARDWARE_PROFILE_UPDATE),
                    empty()), minionIds);
        }
        else {
            ret.put(State.apply(asList(
                    ApplyStatesEventMessage.SYNC_CUSTOM_ALL,
                    ApplyStatesEventMessage.HARDWARE_PROFILE_UPDATE),
                    empty()), minionIds);
        }

        return ret;
    }

    private Map<LocalCall<?>, List<MinionIds>> rebootAction(List<MinionIds> minionIds) {
        Map<LocalCall<?>, List<MinionIds>> ret = new HashMap<>();

        ret.put(com.suse.salt.netapi.calls.modules.System.reboot(of(3)), minionIds);

        return ret;
    }

    /**
     * Deploy files(files, directory, symlink) through state.apply
     *
     * @param minionIds target system ids
     * @param action action which has all the revisions
     * @return minion ids grouped by local call
     */
    private Map<LocalCall<?>, List<MinionIds>> deployFiles(List<MinionIds> minionIds, ConfigAction action) {
        Map<LocalCall<?>, List<MinionIds>> ret = new HashMap<>();

        Map<Long, MinionIds> targetSet = minionIds.stream().
                collect(toMap(MinionIds::getServerId, identity()));

        Map<MinionIds, Set<ConfigRevision>> serverConfigMap = action.getConfigRevisionActions()
                .stream()
                .filter(cra -> targetSet.containsKey(cra.getServer().getId()))
                .collect(Collectors.groupingBy(
                        cra -> targetSet.get(cra.getServer().getId()),
                        Collectors.mapping(ConfigRevisionAction::getConfigRevision, toSet())));
        Map<Set<ConfigRevision>, Set<MinionIds>> revsServersMap = serverConfigMap.entrySet()
                .stream()
                .collect(Collectors.groupingBy(entry -> entry.getValue(),
                        Collectors.mapping(entry -> entry.getKey(), toSet())));

        revsServersMap.forEach((configRevisions, selectedServers) -> {
            List<Map<String, Object>> fileStates = configRevisions
                    .stream()
                    .map(revision -> ConfigChannelSaltManager.getInstance().getStateParameters(revision))
                    .collect(toList());
            ret.put(State.apply(asList(CONFIG_DEPLOY_FILES),
                    of(singletonMap(PARAM_FILES, fileStates))),
                    selectedServers.stream().collect(toList()));
        });

        return ret;
    }

    /**
     * Deploy files(files, directory, symlink) through state.apply
     *
     * @param minionIds target system ids
     * @param action action which has all the revisions
     * @return minion Ids grouped by local call
     */
    private Map<LocalCall<?>, List<MinionIds>> diffFiles(List<MinionIds> minionIds, ConfigAction action) {
        Map<LocalCall<?>, List<MinionIds>> ret = new HashMap<>();

        List<Map<String, Object>> fileStates = action.getConfigRevisionActions().stream()
                .map(revAction -> revAction.getConfigRevision())
                .filter(revision -> revision.isFile() ||
                        revision.isDirectory() ||
                        revision.isSymlink())
                .map(revision -> ConfigChannelSaltManager.getInstance().getStateParameters(revision))
                .collect(toList());

        ret.put(com.suse.salt.netapi.calls.modules.State.apply(
                asList(CONFIG_DIFF_FILES),
                of(singletonMap(PARAM_FILES, fileStates)),
                of(true), of(true)), minionIds);

        return ret;
    }

    private Map<LocalCall<?>, List<MinionIds>> remoteCommandAction(List<MinionIds> minionIds, String script) {
        Map<LocalCall<?>, List<MinionIds>> ret = new HashMap<>();

        // FIXME: This supports only bash at the moment
        ret.put(Cmd.execCodeAll(
                "bash",
                // remove \r or bash will fail
                script.replaceAll("\r\n", "\n")), minionIds);

        return ret;
    }

    private Map<LocalCall<?>, List<MinionIds>> applyStatesAction(List<MinionIds> minionIds, List<String> mods,
            boolean test) {
        Map<LocalCall<?>, List<MinionIds>> ret = new HashMap<>();

        ret.put(com.suse.salt.netapi.calls.modules.State.apply(mods, empty(), of(true),
                test ? of(test) : empty()), minionIds);

        return ret;
    }

    private Map<LocalCall<?>, List<MinionIds>> subscribeChanelsAction(List<MinionIds> minionIds,
            SubscribeChannelsActionDetails actionDetails) {
        Map<LocalCall<?>, List<MinionIds>> ret = new HashMap<>();

        Stream<MinionServer> minions = MinionServerFactory.lookupByIds(
                minionIds.stream().map(MinionIds::getServerId).collect(toList()));

        minions.forEach(minion -> {
            // generate new access tokens
            Set<Channel> allChannels = new HashSet();
            allChannels.addAll(actionDetails.getChannels());
            if (actionDetails.getBaseChannel() != null) {
                allChannels.add(actionDetails.getBaseChannel());
            }

            List<AccessToken> newTokens = allChannels.stream()
                    .map(channel ->
                            AccessTokenFactory.generate(minion, Collections.singleton(channel))
                                    .orElseThrow(() ->
                                            new RuntimeException(
                                                    "Could not generate new channel access token for minion " +
                                                            minion.getMinionId() + " and channel " +
                                                            channel.getName())))
                    .collect(toList());

            newTokens.forEach(newToken -> {
                // persist the access tokens to be activated by the Salt job return event
                // if the state.apply channels returns successfully
                newToken.setValid(false);
                actionDetails.getAccessTokens().add(newToken);
            });

            Map<String, Object> chanPillar = new HashMap<>();
            newTokens.forEach(accessToken ->
                accessToken.getChannels().forEach(chan -> {
                    Map<String, Object> chanProps =
                            SaltStateGeneratorService.getChannelPillarData(minion, accessToken, chan);
                    chanPillar.put(chan.getLabel(), chanProps);
                })
            );

            Map<String, Object> pillar = new HashMap<>();
            pillar.put("_mgr_channels_items_name", "mgr_channels_new");
            pillar.put("mgr_channels_new", chanPillar);

            ret.put(State.apply(asList(ApplyStatesEventMessage.CHANNELS),
                    of(pillar)),
                    singletonList(new MinionIds(minion)));

        });
        if (commitTransaction) {
            // we must be sure that tokens and action Details are in the database
            // before we return and send the salt calls to update the minions.
            HibernateFactory.commitTransaction();
        }

        return ret;
    }

    private static Map<String, Object> dockerRegPillar(List<ImageStore> stores) {
        Map<String, Object> dockerRegistries = new HashMap<>();
        stores.forEach(store -> {
            ofNullable(store.getCreds())
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

    private Map<LocalCall<?>, List<MinionIds>> imageInspectAction(List<MinionIds> minionIds, String version,
            String name, ImageStore store) {
        Map<String, Object> pillar = new HashMap<>();
        pillar.put("imagename", store.getUri() + "/" + name + ":" + version);

        Map<LocalCall<?>, List<MinionIds>> result = new HashMap<>();
        LocalCall<Map<String, ApplyResult>> apply = State.apply(
                singletonList("images.profileupdate"),
                of(pillar)
        );
        result.put(apply, minionIds);

        return result;
    }

    private Map<LocalCall<?>, List<MinionIds>> imageBuildAction(List<MinionIds> minionIds, Optional<String> version,
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

        Stream<MinionServer> minions = MinionServerFactory.lookupByIds(
                minionIds.stream().map(MinionIds::getServerId).collect(toList()));

        //TODO: optimal scheduling would be to group by host and orgid
        Map<LocalCall<?>, List<MinionIds>> ret = minions.collect(
                toMap(minion -> {

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
                                .collect(toSet()));
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
                    // salt 2016.11 dockerng require imagename while salt 2018.3 docker requires it separate
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
                            singletonList("images.docker"),
                            of(pillar)
                    );
                }, minion -> singletonList(new MinionIds(minion))

        ));

        return ret;
    }

    private Map<LocalCall<?>, List<MinionIds>> distUpgradeAction(DistUpgradeAction action,  List<MinionIds> minionIds) {
        Map<Boolean, List<Channel>> collect = action.getDetails().getChannelTasks()
                .stream().collect(Collectors.partitioningBy(
                        ct -> ct.getTask() == DistUpgradeChannelTask.SUBSCRIBE,
                        Collectors.mapping(DistUpgradeChannelTask::getChannel,
                                toList())
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
                .collect(toList()));

        LocalCall<Map<String, ApplyResult>> distUpgrade = State.apply(
                singletonList(ApplyStatesEventMessage.DISTUPGRADE),
                of(pillar)
                );
        Map<LocalCall<?>, List<MinionIds>> ret = new HashMap<>();
        ret.put(distUpgrade, minionIds);

        return ret;
    }

    private Map<LocalCall<?>, List<MinionIds>> scapXccdfEvalAction(List<MinionIds> minionIds,
            ScapActionDetails scapActionDetails) {
        Map<LocalCall<?>, List<MinionIds>> ret = new HashMap<>();

        String parameters = "eval " +
            scapActionDetails.getParametersContents() + " " + scapActionDetails.getPath();
        ret.put(Openscap.xccdf(parameters), minionIds);

        return ret;
    }

    /**
     * Prepare to execute staging job via Salt
     * @param actionIn the action
     * @param minionIds target system ids
     * @return a call with the impacted minions
     */
    private LocalCall<?> prepareStagingTargets(Action actionIn, List<MinionIds> minionIds) {
        LocalCall<?> call = null;
        if (actionIn.getActionType().equals(ActionFactory.TYPE_PACKAGES_UPDATE)) {
            List<List<String>> args =
                    ((PackageUpdateAction) actionIn).getDetails().stream().map(
                            d -> asList(d.getPackageName().getName(),
                                    d.getArch().getName(), d.getEvr().toString())
                    ).collect(toList());
            call = State.apply(asList(PACKAGES_PKGDOWNLOAD),
                    of(singletonMap(PARAM_PKGS, args)));
            LOG.info("Executing staging of packages");
        }
        if (actionIn.getActionType().equals(ActionFactory.TYPE_ERRATA)) {
            Set<Long> errataIds = ((ErrataAction) actionIn).getErrata().stream()
                    .map(e -> e.getId()).collect(toSet());
            Map<Long, Map<Long, Set<ErrataInfo>>> errataNames = ServerFactory
                    .listErrataNamesForServers(minionIds.stream().map(MinionIds::getServerId)
                            .collect(toSet()), errataIds);
            List<String> errataArgs = errataNames.entrySet().stream()
                .flatMap(e -> e.getValue().entrySet().stream()
                     .flatMap(f -> f.getValue().stream()
                         .map(ErrataInfo::getName)
                     )
                )
                .collect(toList());

            call = State.apply(asList(PACKAGES_PATCHDOWNLOAD),
                    of(singletonMap(PARAM_PATCHES, errataArgs)));
            LOG.info("Executing staging of patches");
        }
        return call;
    }

    /**
     * @param actionIn the action
     * @param call the call
     * @param minionIds target system ids
     * @param forcePackageListRefresh add metadata to force a package list refresh
     * @param isStagingJob if the job is a staging job
     * @return a map containing all minions partitioned by success
     */
    private Map<Boolean, List<MinionIds>> execute(Action actionIn, LocalCall<?> call, List<MinionIds> minionIds,
            boolean forcePackageListRefresh, boolean isStagingJob) {
        // Prepare the metadata
        Map<String, Object> metadata = new HashMap<>();

        if (!isStagingJob) {
            metadata.put(ScheduleMetadata.SUMA_ACTION_ID, actionIn.getId());
        }
        if (forcePackageListRefresh) {
            metadata.put(ScheduleMetadata.SUMA_FORCE_PGK_LIST_REFRESH, true);
        }

        List<String> minionStrIds = minionIds.stream().map(MinionIds::getMinionId).collect(toList());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing action for: " +
                    minionStrIds.stream().collect(Collectors.joining(", ")));
        }

        try {
            Map<Boolean, List<MinionIds>> result = new HashMap<>();

            List<String> results = SaltService.INSTANCE
                    .callAsync(call.withMetadata(metadata), new MinionList(minionStrIds))
                    .getMinions();

            result = minionIds.stream().collect(Collectors
                    .partitioningBy(minionId -> results.contains(minionId.getMinionId())));

            return result;
        }
        catch (SaltException ex) {
            LOG.debug("Failed to execute action: " + ex.getMessage());
            Map<Boolean, List<MinionIds>> result = new HashMap<>();
            result.put(true, Collections.emptyList());
            result.put(false, minionIds);
            return result;
        }
    }

    /**
     * Whether to commit hibernate transaction or not. Default is commit.
     * Only used in unit tests.
     *
     * @param commitTransactionIn flag to set
     */
    public void setCommitTransaction(boolean commitTransactionIn) {
        this.commitTransaction = commitTransactionIn;
    }
}
