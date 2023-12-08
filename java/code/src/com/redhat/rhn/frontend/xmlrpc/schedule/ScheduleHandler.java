/*
 * Copyright (c) 2009--2018 Red Hat, Inc.
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
package com.redhat.rhn.frontend.xmlrpc.schedule;

import com.redhat.rhn.FaultException;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.dto.ActionedSystem;
import com.redhat.rhn.frontend.dto.ScheduledAction;
import com.redhat.rhn.frontend.xmlrpc.BaseHandler;
import com.redhat.rhn.frontend.xmlrpc.TaskomaticApiException;
import com.redhat.rhn.frontend.xmlrpc.UnsupportedOperationException;
import com.redhat.rhn.manager.action.ActionIsChildException;
import com.redhat.rhn.manager.action.ActionManager;

import com.suse.manager.api.ReadOnly;

import java.util.ArrayList;
import java.util.List;

/**
 * ScheduleHandler
 * @apidoc.namespace schedule
 * @apidoc.doc Methods to retrieve information about scheduled actions.
 */
public class ScheduleHandler extends BaseHandler {

    /**
     * Cancel all actions in given list. If an invalid action is provided, none of the
     * actions given will be canceled.
     * @param loggedInUser The current user
     * @param actionIds The list of ids for actions to cancel.
     * @return Returns a list of actions with details
     * @throws LookupException Invalid Action ID provided
     *
     * @apidoc.doc Cancel all actions in given list. If an invalid action is provided,
     * none of the actions given will be canceled.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "actionIds")
     * @apidoc.returntype #return_int_success()
     */
    public int cancelActions(User loggedInUser, List<Integer> actionIds) throws LookupException {
        List<Action> actions = new ArrayList<>();
        LocalizationService locService = LocalizationService.getInstance();
        for (Integer actionId : actionIds) {
            Action action = ActionManager.lookupAction(loggedInUser, Long.valueOf(actionId));
            if (action == null) {
                continue;
            }

            for (ServerAction sa : action.getServerActions()) {
                if (ActionFactory.STATUS_PICKED_UP.equals(sa.getStatus())) {
                    throw new UnsupportedOperationException(locService.getMessage("api.schedule.cannotcancelpickedup"));
                }
            }

            actions.add(action);
        }

        try {
            ActionManager.cancelActions(loggedInUser, actions);
            return BaseHandler.VALID;
        }
        catch (ActionIsChildException e) {
            throw new UnsupportedOperationException(String.format("%s%n%s",
                    locService.getMessage("api.schedule.cannotcancelchild"),
                    e.getMessage()));
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Fail specific event on specified system
     * @param loggedInUser The current user
     * @param sid server id
     * @param actionId action id
     * @return int 1 if successfull
     *
     * @apidoc.doc Fail specific event on specified system
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("int", "actionId")
     * @apidoc.returntype #return_int_success()
     */

    public int failSystemAction(User loggedInUser, Integer sid, Integer actionId) {
        return failSystemAction(loggedInUser, sid, actionId,
                "This action has been manually failed by " + loggedInUser.getLogin());
    }

    /**
     * Fail specific event on specified system and let the user provide
     * some info for this fail.
     * @param loggedInUser The current user
     * @param sid server id
     * @param actionId action id
     * @param message some info about this fail
     * @return int 1 if successfull
     *
     *
     * @apidoc.doc Fail specific event on specified system
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "sid")
     * @apidoc.param #param("int", "actionId")
     * @apidoc.param #param("string", "message")
     * @apidoc.returntype #return_int_success()
     */
    public int failSystemAction(User loggedInUser, Integer sid, Integer actionId,
                                 String message) {
        return ActionManager.failSystemAction(loggedInUser, sid.longValue(),
                actionId.longValue(), message);
    }

    /**
     * List all scheduled actions regardless of status.  This includes pending,
     * completed, failed and archived.
     * @param loggedInUser The current user
     * @return Returns a list of actions with details
     *
     * @apidoc.doc Returns a list of all actions.  This includes completed, in progress,
     * failed and archived actions.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     * #return_array_begin()
     *   $ScheduleActionSerializer
     * #array_end()
     */
    @ReadOnly
    public Object[] listAllActions(User loggedInUser) {

        // the second argument is "PageControl". This is not needed for the api usage;
        // therefore, null will be used.
        return ActionManager.allActions(loggedInUser, null).toArray();
    }

    /**
     * List the scheduled actions that have succeeded.
     * @param loggedInUser The current user
     * @return Returns a list of actions with details
     *
     * @apidoc.doc Returns a list of actions that have completed successfully.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     * #return_array_begin()
     *   $ScheduleActionSerializer
     * #array_end()
     */
    @ReadOnly
    public Object[] listCompletedActions(User loggedInUser) {
        // the second argument is "PageControl". This is not needed for the api usage;
        // therefore, null will be used.
        DataResult<ScheduledAction> dr = ActionManager.completedActions(loggedInUser, null);
        return dr.toArray();
    }

    /**
     * List the scheduled actions that are in progress.
     * @param loggedInUser The current user
     * @return Returns a list of actions with details
     *
     * @apidoc.doc Returns a list of actions that are in progress.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     * #return_array_begin()
     *   $ScheduleActionSerializer
     * #array_end()
     */
    @ReadOnly
    public Object[] listInProgressActions(User loggedInUser) {
        // the second argument is "PageControl". This is not needed for the api usage;
        // therefore, null will be used.
        DataResult<ScheduledAction> dr = ActionManager.pendingActions(loggedInUser, null);
        return dr.toArray();
    }

    /**
     * List the scheduled actions that have failed.
     * @param loggedInUser The current user
     * @return Returns a list of actions with details
     *
     * @apidoc.doc Returns a list of actions that have failed.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     * #return_array_begin()
     *   $ScheduleActionSerializer
     * #array_end()
     */
    @ReadOnly
    public Object[] listFailedActions(User loggedInUser) {
        // the second argument is "PageControl". This is not needed for the api usage;
        // therefore, null will be used.
        DataResult<ScheduledAction> dr = ActionManager.failedActions(loggedInUser, null);
        return dr.toArray();
    }

    /**
     * List the scheduled actions that have been archived.
     * @param loggedInUser The current user
     * @return Returns a list of actions with details
     *
     * @apidoc.doc Returns a list of actions that have been archived.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     * #return_array_begin()
     *   $ScheduleActionSerializer
     * #array_end()
     */
    @ReadOnly
    public Object[] listArchivedActions(User loggedInUser) {
        // the second argument is "PageControl". This is not needed for the api usage;
        // therefore, null will be used.
        return ActionManager.archivedActions(loggedInUser, null).toArray();
    }

    /**
     * List all the scheduled actions that have been archived.
     * @param loggedInUser The current user
     * @return Returns a list of actions with details
     *
     * @apidoc.doc Returns a list of actions that have been archived.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     * #return_array_begin()
     *   $ScheduleActionSerializer
     * #array_end()
     */
    @ReadOnly
    public Object[] listAllArchivedActions(User loggedInUser) {
        // the second argument is "PageControl". This is not needed for the api usage;
        // therefore, null will be used.
        return ActionManager.allArchivedActions(loggedInUser, null).toArray();
    }

    /**
     * List the systems that have completed a specific action.
     * @param loggedInUser The current user
     * @param actionId The id of the action.
     * @return Returns a list of systems along with details
     *
     * @apidoc.doc Returns a list of systems that have completed a specific action.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "actionId")
     * @apidoc.returntype
     * #return_array_begin()
     *   $ScheduleSystemSerializer
     * #array_end()
     */
    @ReadOnly
    public Object[] listCompletedSystems(User loggedInUser, Integer actionId) {
        Long aid = actionId.longValue();
        Action action = ActionManager.lookupAction(loggedInUser, aid);
        // the third argument is "PageControl". This is not needed for the api usage;
        // therefore, null will be used.
        DataResult<ActionedSystem> dr = ActionManager.completedSystems(loggedInUser, action, null);
        dr.elaborate();

        return dr.toArray();
    }

    /**
     * List all the scheduled actions that have been completed.
     * @param loggedInUser The current user
     * @return Returns a list of actions with details
     *
     * @apidoc.doc Returns a list of actions that have been completed.
     * @apidoc.param #session_key()
     * @apidoc.returntype
     * #return_array_begin()
     *   $ScheduleActionSerializer
     * #array_end()
     */
    @ReadOnly
    public Object[] listAllCompletedActions(User loggedInUser) {
        // the second argument is "PageControl". This is not needed for the api usage;
        // therefore, null will be used.
        return ActionManager.allCompletedActions(loggedInUser, null).toArray();
    }

    /**
     * List the systems that have a specific action in progress.
     * @param loggedInUser The current user
     * @param actionId The id of the action.
     * @return Returns a list of systems along with details
     *
     * @apidoc.doc Returns a list of systems that have a specific action in progress.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "actionId")
     * @apidoc.returntype
     * #return_array_begin()
     *   $ScheduleSystemSerializer
     * #array_end()
     */
    @ReadOnly
    public Object[] listInProgressSystems(User loggedInUser, Integer actionId) {
        Long aid = actionId.longValue();
        Action action = ActionManager.lookupAction(loggedInUser, aid);
        // the third argument is "PageControl". This is not needed for the api usage;
        // therefore, null will be used.
        DataResult<ActionedSystem> dr = ActionManager.inProgressSystems(loggedInUser, action, null);
        dr.elaborate();

        return dr.toArray();
    }

    /**
     * List the systems that have failed a specific action.
     * @param loggedInUser The current user
     * @param actionId The id of the action.
     * @return Returns a list of systems along with details
     *
     * @apidoc.doc Returns a list of systems that have failed a specific action.
     * @apidoc.param #session_key()
     * @apidoc.param #param("int", "actionId")
     * @apidoc.returntype
     * #return_array_begin()
     *   $ScheduleSystemSerializer
     * #array_end()
     */
    @ReadOnly
    public Object[] listFailedSystems(User loggedInUser, Integer actionId) {
        Long aid = actionId.longValue();
        Action action = ActionManager.lookupAction(loggedInUser, aid);
        // the third argument is "PageControl". This is not needed for the api usage;
        // therefore, null will be used.
        DataResult<ActionedSystem> dr = ActionManager.failedSystems(loggedInUser, action, null);
        dr.elaborate();
        return dr.toArray();
    }

    /**
     * Reschedule all actions in the given list.
     * @param loggedInUser The current user
     * @param actionIds The list of ids for actions to reschedule.
     * @param onlyFailed only reschedule failed actions
     * @return Returns a list of actions with details
     * @throws FaultException A FaultException is thrown if one of the actions provided
     * is invalid.
     *
     * @apidoc.doc Reschedule all actions in the given list.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "actionIds")
     * @apidoc.param #param_desc("boolean", "onlyFailed",
     *               "True to only reschedule failed actions, False to reschedule all")
     * @apidoc.returntype #return_int_success()
     */
    public int rescheduleActions(User loggedInUser, List<Integer> actionIds,
            boolean onlyFailed) throws FaultException {
        try {
            for (Integer actionId : actionIds) {
                Action action =
                        ActionManager.lookupAction(loggedInUser, Long.valueOf(actionId));
                if (action != null) {
                    ActionManager.rescheduleAction(action, onlyFailed);
                }
            }

            return BaseHandler.VALID;
        }
        catch (com.redhat.rhn.taskomatic.TaskomaticApiException e) {
            throw new TaskomaticApiException(e.getMessage());
        }
    }

    /**
     * Archive all actions in the given list.
     * @param loggedInUser The current user
     * @param actionIds The list of ids for actions to archive.
     * @return Returns a integer 1 on success
     * @throws FaultException A FaultException is thrown if one of the actions provided
     * is invalid.
     *
     * @apidoc.doc Archive all actions in the given list.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "actionIds")
     * @apidoc.returntype #return_int_success()
     */
    public int archiveActions(User loggedInUser, List<Integer> actionIds)
            throws FaultException {
        for (Integer actionId : actionIds) {
            Action action = ActionManager.lookupAction(loggedInUser, Long.valueOf(actionId));
            if (action != null) {
                action.setArchived(1L);
            }
        }
        return 1;
    }

    /**
     * Delete all archived actions in the given list.
     * @param loggedInUser The current user
     * @param actionIds The list of ids for actions to delete.
     * @return Returns a integer 1 on success
     * @throws FaultException In case of an error
     *
     * @apidoc.doc Delete all archived actions in the given list.
     * @apidoc.param #session_key()
     * @apidoc.param #array_single("int", "actionIds")
     * @apidoc.returntype #return_int_success()
     */
    public int deleteActions(User loggedInUser, List<Integer> actionIds) {
        ActionManager.deleteActionsById(loggedInUser, actionIds);
        return 1;
    }
}

