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

package com.redhat.rhn.domain.action.cluster;

import com.redhat.rhn.common.validator.ValidatorError;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionType;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.server.MinionServer;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.action.ActionManager;
import com.redhat.rhn.manager.common.UninitializedCommandException;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;
import com.suse.manager.model.clusters.Cluster;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.Date;
import java.util.Optional;
import java.util.function.Consumer;

public class ClusterActionCommand<T extends BaseClusterAction> {

    private static final Logger LOG = Logger.getLogger(ClusterActionCommand.class);

    private static TaskomaticApi taskomaticApi = new TaskomaticApi();

    private MinionServer targetSystem;
    private ActionType actionType;
    private Optional<User> user;
    private Org org;
    private Date scheduleDate;
    private Optional<ActionChain> actionChain;
    private String name;
    private T action;
    private Consumer<T> extraParams;

    /**
     * Constructor.
     * @param userIn the user
     * @param orgIn the org
     * @param scheduleDateIn the date to schedule the execution
     * @param actionChainIn the action chain (optional)
     * @param actionTypeIn the action type
     * @param targetSystemIn the target system
     * @param clusterIn the cluster
     * @param nameIn the name of the action
     * @param extraParamsIn extra params to set
     */
    public ClusterActionCommand(Optional<User> userIn, Org orgIn, Date scheduleDateIn,
                                Optional<ActionChain> actionChainIn, ActionType actionTypeIn,
                                MinionServer targetSystemIn, Cluster clusterIn, String nameIn,
                                Consumer<T> extraParamsIn) {
        this.targetSystem = targetSystemIn;
        this.actionType = actionTypeIn;
        this.user = userIn;
        this.org = orgIn;
        this.scheduleDate = scheduleDateIn;
        this.actionChain = actionChainIn;
        this.name = nameIn;
        this.cluster = clusterIn;
        this.extraParams = extraParamsIn;
    }

    /**
     * @return cluster to get
     */
    public Cluster getCluster() {
        return cluster;
    }

    /**
     * @param clusterIn to set
     */
    public void setCluster(Cluster clusterIn) {
        this.cluster = clusterIn;
    }

    private Cluster cluster;

    /**
     * @return user to get
     */
    public Optional<User> getUser() {
        return user;
    }

    /**
     * @param userIn to set
     */
    public void setUser(Optional<User> userIn) {
        this.user = userIn;
    }

    /**
     * @return scheduleDate to get
     */
    public Date getScheduleDate() {
        return scheduleDate;
    }

    /**
     * @param scheduleDateIn to set
     */
    public void setScheduleDate(Date scheduleDateIn) {
        this.scheduleDate = scheduleDateIn;
    }

    /**
     * @return actionChain to get
     */
    public Optional<ActionChain> getActionChain() {
        return actionChain;
    }

    /**
     * @param actionChainIn to set
     */
    public void setActionChain(Optional<ActionChain> actionChainIn) {
        this.actionChain = actionChainIn;
    }

    /**
     * @return actionType to get
     */
    public ActionType getActionType() {
        return actionType;
    }

    /**
     * @param actionTypeIn to set
     */
    public void setActionType(ActionType actionTypeIn) {
        this.actionType = actionTypeIn;
    }

    /**
     * @return targetSystem to get
     */
    public MinionServer getTargetSystem() {
        return targetSystem;
    }

    /**
     * @return name to get
     */
    public String getName() {
        return name;
    }

    /**
     * @param nameIn to set
     */
    public void setName(String nameIn) {
        this.name = nameIn;
    }

    /**
     * @param targetSystemIn to set
     */
    public void setTargetSystem(MinionServer targetSystemIn) {
        this.targetSystem = targetSystemIn;
    }

    /**
     * @return action to get
     */
    public T getAction() {
        return action;
    }

    /**
     * @return org to get
     */
    public Org getOrg() {
        return org;
    }

    /**
     * @param orgIn to set
     */
    public void setOrg(Org orgIn) {
        this.org = orgIn;
    }

    /**
     * @param taskomaticApiIn to set
     */
    public static void setTaskomaticApi(TaskomaticApi taskomaticApiIn) {
        ClusterActionCommand.taskomaticApi = taskomaticApiIn;
    }

    /**
     * Create and store the action.
     * @return null or a ValidationError
     * @throws UninitializedCommandException
     * @throws TaskomaticApiException
     */
    public ValidatorError store()
            throws UninitializedCommandException, TaskomaticApiException {
        if (this.getTargetSystem() == null) {
            throw new UninitializedCommandException("No targetSystem for " +
                    "ClusterActionCommand");
        }

        LOG.debug("store() called.");
        BaseClusterAction clusterAction =
                (BaseClusterAction) ActionFactory.createAction(this.getActionType());
        String actionName = this.getActionType().getName().replaceAll("\\.$", "");

        clusterAction.setName(actionName + ": " + this.getName());
        clusterAction.setOrg(this.getUser().map(User::getOrg).orElse(getOrg()));
        clusterAction.setSchedulerUser(this.getUser().orElse(null));
        clusterAction.setEarliestAction(this.getScheduleDate());
        clusterAction.setCluster(this.getCluster());

        if (LOG.isDebugEnabled()) {
            LOG.debug("clusterAction.name: " + clusterAction.getName() + " cluster: " +
                    getCluster().getLabel());
        }

        if (extraParams != null) {
            extraParams.accept((T)clusterAction);
        }

        LOG.debug("saving clusterAction.");
        schedule(clusterAction, getTargetSystem(), getActionChain());

        action = (T)clusterAction;
        return null;
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
            throw new UninitializedCommandException("No targetSystem for action");
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


}
