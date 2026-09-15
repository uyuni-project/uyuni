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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Builder for Rest request.
 */
@SuppressWarnings("checkstyle:VisibilityModifier")
public class RestRequestBuilder {

    final RestRequestMethodEnum method;
    final String url;
    RestRequestAuthEnum requestAuth = RestRequestAuthEnum.NONE;
    Object body;
    Map<String, String> headers = new HashMap<>();
    Map<String, String> pathParams = new HashMap<>();
    Map<String, String> queryParams = new HashMap<>();
    String bearerToken;
    String basicUser;
    String basicPassword;

    /**
     * Constructor to create a RequestBuilder instance.
     *
     * @param methodIn the request method
     * @param urlIn           the URL
     */
    public RestRequestBuilder(RestRequestMethodEnum methodIn, String urlIn) {
        method = methodIn;
        url = urlIn;
    }

    /**
     * Sets the body of the request.
     *
     * @param bodyIn the body of the request
     * @return the RequestBuilder instance
     */
    public RestRequestBuilder body(Object bodyIn) {
        this.body = bodyIn;
        return this;
    }


    /**
     * Add a path parameter to the request.
     *
     * @param name  the name of the path parameter
     * @param value the value of the path parameter
     * @return the RequestBuilder instance
     */
    public RestRequestBuilder pathParam(String name, String value) {
        this.pathParams.put(name, value);
        return this;
    }

    /**
     * Add a header to the request.
     *
     * @param key   the key of the header
     * @param value the value of the header
     * @return the RequestBuilder instance
     */
    public RestRequestBuilder header(String key, String value) {
        headers.put(key, value);
        return this;
    }

    /**
     * Add a query parameter to the request.
     *
     * @param name  the name of the query parameter
     * @param value the value of the query parameter
     * @return the RequestBuilder instance
     */
    public RestRequestBuilder queryParam(String name, String value) {
        this.queryParams.put(name, value);
        return this;
    }

    /**
     * Sets the bearer token for the request.
     *
     * @param bearerTokenIn the bearer token
     * @return the RequestBuilder instance
     */
    public RestRequestBuilder bearerToken(String bearerTokenIn) {
        this.requestAuth = RestRequestAuthEnum.BEARER;
        this.bearerToken = bearerTokenIn;
        return this;
    }

    /**
     * Sets the basic authentication for the request.
     *
     * @param usernameIn the username
     * @param passwordIn the password
     * @return the RequestBuilder instance
     */
    public RestRequestBuilder basicAuth(String usernameIn, String passwordIn) {
        this.requestAuth = RestRequestAuthEnum.BASIC;
        this.basicUser = usernameIn;
        this.basicPassword = passwordIn;
        return this;
    }

    /**
     * Builds the request.
     *
     * @return the RestRequest
     */
    public RestRequest build() {
        return new RestRequest(this);
    }

    /**
     * Builds the URL with path and query parameters.
     *
     * @return the URL
     */
    public String buildUrl() {
        String finalUrl = url;
        for (Map.Entry<String, String> entry : pathParams.entrySet()) {
            finalUrl = finalUrl.replace(
                    "{" + entry.getKey() + "}",
                    URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8)
            );
        }

        if (!queryParams.isEmpty()) {
            StringBuilder queryBuilder = new StringBuilder();
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                queryBuilder
                        .append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8))
                        .append("=")
                        .append(URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                        .append("&");
            }
            queryBuilder.setLength(queryBuilder.length() - 1);
            finalUrl += (finalUrl.contains("?") ? "&" : "?") + queryBuilder;
        }

        return finalUrl;
    }


}
