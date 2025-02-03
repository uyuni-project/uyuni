/*
 * Copyright (c) 2025 SUSE LLC
 *
 * This software is licensed to you under the GNU General Public License,
 * version 2 (GPLv2). There is NO WARRANTY for this software, express or
 * implied, including the implied warranties of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. You should have received a copy of GPLv2
 * along with this software; if not, see
 * http://www.gnu.org/licenses/old-licenses/gpl-2.0.txt.
 */

package com.suse.manager.hub.test;

import com.redhat.rhn.common.hibernate.HibernateFactory;
import com.redhat.rhn.testing.RhnMockHttpServletResponse;
import com.redhat.rhn.testing.SparkTestUtils;

import com.suse.manager.model.hub.HubFactory;
import com.suse.manager.model.hub.IssHub;
import com.suse.manager.model.hub.IssPeripheral;
import com.suse.manager.model.hub.IssRole;
import com.suse.manager.model.hub.TokenType;
import com.suse.manager.webui.utils.token.IssTokenBuilder;
import com.suse.manager.webui.utils.token.Token;
import com.suse.manager.webui.utils.token.TokenBuildingException;
import com.suse.manager.webui.utils.token.TokenParsingException;
import com.suse.utils.Json;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import spark.Request;
import spark.RequestResponseFactory;
import spark.Response;
import spark.RouteImpl;
import spark.route.HttpMethod;
import spark.routematch.RouteMatch;

public class ControllerTestUtils {

    private String apiEndpoint;
    private HttpMethod httpMethod;
    private String serverFqdn;
    private String authBearerToken;
    private Instant authBearerTokenExpiration;
    private IssRole role;
    private boolean addBearerTokenToHeaders;
    private Map<String, String> bodyMap;

    public ControllerTestUtils() {
        apiEndpoint = null;
        httpMethod = null;
        serverFqdn = null;
        authBearerToken = null;
        authBearerTokenExpiration = null;
        role = null;
        addBearerTokenToHeaders = false;
        bodyMap = null;
    }

    public ControllerTestUtils withServerFqdn(String serverFqdnIn)
            throws TokenBuildingException, TokenParsingException {
        serverFqdn = serverFqdnIn;
        Token dummyServerToken = new IssTokenBuilder(serverFqdn).usingServerSecret().build();
        authBearerToken = dummyServerToken.getSerializedForm();
        authBearerTokenExpiration = dummyServerToken.getExpirationTime();
        return this;
    }

    public ControllerTestUtils withApiEndpoint(String apiEndpointIn) {
        apiEndpoint = apiEndpointIn;
        return this;
    }

    public ControllerTestUtils withHttpMethod(HttpMethod httpMethodIn) {
        httpMethod = httpMethodIn;
        return this;
    }

    public ControllerTestUtils withRole(IssRole roleIn) {
        role = roleIn;
        return this;
    }

    public ControllerTestUtils withBearerTokenInHeaders() {
        addBearerTokenToHeaders = true;
        return this;
    }

    public ControllerTestUtils withBody(Map<String, String> bodyMapIn) {
        bodyMap = bodyMapIn;
        return this;
    }

    public Object simulateControllerApiCall() throws Exception {
        HubFactory hubFactory = new HubFactory();
        hubFactory.saveToken(serverFqdn, authBearerToken, TokenType.ISSUED, authBearerTokenExpiration);
        if (null != role) {
            switch (role) {
                case HUB:
                    hubFactory.save(new IssHub(serverFqdn, ""));
                    break;
                case PERIPHERAL:
                    hubFactory.save(new IssPeripheral(serverFqdn, ""));
                    break;
                default:
                    throw new IllegalArgumentException("unsupported role " + role.getLabel());
            }
        }

        String bodyString = (null == bodyMap) ? null : Json.GSON.toJson(bodyMap, Map.class);

        return simulateApiEndpointCall(apiEndpoint, httpMethod,
                addBearerTokenToHeaders ? authBearerToken : null, bodyString);
    }

    public static Object simulateApiEndpointCall(String apiEndpoint, HttpMethod httpMethod,
                                                 String authBearerToken, String body)
            throws Exception {
        Optional<RouteMatch> routeMatch = spark.Spark.routes()
                .stream()
                .filter(e -> apiEndpoint.equals(e.getMatchUri()))
                .filter(e -> httpMethod.equals(e.getHttpMethod()))
                .findAny();

        if (routeMatch.isEmpty()) {
            throw new IllegalStateException("route not found for " + apiEndpoint);
        }

        RouteImpl routeImpl = (RouteImpl) routeMatch.get().getTarget();

        Map<String, String> httpHeaders = (null == authBearerToken) ?
                new HashMap<>() :
                Map.of("Authorization", "Bearer " + authBearerToken);

        Request dummyTestRequest = (null == body) ?
                SparkTestUtils.createMockRequestWithParams(apiEndpoint, new HashMap<>(), httpHeaders) :
                SparkTestUtils.createMockRequestWithBody(apiEndpoint, httpHeaders, body);

        Response dummyTestResponse = RequestResponseFactory.create(new RhnMockHttpServletResponse());
        Object returnObject = routeImpl.handle(dummyTestRequest, dummyTestResponse);
        HibernateFactory.rollbackTransaction();
        return returnObject;
    }
}
