/*
 * Copyright (c) 2016--2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 */
package com.redhat.rhn.domain.action.salt;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionFormatter;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.notification.NotificationMessage;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.notification.types.StateApplyFailed;
import com.redhat.rhn.domain.server.MinionSummary;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.reactor.messaging.ApplyStatesEventMessage;
import com.suse.manager.utils.SaltUtils;
import com.suse.salt.netapi.calls.LocalCall;

import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.OneToOne;


/**
 * ApplyStatesAction - Action class representing the application of Salt states.
 */
@Entity
@DiscriminatorValue("503")
public class ApplyStatesAction extends Action {

    @OneToOne(mappedBy = "parentAction", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private ApplyStatesActionDetails details;

    /**
     * Return the details.
     * @return details
     */
    public ApplyStatesActionDetails getDetails() {
        return details;
    }

    /**
     * Set the details.
     * @param detailsIn details
     */
    public void setDetails(ApplyStatesActionDetails detailsIn) {
        if (detailsIn != null) {
            detailsIn.setParentAction(this);
        }
        this.details = detailsIn;
    }

    @Override
    public ActionFormatter getFormatter() {
        if (formatter == null) {
            formatter = new ApplyStatesActionFormatter(this);
        }
        return formatter;
    }

    @Override
    public String getHistoryDetails(Server server, User currentUser) {
        StringBuilder retval = new StringBuilder();
        retval.append("</br>");
        for (ApplyStatesActionResult result : getDetails().getResults()) {
            if (result.getServerId().equals(server.getId())) {
                retval.append("Results:");
                retval.append("</br>");
                retval.append("<pre>");
                retval.append(formatStateApplyResult(result));
                retval.append("</pre>");
            }
        }
        return retval.toString();
    }

    private String formatStateApplyResult(ApplyStatesActionResult result) {
        StringBuilder retval = new StringBuilder();
        Optional<List<StateResult>> resultList = result.getResult();
        if (!resultList.isPresent()) {
            LocalizationService ls = LocalizationService.getInstance();
            retval.append("<strong><span class='text-danger'>");
            retval.append("Error: " + ls.getMessage("system.event.details.syntaxerror"));
            retval.append('\n');
            retval.append(result.getOutputContents());
            retval.append("</span></strong>");
            return retval.toString();
        }
        return ActionFormatter.formatSaltResultMessage(resultList.get());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<LocalCall<?>, List<MinionSummary>> getSaltCalls(List<MinionSummary> minionSummaries) {

        Map<LocalCall<?>, List<MinionSummary>> ret = new HashMap<>();
        ret.put(com.suse.salt.netapi.calls.modules.State.apply(details.getMods(), details.getPillarsMap(),
                Optional.of(true),
                details.isTest() ? Optional.of(details.isTest()) : Optional.empty()), minionSummaries);
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void handleUpdateServerAction(ServerAction serverAction, JsonElement jsonResult, UpdateAuxArgs auxArgs) {

        // Revisit the action status if test=true
        if (details.isTest() && auxArgs.getSuccess() && auxArgs.getRetcode() == 0) {
            serverAction.setStatusCompleted();
        }

        ApplyStatesActionResult statesResult = Optional.ofNullable(
                        details.getResults())
                .orElse(Collections.emptySet())
                .stream()
                .filter(result ->
                        serverAction.getServerId().equals(result.getServerId()))
                .findFirst()
                .orElse(new ApplyStatesActionResult());
        details.addResult(statesResult);
        statesResult.setActionApplyStatesId(details.getId());
        statesResult.setServerId(serverAction.getServerId());
        statesResult.setReturnCode(auxArgs.getRetcode());

        // Set the output to the result
        statesResult.setOutput(SaltUtils.getJsonResultWithPrettyPrint(jsonResult).getBytes());

        // Create the result message depending on the action status
        String states = details.getMods().isEmpty() ?
                "highstate" : details.getMods().toString();
        String message = "Successfully applied state(s): " + states;
        if (serverAction.isStatusFailed()) {
            message = "Failed to apply state(s): " + states;

            NotificationMessage nm = UserNotificationFactory.createNotificationMessage(
                    new StateApplyFailed(serverAction.getServer().getName(),
                            serverAction.getServerId(), serverAction.getParentAction().getId()));

            Set<User> admins = new HashSet<>(ServerFactory.listAdministrators(serverAction.getServer()));
            // Q: are also org admins and the creator part of this list?
            UserNotificationFactory.storeForUsers(nm, admins);
        }
        if (details.isTest()) {
            message += " (test-mode)";
        }
        serverAction.setResultMsg(message);

        serverAction.getServer().asMinionServer().ifPresent(minion -> {
            if (jsonResult.isJsonObject()) {
                auxArgs.getSaltUtils().updateSystemInfo(jsonResult, minion);
            }
        });
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Map<String, String>> createActionSpecificDetails(ServerAction serverAction) {
        final List<Map<String, String>> additionalInfo = new ArrayList<>();

        final ApplyStatesActionDetails detail = ActionFactory.lookupApplyStatesActionDetails(getId());
        if (detail != null) {
            final Optional<ApplyStatesActionResult> serverResult = detail.getResult(serverAction.getServerId());

            final String output = serverResult.flatMap(ApplyStatesActionResult::getResult)
                    .orElse(Collections.emptyList())
                    .stream()
                    .sorted(Comparator.comparing(StateResult::getRunNum))
                    .map(StateResult::toString)
                    .collect(Collectors.joining());

            final String returnCode = serverResult.map(ApplyStatesActionResult::getReturnCode)
                    .map(Object::toString)
                    .orElse("");

            additionalInfo.add(Map.of("detail", output, "result", returnCode));
        }

        return additionalInfo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canBeScheduledAnyway() {
        // Special Case: we want to allow channel changing but it calls a state.apply
        return (getDetails() != null && getDetails().getMods().equals(List.of(ApplyStatesEventMessage.CHANNELS)));
    }

}
