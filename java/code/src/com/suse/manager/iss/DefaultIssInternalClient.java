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

package com.suse.manager.iss;

import com.redhat.rhn.common.util.http.HttpClientAdapter;
import com.redhat.rhn.domain.iss.IssRole;

import com.suse.manager.webui.controllers.ECMAScriptDateAdapter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import java.io.IOException;
import java.security.cert.Certificate;
import java.util.Date;
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
    public void register(IssRole role, String token, String rootCA) throws IOException {
        HttpPost request = createPostRequest("register");

        RegisterJson requestObject = new RegisterJson(role, token, rootCA);
        String body = GSON.toJson(requestObject, new TypeToken<RegisterJson>() { }.getType());
        request.setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));

        HttpResponse response = httpClientAdapter.executeRequest(request);
        int statusCode = response.getStatusLine().getStatusCode();
        if (statusCode != HttpStatus.SC_OK) {
            throw new IOException("Unexpected response code %d".formatted(statusCode));
        }
    }

    private HttpPost createPostRequest(String apiMethod) {
        HttpPost request = new HttpPost(("https://%s/rhn/iss/sync/%s").formatted(remoteHost, apiMethod));
        request.setHeader("Authorization", "Bearer " + this.accessToken);
        return request;
    }
}
