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

    private static final SaltService SALT_SERVICE = SaltService.INSTANCE;

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(RemoteMinionCommands.class);

    private Session session;
    private HttpSession httpSession;
    private long userId;


    @OnOpen
    public void onStart(Session session, EndpointConfig config) {
        this.session = session;
        this.httpSession = (HttpSession) config.getUserProperties().get("httpSession");
        this.userId = LocalizedEnvironmentFilter.USERID.get();

    }

    @OnClose
    public void onClose() {
        LOG.info("closed");
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
            sendMessage(session, new AsyncJobStartEventDto("preview", "234", allVisibleMinions));

            List<CompletableFuture> timedFutures = new LinkedList<>();
            CompletableFuture failAfter = SALT_SERVICE.failAfter(timeout);
            res.forEach((minionId, future) -> {
                CompletableFuture<Void> timedFuture = future.acceptEitherAsync(failAfter, (cmdResult) -> {
                    sendMessage(session, new MinionMatchEventDto(minionId));
                }).exceptionally((err) -> {
                    // TODO check if really timeout
                    sendMessage(session, new ActionTimedOutEventDto(minionId, "preview"));
                    LOG.info("timed out for minion " + minionId + " e=" + err);
                    return null;
                }).whenComplete((r, e) -> {
                    LOG.info("done for minion " + minionId + " e=" + e);
                    future.cancel(true);
                });
                timedFutures.add(timedFuture);
            });

//                    .setTimeout(timeout)
//                    .onInit((jid, minions) -> {
//                        try {
//                            List<String> allMinions = new ArrayList<>(minions);
//                            allMinions.retainAll(allVisibleMinions);
//                            session.getBasicRemote().sendText(Json.GSON.toJson(new AsyncJobStartEventDto("preview", jid, allMinions)));
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//                    })
//                    .onEvent((e) -> {
//                        if (!allVisibleMinions.contains(e.getMinionId())) {
//                            return;
//                        }
//                        try {
//                            session.getBasicRemote().sendText(Json.GSON.toJson(new MinionMatchEventDto(e.getMinionId())));
//                        } catch (IOException e1) {
//                            e1.printStackTrace();
//                        }
//                    })
//                    .onComplete((e) -> {
//                        try {
//                            session.getBasicRemote().sendText(Json.GSON.toJson(new ActionDoneEventDto("preview")));
//                        } catch (IOException ex) {
//                            ex.printStackTrace();
//                        }
//                    })
//                    .onTimeout(() -> {
//                        try {
//                            session.getBasicRemote().sendText(Json.GSON.toJson(new ActionTimedOutEventDto("preview")));
//                        } catch (IOException ex) {
//                            ex.printStackTrace();
//                        }
//                    });
        } else {


            try {
                Map<String, CompletableFuture<String>> res = SALT_SERVICE.completableRemoteCommandAsync(new Glob(msg.getTarget()), msg.getCommand()/*, 20*/);

                sendMessage(session, new AsyncJobStartEventDto("run", "234",
                        res.keySet().stream().collect(Collectors.toList())));

                List<CompletableFuture> timedFutures = new LinkedList<>();
                CompletableFuture failAfter = SALT_SERVICE.failAfter(timeout);
                res.forEach((minionId, future) -> {
                    CompletableFuture<Void> timedFuture = future.acceptEitherAsync(failAfter, (cmdResult) -> {
                        sendMessage(session, new MinionCmdRunResult(minionId, cmdResult));
                    }).exceptionally((err) -> {
                        // TODO check if really timeout
                        sendMessage(session, new ActionTimedOutEventDto(minionId, "run"));
                        LOG.info("timed out for minion " + minionId + " e=" + err);
                        return null;
                    }).whenComplete((r, e) -> {
                        LOG.info("done for minion " + minionId + " e=" + e);
                        future.cancel(true);
                    });
                    timedFutures.add(timedFuture);
                });

                // TODO is this really needed ?
                CompletableFuture.allOf(timedFutures.toArray(new CompletableFuture[timedFutures.size()]))
                        .handleAsync((r, err) -> {
                            LOG.info("all done " + r + " " + err);
                            sendMessage(session, new ActionDoneEventDto("run"));
                            return r;
                        });

            } catch (SaltException e) {
                LOG.error("", e);
            }
        }
    }

    private void sendMessage(Session session, RemoteSaltCommandEventDto dto) {
        LOG.info("send to websocket " + dto.getMinion());
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
