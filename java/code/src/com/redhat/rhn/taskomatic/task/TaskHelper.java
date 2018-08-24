/**
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

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.domain.action.Action;
import com.redhat.rhn.taskomatic.InvalidParamException;
import com.redhat.rhn.taskomatic.NoSuchBunchTaskException;
import com.redhat.rhn.taskomatic.TaskoXmlRpcHandler;
import com.redhat.rhn.taskomatic.TaskomaticApi;
import com.suse.manager.utils.MailHelper;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * TaskHelper
 * Helper class to provide common functionality to tasks
 * @version $Rev$
 */
public class TaskHelper {

    private static final Logger LOG = Logger.getLogger(TaskHelper.class);

    /**
     * private constructor
     */
    private TaskHelper() {
    }

    /**
     * Send an error email to the Satellite admin
     * @param messageBody to send.
     */
    public static void sendErrorEmail(String messageBody) {
        LocalizationService ls = LocalizationService.getInstance();
        String[] recipients = MailHelper.getAdminRecipientsFromConfig();

        StringBuilder subject = new StringBuilder();
        subject.append(ls.getMessage("web traceback subject", Locale.getDefault()));
        try {
            subject.append(InetAddress.getLocalHost().getHostName());
        }
        catch (Throwable t) {
            subject.append("Taskomatic");
        }
        MailHelper.withSmtp().sendEmail(recipients, subject.toString(), messageBody);

    }

    /**
     * Send an information about task run failure/success
     * @param orgId organization id
     * @param messageBody to send.
     */
    public static void sendTaskoEmail(Integer orgId, String messageBody) {
        Config c = Config.get();
        LocalizationService ls = LocalizationService.getInstance();
        String[] recipients = null;
        if (orgId != null) {
            List<String> emails = getActiveOrgAdminEmails(orgId);
            recipients = !emails.isEmpty() ?
                    emails.toArray(new String[emails.size()]) :
                    MailHelper.getAdminRecipientsFromConfig();
        }
        StringBuilder subject = new StringBuilder();
        subject.append(ls.getMessage("taskomatic notif subject", Locale.getDefault()));
        try {
            subject.append(" from " + InetAddress.getLocalHost().getHostName());
        }
        catch (Throwable t) {
            // nothing
        }
        MailHelper.withSmtp().sendEmail(recipients, subject.toString(), messageBody);
    }

    /**
     * Gets the list of active org admins in given org.
     * @return Returns the set of active org admins in given org.
     */
    private static List<String> getActiveOrgAdminEmails(Integer orgId) {
        SelectMode m = ModeFactory.getMode("User_queries", "active_org_admin_emails");
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("org_id", orgId);
        DataResult<Map> dr = m.execute(params);
        List toReturn = new ArrayList<String>();
        if (dr != null) {
            for (Map item : dr) {
                toReturn.add(item.get("email"));
            }
        }

        return toReturn;
    }

    /**
     * Schedule Action execution for Salt minions from within taskomatic.
     *
     * @param action the action to be executed
     */
    public static void scheduleActionExecution(Action action) {
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

        try {
            Map<String, String> params = new HashMap<String, String>();
            params.put("action_id", Long.toString(action.getId()));
            params.put("force_pkg_list_refresh", Boolean.toString(false));
            new TaskoXmlRpcHandler().scheduleSingleSatBunchRun(TaskomaticApi.MINION_ACTION_BUNCH_LABEL,
                    TaskomaticApi.MINION_ACTION_JOB_PREFIX + action.getId(), params, action.getEarliestAction());
        }
        catch (NoSuchBunchTaskException | InvalidParamException e) {
            LOG.error("Could not schedule action: " + action.getActionType(), e);
        }
    }
}
