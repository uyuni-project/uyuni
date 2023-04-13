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

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.recurringactions.RecurringAction;
import com.redhat.rhn.domain.recurringactions.RecurringActionFactory;
import com.redhat.rhn.domain.recurringactions.type.RecurringActionType;
import com.redhat.rhn.domain.recurringactions.type.RecurringHighstate;
import com.redhat.rhn.domain.recurringactions.type.RecurringState;
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
 * @apidoc.doc Provides methods to handle recurring actions for minions, system groups and organizations.
 * <p>
 * <strong>Deprecated</strong> - This namespace will be removed in a future API version. To work with recurring actions,
 * please check out the newer 'recurring' namespace.
 * @deprecated This namespace will be removed in a future API version. To work with recurring actions, please check out
 * the newer 'recurring' namespace.
 */
@Deprecated
public class LegacyRecurringActionHandler extends BaseHandler {

    /* helper method */
    private RecurringAction.TargetType getEntityType(String entityType) {
        try {
            return RecurringAction.TargetType.valueOf(entityType.toUpperCase());
        }
        catch (IllegalArgumentException e) {
            throw new InvalidArgsException("TargetType \"" + entityType + "\" does not exist");
        }
    }

    /**
     * Return a list of recurring actions for a given entity.
     *
     * @param loggedInUser The current user
     * @param entityId the id of the entity
     * @param entityType type of the entity
     * @return the list of recurring actions
     * @deprecated This method will be removed in a future API version. To work with recurring actions, please check
     * out the newer 'recurring' namespace.
     *
     * @apidoc.doc Return a list of recurring actions for a given entity.
     * @apidoc.param #session_key()
     * @apidoc.param
     *   #prop_desc("string", "entityType", "the type of the target entity. One of the following:")
     *     #options()
     *       #item("MINION")
     *       #item("GROUP")
     *       #item("ORG")
     *     #options_end()
     * @apidoc.param #param_desc("int", "entityId", "the ID of the target entity")
     * @apidoc.returntype
     *      #return_array_begin()
     *          $RecurringActionSerializer
     *      #array_end()
     */
    @Deprecated
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
     * Return recurring action with the given action ID.
     *
     * @param loggedInUser The current user
     * @param actionId id of the action
     * @return the recurring action exception thrown otherwise
     * @deprecated This method will be removed in a future API version. To work with recurring actions, please check
     * out the newer 'recurring' namespace.
     *
     * @apidoc.doc Find a recurring action with the given action ID.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "actionId", "the action ID")
     * @apidoc.returntype $RecurringActionSerializer
     */
    @Deprecated
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
     * @deprecated This method will be removed in a future API version. To create recurring actions, please use either
     * 'recurring.highstate.create' or 'recurring.custom.create' instead.
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
     *      #prop_desc("string", "name", "the name of the action")
     *      #prop_desc("string", "cron_expr", "the execution frequency of the action")
     *      #prop_desc("boolean", "test", "whether the action should be executed in test mode (optional)")
     *  #struct_end()
     * @apidoc.returntype #param_desc("int", "id", "the ID of the recurring action")
     */
    @Deprecated
    public int create(User loggedInUser, Map<String, Object> actionProps) {
        RecurringAction action = createAction(RecurringActionType.ActionType.HIGHSTATE, actionProps, loggedInUser);
        return save(loggedInUser, action);
    }

    /* Helper method */
    RecurringAction createAction(RecurringActionType.ActionType actionType, Map<String, Object> actionProps,
            User user) {
        if (actionProps.containsKey("id") || !actionProps.containsKey("entity_type") ||
                !actionProps.containsKey("entity_id") || !actionProps.containsKey("cron_expr") ||
                !actionProps.containsKey("name")) {
            throw new InvalidArgsException("Incomplete action props");
        }
        RecurringAction action;
        try {
            action = RecurringActionManager.createRecurringAction(
                    getEntityType((String) actionProps.get("entity_type")),
                    actionType,
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
            boolean testMode = Boolean.parseBoolean(actionProps.get("test").toString());
            RecurringActionType recurringActionType = action.getRecurringActionType();

            if (RecurringActionType.ActionType.HIGHSTATE.equals(actionType)) {
                ((RecurringHighstate) recurringActionType).setTestMode(testMode);
            }
            else if (RecurringActionType.ActionType.CUSTOMSTATE.equals(actionType)) {
                ((RecurringState) recurringActionType).setTestMode(testMode);
            }
        }
        return action;
    }

    /**
     * Update a recurring action.
     *
     * @param loggedInUser The current user
     * @param actionProps Map containing properties to update
     * @return action id or exception thrown otherwise
     * @deprecated This method will be removed in a future API version. To update recurring actions, please use either
     * 'recurring.highstate.update' or 'recurring.custom.update' instead.
     *
     * @apidoc.doc Update a recurring highstate action.
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
    @Deprecated
    public int update(User loggedInUser, Map<String, Object> actionProps) {
        RecurringAction action = updateAction(actionProps, loggedInUser);
        return save(loggedInUser, action);
    }

    /* Helper method */
    RecurringAction updateAction(Map<String, Object> actionProps, User user) {
        if (!actionProps.containsKey("id")) {
            throw new InvalidArgsException("No action id provided");
        }
        RecurringAction action = lookupById(user, ((Integer) actionProps.get("id")));
        // detach the object and prevent hibernate from auto flushing when fields become dirty
        HibernateFactory.getSession().evict(action);

        if (actionProps.containsKey("name")) {
            action.setName((String) actionProps.get("name"));
        }
        if (actionProps.containsKey("cron_expr")) {
            action.setCronExpr((String) actionProps.get("cron_expr"));
        }
        if (actionProps.containsKey("test")) {
            if (actionProps.containsKey("test")) {
                boolean testMode = Boolean.parseBoolean(actionProps.get("test").toString());
                RecurringActionType actionType = action.getRecurringActionType();
                if (RecurringActionType.ActionType.HIGHSTATE.equals(actionType.getActionType())) {
                    ((RecurringHighstate) action.getRecurringActionType()).setTestMode(testMode);
                }
                else if (RecurringActionType.ActionType.CUSTOMSTATE.equals(actionType.getActionType())) {
                    ((RecurringState) action.getRecurringActionType()).setTestMode(testMode);
                }
            }
        }
        if (actionProps.containsKey("active")) {
            action.setActive(Boolean.parseBoolean(actionProps.get("active").toString()));
        }

        return action; // return "detached" action
    }

    /* Helper method */
    int save(User loggedInUser, RecurringAction action) {
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
     * Delete recurring action with the given action ID.
     *
     * @param loggedInUser the current user
     * @param actionId the id of the action
     * @return the id of deleted action otherwise exception thrown
     * @deprecated This method will be removed in a future API version. To work with recurring actions, please check
     * out the newer 'recurring' namespace.
     *
     * @apidoc.doc Delete a recurring action with the given action ID.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "actionId", "the action ID")
     * @apidoc.returntype #param_desc("int", "id", "the ID of the recurring action")
     */
    @Deprecated
    public int delete(User loggedInUser, Integer actionId) {
        RecurringAction action = lookupById(loggedInUser, actionId);
        try {
            RecurringActionManager.deleteAndUnschedule(action, loggedInUser);
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
        return 1;
    }
}
