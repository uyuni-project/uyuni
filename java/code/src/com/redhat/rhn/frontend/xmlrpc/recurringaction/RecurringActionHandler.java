/*
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
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.recurringactions.RecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.EntityNotExistsFaultException;
import com.redhat.rhn.frontend.xmlrpc.InvalidArgsException;
import com.redhat.rhn.frontend.xmlrpc.PermissionCheckFailureException;
import com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException;
import com.redhat.rhn.frontend.xmlrpc.ValidationException;
import com.redhat.rhn.manager.EntityNotExistsException;
import com.redhat.rhn.manager.recurringactions.RecurringActionManager;

import com.suse.manager.api.ReadOnly;

import java.util.List;
import java.util.Map;

/**
 * Handler for Recurring Actions ({@link RecurringAction})

 * @apidoc.namespace recurringaction
 * @apidoc.doc Provides methods to handle Recurring Actions for Minions, Groups and Organizations.
 */
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
     * @apidoc.doc Return a list of recurring actions for a given entity.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("string", "entityType", "type of the target entity. Can be MINION, GROUP or ORG.")
     * @apidoc.param #param_desc("int", "entityId", "ID of the target entity")
     * @apidoc.returntype
     *      #return_array_begin()
     *          $RecurringActionSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<? extends RecurringAction> listByEntity(User loggedInUser, String entityType, Integer entityId) {
        try {
            switch (getEntityType(entityType)) {
                case MINION:
                    return RecurringActionManager.listMinionRecurringActions(entityId, loggedInUser);
                case GROUP:
                    return RecurringActionManager.listGroupRecurringActions(entityId, loggedInUser);
                case ORG:
                    return RecurringActionManager.listOrgRecurringActions(entityId, loggedInUser);
                default:
                    throw new IllegalStateException("Unsupported type " + entityType);
            }
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
     * @return the recurring action exception thrown otherwise
     *
     * @apidoc.doc Return recurring action with given action ID.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "actionId", "ID of the action")
     * @apidoc.returntype $RecurringActionSerializer
     */
    @ReadOnly
    public RecurringAction lookupById(User loggedInUser, Integer actionId) {
        RecurringAction action = RecurringActionFactory.lookupById(actionId).orElseThrow(
                () -> new EntityNotExistsFaultException("Action with id: " + actionId + " does not exist")
        );
        if (!action.canAccess(loggedInUser)) {
            throw new PermissionCheckFailureException("Action not accessible to user: " + loggedInUser);
        }
        return action;
    }

    /**
     * Create a new recurring action.
     *
     * @param loggedInUser The current user
     * @param actionProps Map containing action properties
     * @return action id or exception thrown otherwise
     *
     * @apidoc.doc Create a new recurring action.
     * @apidoc.param #session_key()
     * @apidoc.param
     *  #struct_begin("actionProps")
     *      #prop_desc("string", "entity_type", "the type of the target entity. One of the following:")
     *        #options()
     *          #item("MINION")
     *          #item("GROUP")
     *          #item("ORG")
     *        #options_end()
     *      #prop_desc("int", "entity_id", "the ID of the target entity")
     *      #prop_desc("string", "name", "the name of the action")
     *      #prop_desc("string", "cron_expr", "the execution frequency of the action")
     *      #prop_desc("boolean", "test", "whether the action should be executed in test mode (optional)")
     *  #struct_end()
     * @apidoc.returntype #param_desc("int", "id", "the ID of the recurring action")
     */
    public int create(User loggedInUser, Map<String, Object> actionProps) {
        RecurringAction action = createAction(actionProps, loggedInUser);
        return save(loggedInUser, action);
    }

    /* Helper method */
    private RecurringAction createAction(Map<String, Object> actionProps, User user) {
        if (actionProps.containsKey("id") || !actionProps.containsKey("entity_type") ||
                !actionProps.containsKey("entity_id") || !actionProps.containsKey("cron_expr") ||
                !actionProps.containsKey("name")) {
            throw new InvalidArgsException("Incomplete action props");
        }
        RecurringAction action;
        try {
            action = RecurringActionManager.createRecurringAction(
                    getEntityType((String) actionProps.get("entity_type")),
                    ((Integer) actionProps.get("entity_id")).longValue(),
                    user
            );
        }
        catch (EntityNotExistsException e) {
            throw new EntityNotExistsFaultException(e.getMessage());
        }
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
     * @apidoc.doc Update a recurring action.
     * @apidoc.param #session_key()
     * @apidoc.param
     *  #struct_begin("actionProps")
     *      #prop_desc("int", "id", "the ID of the action to update")
     *      #prop_desc("string", "name", "the name of the action (optional)")
     *      #prop_desc("string", "cron_expr", "the execution frequency of the action (optional)")
     *      #prop_desc("boolean", "test", "whether the action should be executed in test mode (optional)")
     *      #prop_desc("boolean", "active", "whether the action should be active (optional)")
     *  #struct_end()
     * @apidoc.returntype #param_desc("int", "id", "the ID of the recurring action")
     */
    public int update(User loggedInUser, Map<String, Object> actionProps) {
        RecurringAction action = updateAction(actionProps, loggedInUser);
        return save(loggedInUser, action);
    }

    /* Helper method */
    private  RecurringAction updateAction(Map<String, Object> actionProps, User user) {
        if (!actionProps.containsKey("id")) {
            throw new InvalidArgsException("No action id provided");
        }
        RecurringAction action = lookupById(user, ((Integer) actionProps.get("id")));
        // detach the object and prevent hibernate from auto flushing when fields become dirty
        RecurringActionFactory.getSession().evict(action);

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

        return action; // return "detached" action
    }

    /* Helper method */
    private int save(User loggedInUser, RecurringAction action) {
        try {
            RecurringAction saved = RecurringActionManager.saveAndSchedule(action, loggedInUser);
            // let's throw an exeption on integer overflow
            return Math.toIntExact(saved.getId());
        }
        catch (ValidatorException e) {
            throw new ValidationException(e.getMessage());
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Delete recurring action with given action id.
     *
     * @param loggedInUser The current user
     * @param actionId id of the action
     * @return id of deleted action otherwise exception thrown
     *
     * @apidoc.doc Delete recurring action with given action id.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "actionId", "ID of the action")
     * @apidoc.returntype #param_desc("int", "id", "the ID of the recurring action")
     */
    public int delete(User loggedInUser, Integer actionId) {
        RecurringAction action = lookupById(loggedInUser, actionId);
        try {
            RecurringActionManager.deleteAndUnschedule(action, loggedInUser);
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
        return actionId;
    }
}
