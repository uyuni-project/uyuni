/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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
package com.redhat.rhn.taskomatic.task;

import com.redhat.rhn.GlobalInstanceHolder;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.notification.NotificationMessage;
import com.redhat.rhn.domain.notification.UserNotificationFactory;
import com.redhat.rhn.domain.notification.types.EndOfLifePeriod;
import com.redhat.rhn.domain.notification.types.PaygNotCompliantWarning;
import com.redhat.rhn.domain.notification.types.SubscriptionWarning;
import com.redhat.rhn.domain.notification.types.UpdateAvailable;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.role.RoleFactory;
import com.redhat.rhn.frontend.dto.ActionMessage;
import com.redhat.rhn.frontend.dto.AwolServer;
import com.redhat.rhn.frontend.dto.OrgIdWrapper;
import com.redhat.rhn.frontend.dto.ReportingUser;

import com.suse.cloud.CloudPaygManager;
import com.suse.manager.maintenance.BaseProductManager;
import com.suse.manager.utils.MailHelper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.JobExecutionContext;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;

/**
 * DailySummary task.
 * sends daily report of stats. reaps org suggestions
 * from rhnDailySummaryQueue. Not very "daily" since it runs every
 * 30 seconds.  Need to look at RHN::DailySummaryEngine.  This task
 * queues org emails, mails queued emails, then dequeues the emails.
 */
public class DailySummary extends RhnJavaJob {

    private static final Logger LOGGER = LogManager.getLogger(DailySummary.class);

    private static final int HEADER_SPACER = 10;
    private static final int ERRATA_SPACER = 4;
    private static final String ERRATA_UPDATE = "Errata Update";
    private static final String ERRATA_INDENTION = StringUtils.repeat(" ", ERRATA_SPACER);

    private static final BaseProductManager END_OF_LIFE_MANAGER = new BaseProductManager();
    private final CloudPaygManager cloudPaygManager = GlobalInstanceHolder.PAYG_MANAGER;

    @Override
    public String getConfigNamespace() {
        return "daily_summary";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(JobExecutionContext ctxIn) {

        processUpdateAvailableNotification();
        processEndOfLifeNotification();
        processSubscriptionWarningNotification();
        processPaygNotCompliantNotification();

        processEmails();
    }

    private void processEndOfLifeNotification() {
        if (ConfigDefaults.get().isUyuni()) {
            // No end of life for Uyuni
            return;
        }

        // Notify only on the first day of the month
        final LocalDate today = LocalDate.now();
        if (today.getDayOfMonth() != 1) {
            return;
        }

        if (END_OF_LIFE_MANAGER.isNotificationPeriod(today)) {
            NotificationMessage notification = UserNotificationFactory.createNotificationMessage(
                new EndOfLifePeriod(END_OF_LIFE_MANAGER.getEndOfLifeDate()));
            UserNotificationFactory.storeNotificationMessageFor(notification,
                Collections.singleton(RoleFactory.ORG_ADMIN), Optional.empty());
        }
    }

    private void  processSubscriptionWarningNotification() {
        if (Instant.now().atZone(ZoneId.systemDefault()).getDayOfWeek() != DayOfWeek.MONDAY) {
            // we want to show this notification only on Mondays
            return;
        }
        SubscriptionWarning sw = new SubscriptionWarning();
        if (sw.expiresSoon()) {
            NotificationMessage notificationMessage =
                    UserNotificationFactory.createNotificationMessage(new SubscriptionWarning());
            UserNotificationFactory.storeNotificationMessageFor(notificationMessage,
                    Collections.singleton(RoleFactory.ORG_ADMIN), Optional.empty());
        }
    }

    private void  processUpdateAvailableNotification() {
        UpdateAvailable uan = new UpdateAvailable(Runtime.getRuntime());
        if (uan.updateAvailable()) {
            NotificationMessage notificationMessage =
                    UserNotificationFactory.createNotificationMessage(uan);
            UserNotificationFactory.storeNotificationMessageFor(notificationMessage,
                    Collections.singleton(RoleFactory.SAT_ADMIN), Optional.empty());
        }
    }

    private void processPaygNotCompliantNotification() {
        if (Instant.now().atZone(ZoneId.systemDefault()).getDayOfWeek() != DayOfWeek.MONDAY) {
            // we want to show this notification only on Mondays
            return;
        }

        // This notification will be process only if SUMA is PAYG but is not compliant
        if (cloudPaygManager.isPaygInstance() && !cloudPaygManager.isCompliant()) {
            NotificationMessage notificationMessage =
                    UserNotificationFactory.createNotificationMessage(
                            new PaygNotCompliantWarning());
            UserNotificationFactory.storeNotificationMessageFor(notificationMessage,
                    Collections.emptySet(), Optional.empty()); // This notification will be sent to everyone
        }
    }

    private void processEmails() {
        SelectMode m = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_DAILY_SUMMARY_QUEUE);
        List results = m.execute();


        OrgIdWrapper oiw = null;
        for (Object resultIn : results) {
            try {
                oiw = (OrgIdWrapper) resultIn;
                if (log.isDebugEnabled()) {
                    log.debug("dealing with org: {}", oiw.toLong());
                }
                queueOrgEmails(oiw.toLong());
            }
            catch (Exception e) {
                log.error(e.getMessage(), e);
            }
            finally {
                try {
                    dequeueOrg(oiw.toLong());
                    if (log.isDebugEnabled()) {
                        log.debug("org {} removed from queue", oiw.toLong());
                    }
                }
                finally {
                    HibernateFactory.commitTransaction();
                    HibernateFactory.closeSession();
                }
            }
        }
    }

    /**
     * DO NOT CALL FROM OUTSIDE THIS CLASS. Removes the orgs from the queue
     * table.
     * @param orgId Org Id to be dequeued.
     * @return # of orgs dequeued
     */
    public int dequeueOrg(Long orgId) {
        WriteMode m = ModeFactory.getWriteMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_DEQUEUE_DAILY_SUMMARY);
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", orgId);
        return m.executeUpdate(params);
    }

    /**
     * DO NOT CALL FROM OUTSIDE THIS CLASS. Queues up the Org Emails for
     * mailing.
     * @param orgId Org Id to be processed.
     */
    public void queueOrgEmails(Long orgId) {
        SelectMode m = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_USERS_WANTING_REPORTS);
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", orgId);

        StopWatch watch = new StopWatch();
        watch.start();
        List users = m.execute(params);
        for (Object userIn : users) {
            ReportingUser ru = (ReportingUser) userIn;
            // run_user
            List awol = getAwolServers(ru.idAsLong());
            // send email
            List actions = getActionInfo(ru.idAsLong());
            if ((awol == null || awol.isEmpty()) && (actions == null ||
                    actions.isEmpty())) {
                log.debug("Skipping ORG {} because daily summary info has changed", orgId);
                continue;
            }

            String awolMsg = renderAwolServersMessage(awol);
            String actionMsg = renderActionsMessage(actions);

            String emailMsg = prepareEmail(
                    ru.getLogin(), ru.getAddress(), awolMsg, actionMsg);

            LocalizationService ls = LocalizationService.getInstance();
            String subject = ls.getMessage(
                    "dailysummary.email.subject", ls.formatShortDate(new Date()));
            MailHelper.withSmtp().sendEmail(ru.getAddress(), subject, emailMsg);
        }
        watch.stop();
        if (log.isDebugEnabled()) {
            log.debug("queued emails of org of {} users in {}ms", users.size(), watch.getTime());
        }
    }

    /**
     * DO NOT CALL FROM OUTSIDE THIS CLASS. Returns the list of awol servers.
     * @param uid User id whose awol servers are sought.
     * @return the list of recent awol servers.
     */
    public List getAwolServers(Long uid) {
        SelectMode m = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_USERS_AWOL_SERVERS);
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", uid);

        return m.execute(params);
    }

    /**
     * DO NOT CALL FROM OUTSIDE THIS CLASS. Returns the list of recent actions.
     * @param uid User id whose recent actions are sought.
     * @return the list of recent actions.
     */
    public List getActionInfo(Long uid) {
        SelectMode m = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_GET_ACTION_INFO);
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", uid);

        return m.execute(params);
    }

    /**
     * DO NOT CALL FROM OUTSIDE THIS CLASS. Renders the awol servers message
     * @param servers list of awol servers
     * @return the awol servers message
     */
    public String renderAwolServersMessage(List servers) {
        if (servers == null || servers.isEmpty()) {
            return "";
        }
        /*
         * The Awol message is going to be a table containing a list of systems
         * that have gone AWOL.
         *
         * All the calculation crap for tables will be done...  how many spaces
         * between columns and the column width for the given data.
         * This means that we will read through the data twice, once to find the
         * longest entries and again to build the return string.
         *
         * Since this will be going in an email, if the receiver doesn't use
         * monospace fonts *ever* than all this calculation is for nothing.
         */
        LocalizationService ls = LocalizationService.getInstance();
        String sid = ls.getMessage("taskomatic.daily.sid"); //System Id column
        String sname = ls.getMessage("taskomatic.daily.systemname"); //System Name column
        String checkin = ls.getMessage("taskomatic.daily.checkin"); //Last Checkin column

        //First we need to figure out how long the width of the columns should be.
        int minDiff = 4; //this is the minimum spaces between header elements
        int sidLength = sid.length() + minDiff;
        int snameLength = sid.length() + minDiff;

        //Find the longest entry in the table for both sid and sname.
        for (Object oIn : servers) {
            AwolServer as = (AwolServer) oIn;
            String currentId = as.getId().toString();
            if (currentId.length() >= sidLength) {
                //extra space so the longest entry doesn't connect to the next column
                sidLength = currentId.length() + 1;
            }
            String currentName = as.getName();
            if (currentName.length() >= snameLength) {
                //extra space so the longest entry doesn't connect to the next column
                snameLength = currentName.length() + 1;
            }
        }

        //render the header--  System Id        System Name        LastCheckin
        StringBuilder buf = new StringBuilder();
        buf.append(sid);
        buf.append(StringUtils.repeat(" ", sidLength - sid.length()));
        buf.append(sname);
        buf.append(StringUtils.repeat(" ", snameLength - sname.length()));
        buf.append(checkin);
        buf.append("\n");

        //Now render the data in the table
        for (Object serverIn : servers) {
            AwolServer as = (AwolServer) serverIn;
            String currentId = as.getId().toString();
            buf.append(currentId);
            buf.append(StringUtils.repeat(" ", sidLength - currentId.length()));
            String currentName = as.getName();
            buf.append(currentName);
            buf.append(StringUtils.repeat(" ", snameLength - currentName.length()));
            buf.append(as.getCheckin());
            buf.append("\n");
        }

        //Lastly, create the url for the link in the email.
        StringBuilder url = new StringBuilder();
        url.append("https://");
        url.append(getHostname());
        url.append("/rhn/manager/systems/list/all?q=awol&qc=status_type");

        return LocalizationService.getInstance().getMessage(
                "taskomatic.msg.awolservers", buf.toString(), url);
    }

    /**
     * DO NOT CALL FROM OUTSIDE THIS CLASS. Renders the actions email message
     * @param actions list of recent actions
     * @return the actions email message
     */
    public String renderActionsMessage(List<ActionMessage> actions) {

        int longestActionLength = HEADER_SPACER;
        int longestStatusLength = 0;
        StringBuilder hdr = new StringBuilder();
        StringBuilder body = new StringBuilder();
        StringBuilder legend = new StringBuilder();
        StringBuilder msg = new StringBuilder();
        LinkedHashSet<String> statusSet = new LinkedHashSet<>();
        TreeMap<String, Map<String, Integer>> nonErrataActions = new TreeMap<>();
        TreeMap<String, Map<String, Integer>> errataActions = new TreeMap<>();
        TreeMap<String, String> errataSynopsis = new TreeMap<>();

        legend.append(LocalizationService
                .getInstance().getMessage("taskomatic.daily.errata"));
        legend.append("\n\n");

        for (ActionMessage am : actions) {

            if (!statusSet.contains(am.getStatus())) {
                statusSet.add(am.getStatus());
                if (am.getStatus().length() > longestStatusLength) {
                    longestStatusLength = am.getStatus().length();
                }
            }

            if (am.getType().equals(ERRATA_UPDATE)) {
                String advisoryKey = ERRATA_INDENTION + am.getAdvisory();

                if (!errataActions.containsKey(advisoryKey)) {
                    errataActions.put(advisoryKey, new HashMap<>());
                    if (advisoryKey.length() + HEADER_SPACER > longestActionLength) {
                        longestActionLength = advisoryKey.length() + HEADER_SPACER;
                    }
                }
                Map<String, Integer> counts = errataActions.get(advisoryKey);
                counts.put(am.getStatus(), am.getCount());

                if (am.getAdvisory() != null &&
                        !errataSynopsis.containsKey(am.getAdvisory())) {
                    errataSynopsis.put(am.getAdvisory(), am.getSynopsis());
                }
            }
            else {
                if (!nonErrataActions.containsKey(am.getType())) {
                    if (am.getType().equals("Product Migration")) {
                        am.setType("Product Migration (Total)");
                    }
                    if (am.getType().equals("Apply states")) {
                        am.setType("Apply states (total)");
                    }
                    nonErrataActions.put(am.getType(), new HashMap<>());
                    if (am.getType().length() + HEADER_SPACER > longestActionLength) {
                        longestActionLength = am.getType().length() + HEADER_SPACER;
                    }
                }
                Map<String, Integer> counts = nonErrataActions.get(am.getType());
                counts.put(am.getStatus(), am.getCount());
            }

        }

        hdr.append(StringUtils.repeat(" ", longestActionLength));
        for (String status : statusSet) {
            hdr.append(status + StringUtils.repeat(" ", (longestStatusLength +
                    ERRATA_SPACER) - status.length()));
        }

        if (!errataActions.isEmpty()) {
            body.append("Patch(es) Update" + ":" + "\n");
        }
        StringBuffer formattedErrataActions = renderActionTree(longestActionLength,
                longestStatusLength, statusSet, errataActions);
        body.append(formattedErrataActions);

        for (String advisory : errataSynopsis.keySet()) {
            legend.append(ERRATA_INDENTION + advisory + ERRATA_INDENTION +
                    errataSynopsis.get(advisory) + "\n");
        }

        StringBuffer formattedNonErrataActions = renderActionTree(longestActionLength,
                longestStatusLength, statusSet, nonErrataActions);
        body.append(formattedNonErrataActions);

        // finally put all this together
        msg.append(hdr);
        msg.append("\n");
        msg.append(body);
        msg.append("\n\n");
        if (!errataSynopsis.isEmpty()) {
            msg.append(legend);
        }
        return msg.toString();
    }

    private StringBuffer renderActionTree(int longestActionLength,
            int longestStatusLength, LinkedHashSet<String> statusSet,
            TreeMap<String, Map<String, Integer>> actionTree) {
        StringBuffer formattedActions = new StringBuffer();
        for (String actionName : actionTree.keySet()) {
            formattedActions.append(actionName +
                   StringUtils.repeat(" ", (longestActionLength - (actionName.length()))));
            for (String status : statusSet) {
                Map<String, Integer> counts = actionTree.get(actionName);
                Integer theCount = counts.get(status);
                theCount = counts.getOrDefault(status, 0);
                formattedActions.append(theCount);
                formattedActions.append(StringUtils.repeat(" ", longestStatusLength +
                        ERRATA_SPACER - theCount.toString().length()));
            }
            formattedActions.append("\n");
        }
        return formattedActions;
    }

    /**
     * DO NOT CALL FROM OUTSIDE THIS CLASS. Prepares the email message string
     * @param login users login
     * @param email email address
     * @param awolMsg the awol servers msg
     * @param actionMsg the recent actions message
     * @return the email message string
     */
    public String prepareEmail(
            String login, String email, String awolMsg, String actionMsg) {

        LocalizationService ls = LocalizationService.getInstance();
        String[] args = new String[8];
        args[0] = login;
        args[1] = ls.formatDate(new Date());
        args[2] = actionMsg;
        args[3] = awolMsg;
        args[4] = getHostname();
        // why the hell are these in OrgFactory?
        args[5] = OrgFactory.EMAIL_FOOTER.getValue();
        args[6] = OrgFactory.EMAIL_ACCOUNT_INFO.getValue();

        if (cloudPaygManager.isPaygInstance() && !cloudPaygManager.isCompliant()) {
            args[7] = String.format("%s\n", ls.getMessage("dailysummary.email.notpaygcompliant"));
        }
        else {
            args[7] = "";
        }

        String msg =  ls.getMessage(
                "dailysummary.email.body", (Object[])args);

        // wow, what an ugly @$$ hack, but this requires rewriting
        // the email templating engine which kinda sucks.
        msg = StringUtils.replace(msg, "<login />", login);
        return StringUtils.replace(msg, "<email-address />", email);
    }

    private String getHostname() {
        return ConfigDefaults.get().getHostname();
    }
}
