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
package com.suse.manager.webui.utils;


import static com.suse.utils.Opt.flatMap;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.util.FileUtils;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionServer;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.suse.manager.reactor.messaging.JobReturnEventMessageAction;
import com.suse.manager.utils.SaltUtils;
import com.suse.manager.webui.services.SaltActionChainGeneratorService;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.utils.salt.custom.ScheduleMetadata;
import com.suse.salt.netapi.calls.modules.SaltUtil;
import com.suse.salt.netapi.calls.runner.Jobs;
import com.suse.salt.netapi.calls.runner.Jobs.Info;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.results.Result;
import com.suse.salt.netapi.results.Ret;
import com.suse.salt.netapi.results.StateApplyResult;
import com.suse.utils.Json;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utilities for minion actions
 */
public class MinionActionUtils {

    private static final Logger LOG = Logger.getLogger(MinionActionUtils.class);

    /** Whether the current database is Postgres. */
    public static final boolean POSTGRES = ConfigDefaults.get().isPostgresql();

    private MinionActionUtils() {
    }

    /**
     * Extracts the action id out of a json object like
     * ScheduleMetadata without parsing the whole object
     */
    public static final Function<JsonElement, Optional<Long>> EXTRACT_ACTION_ID =
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
    private static boolean forcePackageListRefresh(Info jobInfo) {
        return jobInfo.getMetadata(ScheduleMetadata.class)
                        .map(ScheduleMetadata::isForcePackageListRefresh)
                        .orElse(false);
    }

    /**
     * Checks the current status of the ServerAction by looking
     * at running jobs on the minion and the job cache using the
     * action id we add to the job as metadata.
     *
     * @param salt the salt service to use
     * @param sa ServerAction to update
     * @param server MinionServer of this ServerAction
     * @param running list of running jobs on the MinionServer
     * @param infoMap map from actionIds to Salt job information objects
     * @return the updated ServerAction
     */
    public static ServerAction updateMinionActionStatus(SaltService salt, ServerAction sa,
            MinionServer server, List<SaltUtil.RunningInfo> running,
            Map<Long, Optional<Info>> infoMap) {
        long actionId = sa.getParentAction().getId();
        boolean actionIsRunning = running.stream().filter(r ->
                r.getMetadata(JsonElement.class)
                        .flatMap(EXTRACT_ACTION_ID)
                        .filter(id -> id == actionId)
                        .isPresent()
        ).findFirst().isPresent();

        if (!actionIsRunning) {
            ServerAction serverAction = infoMap.get(actionId)
                    .map(info -> {
                        Optional<JsonElement> result = info
                                .getResult(server.getMinionId(), JsonElement.class);
                            // the result should only be missing if its still running
                            // since we know at this point that its not running result
                            // being empty means something went horribly wrong.
                        return result.map(o -> {
                            // If it is a string its likely to be an error because our
                            // actions so far don't have String as a result type
                            if (o.isJsonPrimitive() && o.getAsJsonPrimitive().isString()) {
                                sa.fail(o.getAsJsonPrimitive().getAsString());
                                return sa;
                            }
                            else {
                                SaltUtils.INSTANCE.updateServerAction(sa, 0L,
                                        true, info.getJid(), o, info.getFunction());
                                SaltUtils.handlePackageChanges(info.getFunction(), o,
                                        server);
                                return sa;
                            }
                        }).orElseGet(() -> {
                            sa.fail("There was no result.");
                            return sa;
                        });
                    }).orElseGet(() -> {
                        if (!sa.getStatus().equals(ActionFactory.STATUS_QUEUED)) {
                            String message = "No job return event was received.";
                            if (!POSTGRES) {
                                message = "There was no job cache entry.";
                            }
                            sa.fail(message);
                        }
                        return sa;
                    });
            return serverAction;
        }
        else {
            return sa;
        }
    }

    /**
     * Cleanup all minion actions for which we missed the JobReturnEvent
     *
     * @param salt the salt service to use
     */
    public static void cleanupMinionActions(SaltService salt) {
        ZonedDateTime now = ZonedDateTime.now();
        // Select only ServerActions that are for minions and where the Action
        // should already be executed or running
        List<ServerAction> serverActions =
            ActionFactory.pendingMinionServerActions().stream().flatMap(a -> {
                    if (a.getEarliestAction().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .isBefore(now.minus(5, ChronoUnit.MINUTES))) {
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
            ).collect(Collectors.toList());

        List<String> minionIds = serverActions.stream().flatMap(sa ->
                sa.getServer().asMinionServer()
                        .map(MinionServer::getMinionId)
                        .map(Stream::of)
                        .orElseGet(Stream::empty)
        ).collect(Collectors.toList());

        Map<String, Result<List<SaltUtil.RunningInfo>>> running =
                salt.running(new MinionList(minionIds));

        Map<Long, Optional<Jobs.Info>> infoMap = serverActions.stream()
          .map(sa -> sa.getParentAction().getId())
          .distinct()
          .collect(toMap(identity(), id -> infoForActionId(salt, id)));

        serverActions.forEach(sa ->
                sa.getServer().asMinionServer().ifPresent(minion -> {
                    Optional.ofNullable(running.get(minion.getMinionId())).ifPresent(r -> {
                        r.consume(error -> {
                            LOG.error(error.toString());
                        },
                        runningInfos -> {
                            ActionFactory.save(updateMinionActionStatus(
                                    salt, sa, minion, runningInfos, infoMap));
                        });
                    });
                })
        );
    }

    /**
     * Returns the Salt job information object for a SUSE Manager actionId.
     *
     * @param salt the salt service to use
     * @param actionId the actionId
     * @return an optional job information object
     */
    private static Optional<Info> infoForActionId(SaltService salt, long actionId) {
        // if we are running on Postgres, there is no need to check Salt's job cache as job return events are already
        // stored persistently via the database (see PGEventStream)
        if (POSTGRES) {
            return Optional.empty();
        }

        Map<String, Object> metadata = new HashMap<>();
        metadata.put(ScheduleMetadata.SUMA_ACTION_ID, actionId);
        Optional<String> jid = salt.jobsByMetadata(metadata)
                .map(info -> info.keySet().stream().findFirst())
                .orElse(Optional.empty());
        return jid.flatMap(id -> salt.listJob(id));
    }

    /**
     * Cleans up Action Chain records.
     * @param salt a SaltService instance
     */
    public static void cleanupMinionActionChains(SaltService salt) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(ScheduleMetadata.SUMA_ACTION_CHAIN, true);

        // get only jobs since the last cleanup
        LocalDateTime startTime = LocalDateTime.now()
                .minus(1, ChronoUnit.HOURS);

        Optional<Map<String, Jobs.ListJobsEntry>> actionChainsJobs =
                salt.jobsByMetadata(metadata, startTime, LocalDateTime.now());

        actionChainsJobs.ifPresent(jidsMap -> {
            jidsMap.keySet().forEach(jid -> {
                salt.listJob(jid).ifPresent(jobInfo -> {
                    TypeToken<Map<String, StateApplyResult<Ret<JsonElement>>>> typeToken =
                            new TypeToken<Map<String, StateApplyResult<Ret<JsonElement>>>>() { };

                    jobInfo.getMinions().forEach(minionId -> {
                        try {
                            Optional<Map<String, StateApplyResult<Ret<JsonElement>>>> jobResult =
                                    jobInfo.getResult(minionId, typeToken);

                            jobResult.ifPresent(res -> {
                                res.entrySet().stream().forEach(e -> {

                                    Optional<SaltActionChainGeneratorService.ActionChainStateId> stateId =
                                            SaltActionChainGeneratorService.parseActionChainStateId(e.getKey());

                                    if (stateId.isPresent()) {
                                        long retActionId = stateId.get().getActionId();

                                        Optional<ServerAction> serverAction =
                                                Optional.ofNullable(ActionFactory.lookupById(retActionId))
                                                .flatMap(action -> action
                                                        .getServerActions()
                                                        .stream()
                                                        .filter(sa -> sa.getServer().asMinionServer().isPresent())
                                                        .filter(sa -> sa.getServer().asMinionServer().get()
                                                                .getMinionId().equals(minionId)).findFirst());

                                        if (serverAction.isPresent() &&
                                                (ActionFactory.STATUS_COMPLETED
                                                        .equals(serverAction.get().getStatus()) ||
                                                ActionFactory.STATUS_FAILED
                                                        .equals(serverAction.get().getStatus()))) {
                                            return;
                                        }

                                        StateApplyResult<Ret<JsonElement>> stateApplyResult = e.getValue();
                                        JobReturnEventMessageAction.handleAction(retActionId,
                                                minionId,
                                                stateApplyResult.isResult() ? 0 : -1,
                                                stateApplyResult.isResult(),
                                                jid,
                                                stateApplyResult.getChanges().getRet(),
                                                stateApplyResult.getName()
                                        );
                                    }
                                });
                            });

                        }
                        catch (JsonSyntaxException e) {
                            // expected, not all jobInfos will have a state apply result
                            LOG.debug("Could not get result from job " + jid + ": " + e.getMessage());
                        }
                    });
                });
            });
        });
    }

    /**
     * Delete script files corresponding to script run actions.
     * @throws IOException in case of problems listing the scripts
     */
    public static void cleanupScriptActions() throws IOException {
        Path scriptsDir = SaltUtils.INSTANCE.getScriptsDir();
        if (Files.isDirectory(scriptsDir)) {
            Pattern p = Pattern.compile("script_(\\d*).sh");
            Files.list(scriptsDir).forEach(file -> {
                Matcher m = p.matcher(file.getFileName().toString());
                if (m.find()) {
                    long actionId = Long.parseLong(m.group(1));
                    Action action = ActionFactory.lookupById(actionId);
                    if (action == null || action.allServersFinished()) {
                        LOG.info("Deleting script file: " + file);
                        FileUtils.deleteFile(file);
                    }
                }
            });
        }
    }
}
