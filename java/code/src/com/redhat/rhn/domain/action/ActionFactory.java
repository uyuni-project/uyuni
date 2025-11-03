/*
 * Copyright (c) 2017--2025 SUSE LLC
 * Copyright (c) 2009--2017 Red Hat, Inc.
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
package com.redhat.rhn.domain.action;

import static java.util.stream.Collectors.toSet;

import com.redhat.rhn.common.db.datasource.CallableMode;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.hibernate.HibernateRuntimeException;
import com.redhat.rhn.domain.action.ansible.InventoryAction;
import com.redhat.rhn.domain.action.ansible.PlaybookAction;
import com.redhat.rhn.domain.action.appstream.AppStreamAction;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsAction;
import com.redhat.rhn.domain.action.config.ConfigAction;
import com.redhat.rhn.domain.action.config.ConfigDeployAction;
import com.redhat.rhn.domain.action.config.ConfigDiffAction;
import com.redhat.rhn.domain.action.config.ConfigRevisionAction;
import com.redhat.rhn.domain.action.config.ConfigRevisionActionResult;
import com.redhat.rhn.domain.action.config.ConfigUploadAction;
import com.redhat.rhn.domain.action.config.ConfigUploadMtimeAction;
import com.redhat.rhn.domain.action.config.ConfigVerifyAction;
import com.redhat.rhn.domain.action.config.DaemonConfigAction;
import com.redhat.rhn.domain.action.dup.DistUpgradeAction;
import com.redhat.rhn.domain.action.errata.ActionPackageDetails;
import com.redhat.rhn.domain.action.errata.ErrataAction;
import com.redhat.rhn.domain.action.image.DeployImageAction;
import com.redhat.rhn.domain.action.kickstart.KickstartGuestToolsChannelSubscriptionAction;
import com.redhat.rhn.domain.action.kickstart.KickstartHostToolsChannelSubscriptionAction;
import com.redhat.rhn.domain.action.kickstart.KickstartInitiateAction;
import com.redhat.rhn.domain.action.kickstart.KickstartInitiateGuestAction;
import com.redhat.rhn.domain.action.kickstart.KickstartScheduleSyncAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageActionDetails;
import com.redhat.rhn.domain.action.rhnpackage.PackageAutoUpdateAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageDeltaAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageLockAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageRefreshListAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageRemoveAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageRunTransactionAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageUpdateAction;
import com.redhat.rhn.domain.action.rhnpackage.PackageVerifyAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesAction;
import com.redhat.rhn.domain.action.salt.ApplyStatesActionDetails;
import com.redhat.rhn.domain.action.salt.build.ImageBuildAction;
import com.redhat.rhn.domain.action.salt.inspect.ImageInspectAction;
import com.redhat.rhn.domain.action.scap.ScapAction;
import com.redhat.rhn.domain.action.script.ScriptActionDetails;
import com.redhat.rhn.domain.action.script.ScriptRunAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.action.supportdata.SupportDataAction;
import com.redhat.rhn.domain.config.ConfigRevision;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.rhnpackage.PackageEvr;
import com.redhat.rhn.domain.rhnpackage.PackageEvrFactory;
import com.redhat.rhn.domain.rhnset.RhnSet;
import com.redhat.rhn.domain.server.MinionServerFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.server.ServerFactory;
import com.redhat.rhn.domain.server.ServerHistoryEvent;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.manager.rhnset.RhnSetManager;
import com.redhat.rhn.manager.system.SystemManager;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.redhat.rhn.taskomatic.TaskomaticApiException;

import com.suse.manager.maintenance.MaintenanceManager;

import org.apache.commons.collections.ListUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.persistence.Tuple;

/**
 * ActionFactory - the singleton class used to fetch and store
 * com.redhat.rhn.domain.action.Action objects from the
 * database.
 */
public class ActionFactory extends HibernateFactory {

    private static ActionFactory singleton = new ActionFactory();
    private static final Logger LOG = LogManager.getLogger(ActionFactory.class);
    private static Set<String> actionArchTypes;
    private static final TaskomaticApi TASKOMATIC_API = new TaskomaticApi();
    private static MaintenanceManager maintenanceManager = new MaintenanceManager();

    /**
     * This was extracted to a constant from the
     * ActionManager.scheduleAction(User, Server, ActionType, String, Date) method, then moved to
     * ActionFactory.createAddServerAction(Server, Action);
     * At the time it was in there, there was a comment "hmm 10?". Not sure what the hesitation is,
     * but I wanted to retain that comment with regard to this value.
     */
    public static final Long REMAINING_TRIES = 10L;


    private ActionFactory() {
        super();
        setupActionArchTypes();
    }

    private void setupActionArchTypes() {
        synchronized (this) {
            try {
                List<ActionArchType> types = getSession()
                        .createQuery("FROM com.redhat.rhn.domain.action.ActionArchType", ActionArchType.class)
                        .setCacheable(true).list();

                // don't cache the entire ActionArchType bean to avoid
                // any LazyInitializatoinException latter
                actionArchTypes = types.stream()
                        .map(type -> toActionArchTypeKey(type.getActionType().getId(), type.getActionStyle()))
                        .collect(toSet());
            }
            catch (HibernateException he) {
                LOG.error("Error loading ActionArchTypes from DB", he);
                throw new HibernateRuntimeException("Error loading ActionArchTypes from db");
            }
        }
    }

    private static String toActionArchTypeKey(Integer id, String actionStyle) {
        return id + "_" + actionStyle;
    }

    /**
     * Remove an action for an rhnset of system ids with the given label
     * @param actionId the action to remove
     * @param setLabel the set label to pull the ids from
     * @param user the user witht he set
     * @return the number of failed systems to remove an action for.
     * @throws TaskomaticApiException if there was a Taskomatic error
     * (typically: Taskomatic is down)
     */
    public static int removeActionForSystemSet(long actionId, String setLabel, User user)
        throws TaskomaticApiException {

        RhnSet set = RhnSetManager.findByLabel(user.getId(), setLabel, null);
        Set<Server> involvedMinions = MinionServerFactory
                    .lookupByIds(new ArrayList<>(set.getElementValues()))
                    .collect(toSet());
        Set<Server> involvedSystems = new HashSet<>(ServerFactory.lookupByIds(
                new ArrayList<>(set.getElementValues())));
        Action action = ActionFactory.lookupById(actionId);

        TASKOMATIC_API.deleteScheduledActions(Collections.singletonMap(action, involvedMinions));

        return involvedSystems.stream().map(Server::getId).mapToInt(sid -> {
            try {
                removeActionForSystem(actionId, sid);
                SystemManager.updateSystemOverview(sid);
                return 0;
            }
            catch (Exception e) {
                return 1;
            }
        }).sum();
    }

    /**
     * Remove pending action for system
     * @param serverId the server id
     */
    public static void cancelPendingForSystem(Long serverId) {
        SelectMode pending = ModeFactory.getMode("System_queries", "system_pending_actions");
        Map<String, Long> params = new HashMap<>();
        params.put("sid", serverId);
        DataResult<Row> dr = pending.execute(params);

        for (Row action : dr) {
            removeActionForSystem((Long) action.get("id"), serverId);
        }
        SystemManager.updateSystemOverview(serverId);
    }

    /**
     * Remove the system from the passed in Action.
     * @param actionId to process
     * @param sid to remove from Action
     */
    public static void removeActionForSystem(Number actionId, Number sid) {
        CallableMode mode = ModeFactory.getCallableMode("System_queries", "delete_action_for_system");
        Map<String, Object> params = new HashMap<>();
        params.put("action_id", actionId);
        params.put("server_id",  sid);
        mode.execute(params, new HashMap<>());
    }


    /**
     * Get the Logger for the derived class so log messages
     * show up on the correct class
     */
    @Override
    protected Logger getLogger() {
        return LOG;
    }

    /**
     * Create a ConfigRevisionAction for the given server and add it to the parent action.
     * @param revision The config revision to add to the action.
     * @param server The server for the action
     * @param parent The parent action
     */
    public static void addConfigRevisionToAction(ConfigRevision revision, Server server, ConfigAction parent) {
        ConfigRevisionAction cra = new ConfigRevisionAction();
        cra.setConfigRevision(revision);
        cra.setCreated(new Date());
        cra.setModified(new Date());
        cra.setServer(server);
        parent.addConfigRevisionAction(cra);
    }

    /**
     * Creates a ScriptActionDetails which contains an arbitrary script to be
     * run by a ScriptRunAction.
     * @param username Username of script
     * @param groupname Group script runs as
     * @param script Script contents
     * @param timeout script timeout
     * @return ScriptActionDetails containing script to be run by ScriptRunAction
     */
    public static ScriptActionDetails createScriptActionDetails(String username,
            String groupname, Long timeout, String script) {
        ScriptActionDetails sad = new ScriptActionDetails();
        sad.setUsername(username);
        sad.setGroupname(groupname);
        sad.setTimeout(timeout);
        sad.setScript(script.getBytes(StandardCharsets.UTF_8));

        return sad;
    }

    /**
     * Check to see if a server has a pending kickstart scheduled
     * @param serverId server
     * @return true if found, otherwise false
     */
    @SuppressWarnings("unchecked")
    public static boolean doesServerHaveKickstartScheduled(Long serverId) {
        return findFirstPendingActionForServerAndActionType(serverId, "kickstart.initiate").isPresent();
    }

    /**
     * Check if there is a pending product migration in the schedule. Return the
     * action ID if available or null otherwise.
     * @param serverId server
     * @return ID of a possibly scheduled migration or null.
     */
    @SuppressWarnings("unchecked")
    public static Action isMigrationScheduledForServer(Long serverId) {
        return findFirstPendingActionForServerAndActionType(serverId, "distupgrade.upgrade").orElse(null);
    }

    /**
     * Check if the server has a scheudled reboot action. Return the action
     * if available or null otherwise
     * @param serverId server
     * @return reboot Action or null otherwise
     */
    @SuppressWarnings("unchecked")
    public static Action isRebootScheduled(Long serverId) {
        return findFirstPendingActionForServerAndActionType(serverId, "reboot.reboot").orElse(null);
    }

    private static Optional<Action> findFirstPendingActionForServerAndActionType(Long serverId,
                                                                                 String actionTypeLabel) {
        Session session = HibernateFactory.getSession();
        return session.createQuery("""
                        FROM ServerAction AS sa WHERE
                            sa.server.id = :serverId
                            AND sa.parentAction.actionType.label = :label
                            AND status in ( 0, 1 )""", ServerAction.class)
                .setParameter("serverId", serverId)
                .setParameter("label", actionTypeLabel)
                .list().stream()
                .findFirst()
                .map(ServerAction::getParentAction);
    }

    /**
     * Schedules an action for execution on one or more servers (adding rows to
     * rhnServerAction)
     *
     * Also checks if the action scheduled date/time fit in systems maintenance schedules, if there are any assigned.
     *
     * @param action the action
     * @param serverIds server IDs
     */
    public static void scheduleForExecution(Action action, Set<Long> serverIds) {
        maintenanceManager.canActionBeScheduled(serverIds, action);

        Map<String, Object> params = new HashMap<>();
        params.put("status_id", ActionFactory.STATUS_QUEUED.getId());
        params.put("tries", ActionFactory.REMAINING_TRIES);
        params.put("parent_id", action.getId());

        WriteMode m = ModeFactory.getWriteMode("Action_queries", "insert_server_actions");
        List<Long> sidList = new ArrayList<>();
        sidList.addAll(serverIds);
        m.executeUpdate(params, sidList);
    }

    /**
     * Creates a ServerAction and adds it to an Action
     * @param sid The server id
     * @param parent The parent action
     */
    public static void addServerToAction(Long sid, Action parent) {
        addServerToAction(ServerFactory.lookupByIdAndOrg(sid, parent.getOrg()), parent);
    }

    /**
     * Creates a ServerAction and adds it to an Action
     * @param server The server
     * @param parent The parent action
     */
    public static void addServerToAction(Server server, Action parent) {
        ServerAction sa = new ServerAction();
        sa.setCreated(new Date());
        sa.setModified(new Date());
        sa.setStatusQueued();
        sa.setServerWithCheck(server);
        sa.setParentActionWithCheck(parent);
        sa.setRemainingTries(5L); //arbitrary number from perl
        parent.addServerAction(sa);
    }

    /**
     * Creates and adds a ServerAction to an Action
     * @param serverIn the Server associated with the created ServerAction
     * @param actionIn the type of Action we want to create
     */
    public static void createAddServerAction(Server serverIn, Action actionIn) {

        ServerAction sa = new ServerAction();
        sa.setStatusQueued();
        sa.setRemainingTries(REMAINING_TRIES);
        sa.setServerWithCheck(serverIn);

        actionIn.addServerAction(sa);
        //probably not needed, already included in addServerAction?
        sa.setParentActionWithCheck(actionIn);
    }

    /**
     * Creates, saves and returns a new Action
     * @param typeIn the type of Action we want to create
     * @param schedulerUser the user who created this action
     * @param actionName the action name
     * @param earliestAction the earliest execution date
     * @return a saved Action
     */
    public static Action createAndSaveAction(ActionType typeIn, User schedulerUser, String actionName,
                                             Date earliestAction) {
        /*
            We have to re-lookup the type here, because most likely a static final variable
            was passed in.  If we use this and the .reload() gets called below
            if we try to save a new action the instance of the type in the cache
            will be different from the final static variable
            sometimes hibernate is no fun
        */
        ActionType lookedUpType = lookupActionTypeByLabel(typeIn.getLabel());
        Action action = createAction(lookedUpType, schedulerUser, actionName, earliestAction);
        save(action);
        HibernateFactory.getSession().flush();
        return action;
    }

    /**
     * Create a new Action from scratch.
     * @param typeIn the type of Action we want to create
     * @param schedulerUserIn the user who created this action
     * @param earliestIn the earliest execution date
     * @return the Action created
     */
    public static Action createAction(ActionType typeIn, User schedulerUserIn,
                                      Date earliestIn) {
        return createAction(typeIn, schedulerUserIn, typeIn.getName(), schedulerUserIn.getOrg(), earliestIn);
    }

    /**
     * Create a new Action from scratch.
     * @param typeIn the type of Action we want to create
     * @param schedulerUserIn the user who created this action
     * @param actionName the action name
     * @param earliestIn the earliest execution date
     * @return the Action created
     */
    public static Action createAction(ActionType typeIn, User schedulerUserIn, String actionName,
                                      Date earliestIn) {
        return createAction(typeIn, schedulerUserIn, actionName, schedulerUserIn.getOrg(), earliestIn);
    }

    /**
     * Create a new Action from scratch.
     * @param typeIn the type of Action we want to create
     * @param schedulerUserIn the user who created this action
     * @param orgIn the Org of this action
     * @param earliestIn the earliest execution date
     * @return the Action created
     */
    public static Action createAction(ActionType typeIn, User schedulerUserIn, Org orgIn,
                                      Date earliestIn) {
        return createAction(typeIn, schedulerUserIn, typeIn.getName(), orgIn, earliestIn);
    }

    /**
     * Create a new Action from scratch.
     * @param typeIn the type of Action we want to create
     * @param schedulerUserIn the user who created this action
     * @param actionNameIn the action name
     * @param orgIn the Org of this action
     * @param earliestIn the earliest execution date
     * @return the Action created
     */
    public static Action createAction(ActionType typeIn, User schedulerUserIn, String actionNameIn, Org orgIn,
                                      Date earliestIn) {
        Action pa = createAction(typeIn, earliestIn);
        pa.setName(actionNameIn);
        pa.setOrg(orgIn);
        pa.setSchedulerUser(schedulerUserIn);
        return pa;
    }

    /**
     * Create a new Action from scratch.
     * @param typeIn the type of Action we want to create
     * @return the Action created
     */
    public static Action createAction(ActionType typeIn) {
        return createAction(typeIn, new Date());
    }

    /**
     * Create a new Action from scratch with the given earliestIn execution time.
     * @param typeIn the type of Action we want to create
     * @param earliestIn The earliest time that this action can occur.
     * @return the Action created
     */
    public static Action createAction(ActionType typeIn, Date earliestIn) {
        Action retval;

        if (typeIn.equals(TYPE_PACKAGES_REFRESH_LIST)) {
            retval = new PackageRefreshListAction();
        }
        else if (typeIn.equals(TYPE_HARDWARE_REFRESH_LIST)) {
            retval = new HardwareRefreshAction();
        }
        else if (typeIn.equals(TYPE_PACKAGES_UPDATE)) {
            retval = new PackageUpdateAction();
        }
        else if (typeIn.equals(TYPE_PACKAGES_REMOVE)) {
            retval = new PackageRemoveAction();
        }
        else if (typeIn.equals(TYPE_ERRATA)) {
            ErrataAction ea = new ErrataAction();
            ea.setDetails(new ActionPackageDetails(ea, false));
            retval = ea;
        }
        else if (typeIn.equals(TYPE_UP2DATE_CONFIG_GET)) {
            retval = new Up2DateConfigGetAction();
        }
        else if (typeIn.equals(TYPE_UP2DATE_CONFIG_UPDATE)) {
            retval = new Up2DateConfigUpdateAction();
        }
        else if (typeIn.equals(TYPE_PACKAGES_DELTA)) {
            retval = new PackageDeltaAction();
        }
        else if (typeIn.equals(TYPE_REBOOT)) {
            retval = new RebootAction();
        }
        else if (typeIn.equals(TYPE_ROLLBACK_CONFIG)) {
            retval = new RollbackConfigAction();
        }
        else if (typeIn.equals(TYPE_ROLLBACK_LISTTRANSACTIONS)) {
            retval = new RollbackListTransactionsAction();
        }
        else if (typeIn.equals(TYPE_ROLLBACK_ROLLBACK)) {
            retval = new RollbackAction();
        }
        else if (typeIn.equals(TYPE_PACKAGES_AUTOUPDATE)) {
            retval = new PackageAutoUpdateAction();
        }
        else if (typeIn.equals(TYPE_PACKAGES_RUNTRANSACTION)) {
            retval = new PackageRunTransactionAction();
        }
        else if (typeIn.equals(TYPE_CONFIGFILES_UPLOAD)) {
            retval = new ConfigUploadAction();
        }
        else if (typeIn.equals(TYPE_CONFIGFILES_DEPLOY)) {
            retval = new ConfigDeployAction();
        }
        else if (typeIn.equals(TYPE_CONFIGFILES_VERIFY)) {
            retval = new ConfigVerifyAction();
        }
        else if (typeIn.equals(TYPE_CONFIGFILES_DIFF)) {
            retval = new ConfigDiffAction();
        }
        else if (typeIn.equals(TYPE_KICKSTART_INITIATE)) {
            retval = new KickstartInitiateAction();
        }
        else if (typeIn.equals(TYPE_KICKSTART_SCHEDULE_SYNC)) {
            retval = new KickstartScheduleSyncAction();
        }
        else if (typeIn.equals(TYPE_CONFIGFILES_MTIME_UPLOAD)) {
            retval = new ConfigUploadMtimeAction();
        }
        else if (typeIn.equals(TYPE_SCRIPT_RUN)) {
            retval = new ScriptRunAction();
        }
        else if (typeIn.equals(TYPE_DAEMON_CONFIG)) {
            retval = new DaemonConfigAction();
        }
        else if (typeIn.equals(TYPE_PACKAGES_VERIFY)) {
            retval = new PackageVerifyAction();
        }
        else if (typeIn.equals(TYPE_RHN_APPLET_USE_SATELLITE)) {
            retval = new AppletUseSatelliteAction();
        }
        else if (typeIn.equals(TYPE_KICKSTART_INITIATE_GUEST)) {
            retval = new KickstartInitiateGuestAction();
        }
        else if (typeIn.equals(TYPE_VIRTIZATION_HOST_SUBSCRIBE_TO_TOOLS_CHANNEL)) {
            retval = new KickstartHostToolsChannelSubscriptionAction();
        }
        else if (typeIn.equals(TYPE_VIRTUALIZATION_GUEST_SUBSCRIBE_TO_TOOLS_CHANNEL)) {
            retval = new KickstartGuestToolsChannelSubscriptionAction();
        }
        else if (typeIn.equals(TYPE_SCAP_XCCDF_EVAL)) {
            retval = new ScapAction();
        }
        else if (typeIn.equals(TYPE_CLIENTCERT_UPDATE_CLIENT_CERT)) {
            retval = new CertificateUpdateAction();
        }
        else if (typeIn.equals(TYPE_DEPLOY_IMAGE)) {
            retval = new DeployImageAction();
        }
        else if (typeIn.equals(TYPE_DIST_UPGRADE)) {
            retval = new DistUpgradeAction();
        }
        else if (typeIn.equals(TYPE_PACKAGES_LOCK)) {
            retval = new PackageLockAction();
        }
        else if (typeIn.equals(TYPE_APPLY_STATES)) {
            retval = new ApplyStatesAction();
        }
        else if (typeIn.equals(TYPE_IMAGE_BUILD)) {
            retval = new ImageBuildAction();
        }
        else if (typeIn.equals(TYPE_IMAGE_INSPECT)) {
            retval = new ImageInspectAction();
        }
        else if (typeIn.equals(TYPE_SUBSCRIBE_CHANNELS)) {
            retval = new SubscribeChannelsAction();
        }
        else if (typeIn.equals(TYPE_PLAYBOOK)) {
            retval = new PlaybookAction();
        }
        else if (typeIn.equals(TYPE_COCO_ATTESTATION)) {
            retval = new CoCoAttestationAction();
        }
        else if (typeIn.equals(TYPE_APPSTREAM_CONFIGURE)) {
            retval = new AppStreamAction();
        }
        else if (typeIn.equals(TYPE_INVENTORY)) {
            retval = new InventoryAction();
        }
        else if (typeIn.equals(TYPE_SUPPORTDATA_GET)) {
            retval = new SupportDataAction();
        }
        else if (typeIn.equals(TYPE_VIRT_PROFILE_REFRESH)) {
            retval = new VirtualInstanceRefreshAction();
        }
        else {
            retval = new Action();
        }
        retval.setActionType(typeIn);
        retval.setCreated(new Date());
        retval.setModified(new Date());
        if (earliestIn == null) {
            earliestIn = new Date();
        }
        retval.setEarliestAction(earliestIn);
        //in perl(modules/rhn/RHN/DB/Scheduler.pm) version is given a 2.
        //So that's what I did.
        retval.setVersion(2L);
        retval.setArchived(0L); //not archived
        return retval;
    }

    /**
     * Lookup an Action by the id, assuming that it is in the same Org as
     * the user doing the search.  This method ensures security around the
     * Action.
     * @param user the user doing the search
     * @param id of the Action to search for
     * @return the Action found
     */
    public static Action lookupByUserAndId(User user, Long id) {
        return getSession().createQuery("FROM Action AS a where a.id = :aid AND org_id = :orgId", Action.class)
                .setParameter("aid", id)
                .setParameter("orgId", user.getOrg().getId())
                .uniqueResult();
    }

    /**
     * Lookup the number of server actions for a particular action that have a certain status
     * @param status the status you want
     * @param action the action id
     * @return the count
     */
    public static Integer getServerActionCountByStatus(Action action, ActionStatus status) {
        return getSession().createNativeQuery("""
                SELECT COUNT(sa.server_id) AS count
                FROM   rhnServerAction sa
                WHERE  sa.action_id = :aid
                AND    sa.status = :stid
                """, Tuple.class)
                .setParameter("aid", action.getId())
                .setParameter("stid", status.getId())
                .uniqueResult()
                .get("count", Number.class).intValue();
    }


    /**
     * Lookup the last completed Action on a Server
     *  given the user, action type and server.
     * This is useful especially in cases where we want to
     * find the last deployed config action ...
     *
     * @param user the user doing the search (needed for permssion checking)
     * @param type the action type of the action to be queried.
     * @param server the server who's latest completed action is desired.
     * @return the Action found or null if none exists
     */
    public static Action lookupLastCompletedAction(User user, ActionType type, Server server) {
        return getSession().createNativeQuery("""
                SELECT *
                FROM   rhnAction a
                WHERE  a.id = (SELECT     MAX(rA.id)
                               FROM       rhnAction rA
                               inner join rhnServerAction rsa ON rsa.action_id = rA.id
                               inner join rhnActionStatus ras ON ras.id = rsa.status
                               inner join rhnUserServerPerms usp ON usp.server_id = rsa.server_id
                               WHERE      usp.user_id = :userId
                               AND        rsa.server_id = :serverId
                               AND        ras.name IN ('Completed', 'Failed')
                               AND        rA.action_type = :actionTypeId
                              )
                """, Action.class)
                .addSynchronizedEntityClass(Server.class)
                .addSynchronizedEntityClass(ServerAction.class)
                .setParameter("userId", user.getId())
                .setParameter("actionTypeId", type.getId())
                .setParameter("serverId", server.getId())
                .uniqueResult();
    }


    /**
     * Lookup a Action by their id
     * @param id the id to search for
     * @return the Action found
     */
    public static Action lookupById(Long id) {
        Session session = HibernateFactory.getSession();
        return session.get(Action.class, id);
    }

    /**
     * Helper method to get a ActionType by label
     * @param label the Action to lookup
     * @return Returns the ActionType corresponding to label
     */
    public static ActionType lookupActionTypeByLabel(String label) {
        if (label == null) {
            return null;
        }
        return singleton.lookupObjectByParam(ActionType.class, "label", label, true);
    }

    /**
     * Helper method to get a ActionStatus by Name
     * @param name the name of the status we want to lookup.
     * @return Returns the ActionStatus corresponding to name
     */
    private static ActionStatus lookupActionStatusByName(String name) {
        return singleton.lookupObjectByParam(ActionStatus.class, "name", name, true);
    }

    /**
     * Helper method to get a ConfigRevisionActionResult by Action Config Revision Id
     * @param actionConfigRevisionId the id of the ActionConfigRevision for whom we want to lookup the result
     * @return The ConfigRevisionActionResult corresponding to the revison ID.
     */
    public static ConfigRevisionActionResult lookupConfigActionResult(Long actionConfigRevisionId) {
        return singleton.lookupObjectByParam(ConfigRevisionActionResult.class, "id", actionConfigRevisionId, true);
    }

    /**
     * Helper method to get a ConfigRevisionAction by Action Config Revision Id
     * @param id the id of the ActionConfigRevision for whom we want to lookup the result
     * @return The ConfigRevisionAction corresponding to the revison ID.
     */
    public static ConfigRevisionAction lookupConfigRevisionAction(Long id) {
        Session session = HibernateFactory.getSession();
        return session.get(ConfigRevisionAction.class, id);
    }

    /**
     * Helper method to get a {@link ApplyStatesActionDetails} by its action id.
     * @param actionId the id of the {@link ApplyStatesActionDetails}
     * @return the {@link ApplyStatesActionDetails} corresponding to the given action id.
     */
    public static ApplyStatesActionDetails lookupApplyStatesActionDetails(Long actionId) {
        Session session = HibernateFactory.getSession();
        return session.createQuery("FROM ApplyStatesActionDetails WHERE parentAction.id = :action_id",
                        ApplyStatesActionDetails.class)
                .setParameter("action_id", actionId)
                //Retrieve from cache if there
                .setCacheable(true)
                .uniqueResult();
    }

    /**
     * Insert or Update a Action.
     * @param actionIn Action to be stored in database.
     * @return action
     */
    public static Action save(Action actionIn) {
        /**
         * If we are trying to commit a package action, make sure
         * the packageEvr stored proc is called first so that
         * the foreign key constraint holds.
         */
        if (actionIn instanceof PackageAction action) {
            Set<PackageActionDetails> details = action.getDetails();
            for (PackageActionDetails detail : details) {
                PackageEvr evr = detail.getEvr();

                // It is possible to have a Package Action with only a package name
                if (evr != null) {
                    //commit each packageEvr
                    PackageEvr newEvr = PackageEvrFactory.lookupOrCreatePackageEvr(evr);
                    detail.setEvr(newEvr);
                }
            }
        }

        singleton.saveObject(actionIn);
        if (actionIn.getServerActions() != null) {
            actionIn.getServerActions().stream()
                    .map(sa -> sa.getServerId())
                    .forEach(sid -> SystemManager.updateSystemOverview(sid));
        }
        return actionIn;
    }

    /**
     * Remove a Action from the DB
     * @param actionIn Action to be removed from database.
     */
    public static void remove(Action actionIn) {
        singleton.removeObject(actionIn);
        actionIn.getServerActions().stream()
                .map(sa -> sa.getServerId())
                .forEach(sid -> SystemManager.updateSystemOverview(sid));
    }

    /**
     * Check the ActionType against the ActionArchType to see
     *
     * @param actionCheck the Action we want to see if the type matches against
     * @param actionStyle the String type we want to check
     * @return boolean if the passed in Action matches the actionStyle from
     *         the set of ActionArchTypes
     */
    public static boolean checkActionArchType(Action actionCheck, String actionStyle) {
        return actionArchTypes.contains(
                toActionArchTypeKey(actionCheck.getActionType().getId(), actionStyle));
    }

    /**
     * Recursively query the hierarchy of actions dependent on a given
     * parent. While recursive, only one query is executed per level in
     * the hierarchy, and action hierarchies tend to not be more than
     * two levels deep.
     *
     * @param parentAction Parent action.
     * @return Set of actions dependent on the given parent.
     */
    public static Stream<Action> lookupDependentActions(Action parentAction) {
        Session session = HibernateFactory.getSession();

        Set<Action> returnSet = new HashSet<>();
        List<Long> actionsAtHierarchyLevel = new LinkedList<>();
        actionsAtHierarchyLevel.add(parentAction.getId());
        do {
            List<Action> results = session
                    .createQuery("from Action a where a.prerequisite.id in (:action_ids)", Action.class)
                    .setParameterList("action_ids", actionsAtHierarchyLevel)
                    .list();
            returnSet.addAll(results);
            // Reset list of actions for the next hierarchy level:
            actionsAtHierarchyLevel = results.stream().map(Action::getId).collect(Collectors.toList());
        }
        while (!actionsAtHierarchyLevel.isEmpty());

        return returnSet.stream();
    }

    /**
     * Lookup a List of Action objects for a given Server.
     * @param user the user doing the search
     * @param serverIn you want to limit the list of Actions to
     * @return List of Action objects
     */
    public static List<Action> listActionsForServer(User user, Server serverIn) {
        return getSession().createQuery("""
                FROM  com.redhat.rhn.domain.action.Action AS a
                LEFT  JOIN FETCH a.serverActions AS sa
                WHERE a.org = :org
                AND   sa.server = :server
                """, Action.class)
                .setParameter("org", user.getOrg())
                .setParameter("server", serverIn)
                .list();
    }

    /**
     * List all pending server actions of the given types
     * @param typesIn the types to look for
     * @return return a list of server actions
     */
    public static List<ServerAction> listPendingServerActionsByTypes(List<ActionType> typesIn) {
        return getSession().createNativeQuery("""
            SELECT sa.*
              FROM rhnAction a
              JOIN rhnserveraction sa ON a.id = sa.action_id
             WHERE a.action_type IN (:types)
               AND sa.status in (0, 1)
               AND a.earliest_action <= current_timestamp
            """, ServerAction.class)
                .setParameterList("types", typesIn.stream().map(ActionType::getId).toList())
                .addSynchronizedEntityClass(Action.class)
                .list();
    }

    /**
     * Lookup a List of ServerAction objects for a given Server.
     * @param serverIn you want to limit the list of Actions to
     * @return List of ServerAction objects
     */
    public static List<ServerAction> listServerActionsForServer(Server serverIn) {
        Session session = HibernateFactory.getSession();
        return session.createQuery("FROM ServerAction AS sa WHERE sa.server = :server", ServerAction.class)
                .setParameter("server", serverIn)
                .list();
    }

    /**
     * Lookup a List of ServerAction objects for a given Server.
     * @param serverIn you want to limit the list of Actions to
     * @param actionType you want to limit the list of Actions to
     * @param date you want to limit the completion date after
     * @return List of ServerAction objects
     */
    public static List<ServerAction> listServerActionsForServer(Server serverIn, String actionType, Date date) {
        //The explicit cast in this query is needed since the postgresql jdbc driver is not able to define the type.
        //See more info at https://bit.ly/3Gh3Ez5
        Session session = HibernateFactory.getSession();
        return session.createQuery("""
                        FROM ServerAction AS sa
                        WHERE sa.server = :server
                        AND (cast(:actionType AS string) IS NULL OR sa.parentAction.actionType.name = :actionType)
                        AND (cast(:date AS date) IS NULL OR sa.created >= :date)""", ServerAction.class)
                .setParameter("server", serverIn)
                .setParameter("actionType", actionType)
                .setParameter("date", date)
                .list();
    }

    /**
     * Lookup a List of ServerAction objects for a given Server and Action Types.
     * @param serverIn you want to limit the list of Actions to
     * @param typesIn you want to limit the list of Actions to
     * @return List of ServerAction objects
     */
    public static List<ServerAction> listServerActionsForServerAndTypes(Server serverIn, List<ActionType> typesIn) {
        Session session = HibernateFactory.getSession();
        return session.createQuery("""
                        FROM ServerAction AS sa
                        WHERE sa.server = :server
                        AND sa.parentAction.actionType IN (:typeList)""", ServerAction.class)
                .setParameter("server", serverIn)
                .setParameterList("typeList", typesIn)
                .list();
    }

    /**
     * Lookup a List of ServerAction objects in the given states for a given Server.
     * @param serverIn you want to limit the list of Actions to
     * @param statusList to filter the ServerActoins by
     * @return List of ServerAction objects
     */
    public static List<ServerAction> listServerActionsForServer(Server serverIn, List<ActionStatus> statusList) {
        Session session = HibernateFactory.getSession();
        return session.createQuery("""
                        FROM ServerAction AS sa
                        WHERE sa.server = :server AND sa.status IN (:statusList)""", ServerAction.class)
                .setParameter("server", serverIn)
                .setParameterList("statusList", statusList)
                .list();
    }

    /**
     * Lookup a List of ServerAction objects in the given states for a given Server.
     * @param serverIn you want to limit the list of Actions to
     * @param statusList to filter the ServerActions by
     * @param createdDate to filter the ServerActions by
     * @return List of ServerAction objects
     */
    public static List<ServerAction> listServerActionsForServer(Server serverIn, List<ActionStatus> statusList,
                                                                Date createdDate) {
        Session session = HibernateFactory.getSession();
        return session.createQuery("""
                        FROM ServerAction AS sa
                        WHERE sa.server = :server AND sa.created >= :date AND sa.status IN (:statusList)""",
                        ServerAction.class)
                .setParameter("server", serverIn)
                .setParameterList("statusList", statusList)
                .setParameter("date", createdDate)
                .list();
    }

    /**
     * Lookup ServerAction object for given Server/Action pair.
     * @param serverIn the server who's ServerAction you are searching for
     * @param actionIn the action who's ServerAction you are searching for
     * @return matching ServerAction object
     */
    public static ServerAction getServerActionForServerAndAction(Server serverIn, Action actionIn) {
        if (serverIn == null || actionIn == null) {
            return null;
        }

        Session session = HibernateFactory.getSession();
        return session.createQuery("""
                        FROM ServerAction AS sa
                        WHERE sa.server = :server AND sa.parentAction = :action""", ServerAction.class)
                .setParameter("server", serverIn)
                .setParameter("action", actionIn)
                .uniqueResult();
    }

    /**
     * Reschedule All Failed Server Actions associated with an action
     * @param action the action who's server actions you are rescheduling
     * @param tries the number of tries to set (should be set to 5)
     */
    public static void rescheduleFailedServerActions(Action action, Long tries) {
        updateActionEarliestDate(action);
        HibernateFactory.getSession().createQuery("""
                        UPDATE ServerAction sa
                        SET    sa.status = :queued,
                               sa.remainingTries = :tries,
                               sa.pickupTime = null,
                               sa.completionTime = null,
                               resultCode = null,
                               resultMsg = null
                        WHERE  sa.status = :failed
                        AND    sa.parentAction = :action
                        """)
                .setParameter("action", action)
                .setParameter("tries", tries)
                .setParameter("failed", ActionFactory.STATUS_FAILED)
                .setParameter("queued", ActionFactory.STATUS_QUEUED).executeUpdate();
        action.removeInvalidResults();
        action.getServerActions().stream()
                .filter(ServerAction::isFailed)
                .map(ServerAction::getServerId)
                .forEach(SystemManager::updateSystemOverview);
    }

    /**
     * Reschedule All Server Actions associated with an action
     * @param action the action who's server actions you are rescheduling
     * @param tries the number of tries to set (should be set to 5)
     */
    public static void rescheduleAllServerActions(Action action, Long tries) {
        updateActionEarliestDate(action);
        HibernateFactory.getSession().createQuery("""
                        UPDATE  ServerAction sa
                        SET     sa.status = :queued,
                                sa.remainingTries = :tries,
                                sa.pickupTime = null,
                                sa.completionTime = null,
                                resultCode = null,
                                resultMsg = null
                        WHERE   sa.parentAction = :action
                        """)
                .setParameter("action", action)
                .setParameter("tries", tries)
                .setParameter("queued", ActionFactory.STATUS_QUEUED).executeUpdate();
        action.removeInvalidResults();
        action.getServerActions().stream()
                .map(ServerAction::getServerId)
                .forEach(SystemManager::updateSystemOverview);
    }

    /**
     * Returns all pending actions that contain minions
     * @return list of pending minions that contain minions
     */
    public static List<Action> pendingMinionServerActions() {
        return getSession().createNativeQuery("""
                SELECT *
                FROM   rhnAction
                WHERE  id IN (SELECT     DISTINCT ac.id
                              FROM       rhnAction ac
                              INNER JOIN rhnServerAction sa on ac.id = sa.action_id
                              INNER JOIN suseMinionInfo mi on sa.server_id = mi.server_id
                              WHERE      sa.status in (0, 1))
                """, Action.class)
                .list();
    }

    /**
     * Reschedule Server Action associated with an action and system
     * @param action the action who's server actions you are rescheduling
     * @param tries the number of tries to set (should be set to 5)
     * @param server system id of action we want reschedule
     */
    public static void rescheduleSingleServerAction(Action action, Long tries,
            Long server) {
        updateActionEarliestDate(action);
        HibernateFactory.getSession().createQuery("""
                        UPDATE ServerAction sa
                        SET    sa.status = :queued,
                               sa.remainingTries = :tries,
                               sa.pickupTime = null,
                               sa.completionTime = null,
                               resultCode = null,
                               resultMsg = null
                        WHERE  sa.parentAction = :action
                        AND    sa.serverId = :server
                        """)
        .setParameter("action", action)
        .setParameter("tries", tries)
        .setParameter("queued", ActionFactory.STATUS_QUEUED)
        .setParameter("server", server).executeUpdate();
        action.removeInvalidResults();
        SystemManager.updateSystemOverview(server);
    }

    /**
     * @param aid history event id to look up for
     * @return history event
     */
    public static ServerHistoryEvent lookupHistoryEventById(Long aid) {
        Session session = HibernateFactory.getSession();
        return session.createQuery("FROM ServerHistoryEvent AS s WHERE s.id = :id", ServerHistoryEvent.class)
                .setParameter("id", aid)
                .uniqueResult();

    }

    private static void updateActionEarliestDate(Action action) {
        action.setEarliestAction(new Date());
        HibernateFactory.getSession().save(action);
    }

    /**
     * Update the {@link ActionStatus} to "PickedUp" of several rhnServerAction rows identified
     * by server and action IDs.
     *
     * @param actionIn associated action of rhnServerAction records
     * @param serverIds server Ids for which action is scheduled
     */
    public static void updateServerActionsPickedUp(Action actionIn, List<Long> serverIds) {
        LOG.debug("Action status {} is going to b set for these servers: {}",
                ActionFactory.STATUS_PICKED_UP.getName(), serverIds);
        Map<String, Object>  parameters = new HashMap<>();
        parameters.put("action_id", actionIn.getId());
        parameters.put("status", ActionFactory.STATUS_PICKED_UP.getId());
        String sql = """
                UPDATE rhnServerAction
                SET    status = :status,
                       pickup_time = current_timestamp
                WHERE  action_id  = :action_id
                AND    server_id  IN (:server_ids)
                AND    status NOT IN(2,3)
                """;
        udpateByIds(serverIds, sql, "server_ids", parameters);
        serverIds.forEach(SystemManager::updateSystemOverview);
    }

    /**
     * Update the status of several rhnServerAction rows identified by server and action IDs.
     * @param actionIn associated action of rhnServerAction records
     * @param serverIds server Ids for which action is scheduled
     * @param status {@link ActionStatus} object that needs to be set
     */
    public static void updateServerActions(Action actionIn, List<Long> serverIds, ActionStatus status) {
        LOG.debug("Action status {} is going to b set for these servers: {}", status.getName(), serverIds);
        Map<String, Object>  parameters = new HashMap<>();
        parameters.put("action_id", actionIn.getId());
        parameters.put("status", status.getId());
        String sql = """
                UPDATE rhnServerAction
                SET    status = :status
                WHERE  action_id  = :action_id
                AND    server_id  IN (:server_ids)
                AND    status NOT IN(2,3)
                """;
        udpateByIds(serverIds, sql, "server_ids", parameters);
        serverIds.forEach(SystemManager::updateSystemOverview);
    }

    /**
     * Mark queue server actions as failed because the execution has been rejected
     * @param actionsId list of ids of the action to reject
     * @param rejectionReason the reason why the scheduled action was not picked up
     */
    public static void rejectScheduledActions(List<Long> actionsId, String rejectionReason) {
        Query<Tuple> query = getSession()
                .createNativeQuery("""
                        UPDATE rhnServerAction
                        SET    status = 3,
                               result_code = -1,
                               result_msg = :rejection_reason,
                               completion_time = :completion_time,
                               remaining_tries = 0
                        WHERE  action_id IN (:action_ids)
                        AND    status = 0
                        RETURNING server_id
                        """, Tuple.class)
                .setParameter("rejection_reason", rejectionReason)
                .setParameter("completion_time", new Date());

        List<Tuple> updatedServerIds = HibernateFactory.<Long, List<Tuple>, Tuple>splitAndExecuteQuery(
            actionsId, "action_ids", query, query::list, new ArrayList<>(), ListUtils::union
        );

        updatedServerIds.stream()
                .map(t -> t.get(0, Number.class).longValue())
                .forEach(SystemManager::updateSystemOverview);
    }

    /**
     * Save a {@link ServerAction} object.
     * @param serverActionIn the server action to save
     */
    public static void save(ServerAction serverActionIn) {
        singleton.saveObject(serverActionIn);
        SystemManager.updateSystemOverview(serverActionIn.getServerId());
    }

    /**
     * Delete a {@link ServerAction} object.
     * @param serverAction the server action to delete
     */
    public static void delete(ServerAction serverAction) {
        singleton.removeObject(serverAction);
    }

    /**
     * The constant representing the Action Status QUEUED
     */
    public static final ActionStatus STATUS_QUEUED =
            lookupActionStatusByName("Queued");

    /**
     * The constant representing the Action Status PICKED_UP
     */
    public static final ActionStatus STATUS_PICKED_UP =
            lookupActionStatusByName("Picked Up");

    /**
     * The constant representing the Action Status COMPLETED
     */
    public static final ActionStatus STATUS_COMPLETED =
            lookupActionStatusByName("Completed");

    /**
     * The constant representing the Action Status FAILED
     */
    public static final ActionStatus STATUS_FAILED =
            lookupActionStatusByName("Failed");

    /**
     * All the possible action statuses
     */
    public static final List<ActionStatus> ALL_STATUSES = List.of(STATUS_QUEUED, STATUS_PICKED_UP, STATUS_COMPLETED,
        STATUS_FAILED);

    /**
     * All the possible action statuses, but completed
     */
    public static final List<ActionStatus> ALL_STATUSES_BUT_COMPLETED = List.of(STATUS_QUEUED, STATUS_PICKED_UP,
            STATUS_FAILED);

    /**
     * All the pending action statuses
     */
    public static final List<ActionStatus> ALL_PENDING_STATUSES = List.of(STATUS_QUEUED, STATUS_PICKED_UP);

    /**
     * The constant representing Package Refresh List action.  [ID:1]
     */
    public static final ActionType TYPE_PACKAGES_REFRESH_LIST =
            lookupActionTypeByLabel("packages.refresh_list");

    /**
     * The constant representing Hardware Refreshlist action.  [ID:2]
     */
    public static final ActionType TYPE_HARDWARE_REFRESH_LIST =
            lookupActionTypeByLabel("hardware.refresh_list");

    /**
     * The constant representing Package Update action.  [ID:3]
     */
    public static final ActionType TYPE_PACKAGES_UPDATE =
            lookupActionTypeByLabel("packages.update");

    /**
     * The constant representing Package Remove action.  [ID:4]
     */
    public static final ActionType TYPE_PACKAGES_REMOVE =
            lookupActionTypeByLabel("packages.remove");

    /**
     * The constant representing Errata action.  [ID:5]
     */
    public static final ActionType TYPE_ERRATA =
            lookupActionTypeByLabel("errata.update");

    /**
     * The constant representing RHN Get server up2date config action. [ID:6]
     */
    public static final ActionType TYPE_UP2DATE_CONFIG_GET =
            lookupActionTypeByLabel("up2date_config.get");

    /**
     * The constant representing RHN Update server up2date config action.  [ID:7]
     */
    public static final ActionType TYPE_UP2DATE_CONFIG_UPDATE =
            lookupActionTypeByLabel("up2date_config.update");

    /**
     * The constant representing Package Delta action.  [ID:8]
     */
    public static final ActionType TYPE_PACKAGES_DELTA =
            lookupActionTypeByLabel("packages.delta");

    /**
     * The constant representing Reboot action.  [ID:9]
     */
    public static final ActionType TYPE_REBOOT =
            lookupActionTypeByLabel("reboot.reboot");

    /**
     * The constant representing Rollback Config action.  [ID:10]
     */
    public static final ActionType TYPE_ROLLBACK_CONFIG =
            lookupActionTypeByLabel("rollback.config");

    /**
     * The constant representing "Refresh server-side transaction list"  [ID:11]
     */
    public static final ActionType TYPE_ROLLBACK_LISTTRANSACTIONS =
            lookupActionTypeByLabel("rollback.listTransactions");

    /**
     * The constant representing "Automatic package installation".  [ID:13]
     */
    public static final ActionType TYPE_PACKAGES_AUTOUPDATE =
            lookupActionTypeByLabel("packages.autoupdate");

    /**
     * The constant representing "Package Synchronization".  [ID:14]
     */
    public static final ActionType TYPE_PACKAGES_RUNTRANSACTION =
            lookupActionTypeByLabel("packages.runTransaction");


    /**
     * The constant representing "Import config file data from system".  [ID:15]
     */
    public static final ActionType TYPE_CONFIGFILES_UPLOAD =
            lookupActionTypeByLabel("configfiles.upload");

    /**
     * The constant representing "Deploy config files to system".  [ID:16]
     */
    public static final ActionType TYPE_CONFIGFILES_DEPLOY =
            lookupActionTypeByLabel("configfiles.deploy");

    /**
     * The constant representing "Verify deployed config files" [ID:17]
     */
    public static final ActionType TYPE_CONFIGFILES_VERIFY =
            lookupActionTypeByLabel("configfiles.verify");

    /**
     * The constant representing
     * "Show differences between profiled config files and deployed config files"  [ID:18]
     */
    public static final ActionType TYPE_CONFIGFILES_DIFF =
            lookupActionTypeByLabel("configfiles.diff");

    /**
     * The constant representing "Initiate a kickstart".  [ID:19]
     */
    public static final ActionType TYPE_KICKSTART_INITIATE =
            lookupActionTypeByLabel("kickstart.initiate");


    /**
     * The constant representing "Initiate a kickstart for a guest".
     */
    public static final ActionType TYPE_KICKSTART_INITIATE_GUEST =
            lookupActionTypeByLabel("kickstart_guest.initiate");

    /**
     * The constant representing "Schedule a package sync for kickstarts".  [ID:20]
     */
    public static final ActionType TYPE_KICKSTART_SCHEDULE_SYNC =
            lookupActionTypeByLabel("kickstart.schedule_sync");

    /**
     * The constant representing "Schedule a package install for activation key".  [ID:21]
     */
    public static final ActionType TYPE_ACTIVATION_SCHEDULE_PKG_INSTALL =
            lookupActionTypeByLabel("activation.schedule_pkg_install");

    /**
     * The constant representing "Schedule a config deploy for activation key"  [ID:22]
     */
    public static final ActionType TYPE_ACTIVATION_SCHEDULE_DEPLOY =
            lookupActionTypeByLabel("activation.schedule_deploy");

    /**
     * The constant representing
     * "Upload config file data based upon mtime to server" [ID:23]
     */
    public static final ActionType TYPE_CONFIGFILES_MTIME_UPLOAD =
            lookupActionTypeByLabel("configfiles.mtime_upload");

    /**
     * The constant representing "Run an arbitrary script".  [ID:30]
     */
    public static final ActionType TYPE_SCRIPT_RUN =
            lookupActionTypeByLabel("script.run");

    /**
     * The constant representing "RHN Daemon Configuration".  [ID:32]
     */
    public static final ActionType TYPE_DAEMON_CONFIG =
            lookupActionTypeByLabel("rhnsd.configure");

    /**
     * The constant representing "Verify deployed packages"  [ID:33]
     */
    public static final ActionType TYPE_PACKAGES_VERIFY =
            lookupActionTypeByLabel("packages.verify");

    /**
     * The constant representing "Lock packages"  [ID:502]
     */
    public static final ActionType TYPE_PACKAGES_LOCK =
            lookupActionTypeByLabel("packages.setLocks");

    /**
     * The constant representing "Allows for rhn-applet use with an PRODUCTNAME"  [ID:34]
     */
    public static final ActionType TYPE_RHN_APPLET_USE_SATELLITE =
            lookupActionTypeByLabel("rhn_applet.use_satellite");

    /**
     * The constant representing "Rollback a transaction".  [ID:197542]
     */
    public static final ActionType TYPE_ROLLBACK_ROLLBACK =
            lookupActionTypeByLabel("rollback.rollback");

    /**
     * The constant representing "Schedule a package install of host specific
     * functionality."  [ID:44]
     */
    public static final ActionType TYPE_VIRTUALIZATION_HOST_PACKAGE_INSTALL =
            lookupActionTypeByLabel("kickstart_host.schedule_virt_host_pkg_install");

    /**
     * The constant representing "Schedule a package install of guest specific
     * functionality."  [ID:45]
     */
    public static final ActionType TYPE_VIRTUALIZATION_GUEST_PACKAGE_INSTALL =
            lookupActionTypeByLabel("kickstart_guest.schedule_virt_guest_pkg_install");

    /**
     * The constant representing "Subscribes a server to the RHN Tools channel
     * associated with its base channel." [ID:46]
     */
    public static final ActionType TYPE_VIRTIZATION_HOST_SUBSCRIBE_TO_TOOLS_CHANNEL =
            lookupActionTypeByLabel("kickstart_host.add_tools_channel");

    /**
     * The constant represting "Subscribes a virtualization guest to the RHN Tools channel
     * associated with its base channel." [ID: 47]
     */
    public static final ActionType TYPE_VIRTUALIZATION_GUEST_SUBSCRIBE_TO_TOOLS_CHANNEL =
            lookupActionTypeByLabel("kickstart_guest.add_tools_channel");

    public static final ActionType TYPE_SCAP_XCCDF_EVAL =
            lookupActionTypeByLabel("scap.xccdf_eval");

    public static final ActionType TYPE_CLIENTCERT_UPDATE_CLIENT_CERT =
            lookupActionTypeByLabel("clientcert.update_client_cert");

    public static final String TXN_OPERATION_INSERT = "insert";
    public static final String TXN_OPERATION_DELETE = "delete";

    /**
     * The constant representing Image deploy action.  [ID:500]
     */
    public static final ActionType TYPE_DEPLOY_IMAGE =
            lookupActionTypeByLabel("image.deploy");

    /**
     * The constant representing distribution upgrade action.  [ID:501]
     */
    public static final ActionType TYPE_DIST_UPGRADE =
            lookupActionTypeByLabel("distupgrade.upgrade");

    /**
     * The constant representing application of salt states.  [ID:503]
     */
    public static final ActionType TYPE_APPLY_STATES =
            lookupActionTypeByLabel("states.apply");

    /**
     * The constant representing application of image build.  [ID:504]
     */
    public static final ActionType TYPE_IMAGE_BUILD =
            lookupActionTypeByLabel("image.build");

    /**
     * The constant representing application of image inspect.  [ID:505]
     */
    public static final ActionType TYPE_IMAGE_INSPECT =
            lookupActionTypeByLabel("image.inspect");

    /**
     * The constant representing setting of channels.  [ID:506]
     */
    public static final ActionType TYPE_SUBSCRIBE_CHANNELS =
            lookupActionTypeByLabel("channels.subscribe");

    /**
     * The constant representing "Execute an Ansible playbook" [ID:521]
     */
    public static final ActionType TYPE_PLAYBOOK = lookupActionTypeByLabel("ansible.playbook");

    /**
     * The constant representing "Confidential Compute Attestation" [ID:523]
     */
    public static final ActionType TYPE_COCO_ATTESTATION =
            lookupActionTypeByLabel("coco.attestation");

    /**
     * The constant representing appstreams changes action. [ID:524]
     */
    public static final ActionType TYPE_APPSTREAM_CONFIGURE = lookupActionTypeByLabel("appstreams.configure");

    /**
     * The constant representing "Refresh Ansible inventories" [ID:525]
     */
    public static final ActionType TYPE_INVENTORY = lookupActionTypeByLabel("ansible.inventory");

    /**
     * The constant representing "Support Data Get" [ID:526]
     */
    public static final ActionType TYPE_SUPPORTDATA_GET =
            lookupActionTypeByLabel("supportdata.get");

    /**
     * The constant representing "Refresh Virtual Machine list" [ID:527]
     */
    public static final ActionType TYPE_VIRT_PROFILE_REFRESH = lookupActionTypeByLabel("virt.refresh_list");

}

