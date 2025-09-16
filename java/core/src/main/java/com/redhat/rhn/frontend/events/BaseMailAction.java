/*
 * Copyright (c) 2009--2012 Red Hat, Inc.
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
package com.redhat.rhn.frontend.events;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.Mail;
import com.redhat.rhn.common.messaging.SmtpMail;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.utils.MailHelper;

import org.apache.logging.log4j.Logger;

/**
 * BaseMailAction - basic abstract class to encapsulate some common Action logic.
 */
public abstract class BaseMailAction {

    protected abstract String getSubject(BaseEvent evt);

    protected abstract String[] getRecipients(User user);

    /**
     * Execute the TraceBack
     * @param msg EventMessage to executed.
     */
    public void execute(EventMessage msg) {
        BaseEvent aevt = (BaseEvent) msg;
        try {
            MailHelper.withMailer(getMail()).sendEmail(getRecipients(aevt.getUser()), getSubject(aevt), msg.toText());
        }
        catch (Exception e) {
            getLogger().error("Unable to configure a mailer: {}", e.getMessage(), e);
        }
    }

    /**
     * Get the mailer associated with this class
     * @return the mailer associated with this class
     */
    protected Mail getMail() {
        String clazz = Config.get().getString("web.mailer_class");
        if (clazz == null) {
            return new SmtpMail();
        }
        try {
            Class<? extends Mail> cobj = Class.forName(clazz).asSubclass(Mail.class);
            return cobj.getDeclaredConstructor().newInstance();
        }
        catch (Exception | LinkageError e) {
            getLogger().error("An exception was thrown while initializing custom mailer class", e);
            return new SmtpMail();
        }
    }

    /**
     * Get the Logger for the derived class so log messages show up on the
     * correct class
     * @return Logger for this class.
     */
    protected abstract Logger getLogger();

}
