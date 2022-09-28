/*
 * Copyright (c) 2017 SUSE LLC
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
package com.suse.manager.webui.websocket;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.session.WebSession;
import com.redhat.rhn.domain.session.WebSessionFactory;
import com.redhat.rhn.frontend.events.TransactionHelper;
import com.redhat.rhn.frontend.servlets.LocalizedEnvironmentFilter;
import com.redhat.rhn.manager.system.SystemManager;

import com.suse.manager.maintenance.MaintenanceManager;
import com.suse.manager.webui.services.FutureUtils;
import com.suse.manager.webui.services.iface.SaltApi;
import com.suse.manager.webui.websocket.json.AbstractSaltEventDto;
import com.suse.manager.webui.websocket.json.ActionErrorEventDto;
import com.suse.manager.webui.websocket.json.ActionTimedOutEventDto;
import com.suse.manager.webui.websocket.json.AsyncJobStartEventDto;
import com.suse.manager.webui.websocket.json.ExecuteMinionActionDto;
import com.suse.manager.webui.websocket.json.MinionCommandResultEventDto;
import com.suse.manager.webui.websocket.json.MinionMatchResultEventDto;
import com.suse.manager.webui.websocket.json.SSHMinionMatchResultDto;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.errors.JsonParsingError;
import com.suse.salt.netapi.errors.SaltError;
import com.suse.salt.netapi.results.Result;
import com.suse.utils.Json;

import com.google.gson.JsonObject;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.EOFException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

/**
 * Websocket endpoint for executing remote commands on Salt minions.
 * NOTE: there's an endpoint instance for each websocket session
 */
@ServerEndpoint(value = "/websocket/minion/remote-commands")
public class RemoteMinionCommands {

    // Logger for this class
    private static final Logger LOG = LogManager.getLogger(RemoteMinionCommands.class);

    private Long sessionId;

    private MaintenanceManager mm = new MaintenanceManager();
    private CompletableFuture failAfter;
    private List<String> previewedMinions;
    private static ExecutorService eventHistoryExecutor = Executors.newCachedThreadPool();
    private static SaltApi saltApi = GlobalInstanceHolder.SALT_API;
    private static final WebsocketHeartbeatService HEARTBEAT_SERVICE = GlobalInstanceHolder.WEBSOCKET_SESSION_MANAGER;

    /**
     * Callback executed when the websocket is opened.
     * @param session the websocket session
     */
    @OnOpen
    public void onOpen(Session session) {
        this.sessionId = LocalizedEnvironmentFilter.getCurrentSessionId();
        if (this.sessionId == null) {
            try {
                LOG.debug("No web sessionId available. Closing the web socket.");
                session.close();
            }
            catch (IOException e) {
                LOG.debug("Error closing web socket session", e);
            }
        }
        else {
            HEARTBEAT_SERVICE.register(session);
        }
    }

    /**
     * Callback executed when the websocket is closed.
     * @param session the websocket session
     */
    @OnClose
    public void onClose(Session session) {
        LOG.debug("Closing web socket session");
        HEARTBEAT_SERVICE.unregister(session);
        if (this.failAfter != null) {
            this.failAfter.completeExceptionally(
                    new TimeoutException("Canceled waiting because of websocket close"));
        }
    }

    /**
     * Callback executed when a message is received.
     * @param session the websocket session
     * @param messageBody the message as string
     */
    @OnMessage
    public void onMessage(Session session, String messageBody) {
        ExecuteMinionActionDto msg = Json.GSON.fromJson(
                messageBody, ExecuteMinionActionDto.class);
        try {
            WebSession webSession = WebSessionFactory.lookupById(sessionId);

            if (invalidWebSession(session, webSession)) {
                return;
            }

            int timeOut = 600; // 10 minutes

            if (msg.isPreview()) {
                List<String> allVisibleMinions = MinionServerFactory
                        .lookupVisibleToUser(webSession.getUser())
                        .filter(m -> mm.isSystemInMaintenanceMode(m))
                        .map(MinionServer::getMinionId)
                        .collect(Collectors.toList());

                this.failAfter = FutureUtils.failAfter(timeOut);

                String target = StringUtils.trim(msg.getTarget());

                Optional<CompletionStage<Map<String, Result<Boolean>>>> resSSH =
                        saltApi.matchAsyncSSH(target, failAfter);

                Map<String, CompletionStage<Result<Boolean>>> res = new HashMap<>();

                res = saltApi.matchAsync(target, failAfter);
                if (res.isEmpty() && !resSSH.isPresent()) {
                    // just return, no need to wait for salt-ssh results
                    sendMessage(session, new ActionErrorEventDto(null,
                            "ERR_TARGET_NO_MATCH", ""));
                    return;
                }

                previewedMinions = Collections
                        .synchronizedList(new ArrayList<>(res.size()));
                previewedMinions.addAll(
                        res.keySet().stream().collect(Collectors.toList()));
                previewedMinions.retainAll(allVisibleMinions);

                sendMessage(session, new AsyncJobStartEventDto("preview",
                        previewedMinions, resSSH.isPresent()));

                res.forEach((minionId, future) -> future.whenComplete((matchResult, err) -> {
                    if (!previewedMinions.contains(minionId)) {
                        // minion is not visible to this user
                        return;
                    }
                    if (matchResult != null && matchResult.result().orElse(false)) {
                        sendMessage(session, new MinionMatchResultEventDto(minionId));
                    }
                    if (err != null) {
                        if (err instanceof TimeoutException) {
                            sendMessage(session,
                                    new ActionTimedOutEventDto(minionId, "preview"));
                            LOG.debug("Timed out waiting for response from minion {}", minionId);
                        }
                        else {
                            LOG.error("Error waiting for minion {}", minionId, err);
                            sendMessage(session,
                                    new ActionErrorEventDto(minionId,
                                            "ERR_WAIT_MATCH",
                                            "Error waiting for matching: " +
                                                    err.getMessage()));
                        }
                    }
                }));

                resSSH.ifPresent(future -> future.whenComplete((result, err) -> {
                    if (err != null) {
                        if (err instanceof TimeoutException) {
                            sendMessage(session,
                                    new ActionTimedOutEventDto(true, "preview"));
                            LOG.debug(
                                "Timed out waiting for response from salt-ssh minions");
                        }
                        else {
                            LOG.error("Error waiting for salt-ssh minions", err);
                            sendMessage(session,
                                    new ActionErrorEventDto(null,
                                            "ERR_WAIT_SSH_MATCH",
                                            "Error waiting for matching: " +
                                                    err.getMessage()));
                        }
                        return;
                    }
                    if (result != null) {
                        List<String> sshMinions = result.entrySet().stream()
                                .filter((entry) -> {
                                    if (!allVisibleMinions.contains(entry.getKey())) {
                                        // minion is not visible to this user
                                        return false;
                                    }
                                    return entry.getValue() != null &&
                                            entry.getValue().result().orElse(false);
                                })
                                .map(Map.Entry::getKey)
                                .collect(Collectors.toList());

                        previewedMinions.addAll(sshMinions);

                        sendMessage(session,
                                new SSHMinionMatchResultDto(sshMinions));
                    }
                }));

            }
            else if (msg.isCancel()) {
                if (this.failAfter != null) {
                    this.failAfter.completeExceptionally(
                            new TimeoutException("Canceled waiting"));
                }
                if (previewedMinions == null || previewedMinions.isEmpty()) {
                    sendMessage(session,
                            new ActionTimedOutEventDto(null, "preview"));
                }
            }
            else {
                if (previewedMinions == null) {
                    sendMessage(session,
                            new ActionErrorEventDto(null,
                                    "ERR_NO_PREVIEW",
                                    "Please execute preview first."));
                }
                else if (previewedMinions.isEmpty()) {
                    sendMessage(session, new ActionErrorEventDto(null,
                            "ERR_TARGET_NO_MATCH", ""));
                    return;
                }
                this.failAfter = FutureUtils.failAfter(timeOut);
                Map<String, CompletionStage<Result<String>>> res;
                try {
                    res = saltApi
                            .runRemoteCommandAsync(new MinionList(previewedMinions),
                                    msg.getCommand(), failAfter);
                    if (LOG.isInfoEnabled()) {
                        LOG.info("User '{}' has sent the command '{}' to minions [{}]", webSession.getUser().getLogin(),
                                msg.getCommand(), String.join(", ", previewedMinions));
                    }

                }
                catch (NullPointerException e) {
                    sendMessage(session, new ActionErrorEventDto(null,
                            "ERR_TARGET_NO_MATCH", e.getMessage()));
                    return;
                }

                List<String> allMinions = res.keySet().stream()
                        .collect(Collectors.toList());
                sendMessage(session, new AsyncJobStartEventDto("run", allMinions, false));

                res.forEach((minionId, future) -> future.whenComplete((cmdResult, err) -> {
                    if (cmdResult != null) {
                        AbstractSaltEventDto event = cmdResult.fold(
                                error -> {
                                    String errMsg = parseSaltError(error);
                                    LOG.info("Received Salt error for minion {}: {}", minionId, errMsg);
                                    return new ActionErrorEventDto(minionId,
                                        "ERR_CMD_SALT_ERROR", errMsg);
                                },
                                result -> {
                                    LOG.info("User '{}' received command result from minion {}. Output is logged at" +
                                            "DEBUG level.", webSession.getUser().getLogin(), minionId);
                                    addHistoryEvent(webSession.getUser().getLogin(), minionId,
                                            msg.getCommand(), result);
                                    if (LOG.isDebugEnabled()) {
                                        LOG.debug("Minion {} returned:\n{}", minionId, result);

                                    }
                                    return new MinionCommandResultEventDto(minionId, result);
                                });
                        sendMessage(session, event);
                    }
                    if (err != null) {
                        if (err instanceof TimeoutException) {
                            sendMessage(session,
                                    new ActionTimedOutEventDto(minionId, "run"));
                            LOG.debug("Timed out waiting for response from minion {}", minionId);
                        }
                        else {
                            LOG.error("Error waiting for minion {}", minionId, err);
                            sendMessage(session,
                                    new ActionErrorEventDto(minionId, "ERR_WAIT_CMD",
                                            "Error waiting to execute command: " +
                                                    err.getMessage()));
                        }
                    }
                }));
            }
        }
        catch (Exception e) {
            LOG.error("Error executing Salt async remote command", e);
            sendMessage(session, new ActionErrorEventDto(null,
                    "GENERIC_ERR", e.getMessage()));
        }
        finally {
            HibernateFactory.closeSession();
        }

    }

    private void addHistoryEvent(String user, String minionId, String command, String result) {
        eventHistoryExecutor.submit(() ->
                TransactionHelper.handlingTransaction(() -> {
                            Optional<MinionServer> minion =
                                    MinionServerFactory.findByMinionId(minionId);

                            final String end = "</pre>";
                            StringBuilder details = new StringBuilder();
                            details.append("Command: <pre>");
                            details.append(command);
                            details.append("</pre> returned:<pre>");
                            int maxLength = 4000 - details.length() - end.length();
                            String htmlOutput = StringUtil.htmlifyText(result);
                            details.append(
                                htmlOutput.length() > maxLength ?
                                    StringUtils.substring(htmlOutput, 0, maxLength - "...".length()) + "..." :
                                    htmlOutput
                            );
                            details.append("</pre>");
                            minion.ifPresent(min ->
                                    SystemManager.addHistoryEvent(min,
                                        "Salt Remote Command executed by " +
                                                user, details.toString())
                            );
                        },
                        (Exception e) ->
                                LOG.error("Error adding Salt remote command event to history of minion {}", minionId, e)
                )
        );
    }

    private boolean invalidWebSession(Session session, WebSession webSession) {
        if (webSession == null || webSession.getUser() == null) {
            LOG.debug("Invalid web sessionId. Closing the web socket.");
            sendMessage(session, new ActionErrorEventDto(null,
                    "INVALID_SESSION", "Invalid user session."));
            try {
                session.close();
                return true;
            }
            catch (IOException e) {
                LOG.debug("Error closing web socket session", e);
            }
        }
        return false;
    }

    /**
     * Must be synchronized. Sending messages concurrently from separate threads
     * will result in IllegalStateException.
     */
    private void sendMessage(Session session, AbstractSaltEventDto dto) {
        synchronized (session) {
            try {
                if (session.isOpen()) {
                    session.getBasicRemote().sendText(Json.GSON.toJson(dto));
                }
                else {
                    LOG.debug("Could not send websocket message. Session is closed.");
                }
            }
            catch (IOException e) {
                LOG.error("Error sending websocket message", e);
            }
        }
    }

    private String parseSaltError(SaltError error) {
        if (error instanceof JsonParsingError) {
            JsonObject jsonErr = (JsonObject) ((JsonParsingError) error).getJson();
            StringBuilder out = new StringBuilder("Error: ");
            if (StringUtils.isNotEmpty(jsonErr.get("retcode").getAsString())) {
                out.append("[");
                out.append(jsonErr.get("retcode").getAsInt());
                out.append("] ");
            }
            if (StringUtils.isNotEmpty(jsonErr.get("stdout").getAsString())) {
                out.append(jsonErr.get("stdout").getAsString());
            }
            if (StringUtils.isNotEmpty(jsonErr.get("stderr").getAsString())) {
                out.append(jsonErr.get("stderr").getAsString());
            }
            return out.toString();
        }
        else {
            return error.toString();
        }
    }

    /**
     * Callback executed an error occurs.
     * @param session the websocket session
     * @param err the err that occurred
     */
    @OnError
    public void onError(Session session, Throwable err) {
        boolean didClientAbortedConnection = err instanceof EOFException ||
                !session.isOpen() ||
                err.getMessage().startsWith("Unexpected error [32]");

        if (didClientAbortedConnection) {
            LOG.debug("The client aborted the connection.", err);
        }
        else {
            LOG.error("Websocket endpoint error", err);
        }
    }

}
