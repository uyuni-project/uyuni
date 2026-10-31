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
 * SPDX-License-Identifier: GPL-2.0-only
 */

package com.suse.manager.hub;

import com.redhat.rhn.common.util.http.HttpClientAdapter;

import com.suse.manager.model.hub.ChannelInfoDetailsJson;
import com.suse.manager.model.hub.ChannelInfoJson;
import com.suse.manager.model.hub.ManagerInfoJson;
import com.suse.manager.model.hub.OrgInfoJson;
import com.suse.manager.model.hub.RegisterJson;
import com.suse.manager.model.hub.SCCCredentialsJson;
import com.suse.manager.model.hub.ServerInfoJson;
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
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Type;
import java.security.cert.Certificate;
import java.util.Date;
import java.util.List;
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
    public void scheduleProductRefresh() throws IOException {
        invokePost("hub", "scheduleProductRefresh", Map.of());
    }

    @Override
    public void deregister() throws IOException {
        invokePost("hub/sync", "deregister", null);
    }

    @Override
    public List<OrgInfoJson> getAllPeripheralOrgs() throws IOException {
        // Use a TypeToken to preserve the generic type information
        Type type = new TypeToken<List<OrgInfoJson>>() { }.getType();
        return invokeGet("hub", "listAllPeripheralOrgs", type);
    }

    @Override
    public List<ChannelInfoJson> getAllPeripheralChannels() throws IOException {
        // Use a TypeToken to preserve the generic type information
        Type type = new TypeToken<List<ChannelInfoJson>>() { }.getType();
        return invokeGet("hub", "listAllPeripheralChannels", type);
    }

    @Override
    public void syncChannels(List<ChannelInfoDetailsJson> channelInfo) throws IOException {
        invokePost("hub", "syncChannels", channelInfo);
    }

    @Override
    public void deleteIssV1Master() throws IOException {
        invokePost("hub/sync/migrate/v1", "deleteMaster", null);
    }

    @Override
    public ServerInfoJson getServerInfo() throws IOException {
        return invokeGet("hub", "serverInfo", ServerInfoJson.class);
    }

    private <R> R invokeGet(String namespace, String apiMethod, Type responseType)
            throws IOException {
        return invoke(HttpGet.METHOD_NAME, namespace, apiMethod, null, responseType);
    }

    private <T> void invokePost(String namespace, String apiMethod, T requestObject) throws IOException {
        invoke(HttpPost.METHOD_NAME, namespace, apiMethod, requestObject, Void.class);
    }

    private <T, R> R invokePost(String namespace, String apiMethod, T requestObject, Type responseType)
            throws IOException {
        return invoke(HttpPost.METHOD_NAME, namespace, apiMethod, requestObject, responseType);
    }

    private <T, R> R invoke(
            String httpMethod, String namespace, String apiMethod, T requestObject, Type responseType
    ) throws IOException {
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
            throw new InvalidResponseException("Unexpected response code %d: %s".formatted(statusCode,
                    extractErrorMessage(response).orElse("")));
        }

        // Parse the response object, if specified
        if (!Void.class.equals(responseType)) {
            try (Reader responseReader = new InputStreamReader(response.getEntity().getContent())) {
                return Objects.requireNonNull(GSON.fromJson(responseReader, responseType));
            }
            catch (Exception ex) {
                throw new InvalidResponseException("Unable to parse the JSON response", ex);
            }
        }
        return null;
    }

    private Optional<String> extractErrorMessage(HttpResponse response) {
        try {
            String body = EntityUtils.toString(response.getEntity());
            Map<String, Object> responseMap = GSON.fromJson(body, new TypeToken<Map<String, Object>>() { }.getType());
            Object messagesObj = responseMap.get("messages");
            if (messagesObj instanceof List<?> messages) {
                return Optional.of(String.join(", ", (List<String>) messages));
            }
            return Optional.empty();
        }
        catch (Exception eIn) {
            return Optional.empty();
        }
    }
}
