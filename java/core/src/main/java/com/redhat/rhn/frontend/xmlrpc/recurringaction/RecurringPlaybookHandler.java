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
import com.redhat.rhn.domain.recurringactions.type.RecurringPlaybook;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.InvalidArgsException;

import java.util.Map;

/**
 * Handler for recurring Ansible playbooks

 * @apidoc.namespace recurring.playbook
 * @apidoc.doc Provides methods to handle recurring ansible playbook execution
 * for minions, system groups and organizations.
 */
public class RecurringPlaybookHandler extends BaseHandler {
    private final RecurringActionHandler actionHandler = new RecurringActionHandler();

    /**
     * Create a new recurring playbook action.
     *
     * @param loggedInUser The current user
     * @param actionProps Map containing action properties
     * @return action id or exception thrown otherwise
     *
     * @apidoc.doc Create a new recurring Ansible playbook action.
     * @apidoc.param #session_key()
     * @apidoc.param
     *  #struct_begin("actionProps")
     *      #prop_desc("int", "entity_id", "the ID of the target entity")
     *      #prop_desc("string", "name", "the name of the recurring action")
     *      #prop_desc("string", "cron_expr", "the execution frequency of the action as a cron expression")
     *      #prop_desc("string", "extra_vars", "extra variables to override existing
     *                                          vars or create new ones (optional)")
     *      #prop_desc("boolean", "flush_cache", "whether the Ansible cache should be flushed (optional)")
     *      #prop_desc("string", "inventory_path", "the path to the configured Ansible inventory")
     *      #prop_desc("string", "playbook_path", "the path to the playbook to be executed")
     *      #prop_desc("boolean", "test", "whether the action should be executed in test mode (optional)")
     *  #struct_end()
     * @apidoc.returntype #param_desc("int", "id", "the ID of the newly created recurring action")
     */
    public int create(User loggedInUser, Map<String, Object> actionProps) {
        actionProps.put("entity_type", "MINION");
        RecurringAction action =
                actionHandler.createAction(RecurringActionType.ActionType.PLAYBOOK, actionProps, loggedInUser);

        RecurringPlaybook recurringPlaybook = (RecurringPlaybook) action.getRecurringActionType();
        if (!actionProps.containsKey("playbook_path")) {
            throw new InvalidArgsException("'playbook_path' is required.");
        }
        else {
            recurringPlaybook.setPlaybookPath(actionProps.get("playbook_path").toString());
        }
        if (actionProps.containsKey("extra_vars")) {
            recurringPlaybook.setExtraVars(actionProps.get("extra_vars").toString().getBytes());
        }
        if (actionProps.containsKey("flush_cache")) {
            recurringPlaybook.setFlushCache(Boolean.parseBoolean(actionProps.get("flush_cache").toString()));
        }
        if (actionProps.containsKey("inventory_path")) {
            recurringPlaybook.setInventoryPath(actionProps.get("inventory_path").toString());
        }
        return actionHandler.save(loggedInUser, action);
    }

    /**
     * Update a recurring Ansible playbook action.
     *
     * @param loggedInUser The current user
     * @param actionProps Map containing properties to update
     * @return action id or exception thrown otherwise
     *
     * @apidoc.doc Update a recurring Ansbile playbook action.
     * @apidoc.param #session_key()
     * @apidoc.param
     *  #struct_begin("actionProps")
     *      #prop_desc("int", "id", "the ID of the action to update")
     *      #prop_desc("string", "name", "the name of the action (optional)")
     *      #prop_desc("string", "cron_expr", "the execution frequency of the action (optional)")
     *      #prop_desc("string", "extra_vars", "extra variables to override existing
     *                                          vars or create new ones (optional)")
     *      #prop_desc("boolean", "flush_cache", "whether the Ansible cache should be flushed (optional)")
     *      #prop_desc("string", "inventory_path", "the path to the configured Ansible inventory (optional")
     *      #prop_desc("string", "playbook_path", "the path to the playbook to be executed (optional)")
     *      #prop_desc("boolean", "test", "whether the action should be executed in test mode (optional)")
     *      #prop_desc("boolean", "active", "whether the action should be active (optional)")
     *  #struct_end()
     * @apidoc.returntype #param_desc("int", "id", "the ID of the updated recurring action")
     */
    public int update(User loggedInUser, Map<String, Object> actionProps) {
        RecurringAction action = actionHandler.updateAction(actionProps, loggedInUser);

        RecurringPlaybook recurringPlaybook = (RecurringPlaybook) action.getRecurringActionType();
        if (actionProps.containsKey("playbook_path")) {
            recurringPlaybook.setPlaybookPath(actionProps.get("playbook_path").toString());
        }
        if (actionProps.containsKey("extra_vars")) {
            recurringPlaybook.setExtraVars(actionProps.get("extra_vars").toString().getBytes());
        }
        if (actionProps.containsKey("flush_cache")) {
            recurringPlaybook.setFlushCache(Boolean.parseBoolean(actionProps.get("flush_cache").toString()));
        }
        if (actionProps.containsKey("inventory_path")) {
            recurringPlaybook.setInventoryPath(actionProps.get("inventory_path").toString());
        }

        return actionHandler.save(loggedInUser, action);
    }
}
