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
package com.redhat.rhn.frontend.xmlrpc;

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.translation.Translator;
import com.redhat.rhn.domain.user.User;

import com.suse.manager.api.LoggingInvocationProcessor;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

import redstone.xmlrpc.XmlRpcInvocation;
import redstone.xmlrpc.XmlRpcInvocationInterceptor;


/**
 * LoggingInvocationProcessor extends the marquee-xmlrpc library to allow
 * us to log method calls.
 */
public class XmlRpcLoggingInvocationProcessor extends LoggingInvocationProcessor
        implements XmlRpcInvocationInterceptor {
    private static Logger log = LogManager.getLogger(XmlRpcLoggingInvocationProcessor.class);

    private static ThreadLocal<User> caller = new ThreadLocal<>();

    /**
     * {@inheritDoc}
     */
    @Override
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
        if ((arguments != null) && (!arguments.isEmpty())) {
            if (arguments.get(0) instanceof User) {
                setCaller((User)arguments.get(0));
            }
            else {
                String arg = (String) Translator.convert(
                        arguments.get(0), String.class);
                if (potentialSessionKey(arg)) {
                    setCaller(getLoggedInUser(arg));
                }
                else {
                    caller.remove();
                }
            }
        }

        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object after(XmlRpcInvocation invocation, Object returnValue) {
        StringBuilder arguments = processArguments(
            invocation.getHandlerName(),
            invocation.getMethodName(),
            invocation.getArguments()
        );
        afterProcess(
            invocation.getHandlerName(),
            invocation.getMethodName(),
            arguments,
            RhnXmlRpcServer.getCallerIp()
        );

        return returnValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onException(XmlRpcInvocation invocation, Throwable exception) {
        StringBuilder buf = new StringBuilder();
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

            buf.append(System.lineSeparator());
            buf.append(exception);

            log.info(buf);
        }
        catch (RuntimeException e) {
            log.error("postProcess error CALL: {} {}", invocation.getHandlerName(), invocation.getMethodName(), e);
        }
    }

    private StringBuilder processArguments(String handler, String method,
                                  List arguments) {
        StringBuilder ret = new StringBuilder();
        if (arguments != null) {
            int size = arguments.size();
            for (int i = 0; i < size; i++) {
                String arg;
                if (arguments.get(i) instanceof User) {
                    arg = ((User)arguments.get(i)).getLogin();
                }
                else {
                    if (preventValueLogging(handler, method, i)) {
                        arg = "******";
                    }
                    else {
                        arg = (String) Translator.convert(arguments.get(i), String.class);
                    }
                }

                ret.append(arg);

                if ((i + 1) < size) {
                    ret.append(", ");
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

    @Override
    protected String getCallerLogin() {
        return getCallerLogin(getCaller());
    }

    private static User getCaller() {
        return (User) caller.get();
    }

    private static void setCaller(User c) {
        caller.set(c);
    }
}
