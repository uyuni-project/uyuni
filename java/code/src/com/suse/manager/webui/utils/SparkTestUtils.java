/**
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

package com.suse.manager.webui.utils;

import com.redhat.rhn.testing.RhnMockHttpServletRequest;
import com.redhat.rhn.testing.RhnMockHttpSession;
import spark.Request;
import spark.RequestResponseFactory;
import spark.routematch.RouteMatch;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

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
        final String requestUrl = substituteVariables(matchUrl, vals);
        final RouteMatch match = new RouteMatch(new Object(), matchUrl, requestUrl, "");

        final RhnMockHttpServletRequest mockRequest = new RhnMockHttpServletRequest();
        mockRequest.setSession(new RhnMockHttpSession());
        mockRequest.setRequestURL(requestUrl);
        mockRequest.setupGetMethod("GET");

        // we need to set the query params twice as mockobjects request uses two separate
        // backing objects
        queryParams.forEach(
                (name, val) -> mockRequest.setupAddParameter(name, new String[]{val}));
        // we must convert to a "multi-value map"
        mockRequest.setupGetParameterMap(queryParams.entrySet().stream().collect(
                Collectors.toMap(v -> v.getKey(), v -> new String[]{v.getValue()})));

        mockRequest.setupPathInfo(URI.create(requestUrl).getPath());

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
}
