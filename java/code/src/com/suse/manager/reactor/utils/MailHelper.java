/**
 * Copyright (c) 2016 SUSE LLC
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

package com.suse.manager.reactor.utils;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.messaging.SmtpMail;

import com.suse.manager.reactor.SaltReactor;

import org.apache.log4j.Logger;

import java.net.InetAddress;

/**
 * Utilities related to e-mail sending.
 */
public class MailHelper {

    // Logger for this class
    private static final Logger LOG = Logger.getLogger(MailHelper.class);

    private MailHelper() { }

    /**
     * Sends and email to admin users of SUMa.
     * @param subject of the mail.
     * @param messageBody of the mail.
     */
    public static void sendAdminEmail(String subject, String messageBody) {
        Config c = Config.get();
        String[] recipients = null;
        if (recipients == null) {
            if (c.getString("web.traceback_mail").equals("")) {
                recipients = new String[1];
                recipients[0] = "root@localhost";
            }
            else {
                recipients = c.getStringArray("web.traceback_mail");
            }
        }
        SmtpMail mail = new SmtpMail();
        mail.setRecipients(recipients);
        StringBuilder subjectEnriched = new StringBuilder(subject);
        try {
            subjectEnriched.append(" from " + InetAddress.getLocalHost().getHostName());
        }
        catch (Throwable t) {
            // nothing
        }
        mail.setSubject(subjectEnriched.toString());
        mail.setBody(messageBody);

        LOG.info("Sending mail message:\n" + mail.toString());

        mail.send();
    }
}
