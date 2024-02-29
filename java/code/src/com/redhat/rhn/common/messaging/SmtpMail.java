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

package com.redhat.rhn.common.messaging;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import javax.mail.Address;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.Message.RecipientType;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * A simple wrapper around javamail to allow us to send e-mail quickly.
 *
 */
public class SmtpMail implements Mail {

    private String smtpHost;
    private MimeMessage message;
    private static Logger log = LogManager.getLogger(SmtpMail.class);

    private static String[] disallowedDomains = Config.get().getStringArray("web.disallowed_mail_domains");
    private static String[] restrictedDomains = Config.get().getStringArray("web.restrict_mail_domains");

    /**
     * Create a mailer.
     */
    public SmtpMail() {
        log.debug("Constructed new SmtpMail.");

        Config c = Config.get();
        smtpHost = c.getString(ConfigDefaults.WEB_SMTP_SERVER, "localhost");
        int smtpPort = c.getInt(ConfigDefaults.WEB_SMTP_PORT, 25);
        boolean smtpAuth = c.getBoolean(ConfigDefaults.WEB_SMTP_AUTH);
        boolean smtpSSL = c.getBoolean(ConfigDefaults.WEB_SMTP_SSL);
        boolean smtpStartTLS = c.getBoolean(ConfigDefaults.WEB_SMTP_STARTTLS);
        String smtpUser = c.getString(ConfigDefaults.WEB_SMTP_USER);
        String smtpPass = c.getString(ConfigDefaults.WEB_SMTP_PASS);
        String from = c.getString(ConfigDefaults.WEB_DEFAULT_MAIL_FROM, "root@localhost");
        int smtpTimeout = c.getInt(ConfigDefaults.WEB_SMTP_TIMEOUT, 5000);
        int smtpConnectionTimeout = c.getInt(ConfigDefaults.WEB_SMTP_CONNECTION_TIMEOUT, 5000);
        int smtpWriteTimeout = c.getInt(ConfigDefaults.WEB_SMTP_WRITE_TIMEOUT, 5000);

        // Get system properties
        Properties props = System.getProperties();

        // Setup mail server
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.auth", smtpAuth);
        props.put("mail.smtp.ssl.enable", smtpSSL);
        props.put("mail.smtp.starttls.enable", smtpStartTLS);
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");
        props.put("mail.smtp.connectiontimeout", smtpConnectionTimeout);
        props.put("mail.smtp.timeout", smtpTimeout);
        props.put("mail.smtp.writetimeout", smtpWriteTimeout);

        // Setup Authentication
        Authenticator auth = null;
        if (smtpAuth) {
            auth = new Authenticator() {
                @Override
                protected PasswordAuthentication getPasswordAuthentication() {
                    return new PasswordAuthentication(smtpUser, smtpPass);
                }
            };
        }

        // Get session
        Session session = Session.getDefaultInstance(props, auth);
        message = new MimeMessage(session);
        setFrom(from);
    }

    /** {@inheritDoc} */
    @Override
    public void setHeader(String name, String value) {
        try {
            message.setHeader(name, value);
        }
        catch (MessagingException me) {
            String msg = "MessagingException while trying to send email: " + me;
            log.warn(msg);
            throw new JavaMailException(msg, me);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setFrom(String from) {
        try {
            message.setFrom(new InternetAddress(from));
        }
        catch (AddressException me) {
            String msg = "Malformed address in traceback configuration: " +
                                from;
            log.warn(msg);
            throw new JavaMailException(msg, me);
        }
        catch (MessagingException me) {
            String msg = "MessagingException while trying to send email: " + me;
            log.warn(msg);
            throw new JavaMailException(msg, me);
        }
    }


    /** {@inheritDoc} */
    @Override
    public void send() {

        try {
            Address[] addrs = message.getRecipients(RecipientType.TO);
            if (addrs == null || addrs.length == 0) {
                log.warn("Aborting mail message {}: No recipients", message.getSubject());
                return;
            }
            Transport.send(message);
        }
        catch (MessagingException me) {
            String msg = "MessagingException while trying to send email: " + me;
            log.warn(msg);
            throw new JavaMailException(msg, me);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setRecipient(String recipIn) {
        setRecipients(new String[]{recipIn});
    }

    /** {@inheritDoc} */
    @Override
    public void setRecipients(String[] emailAddrs) {
        setRecipients(Message.RecipientType.TO, emailAddrs);
    }

    /** {@inheritDoc} */
    @Override
    public void setCCRecipients(String[] emailAddrs) {
        setRecipients(Message.RecipientType.CC, emailAddrs);
    }

    /** {@inheritDoc} */
    @Override
    public void setBCCRecipients(String[] emailAddrs) {
        setRecipients(Message.RecipientType.BCC, emailAddrs);
    }

    /**
     * Private helper method to do the heavy lifting of setting the recipients field for
     * a message
     * @param type The javax.mail.Message.RecipientType (To, CC, or BCC) for the recipients
     * @param recipIn A string array of email addresses
     */
    private void setRecipients(RecipientType type, String[] recipIn) {
        log.debug("setRecipients called.");
        Address[] recAddr = null;
        try {
            List<InternetAddress> tmp = new LinkedList<>();
            for (String sIn : recipIn) {
                InternetAddress addr = new InternetAddress(sIn);
                log.debug("checking: {}", addr.getAddress());
                if (verifyAddress(addr)) {
                    log.debug("Address verified.  Adding: {}", addr.getAddress());
                    tmp.add(addr);
                }
            }
            recAddr = new Address[tmp.size()];
            tmp.toArray(recAddr);
            message.setRecipients(type, recAddr);
        }
        catch (MessagingException me) {
            String msg = "MessagingException while trying to send email: " + me;
            log.warn(msg);
            throw new JavaMailException(msg, me);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setSubject(String subIn) {
        try {
            message.setSubject(subIn);
        }
        catch (MessagingException me) {
            String msg = "MessagingException while trying to send email: " + me;
            log.warn(msg);
            throw new JavaMailException(msg, me);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void setBody(String textIn) {
        try {
            message.setText(textIn);
        }
        catch (MessagingException me) {
            String msg = "MessagingException while trying to send email: " + me;
            log.warn(msg);
            throw new JavaMailException(msg, me);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        try {
            buf.append("Using SMTP host: ").append(this.smtpHost);
            buf.append("\nFrom: ");
            appendAddresses(buf, this.message.getFrom());
            buf.append("\nTo: ");
            appendAddresses(buf, this.message.getAllRecipients());
            buf.append("\nSubject: ");
            buf.append(this.message.getSubject()).append("\n");
            appendHeaders(buf, this.message.getAllHeaderLines());
            buf.append(this.message.getContent());
        }
        catch (IOException | MessagingException e) {
            log.error(e.getMessage(), e);
        }
        return buf.toString();
    }

    private void appendHeaders(StringBuilder buf, Enumeration<String> headers) {
        while (headers.hasMoreElements()) {
            buf.append(headers.nextElement());
            buf.append("\n");
        }
        buf.append("\n");
    }

    private void appendAddresses(StringBuilder buf, Address[] addrs) {
        if (addrs != null) {
            for (int x = 0; x < addrs.length; x++) {
                buf.append(addrs[x].toString());
                if (addrs.length > 1 && x < (addrs.length - 1)) {
                    buf.append(",");
                }
            }
        }
    }

    private boolean verifyAddress(InternetAddress addr) {
        log.debug("verifyAddress called ...");
        boolean retval = true;
        String domain = addr.getAddress();
        int domainStart = domain.indexOf('@');
        if (domainStart > -1 && domainStart + 1 < domain.length()) {
            domain = domain.substring(domainStart + 1);

        }
        if (log.isDebugEnabled()) {
            log.debug("Restricted domains: {}", StringUtils.join(restrictedDomains, " | "));
            log.debug("disallowedDomains domains: {}", StringUtils.join(disallowedDomains, " | "));
        }
        if (restrictedDomains != null && restrictedDomains.length > 0 &&
                ArrayUtils.lastIndexOf(restrictedDomains, domain) == -1) {
            log.warn("Address {} not in restricted domains list", addr.getAddress());
            retval = false;
        }

        if (retval &&  disallowedDomains != null && disallowedDomains.length > 0 &&
                ArrayUtils.lastIndexOf(disallowedDomains, domain) > -1) {
            log.warn("Address {} in disallowed domains list", addr.getAddress());
            retval = false;
        }
        log.debug("verifyAddress returning: {}", retval);
        return retval;
    }
}


