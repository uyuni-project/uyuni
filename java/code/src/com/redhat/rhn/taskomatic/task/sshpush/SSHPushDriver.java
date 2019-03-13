/**
 * Copyright (c) 2013 SUSE LLC
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
package com.redhat.rhn.taskomatic.task.sshpush;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.domain.action.ActionFactory;
import com.suse.manager.utils.SaltUtils;
import org.apache.log4j.Logger;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.taskomatic.TaskoFactory;
import com.redhat.rhn.taskomatic.domain.TaskoSchedule;
import com.redhat.rhn.taskomatic.task.TaskConstants;
import com.redhat.rhn.taskomatic.task.threaded.QueueDriver;
import com.redhat.rhn.taskomatic.task.threaded.QueueWorker;

/**
 * Call rhn_check on relevant systems via SSH using remote port forwarding.
 */
public class SSHPushDriver implements QueueDriver {

    // Synchronized set of systems we are currently talking to
    private static Set<SSHPushSystem> currentSystems =
            Collections.synchronizedSet(new HashSet<SSHPushSystem>());

    // String constants
    private static final String WORKER_THREADS_KEY = "taskomatic.ssh_push_workers";
    private static final String PORT_HTTPS_KEY = "ssh_push_port_https";
    private static final String JOB_LABEL = "ssh-push-default";

    // Logger passed in from the Job class (SSHPush)
    private Logger log;

    // Port number to use for remote port forwarding
    private int remotePort;

    // Properties used for generating random checkin thresholds
    private int thresholdMax;
    private int thresholdMin;
    private double mean;
    private double stddev;

    // Properties used to determine when to look for checkin candidates
    private int checkInterval = SSHPushUtils.CHECK_INTERVAL;
    private int moduloRemainder;

    /**
     * Get the set of systems we are currently talking to via SSH Push.
     * @return set of systems
     */
    public static Set<SSHPushSystem> getCurrentSystems() {
        return currentSystems;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initialize() {
        // Read the remote port for SSH tunneling from config
        remotePort = Config.get().getInt(PORT_HTTPS_KEY);
        if (log.isDebugEnabled()) {
            log.debug("SSHPushDriver will use port " + remotePort);
        }

        // Init random checkin threshold generation
        int thresholdDays = Config.get().getInt(ConfigDefaults
                .SYSTEM_CHECKIN_THRESHOLD);
        thresholdMax = thresholdDays * 86400;
        thresholdMin = Math.round(thresholdMax / 2);
        mean = thresholdMax;
        stddev = thresholdMax / 6;

        // Randomly select a modulo remainder
        moduloRemainder = SSHPushUtils.nextRandom(0, checkInterval - 1);
        if (log.isDebugEnabled()) {
            log.debug("We will look for checkin candidates every " + checkInterval +
                    " minutes (remainder = " + moduloRemainder + ")");
        }

        // Skip all running or ready jobs if any
        int skipped = skipRunningJobs();
        if (log.isDebugEnabled()) {
            log.debug("Found " + skipped + " jobs to be RUNNING/READY, skipping...");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<SSHPushSystem> getCandidates() {
        List<SSHPushSystem> candidates = new LinkedList<>();

        // Find traditional systems with actions scheduled
        candidates.addAll(getTraditionalCandidates());

        // Find Salt systems currently rebooting,
        // i.e with reboot actions in status picked-up with picked-up time older than 4 minutes
        List<SSHPushSystem> rebootCandidates = getRebootingMinions();
        for (SSHPushSystem s : rebootCandidates) {
            s.setRebooting(true);
            if (!candidates.contains(s)) {
                candidates.add(s);
            }
        }

        // For Salt minions, check all queued actions having a completed reboot prerequisite.
        // Queued actions with a completed reboot prerequisite can appear when a reboot action completes successfully
        // but resuming the action chain is not successful after reboot so the subsequent actions remain in state queued
        // or the reboot action is set to completed before the action chain is resumed
        List<SSHPushAction> queuedActions = getQueuedMinionActionsWithPrerequisites();
        for (SSHPushAction a : queuedActions) {
            Action action = ActionFactory.lookupById(a.getActionId());

            if (SaltUtils.prerequisiteIsCompleted(action, Optional.of(ActionFactory.TYPE_REBOOT), a.getSystemId())) {
                SSHPushSystem s = new SSHPushSystem(a.getSystemId(), a.getSystemName(), a.getMinionId(), true);
                if (!candidates.contains(s)) {
                    candidates.add(s);
                }
            }
        }

        // Look for checkin candidates every <moduloDivisor> minutes
        Calendar cal = Calendar.getInstance();
        int currentMinutes = cal.get(Calendar.MINUTE);
        if (log.isDebugEnabled()) {
            log.debug("Current minutes: " + currentMinutes);
        }

        if (!isDefaultSchedule() || currentMinutes % checkInterval == moduloRemainder) {
            long currentTimestamp = cal.getTime().getTime() / 1000;
            for (SSHPushSystem s : getCheckinCandidates()) {
                // Last checkin timestamp in seconds
                long lastCheckin = s.getLastCheckin().getTime() / 1000;

                // Determine random threshold in [t/2,t] using t as the mean and stddev
                // as defined above.
                long randomThreshold = SSHPushUtils.getRandomThreshold(
                        mean, stddev, thresholdMin, thresholdMax);
                long compareValue = currentTimestamp - randomThreshold;

                if (log.isDebugEnabled()) {
                    log.debug("Candidate --> " + s.getName());
                    log.debug("Last checkin: " + s.getLastCheckin().toString());
                    log.debug("Random threshold (hours): " +
                            SSHPushUtils.toHours(randomThreshold));
                }

                // Do not add candidates twice
                if (lastCheckin < compareValue && !candidates.contains(s)) {
                    if (log.isDebugEnabled()) {
                        log.debug("Contacting system for checkin: " + s.getName());
                    }
                    candidates.add(s);
                }
            }
        }

        if (log.isDebugEnabled()) {
            log.debug("Found " + candidates.size() + " candidates");
        }

        // Do not return candidates we are talking to already
        synchronized (currentSystems) {
            for (SSHPushSystem s : currentSystems) {
                if (candidates.contains(s)) {
                    log.debug("Skipping system: " + s.getName());
                    candidates.remove(s);
                }
            }
        }

        return candidates;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public QueueWorker makeWorker(Object item) {
        SSHPushSystem system = (SSHPushSystem) item;

        // Create a Salt worker if the system has a minion id
        if (system.getMinionId() != null) {
            return new SSHPushWorkerSalt(getLogger(), system);
        }
        else {
            return new SSHPushWorker(getLogger(), remotePort, system);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxWorkers() {
        return Config.get().getInt(WORKER_THREADS_KEY, 2);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canContinue() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setLogger(Logger loggerIn) {
        this.log = loggerIn;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Logger getLogger() {
        return log;
    }

    /**
     * Run query to find all candidates with actions scheduled for right now.
     *
     * @return list of candidates with actions scheduled
     */
    @SuppressWarnings("unchecked")
    private DataResult<SSHPushSystem> getTraditionalCandidates() {
        SelectMode select = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_SSH_PUSH_FIND_TRADITIONAL_CANDIDATES);
        return select.execute();
    }

    /**
     * Run query to find all candidates with ongoing reboot actions.
     *
     * @return list of candidates with ongoing reboot actions
     */
    @SuppressWarnings("unchecked")
    private DataResult<SSHPushSystem> getRebootingMinions() {
        SelectMode select = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_SSH_PUSH_FIND_REBOOTING_MINIONS);
        return select.execute();
    }

    /**
     * Run query to find all minions with queued actions having prerequisites
     * scheduled before now.
     *
     * @return list of candidates with ongoing reboot actions
     */
    @SuppressWarnings("unchecked")
    private DataResult<SSHPushAction> getQueuedMinionActionsWithPrerequisites() {
        SelectMode select = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_SSH_PUSH_FIND_QUEUED_MINION_ACTIONS_WITH_PREREQ);
        return select.execute();
    }

    /**
     * Run query to find checkin candidates to consider (last_checkin < now - t/2).
     *
     * @return list of checkin candidates
     */
    @SuppressWarnings("unchecked")
    private DataResult<SSHPushSystem> getCheckinCandidates() {
        SelectMode select = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_SSH_PUSH_FIND_CHECKIN_CANDIDATES);
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("checkin_threshold", thresholdMin);
        return select.execute(params);
    }

    /**
     * Check if the schedule for this job is once per minute.
     *
     * @return true if the schedule is once per minute, false otherwise
     */
    private boolean isDefaultSchedule() {
        List<TaskoSchedule> schedules =
                TaskoFactory.listActiveSchedulesByOrgAndLabel(null, JOB_LABEL);
        return (schedules.size() == 1) &&
                (schedules.get(0).getCronExpr().equals("0 * * * * ?"));
    }

    /**
     * Skip all currently RUNNING or READY jobs in case of taskomatic restart.
     *
     * @return number of running jobs skipped
     */
    private int skipRunningJobs() {
        WriteMode delete = ModeFactory.getWriteMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_SKIP_RUNNING_AND_READY_JOBS_BY_LABEL);
        HashMap<String, Object> params = new HashMap<String, Object>();
        params.put("job_label", JOB_LABEL);
        return delete.executeUpdate(params);
    }
}
