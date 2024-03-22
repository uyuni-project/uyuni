/*
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

import static com.redhat.rhn.common.hibernate.HibernateFactory.unproxy;
import static com.redhat.rhn.domain.action.ActionFactory.STATUS_COMPLETED;
import static com.redhat.rhn.domain.action.ActionFactory.STATUS_FAILED;
import static com.redhat.rhn.domain.action.ActionFactory.STATUS_QUEUED;
import static com.suse.manager.webui.services.SaltConstants.SALT_FS_PREFIX;
import static com.suse.manager.webui.services.SaltConstants.SCRIPTS_DIR;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.partitioningBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainEntry;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionStatus;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsAction;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsActionDetails;
import com.redhat.rhn.domain.action.config.ConfigAction;
import com.redhat.rhn.domain.action.config.ConfigRevisionAction;
import com.redhat.rhn.domain.action.dup.DistUpgradeAction;
import com.redhat.rhn.domain.action.dup.DistUpgradeChannelTask;
import com.redhat.rhn.domain.action.errata.ErrataAction;
import com.redhat.rhn.domain.action.kickstart.KickstartAction;
import com.redhat.rhn.domain.action.kickstart.KickstartActionDetails;
import com.redhat.rhn.domain.action.kickstart.KickstartInitiateAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageLockAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageRemoveAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageUpdateAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesActionDetails;
import com.redhat.rhn.domain.action.salt.PlaybookAction;
import com.redhat.rhn.domain.action.salt.PlaybookActionDetails;
import com.redhat.rhn.domain.action.salt.build.ImageBuildAction;
import com.redhat.rhn.domain.action.salt.build.ImageBuildActionDetails;
import com.redhat.rhn.domain.action.salt.inspect.ImageInspectAction;
import com.redhat.rhn.domain.action.salt.inspect.ImageInspectActionDetails;
import com.redhat.rhn.domain.action.scap.ScapAction;
import com.redhat.rhn.domain.action.scap.ScapActionDetails;
import com.redhat.rhn.domain.action.script.ScriptAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.virtualization.BaseVirtualizationGuestAction;
import com.redhat.rhn.domain.action.virtualization.BaseVirtualizationVolumeAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationCreateGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationDeleteGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationMigrateGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationNetworkCreateAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationNetworkStateChangeAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationPoolCreateAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationPoolDeleteAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationPoolRefreshAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationPoolStartAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationPoolStopAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationRebootGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationResumeGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSetMemoryGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSetVcpusGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationShutdownGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationStartGuestAction;
import com.redhat.rhn.domain.action.virtualization.VirtualizationSuspendGuestAction;
import com.redhat.rhn.domain.channel.AccessToken;
import com.redhat.rhn.domain.channel.AccessTokenFactory;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.errata.ErrataFactory;
import com.redhat.rhn.domain.image.DockerfileProfile;
import com.redhat.rhn.domain.image.ImageProfile;
import com.redhat.rhn.domain.image.ImageProfileFactory;
import com.redhat.rhn.domain.image.ImageStore;
import com.redhat.rhn.domain.image.ImageStoreFactory;
import com.redhat.rhn.domain.image.KiwiProfile;
import com.redhat.rhn.domain.image.ProfileCustomDataValue;
import com.redhat.rhn.domain.kickstart.KickstartFactory;
import com.redhat.rhn.domain.kickstart.KickstartableTree;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.product.Tuple2;
import com.redhat.rhn.domain.rhnpackage.PackageArch;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageName;
import com.redhat.rhn.domain.server.ErrataInfo;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.VirtualInstance;
import com.redhat.rhn.domain.server.VirtualInstanceFactory;
import com.redhat.rhn.domain.token.ActivationKey;
import com.redhat.rhn.domain.token.ActivationKeyFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.PackageListItem;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.kickstart.cobbler.CobblerXMLRPCHelper;
import com.redhat.rhn.manager.rhnpackage.PackageManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.utils.SaltKeyUtils;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.virtualization.DnsHostDef;
import com.suse.manager.virtualization.DnsTxtDef;
import com.suse.manager.virtualization.NetworkDefinition;
import com.suse.manager.virtualization.VirtManagerSalt;
import com.suse.manager.virtualization.VirtStatesHelper;
import com.suse.manager.webui.controllers.virtualization.gson.VirtualGuestsUpdateActionJson;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.services.iface.VirtManager;
import com.suse.manager.webui.services.impl.SaltSSHService;
import com.suse.manager.webui.services.pillar.MinionGeneralPillarGenerator;
import com.suse.manager.webui.services.pillar.MinionPillarManager;
import com.suse.manager.webui.utils.DownloadTokenBuilder;
import com.suse.manager.webui.utils.SaltModuleRun;
import com.suse.manager.webui.utils.SaltState;
import com.suse.manager.webui.utils.SaltSystemReboot;
import com.suse.manager.webui.utils.salt.custom.MgrActionChains;
import com.suse.manager.webui.utils.salt.custom.ScheduleMetadata;
import com.suse.salt.netapi.calls.LocalAsyncResult;
import com.suse.salt.netapi.calls.LocalCall;
import com.suse.salt.netapi.calls.modules.State;
import com.suse.salt.netapi.calls.modules.State.ApplyResult;
import com.suse.salt.netapi.calls.modules.TransactionalUpdate;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.errors.GenericError;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.results.Ret;
import com.suse.salt.netapi.results.StateApplyResult;
import com.suse.salt.netapi.utils.Xor;
import com.suse.utils.Json;
import com.suse.utils.Opt;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.cobbler.CobblerConnection;
import org.cobbler.Distro;
import org.cobbler.Profile;
import org.cobbler.SystemRecord;
import org.jose4j.lang.JoseException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipal;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;


/**
 * Takes {@link Action} objects to be executed via salt.
 */
public class SaltServerActionService {

    /* Logger for this class */
    private static final Logger LOG = LogManager.getLogger(SaltServerActionService.class);
    public static final String PACKAGES_PKGINSTALL = "packages.pkginstall";
    public static final String PACKAGES_PKGUPDATE = "packages.pkgupdate";
    private static final String PACKAGES_PKGDOWNLOAD = "packages.pkgdownload";
    public static final String PACKAGES_PATCHINSTALL = "packages.patchinstall";
    private static final String PACKAGES_PATCHDOWNLOAD = "packages.patchdownload";
    private static final String PACKAGES_PKGREMOVE = "packages.pkgremove";
    private static final String PACKAGES_PKGLOCK = "packages.pkglock";
    private static final String CONFIG_DEPLOY_FILES = "configuration.deploy_files";
    private static final String CONFIG_DIFF_FILES = "configuration.diff_files";
    private static final String PARAM_PKGS = "param_pkgs";
    private static final String PARAM_PATCHES = "param_patches";
    private static final String PARAM_FILES = "param_files";
    private static final String REMOTE_COMMANDS = "remotecommands";
    private static final String SYSTEM_REBOOT = "system.reboot";
    private static final String KICKSTART_INITIATE = "bootloader.autoinstall";
    private static final String ANSIBLE_RUNPLAYBOOK = "ansible.runplaybook";

    /** SLS pillar parameter name for the list of update stack patch names. */
    public static final String PARAM_UPDATE_STACK_PATCHES = "param_update_stack_patches";

    /** SLS pillar parameter name for the list of regular patch names. */
    public static final String PARAM_REGULAR_PATCHES = "param_regular_patches";
    public static final String ALLOW_VENDOR_CHANGE = "allow_vendor_change";
    private static final String INVENTORY_PATH = "/etc/ansible/hosts";


    private boolean commitTransaction = true;

    private SaltActionChainGeneratorService saltActionChainGeneratorService =
            SaltActionChainGeneratorService.INSTANCE;

    private SaltApi saltApi;
    private final SaltSSHService saltSSHService = GlobalInstanceHolder.SALT_API.getSaltSSHService();
    private SaltUtils saltUtils;
    private final SaltKeyUtils saltKeyUtils;
    private boolean skipCommandScriptPerms;
    private TaskomaticApi taskomaticApi = new TaskomaticApi();

    /**
     * @param saltApiIn instance for getting information from a system.
     * @param saltUtilsIn salt utils instance to use
     * @param saltKeyUtilsIn salt key utils instance to use
     */
    public SaltServerActionService(SaltApi saltApiIn, SaltUtils saltUtilsIn, SaltKeyUtils saltKeyUtilsIn) {
        this.saltApi = saltApiIn;
        this.saltUtils = saltUtilsIn;
        this.saltKeyUtils = saltKeyUtilsIn;
    }

    /**
     * For a given action return the salt call(s) that need to be executed for the minions involved.
     *
     * @param actionIn the action to be executed
     * @return map of Salt local call to list of targeted minion summaries
     */
    public Map<LocalCall<?>, List<MinionSummary>> callsForAction(Action actionIn) {
        List<MinionSummary> minionSummaries = MinionServerFactory.findAllMinionSummaries(actionIn.getId());
        return callsForAction(actionIn, minionSummaries);
    }

    /**
     * For a given action return the salt call(s) that need to be executed for the targeted minions.
     *
     * @param actionIn the action to be executed
     * @param minions the list of minion summaries to target
     * @return map of Salt local call to list of targeted minion summaries
     */
    public Map<LocalCall<?>, List<MinionSummary>> callsForAction(Action actionIn, List<MinionSummary> minions) {
        if (minions.isEmpty()) {
            return Collections.emptyMap();
        }

        ActionType actionType = actionIn.getActionType();
        actionIn = unproxy(actionIn);
        if (ActionFactory.TYPE_ERRATA.equals(actionType)) {
            ErrataAction errataAction = (ErrataAction) actionIn;
            Set<Long> errataIds = errataAction.getErrata().stream()
                    .map(Errata::getId).collect(Collectors.toSet());
            return errataAction(minions, errataIds, errataAction.getDetails().getAllowVendorChange());
        }
        else if (ActionFactory.TYPE_PACKAGES_LOCK.equals(actionType)) {
            return packagesLockAction(minions, (PackageLockAction) actionIn);
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
            return applyStatesAction(minions, actionDetails.getMods(),
                                     actionDetails.getPillarsMap(), actionDetails.isTest());
        }
        else if (ActionFactory.TYPE_IMAGE_INSPECT.equals(actionType)) {
            ImageInspectAction iia = (ImageInspectAction) actionIn;
            ImageInspectActionDetails details = iia.getDetails();
            if (details == null) {
                return Collections.emptyMap();
            }
            return ImageStoreFactory.lookupById(details.getImageStoreId())
                    .map(store -> imageInspectAction(minions, details, store))
                    .orElseGet(Collections::emptyMap);
        }
        else if (ActionFactory.TYPE_IMAGE_BUILD.equals(actionType)) {
            ImageBuildAction imageBuildAction = (ImageBuildAction) actionIn;
           ImageBuildActionDetails details = imageBuildAction.getDetails();
            if (details == null) {
                return Collections.emptyMap();
            }
            return ImageProfileFactory.lookupById(details.getImageProfileId()).map(
                    ip -> imageBuildAction(minions, Optional.ofNullable(details.getVersion()), ip,
                            imageBuildAction.getId())
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
        else if (ActionFactory.TYPE_VIRTUALIZATION_SHUTDOWN.equals(actionType)) {
            VirtualizationShutdownGuestAction virtAction =
                    (VirtualizationShutdownGuestAction)actionIn;
            return virtStateChangeAction(minions, virtAction.getUuid(), "stopped", virtAction);
        }
        else if (ActionFactory.TYPE_VIRTUALIZATION_START.equals(actionType)) {
            VirtualizationStartGuestAction virtAction = (VirtualizationStartGuestAction)actionIn;
            return virtStateChangeAction(minions, virtAction.getUuid(), "running", virtAction);
        }
        else if (ActionFactory.TYPE_VIRTUALIZATION_SUSPEND.equals(actionType)) {
            VirtualizationSuspendGuestAction virtAction =
                    (VirtualizationSuspendGuestAction)actionIn;
            return virtStateChangeAction(minions, virtAction.getUuid(), "suspended", virtAction);
        }
        else if (ActionFactory.TYPE_VIRTUALIZATION_RESUME.equals(actionType)) {
            VirtualizationResumeGuestAction virtAction =
                    (VirtualizationResumeGuestAction)actionIn;
            return virtStateChangeAction(minions, virtAction.getUuid(), "resumed", virtAction);
        }
        else if (ActionFactory.TYPE_VIRTUALIZATION_REBOOT.equals(actionType)) {
            VirtualizationRebootGuestAction virtAction =
                    (VirtualizationRebootGuestAction)actionIn;
            return virtStateChangeAction(minions, virtAction.getUuid(), "rebooted", virtAction);
        }
        else if (ActionFactory.TYPE_VIRTUALIZATION_DELETE.equals(actionType)) {
            VirtualizationDeleteGuestAction virtAction =
                    (VirtualizationDeleteGuestAction)actionIn;
            return virtStateChangeAction(minions, virtAction.getUuid(), "deleted", virtAction);
        }
        else if (ActionFactory.TYPE_VIRTUALIZATION_SET_VCPUS.equals(actionType)) {
            VirtualizationSetVcpusGuestAction virtAction =
                    (VirtualizationSetVcpusGuestAction)actionIn;
            return virtSetterAction(minions, virtAction.getUuid(),
                                    "vcpus", virtAction.getVcpu());
        }
        else if (ActionFactory.TYPE_VIRTUALIZATION_SET_MEMORY.equals(actionType)) {
            VirtualizationSetMemoryGuestAction virtAction =
                    (VirtualizationSetMemoryGuestAction)actionIn;
            return virtSetterAction(minions, virtAction.getUuid(),
                                    "mem", virtAction.getMemory());
        }
        else if (ActionFactory.TYPE_VIRTUALIZATION_CREATE.equals(actionType)) {
            VirtualizationCreateGuestAction createAction =
                    (VirtualizationCreateGuestAction)actionIn;
            return virtCreateAction(minions, createAction);
        }
        else if (ActionFactory.TYPE_VIRTUALIZATION_GUEST_MIGRATE.equals(actionType)) {
            VirtualizationMigrateGuestAction migrationAction =
                    (VirtualizationMigrateGuestAction)actionIn;
            return virtGuestMigrateAction(minions, migrationAction);
        }
        else if (ActionFactory.TYPE_KICKSTART_INITIATE.equals(actionType)) {
            KickstartInitiateAction autoInitAction = (KickstartInitiateAction)actionIn;
            return autoinstallInitAction(minions, autoInitAction);
        }
        else if (ActionFactory.TYPE_VIRTUALIZATION_POOL_REFRESH.equals(actionType)) {
            VirtualizationPoolRefreshAction refreshAction =
                    (VirtualizationPoolRefreshAction)actionIn;
            return virtPoolRefreshAction(minions, refreshAction.getPoolName());
        }
        else if (ActionFactory.TYPE_VIRTUALIZATION_POOL_START.equals(actionType)) {
            VirtualizationPoolStartAction startAction =
                    (VirtualizationPoolStartAction)actionIn;
            return virtPoolStateChangeAction(minions, startAction.getPoolName(), "start");
        }
        else if (ActionFactory.TYPE_VIRTUALIZATION_POOL_STOP.equals(actionType)) {
            VirtualizationPoolStopAction stopAction =
                    (VirtualizationPoolStopAction)actionIn;
            return virtPoolStateChangeAction(minions, stopAction.getPoolName(), "stop");
        }
        else if (ActionFactory.TYPE_VIRTUALIZATION_POOL_DELETE.equals(actionType)) {
            VirtualizationPoolDeleteAction deleteAction =
                    (VirtualizationPoolDeleteAction)actionIn;
            return virtPoolDeleteAction(minions, deleteAction.getPoolName(),
                    deleteAction.isPurge());
        }
        else if (ActionFactory.TYPE_VIRTUALIZATION_POOL_CREATE.equals(actionType)) {
            VirtualizationPoolCreateAction createAction =
                    (VirtualizationPoolCreateAction)actionIn;
            return virtPoolCreateAction(minions, createAction);
        }
        else if (ActionFactory.TYPE_VIRTUALIZATION_VOLUME_DELETE.equals(actionType)) {
            BaseVirtualizationVolumeAction deleteAction =
                    (BaseVirtualizationVolumeAction)actionIn;
            return virtVolumeDeleteAction(minions,
                    deleteAction.getPoolName(), deleteAction.getVolumeName());
        }
        else if (ActionFactory.TYPE_VIRTUALIZATION_NETWORK_STATE_CHANGE.equals(actionType)) {
            VirtualizationNetworkStateChangeAction networkAction = (VirtualizationNetworkStateChangeAction) actionIn;
            return virtNetworkStateChangeAction(minions, networkAction.getNetworkName(), networkAction.getState());
        }
        else if (ActionFactory.TYPE_VIRTUALIZATION_NETWORK_CREATE.equals(actionType)) {
            VirtualizationNetworkCreateAction networkAction = (VirtualizationNetworkCreateAction)actionIn;
            return virtNetworkCreateAction(minions, networkAction.getNetworkName(), networkAction.getDefinition());
        }
        else if (ActionFactory.TYPE_PLAYBOOK.equals(actionType)) {
            return singletonMap(executePlaybookActionCall((PlaybookAction) actionIn), minions);
        }
        else {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Action type {} is not supported with Salt", actionType != null ? actionType.getName() : "");
            }
            return Collections.emptyMap();
        }
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

        List<MinionSummary> allMinions = MinionServerFactory.findQueuedMinionSummaries(actionIn.getId());
        if (CollectionUtils.isEmpty(allMinions)) {
            LOG.warn("Unable to find any minion that have the action id={} in status QUEUED", actionIn.getId());
            return;
        }

        // split minions into regular and salt-ssh
        Map<Boolean, List<MinionSummary>> partitionBySSHPush = allMinions.stream()
                .collect(Collectors.partitioningBy(MinionSummary::isSshPush));

        // Separate SSH push minions from regular minions to apply different states
        List<MinionSummary> sshMinionSummaries = partitionBySSHPush.get(true);
        List<MinionSummary> regularMinionSummaries = partitionBySSHPush.get(false);

        if (!regularMinionSummaries.isEmpty()) {
            executeForRegularMinions(actionIn, forcePackageListRefresh, isStagingJob, stagingJobMinionServerId,
                    regularMinionSummaries);
        }

        List<MinionServer> sshPushMinions = MinionServerFactory.findMinionsByServerIds(
                sshMinionSummaries.stream().map(MinionSummary::getServerId).collect(Collectors.toList()));

        if (!sshPushMinions.isEmpty()) {
            for (MinionServer sshMinion : sshPushMinions) {
                try {
                    taskomaticApi.scheduleSSHActionExecution(actionIn, sshMinion, forcePackageListRefresh);
                }
                catch (TaskomaticApiException e) {
                    LOG.error("Couldn't schedule SSH action id={} minion={}",
                            actionIn.getId(), sshMinion.getMinionId(), e);
                }
            }
        }
    }

    private void executeForRegularMinions(Action actionIn, boolean forcePackageListRefresh,
            boolean isStagingJob, Optional<Long> stagingJobMinionServerId, List<MinionSummary> minionSummaries) {
        for (Map.Entry<LocalCall<?>, List<MinionSummary>> entry : callsForAction(actionIn, minionSummaries)
                .entrySet()) {
            LocalCall<?> call = entry.getKey();
            final List<MinionSummary> targetMinions;
            Map<Boolean, List<MinionSummary>> results;

            if (isStagingJob) {
                targetMinions = new ArrayList<>();
                stagingJobMinionServerId.flatMap(MinionServerFactory::lookupById)
                        .ifPresent(server -> targetMinions.add(new MinionSummary(server)));
                call = prepareStagingTargets(actionIn, targetMinions);
            }
            else {
                targetMinions = entry.getValue();
            }

            LOG.debug("Executing action {} for {} minions.", actionIn.getId(), targetMinions.size());
            results = execute(actionIn, call, targetMinions, forcePackageListRefresh, isStagingJob);
            LOG.debug(
                "Finished action {}. Picked up for {} minions and failed for {} minions.",
                actionIn.getId(),
                results.get(true).size(),
                results.get(false).size()
            );

            if (!isStagingJob) {
                List<Long> succeededServerIds = results.get(true).stream()
                        .map(MinionSummary::getServerId).collect(toList());
                if (!succeededServerIds.isEmpty()) {
                    ActionFactory.updateServerActionsPickedUp(actionIn, succeededServerIds);
                }
                List<Long> failedServerIds  = results.get(false).stream()
                        .map(MinionSummary::getServerId).collect(toList());
                if (!failedServerIds.isEmpty()) {
                    ActionFactory.updateServerActions(actionIn, failedServerIds, ActionFactory.STATUS_FAILED);
                }
            }
        }
    }

    /**
     * Call Salt to start the execution of the given action chain.
     *
     * @param actionChain the action chain to execute
     * @param targetMinions a list containing target minions
     */
    private void startActionChainExecution(ActionChain actionChain, Set<MinionSummary> targetMinions) {
        // prepare the start action chain call
        Map<Boolean, ? extends Collection<MinionSummary>> results =
                callAsyncActionChainStart(actionChain, targetMinions);

        results.get(false).forEach(minionSummary -> {
            LOG.warn("Failed to schedule action chain for minion: {}", minionSummary.getMinionId());
            Optional<Long> firstActionId = actionChain.getEntries().stream()
                    .sorted(Comparator.comparingInt(ActionChainEntry::getSortOrder))
                    .map(ActionChainEntry::getAction)
                    .map(Action::getId)
                    .findFirst();
            failActionChain(minionSummary.getMinionId(), Optional.of(actionChain.getId()), firstActionId,
                    Optional.of("Got empty result."));
        });
    }

    private void startSSHActionChain(ActionChain actionChain, Set<MinionSummary> sshMinions,
                                     Optional<String> extraFilerefs) {
        // use a state to start the action chain in order to trick salt-ssh into including the
        // mgractionchains custom module in the thin-dir tarball
        LocalCall<Map<String, ApplyResult>> call =
                State.apply(singletonList("actionchains.startssh"),
                        Optional.of(singletonMap("actionchain_id", actionChain.getId())));

        Optional<Long> firstActionId = actionChain.getEntries().stream()
                .sorted(Comparator.comparingInt(ActionChainEntry::getSortOrder))
                .map(ActionChainEntry::getAction)
                .map(Action::getId)
                .findFirst();

        // start the action chain synchronously
        try {
            // first check if there's an action chain with a reboot already executing
            Map<String, Result<Map<String, String>>> pendingResumeConf = saltApi.getPendingResume(
                    sshMinions.stream().map(MinionSummary::getMinionId)
                            .collect(Collectors.toList())
            );
            List<MinionSummary> targetSSHMinions = sshMinions.stream()
                    .filter(sshMinion -> {
                        Optional<Map<String, String>> confValues = pendingResumeConf.get(sshMinion.getMinionId())
                                .fold(err -> {
                                            LOG.error("mgractionchains.get_pending_resume failed: {}", err.fold(
                                                    Object::toString,
                                                    Object::toString,
                                                    Object::toString,
                                                    Object::toString,
                                                    Object::toString
                                            ));
                                        return Optional.empty();
                                    },
                                        Optional::of);
                        if (confValues.orElse(Collections.emptyMap()).isEmpty()) {
                            // all good, no action chain currently executing on the minion
                            return true;
                        }
                        // fail the action chain because concurrent execution is not possible
                        LOG.warn("Minion {} has an action chain execution in progress", sshMinion.getMinionId());
                        failActionChain(sshMinion.getMinionId(), Optional.of(actionChain.getId()), firstActionId,
                                Optional.of("An action chain execution is already in progress. " +
                                        "Concurrent action chain execution is not allowed. " +
                                        "If the execution became stale remove directory " +
                                        "/var/tmp/.root_XXXX_salt/minion.d manually."));
                        return false;
                    }).collect(Collectors.toList());

            if (targetSSHMinions.isEmpty()) {
                // do nothing, no targets
                return;
            }

            Map<String, Result<Map<String, ApplyResult>>> res = saltSSHService.callSyncSSH(call,
                    new MinionList(targetSSHMinions.stream().map(MinionSummary::getMinionId)
                            .collect(Collectors.toList())),
                    extraFilerefs);

            res.forEach((minionId, chunkResult) -> {
                if (chunkResult.result().isPresent()) {
                    handleActionChainSSHResult(firstActionId, minionId, chunkResult.result().get());
                }
                else {
                    String errMsg = chunkResult.error().map(saltErr -> saltErr.fold(
                            e ->  {
                                LOG.error(e);
                                return "Function " + e.getFunctionName() + " not available";
                            },
                            e ->  {
                                LOG.error(e);
                                return "Module " + e.getModuleName() + " not supported";
                            },
                            e ->  {
                                LOG.error(e);
                                return "Error parsing JSON: " + e.getJson();
                            },
                            e ->  {
                                LOG.error(e);
                                return "Salt error: " + e.getMessage();
                            },
                            e -> {
                                LOG.error(e);
                                return "Salt SSH error: " + e.getRetcode() + " " + e.getMessage();
                            }
                    )).orElse("Unknonw error");
                    // no result, fail the entire chain
                    failActionChain(minionId, Optional.of(actionChain.getId()), firstActionId,
                            Optional.of(errMsg));
                }
            });

        }
        catch (SaltException e) {
            LOG.error("Error handling action chain execution: ", e);
            // fail the entire chain
            sshMinions.forEach(minion -> failActionChain(minion.getMinionId(), Optional.of(actionChain.getId()),
                    firstActionId, Optional.of("Error handling action chain execution: " + e.getMessage())));
        }
    }

    /**
     * Handle result of applying an action chain.
     * @param firstChunkActionId id of the first action in the chunk
     * @param minionId minion id
     * @param chunkResult result of applying the chunk
     * @return true if the result could be handled
     */
    public boolean handleActionChainSSHResult(Optional<Long> firstChunkActionId, String minionId,
                                              Map<String, ApplyResult> chunkResult) {
        try {
            // mgractionchains.start is executed via state.apply, get the actual output of the module
            // fist look for start result
            StateApplyResult<JsonElement> stateApplyResult =
                    chunkResult.get("mgrcompat_|-startssh_|-mgractionchains.start_|-module_run");

            if (stateApplyResult == null) {
                // if no start result, look for resume
                stateApplyResult =
                        chunkResult.get("mgrcompat_|-resumessh_|-mgractionchains.resume_|-module_run");
            }

            if (stateApplyResult == null) {
                LOG.error("No action chain result for minion {}", minionId);
                failActionChain(minionId, firstChunkActionId, Optional.of("No action chain result"));
            }
            else if (!stateApplyResult.isResult() && (stateApplyResult.getChanges() == null ||
                    (stateApplyResult.getChanges().isJsonObject()) &&
                            ((JsonObject)stateApplyResult.getChanges()).size() == 0)) {
                LOG.error("Error handling action chain execution: {}", stateApplyResult.getComment());
                failActionChain(minionId, firstChunkActionId, Optional.of(stateApplyResult.getComment()));
            }
            else if (stateApplyResult.getChanges() != null) {
                // handle the result
                Optional<Map<String, StateApplyResult<Ret<JsonElement>>>> optActionChainResult = getActionChainResult(
                        stateApplyResult.getChanges(), minionId, firstChunkActionId);
                if (optActionChainResult.isEmpty()) {
                    return false;
                }
                Map<String, StateApplyResult<Ret<JsonElement>>> actionChainResult = optActionChainResult.get();
                handleActionChainResult(minionId, "",
                        actionChainResult,
                        // skip reboot, needs special handling
                        stateResult ->
                                stateResult.getName().map(x -> x.fold(Arrays::asList, List::of)
                                        .contains(SYSTEM_REBOOT)).orElse(false));

                boolean refreshPkg = false;
                Optional<User> scheduler = Optional.empty();
                for (Map.Entry<String, StateApplyResult<Ret<JsonElement>>> entry : actionChainResult.entrySet()) {
                    String stateIdKey = entry.getKey();
                    StateApplyResult<Ret<JsonElement>> stateResult = entry.getValue();

                    Optional<SaltActionChainGeneratorService.ActionChainStateId> actionChainStateId =
                            SaltActionChainGeneratorService.parseActionChainStateId(stateIdKey);
                    if (actionChainStateId.isPresent()) {
                        SaltActionChainGeneratorService.ActionChainStateId stateId = actionChainStateId.get();
                        // only reboot needs special handling,
                        // for salt pkg update there's no need to split the sls in case of salt-ssh minions

                        Action action = ActionFactory.lookupById(stateId.getActionId());
                        if (stateResult.getName().map(x -> x.fold(Arrays::asList, List::of)
                                .contains(SYSTEM_REBOOT)).orElse(false) && stateResult.isResult() &&
                                action.getActionType().equals(ActionFactory.TYPE_REBOOT)) {

                            Optional<ServerAction> rebootServerAction =
                                    action.getServerActions().stream()
                                            .filter(sa -> sa.getServer().asMinionServer()
                                                    .map(m -> m.getMinionId().equals(minionId)).orElse(false))
                                            .findFirst();
                            rebootServerAction.ifPresentOrElse(
                                    ract -> {
                                        if (ract.getStatus().equals(ActionFactory.STATUS_QUEUED)) {
                                            setActionAsPickedUp(ract);
                                        }
                                    },
                                    () -> LOG.error("Action of type {} found in action chain result but not " +
                                            "in actions for minion {}", SYSTEM_REBOOT, minionId));
                        }

                        if (stateResult.isResult() &&
                                saltUtils.shouldRefreshPackageList(stateResult.getName(),
                                        Optional.of(stateResult.getChanges().getRet()))) {
                            scheduler = Optional.ofNullable(action.getSchedulerUser());
                            refreshPkg = true;
                        }
                    }
                }
                Optional<MinionServer> minionServer = MinionServerFactory.findByMinionId(minionId);
                if (refreshPkg) {
                    Optional<User> finalScheduler = scheduler;
                    minionServer.ifPresent(minion -> {
                        LOG.info("Scheduling a package profile update for minion {}", minionId);
                        try {
                            ActionManager.schedulePackageRefresh(finalScheduler, minion);
                        }
                        catch (TaskomaticApiException e) {
                            LOG.error("Could not schedule package refresh for minion: {}", minion.getMinionId(), e);
                        }
                    });
                }
                // update minion last checkin
                minionServer.ifPresent(Server::updateServerInfo);
            }
            else {
                LOG.error("'state.apply mgractionchains.startssh' was successful " +
                        "but not state apply changes are present");
                failActionChain(minionId, firstChunkActionId, Optional.of("Got null result."));
                return false;
            }
        }
        catch (Exception e) {
            LOG.error("Error handling action chain result for SSH minion {}", minionId, e);
            failActionChain(minionId, firstChunkActionId,
                    Optional.of("Error handling action chain result:" + e.getMessage()));
            return false;
        }
        return true;
    }

    private Optional<Map<String, StateApplyResult<Ret<JsonElement>>>> getActionChainResult(
            JsonElement stateChanges, String minionId, Optional<Long> firstChunkActionId) {
        Map<String, StateApplyResult<Ret<JsonElement>>> actionChainResult;
        try {
            Ret<Map<String, StateApplyResult<Ret<JsonElement>>>> actionChainRet =
                    Json.GSON.fromJson(stateChanges,
                            new TypeToken<Ret<Map<String, StateApplyResult<Ret<JsonElement>>>>>() {
                            }.getType());
            actionChainResult = actionChainRet.getRet();
        }
        catch (JsonSyntaxException e) {
            LOG.error("Unexpected response: {}", stateChanges, e);
            String msg = stateChanges.toString();
            if ((stateChanges.isJsonObject()) &&
                    ((JsonObject)stateChanges).get("ret") != null) {
                msg = ((JsonObject)stateChanges).get("ret").toString();
            }
            failActionChain(minionId, firstChunkActionId, Optional.of("Unexpected response: " + msg));
            return Optional.empty();
        }
        return Optional.of(actionChainResult);
    }

    /**
     * Set the action and dependent actions to failed
     * @param minionId the minion id
     * @param failedActionId the failed action id
     * @param message the message to set to the failed action
     */
    public void failActionChain(String minionId, Optional<Long> failedActionId, Optional<String> message) {
        failActionChain(minionId, Optional.empty(), failedActionId, message);
    }

    private void failActionChain(String minionId, Optional<Long> actionChainId, Optional<Long> failedActionId,
                                        Optional<String> message) {
        failedActionId.ifPresent(last ->
                failDependentServerActions(last, minionId, message));
        MinionServerFactory.findByMinionId(minionId).ifPresent(minion -> SaltActionChainGeneratorService.INSTANCE
                .removeActionChainSLSFilesForMinion(minion.getMachineId(), actionChainId));
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
        Map<MinionSummary, List<Pair<ServerAction, List<LocalCall<?>>>>> minionCalls = new HashMap<>();

        actionChain.getEntries().stream()
                .sorted(Comparator.comparingInt(ActionChainEntry::getSortOrder))
                .map(ActionChainEntry::getAction)
                .forEach(actionIn -> {
                    List<MinionSummary> minions = MinionServerFactory.findAllMinionSummaries(actionIn.getId());

                    if (minions.isEmpty()) {
                        // When an Action Chain contains an Action which does not target
                        // any minion we don't generate any Salt call.
                        LOG.warn("No server actions for action id={}", actionIn.getId());
                        return;
                    }

                    // get Salt calls for this action
                    Map<LocalCall<?>, List<MinionSummary>> actionCalls = callsForAction(actionIn, minions);

                    // TODO how to handle staging jobs?

                    // Salt calls for each minion
                    Map<MinionSummary, List<LocalCall<?>>> callsPerMinion =
                            actionCalls.values().stream().flatMap(Collection::stream)
                                .collect(Collectors
                                        .toMap(Function.identity(),
                                                m -> actionCalls.entrySet()
                                                        .stream()
                                                        .filter(e -> e.getValue().contains(m))
                                                        .map(Map.Entry::getKey)
                                                        .collect(Collectors.toList())
                                ));

                    // append the Salt calls for this action to the list of calls of each minion
                    callsPerMinion.forEach((minion, calls) -> {
                        List<Pair<ServerAction, List<LocalCall<?>>>> currentCalls = minionCalls
                                .getOrDefault(minion, new ArrayList<>());
                        Optional<ServerAction> serverAction = actionIn.getServerActions().stream()
                                .filter(sa -> sa.getServer().getId().equals(minion.getServerId()))
                                .findFirst();
                        serverAction.ifPresent(sa -> {
                            Pair<ServerAction, List<LocalCall<?>>> serverActionCalls =
                                    new ImmutablePair<>(sa, calls);
                            currentCalls.add(serverActionCalls);
                        });
                        minionCalls.put(minion, currentCalls);
                    });

                });

        // split minions into regular and salt-ssh
        Map<Boolean, Set<MinionSummary>> minionPartitions =
                minionCalls.keySet().stream()
                        .collect(Collectors.partitioningBy(MinionSummary::isSshPush, Collectors.toSet()));

        Set<MinionSummary> sshMinionIds = minionPartitions.get(true);
        Set<MinionSummary> regularMinionIds = minionPartitions.get(false);

        // convert local calls to salt state objects
        Map<MinionSummary, List<SaltState>> statesPerMinion = new HashMap<>();
        minionCalls.forEach((minion, serverActionCalls) -> {
            List<SaltState> states = serverActionCalls.stream()
                    .flatMap(saCalls -> {
                        ServerAction sa = saCalls.getKey();
                        List<LocalCall<?>> calls = saCalls.getValue();
                        return convertToState(actionChain.getId(), sa, calls, minion).stream();
                    }).collect(Collectors.toList());

            statesPerMinion.put(minion, states);
        });

        // Compute the additional sls files to be included in the state tarball for ssh-push minions.
        // This is needed because we're using module.run + state.apply to apply the corresponding
        // action states and this breaks the introspection that Salt does in order to figure out
        // what files it needs to included in the state tarball.
        Optional<String> extraFilerefs = Optional.empty();
        if (!sshMinionIds.isEmpty()) {
            Map<MinionSummary, Integer> chunksPerMinion =
                    saltActionChainGeneratorService.getChunksPerMinion(statesPerMinion);

            // If there are highstate apply actions the corresponding top files must be generated
            // because we're applying highstates using state.top instead of state.apply.
            // This is due to module.run + state.apply highstate not working properly even if there's
            // a top.sls included in the state tarball.
            Map<MinionSummary, List<Long>> highstateActionPerMinion =
                    saltSSHService.findApplyHighstateActionsPerMinion(statesPerMinion);

            List<String> fileRefsList = new LinkedList<>();
            highstateActionPerMinion.forEach((minion, highstateActionIds) -> {
                for (Long highstateActionId: highstateActionIds) {
                    // generate a top files for each highstate apply action
                    Pair<String, List<String>> highstateTop = saltSSHService
                            .generateTopFile(actionChainId, highstateActionId);
                    fileRefsList.add(highstateTop.getKey());
                }
            });

            // collect additional files (e.g. salt://scripts/script_xxx.sh) referenced from the action
            // chain sls file
            String extraFilerefsStr = saltSSHService
                    .findStatesExtraFilerefs(actionChain.getId(), chunksPerMinion, statesPerMinion);
            fileRefsList.add(extraFilerefsStr);

            // join extra files and tops
            extraFilerefs = Optional.of(String.join(",", fileRefsList));

        }

        // render the action chain sls files
        for (Map.Entry<MinionSummary, List<SaltState>> entry: statesPerMinion.entrySet()) {
            saltActionChainGeneratorService
                    .createActionChainSLSFiles(actionChain, entry.getKey(), entry.getValue(),
                            entry.getKey().isSshPush() ? extraFilerefs : Optional.empty());
        }

        // start the execution
        if (!regularMinionIds.isEmpty()) {
            startActionChainExecution(actionChain, regularMinionIds);
        }

        if (!sshMinionIds.isEmpty()) {
            startSSHActionChain(actionChain, sshMinionIds, extraFilerefs);
        }
    }

    private List<SaltState> convertToState(long actionChainId, ServerAction serverAction,
                                           List<LocalCall<?>> calls, MinionSummary minion) {
        String stateId = SaltActionChainGeneratorService.createStateId(actionChainId,
                serverAction.getParentAction().getId());

        return calls.stream().map(call -> {
            Map<String, Object> payload = call.getPayload();
            String fun = (String)payload.get("fun");
            Map<String, ?> kwargs = (Map<String, ?>)payload.get("kwarg");
            switch(fun) {
                case "state.apply":
                    List<String> mods = (List<String>)kwargs.get("mods");
                    if (CollectionUtils.isEmpty(mods)) {
                        if (minion.isSshPush()) {
                            // Apply highstate using a custom top.
                            // The custom top is needed because salt-ssh invokes
                            // "salt-call --local" and this needs a top file in order to apply the highstate
                            // and salt-ssh doesn't pack one automatically in the state tarball
                            return new SaltModuleRun(stateId,
                                    "state.top",
                                    serverAction.getParentAction().getId(),
                                    singletonMap("topfn",
                                            saltActionChainGeneratorService
                                                    .getActionChainTopPath(actionChainId,
                                                            serverAction.getParentAction().getId())),
                                    emptyMap());
                        }
                        else {
                            return new SaltModuleRun(stateId,
                                    "state.apply",
                                    serverAction.getParentAction().getId(),
                                    emptyMap(),
                                    createStateApplyKwargs(kwargs));
                        }

                    }
                    return new SaltModuleRun(stateId,
                            "state.apply",
                            serverAction.getParentAction().getId(),
                            !mods.isEmpty() ?
                                    singletonMap("mods", mods) : emptyMap(),
                            createStateApplyKwargs(kwargs));
                case SYSTEM_REBOOT:
                    Integer time = (Integer)kwargs.get("at_time");
                    return new SaltSystemReboot(stateId,
                            serverAction.getParentAction().getId(), time);
                case "transactional_update.reboot":
                    // this function will be excluded to the sls file by createActionChainSLSFiles
                    return new SaltSystemReboot(stateId,
                            serverAction.getParentAction().getId(), 0);
                default:
                    throw new RhnRuntimeException("Salt module call " + fun + " can't be converted to a state.");
            }
        }).collect(Collectors.toList());
    }

    private Map<String, Object> createStateApplyKwargs(Map<String, ?> kwargs) {
        Map<String, Object> applyKwargs = new HashMap<>();
        if (kwargs.get("pillar") != null) {
            applyKwargs.put("pillar", kwargs.get("pillar"));
        }
        applyKwargs.put("queue", true);
        return applyKwargs;
    }


    /**
     * This function will return a map with list of minions grouped by the
     * salt netapi local call that executes what needs to be executed on
     * those minions for the given errata ids and minions.
     *
     * @param minionSummaries list of minion summaries to target
     * @param errataIds list of errata ids
     * @param allowVendorChange true if vendor change allowed
     * @return minion summaries grouped by local call
     */
    public Map<LocalCall<?>, List<MinionSummary>> errataAction(List<MinionSummary> minionSummaries,
            Set<Long> errataIds, boolean allowVendorChange) {
        Map<Boolean, List<MinionSummary>> byUbuntu = minionSummaries.stream()
                .collect(partitioningBy(m -> m.getOs().equals("Ubuntu")));

        Map<LocalCall<Map<String, ApplyResult>>, List<MinionSummary>> ubuntuErrataInstallCalls =
                errataToPackageInstallCalls(byUbuntu.get(true), errataIds);

        Set<Long> minionServerIds = byUbuntu.get(false).stream()
                .map(MinionSummary::getServerId)
                .collect(Collectors.toSet());

        Map<Long, Map<Long, Set<ErrataInfo>>> errataInfos = ServerFactory
                .listErrataNamesForServers(minionServerIds, errataIds);

        // Group targeted minions by errata names
        Map<Set<ErrataInfo>, List<MinionSummary>> collect = byUbuntu.get(false).stream()
                .collect(Collectors.groupingBy(minionId -> errataInfos.get(minionId.getServerId())
                        .values().stream()
                        .flatMap(Set::stream)
                        .collect(Collectors.toSet())
        ));

        // Convert errata names to LocalCall objects of type State.apply
        Map<LocalCall<?>, List<MinionSummary>> patchableCalls = collect.entrySet().stream()
            .collect(Collectors.toMap(entry -> {
                Map<String, Object> params = new HashMap<>();
                params.put(PARAM_REGULAR_PATCHES,
                    entry.getKey().stream()
                        .filter(e -> !e.isUpdateStack())
                        .map(ErrataInfo::getName)
                        .sorted()
                        .collect(toList())
                );
                params.put(ALLOW_VENDOR_CHANGE, allowVendorChange);
                params.put(PARAM_UPDATE_STACK_PATCHES,
                    entry.getKey().stream()
                        .filter(ErrataInfo::isUpdateStack)
                        .map(ErrataInfo::getName)
                        .sorted()
                        .collect(toList())
                );
                if (entry.getKey().stream().anyMatch(ErrataInfo::includeSalt)) {
                    params.put("include_salt_upgrade", true);
                }
                return State.apply(
                        List.of(PACKAGES_PATCHINSTALL),
                        Optional.of(params)
                );
            },
            Map.Entry::getValue));
        patchableCalls.putAll(ubuntuErrataInstallCalls);
        return patchableCalls;
    }

    private Map<LocalCall<?>, List<MinionSummary>> packagesLockAction(
            List<MinionSummary> minionSummaries, PackageLockAction action) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();

        for (MinionSummary m : minionSummaries) {
            DataResult<PackageListItem> setLockPkg = PackageManager.systemSetLockedPackages(
                    m.getServerId(), action.getId(), null);
            List<List<String>> pkgs = setLockPkg.stream().map(d -> Arrays.asList(d.getName(), d.getArch(),
                    new PackageEvr(d.getEpoch(), d.getVersion(), d.getRelease(), d.getPackageType())
                    .toUniversalEvrString())).collect(Collectors.toList());
            LocalCall<Map<String, ApplyResult>> localCall =
                    State.apply(List.of(PACKAGES_PKGLOCK), Optional.of(singletonMap(PARAM_PKGS, pkgs)));
            List<MinionSummary> mSums = ret.getOrDefault(localCall, new ArrayList<>());
            mSums.add(m);
            ret.put(localCall, mSums);
        }
        return ret;
    }

    private Map<LocalCall<Map<String, ApplyResult>>, List<MinionSummary>> errataToPackageInstallCalls(
            List<MinionSummary> minions,
            Set<Long> errataIds) {
        Set<Long> minionIds = minions.stream()
                .map(MinionSummary::getServerId).collect(Collectors.toSet());
        Map<Long, Map<String, Tuple2<String, String>>> longMapMap =
                ServerFactory.listNewestPkgsForServerErrata(minionIds, errataIds);

        // group minions by packages that need to be updated
        Map<Map<String, Tuple2<String, String>>, List<MinionSummary>> nameArchVersionToMinions =
                minions.stream().collect(
                        Collectors.groupingBy(minion -> longMapMap.get(minion.getServerId()))
                );

        return nameArchVersionToMinions.entrySet().stream().collect(toMap(
                entry -> State.apply(
                        singletonList(PACKAGES_PKGINSTALL),
                        Optional.of(singletonMap(PARAM_PKGS,
                                entry.getKey().entrySet()
                                        .stream()
                                        .map(e -> List.of(
                                                e.getKey(),
                                                e.getValue().getA().replaceAll("-deb$", ""),
                                                e.getValue().getB().endsWith("-X") ?
                                                    e.getValue().getB().substring(0, e.getValue().getB().length() - 2) :
                                                    e.getValue().getB()))
                                        .collect(Collectors.toList())))
                ),
                Map.Entry::getValue
        ));
    }

    private Map<LocalCall<?>, List<MinionSummary>> packagesUpdateAction(
            List<MinionSummary> minionSummaries, PackageUpdateAction action) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();

        List<Long> sids = minionSummaries.stream().map(MinionSummary::getServerId).collect(toList());

        List<String> nevraStrings = action.getDetails().stream().map(details -> {
            PackageName name = details.getPackageName();
            PackageEvr evr = details.getEvr();
            PackageArch arch = details.getArch();
            return name.getName() + "-" + evr.toUniversalEvrString() + "." + arch.getLabel();
        }).collect(toList());

        List<Tuple2<Long, Long>> retractedPidSidPairs = ErrataFactory.retractedPackagesByNevra(nevraStrings, sids);
        Map<Long, List<Long>> retractedPidsBySid = retractedPidSidPairs.stream()
                .collect(groupingBy(Tuple2::getB, mapping(Tuple2::getA, toList())));
        action.getServerActions().forEach(sa -> {
            List<Long> packageIds = retractedPidsBySid.get(sa.getServerId());
            if (packageIds != null) {
                sa.fail("contains retracted packages: " +
                        packageIds.stream().map(Object::toString).collect(joining(",")));
            }
        });
        List<MinionSummary> filteredMinions = minionSummaries.stream()
                .filter(ms -> retractedPidsBySid.get(ms.getServerId()) == null ||
                        retractedPidsBySid.get(ms.getServerId()).isEmpty())
                .collect(toList());

        List<List<String>> pkgs = action
                .getDetails().stream().map(d -> Arrays.asList(d.getPackageName().getName(),
                        d.getArch().toUniversalArchString(), d.getEvr().toUniversalEvrString()))
                .collect(Collectors.toList());
        if (pkgs.isEmpty()) {
            // Full system package update using update state
            ret.put(State.apply(List.of(PACKAGES_PKGUPDATE), Optional.empty()), filteredMinions);
        }
        else {
            ret.put(State.apply(List.of(PACKAGES_PKGINSTALL),
                    Optional.of(singletonMap(PARAM_PKGS, pkgs))), filteredMinions);
        }
        return ret;
    }

    private Map<LocalCall<?>, List<MinionSummary>> packagesRemoveAction(
            List<MinionSummary> minionSummaries, PackageRemoveAction action) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        List<List<String>> pkgsAll = action
                .getDetails().stream().map(d -> Arrays.asList(d.getPackageName().getName(),
                        d.getArch().toUniversalArchString(), d.getEvr().toUniversalEvrString()))
                .collect(Collectors.toList());

        List<List<String>> uniquePkgs = new ArrayList<>();
        pkgsAll.forEach(d -> {
                if (!uniquePkgs.stream().map(p -> p.get(0))
                        .collect(Collectors.toList())
                        .contains(d.get(0))) {
                                uniquePkgs.add(d);
                }
        });
        List<List<String>> duplicatedPkgs = pkgsAll.stream()
                .filter(p -> !uniquePkgs.contains(p)).collect(Collectors.toList());

        Map<String, Object> params = new HashMap<>();
        params.put(PARAM_PKGS, uniquePkgs);
        params.put("param_pkgs_duplicates", duplicatedPkgs);

        ret.put(State.apply(List.of(PACKAGES_PKGREMOVE),
                Optional.of(params)), minionSummaries);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionSummary>> packagesRefreshListAction(
            List<MinionSummary> minionSummaries) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        ret.put(State.apply(List.of(ApplyStatesEventMessage.PACKAGES_PROFILE_UPDATE),
                Optional.empty()), minionSummaries);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionSummary>> hardwareRefreshListAction(
            List<MinionSummary> minionSummaries) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();

        // salt-ssh minions in the 'true' partition
        // regular minions in the 'false' partition
        Map<Boolean, List<MinionSummary>> partitionBySSHPush = minionSummaries.stream()
                .collect(Collectors.partitioningBy(MinionSummary::isSshPush));

        // Separate SSH push minions from regular minions to apply different states
        List<MinionSummary> sshPushMinions = partitionBySSHPush.get(true);
        List<MinionSummary> regularMinions = partitionBySSHPush.get(false);

        if (!sshPushMinions.isEmpty()) {
            ret.put(State.apply(List.of(
                            ApplyStatesEventMessage.HARDWARE_PROFILE_UPDATE),
                    Optional.empty()), minionSummaries);
        }
        if (!regularMinions.isEmpty()) {
            ret.put(State.apply(Arrays.asList(
                    ApplyStatesEventMessage.SYNC_ALL,
                    ApplyStatesEventMessage.HARDWARE_PROFILE_UPDATE),
                    Optional.empty()), minionSummaries);
        }

        return ret;
    }

    private Map<LocalCall<?>, List<MinionSummary>> rebootAction(List<MinionSummary> minionSummaries) {
        int rebootDelay = ConfigDefaults.get().getRebootDelay();
        return minionSummaries.stream().collect(
            Collectors.groupingBy(
                m -> m.isTransactionalUpdate() ? TransactionalUpdate.reboot() :
                        com.suse.salt.netapi.calls.modules.System.reboot(Optional.of(rebootDelay))
            )
        );
    }

    /**
     * Deploy files(files, directory, symlink) through state.apply
     *
     * @param minionSummaries a list of minion summaries of the minions involved in the given Action
     * @param action action which has all the revisions
     * @return minion summaries grouped by local call
     */
    private Map<LocalCall<?>, List<MinionSummary>> deployFiles(List<MinionSummary> minionSummaries,
            ConfigAction action) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();

        Map<Long, MinionSummary> targetMap = minionSummaries.stream().
                collect(Collectors.toMap(MinionSummary::getServerId, minionId-> minionId));

        Map<MinionSummary, Set<ConfigRevision>> serverConfigMap = action.getConfigRevisionActions()
                .stream()
                .filter(cra -> targetMap.containsKey(cra.getServer().getId()))
                .collect(Collectors.groupingBy(
                        cra -> targetMap.get(cra.getServer().getId()),
                        Collectors.mapping(ConfigRevisionAction::getConfigRevision, Collectors.toSet())));
        Map<Set<ConfigRevision>, Set<MinionSummary>> revsServersMap = serverConfigMap.entrySet()
                .stream()
                .collect(Collectors.groupingBy(Map.Entry::getValue,
                        Collectors.mapping(Map.Entry::getKey, Collectors.toSet())));
        revsServersMap.forEach((configRevisions, selectedServers) -> {
            List<Map<String, Object>> fileStates = configRevisions
                    .stream()
                    .map(revision -> ConfigChannelSaltManager.getInstance().getStateParameters(revision))
                    .collect(Collectors.toList());
            ret.put(State.apply(List.of(CONFIG_DEPLOY_FILES),
                    Optional.of(Collections.singletonMap(PARAM_FILES, fileStates))),
                    new ArrayList<>(selectedServers));
        });
        return ret;
    }

    /**
     * Deploy files(files, directory, symlink) through state.apply
     *
     * @param minionSummaries a list of minion summaries of the minions involved in the given Action
     * @param action action which has all the revisions
     * @return minion summaries grouped by local call
     */
    private Map<LocalCall<?>, List<MinionSummary>> diffFiles(List<MinionSummary> minionSummaries, ConfigAction action) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        List<Map<String, Object>> fileStates = action.getConfigRevisionActions().stream()
                .map(ConfigRevisionAction::getConfigRevision)
                .filter(revision -> revision.isFile() ||
                        revision.isDirectory() ||
                        revision.isSymlink())
                .map(revision -> ConfigChannelSaltManager.getInstance().getStateParameters(revision))
                .collect(Collectors.toList());
        ret.put(com.suse.salt.netapi.calls.modules.State.apply(
                List.of(CONFIG_DIFF_FILES),
                Optional.of(Collections.singletonMap(PARAM_FILES, fileStates)),
                Optional.of(true), Optional.of(true)), minionSummaries);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionSummary>> remoteCommandAction(
            List<MinionSummary> minions, ScriptAction scriptAction) {
        String script = scriptAction.getScriptActionDetails().getScriptContents();

        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        // write script to /srv/susemanager/salt/scripts/script_<action_id>.sh
        Path scriptFile = saltUtils.getScriptPath(scriptAction.getId());
        try {
            // make sure parent dir exists
            if (!Files.exists(scriptFile) && !Files.exists(scriptFile.getParent())) {
                FileAttribute<Set<PosixFilePermission>> dirAttributes =
                        PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rwxr-xr-x"));

                Files.createDirectory(scriptFile.getParent(), dirAttributes);
                // make sure correct user is set
            }

            if (!skipCommandScriptPerms) {
                setFileOwner(scriptFile.getParent());
            }

            // In case of action retry, the files script files will be already created.
            if (!Files.exists(scriptFile)) {
                FileAttribute<Set<PosixFilePermission>> fileAttributes =
                        PosixFilePermissions.asFileAttribute(PosixFilePermissions.fromString("rw-r--r--"));
                Files.createFile(scriptFile, fileAttributes);
                FileUtils.writeStringToFile(scriptFile.toFile(),
                        script.replace("\r\n", "\n"), StandardCharsets.UTF_8);
            }

            if (!skipCommandScriptPerms) {
                setFileOwner(scriptFile);
            }

            // state.apply remotecommands
            Map<String, Object> pillar = new HashMap<>();
            pillar.put("mgr_remote_cmd_script", SALT_FS_PREFIX + SCRIPTS_DIR + "/" + scriptFile.getFileName());
            pillar.put("mgr_remote_cmd_runas", scriptAction.getScriptActionDetails().getUsername());
            pillar.put("mgr_remote_cmd_timeout", scriptAction.getScriptActionDetails().getTimeout());
            ret.put(State.apply(List.of(REMOTE_COMMANDS), Optional.of(pillar)), minions);
        }
        catch (IOException e) {
            String errorMsg = "Could not write script to file " + scriptFile + " - " + e;
            LOG.error(errorMsg, e);
            scriptAction.getServerActions().stream()
                    .filter(entry -> entry.getServer().asMinionServer()
                            .map(minionServer -> minions.contains(new MinionSummary(minionServer)))
                            .orElse(false))
                    .forEach(sa -> {
                        sa.fail("Error scheduling the action: " + errorMsg);
                        ActionFactory.save(sa);
            });
        }
        return ret;
    }

    private void setFileOwner(Path path) throws IOException {
        FileSystem fileSystem = FileSystems.getDefault();
        UserPrincipalLookupService service = fileSystem.getUserPrincipalLookupService();
        UserPrincipal tomcatUser = service.lookupPrincipalByName("tomcat");

        Files.setOwner(path, tomcatUser);
    }

    private Map<LocalCall<?>, List<MinionSummary>> applyStatesAction(
            List<MinionSummary> minionSummaries, List<String> mods,
            Optional<Map<String, Object>> pillar, boolean test) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        ret.put(com.suse.salt.netapi.calls.modules.State.apply(mods, pillar, Optional.of(true),
                test ? Optional.of(test) : Optional.empty()), minionSummaries);
        return ret;
    }

    private Map<LocalCall<?>, List<MinionSummary>> subscribeChanelsAction(
            List<MinionSummary> minionSummaries, SubscribeChannelsActionDetails actionDetails) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();

        Stream<MinionServer> minions = MinionServerFactory.lookupByIds(
                minionSummaries.stream().map(MinionSummary::getServerId).collect(Collectors.toList()));

        minions.forEach(minion -> {
            // generate access tokens
            Set<Channel> allChannels = new HashSet<>(actionDetails.getChannels());
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
                    .collect(Collectors.toList());

            newTokens.forEach(newToken -> {
                // set the token as valid, then if something is wrong, the state chanel will disable it
                newToken.setValid(true);
                actionDetails.getAccessTokens().add(newToken);
            });

            MinionGeneralPillarGenerator minionGeneralPillarGenerator = new MinionGeneralPillarGenerator();
            Map<String, Object> chanPillar = new HashMap<>();
            newTokens.forEach(accessToken ->
                accessToken.getChannels().forEach(chan -> {
                    Map<String, Object> chanProps =
                            minionGeneralPillarGenerator.getChannelPillarData(minion, accessToken, chan);
                    chanPillar.put(chan.getLabel(), chanProps);
                })
            );

            Map<String, Object> pillar = new HashMap<>();
            pillar.put("_mgr_channels_items_name", "mgr_channels_new");
            pillar.put("mgr_channels_new", chanPillar);

            ret.put(State.apply(List.of(ApplyStatesEventMessage.CHANNELS),
                    Optional.of(pillar)), Collections.singletonList(new MinionSummary(minion)));

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
        stores.forEach(store -> Optional.ofNullable(store.getCreds())
                .ifPresent(credentials -> {
                    Map<String, Object> reg = new HashMap<>();
                    reg.put("email", "tux@example.com");
                    reg.put("password", credentials.getPassword());
                    reg.put("username", credentials.getUsername());
                    dockerRegistries.put(store.getUri(), reg);
                }));
        return dockerRegistries;
    }

    private Map<LocalCall<?>, List<MinionSummary>> imageInspectAction(
            List<MinionSummary> minions, ImageInspectActionDetails details, ImageStore store) {
        Map<String, Object> pillar = new HashMap<>();
        Map<LocalCall<?>, List<MinionSummary>> result = new HashMap<>();
        if (ImageStoreFactory.TYPE_OS_IMAGE.equals(store.getStoreType())) {
            pillar.put("build_id", "build" + details.getBuildActionId());
            LocalCall<Map<String, ApplyResult>> apply = State.apply(
                    Collections.singletonList("images.kiwi-image-inspect"),
                    Optional.of(pillar));
            result.put(apply, minions);
            return result;
        }
        else {
            List<ImageStore> imageStores = new LinkedList<>();
            imageStores.add(store);
            Map<String, Object> dockerRegistries = dockerRegPillar(imageStores);
            pillar.put("docker-registries", dockerRegistries);
            pillar.put("imagename", store.getUri() + "/" + details.getName() + ":" + details.getVersion());
            pillar.put("build_id", "build" + details.getBuildActionId());
            LocalCall<Map<String, ApplyResult>> apply = State.apply(
                    Collections.singletonList("images.profileupdate"),
                    Optional.of(pillar));
            result.put(apply, minions);
            return result;
        }
    }

    private String getChannelUrl(MinionServer minion, String channelLabel) {
        DownloadTokenBuilder tokenBuilder = new DownloadTokenBuilder(minion.getOrg().getId());
        tokenBuilder.useServerSecret();
        tokenBuilder.setExpirationTimeMinutesInTheFuture(
            Config.get().getInt(ConfigDefaults.TEMP_TOKEN_LIFETIME)
        );
        tokenBuilder.onlyChannels(Collections.singleton(channelLabel));
        String token = "";
        try {
            token = tokenBuilder.getToken();
        }
        catch (JoseException e) {
            LOG.error("Could not generate token for {}", channelLabel, e);
        }

        String host = minion.getChannelHost();

        return "https://" + host + "/rhn/manager/download/" + channelLabel + "?" + token;
    }

    private Map<LocalCall<?>, List<MinionSummary>> imageBuildAction(
            List<MinionSummary> minionSummaries, Optional<String> version,
            ImageProfile profile, Long actionId) {
        List<ImageStore> imageStores = new LinkedList<>();
        imageStores.add(profile.getTargetStore());

        List<MinionServer> minions = MinionServerFactory.findMinionsByServerIds(
                minionSummaries.stream().map(MinionSummary::getServerId).collect(Collectors.toList()));

        //TODO: optimal scheduling would be to group by host and orgid
        return minions.stream().collect(
                Collectors.toMap(minion -> {
                    Map<String, Object> pillar = new HashMap<>();

                    profile.asDockerfileProfile().ifPresent(dockerfileProfile -> {
                        Map<String, Object> dockerRegistries = dockerRegPillar(imageStores);
                        pillar.put("docker-registries", dockerRegistries);

                        String repoPath = Path.of(profile.getTargetStore().getUri(), profile.getLabel()).toString();
                        String tag = version.orElse("");
                        String certificate = "";
                        // salt 2016.11 dockerng require imagename while salt 2018.3 docker requires it separate
                        pillar.put("imagerepopath", repoPath);
                        pillar.put("imagetag", tag);
                        pillar.put("imagename", repoPath + ":" + tag);
                        pillar.put("builddir", dockerfileProfile.getPath());
                        pillar.put("build_id", "build" + actionId);
                        try {
                            //TODO: maybe from the database
                            certificate = String.join("\n\n", Files.readAllLines(
                                    Paths.get("/srv/www/htdocs/pub/RHN-ORG-TRUSTED-SSL-CERT"),
                                    Charset.defaultCharset()
                            ));
                        }
                        catch (IOException e) {
                            LOG.error("Could not read certificate", e);
                        }
                        pillar.put("cert", certificate);
                        String repocontent = "";
                        if (profile.getToken() != null) {
                            repocontent = profile.getToken().getChannels().stream()
                                .map(s -> "[susemanager:" + s.getLabel() + "]\n\n" +
                                    "name=" + s.getName() + "\n\n" +
                                    "enabled=1\n\n" +
                                    "autorefresh=1\n\n" +
                                    "baseurl=" + getChannelUrl(minion, s.getLabel()) + "\n\n" +
                                    "type=rpm-md\n\n" +
                                    "gpgcheck=0\n\n" // we use trusted content and SSL.
                                ).collect(Collectors.joining("\n\n"));
                        }
                        pillar.put("repo", repocontent);

                        // Add custom info values
                        pillar.put("customvalues", profile.getCustomDataValues().stream()
                                .collect(toMap(v -> v.getKey().getLabel(), ProfileCustomDataValue::getValue)));
                    });

                    profile.asKiwiProfile().ifPresent(kiwiProfile -> {
                        pillar.put("source", kiwiProfile.getPath());
                        pillar.put("build_id", "build" + actionId);
                        pillar.put("kiwi_options", kiwiProfile.getKiwiOptions());
                        List<String> repos = new ArrayList<>();
                        final ActivationKey activationKey = ActivationKeyFactory.lookupByToken(profile.getToken());
                        Set<Channel> channels = activationKey.getChannels();
                        for (Channel channel: channels) {
                            repos.add(getChannelUrl(minion, channel.getLabel()));
                        }
                        pillar.put("kiwi_repositories", repos);
                        pillar.put("activation_key", activationKey.getKey());
                    });

                    String saltCall = "";
                    if (profile instanceof DockerfileProfile) {
                        saltCall = "images.docker";
                    }
                    else if (profile instanceof KiwiProfile) {
                        saltCall = "images.kiwi-image-build";
                    }

                    return State.apply(
                            Collections.singletonList(saltCall),
                            Optional.of(pillar)
                    );
                },
                m -> Collections.singletonList(new MinionSummary(m))
        ));
    }

    private Map<LocalCall<?>, List<MinionSummary>> distUpgradeAction(
            DistUpgradeAction action,
            List<MinionSummary> minionSummaries) {
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
            unsubbed.forEach(currentChannels::remove);
            currentChannels.addAll(subbed);
            MinionPillarManager.INSTANCE.generatePillar(minion);
            ServerFactory.save(minion);
        });

        Map<String, Object> pillar = new HashMap<>();
        Map<String, Object> susemanager = new HashMap<>();
        pillar.put("susemanager", susemanager);
        Map<String, Object> distupgrade = new HashMap<>();
        susemanager.put("distupgrade", distupgrade);
        distupgrade.put("dryrun", action.getDetails().isDryRun());
        distupgrade.put(ALLOW_VENDOR_CHANGE, action.getDetails().isAllowVendorChange());
        distupgrade.put("channels", subbed.stream()
                .sorted()
                .map(c -> "susemanager:" + c.getLabel())
                .collect(Collectors.toList()));
        if (Objects.nonNull(action.getDetails().getMissingSuccessors())) {
            pillar.put("missing_successors", Arrays.asList(action.getDetails().getMissingSuccessors().split(",")));
        }

        if (commitTransaction) {
            HibernateFactory.commitTransaction();
        }

        LocalCall<Map<String, ApplyResult>> distUpgrade = State.apply(
                Collections.singletonList(ApplyStatesEventMessage.DISTUPGRADE),
                Optional.of(pillar)
                );
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        ret.put(distUpgrade, minionSummaries);

        return ret;
    }

    private Map<LocalCall<?>, List<MinionSummary>> scapXccdfEvalAction(
            List<MinionSummary> minionSummaries, ScapActionDetails scapActionDetails) {
        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        Map<String, Object> pillar = new HashMap<>();
        Matcher profileMatcher = Pattern.compile("--profile (([\\w.-])+)")
                .matcher(scapActionDetails.getParametersContents());
        Matcher ruleMatcher = Pattern.compile("--rule (([\\w.-])+)")
                .matcher(scapActionDetails.getParametersContents());
        Matcher tailoringFileMatcher = Pattern.compile("--tailoring-file (([\\w./-])+)")
                .matcher(scapActionDetails.getParametersContents());
        Matcher tailoringIdMatcher = Pattern.compile("--tailoring-id (([\\w.-])+)")
                .matcher(scapActionDetails.getParametersContents());

        String oldParameters = "eval " +
                scapActionDetails.getParametersContents() + " " + scapActionDetails.getPath();
        pillar.put("old_parameters", oldParameters);

        pillar.put("xccdffile", scapActionDetails.getPath());
        if (scapActionDetails.getOvalfiles() != null) {
            pillar.put("ovalfiles", Arrays.stream(scapActionDetails.getOvalfiles().split(","))
                    .map(String::trim).collect(toList()));
        }
        if (profileMatcher.find()) {
            pillar.put("profile", profileMatcher.group(1));
        }
        if (ruleMatcher.find()) {
            pillar.put("rule", ruleMatcher.group(1));
        }
        if (tailoringFileMatcher.find()) {
            pillar.put("tailoring_file", tailoringFileMatcher.group(1));
        }
        if (tailoringIdMatcher.find()) {
            pillar.put("tailoring_id", tailoringIdMatcher.group(1));
        }
        if (scapActionDetails.getParametersContents().contains("--fetch-remote-resources")) {
            pillar.put("fetch_remote_resources", true);
        }
        if (scapActionDetails.getParametersContents().contains("--remediate")) {
            pillar.put("remediate", true);
        }

        ret.put(State.apply(singletonList("scap"),
                Optional.of(singletonMap("mgr_scap_params", (Object)pillar))),
                minionSummaries);
        return ret;
    }

    private String virtGetDomainNameFromUuid(MinionSummary minionSummary, String uuid) {
        String domainName = null;
        VirtualInstance domain = VirtualInstanceFactory.getInstance()
                .lookupVirtualInstanceByHostIdAndUuid(minionSummary.getServerId(), uuid);
        if (domain != null) {
            domainName = domain.getName();
        }
        else {
            // We may have a stopped VM from a cluster that is not defined anywhere in our DB
            // Check if the VM is listed in the cluster VMs
            VirtManager virtManager = new VirtManagerSalt(saltApi);
            domainName = virtManager.getVmInfos(minionSummary.getMinionId())
                    .flatMap(infos -> infos.entrySet().stream()
                            .filter(entry -> {
                            JsonElement vmUuid = entry.getValue().get("uuid");
                            if (vmUuid == null) {
                                return false;
                            }
                            return vmUuid.getAsString().replace("-", "").equals(uuid);
                        })
                        .map(Map.Entry::getKey)
                        .findFirst())
                    .orElse(null);
        }
        return domainName;
    }

    private Map<LocalCall<?>, List<MinionSummary>> virtStateChangeAction(
            List<MinionSummary> minionSummaries, String uuid, String state, BaseVirtualizationGuestAction action) {
        Map<LocalCall<?>, List<MinionSummary>> ret = minionSummaries.stream().collect(
                Collectors.toMap(minion -> {

                    String domainName = virtGetDomainNameFromUuid(minion, uuid);
                    if (domainName != null) {
                        Map<String, Object> pillar = new HashMap<>();
                        pillar.put("domain_name", domainName);

                        String[] states = {"deleted", "suspended", "resumed"};
                        if (Arrays.asList(states).contains(state)) {
                            return State.apply(
                                    Collections.singletonList("virt." + state),
                                    Optional.of(pillar));
                        }
                        else if (state.equals("rebooted") && ((VirtualizationRebootGuestAction)action).isForce()) {
                            return State.apply(
                                    Collections.singletonList("virt.reset"),
                                    Optional.of(pillar));
                        }
                        else {
                            if (state.equals("stopped") && ((VirtualizationShutdownGuestAction)action).isForce()) {
                                pillar.put("domain_state", "powered_off");
                            }
                            else {
                                pillar.put("domain_state", state);
                            }

                            return State.apply(
                                    Collections.singletonList("virt.statechange"),
                                    Optional.of(pillar));
                        }
                    }
                    else {
                        LOG.error("Failed to retrieve domain name for server {} uuid {}", minion.getServerId(), uuid);
                    }
                    return null;
                },
                Collections::singletonList
        ));

        ret.remove(null);

        return ret;
    }

    private Map<LocalCall<?>, List<MinionSummary>> virtSetterAction(
            List<MinionSummary> minionSummaries, String uuid, String property, int value) {
        Map<LocalCall<?>, List<MinionSummary>> ret = minionSummaries.stream().collect(
                Collectors.toMap(minion -> {

                    String domainName = virtGetDomainNameFromUuid(minion, uuid);
                    if (domainName != null) {
                        Map<String, Object> pillar = new HashMap<>();
                        pillar.put("domain_name", domainName);
                        pillar.put("domain_" + property, value);

                        return State.apply(
                                Collections.singletonList("virt.set" + property),
                                Optional.of(pillar));
                    }
                    else {
                        LOG.error("Failed to retrieve domain name for server {} uuid {}", minion.getServerId(), uuid);
                    }
                    return null;
                },
                Collections::singletonList
        ));

        ret.remove(null);

        return ret;
    }

    private Map<LocalCall<?>, List<MinionSummary>> virtGuestMigrateAction(List<MinionSummary> minions,
                                                                           VirtualizationMigrateGuestAction action) {
        Map<String, Object> pillar = Map.of(
                "primitive", action.getPrimitive(),
                "target", action.getTarget()
        );

        return Map.of(
                State.apply(Collections.singletonList("virt.guest-migrate"), Optional.of(pillar)),
                minions
        );
    }

    private List<Map<String, Object>> virtDomainDiskToPillar(VirtualizationCreateGuestAction action) {
        return IntStream.range(0, action.getDetails().getDisks().size()).mapToObj(i -> {
            VirtualGuestsUpdateActionJson.DiskData disk = action.getDetails().getDisks().get(i);
            Map<String, Object> diskData = new HashMap<>();
            String diskName = "system";
            if (i > 0) {
                diskName = String.format("disk-%d", i);
            }
            diskData.put("name", diskName);
            diskData.put("format", disk.getFormat());
            if (disk.getSourceFile() != null || disk.getDevice().equals("cdrom")) {
                diskData.put("source_file", disk.getSourceFile());
            }
            diskData.put("pool", disk.getPool());
            diskData.put("image", disk.getTemplate());
            if (disk.getSize() != 0) {
                diskData.put("size", disk.getSize() * 1024);
            }
            diskData.put("model", disk.getBus());
            diskData.put("device", disk.getDevice());

            return diskData;
        }).collect(Collectors.toList());
    }

    private Map<String, Object> virtDomainActionToPillar(VirtualizationCreateGuestAction action) {
        // Prepare the salt FS with kernel / initrd and pass params to kernel
        String cobblerSystemName = action.getDetails().getCobblerSystem();
        Map<String, String> bootParams = cobblerSystemName != null ?
                prepareCobblerBoot(action.getDetails().getKickstartHost(), cobblerSystemName) :
                null;

        // Some of these pillar data will be useless for update-vm, but they will just be ignored.
        Map<String, Object> pillar = new HashMap<>();
        pillar.put("name", action.getDetails().getName());
        pillar.put("vcpus", action.getDetails().getVcpu());
        pillar.put("mem", action.getDetails().getMemory());
        pillar.put("vm_type", action.getDetails().getType());
        pillar.put("os_type", action.getDetails().getOsType());
        pillar.put("arch", action.getDetails().getArch());
        pillar.put("cluster_definitions", action.getDetails().getClusterDefinitions());
        pillar.put("template", action.getDetails().getTemplate());

        if (action.getDetails().isUefi()) {
            Map<String, Object> uefiData = new HashMap<>();
            if (action.getDetails().getUefiLoader() == null) {
                uefiData.put("efi", true);
            }
            else {
                uefiData.put("loader", action.getDetails().getUefiLoader());
                uefiData.put("nvram", action.getDetails().getNvramTemplate());
            }
            pillar.put("uefi", uefiData);
        }

        // No need to handle copying the image to the minion, salt does it for us
        if (!action.getDetails().getDisks().isEmpty() || action.getDetails().isRemoveDisks()) {
            pillar.put("disks", virtDomainDiskToPillar(action));
        }

        if (!action.getDetails().getInterfaces().isEmpty() || action.getDetails().isRemoveInterfaces()) {
            pillar.put("interfaces",
                    IntStream.range(0, action.getDetails().getInterfaces().size()).mapToObj(i -> {
                        VirtualGuestsUpdateActionJson.InterfaceData iface = action.getDetails()
                                .getInterfaces().get(i);
                        Map<String, Object> ifaceData = new HashMap<>();
                        ifaceData.put("name", String.format("eth%d", i));
                        ifaceData.put("type", iface.getType());
                        ifaceData.put("source", iface.getSource());
                        ifaceData.put("mac", iface.getMac());
                        return ifaceData;
                    }).collect(Collectors.toList()));
        }

        Map<String, Object> graphicsData = new HashMap<>();
        graphicsData.put("type", action.getDetails().getGraphicsType());
        pillar.put("graphics", graphicsData);

        if (bootParams != null) {
            pillar.put("boot", bootParams);
        }

        // If we have a DVD image and we are creating a VM, set "cdrom hd" boot devices
        // otherwise set "network hd"
        boolean hasCdromIso = action.getDetails().getDisks().stream()
                .anyMatch(disk -> disk.getDevice().equals("cdrom") && disk.getSourceFile() != null &&
                        !disk.getSourceFile().isEmpty());
        boolean noTemplateImage = action.getDetails().getDisks().stream()
                .noneMatch(disk -> disk.getTemplate() != null);
        String bootDev = noTemplateImage ? "network hd" : "hd";
        pillar.put("boot_dev", hasCdromIso ? "cdrom hd" : bootDev);

        return pillar;
    }

    private Map<LocalCall<?>, List<MinionSummary>> virtCreateAction(List<MinionSummary> minions,
            VirtualizationCreateGuestAction action) {
        String state = action.getUuid() != null ? "virt.update-vm" : "virt.create-vm";

        Map<String, Object> pillar = virtDomainActionToPillar(action);
        LocalCall<?> stateCall = State.apply(Collections.singletonList(state), Optional.of(pillar));
        return Map.of(stateCall, minions);
    }

    private Map<String, String> prepareCobblerBoot(String kickstartHost,
                                                   String cobblerSystem) {
        CobblerConnection con = CobblerXMLRPCHelper.getAutomatedConnection();
        SystemRecord system = SystemRecord.lookupByName(con, cobblerSystem);
        Profile profile = system.getProfile();
        Distro dist = profile.getDistro();
        String kernel = dist.getKernel();
        String initrd = dist.getInitrd();

        List<String> nameParts = Arrays.asList(dist.getName().split(":"));
        String saltFSKernel = Paths.get(nameParts.get(1), nameParts.get(0), new File(kernel).getName()).toString();
        String saltFSInitrd = Paths.get(nameParts.get(1), nameParts.get(0), new File(initrd).getName()).toString();
        KickstartableTree tree = KickstartFactory.lookupKickstartTreeByLabel(nameParts.get(0),
                OrgFactory.lookupById(Long.valueOf(nameParts.get(1))));
        tree.createOrUpdateSaltFS();
        String kOpts = buildKernelOptions(system, kickstartHost);
        Map<String, String> pillar = new HashMap<>();
        pillar.put("kernel", saltFSKernel);
        pillar.put("initrd", saltFSInitrd);
        pillar.put("kopts", kOpts);

        return pillar;
    }

    private Map<LocalCall<?>, List<MinionSummary>> autoinstallInitAction(List<MinionSummary> minions,
            KickstartInitiateAction autoInitAction) {

        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        KickstartActionDetails ksActionDetails = autoInitAction.getKickstartActionDetails();
        String cobblerSystem = ksActionDetails.getCobblerSystemName();
        String host = ksActionDetails.getKickstartHost();
        Map<String, String> bootParams = prepareCobblerBoot(host, cobblerSystem);
        Map<String, Object> pillar = new HashMap<>(bootParams);
        pillar.put("uyuni-reinstall-name", "reinstall-system");
        String kOpts = bootParams.get("kopts");

        if (kOpts.contains("autoupgrade=1") || kOpts.contains("uyuni_keep_saltkey=1")) {
            ksActionDetails.setUpgrade(true);
        }
        ret.put(State.apply(List.of(KICKSTART_INITIATE), Optional.of(pillar)), minions);

        return ret;
    }

    private Map<LocalCall<?>, List<MinionSummary>> virtPoolRefreshAction(
            List<MinionSummary> minionSummaries, String poolName) {
        return Map.of(
                State.apply(Collections.singletonList("virt.pool-refreshed"),
                        Optional.of(Map.of("pool_name", poolName))),
                minionSummaries
        );
    }

    private String buildKernelOptions(SystemRecord sys, String host) {
        String breed = sys.getProfile().getDistro().getBreed();
        Map<String, Object> kopts = sys.getResolvedKernelOptions();
        if (LOG.isDebugEnabled()) {
            LOG.debug("Resolved kernel options for {}: {}", sys.getName(), convertOptionsMap(kopts));
        }
        if (breed.equals("suse")) {
            //SUSE is not using 'text'. Instead 'textmode' is used as kernel option.
            if (kopts.containsKey("textmode")) {
                kopts.remove("text");
            }
            else if (kopts.containsKey("text")) {
                kopts.remove("text");
                kopts.put("textmode", "1");
            }
        }
        // no additional initrd parameter allowed
        kopts.remove("initrd");
        String kernelOptions = convertOptionsMap(kopts);
        String autoinst = "http://" + host + "/cblr/svc/op/autoinstall/system/" + sys.getName();

        if (StringUtils.isBlank(breed) || breed.equals("redhat")) {
            if (sys.getProfile().getDistro().getOsVersion().equals("rhel6")) {
                kernelOptions += " kssendmac ks=" + autoinst;
            }
            else {
                kernelOptions += " inst.ks.sendmac ks=" + autoinst;
            }
        }
        else if (breed.equals("suse")) {
            kernelOptions += "autoyast=" + autoinst;
        }
        else if (breed.equals("debian") || breed.equals("ubuntu")) {
            kernelOptions += "auto-install/enable=true priority=critical netcfg/choose_interface=auto url=" + autoinst;
        }
        return kernelOptions;
    }

    private String convertOptionsMap(Map<String, Object> map) {
        StringBuilder string = new StringBuilder();
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            String key = entry.getKey();
            List<String> keyList;
            try {
                 keyList = (List<String>)entry.getValue();
            }
            catch (ClassCastException e) {
                keyList = new ArrayList<>();
                keyList.add((String) entry.getValue());
            }
            string.append(key);
            if (keyList.isEmpty()) {
                string.append(" ");
            }
            else {
                for (String value : keyList) {
                    string.append("=").append(value).append(" ");
                }
            }
        }
        return string.toString();
    }

    private Map<LocalCall<?>, List<MinionSummary>> virtPoolStateChangeAction(
            List<MinionSummary> minionSummaries, String poolName, String state) {
        Map<String, Object> pillar = Map.of(
                "pool_state", state,
                "pool_name", poolName
        );

        return Map.of(
                State.apply(Collections.singletonList("virt.pool-statechange"), Optional.of(pillar)),
                minionSummaries
        );
    }

    private Map<LocalCall<?>, List<MinionSummary>> virtPoolDeleteAction(
            List<MinionSummary> minionSummaries, String poolName, boolean purge) {
        Map<String, Object> pillar = Map.of(
                "pool_name", poolName,
                "pool_purge", purge
        );

        return Map.of(
                State.apply(Collections.singletonList("virt.pool-deleted"), Optional.of(pillar)),
                minionSummaries
        );
    }

    Map<String, Object> virtPoolSourceToPillar(VirtualizationPoolCreateAction action) {
        Map<String, Object> source = new HashMap<>();
        source.put("dir", action.getSource().getDir());
        source.put("name", action.getSource().getName());
        source.put("format", action.getSource().getFormat());
        source.put("initiator", action.getSource().getInitiator());
        source.put("hosts", action.getSource().getHosts());
        if (action.getSource().getAuth() != null) {
            Map<String, Object> auth = new HashMap<>();
            auth.put("username", action.getSource().getAuth().getUsername());
            // TODO We surely need this one encrypted
            auth.put("password", action.getSource().getAuth().getPassword());
            source.put("auth", auth);
        }
        if (action.getSource().getAdapter() != null) {
            Map<String, Object> adapter = new HashMap<>();
            adapter.put("type", action.getSource().getAdapter().getType());
            adapter.put("name", action.getSource().getAdapter().getName());
            adapter.put("parent", action.getSource().getAdapter().getParent());
            adapter.put("managed", action.getSource().getAdapter().isManaged());
            adapter.put("parent_wwnn", action.getSource().getAdapter().getParentWwnn());
            adapter.put("parent_wwpn", action.getSource().getAdapter().getParentWwpn());
            adapter.put("parent_fabric_wwn", action.getSource().getAdapter().getParentWwnn());
            adapter.put("wwnn", action.getSource().getAdapter().getWwnn());
            adapter.put("wwpn", action.getSource().getAdapter().getWwpn());

            Map<String, Object> parentAddress = new HashMap<>();
            if (action.getSource().getAdapter().getParentAddressUid() != null) {
                parentAddress.put("unique_id", action.getSource().getAdapter().getParentAddressUid());
            }
            if (action.getSource().getAdapter().getParentAddress() != null) {
                parentAddress.put("address", action.getSource().getAdapter().getParentAddressParsed());
            }
            if (!parentAddress.isEmpty()) {
                adapter.put("parent_address", parentAddress);
            }

            source.put("adapter", adapter);
        }
        if (action.getSource().getDevices() != null) {
            source.put("devices", action.getSource().getDevices().stream().map(device -> {
                Map<String, Object> deviceParam = new HashMap<>();
                deviceParam.put("path", device.getPath());
                device.isSeparator().ifPresent(sep -> deviceParam.put("part_separator", sep));
                return deviceParam;
            }).collect(Collectors.toList()));
        }
        return source;
    }

    private Map<String, Object> virtPoolActionToPillar(VirtualizationPoolCreateAction action) {
        Map<String, Object> pillar = new HashMap<>();
        pillar.put("pool_name", action.getPoolName());
        pillar.put("pool_type", action.getType());
        pillar.put("autostart", action.isAutostart());
        pillar.put("target", action.getTarget());

        Map<String, Object> permissions = new HashMap<>();
        permissions.put("mode", action.getMode());
        permissions.put("owner", action.getOwner());
        permissions.put("group", action.getGroup());
        permissions.put("label", action.getSeclabel());
        pillar.put("permissions", permissions);
        pillar.put("autostart", action.isAutostart());

        if (action.getSource() != null) {
            pillar.put("source", virtPoolSourceToPillar(action));
        }
        pillar.put("action_type", action.getUuid() != null ? "defined" : "running");
        return pillar;
    }

    private Map<LocalCall<?>, List<MinionSummary>> virtPoolCreateAction(
            List<MinionSummary> minionSummaries, VirtualizationPoolCreateAction action) {
        Map<String, Object> pillar = virtPoolActionToPillar(action);
        return Map.of(
                State.apply(Collections.singletonList("virt.pool-create"), Optional.of(pillar)),
                minionSummaries
        );
    }

    private Map<LocalCall<?>, List<MinionSummary>> virtVolumeDeleteAction(
            List<MinionSummary> minionSummaries, String poolName, String volumeName) {
        Map<String, Object> pillar = Map.of(
                "pool_name", poolName,
                "volume_name", volumeName
        );

        return Map.of(
                State.apply(Collections.singletonList("virt.volume-deleted"), Optional.of(pillar)),
                minionSummaries
        );
    }

    private Map<LocalCall<?>, List<MinionSummary>> virtNetworkStateChangeAction(
            List<MinionSummary> minionSummaries, String networkName, String state) {
        Map<String, Object> pillar = Map.of(
                "network_state", state,
                "network_name", networkName
        );

        return Map.of(
                State.apply(Collections.singletonList("virt.network-statechange"), Optional.of(pillar)),
                minionSummaries
        );
    }


    private Map<String, Object> virtNetworkDefinitionToPillar(String networkName, NetworkDefinition def) {
        Map<String, Object> pillar = new HashMap<>();
        pillar.put("network_name", networkName);
        def.getBridge().ifPresent(bridge -> pillar.put("bridge", bridge));
        pillar.put("forward", def.getForwardMode());
        pillar.put("autostart", def.isAutostart());
        def.getMtu().ifPresent(mtu -> pillar.put("mtu", mtu));
        def.getDomain().ifPresent(domain -> pillar.put("domain", domain));
        def.getPhysicalFunction().ifPresent(pf -> pillar.put("physical_function", pf));
        if (!def.getVirtualFunctions().isEmpty()) {
            pillar.put("addresses", String.join(" ", def.getVirtualFunctions()));
        }
        if (!def.getInterfaces().isEmpty()) {
            pillar.put("interfaces", String.join(" ", def.getInterfaces()));
        }
        if (!def.getVlans().isEmpty()) {
            Map<String, Object> tag = new HashMap<>();
            def.getVlanTrunk().ifPresent(trunk -> tag.put("trunk", trunk));
            tag.put("tags",
                    def.getVlans().stream().map(vlan -> {
                        HashMap<String, Object> vlanPillar = new HashMap<>();
                        vlanPillar.put("id", vlan.getTag());
                        vlan.getNativeMode().ifPresent(mode -> vlanPillar.put("nativeMode", mode));
                        return vlanPillar;
                    }).collect(Collectors.toList())
            );
            pillar.put("tag", tag);
        }
        def.getVirtualPort().ifPresent(vportData -> {
            Map<String, Object> vport = new HashMap<>();
            vport.put("type", vportData.getType());
            Map<String, String> params = new HashMap<>();
            vportData.getInstanceId().ifPresent(id -> params.put("instanceid", id));
            vportData.getInterfaceId().ifPresent(id -> params.put("interfaceid", id));
            vportData.getManagerId().ifPresent(id -> params.put("managerid", id));
            vportData.getTypeId().ifPresent(id -> params.put("typeid", id));
            vportData.getTypeIdVersion().ifPresent(v -> params.put("typeidversion", v));
            vportData.getProfileId().ifPresent(id -> params.put("profileid", id));
            vport.put("params", params);
            pillar.put("vport", vport);
        });
        def.getNat().ifPresent(natData -> {
            Map<String, Object> nat = new HashMap<>();
            natData.getAddress().ifPresent(range ->
                    nat.put("address", VirtStatesHelper.rangeToPillar(range)));
            natData.getPort().ifPresent(ports ->
                    nat.put("port", VirtStatesHelper.rangeToPillar(ports)));
            pillar.put("nat", nat);
        });
        def.getIpv4().ifPresent(ipDef ->
                pillar.put("ipv4_config", VirtStatesHelper.ipToPillar(ipDef)));
        def.getIpv6().ifPresent(ipDef ->
                pillar.put("ipv6_config", VirtStatesHelper.ipToPillar(ipDef)));
        def.getDns().ifPresent(dnsData -> {
            Map<String, Object> dns = new HashMap<>();
            if (!dnsData.getForwarders().isEmpty()) {
                dns.put("forwarders", dnsData.getForwarders().stream().map(fwd -> {
                    Map<String, Object> out = new HashMap<>();
                    fwd.getAddress().ifPresent(addr -> out.put("addr", addr));
                    fwd.getDomain().ifPresent(domain -> out.put("domain", domain));
                    return out;
                }).collect(Collectors.toList()));
            }
            if (!dnsData.getHosts().isEmpty()) {
                dns.put("hosts", dnsData.getHosts().stream()
                        .collect(Collectors.toMap(DnsHostDef::getAddress, DnsHostDef::getNames)));
            }
            if (!dnsData.getTxts().isEmpty()) {
                dns.put("txt", dnsData.getTxts().stream()
                        .collect(Collectors.toMap(DnsTxtDef::getName, DnsTxtDef::getValue)));
            }
            if (!dnsData.getSrvs().isEmpty()) {
                dns.put("srvs", dnsData.getSrvs().stream().map(srv -> {
                    Map<String, Object> out = new HashMap<>();
                    out.put("name", srv.getName());
                    out.put("protocol", srv.getProtocol());
                    srv.getTarget().ifPresent(target -> out.put("target", target));
                    srv.getPort().ifPresent(port -> out.put("port", port));
                    srv.getPriority().ifPresent(priority -> out.put("priority", priority));
                    srv.getDomain().ifPresent(domain -> out.put("domain", domain));
                    srv.getWeight().ifPresent(weight -> out.put("weight", weight));
                    return out;
                }).collect(Collectors.toList()));
            }
            pillar.put("dns", dns);
        });
        pillar.put("action_type", def.getUuid().map(uuid -> "defined").orElse("running"));

        return pillar;
    }

    private Map<LocalCall<?>, List<MinionSummary>> virtNetworkCreateAction(List<MinionSummary> minionSummaries,
                                                                           String networkName,
                                                                           NetworkDefinition def) {
        Map<String, Object> pillar = virtNetworkDefinitionToPillar(networkName, def);
        return Map.of(
                State.apply(Collections.singletonList("virt.network-create"), Optional.of(pillar)),
                minionSummaries
        );
    }


    private LocalCall<?> executePlaybookActionCall(PlaybookAction action) {
        PlaybookActionDetails details = action.getDetails();

        String playbookPath = details.getPlaybookPath();
        String rundir = new File(playbookPath).getAbsoluteFile().getParent();
        String inventoryPath = details.getInventoryPath();

        if (StringUtils.isEmpty(inventoryPath)) {
            inventoryPath = INVENTORY_PATH;
        }

        Map<String, Object> pillarData = new HashMap<>();
        pillarData.put("playbook_path", playbookPath);
        pillarData.put("inventory_path", inventoryPath);
        pillarData.put("rundir", rundir);
        pillarData.put("flush_cache", details.isFlushCache());
        return State.apply(singletonList(ANSIBLE_RUNPLAYBOOK), Optional.of(pillarData), Optional.of(true),
                Optional.of(details.isTestMode()));
    }

    /**
     * Prepare to execute staging job via Salt
     * @param actionIn the action
     * @param minionSummaries a list of minion summaries of the minions involved in the given Action
     * @return a call with the impacted minions
     */
    private LocalCall<?> prepareStagingTargets(Action actionIn, List<MinionSummary> minionSummaries) {
        LocalCall<?> call = null;
        if (actionIn.getActionType().equals(ActionFactory.TYPE_PACKAGES_UPDATE)) {
            List<List<String>> args = ((PackageUpdateAction) actionIn)
                    .getDetails().stream().map(d -> Arrays.asList(d.getPackageName().getName(),
                            d.getArch().toUniversalArchString(), d.getEvr().toUniversalEvrString()))
                    .collect(Collectors.toList());
            call = State.apply(List.of(PACKAGES_PKGDOWNLOAD),
                    Optional.of(Collections.singletonMap(PARAM_PKGS, args)));
            LOG.info("Executing staging of packages");
        }
        if (actionIn.getActionType().equals(ActionFactory.TYPE_ERRATA)) {
            Set<Long> errataIds = ((ErrataAction) actionIn).getErrata().stream()
                    .map(Errata::getId).collect(Collectors.toSet());
            Map<Long, Map<Long, Set<ErrataInfo>>> errataNames = ServerFactory
                    .listErrataNamesForServers(minionSummaries.stream().map(MinionSummary::getServerId)
                            .collect(Collectors.toSet()), errataIds);
            List<String> errataArgs = errataNames.entrySet().stream()
                .flatMap(e -> e.getValue().entrySet().stream()
                     .flatMap(f -> f.getValue().stream()
                         .map(ErrataInfo::getName)
                     )
                )
                .collect(Collectors.toList());

            call = State.apply(List.of(PACKAGES_PATCHDOWNLOAD),
                    Optional.of(Collections.singletonMap(PARAM_PATCHES, errataArgs)));
            LOG.info("Executing staging of patches");
        }
        return call;
    }

    /**
     * @param actionIn the action
     * @param call the call
     * @param minionSummaries a list of minion summaries of the minions involved in the given Action
     * @param forcePackageListRefresh add metadata to force a package list refresh
     * @param isStagingJob if the job is a staging job
     * @return a map containing all minions partitioned by success
     */
    private Map<Boolean, List<MinionSummary>> execute(Action actionIn, LocalCall<?> call,
            List<MinionSummary> minionSummaries, boolean forcePackageListRefresh,
            boolean isStagingJob) {
        List<String> minionIds = minionSummaries.stream().map(MinionSummary::getMinionId).collect(Collectors.toList());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing action for: {}", minionIds.stream().collect(Collectors.joining(", ")));
        }

        try {
            ScheduleMetadata metadata = ScheduleMetadata.getMetadataForRegularMinionActions(
                    isStagingJob, forcePackageListRefresh, actionIn.getId());
            List<String> results = Opt.fold(
                    saltApi.callAsync(call, new MinionList(minionIds), Optional.of(metadata)),
                    ArrayList::new,
                    LocalAsyncResult::getMinions);

            return minionSummaries.stream().collect(Collectors
                    .partitioningBy(minionId -> results.contains(minionId.getMinionId())));
        }
        catch (SaltException ex) {
            LOG.debug("Failed to execute action: {}", ex.getMessage());
            Map<Boolean, List<MinionSummary>> result = new HashMap<>();
            result.put(true, Collections.emptyList());
            result.put(false, minionSummaries);
            return result;
        }
    }

    /**
     * @param actionChain the actionChain
     * @param minionSummaries a set of minion summaries of the minions involved in the given Action
     * @return a map containing all minions partitioned by success
     */
    private Map<Boolean, Set<MinionSummary>> callAsyncActionChainStart(
            ActionChain actionChain,
            Set<MinionSummary> minionSummaries) {
        List<String> minionIds = minionSummaries.stream().map(MinionSummary::getMinionId)
                .collect(Collectors.toList());

        if (LOG.isDebugEnabled()) {
            LOG.debug("Executing action chain for: {}", String.join(", ", minionIds));
        }

        try {
            List<String> results = saltApi
                    .callAsync(MgrActionChains.start(actionChain.getId()), new MinionList(minionIds),
                            Optional.of(ScheduleMetadata.getDefaultMetadata().withActionChain(actionChain.getId())))
                    .map(LocalAsyncResult::getMinions)
                    .orElse(Collections.emptyList());

            return minionSummaries.stream()
                    .collect(Collectors.partitioningBy(
                            minion -> results.contains(minion.getMinionId()),
                            Collectors.toSet()
                    ));
        }
        catch (SaltException ex) {
            LOG.debug("Failed to execute action chain: {}", ex.getMessage());
            Map<Boolean, Set<MinionSummary>> result = new HashMap<>();
            result.put(true, Collections.emptySet());
            result.put(false, minionSummaries);
            return result;
        }
    }

    /**
     * Execute an action on an ssh-push minion.
     *
     * @param action the action to be executed
     * @param minion minion on which the action will be executed
     */
    public void executeSSHAction(Action action, MinionServer minion) {
        executeSSHAction(action, minion, false);
    }

    /**
     * Execute an action on an ssh-push minion.
     *
     * @param action the action to be executed
     * @param minion minion on which the action will be executed
     * @param forcePkgRefresh set to true if a package list refresh should be scheduled at the end
     */
    public void executeSSHAction(Action action, MinionServer minion, boolean forcePkgRefresh) {
        Optional<ServerAction> serverAction = action.getServerActions().stream()
                .filter(sa -> sa.getServerId().equals(minion.getId()))
                .findFirst();
        serverAction.ifPresent(sa -> {
            if (List.of(STATUS_FAILED, STATUS_COMPLETED).contains(sa.getStatus())) {
                LOG.info("Action '{}' is completed or failed. Skipping.", action.getName());
                return;
            }

            if (prerequisiteInStatus(sa, ActionFactory.STATUS_QUEUED)) {
                LOG.info("Prerequisite of action '{}' is still queued. Skipping executing of the action.",
                        action.getName());
                return;
            }

            if (prerequisiteInStatus(sa, ActionFactory.STATUS_FAILED)) {
                LOG.info("Failing action '{}' as its prerequisite '{}' failed.", action.getName(),
                        action.getPrerequisite().getName());
                sa.fail(-100L, "Prerequisite failed.");
                return;
            }

            sa.setRemainingTries(sa.getRemainingTries() - 1);

            Map<LocalCall<?>, List<MinionSummary>> calls = callsForAction(action, List.of(new MinionSummary(minion)));

            for (LocalCall<?> call : calls.keySet()) {
                Optional<Result<JsonElement>> result;
                // try-catch as we'd like to log the warning in case of exception
                try {
                    result = saltApi.rawJsonCall(call, minion.getMinionId());
                }
                catch (RuntimeException e) {
                    LOG.error("Error executing Salt call for action: {} on minion {}",
                            action.getName(), minion.getMinionId(), e);
                    sa.setStatus(STATUS_FAILED);
                    sa.setResultMsg("Error calling Salt: " + e.getMessage());
                    sa.setCompletionTime(new Date());
                    return;
                }

                result.ifPresentOrElse(r -> {
                    LOG.trace("Salt call result: {}", r);

                    r.consume(error -> {
                        String errorString = error.toString();
                        if (sa.getRemainingTries() > 0 && errorString.contains("System is going down")) {
                            // SSH login is blocked when a reboot is ongoing. Reschedule this action later again
                            LOG.info("System is going down. Configure re-try in 3 minutes");
                            sa.setStatus(STATUS_QUEUED);
                            sa.setRemainingTries((sa.getRemainingTries() - 1L));
                            sa.setPickupTime(null);
                            sa.setCompletionTime(null);
                            action.setEarliestAction(Date.from(Instant.now().plus(3, ChronoUnit.MINUTES)));
                            ActionFactory.save(action);
                            // We commit as we need to take care that the new date is in DB when we
                            // call taskomatic to execute the action again.
                            HibernateFactory.commitTransaction();
                            try {
                                taskomaticApi.scheduleActionExecution(action);
                            }
                            catch (TaskomaticApiException e) {
                                LOG.error("Unable to reschedule failed Salt SSH Action: {}", errorString, e);
                                sa.setStatus(STATUS_FAILED);
                                sa.setResultMsg(errorString);
                                sa.setCompletionTime(new Date());
                            }
                        }
                        else {
                            sa.setStatus(STATUS_FAILED);
                            sa.setResultMsg(error.fold(
                                    e -> "function " + e.getFunctionName() + " not available.",
                                    e -> "module " + e.getModuleName() + " not supported.",
                                    e -> "error parsing json.",
                                    GenericError::getMessage,
                                    e -> "salt ssh error: " + e.getRetcode() + " " + e.getMessage()
                            ));
                            LOG.error(sa.getResultMsg());
                            sa.setCompletionTime(new Date());
                        }
                    }, jsonResult -> {
                        String function = (String) call.getPayload().get("fun");

                        /* bsc#1197591 ssh push reboot has an answer that is not a failure but the action needs to stay
                        *  in picked up, in this way SSHServiceDriver::getCandidates can schedule a reboot correctly
                        */
                        if (!action.getActionType().equals(ActionFactory.TYPE_REBOOT)) {
                            saltUtils.updateServerAction(sa, 0L, true, "n/a", jsonResult,
                                    Optional.of(Xor.right(function)));
                        }

                        else if (sa.getStatus().equals(ActionFactory.STATUS_QUEUED)) {
                            setActionAsPickedUp(sa);
                        }

                        // Perform a "check-in" after every executed action
                        minion.updateServerInfo();

                        // Perform a package profile update in the end if necessary
                        if (forcePkgRefresh || saltUtils.shouldRefreshPackageList(
                                Optional.of(Xor.right(function)), Optional.of(jsonResult))) {
                            LOG.info("Scheduling a package profile update");

                            try {
                                ActionManager.schedulePackageRefresh(
                                        Optional.ofNullable(action.getSchedulerUser()), minion);
                            }
                            catch (TaskomaticApiException e) {
                                LOG.error("Could not schedule package refresh for minion: {}", minion.getMinionId(), e);
                            }
                        }
                    });
                }, () -> {
                    LOG.error("Action '{}' failed. Got not result from Salt, probably minion is down or " +
                            "could not be contacted.", action.getName());
                    sa.setStatus(STATUS_FAILED);
                    sa.setResultMsg("Minion is down or could not be contacted.");
                    sa.setCompletionTime(new Date());
                });
            }
        });
    }

    /**
     * Checks whether the parent action of given server action contains a server action
     * that is in given state and is associated with the server of given server action.
     * @param serverAction server action
     * @param state state
     * @return true if there exists a server action in given state associated with the same
     * server as serverAction and parent action of serverAction
     */
    private boolean prerequisiteInStatus(ServerAction serverAction, ActionStatus state) {
        Optional<Stream<ServerAction>> prerequisites =
                ofNullable(serverAction.getParentAction())
                        .map(Action::getPrerequisite)
                        .map(Action::getServerActions)
                        .map(Collection::stream);

        return prerequisites
                .flatMap(serverActions ->
                        serverActions
                                .filter(s ->
                                        serverAction.getServer().equals(s.getServer()) &&
                                                state.equals(s.getStatus()))
                                .findAny())
                .isPresent();
    }

    /**
     * Set the given action to FAILED if not already in that state and also the dependent actions.
     * @param actionId the action id
     * @param minionId the minion id
     * @param message the result message to set in the server action
     */
    public void failDependentServerActions(long actionId, String minionId, Optional<String> message) {
        Optional<MinionServer> minion = MinionServerFactory.findByMinionId(
                minionId);
        if (minion.isPresent()) {
            // set first action to failed if not already in that state
            Action action = ActionFactory.lookupById(actionId);
            Optional.ofNullable(action)
                    .flatMap(firstAction -> firstAction.getServerActions().stream()
                            .filter(sa -> sa.getServerId().equals(minion.get().getId()))
                            .filter(sa -> !ActionFactory.STATUS_FAILED.equals(sa.getStatus()))
                            .filter(sa -> !ActionFactory.STATUS_COMPLETED.equals(sa.getStatus()))
                            .findFirst())
                    .ifPresent(sa -> sa.fail(message.orElse("Prerequisite failed")));

            // walk dependent server actions recursively and set them to failed
            Deque<Long> actionIdsDependencies = new ArrayDeque<>();
            actionIdsDependencies.push(actionId);
            List<ServerAction> serverActions = Optional.ofNullable(action).
                    map(firstAction -> ActionFactory
                        .listServerActionsForServer(minion.get(),
                                Arrays.asList(ActionFactory.STATUS_QUEUED, ActionFactory.STATUS_PICKED_UP,
                                        ActionFactory.STATUS_FAILED), action.getCreated()))
                    .orElse(new ArrayList<>());

            while (!actionIdsDependencies.isEmpty()) {
                Long acId = actionIdsDependencies.pop();
                List<ServerAction> serverActionsWithPrereq = serverActions.stream()
                        .filter(s -> s.getParentAction().getPrerequisite() != null)
                        .filter(s -> s.getParentAction().getPrerequisite().getId().equals(acId))
                        .collect(Collectors.toList());
                for (ServerAction sa : serverActionsWithPrereq) {
                    actionIdsDependencies.push(sa.getParentAction().getId());
                    sa.fail("Prerequisite failed");
                }
            }
        }
    }

    /**
     * In action chains at this point the action is still queued so we have
     * to set it to picked up.
     * This could still lead to the race condition on when event processing is slow.
     *
     */
    private void setActionAsPickedUp(ServerAction sa) {
        sa.setStatus(ActionFactory.STATUS_PICKED_UP);
        sa.setPickupTime(new Date());
    }

    /**
     * Update the action properly based on the Job results from Salt.
     *
     * @param actionId the ID of the Action to handle
     * @param minionId the ID of the Minion who performed the action
     * @param retcode the retcode returned
     * @param success indicates if the job executed successfully
     * @param jobId the ID of the Salt job.
     * @param jsonResult the json results from the Salt job.
     * @param function the Salt function executed.
     */
    public void handleAction(long actionId, String minionId, int retcode, boolean success,
                                    String jobId, JsonElement jsonResult,
                                    Optional<Xor<String[], String>> function) {
        // Lookup the corresponding action
        Optional<Action> action = Optional.ofNullable(ActionFactory.lookupById(actionId));
        if (action.isPresent()) {

            if (LOG.isDebugEnabled()) {
                LOG.debug("Matched salt job with action (id={})", actionId);
            }

            Optional<MinionServer> minionServerOpt = MinionServerFactory.findByMinionId(minionId);
            minionServerOpt.ifPresent(minionServer -> {
                Optional<ServerAction> serverAction = action.get()
                        .getServerActions()
                        .stream()
                        .filter(sa -> sa.getServer().equals(minionServer)).findFirst();


                serverAction.ifPresent(sa -> {
                    if (LOG.isDebugEnabled()) {
                        LOG.debug("Updating action for server: {}", minionServer.getId());
                    }
                    try {
                        if (action.get().getActionType().equals(
                                ActionFactory.TYPE_REBOOT) && success && retcode == 0) {
                            // Reboot has been scheduled so set reboot action to PICKED_UP.
                            // Wait until next "minion/start/event" to set it to COMPLETED.
                            if (sa.getStatus().equals(ActionFactory.STATUS_QUEUED)) {
                                setActionAsPickedUp(sa);
                            }
                            return;
                        }
                        else if (action.get().getActionType().equals(ActionFactory.TYPE_KICKSTART_INITIATE) &&
                                success) {
                            KickstartAction ksAction = (KickstartAction) action.get();
                            if (!ksAction.getKickstartActionDetails().getUpgrade()) {
                                // Delete salt key from master
                                saltKeyUtils.deleteSaltKey(ksAction.getSchedulerUser(), minionId);
                            }
                        }
                        saltUtils.updateServerAction(sa,
                                retcode,
                                success,
                                jobId,
                                jsonResult,
                                function);
                        ActionFactory.save(sa);
                        SystemManager.updateSystemOverview(sa.getServer());
                    }
                    catch (Exception e) {
                        LOG.error("Error processing Salt job return", e);
                        // DB exceptions cause the transaction to go into rollback-only
                        // state. We need to rollback this transaction first.
                        HibernateFactory.rollbackTransaction();

                        sa.fail("An unexpected error has occurred. Please check the server logs.");

                        ActionFactory.save(sa);
                        // When we throw the exception again, the current transaction
                        // will be set to rollback-only, so we explicitly commit the
                        // transaction here
                        HibernateFactory.commitTransaction();

                        // We don't actually want to catch any exceptions
                        throw e;
                    }
                });
            });
        }
        else {
            LOG.warn("Action referenced from Salt job was not found: {}", actionId);
        }
    }

    private boolean checkIfRebootRequired(StateApplyResult<Ret<JsonElement>> actionStateApply) {
        JsonElement ret = actionStateApply.getChanges().getRet();
        if (!ret.isJsonObject()) {
            return false;
        }

        if (ret.getAsJsonObject() == null) {
            return false;
        }

        JsonPrimitive prim = ret.getAsJsonObject().getAsJsonPrimitive("reboot_required");
        if (prim == null || !prim.isBoolean()) {
            return false;

        }
        return prim.getAsBoolean();
    }

    private long checkActionID(StateApplyResult<Ret<JsonElement>> actionStateApply) {
        Ret<JsonElement> changes = actionStateApply.getChanges();
        if (changes == null) {
            return 0;
        }

        JsonElement ret = changes.getRet();
        if (ret == null || !ret.isJsonObject()) {
            return 0;
        }

        JsonObject obj = ret.getAsJsonObject();
        if (obj == null) {
            return 0;
        }

        JsonPrimitive prim = obj.getAsJsonPrimitive("current_action_id");
        if (prim == null || !prim.isNumber()) {
            return 0;

        }
        return prim.getAsLong();
    }

    /**
     * Handle action chain Salt result.
     *
     * @param minionId the minion id
     * @param jobId the job id
     * @param actionChainResult job result
     * @param skipFunction function to check if a result should be skipped from handling
     */
    public void handleActionChainResult(
            String minionId, String jobId,
            Map<String, StateApplyResult<Ret<JsonElement>>> actionChainResult,
            Function<StateApplyResult<Ret<JsonElement>>, Boolean> skipFunction) {
        int chunk = 1;
        long retActionChainId = 0L;
        boolean actionChainFailed = false;
        List<Long> failedActionIds = new ArrayList<>();
        for (Map.Entry<String, StateApplyResult<Ret<JsonElement>>> entry : actionChainResult.entrySet()) {
            String key = entry.getKey();
            StateApplyResult<Ret<JsonElement>> actionStateApply = entry.getValue();

            Optional<SaltActionChainGeneratorService.ActionChainStateId> stateId =
                    SaltActionChainGeneratorService.parseActionChainStateId(key);
            if (stateId.isPresent()) {
                retActionChainId = stateId.get().getActionChainId();
                chunk = stateId.get().getChunk();
                long actionId = stateId.get().getActionId();
                if (Boolean.TRUE.equals(skipFunction.apply(actionStateApply))) {
                    continue; // skip this state from handling
                }

                if (!actionStateApply.isResult()) {
                    actionChainFailed = true;
                    failedActionIds.add(actionId);
                    // don't stop handling the result entries if there's a failed action
                    // the result entries are not returned in order
                }
                handleAction(actionId,
                        minionId,
                        actionStateApply.isResult() ? 0 : -1,
                        actionStateApply.isResult(),
                        jobId,
                        actionStateApply.getChanges().getRet(),
                        actionStateApply.getName());
            }
            else if (key.contains("schedule_next_chunk")) {

                Optional<MinionServer> minionServerOpt = MinionServerFactory.findByMinionId(minionId);

                long actionId = checkActionID(actionStateApply);
                minionServerOpt.ifPresent(minionServer -> {

                        if (minionServer.doesOsSupportsTransactionalUpdate() &&
                                actionId != 0 && checkIfRebootRequired(actionStateApply)) {
                            /*
                             * Transactional update does not contains reboot in sls files, but apply a reboot using
                             * activate_transaction=True in transactional_update.sls . So it's required to parse
                             * the return to check if schedule_next_chunk contains reboot_required param,
                             * then we can suppose that the next action is a reboot.
                             * Then we need to pick up the action.
                             */

                            final Optional<Action> action  = Optional.ofNullable(ActionFactory.lookupById(actionId));
                            action.ifPresent(actionIn -> {
                                LOG.debug("Matched salt job with action (id={})", actionId);
                                Optional<ServerAction> serverAction = action.get()
                                        .getServerActions()
                                        .stream()
                                        .filter(sa -> sa.getServer().equals(minionServer)).findFirst();

                                serverAction.ifPresent(this::setActionAsPickedUp);
                            });
                    }
                });
            }
            else {
                LOG.warn("Could not find action id in action chain state key: {}", key);
            }
        }

        if (actionChainFailed) {
            failedActionIds.stream().min(Long::compare).ifPresent(firstFailedActionId ->
                    // Set rest of actions as FAILED due to failed prerequisite
                    failDependentServerActions(firstFailedActionId, minionId, Optional.empty())
            );
        }
        // Removing the generated SLS file
        SaltActionChainGeneratorService.INSTANCE.removeActionChainSLSFiles(
                retActionChainId, minionId, chunk, actionChainFailed);

        ActionChainFactory.getActionChain(retActionChainId).ifPresent(ac -> {
            // We need to reload server actions since saltssh will be in
            // the same db session from when the action was started and
            // won't see results of non ssh minions otherwise.
            ac.getEntries().stream()
                    .flatMap(ace -> ace.getAction().getServerActions().stream())
                    .forEach(HibernateFactory::reload);
            if (ac.isDone()) {
                ActionChainFactory.delete(ac);
            }
        });
    }

    /**
     * @param saltActionChainGeneratorServiceIn to set
     */
    public void setSaltActionChainGeneratorService(SaltActionChainGeneratorService
                                                           saltActionChainGeneratorServiceIn) {
        this.saltActionChainGeneratorService = saltActionChainGeneratorServiceIn;
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

    /**
     * Only used in unit tests.
     * @param saltUtilsIn to set
     */
    public void setSaltUtils(SaltUtils saltUtilsIn) {
        this.saltUtils = saltUtilsIn;
    }

    /**
     * Only used in unit tests.
     * @param saltApiIn to set
     */
    public void setSaltApi(SaltApi saltApiIn) {
        this.saltApi = saltApiIn;
    }

    /**
     * Only used in unit tests.
     * @param skipCommandScriptPermsIn to set
     */
    public void setSkipCommandScriptPerms(boolean skipCommandScriptPermsIn) {
        this.skipCommandScriptPerms = skipCommandScriptPermsIn;
    }

    /**
     * Only needed for unit test.
     * @param taskomaticApiIn to set
     */
    public void setTaskomaticApi(TaskomaticApi taskomaticApiIn) {
        this.taskomaticApi = taskomaticApiIn;
    }
}
