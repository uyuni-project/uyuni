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

import java.util.HashMap;
import java.util.Map;

/**
 * SparkTestUtils - todo javadoc
 */
public class SparkTestUtils {

    private SparkTestUtils() { }

    public static Request createMockRequest(
            String matchUri,
            Map<String, String> queryParams,
            Object ... vals) {
        final String requestUri = substituteVariables(matchUri, vals);
        final RouteMatch match = new RouteMatch(new Object(), matchUri, requestUri, "");

        final RhnMockHttpServletRequest mockRequest = new RhnMockHttpServletRequest();
        mockRequest.setSession(new RhnMockHttpSession());
        mockRequest.setupGetRequestURI(requestUri);
        mockRequest.setupGetMethod("GET");
        mockRequest.setupGetParameterMap(queryParams);
        mockRequest.setupPathInfo(requestUri); // todo verify

        return RequestResponseFactory.create(match, mockRequest);
    }

    /**
     * matchuri: "http://localhost:8080/rhn/manager/:vhm/delete/:vhmlabel/";
     * vals: "foo", "bar"
     * returns: "http://localhost:8080/rhn/manager/foo/delete/bar/";
     * @param matchUri
     * @param vals
     * @return
     */
    public static Request createMockRequest(
            String matchUri,
            Object ... vals) {
        return createMockRequest(matchUri, new HashMap<String, String>(), vals);
    }

    // public for testability
    public static String substituteVariables(String matchUri, Object ... vals) {
        final String format = matchUri.replaceAll("/:[^/]+", "/%s");
        return String.format(format, vals);
    }
}
