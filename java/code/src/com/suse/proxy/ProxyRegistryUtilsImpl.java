/*
 * Copyright (c) 2025 SUSE LLC
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

package com.suse.proxy;

import static com.suse.utils.Predicates.allProvided;
import static com.suse.utils.Predicates.isProvided;
import static java.nio.charset.StandardCharsets.UTF_8;

import com.redhat.rhn.common.RhnRuntimeException;
import com.redhat.rhn.domain.credentials.CredentialsFactory;
import com.redhat.rhn.domain.credentials.SCCCredentials;

import com.suse.manager.api.ParseException;
import com.suse.rest.RestClient;
import com.suse.rest.RestRequestBuilder;
import com.suse.rest.RestRequestMethodEnum;
import com.suse.rest.RestResponse;

import com.google.gson.JsonObject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class ProxyRegistryUtilsImpl implements ProxyRegistryUtils {

    private static final Logger LOG = LogManager.getLogger(ProxyRegistryUtilsImpl.class);
    private static final String SCC_AUTH_API_URL = "https://scc.suse.com/api/registry/authorize";

    /**
     * Executes a registry request with a retry mechanism.
     * If the first request fails with a 401 Unauthorized, a bearer token is retrieved and the request is retried.
     *
     * @param restRequestBuilder the request builder
     * @param responseHandler    the response handler
     * @param <T>                the type of the response
     * @return the response
     * @throws ParseException if the response cannot be parsed
     */
    public <T> T executeWithRetry(
            RestRequestBuilder restRequestBuilder,
            Function<RestResponse, T> responseHandler
    ) throws ParseException {
        RestResponse response = RestClient.getInstance().execute(restRequestBuilder.build());
        T result = responseHandler.apply(response);

        if (response.isSuccessful()) {
            return result;
        }

        // If the registry requires authorization it will return a 401 Unauthorized HTTP
        // In such case, retrieve a bearer token from the SCC and retry
        if (response.getStatusCode() == 401) {
            String bearerToken = getBearerToken(response);
            if (isProvided(bearerToken)) {
                restRequestBuilder.bearerToken(bearerToken);
                response = RestClient.getInstance().execute(restRequestBuilder.build());
                result = responseHandler.apply(response);

                if (response.getStatusCode() == 200) {
                    return result;
                }
            }
        }

        // If the retry also fails, log the issue and return the provided default value
        LOG.debug("Request failed after retrying with bearer token. Status Code: {}, Response: {}",
                response.getStatusCode(), response.getBody());
        throw new RhnRuntimeException("Failed to execute request: " + response.getStatusCode());
    }

    /**
     * Retrieves the list of repositories from the registry.
     *
     * @param registryUrl the registry URL
     * @return the list of repositories
     * @throws ParseException if the response cannot be parsed
     */
    public List<String> getRepositories(RegistryUrl registryUrl) throws ParseException {
        return executeWithRetry(
                new RestRequestBuilder(RestRequestMethodEnum.GET, registryUrl.getCatalogUrl()),
                response -> {
                    try {
                        return (List<String>) response.getBodyAs(Map.class).get("repositories");
                    }
                    catch (ParseException e) {
                        throw new RhnRuntimeException(e);
                    }
                }
        );
    }

    /**
     * Retrieves the list of tags from the registry.
     *
     * @param registryUrl the registry URL
     * @return the list of tags
     * @throws ParseException if the response cannot be parsed
     */
    public List<String> getTags(RegistryUrl registryUrl) throws ParseException {
        return executeWithRetry(
                new RestRequestBuilder(RestRequestMethodEnum.GET, registryUrl.getTagListUrl()),
                response -> {
                    try {
                        return (List<String>) response.getBodyAs(Map.class).get("tags");
                    }
                    catch (ParseException e) {
                        throw new RhnRuntimeException(e);
                    }
                }
        );
    }

    /**
     * Retrieves the bearer token from the response.
     *
     * @param response the response
     * @return the bearer token or null if failed to match all requirements
     * @throws ParseException if the response cannot be parsed
     */
    public String getBearerToken(RestResponse response) throws ParseException {
        List<String> wwwAuthenticateList = null;
        for (String key : response.getHeaders().keySet()) {
            if (key != null && key.equalsIgnoreCase("WWW-Authenticate")) {
                wwwAuthenticateList = response.getHeaders().get(key);
                break;
            }
        }
        if (wwwAuthenticateList == null || wwwAuthenticateList.isEmpty()) {
            LOG.debug("No 'WWW-Authenticate' header (case insensitive) found in the response");
            return null;
        }

        String wwwAuthenticate = wwwAuthenticateList.get(0);
        Map<String, String> authParams = new HashMap<>();
        for (String item : wwwAuthenticate.split(",")) {
            if (item.contains("=")) {
                String[] parts = item.split("=", 2);
                String key = parts[0].trim();
                String value = parts[1].replace("\"", "").trim();
                authParams.put(key, value);
            }
        }

        String bearerRealm = authParams.get("Bearer realm");
        String service = authParams.get("service");
        String scope = authParams.get("scope");

        if (!allProvided(bearerRealm, service, scope)) {
            LOG.debug("Not all required parameters found in 'Www-Authenticate' header: {}", authParams);
            return null;
        }

        // If the bearerRealm is NOT the SCC_AUTH_API_URL, we don't want to provide scc credentials
        if (!SCC_AUTH_API_URL.equals(bearerRealm)) {
            LOG.debug("Bearer realm does not match {}, it is {}", SCC_AUTH_API_URL, bearerRealm);
            return null;
        }

        String authUrl = bearerRealm + "?service=" + URLEncoder.encode(service, UTF_8) + "&scope=" + scope;

        SCCCredentials sccCredentials = CredentialsFactory.listSCCCredentials().get(0);
        RestRequestBuilder sccTokenRequest = new RestRequestBuilder(RestRequestMethodEnum.GET, authUrl);
        sccTokenRequest.basicAuth(sccCredentials.getUsername(), sccCredentials.getPassword());
        RestResponse sccTokenResponse = RestClient.getInstance().execute(sccTokenRequest.build());

        if (!sccTokenResponse.isSuccessful()) {
            LOG.debug("Failed to retrieve bearer token from SCC: {}", sccTokenResponse);
            return null;
        }

        JsonObject jsonResponse = sccTokenResponse.getBodyAs(JsonObject.class);
        return jsonResponse.get("token").getAsString();
    }

}
