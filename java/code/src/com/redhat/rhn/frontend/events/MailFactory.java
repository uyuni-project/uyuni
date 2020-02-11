/**
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
import com.redhat.rhn.common.messaging.Mail;
import com.redhat.rhn.common.messaging.SmtpMail;

/**
 * MailFactory - constructs Mail objects
 */
public final class MailFactory {

    private static Mail mail;

    /**
     * For test use only
     */
    public static void setMail(Mail mailIn) {
        mail = mailIn;
    }

    /**
     * Get the mailer associated with this class
     * @return the mailer associated with this class
     */
    public static Mail construct() {
        if (mail != null) {
            return mail;
        }
        String clazz = Config.get().getString(
                "web.mailer_class");
        if (clazz == null) {
            return new SmtpMail();
        }
        try {
            Class cobj = Class.forName(clazz);
            return (Mail) cobj.newInstance();
        }
        catch (Exception e) {
            return new SmtpMail();
        }
    }
}
