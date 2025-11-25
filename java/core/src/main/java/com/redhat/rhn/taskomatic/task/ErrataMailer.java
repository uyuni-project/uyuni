/*
 * Copyright (c) 2009--2015 Red Hat, Inc.
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 *
 * SPDX-License-Identifier: GPL-2.0-only
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package com.redhat.rhn.taskomatic.task;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.db.datasource.DataResult;
import com.redhat.rhn.common.db.datasource.ModeFactory;
import com.redhat.rhn.common.db.datasource.Row;
import com.redhat.rhn.common.db.datasource.SelectMode;
import com.redhat.rhn.common.db.datasource.WriteMode;
import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.messaging.JavaMailException;
import com.redhat.rhn.domain.errata.Errata;
import com.redhat.rhn.domain.org.OrgFactory;

import com.suse.manager.utils.MailHelper;

import org.apache.commons.lang3.StringUtils;
import org.quartz.JobExecutionContext;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * This is a port of the ErrataEngine taskomatic task
 */
public class ErrataMailer extends RhnJavaJob {

    @Override
    public String getConfigNamespace() {
        return "errata_mailer";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void execute(JobExecutionContext context) {

        List<Row> results = getErrataToProcess();
        if (results == null || results.isEmpty()) {
            log.debug("No patch found...exiting");
        }
        else {
            log.debug("=== Queued up {} patches", results.size());
            for (Row row: results) {
                Long errataId = (Long) row.get("errata_id");
                Long orgId = (Long) row.get("org_id");
                Long channelId = (Long) row.get("channel_id");
                markErrataDone(errataId, orgId, channelId);
                if (OrgFactory.lookupById(orgId).getOrgConfig().isErrataEmailsEnabled()) {
                    log.debug("Processing patch {} for org {}", errataId, orgId);
                    try {
                        sendEmails(errataId, orgId, channelId);
                        log.debug("Finished patch {} for org {}", errataId, orgId);
                    }
                    catch (JavaMailException e) {
                        log.error("Error sending mail", e);
                    }
                }
                else {
                    log.debug("Errata notifications disabled for whole org {} => skipping {}", orgId, errataId);
                }
            }
        }
    }

    protected List<Row> getErrataToProcess() {
        SelectMode select = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_ERRATAMAILER_FIND_ERRATA);
        Map<String, Object> params = new HashMap<>();
        params.put("threshold", 1);
        return select.execute(params);
    }

    private void markErrataDone(Long errataId, Long orgId, Long channelId) {
        HibernateFactory.getSession();
        WriteMode marker = ModeFactory.getWriteMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_ERRATAMAILER_MARK_ERRATA_DONE);
        Map<String, Object> params = new HashMap<>();
        params.put("org_id", orgId);
        params.put("errata_id", errataId);
        params.put("channel_id", channelId);
        int rowsUpdated = marker.executeUpdate(params);
        if (log.isDebugEnabled()) {
            log.debug("Marked {} rows complete", rowsUpdated);
        }
    }

    private void sendEmails(Long errataId, Long orgId, Long channelId) {
        Errata errata = HibernateFactory.getSession().getReference(Errata.class, errataId);
        List<Row> orgServers = getOrgRelevantServers(errataId, orgId, channelId);

        if (orgServers == null || orgServers.isEmpty()) {
            log.debug("No relevant servers found for patch {} in channel {} for org {} ... skipping.",
                    errata.getId(), channelId, orgId);
            return;
        }

        Map<Long, List<Row>> userMap = createUserEmailMap(orgServers);

        log.info("Found {} user(s) to notify about erratum {} in channel {} for org {}.", userMap.keySet().size(),
                errata.getId(), channelId, orgId);

        for (Map.Entry<Long, List<Row>> entry : userMap.entrySet()) {
            Row userInfo = getUserInfo(entry.getKey());
            String email = (String) userInfo.get("email");
            String login = (String) userInfo.get("login");
            List<Row> servers = entry.getValue();
            log.info("Notification for user {}({}) about {} relevant server(s).",
                    login, entry.getKey(), servers.size());
            String emailBody = formatEmail(login, email, errata, servers);
            String rhnHeader = "Autogenerated mail for " + login;
            String subject = Config.get().getString("web.product_name") + " Patch Alert: " +
                    errata.getAdvisory() + " - " +
                    errata.getSynopsis();
            MailHelper.withSmtp().addRhnHeader(rhnHeader).sendEmail(email, subject, emailBody);
        }
    }

    private Map<Long, List<Row>> createUserEmailMap(List<Row> orgServersIn) {
        Map<Long, List<Row>> map = new HashMap<>();
        for (Iterator<Row> i = orgServersIn.iterator(); i.hasNext();) {
            Row row = i.next();
            Long userId = (Long) row.get("user_id");
            if (!map.containsKey(userId)) {
                map.put(userId, new ArrayList<>());
            }
            map.get(userId).add(row);
            i.remove();
        }
        return map;
    }

    private Row getUserInfo(Long userId) {
        SelectMode mode = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_ERRATAMAILER_GET_USERINFO);
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", userId);
        DataResult<Row> dr = mode.execute(params);
        return dr.get(0);
    }

    protected List<Row> getOrgRelevantServers(Long errataId, Long orgId, Long channelId) {
        SelectMode mode = ModeFactory.getMode(TaskConstants.MODE_NAME,
                TaskConstants.TASK_QUERY_ERRATAMAILER_GET_RELEVANT_SERVERS);
        Map<String, Object> params = new HashMap<>();
        params.put("errata_id", errataId);
        params.put("org_id", orgId);
        params.put("channel_id", channelId);
        return mode.execute(params);
    }

    private String formatEmail(String login,
            String email,
            Errata errata,
            List servers) {
        StringBuilder body = new StringBuilder();

        //Build the hostname with protocol. Used to create urls for the email.
        String host = "https://" + ConfigDefaults.get().getHostname();

        //Build the email body
        body.append(getEmailBodySummary(errata, host));
        body.append("\n").append("\n");
        body.append(getEmailBodyAffectedSystems(host, servers));
        body.append("\n").append("\n");
        body.append(getEmailBodyPreferences(host, login, email));

        return body.toString();
    }

    private String getEmailBodySummary(Errata errata, String host) {
        LocalizationService ls = LocalizationService.getInstance();
        Object[] args = new Object[8];

        //Build the errata details url.
        args[0] = host + "/rhn/errata/details/Details.do?eid=" + errata.getId().toString();

        //Add in the errata information.
        args[1] = errata.getAdvisoryType() == null ? "" : errata.getAdvisoryType();
        args[2] = errata.getAdvisory() == null ? "" : errata.getAdvisory();
        args[3] = errata.getSynopsis() == null ? "" : errata.getSynopsis();
        args[4] = errata.getTopic() == null ? "" : errata.getTopic();
        args[5] = errata.getDescription() == null ? "" : errata.getDescription();
        args[6] = errata.getNotes() == null ? "" : errata.getNotes();
        args[7] = errata.getRefersTo() == null ? "" : errata.getRefersTo();
        return ls.getMessage("email.errata.notification.body.summary", args);
    }

    private String getEmailBodyAffectedSystems(String host, List<Row> servers) {
        LocalizationService ls = LocalizationService.getInstance();

        //Render the header of the affected systems section along with helpful text.
        StringBuilder buffy = new StringBuilder();
        buffy.append(ls.getMessage("email.errata.notification.body.affectedheader"));
        buffy.append("\n").append("\n");

        //There is one sentence off on its own that deals with whether there are
        //multiple systems or just one, so this is a separate trans-unit.
        if (servers.size() == 1) {
            buffy.append(ls.getMessage("email.errata.notification.body.onesystem"));
        }
        else {
            buffy.append(ls.getMessage("email.errata.notification.body.numsystems",
                    String.valueOf(servers.size())));
        }
        buffy.append("\n").append("\n");

        //Now show the table of affected systems and the footer text
        Object[] args = new Object[2];

        //Create the data to show in the table
        //TODO: I'm just copying over code that was here before, but it
        //      seems to me that we should be printing another column to
        //      the table according to the String Resource bundle.
        StringWriter writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer, true);
        for (Row row : servers) {
            String release = (String) row.get("release");
            printWriter.print(release);
            for (int i = 0; i < (11 - release.length()); i++) {
                printWriter.print(' ');
            }
            String arch = (String) row.get("arch");
            printWriter.print(arch);
            for (int i = 0; i < (11 - arch.length()); i++) {
                printWriter.print(' ');
            }
            printWriter.println((String) row.get("name"));
        }
        printWriter.flush();
        args[0] = writer.toString();
        //URL for the system list
        args[1] = host + "/rhn/manager/systems/list/all";
        buffy.append(ls.getMessage("email.errata.notification.body.affected", args));
        return buffy.toString();
    }

    private String getEmailBodyPreferences(String host, String login, String email) {
        LocalizationService ls = LocalizationService.getInstance();
        Object[] args = new Object[3];

        //URL for user preferences
        args[0] = host + "/rhn/account/UserPreferences.do";
        //custom email footer
        args[1] = OrgFactory.EMAIL_FOOTER.getValue();

        //custom account info
        args[2] = OrgFactory.EMAIL_ACCOUNT_INFO.getValue();

        //This is so ugly! For some reason we support these 'macros' for
        //account info only. But we made them look like XML tags as if spaces
        //didn't matter. However, spaces do matter. <sigh />
        args[2] = StringUtils.replace(args[2].toString(),
                "<login />",
                login);
        args[2] = StringUtils.replace(args[2].toString(),
                "<email-address />",
                email);

        return ls.getMessage("email.errata.notification.body.preferences", args);
    }
}
