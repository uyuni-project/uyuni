/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.suse.manager.reactor.messaging;

import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.MinionSummary;

import com.suse.manager.action.TransactionalActionManager;
import com.suse.manager.webui.services.SaltServerActionService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Date;
import java.util.List;
import java.util.Optional;

/**
 * Resumes an existing transactional action.
 */
public class ResumeTransactionalActionEventMessageAction implements MessageAction {

    private static final Logger LOG =
            LogManager.getLogger(ResumeTransactionalActionEventMessageAction.class);

    private final SaltServerActionService saltServerActionService;

    /**
     * Standard constructor.
     *
     * @param saltServerActionServiceIn service used to execute the continuation
     */
    public ResumeTransactionalActionEventMessageAction(
            SaltServerActionService saltServerActionServiceIn) {
        saltServerActionService = saltServerActionServiceIn;
    }

    @Override
    public void execute(EventMessage msg) {
        ResumeTransactionalActionEventMessage message =
                (ResumeTransactionalActionEventMessage) msg;

        Action action = ActionFactory.lookupById(message.getActionId());
        if (action == null) {
            LOG.warn("Unable to resume transactional action {}: action not found",
                    message.getActionId());
            markResumeFailed(message);
            return;
        }

        if (TransactionalActionManager.isTransactionalApplyWaitingForReboot(
                action, message.getServerId())) {
            completeTransactionalApplyAction(action, message);
            return;
        }

        var transactionalAction = TransactionalActionManager.getAfterRebootAction(action);
        if (transactionalAction.isEmpty()) {
            LOG.warn("Unable to resume action {}: action does not have an after reboot state",
                    message.getActionId());
            markResumeFailed(message);
            return;
        }

        List<MinionSummary> minions =
                MinionServerFactory.findAllMinionSummaries(message.getActionId());

        Optional<MinionSummary> target = minions.stream()
                .filter(minion -> message.getServerId().equals(minion.getServerId()))
                .findFirst();

        if (target.isEmpty()) {
            LOG.warn("Unable to resume transactional action {} for server {}: target not found",
                    message.getActionId(), message.getServerId());
            markResumeFailed(message);
            return;
        }

        var result = saltServerActionService.resumeTransactionalAction(
                transactionalAction.get(), List.of(target.get()));

        if (result.get(true).contains(target.get())) {
            markPostScheduled(message);
            return;
        }

        var serverAction = action.getServerAction(message.getServerId());
        if (serverAction != null) {
            serverAction.setStatusFailed();
            serverAction.setResultMsg(
                    "Unable to schedule the after reboot Salt call.");
        }
        markResumeFailed(message);
    }

    private void completeTransactionalApplyAction(Action action, ResumeTransactionalActionEventMessage message) {
        ServerAction serverAction = action.getServerAction(message.getServerId());
        if (serverAction == null) {
            LOG.warn("Unable to complete transactional action {} for server {}: server action not found",
                    message.getActionId(), message.getServerId());
            markResumeFailed(message);
            return;
        }

        serverAction.setStatusCompleted();
        serverAction.setCompletionTime(new Date());
        serverAction.setResultCode(0L);
        serverAction.setResultMsg("System reboot detected. Action completed.");
        ActionFactory.save(serverAction);
        TransactionalActionManager.recordTransactionalApplyFinalized(message.getServerId(), message.getActionId());
    }

    private void markPostScheduled(ResumeTransactionalActionEventMessage message) {
        TransactionalActionManager.recordContinuationScheduled(message.getServerId(), message.getActionId());
    }

    private void markResumeFailed(ResumeTransactionalActionEventMessage message) {
        TransactionalActionManager.recordContinuationFailed(message.getServerId(), message.getActionId());
    }

    @Override
    public boolean canRunConcurrently() {
        return true;
    }
}
