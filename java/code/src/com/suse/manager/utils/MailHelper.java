/**
 * Copyright (c) 2018 SUSE LLC
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

package com.suse.manager.utils;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.messaging.Mail;
import com.redhat.rhn.common.messaging.SmtpMail;
import java.net.UnknownHostException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import java.net.InetAddress;

/**
 * Utilities related to e-mail sending.
 */
public class MailHelper {

    public static final String PRODUCT_PREFIX = Config.get().getString("web.product_name") + " ";

    private static final Logger LOG = Logger.getLogger(MailHelper.class);
    private Mail mailer;

    /**
     * Private constructor, use static methods #withSmtp or #withMailer to create instances
     * @param mailerIn Mailer class we want to use
     */
    private MailHelper(Mail mailerIn) {
        this.mailer = mailerIn;
    }

    /**
     * Send an email to admin users of SUSE Manager
     * @param subject of the mail.
     * @param messageBody of the mail.
     */
    public void sendAdminEmail(String subject, String messageBody) {
        String[] recipients = getAdminRecipientsFromConfig();
        StringBuilder enrichedSubject = new StringBuilder(subject);
        try {
            enrichedSubject.append(" from " + InetAddress.getLocalHost().getHostName());
        }
        catch (UnknownHostException ue) {
            LOG.error("Could not retrieve hostname: " + ue);
        }
        sendEmail(recipients, enrichedSubject.toString(), messageBody);
    }

    /**
     * Send email to a single recipient
     * @param recipient of the message
     * @param subject of the mail
     * @param body of the mail
     */
    public void sendEmail(String recipient, String subject, String body) {
        sendEmail(new String[]{recipient}, subject, body);
    }

    /**
     * Send email to a multiple recipients
     * @param recipients os the message
     * @param subject of the mail
     * @param body of the mail
     */
    public void sendEmail(String[] recipients, String subject, String body) {
        mailer.setRecipients(recipients);
        mailer.setSubject(subject);
        mailer.setBody(body);

        if (LOG.isDebugEnabled()) {
            LOG.debug("Sending mail message:\n" + mailer.toString());
        }
        try {
            mailer.send();
        }
        catch (Exception e) {
            LOG.error("Exception while sending email", e);
        }
    }

    /**
     * Add X-RHN-Info type header to message
     * @param value for the header
     * @return class instance (builder pattern)
     */
    public MailHelper addRhnHeader(String value) {
        addHeader("X-RHN-Info", value);
        return this;
    }

    /**
     * Add custom header to message
     * @param key for the header element
     * @param value of the header element
     * @return class instance (builder pattern)
     */
    public MailHelper addHeader(String key, String value) {
        mailer.setHeader(key, value);
        return this;
    }

    /**
     * Create a MailHelper instance with SmtpMail
     * @return an instance of MailHelper class configured to use SmtpMail
     */
    public static MailHelper withSmtp() {
        return new MailHelper(new SmtpMail());
    }

    /**
     * Create a MailHelper instance with custom mailer class
     * @param mailer Custom mailer
     * @return an instance of MailHelper class configured to the specified mailer
     */
    public static MailHelper withMailer(Mail mailer) {
        return new MailHelper(mailer);
    }

    /**
     * Compose email body based on a template and a list of arguments
     * @param template name of template
     * @param args args to be in template
     * @return body text
     */
    public static String composeEmailBody(String template, Object... args) {
        LocalizationService ls = LocalizationService.getInstance();
        return ls.getMessage(template, args);
    }

    /**
     * Get list of admin recipients from Config 'web.traceback_mail' property
     * @return A list of emails from admins as defined in the configuration
     */
    public static String[] getAdminRecipientsFromConfig() {
        Config c = Config.get();
        return StringUtils.isBlank(c.getString("web.traceback_mail")) ?
                new String[]{"root@localhost"} :
                c.getStringArray("web.traceback_mail");
    }
}
