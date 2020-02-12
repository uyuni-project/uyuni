package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.conf.ConfigDefaults;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.util.StringUtil;
import com.redhat.rhn.domain.org.OrgFactory;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.domain.user.UserFactory;
import com.redhat.rhn.frontend.events.MailFactory;

import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import com.suse.manager.utils.MailHelper;
import org.apache.log4j.Logger;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import akka.actor.typed.Behavior;

public class NewUserActor implements Actor {

    private final static Logger LOG = Logger.getLogger(NewUserActor.class);

    private static final int NO_CREATOR_INDEX = 0;
    private static final int WITH_CREATOR_INDEX = 2;

    public static class Message implements Command {
        private final Long accountCreatorId;
        private final String link;
        private final String domain;
        private final List<String> adminEmails;
        private final Long userId;

        public Message(Long accountCreatorId, String link, String domain, List<String> adminEmails, Long userId) {
            this.accountCreatorId = accountCreatorId;
            this.link = link;
            this.domain = domain;
            this.adminEmails = adminEmails;
            this.userId = userId;
        }
    }

    public Behavior<Command> create() {
        return setup(context -> receive(Command.class)
                .onMessage(Message.class, message -> onMessage(message))
                .build());
    }

    private Behavior<Command> onMessage(Message message) {
        execute(message);
        return same();
    }

    public void execute(Message msg) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("execute(EventMessage msg=" + msg + ") - start");
        }

        var user = UserFactory.lookupById(msg.userId);
        var accountCreator = UserFactory.lookupById(msg.accountCreatorId);
        var url = getUrl(msg.domain);

        MailHelper.withMailer(MailFactory.construct()).sendEmail(getRecipients(user), getSubject(), messageToString(user, accountCreator, msg.link, url));

        Map map = new HashMap();
        map.put("login", user.getLogin());
        map.put("email-address", user.getEmail());

        //set url and account info for email to accountOwner
        //url.append();
        String accountInfo = StringUtil.replaceTags(OrgFactory
                .EMAIL_ACCOUNT_INFO.getValue(), map);

        //gather information for the email to accountOwner
        Object[] subjectArgs = new Object[4];
        subjectArgs[0] = user.getLogin();
        subjectArgs[1] = user.getLastName();
        subjectArgs[2] = user.getFirstNames();
        subjectArgs[3] = user.getEmail();

        Object[] bodyArgs = new Object[3];
        bodyArgs[0] = accountInfo;
        bodyArgs[1] = url + "rhn/users/ActiveList.do";
        bodyArgs[2] = OrgFactory.EMAIL_FOOTER.getValue();

        //Get the admin details(email) from the event message
        //and set in recipients to send the mail
        String subject = LocalizationService.getInstance().
                getMessage("email.newuser.subject", LocalizationService.getUserLocale(), subjectArgs);
        String body = LocalizationService.getInstance().
                getMessage("email.newuser.body", LocalizationService.getUserLocale(), bodyArgs);
        MailHelper.withMailer(MailFactory.construct()).sendEmail((String[])msg.adminEmails.toArray(), subject, body);

        if (LOG.isDebugEnabled()) {
            LOG.debug("execute(EventMessage) - end");
        }
    }

    public String[] getRecipients(User userIn) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("getRecipients(User userIn=" + userIn + ") - start");
        }

        String[] retval = new String[1];
        retval[0] = userIn.getEmail();

        if (LOG.isDebugEnabled()) {
            LOG.debug("getRecipients(User) - end - return value=" + retval);
        }
        return retval;
    }

    public String getSubject() {
        String returnString = LocalizationService.getInstance().getMessage(
                "email.newaccount.subject", LocalizationService.getUserLocale());
        if (LOG.isDebugEnabled()) {
            LOG.debug("getSubject(User) - end - return value=" +
                    returnString);
        }
        return returnString;
    }

    /**
     * This mail event includes a link back to the server.  This method
     * generates the String version of this URL.
     * @return String URL.
     */
    public String getUrl(String domain) {
        //create url for new user
        Config c = Config.get();
        StringBuilder url = new StringBuilder();
        if (ConfigDefaults.get().isSSLAvailable()) {
            url.append("https://");
        }
        else {
            url.append("http://");
        }
        if (c.getString("java.base_domain") != null) {
            url.append(c.getString("java.base_domain"));
        }
        else {
            url.append(domain);
        }
        if (c.getString("java.base_port") != null &&
                !ConfigDefaults.get().isSSLAvailable()) {
            url.append(":");
            url.append(c.getString("java.base_port"));
        }
        url.append("/");
        return url.toString();

    }

    /**
     * format this message as a string
     *   TODO mmccune - fill out the email properly with the entire
     *                  request values
     * @return Text of email.
     */
    public String messageToString(User user, User accountCreator, String link, String url) {
        LocalizationService ls = LocalizationService.getInstance();
        //gather information for the email to newUser

        Object[] bodyArgs = new Object[8];
        populateBodyArgs(bodyArgs, accountCreator, user, link, url);
        String retval;
        /*
         * If the user is using pam for authentication, then we don't need to confuse the
         * poor user further by mentioning a new link. Just don't mention it.
         */
        if (user.getUsePamAuthentication()) {
            retval = ls.getMessage("email.newaccount.pam.body",
                    LocalizationService.getUserLocale(), bodyArgs);
        }
        else {
            if (accountCreator != null) {
                retval = ls.getMessage("email.newaccountbycreator.body",
                        LocalizationService.getUserLocale(), bodyArgs);
            }
            else {
                retval = ls.getMessage("email.newaccount.body",
                        LocalizationService.getUserLocale(), bodyArgs);
            }
        }
        return retval;
    }

    /**
     * Populates the arguments that need to go the body of the message
     * that is sent to the new account.
     * @param bodyArgs
     */
    private void populateBodyArgs(Object[] bodyArgs, User accountCreator, User user, String link, String url) {
        if (accountCreator != null) {
            bodyArgs[0] = accountCreator.getFirstNames();
            bodyArgs[1] = accountCreator.getLastName();
            fillUserInfo(bodyArgs, WITH_CREATOR_INDEX, user, link, url);
        }
        else {
            fillUserInfo(bodyArgs, NO_CREATOR_INDEX, user, link, url);
        }
    }

    private void fillUserInfo(Object[] bodyArgs, int index, User user, String link, String url) {
        bodyArgs[index] = user.getLogin();
        bodyArgs[index + 1] = link;
        bodyArgs[index + 2] = user.getEmail();
        bodyArgs[index + 3] = url;
        bodyArgs[index + 4] = OrgFactory.EMAIL_FOOTER.getValue();
    }
}
