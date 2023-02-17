/*
 * Copyright (c) 2022 SUSE LLC
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
package com.suse.manager.api;

import com.redhat.rhn.domain.user.User;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * Base logic for processing logs.
 */
public abstract class LoggingInvocationProcessor {

    private static final Logger LOGGER = LogManager.getLogger(LoggingInvocationProcessor.class);

    private static ThreadLocal timer = new ThreadLocal() {
        protected synchronized Object initialValue() {
            return new StopWatch();
        }
    };

    /**
     * This map contains the method args whose value is sensible and should not be logged.
     *
     * The key follows the pattern handler.methodName and the value is a map containing
     * the restricted args, arg position as key and arg name as value.
     */
    private static final Map<String, Map<Integer, String>> RESTRICTED_ARGS = new HashMap<>();

    static {
        RESTRICTED_ARGS.put("auth.login", Map.of(1, "password"));
        RESTRICTED_ARGS.put("system.bootstrap", Map.of(4, "sshPassword"));
        RESTRICTED_ARGS.put(
            "system.bootstrapWithPrivateKey",
            Map.of(4, "sshPrivKey", 5, "sshPrivKeyPass")
        );
        RESTRICTED_ARGS.put(
            "admin.payg.create",
            Map.of(
                5, "password",
                6, "key",
                7, "keyPassword",
                11, "bastionPassword",
                12, "bastionKey",
                13, "bastionKeyPassword"
            )
        );
        RESTRICTED_ARGS.put("admin.payg.setDetails", Map.of(2, "details"));
        RESTRICTED_ARGS.put(
            "proxy.container_config",
            Map.of(
                6, "caCertificate",
                7, "caKey",
                8, "caPassword"
            )
        );
    }

    /**
     * Makes the after hook processing logic agnostic, taking as parameters all data to be logged and
     * the processingTimer to be stopped.
     *
     * @param handlerName the handler of the request
     * @param methodName the method called
     * @param arguments the string representation of the arguments passed to the method
     * @param ip the request's ip
     */
    public void afterProcess(
        String handlerName,
        String methodName,
        StringBuffer arguments,
        String ip
    ) {
        try {
            // Create the call in a separate buffer for reuse
            StringBuffer buf = new StringBuffer();
            buf.append("REQUESTED FROM: ");
            buf.append(ip);
            buf.append(" CALL: ");
            buf.append(handlerName);
            buf.append(".");
            buf.append(methodName);
            buf.append("(");
            buf.append(arguments);
            buf.append(")");
            buf.append(" CALLER: (");
            buf.append(getCallerLogin());
            buf.append(") TIME: ");

            buf.append(stopTimer() / 1000.00);
            buf.append(" seconds");

            LOGGER.info(buf);
        }
        catch (RuntimeException e) {
            LOGGER.error("postProcess error CALL: {} {}", handlerName, methodName, e);
        }
    }

    protected abstract String getCallerLogin();

    /**
     * Stop the timer
     * @return the time registered in this timer.
     */
    private long stopTimer() {
        getStopWatch().stop();
        return getStopWatch().getTime();
    }

    // determines whether the value should be hidden from logging based on argPosition
    protected static boolean preventValueLogging(String handler, String method, int argPosition) {
        String handlerAndMethod = handler + "." + method;
        return RESTRICTED_ARGS.containsKey(handlerAndMethod) &&
                RESTRICTED_ARGS.get(handlerAndMethod).containsKey(argPosition);
    }

    /**
     * @param handler - name of the handler
     * @param method - name of the method
     * @param argName - name of the argument
     * @return - whether the value should be hidden from logging based on argName
     */
    protected static boolean preventValueLogging(String handler, String method, String argName) {
        if (argName.toLowerCase().contains("password")) {
            return true;
        }
        String handlerAndMethod = handler + "." + method;
        return RESTRICTED_ARGS.containsKey(handlerAndMethod) &&
                RESTRICTED_ARGS.get(handlerAndMethod).containsValue(argName);
    }

    protected static StopWatch getStopWatch() {
        return (StopWatch) timer.get();
    }

    /**
     * Extracts the login of the caller
     * @param caller - the user
     * @return the login of the user or "none" if no user is present
     */
    public String getCallerLogin(User caller) {
        String ret = "none";
        if (caller != null) {
            ret = caller.getLogin();
        }
        return ret;
    }
}
