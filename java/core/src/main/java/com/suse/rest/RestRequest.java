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

package com.suse.rest;

import java.util.Map;

/**
 * Class to represent a generic REST request.
 */
public class RestRequest {

    private final RestRequestMethodEnum method;
    private final String url;
    private final RestRequestAuthEnum requestAuthType;
    private final Object body;
    private final Map<String, String> headers;
    private final Map<String, String> pathParams;
    private final Map<String, String> queryParams;
    private final String bearerToken;
    private final String basicUser;
    private final String basicPassword;

    /**
     * Constructor to create a Request instance.
     *
     * @param builder the input builder
     */
    public RestRequest(RestRequestBuilder builder) {
        this.method = builder.method;
        this.url = builder.url;
        this.requestAuthType = builder.requestAuth;
        this.body = builder.body;
        this.headers = builder.headers;
        this.pathParams = builder.pathParams;
        this.queryParams = builder.queryParams;
        this.bearerToken = builder.bearerToken;
        this.basicUser = builder.basicUser;
        this.basicPassword = builder.basicPassword;
    }

    public RestRequestMethodEnum getMethod() {
        return method;
    }

    public String getUrl() {
        return url;
    }

    public RestRequestAuthEnum getRequestAuthType() {
        return requestAuthType;
    }

    public Object getBody() {
        return body;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public Map<String, String> getPathParams() {
        return pathParams;
    }

    public Map<String, String> getQueryParams() {
        return queryParams;
    }

    public String getBearerToken() {
        return bearerToken;
    }

    public String getBasicUser() {
        return basicUser;
    }

    public String getBasicPassword() {
        return basicPassword;
    }
}
