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

package com.suse.manager.hub;

import com.redhat.rhn.common.util.http.HttpClientAdapter;

import com.suse.manager.model.hub.ManagerInfoJson;
import com.suse.manager.model.hub.RegisterJson;
import com.suse.manager.model.hub.SCCCredentialsJson;
import com.suse.manager.webui.controllers.ECMAScriptDateAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.methods.RequestBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.cert.Certificate;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * HTTP Client for the Hub Inter-Server-Sync internal server-to-server APIs
 */
public class DefaultHubInternalClient implements HubInternalClient {
    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
        .serializeNulls()
        .create();

    private final String remoteHost;

    private final HttpClientAdapter httpClientAdapter;

    private final String accessToken;

    /**
     * Creates a client to connect to the specified remote host
     * @param remoteHostIn the remote host
     * @param tokenIn the token for authenticating
     * @param rootCA the root certificate, if needed to establish a secure connection
     */
    public DefaultHubInternalClient(String remoteHostIn, String tokenIn, Optional<Certificate> rootCA) {
        this.remoteHost = remoteHostIn;
        this.httpClientAdapter = new HttpClientAdapter(rootCA.stream().toList());
        this.accessToken = tokenIn;
    }

    @Override
    public void registerHub(String token, String rootCA, String gpgKey) throws IOException {
        invokePost("hub/sync", "registerHub", new RegisterJson(token, rootCA, gpgKey));
    }

    @Override
    public void storeCredentials(String username, String password) throws IOException {
        invokePost("hub/sync", "storeCredentials", new SCCCredentialsJson(username, password));
    }

    @Override
    public ManagerInfoJson getManagerInfo() throws IOException {
        return invokeGet("hub", "managerinfo", ManagerInfoJson.class);
    }

    @Override
    public void storeReportDbCredentials(String username, String password) throws IOException {
        invokePost("hub", "storeReportDbCredentials", Map.of("username", username, "password", password));
    }

    @Override
    public String replaceTokens(String newHubToken) throws IOException {
        return invokePost("hub/sync", "replaceTokens", newHubToken, String.class);
    }

    @Override
    public void deregister() throws IOException {
        invokePost("hub/sync", "deregister", null);
    }

    private <R> R invokeGet(String namespace, String apiMethod, Class<R> responseClass)
            throws IOException {
        return invoke(HttpGet.METHOD_NAME, namespace, apiMethod, null, responseClass);
    }

    private <T> void invokePost(String namespace, String apiMethod, T requestObject) throws IOException {
        invoke(HttpPost.METHOD_NAME, namespace, apiMethod, requestObject, Void.class);
    }

    private <T, R> R invokePost(String namespace, String apiMethod, T requestObject, Class<R> responseClass)
        throws IOException {
        return invoke(HttpPost.METHOD_NAME, namespace, apiMethod, requestObject, responseClass);
    }

    private <T, R> R invoke(String httpMethod, String namespace, String apiMethod, T requestObject,
                            Class<R> responseClass) throws IOException {
        RequestBuilder builder = RequestBuilder.create(httpMethod)
            .setUri("https://%s/rhn/%s/%s".formatted(remoteHost, namespace, apiMethod))
            .setHeader("Authorization", "Bearer " + accessToken);

        // Add the request object, if specified
        if (requestObject != null) {
            String body = GSON.toJson(requestObject, new TypeToken<>() { }.getType());
            builder.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        }

        HttpRequestBase request = (HttpRequestBase) builder.build();
        HttpResponse response = httpClientAdapter.executeRequest(request);
        int statusCode = response.getStatusLine().getStatusCode();
        // Ensure we get a valid response
        if (statusCode != HttpStatus.SC_OK) {
            throw new InvalidResponseException("Unexpected response code %d".formatted(statusCode));
        }

        // Parse the response object, if specified
        if (!Void.class.equals(responseClass)) {
            try (Reader responseReader = new InputStreamReader(response.getEntity().getContent())) {
                return Objects.requireNonNull(GSON.fromJson(responseReader, responseClass));
            }
            catch (Exception ex) {
                throw new InvalidResponseException("Unable to parse the JSON response", ex);
            }
        }

        return null;
    }
}
