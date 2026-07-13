/*
 * Copyright (c) 2026 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */
package com.redhat.rhn.domain.action.server;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.redhat.rhn.domain.action.ActionStatus;
import com.redhat.rhn.domain.action.ActionTypeEnum;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.manager.system.SystemManager;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.query.MutationQuery;
import org.hibernate.query.Query;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerActionFactory extends HibernateFactory {
    private static final Logger LOG = LogManager.getLogger(ServerActionFactory.class);

    private ServerActionFactory() {
        //utility classes should not have a public constructor
    }

    @Override
    protected Logger getLogger() {
        return LOG;
    }

    // history of the following two constants:
    // REMAINING_TRIES = 10L was extracted from the ActionManager.scheduleAction method, then moved to
    // ActionFactory.createAddServerAction and then to ServerActionFactory
    // There was a comment "hmm 10?". Not sure what the hesitation is, but that comment was retained.
    // REMAINING_TRIES_FROM_PERL = 5L is commonly seen around with a comment like "arbitrary number from perl"
    // There is a chance both values could be merged into one, but no-one knows
    public static final Long REMAINING_TRIES = 10L;
    public static final Long REMAINING_TRIES_FROM_PERL = 5L;

    /**
     * Lookup a List of ServerAction objects for a given Server.
     *
     * @param serverIn you want to limit the list of Actions to
     * @return List of ServerAction objects
     */
    public static List<ServerAction> listServerActionsForServer(Server serverIn) {
        return listServerActionsForServer(serverIn, null, null, null, null);
    }

    /**
     * Lookup a List of ServerAction objects for a given Server.
     *
     * @param serverIn             you want to limit the list of Actions to
     * @param actionTypeNameIn     you want to limit the list of Actions to
     * @param minimumCreatedDateIn you want to limit the completion date after
     * @return List of ServerAction objects
     */
    public static List<ServerAction> listServerActionsForServer(Server serverIn,
                                                                String actionTypeNameIn,
                                                                Date minimumCreatedDateIn) {
        return listServerActionsForServer(serverIn, null, minimumCreatedDateIn, actionTypeNameIn, null);
    }

    /**
     * Lookup a List of ServerAction objects for a given Server and Action Type.
     *
     * @param serverIn         you want to limit the list of Actions to
     * @param actionTypeEnumIn you want to limit the list of Actions to
     * @return List of ServerAction objects
     */
    public static List<ServerAction> listServerActionsForServer(Server serverIn,
                                                                ActionTypeEnum actionTypeEnumIn) {
        return listServerActionsForServer(serverIn, actionTypeEnumIn, null, null, null);
    }

    /**
     * Lookup a List of ServerAction objects in the given status for a given Server.
     *
     * @param serverIn     you want to limit the list of Actions to
     * @param statusIn to filter the ServerActions by
     * @return List of ServerAction objects
     */
    public static List<ServerAction> listServerActionsForServer(Server serverIn, ActionStatus statusIn) {
        return listServerActionsForServer(serverIn, null, null, null, List.of(statusIn));
    }

    /**
     * Lookup a List of ServerAction objects in the given states for a given Server.
     *
     * @param serverIn     you want to limit the list of Actions to
     * @param statusListIn to filter the ServerActoins by
     * @return List of ServerAction objects
     */
    public static List<ServerAction> listServerActionsForServer(Server serverIn,
                                                                List<ActionStatus> statusListIn) {
        return listServerActionsForServer(serverIn, null, null, null, statusListIn);
    }

    /**
     * Lookup a List of ServerAction objects in the given states for a given Server.
     *
     * @param serverIn             you want to limit the list of Actions to
     * @param statusListIn         to filter the ServerActions by
     * @param minimumCreatedDateIn to filter the ServerActions by
     * @return List of ServerAction objects
     */
    public static List<ServerAction> listServerActionsForServer(Server serverIn,
                                                                List<ActionStatus> statusListIn,
                                                                Date minimumCreatedDateIn) {
        return listServerActionsForServer(serverIn, null, minimumCreatedDateIn, null, statusListIn);
    }

    private static List<ServerAction> listServerActionsForServer(Server serverIn,
                                                                 ActionTypeEnum actionTypeEnumIn,
                                                                 Date minimumCreatedDateIn,
                                                                 String actionTypeNameIn,
                                                                 List<ActionStatus> statusListIn) {

        Map<String, Object> parameters = new HashMap<>();
        StringBuilder queryString = new StringBuilder("FROM ServerAction AS sa WHERE sa.server = :server");
        parameters.put("server", serverIn);

        if (null != actionTypeEnumIn) {
            queryString.append(" AND ").append("sa.parentAction.actionType.label = :actionTypeLabel");
            parameters.put("actionTypeLabel", actionTypeEnumIn.getLabel());
        }

        if (null != minimumCreatedDateIn) {
            queryString.append(" AND ").append("sa.created >= :date");
            parameters.put("date", minimumCreatedDateIn);
        }

        if (null != actionTypeNameIn) {
            queryString.append(" AND ").append("sa.parentAction.actionType.name = :actionTypeName");
            parameters.put("actionTypeName", actionTypeNameIn);
        }

        String statusListParam = null;
        if (null != statusListIn) {
            queryString.append(" AND ").append("sa.status IN (:statusList)");
            statusListParam = "statusList";
        }

        Session session = HibernateFactory.getSession();
        Query<ServerAction> query = session.createQuery(queryString.toString(), ServerAction.class);
        for (Map.Entry<String, Object> param : parameters.entrySet()) {
            query.setParameter(param.getKey(), param.getValue());
        }
        if (null != statusListIn) {
            query.setParameterList(statusListParam, statusListIn);
        }
        return query.list();
    }

    /**
     * Reschedule All Failed Server Actions associated with an action
     *
     * @param parentActionIn the action whose server actions you are rescheduling
     * @param tries          the number of tries to set
     */
    public static void rescheduleFailedServerActions(Action parentActionIn, Long tries) {
        rescheduleServerAction(parentActionIn, tries, null, ActionFactory.STATUS_FAILED);
    }

    /**
     * Reschedule All Server Actions associated with an action
     *
     * @param parentActionIn the action whose server actions you are rescheduling
     * @param tries          the number of tries to set (should be set to 5)
     */
    public static void rescheduleAllServerActions(Action parentActionIn, Long tries) {
        rescheduleServerAction(parentActionIn, tries, null, null);
    }

    /**
     * Reschedule Server Action associated with an action and system
     *
     * @param parentActionIn the action whose server actions you are rescheduling
     * @param tries          the number of tries to set (should be set to 5)
     * @param serverIdIn     system id of action we want reschedule
     */
    public static void rescheduleSingleServerAction(Action parentActionIn, Long tries,
                                                    Long serverIdIn) {
        rescheduleServerAction(parentActionIn, tries, serverIdIn, null);
    }

    private static void rescheduleServerAction(Action parentActionIn, Long tries, Long serverIdIn,
                                               ActionStatus actionStatusIn) {
        parentActionIn.setEarliestAction(new Date());
        HibernateFactory.getSession().persist(parentActionIn);

        Map<String, Object> parameters = new HashMap<>();
        StringBuilder queryString = new StringBuilder("""
                UPDATE ServerAction sa
                SET    sa.status = :queued,
                       sa.remainingTries = :tries,
                       sa.pickupTime = null,
                       sa.completionTime = null,
                       resultCode = null,
                       resultMsg = null
                WHERE  sa.parentAction = :parentAction
                """);
        if (null != serverIdIn) {
            queryString.append(" AND ").append("sa.serverId = :serverId");
            parameters.put("serverId", serverIdIn);
        }
        if (null != actionStatusIn) {
            queryString.append(" AND ").append("sa.status = :status");
            parameters.put("status", actionStatusIn);
        }

        MutationQuery query = HibernateFactory.getSession().createMutationQuery(queryString.toString());
        query.setParameter("queued", ActionFactory.STATUS_QUEUED);
        query.setParameter("tries", tries);
        query.setParameter("parentAction", parentActionIn);
        for (Map.Entry<String, Object> param : parameters.entrySet()) {
            query.setParameter(param.getKey(), param.getValue());
        }

        query.executeUpdate();
        parentActionIn.removeInvalidResults();
        parentActionIn.getServerActions().stream()
                .map(ServerAction::getServerId)
                .forEach(SystemManager::updateSystemOverview);
    }

    /**
     * Update the status of several ServerAction rows identified by server and action IDs.
     *
     * @param actionIn  associated action of ServerAction records
     * @param serverIds server Ids for which action is scheduled
     * @param status    {@link ActionStatus} object that needs to be set
     */
    public static void updateServerActions(Action actionIn, List<Long> serverIds, ActionStatus status) {
        LOG.debug("Action status {} is going to be set for these servers: {}", status.getName(), serverIds);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("action_id", actionIn.getId());
        parameters.put("status", status.getId());
        parameters.put("completed", ActionFactory.STATUS_COMPLETED.getId());
        parameters.put("failed", ActionFactory.STATUS_FAILED.getId());

        StringBuilder queryString = new StringBuilder("UPDATE rhnServerAction SET status = :status ");
        if (status.isPickedUp()) {
            queryString.append(", pickup_time = current_timestamp ");
        }
        queryString.append("""
                WHERE  action_id  = :action_id
                AND    server_id  IN (:server_ids)
                AND    status NOT IN (:completed, :failed)
                """);

        HibernateFactory.udpateByIds(serverIds, queryString.toString(), "server_ids", parameters);
        serverIds.forEach(SystemManager::updateSystemOverview);
    }

    /**
     * Creates a ServerAction and adds it to an Action
     * @param serverIdIn The server id
     * @param parentActionIn The parent action
     */
    public static void addServerToAction(Long serverIdIn, Action parentActionIn) {
        addServerToActionPrivate(ServerFactory.lookupByIdAndOrg(serverIdIn, parentActionIn.getOrg()), parentActionIn,
                REMAINING_TRIES_FROM_PERL);
    }

    /**
     * Creates a ServerAction and adds it to an Action
     * @param serverIn The server
     * @param parentActionIn The parent action
     */
    public static void addServerToAction(Server serverIn, Action parentActionIn) {
        addServerToActionPrivate(serverIn, parentActionIn, REMAINING_TRIES_FROM_PERL);
    }

    /**
     * Creates and adds a ServerAction to an Action
     * @param serverIn the Server associated with the created ServerAction
     * @param parentActionIn the type of Action we want to create
     */
    public static void createAddServerAction(Server serverIn, Action parentActionIn) {
        addServerToActionPrivate(serverIn, parentActionIn, REMAINING_TRIES);
    }

    private static void addServerToActionPrivate(Server serverIn, Action parentActionIn, long tries) {
        ServerAction sa = new ServerAction();
        sa.setCreated(new Date());
        sa.setModified(new Date());
        sa.setStatusQueued();
        sa.setServerWithCheck(serverIn);
        sa.setParentActionWithCheck(parentActionIn);
        sa.setRemainingTries(tries);
        parentActionIn.addServerAction(sa);
    }

}
