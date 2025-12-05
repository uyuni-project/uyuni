/*
 * Copyright (c) 2022 SUSE LLC
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
package com.suse.manager.api;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Helper class to parse arguments in an HTTP API request
 * TODO: Test
 */
public class ApiRequestParser {
    private final Gson gson;

    /**
     * Constructs a parser with a {@link Gson} instance
     * @param gsonIn the {@link Gson} instance
     */
    public ApiRequestParser(Gson gsonIn) {
        this.gson = gsonIn;
    }

    /**
     * Parses an API request body as a JSON object into a map of property names to {@link JsonElement}s
     *
     * The body must be in the form of a top-level JSON object.
     * @param body the request body
     * @return the JSON object properties as key-value pairs
     */
    public Map<String, JsonElement> parseBody(String body) throws ParseException {
        JsonElement bodyElement = JsonParser.parseString(body);

        if (bodyElement.isJsonNull()) {
            return Collections.emptyMap();
        }

        // JSON body is expected to be in the following form:
        // { <param>: <value>[, <param>: <value>] }
        if (!bodyElement.isJsonObject()) {
            throw new ParseException("JSON body must be an object");
        }

        return bodyElement.getAsJsonObject().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }

    /**
     * Parses query string parameters as {@link JsonElement}s
     *
     * Single- and multi-value arguments are accepted.
     * @param queryParams the query string map
     * @return the map of parameter names to {@link JsonElement}s
     */
    public Map<String, JsonElement> parseQueryParams(Map<String, String[]> queryParams) throws ParseException {
        Map<String, JsonElement> parsedParams = new HashMap<>(queryParams.size());

        for (Map.Entry<String, String[]> param : queryParams.entrySet()) {
            JsonElement parsedValue;
            if (param.getValue().length == 1) {
                // Single value
                parsedValue = parseQueryParam(param.getValue()[0]);
            }
            else {
                // Multiple values
                JsonArray jsonArray = new JsonArray(param.getValue().length);
                for (String val : param.getValue()) {
                    jsonArray.add(parseQueryParam(val));
                }
                parsedValue = jsonArray;
            }
            parsedParams.put(param.getKey(), parsedValue);
        }
        return parsedParams;
    }

    /**
     * Parses a single {@link JsonElement} into the desired type
     * @param param the parameter
     * @param type the class of the desired type to parse the value into
     * @param <T> the type of the parameter value
     * @return the parsed value
     * @throws ParseException when a parsing error occurs
     */
    public <T> T parseValue(JsonElement param, Class<T> type) throws ParseException {
        try {
            return gson.fromJson(param, type);
        }
        catch (JsonSyntaxException e) {
            throw new ParseException(e);
        }
    }

    private JsonElement parseQueryParam(String value) throws ParseException {
        JsonElement element;
        try {
            // Try to parse the literal value
            element = JsonParser.parseString(value);
        }
        catch (JsonSyntaxException e) {
            // Invalid syntax, try as string
            element = JsonParser.parseString('"' + value + '"');
        }

        if (element.isJsonPrimitive() || element.isJsonNull()) {
            // Only allow primitives
            return element;
        }

        throw new ParseException("Complex types are not allowed in query string");
    }
}
