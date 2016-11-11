package com.suse.manager.webui.websocket;

import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.servlets.LocalizedEnvironmentFilter;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.websocket.json.*;
import com.suse.salt.netapi.datatypes.target.Glob;
import com.suse.salt.netapi.event.JobReturnEvent;
import com.suse.salt.netapi.exception.SaltException;
import com.suse.utils.Json;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
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
        int timeout = 20;
        if (msg.isPreview()) {
            Map<String, CompletableFuture<Boolean>> res = SALT_SERVICE.matchAsync(msg.getTarget());

            List<String> allMinions = new ArrayList<>(res.keySet().stream().collect(Collectors.toList()));
            allMinions.retainAll(allVisibleMinions);
            sendMessage(session, new AsyncJobStartEventDto("preview", allMinions));

            CompletableFuture failAfter = SALT_SERVICE.failAfter(timeout);
             res.forEach((minionId, future) -> {
                future.acceptEitherAsync(failAfter, (cmdResult) -> {
                    sendMessage(session, new MinionMatchEventDto(minionId));
                }).exceptionally((err) -> {
                    if (err.getCause() instanceof TimeoutException) {
                        sendMessage(session, new ActionTimedOutEventDto(minionId, "preview"));
                        LOG.debug("Timed out waiting response from minion " + minionId);
                    } else {
                        LOG.error("Error waiting for minion " + minionId, err);
                    }
                    return null;
                }).whenComplete((r, e) -> {
                    future.cancel(true);
                });
            });
        } else {
            try {
                Map<String, CompletableFuture<String>> res = SALT_SERVICE
                        .completableRemoteCommandAsync(new Glob(msg.getTarget()), msg.getCommand());

                sendMessage(session, new AsyncJobStartEventDto("run",
                        res.keySet().stream().collect(Collectors.toList())));

                CompletableFuture failAfter = SALT_SERVICE.failAfter(timeout);
                res.forEach((minionId, future) -> {
                    future.acceptEitherAsync(failAfter, (cmdResult) -> {
                        sendMessage(session, new MinionCmdRunResult(minionId, cmdResult));
                    }).exceptionally((err) -> {
                        if (err.getCause() instanceof TimeoutException) {
                            sendMessage(session, new ActionTimedOutEventDto(minionId, "run"));
                            LOG.debug("Timed out waiting response from minion '" + minionId + "'");
                        } else {
                            LOG.error("Error waiting for minion " + minionId, err);
                        }
                        return null;
                    }).whenComplete((r, e) -> {
                        // completed normally or timed out
                        future.cancel(true);
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
