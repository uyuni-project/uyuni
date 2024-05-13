/*
 * Copyright (c) 2023 SUSE LLC
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

package com.redhat.rhn.frontend.xmlrpc.recurringaction;

import com.redhat.rhn.domain.config.ConfigChannel;
import com.redhat.rhn.domain.recurringactions.RecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;
import com.redhat.rhn.domain.recurringactions.state.InternalState;
import com.redhat.rhn.domain.recurringactions.state.RecurringStateConfig;
import com.redhat.rhn.domain.recurringactions.type.RecurringActionType;
import com.redhat.rhn.domain.recurringactions.type.RecurringState;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidArgsException;
import com.redhat.rhn.manager.configuration.ConfigurationManager;
import com.redhat.rhn.manager.recurringactions.StateConfigFactory;

import com.suse.manager.api.ReadOnly;

import java.text.MessageFormat;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Handler for recurring custom states

 * @apidoc.namespace recurring.custom
 * @apidoc.doc Provides methods to handle recurring custom states for minions, system groups and organizations.
 */
public class RecurringCustomStateHandler extends BaseHandler {
    private final ConfigurationManager configManager = ConfigurationManager.getInstance();
    private final StateConfigFactory stateConfigFactory = new StateConfigFactory(configManager);
    private final RecurringActionHandler actionHandler = new RecurringActionHandler();

    /**
     * Create a new recurring action.
     *
     * @param loggedInUser The current user
     * @param actionProps Map containing action properties
     * @return action id or exception thrown otherwise
     *
     * @apidoc.doc Create a new recurring custom state action.
     * @apidoc.param #session_key()
     * @apidoc.param
     *  #struct_begin("actionProps")
     *      #prop_desc("string", "entity_type", "the type of the target entity. One of the following:")
     *        #options()
     *          #item("minion")
     *          #item("group")
     *          #item("org")
     *        #options_end()
     *      #prop_desc("int", "entity_id", "the ID of the target entity")
     *      #prop_desc("string", "name", "the name of the recurring action")
     *      #prop_desc("string", "cron_expr", "the execution frequency of the action as a cron expression")
     *      #prop_array("states", "string", "the ordered list of custom state names to be executed")
     *      #prop_desc("boolean", "test", "whether the action should be executed in test mode (optional)")
     *  #struct_end()
     * @apidoc.returntype #param_desc("int", "id", "the ID of the newly created recurring action")
     */
    public int create(User loggedInUser, Map<String, Object> actionProps) {
        RecurringAction action =
                actionHandler.createAction(RecurringActionType.ActionType.CUSTOMSTATE, actionProps, loggedInUser);

        if (!actionProps.containsKey("states") || ((List<String>) actionProps.get("states")).isEmpty()) {
            throw new InvalidArgsException("'states' cannot be empty.");
        }

        List<String> states = (List<String>) actionProps.get("states");
        Set<RecurringStateConfig> stateConfig = new HashSet<>();
        if (states.contains("reboot") && states.contains("rebootifneeded")) {
            throw new InvalidArgsException("'reboot' and 'rebootifneeded' cannot be used together.");
        }
        else if (states.contains("reboot")) {
            stateConfig.add(stateConfigFactory.getRecurringState(loggedInUser, "reboot", states.size()));
            states.remove("reboot");
        }
        else if (states.contains("rebootifneeded")) {
            stateConfig.add(stateConfigFactory.getRecurringState(loggedInUser, "rebootifneeded", states.size()));
            states.remove("rebootifneeded");
        }
        for (int i = 0; i < states.size(); i++) {
            try {
                stateConfig.add(stateConfigFactory.getRecurringState(loggedInUser, states.get(i), i + 1L));
            }
            catch (NoSuchElementException e) {
                throw new NoSuchStateException(MessageFormat.format(
                        "The state ''{0}'' does not exist or is not accessible to the user.", states.get(i)));
            }
        }

        ((RecurringState) action.getRecurringActionType()).saveStateConfig(stateConfig);
        return actionHandler.save(loggedInUser, action);
    }

    /**
     * Update a recurring action.
     *
     * @param loggedInUser The current user
     * @param actionProps Map containing properties to update
     * @return action id or exception thrown otherwise
     *
     * @apidoc.doc Update a recurring custom state action.
     * @apidoc.param #session_key()
     * @apidoc.param
     *  #struct_begin("actionProps")
     *      #prop_desc("int", "id", "the ID of the action to update")
     *      #prop_desc("string", "name", "the name of the action (optional)")
     *      #prop_desc("string", "cron_expr", "the execution frequency of the action (optional)")
     *      #prop_array("states", "string", "the ordered list of custom state names to be executed (optional)")
     *      #prop_desc("boolean", "test", "whether the action should be executed in test mode (optional)")
     *      #prop_desc("boolean", "active", "whether the action should be active (optional)")
     *  #struct_end()
     * @apidoc.returntype #param_desc("int", "id", "the ID of the updated recurring action")
     */
    public int update(User loggedInUser, Map<String, Object> actionProps) {
        RecurringAction action = actionHandler.updateAction(actionProps, loggedInUser);

        if (actionProps.containsKey("states")) {
            List<String> states = (List<String>) actionProps.get("states");
            if (states.isEmpty()) {
                throw new InvalidArgsException("'states' cannot be empty.");
            }

            Set<RecurringStateConfig> stateConfig = new HashSet<>();
            if (states.contains("reboot") && states.contains("rebootifneeded")) {
                throw new InvalidArgsException("'reboot' and 'rebootifneeded' cannot be used together.");
            }
            else if (states.contains("reboot")) {
                stateConfig.add(stateConfigFactory.getRecurringState(loggedInUser, "reboot", states.size()));
                states.remove("reboot");
            }
            else if (states.contains("rebootifneeded")) {
                stateConfig.add(stateConfigFactory.getRecurringState(loggedInUser, "rebootifneeded", states.size()));
                states.remove("rebootifneeded");
            }
            for (int i = 0; i < states.size(); i++) {
                try {
                    stateConfig.add(stateConfigFactory.getRecurringState(loggedInUser, states.get(i), i + 1L));
                }
                catch (NoSuchElementException e) {
                    throw new NoSuchStateException(MessageFormat.format(
                            "The state ''{0}'' does not exist or is not accessible to the user.", states.get(i)));
                }
            }
            ((RecurringState) action.getRecurringActionType()).saveStateConfig(stateConfig);
        }
        return actionHandler.save(loggedInUser, action);
    }

    /**
     * List all the custom states available to the user
     * @param loggedInUser the logged-in user
     * @return the list of custom state names available to the user
     *
     * @apidoc.doc List all the custom states available to the user.
     * @apidoc.param #session_key()
     * @apidoc.returntype #array_single("string", "the list of custom channels available to the user")
     */
    @ReadOnly
    public List<String> listAvailable(User loggedInUser) {
        return Stream.concat(
                // Get available config channels
                ConfigurationManager.getInstance().listGlobalChannels(loggedInUser).stream()
                        .map(ConfigChannel::getLabel),
                // Get internal states
                RecurringActionFactory.listInternalStates().stream()
                        .map(InternalState::getName)
        ).collect(Collectors.toList());
    }
}
