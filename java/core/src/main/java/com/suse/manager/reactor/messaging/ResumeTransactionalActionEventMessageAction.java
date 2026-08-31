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
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.MinionTransactionalActionHistory;
import com.redhat.rhn.domain.server.MinionTransactionalActionHistory.ProgressStatus;

import com.suse.manager.action.TransactionalActionManager;
import com.suse.manager.webui.services.SaltServerActionService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
     * @param saltServerActionServiceIn service used to execute the after-reboot state
     */
    public ResumeTransactionalActionEventMessageAction(
            SaltServerActionService saltServerActionServiceIn) {
        saltServerActionService = saltServerActionServiceIn;
    }

    @Override
    public void execute(EventMessage msg) {
        ResumeTransactionalActionEventMessage message =
                (ResumeTransactionalActionEventMessage) msg;

        Optional<MinionTransactionalActionHistory> history =
                TransactionalActionManager.findTransactionalActionHistory(
                        message.getServerId(), message.getActionId());
        if (history.isEmpty()) {
            LOG.warn("Unable to resume transactional action {} for server {}: transactional history not found",
                    message.getActionId(), message.getServerId());
            return;
        }

        Action action = ActionFactory.lookupById(message.getActionId());
        if (action == null) {
            LOG.warn("Unable to resume transactional action {}: action not found",
                    message.getActionId());
            markResumeFailed(message);
            return;
        }

        if (!TransactionalActionManager.hasPostTransactionalState(action, history.get())) {
            if (history.get().isWaitingForReboot()) {
                TransactionalActionManager.recordTransactionalApplyFinalized(
                        message.getServerId(), message.getActionId());
            }
            return;
        }

        if (history.get().getAfterRebootStatus() != ProgressStatus.PENDING) {
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
                action, List.of(target.get()));

        if (result.get(true).contains(target.get())) {
            markAfterRebootScheduled(message);
            return;
        }

        var serverAction = action.getServerAction(message.getServerId());
        if (serverAction != null) {
            serverAction.fail("Unable to schedule the after-reboot Salt call.");
        }
        markResumeFailed(message);
    }

    private void markAfterRebootScheduled(ResumeTransactionalActionEventMessage message) {
        TransactionalActionManager.recordAfterRebootScheduled(message.getServerId(), message.getActionId());
    }

    private void markResumeFailed(ResumeTransactionalActionEventMessage message) {
        TransactionalActionManager.recordAfterRebootFailed(message.getServerId(), message.getActionId());
    }
}
