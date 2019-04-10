/**
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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.google.gson.JsonObject;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.session.WebSession;
import com.redhat.rhn.domain.session.WebSessionFactory;
import com.redhat.rhn.frontend.servlets.LocalizedEnvironmentFilter;
import com.suse.manager.webui.services.impl.SaltService;
import com.suse.manager.webui.websocket.json.AsyncJobStartEventDto;
import com.suse.manager.webui.websocket.json.ExecuteMinionActionDto;
import com.suse.manager.webui.websocket.json.MinionMatchResultEventDto;
import com.suse.manager.webui.websocket.json.ActionTimedOutEventDto;
import com.suse.manager.webui.websocket.json.MinionCommandResultEventDto;
import com.suse.manager.webui.websocket.json.ActionErrorEventDto;
import com.suse.manager.webui.websocket.json.AbstractSaltEventDto;
import com.suse.manager.webui.websocket.json.SSHMinionMatchResultDto;
import com.suse.salt.netapi.datatypes.target.MinionList;
import com.suse.salt.netapi.errors.JsonParsingError;
import com.suse.salt.netapi.errors.SaltError;
import com.suse.salt.netapi.results.Result;
import com.suse.utils.Json;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import javax.websocket.OnOpen;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnError;
import javax.websocket.Session;
import javax.websocket.EndpointConfig;
import javax.websocket.server.ServerEndpoint;
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
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

/**
 * Websocket endpoint for executing remote commands on Salt minions.
 * NOTE: there's an endpoint instance for each websocket session
 */
@ServerEndpoint(value = "/websocket/minion/remote-commands")
public class RemoteMinionCommands {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(RemoteMinionCommands.class);

    private Long sessionId;

    private CompletableFuture failAfter;
    private List<String> previewedMinions;

    /**
     * Callback executed when the websocket is opened.
     * @param session the websocket session
     * @param config the endpoint config
     */
    @OnOpen
    public void onOpen(Session session, EndpointConfig config) {
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
    }

    /**
     * Callback executed when the websocket is closed.
     */
    @OnClose
    public void onClose() {
        LOG.debug("Closing web socket session");
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
                        .map(MinionServer::getMinionId)
                        .collect(Collectors.toList());

                this.failAfter = SaltService.INSTANCE.failAfter(timeOut);

                String target = StringUtils.trim(msg.getTarget());

                Optional<CompletionStage<Map<String, Result<Boolean>>>> resSSH =
                        SaltService.INSTANCE.matchAsyncSSH(target, failAfter);

                Map<String, CompletionStage<Result<Boolean>>> res = new HashMap<>();

                res = SaltService.INSTANCE.matchAsync(target, failAfter);
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

                res.forEach((minionId, future) -> {
                    future.whenComplete((matchResult, err) -> {
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
                                LOG.debug("Timed out waiting for response from minion " +
                                        minionId);
                            }
                            else {
                                LOG.error("Error waiting for minion " + minionId, err);
                                sendMessage(session,
                                        new ActionErrorEventDto(minionId,
                                                "ERR_WAIT_MATCH",
                                                "Error waiting for matching: " +
                                                        err.getMessage()));
                            }
                        }
                    });
                });

                resSSH.ifPresent(future -> {
                    future.whenComplete((result, err) -> {
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
                                    .map((entry) -> entry.getKey())
                                    .collect(Collectors.toList());

                            previewedMinions.addAll(sshMinions);

                            sendMessage(session,
                                    new SSHMinionMatchResultDto(sshMinions));
                        }
                    });
                });

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
                this.failAfter = SaltService.INSTANCE.failAfter(timeOut);
                Map<String, CompletionStage<Result<String>>> res;
                try {
                    res = SaltService.INSTANCE
                            .runRemoteCommandAsync(new MinionList(previewedMinions),
                                    msg.getCommand(), failAfter);
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
                        sendMessage(session,
                                cmdResult.fold(
                                        error -> new ActionErrorEventDto(minionId,
                                                "ERR_CMD_SALT_ERROR",
                                                parseSaltError(error)),
                                        result -> new MinionCommandResultEventDto(minionId,
                                                result)));
                    }
                    if (err != null) {
                        if (err instanceof TimeoutException) {
                            sendMessage(session,
                                    new ActionTimedOutEventDto(minionId, "run"));
                            LOG.debug("Timed out waiting for response from minion " +
                                    minionId);
                        }
                        else {
                            LOG.error("Error waiting for minion " + minionId, err);
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
        Boolean didClientAbortedConnection = err instanceof EOFException ||
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
