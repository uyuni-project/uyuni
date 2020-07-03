/**
 * Copyright (c) 2020 SUSE LLC
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

package com.suse.manager.reactor.messaging;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionSaltRunnerJob;
import com.redhat.rhn.domain.action.cluster.ClusterRemoveNodeAction;
import com.redhat.rhn.domain.product.Tuple2;
import com.suse.manager.webui.utils.salt.custom.ExecResult;
import com.suse.manager.webui.utils.salt.custom.RunnerRemoveClusterNodeSlsResult;
import com.suse.manager.webui.services.impl.runner.RunnerResult;
import com.suse.salt.netapi.event.RunnerReturnEvent;
import com.suse.utils.Json;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class RunnerReturnEventMessageAction implements MessageAction {

    private static final Logger LOG = Logger.getLogger(RunnerReturnEventMessageAction.class);

    private static final Pattern REMOVE_NODE =
            Pattern.compile("module_\\|-mgr_cluster_remove_node_(.*)_\\|-mgrclusters\\.remove_node_\\|-run");

    @Override
    public void execute(EventMessage msg) {
        RunnerReturnEventMessage eventMessage = (RunnerReturnEventMessage)msg;
        RunnerReturnEvent runnerReturnEvent = eventMessage.getRunnerReturnEvent();

        String jobId = runnerReturnEvent.getJobId();

        Optional<ActionSaltRunnerJob> runnerJob = ActionFactory.lookupSaltRunnerJobByJid(jobId);

        runnerJob.ifPresentOrElse(job -> {
            Object ret = runnerReturnEvent.getData().getResult();
            Optional<JsonElement> jobResult = eventToJson(runnerReturnEvent);
            jobResult.ifPresent(result -> {
                handleAction(job, result);
            });
        },
                () -> {
                    LOG.error("ActionSaltRunnerJob for jid=" + jobId + " not found");
                });

    }

    private void handleAction(ActionSaltRunnerJob job, JsonElement jsonResult) {
        Action action = job.getAction();
        RunnerResult result = Json.GSON.fromJson(jsonResult, RunnerResult.class);
        // only one entry should be present. ignore the kye
        Optional<JsonElement> resultData = result.getData().values().stream().findFirst();
        resultData.ifPresentOrElse(data -> {
            if (ActionFactory.TYPE_CLUSTER_REMOVE_NODE.equals(action.getActionType())) {
                handleClusterRemoveNodes(job, data);
            }
        },
                () -> {
                    LOG.error("Return data missing for action id=" + job.getAction().getId());
                    job.setStatus(ActionFactory.STATUS_FAILED);
                    job.setCompletionTime(new Date());
                    job.setResultCode(-1L);
                    job.setResultMsg("Return data missing");
                });

    }

    private void handleClusterRemoveNodes(ActionSaltRunnerJob job, JsonElement data) {
        ClusterRemoveNodeAction action = (ClusterRemoveNodeAction)job.getAction();
        RunnerRemoveClusterNodeSlsResult slsResult = Json.GSON.fromJson(data, RunnerRemoveClusterNodeSlsResult.class);
        var removeResult = slsResult.getRemoveNodes();
        Optional.ofNullable(removeResult.getChanges().getRet()
                .get(action.getCluster().getManagementNode().getMinionId()))
                .ifPresentOrElse(map -> {
                    Map<String, Tuple2<Boolean, Object>> res = map.entrySet().stream()
                            .filter(entry-> REMOVE_NODE.matcher(entry.getKey()).matches())
                            .collect(Collectors.toMap(
                                    entry -> {
                                        Matcher matcher = REMOVE_NODE.matcher(entry.getKey());
                                        if (matcher.find()) {
                                            return matcher.group(1);
                                        }
                                        else {
                                            return entry.getKey();
                                        }
                                    },
                                    entry -> {
                                        try {
                                            ExecResult execResult =
                                                    Json.GSON.fromJson(entry.getValue().getChanges().getRet(),
                                                            ExecResult.class);
                                            return new Tuple2<>(execResult.getSuccess(), execResult);
                                        }
                                        catch (JsonSyntaxException e) {
                                            return new Tuple2<>(false,
                                                    Collections.singletonMap("error", entry.getValue().getComment()));
                                        }
                                    }
                            ));

                    boolean success = res.values().stream().allMatch(t -> t.getA());

                    job.setStatus(success ? ActionFactory.STATUS_COMPLETED : ActionFactory.STATUS_FAILED);
                    job.setCompletionTime(new Date());
                    job.setResultCode(success ? 0L : 1L);
                    job.setResultMsg(Json.GSON.toJson(res.entrySet().stream()
                            .collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue().getB()))));
                }
                ,
                        () -> {
                            job.setStatus(ActionFactory.STATUS_FAILED);
                            job.setCompletionTime(new Date());
                            job.setResultCode(0L);
                            job.setResultMsg("Return data from cluster management node is missing");
                        });




    }

    // TODO copied from JobReturnEventMessageAction
    private static Optional<JsonElement> eventToJson(RunnerReturnEvent jobReturnEvent) {
        Optional<JsonElement> jsonResult = Optional.empty();
        try {
            jsonResult = Optional.ofNullable(
                    jobReturnEvent.getData().getResult(JsonElement.class));
        }
        catch (JsonSyntaxException e) {
            LOG.error("JSON syntax error while decoding into a StateApplyResult:");
            LOG.error(jobReturnEvent.getData().getResult(JsonElement.class).toString());
        }
        return jsonResult;
    }

    // TODO copied from JobReturnEventMessageAction
    private String  getJsonResultWithPrettyPrint(JsonElement jsonResult) {
        Object returnObject = Json.GSON.fromJson(jsonResult, Object.class);
        Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
        return gson.toJson(returnObject);
    }


}
