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
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.security.cert.Certificate;
import java.util.Date;
import java.util.Map;
import java.util.Optional;

/**
 * HTTP Client for the ISS internal server-to-server APIs
 */
public class DefaultIssInternalClient implements IssInternalClient {
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
    public DefaultIssInternalClient(String remoteHostIn, String tokenIn, Optional<Certificate> rootCA) {
        this.remoteHost = remoteHostIn;
        this.httpClientAdapter = new HttpClientAdapter(rootCA.stream().toList());
        this.accessToken = tokenIn;
    }

    @Override
    public void registerHub(String token, String rootCA) throws IOException {
        invokePostMethod("registerHub", new RegisterJson(token, rootCA), Void.class);
    }

    @Override
    public void storeCredentials(String username, String password) throws IOException {
        invokePostMethod("storeCredentials", new SCCCredentialsJson(username, password), Void.class);
    }

    @Override
    public SCCCredentialsJson generateCredentials() throws IOException {
        SCCCredentialsJson responseObject = invokePostMethod("generateCredentials", null, SCCCredentialsJson.class);
        if (responseObject == null) {
            throw new IOException("Null response object received");
        }

        return responseObject;
    }

    @Override
    public ManagerInfoJson getManagerInfo() throws IOException {
        return invokeGetMethod("managerinfo", ManagerInfoJson.class);
    }

    @Override
    public void storeReportDbCredentials(String username, String password) throws IOException {
        invokePostMethod("storeReportDbCredentials", Map.of("username", username, "password", password), Void.class);
    }

    private <Res> Res invokeGetMethod(String apiMethod, Class<Res> responseClass) throws IOException {
        HttpGet request = new HttpGet(("https://%s/rhn/iss/sync/%s").formatted(remoteHost, apiMethod));
        request.setHeader("Authorization", "Bearer " + accessToken);

        HttpResponse response = httpClientAdapter.executeRequest(request);
        int statusCode = response.getStatusLine().getStatusCode();
        // Ensure we get a valid response
        if (statusCode != HttpStatus.SC_OK) {
            throw new IOException("Unexpected response code %d".formatted(statusCode));
        }

        // Parse the response object, if specified
        if (!Void.class.equals(responseClass)) {
            try (Reader responseReader = new InputStreamReader(response.getEntity().getContent())) {
                return GSON.fromJson(responseReader, responseClass);
            }
        }

        return null;
    }

    private <Req, Res> Res invokePostMethod(String apiMethod, Req requestObject, Class<Res> responseClass)
        throws IOException {
        HttpPost request = new HttpPost(("https://%s/rhn/iss/sync/%s").formatted(remoteHost, apiMethod));
        request.setHeader("Authorization", "Bearer " + accessToken);

        // Add the request object, if specified
        if (requestObject != null) {
            String body = GSON.toJson(requestObject, new TypeToken<>() { }.getType());
            request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
        }

        HttpResponse response = httpClientAdapter.executeRequest(request);
        int statusCode = response.getStatusLine().getStatusCode();
        // Ensure we get a valid response
        if (statusCode != HttpStatus.SC_OK) {
            throw new IOException("Unexpected response code %d".formatted(statusCode));
        }

        // Parse the response object, if specified
        if (!Void.class.equals(responseClass)) {
            try (Reader responseReader = new InputStreamReader(response.getEntity().getContent())) {
                return GSON.fromJson(responseReader, responseClass);
            }
        }

        return null;
    }
}
