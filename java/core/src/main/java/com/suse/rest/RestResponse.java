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

import com.suse.manager.api.ParseException;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.util.List;
import java.util.Map;

/**
 * Class to represent a response from a Rest request.
 */
public class RestResponse {
    private final int statusCode;
    private final Map<String, List<String>> headers;
    private final String body;

    /**
     * Constructor to create a Response instance.
     *
     * @param statusCodeIn the input status code
     * @param headersIn the input headers
     * @param bodyIn the input content
     */
    public RestResponse(int statusCodeIn, Map<String, List<String>> headersIn, String bodyIn) {
        statusCode = statusCodeIn;
        body = bodyIn;
        headers = headersIn;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public Map<String, List<String>> getHeaders() {
        return headers;
    }

    public String getBody() {
        return body;
    }

    /**
     * Parses the body content as a JSON element.
     *
     * @param type the type of the JSON element
     * @return the parsed JSON element
     * @param <T> the type of the JSON element
     * @throws ParseException if the body content is not a valid JSON
     */
    public <T> T getBodyAs(Class<T> type) throws ParseException {
        try {
            return new Gson().fromJson(body, type);
        }
        catch (JsonSyntaxException e) {
            throw new ParseException(e);
        }
    }

    @Override
    public String toString() {
        return "Response{" +
                "statusCode=" + statusCode +
                ", headers=" + headers +
                ", content='" + body + '\'' +
                '}';
    }

    public boolean isSuccessful() {
        return statusCode >= 200 && statusCode < 300;
    }
}
