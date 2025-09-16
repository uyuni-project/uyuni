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
import com.redhat.rhn.domain.recurringactions.type.RecurringPlaybook;
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

 * @apidoc.namespace recurring
 * @apidoc.doc Provides methods to handle recurring actions for minions, system groups and organizations.
 */
public class RecurringActionHandler extends BaseHandler {

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
     * @param id the id of the entity
     * @param type type of the entity
     * @return the list of recurring actions
     *
     * @apidoc.doc Return a list of recurring actions for a given entity.
     * @apidoc.param #session_key()
     * @apidoc.param
     *   #prop_desc("string", "type", "the type of the target entity. One of the following:")
     *     #options()
     *       #item("minion")
     *       #item("group")
     *       #item("org")
     *     #options_end()
     * @apidoc.param #param_desc("int", "id", "the ID of the target entity")
     * @apidoc.returntype
     *      #return_array_begin()
     *          $RecurringActionSerializer
     *      #array_end()
     */
    @ReadOnly
    public List<? extends RecurringAction> listByEntity(User loggedInUser, String type, Integer id) {
        try {
            switch (getEntityType(type)) {
                case MINION:
                    return RecurringActionManager.listMinionRecurringActions(id, loggedInUser);
                case GROUP:
                    return RecurringActionManager.listGroupRecurringActions(id, loggedInUser);
                case ORG:
                    return RecurringActionManager.listOrgRecurringActions(id, loggedInUser);
                default:
                    throw new InvalidArgsException(type);
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
     * @param id id of the action
     * @return the recurring action exception thrown otherwise
     *
     * @apidoc.doc Find a recurring action with the given action ID.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "id", "the action ID")
     * @apidoc.returntype $RecurringActionSerializer
     */
    @ReadOnly
    public RecurringAction lookupById(User loggedInUser, Integer id) {
        RecurringAction action = RecurringActionFactory.lookupById(id).orElseThrow(
                () -> new EntityNotExistsFaultException("Action with id: " + id + " does not exist")
        );
        if (!action.canAccess(loggedInUser)) {
            throw new PermissionCheckFailureException("Action not accessible to user: " + loggedInUser);
        }
        return action;
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
        catch (UnsupportedOperationException e) {
            throw new InvalidArgsException(e.getMessage());
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
            else if (RecurringActionType.ActionType.PLAYBOOK.equals(actionType)) {
                ((RecurringPlaybook) recurringActionType).setTestMode(testMode);
            }
        }
        return action;
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
                else if (RecurringActionType.ActionType.PLAYBOOK.equals(actionType.getActionType())) {
                    ((RecurringPlaybook) action.getRecurringActionType()).setTestMode(testMode);
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
     * @param id the id of the action
     * @return the id of deleted action otherwise exception thrown
     *
     * @apidoc.doc Delete a recurring action with the given action ID.
     * @apidoc.param #session_key()
     * @apidoc.param #param_desc("int", "id", "the action ID")
     * @apidoc.returntype #return_int_success()
     */
    public int delete(User loggedInUser, Integer id) {
        RecurringAction action = lookupById(loggedInUser, id);
        try {
            RecurringActionManager.deleteAndUnschedule(action, loggedInUser);
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
        return 1;
    }
}
