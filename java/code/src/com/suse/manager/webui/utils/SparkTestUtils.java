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
     * Creates a mock request with given parametrized uri, query parameters.
     *
     * @param matchUri - the uri with parameters (prefixed by a colon) in path, for example:
     *     <code>http://localhost:8080/rhn/manager/:vhm/delete/:vhmlabel/</code>
     * @param queryParams - POST or GET parameters (no multivalued parameters,
     *                    for convenience)
     * @param vals - values that will substitute the parameters in matchUri
     * @return Spark Request object corresponding to given URI (after params substitution)
     * and query parameters
     */
    public static Request createMockRequestWithParams(
            String matchUri,
            Map<String, String> queryParams,
            Object... vals) {
        final String requestUri = substituteVariables(matchUri, vals);
        final RouteMatch match = new RouteMatch(new Object(), matchUri, requestUri, "");

        final RhnMockHttpServletRequest mockRequest = new RhnMockHttpServletRequest();
        mockRequest.setSession(new RhnMockHttpSession());
        mockRequest.setupGetRequestURI(requestUri);
        mockRequest.setupGetMethod("GET");

        // we need to set the query params twice as mockobjects request uses two separate
        // backing objects
        queryParams.forEach(
                (name, val) -> mockRequest.setupAddParameter(name, new String[]{val}));
        // we must convert to a "multi-value map"
        mockRequest.setupGetParameterMap(queryParams.entrySet().stream().collect(
                Collectors.toMap(v -> v.getKey(), v -> new String[]{v.getValue()})));

        mockRequest.setupPathInfo(URI.create(requestUri).getPath());

        return RequestResponseFactory.create(match, mockRequest);
    }

    /**
     * Creates a mock request with given parametrized uri, query parameters.
     *
     * @param matchUri - the uri with parameters (prefixed by a colon) in path, for example:
     *     <code>http://localhost:8080/rhn/manager/:vhm/delete/:vhmlabel/</code>
     * @param vals - values that will substitute the parameters in matchUri
     * @return Spark Request object corresponding to given URI (after params substitution)
     * and query parameters
     */
    public static Request createMockRequest(String matchUri, Object... vals) {
        return createMockRequestWithParams(matchUri, new HashMap<>(), vals);
    }

    /**
     * Substitutes variable parameters in uri path.
     *
     * @param matchUri - uri string with parameters (prefixed by a colon)
     * @param vals - parameter values
     * @return string representing a uri with parameters substituted with parameter values
     */
    public static String substituteVariables(String matchUri, Object... vals) {
        final String format = matchUri.replaceAll("/:[^/]+", "/%s");
        return String.format(format, vals);
    }
}
