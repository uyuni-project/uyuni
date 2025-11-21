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

import com.redhat.rhn.domain.recurringactions.RecurringAction;
import com.redhat.rhn.domain.recurringactions.type.RecurringActionType;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;

import java.util.Map;

/**
 * Handler for recurring highstates

 * @apidoc.namespace recurring.highstate
 * @apidoc.doc Provides methods to handle recurring highstates for minions, system groups and organizations.
 */
public class RecurringHighstateHandler extends BaseHandler {
    private final RecurringActionHandler actionHandler = new RecurringActionHandler();

    /**
     * Create a new recurring action.
     *
     * @param loggedInUser The current user
     * @param actionProps Map containing action properties
     * @return action ID or exception thrown otherwise
     *
     * @apidoc.doc Create a new recurring highstate action.
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
     *      #prop_desc("boolean", "test", "whether the action should be executed in test mode (optional)")
     *  #struct_end()
     * @apidoc.returntype #param_desc("int", "id", "the ID of the newly created recurring action")
     */
    public int create(User loggedInUser, Map<String, Object> actionProps) {
        RecurringAction action =
                actionHandler.createAction(RecurringActionType.ActionType.HIGHSTATE, actionProps, loggedInUser);
        return actionHandler.save(loggedInUser, action);
    }

    /**
     * Update a recurring action.
     *
     * @param loggedInUser The current user
     * @param actionProps Map containing properties to update
     * @return action id or exception thrown otherwise
     *
     * @apidoc.doc Update the properties of a recurring highstate action.
     * @apidoc.param #session_key()
     * @apidoc.param
     *  #struct_begin("actionProps")
     *      #prop_desc("int", "id", "the ID of the action to update")
     *      #prop_desc("string", "name", "the name of the action (optional)")
     *      #prop_desc("string", "cron_expr", "the execution frequency of the action (optional)")
     *      #prop_desc("boolean", "test", "whether the action should be executed in test mode (optional)")
     *      #prop_desc("boolean", "active", "whether the action should be active (optional)")
     *  #struct_end()
     * @apidoc.returntype #param_desc("int", "id", "the ID of the updated recurring action")
     */
    public int update(User loggedInUser, Map<String, Object> actionProps) {
        RecurringAction action = actionHandler.updateAction(actionProps, loggedInUser);
        return actionHandler.save(loggedInUser, action);
    }
}
