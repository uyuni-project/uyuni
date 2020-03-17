/**
 * Copyright (c) 2020 SUSE LLC
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

import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.domain.recurringactions.RecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.EntityExistsFaultException;
import com.redhat.rhn.frontend.xmlrpc.EntityNotExistsFaultException;
import com.redhat.rhn.frontend.xmlrpc.InvalidArgsException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException;
import com.redhat.rhn.manager.EntityExistsException;
import com.redhat.rhn.manager.recurringactions.RecurringActionManager;

import org.hibernate.HibernateException;

import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public class RecurringActionHandler extends BaseHandler {

    /* helper method */
    private RecurringAction.Type getEntityType(String entityType) {
        try {
            return RecurringAction.Type.valueOf(entityType.toUpperCase());
        }
        catch (IllegalArgumentException e) {
            throw new InvalidArgsException("Type \"" + entityType + "\" does not exist");
        }
    }

    /**
     * Return a list of recurring actions for a given entity.
     *
     * @param loggedInUser The current user
     * @param entityId the id of the entity
     * @param entityType type of the entity
     * @return the list of recurring actions
     *
     * @xmlrpc.doc Return a list of recurring actions for a given entity.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("string", "entityType", "Type of the target entity")
     * @xmlrpc.param #param_desc("int", "entityId", "Id of the target entity")
     * @xmlrpc.returntype
     *      #array()
     *          $RecurringActionSerializer
     *      #array_end()
     */
    public List<? extends RecurringAction> listByEntity(User loggedInUser, String entityType, Integer entityId) {
        List<? extends RecurringAction> schedules;
        try {
            switch (getEntityType(entityType)) {
                case MINION:
                    schedules = RecurringActionManager.listMinionRecurringActions(entityId, loggedInUser);
                    break;
                case GROUP:
                    schedules = RecurringActionManager.listGroupRecurringActions(entityId, loggedInUser);
                    break;
                case ORG:
                    schedules = RecurringActionManager.listOrgRecurringActions(entityId, loggedInUser);
                    break;
                default:
                    throw new IllegalStateException("Unsupported type " + entityType);
            }
            return schedules;
        }
        catch (PermissionException e) {
            throw new PermissionCheckFailureException(e.getMessage());
        }
    }

    /**
     * Return recurring action with given action id.
     *
     * @param loggedInUser The current user
     * @param actionId id of the action
     * @return the list of recurring actions
     *
     * @xmlrpc.doc Return recurring action with given action id.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param #param_desc("int", "action_id", "Id of the action")
     * @xmlrpc.returntype $RecurringActionSerializer
     */
    public RecurringAction lookupById(User loggedInUser, Integer actionId) {
        try {
            RecurringAction action = RecurringActionFactory.lookupById(actionId).orElseThrow();
            if (action.canAccess(loggedInUser)) {
                return action;
            }
        }
        catch (NoSuchElementException e) {
            throw new EntityNotExistsFaultException("Action with id: " + actionId + " does not exist");
        }
        throw new PermissionCheckFailureException("Action not accessible to user: " + loggedInUser);
    }

    /**
     * Create a new recurring action.
     *
     * @param loggedInUser The current user
     * @param actionProps Map containing action properties
     * @return action id or exception thrown otherwise
     *
     * @xmlrpc.doc Create a new recurring action.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param
     *  #struct("actionProps")
     *      #prop_desc("string", "entity_type", "The type of the target entity")
     *      #prop_desc("string", "entity_id", "The id of the target entity")
     *      #prop_desc("string", "name", "The name of the action")
     *      #prop_desc("string", "cron_expr", "The execution frequency of the action")
     *      #prop_desc("boolean", "test", "Whether the action should be executed in test mode")
     *  #struct_end()
     * @xmlrpc.returntype int action_id - The action id of the recurring action
     */
    public Long create(User loggedInUser, Map<String, Object> actionProps) {
        RecurringAction action = createAction(actionProps, loggedInUser);
        try {
            RecurringActionManager.saveAndSchedule(action, loggedInUser);
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
        catch (EntityExistsException e) {
            throw new EntityExistsFaultException(e.getMessage());
        }
        return action.getId();
    }

    /* Helper method */
    private RecurringAction createAction(Map<String, Object> actionProps, User user) {
        RecurringAction action;
        if (actionProps.containsKey("id") || !actionProps.containsKey("entity_type") ||
                !actionProps.containsKey("entity_id") || !actionProps.containsKey("cron_expr") ||
                !actionProps.containsKey("name")) {
            throw new InvalidArgsException("Incomplete action props");
        }
        action = RecurringActionManager.createRecurringAction(
                getEntityType((String) actionProps.get("entity_type")),
                Long.parseLong((String) actionProps.get("entity_id")),
                user
        );
        action.setName((String) actionProps.get("name"));
        action.setCronExpr((String) actionProps.get("cron_expr"));
        if (actionProps.containsKey("test")) {
            action.setTestMode(Boolean.parseBoolean(actionProps.get("test").toString()));
        }
        return action;
    }

    /**
     * Update a recurring action.
     *
     * @param loggedInUser The current user
     * @param actionProps Map containing properties to update
     * @return action id or exception thrown otherwise
     *
     * @xmlrpc.doc Update a recurring action.
     * @xmlrpc.param #session_key()
     * @xmlrpc.param
     *  #struct("actionProps")
     *      #prop_desc("string", "id", "The id of the action to update")
     *      #prop_desc("string", "name", "The name of the action (optional)")
     *      #prop_desc("string", "cron_expr", "The execution frequency of the action (optional)")
     *      #prop_desc("boolean", "test", "Whether the action should be executed in test mode (optional)")
     *      #prop_desc("boolean", "active", "Whether the action should be active (optional)")
     *  #struct_end()
     * @xmlrpc.returntype int action_Id - The action id of the recurring action
     */
    public Long update(User loggedInUser, Map<String, Object> actionProps) {
        RecurringAction action = updateAction(actionProps, loggedInUser);
        try {
            RecurringActionManager.saveAndSchedule(action, loggedInUser);
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
        return action.getId();
    }

    /* Helper method */
    private  RecurringAction updateAction(Map<String, Object> actionProps, User user) {
        if (!actionProps.containsKey("id")) {
            throw new InvalidArgsException("No action id provided");
        }
        Long id = Long.parseLong((String) actionProps.get("id"));
        try {
            RecurringAction action = RecurringActionFactory.lookupById(id).orElseThrow();
            if (actionProps.containsKey("name")) {
                action.setName((String) actionProps.get("name"));
            }
            if (actionProps.containsKey("cron_expr")) {
                action.setCronExpr((String) actionProps.get("cron_expr"));
            }
            if (actionProps.containsKey("test")) {
                action.setTestMode(Boolean.parseBoolean(actionProps.get("test").toString()));
            }
            if (actionProps.containsKey("active")) {
                action.setActive(Boolean.parseBoolean(actionProps.get("active").toString()));
            }
            return action;
        }
        catch (HibernateException e) {
            throw new EntityNotExistsFaultException("Schedule with id: " + id + " does not exist");
        }
    }
}
