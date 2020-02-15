package com.suse.manager.tasks.actors;

import static akka.actor.typed.javadsl.Behaviors.receive;
import static akka.actor.typed.javadsl.Behaviors.same;
import static akka.actor.typed.javadsl.Behaviors.setup;

import com.redhat.rhn.common.conf.Config;
import com.redhat.rhn.common.localization.LocalizationService;
import com.redhat.rhn.common.messaging.MessageExecuteException;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.events.MailFactory;

import com.suse.manager.tasks.Actor;
import com.suse.manager.tasks.Command;
import com.suse.manager.utils.MailHelper;
import org.apache.log4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.InetAddress;
import java.util.Date;
import java.util.Enumeration;
import javax.servlet.http.HttpServletRequest;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;

public class TraceBackActor implements Actor {

    private final static Logger LOG = Logger.getLogger(TraceBackActor.class);

    public static class Message implements Command {
        private final String text;

        public Message(String text) { this.text = text; }
    }

    public Behavior<Command> create(ActorRef<Command> guardian) {
        return setup(context -> receive(Command.class)
                .onMessage(Message.class, message -> onMessage(message))
                .build());
    }

    private Behavior<Command> onMessage(Message message) {
        MailHelper.withMailer(MailFactory.construct()).sendEmail(getRecipients(), getSubject(), message.text);
        return same();
    }

    public String getSubject() {
        // setup subject
        StringBuilder subject = new StringBuilder();
        subject.append(LocalizationService.getInstance().
                getMessage("web traceback subject", LocalizationService.getUserLocale()));
        // Not sure if getting the local hostname is the correct thing to do
        // here.  But the traceback emails that I've received seem to do this
        try {
            subject.append(InetAddress.getLocalHost().getHostName());
        }
        catch (java.net.UnknownHostException uhe) {
            String message = "TraceBackAction can't find localhost!";
            LOG.warn(message);
            throw new MessageExecuteException(message);
        }
        subject.append(" (");
        subject.append(LocalizationService.getInstance().formatDate(new Date(),
                LocalizationService.getUserLocale()));
        subject.append(")");
        return subject.toString();
    }

    public String[] getRecipients() {
        Config c = Config.get();
        String[] retval = null;
        if (c.getString("web.traceback_mail").equals("")) {

            retval = new String[1];
            retval[0] = "root@localhost";
        }
        else {
            retval = c.getStringArray("web.traceback_mail");
        }
        return retval;
    }

    public static String compose(HttpServletRequest request, User user, Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter out = new PrintWriter(sw);
        LocalizationService ls = LocalizationService.getInstance();

        if (request != null) {
            out.println(ls.getMessage("traceback message header"));
            out.print(request.getMethod());
            out.println(" " + request.getRequestURI());
            out.println();
            out.print(ls.getMessage("date", LocalizationService.getUserLocale()));
            out.print(":");
            out.println(ls.getBasicDate());
            out.print(ls.getMessage("headers", LocalizationService.getUserLocale()));
            out.println(":");
            Enumeration e = request.getHeaderNames();
            while (e.hasMoreElements()) {
                String headerName = (String) e.nextElement();
                out.print("  ");
                out.print(headerName);
                out.print(": ");
                out.println(request.getHeader(headerName));
            }
            out.println();
            out.print(ls.getMessage("request", LocalizationService.getUserLocale()));
            out.println(":");
            out.println(request.toString());

            if (request.getMethod() != null &&
                    request.getMethod().equals("POST")) {
                out.print(ls.getMessage("form variables", LocalizationService.getUserLocale()));
                out.println(":");
                Enumeration ne = request.getParameterNames();
                while (ne.hasMoreElements()) {
                    String paramName = (String) ne.nextElement();
                    out.print("  ");
                    out.print(paramName);
                    out.print(": ");
                    if (paramName.equals("password")) {
                        out.println("########");
                    }
                    else {
                        out.println(request.getParameter(paramName));
                    }
                }
                out.println();
            }
        }
        else {
            out.print(ls.getMessage("date", LocalizationService.getUserLocale()));
            out.print(":");
            out.println(ls.getBasicDate());
            out.println();
            out.print(ls.getMessage("request", LocalizationService.getUserLocale()));
            out.println(":");
            out.println("No request information");
            out.println();
        }

        out.println();

        out.print(ls.getMessage("user info"));
        out.println(":");
        if (user != null) {
            out.println(user.toString());
        }
        else {
            out.println(ls.getMessage("no user loggedin", LocalizationService.getUserLocale()));
        }
        out.println();
        out.print(ls.getMessage("exception", LocalizationService.getUserLocale()));
        out.println(":");
        if (throwable != null) {
            throwable.printStackTrace(out);
        }
        else {
            out.println("no throwable");
        }
        out.close();
        return sw.toString();
    }
}
