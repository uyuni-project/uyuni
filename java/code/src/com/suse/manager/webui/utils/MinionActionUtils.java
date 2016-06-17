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


import com.google.gson.JsonElement;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.suse.manager.reactor.messaging.JobReturnEventMessageAction;
import com.suse.manager.webui.services.SaltService;
import com.suse.manager.webui.utils.salt.custom.ScheduleMetadata;
import com.suse.salt.netapi.calls.modules.SaltUtil;
import com.suse.salt.netapi.calls.runner.Jobs;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.results.Result;
import com.suse.utils.Json;
import org.apache.log4j.Logger;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.suse.utils.Opt.flatMap;

/**
 * Utilities for minion actions
 */
public class MinionActionUtils {

    private static final Logger LOG = Logger.getLogger(MinionActionUtils.class);

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
     * Checks the current status of the ServerAction by looking
     * at running jobs on the minion and the job cache using the
     * action id we add to the job as metadata.
     *
     * @param salt the salt service to use
     * @param sa ServerAction to update
     * @param server MinionServer of this ServerAction
     * @param running list of running jobs on the MinionServer
     * @return the updated ServerAction
     */
    public static ServerAction updateMinionActionStatus(SaltService salt, ServerAction sa,
            MinionServer server, List<SaltUtil.RunningInfo> running) {
        long actionId = sa.getParentAction().getId();
        boolean actionIsRunning = running.stream().filter(r ->
                r.getMetadata(JsonElement.class)
                        .flatMap(EXTRACT_ACTION_ID)
                        .filter(id -> id == actionId)
                        .isPresent()
        ).findFirst().isPresent();

        if (!actionIsRunning) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put(ScheduleMetadata.SUMA_ACTION_ID, actionId);
            Map<String, Jobs.ListJobsEntry> jobCache =
                    salt.jobsByMetadata(metadata);
            ServerAction serverAction = jobCache.entrySet().stream().findFirst()
                    .map(entry -> {
                        String jid = entry.getKey();
                        Jobs.Info job = salt.listJob(jid);
                        Optional<JsonElement> result = job
                                .getResult(server.getMinionId(), JsonElement.class);
                        // the result should only be missing if its still running
                        // since we know at this point that its not running result
                        // being empty means something went horribly wrong.
                        return result.map(o -> {
                            // If it is a string its likely to be an error because our
                            // actions so far don't have String as a result type
                            if (o.isJsonPrimitive() && o.getAsJsonPrimitive().isString()) {
                                String error = o.getAsJsonPrimitive().getAsString();
                                sa.setCompletionTime(new Date());
                                sa.setResultMsg(error);
                                sa.setStatus(ActionFactory.STATUS_FAILED);
                                sa.setResultCode(-1L);
                                return sa;
                            }
                            else {
                                JobReturnEventMessageAction.updateServerAction(sa, 0L,
                                        true, jid, o, job.getFunction());
                                return sa;
                            }
                        }).orElseGet(() -> {
                            sa.setCompletionTime(new Date());
                            sa.setResultMsg("There was no result.");
                            sa.setStatus(ActionFactory.STATUS_FAILED);
                            sa.setResultCode(-1L);
                            return sa;
                        });
                    }).orElseGet(() -> {
                        sa.setCompletionTime(new Date());
                        sa.setResultMsg("There was no job cache entry.");
                        sa.setStatus(ActionFactory.STATUS_FAILED);
                        sa.setResultCode(-1L);
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
                                .filter(sa -> sa.getServer().asMinionServer().isPresent());
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
        serverActions.stream().forEach(sa ->
                sa.getServer().asMinionServer().ifPresent(minion -> {
                    Optional.ofNullable(running.get(minion.getMinionId())).ifPresent(r -> {
                        r.consume(error -> {
                            LOG.error(error.toString());
                        },
                        runningInfos -> {
                            ActionFactory.save(updateMinionActionStatus(
                                    salt, sa, minion, runningInfos));
                        });
                    });
                })
        );
    }
}
