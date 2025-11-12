/*
 * Copyright (c) 2009--2015 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.domain.action;

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.salt.StateResult;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;

import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.yaml.snakeyaml.Yaml;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * ActionFormatter - Class that is responsible for properly formatting the fields
 * on an Action object.  This is so we can localize the output from certain fields
 * as well as produce an HTML version for output on certain fields.
 *
 */
public class ActionFormatter {

    private Action action;

    private static final Logger LOG = LogManager.getLogger(ActionFormatter.class);

    /**
     * Create an ActionFormatter with the associated Action.
     * @param actionIn the Action we associate with this Formatter
     */
    public ActionFormatter(Action actionIn) {
        if (actionIn == null) {
            throw new NullPointerException("ActionIn must not be null");
        }
        this.action = actionIn;
    }

    /**
     * Get the Action for this Formatter
     * @return Action associated with this formatter.
     */
    protected Action getAction() {
        return this.action;
    }

    /**
     * Get the first section of the Notes field
     * @return String of the first section
     */
    protected String getNotesHeader() {

        StringBuilder retval = new StringBuilder();

        if (action.getFailedCount() > 0) {
            retval.append(getActionLink("action.failedlink",
                    action.getFailedCount()));
        }

        if (action.getSuccessfulCount() > 0) {
            retval.append(getActionLink("action.completelink",
                    action.getSuccessfulCount()));

        }
        return retval.toString();

    }

    private String getActionLink(String key, long count) {
        LocalizationService ls = LocalizationService.getInstance();
        //  We may have to append .plural to the key
        //  if the case includes multiple systems
        StringBuilder keybuff = new StringBuilder();
        keybuff.append(key);
        Object[] args = new Object[2];
        args[0] = action.getId().toString();
        args[1] = count;
        if (count > 1) {
            keybuff.append(".plural");
        }
        return ls.getMessage(keybuff.toString(), args);
    }

    /**
     * No body for the default formatter
     * @return returns an empty string.
     */
    protected String getNotesBody() {
        return "";
    }

    /**
     * Get an HTML version of the notes field
     * @return the HTML String representation of the Notes
     */
    public String getNotes() {
        StringBuilder retval = new StringBuilder();
        retval.append(getNotesHeader());
        retval.append(getNotesBody());
        // The default StringBuilder with nothing in it
        // has the value of "null" so we also want to check
        // for that.
        if (retval.toString().isEmpty() ||
                retval.toString().equals("null")) {
            return LocalizationService.getInstance().getMessage("no notes");
        }
        return retval.toString();

    }

    /**
     * Get the Name of the Action
     * @return String name
     */
    public String getName() {
        if (action.getName() == null) {
            return action.getActionTypeName();
        }
        return action.getName();
    }

    /**
     * Get the Action Type
     * @return String of the ActionType
     */
    public String getActionType() {
        return LocalizationService.getInstance().
            getMessage(action.getActionType().getLabel());
    }

    /**
     * Get the earliest date
     * @return String version of the earliest date
     */
    public String getEarliestDate() {
        return LocalizationService.getInstance().
            formatDate(action.getEarliestAction());
    }

    /**
     * get the login of the scheduler User
     * @return String login of the User
     */
    public String getScheduler() {
        if (action.getSchedulerUser() != null) {
            return action.getSchedulerUser().getLogin();
        }
        return null;
    }

    /**
     * Returns a localized string that represents objects (packages, errata,
     * etc.) related to the Action.
     * @return a descriptive string
     */
    public String getRelatedObjectDescription() {
        return null;
    }

    /**
     * @param server on which action has been executed
     * @param currentUser current user
     * @return returns localized formatted string to be used on system event details page
     */
    public String getDetails(Server server, User currentUser) {
        LocalizationService ls = LocalizationService.getInstance();
        StringBuilder retval = new StringBuilder();
        retval.append(ls.getMessage("system.event.details.execute",
                getEarliestDate()));
        retval.append("</br>");
        ServerAction sa = ActionFactory.getServerActionForServerAndAction(server, action);
        retval.append(ls.getMessage("system.event.details.status",
            ls.getMessage("system.event.details.status" + sa.getStatus().getName())));
        retval.append("</br>");
        if (!(server instanceof MinionServer)) {
            if (sa.getPickupTime() != null) {
                retval.append(ls.getMessage("system.event.details.pickup",
                        ls.formatDate(sa.getPickupTime())));
            }
            else {
                retval.append(ls.getMessage("system.event.details.notPickedUp"));
            }
            retval.append("</br>");
        }
        if (sa.getCompletionTime() != null) {
            retval.append(ls.getMessage("system.event.details.completed",
                    ls.formatDate(sa.getCompletionTime())));
            retval.append("</br>");
            if (server instanceof MinionServer && sa.getParentAction().clientExecutionReturnsYamlFormat()) {
                retval.append(ls.getMessage("system.event.details.returned",
                        formatResultMessage(sa.getResultMsg()), sa.getResultCode()));
            }
            else {
                retval.append(ls.getMessage("system.event.details.returned",
                        StringEscapeUtils.escapeHtml4(sa.getResultMsg()), sa.getResultCode()));
            }
        }
        else {
            retval.append(ls.getMessage("system.event.details.notCompleted"));
        }
        retval.append("</br>");
        retval.append(action.getHistoryDetails(server, currentUser));

        return retval.toString();
    }

    /**
     * @param server on which action has been executed
     * @return returns localized formatted string to be used on system event details page
     */
    public Object getDetails(Server server) {
        return getDetails(server, null);
    }

    private String formatResultMessage(String msg) {
        Yaml yaml = new Yaml();
        List<StateResult> result = new LinkedList<>();
        try {
            @SuppressWarnings("unchecked")
            Map<String, Map<String, Object>> payload = yaml.loadAs(msg, Map.class);
            payload.entrySet().forEach(e -> result.add(new StateResult(e)));
        }
        catch (Exception ce) {
            LOG.warn("Unable to parse result message: {}\n{}", ce, msg);
            return StringEscapeUtils.escapeHtml4(msg);
        }
        return ActionFormatter.formatSaltResultMessage(result);
    }

    /**
     * Format a list of StateResults into human readable and formatted text
     * @param result list of state results
     * @return human readable and formatted text
     */
    public static String formatSaltResultMessage(List<StateResult> result) {
        StringBuilder retval = new StringBuilder();
        result.stream()
                .sorted(Comparator.comparingDouble(StateResult::getRunNum))
                .forEach(entry -> {
                    if (!entry.isResult()) {
                        retval.append("<strong><span class='text-danger'>");
                    }
                    else if (!entry.getChanges().equals("{}")) {
                        retval.append("<strong><span class='text-info'>");
                    }
                    retval.append(StringEscapeUtils.escapeHtml4(entry.toString()));
                    if (!entry.isResult() || !entry.getChanges().equals("{}")) {
                        retval.append("</span></strong>");
                    }
                });
        return retval.toString();
    }

}

