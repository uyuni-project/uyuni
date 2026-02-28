/*
 * Copyright (c) 2009--2014 Red Hat, Inc.
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

package com.redhat.rhn.common.util;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.utils.URIBuilder;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * A simple class that assists with Servlet-related activities
 */
public class ServletUtils {
    /** utility class */
    private ServletUtils() { }

    /** util function to take a servlet request and compute the path
     * relative to the server (not relative to the webapp).  needed
     * when getPath() is relative to the webapp instead of the server
     * @param req The request to inspect
     * @return The path requested.
     */
    public static String getRequestPath(HttpServletRequest req) {
        try {
            String requestUri = (String) req.getAttribute(RequestDispatcher.FORWARD_REQUEST_URI);
            if (StringUtils.isBlank(requestUri)) {
                requestUri = new URL(req.getRequestURL().toString()).getPath();
            }
            return requestUri;
        }
        catch (Exception e) {
            throw new IllegalArgumentException("Unable to parse url: " + req.getRequestURL());
        }
    }

    /**
     * Create a URL with parameters tacked on.  Used in redirects and
     * programmatic URL creation with parameters.  Handles URL encoding of
     * incoming keys and values as well.
     *
     * @param base baseUrl to compute path from
     * @param params params to fillout on the URL
     * @return String path
     */
    @SuppressWarnings("unchecked")
    public static String pathWithParams(String base, Map<String, Object> params) {
        StringBuilder ret = new StringBuilder(base);
        boolean firstPass = true;

        // let's bail if there's no params
        if (params == null) {
            return ret.toString();
        }

        for (Map.Entry<String, Object> me : params.entrySet()) {
            List<Object> values;

            // No guarantee of receiving strings here, use toString() instead of casts:
            if (me.getValue() == null) {
                values = List.of();
            }
            else if (me.getValue() instanceof Object[] paramValues) {
                values = Arrays.stream(paramValues)
                        .map(o -> StringUtil.urlEncode(String.valueOf(o)))
                        .collect(Collectors.toList());
            }
            else if (me.getValue() instanceof List) {
                List<Object> paramValues = (List<Object>) me.getValue();
                values = paramValues.stream()
                        .map(o -> StringUtil.urlEncode(String.valueOf(o)))
                        .collect(Collectors.toList());
            }
            else {
                String paramValue = me.getValue().toString();
                values = List.of(StringUtil.urlEncode(paramValue));
            }

            List<String> allValues = values.stream()
                    .map(v -> StringUtil.urlEncode(me.getKey()) + "=" + v)
                    .collect(Collectors.toList());

            if (!allValues.isEmpty()) {
                if (firstPass) {
                    ret.append("?");
                    firstPass = false;
                }
                else {
                    ret.append("&");
                }
                ret.append(String.join("&", allValues));
            }
        } //while

        return ret.toString();
    }

    /**
     * Creates a encoded URL query string with the parameters from the given request. If the
     * request is a GET, then the returned query string will simply consist of the query
     * string from the request. If the request is a POST, the returned query string will
     * consist of the form variables.
     * <p>
     * <strong>Note</strong>: This method does not support multi-value parameters.
     *
     * @param request The request for which the query string will be generated.
     *
     * @return An encoded URL query string with the parameters from the given request.
     */
    public static String requestParamsToQueryString(ServletRequest request) {

        StringBuffer queryString = new StringBuffer();

        String paramName;
        String paramValue;

        Enumeration<String> paramNames = request.getParameterNames();

        while (paramNames.hasMoreElements()) {
            paramName = paramNames.nextElement();
            paramValue = request.getParameter(paramName);

            queryString.append(encode(paramName)).append("=").append(encode(paramValue))
                    .append("&");
        }

        if (endsWith(queryString, '&')) {
            queryString.deleteCharAt(queryString.length() - 1);
        }

        return queryString.toString();
    }

    /**
     * Encodes the specified string with a UTF-8 encoding.
     *
     * @param string The String to encode.
     *
     * @return The encoded String.
     */
    public static String encode(String string) {
        return URLEncoder.encode(string, StandardCharsets.UTF_8);
    }

    private static boolean endsWith(StringBuffer buffer, char c) {
        if (buffer.length() == 0) {
            return false;
        }

        return buffer.charAt(buffer.length() - 1) == c;
    }

    /**
     * Reconstructs the request URL without the query string.
     * This is used for SAML signature validation where the URL must match
     * the public-facing address exactly, regardless of internal proxy routing.
     * @param request the {@link HttpServletRequest servlet request}
     * @return the public request URL without the query string
     */
    public static String getAbsoluteRequestUrl(HttpServletRequest request) {
        StringBuilder url = new StringBuilder();

        String scheme = request.getScheme();
        String serverName = request.getServerName();
        int port = request.getServerPort();

        url.append(scheme)
                .append("://")
                .append(serverName);

        if (port != 80 && port != 443 && port != 0) {
            url.append(":").append(port);
        }

        String requestUri = request.getRequestURI();
        if (requestUri != null && !requestUri.isEmpty()) {
            url.append(requestUri);
        }

        return url.toString();
    }

    public static String sendRedirect(HttpServletResponse response, String location, Map<String, String> parameters)
            throws IOException, URISyntaxException {
        if (MapUtils.isEmpty(parameters)) {
            response.sendRedirect(location);
            return location;
        }

        URIBuilder uriBuilder = new URIBuilder(location);
        // Ensure parameters with an empty value are translated to ?param and not to ?param=
        parameters.forEach((name, value) -> uriBuilder.addParameter(name, StringUtils.defaultIfEmpty(value, null)));

        String target = uriBuilder.build().toString();
        response.sendRedirect(target);
        return target;
    }
}
