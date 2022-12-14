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

import com.redhat.rhn.common.hibernate.LookupException;
import com.redhat.rhn.common.translation.Translator;
import com.redhat.rhn.domain.session.InvalidSessionIdException;
import com.redhat.rhn.domain.user.User;
import com.redhat.rhn.frontend.servlets.PxtCookieManager;
import com.redhat.rhn.manager.session.SessionManager;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import spark.Request;
import spark.Spark;

/**
 * HttpApiLoggingInvocationProcessor defines Spark filters (after and before)
 * to log method calls in the context of the HTTP API.
 */
public class HttpApiLoggingInvocationProcessor extends LoggingInvocationProcessor {

    // The pattern to capture everything starting with HTTP_API_ROOT
    public static final String HTTP_API_URL_PATTERN = HttpApiRegistry.HTTP_API_ROOT + "*";
    private final ApiRequestParser apiRequestParser = new ApiRequestParser(new Gson());

    private static ThreadLocal<User> caller = new ThreadLocal<>();

    /**
     * Register before and after filters in Spark
     */
    public void register() {

        // Reset the timer and set the caller (the caller is set here, instead of in after, to capture logout caller)
        Spark.before(HTTP_API_URL_PATTERN, (request, response) -> {
            getStopWatch().reset();
            getStopWatch().start();
            caller.set(getCaller(request));
        });

        Spark.after(HTTP_API_URL_PATTERN, (request, response) -> {
            String[] urlTokens = request.pathInfo().split(HttpApiRegistry.HTTP_API_ROOT)[1].split("/");

            // To be HTTP API related, the URL needs to have at least two parts (handler and method names)
            if (urlTokens.length < 2) {
                return;
            }
            String handler = getHandlerName(urlTokens);
            String methodName = urlTokens[urlTokens.length - 1];

            afterProcess(
                handler,
                methodName,
                processParams(request, handler, methodName),
                request.ip()
            );
        });
    }

    /**
     * Extracts params from request.queryMap() and request.body()
     *
     * @param request - the spark request being processed
     * @param handler - the name of the handler
     * @param methodName - the name of the method
     * @return the params as a StringBuilder ready to be logged
     */
    public StringBuilder processParams(Request request, String handler, String methodName) {
        Map<String, String> paramsMap = getQueryMapParams(request);
        paramsMap.putAll(parseBody(request.body()));
        return processParams(paramsMap, handler, methodName);
    }

    /**
     * Convert the params from a Map to a StringBuilder with format of a method call
     * @param paramsMap the Map containing the parameters
     * @param handler name of the handler
     * @param methodName name of the method
     * @return the params as a StringBuilder ready to be logged
     */
    public StringBuilder processParams(Map<String, String> paramsMap, String handler, String methodName) {
        StringBuilder params = new StringBuilder();
        for (String key : paramsMap.keySet()) {
            params.append(key);
            params.append("=");
            params.append(getParamValueToLog(handler, methodName, key, paramsMap.get(key)));
            params.append(", ");
        }

        // Removes trailing comma
        if (params.length() > 1) {
            params.delete(params.length() - 2, params.length());
        }
        return params;
    }

    /**
     * Extract the params from request.queryMap()
     *
     * @param request - the Spark request being processed
     * @return a map with the param names as keys and param values as values
     */
    public Map<String, String> getQueryMapParams(Request request) {
        return request.queryMap().toMap().entrySet().stream().collect(
            Collectors.toMap(
                Map.Entry::getKey,
                e -> e.getValue().length == 1 ? e.getValue()[0] : Arrays.toString(e.getValue())
            )
        );
    }

    /**
     * Converts the request body in a map of params
     * @param body request body as String
     * @return the map of parameters extracted from the body
     */
    public Map<String, String> parseBody(String body) {
        try {
            Map<String, JsonElement> bodyParams = apiRequestParser.parseBody(body);
            return bodyParams.entrySet().stream().collect(
                    Collectors.toMap(
                            Map.Entry::getKey,
                            e -> e.getValue().toString()
                    )
            );
        }
        catch (ParseException e) {
            return Collections.emptyMap();
        }
    }

    /**
     * Translate the param value and hide it if necessary
     * @param handler - the name of the handler
     * @param methodName - the name of the method
     * @param key is the param name
     * @param value param value
     * @return param value ready to be logged
     */
    public String getParamValueToLog(String handler, String methodName, String key, String value) {
        if (preventValueLogging(handler, methodName, key)) {
            return "******";
        }
        else {
            return (String) Translator.convert(value, String.class);
        }
    }

    /**
     * Extracts the handler name from URL tokens.
     * Basically, it concatenates all the tokens, except the last one
     * that is the methodName, connecting them through a dot.
     *
     * @param urlTokens - the parts of the URL after splitting it
     * @return the handler name
     */
    public String getHandlerName(String[] urlTokens) {
        StringBuilder handlerName = new StringBuilder();
        for (int i = 0; i < urlTokens.length - 1; i++) {
            handlerName.append(urlTokens[i]);
            handlerName.append(".");
        }
        handlerName.deleteCharAt(handlerName.length() - 1);
        return handlerName.toString();
    }

    /**
     * Extracts the logged user from session
     * @param request - the Spark request being processed
     * @return the logged user or null
     */
    public User getCaller(Request request) {
        try {
            String sessionKey = request.cookie(PxtCookieManager.PXT_SESSION_COOKIE_NAME);
            return SessionManager.loadSession(sessionKey).getUser();
        }
        catch (InvalidSessionIdException | LookupException e) {
            // As this code is just for logging, it could keep going when no session is found.
            return null;
        }
    }

    @Override
    public String getCallerLogin() {
        return getCallerLogin(caller.get());
    }

}
