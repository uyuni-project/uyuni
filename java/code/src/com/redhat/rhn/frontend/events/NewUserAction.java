/*
 * Copyright (c) 2009--2010 Red Hat, Inc.
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

import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.messaging.EventMessage;
import com.redhat.rhn.common.messaging.MessageAction;
import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.utils.MailHelper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implement Action for TraceBackEvents
 */
public class NewUserAction extends BaseMailAction implements MessageAction {

    /**
     * Logger for this class
     */
    private static Logger logger = LogManager.getLogger(NewUserAction.class);

    /**
     * Execute the Event.  This Action actually sends 2 mail messages
     * so we need to override the default execute() method.
     * @param msg EventMessage to executed.
     */
    @Override
    public void execute(EventMessage msg) {
        if (logger.isDebugEnabled()) {
            logger.debug("execute(EventMessage msg={}) - start", msg);
        }

        super.execute(msg);
        NewUserEvent evt = (NewUserEvent) msg;

        Map<String, String> map = new HashMap<>();
        map.put("login", evt.getUser().getLogin());
        map.put("email-address", evt.getUser().getEmail());

        String accountInfo = StringUtil.replaceTags(OrgFactory
                .EMAIL_ACCOUNT_INFO.getValue(), map);

        //gather information for the email to accountOwner
        Object[] subjectArgs = new Object[4];
        subjectArgs[0] = evt.getUser().getLogin();
        subjectArgs[1] = evt.getUser().getLastName();
        subjectArgs[2] = evt.getUser().getFirstNames();
        subjectArgs[3] = evt.getUser().getEmail();

        Object[] bodyArgs = new Object[3];
        bodyArgs[0] = accountInfo;
        bodyArgs[1] = evt.getUrl() + "rhn/users/ActiveList.do";
        bodyArgs[2] = OrgFactory.EMAIL_FOOTER.getValue();

        //Get the admin details(email) from the event message
        //and set in recipients to send the mail
        String subject = LocalizationService.getInstance().
                getMessage("email.newuser.subject", evt.getUserLocale(), subjectArgs);
        String body = LocalizationService.getInstance().
                getMessage("email.newuser.body", evt.getUserLocale(), bodyArgs);
        MailHelper.withMailer(getMail()).sendEmail(getEmails(evt), subject, body);

        if (logger.isDebugEnabled()) {
            logger.debug("execute(EventMessage) - end");
        }
    }


    private String[] getEmails(NewUserEvent evt) {
        List adminList = evt.getAdmins();
        String[] adminEmails = new String[adminList.size()];
        int index = 0;
        for (Object oIn : adminList) {
            adminEmails[index] = ((User) oIn).getEmail();
            index++;
        }
        return adminEmails;
    }


    @Override
    protected String getSubject(BaseEvent evtIn) {
        if (logger.isDebugEnabled()) {
            logger.debug("getSubject(User userIn={}) - start", evtIn.getUser());
        }

        String returnString = LocalizationService.getInstance().getMessage(
                "email.newaccount.subject", evtIn.getUserLocale());
        if (logger.isDebugEnabled()) {
            logger.debug("getSubject(User) - end - return value={}", returnString);
        }
        return returnString;
    }

    @Override
    protected String[] getRecipients(User userIn) {
        if (logger.isDebugEnabled()) {
            logger.debug("getRecipients(User userIn={}) - start", userIn);
        }

        String[] retval = new String[1];
        retval[0] = userIn.getEmail();

        if (logger.isDebugEnabled()) {
            logger.debug("getRecipients(User) - end - return value={}", (Object)retval);
        }
        return retval;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean needsTransactionHandling() {
        return false;
    }
}
