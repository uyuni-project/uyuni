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
 *
 * Red Hat trademarks are not licensed under GPLv2. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */

package com.suse.rest;

import static com.suse.utils.Predicates.allProvided;

import com.redhat.rhn.common.util.http.HttpClientAdapter;

import com.google.gson.Gson;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Class to execute a REST request.
 * Designed to provide and accept JSON.
 */
public class RestClient {

    private static final RestClient INSTANCE = new RestClient();

    private RestClient() {
    }

    /**
     * Gets the single instance of SCCRequestFactory.
     *
     * @return single instance of SCCRequestFactory
     */
    public static RestClient getInstance() {
        return INSTANCE;
    }


    /**
     * Executes a REST request.
     *
     * @param restRequest the request to execute
     * @return the response
     */
    public RestResponse execute(RestRequest restRequest) {
        try {
            // Prep the request
            HttpRequestBase request = null;
            switch (restRequest.getMethod()) {
                case GET:
                    request = new HttpGet(restRequest.getUrl());
                    break;
                case POST:
                    request = new HttpPost(restRequest.getUrl());
                    break;
                case PUT:
                    request = new HttpPut(restRequest.getUrl());
                    break;
                case DELETE:
                    request = new HttpDelete(restRequest.getUrl());
                    break;
                case PATCH:
                    request = new HttpPatch(restRequest.getUrl());
                    break;
                default:
                    throw new RestClientException("HTTP method not supported: " + restRequest.getMethod());
            }

            HttpClientAdapter httpClient = new HttpClientAdapter();

            request.addHeader("Content-Type", "application/json");
            request.addHeader("Accept", "application/json");


            if (restRequest.getRequestAuthType() == RestRequestAuthEnum.BEARER) {
                request.addHeader("Authorization", "Bearer " + restRequest.getBearerToken());
            }
            else if (restRequest.getRequestAuthType() == RestRequestAuthEnum.BASIC) {
                String basicAuth = "Basic " + Base64.getEncoder().encodeToString((restRequest.getBasicUser() + ":" +
                        restRequest.getBasicPassword()).getBytes());
                request.setHeader("Authorization", basicAuth);
            }

            if (request instanceof HttpEntityEnclosingRequestBase httpEntityEnclosingRequestBase) {
                Object body = restRequest.getBody();
                if (body != null) {
                    String jsonBody = new Gson().toJson(body);
                    httpEntityEnclosingRequestBase.setEntity(
                            new StringEntity(jsonBody, StandardCharsets.UTF_8)
                    );
                }
            }

            // Execute request
            HttpResponse response = httpClient.executeRequest(request);

            // Handle the response
            int responseCode = response.getStatusLine().getStatusCode();

            String body = null;
            if (allProvided(response, response.getEntity())) {
                body = EntityUtils.toString(response.getEntity());
            }

            Map<String, List<String>> responseHeaders = new HashMap<>();
            for (Header header : response.getAllHeaders()) {
                String headerName = header.getName();
                String headerValue = header.getValue();
                responseHeaders
                        .computeIfAbsent(headerName, k -> new ArrayList<>())
                        .add(headerValue);
            }

            return new RestResponse(
                    responseCode,
                    responseHeaders,
                    body
            );
        }
        catch (IOException e) {
            throw new RestClientException(e);
        }
    }

}
