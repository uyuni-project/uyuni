/*
 * Copyright (c) 2015 SUSE LLC
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

package com.redhat.rhn.testing;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import spark.Request;
import spark.RequestResponseFactory;
import spark.routematch.RouteMatch;

/**
 * SparkTestUtils - utils methods for testing controllers.
 */
public class SparkTestUtils {

    private SparkTestUtils() { }

    /**
     * Creates a mock request with given parametrized url, query parameters.
     *
     * @param matchUrl - the url with parameters (prefixed by a colon) in path, for example:
     *     <code>http://localhost:8080/rhn/manager/:vhm/delete/:vhmlabel/</code>
     * @param queryParams - POST or GET parameters (no multivalued parameters,
     *                    for convenience)
     * @param vals - values that will substitute the parameters in matchUri
     * @return Spark Request object corresponding to given URI (after params substitution)
     * and query parameters
     */
    public static Request createMockRequestWithParams(
            String matchUrl,
            Map<String, String> queryParams,
            Object... vals) {
        return createMockRequestWithParams(
                matchUrl, queryParams, Collections.emptyMap(), vals);
    }

    /**
     * Creates a mock request with given parametrized url, query parameters.
     *
     * @param matchUrl - the url with parameters (prefixed by a colon) in path, for example:
     *     <code>http://localhost:8080/rhn/manager/:vhm/delete/:vhmlabel/</code>
     * @param queryParams - POST or GET parameters (no multivalued parameters,
     *                    for convenience)
     * @param httpHeaders - HTTP headers
     * @param vals - values that will substitute the parameters in matchUri
     * @return Spark Request object corresponding to given URI (after params substitution)
     * and query parameters
     */
    public static Request createMockRequestWithParams(
            String matchUrl,
            Map<String, String> queryParams,
            Map<String, String> httpHeaders,
            Object... vals) {
        final String requestUrl = substituteVariables(matchUrl, vals);
        final RouteMatch match = new RouteMatch(new Object(), matchUrl, requestUrl, "");

        final RhnMockHttpServletRequest mockRequest = new RhnMockHttpServletRequest();
        mockRequest.setSession(new RhnMockHttpSession());
        mockRequest.setRequestURL(requestUrl);
        mockRequest.setMethod("GET");
        mockRequest.setInputStream(new MockServletInputStream());
        setQueryParams(mockRequest, queryParams);
        mockRequest.setPathInfo(URI.create(requestUrl).getPath());
        httpHeaders.forEach(mockRequest::setHeader);

        return RequestResponseFactory.create(match, mockRequest);
    }

    /**
     * Creates a mock request with given parametrized url, query parameters.
     *
     * @param matchUrl - the url with parameters (prefixed by a colon) in path, for example:
     *     <code>http://localhost:8080/rhn/manager/:vhm/delete/:vhmlabel/</code>
     * @param queryParams - POST or GET parameters (multivalued)
     * @param vals - values that will substitute the parameters in matchUri
     * @return Spark Request object corresponding to given URI (after params substitution)
     * and query parameters
     */
    public static Request createMockRequestWithMultiValueParams(
            String matchUrl,
            Map<String, List<String>> queryParams,
            Object... vals) {
        return createMockRequestWithMultiValueParams(matchUrl, queryParams, Collections.emptyMap(), vals);
    }

    /**
     * Creates a mock request with given parametrized url, query parameters.
     *
     * @param matchUrl - the url with parameters (prefixed by a colon) in path, for example:
     *     <code>http://localhost:8080/rhn/manager/:vhm/delete/:vhmlabel/</code>
     * @param queryParams - POST or GET parameters (multivalued)
     * @param httpHeaders - HTTP headers
     * @param vals - values that will substitute the parameters in matchUri
     * @return Spark Request object corresponding to given URI (after params substitution)
     * and query parameters
     */
    public static Request createMockRequestWithMultiValueParams(
            String matchUrl,
            Map<String, List<String>> queryParams,
            Map<String, String> httpHeaders,
            Object... vals) {
        final String requestUrl = substituteVariables(matchUrl, vals);
        final RouteMatch match = new RouteMatch(new Object(), matchUrl, requestUrl, "");

        final RhnMockHttpServletRequest mockRequest = new RhnMockHttpServletRequest();
        mockRequest.setSession(new RhnMockHttpSession());
        mockRequest.setRequestURL(requestUrl);
        mockRequest.setMethod("GET");
        mockRequest.setInputStream(new MockServletInputStream());
        setMultiValueQueryParams(mockRequest, queryParams);
        mockRequest.setPathInfo(URI.create(requestUrl).getPath());
        httpHeaders.forEach(mockRequest::setHeader);

        return RequestResponseFactory.create(match, mockRequest);
    }

    /**
     * Creates a mock request with given parametrized url, query parameters.
     *
     * @param matchUrl - the url with parameters (prefixed by a colon) in path, for example:
     *     <code>http://localhost:8080/rhn/manager/:vhm/delete/:vhmlabel/</code>
     * @param vals - values that will substitute the parameters in matchUri
     * @return Spark Request object corresponding to given URI (after params substitution)
     * and query parameters
     */
    public static Request createMockRequest(String matchUrl, Object... vals) {
        return createMockRequestWithParams(matchUrl, new HashMap<>(), vals);
    }

    /**
     * Substitutes variable parameters in url path.
     *
     * @param matchUrl - url string with parameters (prefixed by a colon)
     * @param vals - parameter values
     * @return string representing a url with parameters substituted with parameter values
     */
    public static String substituteVariables(String matchUrl, Object... vals) {
        final String format = matchUrl.replaceAll("/:[^/]+", "/%s");
        return String.format(format, vals);
    }

    /**
     * Creates a POST mock request with given parametrized url, query parameters.
     *
     * @param matchUrl - the url with parameters (prefixed by a colon) in path, for example:
     *     <code>http://localhost:8080/rhn/manager/:vhm/delete/:vhmlabel/</code>
     * @param httpHeaders - request headers to set
     * @param body - request body to set
     * @param vals - values that will substitute the parameters in matchUri
     * @return Spark Request object corresponding to given URI (after params substitution)
     * and query parameters
     * @throws UnsupportedEncodingException in case the character encoding of the request
     * is not valid.
     */
    public static Request createMockRequestWithBody(String matchUrl,
                                                    Map<String, String> httpHeaders,
                                                    String body, Object... vals)
            throws UnsupportedEncodingException {
        return createMockRequestWithBody("POST", matchUrl, httpHeaders, body, vals);
    }

    /**
     * Creates a POST mock request with given parametrized url, query parameters.
     *
     * @param matchUrl - the url with parameters (prefixed by a colon) in path, for example:
     *     <code>http://localhost:8080/rhn/manager/:vhm/delete/:vhmlabel/</code>
     * @param queryParams - POST or GET parameters (no multivalued parameters,
     *                    for convenience)
     * @param body - request body to set
     * @param vals - values that will substitute the parameters in matchUri
     * @return Spark Request object corresponding to given URI (after params substitution)
     * and query parameters
     * @throws UnsupportedEncodingException in case the character encoding of the request
     * is not valid.
     */
    public static Request createMockRequestWithBodyAndParams(String matchUrl, Map<String, String> queryParams,
                                                             String body, Object... vals)
            throws UnsupportedEncodingException {
        return createMockRequestWithBodyAndParams("POST", matchUrl, queryParams, Collections.emptyMap(), body, vals);
    }

    private static Request createMockRequestWithBodyAndParams(String method, String matchUrl,
                                                              Map<String, String> queryParams,
                                                              Map<String, String> httpHeaders,
                                                             String body, Object... vals)
            throws UnsupportedEncodingException {
        final String requestUrl = substituteVariables(matchUrl, vals);
        final RouteMatch match = new RouteMatch(new Object(), matchUrl, requestUrl, "");

        final RhnMockHttpServletRequest mockRequest = new RhnMockHttpServletRequest();
        mockRequest.setSession(new RhnMockHttpSession());
        mockRequest.setRequestURL(requestUrl);
        mockRequest.setMethod(method);
        MockServletInputStream in = new MockServletInputStream();
        in.setupRead(body.getBytes(
                mockRequest.getCharacterEncoding() != null ?
                        mockRequest.getCharacterEncoding() : "UTF-8"));
        mockRequest.setInputStream(in);
        setQueryParams(mockRequest, queryParams);
        mockRequest.setPathInfo(URI.create(requestUrl).getPath());

        httpHeaders.forEach(
                (name, val) -> mockRequest.setHeader(name, val));

        return RequestResponseFactory.create(match, mockRequest);
    }

    /**
     * Creates a DELETE mock request with given parametrized url, query parameters.
     *
     * @param matchUrl - the url with parameters (prefixed by a colon) in path, for example:
     *     <code>http://localhost:8080/rhn/manager/:vhm/delete/:vhmlabel/</code>
     * @param httpHeaders - request headers to set
     * @param body - request body to set
     * @param vals - values that will substitute the parameters in matchUri
     * @return Spark Request object corresponding to given URI (after params substitution)
     * and query parameters
     * @throws UnsupportedEncodingException in case the character encoding of the request
     * is not valid.
     */
    public static Request createDeleteMockRequestWithBody(String matchUrl,
                                                    Map<String, String> httpHeaders,
                                                    String body, Object... vals)
            throws UnsupportedEncodingException {
        return createMockRequestWithBody("DELETE", matchUrl, httpHeaders, body, vals);
    }

    private static Request createMockRequestWithBody(String method, String matchUrl,
                                                          Map<String, String> httpHeaders,
                                                          String body, Object... vals)
            throws UnsupportedEncodingException {
        final String requestUrl = substituteVariables(matchUrl, vals);
        final RouteMatch match = new RouteMatch(new Object(), matchUrl, requestUrl, "");

        final RhnMockHttpServletRequest mockRequest = new RhnMockHttpServletRequest();
        mockRequest.setSession(new RhnMockHttpSession());
        mockRequest.setRequestURL(requestUrl);
        mockRequest.setMethod(method);
        MockServletInputStream in = new MockServletInputStream();
        in.setupRead(body.getBytes(
                mockRequest.getCharacterEncoding() != null ?
                        mockRequest.getCharacterEncoding() : "UTF-8"));
        mockRequest.setInputStream(in);
        mockRequest.setPathInfo(URI.create(requestUrl).getPath());

        httpHeaders.forEach(
                mockRequest::setHeader);

        return RequestResponseFactory.create(match, mockRequest);
    }

    private static void setQueryParams(RhnMockHttpServletRequest request, Map<String, String> queryParams) {
        queryParams.forEach((name, val) -> request.addParameter(name, new String[]{val}));
        // we must convert to a "multi-value map"
        request.setParameters(queryParams.entrySet().stream().collect(
                Collectors.toMap(v -> v.getKey(), v -> new String[]{v.getValue()})));
    }

    private static void setMultiValueQueryParams(RhnMockHttpServletRequest request,
                                                 Map<String, List<String>> queryParams) {
        queryParams.forEach((name, val) -> request.addParameter(name, val.toArray(new String[0])));
        request.setParameters(queryParams.entrySet().stream().collect(
                Collectors.toMap(v -> v.getKey(), v -> v.getValue().toArray(new String[0]))));
    }
}
