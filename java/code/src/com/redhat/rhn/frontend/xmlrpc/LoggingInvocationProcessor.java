/**
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
package com.redhat.rhn.frontend.xmlrpc;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.translation.Translator;
import com.redhat.rhn.domain.user.User;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;

import java.util.List;

import redstone.xmlrpc.XmlRpcInvocation;
import redstone.xmlrpc.XmlRpcInvocationInterceptor;

/**
 * LoggingInvocationProcessor extends the marquee-xmlrpc library to allow
 * us to log method calls.
 */
public class LoggingInvocationProcessor implements XmlRpcInvocationInterceptor {
    private static Logger log = Logger.getLogger(LoggingInvocationProcessor.class);
    private static ThreadLocal<User> caller = new ThreadLocal<User>();

    private static ThreadLocal timer = new ThreadLocal() {
        protected synchronized Object initialValue() {
            return new StopWatch();
        }
    };

    /**
     * {@inheritDoc}
     */
    public boolean before(XmlRpcInvocation invocation) {

        // we start the timing and return true so processing
        // continues.
        // NOTE: as of commons-lang 2.1 we must reset before
        // starting.
        getStopWatch().reset();
        getStopWatch().start();

        List arguments = invocation.getArguments();
        // HACK ALERT!  We need the caller, would be better in
        // the postProcess, but that works for ALL methods except
        // logout.  So we do it here.
        if ((arguments != null) && (arguments.size() > 0)) {
            if (arguments.get(0) instanceof User) {
                setCaller((User)arguments.get(0));
            }
            else {
                String arg = (String) Translator.convert(
                        arguments.get(0), String.class);
                if (potentialSessionKey(arg)) {
                    setCaller(getLoggedInUser(arg));
                }
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    public Object after(XmlRpcInvocation invocation, Object returnValue) {
        StringBuffer buf = new StringBuffer();
        try {
            // Create the call in a separate buffer for reuse
            StringBuffer call = new StringBuffer();
            call.append(invocation.getHandlerName());
            call.append(".");
            call.append(invocation.getMethodName());
            call.append("(");
            call.append(processArguments(invocation.getHandlerName(),
                    invocation.getMethodName(), invocation.getArguments()));
            call.append(")");

            buf.append("REQUESTED FROM: ");
            buf.append(RhnXmlRpcServer.getCallerIp());
            buf.append(" CALL: ");
            buf.append(call);
            buf.append(" CALLER: (");
            buf.append(getCallerLogin());
            buf.append(") TIME: ");

            getStopWatch().stop();

            buf.append(getStopWatch().getTime() / 1000.00);
            buf.append(" seconds");

            log.info(buf.toString());
        }
        catch (RuntimeException e) {
            log.error("postProcess error CALL: " + invocation.getHandlerName() +
                    " " + invocation.getMethodName(), e);
        }

        return returnValue;
    }

    /**
     * {@inheritDoc}
     */
    public void onException(XmlRpcInvocation invocation, Throwable exception) {
        StringBuffer buf = new StringBuffer();
        try {
            buf.append("REQUESTED FROM: ");
            buf.append(RhnXmlRpcServer.getCallerIp());
            buf.append(" CALL: ");
            buf.append(invocation.getHandlerName());
            buf.append(".");
            buf.append(invocation.getMethodName());
            buf.append("(");
            buf.append(processArguments(invocation.getHandlerName(),
                    invocation.getMethodName(), invocation.getArguments()));
            buf.append(") CALLER: (");
            buf.append(getCallerLogin());
            buf.append(") TIME: ");

            getStopWatch().stop();

            buf.append(getStopWatch().getTime() / 1000.00);
            buf.append(" seconds");

            log.error(buf.toString(), exception);
        }
        catch (RuntimeException e) {
            log.error("postProcess error CALL: " + invocation.getHandlerName() +
                    " " + invocation.getMethodName(), e);
        }
    }

    private StringBuffer processArguments(String handler, String method,
                                  List arguments) {
        StringBuffer ret = new StringBuffer();

        // bug 199130: don't log password :)
        if ("auth.login".equals(handler + "." + method)) {
            if (arguments != null && arguments.size() > 0) {
                String arg = (String) Translator.convert(
                        arguments.get(0), String.class);

                ret.append(arg);
                ret.append(", ********");
            }
        }
        else {
            if (arguments != null) {
                int size = arguments.size();
                for (int i = 0; i < size; i++) {
                    String arg = null;
                    if (arguments.get(i) instanceof User) {
                        arg = ((User)arguments.get(i)).getLogin();
                    }
                    else {
                        arg = (String) Translator.convert(
                                arguments.get(i), String.class);
                    }

                    ret.append(arg);

                    if ((i + 1) < size) {
                        ret.append(", ");
                    }
                }
            }
        }
        return ret;
    }

    /**
     * If the key is a sessionKey, we'll return the username, otherwise we'll
     * return (unknown).
     * @param key potential sessionKey.
     * @return  username, (Invalid Session ID), or (unknown);
     */
    private User getLoggedInUser(String key) {
        try {
            User user = BaseHandler.getLoggedInUser(key);
            if (user != null) {
                return user;
            }
        }
        catch (LookupException le) {
            // do nothing
        }
        catch (Exception e) {
            log.error("problem with getting logged in user for logging", e);
        }

        return null;
    }

    /**
     * Returns true if the given key contains an 'x' which is the separator
     * character in the session key.
     * @param key Potential key candidate.
     * @return true if the given key contains an 'x' which is the separator
     * character in the session key.
     */
    private boolean potentialSessionKey(String key) {
        if (key == null || key.equals("")) {
            return false;
        }

        // Get the id
        String[] keyParts = StringUtils.split(key, 'x');

        // make sure the id is numeric and can be made into a Long
        return StringUtils.isNumeric(keyParts[0]);
    }

    private static StopWatch getStopWatch() {
        return (StopWatch) timer.get();
    }

    private String getCallerLogin() {
        String ret = "none";
        if (getCaller() != null) {
            ret = getCaller().getLogin();
        }
        return ret;
    }

    private static User getCaller() {
        return (User) caller.get();
    }

    private static void setCaller(User c) {
        caller.set(c);
    }
}
