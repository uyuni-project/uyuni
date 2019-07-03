/**
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
package com.redhat.rhn.taskomatic;

import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.security.PermissionException;
import com.redhat.rhn.common.validator.ValidatorException;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionChain;
import com.redhat.rhn.domain.action.ActionChainFactory;
import com.redhat.rhn.domain.action.channel.SubscribeChannelsAction;
import com.redhat.rhn.domain.action.server.ServerAction;
import com.redhat.rhn.domain.channel.Channel;
import com.redhat.rhn.domain.channel.ChannelFactory;
import com.redhat.rhn.domain.org.Org;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.domain.server.Server;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.taskomatic.domain.TaskoSchedule;
import com.redhat.rhn.taskomatic.task.RepoSyncTask;

import com.suse.manager.utils.MinionServerUtils;
import org.apache.log4j.Logger;

import java.net.MalformedURLException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import redstone.xmlrpc.XmlRpcClient;
import redstone.xmlrpc.XmlRpcException;
import redstone.xmlrpc.XmlRpcFault;

import static java.util.Collections.singletonList;

/**
 * TaskomaticApi
 */
public class TaskomaticApi {

    public static final String MINION_ACTION_BUNCH_LABEL = "minion-action-executor-bunch";
    public static final String MINION_ACTION_JOB_PREFIX = "minion-action-executor-";
    public static final String MINION_ACTION_JOB_DOWNLOAD_PREFIX =
            MINION_ACTION_JOB_PREFIX + "download-";
    public static final String MINION_ACTIONCHAIN_BUNCH_LABEL = "minion-action-chain-executor-bunch";
    public static final String MINION_ACTIONCHAIN_JOB_PREFIX = "minion-action-chain-executor-";
    private static final Logger LOG = Logger.getLogger(TaskomaticApi.class);


    private XmlRpcClient getClient() throws TaskomaticApiException {
        try {
           return  new XmlRpcClient(
                    ConfigDefaults.get().getTaskoServerUrl(), false);
        }
        catch (MalformedURLException e) {
            throw new TaskomaticApiException(e);
        }
    }

    private Object invoke(String name, Object...args) throws TaskomaticApiException {
        try {
            return getClient().invoke(name, args);
        }
        catch (XmlRpcException e) {
            throw new TaskomaticApiException(e);
        }
        catch (XmlRpcFault e) {
            throw new TaskomaticApiException(e);
        }
    }

    /**
     * Returns whether taskomatic is running
     * @return True if taskomatic is running
     */
    public boolean isRunning() {
        try {
            invoke("tasko.one", 0);
            return true;
        }
        catch (Exception e) {
            return false;
        }
    }

    /**
     * Schedule a single reposync
     * @param chan the channel
     * @param user the user
     * @throws TaskomaticApiException if there was an error
     */
    public void scheduleSingleRepoSync(Channel chan, User user)
                                    throws TaskomaticApiException {
        Map scheduleParams = new HashMap();
        scheduleParams.put("channel_id", chan.getId().toString());
        invoke("tasko.scheduleSingleBunchRun", user.getOrg().getId(),
                "repo-sync-bunch", scheduleParams);
    }

    /**
     * Schedule a single reposync for a given list of channels. This is scheduled from
     * within another taskomatic job, so we don't have a user here. We pass in the
     * satellite org to create the job label internally.
     * @param channels list of channels
     * @throws TaskomaticApiException if there was an error
     */
    public void scheduleSingleRepoSync(List<Channel> channels)
            throws TaskomaticApiException {
        List<String> channelIds = new ArrayList<>(channels.size());
        for (Channel channel : channels) {
            channelIds.add(channel.getId().toString());
        }
        Map<String, List<String>> scheduleParams = new HashMap<>();
        scheduleParams.put("channel_ids", channelIds);
        invoke("tasko.scheduleSingleBunchRun", OrgFactory.getSatelliteOrg().getId(),
                "repo-sync-bunch", scheduleParams);
    }

    /**
     * Schedule a single reposync
     * @param chan the channel
     * @param user the user
     * @param params parameters
     * @throws TaskomaticApiException if there was an error
     */
    public void scheduleSingleRepoSync(Channel chan, User user, Map<String, String> params)
                                    throws TaskomaticApiException {

        Map<String, String> scheduleParams = new HashMap<String, String>();
        scheduleParams.put("channel_id", chan.getId().toString());
        scheduleParams.putAll(params);

        invoke("tasko.scheduleSingleBunchRun", user.getOrg().getId(),
                "repo-sync-bunch", scheduleParams);
    }

    private String createRepoSyncScheduleName(Channel chan, User user) {
        return "repo-sync-" + user.getOrg().getId() + "-" + chan.getId();
    }

    /**
     * Schedule a recurring reposync
     * @param chan the channel
     * @param user the user
     * @param cron the cron format
     * @return the Date?
     * @throws TaskomaticApiException if there was an error
     */
    public Date scheduleRepoSync(Channel chan, User user, String cron)
                                        throws TaskomaticApiException {
        String jobLabel = createRepoSyncScheduleName(chan, user);

        Map task = findScheduleByBunchAndLabel("repo-sync-bunch", jobLabel, user);
        if (task != null) {
            unscheduleRepoTask(jobLabel, user);
        }
        Map scheduleParams = new HashMap();
        scheduleParams.put("channel_id", chan.getId().toString());
        return (Date) invoke("tasko.scheduleBunch", user.getOrg().getId(),
                "repo-sync-bunch", jobLabel, cron, scheduleParams);
    }

    /**
     * Schedule a recurring reposync
     * @param chan the channel
     * @param user the user
     * @param cron the cron format
     * @param params parameters
     * @return the Date?
     * @throws TaskomaticApiException if there was an error
     */
    public Date scheduleRepoSync(Channel chan, User user, String cron,
            Map<String, String> params) throws TaskomaticApiException {
        String jobLabel = createRepoSyncScheduleName(chan, user);

        Map task = findScheduleByBunchAndLabel("repo-sync-bunch", jobLabel, user);
        if (task != null) {
            unscheduleRepoTask(jobLabel, user);
        }
        Map<String, String> scheduleParams = new HashMap<String, String>();
        scheduleParams.put("channel_id", chan.getId().toString());
        scheduleParams.putAll(params);

        return (Date) invoke("tasko.scheduleBunch", user.getOrg().getId(),
                "repo-sync-bunch", jobLabel, cron, scheduleParams);
    }

    /**
     * Creates a new single satellite schedule
     * @param user shall be sat admin
     * @param bunchName bunch name
     * @param params parameters for the bunch
     * @return date of the first schedule
     * @throws TaskomaticApiException if there was an error
     */
    public Date scheduleSingleSatBunch(User user, String bunchName,
            Map<String, String> params) throws TaskomaticApiException {
        ensureSatAdminRole(user);
        return (Date) invoke("tasko.scheduleSingleSatBunchRun", bunchName, params);
    }

    /**
     * Validates user has sat admin role
     * @param user shall be sat admin
     * @throws PermissionException if there was an error
     */
    private void ensureSatAdminRole(User user) {
        if (!user.hasRole(RoleFactory.SAT_ADMIN)) {
            ValidatorException.raiseException("satadmin.jsp.error.notsatadmin",
                                user.getLogin());
        }
    }

    /**
     * Validates user has org admin role
     * @param user shall be org admin
     * @throws PermissionException if there was an error
     */
    private void ensureOrgAdminRole(User user) {
        if (!user.hasRole(RoleFactory.ORG_ADMIN)) {
            throw new PermissionException(RoleFactory.ORG_ADMIN);
        }
    }

    /**
     * Validates user has channel admin role
     * @param user shall be channel admin
     * @throws PermissionException if there was an error
     */
    private void ensureChannelAdminRole(User user) {
        if (!user.hasRole(RoleFactory.CHANNEL_ADMIN)) {
            throw new PermissionException(RoleFactory.CHANNEL_ADMIN);
        }
    }

    /**
     * Creates a new schedule, unschedules, if en existing is defined
     * @param user shall be sat admin
     * @param jobLabel name of the schedule
     * @param bunchName bunch name
     * @param cron cron expression
     * @return date of the first schedule
     * @throws TaskomaticApiException if there was an error
     */
    public Date scheduleSatBunch(User user, String jobLabel, String bunchName, String cron)
    throws TaskomaticApiException {
        ensureSatAdminRole(user);
        Map task = findSatScheduleByBunchAndLabel(bunchName, jobLabel, user);
        if (task != null) {
            unscheduleSatTask(jobLabel, user);
        }
        return (Date) invoke("tasko.scheduleSatBunch", bunchName, jobLabel , cron,
                new HashMap());
    }

    /**
     * Unchedule a reposync task
     * @param chan the channel
     * @param user the user
     * @throws TaskomaticApiException if there was an error
     */
    public void unscheduleRepoSync(Channel chan, User user) throws TaskomaticApiException {
        String jobLabel = createRepoSyncScheduleName(chan, user);
        Map task = findScheduleByBunchAndLabel("repo-sync-bunch", jobLabel, user);
        if (task != null) {
            unscheduleRepoTask(jobLabel, user);
        }
    }

    private void unscheduleRepoTask(String jobLabel, User user)
        throws TaskomaticApiException {
        ensureChannelAdminRole(user);
        invoke("tasko.unscheduleBunch", user.getOrg().getId(), jobLabel);
    }

    /**
     * unschedule satellite task
     * @param jobLabel schedule name
     * @param user shall be satellite admin
     * @throws TaskomaticApiException if there was an error
     */
    public void unscheduleSatTask(String jobLabel, User user)
        throws TaskomaticApiException {
        ensureSatAdminRole(user);
        invoke("tasko.unscheduleSatBunches", singletonList(jobLabel));
    }

    /**
     * Return list of active schedules
     * @param user shall be sat admin
     * @return list of schedules
     * @throws TaskomaticApiException if there was an error
     */
    public List findActiveSchedules(User user) throws TaskomaticApiException {
        List<Map> schedules = (List<Map>) invoke("tasko.listActiveSatSchedules");
        return schedules;
    }

    /**
     * Return list of bunch runs
     * @param user shall be sat admin
     * @param bunchName name of the bunch
     * @return list of schedules
     * @throws TaskomaticApiException if there was an error
     */
    public List findRunsByBunch(User user, String bunchName) throws TaskomaticApiException {
        List<Map> runs = (List<Map>) invoke("tasko.listBunchSatRuns", bunchName);
        return runs;
    }

    private Map findScheduleByBunchAndLabel(String bunchName, String jobLabel, User user)
        throws TaskomaticApiException {
        List<Map> schedules = (List<Map>) invoke("tasko.listActiveSchedulesByBunch",
                user.getOrg().getId(), bunchName);
        for (Map schedule : schedules) {
            if (schedule.get("job_label").equals(jobLabel)) {
                return schedule;
            }
         }
        return null;
    }

    private Map findSatScheduleByBunchAndLabel(String bunchName, String jobLabel,
            User user) throws TaskomaticApiException {
        List<Map> schedules = (List<Map>) invoke("tasko.listActiveSatSchedulesByBunch",
                bunchName);
        for (Map schedule : schedules) {
            if (schedule.get("job_label").equals(jobLabel)) {
                return schedule;
            }
         }
        return null;
    }

    /**
     * Check whether there's an active schedule of given job label
     * @param jobLabel job label
     * @param user the user
     * @return true, if schedule exists
     * @throws TaskomaticApiException if there was an error
     */
    public boolean satScheduleActive(String jobLabel, User user)
        throws TaskomaticApiException {
        List<Map> schedules = (List<Map>) invoke("tasko.listActiveSatSchedules");
        for (Map schedule : schedules) {
            if (schedule.get("job_label").equals(jobLabel)) {
                return Boolean.TRUE;
            }
         }
        return Boolean.FALSE;
    }

    /**
     * Get the cron format for a single channel
     * @param chan the channel
     * @param user the user
     * @return the Cron format
     * @throws TaskomaticApiException if there was an error
     */
    public String getRepoSyncSchedule(Channel chan, User user)
        throws TaskomaticApiException {
        String jobLabel = createRepoSyncScheduleName(chan, user);
        Map task = findScheduleByBunchAndLabel("repo-sync-bunch", jobLabel, user);
        if (task == null) {
            return null;
        }
        return (String) task.get("cron_expr");
    }

    /**
     * Return list of available bunches
     * @param user shall be sat admin
     * @return list of bunches
     * @throws TaskomaticApiException if there was an error
     */
    public List listSatBunchSchedules(User user) throws TaskomaticApiException {
        List<Map> bunches = (List<Map>) invoke("tasko.listSatBunches");
        return bunches;
    }

    /**
     * looks up schedule according to id
     * @param user shall be sat admin
     * @param scheduleId schedule id
     * @return schedule
     * @throws TaskomaticApiException if there was an error
     */
    public Map lookupScheduleById(User user, Long scheduleId)
        throws TaskomaticApiException {
        return (Map) invoke("tasko.lookupScheduleById", scheduleId);
    }

    /**
     * looks up schedule according to label
     * @param user shall be sat admin
     * @param bunchName bunch name
     * @param scheduleLabel schedule label
     * @return schedule
     * @throws TaskomaticApiException if there was an error
     */
    public Map lookupScheduleByBunchAndLabel(User user, String bunchName,
            String scheduleLabel) throws TaskomaticApiException {
        return findSatScheduleByBunchAndLabel(bunchName, scheduleLabel, user);
    }

    /**
     * looks up bunch according to name
     * @param user shall be sat admin
     * @param bunchName bunch name
     * @return bunch
     * @throws TaskomaticApiException if there was an error
     */
    public Map lookupBunchByName(User user, String bunchName)
        throws TaskomaticApiException {
        return (Map) invoke("tasko.lookupBunchByName", bunchName);
    }

    /**
     * List all reposync schedules within an organization
     * @param org organization
     * @return list of schedules
     */
    private List<TaskoSchedule> listActiveRepoSyncSchedules(Org org) {
        try {
            return TaskoFactory.listActiveSchedulesByOrgAndBunch(org.getId().intValue(),
                    "repo-sync-bunch");
        }
        catch (NoSuchBunchTaskException e) {
            // no such schedules available
            return new ArrayList<TaskoSchedule>();
        }
    }

    /**
     * unschedule all outdated repo-sync schedules within an org
     * @param orgIn organization
     * @return number of removed schedules
     * @throws TaskomaticApiException if there was an error
     */
    @SuppressWarnings("unchecked")
    public int unscheduleInvalidRepoSyncSchedules(Org orgIn) throws TaskomaticApiException {
        Set<String> unscheduledLabels = new HashSet<String>();
        for (TaskoSchedule schedule : listActiveRepoSyncSchedules(orgIn)) {
            List<Long> channelIds = RepoSyncTask.getChannelIds(schedule.getDataMap());
            for (Long channelId : channelIds) {
                if (ChannelFactory.lookupById(channelId) == null) {
                    String label = schedule.getJobLabel();
                    if (!unscheduledLabels.contains(label)) {
                        invoke("tasko.unscheduleBunch", orgIn.getId(), label);
                        unscheduledLabels.add(label);
                    }
                }
            }
        }
        return unscheduledLabels.size();
    }

    /**
     * Schedule an Action execution for Salt minions.
     *
     * @param action the action to be executed
     * @param forcePackageListRefresh is a package list is requested
     * @param checkIfMinionInvolved check if action involves minions
     * @throws TaskomaticApiException if there was an error
     */
    public void scheduleActionExecution(Action action, boolean forcePackageListRefresh, boolean checkIfMinionInvolved)
            throws TaskomaticApiException {
        if (checkIfMinionInvolved) {
            boolean minionsInvolved = HibernateFactory.getSession()
                    .getNamedQuery("Action.findMinionIds")
                    .setParameter("id", action.getId())
                    .setMaxResults(1)
                    .stream()
                    .findAny()
                    .isPresent();
            if (!minionsInvolved) {
                return;
            }
        }
        scheduleMinionActionExecutions(singletonList(action), forcePackageListRefresh);
    }
    /**
     * Schedule Actions execution for Salt minions.
     *
     * @param actions the list of actions to be executed
     * @param forcePackageListRefresh is a package list is requested
     * @throws TaskomaticApiException if there was an error
     */
    public void scheduleMinionActionExecutions(List<Action> actions, boolean forcePackageListRefresh)
            throws TaskomaticApiException {
        List<Map<String, String>> paramsList = new ArrayList<>();
        for (Action action: actions) {
            Map<String, String> params = new HashMap<>();
            params.put("action_id", Long.toString(action.getId()));
            params.put("force_pkg_list_refresh", Boolean.toString(forcePackageListRefresh));
            params.put("earliest_action", action.getEarliestAction().toInstant().toString());
            paramsList.add(params);
        }
        invoke("tasko.scheduleRuns", MINION_ACTION_BUNCH_LABEL, MINION_ACTION_JOB_PREFIX, paramsList);
    }

    /**
     * Schedule an Action Chain execution for Salt minions.
     *
     * @param actionchain the actionchain to be executed
     * @throws TaskomaticApiException if there was an error
     */
    public void scheduleActionChainExecution(ActionChain actionchain)
        throws TaskomaticApiException {
        if (!ActionChainFactory.isActionChainTargettingMinions(actionchain)) {
            return;
        }

        Date earliestAction = actionchain.getEarliestAction();

        Map<String, String> params = new HashMap<>();
        params.put("actionchain_id", Long.toString(actionchain.getId()));

        invoke("tasko.scheduleSingleSatBunchRun", MINION_ACTIONCHAIN_BUNCH_LABEL,
                MINION_ACTIONCHAIN_JOB_PREFIX + actionchain.getId(), params,
                earliestAction);
    }

    /**
     * Schedule a staging job for Salt minions.
     *
     * @param actionId ID of the action to be executed
     * @param minionId ID of the minion involved
     * @param stagingDateTime scheduling time of staging
     * @throws TaskomaticApiException if there was an error
     */
    public void scheduleStagingJob(Long actionId, Long minionId, Date stagingDateTime)
        throws TaskomaticApiException {
        Map<String, String> params = new HashMap<String, String>();
        params.put("action_id", Long.toString(actionId));
        params.put("staging_job", "true");
        params.put("staging_job_minion_server_id", Long.toString(minionId));

        invoke("tasko.scheduleSingleSatBunchRun", MINION_ACTION_BUNCH_LABEL,
                MINION_ACTION_JOB_DOWNLOAD_PREFIX + actionId + "-" + minionId, params,
                stagingDateTime);
    }

    /**
     * Schedule a staging job for Salt minions.
     * @param actionData Map containing mapping between action and minions data
     * @throws TaskomaticApiException if there was an error
     */
    public void scheduleStagingJobs(Map<Long, Map<Long, ZonedDateTime>> actionData) throws TaskomaticApiException {
        List<Map<String, String>> paramList = actionData.entrySet()
                .stream()
                .flatMap(actionEntry -> actionEntry.getValue().entrySet().stream()
                        .map(minionData -> {
                            Map<String, String> params = new HashMap<>();
                            params.put("action_id", Long.toString(actionEntry.getKey()));
                            params.put("staging_job", "true");
                            params.put("staging_job_minion_server_id", Long.toString(minionData.getKey()));
                            params.put("earliest_action", minionData.getValue().toInstant().toString());
                            return params;
                        })).collect(Collectors.toList());
        invoke("tasko.scheduleRuns", MINION_ACTION_BUNCH_LABEL, MINION_ACTION_JOB_DOWNLOAD_PREFIX, paramList);
    }

    /**
     * Schedule an Action execution for Salt minions, without forced
     * package refresh.
     *
     * @param action the action to be executed
     * @throws TaskomaticApiException if there was an error
     */
    public void scheduleActionExecution(Action action)
        throws TaskomaticApiException {
        scheduleActionExecution(action, false);
    }

    /**
     * Schedule an Action execution for Salt minions.
     *
     * @param action the action to be executed
     * @param forcePackageListRefresh is a package list is requested
     * @throws TaskomaticApiException if there was an error
     */
    public void scheduleActionExecution(Action action, boolean forcePackageListRefresh)
            throws TaskomaticApiException {
        scheduleActionExecution(action, forcePackageListRefresh, true);
    }

    /**
     * Schedule a channel subscription action.
     *
     * @param user the user that schedules the action
     * @param action the action to schedule
     * @throws TaskomaticApiException if there was an error
     */
    public void scheduleSubscribeChannels(User user, SubscribeChannelsAction action)
            throws TaskomaticApiException {
        Map<String, String> params = new HashMap<>();
        params.put("action_id", Long.toString(action.getId()));
        params.put("user_id", Long.toString(user.getId()));
        invoke("tasko.scheduleSingleSatBunchRun", MINION_ACTION_BUNCH_LABEL,
                MINION_ACTION_JOB_PREFIX + action.getId(), params,
                action.getEarliestAction());
    }

    /**
     * Delete several scheduled Actions.
     *
     * @param actionMap mapping from Actions to involved Servers
     * @throws TaskomaticApiException if there was an error
     */
    public void deleteScheduledActions(Map<Action, Set<Server>> actionMap)
        throws TaskomaticApiException {

        Stream<Action> actionsToBeUnscheduled = actionMap.entrySet().stream()
            // select Actions that have no minions besides those in the specified set
            // (those that have any other minion should NOT be unscheduled!)
            .filter(e -> e.getKey().getServerActions().stream()
                    .map(ServerAction::getServer)
                    .filter(s -> MinionServerUtils.isMinionServer(s))
                    .allMatch(s -> e.getValue().contains(s))
            )
            .map(Map.Entry::getKey);

        List<String> jobLabels = actionsToBeUnscheduled
                .map(a -> MINION_ACTION_JOB_PREFIX + a.getId())
                .collect(Collectors.toList());

        if (!jobLabels.isEmpty()) {
            LOG.debug("Unscheduling jobs: " + jobLabels);
            invoke("tasko.unscheduleSatBunches", jobLabels);
        }
    }
}
