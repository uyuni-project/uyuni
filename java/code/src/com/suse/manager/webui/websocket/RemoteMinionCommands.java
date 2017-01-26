package com.suse.manager.webui.websocket;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.servlets.LocalizedEnvironmentFilter;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.websocket.json.*;
import com.suse.salt.netapi.datatypes.target.Glob;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.salt.netapi.results.Result;
import com.suse.utils.Json;
import org.apache.log4j.Logger;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Created by matei on 9/26/16.
 */
@ServerEndpoint(value = "/websocket/minion/remote-commands", configurator = ServletAwareConfig.class)
public class RemoteMinionCommands {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(RemoteMinionCommands.class);

    private static final SaltService SALT_SERVICE = SaltService.INSTANCE;

    private long userId;

    @OnOpen
    public void onStart(Session session, EndpointConfig config) {
        this.userId = LocalizedEnvironmentFilter.USERID.get();
    }

    @OnClose
    public void onClose() {
        LOG.debug("Websocket closed");
        // TODO cancel the JobReturnStream (if any)
    }

    @OnMessage
    public void onMessage(Session session, String messageBody) {
        User user = UserFactory.lookupById(this.userId);

        List<String> allVisibleMinions = MinionServerFactory
                .lookupVisibleToUser(user)
                .map(MinionServer::getMinionId)
                .collect(Collectors.toList());

        RemoteMinionCommandDto msg = Json.GSON.fromJson(messageBody, RemoteMinionCommandDto.class);
        CompletableFuture failAfter = SALT_SERVICE.failAfter(20); // TODO remove hardcoding
        if (msg.isPreview()) {

            Map<String, CompletionStage<Result<Boolean>>> res = SALT_SERVICE
                    .matchAsync(msg.getTarget(), failAfter);

            List<String> allMinions = res.keySet().stream().collect(Collectors.toList());
            allMinions.retainAll(allVisibleMinions);
            sendMessage(session, new AsyncJobStartEventDto("preview", allMinions));

            res.forEach((minionId, future) -> {
                future.whenComplete((matchResult, err) -> {
                    if (matchResult != null) {
                        sendMessage(session, new MinionMatchEventDto(minionId));
                    }
                    if (err != null) {
                        if (err instanceof TimeoutException) {
                            sendMessage(session, new ActionTimedOutEventDto(minionId, "preview"));
                            LOG.debug("Timed out waiting response from minion " + minionId);
                        } else {
                            LOG.error("Error waiting for minion " + minionId, err);
                        }
                    }
                });
            });
        } else {
            try {
                Map<String, CompletionStage<Result<String>>> res = SALT_SERVICE
                        .completableRemoteCommandAsync(new Glob(msg.getTarget()), msg.getCommand(), failAfter);

                List<String> allMinions = res.keySet().stream().collect(Collectors.toList());
                sendMessage(session, new AsyncJobStartEventDto("run", allMinions));

                res.forEach((minionId, future) -> {
                    future.whenComplete((cmdResult, err) -> {
                        if (cmdResult != null) {
                            sendMessage(session, new MinionCmdRunResult(minionId,
                                    cmdResult.result().orElse(""))); // TODO handle empty result
                        }
                        if (err != null) {
                            if (err instanceof TimeoutException) {
                                sendMessage(session, new ActionTimedOutEventDto(minionId, "run"));
                                LOG.debug("Timed out waiting response from minion '" + minionId + "'");
                            } else {
                                LOG.error("Error waiting for minion " + minionId, err);
                                // TODO send err msg
                            }
                        }
                    });
                });
            } catch (SaltException e) {
                LOG.error("Error executing Salt async remote command", e);
            }
        }
    }

    /**
     * Must be synchronized. Sending messages concurrently from separate threads
     * will result in IllegalStateException.
     */
    private synchronized void sendMessage(Session session, RemoteSaltCommandEventDto dto) {
        try {
            session.getBasicRemote().sendText(Json.GSON.toJson(dto));
        } catch (IOException e) {
            LOG.error("Error sending websocket message", e);
        }
    }

    @OnError
    public void onError(Session session, Throwable err) {
        LOG.error("Websocket endpoint error", err);
    }

}
