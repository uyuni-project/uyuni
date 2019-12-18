/**
 * Copyright (c) 2009--2010 Red Hat, Inc.
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
package com.redhat.rhn.manager.system;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.action.virtualization.BaseVirtualizationAction;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.common.UninitializedCommandException;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * Command class for schedule virtualization-related actions
 */
public class VirtualizationActionCommand {

    private static final Logger LOG = Logger.getLogger(VirtualizationActionCommand.class);

    private static TaskomaticApi taskomaticApi = new TaskomaticApi();

    private User user;
    private Date scheduleDate;
    private Optional<ActionChain> actionChain;
    private ActionType actionType;
    private Server targetSystem;
    private String uuid;
    private String name;
    private Map context;
    private Action action;


    /**
     * Constructor
     * @param userIn User performing the action
     * @param dateIn Earliest execution date/time for the action
     * @param actionChainIn the action chain to wire the action to or <code>null</code>.
     * @param actionTypeIn ActionType of the action being performed.
     * @param targetSystemIn The host system for this action.
     * @param uuidIn String representation of the target instance's UUID
     * @param nameIn name of the target instance for display purpose
     * @param contextIn Map of optional action arguments.
     */
    public VirtualizationActionCommand(User userIn, Date dateIn, Optional<ActionChain> actionChainIn,
                                       ActionType actionTypeIn, Server targetSystemIn, String uuidIn,
                                       String nameIn, Map contextIn) {
        this.setUser(userIn);
        this.setScheduleDate(dateIn);
        this.setActionChain(actionChainIn);
        this.setActionType(actionTypeIn);
        this.setTargetSystem(targetSystemIn);
        this.setUuid(uuidIn);
        this.setName(nameIn);
        this.setContext(contextIn);
    }

    /**
     * Stores virtualization action to be picked up by the client.
     * @return null ALWAYS!
     * @throws UninitializedCommandException if the target system is null.
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public ValidatorError store()
        throws UninitializedCommandException, TaskomaticApiException {
        if (this.getTargetSystem() == null) {
            throw new UninitializedCommandException("No targetSystem for " +
                                                    "VirtualizationActionCommand");
        }

        LOG.debug("store() called.");

        LOG.debug("creating virtAction");
        BaseVirtualizationAction virtAction =
            (BaseVirtualizationAction) ActionFactory.createAction(this.getActionType());
        String actionName = this.getActionType().getName().replaceAll("\\.$", "");
        virtAction.setName(actionName + ": " + this.getName());
        virtAction.setOrg(this.getUser().getOrg());
        virtAction.setSchedulerUser(this.getUser());
        virtAction.setEarliestAction(this.getScheduleDate());
        virtAction.setUuid(this.getUuid());

        if (LOG.isDebugEnabled()) {
            LOG.debug("virtAction.name: " + virtAction.getName() + " uuid: " +
                    virtAction.getUuid());
        }

        virtAction.extractParameters(getContext());

        LOG.debug("saving virtAction.");
        schedule(virtAction, getTargetSystem(), getActionChain());

        action = virtAction;
        return null;
    }

    /**
     * gets the action.  Should return null before store() is called.
     * @return the action
     */
    public Action getAction() {
        return action;
    }

    /**
     * Find the appropriate action label for a given action name and current state.
     *
     * @param currentState The current state of the instance
     * @param actionName The name of the action the user wants to perform
     *                   from the button on the form in the web UI.
     * @return The ActionType of the (RHN) action that should be performed.
     */
    public static ActionType lookupActionType(String currentState, String actionName) {
        return VirtualizationActionMap.lookupActionType(currentState, actionName);
    }

    /**
     * Gets the value of user
     *
     * @return the value of user
     */
    public User getUser() {
        return this.user;
    }

    /**
     * Sets the value of user
     *
     * @param argUser Value to assign to this.user
     */
    public void setUser(User argUser) {
        this.user = argUser;
    }

    /**
     * Get the context.
     *
     * @return the context.
     */
    public Map getContext() {
        return context;
    }

    /**
     * Sets the context.
     *
     * @param contextIn Context to set.
     */
    public void setContext(Map contextIn) {
        context = contextIn;
    }

    /**
     * Gets the value of scheduleDate
     *
     * @return the value of scheduleDate
     */
    public Date getScheduleDate() {
        return this.scheduleDate;
    }

    /**
     * Sets the value of scheduleDate
     *
     * @param argScheduleDate Value to assign to this.scheduleDate
     */
    public void setScheduleDate(Date argScheduleDate) {
        this.scheduleDate = argScheduleDate;
    }


    /**
     * @return Returns the actionChain.
     */
    public Optional<ActionChain> getActionChain() {
        return actionChain;
    }


    /**
     * @param actionChainIn The actionChain to set.
     */
    public void setActionChain(Optional<ActionChain> actionChainIn) {
        actionChain = actionChainIn;
    }

    /**
     * Gets the value of actionType
     *
     * @return the value of actionType
     */
    public ActionType getActionType() {
        return this.actionType;
    }

    /**
     * Sets the value of actionType
     *
     * @param argActionType Value to assign to this.actionType
     */
    public void setActionType(ActionType argActionType) {
        this.actionType = argActionType;
    }

    /**
     * Gets the value of targetSystem
     *
     * @return the value of targetSystem
     */
    public Server getTargetSystem() {
        return this.targetSystem;
    }

    /**
     * HashSets the value of targetSystem
     *
     * @param argTargetSystems Value to assign to this.targetSystem
     */
    public void setTargetSystem(Server argTargetSystems) {
        this.targetSystem = argTargetSystems;
    }

    /**
     * Gets the value of uuid
     *
     * @return the value of uuid
     */
    public String getUuid() {
        return this.uuid;
    }

    /**
     * HashSets the value of uuid
     *
     * @param argUuid Value to assign to this.uuid
     */
    public void setUuid(String argUuid) {
        this.uuid = argUuid;
    }


    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }


    /**
     * @param nameIn The name to set.
     */
    public void setName(String nameIn) {
        name = nameIn;
    }

    /**
     * Helper function to schedule actions.
     *
     * @param action the action to schedule
     * @param targetSystem the system to run the action on
     * @param actionChain an optional action chain to append the action to
     *
     * @throws TaskomaticApiException if an error happened while scheduling
     */
    public static void schedule(Action action, Server targetSystem, Optional<ActionChain> actionChain)
            throws TaskomaticApiException {
        if (targetSystem == null) {
            throw new UninitializedCommandException("No targetSystem for virtualization action");
        }

        LOG.debug("schedule() called.");
        ActionFactory.save(action);

        if (actionChain == null || !actionChain.isPresent()) {
            ActionManager.scheduleForExecution(action, Collections.singleton(targetSystem.getId()));
            taskomaticApi.scheduleActionExecution(action);
        }
        else {
            Integer sortOrder = ActionChainFactory.getNextSortOrderValue(actionChain.get());
            ActionChainFactory.queueActionChainEntry(action, actionChain.get(),
                    targetSystem.getId(), sortOrder);
        }
    }

    /**
     * Set the {@link TaskomaticApi} instance to use. Only needed for unit tests.
     * @param taskomaticApiIn the {@link TaskomaticApi}
     */
    public static void setTaskomaticApi(TaskomaticApi taskomaticApiIn) {
        taskomaticApi = taskomaticApiIn;
    }
}

