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

package com.redhat.rhn.testing;

import com.redhat.rhn.common.messaging.Mail;

/**
 * This mailer is used in test configuration ("buildconf/rhn.conf.postgresql-example", copied to "buildconf/rhn.conf")
 * in the entry:
 * web.mailer_class = com.redhat.rhn.testing.NoOpMail
 * in order to avoid flooding "galaxy-noise@suse.de" mailbox with error messages when unit tests are run
 * The method MailHelper.createMailFromConfig() looks for the configuration entry "web.mailer_class"
 * to create a child object of class Mail.
 * In production, the entry "web.mailer_class" is not found in rhn.conf and so the mailer is the default one (SmtpMail)
 * When testing, the entry "web.mailer_class" is found and points to create an instance of this class
 */
public class NoOpMail implements Mail  {
    @Override
    public void send() {
        //do nothing
    }

    @Override
    public void setFrom(String from) {
        //do nothing
    }

    @Override
    public void setHeader(String name, String value) {
        //do nothing
    }

    @Override
    public void setRecipients(String[] recipIn) {
        //do nothing
    }

    @Override
    public void setCCRecipients(String[] emailAddrs) {
        //do nothing
    }

    @Override
    public void setBCCRecipients(String[] emailAddrs) {
        //do nothing
    }

    @Override
    public void setRecipient(String recipIn) {
        //do nothing
    }

    @Override
    public void setSubject(String subIn) {
        //do nothing
    }

    @Override
    public void setBody(String textIn) {
        //do nothing
    }
}
