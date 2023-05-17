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

import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Base logic for processing logs.
 */
public class LoggingInvocationProcessor {

    private static final Logger LOGGER = LogManager.getLogger(LoggingInvocationProcessor.class);

    private static ThreadLocal timer = new ThreadLocal() {
        @Override
        protected synchronized Object initialValue() {
            return new StopWatch();
        }
    };

    public static final Set<String> DEFAULT_SENSITIVE_KEYWORDS =
            Set.of("pass", "key", "certificate", "token", "secret");
    /**
     * This map is a way to explicitly override whether or not a value should be redacted in the
     * log or not.
     * If the value for a handler.method / parameter name pair is
     *
     *  - true it will be redacted
     *  - false it won't be redacted
     *  - not present in the map the redaction will be based on the additional filtering for keywords
     */
    public static final Map<String, Map<String, Boolean>> DEFAULT_EXPLICIT_OVERRIDE = Map.ofEntries(
    );

    /**
     * @param sensitiveKeywordsIn list of keyword suggesting sensitive values that should be redacted in the log.
     * @param explicitOverridesIn a map containing explicit overrides to the automatic keyword based filtering to
     *                          cover false positives/negatives.
     */
    public LoggingInvocationProcessor(Set<String> sensitiveKeywordsIn,
                                      Map<String, Map<String, Boolean>> explicitOverridesIn) {
        this.sensitiveKeywords = sensitiveKeywordsIn;
        this.explicitOverrides = explicitOverridesIn;
    }

    /**
     * default constructor
     */
    public LoggingInvocationProcessor() {
        this(DEFAULT_SENSITIVE_KEYWORDS, DEFAULT_EXPLICIT_OVERRIDE);
    }

    private final Map<String, Map<String, Boolean>> explicitOverrides;
    private final Set<String> sensitiveKeywords;



    /**
     * Method for creating the log message taking redaction logic into account
     *
     * @param handlerName name of the handler
     * @param methodName name of the method
     * @param arguments map from argument name to value
     * @param ip ip of the caller
     * @param caller user making the call
     * @param duration duration of how long the execution took
     *
     * @return a string buffer containing the log message
     */
    public StringBuilder logMessage(
        String handlerName,
        String methodName,
        Optional<Map<String, String>> arguments,
        String ip,
        Optional<User> caller,
        Duration duration
    ) {
        Optional<Map<String, String>> argumentsAfterRedaction = arguments.map(args ->
                args.entrySet().stream()
                    .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> preventValueLogging(handlerName, methodName, e.getKey(), e.getValue()) ?
                                "******" :
                                e.getValue()
                    ))
        );
        // Create the call in a separate buffer for reuse
        StringBuilder buf = new StringBuilder();
        buf.append("REQUESTED FROM: ");
        buf.append(ip);
        buf.append(" CALL: ");
        buf.append(handlerName);
        buf.append(".");
        buf.append(methodName);
        buf.append("(");
        buf.append(
            argumentsAfterRedaction.map(args ->
                args.entrySet().stream()
                    .map(e -> e.getKey() + "=" + e.getValue())
                    .collect(Collectors.joining(", "))
            ).orElse("no argument information provided")
        );
        buf.append(")");
        buf.append(" CALLER: (");
        buf.append(caller.map(User::getLogin).orElse("none"));
        buf.append(") TIME: ");

        buf.append(duration.toMillis() / 1000.0);
        buf.append(" seconds");
        return buf;
    }

    /**
     * Logging method being called in case of an exception.
     *
     * @param handlerName name of the handler
     * @param methodName name of the method
     * @param arguments map from argument name to value
     * @param ip ip of the caller
     * @param caller user making the call
     * @param exception exception to log
     *
     */
    public void processException(
            String handlerName,
            String methodName,
            Optional<Map<String, String>> arguments,
            Optional<User> caller,
            String ip,
            Throwable exception) {
        try {
            StringBuilder buf = logMessage(handlerName, methodName, arguments, ip,
                    caller, Duration.ofMillis(stopTimer()));
            buf.append(System.lineSeparator());
            buf.append(exception);

            LOGGER.info(buf);
        }
        catch (RuntimeException e) {
            LOGGER.error("onException error CALL: {} {}", handlerName, methodName, e);
        }
    }

    /**
     * Makes the after hook processing logic agnostic, taking as parameters all data to be logged and
     * the processingTimer to be stopped.
     *
     * @param handlerName the handler of the request
     * @param methodName the method called
     * @param arguments a map containing argument name / string value pairs
     * @param caller user making the call
     * @param ip the request's ip
     */
    public void afterProcess(
            String handlerName,
            String methodName,
            Optional<Map<String, String>> arguments,
            Optional<User> caller,
            String ip
    ) {
        try {
            LOGGER.info(logMessage(handlerName, methodName, arguments, ip,
                    caller, Duration.ofMillis(stopTimer())));
        }
        catch (RuntimeException e) {
            LOGGER.error("postProcess error CALL: {} {}", handlerName, methodName, e);
        }
    }

    /**
     * Stop the timer
     * @return the time registered in this timer.
     */
    private long stopTimer() {
        getStopWatch().stop();
        return getStopWatch().getTime();
    }

    /**
     * @param handler - name of the handler
     * @param method - name of the method
     * @param argName - name of the argument
     * @return - whether the value should be hidden from logging based on argName
     */
    private boolean preventValueLogging(String handler, String method, String argName, String value) {
        String handlerAndMethod = handler + "." + method;
        // Look into values as well as some endpoints accept arbitrary json data like custom states which can contain
        // sensitive information. The current implementation redacts the whole value which could be too much.
        // In the future it might make sense to at least check the value for complex formats like json and only
        // redact the part that is necessary.
        boolean restrictedValue = sensitiveKeywords.stream().anyMatch(s -> value.toLowerCase().contains(s));
        boolean restrictedArgument = sensitiveKeywords.stream().anyMatch(s -> argName.toLowerCase().contains(s));
        Optional<Boolean> overrideValue =
                Optional.ofNullable(explicitOverrides.get(handlerAndMethod))
                        .flatMap(params -> Optional.ofNullable(params.get(argName)));

        return overrideValue.orElse(restrictedArgument || restrictedValue);
    }

    protected static StopWatch getStopWatch() {
        return (StopWatch) timer.get();
    }

}
