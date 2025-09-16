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

package com.suse.manager.xmlrpc.iss;

import com.redhat.rhn.common.util.http.HttpClientAdapter;

import com.suse.manager.hub.HubExternalClient;
import com.suse.manager.webui.controllers.ECMAScriptDateAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.security.cert.Certificate;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * HTTP Client for the Hub Inter-Server-Sync External-facing APIs
 */
public class RestHubExternalClient implements HubExternalClient {

    private static final Gson GSON = new GsonBuilder()
        .registerTypeAdapter(Date.class, new ECMAScriptDateAdapter())
        .serializeNulls()
        .create();

    private final String remoteHost;

    private final HttpClientAdapter httpClientAdapter;

    private Cookie sessionCookie;

    /**
     * Builds an instance that connects to the given host using the given username/password combination.
     * @param remoteHostIn the remote host
     * @param username the username
     * @param password the password
     * @param rootCA the root certificate, if needed to establish a secure connection
     * @throws IOException when a failure happens while establishing the connection
     */
    public RestHubExternalClient(String remoteHostIn, String username, String password, Optional<Certificate> rootCA)
        throws IOException {
        List<Certificate> maybeRootCAs = rootCA.stream().toList();

        this.remoteHost = remoteHostIn;
        this.httpClientAdapter = new HttpClientAdapter(maybeRootCAs, true);
        this.sessionCookie = null;

        login(username, password);
    }

    @Override
    public String generateAccessToken(String fqdn) throws IOException {
        HttpPost request = createPostRequest("sync.hub", "generateAccessToken", Map.of("fqdn", fqdn));
        HttpResponse response = httpClientAdapter.executeRequest(request);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            throw new IOException("Unexpected response code %d".formatted(statusCode));
        }

        String body = EntityUtils.toString(response.getEntity());
        Map<String, Object> responseMap = GSON.fromJson(body, new TypeToken<Map<String, Object>>() { }.getType());

        Object result = responseMap.get("result");
        if (!(result instanceof String token)) {
            throw new IOException("Unexpected response in JSON object %s".formatted(result));
        }

        return token;
    }

    @Override
    public void storeAccessToken(String fqdn, String token) throws IOException {
        HttpPost request = createPostRequest("sync.hub", "storeAccessToken", Map.of("fqdn", fqdn, "token", token));
        HttpResponse response = httpClientAdapter.executeRequest(request);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            throw new IOException("Unexpected response code %d".formatted(statusCode));
        }
    }

    private void login(String username, String password) throws IOException {
        try {
            HttpPost request = createPostRequest("auth", "login", Map.of("login", username, "password", password));
            HttpResponse response = httpClientAdapter.executeRequest(request, username, password);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK) {
                throw new IOException("Unexpected response code %d".formatted(statusCode));
            }

            List<Cookie> ptxSessionCookie = httpClientAdapter.getCookies("pxt-session-cookie");
            if (ptxSessionCookie.size() != 1) {
                throw new IOException("One and only one ptx-session-cookie is expected");
            }

            sessionCookie = ptxSessionCookie.get(0);
        }
        catch (IOException e) {
            sessionCookie = null;
            throw e;
        }
    }

    private void logout() throws IOException {
        if (sessionCookie == null) {
            return;
        }

        HttpGet request = createGetRequest("auth", "logout");
        HttpResponse response = httpClientAdapter.executeRequest(request);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            throw new IOException("Unexpected response code %d".formatted(statusCode));
        }

        sessionCookie = null;
    }

    @Override
    public void close() throws IOException {
        logout();
    }

    private HttpPost createPostRequest(String namespace, String method, Map<String, Object> paramtersMap) {
        String url = "https://%s/rhn/manager/api/%s/%s".formatted(remoteHost, namespace.replace(".", "/"), method);
        HttpPost request = new HttpPost(url);

        if (!paramtersMap.isEmpty()) {
            String jsonBody = GSON.toJson(paramtersMap, new TypeToken<Map<String, Object>>() { }.getType());
            request.setEntity(new StringEntity(jsonBody, ContentType.APPLICATION_JSON));
        }

        return request;
    }

    private HttpGet createGetRequest(String namespace, String method) {
        String url = "https://%s/rhn/manager/api/%s/%s".formatted(remoteHost, namespace.replace(".", "/"), method);
        return new HttpGet(url);
    }
}
