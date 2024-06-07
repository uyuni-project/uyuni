/*
 * Copyright (c) 2018 SUSE LLC
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
package com.redhat.rhn.taskomatic.task;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainEntry;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.server.Server;

import com.suse.cloud.CloudPaygManager;
import com.suse.manager.webui.services.SaltServerActionService;

import org.apache.commons.collections.CollectionUtils;
import org.quartz.JobExecutionContext;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Execute SUSE Manager actions via Salt.
 */
public class MinionActionChainExecutor extends RhnJavaJob {

    public static final int ACTION_DATABASE_GRACE_TIME = 600_000;
    public static final int ACTION_DATABASE_POLL_TIME = 100;
    public static final long MAXIMUM_TIMEDELTA_FOR_SCHEDULED_ACTIONS = 24; // hours
    public static final LocalizationService LOCALIZATION = LocalizationService.getInstance();

    private final SaltServerActionService saltServerActionService;
    private final CloudPaygManager cloudPaygManager;

    /**
     * Default constructor.
     */
    public MinionActionChainExecutor() {
        this(GlobalInstanceHolder.SALT_SERVER_ACTION_SERVICE, GlobalInstanceHolder.PAYG_MANAGER);
    }

    /**
     * Constructs an instance specifying the {@link SaltServerActionService}. Meant to be used only for unit test.
     * @param saltServerActionServiceIn the salt service
     * @param cloudPaygManagerIn the cloud payg manager
     */
    public MinionActionChainExecutor(SaltServerActionService saltServerActionServiceIn,
                                     CloudPaygManager cloudPaygManagerIn) {
        saltServerActionService = saltServerActionServiceIn;
        cloudPaygManager = cloudPaygManagerIn;
    }

    @Override
    public String getConfigNamespace() {
        return "minion_actionchain_executor";
    }

    /**
     * @param context the job execution context
     * @see org.quartz.Job#execute(JobExecutionContext)
     */
    @Override
    public void execute(JobExecutionContext context) {
        if (log.isDebugEnabled()) {
            log.debug("Start minion action chain executor");
        }

        // Measure time to calculate the total duration
        long start = System.currentTimeMillis();
        long actionChainId = Long.parseLong((String)context.getJobDetail()
                .getJobDataMap().get("actionchain_id"));

        ActionChain actionChain = ActionChainFactory.getActionChain(actionChainId).orElse(null);
        int waitedTime = 0;
        while (countQueuedServerActions(actionChain) == 0 && waitedTime < ACTION_DATABASE_GRACE_TIME) {
            actionChain = ActionChainFactory.getActionChain(actionChainId).orElse(null);
            try {
                Thread.sleep(ACTION_DATABASE_POLL_TIME);
            }
            catch (InterruptedException e) {
                // never happens
                Thread.currentThread().interrupt();
            }
            waitedTime += ACTION_DATABASE_POLL_TIME;
        }

        if (actionChain == null) {
            log.error("Action chain not found id={}", actionChainId);
            return;
        }

        if (countQueuedServerActions(actionChain) == 0) {
            log.error("Action chain with id={} has no server where an action is in status QUEUED", actionChainId);
            return;
        }

        // calculate offset between scheduled time of
        // actions and (now)
        long timeDelta = Duration
                .between(ZonedDateTime.ofInstant(actionChain.getEarliestAction().toInstant(),
                        ZoneId.systemDefault()), ZonedDateTime.now())
                .toHours();
        if (timeDelta >= MAXIMUM_TIMEDELTA_FOR_SCHEDULED_ACTIONS) {
            log.warn("Scheduled action chain {} was scheduled to be executed more than {} hours ago. Skipping it.",
                    actionChain.getId(), MAXIMUM_TIMEDELTA_FOR_SCHEDULED_ACTIONS);

            List<Long> actionsId = actionChain.getEntries()
                                              .stream()
                                              .map(ActionChainEntry::getActionId)
                                              .filter(Objects::nonNull)
                                              .collect(Collectors.toList());

            ActionFactory.rejectScheduledActions(actionsId,
                LOCALIZATION.getMessage("task.action.rejection.reason", MAXIMUM_TIMEDELTA_FOR_SCHEDULED_ACTIONS));

            return;
        }
        if (!cloudPaygManager.isCompliant()) {
            log.error("This action was not executed because SUSE Manager Server PAYG is unable to send " +
                    "accounting data to the cloud provider.");
            List<Long> actionsId = actionChain.getEntries()
                    .stream()
                    .map(ActionChainEntry::getActionId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            ActionFactory.rejectScheduledActions(actionsId,
                    LOCALIZATION.getMessage("task.action.rejection.notcompliant"));
            return;
        }

        if (cloudPaygManager.isPaygInstance()) {
            cloudPaygManager.checkRefreshCache(true);
            if (!cloudPaygManager.hasSCCCredentials()) {
                boolean hasNonCompliantByosMinion = actionChain.getEntries()
                        .stream()
                        .map(ActionChainEntry::getServer)
                        .filter(Objects::nonNull)
                        .anyMatch(server -> !server.isAllowedOnPayg());

                if (hasNonCompliantByosMinion) {
                    List<Long> actionsId = actionChain.getEntries()
                            .stream()
                            .map(ActionChainEntry::getActionId)
                            .filter(Objects::nonNull)
                            .collect(Collectors.toList());

                    Set<Server> nonCompliantByosMinions = actionChain.getEntries()
                            .stream()
                            .map(ActionChainEntry::getServer)
                            .filter(s -> !s.isAllowedOnPayg())
                            .collect(Collectors.toSet());

                    ActionFactory.rejectScheduledActions(actionsId,
                            LOCALIZATION.getMessage("task.action.rejection.notcompliantPaygByosActionChain",
                                    formatByosListToStringErrorMsg(nonCompliantByosMinions)
                                    ));
                    return;
                }
            }
        }
        log.info("Executing action chain: {}", actionChainId);

        saltServerActionService.executeActionChain(actionChainId);

        if (log.isDebugEnabled()) {
            long duration = System.currentTimeMillis() - start;
            log.debug("Total duration was: {} ms", duration);
        }
    }

    private long countQueuedServerActions(ActionChain actionChain) {
        if (actionChain == null || CollectionUtils.isEmpty(actionChain.getEntries())) {
            return 0;
        }

        return actionChain.getEntries()
                          .stream()
                          .map(ActionChainEntry::getAction)
                          .filter(action -> action != null && CollectionUtils.isNotEmpty(action.getServerActions()))
                          .flatMap(action -> action.getServerActions().stream())
                          .filter(serverAction -> ActionFactory.STATUS_QUEUED.equals(serverAction.getStatus()))
                          .count();
    }

    private String formatByosListToStringErrorMsg(Set<Server> byosMinions) {
        if (byosMinions.size() <= 2) {
            return byosMinions.stream()
                    .map(Server::getName)
                    .collect(Collectors.joining(","));
        }

        String errorMsg = byosMinions.stream()
                .map(Server::getName)
                .limit(2)
                .collect(Collectors.joining(","));

        int numberOfLeftByosServers = byosMinions.size() - 2;

        return String.format("%s and %d more", errorMsg, numberOfLeftByosServers);
    }
}
