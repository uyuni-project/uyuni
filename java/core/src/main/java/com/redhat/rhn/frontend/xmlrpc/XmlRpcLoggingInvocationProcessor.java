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

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    private static ThreadLocal<Method> calledMethod = new ThreadLocal<>();

    /**
     * This is a hack to determine the actual method BaseHandler called and recover the
     * parameter names for filtering sensitive parameters from logging.
     * @param method the method the BaseHandler called in this request thread.
     */
    public static void setCalledMethod(Method method) {
        calledMethod.set(method);
    }

    private Optional<Method> getAndResetMethod() {
        Method method = calledMethod.get();
        calledMethod.remove();
        return Optional.ofNullable(method);
    }

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

        List<Object> arguments = invocation.getArguments();
        // HACK ALERT!  We need the caller, would be better in
        // the postProcess, but that works for ALL methods except
        // logout.  So we do it here.
        if ((arguments != null) && (!arguments.isEmpty())) {
            if (arguments.get(0) instanceof User u) {
                setCaller(u);
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

    private Map<String, String> getParamMap(XmlRpcInvocation invocation, Method method) {
        List<Object> rawArguments = invocation.getArguments();
        List<String> paramNames = Arrays
                .stream(method.getParameters())
                .map(Parameter::getName)
                .toList();
        return IntStream.range(0, rawArguments.size()).boxed().collect(
                Collectors.toMap(
                        i -> paramNames.get(i),
                        i -> {
                            Object arg = rawArguments.get(i);
                            if (arg instanceof User u) {
                                return u.getLogin();
                            }
                            else {
                                return (String) Translator.convert(arg, String.class);
                            }
                        }

                )
        );
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Object after(XmlRpcInvocation invocation, Object returnValue) {
        afterProcess(
            invocation.getHandlerName(),
            invocation.getMethodName(),
            getAndResetMethod().map(m -> getParamMap(invocation, m)),
            Optional.ofNullable(getCaller()),
            RhnXmlRpcServer.getCallerIp()
        );
        return returnValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onException(XmlRpcInvocation invocation, Throwable exception) {
        processException(
            invocation.getHandlerName(),
            invocation.getMethodName(),
            getAndResetMethod().map(m -> getParamMap(invocation, m)),
            Optional.ofNullable(getCaller()),
            RhnXmlRpcServer.getCallerIp(),
            exception
        );
    }

    /**
     * If the key is a sessionKey, we'll return the username, otherwise we'll
     * return (unknown).
     * @param key potential sessionKey.
     * @return  username, (Invalid Session ID), or (unknown);
     */
    private User getLoggedInUser(String key) {
        try {
            return BaseHandler.getLoggedInUser(key);
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
        if (key == null || key.isEmpty()) {
            return false;
        }

        // Get the id
        String[] keyParts = StringUtils.split(key, 'x');

        // make sure the id is numeric and can be made into a Long
        return StringUtils.isNumeric(keyParts[0]);
    }

    private static User getCaller() {
        return caller.get();
    }

    private static void setCaller(User c) {
        caller.set(c);
    }
}
