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
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.suse.manager.webui.utils;


import static com.suse.utils.Opt.flatMap;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.context.Context;

import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.utils.salt.custom.ScheduleMetadata;
import com.suse.salt.netapi.calls.modules.SaltUtil;
import com.suse.salt.netapi.calls.runner.Jobs.Info;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.results.Result;
import com.suse.utils.Json;

import com.google.gson.JsonElement;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.GroupPrincipal;
import java.nio.file.attribute.PosixFileAttributeView;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.nio.file.attribute.UserPrincipalLookupService;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TimeZone;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utilities for minion actions
 */
public class MinionActionUtils {

    private static final Logger LOG = LogManager.getLogger(MinionActionUtils.class);

    private final SaltApi saltApi;
    private final SaltUtils saltUtils;

    /**
     * Utilities for minion actions
     *
     * @param saltApiIn
     * @param saltUtilsIn
     */
    public MinionActionUtils(SaltApi saltApiIn, SaltUtils saltUtilsIn) {
        this.saltApi = saltApiIn;
        this.saltUtils = saltUtilsIn;
    }

    /**
     * Extracts the action id out of a json object like
     * ScheduleMetadata without parsing the whole object
     */
    private static final Function<JsonElement, Optional<Long>> EXTRACT_ACTION_ID =
            flatMap(Json::asLong)
                    .compose(flatMap(Json::asPrim))
                    .compose(flatMap(Json.getField(ScheduleMetadata.SUMA_ACTION_ID)))
                    .compose(Json::asObj);

    /**
    * Lookup job metadata to see if a package list refresh was requested.
    *
    * @param jobInfo job info containing the metadata
    * @return true if a package list refresh was requested, otherwise false
    */
    private boolean forcePackageListRefresh(Info jobInfo) {
        return jobInfo.getMetadata(ScheduleMetadata.class)
                        .map(ScheduleMetadata::isForcePackageListRefresh)
                        .orElse(false);
    }

    /**
     * Checks the current status of the ServerAction by looking
     * at running jobs on the minion and the job cache using the
     * action id we add to the job as metadata.
     *
     * @param serverAction ServerAction to update
     * @param running list of running jobs on the MinionServer
     * @return the updated ServerAction
     */
    public ServerAction updateMinionActionStatus(ServerAction serverAction, List<SaltUtil.RunningInfo> running) {
        long actionId = serverAction.getParentAction().getId();
        boolean actionIsRunning = running.stream().anyMatch(r ->
            r.getMetadata(JsonElement.class)
             .flatMap(EXTRACT_ACTION_ID)
             .filter(id -> id == actionId)
             .isPresent()
        );

        if (!actionIsRunning && !serverAction.isStatusQueued()) {
            String message = "No job return event was received.";
            serverAction.fail(message);
        }

        return serverAction;
    }

    private static void changeGroupAndPerms(Path dir, GroupPrincipal group) {
        PosixFileAttributeView posixAttrs = Files
                .getFileAttributeView(dir,
                        PosixFileAttributeView.class, LinkOption.NOFOLLOW_LINKS);
        try {
            Set<PosixFilePermission> wantedPers = PosixFilePermissions.fromString("rwxrwxr-x");
            if (!posixAttrs.readAttributes().permissions().equals(wantedPers)) {
                posixAttrs.setPermissions(wantedPers);
            }
        }
        catch (IOException e) {
            LOG.warn(String.format("Could not set 'rwxrwxr-x' permissions on %s: %s",
                    dir, e.getMessage()));
        }
        try {
            if (!posixAttrs.readAttributes().group().equals(group)) {
                posixAttrs.setGroup(group);
            }
        }
        catch (IOException e) {
            LOG.warn(String.format("Could not set group on %s to %s: %s",
                    dir, group, e.getMessage()));
        }
    }

    /**
     * Get the relative path of the storage directory assigned to a given org,
     * system and action.
     * @param orgId the org
     * @param systemId the system
     * @param actionId the action
     * @return the path
     */
    public static String getActionPath(Long orgId, Long systemId, Long actionId) {
        // an equivalent of rhnLib.get_action_path()
        return "systems/" + orgId + "/" + systemId + "/actions/" + actionId;
    }

    /**
     * Returns a directory specific to an org/system/action combination to store files.
     *
     * @param orgId org id
     * @param systemId system id
     * @param actionId action id
     * @return path to an org/system/action specific directory to store files
     */
    public static Path getFullActionPath(Long orgId, Long systemId, Long actionId) {
        String actionPath = getActionPath(orgId, systemId, actionId);
        Path mountPoint = Paths.get(com.redhat.rhn.common.conf.Config.get()
                .getString(ConfigDefaults.MOUNT_POINT));
        return mountPoint.resolve(actionPath);
    }

    /**
     * Creates and returns a directory specific to an org/system/action combination to store files.
     * @param minion minion
     * @param actionId action id
     * @return path to an org/system/action specific directory to store files
     *
     * @throws IOException in case of problems creating the directory or setting permissions
     */
    public static Path getActionPath(MinionServer minion, Long actionId) throws IOException {
        String actionPath = getActionPath(minion.getOrg().getId(),
                        minion.getId(), actionId);
        Path mountPoint = Paths.get(Config.get().getString(ConfigDefaults.MOUNT_POINT));
        // create dirs
        Path result = Files.createDirectories(mountPoint.resolve(actionPath));
        Path actionDir = result;

        UserPrincipalLookupService lookupService = FileSystems.getDefault().getUserPrincipalLookupService();
        GroupPrincipal susemanagerGroup = lookupService.lookupPrincipalByGroupName("susemanager");
        GroupPrincipal wwwGroup = lookupService.lookupPrincipalByGroupName("www");
        // systems/<orgId>/<serverId>/actions/<actionId>
        changeGroupAndPerms(actionDir, susemanagerGroup);
        // systems/<orgId>/<serverId>/actions
        actionDir = actionDir.getParent();
        while (!actionDir.equals(mountPoint)) {
            changeGroupAndPerms(actionDir, wwwGroup);
            actionDir = actionDir.getParent();
        }

        return result;
    }

    /**
     * Cleanup all minion actions for which we missed the JobReturnEvent
     *
     */
    public void cleanupMinionActions() {
        ZonedDateTime now = ZonedDateTime.now();
        // Select only ServerActions that are for minions and where the Action
        // should already be executed or running
        List<ServerAction> serverActions =
            ActionFactory.pendingMinionServerActions().stream().flatMap(a -> {
                    if (a.getEarliestAction().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .isBefore(now.minusHours(1))) {
                        return a.getServerActions()
                                .stream()
                                .filter(sa -> sa.getServer().asMinionServer().isPresent() &&
                                        // Do not clean up SSH push tasks
                                        sa.getServer().getContactMethod().getLabel()
                                        .equals("default"));
                    }
                    else {
                        return Stream.empty();
                    }
                }
            ).toList();

        List<String> minionIds = serverActions.stream().flatMap(sa ->
                sa.getServer().asMinionServer()
                        .map(MinionServer::getMinionId).stream()
        ).collect(Collectors.toList());

        Map<String, Result<List<SaltUtil.RunningInfo>>> running =
                saltApi.running(new MinionList(minionIds));

        serverActions.forEach(serverAction ->
            serverAction.getServer().asMinionServer().map(minion -> running.get(minion.getMinionId()))
              .ifPresent(r -> {
                  r.consume(error -> LOG.error(error.toString()),
                  runningInfos -> ActionFactory.save(updateMinionActionStatus(serverAction, runningInfos)));
              })
        );
    }

    /**
     * Delete script files corresponding to script run actions.
     * @throws IOException in case of problems listing the scripts
     */
    public void cleanupScriptActions() throws IOException {
        Path scriptsDir = saltUtils.getScriptsDir();
        if (Files.isDirectory(scriptsDir)) {
            Pattern p = Pattern.compile("script_(\\d*).sh");
            try (Stream<Path> pathStream = Files.list(scriptsDir)) {
                pathStream.forEach(file -> {
                    Matcher m = p.matcher(file.getFileName().toString());
                    if (m.find()) {
                        long actionId = Long.parseLong(m.group(1));
                        Action action = ActionFactory.lookupById(actionId);
                        if (action == null || action.allServersFinished()) {
                            LOG.info("Deleting script file: {}", file);
                            FileUtils.deleteFile(file);
                        }
                    }
                });
            }
        }
    }

    /**
     * Compute the schedule date of an action.
     *
     * @param earliest the earliest locat date time to execute the action, may be <code>null</code>
     * @return the date to run the action
     */
    public static Date getScheduleDate(Optional<LocalDateTime> earliest) {
        ZoneId zoneId = Optional.ofNullable(Context.getCurrentContext().getTimezone())
                .orElse(TimeZone.getDefault()).toZoneId();
        return Date.from(earliest.orElseGet(LocalDateTime::now).atZone(zoneId).toInstant());
    }

    /**
     * Compute the action chain
     * @param actionChain the action chain, may be <code>null</code>
     * @param user the current user
     * @return the {@link ActionChain} object or null if no action chain is defined
     */
    public static ActionChain getActionChain(Optional<String> actionChain, User user) {
        return actionChain.filter(StringUtils::isNotEmpty)
                          .map(label -> ActionChainFactory.getOrCreateActionChain(label, user))
                          .orElse(null);
    }
}
